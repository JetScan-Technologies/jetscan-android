package io.github.dracula101.jetscan.data.platform.manager.models

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.io.File


/**
 * Represents a special circumstance the app may be in. These circumstances could require some kind
 * of navigation that is counter to what otherwise may happen based on the state of the app.
 */
@Parcelize
sealed class SpecialCircumstance : Parcelable {

    @Parcelize
    data class ImportPdfEvent(
        val uri: Uri,
        val pdfName: String,
    ) : SpecialCircumstance(), Parcelable

}
