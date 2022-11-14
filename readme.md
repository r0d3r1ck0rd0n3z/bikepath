## Taipei Riverside Bike Exits

The '**Taipei Riverside Bike Exits**' app is an Android app that maps all known exits located along Taipei's riverside bikepaths. It offers a limited set of options that you can use to customize the map:

* Change map styles and map markers
* Switch between EN and ZH descriptions 
* Link to Google street views
* Drop temporary map markers

<br>

I created this app to help me navigate the long winding bike paths of the Taipei riverside. There have been multiple times when I would usually encounter roadwork that forces me to look for bike exits where I can get in and out my usual route. However, you can also use this app for other purposes:

* Planning cycling tours or day trips
* Looking for alternative biking trails
* Finding the optimal bike path for your commute 
* Scouting out your favorite riverside park
* Tourism and travelling 

<br>

Exit data is taken from information published by data.gov.tw. For more details, navigate to the 'More information' section of the app. The app uses Google Maps and requires access to your location to determine your current position. All data is saved locally and remains on your device. 

Note that this is my first app and is very much a work in progress. I detail all known issues, troubleshooting tips, and additional information here:

* https://bit.ly/TaipeiRiversideBikeExits

<br>

## Installing Taipei Riverside Bike Exits

You can download the app from the Google Play Store:

* [Taipei Riverside Bike Exits](https://play.google.com/store/apps/details?id=com.rodericko.bikepathtaipei)

<br>

The most recent builds will be uploaded to the ['*Releases*'](https://github.com/r0d3r1ck0rd0n3z/bikepath/releases) page here. Installation is quite simple:

1. Locate and download the most recent APK.
2. Transfer the APK to your Android device.
3. On your Android device, navigate to the location of the transferred APK using your favorite file manager. 
4. Tap on the APK to start installation.

<br>

For usage instructions, please visit the following page:

* https://bit.ly/TaipeiRiversideBikeExits

<br>

For a quick overview of the app's features, see video below:

https://user-images.githubusercontent.com/86142912/201138586-ac34e2da-b26f-4c6f-8c99-15c18a059dc0.mp4

Watch the full hi-res vid on [YouTube](https://youtu.be/JAVwOdrk7Y4).

<br>

Thank you for installing Taipei Riverside Bike Exits!

<br><br>

# Building the APK

If you want to compile the app on your local machine, perform the following:

1. Clone the repository and launch via Android Studio. 
2. To create a successful build, you'll need to update the <tt>google_maps_api.xml</tt> file with your own Google Maps API key. The SHA1 of the API key must be properly setup in Google Cloud Console. To do this, follow the instructions here: https://developers.google.com/maps/documentation/android-sdk/start#get-key
3. Try compiling a build to see if everything runs correctly. Most errors are usually related to the API key and its associated SHA1 string.

<br>

If everything runs without errors, you can proceed with the following steps:

1. Using Android Studio's Device Manager, create an emulated Android device. 
2. Run the build on the emulated Android device.
3. If everything launches correctly, you can now start testing the app.

<br>

Thanks!
🐸





