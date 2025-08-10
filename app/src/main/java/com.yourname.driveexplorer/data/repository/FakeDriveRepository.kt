package com.yourname.driveexplorer.data.repository

import com.yourname.driveexplorer.data.model.DriveFile
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.Instant
import java.util.UUID

/**
 * Fake implementation of DriveRepository for testing and previews
 * Simulates network delays and provides mock data
 */
class FakeDriveRepository : DriveRepositoryInterface {

    // In-memory "cloud" storage
    private val fakeDriveStorage = mutableMapOf<String, List<DriveFile>>().apply {
        put("root", generateFakeFiles("root"))
        put("folder1", generateFakeFiles("folder1"))
        put("folder2", generateFakeFiles("folder2"))
    }

    override suspend fun listFiles(folderId: String): List<DriveFile> {
        // Simulate network delay
        delay(500)
        return fakeDriveStorage[folderId] ?: emptyList()
    }

    override suspend fun moveFile(fileId: String, fromFolderId: String, toFolderId: String): Boolean {
        delay(300)
        return true // Always succeeds in fake implementation
    }

    override suspend fun copyFile(fileId: String, toFolderId: String): DriveFile {
        delay(400)
        return getFileById(fileId)?.copy(id = UUID.randomUUID().toString()) 
            ?: throw IllegalArgumentException("File not found")
    }

    override suspend fun getFileById(fileId: String): DriveFile? {
        delay(200)
        return fakeDriveStorage.values.flatten().find { it.id == fileId }
    }

    override fun observeFolderContents(folderId: String): Flow<List<DriveFile>> = flow {
        while (true) {
            emit(listFiles(folderId))
            delay(5000) // Simulate periodic updates
        }
    }

    private fun generateFakeFiles(parentId: String): List<DriveFile> {
        val prefix = if (parentId == "root") "" else "child-"
        return listOf(
            DriveFile(
                id = "$parentId-1",
                name = "${prefix}Document.pdf",
                mimeType = "application/pdf",
                modifiedTime = Instant.now().minusSeconds(86400),
                size = 2500000,
                parentIds = listOf(parentId)
            ),
            DriveFile(
                id = "$parentId-2",
                name = "${prefix}Spreadsheet.xlsx",
                mimeType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                modifiedTime = Instant.now().minusSeconds(3600),
                size = 1800000,
                parentIds = listOf(parentId)
            ),
            DriveFile(
                id = "$parentId-3",
                name = "${prefix}Images",
                mimeType = "application/vnd.google-apps.folder",
                modifiedTime = Instant.now().minusSeconds(7200),
                size = null,
                parentIds = listOf(parentId)
            )
        )
    }
}
