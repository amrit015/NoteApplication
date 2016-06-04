package com.example.notesapplication;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.notesapplication.Utils.Config;
import com.example.notesapplication.Utils.Log;
import com.example.notesapplication.Utils.OnAlarmReceiver;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


/**
 * This is the fragment which is also the startup fragment that displays on opening the app.
 * The data are obtained from the server here and passed to RecyclerAdapter
 */
public class HomeFragment extends android.support.v4.app.Fragment implements View.OnClickListener {

    public final static String TAG = "HomeFragment";
    Context context;
    File folder;
    ArrayList<NotesModule> list = new ArrayList<>();
    LinearLayout progressLayout;
    String title, notes, user;
    int position, postId;
    Dialog dialogRemainder;
    DatePickerDialog toDatePickerDialog;
    DatabaseReminder databaseReminder;
    String dateToReturn;
    String requiredDate;
    String timeHere;
    String timeToReturn;
    LinearLayout layoutDate;
    LinearLayout layoutTime;
    TextView time;
    TextView date;
    String currentUser;
    AlarmManager alarmManager;
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
            Log.i(TAG, "folder : " + folder);
        }
        // if phone DOES have sd card
        if (Environment.getExternalStorageState() != null) {
            // search for directory on SD card
            folder = new File(Environment.getExternalStorageDirectory()
                    + "/NoteApplication/");
            folder.mkdirs();
            Log.i(TAG, "folder : " + folder);
        }
        //Fetching email from shared preferences
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(Config.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        currentUser = sharedPreferences.getString(Config.USER_SHARED_PREF, "Not Available");
        //use volley to inflate here and obtain data from the interent
        getNotes();
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

    private void getNotes() {
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
                            NotesModule notesModule = new NotesModule();
                            notesModule.setNotes(message);
                            notesModule.setTitle(title);
                            notesModule.setUser(username);
                            notesModule.setPostId(postId);
                            list.add(notesModule);
                            Log.i(TAG, "username :" + username);
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
        menu.add(0, v.getId(), 0, "Reminder");
        menu.add(0, v.getId(), 0, "Share");
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
            Log.i(TAG, "title: " + title);
            Log.i(TAG, "note: " + notes);
            Log.i(TAG, "position: " + position);
        } catch (Exception e) {
            Log.d(TAG, e.getLocalizedMessage());
            return super.onContextItemSelected(item);
        }
        if (item.getTitle() == "Delete") {
            Log.i(TAG, "current user:" + currentUser);
            Log.i(TAG, "user who uploaded note:" + user);
            if (currentUser.equals(user)) {
                removeItem();
                Log.i(TAG, "deleted");
            } else {
                Toast.makeText(getActivity(), "Cannot Delete Note. Acess Denied", Toast.LENGTH_SHORT).show();
            }
        } else if (item.getTitle() == "Share") {
            shareItem(title, notes);
        } else if (item.getTitle() == "Reminder") {
            addReminder();
        } else {
            item.collapseActionView();
        }
        return super.onContextItemSelected(item);
    }

    // reminder -- see more comments on NotesFragment
    private void addReminder() {
        databaseReminder = new DatabaseReminder(folder, getActivity());
        if (databaseReminder.getReminderDate(title).length() == 0) {
            databaseReminder.addToReminder(title, "", "");
        }
        dialogRemainder = new Dialog(getActivity());
        dialogRemainder.setContentView(R.layout.remainder_layout);
        layoutDate = (LinearLayout) dialogRemainder.findViewById(R.id.layout_date);
        layoutTime = (LinearLayout) dialogRemainder.findViewById(R.id.layout_time);
        date = (TextView) dialogRemainder.findViewById(R.id.remainder_date);
        time = (TextView) dialogRemainder.findViewById(R.id.remainder_time);
        Button exit = (Button) dialogRemainder.findViewById(R.id.cancel_dialog);
        Button setRemainder = (Button) dialogRemainder.findViewById(R.id.set_remainder);
        Button cancelRemainder = (Button) dialogRemainder.findViewById(R.id.cancel_remainder);
        cancelRemainder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), OnAlarmReceiver.class);
                PendingIntent pi = PendingIntent.getService(getActivity(), 0, i, 0);
                alarmManager.cancel(pi);
                Toast.makeText(getActivity(), "Cancelling Alarm", Toast.LENGTH_SHORT).show();
            }
        });

        ///date picker
        final Calendar calendar = Calendar.getInstance();
        final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
        layoutDate.setOnClickListener(this);
        toDatePickerDialog = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
                date.setText(dateFormatter.format(newDate.getTime()));
                requiredDate = dateFormatter.format(newDate.getTime());
                databaseReminder.UpdateReminderDate(requiredDate, title);
            }

        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        //timepicker
        layoutTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker0, int selectedHour, int selectedMinute) {
                        String formattedMinute;
                        if (selectedMinute >= 0 && selectedMinute <= 9) {
                            formattedMinute = "0" + selectedMinute;
                        } else {
                            formattedMinute = String.valueOf(selectedMinute);
                        }
                        timeHere = selectedHour + ":" + formattedMinute;
                        time.setText(timeHere);
                        databaseReminder.UpdateReminderTime(timeHere, title);
                    }
                }, hour, minute, true);//Yes 24 hour time
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();
            }
        });

        dateToReturn = databaseReminder.getReminderDate(title);
        if (!dateToReturn.equals("")) {
            date.setText(dateToReturn);
        }
        timeToReturn = databaseReminder.getReminderTime(title);
        if (!timeToReturn.equals("")) {
            time.setText(timeToReturn);
        }
        // on click  "Set Remainder & Exit"
        setRemainder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!dateToReturn.equals("") && !timeToReturn.equals(""))
                    setNotification(title, notes, user);
                dialogRemainder.dismiss();
            }
        });
        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogRemainder.dismiss();
            }
        });
        dialogRemainder.show();
    }

    private void setNotification(String subject, String notes, String user) {
        String getTime = databaseReminder.getReminderTime(title);
        String getDate = databaseReminder.getReminderDate(title);

        //using AlarmManager
        alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(getActivity(), OnAlarmReceiver.class);
        Bundle bundle = new Bundle();
        bundle.putString("title_key", subject);
        bundle.putString("notes_key", notes);
        bundle.putString("user_key", user);
        i.putExtras(bundle);
        PendingIntent pi = PendingIntent.getService(getActivity(), 0, i, 0);

        String value = (Integer.parseInt(getDate.substring(6, 10)) + "*" + Integer.parseInt(getDate.substring(3, 5)) + "*" +
                Integer.parseInt(getDate.substring(0, 2)) + "*" + Integer.parseInt(getTime.substring(0, 2)) + "*" +
                Integer.parseInt(getTime.substring(3, 5)));
        android.util.Log.i(TAG, "reminder ko :" + value);

        Calendar myCal = Calendar.getInstance();
        myCal.setTimeInMillis(System.currentTimeMillis());
        myCal.clear();
        myCal.set(Integer.parseInt(getDate.substring(6, 10)), (Integer.parseInt(getDate.substring(3, 5)) - 1),
                Integer.parseInt(getDate.substring(0, 2)), Integer.parseInt(getTime.substring(0, 2)),
                Integer.parseInt(getTime.substring(3, 5)));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, myCal.getTimeInMillis(), pi);
            Toast.makeText(getActivity(), "Alarm set for " + myCal.getTime().toLocaleString(), Toast.LENGTH_LONG).show();
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, myCal.getTimeInMillis(), pi);
//            Toast.makeText(getActivity(), "Alarm set for " + myCal.getTime().toLocaleString(), Toast.LENGTH_LONG).show();
        }
    }

    //share functionality
    private void shareItem(String title, String notes) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        intent.putExtra(Intent.EXTRA_TEXT, "Title : " + title + "\n" + "Notes: " + notes);
        startActivity(Intent.createChooser(intent, "SHARE NOTES"));
    }

    //onclick listener for datePicker
    @Override
    public void onClick(View v) {
        // for datePicker
        if (v == layoutDate) {
            toDatePickerDialog.show();
        }
    }

    //removing items from the database online
    private void removeItem() {

        StringRequest stringRequest = new StringRequest(Request.Method.POST, Config.DELETE_CMT, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                //If we are getting success from server
                String getResponse = response.toLowerCase();
                Log.i(TAG, "Response while sending delete command : " + response);

                if (getResponse.contains("1")) {
                    Toast.makeText(getActivity(), "Successfully deleted notes", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), "Error while deleting notes:" + response, Toast.LENGTH_SHORT).show();
                }
                //updating the list through server and populating with the updated data
                progressLayout.setVisibility(View.VISIBLE);
                getNotes();
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
                Log.i(TAG, "postID to delete :" + String.valueOf(postId));
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