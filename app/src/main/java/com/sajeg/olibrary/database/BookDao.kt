package com.sajeg.olibrary.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface BookDao {
    @Query("SELECT * FROM bookdbitem")
    fun getAll(): List<BookDBItem>

    @Query("SELECT * FROM bookdbitem WHERE title LIKE :titleQuery")
    fun findByTitle(titleQuery: String): List<BookDBItem>

    @Query("SELECT * FROM bookdbitem WHERE recordId LIKE :id")
    fun getById(id: Int): BookDBItem?

    @Insert
    fun insertAll(vararg books: BookDBItem)

    @Insert
    fun importBook(book: BookDBItem)

    @Delete
    fun delete(book: BookDBItem)
}