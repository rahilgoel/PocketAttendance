package iit.iitr.pocketattendance;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.bluecats.sdk.BCBeacon;
import com.bluecats.sdk.BCCategory;
import com.bluecats.sdk.BCEventFilter;
import com.bluecats.sdk.BCEventManager;
import com.bluecats.sdk.BCEventManagerCallback;
import com.bluecats.sdk.BCLocalNotification;
import com.bluecats.sdk.BCLocalNotificationManager;
import com.bluecats.sdk.BCLocalNotificationManagerCallback;
import com.bluecats.sdk.BCMicroLocation;
import com.bluecats.sdk.BCMicroLocationManager;
import com.bluecats.sdk.BCMicroLocationManagerCallback;
import com.bluecats.sdk.BCSite;
import com.bluecats.sdk.BCTrigger;
import com.bluecats.sdk.BCTriggeredEvent;
import com.bluecats.sdk.BlueCatsSDK;
import com.bluecats.sdk.IBCEventFilter;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";
    private final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    private ProgressDialog pDialog;
    private SessionManager session;
    private BCSite mSite;
    private LocationManager locationManager;
    private BCMicroLocationManager mMicrolocationManager;
    private DialogInterface.OnClickListener mBluetoothDailogClickListner = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    final Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivity(enableBluetoothIntent);
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    break;
            }
        }
    };
    private DialogInterface.OnClickListener mLocationServicesClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(final DialogInterface dialog, final int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    final Intent enableLocationServicesIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(enableLocationServicesIntent);
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    // do nothing
                    break;
            }
        }
    };
    private DialogInterface.OnClickListener mRegisterDailogClickListner = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            String email = session.getEmail();
            Log.d("check", email);
            switch (which) {
                case DialogInterface.BUTTON_NEGATIVE:
                    //going out
                    updateEntry(email, "WentOut");
                    Log.d("no", "you clicked on negative button");

                    break;
                case DialogInterface.BUTTON_POSITIVE:
                    //coming in

                    updateEntry(email, "CameIn");
                    Log.d("yss", "you clicked on positive button");
                    break;
            }
        }
    };
    private BCMicroLocationManagerCallback mMicroLocationManagerCallback = new BCMicroLocationManagerCallback() {
        @Override
        public void onDidEnterSite(BCSite bcSite) {

        }

        @Override
        public void onDidExitSite(BCSite bcSite) {

        }

        @Override
        public void onDidUpdateNearbySites(final List<BCSite> list) {
            Handler h = new Handler(Looper.getMainLooper());
            h.post(new Runnable() {
                public void run() {
                    Toast.makeText(getApplicationContext(), list.get(0).getName(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public void onDidRangeBeaconsForSiteID(BCSite bcSite, final List<BCBeacon> list) {
            BCBeacon beacon = list.get(0);
            switch (beacon.getProximity()) {

                case BC_PROXIMITY_IMMEDIATE:

                    Handler h = new Handler(Looper.getMainLooper());
                    h.post(new Runnable() {
                        public void run() {
                            Toast.makeText(getApplicationContext(), list.get(0).getBeaconID() + "immediate", Toast.LENGTH_SHORT).show();
                        }
                    });

                    break;
                case BC_PROXIMITY_NEAR:
                    Handler h1 = new Handler(Looper.getMainLooper());
                    h1.post(new Runnable() {
                        public void run() {
                            Toast.makeText(getApplicationContext(), list.get(0).getBeaconID() + "near", Toast.LENGTH_SHORT).show();
                        }
                    });

                    break;
                case BC_PROXIMITY_FAR:
                    Handler h2 = new Handler(Looper.getMainLooper());
                    h2.post(new Runnable() {
                        public void run() {
                            Toast.makeText(getApplicationContext(), list.get(0).getBeaconID() + "far", Toast.LENGTH_SHORT).show();
                        }
                    });

                    break;
                case BC_PROXIMITY_UNKNOWN:

            }
        }


        @Override
        public void onDidUpdateMicroLocation(List<BCMicroLocation> list) {
            BCMicroLocation microLocation = list.get(list.size() - 1);
            for (final Map.Entry<String, List<BCBeacon>> entry : microLocation.getBeaconsForSiteID().entrySet()) {

                final List<BCBeacon> beacons = entry.getValue();

                  /*  mBeaconsImmediate.clear();
                    mBeaconsNear.clear();
                    mBeaconsFar.clear();
                    mBeaconsUnknown.clear();
*/
                // update the beacons lists depending on proximity
                for (final BCBeacon beacon : beacons) {
                    switch (beacon.getProximity()) {
                        case BC_PROXIMITY_IMMEDIATE:
                            //   mBeaconsImmediate.add(beacon);

//                            runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    //BCMicroLocationManager.getInstance().stopUpdatingMicroLocation(mMicroLocationManagerCallback);
//                                    new AlertDialog.Builder(MainActivity.this).setMessage("Hey you are near the gate of our office! Are you coming in or going out?")
//                                            .setPositiveButton("Coming In", mRegisterDailogClickListner)
//                                            .setNegativeButton("Going Out", mRegisterDailogClickListner)
//                                            .show();
//                                    BCMicroLocationManager.getInstance().stopUpdatingMicroLocation(mMicroLocationManagerCallback);
//                                }
//                            });

                            break;
                        case BC_PROXIMITY_NEAR:
                            //          mBeaconsNear.add(beacon);
//                            runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    new AlertDialog.Builder(MainActivity.this).setMessage("Hey you are near the gate of our office! Are you coming in or going out?")
//                                            .setPositiveButton("Coming In", mRegisterDailogClickListner)
//                                            .setNegativeButton("Going Out", mRegisterDailogClickListner)
//                                            .show();
//                                    BCMicroLocationManager.getInstance().stopUpdatingMicroLocation(mMicroLocationManagerCallback);
//                                    Log.d("hi", "near main aa gya");
//                                }
//                            });

                            break;
                        case BC_PROXIMITY_FAR:

//                            runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    new AlertDialog.Builder(MainActivity.this).setMessage("Hey you are near the gate of our office! Are you coming in or going out?")
//                                            .setPositiveButton("yes", mRegisterDailogClickListner)
//                                            .setNegativeButton("no", mRegisterDailogClickListner)
//                                            .show();
//                                    BCMicroLocationManager.getInstance().stopUpdatingMicroLocation(mMicroLocationManagerCallback);
//                                    Log.d("hi", "near main aa gya");
//                                }
//                            });
//                                mBeaconsFar.add(beacon);

                            break;
                        case BC_PROXIMITY_UNKNOWN:
                            //                              mBeaconsUnknown.add(beacon);
                            Log.d("hi", "unknown main aa gya");
                    }
                }

                   /* runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mAdapterBeaconsImmediate.notifyDataSetChanged();
                            mAdapterBeaconsNear.notifyDataSetChanged();
                            mAdapterBeaconsFar.notifyDataSetChanged();
                            mAdapterBeaconsUnknown.notifyDataSetChanged();
                        }
                    });*/

            }
        }

        @Override
        public void didBeginVisitForBeaconsWithSerialNumbers(List<String> list) {

        }

        @Override
        public void didEndVisitForBeaconsWithSerialNumbers(List<String> list) {

        }
    };

    public void checkStatus() {
        final BlueCatsSDK.BCAppTokenVerificationStatus appTokenVerificationStatus = BlueCatsSDK.getAppTokenVerificationStatus();
        if (appTokenVerificationStatus == BlueCatsSDK.BCAppTokenVerificationStatus.BC_APP_TOKEN_VERIFICATION_STATUS_NOT_PROVIDED) {
            //The app  token hasn't been provided; do something.
            Log.d(TAG, "app token is not been provided");

        }

        if (!BlueCatsSDK.isLocationAuthorized()) {
            //No GPS available; enable GPS.
            Log.d(TAG, "location is not authorised");
        }

        if (!BlueCatsSDK.isNetworkReachable()) {
            //No network reachable; enable network connection.
            Log.d(TAG, "network is not enabled so do something");
        }

        if (!BlueCatsSDK.isBluetoothEnabled()) {
            //Bluetooth is turned off; enable it.
            Log.d(TAG, "bluetoot h is not enable d");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Intent sitesIntent = getIntent();
        pDialog = new ProgressDialog(this);
        session = new SessionManager(getApplicationContext());
        prefs = this.getSharedPreferences("mydata", Context.MODE_PRIVATE);
        editor = prefs.edit();
        editor.putBoolean("entered", false);
        editor.commit();
        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, PersonalDetails.class));
            }
        });
        findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, AllDetails.class));

            }
        });
        mSite = sitesIntent.getParcelableExtra(BlueCatsSDK.EXTRA_SITE);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        //  Toast.makeText(MainActivity.this, "hi", Toast.LENGTH_SHORT).show();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        if (!bluetoothAdapter.isEnabled()) {
            new AlertDialog.Builder(MainActivity.this).setMessage("This app requires bluetooth to be enabled. Would you like to enambel bluetooth now")
                    .setPositiveButton("Yes", mBluetoothDailogClickListner)
                    .setNegativeButton("No", mBluetoothDailogClickListner)
                    .show();
        }
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            new AlertDialog.Builder(MainActivity.this)
                    .setMessage("This app requires Location Services to be enabled. Would you like to enable location Services now?")
                    .setPositiveButton("Yes", mLocationServicesClickListener)
                    .setNegativeButton("No", mLocationServicesClickListener)
                    .show();
        }
        checkStatus();
        BlueCatsSDK.startPurringWithAppToken(getApplicationContext(), AppController.BLUECATS_APP_TOKEN);
        BCMicroLocationManager.getInstance().startUpdatingMicroLocation(mMicroLocationManagerCallback);
        monitorCurrentGalleryExhibitChanged();
        BCLocalNotificationManager.getInstance().registerLocalNotificationManagerCallback(1, mLocalNotificationManagerCallback);
    }

    private final BCLocalNotificationManagerCallback mLocalNotificationManagerCallback = new BCLocalNotificationManagerCallback() {
        @Override
        public void onDidNotify(final int id) {
            // handle notification sent logic

        }
    };
    private final BCEventManagerCallback mBCEventManagerCallback = new BCEventManagerCallback() {
        @Override
        public void onTriggeredEvent(BCTriggeredEvent bcTriggeredEvent) {
            if (bcTriggeredEvent.getEvent().getEventIdentifier().equals("ChangedGalleryExhibitTrigger")) {
                //Display content for the new exhibit
                //Triggered event contains summary information about the trigger
                final int numberOfExhibitsVisited = bcTriggeredEvent.getTriggeredCount();
                final Date firstExhibitVisitedAt = bcTriggeredEvent.getFirstTriggeredAt();
                Log.d("hahahjaha", "triggered fired");
                scheduleLocalNotification();
                showNotiyDialog();
                //Triggered event also contains a BCMicroLocation object containing the remaining
                //filtered beacons and their sites which triggered the event
                final BCBeacon newExhibitBeacon = bcTriggeredEvent.getFilteredMicroLocation().getBeacons().get(0);
                final BCSite gallerySite = bcTriggeredEvent.getFilteredMicroLocation().getSites().get(0);
            } else if (bcTriggeredEvent.getEvent().getEventIdentifier().equals("AtMyDeskTrigger")) {
                //Welcome to my desk!
            }
        }

        private void showNotiyDialog() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //BCMicroLocationManager.getInstance().stopUpdatingMicroLocation(mMicroLocationManagerCallback);
                    new AlertDialog.Builder(MainActivity.this).setMessage("Hey you are near the gate of our office! Are you coming in or going out?")
                            .setPositiveButton("Coming In", mRegisterDailogClickListner)
                            .setNegativeButton("Going Out", mRegisterDailogClickListner)
                            .show();
                    BCMicroLocationManager.getInstance().stopUpdatingMicroLocation(mMicroLocationManagerCallback);
                }
            });
        }
    };

    private void scheduleLocalNotification() {
        //Create a new BCLocalNotification.
        final BCLocalNotification localNotification = new BCLocalNotification(1);

        //Add an optional site to trigger the notification in.
        final BCSite site = new BCSite();
        site.setSiteID("a980f01-12a6-5a93-e44d-c9dd2d5b49a1");
        site.setName("TaisTech");
        localNotification.setFireInSite(site);


        //Add one or several categories to fire the notification in.
        final BCCategory category = new BCCategory();
        category.setName("rahil");

        final List<BCCategory> categories = new ArrayList<>();
        categories.add(category);
        localNotification.setFireInCategories(categories);


        //Set the time that the notification becomes 'active', i.e. able to be fired.
        localNotification.setFireAfter(new Date(new Date().getTime() + (2 * 1000)));


        //Trigger the notification when the beacon is extremely close.
        localNotification.setFireInProximity(BCBeacon.BCProximity.BC_PROXIMITY_IMMEDIATE);


        //Give the notification a title and content.
        localNotification.setAlertContentTitle("TaisTech");
        localNotification.setAlertContentText("Welcome to our office");


        //Notification icon and sound are optional. If untouched, the app icon and default notification sound will be used.
        localNotification.setAlertSmallIcon(R.mipmap.ic_launcher);
        localNotification.setAlertSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        //Control where the notification goes when touched.
        //Can hold a bundle or any other information you'd like to hold.
        final Intent contentIntent = new Intent(MainActivity.this, AllDetails.class);
        contentIntent.putExtra("fromNotification", true);
        localNotification.setContentIntent(contentIntent);
        BCLocalNotificationManager.getInstance().scheduleLocalNotification(localNotification);
    }

    private void monitorCurrentGalleryExhibitChanged() {
//Set up filters to apply to ranged beacons
        final List<IBCEventFilter> filters = new ArrayList<IBCEventFilter>();

        //Only include beacons in the site named 'Art Gallery'
        filters.add(BCEventFilter.filterBySitesNamed(Arrays.asList(
                "TaisTech"
        )));

        //Only beacons tagged with a category named 'Exhibit'
        filters.add(BCEventFilter.filterByCategoriesNamed(Arrays.asList(
                "rahil"
        )));

        //Only allow beacons that have been in range for at least 1 seconds (we don't care about maxTimeIntervalNotMatched)
        filters.add(BCEventFilter.filterByMinTimeIntervalBeaconMatched(1000, Long.MAX_VALUE));

        //Only include beacons when they are detected as less than approximately 1 meters away
        filters.add(BCEventFilter.filterByAccuracyRangeFrom(0.0, 1.0));

        //Apply additional smoothing of changes in RSSI and accuracy
        filters.add(BCEventFilter.filterApplySmoothedRSSIOverTimeInterval(5000));

        //Only fire the event when the closest beacon passing the previous tests changes
        filters.add(BCEventFilter.filterByClosestBeaconChanged());

//Create the trigger
        final BCTrigger changedGalleryExhibitTrigger = new BCTrigger("ChangedGalleryExhibitTrigger", filters);

        //Set the repeat count for the event to fire indefinitely
        changedGalleryExhibitTrigger.setRepeatCount(Integer.MAX_VALUE);

        //Send the trigger to the event manager to be monitored
        BCEventManager.getInstance().monitorEventWithTrigger(changedGalleryExhibitTrigger, mBCEventManagerCallback);
    }

    public void updateEntry(final String email, final String status) {
        String tag_string_req = "req_login";
        pDialog.setMessage("Updating...");
        pDialog.show();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, AppController.IP + "/entry.php", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    boolean error = jsonObject.getBoolean("error");
                    if (!error) {
                        pDialog.hide();
                        Toast.makeText(getApplicationContext(), "Checked IN", Toast.LENGTH_SHORT).show();
                    } else {
                        pDialog.hide();
                        String errorMsg = jsonObject.getString("mssg");
                        Toast.makeText(getApplicationContext(),
                                errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("email", email);
                params.put("status", status);
                return params;
            }
        };

        AppController.getInstance().addToRequestQueue(stringRequest, tag_string_req);
    }

    @Override
    protected void onResume() {
        super.onResume();

        BlueCatsSDK.didEnterForeground();
        BCMicroLocationManager.getInstance().startUpdatingMicroLocation(mMicroLocationManagerCallback);


    }

    @Override
    protected void onPause() {
        super.onPause();

        BlueCatsSDK.didEnterBackground();
        BCMicroLocationManager.getInstance().stopUpdatingMicroLocation(mMicroLocationManagerCallback);
    }
}
