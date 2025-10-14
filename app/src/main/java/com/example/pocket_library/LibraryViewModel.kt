package com.example.pocket_library

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.NoSuchElementException

data class Book(
    val id: Id_type,
    val title: String,
    val author: String,
    val year: Year_type,
    val coverImg: Img_type?,
    val myPicture: Img_type?
)

data class Search(
    val title: String = "",
    val author: String = "",
    val year: Year_type? = null
)
class LibraryViewModel : ViewModel() {
    class FavouriteList : ArrayList<Book>() {
        var syncJob : Job? = null
        val mutex = Mutex()

        fun isLoading():Boolean{
            return !(syncJob?.isCompleted ?: true)
        }
    }
    val _favourites = MutableStateFlow<FavouriteList>(FavouriteList())
    val favourites : StateFlow<List<Book>> = _favourites

    var search_job:Job? = null
    val search_mutex = Mutex()
    val _search = MutableStateFlow<Search>(Search())
    val search : StateFlow<Search> = _search

    val _searchResults = MutableStateFlow<List<Book>>(emptyList())
    val searchResults : StateFlow<List<Book>> = _searchResults

    init {
        _favourites = loadFavourites()
    }

    init {
        viewModelScope.launch {
            favourites.collect {
                _favourites.value.mutex.withLock {
                    _favourites.value.syncJob?.cancel()
                    _favourites.value.syncJob = viewModelScope.launch {
                        viewModelScope.launch{
                            //TODO:sync favourites with local data base
                        }
                        Thread.sleep(5000)
                        viewModelScope.launch{
                            //TODO:sync local database with internet
                        }
                    }
                }
            }
        }
        viewModelScope.launch {
            search.collect {
                search_mutex.withLock {
                    search_job?.cancel()
                    search_job = viewModelScope.launch {
                        Thread.sleep(300)
                        viewModelScope.launch {
                            //TODO: get results from database
                        }
                    }
                }
            }
        }
    }

    fun setSearch(title:String? = null, author:String? = null, year: Year_type? = null){
        val s = _search.value
        setSearch(_search.value.copy(title=title?:s.title,author=author?:s.author,year=year?:s.year))
    }
    fun setSearch(s:Search){
        _search.value = s
    }

    fun addFavourite(b:Book){
        if(favourites.value.indexOfFirst { it.id == b.id } == -1){
            _favourites.value = (_favourites.value + b) as FavouriteList
        }
    }

    fun removeFavourite(id:Id_type){
        val idx = favourites.value.indexOfFirst { it.id == id }
        if (idx != -1){
            _favourites.value = _favourites.value.filterNot{it.id == id} as FavouriteList
        }
    }

    /**
     * @throws IllegalArgumentException if the id of updated book is not present in favourites list
     */
    @Throws(IllegalArgumentException::class)
    fun changeFavourite(u:Book){
        val idx = state.value.favourites.indexOfFirst { it.id == u.id }
        require(idx != -1){"cannot update favourite that does not exist"}
        val favourites = state.value.favourites.filterNot { it.id == u.id }
        _state.value = state.value.copy(favourites=favourites+u)
    }

    fun addOrChangeFavourite(b:Book){
        try{
            changeFavourite(b)
        }catch (e: IllegalArgumentException){
            addFavourite(b)
        }
    }

    /**
     * @throws IllegalArgumentException if id is not in favourites
     * @throws IllegalStateException if camera fails to respond
     */
    @Throws(IllegalStateException::class, IllegalArgumentException::class)
    fun addPersonalPhoto(id:Id_type){
        val book = getBookFromFavourites(id)
        val photo = takePhoto()
        changeFavourite(book.copy(myPicture = photo))
    }

    /**
     * @throws NoSuchElementException if id is not present in search or database
     */
    @Throws(NoSuchElementException::class)
    fun shareBook(id:Id_type){
        var book = getBookFromFavourites(id)
        if (book == null){
            book = getBookFromSearchResults(id)
        }
        if (book == null){
            book = getBookFromDataBase(id)
        }
        if (book == null){
            throw NoSuchElementException("book of id $id was not found in search results or database")
        }else{
            shareBook(book)
        }
    }
    fun shareBook(b:Book){
        //TODO do API call to share book with contacts
        return
    }

    /**
     * @throws IllegalArgumentException if book was not present in argument
     */
    @Throws(IllegalStateException::class)
    private fun getBookFromFavourites(b:Book):Book{
        return getBookFromFavourites(b.id)
    }
    /**
     * @throws IllegalArgumentException if book was not present in argument
     */
    @Throws(IllegalStateException::class)
    private fun getBookFromFavourites(id:Id_type):Book{
        val idx = state.value.favourites.indexOfFirst { it.id == id }
        require(idx != -1){"cannot get book that is not in favourites"}
        return state.value.favourites[idx]
    }
    /**
     * @throws IllegalArgumentException if book was not present in argument
     */
    @Throws(IllegalStateException::class)
    private fun getBookFromSearchResults(id:Id_type):Book{
        val idx = state.value.searchResults.indexOfFirst { it.id == id }
        require(idx != -1){"cannot get book that is not in favourites"}
        return state.value.searchResults[idx]
    }
    /**
     * @throws IllegalArgumentException if book was not present in argument
     */
    @Throws(IllegalStateException::class)
    private fun getBookFromDataBaseById(id:Id_type):Book{
        //TODO
    }
    /**
     * @throws IllegalArgumentException if book was not present in argument
     */
    @Throws(IllegalStateException::class)
    private fun getBookById(id:Id_type):Book{
        try{
            return getBookFromFavourites(id)
        }catch(e: IllegalArgumentException){}
        try{
            return getBookFromSearchResults(id)
        }catch(e: IllegalArgumentException){}
        try{
            return getBookFromDataBaseById(id)
        }catch(e: IllegalArgumentException){
            throw IllegalArgumentException("could not find book of id $id in favourites, search results or database")
        }
    }
    @Throws(IllegalStateException::class)
    private fun takePhoto():Img_type{
        var image:Img_type? = null
        try {
            image = getPhotoFromAPI()
        }catch(e: Exception){
            throw IllegalStateException("an exception occurred while taking photo",e)
        }
        checkNotNull(image){"photo was not taken.  reason is unknown"}
        return image
    }
    private fun getPhotoFromAPI():Img_type?{
        //TODO implement API call
        return null
    }
    private fun loadFavourites():List<Book>{
        //TODO
    }
    private fun syncFavouriteBooks(){
        Log.d("VM - database syncing","Syncing favourite books with database")
        syncFavouriteBooksLocal()
        syncFavouriteBooksInternet()
    }
    private fun syncFavouriteBooksLocal(){
        //TODO implement API call
    }
    private fun syncFavouriteBooksInternet(){
        //TODO implement API call
    }
    private fun getBookFromDataBase(id:Id_type):Book?{
        Log.d("VM - lookup","searching for book of id $id in database")
        //TODO implement API call
        return null
    }
}