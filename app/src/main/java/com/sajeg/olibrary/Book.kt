package com.sajeg.olibrary

data class Book(
    val recordId: Int?,
    val title: String,
    val author: String,
    val year: String,
    val language: String,
    val genre: String,
    val series: String?,
    val imgUrl: String,
    val url: String
) {
    fun getAuthorFormated(): String {
        if (author == "") {
            return ""
        }
        try {
            val names = author
                .replace("[", "")
                .replace("]", "")
                .split(",")
            val output = if (names.size == 4) {
                "Von ${names[1]} ${names[0]} und ${names[3]} ${names[2]}"
            } else {
                "Von ${names[1]} ${names[0]}"
            }
            return output
        } catch (e: Exception) {
            return ""
        }
    }
}