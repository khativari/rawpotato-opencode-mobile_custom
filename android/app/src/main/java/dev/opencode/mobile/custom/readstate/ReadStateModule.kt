package dev.opencode.mobile.custom.readstate

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Ключ состояния «прочитано»: один сервер — одно пространство ключей. */
fun readStateKey(serverKey: String, sessionId: String): String = "$serverKey|$sessionId"

@Module
@InstallIn(SingletonComponent::class)
object ReadStateModule {
    @Provides
    @Singleton
    fun provideReadStateStore(
        @ApplicationContext context: Context,
    ): ReadStateStore = DataStoreReadStateStore(context)
}
