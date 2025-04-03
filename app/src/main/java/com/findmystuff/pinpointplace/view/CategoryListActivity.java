package com.findmystuff.pinpointplace.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.findmystuff.pinpointplace.controller.CategoryActivityController;
import com.ppp.pinpointplace.R;
import com.findmystuff.pinpointplace.model.ApplicationModel;
import com.findmystuff.pinpointplace.model.Category;
import com.findmystuff.pinpointplace.utils.Constant;
import com.findmystuff.pinpointplace.utils.SQLLiteDatabaseHelper;

import java.util.List;

/**
 * Created by Greg on 15/01/2017.
 */

public class CategoryListActivity  extends AppCompatActivity implements Constant {

    private ListView mListView;
    private CategoryAdapter adapter;

    public CategoryActivityController getCac() {
        return cac;
    }

    private CategoryActivityController cac;
    private Context context;
    private TextView txtName, txtWebsite;
    private Toolbar toolbar;
    private Handler mHandler;
    private LinearLayout filterBarLayout;
    private LinearLayout filterDateLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_stuff_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_action_back_white);
        toolbar.setTitle("Gestion des catégories");
        setSupportActionBar(toolbar);

        context = getApplicationContext();
        mListView = (ListView) findViewById(R.id.listView);

        filterBarLayout = (LinearLayout) findViewById(R.id.filter_bar_id);
        filterBarLayout.setVisibility(View.GONE);
        filterDateLayout = (LinearLayout) findViewById(R.id.filter_date_id);
        filterDateLayout.setVisibility(View.GONE);
        List<Category> categories = generateCategoryList(this);

        adapter = new CategoryAdapter(this, categories);
        mListView.setAdapter(adapter);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CategoryListActivity.this, AddCategoryActivity.class);
                CategoryListActivity.this.startActivity(intent);
            }
        });

        //this.deleteDatabase("find_my_stuff.db");
        SQLLiteDatabaseHelper.getInstance(this);

    }

    private List<Category> generateCategoryList(Context context){
        ApplicationModel applicationModel = new ApplicationModel();
        return applicationModel.getCategoryList(context);
    }

    @Override
    public void onResume() {
        super.onResume();
        List<Category> categories = generateCategoryList(this);
        adapter = new CategoryAdapter(this, categories);
        mListView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
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
