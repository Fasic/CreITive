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

    /** if no token, go back
     * else make requast, for blog list
     *
     * @param savedInstanceState
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blog_list);
        getSupportActionBar().setTitle(R.string.blog_list_label);
        context = this;

        Intent intent = this.getIntent();
        token = intent.getStringExtra("token");

        if(token == "") goBack();
        else{
            queue = Volley.newRequestQueue(this);
            makeJsonObjReq(token);
        }
    }

    /**
     * Fill LinearLayout with blogs, from JSON array
     *
     * @param array
     */

    private void fillTable(JSONArray array) {
        LinearLayout linearLayout = this.findViewById(R.id.holder);
        for(int i = 0 ; i < array.length(); i ++) {
            try {
                JSONObject obj = array.getJSONObject(i);
                String title = obj.getString("title");
                String description = obj.getString("description");
                String imgUrl = obj.getString("image_url");
                int id = obj.getInt("id");
                setRow(linearLayout, title, description, imgUrl, id);
            }catch (JSONException error){
                Toast.makeText(getApplicationContext(), context.getString(R.string.error_wrong), Toast.LENGTH_SHORT).show();
                Log.i("error", "Error with JOSNArray respones from server! Back-end problem!");
                //need better way of error handling? and user messige about it?
            }
        }

    }

    /**
     *Sets one row of blogs,
     * * sets values for text filds
     * * img for img
     * * and sets on click listener.
     *
     * @param linearLayout
     * @param title
     * @param description
     * @param imgUrl
     * @param id
     */

    private void setRow(LinearLayout linearLayout, String title, String description, String imgUrl, int id){
        View row = View.inflate(this, R.layout.blog_item, null);

        TextView textViewTitle = row.findViewById(R.id.title);
        textViewTitle.setText(title);

        TextView textViewDesc = row.findViewById(R.id.description);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            textViewDesc.setText(Html.fromHtml(description,Html.FROM_HTML_MODE_LEGACY));
        } else {
            textViewDesc.setText(Html.fromHtml(description));
        }

        ImageView imageView = row.findViewById(R.id.imageView);
        Picasso.get().load(imgUrl).into(imageView);

        /** on click, call next activity with id, id of blog */

        row.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                startNextActivity(v.getId());
            }
        });

        row.setId(id);
        linearLayout.addView(row);
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
                    goBack(); //goBack if token error
                }//drugi errori?
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
        queue.add(jsonObjReq);
    }

    /** Method starts next activity, and intent id of blog */

    private void startNextActivity(int id){
        Intent i = new Intent(getBaseContext(), Blog.class);
        i.putExtra("id", id);
        i.putExtra("token", token);
        startActivity(i);
    }

    /** Method for going back to Login, empty SharedPref, and go back */

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
