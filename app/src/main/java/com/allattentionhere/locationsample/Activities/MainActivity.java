package com.allattentionhere.locationsample.Activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.allattentionhere.locationsample.Helper.LocationHelper;
import com.allattentionhere.locationsample.R;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, PlaceSelectionListener {

    Button btn_show, btn_locate;
    private static final String TAG = "Main";
    private static final int REQUEST_LOCATION = 100;
    private static final int REQUEST_PERMISSION_LOCATION = 200;

    LocationHelper myLocation;
    LocationHelper.LocationResult locationResult;
    Snackbar sb;
    PlaceAutocompleteFragment autocompleteFragment;
    TextView txt_location;
    ProgressBar pb_locate;
    double lat, lng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
        setListener();

    }

    private void init() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        btn_show = (Button) findViewById(R.id.btn_show);
        btn_locate = (Button) findViewById(R.id.btn_locate);
        txt_location = (TextView) findViewById(R.id.txt_location);
        pb_locate = (ProgressBar) findViewById(R.id.pb_locate);
        autocompleteFragment = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        myLocation = new LocationHelper();
        locationResult = new LocationHelper.LocationResult() {
            @Override
            public void gotLocation(Location location) {
                //Got the location!
                if (location != null) {
                    lat = location.getLatitude();
                    lng = location.getLongitude();
                    showAutoLocateLocation("LAT=" + location.getLatitude() + "\nLONG=" + location.getLongitude());
                    Log.d(TAG, "lat=" + location.getLatitude());

                } else {
                    Log.d(TAG, "null loc");
                    showAutoLocateButton();
                }
            }
        };

    }

    private void setListener() {
        btn_show.setOnClickListener(this);
        btn_locate.setOnClickListener(this);
        autocompleteFragment.setOnPlaceSelectedListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_show:
                Intent i = new Intent(MainActivity.this, MapsActivity.class);
                i.putExtra("lat", lat);
                i.putExtra("lng", lng);
                startActivity(i);
                break;
            case R.id.btn_locate:
                if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_LOCATION)) {
                    //device does not have LOCATION SERVICES
                    showSnackbar("Location Services not supported", null);
                } else if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    //has location permission
                    autoLocate();
                } else {
                    //does not have location permission
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            REQUEST_PERMISSION_LOCATION);

                }
                break;
        }
    }

    private void autoLocate() {
        if (!myLocation.getLocation(this, locationResult)) {
            //gps not active
            showSnackbar("Location Services are OFF", "TURN ON");
        } else {
            //start request, show progress
            showAutoLocateProgress();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    autoLocate();
                } else {
                    showSnackbar("Permission denied", null);
                }
                return;
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_LOCATION:
                if (!myLocation.getLocation(this, locationResult)) {
                    showSnackbar("Location Services are OFF", "TURN ON");
                } else {
                    showAutoLocateProgress();
                }
                break;
        }
    }

    private void showSnackbar(String s, String action) {
        if (sb != null && sb.isShownOrQueued()) {
            sb.dismiss();
        }
        sb = Snackbar.make(findViewById(R.id.cl_main), s, Snackbar.LENGTH_LONG).setDuration(Snackbar.LENGTH_LONG);
        if (action != null) {
            sb.setAction(action, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivityForResult(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS), REQUEST_LOCATION);
                }
            });
        } else {
        }
        sb.show();
    }

    public void showAutoLocateProgress() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                pb_locate.setVisibility(View.VISIBLE);
                btn_locate.setVisibility(View.GONE);
            }
        });
    }

    public void showAutoLocateButton() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                pb_locate.setVisibility(View.GONE);
                btn_locate.setVisibility(View.VISIBLE);
            }
        });
    }

    public void showAutoLocateLocation(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                txt_location.setText(text);
                pb_locate.setVisibility(View.GONE);
                btn_locate.setVisibility(View.VISIBLE);
                txt_location.setVisibility(View.VISIBLE);
                btn_show.setEnabled(true);
            }
        });

    }

    @Override
    public void onPlaceSelected(Place place) {
        Log.i(TAG, "Place: " + place.getName());
        showAutoLocateLocation("LAT=" + place.getLatLng().latitude + "\nLONG=" + place.getLatLng().longitude);
        lat = place.getLatLng().latitude;
        lng = place.getLatLng().longitude;
    }

    @Override
    public void onError(Status status) {
        showSnackbar(status.getStatusMessage(),null);
    }

}
