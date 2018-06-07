package com.example.fasic.creitive;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NoConnectionError;

import com.android.volley.Request;
import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response.Listener;
import com.android.volley.Response.ErrorListener;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Login extends AppCompatActivity {
    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    String passwordPattern = "^.{6,}$";
    String url = "http://blogsdemo.creitiveapps.com/login";
    Context context;

    SharedPreferences sharedPref;
    SharedPreferences.Editor sharedPrefEditor;

    RequestQueue queue;

    /**If there is token in sharedPref jumps to next activity */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        context = this; //need context for later
        getSupportActionBar().setTitle(R.string.login_label); //set title of activity

        sharedPref = getSharedPreferences("creitive", Context.MODE_PRIVATE);
        sharedPrefEditor = sharedPref.edit();

        String token = sharedPref.getString("token","");

        if(token != "") startNextActivity(token);
        else queue = Volley.newRequestQueue(this);
    }

    /** On login button press
     * Validates inputs, if error, toast's to user
     * On valid inut, sends request.
     * @param v
     */

    public void onLogin(View v) {
        final EditText emailET = findViewById(R.id.email);
        final EditText passwordET = findViewById(R.id.password);

        String email = emailET.getText().toString().trim();
        String password = passwordET.getText().toString().trim();

        if (email.matches(emailPattern)) {
            if (password.matches(passwordPattern))
                makeJsonObjReq(email, password);
            else
                Toast.makeText(getApplicationContext(), context.getString(R.string.error_password), Toast.LENGTH_SHORT).show();
        } else
            Toast.makeText(getApplicationContext(), context.getString(R.string.error_email), Toast.LENGTH_SHORT).show();
    }

    /**Makes new volley request, with json in header, and adds it to queue.
     *
     * @param email
     * @param password
     */

    private void makeJsonObjReq(String email, String password) {
        /**
         * Make json oblect for header.
         */
        Map<String, String> postParam = new HashMap<>();
        postParam.put("email", email);
        postParam.put("password", password);
        JSONObject json = new JSONObject(postParam);

        /**Listener for volley onRespones.
         * Gets token, saves it to sharedPref, and opens nex activity
         * If there is error, toast's it to user.
         */

        Listener listener = new Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                String token = "";
                try {
                    token = response.getString("token");
                }catch (JSONException error){
                    Log.i("Error", "Error getting token from response!");
                    Toast.makeText(getApplicationContext(), context.getString(R.string.error_wrong), Toast.LENGTH_SHORT).show();
                }
                // Save token  to sharedPref.
                sharedPrefEditor.putString("token", token);
                sharedPrefEditor.commit();

                if(token != "") startNextActivity(token);
                else Toast.makeText(getApplicationContext(), context.getString(R.string.error_wrong), Toast.LENGTH_SHORT).show();

            }
        };

        /** Error listener, if there is error on requast.
         * Gives user feedback for errors.
         */

        ErrorListener errorListener =  new ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                    Toast.makeText(context, context.getString(R.string.error_network_timeout), Toast.LENGTH_LONG).show();
                } else if (error instanceof AuthFailureError) {
                    Toast.makeText(context, context.getString(R.string.error_auth), Toast.LENGTH_LONG).show();
                }
            }
        };

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Method.POST, url, json,listener,errorListener) {

            /**
             * Passing request headers
             */
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Accept", "application/json");
                return headers;
            }

        };
        queue.add(jsonObjReq);
    }

    /** Starts next activity (Blog list) with param token.
     *
     * @param token
     **/

    private void startNextActivity(String token){
        Intent i = new Intent(getBaseContext(), BlogList.class);
        i.putExtra("token", token);
        startActivity(i);
    }

    /**
     * super.onStop
     * Method stops all volley requasts.
     * **/
    @Override
    protected void onStop() {
        super.onStop();
        if(queue != null) {
            queue.cancelAll(new RequestQueue.RequestFilter() {
                @Override
                public boolean apply(Request<?> request) {
                    return true;
                }
            });
        }
    }
}
