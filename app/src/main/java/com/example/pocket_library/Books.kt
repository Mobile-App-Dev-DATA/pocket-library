package com.example.pocket_library

import android.graphics.drawable.Drawable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "books")
data class Books(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    @ColumnInfo(name = "ISBN")
    val isbn: UInt,
    @ColumnInfo(name = "Title")
    val title: String,
    @ColumnInfo(name = "Author")
    val author: String,
    @ColumnInfo(name = "Year")
    val year: Int,
    @ColumnInfo(name = "Official Cover")
    val officialCover: Drawable,
    @ColumnInfo(name = "Personal Photo")
    val personalPhoto: Drawable
)