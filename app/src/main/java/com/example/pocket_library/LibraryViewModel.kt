package com.example.pocket_library

import android.content.res.Resources
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
        val requireDatabaseSync: Boolean = false,
        val favouriteBooks: MutableList<Book> = mutableListOf(),
        val searchParameters: Search = Search(),
        val searchResults: List<Book> = emptyList()
    )
    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state

    private var search = MutableStateFlow<Search>(Search())

    fun setSearchTitle(t:String){
        setSearch(search.value.copy(title=t))
    }
    fun setSearchAuthor(a:String){
        setSearch(search.value.copy(author = a))
    }
    fun setSearchYear(y:Year_type){
        setSearch(search.value.copy(year = y))
    }
    fun setSearch(s:Search){
        search.value = s
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
    fun addFavourite(b:Book){
        val favouriteBooks = state.value.favouriteBooks
        favouriteBooks.add(b)
        _state.value = state.value.copy(favouriteBooks=favouriteBooks, requireDatabaseSync = true)
    }
    fun removeFavourite(id:Id_type){
        //pass removal job to database
        return
    }

    /**
     * @throws IllegalArgumentException if the id of updated book is not present in favourites list
     */
    @Throws(IllegalArgumentException::class)
    fun changeFavourite(u:Book){
        val favouriteBooks = state.value.favouriteBooks
        val idx = favouriteBooks.indexOfFirst { it.id == u.id }
        require(idx != -1){"no book of id ${u.id} in favourites list"}
        favouriteBooks.removeAt(idx)
        favouriteBooks.add(idx,u)
        _state.value = state.value.copy(favouriteBooks = favouriteBooks, requireDatabaseSync = true)
    }

    /**
     * @throws NoSuchElementException if id is not in favourites
     * @throws IllegalStateException if camera fails to respond
     */
    @Throws(IllegalStateException::class, NoSuchElementException::class)
    fun addPersonalPhoto(id:Id_type){
        val book:Book = state.value.favouriteBooks.first{it.id == id}   //throws exception
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
    fun shareBook(b:Book){
        //TODO do API call to share book with contacts
        return
    }




    init {
        viewModelScope.launch { state.collect { stateValue ->
            if(stateValue.requireDatabaseSync){
                syncFavouriteBooks()
                _state.value = state.value.copy(requireDatabaseSync = false)
            }
        } }
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
        //TODO replace with actual API call
        return null
    }
    private fun syncFavouriteBooks(){
        Log.d("VM - database syncing","Syncing favourite books with database")
        //TODO
        return
    }

    private fun getBookFromSearchResults(id:Id_type):Book?{
        Log.d("VM - lookup","searching for book of id $id in search results")
        //TODO
        return null
    }
    private fun getBookFromDataBase(id:Id_type):Book?{
        Log.d("VM - lookup","searching for book of id $id in database")
        //TODO
        return null
    }
}