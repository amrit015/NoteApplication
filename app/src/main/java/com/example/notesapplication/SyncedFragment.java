package com.example.notesapplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.notesapplication.Utils.Config;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Amrit on 4/18/2016.
 */
public class SyncedFragment extends Fragment {
    ArrayList<NotesModule> list = new ArrayList<>();
    Context context;
    LinearLayout progressLayout;
    File folder;
    String currentUser;
    String title, notes, user;
    int postId, position;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // initialization
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        progressLayout = (LinearLayout) view.findViewById(R.id.headerProgress);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerview_home);
        mLayoutManager = new LinearLayoutManager(context);

        //if there is no SD card, create new directory objects to make directory on device
        if (Environment.getExternalStorageState() == null) {
            folder = new File(Environment.getDataDirectory()
                    + "/NoteApplication/");
            folder.mkdirs();
        }
        // if phone DOES have sd card
        if (Environment.getExternalStorageState() != null) {
            // search for directory on SD card
            folder = new File(Environment.getExternalStorageDirectory()
                    + "/NoteApplication/");
            folder.mkdirs();
        }
        //Fetching email from shared preferences
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(Config.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        currentUser = sharedPreferences.getString(Config.USER_SHARED_PREF, "Not Available");
        //use volley to inflate here and obtain data from the interent
        getSyncedNotes();
        // click on recyclerview items
        mRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(context, mRecyclerView, new RecyclerClickListener() {
            @Override
            public void onClick(View view, int position) {
                getActivity().openContextMenu(mRecyclerView);
            }
        }));
        return view;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        registerForContextMenu(mRecyclerView);
        mRecyclerView.setHasFixedSize(false);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    private void getSyncedNotes() {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, Config.VIEWCMT_URL, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                list.clear();
                //If we are getting success from server
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray jsonArray = jsonObject.getJSONArray("posts");
                    for (int i = jsonArray.length() - 1; i >= 0; i--) {
                        JSONObject c = jsonArray.getJSONObject(i);
                        String username = c.getString("username");
                        String title = c.getString("title");
                        String message = c.getString("message");
                        int postId = c.getInt("post_id");

                        if (c.getInt("delete") == 0) {
                            if (currentUser.equals(c.getString("username"))) {
                                NotesModule notesModule = new NotesModule();
                                notesModule.setNotes(message);
                                notesModule.setTitle(title);
                                notesModule.setUser(username);
                                notesModule.setPostId(postId);
                                list.add(notesModule);
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //displaying the obtained data
                displayView();
            }
        },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //You can handle error here if you want
                    }
                }) {
        };
        //Adding the string request to the queue
        RequestQueue requestQueue = Volley.newRequestQueue(getActivity());
        requestQueue.add(stringRequest);
    }

    // passing to recyclerview adapter to display on cardview and recyclerview
    private void displayView() {
        progressLayout.setVisibility(View.GONE);
        mAdapter = new MyRecyclerViewAdapter(list);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
    }

    //adding context menu on singleclick to each cards
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, v.getId(), 0, "Delete");
        menu.add(0, v.getId(), 0, "Cancel");
    }

    //defining context menu actions
    @Override
    public boolean onContextItemSelected(MenuItem item) {

        try {
            title = ((MyRecyclerViewAdapter) mRecyclerView.getAdapter()).getTitle();
            notes = ((MyRecyclerViewAdapter) mRecyclerView.getAdapter()).getNotes();
            position = ((MyRecyclerViewAdapter) mRecyclerView.getAdapter()).getPosition();
            user = ((MyRecyclerViewAdapter) mRecyclerView.getAdapter()).getUser();
            postId = ((MyRecyclerViewAdapter) mRecyclerView.getAdapter()).getPostId();
        } catch (Exception e) {
            return super.onContextItemSelected(item);
        }
        if (item.getTitle() == "Delete") {
            removeItem();
        } else {
            item.collapseActionView();
        }
        return super.onContextItemSelected(item);
    }

    private void removeItem() {

        StringRequest stringRequest = new StringRequest(Request.Method.POST, Config.DELETE_CMT, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                //If we are getting success from server
                String getResponse = response.toLowerCase();

                if (getResponse.contains("1")) {
                    Toast.makeText(getActivity(), "Successfully deleted notes", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), "Error while deleting notes:" + response, Toast.LENGTH_SHORT).show();
                }
                //updating the list through server and populating with the updated data
                progressLayout.setVisibility(View.VISIBLE);
                getSyncedNotes();
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
                params.put(Config.KETPOSTID, String.valueOf(postId));
                params.put(Config.KEY_DELETE, "1");
                //returning parameter
                return params;
            }
        };
        //Adding the string request to the queue
        RequestQueue requestQueue = Volley.newRequestQueue(getActivity());
        requestQueue.add(stringRequest);
    }
}
