package com.example.pocket_library

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Update

@Dao
interface BooksDAO {
    @Insert
    suspend fun insert(vararg book: DataBaseBook)

    @Update
    suspend fun update(vararg book: DataBaseBook)

    @Delete
    suspend fun delete(vararg book: DataBaseBook)
}