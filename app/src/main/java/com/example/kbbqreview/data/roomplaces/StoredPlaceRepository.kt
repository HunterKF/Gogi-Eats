package com.example.kbbqreview.data.roomplaces

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.*

class StoredPlaceRepository(private val storedPlaceDao: StoredPlaceDao) {
    val readAllData: LiveData<List<StoredPlace>> = storedPlaceDao.getAllItems()

    val searchResults = MutableLiveData<List<StoredPlace>>()

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    suspend fun addStoredItem(storedPlace: StoredPlace) {
        storedPlaceDao.insert(storedPlace)
    }

    suspend fun deleteStoredItem(storedPlace: StoredPlace) {
        storedPlaceDao.delete(storedPlace)
    }

    fun searchStoredItem(id: Long) {
        coroutineScope.launch(Dispatchers.Main) {
            searchResults.value = asyncFind(id).await()
        }
    }

    private fun asyncFind(id: Long): Deferred<List<StoredPlace>?> =
        coroutineScope.async(Dispatchers.IO) {
            return@async storedPlaceDao.searchStoredItem(id)
        }

}