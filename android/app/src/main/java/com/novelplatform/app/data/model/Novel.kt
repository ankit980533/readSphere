package com.novelplatform.app.data.model

data class Novel(
    val id: Long,
    val title: String,
    val description: String?,
    val summary: String?,
    val coverImage: String?,
    val genre: String,
    val author: String,
    val chapterCount: Int?,
    val status: String?
)

data class Chapter(
    val id: Long,
    val novelId: Long?,
    val chapterNumber: Int,
    val title: String,
    val content: String?,
    val wordCount: Int?
)
