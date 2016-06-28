package iit.iitr.pocketattendance;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PersonalDetails extends AppCompatActivity {
    private SessionManager session;
    private ProgressDialog pDailog;
    List<SingleData> list;
    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_details);
        pDailog = new ProgressDialog(this);
        session = new SessionManager(getApplicationContext());
        String email = session.getEmail();
        Log.d("email", email);
        //List<SingleData> list = getData("rahilever@gmail.com");
        //Log.d("check",list.get(0).getDay()+"rahil day");
        /*
        listView.setAdapter(adapter);*/
        getData(email);
        list=new ArrayList<SingleData>();
        listView = (ListView) findViewById(R.id.singleContactList);


    }

    @Override
    public void onBackPressed() {
        pDailog.dismiss();
        super.onBackPressed();
    }

    public void getData(final String email) {
        String tag = "get_data";
        pDailog.setMessage("getting data...");
        pDailog.show();
       // final List<SingleData> list = new ArrayList<SingleData>();
        StringRequest str_req = new StringRequest(Request.Method.POST, AppController.IP+"/getStatusSingle.php", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    boolean error = jsonObject.getBoolean("error");
                    if (!error) {
                        pDailog.dismiss();
                        Log.d("chu","rahil");
                        JSONArray data = jsonObject.getJSONArray("data");
                        for (int i = 0; i < data.length(); i++) {
                            SingleData entry = new SingleData();
                            JSONObject object = data.getJSONObject(i);
                            entry.setDay(object.getString("day"));
                            entry.setTime(object.getString("time"));
                            entry.setStatus(object.getString("status"));
                            Log.d("chu",object.getString("time"));
                            list.add(entry);
                        }
                        SingleListAdapter adapter = new SingleListAdapter(PersonalDetails.this, list);
                        listView.setAdapter(adapter);

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
//may be we will write something;
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("email", email);
                return params;
            }
        };
        AppController.getInstance().addToRequestQueue(str_req, tag);

    }
}
