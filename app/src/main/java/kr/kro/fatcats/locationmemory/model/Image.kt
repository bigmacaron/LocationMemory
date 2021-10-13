package kr.kro.fatcats.locationmemory.model

import android.net.Uri
import com.naver.maps.geometry.LatLng

data class Image(
    val id: String,
    val title: String,
    val uri: Uri,
    var modelLatLng: LatLng?
)
