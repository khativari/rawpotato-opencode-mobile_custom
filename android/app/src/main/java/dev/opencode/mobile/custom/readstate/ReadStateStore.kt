package dev.opencode.mobile.custom.readstate

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
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
            val readAt = prefs[READ_AT].orEmpty().mapNotNull { (k, v) -> v.toLongOrNull()?.let { k to it } }.toMap()
            val manual = prefs[MANUALLY_UNREAD].orEmpty()
            (readAt.keys + manual).associateWith { key ->
                SessionReadState(lastReadAtEpochMs = readAt[key], manuallyUnread = key in manual)
            }
        }

    override suspend fun markRead(key: String, atEpochMs: Long) {
        dataStore.edit { prefs ->
            prefs[READ_AT] = prefs[READ_AT].orEmpty() + (key to atEpochMs.toString())
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
            prefs[READ_AT] = prefs[READ_AT].orEmpty().filterKeys { !it.startsWith("$serverKey|") }
            prefs[MANUALLY_UNREAD] = prefs[MANUALLY_UNREAD].orEmpty().filterNot { it.startsWith("$serverKey|") }.toSet()
        }
    }

    private companion object {
        val READ_AT = stringPreferencesKey("read_at")
        val MANUALLY_UNREAD = stringSetPreferencesKey("manually_unread")
    }
}
