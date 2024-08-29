package io.github.dracula101.jetscan.presentation.platform.feature.scanner.model.graph

class Graph {
    private val nodes = mutableListOf<Node>()
    private val edges = mutableListOf<Edge>()

    fun getNodes(): List<Node> {
        return nodes
    }

    fun addNode(node: Node) {
        nodes.add(node)
    }

    fun addEdge(edge: Edge) {
        edges.add(edge)
    }

    fun depthFirstSearch(start: Node, end: Node): List<Node> {
        val visited = mutableSetOf<Node>()
        val path = mutableListOf<Node>()
        fun dfs(current: Node) {
            if (current == end) {
                path.add(current)
                return
            }
            if (visited.contains(current)) {
                return
            }
            visited.add(current)
            path.add(current)
            val neighbors = edges.filter { it.nodes.first == current || it.nodes.second == current }
            for (neighbor in neighbors) {
                val next =
                    if (neighbor.nodes.first == current) neighbor.nodes.second else neighbor.nodes.first
                dfs(next)
            }
        }
        dfs(start)
        return path
    }

    fun breadthFirstSearch(start: Node, end: Node): List<Node> {
        val visited = mutableSetOf<Node>()
        val path = mutableListOf<Node>()
        val queue = mutableListOf<Node>()
        queue.add(start)
        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            if (current == end) {
                path.add(current)
                break
            }
            if (visited.contains(current)) {
                continue
            }
            visited.add(current)
            path.add(current)
            val neighbors = edges.filter { it.nodes.first == current || it.nodes.second == current }
            for (neighbor in neighbors) {
                val next =
                    if (neighbor.nodes.first == current) neighbor.nodes.second else neighbor.nodes.first
                queue.add(next)
            }
        }
        return path
    }

}