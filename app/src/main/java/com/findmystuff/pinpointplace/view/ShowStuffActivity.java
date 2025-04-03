package com.findmystuff.pinpointplace.view;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.findmystuff.pinpointplace.controller.ShowStuffActivityController;
import com.findmystuff.pinpointplace.model.ApplicationModel;
import com.findmystuff.pinpointplace.model.Stuff;
import com.findmystuff.pinpointplace.utils.Constant;
import com.findmystuff.pinpointplace.utils.SQLLiteDatabaseHelper;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.ppp.pinpointplace.R;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Greg on 27/12/2016.
 */

public class ShowStuffActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.InfoWindowAdapter, GoogleMap.OnInfoWindowClickListener, GoogleMap.OnMarkerClickListener, Constant {
    private ApplicationModel applicationModel;
    private Stuff currentStuffOnMap;
    private Marker currentMarker;
    private NavigationView navigationView;
    private DrawerLayout drawer;
    private View navHeader;
    private TextView txtDate;
    private String[] activityTitles;
    private Context context;
    private TextView txtName;
    private String searchBarValue;
    private Spinner spinner;
    private Toolbar toolbar;
    private ShowStuffActivityController ssac;
    public ShowStuffActivityController getSsac() {
        return ssac;
    }
    private LinearLayout btnActionLinearLayout;
    private GoogleMap currentMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Fabric.with(this, new Crashlytics());
        setContentView(R.layout.map_layout_container);

