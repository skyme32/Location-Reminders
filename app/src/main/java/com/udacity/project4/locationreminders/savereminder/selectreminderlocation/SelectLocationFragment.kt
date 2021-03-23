package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PointOfInterest
import com.google.android.gms.tasks.Task
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import com.udacity.project4.utils.setTitle
import org.koin.android.ext.android.inject
import java.util.*


class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding

    // variable about location
    private lateinit var map: GoogleMap
    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient
    private var mLastKnownLocation: Location? = null
    private var latLngFinally: PointOfInterest? = null

    companion object {
        const val TAG = "SelectLocationSelect"
        const val REQUEST_LOCATION_PERMISSION = 1
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_select_location,
            container,
            false
        )

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        // Toolbar Action bar
        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)
        setTitle(getString(R.string.add_location))

        val mapFragment = childFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        mFusedLocationProviderClient = FusedLocationProviderClient(requireActivity())
        binding.buttonSave.setOnClickListener {
            onLocationSelected()
        }

        return binding.root
    }


    private fun onLocationSelected() {
        Log.d(TAG, "latLngFinally: $latLngFinally")
        if (latLngFinally != null) {
            _viewModel.selectLocation(latLngFinally!!)
            _viewModel.navigationCommand.postValue(NavigationCommand.Back)
        } else {
            _viewModel.showSnackBarInt.postValue(R.string.select_location)
        }
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.normal_map -> {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        map = googleMap!!
        map.uiSettings?.isMyLocationButtonEnabled = false
        map.uiSettings?.isMapToolbarEnabled = false

        GoogleMapOptions().mapType(GoogleMap.MAP_TYPE_SATELLITE)
            .compassEnabled(true)
            .rotateGesturesEnabled(true)
            .tiltGesturesEnabled(true)

        binding.floatLocation.setOnClickListener {
            enableMyLocation()
        }

        //call this function after the user confirms on the selected location
        enableMyLocation()

        //add style to the map
        setMapStyle(map)

        //put a marker to location that the user selected
        setMapLongClick(map)
        setPoiClick(map)
    }


    private fun setMapLongClick(map: GoogleMap) {
        map.setOnMapLongClickListener { latLng ->
            createMArker(latLng, getString(R.string.dropped_pin))
        }
    }

    private fun setPoiClick(map: GoogleMap) {
        map.setOnPoiClickListener { poi ->
            createMArker(poi.latLng, poi.name)
        }
    }


    private fun createMArker(latLng: LatLng, title: String) {
        map.clear()

        // A Snippet is Additional text that's displayed below the title.
        val snippet = String.format(
            Locale.getDefault(),
            geoCodeLocation(latLng),
            latLng.latitude, latLng.longitude
        )

        val marker = map.addMarker(
            MarkerOptions()
                .position(latLng)
                .title(title)
                .snippet(snippet)
        )

        marker.showInfoWindow()
        latLngFinally = PointOfInterest(latLng, snippet, title)
    }


    private fun setMapStyle(map: GoogleMap) {
        try {
            // Customize the styling of the base map using a JSON object defined
            // in a raw resource file.
            map.setMapStyle(MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style))
        } catch (e: Resources.NotFoundException) {
            Log.e(TAG, "Can't find style. Error: ", e)
        }
    }


    private fun isPermissionGranted() : Boolean {
        return checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }


    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        if (isPermissionGranted()) {
            map.isMyLocationEnabled = true
            getDeviceLocation()
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        // Check if location permissions are granted and if so enable the
        // location data layer.
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                enableMyLocation()
            }
        }
    }

    /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
    private fun getDeviceLocation() {
        try {
                val locationResult: Task<Location> = mFusedLocationProviderClient.lastLocation
                locationResult.addOnCompleteListener(requireActivity()) { task ->

                    if (task.isSuccessful) {
                        // Set the map's camera position to the current location of the device.
                        mLastKnownLocation = task.result
                        if (mLastKnownLocation != null) {
                            val coordinate = LatLng(
                                mLastKnownLocation!!.latitude,
                                mLastKnownLocation!!.longitude
                            )
                            val location = CameraUpdateFactory.newLatLngZoom(
                                coordinate, 17f
                            )
                            map.animateCamera(location)
                            createMArker(coordinate, getString(R.string.location_pin))
                        } else {
                            _viewModel.showSnackBarLoctInt.postValue(R.string.location_required_error)
                            Log.d(TAG, "Current location is null. Using defaults.")
                        }
                    }
                }
        } catch (e: SecurityException) {
            Log.e(TAG, e.message!!)
        }
    }


    private fun geoCodeLocation(latLng: LatLng): String {
        val geocoder = Geocoder(context, Locale.getDefault())
        return geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            .map { address ->
                "${address.thoroughfare}, ${address.subThoroughfare}, ${address.postalCode}"
            }
            .first()
    }


}
