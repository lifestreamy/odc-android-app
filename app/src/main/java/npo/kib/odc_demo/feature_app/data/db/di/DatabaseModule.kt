package npo.kib.odc_demo.feature_app.data.db.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import npo.kib.odc_demo.feature_app.data.db.BlockchainDatabase
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    //BlockchainDatabase with BlockDao, BanknotesDao
    @Singleton
    @Provides
    fun provideBlockchainDatabase(
        @ApplicationContext
        context: Context
    ): BlockchainDatabase = Room.databaseBuilder(
        context, BlockchainDatabase::class.java, BlockchainDatabase.DATABASE_NAME
    ).build()

}