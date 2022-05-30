package com.example.androidapp;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.text.Layout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.androidapp.Model.Data;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.eazegraph.lib.charts.PieChart;
import org.eazegraph.lib.models.PieModel;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Dashboard#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Dashboard extends Fragment {


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    TextView tvFood, tvShopping, tvTransport, tvOthers;
    PieChart pieChart;

    public Dashboard() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Dashboard.
     */
    // TODO: Rename and change types and number of parameters
    public static Dashboard newInstance(String param1, String param2) {
        Dashboard fragment = new Dashboard();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String CAMERA_PERMISSION = android.Manifest.permission.CAMERA;
        ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.CAMERA);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    // Floating button

    private FloatingActionButton fab_main_btn;
    private FloatingActionButton fab_income_btn;
    private FloatingActionButton fab_expense_btn;
    private Button takepic_btn;

    // Floating button text

    private TextView fab_income_text;
    private TextView fab_expense_text;
    private TextView textView;
    private TextView showPrice;

    //imageView
    private ImageView imageView;

    //boolean
    private boolean isOpen = false;

    // Dashboard income and expense

    private TextView totalIncome;
    private TextView totalExpense;

    // Recyclerview

    private RecyclerView incomeRecycler;
    private RecyclerView expenseRecycler ;

    //animation
    private Animation fadeOpen, fadeClose;

    //Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference mIncomeDatabase;
    private DatabaseReference mExpenseDatabase;

    int expenseFood;
    int expenseTransport;
    int expenseShopping;
    int expenseOthers;

    public String totalPrice;

    String[] categories = { "Food", "Transport", "Shopping", "Others"};

    public static boolean isNumeric(String strNum){
        try {
            double d = Double.parseDouble(strNum);
        }
        catch (NumberFormatException nfe){
            return false;
        }
        return true;
    }

    @Override
    public void onActivityResult (int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Bundle bundle = data.getExtras();
        //from bundle, extract the image
        Bitmap bitmap = (Bitmap) bundle.get("data");

        //set image in imageview

        //process the image to extract the text
        // 1. create a FirebaseVisionImage object from a Bitmap object
        FirebaseVisionImage firebaseVisionImage = FirebaseVisionImage.fromBitmap(bitmap);

        //2. Get instance of FirebaseVision
        FirebaseVision firebaseVision = FirebaseVision.getInstance();

        //3. Create instance of FirebaseVisionTextRecognizer
        FirebaseVisionTextRecognizer firebaseVisionTextRecognizer = firebaseVision.getOnDeviceTextRecognizer();

        //4. Create a task to process image
        Task<FirebaseVisionText> task = firebaseVisionTextRecognizer.processImage(firebaseVisionImage);

        //5. if task is successful
        task.addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
            @Override
            public void onSuccess(FirebaseVisionText firebaseVisionText) {
                String s = "";
                totalPrice = "";
                ArrayList<String> words = new ArrayList<>();

                s = firebaseVisionText.getText();
                String lowerCase = s.toLowerCase();
                String[] splitWords = lowerCase.split("[ \\r?\\n]");

                for (int n = 0; n<splitWords.length; n++){
                    words.add(splitWords[n]);
                }

                if (words.contains("total")){
                    int index = words.indexOf("total");

                    for (int n = index+1; n<words.size(); n++){
                        if(totalPrice==""){
                            for (int m = 0; m<words.get(n).length(); m++){
                                if (isNumeric(words.get(n).substring(m))==true){
                                    totalPrice = words.get(n).substring(m);
                                    break;
                                }
                            }
                        }
                        else if(totalPrice!=""){
                            break;
                        }
                    }


                    expenseTransaction(totalPrice);

                }

                else{
                    showPrice.setText("There is no total price to compute.");
                }

                totalPrice = "";

            }
        });

        //6. if task failed
        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View myview = inflater.inflate(R.layout.fragment_dashboard, container, false);
        View xview = inflater.inflate(R.layout.custom_layout_insertdata, container, false);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser mUser = mAuth.getCurrentUser();
        String uid = mUser.getUid();

        mIncomeDatabase = FirebaseDatabase.getInstance().getReference().child("IncomeData").child(uid);
        mExpenseDatabase = FirebaseDatabase.getInstance().getReference().child("ExpenseData").child(uid);

        mExpenseDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int expenseSum = 0;

                for (DataSnapshot mysnapshot:snapshot.getChildren()){

                    Data data = mysnapshot.getValue(Data.class);

                    expenseSum += data.getAmount();

                    String stincomeSum = String.valueOf(expenseSum);
                    totalExpense.setText(stincomeSum);

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        mIncomeDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                int incomeSum = 0;

                for (DataSnapshot mysnapshot:snapshot.getChildren()){

                    Data data = mysnapshot.getValue(Data.class);

                    incomeSum += data.getAmount();

                    String strincomeSum = String.valueOf(incomeSum);
                    totalIncome.setText(strincomeSum);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //Connect floating button

        fab_main_btn = myview.findViewById(R.id.main_plus_btn);
        fab_income_btn = myview.findViewById(R.id.income_ft_button);
        fab_expense_btn = myview.findViewById(R.id.expense_ft_button);
        takepic_btn = myview.findViewById(R.id.takepic);

        //Connect floating text

        fab_income_text = myview.findViewById(R.id.income_ft_text);
        fab_expense_text = myview.findViewById(R.id.expense_ft_text);
        textView = myview.findViewById(R.id.textID);
        showPrice = myview.findViewById(R.id.showAmount);

        //imageView
        imageView = myview.findViewById(R.id.imageID);

        // total income expense

        totalIncome = myview.findViewById(R.id.income_result);
        totalExpense = myview.findViewById(R.id.expense_result);

        // Spinner



        // Recycler




        //Connect animation

        fadeOpen = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_open);
        fadeClose = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_close);

        takepic_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePic(v);
                System.out.println("hello");
            }
        });

        fab_main_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                addTransaction();

                if (isOpen) {
                    fab_income_btn.startAnimation(fadeClose);
                    fab_expense_btn.startAnimation(fadeClose);
                    fab_income_btn.setClickable(false);
                    fab_expense_btn.setClickable(false);

                    fab_income_text.startAnimation(fadeClose);
                    fab_expense_text.startAnimation(fadeClose);
                    fab_income_text.setClickable(false);
                    fab_expense_text.setClickable(false);
                    isOpen = false;
                }
                else {
                    fab_income_btn.startAnimation(fadeOpen);
                    fab_expense_btn.startAnimation(fadeOpen);
                    fab_income_btn.setClickable(true);
                    fab_expense_btn.setClickable(true);

                    fab_income_text.startAnimation(fadeOpen);
                    fab_expense_text.startAnimation(fadeOpen);
                    fab_income_text.setClickable(true);
                    fab_expense_text.setClickable(true);
                    isOpen = true;
                }
            }
        });

        // Calculate totals
        //add here



        // Recycler
        //add here

        // Link those objects with their
        // respective id's that
        // we have given in .XML file
        tvFood = myview.findViewById(R.id.tvFood);
        tvShopping = myview.findViewById(R.id.tvShopping);
        tvTransport = myview.findViewById(R.id.tvTransport);
        tvOthers = myview.findViewById(R.id.tvOthers);
        pieChart = myview.findViewById(R.id.piechart);

        // Creating a method setData()
        // to set the text in text view and pie chart
        setData();

        return myview;
    }

    //Floating button animation

    private void btnAnimation() {
        if (isOpen) {
            fab_income_btn.startAnimation(fadeClose);
            fab_expense_btn.startAnimation(fadeClose);
            fab_income_btn.setClickable(false);
            fab_expense_btn.setClickable(false);

            fab_income_text.startAnimation(fadeClose);
            fab_expense_text.startAnimation(fadeClose);
            fab_income_text.setClickable(false);
            fab_expense_text.setClickable(false);
            isOpen = false;
        }
        else {
            fab_income_btn.startAnimation(fadeOpen);
            fab_expense_btn.startAnimation(fadeOpen);
            fab_income_btn.setClickable(true);
            fab_expense_btn.setClickable(true);

            fab_income_text.startAnimation(fadeOpen);
            fab_expense_text.startAnimation(fadeOpen);
            fab_income_text.setClickable(true);
            fab_expense_text.setClickable(true);
            isOpen = true;
        }
    }

    private void addTransaction() {

        // Fab income button

        fab_income_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                incomeTransaction();
            }
        });

        fab_expense_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                expenseTransaction("0");
            }
        });



    }

    public void incomeTransaction(){
        AlertDialog.Builder mydialog=new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = LayoutInflater.from(getActivity());

        View myview = inflater.inflate(R.layout.custom_layout_insertdata_income, null);
        mydialog.setView(myview);

        final AlertDialog dialog = mydialog.create();

        dialog.setCancelable(false);

        final EditText amount_input = myview.findViewById(R.id.amount_text);
        final Spinner type_input = myview.findViewById(R.id.type_text);
        final EditText note_input = myview.findViewById(R.id.note_text);

        Spinner spinner = (Spinner) myview.findViewById(R.id.type_text);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(myview.getContext(), R.array.categories,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id)
            {
                parentView.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }
        });


        Button saveBtn = myview.findViewById(R.id.btn_save);
        Button cancelBtn = myview.findViewById(R.id.btn_cancel);

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String type = type_input.getSelectedItem().toString().trim();
                String amount = amount_input.getText().toString().trim();
                String note = note_input.getText().toString().trim();

                if (TextUtils.isEmpty(type)){
                    ((TextView)type_input.getSelectedView()).setError("Required Field");
                    return;
                }
                if (TextUtils.isEmpty(amount)){
                    amount_input.setError("Required Field");
                    return;
                }
                int amount_int = Integer.parseInt(amount);
                if (TextUtils.isEmpty(note)){
                    note_input.setError("Required Field");
                    return;
                }

                String id =  mIncomeDatabase.push().getKey();
                String mDate = DateFormat.getDateInstance().format(new Date());

                Data data = new Data(amount_int, type, note, id, mDate);

                mIncomeDatabase.child(id).setValue(data);

                Toast.makeText(getActivity(), "Data Added", Toast.LENGTH_SHORT).show();

                btnAnimation();
                dialog.dismiss();

            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }

        });

        btnAnimation();
        dialog.show();

    }

    public void expenseTransaction(final String amt){
        AlertDialog.Builder mydialog=new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = LayoutInflater.from(getActivity());

        View myview = inflater.inflate(R.layout.custom_layout_insertdata, null);
        mydialog.setView(myview);

        final AlertDialog dialog = mydialog.create();

        dialog.setCancelable(false);



        final EditText amount_input = myview.findViewById(R.id.amount_text);
        final Spinner type_input = myview.findViewById(R.id.type_text);
        final EditText note_input = myview.findViewById(R.id.note_text);

        if (Integer.parseInt(amt) != 0){
            amount_input.setText(amt);
        }

        Spinner spinner = (Spinner) myview.findViewById(R.id.type_text);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(myview.getContext(), R.array.categories,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id)
            {
                parentView.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }
        });

        Button saveBtn = myview.findViewById(R.id.btn_save);
        Button cancelBtn = myview.findViewById(R.id.btn_cancel);
