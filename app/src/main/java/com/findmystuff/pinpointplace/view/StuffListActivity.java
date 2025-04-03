package com.findmystuff.pinpointplace.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.media.ExifInterface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
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
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import com.findmystuff.pinpointplace.controller.StuffActivityListController;
import com.findmystuff.pinpointplace.model.ApplicationModel;
import com.findmystuff.pinpointplace.model.Stuff;
import com.findmystuff.pinpointplace.utils.Constant;
import com.findmystuff.pinpointplace.utils.SQLLiteDatabaseHelper;
import com.ppp.pinpointplace.R;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class StuffListActivity extends AppCompatActivity implements Constant {

    private ListView mListView;
    private StuffAdapter adapter;

    public StuffActivityListController getSalc() {
        return salc;
    }

    private StuffActivityListController salc;
    private Context context;
    private NavigationView navigationView;
    private DrawerLayout drawer;
    private View navHeader;
    private TextView txtName;
    private TextView txtWebsite;
    private TextView txtInfoMessage;
    private TextView txtDate;
    private Toolbar toolbar;
    private String[] activityTitles;
    private Handler mHandler;
    private LinearLayout linlaHeaderProgress;
    private Spinner spinner;
    private String searchBarValue;

    // Hold a reference to the current animator,
    // so that it can be canceled mid-way.
    private Animator mCurrentAnimator;

    // The system "short" animation time duration, in milliseconds. This
    // duration is ideal for subtle animations or animations that occur
    // very frequently.
    private int mShortAnimationDuration;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_stuff_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_nav_menu);
        toolbar.setTitle("Gestion des pinpoints");
        setSupportActionBar(toolbar);

        // CAST THE LINEARLAYOUT HOLDING THE MAIN PROGRESS (SPINNER)
        linlaHeaderProgress = (LinearLayout) findViewById(R.id.linlaHeaderProgress);

        spinner = (Spinner) findViewById(R.id.spinner_stuff_type);

        mHandler = new Handler();

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.nav_view);

        // Navigation view header
        //navHeader = navigationView.getHeaderView(0);

        // load toolbar titles from string resources
        activityTitles = getResources().getStringArray(R.array.nav_item_activity_titles);
        txtDate = (TextView)findViewById(R.id.txtFilterDate);

        txtInfoMessage = (TextView)findViewById(R.id.infoMessageList);

        // load nav menu header data
        //loadNavHeader();

        // initializing navigation menu
        setUpNavigationView();

        context = getApplicationContext();
        mListView = (ListView) findViewById(R.id.listView);


        salc = new StuffActivityListController(this);
        //attach a listener to the list view
        mListView.setOnItemClickListener (salc);

        // Spinner click listener
        spinner.setOnItemSelectedListener(salc);

        // Assume thisActivity is the current activity
        loadSpinnerData(salc);
        ImageButton btnResetCategoryFilter = (ImageButton) findViewById(R.id.btn_reset_category_filters);
        btnResetCategoryFilter.setOnClickListener(salc);
        ImageButton btnResetDateFilter = (ImageButton) findViewById(R.id.btn_reset_date_filters);
        btnResetDateFilter.setOnClickListener(salc);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(salc);

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
                        StuffListActivity.this.startActivity(gpsOptionsIntent);
                    }

                });
                // 3. Get the AlertDialog from create()
                AlertDialog dialog = builder.create();
                dialog.show();
            }

            new LoadingListViewContentTask(generateStuffList(this)).execute();

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

    private class LoadingListViewContentTask extends AsyncTask<Void, String, Void> {
        List<Object> data;

        public LoadingListViewContentTask(List<Object> data) {
            this.data = data;
        }
        @Override
        protected void onPreExecute() {
            //layout.setVisibility(View.VISIBLE);
            mListView.setVisibility(View.GONE);
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void result) {
            if (data.size() == 0)
                txtInfoMessage.setVisibility(View.VISIBLE);
            else {
                mListView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
                mListView.setVisibility(View.VISIBLE);
                txtInfoMessage.setVisibility(View.GONE);
            }
            linlaHeaderProgress.setVisibility(View.GONE);
            super.onPostExecute(result);
        }

        @Override
        protected Void doInBackground(Void... params) {
            adapter = new StuffAdapter(StuffListActivity.this, data);
            return null;
        }
    }

    /**
     * Function to load the spinner data from SQLite database
     * */
    private void loadSpinnerData(StuffActivityListController salc) {

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


    public void resetSpinnerValue() {
        spinner.setSelection(0);
    }

    @Override
    public void onDestroy() {
        if (SQLLiteDatabaseHelper.getInstance(this) != null) {
            SQLLiteDatabaseHelper.getInstance(this).close();
            super.onDestroy();
        }
    }

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
        List<Object> objects = new ArrayList<Object>();
        String category = spinner.getSelectedItem().toString();
        String date = (String)txtDate.getText();
        // Log.w("GJT", "Date sélectionnée : " + date);
        // Log.w("GJT", "Catégorie sélectionnée " + category);

        if (searchBarValue != null) {
            if (!date.equals("Filtrer par date      "))
                objects = salc.getApplicationModel().filter(StuffListActivity.this, category, searchBarValue.toString().trim(), date.substring(10).trim());
            else
                objects = salc.getApplicationModel().filter(StuffListActivity.this, category, searchBarValue.toString().trim(), null);
        } else {
            if (!date.equals("Filtrer par date      "))
                objects = salc.getApplicationModel().filter(StuffListActivity.this, category, "", date.substring(10).trim());
            else
                objects = salc.getApplicationModel().filter(StuffListActivity.this, category, "", null);
        }

        new LoadingListViewContentTask(objects).execute();
    }

    public void refreshStuffListData(List<Object> objectList) {
        new LoadingListViewContentTask(objectList).execute();
    }

    private void setUpNavigationView() {
        //Setting Navigation View Item Selected Listener to handle the item click of the navigation menu
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {

            // This method will trigger on item Click of navigation menu
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {

                //Check to see which item was being clicked and perform appropriate action
                switch (menuItem.getItemId()) {

                    case R.id.nav_locate_stuff:
                        List<Stuff> stuff = salc.getApplicationModel().getStuffList(StuffListActivity.this);

                        if (stuff.size() > 0) {
                            // Answers.getInstance().logContentView(new ContentViewEvent()
                             //        .putContentName("Locating All Pinpoints"));
                            //Answers.getInstance().logCustom(new CustomEvent("Locating All Pinpoints"));
                            // Start location service
                            //Intent intentMap = new Intent(context, ShowStuffActivity.class);
                            // StuffListActivity.this.startActivity(intentMap);
                            StuffListActivity.this.finish();
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
                    case R.id.nav_handle_category:
                        // launch new intent instead of loading fragment
                        // Start location service
                        Intent intent = new Intent(context, CategoryListActivity.class);
                        StuffListActivity.this.startActivity(intent);
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
                        List<Stuff> stuff2 = salc.getApplicationModel().getStuffList(StuffListActivity.this);

                        // 1. Instantiate an AlertDialog.Builder with its constructor
                        AlertDialog.Builder builder = new AlertDialog.Builder(StuffListActivity.this);

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
                        AlertDialog.Builder builder2 = new AlertDialog.Builder(StuffListActivity.this);

                        // 2. Chain together various setter methods to set the dialog characteristics
                        builder2.setMessage("Etes-vous sûr(e) de vouloir quitter l'application ?")
                                .setTitle("PinPointPlace");

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
                List<Object> objects = new ArrayList<Object>();
                String category = spinner.getSelectedItem().toString();
                String date = (String)txtDate.getText();
                // Log.w("GJT", "Catégorie sélectionnée " + category);
                if (!date.equals("Filtrer par date      "))
                    objects = salc.getApplicationModel().filter(StuffListActivity.this, category, searchQuery.toString().trim(), date);
                else
                    objects = salc.getApplicationModel().filter(StuffListActivity.this, category, searchQuery.toString().trim(), null);
                refreshStuffListData(objects);
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
        salc.getApplicationModel().deleteAllStuffFromDatabase(context);
        adapter.clear();
        mListView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    public boolean checkLocationPermission()
    {
        String permission = "android.Manifest.permission.ACCESS_FINE_LOCATION";
        int res = this.checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }


    protected void setPictureToImageView(final String imagePath, final ImageView mImageView) {
        ViewTreeObserver vto = mImageView.getViewTreeObserver();
        vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            public boolean onPreDraw() {
                mImageView.getViewTreeObserver().removeOnPreDrawListener(this);
                int finalHeight = mImageView.getMeasuredHeight();
                int finalWidth = mImageView.getMeasuredWidth();

                // Get the dimensions of the bitmap
                BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                bmOptions.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(imagePath, bmOptions);
                int photoW = bmOptions.outWidth;
                int photoH = bmOptions.outHeight;
                // Determine how much to scale down the image
                int scaleFactor = Math.min(photoW/finalWidth, photoH/finalHeight);

                // Decode the image file into a Bitmap sized to fill the View
                bmOptions.inJustDecodeBounds = false;
                bmOptions.inSampleSize = scaleFactor;
                bmOptions.inPurgeable = true;

                Bitmap bitmap = BitmapFactory.decodeFile(imagePath, bmOptions);

                Matrix matrix = new Matrix();
                matrix.postRotate(getImageOrientation(imagePath));
                Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                        bitmap.getHeight(), matrix, true);

                mImageView.setImageBitmap(rotatedBitmap);

                return true;
            }
        });


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

    private Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
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
            List<Object> objects = new ArrayList<Object>();
            String category = ((StuffListActivity)getActivity()).spinner.getSelectedItem().toString();
            String date = ((String)((StuffListActivity)getActivity()).txtDate.getText()).substring(10).trim();
            // Log.w("GJT", "Date choisie : " + date);
            if (!date.equals("Filtrer par date      "))
                objects = ((StuffListActivity)getActivity()).salc.getApplicationModel().filter(((StuffListActivity)getActivity()), category, "", date);
            else
                objects = ((StuffListActivity)getActivity()).salc.getApplicationModel().filter(((StuffListActivity)getActivity()), category, "", null);
            ((StuffListActivity)getActivity()).refreshStuffListData(objects);
            //((StuffListActivity)getActivity()).mListView.invalidate();
        }
    }


    public void showDatePickerDialog(View v){
        DialogFragment dialogFragment = new DatePickerFragment();
        dialogFragment.show(getSupportFragmentManager(), "show_date_picker");
    }

    public Spinner getSpinner() {
        return spinner;
    }

    public void setTxtDate(TextView txtDate) {
        this.txtDate = txtDate;
    }


    public void zoomImageFromThumb(final View thumbView, String imagePath) {
        // If there's an animation in progress, cancel it
        // immediately and proceed with this one.
        if (mCurrentAnimator != null) {
            mCurrentAnimator.cancel();
        }

        // Load the high-resolution "zoomed-in" image.
        final ImageView expandedImageView = (ImageView) findViewById(
                R.id.expanded_image);
        //expandedImageView.setImageBitmap(imageResId);
        if(imagePath != null)
            Picasso.with(this).load(new File(imagePath)).noFade().into(expandedImageView);
        // Calculate the starting and ending bounds for the zoomed-in image.
        // This step involves lots of math. Yay, math.
        final Rect startBounds = new Rect();
        final Rect finalBounds = new Rect();
        final Point globalOffset = new Point();

        // The start bounds are the global visible rectangle of the thumbnail,
        // and the final bounds are the global visible rectangle of the container
        // view. Also set the container view's offset as the origin for the
        // bounds, since that's the origin for the positioning animation
        // properties (X, Y).
        thumbView.getGlobalVisibleRect(startBounds);
        findViewById(R.id.drawer_layout).getGlobalVisibleRect(finalBounds, globalOffset);
        startBounds.offset(-globalOffset.x, -globalOffset.y);
        finalBounds.offset(-globalOffset.x, -globalOffset.y);

        // Adjust the start bounds to be the same aspect ratio as the final
        // bounds using the "center crop" technique. This prevents undesirable
        // stretching during the animation. Also calculate the start scaling
        // factor (the end scaling factor is always 1.0).
        float startScale;
        if ((float) finalBounds.width() / finalBounds.height()
                > (float) startBounds.width() / startBounds.height()) {
            // Extend start bounds horizontally
            startScale = (float) startBounds.height() / finalBounds.height();
            float startWidth = startScale * finalBounds.width();
            float deltaWidth = (startWidth - startBounds.width()) / 2;
            startBounds.left -= deltaWidth;
            startBounds.right += deltaWidth;
        } else {
            // Extend start bounds vertically
            startScale = (float) startBounds.width() / finalBounds.width();
            float startHeight = startScale * finalBounds.height();
            float deltaHeight = (startHeight - startBounds.height()) / 2;
            startBounds.top -= deltaHeight;
            startBounds.bottom += deltaHeight;
        }

        // Hide the thumbnail and show the zoomed-in view. When the animation
        // begins, it will position the zoomed-in view in the place of the
        // thumbnail.
        thumbView.setAlpha(0f);
        expandedImageView.setVisibility(View.VISIBLE);

        // Set the pivot point for SCALE_X and SCALE_Y transformations
        // to the top-left corner of the zoomed-in view (the default
        // is the center of the view).
        expandedImageView.setPivotX(0f);
        expandedImageView.setPivotY(0f);

        // Construct and run the parallel animation of the four translation and
        // scale properties (X, Y, SCALE_X, and SCALE_Y).
        AnimatorSet set = new AnimatorSet();
        set
                .play(ObjectAnimator.ofFloat(expandedImageView, View.X,
                        startBounds.left, finalBounds.left))
                .with(ObjectAnimator.ofFloat(expandedImageView, View.Y,
                        startBounds.top, finalBounds.top))
                .with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_X,
                        startScale, 1f)).with(ObjectAnimator.ofFloat(expandedImageView,
                View.SCALE_Y, startScale, 1f));
        set.setDuration(mShortAnimationDuration);
        set.setInterpolator(new DecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mCurrentAnimator = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mCurrentAnimator = null;
            }
        });
        set.start();
        mCurrentAnimator = set;

        // Upon clicking the zoomed-in image, it should zoom back down
        // to the original bounds and show the thumbnail instead of
        // the expanded image.
        final float startScaleFinal = startScale;
        expandedImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCurrentAnimator != null) {
                    mCurrentAnimator.cancel();
                }

                // Animate the four positioning/sizing properties in parallel,
                // back to their original values.
                AnimatorSet set = new AnimatorSet();
                set.play(ObjectAnimator
                        .ofFloat(expandedImageView, View.X, startBounds.left))
                        .with(ObjectAnimator
                                .ofFloat(expandedImageView,
                                        View.Y,startBounds.top))
                        .with(ObjectAnimator
                                .ofFloat(expandedImageView,
                                        View.SCALE_X, startScaleFinal))
                        .with(ObjectAnimator
                                .ofFloat(expandedImageView,
                                        View.SCALE_Y, startScaleFinal));
                set.setDuration(mShortAnimationDuration);
                set.setInterpolator(new DecelerateInterpolator());
                set.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        thumbView.setAlpha(1f);
                        expandedImageView.setVisibility(View.GONE);
                        mCurrentAnimator = null;
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        thumbView.setAlpha(1f);
                        expandedImageView.setVisibility(View.GONE);
                        mCurrentAnimator = null;
                    }
                });
                set.start();
                mCurrentAnimator = set;
            }
        });
    }

}
