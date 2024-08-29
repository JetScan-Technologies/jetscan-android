package io.github.dracula101.jetscan.data.document.manager.video

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import java.io.File

interface VideoManager {

    fun getFirstFrameFromVideo(videoFile: Uri): Bitmap?

    fun saveVideoFirstFrame(context: Context, uri: Uri, name: String): File?

}
