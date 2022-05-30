package com.example.androidapp;

import android.app.AlertDialog;
import android.icu.util.ULocale;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.example.androidapp.Model.Data;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerAdapter_LifecycleAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.Date;


/*
 * A simple {@link Fragment} subclass.
 * Use the {@link ExpenseFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Expense extends Fragment {

    //Firebase DB
    private FirebaseAuth mAuth;
    private DatabaseReference mExpenseDatabase;

    //RecyclerView
    private RecyclerView recyclerView;
    private FirebaseRecyclerAdapter adapter;

    private TextView expenseTotal;

    //Update expense
    private EditText editAmount;
    private EditText editType;
    private EditText editNote;

    private Button btnUpdate;
    private Button btnDelete;

    private String type;
    private String note;
    private int amount;

    private String post_key;


    /**   // TODO: Rename parameter arguments, choose names that match
     // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
     private static final String ARG_PARAM1 = "param1";
     private static final String ARG_PARAM2 = "param2";
     // TODO: Rename and change types of parameters
     private String mParam1;
     private String mParam2;
     public ExpenseFragment() {
     // Required empty public constructor
     }
     /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ExpenseFragment.
     */
    // TODO: Rename and change types and number of parameters

    /**
     * public static ExpenseFragment newInstance(String param1, String param2) {
     * ExpenseFragment fragment = new ExpenseFragment();
     * Bundle args = new Bundle();
     * args.putString(ARG_PARAM1, param1);
     * args.putString(ARG_PARAM2, param2);
     * fragment.setArguments(args);
     * return fragment;
     * }
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /**if (getArguments() != null) {
         //mParam1 = getArguments().getString(ARG_PARAM1);
         //mParam2 = getArguments().getString(ARG_PARAM2);
         }*/
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View myview = inflater.inflate(R.layout.fragment_expense, container, false);

        mAuth = FirebaseAuth.getInstance();

        FirebaseUser mUser = mAuth.getCurrentUser();
        String uid = mUser.getUid();

        mExpenseDatabase = FirebaseDatabase.getInstance().getReference().child("ExpenseData").child(uid);

        expenseTotal = myview.findViewById(R.id.expense_txt);

        recyclerView = myview.findViewById(R.id.recycler_id_expense);


        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());

        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);

        mExpenseDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                int expenseSum = 0;

                for (DataSnapshot mysnapshot:snapshot.getChildren()){

                    Data data = mysnapshot.getValue(Data.class);

                    expenseSum += data.getAmount();

                    String stexpenseSum = String.valueOf(expenseSum);

                    expenseTotal.setText(stexpenseSum);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        FirebaseRecyclerOptions<Data> options = new FirebaseRecyclerOptions.Builder<Data>()
                .setQuery(mExpenseDatabase, Data.class)
                .build();

        adapter = new FirebaseRecyclerAdapter<Data, ExpenseViewHolder>(options) {

            public ExpenseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                return new ExpenseViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.expense_recycler, parent, false));
            }

            protected void onBindViewHolder(ExpenseViewHolder holder, final int position, @NonNull final Data model) {
                holder.setAmmount(model.getAmount());
                holder.setType(model.getType());
                holder.setNote(model.getNote());
                holder.setDate(model.getDate());

                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        post_key = getRef(position).getKey();

                        type = model.getType();
                        note = model.getNote();
                        amount = model.getAmount();

                        UpdateData();
                    }
                });
            }
        };
        recyclerView.setAdapter(adapter);

        return myview;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Data> options = new FirebaseRecyclerOptions.Builder<Data>()
                .setQuery(mExpenseDatabase, Data.class)
                .build();

        adapter = new FirebaseRecyclerAdapter<Data, ExpenseViewHolder>(options) {

            public ExpenseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                return new ExpenseViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.expense_recycler, parent, false));
            }

            protected void onBindViewHolder(ExpenseViewHolder holder, int position, @NonNull Data model) {
                holder.setAmmount(model.getAmount());
                holder.setType(model.getType());
                holder.setNote(model.getNote());
                holder.setDate(model.getDate());
            }
        };
        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    public static class ExpenseViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }

        void setType(String type) {
            TextView mType = mView.findViewById(R.id.expense_type);
            mType.setText(type);
        }

        void setNote(String note) {

            TextView mNote = mView.findViewById(R.id.expense_note);
            mNote.setText(note);
        }

        void setDate(String date) {
            TextView mDate = mView.findViewById(R.id.expense_date);
            mDate.setText(date);
        }

        void setAmmount(int ammount) {
            TextView mAmmount = mView.findViewById(R.id.expense_amount);
            String stammount = String.valueOf(ammount);
            mAmmount.setText(stammount);
        }

    }

    private void UpdateData(){

        AlertDialog.Builder mydialog = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = LayoutInflater.from(getActivity());

        View myview = inflater.inflate(R.layout.update_data, null);

        mydialog.setView(myview);

        editAmount = myview.findViewById(R.id.amount_edit);
        editType = myview.findViewById(R.id.type_edit);
        editNote = myview.findViewById(R.id.note_edit);

        // Set data to edittext

        editType.setText(type);
        editType.setSelection(type.length());

        editNote.setText(note);
        editNote.setSelection(note.length());

        editAmount.setText(String.valueOf(amount));
        editAmount.setSelection(String.valueOf(amount).length());

        btnUpdate = myview.findViewById(R.id.btn_update);
        btnDelete = myview.findViewById(R.id.btn_delete);

        final AlertDialog dialog = mydialog.create();

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                type = editType.getText().toString().trim();
                note = editNote.getText().toString().trim();

                String mdamount = String.valueOf(amount);
                mdamount = editAmount.getText().toString().trim();

                int newAmount = Integer.parseInt(mdamount);

                String mDate = DateFormat.getDateInstance().format(new Date());

                Data data = new Data(newAmount, type, note, post_key, mDate);

                mExpenseDatabase.child(post_key).setValue(data);

                dialog.dismiss();

            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mExpenseDatabase.child(post_key).removeValue();
                dialog.dismiss();

            }
        });

        dialog.show();

    }

}


