package com.findmystuff.pinpointplace.controller;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Toast;
import com.findmystuff.pinpointplace.model.ApplicationModel;
import com.findmystuff.pinpointplace.model.Stuff;
import com.findmystuff.pinpointplace.view.AddStuffActivity;
import com.ppp.pinpointplace.R;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by Greg on 27/12/2016.
 */

public class AddStuffActivityController implements View.OnClickListener, AdapterView.OnItemSelectedListener, LocationListener {
    private Context context;

    public ApplicationModel getApplicationModel() {
        return applicationModel;
    }

    private ApplicationModel applicationModel;

    private String stuffType;
    private EditText stuffEditText;
    private double currentLatitude;
    private double currentLongitude;
    private Stuff stuff;
    private LocationManager locationManager;

    private Location location;

    public AddStuffActivityController(Context context, EditText stuffEditText, LocationManager locationManager) {
        this.context = context;
        this.stuffEditText = stuffEditText;
        this.applicationModel = new ApplicationModel();
        this.currentLatitude = 0.0;
        this.currentLongitude = 0.0;
        this.locationManager = locationManager;
    }

    @Override
    public void onClick(final View v) {
        switch(v.getId()) {
            case R.id.btn_take_pic:
                // Log.w("GJT", "Adding picture..");
                ((AddStuffActivity)this.context).callCamera();
                break;
            case R.id.btn_save:
                //Inform the user the button1 has been clicked
                //Toast.makeText(this, "Button1 clicked.", Toast.LENGTH_SHORT).show();
                String stuffName = stuffEditText.getText().toString();
                String stuffDescription = ((AddStuffActivity)context).getDescriptionTxt().getText().toString();
                Location lastKnownLocation = null;
                Criteria locationCritera = new Criteria();
                locationCritera.setAccuracy(Criteria.ACCURACY_FINE);
                locationCritera.setAltitudeRequired(false);
                locationCritera.setBearingRequired(false);
                locationCritera.setCostAllowed(true);
                locationCritera.setPowerRequirement(Criteria.POWER_LOW);
                String providerName = locationManager.getBestProvider(locationCritera,
                        true);
                /**
                if (((AddStuffActivity)context).checkLocationPermission() && providerName != null) {
                    Log.w("GJT", "GPS permission granted, getting last known location");
                    location = locationManager.getLastKnownLocation(providerName);
                }
                */
                if (location != null) {
                    //Log.w("TAG", "GPS is on");
                    currentLatitude = location.getLatitude();
                    currentLongitude = location.getLongitude();
                    // }
                } else {
                    //Log.w("TAG", "GPS is off");
                    if (((AddStuffActivity)context).checkLocationPermission()) {
                        locationManager.requestLocationUpdates(providerName, 0, 0, this);
                    }
                }
                //Log.w("GJT", " SAVING OBJECT");
                /**
                if (this.location != null) {
                    Log.w("GJT", "Latitude calculated " + this.location.getLatitude());
                    Log.w("GJT", "Longitude calculated " + this.location.getLongitude());
                }
                 */


                //Log.w("GJT", "Current latitude known " + currentLatitude);
                // Log.w("GJT", "Current longitude known " + currentLongitude);

                if(location == null) {
                    if(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
                        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
                }

                if (location != null) {
                    /**
                     * Si un objet du même nom existe déjà
                     */
                    if (this.applicationModel.checkIfStuffAlreadyExists(context, stuffName)) {
                        // 1. Instantiate an AlertDialog.Builder with its constructor
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);

                        // 2. Chain together various setter methods to set the dialog characteristics
                        builder.setMessage("L'objet " + stuffName + " existe déjà")
                                .setTitle("FindMyStuff");

                        // Add the buttons
                        builder.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        });
                        // 3. Get the AlertDialog from create()
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                    else {
                        if (((AddStuffActivity) context).getmCurrentPhotoPath() != null) {
                            final int THUMBSIZE = 120;
                            Bitmap thumbImage = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(((AddStuffActivity) context).getmCurrentPhotoPath()), THUMBSIZE, THUMBSIZE);
                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            thumbImage.compress(Bitmap.CompressFormat.PNG, 100, stream);
                            byte[] byteArray = stream.toByteArray();

                            Calendar c = Calendar.getInstance();
                            SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
                            String formattedDate = df.format(c.getTime());
                            // Log.w("GJT", "Nouvelle date : " + formattedDate);

                            // TODO: Use your own attributes to track content views in your app
                            // Answers.getInstance().logContentView(new ContentViewEvent()
                                    // .putContentName("Adding Pinpoint"));
                            //Answers.getInstance().logCustom(new CustomEvent("Adding Pinpoint"));
                            // Log.w("GJT", "Pinpoint update Current latitude : " + currentLatitude);
                            // Log.w("GJT", "Pinpoint update Current longitude :" + currentLongitude);
                            this.applicationModel.insertStuffToDatabase(context, stuffType, stuffName, location.getLatitude(), location.getLongitude(), ((AddStuffActivity) context).getmCurrentPhotoPath(), byteArray, formattedDate, stuffDescription);
                            locationManager.removeUpdates(this);
                            Toast.makeText(context, "L'objet a été enregistré avec succès", Toast.LENGTH_SHORT).show();
                            ((AddStuffActivity) context).finish();
                        } else{
                            // 1. Instantiate an AlertDialog.Builder with its constructor
                            AlertDialog.Builder builder = new AlertDialog.Builder(context);

                            // 2. Chain together various setter methods to set the dialog characteristics
                            builder.setMessage("Votre nouvel objet ne contient pas de photo")
                                    .setTitle("Attention !");

                            // Add the buttons
                            builder.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                }
                            });

                            // 3. Get the AlertDialog from create()
                            AlertDialog dialog = builder.create();
                            dialog.show();
                        }

                    }
                }

                break;
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position,
                               long id) {
        // On selecting a spinner item
        stuffType = parent.getItemAtPosition(position).toString();

        // Showing selected spinner item
        //Toast.makeText(parent.getContext(), "You selected: " + label,
         //       Toast.LENGTH_LONG).show();
        // Log.w("GJT"," Type " + stuffType + " selected !");

    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null && (location.getLatitude() != 0.0 || location.getLongitude() != 0.0)) {
            this.location = location;
            // Log.w("GJT", "Latitude calculated " + this.location.getLatitude());
            // Log.w("GJT", "Longitude calculated " + this.location.getLongitude());
            if (((AddStuffActivity)context).checkLocationPermission())
                locationManager.removeUpdates(this);
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    public Location getLocation() {
        return location;
    }

}
