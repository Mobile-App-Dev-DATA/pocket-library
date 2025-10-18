package com.example.pocket_library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.math.min

class LibraryViewModel : ViewModel() {
    private class InternetSynchronizer(val vm : LibraryViewModel){
        val minNoInternetWaitTimeMills : Long = 1
        val maxNoInternetWaitTimeMills : Long = 10000
        private var synchronizationJob : Job? = null

        enum class SYNCJOB{
            NONE,UPLOAD,DOWNLOAD,DUPLEXSYNC
        }
        private var _currentJob : SYNCJOB = SYNCJOB.NONE
        fun getCurrentJob(): SYNCJOB{return _currentJob}
        fun isSyncing(): Boolean {return getCurrentJob() != SYNCJOB.NONE }

        fun stop(){
            synchronizationJob?.cancel()
            synchronizationJob = null
            _currentJob = SYNCJOB.NONE
        }

        fun upload(){
            if(_currentJob == SYNCJOB.DUPLEXSYNC){return}//DUPLEXSYNC already preforms an upload
            if(_currentJob == SYNCJOB.DOWNLOAD){
                // download does not account for any new favourites added.  we must therefore preform the upload once the download completes
                _currentJob = SYNCJOB.DUPLEXSYNC
                synchronizationJob?.invokeOnCompletion{
                    _currentJob = SYNCJOB.UPLOAD
                    upload()
                }
            } else {
                stop()
                _currentJob = SYNCJOB.UPLOAD
                synchronizationJob = vm.viewModelScope.launch {
                    val localFaves: List<Book> = vm.state.value.favList
                    vm.viewModelScope.launch {
                        API.setFavouritesLocal(localFaves)
                    }
                        .join()        // this ensures that the API call runs synchronously with this function, but if this function is canceled
                    // it will not effect the API call
                    var updatePushed: Boolean = false
                    var waitTimeMills: Long = minNoInternetWaitTimeMills
                    while (!updatePushed) {
                        vm.viewModelScope.launch {
                            try {
                                API.setFavouritesInternet(localFaves)
                                updatePushed = true
                            } catch (e: NoInternetException) {
                                // reattempt, as while loop is not broken.  new job is launched as this will break loop
                                // if sync job is canceled
                                Thread.sleep(waitTimeMills)
                                waitTimeMills *= 2
                                waitTimeMills = min(waitTimeMills, maxNoInternetWaitTimeMills)
                            }
                        }.join()
                    }
                    _currentJob = SYNCJOB.NONE
                }
            }
        }
        fun download(){
            if (isSyncing()){return}  // to ensure slow connections still work, download will not overwrite self.  also, download is redundant after upload
            _currentJob = SYNCJOB.DOWNLOAD
            synchronizationJob = vm.viewModelScope.launch {
                var favList : List<Book> = vm.state.value.favList
                var updatePulled : Boolean = false
                var waitTimeMills : Long = minNoInternetWaitTimeMills
                while(!updatePulled){
                    vm.viewModelScope.launch {
                        try {
                            val listOnFile = API.getFavouritesInternet()
                            favList = (favList+listOnFile).toSet().toList()
                            updatePulled = true
                        }catch(e : NoInternetException){
                            Thread.sleep(waitTimeMills)
                            waitTimeMills *= 2
                            waitTimeMills = min(waitTimeMills, maxNoInternetWaitTimeMills)
                        }
                    }.join()
                }
                vm.viewModelScope.launch {
                    API.setFavouritesLocal(favList)
                }.join()
                vm._state.value = vm.state.value.copy(favList = favList)
                _currentJob = SYNCJOB.NONE
            }
        }
    }
    private val internetSynchronizer = InternetSynchronizer(this)

    // FIXED: State declared ONCE with all fields
    data class State(
        val favList : List<Book> = emptyList(),
        val search : Search = Search(),
        val searchResults : List<Book> = emptyList(),
        val searchScrollIndex: Int = 0,
        val libraryScrollIndex: Int = 0
    ){
        data class Search(
            val title: String = "",
            val author: String = "",
            val year: Year_type? = null
        )
    }

    private val _state = MutableStateFlow(State())
    val state : StateFlow<State> = _state

    init {
        internetSynchronizer.download()
    }

    fun setSearch(title : String? = null, author : String? = null, year : Year_type? = null){
        val oldSearch = state.value.search
        _state.value = state.value.copy(search = state.value.search.copy(title = title ?: oldSearch.title, author = author ?: oldSearch.author, year = year ?: oldSearch.year))
    }

    fun performSearch() {
        viewModelScope.launch {
            try {
                val results = API.getBooksMatchingSearch(
                    title = state.value.search.title,
                    author = state.value.search.author,
                    year = state.value.search.year ?: ""
                )
                _state.value = state.value.copy(searchResults = results)
            } catch (e: NoInternetException) {
                _state.value = state.value.copy(searchResults = emptyList())
            }
        }
    }

    fun addFavourite(book : Book):Unit {
        if(book !in state.value.favList) {
            updateFavList(state.value.favList + book)
        }
    }

    @Throws(IllegalStateException::class)
    fun removeFavourite(book : Book):Unit {
        check(API.isConnectedToInternet()){"Favourites cannot be removed while offline"}
        if(state.value.favList.any{it.id == book.id}){
            updateFavList(state.value.favList.filterNot { it.id == book.id })
        }
    }

    fun addPersonalPhotoToFavourite(book : Book) {
        val idx = state.value.favList.indexOf(book)
        require(idx != -1) { "book {id:${book.id},title:${book.title},year:${book.year}} was not present in fav list" }
        val photo = API.takePhoto()
        updateFavList(state.value.favList.mapIndexed { ii, element ->
            if(ii == idx) element.copy(myPicture = photo) else element
        })
    }

    fun shareBook(book:Book, contact: Contact):Unit {
        API.shareBook(book, contact)
    }

    fun updateSearchScrollPosition(index: Int) {
        _state.value = state.value.copy(searchScrollIndex = index)
    }

    fun updateLibraryScrollPosition(index: Int) {
        _state.value = state.value.copy(libraryScrollIndex = index)
    }

    private fun updateFavList(favList : List<Book>){
        _state.value = state.value.copy(favList = favList)
        internetSynchronizer.upload()
    }
}