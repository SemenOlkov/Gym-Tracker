package hihihiha.semchik2017.gymtracker.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import hihihiha.semchik2017.gymtracker.data.GymDatabase
import hihihiha.semchik2017.gymtracker.data.dao.GymDao
import hihihiha.semchik2017.gymtracker.data.dao.NutritionDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): GymDatabase {
        return GymDatabase.getDatabase(context, CoroutineScope(SupervisorJob()))
    }

    @Provides
    fun provideGymDao(database: GymDatabase): GymDao {
        return database.gymDao()
    }

    @Provides
    fun provideNutritionDao(database: GymDatabase): NutritionDao {
        return database.nutritionDao()
    }
}