        context = getApplicationContext();

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_nav_menu);
        toolbar.setTitle("PinPointPlace");
        setSupportActionBar(toolbar);
        applicationModel = new ApplicationModel();
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        spinner = (Spinner) findViewById(R.id.spinner_stuff_type);

        btnActionLinearLayout = (LinearLayout) findViewById(R.id.btn_group);
        // Navigation view header
        //navHeader = navigationView.getHeaderView(0);
        txtDate = (TextView)findViewById(R.id.txtFilterDate);


        // load toolbar titles from string resources
        activityTitles = getResources().getStringArray(R.array.nav_item_activity_titles);

        // initializing navigation menu
        setUpNavigationView();


        ssac = new ShowStuffActivityController(this);
        // Spinner click listener
        spinner.setOnItemSelectedListener(ssac);

        // Assume thisActivity is the current activity
        loadSpinnerData(ssac);
        ImageButton btnResetCategoryFilter = (ImageButton) findViewById(R.id.btn_reset_category_filters);
        btnResetCategoryFilter.setOnClickListener(ssac);
        ImageButton btnResetDateFilter = (ImageButton) findViewById(R.id.btn_reset_date_filters);
        btnResetDateFilter.setOnClickListener(ssac);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(ssac);

        //this.deleteDatabase("find_my_stuff.db");
        SQLLiteDatabaseHelper.getInstance(this);

        requestUserToAllowLocationPermission();
        requestUserToAllowWriteOnExternalStorage();

        int off = -1;
        try {
            off = Settings.Secure.getInt(getContentResolver(), Settings.Secure.LOCATION_MODE);
            /**
             * Si la localisation n'est pas activée
             */
            if(off==0) {
                // 1. Instantiate an AlertDialog.Builder with its constructor
                AlertDialog.Builder builder = new AlertDialog.Builder(this);

                // 2. Chain together various setter methods to set the dialog characteristics
                builder.setMessage("Activer les paramètres de localisation")
                        .setTitle("PinPointPlace");

                // Add the buttons
                builder.setNeutralButton("Continuer", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent gpsOptionsIntent = new Intent(
                                android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        ShowStuffActivity.this.startActivity(gpsOptionsIntent);
                    }

                });
                // 3. Get the AlertDialog from create()
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        } catch(Settings.SettingNotFoundException ex) {
            // Log.w("GJT", "Unable to access settings");
        }
    }

    private List<Object> generateStuffList(Context context){
        ApplicationModel applicationModel = new ApplicationModel();
        return applicationModel.getObjectListForListView(context, null, null);
    }

    public TextView getTxtDate() {
        return txtDate;
    }


    @Override
    public void onMapReady(GoogleMap map) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        map.setOnMarkerClickListener(this);
        map.setInfoWindowAdapter(this);
        map.setOnInfoWindowClickListener(this);
        int counter = 0;
        for(Stuff stuff: applicationModel.getStuffList(this)) {
            currentStuffOnMap = null;
            currentStuffOnMap = new Stuff(stuff.getName(), stuff.getLatitude(), stuff.getLongitude(), stuff.getImagePath(), stuff.getIdCategory(), stuff.getDescription(), stuff.getThumbnailByte(), stuff.getDate());
            LatLng stuffMap = new LatLng(currentStuffOnMap.getLatitude(), currentStuffOnMap.getLongitude());

            //BitmapDescriptor iconBitmap = BitmapDescriptorFactory.fromBitmap(rotateBitmap(bmp, getImageOrientation(stuff.getImagePath())));
            Marker marker = map.addMarker(new MarkerOptions()
                    .title(stuff.getName())
                   // .snippet(applicationModel.getCategoryNameFromId(this, stuff.getIdCategory().intValue()))
                    .position(stuffMap));
            //marker.showInfoWindow();
            builder.include(stuffMap);
            counter += 1;
        }
        if (checkLocationPermission())
            map.setMyLocationEnabled(true);

        if (builder != null && counter > 0) {
            LatLngBounds bounds = builder.build();
            // begin new code:
            int width = getResources().getDisplayMetrics().widthPixels;
            int height = getResources().getDisplayMetrics().heightPixels;
            int padding = (int) (width * 0.12); // offset from edges of the map 12% of screen
            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);
            map.animateCamera(cu);
        }
        currentMap = map;
    }

    /**
     * Function to load the spinner data from SQLite database
     * */
    private void loadSpinnerData(ShowStuffActivityController salc) {

        // Spinner Drop down elements
        List<String> types = new ArrayList<String>();

        types.add("Filtrer par catégorie   ");
        types.addAll(salc.getApplicationModel().getAllStuffType(this));

        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, types);

        // Drop down layout style - list view with radio button
        dataAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        spinner.setAdapter(dataAdapter);
    }

    public boolean checkLocationPermission()
    {
        String permission = "android.permission.ACCESS_FINE_LOCATION";
        int res = this.checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

    /**
    private class LoadingMapContentTask extends AsyncTask<Void, String, Void> {
        @Override
        protected void onPreExecute() {
            //layout.setVisibility(View.VISIBLE);
            mListView.setVisibility(View.GONE);
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void result) {
            mListView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
            mListView.setVisibility(View.VISIBLE);
            linlaHeaderProgress.setVisibility(View.GONE);
            super.onPostExecute(result);
        }

        @Override
        protected Void doInBackground(Void... params) {
            List<Object> objects = generateStuffList(StuffListActivity.this);
            if (objects == null)
                Log.w("GJT", "Object list is null");
            else
                Log.w("GJT", "Object list size : " + objects.size());
            adapter = new StuffAdapter(StuffListActivity.this, objects);

            return null;
        }
    } */

    public void requestUserToAllowLocationPermission() {
        // Here, thisActivity is the current activity
        // Log.w("GJT", "Location permission " + checkLocationPermission());

        int permission = ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= 23) {
                //Demander la permission
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        LOCATION_REQUEST);
                // Should we show an explanation?
                /**
                 if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                 android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                 } else {

                 }*/
            }
        }
    }

    public void requestUserToAllowWriteOnExternalStorage () {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= 23) {
                //Demander la permission
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        LOCATION_REQUEST);
                // Should we show an explanation?
                /**
                 if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                 android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                 } else {

                 }*/
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case LOCATION_REQUEST: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.w("GJT", "GPS finally activated !!!");
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        List<Stuff> stuffs = new ArrayList<Stuff>();
        String category = spinner.getSelectedItem().toString();
        String date = (String)txtDate.getText();
        // Log.w("GJT", "Date sélectionnée : " + date);
        // Log.w("GJT", "Catégorie sélectionnée " + category);

        if (searchBarValue != null) {
            if (!date.equals("Filtrer par date      "))
                stuffs = ssac.getApplicationModel().filterMap(ShowStuffActivity.this, category, searchBarValue.toString().trim(), date.substring(10).trim());
            else
                stuffs = ssac.getApplicationModel().filterMap(ShowStuffActivity.this, category, searchBarValue.toString().trim(), null);
        } else {
            if (!date.equals("Filtrer par date      "))
                stuffs = ssac.getApplicationModel().filterMap(ShowStuffActivity.this, category, "", date.substring(10).trim());
            else
                stuffs = ssac.getApplicationModel().filterMap(ShowStuffActivity.this, category, "", null);
        }
        refreshStuffListData(stuffs);
    }

    protected Bitmap rotateBitmap(Bitmap source, float angle)
    {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }
    protected int getImageOrientation(String imagePath){
        int rotate = 0;
        try {

            File imageFile = new File(imagePath);
            ExifInterface exif = new ExifInterface(
                    imageFile.getAbsolutePath());
            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rotate;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
        //return prepareInfoView(marker);
    }

    @Override
    public View getInfoContents(Marker marker) {
        //return null;
        return prepareInfoView(marker);

    }

    private View prepareInfoView(Marker marker){
        currentStuffOnMap = applicationModel.getStuffFromName(this, marker.getTitle());
        //prepare InfoView programmatically
        LinearLayout mainInfoView = new LinearLayout(this);
        LinearLayout.LayoutParams mainInfoViewParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        mainInfoView.setOrientation(LinearLayout.VERTICAL);
        mainInfoView.setLayoutParams(mainInfoViewParams);

        LinearLayout firstInfoView = new LinearLayout(this);
        LinearLayout.LayoutParams firstInfoViewParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        firstInfoView.setOrientation(LinearLayout.HORIZONTAL);
        firstInfoView.setLayoutParams(firstInfoViewParams);
        byte [] thumbnailByte = currentStuffOnMap.getThumbnailByte();
        Bitmap bmp = null;

        if (thumbnailByte != null) {
            bmp = BitmapFactory.decodeByteArray(thumbnailByte, 0, thumbnailByte.length);
        }
        ImageView infoImageView = new ImageView(this);
        infoImageView.setImageBitmap(rotateBitmap(bmp, getImageOrientation(currentStuffOnMap.getImagePath())));
        firstInfoView.addView(infoImageView);

        LinearLayout subInfoView = new LinearLayout(this);
        LinearLayout.LayoutParams subInfoViewParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        subInfoView.setOrientation(LinearLayout.VERTICAL);
        subInfoView.setLayoutParams(subInfoViewParams);
        subInfoView.setPadding(20, 0, 5, 20);

        TextView category = new TextView(this);
        category.setText(applicationModel.getCategoryNameFromId(this, currentStuffOnMap.getIdCategory().intValue()));
        category.setTextColor(Color.BLACK);

        TextView name = new TextView(this);
        name.setText(currentStuffOnMap.getName());
        name.setTextColor(Color.BLACK);

        TextView descriptionLabel = new TextView(this);
        descriptionLabel.setTextColor(Color.BLACK);
        descriptionLabel.setText("Informations : ");
        descriptionLabel.setPadding(0,2, 0, 0);
        TextView description = new TextView(this);
        description.setText(currentStuffOnMap.getDescription());
        description.setTextColor(Color.BLACK);
        description.setPadding(0,2, 0, 0);

        subInfoView.addView(category);
        subInfoView.addView(name);
        subInfoView.addView(descriptionLabel);
        subInfoView.addView(description);

        firstInfoView.addView(subInfoView);

        TextView date = new TextView(this);
        date.setText(currentStuffOnMap.getDate());
        date.setTextColor(Color.BLACK);
        firstInfoView.addView(date);

        /**
        LinearLayout secondInfoView = new LinearLayout(this);
        LinearLayout.LayoutParams secondInfoViewParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        secondInfoView.setOrientation(LinearLayout.HORIZONTAL);
        secondInfoView.setLayoutParams(secondInfoViewParams);

        ImageButton shareImageButton = new ImageButton(this);
        shareImageButton.setImageResource(R.drawable.ic_action_share);
        secondInfoView.addView(shareImageButton);

        ImageButton locateImageButton = new ImageButton(this);
        locateImageButton.setImageResource(R.drawable.ic_action_stuff_map);
        secondInfoView.addView(locateImageButton);

        ImageButton updateImageButton = new ImageButton(this);
        updateImageButton.setImageResource(R.drawable.ic_category_handling);
        secondInfoView.addView(updateImageButton);

        ImageButton deleteImageButton = new ImageButton(this);
        deleteImageButton.setImageResource(R.drawable.ic_action_delete_all_stuff);
        secondInfoView.addView(deleteImageButton);
        */

        mainInfoView.addView(firstInfoView);
        //mainInfoView.addView(secondInfoView);

        /**
        LinearLayout infoView = View.inflate(this, R.id.map_pinpoint_layout, this);findViewById(R.id.map_pinpoint_layout);
        ImageButton imageButton = (ImageButton) findViewById(R.id.map_pinpoint_img_icon);
        byte [] thumbnailByte = currentStuffOnMap.getThumbnailByte();
        Bitmap bmp = null;

        if (thumbnailByte != null) {
            bmp = BitmapFactory.decodeByteArray(thumbnailByte, 0, thumbnailByte.length);
        }
        imageButton.setImageBitmap(rotateBitmap(bmp, getImageOrientation(currentStuffOnMap.getImagePath())));
        TextView category = (TextView) findViewById(R.id.map_pinpoint_category_name);
        category.setText(applicationModel.getCategoryNameFromId(this, currentStuffOnMap.getIdCategory().intValue()));
        TextView  name = (TextView) findViewById(R.id.map_pinpoint_stuff_name);
        name.setText(currentStuffOnMap.getName());

        TextView description = (TextView) findViewById(R.id.map_pinpoint_description);
        description.setText(currentStuffOnMap.getDescription());


        TextView date = (TextView) findViewById(R.id.map_pinpoint_stuff_date);
        date.setText(currentStuffOnMap.getDate());
         */
        return mainInfoView;
    }

    @Override
    public boolean onMarkerClick(final Marker arg0) {
        currentMarker = arg0;
       // this= "my dynamic text";
        if (!arg0.isInfoWindowShown())
            arg0.showInfoWindow();
        btnActionLinearLayout.setVisibility(View.VISIBLE);
        return true;
    }


public static class DatePickerFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        // Create a new instance of DatePickerDialog and return it
        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        // Do something with the date chosen by the user
        TextView tv1= (TextView) getActivity().findViewById(R.id.txtFilterDate);
        int fullMonth = month + 1;

        String strFullDay;
        if (day < 10)
            strFullDay = "0" + day;
        else
            strFullDay = "" + day;

        String strFullMonth;
        if (fullMonth < 10)
            strFullMonth = "0" + fullMonth;
        else
            strFullMonth = "" + fullMonth;
        tv1.setText("Depuis le " + strFullDay + "/" + strFullMonth + "/" + view.getYear());
        List<Stuff> stuffs = new ArrayList<Stuff>();
        String category = ((ShowStuffActivity)getActivity()).spinner.getSelectedItem().toString();
        String date = ((String)((ShowStuffActivity)getActivity()).txtDate.getText()).substring(10).trim();
        // Log.w("GJT", "Date choisie : " + date);
        if (!date.equals("Filtrer par date      "))
            stuffs = ((ShowStuffActivity)getActivity()).ssac.getApplicationModel().filterMap(((ShowStuffActivity)getActivity()), category, "", date);
        else
            stuffs = ((ShowStuffActivity)getActivity()).ssac.getApplicationModel().filterMap(((ShowStuffActivity)getActivity()), category, "", null);
        ((ShowStuffActivity)getActivity()).refreshStuffListData(stuffs);
        //((StuffListActivity)getActivity()).mListView.invalidate();
    }
}

    private void setUpNavigationView() {
        //Setting Navigation View Item Selected Listener to handle the item click of the navigation menu
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {

            // This method will trigger on item Click of navigation menu
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {

                //Check to see which item was being clicked and perform appropriate action
                switch (menuItem.getItemId()) {
                    /**
                    case R.id.nav_locate_stuff:
                        List<Stuff> stuff = ssac.getApplicationModel().getStuffList(ShowStuffActivity.this);

                        if (stuff.size() > 0) {
                            Answers.getInstance().logContentView(new ContentViewEvent()
                                    .putContentName("Locating All Pinpoints"));
                            //Answers.getInstance().logCustom(new CustomEvent("Locating All Pinpoints"));
                            // Start location service
                            Intent intentMap = new Intent(context, ShowStuffActivity.class);
                            ShowStuffActivity.this.startActivity(intentMap);
                        } else {
                            // 1. Instantiate an AlertDialog.Builder with its constructor
                            AlertDialog.Builder builder = new AlertDialog.Builder(StuffListActivity.this);

                            // 2. Chain together various setter methods to set the dialog characteristics
                            builder.setMessage("Aucun pinpoint à localiser présent dans la liste. Veuillez ajouter au moins un pinpoint pour pouvoir utiliser cette fonctionnalité")
                                    .setTitle("PinPointPlace");

                            // Add the buttons
                            builder.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                }
                            });
                            // 3. Get the AlertDialog from create()
                            AlertDialog dialog = builder.create();
                            dialog.show();
                        }
                        drawer.closeDrawers();
                        return true;
                     */

                    case R.id.nav_handle_stuff:
                        Intent intentStuffList = new Intent(context, StuffListActivity.class);
                        ShowStuffActivity.this.startActivity(intentStuffList);
                        drawer.closeDrawers();
                        return true;

                    case R.id.nav_handle_category:
                        // launch new intent instead of loading fragment
                        // Start location service
                        Intent intent = new Intent(context, CategoryListActivity.class);
                        ShowStuffActivity.this.startActivity(intent);
                        drawer.closeDrawers();
                        return true;
                    /**
                     case R.id.nav_share_mylocation:
                     // launch new intent instead of loading fragment
                     drawer.closeDrawers();
                     return true;
                     */

                    case R.id.nav_delete_all_stuff:
                        // launch new intent instead of loading fragment
                        List<Stuff> stuff2 = ssac.getApplicationModel().getStuffList(ShowStuffActivity.this);

                        // 1. Instantiate an AlertDialog.Builder with its constructor
                        AlertDialog.Builder builder = new AlertDialog.Builder(ShowStuffActivity.this);

                        if (stuff2.size() > 0) {
                            // 2. Chain together various setter methods to set the dialog characteristics
                            builder.setMessage("Etes-vous sûr(e) de vouloir supprimer tous les objets de la liste ?")
                                    .setTitle("PinPointPlace");

                            // Add the buttons
                            builder.setPositiveButton("Confirmer", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    // Answers.getInstance().logContentView(new ContentViewEvent()
                                     //       .putContentName("Deleting All Pinpoints"));
                                    //Answers.getInstance().logCustom(new CustomEvent("Deleting All Pinpoints"));
                                    ShowStuffActivity.this.deleteAllStuff(context);
                                }
                            });
                            builder.setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    // User cancelled the dialog
                                }
                            });
                        } else {
                            // 2. Chain together various setter methods to set the dialog characteristics
                            builder.setMessage("La liste des pinpoints est vide !")
                                    .setTitle("PinPointPlace");

                            // Add the buttons
                            builder.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                }
                            });
                        }
                        // 3. Get the AlertDialog from create()
                        AlertDialog dialog = builder.create();
                        dialog.show();
                        drawer.closeDrawers();
                        return true;

                    case R.id.nav_exit:
                        // 1. Instantiate an AlertDialog.Builder with its constructor
                        AlertDialog.Builder builder2 = new AlertDialog.Builder(ShowStuffActivity.this);

                        // 2. Chain together various setter methods to set the dialog characteristics
                        builder2.setMessage("Etes-vous sûr(e) de vouloir quitter l'application ?")
                                .setTitle("PinPointPlace");

                        // Add the buttons
                        builder2.setPositiveButton("Confirmer", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                ShowStuffActivity.this.finish();
                            }
                        });
                        builder2.setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // User cancelled the dialog
                            }
                        });
                        // 3. Get the AlertDialog from create()
                        AlertDialog dialog2 = builder2.create();
                        dialog2.show();
                        drawer.closeDrawers();
                        return true;

                    default:
                }

                //Checking if the item is in checked state or not, if not make it in checked state
                if (menuItem.isChecked()) {
                    menuItem.setChecked(false);
                } else {
                    menuItem.setChecked(true);
                }
                menuItem.setChecked(true);
                return true;
            }
        });


        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.openDrawer, R.string.closeDrawer) {

            @Override
            public void onDrawerClosed(View drawerView) {
                // Code here will be triggered once the drawer closes as we dont want anything to happen so we leave this blank
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                // Code here will be triggered once the drawer open as we dont want anything to happen so we leave this blank
                super.onDrawerOpened(drawerView);
            }
        };

        //Setting the actionbarToggle to drawer layout
        drawer.setDrawerListener(actionBarDrawerToggle);

        //calling sync state is necessary or else your hamburger icon wont show up
        actionBarDrawerToggle.syncState();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //ajoute les entrées de menu_test à l'ActionBar
        getMenuInflater().inflate(R.menu.main_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        //*** setOnQueryTextFocusChangeListener ***
        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {

            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                searchBarValue = query;
                return false;
            }

            @Override
            public boolean onQueryTextChange(String searchQuery) {
                searchBarValue = searchQuery;
                List<Stuff> stuffs = new ArrayList<Stuff>();
                String category = spinner.getSelectedItem().toString();
                String date = (String)txtDate.getText();
                // Log.w("GJT", "Catégorie sélectionnée " + category);
                if (!date.equals("Filtrer par date      "))
                    stuffs = ssac.getApplicationModel().filterMap(ShowStuffActivity.this, category, searchQuery.toString().trim(), date);
                else
                    stuffs = ssac.getApplicationModel().filterMap(ShowStuffActivity.this, category, searchQuery.toString().trim(), null);
                refreshStuffListData(stuffs);
                //mListView.invalidate();
                return true;
            }
        });

        MenuItemCompat.setOnActionExpandListener(searchItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                // Do something when collapsed
                return true;  // Return true to collapse action view
            }

            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                // Do something when expanded
                return true;  // Return true to expand action view
            }
        });
        return true;
    }

    //gère le click sur une action de l'ActionBar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_search) {
            return true;
        }

        switch (item.getItemId()){

            case android.R.id.home:
                drawer.openDrawer(GravityCompat.START);
                return true;


            /**
             case R.id.action_locate:

             List<Stuff> stuff = salc.getApplicationModel().getStuffList(this);

             if (stuff.size() > 0) {
             // Start location service
             Intent intentMap = new Intent(context, ShowStuffActivity.class);
             StuffListActivity.this.startActivity(intentMap);
             } else {
             // 1. Instantiate an AlertDialog.Builder with its constructor
             AlertDialog.Builder builder = new AlertDialog.Builder(StuffListActivity.this);

             // 2. Chain together various setter methods to set the dialog characteristics
             builder.setMessage("Aucun objet à localiser présent dans la liste. Veuillez ajouter au moins un objet pour pouvoir utiliser cette fonctionnalité")
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
             return true;

             case R.id.action_delete:

             List<Stuff> stuff2 = salc.getApplicationModel().getStuffList(this);

             // 1. Instantiate an AlertDialog.Builder with its constructor
             AlertDialog.Builder builder = new AlertDialog.Builder(StuffListActivity.this);

             if (stuff2.size() > 0) {
             // 2. Chain together various setter methods to set the dialog characteristics
             builder.setMessage("Etes-vous sûr(e) de vouloir supprimer tous les objets de la liste ?")
             .setTitle("FindMyStuff");

             // Add the buttons
             builder.setPositiveButton("Confirmer", new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int id) {
             StuffListActivity.this.deleteAllStuff(context);
             }
             });
             builder.setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int id) {
             // User cancelled the dialog
             }
             });
             } else {
             // 2. Chain together various setter methods to set the dialog characteristics
             builder.setMessage("La liste des objets est vide !")
             .setTitle("FindMyStuff");

             // Add the buttons
             builder.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int id) {
             }
             });
             }
             // 3. Get the AlertDialog from create()
             AlertDialog dialog = builder.create();
             dialog.show();

             return true;
             case R.id.action_exit:
             // 1. Instantiate an AlertDialog.Builder with its constructor
             AlertDialog.Builder builder2 = new AlertDialog.Builder(StuffListActivity.this);

             // 2. Chain together various setter methods to set the dialog characteristics
             builder2.setMessage("Etes-vous sûr(e) de vouloir quitter l'application ?")
             .setTitle("FindMyStuff");

             // Add the buttons
             builder2.setPositiveButton("Confirmer", new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int id) {
             StuffListActivity.this.finish();
             }
             });
             builder2.setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int id) {
             // User cancelled the dialog
             }
             });
             // 3. Get the AlertDialog from create()
             AlertDialog dialog2 = builder2.create();
             dialog2.show();
             return true;
             */
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawers();
            return;
        }
        super.onBackPressed();
    }

    private void deleteAllStuff(Context context) {
        ssac.getApplicationModel().deleteAllStuffFromDatabase(context);
        refreshStuffListData(ssac.getApplicationModel().getStuffList(context));
    }

    public void refreshStuffListData(List<Stuff> stuffList) {
        // Log.w("GJT", "Rafraichissement de la map..");
        if (currentMap != null) {
            currentMap.clear();
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            int counter = 0;
            for (Stuff stuff : stuffList) {
                currentStuffOnMap = null;
                currentStuffOnMap = new Stuff(stuff.getName(), stuff.getLatitude(), stuff.getLongitude(), stuff.getImagePath(), stuff.getIdCategory(), stuff.getDescription(), stuff.getThumbnailByte(), stuff.getDate());
                LatLng stuffMap = new LatLng(currentStuffOnMap.getLatitude(), currentStuffOnMap.getLongitude());

                //BitmapDescriptor iconBitmap = BitmapDescriptorFactory.fromBitmap(rotateBitmap(bmp, getImageOrientation(stuff.getImagePath())));
                Marker marker = currentMap.addMarker(new MarkerOptions()
                        .title(stuff.getName())
                        // .snippet(applicationModel.getCategoryNameFromId(this, stuff.getIdCategory().intValue()))
                        .position(stuffMap));
                //marker.showInfoWindow();
                builder.include(stuffMap);
                counter += 1;

            }
            if (checkLocationPermission())
                currentMap.setMyLocationEnabled(true);

            if (builder != null && counter > 0) {
                LatLngBounds bounds = builder.build();
                // begin new code:
                int width = getResources().getDisplayMetrics().widthPixels;
                int height = getResources().getDisplayMetrics().heightPixels;
                int padding = (int) (width * 0.12); // offset from edges of the map 12% of screen

                CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);
                currentMap.animateCamera(cu);
            }
        }
        //new ShowStuffActivity.LoadingListViewContentTask(objectList).execute();
    }

    public void showDatePickerDialog(View v){
        DialogFragment dialogFragment = new ShowStuffActivity.DatePickerFragment();
        dialogFragment.show(getSupportFragmentManager(), "show_date_picker");
    }

    public Spinner getSpinner() {
        return spinner;
    }

    public void setTxtDate(TextView txtDate) {
        this.txtDate = txtDate;
    }

    public void resetSpinnerValue() {
        spinner.setSelection(0);
    }

    public Marker getCurrentMarker() {
        return currentMarker;
    }

    @Override
    public void onDestroy() {
        if (SQLLiteDatabaseHelper.getInstance(this) != null) {
            SQLLiteDatabaseHelper.getInstance(this).close();
            super.onDestroy();
        }
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        String stuffName = currentStuffOnMap.getName();
        String stuffDescription = currentStuffOnMap.getDescription();
        Intent intent = new Intent(ShowStuffActivity.this, ViewStuffActivity.class);
        intent.putExtra("STUFF_NAME", stuffName);
        intent.putExtra("STUFF_DESCRIPTION", stuffDescription);
        ShowStuffActivity.this.startActivity(intent);

    }

}
