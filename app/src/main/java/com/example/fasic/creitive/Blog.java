package com.example.fasic.creitive;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.webkit.WebView;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Blog extends AppCompatActivity {
    Context context;
    RequestQueue queue;
    String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blog);
        getSupportActionBar().setTitle(R.string.blog_label);
        context = this;

        Intent intent = this.getIntent();
        int id = intent.getIntExtra("id", -1);
        token = intent.getStringExtra("token");

        if(id == -1) this.finish(); //go back somthing wrong with id
        else{
            queue = Volley.newRequestQueue(this);
            makeJsonObjReq(id);
        }
    }

    private void makeJsonObjReq(int id) {
        final String tokenF = token;

        /**Listener for volley onRespones.
         *
         */

        Response.Listener listener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                if(response != null) fillBlog(response);
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
                    goLogin(); //goLogin if token error
                }//drugi errori?
                Log.i("error", " " + error.getMessage());
            }
        };

        String url = context.getString(R.string.url_blogs);
        url +=  "/" + id;

        JsonObjectRequest jsonObjReq = new JsonObjectRequest (Request.Method.GET, url, null, listener, errorListener) {

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
        queue.add(jsonObjReq);
    }

    /** Method for going back to Login, empty SharedPref, and go to Login activity */

    private void goLogin(){
        SharedPreferences sharedPref;
        SharedPreferences.Editor sharedPrefEditor;
        sharedPref = getSharedPreferences("creitive", Context.MODE_PRIVATE);
        sharedPrefEditor = sharedPref.edit();
        sharedPrefEditor.putString("token", "");
        sharedPrefEditor.commit();

        Intent i = new Intent(getBaseContext(), Login.class);
        startActivity(i);
    }

    /***/

    private void fillBlog(JSONObject blog){
        WebView blogView = findViewById(R.id.blogView);

        String content = "";
        try {
            content = blog.get("content").toString();
            Log.i("-->", content);
        }catch (JSONException error){
            Toast.makeText(getApplicationContext(), context.getString(R.string.error_wrong), Toast.LENGTH_SHORT).show();
            Log.i("error", "Error with JOSNObject content from server! Back-end problem!");
            //need better way of error handling? and user message about it?
        }

        blogView.setInitialScale(1);
        blogView.getSettings().setJavaScriptEnabled(true);
        blogView.getSettings().setLoadWithOverviewMode(true);
        blogView.getSettings().setUseWideViewPort(true);
        blogView.getSettings().setSupportZoom(true);
        blogView.getSettings().setBuiltInZoomControls(true);
        blogView.getSettings().setDisplayZoomControls(false);
        blogView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        blogView.setScrollbarFadingEnabled(false);

        blogView.loadData(content, "text/html", null);



    }
}
