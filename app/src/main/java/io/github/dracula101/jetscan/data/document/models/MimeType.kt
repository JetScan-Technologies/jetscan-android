package io.github.dracula101.jetscan.data.document.models


import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Locale

@Parcelize
enum class MimeType(val value: String?) : Parcelable {

    TEXT_PLAIN("text/plain"),
    TEXT_HTML("text/html"),
    APPLICATION_JSON("application/json"),
    APPLICATION_PDF("application/pdf"),
    APPLICATION_MSWORD("application/msword"),
    APPLICATION_VND_MS_EXCEL("application/vnd.ms-excel"),
    APPLICATION_VND_MS_POWERPOINT("application/vnd.ms-powerpoint"),
    APPLICATION_ZIP("application/zip"),
    APPLICATION_RAR("application/rar"),
    APPLICATION_APK("application/vnd.android.package-archive"),
    IMAGE_GIF("image/gif"),
    IMAGE_JPG("image/jpg"),
    IMAGE_JPEG("image/jpeg"),
    IMAGE_PNG("image/png"),
    IMAGE_HEIC("image/heic"),
    AUDIO_MP3("audio/mp3"),
    VIDEO_MP4("video/mp4"),
    UNKNOWN("unknown");


    companion object {

        fun getMimeType(value: String?): MimeType {
            return when (value?.lowercase(Locale.ROOT)) {
                "text/plain" -> TEXT_PLAIN
                "text/html" -> TEXT_HTML
                "application/json" -> APPLICATION_JSON
                "application/pdf" -> APPLICATION_PDF
                "application/msword" -> APPLICATION_MSWORD
                "application/vnd.ms-excel" -> APPLICATION_VND_MS_EXCEL
                "application/vnd.ms-powerpoint" -> APPLICATION_VND_MS_POWERPOINT
                "application/zip" -> APPLICATION_ZIP
                "application/rar" -> APPLICATION_RAR
                "application/vnd.android.package-archive" -> APPLICATION_APK
                "image/gif" -> IMAGE_GIF
                "image/jpg" -> IMAGE_JPG
                "image/jpeg" -> IMAGE_JPEG
                "image/png" -> IMAGE_PNG
                "image/heic" -> IMAGE_HEIC
                "audio/mp3" -> AUDIO_MP3
                "video/mp4" -> VIDEO_MP4
                else -> UNKNOWN
            }
        }
    }
}
