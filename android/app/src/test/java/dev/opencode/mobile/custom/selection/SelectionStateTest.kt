package dev.opencode.mobile.custom.selection

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SelectionStateTest {
    @Test
    fun `starts inactive`() {
        val state = SelectionState()
        assertFalse(state.active)
        assertEquals(0, state.count)
    }

    @Test
    fun `enter selects one and activates`() {
        val state = SelectionState().enter("a")
        assertTrue(state.active)
        assertEquals(setOf("a"), state.ids)
    }

    @Test
    fun `toggle adds and removes`() {
        val one = SelectionState().enter("a")
        val two = one.toggle("b")
        assertEquals(setOf("a", "b"), two.ids)
        assertEquals(setOf("b"), two.toggle("a").ids)
    }

    @Test
    fun `toggle back to empty leaves the mode`() {
        val state = SelectionState().enter("a").toggle("a")
        assertFalse(state.active)
        assertTrue(state.ids.isEmpty())
    }

    @Test
    fun `clear resets everything`() {
        val state = SelectionState().enter("a").toggle("b").clear()
        assertFalse(state.active)
        assertEquals(0, state.count)
    }

    @Test
    fun `select all takes every id`() {
        val state = SelectionState().selectAll(listOf("a", "b", "c"))
        assertTrue(state.active)
        assertEquals(setOf("a", "b", "c"), state.ids)
    }

    @Test
    fun `select all on an empty list changes nothing`() {
        val state = SelectionState().enter("a")
        assertEquals(state, state.selectAll(emptyList()))
    }

    @Test
    fun `without drops one and leaves the mode when empty`() {
        val two = SelectionState().enter("a").toggle("b")
        assertEquals(setOf("b"), two.without("a").ids)
        assertFalse(two.without("a").without("b").active)
    }
}
