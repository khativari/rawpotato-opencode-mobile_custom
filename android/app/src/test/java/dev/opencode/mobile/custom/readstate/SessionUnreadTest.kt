package dev.opencode.mobile.custom.readstate

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionUnreadTest {
    @Test
    fun `fresh session with a server unread is unread`() {
        assertTrue(SessionUnread.isUnread(updatedAtEpochMs = 1_000, serverUnread = true, state = null))
    }

    @Test
    fun `fresh session with nothing new is read`() {
        assertFalse(SessionUnread.isUnread(updatedAtEpochMs = 1_000, serverUnread = false, state = null))
    }

    @Test
    fun `manual unread wins over everything`() {
        val state = SessionReadState(lastReadAtEpochMs = 2_000, manuallyUnread = true)
        assertTrue(SessionUnread.isUnread(updatedAtEpochMs = 1_000, serverUnread = false, state = state))
        assertTrue(SessionUnread.isUnread(updatedAtEpochMs = 3_000, serverUnread = false, state = state))
    }

    @Test
    fun `reading after the update clears the server flag`() {
        val state = SessionReadState(lastReadAtEpochMs = 2_000, manuallyUnread = false)
        assertFalse(SessionUnread.isUnread(updatedAtEpochMs = 1_500, serverUnread = true, state = state))
    }

    @Test
    fun `reading before a newer update keeps it unread`() {
        val state = SessionReadState(lastReadAtEpochMs = 2_000, manuallyUnread = false)
        assertTrue(SessionUnread.isUnread(updatedAtEpochMs = 3_000, serverUnread = true, state = state))
    }

    @Test
    fun `unknown update time falls back to the server flag`() {
        val state = SessionReadState(lastReadAtEpochMs = 2_000, manuallyUnread = false)
        assertTrue(SessionUnread.isUnread(updatedAtEpochMs = null, serverUnread = true, state = state))
        assertFalse(SessionUnread.isUnread(updatedAtEpochMs = null, serverUnread = false, state = state))
    }
}
