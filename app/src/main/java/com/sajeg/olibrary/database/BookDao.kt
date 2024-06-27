package com.sajeg.olibrary.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface BookDao {
    @Query("SELECT * FROM book")
    fun getAll(): List<Book>

    @Query("SELECT * FROM book WHERE title LIKE :titleQuery")
    fun findByTitle(titleQuery: String): List<Book>

    @Insert
    fun insertAll(vararg books: Book)

    @Delete
    fun delete(book: Book)
}