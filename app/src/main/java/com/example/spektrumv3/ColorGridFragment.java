package com.example.spektrumv3;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.palette.graphics.Palette;

import java.util.Collections;
import java.util.Comparator;

public class ColorGridFragment extends Fragment {


    TextView color1GridFragment;
    TextView color2GridFragment;
    TextView color3GridFragment;
//    TextView color4GridFragment;
//    TextView color5GridFragment;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.color_grid, container, false);
        color1GridFragment = (TextView) v.findViewById(R.id.fragColorTextView1);
        color2GridFragment = (TextView) v.findViewById(R.id.fragColorTextView2);
        color3GridFragment = (TextView) v.findViewById(R.id.fragColorTextView3);
//        color4GridFragment = (TextView) v.findViewById(R.id.fragColorTextView4);
//        color5GridFragment = (TextView) v.findViewById(R.id.fragColorTextView5);

        Log.d("colorGridFrag", "post-init: " + color1GridFragment);
        return v;
    }


    public void paintTextBackground(Bitmap bitmap) {

        Log.d("colorGridFrag", "post-init1: " + color1GridFragment);

        Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
            @Override
            public void onGenerated(Palette palette) {
                //work with the palette here

                int defaultValue = 0x000000;

                Palette.Swatch mostPopularColor = getMostPopulousSwatch(palette);
                Palette.Swatch dominantColor = palette.getDominantSwatch();


                int dominant = palette.getDominantColor(defaultValue);
                Log.d("TEST", "" + dominant);
                int vibrant = palette.getVibrantColor(defaultValue);
                int vibrantLight = palette.getVibrantColor(defaultValue);
                int vibrantDark = palette.getDarkVibrantColor(defaultValue);
                int muted = palette.getMutedColor(defaultValue);
                int mutedDark = palette.getDarkMutedColor(defaultValue);

                if(color1GridFragment != null) {
                    color1GridFragment.setBackgroundColor(vibrant);
                    color2GridFragment.setBackgroundColor(dominant);
                    color3GridFragment.setBackgroundColor(muted);
//                    color4GridFragment.setBackgroundColor(muted);
//                    color5GridFragment.setBackgroundColor(mutedDark);
                }
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