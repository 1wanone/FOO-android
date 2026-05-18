package playfoo.com.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "palavras")
data class PalavraEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val texto: String,
    val temaId: Int,
    val temaNome: String
)
