package com.example.spektrumv3;

import android.app.Activity;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.MyViewHolder> {

    private List<String> nameList;
    private Activity context;
    private int READ_EXTERNAL_STORAGE_REQUEST_CODE = 1001;

    public ImageAdapter(Activity context, List<String> list) {
        this.nameList = list;
        this.context = context;
        Log.d("PicturePath", "ImageAdapter Constructor: " + list.size());
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.item, parent, false);

        MyViewHolder myViewHolder = new MyViewHolder(view, "");
        Log.d("PicturePath", "onCreateViewHolder ran");
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, final int position) {

        Log.d("ImageAdapter","onBindViewHolder");
        final String uri = nameList.get(position);
        Uri myUri = Uri.parse(uri);

        try {
            final Bitmap bm = MediaStore.Images.Media.getBitmap(this.context.getContentResolver(), myUri);
            holder.imageView.setImageBitmap(bm);


            holder.itemView.setOnClickListener( new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ImageView iv = context.findViewById(R.id.analyzeImageView);
                    iv.setImageBitmap(bm);
                    ((MainActivity) context).toggleFragments(false);
                    //on image click in list, set selectedImageURI passed by intent to be updated
                }
            });
        } catch (Exception e) {
            Log.d("PicturePath", e.getMessage());
        }


    }

    @Override
    public int getItemCount() {
        Log.d("PicturePath", "listSize: " + nameList.size());
        if ( nameList == null) {
            return 0;
        } else {
            return nameList.size();
        }
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;
        public String uri;

        public MyViewHolder(View itemView, String uri) {
            super(itemView);
            this.uri = uri;

            imageView = itemView.findViewById(R.id.imageListView);
        }
    }
}
