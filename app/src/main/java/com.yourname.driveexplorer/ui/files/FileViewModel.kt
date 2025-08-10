package com.yourname.driveexplorer.ui.files

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourname.driveexplorer.data.model.DriveFile
import com.yourname.driveexplorer.data.repository.DriveRepositoryInterface
import com.yourname.driveexplorer.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FileViewModel @Inject constructor(
    private val repository: DriveRepositoryInterface
) : ViewModel() {

    // UI State
    private val _uiState = MutableStateFlow(FileUiState())
    val uiState: StateFlow<FileUiState> = _uiState.asStateFlow()

    // Current folder state
    private var currentFolderId: String = "root"

    init {
        loadFiles(currentFolderId)
    }

    fun loadFiles(folderId: String = currentFolderId) {
        currentFolderId = folderId
        _uiState.update { it.copy(isLoading = true) }
        
        viewModelScope.launch {
            val result = repository.listFiles(folderId)
            _uiState.update {
                when (result) {
                    is Resource.Success -> {
                        it.copy(
                            isLoading = false,
                            files = result.data,
                            error = null,
                            currentFolder = folderId
                        )
                    }
                    is Resource.Error -> {
                        it.copy(
                            isLoading = false,
                            error = result.message,
                            currentFolder = folderId
                        )
                    }
                }
            }
        }
    }

    fun refresh() {
        loadFiles(currentFolderId)
    }

    fun createFolder(folderName: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isCreatingFolder = true) }
            when (val result = repository.createFolder(currentFolderId, folderName)) {
                is Resource.Success -> {
                    loadFiles(currentFolderId) // Refresh list
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(error = result.message) }
                }
            }
            _uiState.update { it.copy(isCreatingFolder = false) }
        }
    }

    fun moveFile(fileId: String, targetFolderId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessingFile = true) }
            when (val result = repository.moveFile(fileId, currentFolderId, targetFolderId)) {
                is Resource.Success -> {
                    loadFiles(currentFolderId) // Refresh list
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(error = result.message) }
                }
            }
            _uiState.update { it.copy(isProcessingFile = false) }
        }
    }

    fun copyFile(fileId: String, targetFolderId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessingFile = true) }
            when (val result = repository.copyFile(fileId, targetFolderId)) {
                is Resource.Success -> {
                    loadFiles(targetFolderId) // Show destination folder
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(error = result.message) }
                }
            }
            _uiState.update { it.copy(isProcessingFile = false) }
        }
    }

    fun deleteFile(fileId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessingFile = true) }
            when (val result = repository.deleteFile(fileId)) {
                is Resource.Success -> {
                    loadFiles(currentFolderId) // Refresh list
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(error = result.message) }
                }
            }
            _uiState.update { it.copy(isProcessingFile = false) }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class FileUiState(
    val isLoading: Boolean = false,
    val isCreatingFolder: Boolean = false,
    val isProcessingFile: Boolean = false,
    val files: List<DriveFile> = emptyList(),
    val currentFolder: String = "root",
    val error: String? = null
)
