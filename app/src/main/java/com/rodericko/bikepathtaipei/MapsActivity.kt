package com.rodericko.bikepathtaipei

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Criteria
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.MenuCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction.TRANSIT_FRAGMENT_OPEN
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.google.maps.android.ui.IconGenerator
import com.rodericko.bikepathtaipei.databinding.ActivityMapsBinding
import kotlinx.android.synthetic.main.exit_info.view.*
import java.util.*


internal class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private val mREQUESTLOCATIONPERMISSION  = 1
    private lateinit var binding: ActivityMapsBinding



    override fun onCreate(savedInstanceState: Bundle?) {

            super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        mapFragment.activity?.title = "Where's the nearest exit?"

        checkPlayServices()

    }


    override fun onSupportNavigateUp(): Boolean {
        if (supportFragmentManager.popBackStackImmediate()) {
            supportActionBar?.setDisplayHomeAsUpEnabled(false)
            supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_TITLE
            supportFragmentManager.popBackStack()
            setTitle(R.string.exit_question)

            // Handle Reset All Markers command from settings menu

            val sharedPref = getPreferences(MODE_PRIVATE)
            val dDefaultMarkerState = 0
            val dCurrentMarkerState = sharedPref.getInt(getString(R.string.reset_markers), dDefaultMarkerState)
            if (dCurrentMarkerState == 1) {
                mMap.clear()
                applyUserSettings()
                }
            with (sharedPref.edit()) {
                putInt(getString(R.string.reset_markers), dDefaultMarkerState)
                apply()
                }

            // Handle user customization of map styles from settings menu

            applyUserSettings()
            return true
        }
        return super.onSupportNavigateUp()
    }



    /**
     * ~~~~~~~ Do stuff on the map once available. ~~~~~~~
     */

    @SuppressLint("MissingPermission", "InflateParams")
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        setMapLongClick(mMap)
        setPoiClick(mMap)
        applyUserSettings()

        val mGOLatLng = LatLng(25.12299, 121.46196)
        val mGOMapOpt = GroundOverlayOptions()
            .image(BitmapDescriptorFactory.fromResource(R.drawable.sampleimage))
            .position(mGOLatLng, 50f)
            .clickable(true)


        mMap.addGroundOverlay(mGOMapOpt)?.tag = "haha"
        mMap.setOnGroundOverlayClickListener {

           if (it.tag == "haha") {
              oToast("This is a haha message")
           }
        }


        /**
        * ~~~~~~~ Enable location data layer. ~~~~~~~
        * Get 'last known location' details from LocationManager.
        * Use that data to zoom to user's last known location.
        * If no last known location, show the whole map.
        */

        enableMyLocation()
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        val criteria = Criteria()
        val provider = locationManager.getBestProvider(criteria, true)
        val location: Location? =
            try { locationManager.getLastKnownLocation(provider!!) }
            catch (e: NullPointerException) { null }


        if (location != null) {
            val latitude = location.latitude
            val longitude = location.longitude
            val coordinate = LatLng(latitude, longitude)
            val yourLocation = CameraUpdateFactory.newLatLngZoom(coordinate, 19f)
            mMap.animateCamera(yourLocation)
        }

    }





    // region ➕ ~~~~~~~ Permissions ~~~~~~~
    /**
    * ~~~~~~~ Create a brand new section just to handle permissions. ~~~~~~~
    */


    // Checks that users have given permission
    private fun isPermissionGranted() : Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    // Callback for the result from requesting permissions.
    // This method is invoked for every call on
    // requestPermissions(android.app.Activity, String[], int).
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // Check if location permissions are granted and if so enable the
        // location data layer.
        if (requestCode == mREQUESTLOCATIONPERMISSION ) {
            if (grantResults.contains(PackageManager.PERMISSION_GRANTED)) {
                enableMyLocation()

                Toast.makeText(
                    applicationContext,
                    ("\ud83d\ude01")+"  Restart app to apply updated permissions",
                    Toast.LENGTH_LONG
                ).show()

            }
        }
    }

    // Checks if users have given their location and sets location enabled if so.
    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        if (isPermissionGranted()) {
            mMap.isMyLocationEnabled = true
        }
        else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                mREQUESTLOCATIONPERMISSION
            )
        }
    }

    /**
    * ~~~~~~~ end of permissions section ~~~~~~~
    */
    // endregion permissions x


    private fun checkPlayServices(): Boolean {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val resultCode = apiAvailability.isGooglePlayServicesAvailable(this)
        val currentView: View = this.findViewById(android.R.id.content)

        class CustomSnackBarAction : View.OnClickListener {

            override fun onClick(v: View) {
                oToast("This device is not supported.")
            }
        }

        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                val snackBar = Snackbar.make(
                    currentView,
                    "I'm working on a version that doesn't require Google Play Services. Thank you for your patience.",
                    Snackbar.LENGTH_INDEFINITE
                )
                snackBar.setAction("OK", CustomSnackBarAction())
                snackBar.setActionTextColor(Color.WHITE)
                snackBar.setTextColor(Color.YELLOW)
                snackBar.setMaxInlineActionWidth(500)
                val snackBarView = snackBar.view
                snackBarView.setBackgroundColor(Color.DKGRAY)
                val textView =
                    snackBarView.findViewById(com.google.android.material.R.id.snackbar_text) as TextView
                textView.setTextColor(Color.YELLOW)
                textView.textSize = 28f
                textView.maxLines = 5
                snackBar.show()
            } else {
                oToast("This device is not supported.")
                finish()
            }
            return false
        }
        return true
    }



    /**
     * INITIALIZE THE OPTIONS MENU.
     */

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        val inflater = menuInflater
        inflater.inflate(R.menu.overflow_menu, menu)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            menu?.setGroupDividerEnabled(true)
        }
        MenuCompat.setGroupDividerEnabled(menu, true)

        return true
    }


    /**
     * CONFIGURE BEHAVIOR OF ITEMS IN THE OPTIONS MENU.
     */
    // Called whenever an item in your options menu is selected.
    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {

        R.id.normal_map -> {
            changeMapStyle(0, 0)
            true
        }
        R.id.hybrid_map -> {
            changeMapStyle(2)
            true
        }
        R.id.satellite_map -> {
            changeMapStyle(1)
            true
        }
        R.id.minimal_map -> {
            changeMapStyle(0, 1)
            true
        }
        R.id.marker_circles -> {
            addCirclesOnTheMap()
            true
        }
        R.id.marker_markers -> {
            addMarkersOnTheMap()
            true
        }
        R.id.marker_numbers -> {
            addNumbersOnTheMap()
            true
        }
        R.id.settings_dd_menu -> {


            supportFragmentManager
                .beginTransaction()
                .setTransition(TRANSIT_FRAGMENT_OPEN)
                .setReorderingAllowed(true)
                .addToBackStack("DefaultView")
                .add(R.id.map, PrefBackgroundFragment())
                .commit()


            supportFragmentManager.addOnBackStackChangedListener {
                if (supportFragmentManager.backStackEntryCount == 0) {
                    setTitle(R.string.settings)
                }
            }

            supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
            supportActionBar?.setCustomView (R.layout.pref_toolbar)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)

            true
        }

        else -> super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        onSupportNavigateUp()
    }


    /**
     * CONFIGURE LONG PRESS.
     */

    // Called when user makes a long press gesture on the map

    private fun setMapLongClick(map: GoogleMap) {
        map.setOnMapLongClickListener { latLng ->

            val snippet = String.format(
                Locale.getDefault(),
                "Lat: %1$.5f, Long: %2$.5f",
                latLng.latitude,
                latLng.longitude
            )
            map.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title(getString(R.string.dropped_pin))
                    .snippet(snippet)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
            )
        }
    }


    /**
     * ADD MARKER ON MAP WHEN USER CLICKS.
     */

    // Places a marker on the map and displays an info window that contains POI name

    private fun setPoiClick(map: GoogleMap) {
        map.setOnPoiClickListener { poi ->
            val poiMarker = map.addMarker(
                MarkerOptions()
                    .position(poi.latLng)
                    .title(poi.name)
            )
            poiMarker?.showInfoWindow()
        }
    }



    private fun changeMapStyle(x: Int = 0, y: Int = 0, z: Int = 0) {

        // Save new settings in our preferences file

        val sharedPref = getPreferences(MODE_PRIVATE)
        with (sharedPref.edit()) {
            putInt(getString(R.string.MapTypes), x)
            putInt(getString(R.string.MapStyles), y)
            apply()
        }

        // Apply new map style (x and y)

        val style = MapStyleOptions.loadRawResourceStyle(this, mMapStyleStyles[y])
        mMap.setMapStyle(style)
        mMap.mapType = mMapStyleTypes[x]

        // Apply new marker styles (z)

        when (sharedPref.getInt(getString(R.string.MarkerType), z)) {
            0 -> addCirclesOnTheMap()
            1 -> addMarkersOnTheMap()
            2 -> addNumbersOnTheMap()
            else -> {
                oToast(getString(R.string.not_implemented_yet))
            }
        }
    }


    private fun createCircleMarks(mPoint01: Any, mPoint02: Any, str: Any, m: Int = 0) {

        val mLat =  mPoint01 as Double
        val mLang = mPoint02 as Double
        val mTagged = str as Int

        val mConvPoint = LatLng(mLat, mLang)

        val circles = CircleOptions()
            .radius(10.0)
            .clickable(true)

        if ( m == 1 ) {
            circles
                .strokeColor(0x00000000)
                .zIndex(5F)
           }

         val mPlacedCircle = mMap.addCircle(circles.center(mConvPoint))
         mPlacedCircle.tag = mTagged

    }



    private fun addCirclesOnTheMap() {

        mMap.clear()

        for (i in 0..120) {
            val mLat = mTwoDee[i][0]
            val mLon = mTwoDee[i][1]
            createCircleMarks(mLat, mLon, i, 0)
            }

        mMap.setOnCircleClickListener {
            val hTagged = it.tag as Int
            showBottomDrawer(hTagged)
        }

        val sharedPref = getPreferences(MODE_PRIVATE)
        with (sharedPref.edit()) {
            putInt(getString(R.string.MarkerType), 0)
            apply()
        }

    }

    @SuppressLint("PotentialBehaviorOverride")
    private fun addMarkersOnTheMap() {

        mMap.clear()

        for (i in 0..120) {
            val mLat  = mTwoDee[i][0]
            val mLang = mTwoDee[i][1]

            createCircleMarks(mLat, mLang, i, 1)

            mLat as Double
            mLang as Double
            val mConvPoint = LatLng(mLat, mLang)

            val mPlacedMarker = mMap.addMarker(
                MarkerOptions()
                    .position(mConvPoint)
                    .zIndex(0F)
            )
            mPlacedMarker?.tag = i
        }

        mMap.setOnMarkerClickListener {
            val hTagged = it.tag as Int
            showBottomDrawer(hTagged)
            true
        }

        val sharedPref = getPreferences(MODE_PRIVATE)
        with (sharedPref.edit()) {
            putInt(getString(R.string.MarkerType), 1)
            apply()
        }

    }

    @SuppressLint("PotentialBehaviorOverride")
    private fun addNumbersOnTheMap() {

        mMap.clear()

        for (i in 0..120) {
            val mLat  = mTwoDee[i][0]
            val mLang = mTwoDee[i][1]

            createCircleMarks(mLat, mLang, i, 1)

            mLat as Double
            mLang as Double
            val mConvPoint = LatLng(mLat, mLang)

            val numeral = IconGenerator(applicationContext)
            numeral.setColor(0xfff00000.toInt())
            numeral.setTextAppearance(R.style.numeralStyle)

            val mPlacedNumber = mMap.addMarker(
                MarkerOptions()
                    .position(mConvPoint)
                    .zIndex(0F)
                    .icon(BitmapDescriptorFactory.fromBitmap(numeral.makeIcon("$i")))
            )
            mPlacedNumber?.tag = i

        }

        mMap.setOnMarkerClickListener {
            val hTagged = it.tag as Int
            showBottomDrawer(hTagged)
            true
        }

        val sharedPref = getPreferences(MODE_PRIVATE)
        with (sharedPref.edit()) {
            putInt(getString(R.string.MarkerType), 2)
            apply()
        }

    }

    @SuppressLint("InflateParams")
    private fun showBottomDrawer(hTagged: Int) {

        val hAddress = mTwoDee[hTagged][3] as String
        val hBottom = layoutInflater.inflate(R.layout.exit_info, null)
        hBottom.kAddress_Line.text = hAddress
        val dialog = BottomSheetDialog(this)
        dialog.setContentView(hBottom)

        hBottom.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()

        hBottom.kStreetViewLink.setOnClickListener {

            val hStreetURL = mTwoDee[hTagged][2] as String
            val openURL = Intent(Intent.ACTION_VIEW)
            openURL.data = Uri.parse(hStreetURL)
            startActivity(openURL)

        }

    }

    private fun applyUserSettings() {

        val sharedPref = getPreferences(MODE_PRIVATE)
        val dDefaultMapType = 0
        val x = sharedPref.getInt(getString(R.string.MapTypes), dDefaultMapType)
        val y = sharedPref.getInt(getString(R.string.MapStyles), dDefaultMapType)
        val z = sharedPref.getInt(getString(R.string.MarkerType), dDefaultMapType)
        changeMapStyle( x, y, z )
        when (z) {
            0 -> addCirclesOnTheMap()
            1 -> addMarkersOnTheMap()
            2 -> addNumbersOnTheMap()
            else -> {
                oToast(getString(R.string.not_implemented_yet))
                addCirclesOnTheMap()
            }
        }

    }


    private fun oToast(text: String) {
        Toast.makeText(applicationContext,text,Toast.LENGTH_LONG).show()
    }

    /**
     * ~~~~~~~ Last brace below ~~~~~~~
     */

}

