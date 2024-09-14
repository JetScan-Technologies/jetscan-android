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

    fun value(): String {
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

        fun getExtensionType(uppercase: String): Extension {
            return when (uppercase) {
                "PDF" -> PDF
                "DOC" -> DOC
                "DOCX" -> DOCX
                "XLS" -> XLS
                "XLSX" -> XLSX
                "PPT" -> PPT
                "PPTX" -> PPTX
                "TXT" -> TXT
                "JPG" -> JPG
                "JPEG" -> JPEG
                "PNG" -> PNG
                "GIF" -> GIF
                "HEIC" -> HEIC
                "MP3" -> MP3
                "MP4" -> MP4
                "AVI" -> AVI
                "MKV" -> MKV
                "ZIP" -> ZIP
                "RAR" -> RAR
                "APK" -> APK
                else -> OTHER
            }
        }
    }
}
