package com.findmystuff.pinpointplace.controller;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import com.findmystuff.pinpointplace.model.Stuff;
import com.findmystuff.pinpointplace.view.StuffListActivity;
import com.ppp.pinpointplace.R;
import com.findmystuff.pinpointplace.model.ApplicationModel;
import com.findmystuff.pinpointplace.view.AddStuffActivity;
import com.findmystuff.pinpointplace.view.CategoryListActivity;
import com.findmystuff.pinpointplace.view.UpdateStuffActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Greg on 30/12/2016.
 */

public class StuffActivityListController implements AdapterView.OnItemClickListener, AdapterView.OnItemSelectedListener,  View.OnClickListener {
    private Context context;
    public ApplicationModel getApplicationModel() {
        return applicationModel;
    }

    private ApplicationModel applicationModel;

    public StuffActivityListController(Context context) {
        this.context = context;
        applicationModel = new ApplicationModel();
    }
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id){
        /**
        String itemStuffName = ((TextView)view.findViewById(R.id.stuff_name)).getText().toString();
        Intent intent = new Intent(context, UpdateStuffActivity.class);
        context.startActivity(intent);
        intent.putExtra("STUFF_NAME", itemStuffName);
         */
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position,
                               long id) {
        /*On selecting a spinner item */
        String stuffType = parent.getItemAtPosition(position).toString();
        List<Object> lstObject = new ArrayList<Object>();
        String date = (String)((StuffListActivity)context).getTxtDate().getText();

        // Mettre à jour la listview avec la catégorie
        /**
        if (!stuffType.equals("Filtrer par catégorie   ")) {
            lstObject = applicationModel.getObjectListForListViewFromCategoryName(context, stuffType, null);
            ((StuffListActivity)context).refreshStuffListData(lstObject);
        } else {
            lstObject = applicationModel.getObjectListForListView(context, null);
            ((StuffListActivity)context).refreshStuffListData(lstObject);
        }
    */
        if (!date.equals("Filtrer par date      "))
            lstObject = applicationModel.filter(context, stuffType, "", date.substring(10));
        else
            lstObject = applicationModel.filter(context, stuffType, "", null);

        ((StuffListActivity)context).refreshStuffListData(lstObject);
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub

    }


    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.btn_reset_category_filters:
                // Log.w("GJT", "Reseting category filters");
                List<Object> lstObject = new ArrayList<Object>();
                // Mettre à jour la listview avec la catégorie
                String date = ((String)((StuffListActivity)context).getTxtDate().getText());
                //lstObject = applicationModel.getObjectListForListView(context, null);
                if (!date.equals("Filtrer par date      "))
                    lstObject = applicationModel.filter(context, null, "", date.substring(10));
                else
                    lstObject = applicationModel.filter(context, null, "", null);
                ((StuffListActivity)context).refreshStuffListData(lstObject);
                ((StuffListActivity)context).resetSpinnerValue();
                break;

            case R.id.btn_reset_date_filters:
                // Log.w("GJT", "Reseting date filters");
                List<Object> objects = new ArrayList<Object>();
                // Mettre à jour la listview avec la catégorie
                ((StuffListActivity)context).getTxtDate().setText("Filtrer par date      ");
                String category = ((StuffListActivity)context).getSpinner().getSelectedItem().toString();
                //lstObject = applicationModel.getObjectListForListView(context, null);
                if (!category.equals("Filtrer par catégorie   "))
                    lstObject = applicationModel.filter(context, category, "", null);
                else
                    lstObject = applicationModel.filter(context, null, "", null);
                ((StuffListActivity)context).refreshStuffListData(lstObject);
               //((StuffListActivity)context).resetSpinnerValue();
                break;

            case R.id.fab:
                if (applicationModel.checkIfAppHasCategories(context)) {
                    Intent intent = new Intent(context, AddStuffActivity.class);
                    context.startActivity(intent);
                } else {
                    // 1. Instantiate an AlertDialog.Builder with its constructor
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);

                    // 2. Chain together various setter methods to set the dialog characteristics
                    builder.setMessage("Vous ne pouvez pas encore ajouter de pinpoint. Vous n'avez pas de catégorie.")
                            .setTitle("PinPointPlace");

                    // Add the buttons
                    builder.setNeutralButton("Ajouter une catégorie", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Intent intent = new Intent(context, CategoryListActivity.class);
                            context.startActivity(intent);
                        }
                    });

                    // 3. Get the AlertDialog from create()
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }

                break;
        }
    }
}
