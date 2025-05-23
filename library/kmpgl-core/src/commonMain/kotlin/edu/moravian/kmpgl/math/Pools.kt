@file:Suppress("NOTHING_TO_INLINE")

package edu.moravian.kmpgl.math

// TODO: make all pools (and the pool scoper) thread-local? or maybe use the concurrent strategy from KDS?

class Pool<T: Any>(preallocate: Int = 0, private val gen: (Int) -> T) {
    private val items = ArrayList<T>()
    private var lastId = 0

    val totalAllocatedItems get() = lastId
    val totalItemsInUse get() = totalAllocatedItems - itemsInPool
    val itemsInPool: Int get() = items.size

    init {
        for (n in 0 until preallocate) items.add(gen(lastId++))
    }

    fun alloc(): T = if (items.isNotEmpty()) items.removeAt(items.lastIndex) else gen(lastId++)
    fun clear() { items.clear(); lastId = 0 }
    fun free(element: T) { items.add(element) }
    fun free(elements: List<T>) { elements.forEach { free(it) } }

    inline operator fun invoke(scope: PoolScope): T = alloc().also { scope.add(this, it) }
    inline operator fun <R> invoke(callback: (T) -> R): R = alloc(callback)
    inline operator fun <R> invoke(callback: (T, T) -> R): R = alloc2(callback)
    inline operator fun <R> invoke(callback: (T, T, T) -> R): R = alloc3(callback)
    inline operator fun <R> invoke(callback: (T, T, T, T) -> R): R = alloc4(callback)
    inline operator fun <R> invoke(count: Int, temp: ArrayList<T> = ArrayList(), callback: (ArrayList<T>) -> R): R = allocMultiple(count, temp, callback)

    inline fun <R> alloc(callback: (T) -> R): R {
        val temp = alloc()
        try {
            return callback(temp)
        } finally {
            free(temp)
        }
    }

    inline fun <R> alloc2(callback: (T, T) -> R): R {
        val temp1 = alloc()
        val temp2 = alloc()
        try {
            return callback(temp1, temp2)
        } finally {
            free(temp2)
            free(temp1)
        }
    }

    inline fun <R> alloc3(callback: (T, T, T) -> R): R {
        val temp1 = alloc()
        val temp2 = alloc()
        val temp3 = alloc()
        try {
            return callback(temp1, temp2, temp3)
        } finally {
            free(temp3)
            free(temp2)
            free(temp1)
        }
    }

    inline fun <R> alloc4(callback: (T, T, T, T) -> R): R {
        val temp1 = alloc()
        val temp2 = alloc()
        val temp3 = alloc()
        val temp4 = alloc()
        try {
            return callback(temp1, temp2, temp3, temp4)
        } finally {
            free(temp4)
            free(temp3)
            free(temp2)
            free(temp1)
        }
    }

    inline fun <R> allocMultiple(count: Int, temp: ArrayList<T> = ArrayList(), callback: (ArrayList<T>) -> R): R {
        temp.clear()
        for (n in 0 until count) temp.add(alloc())
        try {
            return callback(temp)
        } finally {
            free(temp)
            temp.clear()
        }
    }

    inline fun <R> allocThis(callback: T.() -> R): R {
        val temp = alloc()
        try {
            return callback(temp)
        } finally {
            free(temp)
        }
    }

    override fun hashCode() = items.hashCode()
    override fun equals(other: Any?) = (other === this) || (other is Pool<*>) && this.items == other.items && this.itemsInPool == other.itemsInPool
}

class PoolScope {
    val pools = ArrayList<Pool<Any>>()
    val items = ArrayList<Any>()
    inline fun <T: Any> add(pool: Pool<T>, item: T) {
        pools.add(pool as Pool<Any>)
        items.add(item)
    }
    inline fun empty() {
        for (i in this.pools.indices) { pools[i].free(items[i]) }
    }
}

@Suppress("ClassName")
object poolScoper {
    val scopes = Pool { PoolScope() }
    inline operator fun <R> invoke(callback: (PoolScope) -> R): R {
        val scope = scopes.alloc()
        try {
            return callback(scope)
        } finally {
            scope.empty()
            scopes.free(scope)
        }
    }
}
