package com.findmystuff.pinpointplace.view;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.LabeledIntent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.findmystuff.pinpointplace.model.Category;
import com.ppp.pinpointplace.R;
import com.findmystuff.pinpointplace.model.ApplicationModel;
import com.findmystuff.pinpointplace.model.Stuff;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Greg on 30/12/2016.
 */

public class StuffAdapter extends ArrayAdapter<Object> {
    private ApplicationModel applicationModel;
    private Context context;
    private Object object;
    private static final int TYPE_STUFF = 0;
    private static final int TYPE_DIVIDER = 1;
    private List<Object> objects;

    //stuffs est la liste des models à afficher
    public StuffAdapter(Context context, List<Object> objects) {
        super(context, 0, objects);
        this.context = context;
        this.objects = objects;
        applicationModel = new ApplicationModel();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        int type = getItemViewType(position);
        if (convertView == null) {
            switch (type) {
                case TYPE_STUFF:
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.row_stuff, parent, false);
                    break;
                case TYPE_DIVIDER:
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.row_header, parent, false);
                    break;
            }
        }

        switch (type) {
            case TYPE_STUFF:
                StuffViewHolder viewHolder = (StuffViewHolder) convertView.getTag();
                if(viewHolder == null){
                    viewHolder = new StuffViewHolder();
                    viewHolder.name = (TextView) convertView.findViewById(R.id.stuff_name);
                    viewHolder.image = (ImageButton) convertView.findViewById(R.id.img_icon);
                    viewHolder.date = (TextView) convertView.findViewById(R.id.stuff_date);
                    convertView.setTag(viewHolder);
                }

                //getItem(position) va récupérer l'item [position] de la List<Tweet> tweets
                object = getItem(position);
                Stuff stuff = (Stuff)object;
                //il ne reste plus qu'à remplir notre vue
                if (stuff.getName().length() > 12)
                    viewHolder.name.setText(stuff.getName().substring(0, 12) + "...");
                else
                    viewHolder.name.setText(stuff.getName());
                viewHolder.date.setText(stuff.getDate());
                /**
                 String imagePath = stuff.getImagePath();
                 Bitmap bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.no_image_available);
                 if(imagePath != null)
                 //((StuffListActivity)context).setPictureToImageView(imagePath, viewHolder.image);
                 Picasso.with(context).load(new File(imagePath)).noFade().into(viewHolder.image);
                 else {
                 Picasso.with(context).load(R.drawable.no_image_available).noFade().into(viewHolder.image);
                 //viewHolder.image.setImageBitmap(bmp);
                 }
                 */
                byte [] thumbnailByte = stuff.getThumbnailByte();
                Bitmap bmp = null;

                if (thumbnailByte != null) {
                    bmp = BitmapFactory.decodeByteArray(thumbnailByte, 0, thumbnailByte.length);
                    viewHolder.image.setImageBitmap(((StuffListActivity)context).rotateBitmap(bmp, getImageOrientation(stuff.getImagePath())));
                }

                ImageButton imgBtnShow = (ImageButton) convertView.findViewById(R.id.img_icon);
                final View imgBtnShowView = convertView.findViewById(R.id.img_icon);
                imgBtnShow.setTag(position);
                imgBtnShow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Integer taggedPosition = (Integer) v.getTag();
                        String stuffName = ((Stuff)getItem(taggedPosition)).getName();
                        String stuffDescription = ((Stuff)getItem(taggedPosition)).getDescription();
                        Intent intent = new Intent(context, ViewStuffActivity.class);
                        intent.putExtra("STUFF_NAME", stuffName);
                        intent.putExtra("STUFF_DESCRIPTION", stuffDescription);
                        context.startActivity(intent);
                         //   ((StuffListActivity)context).zoomImageFromThumb(imgBtnShowView, stuffSelected.getImagePath());
                    }
                });

                ImageButton imgBtnShare = (ImageButton) convertView.findViewById(R.id.btn_share);
                imgBtnShare.setTag(position);
                imgBtnShare.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Answers.getInstance().logContentView(new ContentViewEvent()
                          //      .putContentName("Sharing Pinpoint"));
                        //Answers.getInstance().logCustom(new CustomEvent("Sharing Pinpoint"));
                        Integer taggedPosition = (Integer) v.getTag();
                        Stuff stuffSelected = (Stuff)getItem(taggedPosition);
                        String stuffCategory = ((StuffListActivity)context).getSalc().getApplicationModel().getCategoryNameFromStuffName(context, stuffSelected.getName());
                        String uri = "http://maps.google.com/maps?daddr=" +stuffSelected.getLatitude().toString()+","+stuffSelected.getLongitude().toString();
                        File file = null;
                        if( stuffSelected.getImagePath() != null)
                            file = new File(stuffSelected.getImagePath());


                        Resources resources = context.getResources();
                        Intent emailIntent = new Intent(Intent.ACTION_SEND);
                        // Native email client doesn't currently support HTML, but it doesn't hurt to try in case they fix it
                        emailIntent.putExtra(Intent.EXTRA_TEXT, uri);
                        //emailIntent.putExtra(Intent.EXTRA_SUBJECT, stuffSelected.getName());
                        emailIntent.setType("*/*");

                        Intent openInChooser = Intent.createChooser(emailIntent, "Partager avec");
                        PackageManager pm = context.getPackageManager();
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
                                emailIntent.putExtra(Intent.EXTRA_SUBJECT, stuffSelected.getName());
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
                                                    ClipData.newUri(context.getContentResolver(), "A photo", Uri.parse(file.toString()));

                                            intent.setClipData(clip);
                                            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                                        }
                                        else {
                                            List<ResolveInfo> resInfoList=
                                                    context.getPackageManager()
                                                            .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);

                                            for (ResolveInfo resolveInfo : resInfoList) {
                                                context.grantUriPermission(packageName, Uri.parse(file.toString()),
                                                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                                            }
                                        }
                                    }
                                    intent.putExtra(Intent.EXTRA_TEXT, "Nom : " + stuffSelected.getName() + "\n\n" + "Categorie : " + stuffCategory + "\n\n" + "Informations complémentaires : " + "\n" + stuffSelected.getDescription() + "\n\n" + uri);
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
                                    intent.putExtra(Intent.EXTRA_TEXT, "Nom : " + stuffSelected.getName() + "\n\n" + "Categorie : " + stuffCategory + "\n\n" + "Informations complémentaires : " + "\n" + stuffSelected.getDescription() + "\n\n" + uri);
                                    //if (file != null)
                                    //    intent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + file.toString()));
                                    //intent.putExtra(Intent.EXTRA_STREAM,
                                    //       Uri.parse( Environment.getExternalStorageDirectory()+ File.separator+"temporary_file.jpg"));
                                    intent.setType("text/plain");
                                } else if(packageName.contains("android.gm")) { // If Gmail shows up twice, try removing this else-if clause and the reference to "android.gm" above
                                    intent.putExtra(Intent.EXTRA_SUBJECT, "Objet provenant de PinPointPlace");
                                    if (file != null)
                                        intent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + file.toString()));
                                    intent.putExtra(Intent.EXTRA_TEXT, "Nom : " + stuffSelected.getName() + "\n\n" + "Categorie : " + stuffCategory + "\n\n" + "Informations complémentaires : " + "\n" + stuffSelected.getDescription() + "\n\n" + uri);
                                    intent.setType("message/rfc822");
                                }
                                intent.setPackage(packageName);
                                intentList.add(new LabeledIntent(intent, packageName, ri.loadLabel(pm), ri.icon));
                            } else if (packageName.contains("com.twitter.android")) {
                                Intent intent = new Intent();
                                intent.setComponent(new ComponentName(packageName, ri.activityInfo.name));
                                intent.setAction(Intent.ACTION_SEND);
                                intent.setType("*/*");//Set MIME Type
                                intent.putExtra(Intent.EXTRA_SUBJECT, stuffCategory + " partagé via PinPointPlace");
                                intent.putExtra(Intent.EXTRA_TEXT, stuffCategory + " " + stuffSelected.getName() + " partagé via PinPointPlace" + "\n\n" + "Informations complémentaires : " + "\n" + stuffSelected.getDescription() + "\n\nCoordonnées :\n\n" + uri + "\n\n\n");
                                if (file != null) {
                                    intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(file.toString()));// Pur Image to intent
                                    if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP) {
                                        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                                    }
                                    else if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.JELLY_BEAN) {
                                        ClipData clip=
                                                ClipData.newUri(context.getContentResolver(), "A photo", Uri.parse(file.toString()));

                                        intent.setClipData(clip);
                                        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                                    }
                                    else {
                                        List<ResolveInfo> resInfoList=
                                                context.getPackageManager()
                                                        .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);

                                        for (ResolveInfo resolveInfo : resInfoList) {
                                            context.grantUriPermission(packageName, Uri.parse(file.toString()),
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
                                Intent intentFacebook = new Intent(context, FacebookActivity.class);
                                intentFacebook.putExtra("STUFF_CATEGORY", stuffCategory);
                                intentFacebook.putExtra("STUFF_NAME", stuffSelected.getName());
                                intentFacebook.putExtra("STUFF_DESCRIPTION", stuffSelected.getDescription());
                                intentFacebook.putExtra("STUFF_IMG_FILE_URI", file.toString());
                                if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP) {
                                    intentFacebook.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                                }
                                else if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.JELLY_BEAN) {
                                    ClipData clip=
                                            ClipData.newUri(context.getContentResolver(), "A photo", Uri.parse(file.toString()));

                                    intentFacebook.setClipData(clip);
                                    intentFacebook.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                                }
                                else {
                                    List<ResolveInfo> resInfoList=
                                            context.getPackageManager()
                                                    .queryIntentActivities(intentFacebook, PackageManager.MATCH_DEFAULT_ONLY);

                                    for (ResolveInfo resolveInfo : resInfoList) {
                                        context.grantUriPermission(packageName, Uri.parse(file.toString()),
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
                        context.startActivity(openInChooser);
                    }
                });

                ImageButton imgBtnLocate = (ImageButton) convertView.findViewById(R.id.btn_locate_stuff);
                imgBtnLocate.setTag(position);
                imgBtnLocate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Answers.getInstance().logContentView(new ContentViewEvent()
                         //       .putContentName("Locating Pinpoint"));

                        //Answers.getInstance().logCustom(new CustomEvent("Locating Pinpoint"));

                        // Log.w("GJT", "Locating object");
                        Integer taggedPosition = (Integer) v.getTag();
                        Stuff stuffSelected = (Stuff)getItem(taggedPosition);
                        String stuffCategory = ((StuffListActivity)context).getSalc().getApplicationModel().getCategoryNameFromStuffName(context, stuffSelected.getName());
                        //Uri gmmIntentUri = Uri.parse("google.navigation:q="+stuffSelected.getLatitude().toString()+","+stuffSelected.getLongitude().toString() + "&mode=w");
                        Uri gmmIntentUri = Uri.parse("geo:" + stuffSelected.getLatitude() + "," + stuffSelected.getLongitude() + "?q=" + stuffSelected.getLatitude() + "," + stuffSelected.getLongitude() + "?z=13(" + stuffCategory + " " + stuffSelected.getName() + ")");
                        Uri genericUri = Uri.parse("geo:" + stuffSelected.getLatitude() + "," + stuffSelected.getLongitude() + "?q=" + stuffSelected.getLatitude() + "," + stuffSelected.getLongitude());
                        Uri wazeIntentUri = Uri.parse("waze://?ll=" + stuffSelected.getLatitude() + "," + stuffSelected.getLongitude());
                        Uri cityMapperIntentUri = Uri.parse("citymapper://directions?endcoord=" + stuffSelected.getLatitude() + "%2C" + stuffSelected.getLongitude());

                        Intent wazeIntent = new Intent(Intent.ACTION_VIEW, wazeIntentUri);
                        Intent googleMapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                        Intent cityMapperIntent = new Intent(Intent.ACTION_VIEW, cityMapperIntentUri);
                        Intent genericIntent = new Intent(Intent.ACTION_VIEW, genericUri);
                        PackageManager pm = context.getPackageManager();
                        List<LabeledIntent> intentList = new ArrayList<LabeledIntent>();
                        Intent openInChooser = Intent.createChooser(genericIntent, "Localiser avec");

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
                        List<ResolveInfo> resInfo = null;
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
                        context.startActivity(openInChooser);

                    }
                });

                ImageButton imgBtnUpdate = (ImageButton) convertView.findViewById(R.id.btn_update);
                imgBtnUpdate.setTag(position);
                imgBtnUpdate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Integer taggedPosition = (Integer) v.getTag();
                        String stuffName = ((Stuff)getItem(taggedPosition)).getName();
                        String stuffDescription = ((Stuff)getItem(taggedPosition)).getDescription();
                        Intent intent = new Intent(context, UpdateStuffActivity.class);
                        intent.putExtra("STUFF_NAME", stuffName);
                        intent.putExtra("STUFF_DESCRIPTION", stuffDescription);
                        context.startActivity(intent);
                    }
                });

                ImageButton imgBtnDelete = (ImageButton) convertView.findViewById(R.id.btn_delete);

                // click listener for remove button
                imgBtnDelete.setTag(position);
                imgBtnDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Integer taggedPosition = (Integer) v.getTag();
                        final String stuffName = ((Stuff)getItem(taggedPosition)).getName();
                        final String stuffImagePath = ((Stuff)getItem(taggedPosition)).getImagePath();
                        // 1. Instantiate an AlertDialog.Builder with its constructor
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);

                        // 2. Chain together various setter methods to set the dialog characteristics
                        builder.setMessage("Etes-vous sûr(e) de vouloir supprimer le pinpoint " + stuffName + " de la liste ?")
                                .setTitle("PinPointPlace");

                        // Add the buttons
                        builder.setPositiveButton("Confirmer", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // Answers.getInstance().logContentView(new ContentViewEvent()
                                //        .putContentName("Deleting Pinpoint"));
                                //Answers.getInstance().logCustom(new CustomEvent("Deleting Pinpoint"));
                                applicationModel.deleteStuffFromName(context, stuffName, stuffImagePath);
                                ((StuffListActivity)context).onResume();
                                Toast.makeText((StuffListActivity)context, "Le pinpoint a été bien été supprimé", Toast.LENGTH_SHORT).show();
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
                    }
                });
                break;
            case TYPE_DIVIDER:
                TextView title = (TextView)convertView.findViewById(R.id.header_title);
                CategoryViewHolder cviewHolder = (CategoryViewHolder) convertView.getTag();
                if(cviewHolder == null){
                    cviewHolder = new CategoryViewHolder();
                    cviewHolder.title = (TextView) convertView.findViewById(R.id.header_title);
                    convertView.setTag(cviewHolder);
                }

                //getItem(position) va récupérer l'item [position] de la List<Tweet> tweets
                object = getItem(position);
                Category category = (Category)object;
                //il ne reste plus qu'à remplir notre vue
                cviewHolder.title.setText(category.getName());
                title.setText(category.getName());
                break;
        }



        return convertView;
    }

    @Override
    public int getItemViewType(int position) {
        if (getItem(position) instanceof Stuff) {
            return TYPE_STUFF;
        }

        return TYPE_DIVIDER;
    }

    @Override
    public int getCount() {
        return objects.size();
    }

    @Override
    public int getViewTypeCount() {
        // TYPE_STUFF and TYPE_DIVIDER
        return 2;
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

    private class StuffViewHolder {
        public TextView name;
        public ImageButton image;
        public TextView date;
    }

    private class CategoryViewHolder {
        public TextView title;
    }

    private boolean isPackageInstalled(String packagename, PackageManager packageManager) {
        try {
            packageManager.getPackageInfo(packagename, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
}
