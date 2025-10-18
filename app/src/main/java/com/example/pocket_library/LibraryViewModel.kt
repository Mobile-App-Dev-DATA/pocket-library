package com.example.pocket_library

import android.Manifest
import androidx.annotation.RequiresPermission
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.math.min
import kotlinx.coroutines.delay

class LibraryViewModel : ViewModel() {
    private class InternetSynchronizer(val vm: LibraryViewModel) {

        private val minNoInternetWaitMillis: Long = 1000
        private val maxNoInternetWaitMillis: Long = 10000

        enum class SyncJob { NONE, UPLOAD, DOWNLOAD, DUPLEX }
        private var currentJob: SyncJob = SyncJob.NONE

        fun getCurrentJob() = currentJob
        fun isSyncing() = currentJob != SyncJob.NONE

        fun stop() {
            currentJob = SyncJob.NONE
        }

        fun startUpload() {
            if (currentJob == SyncJob.DUPLEX || currentJob == SyncJob.UPLOAD) return

            currentJob = if (currentJob == SyncJob.DOWNLOAD) SyncJob.DUPLEX else SyncJob.UPLOAD

            vm.viewModelScope.launch {
                val localFaves = vm.state.value.favList
                var success = false
                var waitTime = minNoInternetWaitMillis

                while (!success) {
                    try {
                        APIImpl.setFavouritesInternet(localFaves)
                        success = true
                    } catch (e: NoInternetException) {
                        delay(waitTime)
                        waitTime = (waitTime * 2).coerceAtMost(maxNoInternetWaitMillis)
                    }
                }

                currentJob = SyncJob.NONE
            }
        }

        fun startDownload() {
            if (isSyncing()) return

            currentJob = SyncJob.DOWNLOAD

            vm.viewModelScope.launch {
                var favList = vm.state.value.favList
                var success = false
                var waitTime = minNoInternetWaitMillis

                while (!success) {
                    try {
                        val remoteList = APIImpl.getFavouritesInternet()
                        favList = (favList + remoteList).distinctBy { it.id }
                        success = true
                    } catch (e: NoInternetException) {
                        delay(waitTime)
                        waitTime = (waitTime * 2).coerceAtMost(maxNoInternetWaitMillis)
                    }
                }

                // Update local storage
                APIImpl.setFavouritesLocal(favList)

                // Update state
                vm._state.value = vm.state.value.copy(favList = favList)

                currentJob = SyncJob.NONE
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
        internetSynchronizer.startDownload()
    }

    fun setSearch(title : String? = null, author : String? = null, year : Year_type? = null){
        val oldSearch = state.value.search
        _state.value = state.value.copy(search = state.value.search.copy(title = title ?: oldSearch.title, author = author ?: oldSearch.author, year = year ?: oldSearch.year))
    }

    fun performSearch() {
        viewModelScope.launch {
            try {
                val results = APIImpl.getBooksMatchingSearch(
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

    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    @Throws(IllegalStateException::class)
    fun removeFavourite(book : Book):Unit {
        check(APIImpl.isConnectedToInternet()){"Favourites cannot be removed while offline"}
        if(state.value.favList.any{it.id == book.id}){
            updateFavList(state.value.favList.filterNot { it.id == book.id })
        }
    }

    fun addPersonalPhotoToFavourite(book : Book) {
        val idx = state.value.favList.indexOf(book)
        require(idx != -1) { "book {id:${book.id},title:${book.title},year:${book.year}} was not present in fav list" }
        val photo = APIImpl.takePhoto()
        updateFavList(state.value.favList.mapIndexed { ii, element ->
            if(ii == idx) element.copy(myPicture = photo) else element
        })
    }

    fun shareBook(book:Book, contact: Contact):Unit {
        APIImpl.shareBook(book, contact)
    }

    fun updateSearchScrollPosition(index: Int) {
        _state.value = state.value.copy(searchScrollIndex = index)
    }

    fun updateLibraryScrollPosition(index: Int) {
        _state.value = state.value.copy(libraryScrollIndex = index)
    }

    private fun updateFavList(favList : List<Book>){
        _state.value = state.value.copy(favList = favList)
        internetSynchronizer.startUpload()
    }
}