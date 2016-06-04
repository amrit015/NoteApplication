package com.example.notesapplication;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.notesapplication.Utils.Log;

import java.util.ArrayList;

/**
 * This is the adapter for RecyclerView. The adapter must extend its child
 * i.e. RecyclerView.Adapter<MyRecyclerViewAdapter.ItemObjectHolder> and must have a ViewHolder
 * which extends RecyclerView.ViewHolder
 */
//public class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.NoticesHolder>
public class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.NotesHolder> {
    private static String LOG_TAG = "MyRecyclerViewAdapter";
    Context context;
    NotesModule notesModule;
    private ArrayList<NotesModule> mDataset;
    private String title;
    private String notes;
    private int position;
    private int postId;
    private String user;

    public MyRecyclerViewAdapter(ArrayList mDataset) {
        this.mDataset = mDataset;
    }

    @Override
    public NotesHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_view_myrecycler, parent, false);
        context = parent.getContext();
        NotesHolder notesHolder = new NotesHolder(view);
        return notesHolder;
    }

    @Override
    public void onBindViewHolder(final NotesHolder holder, final int position) {
        // binding the views to display the title, notes and user in the recyclerview
        notesModule = mDataset.get(position);
        if (!notesModule.getTitle().equals("") && !notesModule.getNotes().equals("")) {
            holder.noteTitle.setText(notesModule.getTitle());
            holder.noteContents.setText(notesModule.getNotes());
            if (!notesModule.getUser().equals("")) {
                holder.noteUser.setText("Updated by " + notesModule.getUser());
            }
        } else {
            holder.cardView.setVisibility(View.GONE);
        }
        //saving title,notes and position of each cards for contextmenu
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notesModule = mDataset.get(position);
                setTitle(holder.noteTitle.getText().toString());
                setNotes(holder.noteContents.getText().toString());
                setUser(notesModule.getUser());
                setPostId(notesModule.getPostId());
                setPosition(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    // saving and accessing title,notes,position for use with NotesFragment
    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public int getPostId() {
        return postId;
    }

    public void setPostId(int postId) {
        this.postId = postId;
    }

    public class NotesHolder extends RecyclerView.ViewHolder {

        TextView noteTitle;
        TextView noteContents;
        CardView cardView;
        TextView noteUser;

        public NotesHolder(View itemView) {
            super(itemView);
            cardView = (CardView) itemView.findViewById(R.id.card_view);
            noteTitle = (TextView) itemView.findViewById(R.id.note_title);
            noteContents = (TextView) itemView.findViewById(R.id.note_contents);
            noteUser = (TextView) itemView.findViewById(R.id.note_user);
            Log.i(LOG_TAG, "Adding Listener");
        }
    }
}