class PrefBackgroundFragment : Fragment(R.layout.white_bg)

class PrefSettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings, rootKey)

        // fun oToast(text: String) {
        //     Toast.makeText(context,text,Toast.LENGTH_LONG).show()
        // }


        // Get current pref values

        val sharedPref = activity?.getPreferences(AppCompatActivity.MODE_PRIVATE) ?: return
        val dDefaultMapType = 0
        val dCurrentTypes = sharedPref.getInt(getString(R.string.MapTypes), dDefaultMapType)
        val dCurrentStyle = sharedPref.getInt(getString(R.string.MapStyles), dDefaultMapType)

        // Update map style pref to show current map style values

        val mapStyleOption: ListPreference? = findPreference("mapStyleList")
        mapStyleOption?.title = getString(mMapStyleSummary[dCurrentTypes])
        mapStyleOption?.value = "$dCurrentTypes"
        if (dCurrentStyle == 1 ) {
            mapStyleOption?.title = getString(mMapStyleSummary[3])
            mapStyleOption?.value = "3"
            }

        // Do stuff when user clicks on option for map styles

        mapStyleOption?.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _, newValue ->
                val x: Int = newValue.toString().toInt()

                mapStyleOption?.title = getString(mMapStyleSummary[x])
                with (sharedPref.edit()) {
                    putInt(getString(R.string.MapTypes), x)
                    if (x == 3) { putInt(getString(R.string.MapStyles), 1) }
                    else        { putInt(getString(R.string.MapStyles), 0) }
                    apply()
                    }

                true
            }


        // Update marker pref to show currently selected marker types

        val dCurrentMarks = sharedPref.getInt(getString(R.string.MarkerType), dDefaultMapType)
        val markerTypeOption: ListPreference? = findPreference("markerTypeList")
        markerTypeOption?.title = getString(mMapMarkerTypes[dCurrentMarks])
        markerTypeOption?.value = "$dCurrentMarks"

        // Do stuff when user clicks on option for marker types

        markerTypeOption?.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _, newValue ->
                val x: Int = newValue.toString().toInt()

                markerTypeOption?.title = getString(mMapMarkerTypes[x])
                with (sharedPref.edit()) {
                    putInt(getString(R.string.MarkerType), x)
                    apply()
                }

                true
            }


        val vMapStyles: Preference? = findPreference(getString(R.string.YouBike))
        vMapStyles?.onPreferenceClickListener =
            Preference.OnPreferenceClickListener {

                it.setIcon(R.drawable.icon_radio_grey)
                true
        }


        // Do stuff when user clicks the Reset All Markers button

        val vResetMarkers: Preference? = findPreference(getString(R.string.reset_markers))
        vResetMarkers?.onPreferenceClickListener =
            Preference.OnPreferenceClickListener {

                Toast.makeText(context,"All markers removed from the map",Toast.LENGTH_LONG).show()

                val defaultValue = 0
                sharedPref.getInt(getString(R.string.reset_markers), defaultValue)
                with (sharedPref.edit()) {
                    putInt(getString(R.string.reset_markers), 1)
                    apply()
                }

                true
        }



    }
}
