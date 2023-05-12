package com.example.eyesyhopefyp.Introduction;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.eyesyhopefyp.Dashboard.dashboardActivity;
import com.example.eyesyhopefyp.MyDbHelper;
import com.example.eyesyhopefyp.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignUp extends AppCompatActivity {

    MyDbHelper dbHelper;
    TextToSpeech textToSpeech;
    TextView tBattery;

    Button btn_sign_up_guardian;
    TextInputLayout nameLayout, phoneLayout, emailLayout, name_BlindLayout;
    TextInputEditText name, phone, email,inputBlindName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_sign_up);


        nameLayout = (TextInputLayout) findViewById(R.id.name_Guardian);
        phoneLayout = (TextInputLayout) findViewById(R.id.phoneNo_Guardian);
        emailLayout = (TextInputLayout) findViewById(R.id.email_Guardian);
        name_BlindLayout= (TextInputLayout) findViewById(R.id.name_Blind);

        tBattery = findViewById(R.id.battery_Indication);
        btn_sign_up_guardian = findViewById(R.id.btn_sign_up_guardian);

        phone = findViewById(R.id.phoneNo);
        name = findViewById(R.id.nameInput);
        email = findViewById(R.id.inputEmail);
        inputBlindName = findViewById(R.id.inputBlindName);

        //initialise dphelper
        dbHelper = new MyDbHelper(this);


        checkBattery();
        btn_sign_up_guardian.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("Sign up ", " Clicked");

                String Name = name.getText().toString().trim();
                String Email = email.getText().toString().trim();
                String Phone = phone.getText().toString();
                String BlindPersonName = inputBlindName.getText().toString();

                if (Validate()) {

                    if (validEmail(Email)) {

                        if (validPhoneNumber(Phone)) {
                            long id = dbHelper.insertToUsers(
                                    Name,
                                    Phone,
                                    Email,
                                    BlindPersonName

                            );


                            Toast.makeText(SignUp.this, "Helper Saved Successfully", Toast.LENGTH_SHORT).show();

                            textToSpeech = new TextToSpeech(SignUp.this, new TextToSpeech.OnInitListener() {
                                @Override
                                public void onInit(int status) {
                                    if (status != TextToSpeech.ERROR) {
                                        textToSpeech.speak("Helper Saved Successfully", TextToSpeech.QUEUE_FLUSH, null, null);
                                    }

                                }
                            }, "com.google.android.tts");


                            Intent i = new Intent(SignUp.this, dashboardActivity.class);
                            i.putExtra("name", Name);
                            i.putExtra("email", Email);
                            i.putExtra("phone", Phone);
                            i.putExtra("blind person", BlindPersonName);
                            finish();
                            startActivity(i);
                            phoneLayout.setErrorEnabled(false);

                            Log.e("Data "," All valid Data");
                        } else {
                            phoneLayout.setError("Enter Valid Phone Number");
                        }

                        emailLayout.setErrorEnabled(false);
                    } else {
                        emailLayout.setError("Invalid Email");
                    }


                }

            }
        });


    }

    private boolean validEmail(String email) {
        boolean validEmail;
        //Regular Expression
        String regex = "^(.+)@(.+)$";
        //Compile regular expression to get the pattern
        Pattern pattern = Pattern.compile(regex);
        //Iterate emails array list

        //Create instance of matcher
        Matcher matcher = pattern.matcher(email);
        if (matcher.matches()) {
            validEmail = true;

        } else {
            validEmail = false;
        }
        return validEmail;
    }

    private void checkBattery() {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = getApplicationContext().registerReceiver(null, ifilter);
        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        float batterPCT = level / (float) scale;
        float p = batterPCT * 100;

        tBattery.setText(String.valueOf(p).concat("%"));

    }

    private boolean Validate() {



        int flag = 0;
        if (name.getText().toString().trim().isEmpty()) {
            flag = 1;
            nameLayout.setError("Cannot be Empty");
        } else
            nameLayout.setErrorEnabled(false);

        if (phone.getText().toString().trim().isEmpty()) {
            flag = 1;
            phoneLayout.setError("Cannot be Empty");
        } else
            phoneLayout.setErrorEnabled(false);

        if (email.getText().toString().trim().isEmpty()) {
            flag = 1;
            emailLayout.setError("Cannot be Empty");
        } else
            emailLayout.setErrorEnabled(false);


        if (inputBlindName.getText().toString().trim().isEmpty()) {
            flag = 1;
            name_BlindLayout.setError("Cannot be Empty");
        } else
            name_BlindLayout.setErrorEnabled(false);

        if (flag == 1)
            return false;
        else
            return true;


    }

    private boolean validPhoneNumber(String phone) {

        // Creating a Pattern class object
        Pattern p = Pattern.compile("^\\d{10}$");

        // Pattern class contains matcher() method
        // to find matching between given number
        // and regular expression for which
        // object of Matcher class is created
        Matcher m = p.matcher(phone);
        if (!m.matches()) {
            return false;
        } else {
            return true;
        }
    }


    @Override
    public void onBackPressed() {
       /* Intent i = new Intent(this, Login.class);
        startActivity(i);*/
        // super.onBackPressed();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Do you want to exit?").setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                System.exit(0);
                finish();
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        builder.show();


        super.onBackPressed();
    }


}
