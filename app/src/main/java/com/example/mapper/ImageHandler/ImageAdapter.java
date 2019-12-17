package com.example.mapper.ImageHandler;


import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.mapper.R;
import com.example.mapper.services.ImageFetchService;
import com.example.mapper.views.VisitImageView;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.View_Holder> {
    static private Context context;
    private static List<ImageObj> items;
    private Boolean sort=  false;
    private ArrayList<ImageObj> itemsList = new ArrayList<>();
    private static final int THUMBSIZE = Resources.getSystem().getDisplayMetrics().widthPixels;
    private int size = 0;
    CacheHandler cache = CacheHandler.getInstance();

    public ImageAdapter(List<ImageObj> items) {
        this.items = items;
    }

    public ImageAdapter(Context cont, List<ImageObj> items) {
        super();
        this.items = items;
        context = cont;
    }


    @Override
    public View_Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        //Inflate the layout, initialize the View Holder
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_image_item,
                parent, false);
        View_Holder holder = new View_Holder(v);
        context= parent.getContext();
        return holder;
    }

//    Need to fix
    @Override
    public void onBindViewHolder(final View_Holder holder, final int position) {

        //Use the provided View Holder on the onCreateViewHolder method to populate the
        // current row on the RecyclerView

        if (holder!=null && items.get(position)!=null) {

            if (items.get(position).file!=null){
                String path = items.get(position).file.getAbsolutePath();
                Bitmap bitmap = cache.getFromCache(items.get(position).file.getAbsolutePath());
                if(bitmap == null) {
                    bitmap = ImageFetchService.decodeSampledBitmapFromResource(items.get(position).file, 150, 150);
                    cache.addToCache(path, bitmap);
                }
                Bitmap thumbImage = ThumbnailUtils.extractThumbnail(bitmap ,
                        (THUMBSIZE/3 - 5), (THUMBSIZE/3 -5));

                holder.imageView.setImageBitmap(thumbImage);
            }

            itemsList.add(items.get(position));
            holder.itemView.setOnClickListener(new View.OnClickListener() {
            File image = itemsList.get(position).file;
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, VisitImageView.class);
                    intent.putExtra("image", image);
                    context.startActivity(intent);
                }
            });


        }
    }


    // convenience method for getting data at click position
    ImageObj getItem(int id) {
        return items.get(id);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class View_Holder extends RecyclerView.ViewHolder  {
        ImageView imageView;


        View_Holder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.visit_image);

        }

    }
//
//    public static List<ImageObj> getItems() {
//        return items;
//    }
//
//    public static void setItems(List<ImageObj> items) {
//        ImageAdapter.items = items;
//    }
}