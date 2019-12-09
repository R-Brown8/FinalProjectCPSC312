package com.example.spektrumv3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;

import org.w3c.dom.Text;

import java.util.List;

public class colorActivity extends AppCompatActivity {
    String TAG = "ColorActivity";

    private ImageView selectedImage;
    //private Uri selectedImageURI;
    ColorGridFragment colorGridFragment;
    VisionAPIFragment visionAPIFragment;
    Spinner spinner;

    Uri myURI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color);

        //we want a back button on the action bar
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        String uriString = intent.getStringExtra("selectedImageURI");

        myURI = Uri.parse(uriString);
        selectedImage = findViewById(R.id.analyzeImageView);
        selectedImage.setImageURI(myURI);

        initViews();

        //we want to use a spinner and add the options to it
        String[] spinnerOptions = {"Colors", "Labels", "Landmarks", "Faces", "Text", "Hot Dog?"};
        spinner = findViewById(R.id.spinnerOptions);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                spinnerOptions);

        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position,  final long id) {
                FragmentManager fm = getSupportFragmentManager();
                //hide the colors fragment
                fm.beginTransaction().setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                        .hide(colorGridFragment).commit();

                //show the visionAPIFragment
                fm.beginTransaction().setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                        .show(visionAPIFragment).commit();

                //once the text view is visible, we want to update the text with what the user wants
                visionAPIFragment.toggleProgressBar(true);
                Thread thread = new Thread(new Runnable() {
                    public void run() {
                        if(id == 0) {
                            FragmentManager frag = getSupportFragmentManager();
                            frag.beginTransaction().setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                                    .show(colorGridFragment).commit();

                            frag.beginTransaction().setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                                    .hide(visionAPIFragment).commit();
                        }
                        else if(id == 1)
                            visionAPIFragment.analyzeImageForLabels(uriToBitmap(myURI));
                        else if(id == 2)
                            visionAPIFragment.analyzeImageForLandmark(uriToBitmap(myURI));
                        else if(id == 3)
                            visionAPIFragment.analyzeImageForFaces(uriToBitmap(myURI));
                        else if(id == 4)
                            visionAPIFragment.analyzeImageForText(uriToBitmap(myURI));
                        else if(id == 5)
                            visionAPIFragment.analyzeImageForHotDog(uriToBitmap(myURI));
                    }
                });
                thread.start();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                FragmentManager fm = getSupportFragmentManager();
                fm.beginTransaction().setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                        .show(colorGridFragment).commit();

                fm.beginTransaction().setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                        .hide(visionAPIFragment).commit();
            }
        });

        Thread thread = new Thread(new Runnable() {
            public void run() {
                colorGridFragment.paintTextBackground(uriToBitmap(myURI));
            }
        });
        thread.start();
    }//end onCreate

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                this.finish();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void initViews() {
        colorGridFragment  = (ColorGridFragment) getSupportFragmentManager().findFragmentById(R.id.colorGridFragment);
        visionAPIFragment  = (VisionAPIFragment) getSupportFragmentManager().findFragmentById(R.id.visionApiFragment);
    }

    private Bitmap uriToBitmap(Uri uri) {
        Bitmap returnValue = null;
        try{
            returnValue = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
        } catch (Exception e) { }
        return returnValue;
    }

}

