package de.sixbits.pixaclient.di

import android.app.Application
import androidx.room.Room
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.RequestOptions
import dagger.Module
import dagger.Provides
import de.sixbits.pixaclient.config.Consts
import de.sixbits.pixaclient.database.DatabaseComponent
import de.sixbits.pixaclient.database.database.CacheDatabase
import de.sixbits.pixaclient.main.MainComponent
import de.sixbits.pixaclient.network.NetworkComponent
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@Module(
    subcomponents = [
        NetworkComponent::class,
        MainComponent::class,
        DatabaseComponent::class
    ]
)
open class AppModule {
    @Singleton
    @Provides
    fun provideRetrofitInstance(): Retrofit {
        return Retrofit
            .Builder()
            .baseUrl(Consts.API_ROOT)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
    }

    @Singleton
    @Provides
    fun provideGlideInstance(
        application: Application,
        requestOptions: RequestOptions
    ): RequestManager {
        return Glide.with(application)
            .setDefaultRequestOptions(requestOptions)
    }

    @Singleton
    @Provides
    fun provideDatabase(application: Application): CacheDatabase {
        return Room.databaseBuilder(
            application,
            CacheDatabase::class.java,
            "cache-database.db"
        ).build()
    }
}