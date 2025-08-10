package com.yourname.driveexplorer.utils

import android.content.Context
import android.view.View
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import com.google.api.services.drive.model.File
import com.yourname.driveexplorer.data.model.DriveFile
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

// region Context Extensions
fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

fun Context.showToast(@StringRes resId: Int, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, resId, duration).show()
}
// endregion

// region Fragment Extensions
fun Fragment.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    requireContext().showToast(message, duration)
}

fun Fragment.showToast(@StringRes resId: Int, duration: Int = Toast.LENGTH_SHORT) {
    requireContext().showToast(resId, duration)
}
// endregion

// region View Extensions
fun View.show() {
    visibility = View.VISIBLE
}

fun View.hide() {
    visibility = View.GONE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

fun View.setDebouncedClickListener(debounceTime: Long = 300L, action: () -> Unit) {
    setOnClickListener(object : View.OnClickListener {
        private var lastClickTime: Long = 0

        override fun onClick(v: View) {
            if (System.currentTimeMillis() - lastClickTime < debounceTime) return
            lastClickTime = System.currentTimeMillis()
            action()
        }
    })
}
// endregion

// region Drive API Extensions
fun File.toDriveFile(): DriveFile {
    return DriveFile(
        id = id,
        name = name ?: "Untitled",
        mimeType = mimeType,
        modifiedTime = modifiedTime.toInstant(),
        size = size?.toLong(),
        parentIds = parents?.map { it } ?: emptyList(),
        webViewLink = webViewLink,
        thumbnailLink = thumbnailLink
    )
}

fun String?.toInstant(): Instant {
    return this?.let {
        Instant.ofEpochMilli(java.lang.Long.parseLong(it))
    } ?: Instant.now()
}

fun Instant.toReadableDateTime(): String {
    val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a")
        .withZone(ZoneId.systemDefault())
    return formatter.format(this)
}
// endregion

// region File Size Extensions
fun Long.toReadableFileSize(): String {
    if (this <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (Math.log10(toDouble()) / Math.log10(1024.0)).toInt()
    return "%.1f %s".format(
        this / Math.pow(1024.0, digitGroups.toDouble()),
        units[digitGroups]
    )
}

fun Long.toFileSizeProgress(maxSize: Long): Int {
    return if (maxSize == 0L) 0 else ((this.toFloat() / maxSize) * 100).roundToInt()
}
// endregion

// region String Extensions
fun String?.nullIfEmpty(): String? {
    return if (this.isNullOrEmpty()) null else this
}

fun String.capitalizeWords(): String {
    return split(" ").joinToString(" ") { word ->
        word.replaceFirstChar { char ->
            if (char.isLowerCase()) char.titlecase(Locale.getDefault()) 
            else char.toString()
        }
    }
}
// endregion

// region Boolean Extensions
fun Boolean?.orFalse(): Boolean = this ?: false

fun Boolean.toVisibility(invisible: Boolean = false): Int {
    return if (this) View.VISIBLE else if (invisible) View.INVISIBLE else View.GONE
}
// endregion
