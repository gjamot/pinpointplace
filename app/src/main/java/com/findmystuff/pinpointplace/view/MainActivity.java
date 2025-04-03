package com.findmystuff.pinpointplace.view;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;

import com.ppp.pinpointplace.R;

public class MainActivity extends TabActivity
{
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tab);

        // create the TabHost that will contain the Tabs
        TabHost tabHost = (TabHost)findViewById(android.R.id.tabhost);


        TabHost.TabSpec tab1 = tabHost.newTabSpec("Stuff List");
        TabHost.TabSpec tab2 = tabHost.newTabSpec("Stuff Map");

        // Set the Tab name and Activity
        // that will be opened when particular Tab will be selected
        tab1.setIndicator("Liste d'objets");
        tab1.setContent(new Intent(this,StuffListActivity.class));

        tab2.setIndicator("Localisation des objets");
        tab2.setContent(new Intent(this, ShowStuffActivity.class));

        /** Add the tabs  to the TabHost to display. */
        tabHost.addTab(tab1);
        tabHost.addTab(tab2);

    }
}

