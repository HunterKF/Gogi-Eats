package com.example.kbbqreview.data.roomplaces

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.android.gms.maps.model.LatLng

@Entity(tableName = "stored_place_table")
class StoredPlace(
    @PrimaryKey(autoGenerate = true)
    var itemId: Long = 0L,

    @ColumnInfo(name = "firebase_id")
    var firebaseId: String = "",


    @ColumnInfo(name = "user_id")
    var userId: String = "",

    @ColumnInfo(name = "name")
    val name: String = "",

    @ColumnInfo(name = "latitude")
    val latitude: Double = 0.0,

    @ColumnInfo(name = "longitude")
    val longitude: Double = 0.0,

    @ColumnInfo(name = "address")
    val address: String = "",

    @ColumnInfo(name = "meat_quality")
    val meatQuality: Int = 0,

    @ColumnInfo(name = "banchan_quality")
    val banchanQuality: Int = 0,

    @ColumnInfo(name = "amenities_quality")
    val amenitiesQuality: Int = 0,

    @ColumnInfo(name = "atmosphere_quality")
    val atmosphereQuality: Int = 0,

    @ColumnInfo(name = "total_values")
    val totalValues: Int = 0,

    @ColumnInfo(name = "review_comment")
    val reviewComment: String = "",
)

