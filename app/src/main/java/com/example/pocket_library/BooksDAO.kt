package com.example.pocket_library

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Update

@Dao
interface BooksDAO {
    @Insert
    suspend fun insert(vararg book: Books)

    @Update
    suspend fun update(vararg book: Books)

    @Delete
    suspend fun delete(vararg book: Books)
}