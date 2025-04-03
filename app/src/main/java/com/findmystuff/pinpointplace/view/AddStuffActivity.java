package com.findmystuff.pinpointplace.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;

import com.findmystuff.pinpointplace.controller.AddStuffActivityController;
import com.findmystuff.pinpointplace.model.GpsLocationAsyncTask;
import com.findmystuff.pinpointplace.utils.Constant;
import com.ppp.pinpointplace.R;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by Greg on 27/12/2016.
 */

public class AddStuffActivity extends AppCompatActivity implements Constant {
    Button btnSave;
    ImageButton btnTakePicture;
    Spinner spinner;
    EditText stuffEditText;
    ImageView imageView;
    AddStuffActivityController asac;
    LocationManager locationManager;
    GpsLocationAsyncTask myAsyncTask;
    String mCurrentPhotoPath;
    EditText descriptionTxt;
    byte imageInByte[];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_new);
        btnTakePicture = (ImageButton) findViewById(R.id.btn_take_pic);
        btnSave = (Button) findViewById(R.id.btn_save);
        spinner = (Spinner) findViewById(R.id.spinner_stuff_type);
        imageView = (ImageView) findViewById(R.id.img_icon);
        stuffEditText = (EditText)findViewById(R.id.editStuffName);
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        descriptionTxt = (EditText) findViewById(R.id.stuff_description);
        descriptionTxt.setMovementMethod(new ScrollingMovementMethod());
        View.OnFocusChangeListener ofcListener = new MyFocusChangeListener();
        stuffEditText.setOnFocusChangeListener(ofcListener);
        descriptionTxt.setOnFocusChangeListener(ofcListener);
        stuffEditText.setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {
                AddStuffActivity.hideSoftKeyboard(AddStuffActivity.this);
                return false;
            }

        });
        descriptionTxt.setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {
                AddStuffActivity.hideSoftKeyboard(AddStuffActivity.this);
                return false;
            }

        });
        asac = new AddStuffActivityController(this, stuffEditText, locationManager);
        if (checkLocationPermission())
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, asac);
        btnTakePicture.setOnClickListener(asac);
        btnSave.setOnClickListener(asac);
        // Spinner click listener
        spinner.setOnItemSelectedListener(asac);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_action_back_white);
        toolbar.setTitle("Ajout d'un pinpoint");
        setSupportActionBar(toolbar);

        // Register the listener with the Location Manager to receive location updates
        // Assume thisActivity is the current activity

        loadSpinnerData(asac);
        myAsyncTask = new GpsLocationAsyncTask(this, locationManager, asac);
        myAsyncTask.execute();

    }

    /**
     * Function to load the spinner data from SQLite database
     * */
    private void loadSpinnerData(AddStuffActivityController asac) {

        // Spinner Drop down elements
        List<String> types = asac.getApplicationModel().getAllStuffType(this);

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

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    /**
     * open camera method
     */
    public void callCamera()
    {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File existingfile = null;
            boolean deleted = false;

            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // 1. Instantiate an AlertDialog.Builder with its constructor
                AlertDialog.Builder builder = new AlertDialog.Builder(AddStuffActivity.this);

                // 2. Chain together various setter methods to set the dialog characteristics
                builder.setMessage("Espace insuffisant pour la création d'un nouveau pinpoint. Veuillez libérer de l'espace ou supprimer un pinpoint.")
                        .setTitle("PinPointPlace");

                // Add the buttons
                builder.setNeutralButton("Retour", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        AddStuffActivity.this.finish();
                    }
                });

                // 3. Get the AlertDialog from create()
                AlertDialog dialog = builder.create();
                dialog.show();
            }

            if (mCurrentPhotoPath != null) {
                existingfile = new File(mCurrentPhotoPath);
                if (existingfile != null)
                    deleted = existingfile.delete();
                // if (deleted)
                    // Log.w("GJT", "File successfully deleted");
            }

            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.ppp.pinpointplace.android.fileprovider",
                        photoFile);

                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);

                if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP) {
                    takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                }
                else if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.JELLY_BEAN) {
                    ClipData clip=
                            ClipData.newUri(getContentResolver(), "A photo", photoURI);

                    takePictureIntent.setClipData(clip);
                    takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                }
                else {
                    List<ResolveInfo> resInfoList=
                            getPackageManager()
                                    .queryIntentActivities(takePictureIntent, PackageManager.MATCH_DEFAULT_ONLY);

                    for (ResolveInfo resolveInfo : resInfoList) {
                        String packageName = resolveInfo.activityInfo.packageName;
                        grantUriPermission(packageName, photoURI,
                                Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    }
                }
                startActivityForResult(takePictureIntent, CAMERA_REQUEST);
            }

        }

    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    private void setPictureToImageView(ImageView mImageView) {
        // Get the dimensions of the View
        int targetW = mImageView.getWidth();
        int targetH = mImageView.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);

        Matrix matrix = new Matrix();
        matrix.postRotate(getImageOrientation(mCurrentPhotoPath));
        Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                bitmap.getHeight(), matrix, true);

        mImageView.setImageBitmap(rotatedBitmap);
    }

    private int getImageOrientation(String imagePath){
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


    /**
     * On activity result
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK)
            return;

        switch (requestCode) {
            case CAMERA_REQUEST:
                //data.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                if(mCurrentPhotoPath != null)
                    Picasso.with(this).load(new File(mCurrentPhotoPath)).noFade().into(imageView);
                else {
                    Picasso.with(this).load(R.drawable.no_image_available).noFade().into(imageView);
                }
                //if (mCurrentPhotoPath != null)
                //    setPictureToImageView(imageView);
                break;
        }
    }

    public byte[] getImageInByte() {
        return imageInByte;
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


    public String getmCurrentPhotoPath() {
        return mCurrentPhotoPath;
    }

    public EditText getDescriptionTxt() {
        return descriptionTxt;
    }

    private class MyFocusChangeListener implements View.OnFocusChangeListener {

        public void onFocusChange(View v, boolean hasFocus){

            if((v.getId() == R.id.stuff_description || v.getId() == R.id.editStuffName) && !hasFocus) {

                InputMethodManager imm =  (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

            }
        }
    }


    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager)  activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }


}
