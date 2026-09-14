package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.DailyCheckEntity
import com.example.data.model.EducationArticle
import com.example.data.model.FoodScanEntity
import com.example.data.model.NutritionCheckEntity
import com.example.data.model.UksFollowUpEntity
import com.example.data.model.UserEntity

@Database(
    entities = [
        UserEntity::class,
        NutritionCheckEntity::class,
        DailyCheckEntity::class,
        FoodScanEntity::class,
        EducationArticle::class,
        UksFollowUpEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "nutrimind_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
