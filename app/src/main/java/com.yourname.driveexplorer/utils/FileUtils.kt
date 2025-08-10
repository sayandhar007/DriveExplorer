fun getFileIcon(mimeType: String): Int {
    return when {
        mimeType == "application/vnd.google-apps.folder" -> R.drawable.ic_folder
        mimeType.startsWith("image/") -> R.drawable.ic_image
        else -> R.drawable.ic_file
    }
}
