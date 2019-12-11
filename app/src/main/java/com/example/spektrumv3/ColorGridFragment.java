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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ColorGridFragment extends Fragment {
    static final String TAG = "ColorGridFragment";

    private TextView color1GridFragment;
    private TextView color2GridFragment;
    private TextView color3GridFragment;
    private TextView color4GridFragment;
    private TextView color5GridFragment;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.color_grid, container, false);
        color1GridFragment = v.findViewById(R.id.fragColorTextView1);
        color2GridFragment = v.findViewById(R.id.fragColorTextView2);
        color3GridFragment = v.findViewById(R.id.fragColorTextView3);
        color4GridFragment = v.findViewById(R.id.fragColorTextView4);
        color5GridFragment = v.findViewById(R.id.fragColorTextView5);
        return v;
    }

    public void paintTextBackground(Bitmap bitmap) {
        Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
            @Override
            public void onGenerated(Palette palette) {
                //work with the palette here

                int defaultValue = 0x000000;

                Palette.Swatch mostPopularColor = getMostPopulousSwatch(palette);
                Palette.Swatch dominantColor = palette.getDominantSwatch();

                int dominant = palette.getDominantColor(defaultValue);
                int vibrant = palette.getVibrantColor(defaultValue);
                int vibrantLight = palette.getVibrantColor(defaultValue);
                int vibrantDark = palette.getDarkVibrantColor(defaultValue);
                int muted = palette.getMutedColor(defaultValue);
                int mutedDark = palette.getDarkMutedColor(defaultValue);

                List<Integer> colorSet = new ArrayList<>();
                List<Integer> allColors = new ArrayList<>();
                allColors.add(dominant);
                allColors.add(vibrant);
                allColors.add(muted);
                allColors.add(vibrantLight);
                allColors.add(vibrantDark);
                allColors.add(mutedDark);

                color1GridFragment.setVisibility(View.VISIBLE);
                color2GridFragment.setVisibility(View.VISIBLE);
                color3GridFragment.setVisibility(View.VISIBLE);
                color4GridFragment.setVisibility(View.VISIBLE);
                color5GridFragment.setVisibility(View.VISIBLE);

                //creating a set of colors without duplicates and without white
                for(int color : allColors){
                    if(!colorSet.contains(color) && color != 0){
                        colorSet.add(color);
                    }
                }

                Log.d(TAG, "onGenerated: SET: " + colorSet);

                //if there are three colors that we can use, set grid colors, if not
                //just set the middle grid to the dominant color
                if(colorSet.size() == 5){
                    color1GridFragment.setBackgroundColor(colorSet.get(0));
                    color2GridFragment.setBackgroundColor(colorSet.get(1));
                    color3GridFragment.setBackgroundColor(colorSet.get(2));
                    color4GridFragment.setBackgroundColor(colorSet.get(3));
                    color5GridFragment.setBackgroundColor(colorSet.get(4));
                }else if(colorSet.size() == 4){
                    color1GridFragment.setBackgroundColor(colorSet.get(0));
                    color2GridFragment.setBackgroundColor(colorSet.get(1));
                    color3GridFragment.setVisibility(View.GONE);
                    color4GridFragment.setBackgroundColor(colorSet.get(2));
                    color5GridFragment.setBackgroundColor(colorSet.get(3));
                } else if(colorSet.size() == 3) {
                    color1GridFragment.setBackgroundColor(colorSet.get(0));
                    color2GridFragment.setVisibility(View.GONE);
                    color3GridFragment.setBackgroundColor(colorSet.get(1));
                    color4GridFragment.setVisibility(View.GONE);
                    color5GridFragment.setBackgroundColor(colorSet.get(2));
                }else if(colorSet.size() == 2){
                    color1GridFragment.setVisibility(View.GONE);
                    color2GridFragment.setBackgroundColor(colorSet.get(0));
                    color2GridFragment.setVisibility(View.GONE);
                    color4GridFragment.setBackgroundColor(colorSet.get(1));
                    color5GridFragment.setVisibility(View.GONE);
                }else {
                    color1GridFragment.setVisibility(View.GONE);
                    color2GridFragment.setVisibility(View.GONE);
                    color3GridFragment.setBackgroundColor(colorSet.get(0));
                    color4GridFragment.setVisibility(View.GONE);
                    color5GridFragment.setVisibility(View.GONE);
                }
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