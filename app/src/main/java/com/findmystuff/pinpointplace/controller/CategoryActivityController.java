package com.findmystuff.pinpointplace.controller;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.findmystuff.pinpointplace.model.ApplicationModel;
import com.findmystuff.pinpointplace.model.Category;
import com.findmystuff.pinpointplace.view.AddCategoryActivity;
import com.findmystuff.pinpointplace.view.UpdateCategoryActivity;
import com.ppp.pinpointplace.R;

/**
 * Created by Greg on 15/01/2017.
 */

public class CategoryActivityController implements View.OnClickListener {
    private Context context;

    public ApplicationModel getApplicationModel() {
        return applicationModel;
    }

    private ApplicationModel applicationModel;

    private EditText categoryEditText;
    private Category category;
    private String oldCategoryName;

    public CategoryActivityController(Context context, EditText categoryEditText) {
        this.context = context;
        this.categoryEditText = categoryEditText;

        this.oldCategoryName = categoryEditText.getText().toString();
        this.applicationModel = new ApplicationModel();
    }

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {

            case R.id.btn_save_category:
                //Inform the user the button1 has been clicked
                //Toast.makeText(this, "Button1 clicked.", Toast.LENGTH_SHORT).show();
                String categoryName = categoryEditText.getText().toString();

                if (this.applicationModel.checkIfCategoryAlreadyExists(context, categoryName)) {
                    // 1. Instantiate an AlertDialog.Builder with its constructor
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);

                    // 2. Chain together various setter methods to set the dialog characteristics
                    builder.setMessage("La catégorie " + categoryName + " existe déjà")
                            .setTitle("FindMyStuff");

                    // Add the buttons
                    builder.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                        }
                    });
                    // 3. Get the AlertDialog from create()
                    AlertDialog dialog = builder.create();
                    dialog.show();
                } else {
                    // Answers.getInstance().logContentView(new ContentViewEvent()
                      //       .putContentName("Adding Category"));
                    //Answers.getInstance().logCustom(new CustomEvent("Adding Category"));
                    this.applicationModel.insertCategoryToDatabase(context, categoryName);

                    Toast.makeText(context, "L'objet a été enregistré avec succès", Toast.LENGTH_SHORT).show();
                    ((AddCategoryActivity) context).finish();
                }
                break;

            case R.id.btn_update_category:
                //Inform the user the button1 has been clicked
                //Toast.makeText(this, "Button1 clicked.", Toast.LENGTH_SHORT).show();
                String newCategoryName = categoryEditText.getText().toString();

                //Answers.getInstance().logContentView(new ContentViewEvent()
                 //       .putContentName("Updating Category"));

                // Answers.getInstance().logCustom(new CustomEvent("Updating Category"));
                this.applicationModel.updateCategoryToDatabase(context, oldCategoryName, newCategoryName);
                Toast.makeText(context, "L'objet a été bien été mis à jour", Toast.LENGTH_SHORT).show();
                ((UpdateCategoryActivity) context).finish();
                break;
        }
    }
}
