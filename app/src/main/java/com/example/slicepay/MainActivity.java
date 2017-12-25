package com.example.slicepay;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int STORAGE_PERMISSIONS_REQUEST = 102;
    private static Button openCustomGallery;
    private static GridView selectedImageGridView;

    private static final int CustomGallerySelectId = 1;//Set Intent Id
    public static final String CustomGalleryIntentKey = "ImageArray";//Set Intent Key Value
    private String imagesArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        setListeners();
        getSharedImages();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) { //FOR MAINTAINING ORIENTATION
        Log.e("onSaveInstanceState:  ", "Calling");
        super.onSaveInstanceState(outState);

        if (!TextUtils.isEmpty(imagesArray))
            outState.putString("imageArray", imagesArray);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {   //FOR MAINTAINING ORIENTATION
        super.onRestoreInstanceState(savedInstanceState);
        imagesArray = savedInstanceState.getString("imageArray");

        if (!TextUtils.isEmpty(imagesArray))
            showImages(savedInstanceState.getString("imageArray"));
    }

    //Init all views
    private void initViews() {
        openCustomGallery = (Button) findViewById(R.id.openCustomGallery);
        selectedImageGridView = (GridView) findViewById(R.id.selectedImagesGridView);
    }

    //set Listeners
    private void setListeners() {
        openCustomGallery.setOnClickListener(this);
    }


    //READ STORAGE PERMISSIONS
    @TargetApi(23)
    private void getPermissionStorage() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    STORAGE_PERMISSIONS_REQUEST);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.openCustomGallery:
                //PERMISSIONS CHECKER
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    getPermissionStorage();
                } else {
                    startActivityForResult(new Intent(MainActivity.this, CustomGalleryActivity.class), CustomGallerySelectId);
                }
                break;
        }

    }

    //PERMISSION CALLBACK
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSIONS_REQUEST) {
            for (int i = 0; i < permissions.length; i++) {
                String permission = permissions[i];
                int grantResult = grantResults[i];

                if (permission.equals(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    if (grantResult == PackageManager.PERMISSION_GRANTED) {
                        Log.i("Permission Granted", "");
                        startActivityForResult(new Intent(MainActivity.this, CustomGalleryActivity.class), CustomGallerySelectId);
                    }
                } else {
                    Log.e("Permission fail", "");
                }
            }

        }
    }

    protected void onActivityResult(int requestcode, int resultcode,
                                    Intent imagereturnintent) {
        super.onActivityResult(requestcode, resultcode, imagereturnintent);
        switch (requestcode) {
            case CustomGallerySelectId:
                if (resultcode == RESULT_OK) {
                    imagesArray = imagereturnintent.getStringExtra(CustomGalleryIntentKey);//get Intent data
                    showImages(imagesArray);     //FOR SHOWING IMAGES
                }
                break;

        }
    }

    private void showImages(String imagesArray) {
        List<String> selectedImages = Arrays.asList(imagesArray.substring(1, imagesArray.length() - 1).split(", "));
        loadGridView(new ArrayList<String>(selectedImages));//call load gridview method by passing converted list into arrayList
    }


    //Load GridView
    private void loadGridView(ArrayList<String> imagesArray) {
        GridViewAdapter adapter = new GridViewAdapter(MainActivity.this, imagesArray, false);
        selectedImageGridView.setAdapter(adapter);
    }

    //Read Shared Images
    private void getSharedImages() {
        if (Intent.ACTION_SEND_MULTIPLE.equals(getIntent().getAction())
                && getIntent().hasExtra(Intent.EXTRA_STREAM)) {
            ArrayList<Parcelable> list =
                    getIntent().getParcelableArrayListExtra(Intent.EXTRA_STREAM);
            ArrayList<String> selectedImages = new ArrayList<>();

            //Loop to all parcelable list
            for (Parcelable parcel : list) {
                Uri uri = (Uri) parcel;
                String sourcepath = getPath(uri);
                selectedImages.add(sourcepath);
            }
            loadGridView(selectedImages);//LOAD GRID-VIEW
        }
    }


    //get actual path
    public String getPath(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        startManagingCursor(cursor);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

}
