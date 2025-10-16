package com.example.pocket_library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.math.min

class LibraryViewModel : ViewModel() {
    private class LocalInternetSynchronizer(val vm : LibraryViewModel){
        val minNoInternetWaitTimeMills : Long = 1
        val maxNoInternetWaitTimeMills : Long = 10000
        private var synchronizationJob : Job? = null

        enum class SYNCJOB{
            NONE,UPLOAD,DOWNLOAD
        }
        private var _currentJob : SYNCJOB = SYNCJOB.NONE
        fun getCurrentJob(): SYNCJOB{return _currentJob}
        fun isSyncing(): Boolean {return getCurrentJob() != SYNCJOB.NONE }

        fun stop(){
            val oldJob = synchronizationJob
            synchronizationJob = null
            oldJob?.cancel()
            _currentJob = SYNCJOB.NONE
        }

        fun upload(){
            stop()
            _currentJob = SYNCJOB.UPLOAD
            synchronizationJob = vm.viewModelScope.launch{
                val localFaves : List<Book> = vm.state.value.favList
                vm.viewModelScope.launch {
                    API.setFavouritesLocal(localFaves)
                }.join()        // this ensures that the API call runs synchronously with this function, but if this function is canceled
                                // it will not effect the API call
                var updatePushed : Boolean = false
                var waitTimeMills : Long = minNoInternetWaitTimeMills
                while(!updatePushed) {
                    vm.viewModelScope.launch {
                        try {
                            API.setFavouritesInternet(localFaves)
                            updatePushed = true
                        }catch (e : NoInternetException){
                            // reattempt, as while loop is not broken.  new job is launched as this will break loop
                            // if sync job is canceled
                            Thread.sleep(waitTimeMills)
                            waitTimeMills *= 2
                            waitTimeMills = min(waitTimeMills, maxNoInternetWaitTimeMills)
                        }
                    }.join()
                }
                stop()
            }
        }
        fun download(){
            stop()
            _currentJob = SYNCJOB.DOWNLOAD
            synchronizationJob = vm.viewModelScope.launch {
                var favList : List<Book>? = null
                var updatePulled : Boolean = false
                var waitTimeMills : Long = minNoInternetWaitTimeMills
                while(!updatePulled){
                    vm.viewModelScope.launch {
                        try {
                            favList = API.getFavouritesInternet()
                            updatePulled = true
                        }catch(e: NoInternetException){
                            Thread.sleep(waitTimeMills)
                            waitTimeMills *= 2
                            waitTimeMills = min(waitTimeMills, maxNoInternetWaitTimeMills)
                        }
                    }.join()
                }
                vm.viewModelScope.launch {
                    API.setFavouritesLocal(favList?:emptyList())
                }.join()
                vm._state.value = vm.state.value.copy(favList = favList?:emptyList())
            }
        }
    }
    private val localInternetSynchronizer = LocalInternetSynchronizer(this)


    data class State(
        val favList : List<Book> = emptyList(),
        val search : Search = Search(),
        val searchResults : List<Book> = emptyList()
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
        localInternetSynchronizer.download()
        API.observeInternetStatus{ isConnected ->
            if(isConnected){
                localInternetSynchronizer.upload()
            }
        }
    }

    fun setSearch(title : String? = null, author : String? = null, year : Year_type? = null){
        val oldSearch = state.value.search
        _state.value = state.value.copy(search = state.value.search.copy(title = title ?: oldSearch.title, author = author ?: oldSearch.author, year = year ?: oldSearch.year))
    }

    fun addFavourite(book : Book):Unit {
        if(book !in state.value.favList) {
            updateFavList(state.value.favList + book)
        }
    }

    fun removeFavourite(book : Book):Unit {
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

    fun shareBook(book:Book):Unit

    fun shareBook(id:Id_type):Unit

    private fun updateFavList(favList : List<Book>){
        _state.value = state.value.copy(favList = favList)
        localInternetSynchronizer.upload()
    }
}