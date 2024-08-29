package io.github.dracula101.jetscan.data.document.datasource.disk.database

import androidx.room.Database
import androidx.room.RoomDatabase
import io.github.dracula101.jetscan.data.document.datasource.disk.dao.DocumentDao
import io.github.dracula101.jetscan.data.document.datasource.disk.dao.DocumentFolderDao
import io.github.dracula101.jetscan.data.document.datasource.disk.entity.DocumentEntity
import io.github.dracula101.jetscan.data.document.datasource.disk.entity.DocumentFolderEntity
import io.github.dracula101.jetscan.data.document.datasource.disk.entity.ScannedImageEntity
import io.github.dracula101.jetscan.data.document.utils.DBConstants


@Database(
    entities = [DocumentEntity::class, ScannedImageEntity::class, DocumentFolderEntity::class],
    version = DBConstants.DocDB.version,
    exportSchema = true
)
abstract class DocumentDatabase : RoomDatabase() {
    abstract val documentDao: DocumentDao
    abstract val foldersDao: DocumentFolderDao
}