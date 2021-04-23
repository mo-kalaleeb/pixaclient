package de.sixbits.pixaclient.main.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import de.sixbits.pixaclient.database.dao.CacheDao
import de.sixbits.pixaclient.network.manager.PixabayManager
import de.sixbits.pixaclient.network.model.ImageListItemModel
import io.reactivex.Observable
import javax.inject.Inject

class MainRepository @Inject constructor(private val pixabayManager: PixabayManager) {
    fun searchFor(query: String) : Observable<List<ImageListItemModel>> {
        return this.pixabayManager.getSearchResult(query)
    }
}