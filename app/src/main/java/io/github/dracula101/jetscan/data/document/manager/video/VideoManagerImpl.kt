package io.github.dracula101.jetscan.data.document.manager.video


import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import io.github.dracula101.jetscan.data.document.models.image.ImageQuality
import io.github.dracula101.jetscan.data.document.utils.toBitmapQuality
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream

class VideoManagerImpl : VideoManager {

    override fun getFirstFrameFromVideo(videoFile: Uri): Bitmap? {
        return try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(videoFile.path)
            val bitmap = retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
            retriever.release()
            bitmap
        } catch (e: Exception) {
            null
        }
    }

    override fun saveVideoFirstFrame(context: Context, uri: Uri, name: String): File? {
        try {
            val mediaMetadataRetriever = MediaMetadataRetriever()
            mediaMetadataRetriever.setDataSource(context, uri)
            val bitmap =
                mediaMetadataRetriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
            val file = File(context.filesDir, name.substring(0, name.lastIndexOf(".")).plus(".png"))
            val outputStream = FileOutputStream(file)
            bitmap?.compress(
                Bitmap.CompressFormat.PNG,
                ImageQuality.HIGH.toBitmapQuality(),
                outputStream
            )
            outputStream.flush()
            return file
        } catch (e: Exception) {
            Timber.e(e)
        }
        return null
    }
}