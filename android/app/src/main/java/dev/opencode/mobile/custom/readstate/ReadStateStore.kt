package dev.opencode.mobile.custom.readstate

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Состояние «прочитано» одной сессии: когда её читали последний раз и ручная метка. */
data class SessionReadState(
    val lastReadAtEpochMs: Long? = null,
    val manuallyUnread: Boolean = false,
)

/**
 * Локальная память прочитанного: сервер состояние «прочитано» не хранит, поэтому решение
 * принимает клиент. Ключ — `serverKey|sessionId`, чтобы одинаковые id на разных серверах
 * не путались.
 */
interface ReadStateStore {
    fun observe(): Flow<Map<String, SessionReadState>>

    suspend fun markRead(key: String, atEpochMs: Long)

    suspend fun markUnread(key: String)

    suspend fun clearForServer(serverKey: String)
}

private val Context.customReadStateDataStore: DataStore<Preferences> by
    preferencesDataStore(name = "custom_read_state")

class DataStoreReadStateStore(
    private val dataStore: DataStore<Preferences>,
) : ReadStateStore {
    constructor(context: Context) : this(context.applicationContext.customReadStateDataStore)

    override fun observe(): Flow<Map<String, SessionReadState>> =
        dataStore.data.map { prefs ->
            val readAt = parseReadAt(prefs[READ_AT].orEmpty())
            val manual = prefs[MANUALLY_UNREAD].orEmpty()
            (readAt.keys + manual).associateWith { key ->
                SessionReadState(lastReadAtEpochMs = readAt[key], manuallyUnread = key in manual)
            }
        }

    override suspend fun markRead(key: String, atEpochMs: Long) {
        dataStore.edit { prefs ->
            val kept = prefs[READ_AT].orEmpty().filterNot { decodeReadAt(it)?.first == key }
            prefs[READ_AT] = (kept + encodeReadAt(key, atEpochMs)).toSet()
            prefs[MANUALLY_UNREAD] = prefs[MANUALLY_UNREAD].orEmpty() - key
        }
    }

    override suspend fun markUnread(key: String) {
        dataStore.edit { prefs ->
            prefs[MANUALLY_UNREAD] = prefs[MANUALLY_UNREAD].orEmpty() + key
        }
    }

    override suspend fun clearForServer(serverKey: String) {
        dataStore.edit { prefs ->
            prefs[READ_AT] =
                prefs[READ_AT].orEmpty().filterNot { decodeReadAt(it)?.first?.startsWith("$serverKey|") == true }.toSet()
            prefs[MANUALLY_UNREAD] = prefs[MANUALLY_UNREAD].orEmpty().filterNot { it.startsWith("$serverKey|") }.toSet()
        }
    }

    /** Запись «ключ + время» в одном элементе: разделитель NUL, чтобы не ломать `|` в ключе. */
    private fun encodeReadAt(key: String, atEpochMs: Long): String = key + SEPARATOR + atEpochMs

    private fun decodeReadAt(entry: String): Pair<String, Long>? {
        val at = entry.lastIndexOf(SEPARATOR)
        if (at <= 0) return null
        val time = entry.substring(at + 1).toLongOrNull() ?: return null
        return entry.substring(0, at) to time
    }

    private fun parseReadAt(entries: Set<String>): Map<String, Long> =
        entries.mapNotNull(::decodeReadAt).toMap()

    private companion object {
        const val SEPARATOR = '\u0000'
        val READ_AT = stringSetPreferencesKey("read_at")
        val MANUALLY_UNREAD = stringSetPreferencesKey("manually_unread")
    }
}

/** Для превью и песочницы: то же поведение в памяти, без диска. */
class InMemoryReadStateStore : ReadStateStore {
    private val state = kotlinx.coroutines.flow.MutableStateFlow<Map<String, SessionReadState>>(emptyMap())

    override fun observe(): Flow<Map<String, SessionReadState>> = state

    override suspend fun markRead(key: String, atEpochMs: Long) {
        state.value = state.value + (key to SessionReadState(lastReadAtEpochMs = atEpochMs))
    }

    override suspend fun markUnread(key: String) {
        state.value = state.value + (key to (state.value[key] ?: SessionReadState()).copy(manuallyUnread = true))
    }

    override suspend fun clearForServer(serverKey: String) {
        state.value = state.value.filterKeys { !it.startsWith("$serverKey|") }
    }
}
