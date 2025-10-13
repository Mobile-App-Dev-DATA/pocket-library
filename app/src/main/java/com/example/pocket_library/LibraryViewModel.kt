package com.example.pocket_library

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
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
    data class State(
        val favourites:List<Books> = emptyList(),
        val search:Search = Search(),
        val searchResults:List<Books> = emptyList()
    )

    val _state = MutableStateFlow<State>(State())
    val state : StateFlow<State> = _state

    init {
        viewModelScope.launch { state.collect {
            //TODO preform sync
        } }
    }

    fun setSearchTitle(t:String){
        setSearch(state.value.search.copy(title=t))
    }
    fun setSearchAuthor(a:String){
        setSearch(state.value.search.copy(author = a))
    }
    fun setSearchYear(y:Year_type){
        setSearch(state.value.search.copy(year = y))
    }
    fun setSearch(s:Search){
        _state.value = state.value.copy(search = s)
    }

    /**
     * @throws NoSuchElementException if book of id cannot be found in search results or on database
     */
    @Throws(NoSuchElementException::class)
    fun addFavourite(id:Id_type){
        var book = getBookFromSearchResults(id)
        if (book == null){
            book = getBookFromDataBase(id)
        }
        if (book == null){
            throw NoSuchElementException("book of id $id was not found in search results or database")
        }else{
            addFavourite(book)
        }
    }

    fun addFavourite(b:Books){
        if(state.value.favourites.indexOfFirst { it.id == b.id } == -1){
            _state.value = state.value.copy(favourites = state.value.favourites + b)
        }
    }
    fun removeFavourite(id:Id_type){
        val idx = state.value.favourites.indexOfFirst { it.id == id }
        if (idx != -1){
            _state.value = state.value.copy(favourites = state.value.favourites.filterNot{it.id == id})
        }
    }

    /**
     * @throws IllegalArgumentException if the id of updated book is not present in favourites list
     */
    @Throws(IllegalArgumentException::class)
    fun changeFavourite(u:Books){
        val idx = state.value.favourites.indexOfFirst { it.id == u.id }
        require(idx != -1){"cannot update favourite that does not exist"}
        val favourites = state.value.favourites.filterNot { it.id == u.id }
        _state.value = state.value.copy(favourites=favourites+u)
    }

    /**
     * @throws IllegalArgumentException if id is not in favourites
     * @throws IllegalStateException if camera fails to respond
     */
    @Throws(IllegalStateException::class, IllegalArgumentException::class)
    fun addPersonalPhoto(id:Id_type){
        val book = getBookFromFavouritesById(id)
        val photo = takePhoto()
        changeFavourite(book.copy(myPicture = photo))
    }

    /**
     * @throws NoSuchElementException if id is not present in search or database
     */
    @Throws(NoSuchElementException::class)
    fun shareBook(id:Id_type){
        var book = getBookFromSearchResults(id)
        if (book == null){
            book = getBookFromDataBase(id)
        }
        if (book == null){
            throw NoSuchElementException("book of id $id was not found in search results or database")
        }else{
            shareBook(book)
        }
    }
    fun shareBook(b:Books){
        //TODO do API call to share book with contacts
        return
    }

    /**
     * @throws IllegalArgumentException if book was not present in argument
     */
    @Throws(IllegalStateException::class)
    private fun getBookFromFavouritesById(id:Id_type):Books{
        val idx = state.value.favourites.indexOfFirst { it.id == id }
        require(idx != -1){"cannot get book that is not in favourites"}
        return state.value.favourites[idx]
    }
    /**
     * @throws IllegalArgumentException if book was not present in argument
     */
    @Throws(IllegalStateException::class)
    private fun getBookFromSearchResultsById(id:Id_type):Books{
        val idx = state.value.searchResults.indexOfFirst { it.id == id }
        require(idx != -1){"cannot get book that is not in favourites"}
        return state.value.searchResults[idx]
    }
    /**
     * @throws IllegalArgumentException if book was not present in argument
     */
    @Throws(IllegalStateException::class)
    private fun getBookFromDataBaseById(id:Id_type):Books{
        //TODO
        return state.value.searchResults[0]
    }
    /**
     * @throws IllegalArgumentException if book was not present in argument
     */
    @Throws(IllegalStateException::class)
    private fun getBookById(id:Id_type):Books{
        try{
            return getBookFromFavouritesById(id)
        }catch(e: IllegalArgumentException){}
        try{
            return getBookFromSearchResultsById(id)
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
    private fun getBookFromSearchResults(id:Id_type):Books?{
        Log.d("VM - lookup","searching for book of id $id in search results")
        //TODO implement API call
        return null
    }
    private fun getBookFromDataBase(id:Id_type):Books?{
        Log.d("VM - lookup","searching for book of id $id in database")
        //TODO implement API call
        return null
    }
    private fun doSearch():List<Books>{
        val searchVal = search.value
        //TODO implement API call
        return emptyList()
    }
}