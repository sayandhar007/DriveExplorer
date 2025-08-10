data class DriveFile(
    val id: String,
    val name: String,
    val isFolder: Boolean,
    val mimeType: String,
    val modifiedTime: Instant,
    val size: Long? = null
)
