package com.fkg.smarttooth.di

import com.fkg.smarttooth.data.firebase.FirebaseUtil
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideFirebaseUtils() : FirebaseUtil = FirebaseUtil
}