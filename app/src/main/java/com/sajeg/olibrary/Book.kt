package com.sajeg.olibrary

open class Book(
    val rowid: Int?,
    val title: String,
    val author: String,
    val year: String,
    val language: String,
    val genre: String,
    val series: String?,
    val imgUrl: String,
    val url: String
) {
    fun getAuthorFormated(onlyName: Boolean = false): String {
        if (author == "") {
            return ""
        }
        try {
            val names = author
                .replace("[", "")
                .replace("]", "")
                .split(",")
            var output = if (names.size == 6){
                "${names[1]} ${names[0]},${names[3]} ${names[2]} und${names[5]} ${names[4]}"
            }else if (names.size == 4) {
                "${names[1]} ${names[0]} und${names[3]} ${names[2]}"
            } else {
                "${names[1]} ${names[0]}"
            }
            if (!onlyName) {
                output = "Von$output"
            }
            return output
        } catch (e: Exception) {
            return ""
        }
    }
}