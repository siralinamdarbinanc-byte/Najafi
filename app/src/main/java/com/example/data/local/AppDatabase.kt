package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.DeliveryDao
import com.example.data.local.dao.DriverDao
import com.example.data.local.entity.Delivery
import com.example.data.local.entity.DeliveryPhoto
import com.example.data.local.entity.Driver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [Driver::class, Delivery::class, DeliveryPhoto::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun driverDao(): DriverDao
    abstract fun deliveryDao(): DeliveryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "shop_delivery_database"
                )
                    .addCallback(DatabaseCallback(context.applicationContext))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(private val context: Context) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialData(database.driverDao())
                    }
                }
            }

            suspend fun populateInitialData(driverDao: DriverDao) {
                if (driverDao.getDriverCount() == 0) {
                    driverDao.insertDriver(Driver(name = "علی رضایی", phone = "09121111111", description = "پیک نوبت صبح", isActive = true))
                    driverDao.insertDriver(Driver(name = "محمد حسینی", phone = "09122222222", description = "پیک نوبت عصر", isActive = true))
                    driverDao.insertDriver(Driver(name = "رضا کریمی", phone = "09123333333", description = "پیک پشتیبان", isActive = true))
                }
            }
        }
    }
}
