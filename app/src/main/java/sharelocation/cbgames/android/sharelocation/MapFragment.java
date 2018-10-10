package sharelocation.cbgames.android.sharelocation;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class MapFragment extends SupportMapFragment {


    private static final String TAG = "MapFragment";
    private GoogleMap mMap;
    boolean mStopHandler = false;
    Handler mHandler;
    private boolean isMapLoaded = false;

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            // do your stuff - don't create a new runnable here!
            if (!mStopHandler) {
                if(MyInformation.get(getActivity().getBaseContext()).getUser("0").getLocation() == null || MyInformation.get(getActivity().getBaseContext()).getUser("0").getLocation().latitude == 0) {
                    mHandler.postDelayed(this, 1000);
                }else{
                    mHandler.postDelayed(this, 40000);
                }
                updateUI();
            }
        }
    };

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mStopHandler = false;
    }

    @Override
    public void onDetach(){
        super.onDetach();
        mHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onPause(){
        super.onPause();
        mStopHandler = true;
    }

    @Override
    public void onResume(){
        super.onResume();
        mStopHandler = false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;
                updateUI();
                isMapLoaded = true;
            }
        });


        mHandler = new Handler();
        mHandler.post(runnable);
    }

    public void updateUI() {
        if (mMap == null){
            return;
        }
        LatLng location = MyInformation.get(getActivity().getBaseContext()).getUser("0").getLocation();

        int countPoints = 0;
        mMap.clear();

        boolean myLocationEmpty = true;

        if(location != null && location.latitude != 0){
            myLocationEmpty = false;
        }

        //Log.d(TAG, meterDistanceBetweenPoints(mLocation.getLatitude(), mLocation.getLongitude(), 48.484038, 135.077127)+"");
/*
        BitmapDescriptor itemBitmap = BitmapDescriptorFactory.fromBitmap(mMapImage);
        MarkerOptions itemMarker = new MarkerOptions()
                .position(itemPoint)
                .icon(itemBitmap);
*/
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        LatLngBounds bounds;
        LatLng myPoint = null;
        LatLng otherPoint = null;

        if(!myLocationEmpty) {
            BitmapDescriptor itemBitmap = BitmapDescriptorFactory.fromResource(R.drawable.myicon);
            myPoint = new LatLng(location.latitude, location.longitude);
            MarkerOptions myMarker = new MarkerOptions()
                    .position(myPoint)
                    .icon(itemBitmap);
            mMap.addMarker(myMarker);
            countPoints++;
            builder.include(myPoint);
        }
        ArrayList<MyInformation.InformationUser> informUsers = MyInformation.get(getContext()).getUsers();
        BitmapDescriptor itemBitmap = BitmapDescriptorFactory.fromResource(R.drawable.othericon);
        for(int i=0; i < informUsers.size(); i++){
            LatLng loc = informUsers.get(i).getLocation();
            otherPoint = new LatLng(loc.latitude, loc.longitude);
            MarkerOptions otherMarker = new MarkerOptions()
                    .position(otherPoint)
                    .icon(itemBitmap);
            mMap.addMarker(otherMarker);
            countPoints++;
            builder.include(otherPoint);
        }


        CameraUpdate update = null;

        int margin = getResources().getDimensionPixelSize(R.dimen.map_inset_margin);

        if(countPoints == 1){
            if(!myLocationEmpty){
                builder.include(myPoint);
            }else{
                builder.include(otherPoint);
            }
            bounds = builder.build();
            update = CameraUpdateFactory.newLatLngBounds(bounds, margin);
        }else if(countPoints != 0){
            bounds = builder.build();
            update = CameraUpdateFactory.newLatLngBounds(bounds, margin);
        }

        if(update != null && isMapLoaded == true)
            mMap.animateCamera(update);
    }
}