//        Button picBtn = myview.findViewById(R.id.picBtn);

//        picBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                takePic(v);
//            }
//        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String type = type_input.getSelectedItem().toString().trim();
                String amount;
                if (Integer.parseInt(amt) == 0){
                     amount = amount_input.getText().toString().trim();
                }
                else {
                     amount = amt;
                }
                String note = note_input.getText().toString().trim();

                if (TextUtils.isEmpty(type)){
                    ((TextView)type_input.getSelectedView()).setError("Required Field");
                    return;
                }
                if (TextUtils.isEmpty(amount)){
                    amount_input.setError("Required Field");
                    return;
                }
                int amount_int = Integer.parseInt(amount);
                if (TextUtils.isEmpty(note)){
                    note_input.setError("Required Field");
                    return;
                }

                String id =  mExpenseDatabase.push().getKey();
                String mDate = DateFormat.getDateInstance().format(new Date());

                Data data = new Data(amount_int, type, note, id, mDate);

                mExpenseDatabase.child(id).setValue(data);

                Toast.makeText(getActivity(), "Data Added", Toast.LENGTH_SHORT).show();

                btnAnimation();
                dialog.dismiss();

            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnAnimation();
                dialog.dismiss();
            }

        });
        dialog.show();

    }

    private void setData()
    {
        // TODO: calculate percentage based on the amount.

        mExpenseDatabase.orderByChild("type").equalTo("Food").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                expenseFood = 0;
                for (DataSnapshot mysnapshot:snapshot.getChildren()){
                    Data data = mysnapshot.getValue(Data.class);
                    expenseFood += data.getAmount();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        mExpenseDatabase.orderByChild("type").equalTo("Transport").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                expenseTransport = 0;
                for (DataSnapshot mysnapshot:snapshot.getChildren()){
                    Data data = mysnapshot.getValue(Data.class);
                    expenseTransport += data.getAmount();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        mExpenseDatabase.orderByChild("type").equalTo("Shopping").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                expenseShopping = 0;
                for (DataSnapshot mysnapshot:snapshot.getChildren()){
                    Data data = mysnapshot.getValue(Data.class);
                    expenseShopping += data.getAmount();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        mExpenseDatabase.orderByChild("type").equalTo("Other").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                expenseOthers = 0;
                for (DataSnapshot mysnapshot:snapshot.getChildren()){
                    Data data = mysnapshot.getValue(Data.class);
                    expenseOthers += data.getAmount();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



        // Set the percentage of language used
        tvFood.setText(Integer.toString(expenseFood));
        tvShopping.setText(Integer.toString(expenseShopping));
        tvTransport.setText(Integer.toString(expenseTransport));
        tvOthers.setText(Integer.toString(expenseOthers));

        // Set the data and color to the pie chart
        pieChart.addPieSlice(
                new PieModel(
                        "Food",
                        Integer.parseInt(tvFood.getText().toString()),
                        Color.parseColor("#FFA726")));
        pieChart.addPieSlice(
                new PieModel(
                        "Groceries",
                        Integer.parseInt(tvShopping.getText().toString()),
                        Color.parseColor("#66BB6A")));
        pieChart.addPieSlice(
                new PieModel(
                        "Shopping",
                        Integer.parseInt(tvTransport.getText().toString()),
                        Color.parseColor("#EF5350")));
        pieChart.addPieSlice(
                new PieModel(
                        "Others",
                        Integer.parseInt(tvOthers.getText().toString()),
                        Color.parseColor("#29B6F6")));

        // To animate the pie chart
        pieChart.startAnimation();
    }
    public void takePic(View view) {
        //open camera => create intent object
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, 101);
    }



}