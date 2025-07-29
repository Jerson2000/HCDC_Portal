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
import com.jerson.hcdc_portal.util.NetworkMonitor
import com.jerson.hcdc_portal.util.TimingInterceptorKt
import com.jerson.hcdc_portal.util.bypassSSLErrors
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Cache
import okhttp3.OkHttpClient
import java.io.File
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

@Module
@InstallIn(SingletonComponent::class)

object AppModule {


    @Singleton
    @Provides
    fun providesOkhttp(app: Application): OkHttpClient {
        val cacheSize = 10L * 1024 * 1024 // 10 MB
        val cache = Cache(File(app.cacheDir, "http_cache"), cacheSize)

        return OkHttpClient.Builder()
            .addInterceptor(TimingInterceptorKt())
            .cache(cache)
            .cookieJar(PersistentCookieJar(SetCookieCache(), SharedPrefsCookiePersistor(app)))
            .bypassSSLErrors()
            .retryOnConnectionFailure(true)
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    @Singleton
    @Provides
    fun providesNetworkMonitor(@ApplicationContext context:Context) = NetworkMonitor(context)

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


