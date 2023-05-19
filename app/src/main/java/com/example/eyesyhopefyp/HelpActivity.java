package com.example.eyesyhopefyp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;

import com.example.eyesyhopefyp.Dashboard.dashboardActivity;
import com.example.eyesyhopefyp.Receivers.BatteryReceiver;
import com.example.eyesyhopefyp.Receivers.NetworkStatus;
import com.example.eyesyhopefyp.Utility.IntroductionMessageHelper;
import com.example.eyesyhopefyp.Utility.Voice;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

import java.util.ArrayList;

import io.paperdb.Paper;

public class HelpActivity extends AppCompatActivity {

    String Name;
    String Email;
    String Phone;
    String blindPersonName;
    TextView txtHelperName, txtHelperphone, txtHelperemail;
    double latitude;
    double longitude;

    private static final int REQ_CODE_DASHBOARD_RESULT = 100;
    private static final int requestCode = 100;
    CardView call, email;
    Button assistant;
    private int swipeStep = 0;
    private final static int swipesNumber = 2;
    private int swipeStepDetails = 0;
    private final static int swipesDetailNumber = 4;
    private SwiperListener swiperListener;
    public int click = 0;
    TextView battery;
    BatteryReceiver mBattery;
    Vibrator vibrator;
    NetworkStatus networkStatus;
    Intent intent;


    //region Common Functions Parameters
    String city, country, postal, province;
    FusedLocationProviderClient fusedLocationProviderClient;
    Translator englishUrduTranslator;
    double longt, latt;
    LocationManager locationManager;
    ArrayList<UserModel> users = new ArrayList<>();
    MyDbHelper myDbHelper;
    private FusedLocationProviderClient fusedLocationClient;
    //endregion

    IntroductionMessageHelper introductionMessageHelper;
    private String Phone1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        txtHelperName = findViewById(R.id.txtHelperName);
        txtHelperphone = findViewById(R.id.txtHelperphone);
        txtHelperemail = findViewById(R.id.txtHelperemail);

        myDbHelper = new MyDbHelper(this);
        users = myDbHelper.getAllUSERS();
        latitude = -20.0866183;
        longitude = 30.8160517;

        Name = users.get(0).getName();

        Phone1 = users.get(0).getPhone();
        String newFirstChar = "+263";
        Phone = newFirstChar + Phone1.substring(1);

        Email = users.get(0).getEmail();
        blindPersonName = users.get(0).getBlindPersonName();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        Log.e("Helper ", "Name = " + Name + "\nEmail = " + Email + " \nPhone = " + Phone + " \nBlind person name = " + blindPersonName);

        txtHelperName.setText(Name);
        txtHelperphone.setText(Phone);
        txtHelperemail.setText(Email);


        Paper.init(this);
        Voice.init(HelpActivity.this);
        initWidget();
        //firebase language setup
        languageSetup();
        //Services
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        //Battery Receiver
        mBattery = new BatteryReceiver(battery, getApplicationContext());
        registerReceiver(mBattery, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        //Check Internet Connection
        networkStatus = new NetworkStatus(HelpActivity.this);
        //Intro
        introductionMessageHelper = new IntroductionMessageHelper(this, this);
        introductionMessageHelper.introductionMessageForHelpDashboard(this);

        swiperListener = new SwiperListener(assistant);
        location_service();


    }

    private void languageSetup() {
        // Create an English-Urdu translator:
        TranslatorOptions options =
                new TranslatorOptions.Builder()
                        .setSourceLanguage(TranslateLanguage.ENGLISH)
                        .setTargetLanguage(TranslateLanguage.URDU)
                        .build();
        englishUrduTranslator = Translation.getClient(options);


        dictionary_model();


    }

    private void dictionary_model() {
        //Installation of Firebase Model Urdu
        DownloadConditions conditions = new DownloadConditions.Builder()
                .requireWifi()
                .build();
        englishUrduTranslator.downloadModelIfNeeded(conditions)
                .addOnSuccessListener(
                        new OnSuccessListener() {
                            @Override
                            public void onSuccess(Object o) {

                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Model couldn’t be downloaded or other internal error.
                                // ...
                            }
                        });
    }

