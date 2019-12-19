package com.example.mapper.views;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.example.mapper.ImageHandler.CacheHandler;
import com.example.mapper.ImageHandler.ImageAdapter;
import com.example.mapper.ImageHandler.ImageObj;
import com.example.mapper.R;
import com.example.mapper.services.ImageFetchService;
import com.example.mapper.services.PathRepository;
import com.example.mapper.services.PicturePointRepository;
import com.example.mapper.services.PointRepository;
import com.example.mapper.services.VisitRepository;
import com.example.mapper.services.models.PicturePoint;
import com.example.mapper.services.models.Point;
import com.example.mapper.services.models.Visit;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.card.MaterialCardView;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author Ellis Squires
 * @version 1.0
 * @since 1.0
 */
public class VisitImageView extends AppCompatActivity implements OnMapReadyCallback {

    CacheHandler cache = CacheHandler.getInstance();
    private VisitRepository mVisitRepo;
    private PicturePointRepository mPicturePointRepo;
    private PathRepository mPathRepo;
    private PointRepository mPointRepo;

    private TextView visitImageViewPressure;
    private TextView visitImageViewTemperature;
    private TextView visitImageTitleView;
    private Context mContext;
    private GoogleMap mMap;
    private List<Point> mPoints;
    String starterID = null;
    String finisherID = null;
    private Visit mVisit;
    private PicturePoint mPoint;
    private Point imagePoint;

