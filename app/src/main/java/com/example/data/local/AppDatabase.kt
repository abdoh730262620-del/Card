package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.AppUser
import com.example.data.model.CardCategory
import com.example.data.model.CardSwapRecord
import com.example.data.model.Distributor
import com.example.data.model.EmployeeTransferRecord
import com.example.data.model.NetworkSettings
import com.example.data.model.SystemNotification
import com.example.data.model.TransactionRecord
import com.example.data.model.TransactionItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        CardCategory::class,
        Distributor::class,
        TransactionRecord::class,
        TransactionItem::class,
        CardSwapRecord::class,
        EmployeeTransferRecord::class,
        AppUser::class,
        SystemNotification::class,
        NetworkSettings::class,
        com.example.data.model.CashRegister::class,
        com.example.data.model.FundMovement::class,
        com.example.data.model.EmployeeStock::class
    ],
    version = 7,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun netCardsDao(): NetCardsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE distributors ADD COLUMN discountPercent REAL NOT NULL DEFAULT 0.0")
            }
        }

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE app_users ADD COLUMN canAddSales INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE app_users ADD COLUMN canEditSales INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE app_users ADD COLUMN canDeleteSales INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE app_users ADD COLUMN canAddPurchases INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE app_users ADD COLUMN canEditPurchases INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE app_users ADD COLUMN canDeletePurchases INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE app_users ADD COLUMN canAddVouchers INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE app_users ADD COLUMN canEditVouchers INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE app_users ADD COLUMN canDeleteVouchers INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE app_users ADD COLUMN canAddSwaps INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE app_users ADD COLUMN canEditSwaps INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE app_users ADD COLUMN canDeleteSwaps INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE app_users ADD COLUMN canAddTransfers INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE app_users ADD COLUMN canEditTransfers INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE app_users ADD COLUMN canDeleteTransfers INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE app_users ADD COLUMN canAddReports INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE app_users ADD COLUMN canEditReports INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE app_users ADD COLUMN canDeleteReports INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE app_users ADD COLUMN canAddCards INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE app_users ADD COLUMN canEditCards INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE app_users ADD COLUMN canDeleteCards INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE app_users ADD COLUMN email TEXT NOT NULL DEFAULT ''")
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "netcards_database.db"
                )
                    .addMigrations(MIGRATION_2_3, MIGRATION_5_6, MIGRATION_6_7)
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        InitialDataSeeder.seedDatabase(database.netCardsDao())
                    }
                }
            }
        }
    }
}
