package com.novelplatform.app.data.api

import com.novelplatform.app.data.model.Novel
import com.novelplatform.app.data.model.Chapter
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface NovelApiService {
    
    @GET("api/novels")
    suspend fun getNovels(): List<Novel>
    
    @GET("api/novels/{id}")
    suspend fun getNovel(@Path("id") id: Long): Novel
    
    @GET("api/novels/genre/{genreId}")
    suspend fun getNovelsByGenre(@Path("genreId") genreId: Long): List<Novel>
    
    @GET("api/novels/search")
    suspend fun searchNovels(@Query("query") query: String): List<Novel>
    
    @GET("api/novels/{novelId}/chapters")
    suspend fun getChapters(@Path("novelId") novelId: Long): List<Chapter>
    
    @GET("api/chapters/{id}")
    suspend fun getChapter(@Path("id") id: Long): Chapter
}
