package com.example.spektrumv3;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;

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
        selectedImageView = findViewById(R.id.analyzeImageView);
        imageRecycler = findViewById(R.id.recyclerListView);

        displayFragment  = (ImageDisplayFragment) getSupportFragmentManager().findFragmentById(R.id.imageDisplayFragment);
        listViewFragment  = (ImageListViewFragment) getSupportFragmentManager().findFragmentById(R.id.listViewFragment);

       imageRecycler.setHasFixedSize(true);
       LinearLayoutManager layoutManager = new LinearLayoutManager(this);
       layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
       imageRecycler.setLayoutManager(layoutManager);
       updateRecycleView();

       toggleFragments(false);
       analyzeButton.setEnabled(false);


    }

    public void clearButtonListener() {
        clearButton = findViewById(R.id.clearButton);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
// Add the buttons
                builder.setTitle("Are you sure you want to clear the saved images?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked Yes button
                        sqlDb.deleteAll();
                        updateRecycleView();
                        Toast toast = Toast.makeText(MainActivity.this, "Cleared Saved Images", Toast.LENGTH_LONG );
                        toast.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL, 0, 0);
                        toast.show();

                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });

// Create the AlertDialog
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
    }

    public void uploadButtonListener() {
        uploadButton = findViewById(R.id.uploadButton);
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
        listButton = findViewById(R.id.listButton);
        listButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d(TAG, "listButtonListener: ");

                Toast toast = Toast.makeText(MainActivity.this, "Showing saved images list", Toast.LENGTH_LONG );
                toast.setGravity(Gravity.CENTER|Gravity.BOTTOM, 0, 325);
                toast.show();

                analyzeButton.setEnabled(false);

                toggleFragments(true);
            }
        });
    }


    public static final int RESULT_LOAD_IMAGE = 1;

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        Log.d(TAG, "onActivityResult: before if statement");
        Thread thread = new Thread(new Runnable() {
            public void run() {
                if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && data != null) {
                    selectedImage = data.getData();
                    Uri uri = data.getData();
                    Log.d(TAG, "newURI: " + selectedImage);

                    final Bitmap bitmap = uriToBitmap(data.getData());


                    sqlDb.insertImage(uri.toString());

                    runOnUiThread(new Runnable() {
                        public void run() {
                            selectedImageView.setImageBitmap(bitmap);

                            updateRecycleView();

                            toggleFragments(false);
                        }
                    });
                }
            }
        });
        thread.start();
    }

    private Bitmap uriToBitmap(Uri uri) {
        Bitmap returnValue = null;
        try{
            returnValue = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
        } catch (Exception e) { }
        return returnValue;
    }

    public void analyzeButtonListener(){
        analyzeButton = findViewById(R.id.analyzeButton);
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
        if(c.getCount() == 0){
            listViewFragment.toggleListView(false);
        } else {
            listViewFragment.toggleListView(true);
        }

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

            analyzeButton.setEnabled(false);

        } else {

            FragmentManager fm = getSupportFragmentManager();
            fm.beginTransaction().setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                    .show(displayFragment).commit();

            fm.beginTransaction().setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                    .hide(listViewFragment).commit();

            analyzeButton.setEnabled(true);
        }
    }
}
