package com.example.pocket_library

interface API {
    fun setFavouritesLocal(newFav:List<Book>)      // just sets the local favourites database to these books
    fun setFavouritesInternet(newFav:List<Book>)

    fun getFavouritesLocal()
    fun getFavouritesInternet()

    fun getBooksMatchingSearch(search:Search)

    fun takePhoto()

    fun shareBook(book:Book, contact:Contact)   // not really sure how the contact should be specified.  this you should defo change to match how u need it to be

    /*

    These are just examples of what I need.  feel free to change these to what ever you feel best matches how u intend to implement
    would be idea if you could come to a conclusion on what this interface contains soon ish tho cause I cant do much more work till I know what im working with

     */
}