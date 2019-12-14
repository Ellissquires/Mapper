package com.example.mapper.ImageHandler;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mapper.R;
import com.example.mapper.services.ImageFetchService;
import com.example.mapper.views.VisitImageView;

import org.w3c.dom.Text;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SortedImageAdapter extends RecyclerView.Adapter<SortedImageAdapter.View_Holder> {
    static private Context context;
    private static List<File> items;
    private RecyclerView.Adapter mAdapter;

    public SortedImageAdapter(List<File> items) {
        this.items = items;
    }

    public SortedImageAdapter(Context cont, List<File> items) {
        super();
        this.items = items;
        context = cont;
    }

    @Override
    public View_Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        //Inflate the layout, initialize the View Holder
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_sorted_image_item,
                parent, false);
        View_Holder holder = new View_Holder(v);
        context= parent.getContext();
        return holder;
    }

    @Override
    public void onBindViewHolder(final View_Holder holder, final int position) {

        //Use the provided View Holder on the onCreateViewHolder method to populate the
        // current row on the RecyclerView
        int numberOfColumns = 3;

        if (holder!=null && items.get(position)!=null) {
            if (items.get(position).listFiles() !=null ) {
                List<ImageObj> myPictureList = new ArrayList<>();
                String contentTitle = items.get(position).getAbsolutePath().split("/")[9];
                List<File> imageFiles = Arrays.asList(items.get(position).listFiles());
                mAdapter= new ImageAdapter(myPictureList);
                holder.recyclerView.setLayoutManager(new GridLayoutManager(context, numberOfColumns));
                holder.textView.setText(contentTitle);
                holder.recyclerView.setAdapter(mAdapter);
                myPictureList.addAll(ImageFetchService.getImageElements(imageFiles));
                mAdapter.notifyDataSetChanged();
            }
        }
    }


    // convenience method for getting data at click position
    File getItem(int id) {
        return items.get(id);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class View_Holder extends RecyclerView.ViewHolder  {
        RecyclerView recyclerView;
        TextView textView;

        View_Holder(View itemView) {
            super(itemView);
             recyclerView = (RecyclerView) itemView.findViewById(R.id.sorted_visit_gallery);
             textView = (TextView) itemView.findViewById(R.id.Sorted_Title);

        }

    }

    public static List<File> getItems() {
        return items;
    }

    public static void setItems(List<File> items) {
        SortedImageAdapter.items = items;
    }
}