package bg.crc.roamingapp.app

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices

object FuseLocationUpdate {
    // FusedLocationProviderClient - Main class for receiving location updates.
      var fusedLocationProviderClient: MutableLiveData<FusedLocationProviderClient> = MutableLiveData()


    // updates, the priority, etc.
      var locationRequest: LocationRequest? = null

    // LocationCallback - Called when FusedLocationProviderClient has a new Location.
      var locationCallback: LocationCallback? =null

    fun createFuseLocationService(context : Context){
        // Review the FusedLocationProviderClient.
        fusedLocationProviderClient.postValue(LocationServices.getFusedLocationProviderClient(context))
    }
}