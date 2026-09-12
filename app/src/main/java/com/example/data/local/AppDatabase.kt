package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.DailyCheckEntity
import com.example.data.model.EducationArticle
import com.example.data.model.FoodScanEntity
import com.example.data.model.NutritionCheckEntity
import com.example.data.model.UksFollowUpEntity
import com.example.data.model.UserEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                val dao = getDatabase(context).appDao()
                                // Prepopulate sample users
                                for (user in PrepopulateData.sampleUsers) {
                                    dao.insertUser(user)
                                }
                                // Prepopulate sample nutrition checks
                                for (check in PrepopulateData.sampleNutritionChecks) {
                                    dao.insertNutritionCheck(check)
                                }
                                // Prepopulate education articles
                                dao.insertArticles(PrepopulateData.articles)
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
