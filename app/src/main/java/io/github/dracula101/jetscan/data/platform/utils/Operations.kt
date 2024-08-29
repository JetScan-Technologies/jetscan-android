package io.github.dracula101.jetscan.data.platform.utils

fun <T> permutations(original: List<T>): Set<List<T>> {
    if (original.isEmpty())
        return emptySet()
    val permutationInstructions = original.toSet()
        .map { it to original.count { x -> x == it } }
        .fold(listOf(setOf<Pair<T, Int>>())) { acc, (value, valueCount) ->
            mutableListOf<Set<Pair<T, Int>>>().apply {
                for (set in acc) for (retainIndex in 0 until valueCount) add(set + (value to retainIndex))
            }
        }
    return mutableSetOf<List<T>>().also { outSet ->
        for (instructionSet in permutationInstructions) {
            outSet += original.toMutableList().apply {
                for ((value, retainIndex) in instructionSet) {
                    repeat(retainIndex) { removeAt(indexOfFirst { it == value }) }
                    repeat(count { it == value } - 1) { removeAt(indexOfLast { it == value }) }
                }
            }
        }
    }
}