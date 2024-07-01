package com.jerson.hcdc_portal.di

import android.app.Application
import android.content.Context
import androidx.room.Room
import coil.ImageLoader
import com.franmontiel.persistentcookiejar.PersistentCookieJar
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
import com.jerson.hcdc_portal.data.local.PortalDB
import com.jerson.hcdc_portal.util.AppPreference
import com.jerson.hcdc_portal.util.TimingInterceptorKt
import com.jerson.hcdc_portal.util.bypassSSLErrors
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import java.security.cert.X509Certificate
import javax.inject.Singleton
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

@Module
@InstallIn(SingletonComponent::class)

object AppModule {


    @Singleton
    @Provides
    fun providesOkhttp(app: Application) = OkHttpClient.Builder()
        .addInterceptor(TimingInterceptorKt())
        .cookieJar(PersistentCookieJar(SetCookieCache(),SharedPrefsCookiePersistor(app)))
        .bypassSSLErrors()
        .build()

    @Singleton
    @Provides
    fun providesPortalDB(app:Application):PortalDB = Room.databaseBuilder(app,PortalDB::class.java,"portal.db").build()

    @Provides
    @Singleton
    fun providesAppPreference(application:Application):AppPreference{
        return AppPreference(application.applicationContext)
    }

    @Provides
    fun providesImageLoader(app:Application): ImageLoader {
        return ImageLoader.Builder(app)
            .okHttpClient(OkHttpClient())
            .build()
    }


}


