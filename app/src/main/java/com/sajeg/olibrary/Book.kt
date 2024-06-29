package com.sajeg.olibrary

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import java.lang.reflect.Type

class Book(
    val recordId: Int,
    val title: String,
    val author: String,
    val year: String,
    val language: String,
    val genre: String,
    val series: String?,
    val imgUrl: String,
    val url: String
)

class BookDeserializer : JsonDeserializer<Book> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): Book {
        val jsonObject = json?.asJsonObject ?: throw JsonParseException("Invalid JSON")
        return Book(
            recordId = jsonObject.get("recordId")?.asInt ?: -1,
            title = jsonObject.get("title")?.asString ?: "",
            author = jsonObject.get("author")?.asString ?: "",
            year = jsonObject.get("year")?.asString ?: "",
            language = jsonObject.get("language")?.asString ?: "",
            genre = jsonObject.get("genre")?.asString ?: "",
            series = jsonObject.get("series")?.asString,
            imgUrl = jsonObject.get("imgUrl")?.asString ?: "",
            url = jsonObject.get("url")?.asString ?: ""
        )
    }
}