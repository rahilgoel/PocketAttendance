package iit.iitr.pocketattendance;

import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.bluecats.sdk.BCBeacon;
import com.bluecats.sdk.BCBeaconCommandCallback;
import com.bluecats.sdk.BCBeaconUpdates;
import com.bluecats.sdk.BCCategory;
import com.bluecats.sdk.BCError;
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

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class TestActivity extends AppCompatActivity {
    public final String TAG = TestActivity.this.getClass().getSimpleName();
    private BCMicroLocationManagerCallback mMicroLocationManagerCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BlueCatsSDK.startPurringWithAppToken(getApplicationContext(), "f0924d58-45b3-44f7-ae8e-e64c03acd4f5");
       BCMicroLocationManager.getInstance().startUpdatingMicroLocation(mMicrolocationCallback);
        checkStatus();
        monitorCurrentGalleryExhibitChanged();
        BCLocalNotificationManager.getInstance().registerLocalNotificationManagerCallback( 1,mLocalNotificationManagerCallback );
    }

    private BCMicroLocationManagerCallback mMicrolocationCallback = new BCMicroLocationManagerCallback() {
        @Override
        public void onDidEnterSite(final BCSite bcSite) {
            Log.d(TAG, "entered the site " + bcSite.getName().toString()+bcSite.getSiteID().toString());
            BCMicroLocationManager.getInstance().startRangingBeaconsInSite(bcSite, mMicroLocationManagerCallback);
        }

        @Override
        public void onDidExitSite(BCSite bcSite) {
            Log.d(TAG, "exited the site " + bcSite.getName().toString());
        }

        @Override
        public void onDidUpdateNearbySites(List<BCSite> list) {
            Log.d(TAG, "updated the site" + list.get(0).getName().toString());
        }

        @Override
        public void onDidRangeBeaconsForSiteID(BCSite bcSite, List<BCBeacon> list) {
            Log.d(TAG, "beacons detected at" + bcSite.toString() + "named as" + list.get(0).getName().toString());
        }

        @Override
        public void onDidUpdateMicroLocation(List<BCMicroLocation> list) {
            Log.d(TAG, "we got the beacon and the beacon id is: " + list.get(0).getBeacons().get(0).getName());
        }

        @Override
        public void didBeginVisitForBeaconsWithSerialNumbers(List<String> list) {

        }

        @Override
        public void didEndVisitForBeaconsWithSerialNumbers(List<String> list) {

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
                //Triggered event also contains a BCMicroLocation object containing the remaining
                //filtered beacons and their sites which triggered the event
                final BCBeacon newExhibitBeacon = bcTriggeredEvent.getFilteredMicroLocation().getBeacons().get(0);
                final BCSite gallerySite = bcTriggeredEvent.getFilteredMicroLocation().getSites().get(0);
            } else if (bcTriggeredEvent.getEvent().getEventIdentifier().equals("AtMyDeskTrigger")) {
                //Welcome to my desk!
            }
        }
    };
    private final BCLocalNotificationManagerCallback mLocalNotificationManagerCallback = new BCLocalNotificationManagerCallback()
    {
        @Override
        public void onDidNotify( final int id )
        {
            // handle notification sent logic

        }
    };
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

        //Only allow beacons that have been in range for at least 5 seconds (we don't care about maxTimeIntervalNotMatched)
        filters.add(BCEventFilter.filterByMinTimeIntervalBeaconMatched(5000, Long.MAX_VALUE));

        //Only include beacons when they are detected as less than approximately 2 meters away
        filters.add(BCEventFilter.filterByAccuracyRangeFrom(0.0, 2.0));

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

    private void scheduleLocalNotification()
    {
        //Create a new BCLocalNotification.
        final BCLocalNotification localNotification = new BCLocalNotification(1);

        //Add an optional site to trigger the notification in.
        final BCSite site = new BCSite();
        site.setSiteID( "a980f01-12a6-5a93-e44d-c9dd2d5b49a1" );
        site.setName( "TaisTech" );
        localNotification.setFireInSite( site );


        //Add one or several categories to fire the notification in.
        final BCCategory category = new BCCategory();
        category.setName( "rahil" );

        final List<BCCategory> categories = new ArrayList<>();
        categories.add( category );
        localNotification.setFireInCategories( categories );


        //Set the time that the notification becomes 'active', i.e. able to be fired.
        localNotification.setFireAfter( new Date( new Date().getTime() + ( 2 * 1000 ) ) );


        //Trigger the notification when the beacon is extremely close.
        localNotification.setFireInProximity( BCBeacon.BCProximity.BC_PROXIMITY_IMMEDIATE );


        //Give the notification a title and content.
        localNotification.setAlertContentTitle( "ALERT_TITLE" );
        localNotification.setAlertContentText( "ALERT_CONTENT" );


        //Notification icon and sound are optional. If untouched, the app icon and default notification sound will be used.
        localNotification.setAlertSmallIcon( R.mipmap.ic_launcher );
        localNotification.setAlertSound( RingtoneManager.getDefaultUri( RingtoneManager.TYPE_NOTIFICATION ) );
        //Control where the notification goes when touched.
        //Can hold a bundle or any other information you'd like to hold.
        final Intent contentIntent = new Intent( TestActivity.this, TestActivity.class );
        contentIntent.putExtra( "fromNotification", true );
        localNotification.setContentIntent( contentIntent );
        BCLocalNotificationManager.getInstance().scheduleLocalNotification( localNotification );
    }

    public void getDataFromBeacon(BCBeacon beacon) {
        final List<ByteBuffer> requestData = new ArrayList<>();
        requestData.add(ByteBuffer.wrap(("request data").getBytes()));

        beacon.requestDataArrayFromBeaconEndpoint(BCBeaconUpdates.BCBeaconEndpoint.BC_BEACON_ENDPOINT_USB_HOST, requestData, new BCBeaconCommandCallback() {
                    @Override
                    public void onDidComplete(final BCError error) {
                        //Operation complete
                        Log.d(TAG, "it is done");
                    }

                    @Override
                    public void onDidUpdateProgress(final int percent, final String status) {
                        //How far through the operation we are
                        Log.d(TAG, status + "" + percent);
                    }

                    @Override
                    public void onDidUpdateStatus() {
                        //Status update
                        Log.d(TAG, "status updated");

                    }

                    @Override
                    public void onDidResponseData(final List<ByteBuffer> responseData) {
                        //Success
                        Log.d(TAG, requestData.get(0).toString());
                    }
                }
        );
    }

    @Override
    protected void onPause() {
        super.onPause();

        BlueCatsSDK.didEnterBackground();
    }

    @Override
    protected void onResume() {
        super.onResume();

        BlueCatsSDK.didEnterForeground();
    }
}
