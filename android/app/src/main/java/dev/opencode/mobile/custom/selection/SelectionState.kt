package dev.opencode.mobile.custom.selection

/** Режим массового выделения сессий на главном экране. */
data class SelectionState(
    val active: Boolean = false,
    val ids: Set<String> = emptySet(),
) {
    val count: Int get() = ids.size

    fun enter(id: String): SelectionState = SelectionState(active = true, ids = setOf(id))

    fun toggle(id: String): SelectionState =
        if (!active) {
            enter(id)
        } else {
            val next = if (id in ids) ids - id else ids + id
            if (next.isEmpty()) SelectionState() else SelectionState(active = true, ids = next)
        }

    fun selectAll(all: Collection<String>): SelectionState =
        if (all.isEmpty()) this else SelectionState(active = true, ids = all.toSet())

    fun clear(): SelectionState = SelectionState()

    fun without(id: String): SelectionState {
        val next = ids - id
        return if (next.isEmpty()) SelectionState() else copy(ids = next)
    }
}

/** Итог пачечной операции: сколько получилось, сколько нет. */
data class BatchResult(
    val done: Int,
    val failed: Int,
)
