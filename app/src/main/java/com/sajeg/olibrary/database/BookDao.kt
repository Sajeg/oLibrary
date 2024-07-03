package com.sajeg.olibrary.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.sajeg.olibrary.Book

@Dao
interface BookDao {
    @Query("SELECT COUNT(*) FROM books")
    fun getRowCount(): Int

    @Query("SELECT rowid, * FROM books WHERE books MATCH '*' || :query || '*' LIMIT 30")
    fun search(query: String): List<Book>

    @Query("SELECT * FROM books WHERE rowid LIKE :id")
    fun getById(id: Int): Book?

    @Query("SELECT * FROM books WHERE imgUrl LIKE '%de%' ORDER BY RANDOM() LIMIT :count")
    fun getRandomBooks(count: Int): List<Book>

    @Insert
    fun importBook(book: BookDBItem)

    @Update
    fun updateBook(book: BookDBItem)

    @Delete
    fun delete(book: BookDBItem)
}