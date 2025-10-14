package com.example.pocket_library

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [DataBaseBook::class], version = 1, exportSchema = false)
abstract class BooksDatabase : RoomDatabase() {

    abstract fun booksDao(): BooksDAO

    companion object {

        @Volatile
        private var INSTANCE: BooksDatabase? = null

        fun getDatabase(context: Context): BooksDatabase {
            return INSTANCE ?: synchronized(this) {
                (INSTANCE ?: {
                    val instance = Room.databaseBuilder(
                        context.applicationContext,
                        BooksDatabase::class.java,
                        "books_database"
                    ).build()
                    INSTANCE = instance
                    instance
                }) as BooksDatabase
            }
        }
    }
}