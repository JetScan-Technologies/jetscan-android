package io.github.dracula101.jetscan.data.document.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
enum class Extension : Parcelable {
    PDF,
    DOC,
    DOCX,
    XLS,
    XLSX,
    PPT,
    PPTX,
    TXT,
    JPG,
    JPEG,
    PNG,
    GIF,
    HEIC,
    MP3,
    MP4,
    AVI,
    MKV,
    ZIP,
    RAR,
    APK,
    OTHER;

    fun isImage(): Boolean {
        return when (this) {
            JPEG, JPG, PNG, HEIC -> true
            else -> false
        }
    }

    fun isDocument(): Boolean {
        return when (this) {
            PDF, JPEG, JPG, PNG, HEIC -> true
            else -> false
        }
    }

    override fun toString(): String {
        return when (this) {
            PDF -> ".pdf"
            DOC -> ".doc"
            DOCX -> ".docx"
            XLS -> ".xls"
            XLSX -> ".xlsx"
            PPT -> ".ppt"
            PPTX -> ".pptx"
            TXT -> ".txt"
            JPG -> ".jpg"
            JPEG -> ".jpeg"
            PNG -> ".png"
            GIF -> ".gif"
            HEIC -> ".heic"
            MP3 -> ".mp3"
            MP4 -> ".mp4"
            AVI -> ".avi"
            MKV -> ".mkv"
            ZIP -> ".zip"
            RAR -> ".rar"
            APK -> ".apk"
            OTHER -> ""
        }
    }

    companion object {

        fun getExtensionType(value: String): Extension {
            return when (value) {
                "pdf" -> PDF
                "doc" -> DOC
                "docx" -> DOCX
                "xls" -> XLS
                "xlsx" -> XLSX
                "ppt" -> PPT
                "pptx" -> PPTX
                "txt" -> TXT
                "jpg" -> JPG
                "jpeg" -> JPEG
                "png" -> PNG
                "gif" -> GIF
                "heic" -> HEIC
                "mp3" -> MP3
                "mp4" -> MP4
                "avi" -> AVI
                "mkv" -> MKV
                "zip" -> ZIP
                "rar" -> RAR
                "apk" -> APK
                else -> OTHER
            }
        }
    }
}
