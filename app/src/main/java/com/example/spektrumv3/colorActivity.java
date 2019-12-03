package com.example.spektrumv3;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;


import androidx.palette.graphics.Palette;


public class colorActivity extends AppCompatActivity {

    String TAG = "ColorActivity";

    private TextView color1TextView;
    private TextView color2TextView;
    private TextView color3TextView;
    private TextView color4TextView;
    private TextView color5TextView;
    private ImageView selectedImage;

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

        selectedImage = findViewById(R.id.analyzeImageView);
        selectedImage.setImageURI(myURI);
        Log.d(TAG, "ImageURI: " + myURI);
        Log.d(TAG, "ImageURI: " + myURI.toString());

        initViews();
        paintTextBackground();
    }

    private void initViews() {
        color1TextView = (TextView) findViewById(R.id.color1TextView);
        color2TextView = (TextView) findViewById(R.id.color2TextView);
        color3TextView= (TextView) findViewById(R.id.color3TextView);
        color4TextView= (TextView) findViewById(R.id.color4textView);
        color5TextView = (TextView) findViewById(R.id.color5textView);
    }



    private void paintTextBackground() {

        BitmapDrawable convertedImage =  (BitmapDrawable) selectedImage.getDrawable();
        Bitmap bitmapImage = convertedImage.getBitmap();

        Bitmap bitmap = bitmapImage;

        Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
            @Override
            public void onGenerated(Palette palette) {
                //work with the palette here

                int defaultValue = 0x000000;
//                swatchList = palette.getSwatches();    //gets 16 colors




//                Object popularColor = getMostPopulousSwatch(palette);
//
//                Object color = swatchList.get(0);
//                Object color1 = swatchList.get(1);
//                Object color2 = swatchList.get(2);
//
//
//                Log.d(TAG, "popularSwatch: " + popularColor);
//                Log.d(TAG, "swatch1: " + color);
//                Log.d(TAG, "swatch2: " + color1);
//                Log.d(TAG, "swatch3: " + color2);

                Palette.Swatch mostPopularColor = getMostPopulousSwatch(palette);
                Palette.Swatch dominantColor = palette.getDominantSwatch();



                int dominant = palette.getDominantColor(defaultValue);
                Log.d("TEST", "" + dominant);
                int vibrant = palette.getVibrantColor(defaultValue);
                int vibrantLight = palette.getVibrantColor(defaultValue);
                int vibrantDark = palette.getDarkVibrantColor(defaultValue);
                int muted = palette.getMutedColor(defaultValue);
                int mutedDark = palette.getDarkMutedColor(defaultValue);





                color1TextView.setBackgroundColor(dominant);
                color2TextView.setBackgroundColor(vibrant);
                color3TextView.setBackgroundColor(vibrantDark);
                color4TextView.setBackgroundColor(muted);
                color5TextView.setBackgroundColor(mutedDark);
//
            }
        });




    }

    public static Palette.Swatch getMostPopulousSwatch(Palette palette) {
        Palette.Swatch mostPopulous = null;
        if (palette != null) {
            for (Palette.Swatch swatch : palette.getSwatches()) {
                if (mostPopulous == null || swatch.getPopulation() > mostPopulous.getPopulation()) {
                    mostPopulous = swatch;
                }
            }
        }
        return mostPopulous;
    }

    public static Palette.Swatch getDominantSwatch(Palette palette) {
        // find most-represented swatch based on population
        return Collections.max(palette.getSwatches(), new Comparator<Palette.Swatch>() {
            @Override
            public int compare(Palette.Swatch sw1, Palette.Swatch sw2) {
                return Integer.compare(sw1.getPopulation(), sw2.getPopulation());
            }
        });
    }
}

