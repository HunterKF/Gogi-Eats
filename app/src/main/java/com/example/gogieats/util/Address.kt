package com.example.gogieats.util

import android.content.Context
import android.location.Address
import android.location.Geocoder
import java.util.*

class AddressMap {
    companion object {
        fun getAddressFromLocation(context: Context, lat: Double, long: Double): String {

            val geocoder = Geocoder(context, Locale.getDefault())
            var addresses: List<Address>? = null
            var addressText = ""

            try {
                addresses = geocoder.getFromLocation(
                    lat,
                    long,
                    1
                )
            } catch (ex: Exception) {
                ex.printStackTrace()
            }

            val address: Address? = addresses?.get(0)
            address?.let {
                addressText = "${address!!.subThoroughfare} ${address.thoroughfare}, ${address.subLocality}, ${address.adminArea}"
            }

            return addressText
        }
    }
}