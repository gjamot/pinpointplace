package com.findmystuff.pinpointplace.model;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Looper;
import android.util.Log;

import com.findmystuff.pinpointplace.controller.AddStuffActivityController;
import com.findmystuff.pinpointplace.controller.UpdateStuffActivityController;
import com.findmystuff.pinpointplace.view.AddStuffActivity;
import com.findmystuff.pinpointplace.view.UpdateStuffActivity;

/**
 * Created by Greg on 29/12/2016.
 */

public class GpsLocationAsyncTask extends AsyncTask<Void, String, Void> {
    boolean running;
    ProgressDialog progressDialog;
    private Context context;
    private LocationManager locationManager;
    private AddStuffActivityController asac;
    private UpdateStuffActivityController usac;

    public GpsLocationAsyncTask(Context context, LocationManager locationManager, AddStuffActivityController asac) {
        this.context = context;
        this.locationManager = locationManager;
        this.asac = asac;
    }

    public GpsLocationAsyncTask(Context context, LocationManager locationManager, UpdateStuffActivityController usac) {
        this.context = context;
        this.locationManager = locationManager;
        this.usac = usac;
    }

    @Override
    protected Void doInBackground(Void... params) {
        if (Looper.myLooper() == null)
        {
            Looper.prepare();
        }
        Location lastKnownLocation = null;
        Criteria locationCritera = new Criteria();
        locationCritera.setAccuracy(Criteria.ACCURACY_FINE);
        locationCritera.setAltitudeRequired(false);
        locationCritera.setBearingRequired(false);
        locationCritera.setCostAllowed(true);
        locationCritera.setPowerRequirement(Criteria.POWER_LOW);
        String providerName = locationManager.getBestProvider(locationCritera,
                true);
        // Log.w("GJT", "Best gps provider : " + providerName);

        //if (providerName == null) {
            providerName = LocationManager.NETWORK_PROVIDER;
        //}
        /**
        if (((AddStuffActivity) context).checkLocationPermission() && providerName != null) {
            Log.w("GJT", "GPS permission granted, getting last known location");
            lastKnownLocation = locationManager.getLastKnownLocation(providerName);
        }
         */

        if (asac != null) {
            if (((AddStuffActivity) context).checkLocationPermission() && lastKnownLocation == null && providerName != null) {
                // Log.w("GJT", "Last known location null");
                locationManager.requestLocationUpdates(providerName, 0, 0, asac);
            }

            while (asac.getLocation() == null) {
                //if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
                //  lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    // if (asac.getLocation() != null)
                     //   Log.w("GJT", "Location found : " + asac.getLocation().getLatitude() + "," + asac.getLocation().getLongitude());
                }

            }
        } else if (usac != null) {
            if (((UpdateStuffActivity) context).checkLocationPermission() && lastKnownLocation == null && providerName != null) {
                // Log.w("GJT", "Last known location null");
                locationManager.requestLocationUpdates(providerName, 0, 0, usac);
            }

            while (usac.getLocation() == null) {
                //if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
                //  lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    // if (usac.getLocation() != null)
                      //  Log.w("GJT", "Location found : " + usac.getLocation().getLatitude() + "," + usac.getLocation().getLongitude());
                }

            }
        }

    //
        return null;
    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
        progressDialog.setMessage(String.valueOf(values[0]));
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        running = true;

        progressDialog = ProgressDialog.show(this.context,
                "PinPointPlace",
                "Récupération des données de localisation...");

        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                running = false;
            }
        });
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if (progressDialog !=null)
            progressDialog.dismiss();
    }
}
