package com.jerson.hcdc_portal.di

import com.jerson.hcdc_portal.data.repository.AccountsRepositoryImpl
import com.jerson.hcdc_portal.data.repository.EnrollHistoryRepositoryImpl
import com.jerson.hcdc_portal.data.repository.EvaluationRepositoryImpl
import com.jerson.hcdc_portal.data.repository.GradesRepositoryImpl
import com.jerson.hcdc_portal.data.repository.LoginRepositoryImpl
import com.jerson.hcdc_portal.data.repository.SchedulesRepositoryImpl
import com.jerson.hcdc_portal.data.repository.SubjectOfferedRepositoryImpl
import com.jerson.hcdc_portal.domain.repository.AccountsRepository
import com.jerson.hcdc_portal.domain.repository.EnrollHistoryRepository
import com.jerson.hcdc_portal.domain.repository.EvaluationRepository
import com.jerson.hcdc_portal.domain.repository.GradesRepository
import com.jerson.hcdc_portal.domain.repository.LoginRepository
import com.jerson.hcdc_portal.domain.repository.SchedulesRepository
import com.jerson.hcdc_portal.domain.repository.SubjectOfferedRepository
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

    @Binds
    @ViewModelScoped
    abstract fun bindsAccountsRepository(accountsRepositoryImpl: AccountsRepositoryImpl):AccountsRepository

    @Binds
    @ViewModelScoped
    abstract fun bindsGradesRepository(gradesRepositoryImpl: GradesRepositoryImpl):GradesRepository

    @Binds
    @ViewModelScoped
    abstract fun bindsEnrollHistoryRepository(enrollHistoryRepositoryImpl: EnrollHistoryRepositoryImpl): EnrollHistoryRepository

    @Binds
    @ViewModelScoped
    abstract fun bindsEvaluationRepository(evalRepositoryImpl:EvaluationRepositoryImpl):EvaluationRepository

    @Binds
    @ViewModelScoped
    abstract fun bindsSubjectOfferedRepository(subjectOfferedRepositoryImpl:SubjectOfferedRepositoryImpl):SubjectOfferedRepository

}