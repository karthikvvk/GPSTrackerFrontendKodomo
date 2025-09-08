package com.example.kodomo.data

class BacKodomoRepository(private val dao: BacDatabaseDao) {

    suspend fun getAllLogs(): List<BacDataclassDb> {
        return dao.getAll()
    }

    suspend fun addOrUpdateLog(log: BacDataclassDb) {
        dao.upsertLog(log)
    }

    suspend fun deleteLog(log: BacDataclassDb) {
        dao.deleteLog(log)
    }

    suspend fun getAllDays(): List<String> {
        return dao.getDays()
    }


}
