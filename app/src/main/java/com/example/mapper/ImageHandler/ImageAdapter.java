package com.example.mapper.ImageHandler;


import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ThumbnailUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.mapper.R;
import com.example.mapper.services.ImageFetchService;
import com.example.mapper.views.VisitImageView;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.View_Holder> {
    /**
     * variables to be used in this class.
     */
    private Context context;

    private List<ImageObj> items;
    private ArrayList<ImageObj> itemsList = new ArrayList<>();

    private final int THUMBSIZE = Resources.getSystem().getDisplayMetrics().widthPixels;

    private CacheHandler cache = CacheHandler.getInstance();

    /**
     * An Constructor method for the class ImageAdapter, that takes a list of ImageObj objects,
     * that are populated within the recycler view.
     * @param items
     */
    public ImageAdapter(List<ImageObj> items) {
        this.items = items;
    }

    /**
     * Creating holders for the recycler view. This is when the row layout is inflated,
     * passed to the ViewHolder object and each child view can be found, stored and manipulated
     * using the holder
     * @param parent
     * @param viewType
     * @return
     */
    @NotNull
    @Override
    public View_Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        //Inflate the layout, initialize the View Holder
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_image_item,
                parent, false);
        View_Holder holder = new View_Holder(v);
        context= parent.getContext();
        return holder;
    }

    /**
     * Binder for the adapter method.
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(@NotNull final View_Holder holder, final int position) {

        //Use the provided View Holder on the onCreateViewHolder method to populate the
        // current row on the RecyclerView

        if (items.get(position)!=null) {

            if (items.get(position).file!=null){
                String path = items.get(position).file.getAbsolutePath();
                Bitmap bitmap = cache.getFromCache(items.get(position).file.getAbsolutePath());
                if(bitmap == null) {
                    bitmap = ImageFetchService.decodeSampledBitmapFromResource(items.get(position).file, 150, 150);
                    if(items.get(position).getTag().equals("rotate"))
                        bitmap = rotateBitmap(bitmap,90);
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

    /**
     * Get the size of the items passed into the Adapter.
     * An integer value of the size of the adapter is returned
     * @return
     */
    @Override
    public int getItemCount() {
        return items.size();
    }

    /**
     * Constructor method for the View_Holder class. Instantiates the view, and assigns the values of
     * the Layout .
     */
     class View_Holder extends RecyclerView.ViewHolder  {
        ImageView imageView;


        View_Holder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.visit_image);

        }

    }

    public static Bitmap rotateBitmap(Bitmap source, float angle)
    {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getHeight(), source.getWidth(), matrix, true);
    }
}