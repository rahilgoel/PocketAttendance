package iit.iitr.pocketattendance;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AllDetails extends AppCompatActivity {

    ListView listView;
    List<AllData> list;
    ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_details);
        listView = (ListView) findViewById(R.id.getAllData);
        pDialog = new ProgressDialog(this);
        list = new ArrayList<AllData>();
        getData();

    }

    @Override
    public void onBackPressed() {
        pDialog.dismiss();
        super.onBackPressed();


    }

    public void getData() {
        String tag = "get_all_data";
        pDialog.setMessage("loading data...");
        pDialog.show();
        StringRequest str_request = new StringRequest(Request.Method.POST, AppController.IP+"/getStatusAll.php", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    boolean error = jsonObject.getBoolean("error");
                    if (!error) {
                        pDialog.dismiss();
                        JSONArray jsonArray = jsonObject.getJSONArray("data");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            AllData data = new AllData();
                            JSONObject object = jsonArray.getJSONObject(i);
                            data.setId(object.getString("id"));
                            data.setName(object.getString("name"));
                            data.setDay(object.getString("day"));
                            data.setTime(object.getString("time"));
                            data.setStatus(object.getString("status"));
                            list.add(data);
                        }
                        AllListAdapter adapter = new AllListAdapter(AllDetails.this, list);
                        listView.setAdapter(adapter);
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

        };
        AppController.getInstance().addToRequestQueue(str_request, tag);
    }

}
