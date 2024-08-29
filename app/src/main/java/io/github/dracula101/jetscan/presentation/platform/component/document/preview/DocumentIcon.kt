package io.github.dracula101.jetscan.presentation.platform.component.document.preview

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayCircleOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toFile
import androidx.core.net.toUri
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.request.CachePolicy
import coil.request.ImageRequest
import io.github.dracula101.jetscan.R
import io.github.dracula101.jetscan.data.document.models.doc.Document
import io.github.dracula101.jetscan.data.document.models.Extension
import java.io.File

@Composable
fun DocumentIcon(document: Document) {
    Box(
        modifier = Modifier
            .width(48.dp)
            .fillMaxHeight(),
        contentAlignment = Alignment.Center
    ) {
        when (document.extension) {
            Extension.APK -> {
                APKIcon(file = document.previewImageUri?.toFile())
            }

            Extension.JPG, Extension.JPEG, Extension.PNG, Extension.GIF, Extension.HEIC -> {
                ImageIcon(file = document.previewImageUri?.toFile())
            }

            Extension.AVI, Extension.MP4, Extension.MKV, Extension.MP3 -> {
                VideoIcon(file = document.previewImageUri?.toFile())
            }

            Extension.RAR, Extension.ZIP -> {
                ZIPIcon()
            }

            Extension.PDF -> {
                PDFIcon(file = document.previewImageUri?.toFile())
            }

            Extension.DOCX, Extension.DOC -> {
                DOCXIcon()
            }

            Extension.XLSX, Extension.XLS -> {
                XLSXIcon(isXLSX = document.extension == Extension.XLSX)
            }

            Extension.PPTX, Extension.PPT -> {
                PPTIcon(isPPTX = document.extension == Extension.PPTX)
            }

            Extension.TXT -> {
                TXTIcon()
            }

            Extension.OTHER -> {
                OtherIcon(file = document.name)
            }
        }
    }
}


@Composable
fun ZIPIcon() {
    Image(
        painter = painterResource(id = R.drawable.zip_icon),
        contentDescription = null,
        modifier = Modifier
            .size(46.dp)
            .clip(MaterialTheme.shapes.small)
            .background(MaterialTheme.colorScheme.surface),
        contentScale = ContentScale.Crop,
        colorFilter = ColorFilter.tint(
            MaterialTheme.colorScheme.primary
        )
    )
}

@Composable
fun XLSXIcon(isXLSX: Boolean = true) {
    Image(
        painter = painterResource(id = if (isXLSX) R.drawable.xlsx_icon else R.drawable.xls_icon),
        contentDescription = null,
        modifier = Modifier
            .size(38.dp)
            .background(MaterialTheme.colorScheme.surface),
        contentScale = ContentScale.Crop,
        colorFilter = ColorFilter.tint(
            MaterialTheme.colorScheme.primary
        )
    )
}

@Composable
fun DOCXIcon() {
    Image(
        painter = painterResource(id = R.drawable.docx_icon),
        contentDescription = null,
        modifier = Modifier
            .size(38.dp)
            .background(MaterialTheme.colorScheme.surface),
        contentScale = ContentScale.Crop,
        colorFilter = ColorFilter.tint(
            MaterialTheme.colorScheme.primary
        )
    )
}

@Composable
fun PPTIcon(isPPTX: Boolean = true) {
    Image(
        painter = painterResource(id = if (isPPTX) R.drawable.pptx_icon else R.drawable.ppt_icon),
        contentDescription = null,
        modifier = Modifier
            .size(38.dp)
            .background(MaterialTheme.colorScheme.surface),
        contentScale = ContentScale.Crop,
        colorFilter = ColorFilter.tint(
            MaterialTheme.colorScheme.primary
        )
    )
}

@Composable
fun TXTIcon() {
    Image(
        painter = painterResource(id = R.drawable.txt_icon),
        contentDescription = null,
        modifier = Modifier
            .size(38.dp)
            .background(MaterialTheme.colorScheme.surface),
        contentScale = ContentScale.Crop,
        colorFilter = ColorFilter.tint(
            MaterialTheme.colorScheme.primary
        )
    )
}

@Composable
fun OtherIcon(file: String) {
    val extension = remember { file.substringAfterLast(".", "").uppercase() }
    val color = remember { getRandomColor(extension) }
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(MaterialTheme.shapes.small)
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = extension,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
fun VideoIcon(file: File?) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(MaterialTheme.shapes.small)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = rememberAsyncImagePainter(
                ImageRequest
                    .Builder(LocalContext.current)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .data(file)
                    .crossfade(true)
                    .lifecycle(LocalLifecycleOwner.current)
                    .build(),
                contentScale = ContentScale.Crop,
            ),
            contentDescription = null,
            modifier = Modifier
                .size(48.dp)
                .clip(MaterialTheme.shapes.small)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
            contentScale = ContentScale.Crop,
        )
        Icon(
            Icons.Rounded.PlayCircleOutline,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
fun ImageIcon(file: File?) {
    AsyncImage(
        model = ImageRequest
            .Builder(LocalContext.current)
            .crossfade(true)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .error(R.drawable.image_error)
            .crossfade(true)
            .data(file)
            .build(),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .size(48.dp)
            .clip(MaterialTheme.shapes.small)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
    )
}


@Composable
fun APKIcon(file: File?) {
    AsyncImage(
        model = ImageRequest
            .Builder(LocalContext.current)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .error(R.drawable.image_error)
            .data(file?.path)
            .crossfade(true)
            .build(),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .size(48.dp)
            .clip(MaterialTheme.shapes.small)
    )
}

@Composable
fun PDFIcon(modifier : Modifier = Modifier, file: File? = null, uri: Uri? = null,) {
    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
    ) {
        AsyncImage(
            model = ImageRequest
                .Builder(LocalContext.current)
                .memoryCachePolicy(CachePolicy.ENABLED)
                .diskCachePolicy(CachePolicy.ENABLED)
                .error(R.drawable.image_error)
                .crossfade(true)
                .data(file ?: uri)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .clip(MaterialTheme.shapes.extraSmall)
                .background(Color.White)
        )
    }
}

private fun getRandomColor(extension: String?): Color {
    if (extension.isNullOrEmpty()) {
        return Color.Gray
    }
    val hashCode = extension.hashCode()
    val red = (hashCode and 0xFF0000 shr 16) / 255.0f
    val green = (hashCode and 0x00FF00 shr 8) / 255.0f
    val blue = (hashCode and 0x0000FF) / 255.0f
    return Color(red, green, blue, alpha = 1.0f)
}