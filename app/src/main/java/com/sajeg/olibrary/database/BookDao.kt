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

    @Query("""
        SELECT *, 
        CASE 
            WHEN title LIKE :query THEN 50
            WHEN author LIKE :query THEN 40
            WHEN series LIKE :query THEN 30
            WHEN title LIKE :query || '%' THEN 25
            WHEN author LIKE :query || '%' THEN 20
            WHEN series LIKE :query || '%' THEN 15
            WHEN genre LIKE :query THEN 10
            WHEN language LIKE :query THEN 5
            ELSE 1
        END as relevance,
        LENGTH(title) as titleLength
        FROM books
        WHERE title LIKE '%' || REPLACE(:query, ' ', '%') || '%'
           OR author LIKE '%' || REPLACE(:query, ' ', '%') || '%'
           OR language LIKE '%' || REPLACE(:query, ' ', '%') || '%'
           OR genre LIKE '%' || REPLACE(:query, ' ', '%') || '%'
           OR series LIKE '%' || REPLACE(:query, ' ', '%') || '%'
        ORDER BY relevance DESC, titleLength ASC, title ASC
    """)
    fun search(query: String): List<Book>

    @Query("SELECT * FROM books WHERE rowid LIKE :id")
    fun getById(id: Int): Book?

    @Insert
    fun importBook(book: BookDBItem)

    @Update
    fun updateBook(book: BookDBItem)

    @Delete
    fun delete(book: BookDBItem)
}