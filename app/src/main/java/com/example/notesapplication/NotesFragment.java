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
import android.support.design.widget.FloatingActionButton;
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
import android.widget.EditText;
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

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * This fragment displays the saved notes on the database and provides options such as EDIT,DELETE,SHARE through
 * the use of contextmenu on long click on the card items.
 */
public class NotesFragment extends android.support.v4.app.Fragment implements View.OnClickListener {

    private static final String TAG = "NotesFragment";
    Context context;
    File folder;
    DatabaseHelperNotes db;
    NotesModule notesModule;
    EditText dialogNotesTitle;
    EditText dialogNotesContent;
    LinearLayout dialogUpdateLayout;
    TextView dialog_save;
    TextView dialog_cancel;
    Dialog dialog;
    String title;
    String notes;
    int position;
    String currentUser;
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
    AlarmManager alarmManager;
    String user;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ArrayList<NotesModule> memoArray = new ArrayList<>();

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notes, container, false);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);

        //if there is no SD card, create new directory objects to make directory on device
        if (Environment.getExternalStorageState() == null) {
            folder = new File(Environment.getDataDirectory()
                    + "/NoteApplication/");
        }
        // if phone DOES have sd card
        if (Environment.getExternalStorageState() != null) {
            // search for directory on SD card
            folder = new File(Environment.getExternalStorageDirectory()
                    + "/NoteApplication/");
        }
        Log.i(TAG, "folder :" + folder);

        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.floating_button_add_notes);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), NotesAddActivity.class);
                startActivityForResult(intent, 0);
            }
        });
        mLayoutManager = new LinearLayoutManager(context);
        //inflating recyclerview
        try {
            mAdapter = new MyRecyclerViewAdapter(getDataSet());
        } catch (IOException e) {
            e.printStackTrace();
        }
        mRecyclerView.setOnCreateContextMenuListener(this);
        mRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(context, mRecyclerView, new RecyclerClickListener() {
            @Override
            public void onClick(View view, int position) {
                getActivity().openContextMenu(mRecyclerView);
            }
        }));
        //Fetching email from shared preferences
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(Config.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        currentUser = sharedPreferences.getString(Config.USER_SHARED_PREF, "Not Available");
        return view;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        registerForContextMenu(mRecyclerView);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    //adding context menu on singleclick to each cards
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, v.getId(), 0, "Edit");
        menu.add(0, v.getId(), 0, "Delete");
        menu.add(0, v.getId(), 0, "Upload");
        menu.add(0, v.getId(), 0, "Set Reminder");
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
            Log.i(TAG, "title: " + title);
            Log.i(TAG, "note: " + notes);
            Log.i(TAG, "position: " + position);
        } catch (Exception e) {
            Log.d(TAG, e.getLocalizedMessage());
            return super.onContextItemSelected(item);
        }
        if (item.getTitle() == "Edit") {
            editItem(title, notes);
        } else if (item.getTitle() == "Delete") {
            removeItem(title, position);
        } else if (item.getTitle() == "Share") {
            shareItem(title, notes);
        } else if (item.getTitle() == "Upload") {
            uploadNotes(title, notes);
        } else if (item.getTitle() == "Set Reminder") {
            detailsRemainder();
        } else {
            item.collapseActionView();
        }
        return super.onContextItemSelected(item);
    }

    private void detailsRemainder() {
        // initializing database
        databaseReminder = new DatabaseReminder(folder, getActivity());
        if (databaseReminder.getReminderDate(title).length() == 0) {
            databaseReminder.addToReminder(title, "", "");
        }
        // initializing dialog
        dialogRemainder = new Dialog(getActivity());
        dialogRemainder.setContentView(R.layout.remainder_layout);
        layoutDate = (LinearLayout) dialogRemainder.findViewById(R.id.layout_date);
        layoutTime = (LinearLayout) dialogRemainder.findViewById(R.id.layout_time);
        date = (TextView) dialogRemainder.findViewById(R.id.remainder_date);
        time = (TextView) dialogRemainder.findViewById(R.id.remainder_time);
        Button exit = (Button) dialogRemainder.findViewById(R.id.cancel_dialog);
        Button setRemainder = (Button) dialogRemainder.findViewById(R.id.set_remainder);
        Button cancelRemainder = (Button) dialogRemainder.findViewById(R.id.cancel_remainder);
        // on click method to cancel previously set alarms
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
        //on click method for datePicker to get the clicked date
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
                // initializing time picker method to get the clicked time
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

    // setting up notification on the specified time using AlarmManager
    private void setNotification(String title, String notes, String user) {
        String getTime = databaseReminder.getReminderTime(title);
        String getDate = databaseReminder.getReminderDate(title);

        //using AlarmManager
        alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(getActivity(), OnAlarmReceiver.class);
        Bundle bundle = new Bundle();
        bundle.putString("title_key", title);
        bundle.putString("notes_key", notes);
        bundle.putString("user_key", user);
        i.putExtras(bundle);
        //using service for getting notification
        PendingIntent pi = PendingIntent.getService(getActivity(), 0, i, 0);

        String value = (Integer.parseInt(getDate.substring(6, 10)) + "*" + Integer.parseInt(getDate.substring(3, 5)) + "*" +
                Integer.parseInt(getDate.substring(0, 2)) + "*" + Integer.parseInt(getTime.substring(0, 2)) + "*" +
                Integer.parseInt(getTime.substring(3, 5)));
        Log.i(TAG, "reminder ko :" + value);

        Calendar myCal = Calendar.getInstance();
        myCal.setTimeInMillis(System.currentTimeMillis());
        myCal.clear();
        // set up notification in the format : year-mm-dd hour-minutes
        myCal.set(Integer.parseInt(getDate.substring(6, 10)), (Integer.parseInt(getDate.substring(3, 5)) - 1),
                Integer.parseInt(getDate.substring(0, 2)), Integer.parseInt(getTime.substring(0, 2)),
                Integer.parseInt(getTime.substring(3, 5)));

        // for different devices(api)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, myCal.getTimeInMillis(), pi);
            Toast.makeText(getActivity(), "Alarm set for " + myCal.getTime().toLocaleString(), Toast.LENGTH_LONG).show();
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, myCal.getTimeInMillis(), pi);
            Toast.makeText(getActivity(), "Alarm set for " + myCal.getTime().toLocaleString(), Toast.LENGTH_LONG).show();
        }
    }

    //uploading notes
    private void uploadNotes(final String title, final String notes) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Config.ADDCMT_URL, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                //If we are getting success from server
                String getResponse = response.toLowerCase();

                if (getResponse.contains("1")) {
                    Toast.makeText(getActivity(), "Successfully uploaded notes", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), "Error while uploading notes:" + response, Toast.LENGTH_SHORT).show();
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
                params.put(Config.KEY_AUTHOR, currentUser);
                params.put(Config.KEY_TITLE, title);
                params.put(Config.KEY_TEXT, notes);

                //returning parameter
                return params;
            }
        };

        //Adding the string request to the queue
        RequestQueue requestQueue = Volley.newRequestQueue(getActivity());
        requestQueue.add(stringRequest);
    }

    //share functionality
    private void shareItem(String title, String notes) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        intent.putExtra(Intent.EXTRA_TEXT, "Title : " + title + "\n" + "Notes: " + notes);
        startActivity(Intent.createChooser(intent, "SHARE NOTES"));
    }

    //editing the database
    private void editItem(String title, String notes) {
        dialog = new Dialog(getActivity());
        dialog.setContentView(R.layout.activity_add_notes);
        dialog.show();
        dialogNotesTitle = (EditText) dialog.findViewById(R.id.memo_add_title);
        dialogNotesContent = (EditText) dialog.findViewById(R.id.memo_add_contents);
        dialogUpdateLayout = (LinearLayout) dialog.findViewById(R.id.update_layout);
        dialog_save = (TextView) dialog.findViewById(R.id.update_save);
        dialog_cancel = (TextView) dialog.findViewById(R.id.update_cancel);
        dialogUpdateLayout.setVisibility(View.VISIBLE);
        dialogNotesTitle.setText(title);
        dialogNotesContent.setText(notes);

        dialog_save.setOnClickListener(this);
        dialog_cancel.setOnClickListener(this);
    }

    //onclick listener for dialog save and cancel textviews
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.update_save:
                try {
                    DialogUpdateNote(title);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.update_cancel:
                dialog.dismiss();
                break;
            default:
                break;
        }
        // for datePicker
        if (v == layoutDate) {
            toDatePickerDialog.show();
        }
    }

    // update dialog
    private void DialogUpdateNote(String title) throws IOException {
        notesModule = new NotesModule();
        notesModule.title = dialogNotesTitle.getText().toString();
        notesModule.notes = dialogNotesContent.getText().toString();
        db.updateNotes(notesModule, title);
        mAdapter.notifyItemChanged(position);
        mAdapter.notifyDataSetChanged();
        mAdapter = new MyRecyclerViewAdapter(getDataSet());
        mRecyclerView.setAdapter(mAdapter);
        Toast.makeText(getActivity(), "Successfully Edited", Toast.LENGTH_SHORT).show();
        dialog.dismiss();
    }

    //removing items from the database
    private void removeItem(String title, int position) {
        memoArray.remove(position);
        db.removeNotes(title);
        mAdapter.notifyDataSetChanged();
        Log.i(TAG, "getTitle : " + title);
    }

    //getting datas from database and passing to recyclerview
    public ArrayList getDataSet() throws IOException {
        memoArray.clear();
        db = new DatabaseHelperNotes(folder, getActivity());
        ArrayList<NotesModule> list = db.getNotes();
        for (int i = list.size() - 1; i >= 0; i--) {
            String nTitle = list.get(i).getTitle();
            String nNotes = list.get(i).getNotes();
            notesModule = new NotesModule();
            notesModule.setTitle(nTitle);
            notesModule.setNotes(nNotes);
            memoArray.add(notesModule);
        }
        return memoArray;
    }

    //after NoticesAddActivity is closed, the results are obtained from NoticesAddActivity and corresponding action is taken
    //the recyclerview is repopulated from the updated arraylist
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 0) {
            try {
//                the google cards are repopulated with updated list
                mAdapter = new MyRecyclerViewAdapter(getDataSet());
                mRecyclerView.setAdapter(mAdapter);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
