package com.example.fasic.creitive;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class BlogList extends AppCompatActivity {
    Context context;
    RequestQueue queue;
    String token;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;

        setContentView(R.layout.activity_blog_list);
        getSupportActionBar().setTitle(R.string.blog_list_label); //set title of activity

        Intent intent = this.getIntent();
        token = intent.getStringExtra("token");

        if(token == "") goBack();
        else{
            queue = Volley.newRequestQueue(this);
            Log.i("-->", token);
            makeJsonObjReq(token);
        }
    }

    private void fillTable(JSONArray array) {

        LinearLayout ll = this.findViewById(R.id.holder);
        for(int i = 0 ; i < array.length(); i ++) {
            try {
                JSONObject obj = array.getJSONObject(i);
                String title = obj.getString("title");
                String description = obj.getString("description");
                String imgUrl = obj.getString("image_url");
                int id = obj.getInt("id");
                setRow(ll, title, description, imgUrl, id);
            }catch (JSONException error){/*error za korisnika*/}
        }

    }

    private void setRow(LinearLayout ll, String title, String description, String imgUrl, int id){
        View mTableRow = View.inflate(this, R.layout.blog_item, null);

        TextView cb = mTableRow.findViewById(R.id.title);
        cb.setText(title);

        TextView cb2 = mTableRow.findViewById(R.id.description);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            cb2.setText(Html.fromHtml(description,Html.FROM_HTML_MODE_LEGACY));
        } else {
            cb2.setText(Html.fromHtml(description));
        }

        ImageView imageView = mTableRow.findViewById(R.id.imageView);
        Picasso.get().load(imgUrl).into(imageView);


        mTableRow.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                startNextActivity(v.getId());
            }
        });

        mTableRow.setId(id);

        ll.addView(mTableRow);
    }




    /**Makes new volley request, with token in header, and adds it to queue.
     *
     * @param token
     *
     */

    private void makeJsonObjReq(String token) {
        final String tokenF = token;
        /**Listener for volley onRespones.
         *
         */

        Response.Listener listener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i("-->", response.toString());
                JSONArray array = null;
                try {
                    array = new JSONArray(response);
                }catch (JSONException error){
                    Log.i("Error", "Error getting response array!");
                    Toast.makeText(getApplicationContext(), context.getString(R.string.error_wrong), Toast.LENGTH_SHORT).show();
                }

                if(array != null) fillTable(array);
                else Toast.makeText(getApplicationContext(), context.getString(R.string.error_wrong), Toast.LENGTH_SHORT).show();

            }
        };

        /** Error listener, if there is error on requast.
         * Gives user feedback for errors.
         */

        Response.ErrorListener errorListener =  new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                    Toast.makeText(context, context.getString(R.string.error_network_timeout), Toast.LENGTH_LONG).show();
                } else if (error instanceof AuthFailureError) {
                    goBack();
                }//drugi errori
                Log.i("error", " " + error.getMessage());
            }
        };

        String url = context.getString(R.string.url_blogs);

        StringRequest  jsonObjReq = new StringRequest (Request.Method.GET, url, listener, errorListener) {

            /**
             * Passing request headers
             */
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Accept", "application/json");
                headers.put("X-Authorize", tokenF);
                return headers;
            }

        };
        Log.i("-->", "kao");
        queue.add(jsonObjReq);
    }

    private void startNextActivity(int id){
        Log.i("-->", id+"");
        Intent i = new Intent(getBaseContext(), Blog.class);
        startActivity(i);
    }

    private void goBack(){
        SharedPreferences sharedPref;
        SharedPreferences.Editor sharedPrefEditor;
        sharedPref = getSharedPreferences("creitive", Context.MODE_PRIVATE);
        sharedPrefEditor = sharedPref.edit();
        sharedPrefEditor.putString("token", "");
        sharedPrefEditor.commit();

        Intent i = new Intent(getBaseContext(), Login.class);
        startActivity(i);
    }


}
