package com.rodericko.bikepathtaipei

import com.google.android.gms.maps.GoogleMap

val mMapStyleTypes = arrayOf(
    GoogleMap.MAP_TYPE_NORMAL,
    GoogleMap.MAP_TYPE_SATELLITE,
    GoogleMap.MAP_TYPE_HYBRID,
    GoogleMap.MAP_TYPE_NORMAL
)

val mMapStyleSummary = arrayOf(
    R.string.normal_map_desc,
    R.string.satellite_map_desc,
    R.string.hybrid_map_desc,
    R.string.minimal_map_desc
)

val mMapMarkerTypes = arrayOf(
    R.string.mType_markers_desc,
    R.string.mType_circles_desc,
    R.string.mType_numbers_desc
)

val mMapStyleStyles = arrayOf(
    R.raw.default_map_style,
    R.raw.map_style
)

val mLanguageTypes = arrayOf(
    R.string.langEN_desc,
    R.string.langZH_desc
)

val mStreetViewOpen = arrayOf(
    R.string.openDefault_desc,
    R.string.openBrowser_desc
)