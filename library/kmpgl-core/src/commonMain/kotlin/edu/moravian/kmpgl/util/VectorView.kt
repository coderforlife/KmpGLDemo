package edu.moravian.kmpgl.util

import edu.moravian.kmpgl.math.Vector
import edu.moravian.kmpgl.math.Vector2
import edu.moravian.kmpgl.math.Vector3
import edu.moravian.kmpgl.math.Vector4


sealed class VectorView<V: Vector<V>>(
    val floats: FloatView,
    val numComponents: Int,
    val stride: Int = numComponents, // number of floats to skip to get to the next value
    val indices: Indices = Indices(floats.size / stride),
): List<V> {
    init {
        require(numComponents in 1..4)
        require(stride > 0)
    }
    abstract override val size: Int // in vectors
    override fun isEmpty() = size == 0
    abstract override operator fun get(index: Int): V
    abstract fun get(index: Int, out: V): V
    abstract operator fun set(index: Int, value: V)
    override operator fun iterator(): VectorIterator<V> = object: VectorIterator<V> {
        private var index = 0
        override fun hasNext() = index < this@VectorView.size
        override fun next() = get(index++)
        override fun next(output: V) = get(index++, output)
    }
    override fun listIterator() = listIterator(0)
    override fun listIterator(index: Int) = object : ListIterator<V> {
        private var idx = index
        override fun hasNext() = idx < this@VectorView.size
        override fun hasPrevious() = idx > 0
        override fun next() = get(idx++)
        override fun nextIndex() = idx
        override fun previous() = get(--idx)
        override fun previousIndex() = idx - 1
    }
    abstract override fun subList(fromIndex: Int, toIndex: Int): VectorView<V>

    // The rest of the methods are only implemented to complete the List interface
    // and are not intended to be used in performance-critical code
    override fun contains(element: V) = indices.any { get(it) == element }
    override fun containsAll(elements: Collection<V>) = elements.all { contains(it) }
    override fun indexOf(element: V) = indices.first { get(it) == element }
    override fun lastIndexOf(element: V) = indices.last { get(it) == element }
}

interface VectorIterator<V: Vector<V>>: Iterator<V> {
    override fun next(): V
    fun next(output: V): V
}

class Vector2View(
    floats: FloatView,
    numComponents: Int = 2,
    stride: Int = numComponents,
    indices: Indices = Indices(floats.size / stride),
): VectorView<Vector2>(floats, numComponents, stride, indices) {
    override val size = indices.size
    override fun get(index: Int) = get(index, Vector2())
    override fun get(index: Int, out: Vector2) = (indices[index]*stride).let { out.set(
        floats[it],
        if (numComponents < 2) 0f else floats[it+1]
    ) }
    override fun set(index: Int, value: Vector2) {
        val it = indices[index]*stride
        floats[it] = value.x
        if (numComponents >= 2) floats[it+1] = value.y
    }
    override fun subList(fromIndex: Int, toIndex: Int) = TODO()
}

class Vector3View(
    floats: FloatView,
    numComponents: Int = 3,
    stride: Int = numComponents,
    indices: Indices = Indices(floats.size / stride),
): VectorView<Vector3>(floats, numComponents, stride, indices) {
    override val size = indices.size
    override fun get(index: Int) = get(index, Vector3())
    override fun get(index: Int, out: Vector3) = (indices[index]*stride).let { out.set(
        floats[it],
        if (numComponents < 2) 0f else floats[it+1],
        if (numComponents < 3) 0f else floats[it+2]
    ) }
    override fun set(index: Int, value: Vector3) {
        val it = indices[index]*stride
        floats[it] = value.x
        if (numComponents >= 2) floats[it+1] = value.y
        if (numComponents >= 3) floats[it+2] = value.z
    }
    override fun subList(fromIndex: Int, toIndex: Int) = TODO()
}

class Vector4View(
    floats: FloatView,
    numComponents: Int = 4,
    stride: Int = numComponents,
    indices: Indices = Indices(floats.size / stride),
): VectorView<Vector4>(floats, numComponents, stride, indices) {
    override val size = indices.size
    override fun get(index: Int) = get(index, Vector4())
    override fun get(index: Int, out: Vector4) = (indices[index]*stride).let { out.set(
        floats[it],
        if (numComponents < 2) 0f else floats[it+1],
        if (numComponents < 3) 0f else floats[it+2],
        if (numComponents < 4) 1f else floats[it+3]
    ) }
    override fun set(index: Int, value: Vector4) {
        val it = indices[index]*stride
        floats[it] = value.x
        if (numComponents >= 2) floats[it+1] = value.y
        if (numComponents >= 3) floats[it+2] = value.z
        if (numComponents >= 4) floats[it+3] = value.w
    }
    override fun subList(fromIndex: Int, toIndex: Int) = TODO()
}
