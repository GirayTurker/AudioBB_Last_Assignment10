package com.example.audiobb

import java.io.Serializable

data class Book(val title: String, var author: String, var id: Int, var coverUrl: String, var duration: Int, var progress: Int, var isDownloaded: Boolean) :Serializable