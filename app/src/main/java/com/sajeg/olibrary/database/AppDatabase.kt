package com.sajeg.olibrary.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [BookDBItem::class], version = 1)
abstract class AppDatabase: RoomDatabase() {
    abstract fun bookDao(): BookDao
}