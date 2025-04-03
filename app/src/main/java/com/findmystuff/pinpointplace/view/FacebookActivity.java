package com.findmystuff.pinpointplace.view;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookSdk;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.ppp.pinpointplace.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FacebookActivity extends FragmentActivity implements View.OnClickListener {

    CallbackManager callbackManager;
    ShareDialog shareDialog;
    private ShareLinkContent content;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras != null) {
                // Log.w("GJT", "Facebook Activity Pass 1");
                String stuffCategory = extras.getString("STUFF_CATEGORY");
                String stuffName = extras.getString("STUFF_NAME");
                String stuffDescription = extras.getString("STUFF_DESCRIPTION");
                String stuffImgUri = extras.getString("STUFF_IMG_FILE_URI");
                String locationUri = extras.getString("LOCATION_URI");
                /**
                final int THUMBSIZE = 120;
                Bitmap thumbImage = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(stuffImgUri), THUMBSIZE, THUMBSIZE);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                thumbImage.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] byteArray = stream.toByteArray();

                File file = new File("facebook_thumbnail");
                File image = null;
                try {
                    if (file.exists()) {
                        file.delete();
                        Log.w("GJT", "File deleted !!!");
                    }

                    if (!file.exists()) {
                        // Create an image file name
                        String imageFileName = "facebook_thumbnail";
                        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                        image = File.createTempFile(
                                imageFileName,  /* prefix
                                ".jpg",         /* suffix
                                storageDir      /* directory
                        );
                        FileOutputStream fos = new FileOutputStream(image);
                        fos.write(byteArray);
                    }
                } catch (IOException e){
                    Log.w("GJT", "Error creating file");
                }
                */
                content = new ShareLinkContent.Builder()
                        .setContentTitle(stuffCategory + " " + stuffName)
                        //.setImageUrl(Uri.parse(stuffImgUri))
                        .setContentUrl(Uri.parse(locationUri))
                        .setContentDescription("Partagé via PinPointPlace")
                        .build();
                FacebookSdk.sdkInitialize(getApplicationContext());
                callbackManager = CallbackManager.Factory.create();
                shareDialog = new ShareDialog(this);
                // this part is optional
                //shareDialog.registerCallback(callbackManager, new FacebookCallback<Sharer.Result>() { ... });
                if (ShareDialog.canShow(ShareLinkContent.class)) {
                    // Log.w("GJT", "Facebook can be showed");
                    ShareLinkContent linkContent = new ShareLinkContent.Builder()
                            .setContentTitle(content.getContentTitle())
                            .setContentDescription(content.getContentDescription())
                            .setContentUrl(content.getContentUrl())
                            .setImageUrl(content.getImageUrl())
                            .build();
                    // Log.w("GJT", "Facebook can be showed 2");
                    // Log.w("GJT", "Content title : " + linkContent.getContentTitle());
                    // Log.w("GJT", "Content description : " + linkContent.getContentDescription());
                    // Log.w("GJT", "Content url : " + linkContent.getContentUrl().toString());
                    //Log.w("GJT", "Content img url : " + linkContent.getImageUrl().toString());
                    shareDialog.show(linkContent);
                    // Log.w("GJT", "Facebook can be showed 3");
                    this.finish();
                }
            }
        }
    }

    public void setContent(ShareLinkContent content) {
        this.content = content;
    }

    @Override
    public void onClick(final View v) {
        Log.w("GJT", "Facebook Activity Pass 2");

    }
}
