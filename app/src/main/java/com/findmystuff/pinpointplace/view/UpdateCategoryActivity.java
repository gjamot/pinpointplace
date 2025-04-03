package com.findmystuff.pinpointplace.view;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import com.findmystuff.pinpointplace.controller.CategoryActivityController;
import com.ppp.pinpointplace.R;
import com.findmystuff.pinpointplace.utils.Constant;

/**
 * Created by Greg on 15/01/2017.
 */

public class UpdateCategoryActivity extends AppCompatActivity implements Constant {
    Button btnSave;
    EditText categoryEditText;
    CategoryActivityController cac;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_category_update);
        btnSave = (Button) findViewById(R.id.btn_update_category);
        categoryEditText = (EditText)findViewById(R.id.editCategoryName);

        cac = new CategoryActivityController(this, categoryEditText);
        btnSave.setOnClickListener(cac);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_action_back_white);
        toolbar.setTitle("Modification d'une catégorie");
        setSupportActionBar(toolbar);
    }

    //gère le click sur une action de l'ActionBar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
