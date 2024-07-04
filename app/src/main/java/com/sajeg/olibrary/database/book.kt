package com.sajeg.olibrary.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.PrimaryKey

@Fts4
@Entity(tableName = "Books")
data class BookDBItem(
    @PrimaryKey @ColumnInfo(name = "rowid") val rowid: Int,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "author") val author: String,
    @ColumnInfo(name = "year") val year: String,
    @ColumnInfo(name = "language") val language: String,
    @ColumnInfo(name = "genre") val genre: String,
    @ColumnInfo(name = "series") val series: String,
    @ColumnInfo(name = "imgUrl") val imageUrl: String,
    @ColumnInfo(name = "url") val url: String
) {
}
