package com.example.android.car_locator.di

import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.android.car_locator.constants.CONSTANTS
import com.example.android.car_locator.database.room_dao.LocalDatabaseDao
import com.example.android.car_locator.database.room_db.LocalDatabase
import com.example.android.car_locator.repository.preferences.AppPreferences
import com.example.android.car_locator.repository.back_end.RetrofitApi
import com.example.android.car_locator.repository.local_db.LocalDbRepository
import com.example.android.car_locator.ui.SharedViewModel
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val myModules = module {

    single<LocalDatabase> {
        Room.databaseBuilder(
            androidApplication(),
            LocalDatabase::class.java,
            CONSTANTS.LOCAL_DATABASE_NAME
        ).setJournalMode(RoomDatabase.JournalMode.TRUNCATE)
            .build()
    }

    single<LocalDatabaseDao> { get<LocalDatabase>().localDatabaseDao() }

    single { LocalDbRepository(localDatabaseDao = get()) }

    single {
        Retrofit.Builder()
            .baseUrl("https://mobi.connectedcar360.net/")
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().setLenient().create()))
            .build()
            .create(RetrofitApi::class.java)
    }

    single { AppPreferences(context = get()) }

    viewModel {
        SharedViewModel(
            preferences = get(),
            localDbRepository = get(),
            retrofitApi = get()
        )
    }
}