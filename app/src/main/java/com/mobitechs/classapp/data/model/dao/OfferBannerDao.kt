package com.mobitechs.classapp.data.model.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mobitechs.classapp.data.model.response.OfferBanner


@Dao
interface OfferBannerDao {

    @Query("SELECT * FROM offerBanner WHERE is_active = 'Active'")
    fun getAllOfferBanner(): List<OfferBanner>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOfferBanner(offerBanner: List<OfferBanner>)


}