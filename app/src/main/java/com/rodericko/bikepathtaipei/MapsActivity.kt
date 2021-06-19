package com.rodericko.bikepathtaipei

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Criteria
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.MenuCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction.TRANSIT_FRAGMENT_OPEN
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.rodericko.bikepathtaipei.databinding.ActivityMapsBinding
import kotlinx.android.synthetic.main.exit_info.view.*
import java.util.*


internal class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private val mREQUESTLOCATIONPERMISSION  = 1
    private lateinit var binding: ActivityMapsBinding
    private val mTAG = MapsActivity::class.java.simpleName



    override fun onCreate(savedInstanceState: Bundle?) {

            super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        mapFragment.activity?.title = "Where's the nearest exit?"

    }


    override fun onSupportNavigateUp(): Boolean {
        if (supportFragmentManager.popBackStackImmediate()) {
            supportActionBar?.setDisplayHomeAsUpEnabled(false)
            supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_TITLE
            setTitle(R.string.exit_question)
            supportFragmentManager.popBackStack()

            Toast.makeText(applicationContext,"Show me the UI!",Toast.LENGTH_LONG).show()


            return true
        }

        return super.onSupportNavigateUp()
    }



    /**
 * ~~~~~~~ Do stuff on the map once available. ~~~~~~~
 */

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        setMapLongClick(mMap)
        setPoiClick(mMap)

    val mGOLatLng = LatLng(25.12299, 121.46196)
    val mGOMapOpt = GroundOverlayOptions()
        .image(BitmapDescriptorFactory.fromResource(R.drawable.sampleimage))
        .position(mGOLatLng, 50f)
        .clickable(true)


    mMap.addGroundOverlay(mGOMapOpt)?.tag = "haha"
    mMap.setOnGroundOverlayClickListener {

       if (it.tag == "haha") {

           Toast.makeText(
               applicationContext,
               "This is a haha message",
               Toast.LENGTH_SHORT
           ).show()
       }
    }




/**
 * ~~~~~~~ ADD CIRCLES ON THE MAP ~~~~~~~
 */

    for (i in 0..120) {
        val mLat = mTwoDee[i][0]
        val mLon = mTwoDee[i][1]
        createCircleMarks(mLat, mLon, i)
    }


    mMap.setOnCircleClickListener {

              val hTagged = it.tag as Int
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






    /**
     * INITIALIZE THE OPTIONS MENU.
     */

    // Initializes contents of Activity's standard options menu. Only called the first time options
    // menu is displayed.

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
        // Change the map type based on the user's selection.
        R.id.normal_map -> {
            val style = MapStyleOptions.loadRawResourceStyle(this, R.raw.default_map_style)
            mMap.setMapStyle(style)
            mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            mMap.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            mMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.minimal_map -> {
            val style = MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style)
            mMap.setMapStyle(style)
            mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.reset_dd_menu -> {
            mMap.clear()

            for (i in 0..120) {
                val mLat = mTwoDee[i][0]
                val mLon = mTwoDee[i][1]
                createCircleMarks(mLat, mLon, i)
            }


            true
        }
        R.id.settings_dd_menu -> {


            supportFragmentManager
                .beginTransaction()
                .setTransition(TRANSIT_FRAGMENT_OPEN)
                .setReorderingAllowed(true)
                .addToBackStack("DefaultView")
                .add(R.id.map, ExampleFragment())
                .commit()



            supportFragmentManager.addOnBackStackChangedListener {
                if (supportFragmentManager.backStackEntryCount == 0) {
                    setTitle(R.string.customize_appearance)
                }
            }


            supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
            supportActionBar?.setCustomView (R.layout.pref_toolbar)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)

            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
            val name = sharedPreferences.getString("username", "")
            println("This is a value in settings: $name")


            true
        }

        else -> super.onOptionsItemSelected(item)
    }



    /**
     * CONFIGURE LONG PRESS.
     */

    // Called when user makes a long press gesture on the map.
    private fun setMapLongClick(map: GoogleMap) {
        map.setOnMapLongClickListener { latLng ->
            // A Snippet is Additional text that's displayed below the title.
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

    // Places a marker on the map and displays an info window that contains POI name.
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


    /**
     * LOAD JSON STYLE FROM THE RAW FOLDER.
     */


    private fun setMapStyle(map: GoogleMap) {
        try {
            // Customize the styling of the base map using a JSON object defined
            // in a raw resource file.
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    this,
                    R.raw.map_style
                )
            )

            if (!success) {
                Log.e(mTAG, "Style parsing failed.")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e(mTAG, "Can't find style. Error: ", e)
        }
    }



    private fun createCircleMarks(mPoint01: Any, mPoint02: Any, str: Any) {

        val mLat =  mPoint01 as Double
        val mLang = mPoint02 as Double
        val mTagged = str as Int

        val mConvPoint = LatLng(mLat, mLang)

        val circles = CircleOptions()
            .radius(10.0)
            .clickable(true)
         val mPlacedCircle = mMap.addCircle(circles.center(mConvPoint))
         mPlacedCircle.tag = mTagged

    }



    override fun onBackPressed() {
        onSupportNavigateUp()
        }




    /**
     * ~~~~~~~ Last brace below ~~~~~~~
     */
}



class ExampleFragment : Fragment(R.layout.white_bg)

class MySettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings, rootKey)

        val sharedPref = activity?.getPreferences(AppCompatActivity.MODE_PRIVATE) ?: return
        val vNormalMap: Preference? = findPreference(getString(R.string.pNormal_map))


        vNormalMap?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            it.setIcon(R.drawable.icon_radio_grey)

            with (sharedPref.edit()) {
                putString(getString(R.string.pNormal_map), "newHighScore")
                apply()
            }
            val defaultValue = "resources"
            val highScore = sharedPref.getString(getString(R.string.pNormal_map), defaultValue)
            println("The current value is $highScore")



            true
        }

    }
}


