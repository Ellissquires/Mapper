package com.example.mapper.ImageHandler;


import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.mapper.R;
import com.example.mapper.views.VisitImageView;

import java.io.File;
import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.View_Holder> {
    static private Context context;
    private static List<ImageObj> items;
    private static final int THUMBSIZE = Resources.getSystem().getDisplayMetrics().widthPixels;

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

    @Override
    public void onBindViewHolder(final View_Holder holder, final int position) {

        //Use the provided View Holder on the onCreateViewHolder method to populate the
        // current row on the RecyclerView
        if (holder!=null && items.get(position)!=null) {
            if (items.get(position).image!=-1) {
                holder.imageView.setImageResource(items.get(position).image);
            } else if (items.get(position).file!=null){

                Bitmap thumbImage = ThumbnailUtils.extractThumbnail(decodeSampledBitmapFromResource(items.get(position).file, 150, 150),
                        (THUMBSIZE/3 - 5), (THUMBSIZE/3 -5));
//                Bitmap myBitmap = BitmapFactory.decodeFile(items.get(position).file.getAbsolutePath());
                holder.imageView.setImageBitmap(thumbImage);
            }
            else if(items.get(position).bitmap != null){
                Bitmap thumbImage = ThumbnailUtils.extractThumbnail((items.get(position).bitmap),
                        (THUMBSIZE/3 - 5), (THUMBSIZE/3 -5));
//                Bitmap myBitmap = BitmapFactory.decodeFile(items.get(position).file.getAbsolutePath());
                holder.imageView.setImageBitmap(thumbImage);
            }
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, VisitImageView.class);
                    intent.putExtra("position", position);
                    context.startActivity(intent);
                }
            });
        }
        //animate(holder);
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

    public static List<ImageObj> getItems() {
        return items;
    }

    public static void setItems(List<ImageObj> items) {
        ImageAdapter.items = items;
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            // Calculate ratios of height and width to requested height and width
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will guarantee
            // a final image with both dimensions larger than or equal to the
            // requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }

        return inSampleSize;
    }

    public static Bitmap decodeSampledBitmapFromResource(File file,
                                                         int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file.getAbsolutePath(), options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
            return BitmapFactory.decodeFile(file.getAbsolutePath(), options);
    }
}