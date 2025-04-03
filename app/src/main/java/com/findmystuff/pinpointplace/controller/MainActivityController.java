package com.findmystuff.pinpointplace.controller;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;
import android.view.View;
import com.ppp.pinpointplace.R;
import com.findmystuff.pinpointplace.model.ApplicationModel;
import com.findmystuff.pinpointplace.view.AddStuffActivity;
import com.findmystuff.pinpointplace.view.MainActivity;
import com.findmystuff.pinpointplace.view.ShowStuffActivity;

import android.content.Intent;

import android.content.Context;

/**
 * Created by Greg on 27/12/2016.
 */

public class MainActivityController implements View.OnClickListener {
    private Context context;
    private ApplicationModel applicationModel;

    public MainActivityController(Context context) {
        this.context = context;
        applicationModel = new ApplicationModel();
    }

    @Override
    public void onClick(final View v) {
        switch(v.getId()) {
            case R.id.btn_add:
                //Inform the user the button1 has been clicked
                //Toast.makeText(this, "Button1 clicked.", Toast.LENGTH_SHORT).show();
                // Log.w("GJT", " ADDING OBJECT");
                Intent intent = new Intent(context, AddStuffActivity.class);
                context.startActivity(intent);
                break;
            case R.id.btn_show:
                //Inform the user the button2 has been clicked
                //Toast.makeText(this, "Button2 clicked.", Toast.LENGTH_SHORT).show();
                // Log.w("GJT", " SHOWING OBJECT");
                // Start location service
                Intent intentMap = new Intent(context, ShowStuffActivity.class);
                context.startActivity(intentMap);
                break;


            case R.id.btn_exit:
                // 1. Instantiate an AlertDialog.Builder with its constructor
                AlertDialog.Builder builder = new AlertDialog.Builder(context);

                // 2. Chain together various setter methods to set the dialog characteristics
                builder.setMessage("Etes-vous sûr(e) de vouloir quitter l'application ?")
                        .setTitle("FindMyStuff");

                // Add the buttons
                builder.setPositiveButton("Confirmer", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ((MainActivity)context).finish();
                    }
                });
                builder.setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
                // 3. Get the AlertDialog from create()
                AlertDialog dialog = builder.create();
                dialog.show();
                break;
        }
    }
}
