package com.findmystuff.pinpointplace.view;

import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.findmystuff.pinpointplace.controller.UpdateStuffActivityController;
import com.ppp.pinpointplace.R;
import com.findmystuff.pinpointplace.utils.Constant;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by Greg on 30/12/2016.
 */

public class UpdateStuffActivity extends AppCompatActivity implements Constant{
    private Button btnSave;
    private ImageButton btnUpdatePic;
    private ImageButton btnUpdateLocation;
    private Spinner spinner;
    private UpdateStuffActivityController usac;
    private String stuffName;
    private String stuffDescription;
    ImageView imageView;
    String mCurrentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_update);
        btnUpdatePic = (ImageButton) findViewById(R.id.btn_take_pic);
        btnUpdateLocation = (ImageButton) findViewById(R.id.btn_update_location);
        imageView = (ImageView) findViewById(R.id.img_icon);
        btnSave = (Button) findViewById(R.id.btn_save);
        spinner = (Spinner) findViewById(R.id.spinner_stuff_type);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_action_back_white);
        toolbar.setTitle("Modification d'un pinpoint");
        setSupportActionBar(toolbar);

        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras == null) {
                stuffName = null;
                stuffDescription = null;
            } else {
                stuffName = extras.getString("STUFF_NAME");
                stuffDescription = extras.getString("STUFF_DESCRIPTION");
            }
        } else {
            stuffName = (String) savedInstanceState.getSerializable("STUFF_NAME");
            stuffDescription = (String) savedInstanceState.getSerializable("STUFF_DESCRIPTION");
        }

        TextView mainTitleTextView = (TextView)findViewById(R.id.title);
        mainTitleTextView.setText(mainTitleTextView.getText().toString() + stuffName);
        EditText stuffEditText = (EditText)findViewById(R.id.editStuffName);
        stuffEditText.setText(stuffName);

        EditText stuffDescriptionTxt = (EditText)findViewById(R.id.stuff_description);
        stuffDescriptionTxt.setText(stuffDescription);
        View.OnFocusChangeListener ofcListener = new MyFocusChangeListener();
        stuffEditText.setOnFocusChangeListener(ofcListener);
        stuffDescriptionTxt.setOnFocusChangeListener(ofcListener);
        stuffEditText.setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {
                UpdateStuffActivity.hideSoftKeyboard(UpdateStuffActivity.this);
                return false;
            }

        });
        stuffDescriptionTxt.setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {
                UpdateStuffActivity.hideSoftKeyboard(UpdateStuffActivity.this);
                return false;
            }

        });
        // Log.w("GJT", stuffName);
        usac = new UpdateStuffActivityController(this, stuffEditText, stuffDescriptionTxt);

        ImageView stuffImage = (ImageView)findViewById(R.id.img_icon);
        Bitmap bmp = BitmapFactory.decodeResource(this.getResources(), R.drawable.no_image_available);
        mCurrentPhotoPath = usac.getApplicationModel().getImagePathFromStuffName(this, stuffName);
        /**
        if (mCurrentPhotoPath != null)
            this.setPictureToImageView(mCurrentPhotoPath, stuffImage);
        else
            stuffImage.setImageBitmap(bmp);
        */
        if(mCurrentPhotoPath != null)
            Picasso.with(this).load(new File(mCurrentPhotoPath)).noFade().into(imageView);
        else {
            Picasso.with(this).load(R.drawable.no_image_available).noFade().into(imageView);
        }

        btnUpdatePic.setOnClickListener(usac);
        btnUpdateLocation.setOnClickListener(usac);
        btnSave.setOnClickListener(usac);

        // Register the listener with the Location Manager to receive location updates
        // Assume thisActivity is the current activity
        loadSpinnerData(usac, stuffName);

        // Spinner click listener
        spinner.setOnItemSelectedListener(usac);
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
     * Function to load the spinner data from SQLite database
     * */
    private void loadSpinnerData(UpdateStuffActivityController asac, String stuffName) {

        // Spinner Drop down elements
        List<String> types = asac.getApplicationModel().getAllStuffType(this);

        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, types);

        // Drop down layout style - list view with radio button
        dataAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        String categoryName = asac.getApplicationModel().getCategoryNameFromStuffName(this, stuffName);
        //Log.w("GJT", "Category in spinner : " + categoryName);
        // attaching data adapter to spinner
        spinner.setAdapter(dataAdapter);
        int position = dataAdapter.getPosition(categoryName);
        //Log.w("GJT", "Position du spinner : " + position);
        spinner.setSelection(position);
        //Log.w("GJT", "Catégorie dans le spinner : " + spinner.getSelectedItem().toString());
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
            if (mCurrentPhotoPath != null) {
                existingfile = new File(mCurrentPhotoPath);
                if (existingfile != null)
                    deleted = existingfile.delete();
                // if (deleted)
                    // Log.w("GJT", "File successfully deleted");
            }
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.e("GJT", "Error creating file");
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
     * On activity result
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK)
            return;

        switch (requestCode) {
            case CAMERA_REQUEST:
                if(mCurrentPhotoPath != null)
                    Picasso.with(this).load(new File(mCurrentPhotoPath)).noFade().into(imageView);
                //if (mCurrentPhotoPath != null)
                //    setPictureToImageView(imageView);
                break;
        }
    }

    public String getImagePath() {
        return mCurrentPhotoPath;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean checkLocationPermission()
    {
        String permission = "android.permission.ACCESS_FINE_LOCATION";
        int res = this.checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

    private class MyFocusChangeListener implements View.OnFocusChangeListener {

        public void onFocusChange(View v, boolean hasFocus){

            if(!hasFocus) {
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
