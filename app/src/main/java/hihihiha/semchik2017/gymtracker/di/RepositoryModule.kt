package hihihiha.semchik2017.gymtracker.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import hihihiha.semchik2017.gymtracker.data.repository.*
import hihihiha.semchik2017.gymtracker.domain.repository.*
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindGymRepository(gymRepositoryImpl: GymRepositoryImpl): GymRepository

    @Binds
    @Singleton
    abstract fun bindNutritionRepository(nutritionRepositoryImpl: NutritionRepositoryImpl): NutritionRepository
}
