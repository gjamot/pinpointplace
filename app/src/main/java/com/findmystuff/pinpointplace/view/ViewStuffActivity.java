package com.findmystuff.pinpointplace.view;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.LabeledIntent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;
import com.findmystuff.pinpointplace.controller.StuffActivityListController;
import com.findmystuff.pinpointplace.model.Stuff;
import com.ppp.pinpointplace.R;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ViewStuffActivity extends AppCompatActivity {
    private TextView stuffTypeTxt;
    private String stuffName;
    private String stuffDescription;
    private String stuffType;
    private StuffActivityListController salc;
    ImageView imageView;
    String mCurrentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_stuff);

        imageView = (ImageView) findViewById(R.id.img_icon);
        stuffTypeTxt = (TextView) findViewById(R.id.stuff_type);

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

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_action_back_white);
        toolbar.setTitle("Pinpoint " + stuffName);
        setSupportActionBar(toolbar);

        TextView stuffText = (TextView)findViewById(R.id.viewStuffName);
        stuffText.setText(stuffName);

        TextView stuffDescriptionTxt = (TextView)findViewById(R.id.stuff_description);
        stuffDescriptionTxt.setText(stuffDescription);
        // Log.w("GJT", stuffName);
        salc = new StuffActivityListController(this);
        ImageView stuffImage = (ImageView)findViewById(R.id.img_icon);
        Bitmap bmp = BitmapFactory.decodeResource(this.getResources(), R.drawable.no_image_available);
        mCurrentPhotoPath = salc.getApplicationModel().getImagePathFromStuffName(this, stuffName);
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

        stuffType = salc.getApplicationModel().getCategoryNameFromStuffName(this, stuffName);
        stuffTypeTxt.setText(stuffType);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.removeItem(R.id.action_search);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_view, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case android.R.id.home:
                this.finish();
                return true;

            case R.id.action_share:
                // Answers.getInstance().logContentView(new ContentViewEvent()
                //         .putContentName("Sharing Pinpoint"));
                Stuff stuff = salc.getApplicationModel().getStuffFromName(this, stuffName);
                //Answers.getInstance().logCustom(new CustomEvent("Sharing Pinpoint"));
               String uri = "http://maps.google.com/maps?daddr=" +stuff.getLatitude().toString()+","+stuff.getLongitude().toString();
                File file = null;
                if( stuff.getImagePath() != null)
                    file = new File(stuff.getImagePath());


                Resources resources = this.getResources();
                Intent emailIntent = new Intent(Intent.ACTION_SEND);
                // Native email client doesn't currently support HTML, but it doesn't hurt to try in case they fix it
                emailIntent.putExtra(Intent.EXTRA_TEXT, uri);
                //emailIntent.putExtra(Intent.EXTRA_SUBJECT, stuffSelected.getName());
                emailIntent.setType("*/*");

                Intent openInChooser = Intent.createChooser(emailIntent, "Partager avec");
                PackageManager pm = this.getPackageManager();
                List<ResolveInfo> resInfo = pm.queryIntentActivities(emailIntent, 0);
                List<LabeledIntent> intentList = new ArrayList<LabeledIntent>();
                for (int i = 0; i < resInfo.size(); i++) {
                    // Extract the label, append it, and repackage it in a LabeledIntent
                    ResolveInfo ri = resInfo.get(i);
                    String packageName = ri.activityInfo.packageName;
                    //Log.w("GJT", "Package name : " + packageName);
                    if(packageName.contains("android.email")) {
                        emailIntent.setPackage(packageName);
                        if (file != null)
                            emailIntent.putExtra(Intent.EXTRA_STREAM, "file://" + Uri.parse(file.toString()));
                        emailIntent.putExtra(Intent.EXTRA_TEXT, uri);
                        emailIntent.putExtra(Intent.EXTRA_SUBJECT, stuff.getName());
                        emailIntent.setType("message/rfc822");
                    } else if(packageName.contains("com.whatsapp") || packageName.contains("com.facebook.orca") || packageName.contains("android.gm")) {
                        Intent intent = new Intent();
                        intent.setComponent(new ComponentName(packageName, ri.activityInfo.name));
                        intent.setAction(Intent.ACTION_SEND);
                        intent.setType("text/plain");

                        if(packageName.contains("com.whatsapp")) {
                            if (file != null) {
                                intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(file.toString()));
                                if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP) {
                                    intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                                }
                                else if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.JELLY_BEAN) {
                                    ClipData clip=
                                            ClipData.newUri(this.getContentResolver(), "A photo", Uri.parse(file.toString()));

                                    intent.setClipData(clip);
                                    intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                                }
                                else {
                                    List<ResolveInfo> resInfoList=
                                            this.getPackageManager()
                                                    .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);

                                    for (ResolveInfo resolveInfo : resInfoList) {
                                        this.grantUriPermission(packageName, Uri.parse(file.toString()),
                                                Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                                    }
                                }
                            }
                            intent.putExtra(Intent.EXTRA_TEXT, "Nom : " + stuff.getName() + "\n\n" + "Categorie : " + stuffType + "\n\n" + "Informations complémentaires : " + "\n" + stuff.getDescription() + "\n\n" + uri);
                            intent.setType("*/*");
                        } else if(packageName.contains("com.facebook.orca")) {
                            // Warning: Facebook IGNORES our text. They say "These fields are intended for users to express themselves. Pre-filling these fields erodes the authenticity of the user voice."
                            // One workaround is to use the Facebook SDK to post, but that doesn't allow the user to choose how they want to share. We can also make a custom landing page, and the link
                            // will show the <meta content ="..."> text from that page with our link in Facebook.
                            //intent.setClassName("com.facebook.orca", "com.facebook.orca.activity.composer.ImplicitShareIntentHandler");
                            //intent.setClassName("com.facebook.katana", "com.facebook.katana.activity.composer.ImplicitShareIntentHandler");
                            intent.setPackage("com.facebook.orca");
                            //intent.putExtra(Intent.EXTRA_SUBJECT, "Object provenant de FindMyStuff");
                            //if (file != null)
                            //   intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(file.toString()));
                            intent.putExtra(Intent.EXTRA_TEXT, "Nom : " + stuff.getName() + "\n\n" + "Categorie : " + stuffType + "\n\n" + "Informations complémentaires : " + "\n" + stuff.getDescription() + "\n\n" + uri);
                            //if (file != null)
                            //    intent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + file.toString()));
                            //intent.putExtra(Intent.EXTRA_STREAM,
                            //       Uri.parse( Environment.getExternalStorageDirectory()+ File.separator+"temporary_file.jpg"));
                            intent.setType("text/plain");
                        } else if(packageName.contains("android.gm")) { // If Gmail shows up twice, try removing this else-if clause and the reference to "android.gm" above
                            intent.putExtra(Intent.EXTRA_SUBJECT, "Objet provenant de PinPointPlace");
                            if (file != null)
                                intent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + file.toString()));
                            intent.putExtra(Intent.EXTRA_TEXT, "Nom : " + stuff.getName() + "\n\n" + "Categorie : " + stuffType + "\n\n" + "Informations complémentaires : " + "\n" + stuff.getDescription() + "\n\n" + uri);
                            intent.setType("message/rfc822");
                        }
                        intent.setPackage(packageName);
                        intentList.add(new LabeledIntent(intent, packageName, ri.loadLabel(pm), ri.icon));
                    } else if (packageName.contains("com.twitter.android")) {
                        Intent intent = new Intent();
                        intent.setComponent(new ComponentName(packageName, ri.activityInfo.name));
                        intent.setAction(Intent.ACTION_SEND);
                        intent.setType("*/*");//Set MIME Type
                        intent.putExtra(Intent.EXTRA_SUBJECT, stuffType + " partagé via PinPointPlace");
                        intent.putExtra(Intent.EXTRA_TEXT, stuffType + " " + stuff.getName() + " partagé via PinPointPlace" + "\n\n" + "Informations complémentaires : " + "\n" + stuff.getDescription() + "\n\nCoordonnées :\n\n" + uri + "\n\n\n");
                        if (file != null) {
                            intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(file.toString()));// Pur Image to intent
                            if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP) {
                                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                            }
                            else if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.JELLY_BEAN) {
                                ClipData clip=
                                        ClipData.newUri(this.getContentResolver(), "A photo", Uri.parse(file.toString()));

                                intent.setClipData(clip);
                                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                            }
                            else {
                                List<ResolveInfo> resInfoList=
                                        this.getPackageManager()
                                                .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);

                                for (ResolveInfo resolveInfo : resInfoList) {
                                    this.grantUriPermission(packageName, Uri.parse(file.toString()),
                                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                                }
                            }
                        }
                        //intent.putExtra(Intent.EXTRA_TEXT, uri);
                        intent.setPackage(packageName);
                        intentList.add(new LabeledIntent(intent, packageName, ri.loadLabel(pm), ri.icon));
                    }
                    /**
                     else if (packageName.contains("com.facebook")) {
                     ShareLinkContent content = new ShareLinkContent.Builder()
                     .setContentTitle(stuffCategory + " " + stuffSelected.getName())
                     .setImageUrl(Uri.parse(file.toString()))
                     .setContentUrl(Uri.parse(uri))
                     .setContentDescription("Partagé via PinPointPlace")
                     .build();
                     Intent intent = new Intent();
                     intent.setComponent(new ComponentName(packageName, ri.activityInfo.name));
                     intent.setAction(Intent.ACTION_SEND);
                     intent.setType("image/*");//Set MIME Type
                     intent.putExtra(Intent.EXTRA_SUBJECT, stuffCategory + " partagé via PinPointPlace");
                     intent.putExtra(Intent.EXTRA_TEXT, stuffCategory + " " + stuffSelected.getName() + " partagé via PinPointPlace\n\nCoordonnées :\n\n" + uri + "\n\n\n");
                     if (file != null) {
                     intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(file.toString()));// Pur Image to intent
                     }
                     //intent.putExtra(Intent.EXTRA_TEXT, uri);
                     intent.setPackage(packageName);
                     intentList.add(new LabeledIntent(intent, packageName, ri.loadLabel(pm), ri.icon));
                     } */
                    else if (packageName.contains("com.ppp.pinpointplace")) {
                        Intent intentFacebook = new Intent(this, FacebookActivity.class);
                        intentFacebook.putExtra("STUFF_CATEGORY", stuffType);
                        intentFacebook.putExtra("STUFF_NAME", stuff.getName());
                        intentFacebook.putExtra("STUFF_DESCRIPTION", stuff.getDescription());
                        intentFacebook.putExtra("STUFF_IMG_FILE_URI", file.toString());
                        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP) {
                            intentFacebook.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        }
                        else if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.JELLY_BEAN) {
                            ClipData clip=
                                    ClipData.newUri(this.getContentResolver(), "A photo", Uri.parse(file.toString()));

                            intentFacebook.setClipData(clip);
                            intentFacebook.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        }
                        else {
                            List<ResolveInfo> resInfoList=
                                    this.getPackageManager()
                                            .queryIntentActivities(intentFacebook, PackageManager.MATCH_DEFAULT_ONLY);

                            for (ResolveInfo resolveInfo : resInfoList) {
                                this.grantUriPermission(packageName, Uri.parse(file.toString()),
                                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                            }
                        }
                        intentFacebook.putExtra("LOCATION_URI", uri);
                        intentFacebook.setComponent(new ComponentName(packageName, ri.activityInfo.name));
                        intentFacebook.setPackage(packageName);
                        intentList.add(new LabeledIntent(intentFacebook, packageName, ri.loadLabel(pm), ri.icon));
                    }

                }


                // convert intentList to array
                LabeledIntent[] extraIntents = intentList.toArray( new LabeledIntent[ intentList.size() ]);

                openInChooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, extraIntents);
                this.startActivity(openInChooser);
                return true;

            case R.id.action_locate:
                // Answers.getInstance().logContentView(new ContentViewEvent()
                     //   .putContentName("Locating Pinpoint"));

                //Answers.getInstance().logCustom(new CustomEvent("Locating Pinpoint"));

                // Log.w("GJT", "Locating object");
                Stuff stuffSelected = salc.getApplicationModel().getStuffFromName(this, stuffName);
                //Uri gmmIntentUri = Uri.parse("google.navigation:q="+stuffSelected.getLatitude().toString()+","+stuffSelected.getLongitude().toString() + "&mode=w");
                Uri gmmIntentUri = Uri.parse("geo:" + stuffSelected.getLatitude() + "," + stuffSelected.getLongitude() + "?q=" + stuffSelected.getLatitude() + "," + stuffSelected.getLongitude() + "?z=13(" + stuffType + " " + stuffSelected.getName() + ")");
                Uri genericUri = Uri.parse("geo:" + stuffSelected.getLatitude() + "," + stuffSelected.getLongitude() + "?q=" + stuffSelected.getLatitude() + "," + stuffSelected.getLongitude());
                Uri wazeIntentUri = Uri.parse("waze://?ll=" + stuffSelected.getLatitude() + "," + stuffSelected.getLongitude());
                Uri cityMapperIntentUri = Uri.parse("citymapper://directions?endcoord=" + stuffSelected.getLatitude() + "%2C" + stuffSelected.getLongitude());

                Intent wazeIntent = new Intent(Intent.ACTION_VIEW, wazeIntentUri);
                Intent googleMapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                Intent cityMapperIntent = new Intent(Intent.ACTION_VIEW, cityMapperIntentUri);
                Intent genericIntent = new Intent(Intent.ACTION_VIEW, genericUri);
                PackageManager pm2 = this.getPackageManager();
                List<LabeledIntent> intentList2 = new ArrayList<LabeledIntent>();
                Intent openInChooser2 = Intent.createChooser(genericIntent, "Localiser avec");

                /** Google map intent handling
                 if (isPackageInstalled("com.google.android.apps.maps", pm)){
                 if (!packageName.equals("com.facebook.katana")) { // Remove Facebook Intent share
                 intentList.add(new LabeledIntent(intent, packageName, ri.loadLabel(pm), ri.icon));
                 }
                 Intent intent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                 ResolveInfo ri = pm.resolveActivity(intent, 0);
                 String packageName = ri.activityInfo.packageName;
                 intent.setComponent(new ComponentName(packageName, ri.activityInfo.name));
                 intent.setAction(Intent.ACTION_VIEW);
                 intent.putExtra(Intent.EXTRA_TEXT, gmmIntentUri);
                 intent.setType("text/plain");
                 intent.setPackage(packageName);

                 }
                 Log.w("GJT", "labeled intent list size : " + intentList.size());
                 */
                List<ResolveInfo> resInfo2 = null;
                /** CITYMAPPER INTENT HANDLING
                 resInfo = pm.queryIntentActivities(cityMapperIntent, 0);
                 for (int i = 0; i < resInfo.size(); i++) {
                 // Extract the label, append it, and repackage it in a LabeledIntent
                 ResolveInfo ri = resInfo.get(i);
                 String packageName = ri.activityInfo.packageName;
                 if (packageName.equals("com.citymapper.app.release")) {
                 Intent intent = new Intent();
                 intent.setComponent(new ComponentName(packageName, ri.activityInfo.name));
                 intent.setAction(Intent.ACTION_VIEW);
                 intent.putExtra(Intent.EXTRA_TEXT, cityMapperIntentUri);
                 //intent.setData(cityMapperIntentUri);
                 intent.setType("text/plain");
                 intent.setPackage(packageName);
                 intentList.add(new LabeledIntent(intent, packageName, ri.loadLabel(pm), ri.icon));
                 }
                 } */

                /** GOOGLE MAP INTENT HANDLING
                 resInfo = pm.queryIntentActivities(googleMapIntent, 0);
                 for (int i = 0; i < resInfo.size(); i++) {
                 // Extract the label, append it, and repackage it in a LabeledIntent
                 ResolveInfo ri = resInfo.get(i);
                 String packageName = ri.activityInfo.packageName;
                 if (packageName.equals("com.google.android.apps.maps") && !packageName.equals("com.citymapper.app.release")) {
                 Intent intent = new Intent();
                 intent.setComponent(new ComponentName(packageName, ri.activityInfo.name));
                 intent.setAction(Intent.ACTION_VIEW);
                 intent.putExtra(Intent.EXTRA_TEXT, gmmIntentUri);
                 intent.setType("text/plain");
                 intent.setPackage(packageName);
                 intentList.add(new LabeledIntent(intent, packageName, ri.loadLabel(pm), ri.icon));
                 }
                 }

                 /** WAZE INTENT HANDLING
                 resInfo = pm.queryIntentActivities(wazeIntent, 0);
                 for (int i = 0; i < resInfo.size(); i++) {
                 // Extract the label, append it, and repackage it in a LabeledIntent
                 ResolveInfo ri = resInfo.get(i);
                 String packageName = ri.activityInfo.packageName;
                 if (packageName.contains("com.waze")) {
                 Intent intent = new Intent();
                 intent.setComponent(new ComponentName(packageName, ri.activityInfo.name));
                 intent.setAction(Intent.ACTION_VIEW);
                 intent.putExtra(Intent.EXTRA_TEXT, gmmIntentUri);
                 intent.setType("text/plain");
                 intent.setPackage(packageName);
                 intentList.add(new LabeledIntent(intent, packageName, ri.loadLabel(pm), ri.icon));
                 }
                 }

                 */
                /** CITYMAPPER INTENT HANDLING
                 resInfo = pm.queryIntentActivities(cityMapperIntent, 0);
                 for (int i = 0; i < resInfo.size(); i++) {
                 // Extract the label, append it, and repackage it in a LabeledIntent
                 ResolveInfo ri = resInfo.get(i);
                 String packageName = ri.activityInfo.packageName;
                 if (packageName.contains("com.citymapper.app.release")) {
                 Intent intent = new Intent();
                 intent.setComponent(new ComponentName(packageName, ri.activityInfo.name));
                 intent.setAction(Intent.ACTION_VIEW);
                 intent.putExtra(Intent.EXTRA_TEXT, cityMapperIntentUri);
                 intent.setType("text/plain");
                 intent.setPackage(packageName);
                 intentList.add(new LabeledIntent(intent, packageName, ri.loadLabel(pm), ri.icon));
                 }
                 }
                 */
                /**
                 for (int i = 0; i < resInfo.size(); i++) {
                 // Extract the label, append it, and repackage it in a LabeledIntent
                 ResolveInfo ri = resInfo.get(i);
                 String packageName = ri.activityInfo.packageName;

                 if (packageName.contains("com.waze") || packageName.contains("com.citymapper.app.release") || packageName.contains("com.google.android.apps.maps")) {
                 if(packageName.contains("com.waze") || packageName.contains("com.citymapper.app.release")) {
                 Intent intent = new Intent();
                 intent.setComponent(new ComponentName(packageName, ri.activityInfo.name));
                 intent.setAction(Intent.ACTION_VIEW);
                 if (packageName.contains("com.waze")) {
                 Uri intentUri = Uri.parse("waze://?ll=" + stuffSelected.getLatitude() + "," + stuffSelected.getLongitude());
                 intent.putExtra(Intent.EXTRA_TEXT, intentUri);
                 intent.setType("text/plain");
                 } else if (packageName.contains("com.citymapper.app.release")) {
                 Uri intentUri = Uri.parse("citymapper://directions?endcoord=" + stuffSelected.getLatitude() + "%2C" + stuffSelected.getLongitude());
                 /**
                 try {
                 Geocoder geo = new Geocoder(context, Locale.getDefault());
                 List<Address> addresses = geo.getFromLocation(stuffSelected.getLatitude(), stuffSelected.getLongitude(), 1);
                 if (addresses.size() > 0) {
                 intentUri = Uri.parse("citymapper://directions?endcoord=" + stuffSelected.getLatitude() + "%2C" + stuffSelected.getLongitude() + "&endaddress= " + addresses.get(0).getFeatureName() + "%20" + addresses.get(0).getLocality() + "%20" + addresses.get(0).getAdminArea() + "%20" + addresses.get(0).getCountryName());

                 }
                 } catch (Exception e) {
                 e.printStackTrace(); // getFromLocation() may sometimes fail
                 }

                 intent.putExtra(Intent.EXTRA_TEXT, intentUri);
                 }
                 }

                 }
                 }*/


                // convert intentList to array
                //LabeledIntent[] extraIntents = intentList.toArray( new LabeledIntent[ intentList.size() ]);
                //openInChooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, extraIntents);
                this.startActivity(openInChooser2);
                return true;

            case R.id.action_update:
                Intent intent = new Intent(this, UpdateStuffActivity.class);
                intent.putExtra("STUFF_NAME", stuffName);
                intent.putExtra("STUFF_DESCRIPTION", stuffDescription);
                this.finish();
                this.startActivity(intent);
                return true;
            case R.id.action_delete:
                final String stuffImagePath = mCurrentPhotoPath;
                // 1. Instantiate an AlertDialog.Builder with its constructor
                AlertDialog.Builder builder = new AlertDialog.Builder(this);

                // 2. Chain together various setter methods to set the dialog characteristics
                builder.setMessage("Etes-vous sûr(e) de vouloir supprimer le pinpoint " + stuffName + " de la liste ?")
                        .setTitle("PinPointPlace");

                // Add the buttons
                builder.setPositiveButton("Confirmer", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Answers.getInstance().logContentView(new ContentViewEvent()
                          //      .putContentName("Deleting Pinpoint"));
                        //Answers.getInstance().logCustom(new CustomEvent("Deleting Pinpoint"));
                        salc.getApplicationModel().deleteStuffFromName(ViewStuffActivity.this, stuffName, stuffImagePath);
                        ViewStuffActivity.this.finish();
                        //Toast.makeText(ShowStuffActivity.g, "Le pinpoint a été bien été supprimé", Toast.LENGTH_SHORT).show();
                    }
                });
                builder.setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
                // 3. Get the AlertDialog from create()
                AlertDialog dialog = builder.create();
                dialog.show();
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        invalidateOptionsMenu();
    }

}
