package playfoo.com.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
//mnjk
//mjnh
@Database(entities = [PalavraEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase()
