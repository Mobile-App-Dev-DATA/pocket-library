package com.example.pocket_library

abstract class API {
    companion object {

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
    }
}