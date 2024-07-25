package com.example.filoangler;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class RegisterP2Activity extends AppCompatActivity {

    //Elements
    private EditText txtFirstName, txtLastName;
    private DatePickerDialog datePickerDialog;
    private Button btnDate, btnConfirm;
    private Spinner spnrCity, spnrProvince;
    private RadioGroup radioGroupStatus;
    private RadioButton rbtAspiring, rbtNovice, rbtProficient;
    private TextView lblStatusDesc;
    private HashMap<String, String> ProvincesMap, CitiesMap;
    private List<String> ProvincesList, CitiesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_p2);

        txtFirstName = findViewById(R.id.txtFirstName);
        txtLastName = findViewById(R.id.txtLastName);
        btnConfirm = findViewById(R.id.btnConfirm);
        btnDate = findViewById(R.id.btnDate);
        spnrProvince = findViewById(R.id.spnrProvince);
        spnrCity = findViewById(R.id.spnrCity);
        radioGroupStatus = findViewById(R.id.radioGroupStatus);
        rbtAspiring = findViewById(R.id.rbtAspiring);
        rbtNovice = findViewById(R.id.rbtNovice);
        rbtProficient = findViewById(R.id.rbtProficient);
        lblStatusDesc = findViewById(R.id.lblStatusDesc);

        lblStatusDesc.setText(" ");

        initDatePicker();
        btnDate.setText(getTodaysDate());

        try{
            LoadJSONData();
        }catch(Exception e){
            Log.e("TAG", "Json Error" + e);
        }
        try{
            ProvincesSpinnerContent();
        }catch(Exception e) {
            Log.e("TAG", "Spinner Error" + e);
        }

        btnDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePickerDialog.show();
            }
        });

        radioGroupStatus.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId == R.id.rbtAspiring){
                    lblStatusDesc.setText(getString(R.string.Aspiring_Angler));
                }else if(checkedId == R.id.rbtNovice){
                    lblStatusDesc.setText(getString(R.string.Novice_Angler));
                }else if(checkedId == R.id.rbtProficient){
                    lblStatusDesc.setText(getString(R.string.Proficient_Angler));
                }else{
                    lblStatusDesc.setText(" ");
                }
            }
        });

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginManager loginManager = new LoginManager(RegisterP2Activity.this);

                String Email = getIntent().getStringExtra("Email");
                String Username = getIntent().getStringExtra("Username");
                String Password = getIntent().getStringExtra("Password");
                String FirstName = txtFirstName.getText().toString();
                String LastName = txtLastName.getText().toString();
                String Birthdate = btnDate.getText().toString();
                String ProvinceAddress = spnrProvince.getSelectedItem().toString();
                String CityAddress = spnrCity.getSelectedItem().toString();
                String AnglerStatus = " ";
                int RadioButtonId = radioGroupStatus.getCheckedRadioButtonId();
                if(RadioButtonId != 1){
                    RadioButton SelectedRadioButton = findViewById(RadioButtonId);
                    AnglerStatus = SelectedRadioButton.getText().toString();
                }

                try{
                    if (loginManager.GetCurrentUser() != null && loginManager.GetCurrentUser().isEmailVerified()) {
                        RegisterManager registerManager = new RegisterManager();
                        User user = new User(Email, Password, Username, FirstName, LastName, Birthdate, ProvinceAddress, CityAddress, AnglerStatus);
                        try{
                            registerManager.RegisterUser(user, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> taskAuth) {
                                    if(taskAuth.isSuccessful()){
                                        registerManager.AddUserToDatabase(user, taskAuth);
                                        Toast.makeText(RegisterP2Activity.this, "Registered Successfully!", Toast.LENGTH_SHORT).show();
                                        Utils.ChangeIntent(RegisterP2Activity.this, LoginActivity.class);
                                        finish();
                                    }
                                }
                            });
                        }catch(Exception e){
                            Log.e("TAG", "Error with adding user to db" + e);
                        }
                    }else{
                        Intent intent = new Intent(RegisterP2Activity.this, RegisterP3Activity.class);
                        intent.putExtra("Email", Email);
                        intent.putExtra("Username", Username);
                        intent.putExtra("Password", Password);
                        intent.putExtra("FirstName", FirstName);
                        intent.putExtra("LastName", LastName);
                        intent.putExtra("Birthdate", Birthdate);
                        intent.putExtra("ProvinceAddress", ProvinceAddress);
                        intent.putExtra("CityAddress", CityAddress);
                        intent.putExtra("AnglerStatus", AnglerStatus);
                        startActivity(intent);
                    }
                }catch(Exception e){
                    Log.e("RegisterP2Activity", "Add to database: failure" + e);
                }
            }
        });
    }

    //Handles Date
    private String getTodaysDate() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        month = month + 1;
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        return makeDateString(dayOfMonth, month, year);
    }

    private void initDatePicker(){
        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                month = month + 1;
                String date = makeDateString(dayOfMonth, month, year);
                btnDate.setText(date);
            }
        };

        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        int style = AlertDialog.THEME_HOLO_LIGHT;

        datePickerDialog = new DatePickerDialog(this, style, dateSetListener, year, month, dayOfMonth);
        datePickerDialog.getDatePicker().setMaxDate(calendar.getTimeInMillis());
    }

    private String makeDateString(int dayOfMonth, int month, int year){
        return getMonthFormat(month) + " " + dayOfMonth + " " + year;
    }

    private String getMonthFormat(int month){
        switch(month) {
            case 1:
                return "JAN";
            case 2:
                return "FEB";
            case 3:
                return "MAR";
            case 4:
                return "APR";
            case 5:
                return "MAY";
            case 6:
                return "JUN";
            case 7:
                return "JUL";
            case 8:
                return "AUG";
            case 9:
                return "SEP";
            case 10:
                return "OCT";
            case 11:
                return "NOV";
            case 12:
                return "DEC";
            default:
                return "Invalid month";
        }
    }
    //End of date handling

    //Handles Json files
    private void LoadJSONData(){
        //Fetch json data (cities and provinces)
        String ProvinceName, ProvinceKey, CityName, CityKey;
        ProvincesMap = new HashMap<>();
        CitiesMap = new HashMap<>();
        ProvincesList = new ArrayList<>();

        try{
            //Loads provinces
            AssetManager assetManager = getAssets();
            InputStream imsProvinces = assetManager.open("provinces.json");
            int size = imsProvinces.available();
            byte[] buffer = new byte[size];
            imsProvinces.read(buffer);
            imsProvinces.close();
            String ProvincesJSON = new String(buffer, StandardCharsets.UTF_8);

            JSONArray ProvincesArray = new JSONArray(ProvincesJSON);
            for (int i = 0; i < ProvincesArray.length(); i++){

                JSONObject jsonObject = ProvincesArray.getJSONObject(i);
                ProvinceName = jsonObject.getString("name");
                ProvinceKey = jsonObject.getString("key");

                ProvincesMap.put(ProvinceName, ProvinceKey);
                ProvincesList.add(ProvinceName);
            }
        }catch(Exception e){
            Log.e("TAG", "LoadJson Provinces: Error" + e);
        }

        try{
            //Loads cities
            AssetManager assetManager = getAssets();
            InputStream imsCities = assetManager.open("cities.json");
            int size = imsCities.available();
            byte[] buffer = new byte[size];
            imsCities.read(buffer);
            imsCities.close();
            String CitiesJSON = new String(buffer, StandardCharsets.UTF_8);

            JSONArray CitiesArray = new JSONArray(CitiesJSON);
            for (int i = 0; i < CitiesArray.length(); i++){

                JSONObject jsonObject = CitiesArray.getJSONObject(i);
                CityName = jsonObject.getString("name");
                CityKey = jsonObject.getString("province");

                CitiesMap.put(CityName, CityKey);
            }
        }catch(Exception e){
            Log.e("TAG", "LoadJson Cities: Error" + e);
        }
    }
    //End

    //Handles spinner content
    private void ProvincesSpinnerContent(){
        Collections.sort(ProvincesList);
        ArrayAdapter<String> ProvinceAdapter = new ArrayAdapter<>(this, R.layout.spinner_selected_item, ProvincesList);
        ProvinceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnrProvince.setAdapter(ProvinceAdapter);

        spnrProvince.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String SelectedProvince = ProvincesList.get(position);
                CitiesSpinnerContent(SelectedProvince);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void CitiesSpinnerContent(String SelectedProvince){
        CitiesList = new ArrayList<>();

        String ProvinceKey = ProvincesMap.get(SelectedProvince);
        for(Map.Entry<String, String> entry : CitiesMap.entrySet()){
            if(entry.getValue().equals(ProvinceKey)){
                CitiesList.add(entry.getKey());
            }
        }

        Collections.sort(CitiesList);
        ArrayAdapter<String> CityAdapter = new ArrayAdapter<>(this, R.layout.spinner_selected_item, CitiesList);
        CityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnrCity.setAdapter(CityAdapter);
    }
    //End
}