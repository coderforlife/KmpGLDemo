package edu.moravian.kmpgl.util

interface Indices: Iterable<Int> {
    val size: Int
    operator fun get(index: Int): Int
    override fun iterator(): IntIterator
}

fun Indices(size: Int): Indices = BasicIndices(size)
fun Indices(progression: IntProgression): Indices = IntProgressionIndices(progression)
fun Indices(range: IntRange): Indices = IntRangeIndices(range)
fun Indices(indices: IntArray): Indices = IntArrayIndices(indices)
fun Indices(indices: IntView): Indices = IntViewIndices(indices)

private class BasicIndices(
    override val size: Int
): Indices {
    override fun get(index: Int) = index
    override fun iterator() = object: IntIterator() {
        private var index = 0
        override fun hasNext() = index < size
        override fun nextInt() = index++
    }
}

private class IntProgressionIndices(
    private val progression: IntProgression
): Indices {
    override val size = (progression.last - progression.first) / progression.step + 1
    override fun get(index: Int) = progression.first + index * progression.step
    override fun iterator() = progression.iterator()
}

private class IntRangeIndices(
    private val range: IntRange
): Indices {
    override val size = range.last - range.first + 1
    override fun get(index: Int) = range.first + index
    override fun iterator() = range.iterator()
}

private class IntArrayIndices(
    private val array: IntArray
): Indices {
    override val size = array.size
    override fun get(index: Int) = array[index]
    override fun iterator() = array.iterator()
}

private class IntViewIndices(
    private val array: IntView
): Indices {
    override val size = array.size
    override fun get(index: Int) = array[index]
    override fun iterator() = array.iterator()
}
