package com.dataon.cordova.plugin;

// The native Toast API
import android.widget.Toast;
import jdk.nashorn.internal.parser.JSONParser;
import android.content.pm.PackageManager;
import android.Manifest;
import android.os.Build;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
// Cordova-required packages
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.apache.cordova.PermissionHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.apache.cordova.LOG;
import android.util.Log;

public class GeolocationPlugin extends CordovaPlugin implements LocationListener {
  private static final String DURATION_LONG = "long";
  String TAG = "GeolocationPlugin";
  CallbackContext context;

  String[] permissions = { Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION };
  boolean isGPSEnabled = false;

  // flag for network status
  boolean isNetworkEnabled = false;

  // flag for GPS status
  boolean canGetLocation = false;

  Location location; // location
  double latitude; // latitude
  double longitude; // longitude

  // The minimum distance to change Updates in meters
  private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 20; // 10 meters

  // The minimum time between updates in milliseconds
  private static final long MIN_TIME_BW_UPDATES = 1000 * 40 ; // 1 minute

  // Declaring a Location Manager
  protected LocationManager locationManager;

  @Override
  public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) {
    // Verify that the user sent a 'show' action
    PluginResult result;
    LOG.d(TAG, "We are entering execute");
    context = callbackContext;
    if (!action.equals("show")) {
      context.error("\"" + action + "\" is not a recognized action.");
      return false;
    } else {
     

      if (hasPermisssion()) {
        locationManager = (LocationManager) cordova.getActivity().getSystemService("location");

        // getting GPS status
        isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        // getting network status
        isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (!isGPSEnabled && !isNetworkEnabled) {
          // no network provider is enabled
          showSettingsAlert();
        } else {
          Location lg = getLocation();

     
          if (lg == null) {
            result = new PluginResult(PluginResult.Status.ERROR);
            context.sendPluginResult(result);
          } else {
            final JSONObject json = new JSONObject();
            // if(location != null){
            try {

              json.put("timestamp", location.getTime());
              json.put("latitude", "" + location.getLatitude());
              json.put("longitude", "" + location.getLongitude());
              // json.put("altitude", location.getAltitude());
              // json.put("accuracy", location.getAccuracy());
              // json.put("bearing", location.getBearing());
              // json.put("speed", location.getSpeed());

              result = new PluginResult(PluginResult.Status.OK, json);
              context.sendPluginResult(result);
            } catch (JSONException exc) {
              result = new PluginResult(PluginResult.Status.ERROR);
              context.sendPluginResult(result);
            }
           
          }
        
        }
     
    } else {
      PermissionHelper.requestPermissions(this, 0, permissions);
    }

      return true;
    
  }
}


  public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults)
      throws JSONException {
    PluginResult result;
    // This is important if we're using Cordova without using Cordova, but we have
    // the geolocation plugin installed
    if (context != null) {
      int r = grantResults[0];
      if (r == PackageManager.PERMISSION_DENIED) {
        LOG.d(TAG, "Permission Denied!");
        result = new PluginResult(PluginResult.Status.ILLEGAL_ACCESS_EXCEPTION);
        context.sendPluginResult(result);
        return;
      } else if (r == PackageManager.PERMISSION_GRANTED) {
        // if (!isGPSEnabled && !isGPSEnabled ) {
        // // no network provider is enabled
        // showSettingsAlert();
        // } else {

        Location lg = getLocation();

        if (lg == null) {
          result = new PluginResult(PluginResult.Status.ERROR);
          context.sendPluginResult(result);
        } else {
          final JSONObject json = new JSONObject();
          // if(location != null){
          try {

            json.put("timestamp", location.getTime());
            json.put("latitude", "" + location.getLatitude());
            json.put("longitude", "" + location.getLongitude());
            // json.put("altitude", location.getAltitude());
            // json.put("accuracy", location.getAccuracy());
            // json.put("bearing", location.getBearing());
            // json.put("speed", location.getSpeed());

            result = new PluginResult(PluginResult.Status.OK, json);
            context.sendPluginResult(result);
          } catch (JSONException exc) {
            result = new PluginResult(PluginResult.Status.ERROR);
            context.sendPluginResult(result);
          }

        }
      }
    }

    // }
  }

  public boolean hasPermisssion() {
    for (String p : permissions) {
      if (!PermissionHelper.hasPermission(this, p)) {
        return false;
      }
    }
    return true;
  }

  /*
   * We override this so that we can access the permissions variable, which no
   * longer exists in the parent class, since we can't initialize it reliably in
   * the constructor!
   */

  public void requestPermissions(int requestCode) {
    PermissionHelper.requestPermissions(this, requestCode, permissions);
  }

  public void showSettingsAlert() {
    AlertDialog.Builder alertDialog = new AlertDialog.Builder(cordova.getActivity());

    // Setting Dialog Title
    alertDialog.setTitle("GPS is settings");

    // Setting Dialog Message
    alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");

    // On pressing Settings button
    alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int which) {
        dialog.cancel();
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        cordova.getActivity().startActivity(intent);
        // getLocation();
      }
    });

    // on pressing cancel button
    alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int which) {
        dialog.cancel();
      }
    });

    // Showing Alert Message
    alertDialog.show();
  }

  public Location getLocation() {
    try {
      locationManager = (LocationManager) cordova.getActivity().getSystemService("location");

      // getting GPS status
      isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

      // getting network status
      isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

      if (!isGPSEnabled && !isNetworkEnabled) {
        // no network provider is enabled
        showSettingsAlert();
      } else {
        this.canGetLocation = true;
        // First get location from Network Provider
        if (isNetworkEnabled) {
          try {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES,
                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
            Log.e("Network", "Network");
            if (locationManager != null) {
              location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
              if (location != null) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                // // Log.e("show picture", longitude);
                // AlertDialog.Builder alertDialog = new
                // AlertDialog.Builder(cordova.getActivity());

                // // Setting Dialog Title
                // alertDialog.setTitle("success");
                // alertDialog.show();

              }
            }
          } catch (SecurityException ex) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(cordova.getActivity());

            // Setting Dialog Title
            alertDialog.setTitle("failed 1");
            alertDialog.show();

          }

        }
        // if GPS Enabled get lat/long using GPS Services
        if (isGPSEnabled) {
          if (location == null) {
            try {
              locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES,
                  MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
              // Log.e("GPS Enabled", "GPS Enabled");
              if (locationManager != null) {
                location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (location != null) {
                  latitude = location.getLatitude();
                  longitude = location.getLongitude();

                  // Log.e("show picture", "" + location.getLongitude());

                  // AlertDialog.Builder alertDialog = new
                  // AlertDialog.Builder(cordova.getActivity());

                  // // Setting Dialog Title
                  // alertDialog.setTitle("success 2");
                  // alertDialog.show();

                }
              }
            } catch (SecurityException ex) {
              AlertDialog.Builder alertDialog = new AlertDialog.Builder(cordova.getActivity());

              // Setting Dialog Title
              alertDialog.setTitle("failed 2");
              alertDialog.show();

            }

          }
        }
      }

    } catch (Exception e) {
      AlertDialog.Builder alertDialog = new AlertDialog.Builder(cordova.getActivity());

      // Setting Dialog Title
      alertDialog.setTitle("failed 3");
      alertDialog.show();
      // Log.e("error 3", e.toString());
    }

    return location;
  }

  public void stopUsingGPS() {
    if (locationManager != null) {
      try {
        locationManager.removeUpdates(this);
      } catch (SecurityException e) {

      }

    }
  }

  /**
   * Function to get latitude
   */
  public double getLatitude() {
    if (location != null) {
      latitude = location.getLatitude();
    }

    // return latitude
    return latitude;
  }

  /**
   * Function to get longitude
   */
  public double getLongitude() {
    if (location != null) {
      longitude = location.getLongitude();
    }

    // return longitude
    return longitude;
  }

  /**
   * Function to check GPS/wifi enabled
   * 
   * @return boolean
   */
  public boolean canGetLocation() {
    return this.canGetLocation;
  }

  public void onLocationChanged(Location location) {
  }

  public void onProviderDisabled(String provider) {
  }

  public void onProviderEnabled(String provider) {
  }

  public void onStatusChanged(String provider, int status, Bundle extras) {
  }

  public IBinder onBind(Intent arg0) {
    return null;
  }

  // public boolean checkPermissionGPS() {
  // int result = ContextCompat.checkSelfPermission(this,
  // Manifest.permission.ACCESS_FINE_LOCATION);
  // if (result == PackageManager.PERMISSION_GRANTED) {
  // return true;
  // } else {
  // return false;
  // }
  // }
}