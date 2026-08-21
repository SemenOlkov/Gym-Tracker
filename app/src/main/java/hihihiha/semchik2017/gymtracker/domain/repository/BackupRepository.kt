package hihihiha.semchik2017.gymtracker.domain.repository

import hihihiha.semchik2017.gymtracker.data.model.AppBackup

interface BackupRepository {
    suspend fun createBackup(): AppBackup
    suspend fun restoreBackup(backup: AppBackup)
}
