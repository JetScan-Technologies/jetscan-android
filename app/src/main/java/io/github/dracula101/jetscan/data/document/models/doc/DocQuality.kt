package io.github.dracula101.jetscan.data.document.models.doc

import androidx.compose.ui.geometry.Size

enum class DocQuality {
    PPI_72,
    PPI_100,
    PPI_150,
    PPI_200,
    PPI_300,
    PPI_400;

    fun toSizePx(): Size {
        return when (this) {
            PPI_72 -> Size(595f, 842f)
            PPI_100 -> Size(827f, 1169f)
            PPI_150 -> Size(1240f, 1754f)
            PPI_200 -> Size(1654f, 2339f)
            PPI_300 -> Size(2480f, 3508f)
            PPI_400 -> Size(3307f, 4677f)
        }
    }

    fun toSizeMm(): Size {
        return when (this) {
            PPI_72 -> Size(210f, 297f)
            PPI_100 -> Size(297f, 420f)
            PPI_150 -> Size(420f, 594f)
            PPI_200 -> Size(594f, 841f)
            PPI_300 -> Size(841f, 1190f)
            PPI_400 -> Size(1190f, 1684f)
        }
    }

    fun toDensity(): Int {
        return when (this) {
            PPI_72 -> 72
            PPI_100 -> 100
            PPI_150 -> 150
            PPI_200 -> 200
            PPI_300 -> 300
            PPI_400 -> 400
        }
    }
}