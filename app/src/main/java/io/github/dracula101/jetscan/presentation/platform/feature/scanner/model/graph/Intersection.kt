package io.github.dracula101.jetscan.presentation.platform.feature.scanner.model.graph

data class Intersection(
    val id: Int,
    val lines: Pair<Line, Line>,
    val point: CPoint
)