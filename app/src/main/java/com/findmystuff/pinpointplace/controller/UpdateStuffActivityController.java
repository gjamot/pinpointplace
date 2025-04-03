package com.findmystuff.pinpointplace.controller;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import com.findmystuff.pinpointplace.model.GpsLocationAsyncTask;
import com.findmystuff.pinpointplace.model.Stuff;
import com.findmystuff.pinpointplace.view.UpdateStuffActivity;
import com.ppp.pinpointplace.R;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by Greg on 30/12/2016.
 */

public class UpdateStuffActivityController implements View.OnClickListener, AdapterView.OnItemSelectedListener, LocationListener {
    private Context context;

    public ApplicationModel getApplicationModel() {
        return applicationModel;
    }

    private ApplicationModel applicationModel;

    private String stuffType;
    private EditText stuffEditText;
    private EditText stuffDescriptionText;
    private Location location;
    private double currentLatitude;
    private double currentLongitude;
    private String oldStuffName;
    private Stuff stuff;
    LocationManager locationManager;
    GpsLocationAsyncTask myAsyncTask;

    public UpdateStuffActivityController(Context context, EditText stuffEditText, EditText stuffDescriptionText) {
        this.context = context;
        this.stuffEditText = stuffEditText;
        this.stuffDescriptionText = stuffDescriptionText;
        this.oldStuffName = stuffEditText.getText().toString();
        this.applicationModel = new ApplicationModel();
    }

    @Override
    public void onClick(final View v) {
        switch(v.getId()) {

            case R.id.btn_take_pic:
                // Log.w("GJT", "Updating picture..");
                ((UpdateStuffActivity)this.context).callCamera();
                break;

            case R.id.btn_update_location:
                locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
                if (((UpdateStuffActivity)context).checkLocationPermission())
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
                myAsyncTask = new GpsLocationAsyncTask(context, locationManager, this);
                myAsyncTask.execute();
                if (this.location != null) {
                    currentLatitude = this.location.getLatitude();
                    currentLongitude = this.location.getLongitude();
                }
                break;

            case R.id.btn_save:
                //Inform the user the button1 has been clicked
                //Toast.makeText(this, "Button1 clicked.", Toast.LENGTH_SHORT).show();
                String newStuffName = stuffEditText.getText().toString();

                // Answers.getInstance().logContentView(new ContentViewEvent()
                //       .putContentName("Updating Pinpoint"));
                //Answers.getInstance().logCustom(new CustomEvent("Updating Pinpoint"));
                final int THUMBSIZE = 120;
                Bitmap thumbImage = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(((UpdateStuffActivity) context).getImagePath()), THUMBSIZE, THUMBSIZE);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                thumbImage.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] byteArray = stream.toByteArray();
                Calendar c = Calendar.getInstance();
                SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
                String formattedDate = df.format(c.getTime());

                if (location != null)
                    this.applicationModel.updateStuffToDatabase(context, stuffType,  oldStuffName, newStuffName, ((UpdateStuffActivity) context).getImagePath(), byteArray, formattedDate, location.getLatitude(), location.getLongitude(), stuffDescriptionText.getText().toString());
                else
                    this.applicationModel.updateStuffToDatabase(context, stuffType,  oldStuffName, newStuffName, ((UpdateStuffActivity) context).getImagePath(), byteArray, formattedDate, null, null, stuffDescriptionText.getText().toString());
                Toast.makeText(context, "L'objet a été bien été mis à jour", Toast.LENGTH_SHORT).show();
                ((UpdateStuffActivity) context).finish();
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
            if (((UpdateStuffActivity)context).checkLocationPermission())
                locationManager.removeUpdates(this);
        }
    }

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
