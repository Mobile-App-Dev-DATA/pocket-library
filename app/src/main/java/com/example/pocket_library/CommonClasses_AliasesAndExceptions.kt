package com.example.pocket_library

import android.graphics.drawable.Drawable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

typealias Id_type = String
typealias Img_type = ImageVector
typealias Year_type = String

typealias Contact = String

data class Book(
    val id: Id_type,
    val title: String,
    val author: String,
    val year: Year_type,
    val coverUrl: String? = null,
    val myPicture: Img_type? = null
)

@Entity(tableName = "books")
data class DataBaseBook(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    @ColumnInfo(name = "ISBN")
    val isbn: String,
    @ColumnInfo(name = "Title")
    val title: String,
    @ColumnInfo(name = "Author")
    val author: String,
    @ColumnInfo(name = "Year")
    val year: Int,
    @ColumnInfo(name = "Official Cover")
    val officialCover: String, //url instead of drawable for testing purposes
    @ColumnInfo(name = "Personal Photo")
    val personalPhoto: String? // local uri path instead of drawable
)

fun interface BooleanObserver{
    fun update(bool:Boolean)
}
class NoInternetException(message: String, cause: Throwable?) : Exception(message, cause)