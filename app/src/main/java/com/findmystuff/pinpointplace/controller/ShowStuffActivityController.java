package com.findmystuff.pinpointplace.controller;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;

import com.findmystuff.pinpointplace.model.ApplicationModel;
import com.findmystuff.pinpointplace.model.Stuff;
import com.findmystuff.pinpointplace.view.AddStuffActivity;
import com.findmystuff.pinpointplace.view.CategoryListActivity;
import com.findmystuff.pinpointplace.view.ShowStuffActivity;
import com.ppp.pinpointplace.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Greg on 30/12/2016.
 */

public class ShowStuffActivityController implements AdapterView.OnItemClickListener, AdapterView.OnItemSelectedListener,  View.OnClickListener {
    private Context context;
    public ApplicationModel getApplicationModel() {
        return applicationModel;
    }

    private ApplicationModel applicationModel;

    public ShowStuffActivityController(Context context) {
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
        List<Stuff> lstStuff = new ArrayList<Stuff>();
        String date = (String)((ShowStuffActivity)context).getTxtDate().getText();

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
            lstStuff = applicationModel.filterMap(context, stuffType, "", date.substring(10));
        else
            lstStuff = applicationModel.filterMap(context, stuffType, "", null);

        ((ShowStuffActivity)context).refreshStuffListData(lstStuff);
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
                List<Stuff> lstStuff = new ArrayList<Stuff>();
                String date = ((String)((ShowStuffActivity)context).getTxtDate().getText());
                //lstObject = applicationModel.getObjectListForListView(context, null);
                if (!date.equals("Filtrer par date      "))
                    lstStuff = applicationModel.filterMap(context, null, "", date.substring(10));
                else
                    lstStuff = applicationModel.filterMap(context, null, "", null);
                ((ShowStuffActivity)context).refreshStuffListData(lstStuff);
                ((ShowStuffActivity)context).resetSpinnerValue();
                break;

            case R.id.btn_reset_date_filters:
                // Log.w("GJT", "Reseting date filters");
                List<Stuff> stuffs = new ArrayList<Stuff>();
                // Mettre à jour la listview avec la catégorie
                ((ShowStuffActivity)context).getTxtDate().setText("Filtrer par date      ");
                String category = ((ShowStuffActivity)context).getSpinner().getSelectedItem().toString();
                //lstObject = applicationModel.getObjectListForListView(context, null);
                if (!category.equals("Filtrer par catégorie   "))
                    stuffs = applicationModel.filterMap(context, category, "", null);
                else
                    stuffs = applicationModel.filterMap(context, null, "", null);
                ((ShowStuffActivity)context).refreshStuffListData(stuffs);
                ((ShowStuffActivity)context).resetSpinnerValue();
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

            /**
            case R.id.fab2:
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
             */
        }
    }
}

