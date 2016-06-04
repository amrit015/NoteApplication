package com.example.notesapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.notesapplication.Utils.Config;

import java.util.HashMap;
import java.util.Map;

/**
 * Here a new user is registered on the online database
 */
public class RegisterActivity extends AppCompatActivity {

    private EditText user, pass;
    private Button mRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        user = (EditText) findViewById(R.id.register_user);
        pass = (EditText) findViewById(R.id.register_pass);
        mRegister = (Button) findViewById(R.id.register_to_server);
        mRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register();
            }
        });
    }

    private void register() {
        final String username = user.getText().toString().trim();
        final String password = pass.getText().toString().trim();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, Config.REGISTER_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //If we are getting success from server
                String getResponse = response.toLowerCase();
//                Toast.makeText(getApplicationContext(), "hell:" + response, Toast.LENGTH_SHORT).show();

                if (getResponse.contains("1")) {
                    Intent i = new Intent(getApplicationContext(), SignInActivity.class);
                    Toast.makeText(getApplicationContext(), "Success!!", Toast.LENGTH_SHORT).show();
                    startActivity(i);

                } else {
                    Toast.makeText(getApplicationContext(), "Error:" + response, Toast.LENGTH_SHORT).show();
                }
            }
        },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //You can handle error here if you want
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                //Adding parameters to request
                params.put(Config.KEY_EMAIL, username);
                params.put(Config.KEY_PASSWORD, password);
                //returning parameter
                return params;
            }
        };

        //Adding the string request to the queue
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }
}


