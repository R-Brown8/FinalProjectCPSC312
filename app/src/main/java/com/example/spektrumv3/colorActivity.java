package com.example.spektrumv3;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Switch;

import java.util.List;



public class colorActivity extends AppCompatActivity {

    String TAG = "ColorActivity";


    private ImageView selectedImage;
    private Uri selectedImageURI;
    Boolean showInfo;
    Switch switchButton;
    ColorGridFragment colorGridFragment;
    VisionAPIFragment visionAPIFragment;

    Uri myURI;

    List swatchList = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color);


        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        String uriString = intent.getStringExtra("selectedImageURI");

        myURI = Uri.parse(uriString);
        selectedImageURI = Uri.parse(uriString);

        selectedImage = findViewById(R.id.analyzeImageView);
        selectedImage.setImageURI(myURI);
        Log.d(TAG, "ImageURI: " + myURI);
        Log.d(TAG, "ImageURI: " + myURI.toString());

        initViews();
        switchButtonListener();



        colorGridFragment.paintTextBackground(uriToBitmap(myURI));
    }

    private void initViews() {
        colorGridFragment  = (ColorGridFragment) getSupportFragmentManager().findFragmentById(R.id.colorGridFragment);
        visionAPIFragment  = (VisionAPIFragment) getSupportFragmentManager().findFragmentById(R.id.visionApiFragment);
        switchButton = (Switch) findViewById(R.id.switchButton);
    }

    public void switchButtonListener(){
        switchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if( switchButton.isChecked()) {
                    FragmentManager fm = getSupportFragmentManager();
                    fm.beginTransaction().setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                            .hide(colorGridFragment).commit();

                    fm.beginTransaction().setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                            .show(visionAPIFragment).commit();
                } else {
                    FragmentManager fm = getSupportFragmentManager();
                    fm.beginTransaction().setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                            .show(colorGridFragment).commit();

                    fm.beginTransaction().setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                            .hide(visionAPIFragment).commit();
                }
                Log.d(TAG, "switch Listener: " + switchButton.isChecked());

            }
        });
    }

    private Bitmap uriToBitmap(Uri uri) {
        Bitmap returnValue = null;
        try{
            returnValue = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
        } catch (Exception e) { }
        return returnValue;
    }

}

