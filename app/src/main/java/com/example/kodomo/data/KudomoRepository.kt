package com.example.kodomo.data

class KodomoRepository(private val dao: DatabaseDao) {

    suspend fun getAllLogs(): List<DataclassDb> {
        return dao.getAll()
    }

    suspend fun addOrUpdateLog(log: DataclassDb) {
        dao.upsertLog(log)
    }

    suspend fun deleteLog(log: DataclassDb) {
        dao.deleteLog(log)
    }

    suspend fun getAllDays(): List<String> {
        return dao.getDays()
    }


}
