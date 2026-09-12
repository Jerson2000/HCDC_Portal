package com.jerson.hcdc_portal.di

import com.jerson.hcdc_portal.data.parser.AccountParser
import com.jerson.hcdc_portal.data.parser.AccountParserImpl
import com.jerson.hcdc_portal.data.parser.EnrollHistoryParser
import com.jerson.hcdc_portal.data.parser.EnrollHistoryParserImpl
import com.jerson.hcdc_portal.data.parser.GradeParser
import com.jerson.hcdc_portal.data.parser.GradeParserImpl
import com.jerson.hcdc_portal.data.parser.ScheduleParser
import com.jerson.hcdc_portal.data.parser.ScheduleParserImpl
import com.jerson.hcdc_portal.data.parser.UserSessionParser
import com.jerson.hcdc_portal.data.parser.UserSessionParserImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ParserModule {

    @Binds
    @Singleton
    abstract fun bindScheduleParser(impl: ScheduleParserImpl): ScheduleParser

    @Binds
    @Singleton
    abstract fun bindGradeParser(impl: GradeParserImpl): GradeParser

    @Binds
    @Singleton
    abstract fun bindAccountParser(impl: AccountParserImpl): AccountParser

    @Binds
    @Singleton
    abstract fun bindEnrollHistoryParser(impl: EnrollHistoryParserImpl): EnrollHistoryParser

    @Binds
    @Singleton
    abstract fun bindUserSessionParser(impl: UserSessionParserImpl): UserSessionParser
}
