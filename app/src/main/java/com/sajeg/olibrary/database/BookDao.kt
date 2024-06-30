package com.sajeg.olibrary.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.sajeg.olibrary.Book

@Dao
interface BookDao {

    @Query("SELECT * FROM books WHERE books MATCH :query")
    fun search(query: String): List<Book>

    @Query("SELECT * FROM books WHERE rowid LIKE :id")
    fun getById(id: Int): Book?

    @Insert
    fun insertAll(vararg books: BookDBItem)

    @Insert
    fun importBook(book: BookDBItem)

    @Delete
    fun delete(book: BookDBItem)
}