    private void location_service() {

        try {
            locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5, (LocationListener) HelpActivity.this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void initWidget() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(HelpActivity.this);


        call = findViewById(R.id.call);
        email = findViewById(R.id.email);


        assistant = findViewById(R.id.btn_Assist);
        battery = findViewById(R.id.battery_Indication);


        resetCardViewColors();
    }

    private void resetCardViewColors() {
        email.setCardBackgroundColor(getResources().getColor(R.color.white));
        call.setCardBackgroundColor(getResources().getColor(R.color.white));
    }

    //Function To Check Either Words exist in Statement
    public static boolean containsWords(String inputString, String[] items) {
        boolean found = true;
        for (String item : items) {
            if (!inputString.contains(item)) {
                found = false;
                break;
            }
        }
        Log.i("app", String.valueOf(found));
        return found;
    }
    //End Of Function


    @Override
    public void onDestroy() {
        super.onDestroy();
        Voice.release();
        unregisterReceiver(mBattery);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Voice.release();
    }


    public void speakAtFirst() {

        // Voice.speak(HelpActivity.this, String.valueOf(R.string.welcomeToHelpIntro), false);
        Voice.speak(HelpActivity.this, getResources().getString(R.string.welcomeToHelpIntro), false);
    }

    public void speakAtwelcome() {
        // Voice.speak(HelpActivity.this, String.valueOf(R.string.welcomeToHelp), false);
        Voice.speak(HelpActivity.this, getResources().getString(R.string.welcomeToHelp), false);
    }

    private class SwiperListener implements View.OnTouchListener {

        GestureDetector gestureDetector;

        //Making Constructor
        public SwiperListener(View view) {
            //Required Variables init
            int threshold = 100;
            int velocity_threshold = 100;

            //Init Simple Gesture Listener

            GestureDetector.SimpleOnGestureListener listener = new GestureDetector.SimpleOnGestureListener() {

                @Override
                public boolean onDown(MotionEvent e) {
                    doubleTapToOpenModule();
                    return false;
                }

                @Override
                public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                    float xDiff = e2.getX() - e1.getX();
                    float yDiff = e2.getY() - e1.getY();

                    try {
                        //Checking conditions
                        if (Math.abs(xDiff) > Math.abs(yDiff)) {
                            //Checking Conditions
                            if (Math.abs(xDiff) > threshold && Math.abs(velocityX) > velocity_threshold) {
                                //When X differ is greater than threshold and X velocity is greater than velocity threshold
                                if (xDiff < 0) {
                                    swipeStep = ((swipeStep - 1) % swipesNumber + swipesNumber) % swipesNumber; // to handle Negative value
                                } else {
                                    swipeStep = (swipeStep + 1) % swipesNumber;
                                }
                                switch (swipeStep) {
                                    case 0:
                                        Voice.speak(HelpActivity.this, getString(R.string.swipe_call_assistant), false);
                                        call.setCardBackgroundColor(getResources().getColor(R.color.cardBackground));
                                        email.setCardBackgroundColor(getResources().getColor(R.color.white));
                                        break;
                                    case 1:
                                        Voice.speak(HelpActivity.this, getString(R.string.swipe_send_email), false);
                                        call.setCardBackgroundColor(getResources().getColor(R.color.white));
                                        email.setCardBackgroundColor(getResources().getColor(R.color.cardBackground));
                                        break;

                                    default:
                                        break;
                                }
                            }
                        } else {
                            if (Math.abs(yDiff) > threshold && Math.abs(velocityY) > velocity_threshold) {
                                //When X differ is greater than threshold and X velocity is greater than velocity threshold
                                if (yDiff > 0) {
                                    resetCardViewColors();
                                    speakAtFirst();

                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return false;
                }
            };
            gestureDetector = new GestureDetector(listener);
            view.setOnTouchListener(this);

        }

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            //return Gesture event
            return gestureDetector.onTouchEvent(motionEvent);
        }


    }

    public void doubleTapToOpenModule() {

        assistant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Normal Single Click Button will be done
                click++;
                Handler handler = new Handler();
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        click = 0;
                    }
                };
                if (click == 1) {
                    handler.postDelayed(runnable, 400);
                } else if (click == 2) {
                    callActivityOnDoubleTap();
                } else if (click == 3) {

                    click = 0;
                }
            }
        });

        assistant.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                resetCardViewColors();
                Intent in = new Intent(HelpActivity.this, dashboardActivity.class); //Manipulating it and sending after splash to dashboardintroductoryActivity
                // Intent in = new Intent(splashScreen.this, introductoryActivity.class); //Manipulating it and sending after splash to dashboardintroductoryActivity
                in.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(in);
                finish();
                return true;
            }
        });
    }

    private void callActivityOnDoubleTap() {
        switch (swipeStep) {
            case 0:
                // Voice.speak(dashboardActivity.this, "OCR IS ABOUT TO START", false);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        resetCardViewColors();
                        Voice.speak(HelpActivity.this, "Calling" + Name + " Please wait", false);

                        Intent intent = new Intent(Intent.ACTION_CALL);
                        intent.setData(Uri.parse("tel:" + Phone));

                        if (ActivityCompat.checkSelfPermission(HelpActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                            // Request permission to make phone calls
                            ActivityCompat.requestPermissions(HelpActivity.this, new String[]{Manifest.permission.CALL_PHONE}, 1);
                        } else {
                            // Permission already granted, make the phone call
                            startActivity(intent);
                        }
                    }
                }, 2000);
                break;
            case 1:
                resetCardViewColors();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        sendEmail();
                    }
                }, 2000);
                break;


            default:

                break;
        }
    }


    private void sendEmail() {


        //From email : eyesyhope7@gmail.com
        // key : byljuxduyfrpzzph


        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        Network network = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            network = connectivityManager.getActiveNetwork();

            if (network != null) {
                NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(network);
                if (networkCapabilities != null && (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))) {
                    // There is an active internet connection

                    Sendmail();
                } else {
                    // There is no active internet connection
                    connectToNetwork();
                }
            } else {
                // There is no active internet connection
                connectToNetwork();
            }
        } else {


            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

            if (networkInfo != null && networkInfo.isConnected()) {
                // There is an active internet connection
                Sendmail();
            } else {
                // There is no active internet connection
                connectToNetwork();

            }
        }
    }

    private void connectToNetwork() {
        Log.e("Network : ", "OFF");
        Voice.speak(HelpActivity.this, "your phone does not have internet connection", false);
    }

    private void Sendmail() {
        Voice.speak(HelpActivity.this, "Sending email , please wait", false);
        // First, check if location permissions are granted

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                            Log.e("Location", "Latitude: " + latitude + ", Longitude: " + longitude);
                        } else {
                            latitude = -20.0866183;
                            longitude = 30.8160517;

                        }
                    }
                });

        Log.e("Network : ", "ON");

        // To send the email, create a new instance of the com.example.eyesyhopefyp.SendEmailTask class and execute it
        SendEmailTask sendEmailTask = new SendEmailTask(
                Email,
                "Emergency Pick Up",
                "Hie " + Name + "\n Its " + blindPersonName + ", can you please  come and collect me i am lost \n\nMy current location is" + "\nhttps://maps.google.com/maps?daddr=" + latitude + "," + longitude,
                "eyesyhope7@gmail.com",
                "byljuxduyfrpzzph"
                , getApplication(),
                HelpActivity.this);
        sendEmailTask.execute();

    }

  /*  private void trygettingLocationWithGps() {
        // First, check if location permissions are granted

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Get the location manager
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

            // Define a LocationListener to listen for location updates
            LocationListener locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    // This method is called when the location changes
                     latitude = location.getLatitude();
                     longitude = location.getLongitude();
                    Log.d("Location", "Latitude: " + latitude + ", Longitude: " + longitude);
                }

                // Implement other LocationListener methods here
            };

            // Request location updates from the location manager
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        } else {
            // Request location permissions if not granted
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, requestCode);
        }
    }*/


}