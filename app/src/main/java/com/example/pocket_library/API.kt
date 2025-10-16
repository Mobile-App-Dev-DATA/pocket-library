package com.example.pocket_library

abstract class API {
    companion object {
        var internetStateObservers : List<BooleanObserver> = emptyList()

        fun isConnectedToInternet() : Boolean

        fun setFavouritesLocal(newFav: List<Book>)      // just sets the local favourites database to these books
        @Throws(NoInternetException::class)
        fun setFavouritesInternet(newFav: List<Book>)

        fun getFavouritesLocal(): List<Book>
        @Throws(NoInternetException::class)
        fun getFavouritesInternet(): List<Book>

        @Throws(NoInternetException::class)
        fun getBooksMatchingSearch(title:String, author: String, year:Year_type): List<Book>

        fun takePhoto(): Img_type

        fun shareBook(
            book: Book,
            contact: Contact
        )   // not really sure how the contact should be specified.  this you should defo change to match how u need it to be

        /*

        These are just examples of what I need.  feel free to change these to what ever you feel best matches how u intend to implement
        would be idea if you could come to a conclusion on what this interface contains soon ish tho cause I cant do much more work till I know what im working with

         */

        /*
        If U could be so kind, It would be ideal if I could observe the state of the internet connectivity, so an action can be fired
        when the internet connection is restored.  I have implemented the below function and the list of observers at the top of this class.
        I just need so that each element in the list will have its update function called with the current state of the internet connection
        (true if connected, false if not) passed in
         */
        fun observeInternetStatus(observer : BooleanObserver) {
            internetStateObservers += observer
        }
    }
}