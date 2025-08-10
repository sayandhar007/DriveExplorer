class DriveRepository(private val driveService: Drive) {
    suspend fun listFiles(folderId: String = "root"): List<DriveFile> {
        val result = driveService.files().list()
            .setQ("'$folderId' in parents and trashed = false")
            .setFields("files(id,name,mimeType,size,modifiedTime)")
            .execute()
        
        return result.files.map { file ->
            DriveFile(
                id = file.id,
                name = file.name,
                isFolder = file.mimeType == "application/vnd.google-apps.folder",
                mimeType = file.mimeType,
                modifiedTime = Instant.parse(file.modifiedTime.toString()),
                size = file.size
            )
        }
    }
}
