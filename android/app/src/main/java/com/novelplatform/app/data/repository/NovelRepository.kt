package com.novelplatform.app.data.repository

import com.novelplatform.app.data.api.NovelApiService
import com.novelplatform.app.data.model.Novel
import com.novelplatform.app.data.model.Chapter

class NovelRepository(private val apiService: NovelApiService) {
    
    suspend fun getNovels(): List<Novel> {
        return apiService.getNovels()
    }
    
    suspend fun getNovel(id: Long): Novel {
        return apiService.getNovel(id)
    }
    
    suspend fun getNovelsByGenre(genreId: Long): List<Novel> {
        return apiService.getNovelsByGenre(genreId)
    }
    
    suspend fun searchNovels(query: String): List<Novel> {
        return apiService.searchNovels(query)
    }
    
    suspend fun getChapters(novelId: Long): List<Chapter> {
        return apiService.getChapters(novelId)
    }
    
    suspend fun getChapter(id: Long): Chapter {
        return apiService.getChapter(id)
    }
}
