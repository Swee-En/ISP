package com.example.androidapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidapp.Model.Data;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;

// FirebaseRecyclerAdapter is a class provided by
// FirebaseUI. it provides functions to bind, adapt and show
// database contents in a Recycler View
public class IncomeAdapter extends FirebaseRecyclerAdapter<
        Data, IncomeAdapter.incomeViewholder> {

    public IncomeAdapter(
            @NonNull FirebaseRecyclerOptions<Data> options)
    {
        super(options);
    }

    // Function to bind the view in Card view(here
    // "person.xml") iwth data in
    // model class(here "person.class")
    @Override
    protected void
    onBindViewHolder(@NonNull incomeViewholder holder,
                     int position, @NonNull Data model)
    {

        holder.setAmmount(model.getAmount());
        holder.setType(model.getType());
        holder.setNote(model.getNote());
        holder.setDate(model.getDate());
    }

    // Function to tell the class about the Card view (here
    // "person.xml")in
    // which the data will be shown
    @NonNull
    @Override
    public incomeViewholder
    onCreateViewHolder(@NonNull ViewGroup parent,
                       int viewType)
    {
        View view
                = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_income, parent, false);
        return new IncomeAdapter.incomeViewholder(view);
    }

    // Sub Class to create references of the views in Crad
    // view (here "person.xml")
    class incomeViewholder
            extends RecyclerView.ViewHolder {

        View mView;
        public incomeViewholder(@NonNull View itemView)
        {
            super(itemView);
            mView = itemView;
        }

        void setType(String type) {
            TextView mType = itemView.findViewById(R.id.type_txt_income);
            mType.setText(type);
        }

        void setNote(String note) {

            TextView mNote = itemView.findViewById(R.id.note_txt_income);
            mNote.setText(note);
        }

        void setDate(String date) {
            TextView mDate = itemView.findViewById(R.id.date_txt_income);
            mDate.setText(date);
        }

        void setAmmount(int ammount) {
            TextView mAmmount = itemView.findViewById(R.id.ammount_txt_income);
            String stammount = String.valueOf(ammount);
            mAmmount.setText(stammount);
        }
    }
}


