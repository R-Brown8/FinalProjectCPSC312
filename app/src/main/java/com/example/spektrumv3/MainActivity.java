package com.example.spektrumv3;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.util.ArrayList;

// database image deletion
// color analyze cleanup
        //
// image resizing ( crashes if bitmap too large )
// once cleaned up potentially add Flickr API functionality of random "interesting" image
//      or Google API fragment in place of color grid (fragment)

public class MainActivity extends AppCompatActivity {

    SqlImageDatabase sqlDb = null;

    ImageButton uploadButton;
    Button listButton;
    ImageView selectedImageView;
    Button analyzeButton;
    Button clearButton;
    RecyclerView imageRecycler;
    ImageDisplayFragment displayFragment  = (ImageDisplayFragment) getSupportFragmentManager().findFragmentById(R.id.imageDisplayFragment);
    ImageListViewFragment listViewFragment  = (ImageListViewFragment) getSupportFragmentManager().findFragmentById(R.id.listViewFragment);

    Uri selectedImage;

    static final String TAG = "PicturePath";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        uploadButtonListener();
        analyzeButtonListener();
        listButtonListener();
        clearButtonListener();
        sqlDb = new SqlImageDatabase(this);
       selectedImageView = (ImageView) findViewById(R.id.analyzeImageView);
       imageRecycler = (RecyclerView) findViewById(R.id.recyclerListView);

        displayFragment  = (ImageDisplayFragment) getSupportFragmentManager().findFragmentById(R.id.imageDisplayFragment);
        listViewFragment  = (ImageListViewFragment) getSupportFragmentManager().findFragmentById(R.id.listViewFragment);

       imageRecycler.setHasFixedSize(true);
       LinearLayoutManager layoutManager = new LinearLayoutManager(this);
       layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
       imageRecycler.setLayoutManager(layoutManager);
       updateRecycleView();

       toggleFragments(false);


    }

    public void clearButtonListener() {
        clearButton = (Button) findViewById(R.id.clearButton);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sqlDb.deleteAll();
                updateRecycleView();
            }
        });
    }

    public void uploadButtonListener() {
        uploadButton = (ImageButton) findViewById(R.id.uploadButton);
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //code for selecting image
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT,
                        MediaStore.Images.Media.INTERNAL_CONTENT_URI);

                Log.d(TAG, "uploadbuttonListener: ");

                startActivityForResult(intent, RESULT_LOAD_IMAGE);
            }
        });
    }


    public void listButtonListener() {
        listButton = (Button) findViewById(R.id.listButton);
        listButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d(TAG, "listButtonListener: ");

                toggleFragments(true);
            }
        });
    }


    public static final int RESULT_LOAD_IMAGE = 1;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        Log.d(TAG, "onActivityResult: before if statement");
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && data != null) {
            selectedImage = data.getData();
            Uri uri = (Uri) data.getData();
            Log.d(TAG, "newURI: " + selectedImage);

            Bitmap bitmap = uriToBitmap(data.getData());

            selectedImageView.setImageBitmap(bitmap);

            sqlDb.insertImage( uri.toString() );


            updateRecycleView();
            toggleFragments(false);
        }
    }

    private Bitmap uriToBitmap(Uri uri) {
        Bitmap returnValue = null;
        try{
            returnValue = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
//            returnValue.setWidth(256);
//            returnValue.setHeight(256);
        } catch (Exception e) { }
        return returnValue;
    }

    public void analyzeButtonListener(){
        analyzeButton = (Button) findViewById(R.id.analyzeButton);
        analyzeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, colorActivity.class);
                intent.putExtra("selectedImageURI", selectedImage.toString());
                startActivity(intent);
            }
        });
    }

    public void updateRecycleView() {
        ArrayList<String> list = new ArrayList<>();
        Cursor c = sqlDb.getImageList(); //returns a cursor
        if (c.moveToFirst()) {
            do {
                String data = c.getString(c.getColumnIndex("uri"));
                list.add(data);
                Log.d(TAG, "data received: " + data);
            } while (c.moveToNext());
        }
        c.close(); //always close cursor
        ImageAdapter myAdapter = new ImageAdapter(this, list);
        imageRecycler.setAdapter(myAdapter);
    }

    public void setSelectedImage(Uri uri) {
        selectedImage = uri;

    }

    public void toggleFragments(boolean showList){
        if (showList) {
            FragmentManager fm = getSupportFragmentManager();
            fm.beginTransaction().setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                    .hide(displayFragment).commit();

            fm.beginTransaction().setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                    .show(listViewFragment).commit();

        } else {

            FragmentManager fm = getSupportFragmentManager();
            fm.beginTransaction().setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                    .show(displayFragment).commit();

            fm.beginTransaction().setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                    .hide(listViewFragment).commit();
        }
    }
}
