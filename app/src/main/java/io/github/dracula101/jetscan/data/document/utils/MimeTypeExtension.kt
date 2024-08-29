package io.github.dracula101.jetscan.data.document.utils

import io.github.dracula101.jetscan.data.document.models.Extension
import io.github.dracula101.jetscan.data.document.models.MimeType


fun MimeType.toExtension(): Extension {
    return when (this) {
        MimeType.APPLICATION_PDF -> Extension.PDF
        MimeType.APPLICATION_MSWORD -> Extension.DOC
        MimeType.APPLICATION_VND_MS_EXCEL -> Extension.XLS
        MimeType.APPLICATION_VND_MS_POWERPOINT -> Extension.PPT
        MimeType.APPLICATION_ZIP -> Extension.ZIP
        MimeType.APPLICATION_RAR -> Extension.RAR
        MimeType.APPLICATION_APK -> Extension.APK
        MimeType.IMAGE_GIF -> Extension.GIF
        MimeType.IMAGE_JPG -> Extension.JPG
        MimeType.IMAGE_JPEG -> Extension.JPG
        MimeType.IMAGE_PNG -> Extension.PNG
        MimeType.IMAGE_HEIC -> Extension.HEIC
        MimeType.AUDIO_MP3 -> Extension.MP3
        MimeType.VIDEO_MP4 -> Extension.MP4
        else -> Extension.OTHER
    }
}

fun MimeType.toImageExtension(): Extension {
    return when (this) {
        MimeType.IMAGE_GIF -> Extension.GIF
        MimeType.IMAGE_JPG -> Extension.JPG
        MimeType.IMAGE_JPEG -> Extension.JPG
        MimeType.IMAGE_PNG -> Extension.PNG
        MimeType.IMAGE_HEIC -> Extension.HEIC
        else -> Extension.OTHER
    }
}