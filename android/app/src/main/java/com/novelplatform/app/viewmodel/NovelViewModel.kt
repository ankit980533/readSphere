package com.novelplatform.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.novelplatform.app.data.api.RetrofitClient
import com.novelplatform.app.data.model.Novel
import com.novelplatform.app.data.model.Chapter
import com.novelplatform.app.data.repository.NovelRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class NovelViewModel : ViewModel() {
    
    private val repository = NovelRepository(RetrofitClient.instance)
    
    private val _novels = MutableStateFlow<List<Novel>>(emptyList())
    val novels: StateFlow<List<Novel>> = _novels
    
    private val _selectedNovel = MutableStateFlow<Novel?>(null)
    val selectedNovel: StateFlow<Novel?> = _selectedNovel
    
    private val _chapters = MutableStateFlow<List<Chapter>>(emptyList())
    val chapters: StateFlow<List<Chapter>> = _chapters
    
    private val _currentChapter = MutableStateFlow<Chapter?>(null)
    val currentChapter: StateFlow<Chapter?> = _currentChapter
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    fun loadNovels() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _novels.value = repository.getNovels()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun loadNovel(id: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _selectedNovel.value = repository.getNovel(id)
                _chapters.value = repository.getChapters(id)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun loadChapter(id: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _currentChapter.value = repository.getChapter(id)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun searchNovels(query: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _novels.value = repository.searchNovels(query)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
}
