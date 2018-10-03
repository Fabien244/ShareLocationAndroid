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

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            // do your stuff - don't create a new runnable here!
            if (!mStopHandler) {
                mHandler.postDelayed(this, 1000);
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
                //updateUI();
            }
        });

        mHandler = new Handler();
        mHandler.post(runnable);
    }

    public void updateUI() {
        if (mMap == null){
            return;
        }
        Location location = MyInformation.get(getActivity().getBaseContext()).getUser(0).getLocation();

        if(location == null) {
            return;
        }

        mMap.clear();

        //Log.d(TAG, meterDistanceBetweenPoints(mLocation.getLatitude(), mLocation.getLongitude(), 48.484038, 135.077127)+"");
/*
        BitmapDescriptor itemBitmap = BitmapDescriptorFactory.fromBitmap(mMapImage);
        MarkerOptions itemMarker = new MarkerOptions()
                .position(itemPoint)
                .icon(itemBitmap);
*/
        LatLng myPoint = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions myMarker = new MarkerOptions()
                .position(myPoint);
        mMap.addMarker(myMarker);

        ArrayList<MyInformation.InformationUser> informUsers = MyInformation.get(getContext()).getUsers();
        for(int i=1; i < informUsers.size(); i++){
            Location loc = informUsers.get(i).getLocation();
            LatLng otherPoint = new LatLng(loc.getLatitude(), loc.getLongitude());
            MarkerOptions otherMarker = new MarkerOptions()
                    .position(otherPoint);
            mMap.addMarker(otherMarker);
        }


/*
        LatLngBounds bounds = new LatLngBounds.Builder()
                .include(otherPoint)
                .include(myPoint)
                .build();
                */
        int margin = getResources().getDimensionPixelSize(R.dimen.map_inset_margin);
        //CameraUpdate update = CameraUpdateFactory.newLatLngBounds(bounds, margin);
        CameraUpdate update = CameraUpdateFactory.newLatLng(myPoint);
        mMap.animateCamera(update);
    }
}