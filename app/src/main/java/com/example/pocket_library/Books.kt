package com.example.pocket_library

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "books")
data class Books(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    @ColumnInfo(name = "ISBN") val isbn: String,
    @ColumnInfo(name = "Title") val title : String,
    @ColumnInfo(name = "Author") val author : String,
    @ColumnInfo(name = "Year") val year : String,
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB) val cover : ByteArray,
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB) val photo : ByteArray
)