    /**
     * A default method for every class that extends AppCompatActivity that allows it to define
     * which layout it will use, as well as how define and to manipulate its contents
     * This method makes use of the activity_map layout.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_image);
        mVisitRepo = new VisitRepository(getApplication());
        mPicturePointRepo = new PicturePointRepository(getApplication());
        mPathRepo = new PathRepository(getApplication());
        mPointRepo = new PointRepository(getApplication());
        mContext = getApplicationContext();
        visitImageViewPressure = findViewById(R.id.pressure);
        visitImageViewTemperature = findViewById(R.id.temperature);
        visitImageTitleView = findViewById(R.id.title);

        Bundle bundle = getIntent().getExtras();
        File element = (File) bundle.get("image");
        String tag = (String) bundle.get("tag");

        ImageButton details = (ImageButton) findViewById(R.id.fab_details);
        final MaterialCardView card = (MaterialCardView) findViewById(R.id.final_path_view);
        card.setVisibility(View.GONE);

        details.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(card.getVisibility() == View.GONE){
                    card.setVisibility(View.VISIBLE);
                }
                else{
                    card.setVisibility(View.GONE);
                }
            }
        });

        if(element != null){
            ImageView imageView = findViewById(R.id.image);

            String uri = element.toURI().toString();
            Log.d("VisitImageView", "URI " + uri.toString());
            LiveData<PicturePoint> picturePoint = mPicturePointRepo.getPicturePointFromURI(uri);
            picturePoint.observe(this, new Observer<PicturePoint>() {
                @Override
                public void onChanged(PicturePoint picturePoint) {
                    if (picturePoint != null) {
                        Log.d("VisitImageView", "Point retrieved" + picturePoint.toString());
                        mPoint = picturePoint;
                        LiveData<Point> p = mPointRepo.getPoint(picturePoint.getPointId()-1);
                        p.observe(VisitImageView.this, new Observer<Point>() {
                            @Override
                            public void onChanged(Point point) {
                                if (point != null) {
                                    Log.d("VisitImageView", "Point111 retrieved" + point.toString());
                                    String temp = point.getTemperature() + "C";
                                    imagePoint = point;
                                    visitImageViewTemperature.setText(temp);
                                    String pressure = String.format("%.1f mbars", point.getPressure());
                                    visitImageViewPressure.setText(pressure);
                                }
                            }
                        });


                    }
                }
            });
            String contentTitle = element.getAbsolutePath().split("/")[9];
            LiveData<Visit> visit = mVisitRepo.getVisit(contentTitle);
            visit.observe(this, new Observer<Visit>() {
                @Override
                public void onChanged(Visit visit) {
                    if (visit != null) {
                        Log.d("VisitImageView", "Visit retrieved" + visit.toString());
                        mVisit = visit;
                        visitImageTitleView.setText(mVisit.getTitle());
                        // Map initialisation (async)
                        SupportMapFragment mapFragment =
                                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
                        mapFragment.getMapAsync(VisitImageView.this);
                    }
                }
            });

            Bitmap myBitmap = BitmapFactory.decodeFile(element.getAbsolutePath());
            imageView.setImageBitmap(myBitmap);
        }


    }


    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;

        try {
            // Set the map style
            mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(
                    this, R.raw.map_style));
        } catch (Resources.NotFoundException e) {

        }

        // Fetching the visit path and drawing it on the map
        long pathID = mVisit.getPathId();
        LiveData<List<Point>> path = mPathRepo.getPointsOnPath(pathID - 1);

        final LifecycleOwner owner = this;
        path.observe(this, new Observer<List<Point>>() {
            @Override
            public void onChanged(@Nullable final List<Point> path) {
                if (path.size() > 0){
                    Log.d("VisitView", "Path retrieved with size " + path.size());
                    mMap.clear();
                    // Define line options
                    PolylineOptions options = new PolylineOptions().width(10).color(Color.BLUE).geodesic(true);
                    Point p1 = path.get(0);
                    // Center the map on the first point of the path
                    Log.d("VisitView", "First Point" + p1.getLat() + " - " + p1.getLng());
                    LatLng latLng = new LatLng(p1.getLat(),p1.getLng());
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15);
                    mMap.animateCamera(cameraUpdate);
                    int counter = 0;
                    // Loop points and add them to the line
                    for(final Point p : path){
                        final LatLng mapPoint = new LatLng(p.getLat(), p.getLng());
                        options.add(mapPoint);
                        if(counter == 0){
                            Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.map_start_pin);
                            bitmap = ImageFetchService.getIcon(bitmap, 150);
                            starterID = mMap.addMarker(new MarkerOptions()
                                    .position(mapPoint)
                                    .icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                                    .title("Start")).getId();
                        }
                        else if(counter == (path.size()-1)){
                            Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.map_finish_pin);
                            bitmap = ImageFetchService.getIcon(bitmap, 150);
                            finisherID = mMap.addMarker(new MarkerOptions()
                                    .position(mapPoint)
                                    .icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                                    .title("Finish")).getId();
                        }

                        // For each point, check if there is a corresponding picture point.
                        // If there is, add a marker to the map.
                        LiveData<PicturePoint> pictPoint = mPicturePointRepo.getPicturePoint(p.getId()-1);
                        pictPoint.observe(owner, new Observer<PicturePoint>() {
                            @Override
                            public void onChanged(PicturePoint picturePoint) {

                                if (picturePoint != null) {
                                    if (picturePoint.getPointId() == mPoint.getPointId()) {
                                        Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.map_image_pin);
                                        bitmap = ImageFetchService.getIcon(bitmap, 250);
                                        finisherID = mMap.addMarker(new MarkerOptions()
                                                .position(mapPoint)
                                                .icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                                                .title("Image")).getId();
                                    }
                                    else {
                                        final BitmapFactory.Options options = new BitmapFactory.Options();
                                        Bitmap bitmap;
                                        bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.map_pin);

                                        bitmap = ImageFetchService.getIcon(bitmap, 150);
                                        Marker m = mMap.addMarker(new MarkerOptions()
                                                .position(mapPoint)
                                                .icon(BitmapDescriptorFactory.fromBitmap(bitmap)));
                                        HashMap<String, Object> tags = new HashMap<>();
                                        tags.put("point", p);
                                        tags.put("picturePoint", picturePoint);
                                        m.setTag(tags);
                                    }
                                }
                            }
                        });
                        counter++;
                    }
                    // Draw the line
                    mMap.addPolyline(options);

                    mPoints = new ArrayList<Point>();
                    mPoints.addAll(path);
                }

            }
        });
    }

}