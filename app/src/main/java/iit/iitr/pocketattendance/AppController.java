package iit.iitr.pocketattendance;

import android.app.Application;
import android.text.TextUtils;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Created by rahil on 6/1/2016.
 */
public class AppController extends Application {
    static final String BLUECATS_APP_TOKEN="7bee2cbe-b212-4a14-a589-8fb7f4a084e9";
    public static final String Tag=AppController.class.getSimpleName();
    private RequestQueue requestQueue;
    private static AppController mInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance=this;
    }
    public static synchronized AppController getInstance(){
        return mInstance;
    }
    public RequestQueue getRequestQueue(){
    if(requestQueue==null){
        requestQueue= Volley.newRequestQueue(getApplicationContext());
    }
        return requestQueue;
    }
    public <T> void addToRequestQueue(Request<T> req,String tag){
        req.setTag(TextUtils.isEmpty(tag) ? Tag:tag);
        getRequestQueue().add(req);
    }

    public <T> void addToRequestQueue(Request<T> req){
        req.setTag(Tag);
        getRequestQueue().add(req);
    }

public void cancelPendingRequests(Object tag){
    if(requestQueue!=null){
        requestQueue.cancelAll(tag);
    }
}
}
