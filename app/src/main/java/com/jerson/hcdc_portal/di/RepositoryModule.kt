package com.jerson.hcdc_portal.di

import com.jerson.hcdc_portal.data.repository.LoginRepositoryImpl
import com.jerson.hcdc_portal.data.repository.SchedulesRepositoryImpl
import com.jerson.hcdc_portal.domain.repository.LoginRepository
import com.jerson.hcdc_portal.domain.repository.SchedulesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
abstract class RepositoryModule {

    @Binds
    @ViewModelScoped
    abstract fun bindsLoginRepository(loginRepositoryImpl: LoginRepositoryImpl):LoginRepository

    @Binds
    @ViewModelScoped
    abstract fun bindsSchedulesRepository(schedulesRepositoryImpl: SchedulesRepositoryImpl):SchedulesRepository
}