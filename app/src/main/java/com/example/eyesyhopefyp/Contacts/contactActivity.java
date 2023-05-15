package com.example.eyesyhopefyp.Contacts;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.Prediction;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.provider.ContactsContract;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.agrawalsuneet.dotsloader.loaders.ZeeLoader;
import com.example.eyesyhopefyp.Common.model.Contact;
import com.example.eyesyhopefyp.Common.volumeHandler;
import com.example.eyesyhopefyp.Dashboard.dashboardActivity;
import com.example.eyesyhopefyp.HelpActivity;
import com.example.eyesyhopefyp.R;
import com.example.eyesyhopefyp.Receivers.BatteryReceiver;
import com.example.eyesyhopefyp.Utility.Voice;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class contactActivity extends AppCompatActivity {

    private TextView battery;
    private TextInputEditText searchBox;
    private TextToSpeech textToSpeech;
    private Button btnAssist;
    private Intent intent;
    private RecyclerView recyclerView;
    ZeeLoader parentLayout;
    ArrayList<String> bindList;
    ArrayList<Prediction> result;
    public List<Contact> contactPopulate = new ArrayList<>();
    public contactAdapter adapter;
    myDBHelper db;
    GestureLibrary gesturerLib;
    GestureOverlayView objGestureOverlay;
    public static String APP_LANG = "";
    SharedPreferences pref;
    BatteryReceiver mBattery;
    Vibrator vibrator;
    Timer timer;
    dashboardActivity mainActivity;

    private int swipeStep = 0;
    int swipesNumber;
    private int swipeStepDetails = 0;
    private final static int swipesDetailNumber = 4;
    private SwiperListener swiperListener;
    public int click = 0;

    String nameToCall, numberToCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

        parentLayout = findViewById(R.id.loader);
        //Database working
        db = new myDBHelper(contactActivity.this);
        //Adding Contact to a db
        contactPopulate = db.getAllContacts();
        InitWidget();
        //
        adapter = new contactAdapter(contactActivity.this, contactPopulate);
        searchBox = findViewById(R.id.searchBox);
        searchBox.clearFocus();
        swiperListener = new SwiperListener(btnAssist);

        if (contactPopulate.isEmpty()) {
            Log.i("check", "Contact is empty");
            new loadTask().execute();
        } else {
            recyclerView.setLayoutManager(new LinearLayoutManager(contactActivity.this));
            adapter = new contactAdapter(contactActivity.this, contactPopulate);
            recyclerView.setAdapter(adapter);
        }


        nameToCall = "";
        numberToCall = "";


        swipesNumber = contactPopulate.size();
        Log.e("swipesNumber ", String.valueOf(swipesNumber));

        searchBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                filter(s.toString());
            }
        });
        Log.i("check", "Contact is loading");

        //Services
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        //Battery Receiver
        mBattery = new BatteryReceiver(battery, getApplicationContext());
        registerReceiver(mBattery, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        //Checking For saved language in shared preference
        pref = getSharedPreferences("Settings", MODE_PRIVATE);
        //Handlers
        volumeHandler volumeHandler = new volumeHandler(getApplicationContext(), textToSpeech);
        mainActivity = new dashboardActivity();


    }


    public class loadTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            parentLayout.setVisibility(View.VISIBLE);
            ZeeLoader zeeLoader = new ZeeLoader(
                    contactActivity.this,
                    12,
                    2,
                    ContextCompat.getColor(contactActivity.this, R.color.blue_light),
                    ContextCompat.getColor(contactActivity.this, R.color.blue));

            zeeLoader.setAnimDuration(300);

            parentLayout.addView(zeeLoader);
        }

        @Override
        protected Void doInBackground(Void... params) {
            checkPermission();
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            parentLayout.setVisibility(View.GONE);
            recyclerView.setLayoutManager(new LinearLayoutManager(contactActivity.this));
            adapter = new contactAdapter(contactActivity.this, contactPopulate);

            recyclerView.setAdapter(adapter);
            Intent intent = getIntent();
            finishAfterTransition();
            startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(contactActivity.this).toBundle());
        }

        private void checkPermission() {
            if ((ContextCompat.checkSelfPermission(contactActivity.this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(contactActivity.this, Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED)) {
                textToSpeech.speak("Permissions Not Granted", TextToSpeech.QUEUE_ADD, null, null);
            } else {
                getContactList();
            }
        }

        @SuppressLint("Range")
        private void getContactList() {
            if (db.getAllContacts().isEmpty()) {
                //Init Uri
                Uri uri = ContactsContract.Contacts.CONTENT_URI;
                //Sort By Ascending
                String sort = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME;
                //Init Cursor
                Cursor cursor = getContentResolver().query(uri, null, null, null, sort);
                //Check Condition
                if (cursor.getCount() > 0) {
                    Contact contact = new Contact();
                    //When count is greater than 0 then
                    while (cursor.moveToNext()) {
                        //Cursor moves to next item
                        String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                        String contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                        //Init Phone Uri
                        Uri uriPhone = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
                        //Init Selection
                        String selection = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " =?";
                        //Init Phone Cursor
                        Cursor phoneCursor = getContentResolver().query(
                                uriPhone, null, selection, new String[]{id}, null
                        );
                        if (phoneCursor.moveToNext()) {
                            //When Phone Cursor move to next
                            String number = phoneCursor.getString(phoneCursor.getColumnIndex(
                                    ContactsContract.CommonDataKinds.Phone.NUMBER
                            ));
                            //Init Contact Model
                            Contact model = new Contact();
                            //set Name
                            model.setName(contactName);
                            //set Contact Number
                            model.setPhoneNumber(number);
                            //Add model in array
                            //arrayListContact.add(model);
                            //Putting Data in DB
                            db.addContact(model);
                            //Close Phone Cursor
                            phoneCursor.close();
                        }
                    }
                    cursor.close();
                }
                //restartThis();
            }
            //Set layout manager
        }
    }


    private void clickOnAssistButton() {
        btnAssist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        if (!isConnected(contactActivity.this)) {
                            textToSpeech.speak(getString(R.string.network_connectivity_off), TextToSpeech.QUEUE_ADD, null, null);
                        } else {
                            switch (pref.getString("lang", "en")) {
                                case "ur":
                                    APP_LANG = "ur-pk";
                                    openAssistant(APP_LANG);
                                    break;
                                default:
                                    APP_LANG = "en-us";
                                    openAssistant(APP_LANG);
                                    break;
                            }
                        }
                    }
                }, 1000);
            }
        });

        btnAssist.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {


                Intent in = new Intent(contactActivity.this, dashboardActivity.class); //Manipulating it and sending after splash to dashboardintroductoryActivity
                // Intent in = new Intent(splashScreen.this, introductoryActivity.class); //Manipulating it and sending after splash to dashboardintroductoryActivity
                in.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(in);
                finish();


                return true;
            }
        });
    }

    public boolean isConnected(contactActivity mainActivity) {
        ConnectivityManager connectivityManager = (ConnectivityManager) mainActivity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiConn = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobileConn = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if ((wifiConn != null && wifiConn.isConnected() || (mobileConn != null && mobileConn.isConnected()))) {
            return true;
        } else {
            return false;
        }


    }

    private void openAssistant(String language_code) {
        textToSpeech.speak("How may I help you!", TextToSpeech.QUEUE_FLUSH, null, null);
        intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, language_code);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.assistant_greet));
        try {
            startActivityForResult(intent, 1);
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ActivityNotFoundException r) {
            Log.e("Error", "Device does not support speech recognition");
            textToSpeech.speak("Your device does not support speech recognition, use gestures instead", TextToSpeech.QUEUE_FLUSH, null, null);
          /*  Log.e("Error", r.getMessage());
            r.printStackTrace();
            textToSpeech.speak("Your device does not support speech recognition, use gestures instead", TextToSpeech.QUEUE_FLUSH, null, null);
       */


            String appPackageName = "com.google.android.googlequicksearchbox";
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
            } catch (android.content.ActivityNotFoundException anfe) {

                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
            }


        }
    }

    private void openAssistantWithReqCode(String language_code, int reqCode) {
        intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, language_code);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak contact name or number");
        try {
            startActivityForResult(intent, reqCode);
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ActivityNotFoundException r) {
            textToSpeech.speak("Your device does not support speech recognition, use gestures instead", TextToSpeech.QUEUE_FLUSH, null, null);


            String appPackageName = "com.google.android.googlequicksearchbox";
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
            } catch (android.content.ActivityNotFoundException anfe) {

                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
            }


        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1:
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

                    String cmd = result.get(0);
                    cmd = cmd.toLowerCase().trim();


                    if (containsWords(cmd, new String[]{"search"}) || containsWords(cmd, new String[]{"search contact"}) ||
                            containsWords(cmd, new String[]{"find"}) || containsWords(cmd, new String[]{"search a contact"}) ||
                            containsWords(cmd, new String[]{"search a number"}) || containsWords(cmd, new String[]{"find a contact"}) ||
                            containsWords(cmd, new String[]{"find a number"})
                    ) {
                        textToSpeech.speak("Speak out Contact Name or Number to search!", TextToSpeech.QUEUE_ADD, null, null);
                        try {
                            Thread.sleep(3000);
                            openAssistantWithReqCode(APP_LANG, 10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else if (containsWords(cmd, new String[]{"call"}) || containsWords(cmd, new String[]{"cool"}) ||
                            containsWords(cmd, new String[]{"phone call"}) || containsWords(cmd, new String[]{"make a phone call"})
                            || containsWords(cmd, new String[]{"i want to make a phone call"})
                    ) {
                        textToSpeech.speak("Speak out Contact Name or Number to call!", TextToSpeech.QUEUE_ADD, null, null);
                        try {
                            Thread.sleep(3000);
                            openAssistantWithReqCode(APP_LANG, 20);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    }else if (containsWords(cmd, new String[]{"clear"}) || containsWords(cmd, new String[]{"clear search"}) ||
                            containsWords(cmd, new String[]{"clear search results"}) || containsWords(cmd, new String[]{"clear results"})
                    ) {
                        textToSpeech.speak("Search cleared", TextToSpeech.QUEUE_ADD, null, null);
                        try {
                            Thread.sleep(3000);

                            contactPopulate=db.getAllContacts();
                            adapter.filterList(contactPopulate);

                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                break;
            case 10:
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    String cmd = result.get(0);
                    cmd = cmd.toLowerCase().trim();
                    Log.i("number", cmd);
                    filter(cmd);
                }
                break;
            case 20:
                if (resultCode == RESULT_OK && null != data) {
                    //  textToSpeech.speak("Speak out Contact Name or Number to call!", TextToSpeech.QUEUE_FLUSH, null, null);
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    Log.e("og ressss", result.toString());
                    String cmd = result.get(0);
                    cmd = cmd.toLowerCase().trim();

                    Log.e("ressss", cmd);


                    ArrayList<String> newList = new ArrayList<>();

                    for (String res : result) {

                        String newvalue = res.replaceAll(" ", "");

                        newList.add(newvalue.replaceAll("-", ""));
                    }


                    Log.e("new ressss", newList.toString() + "\nLength : " + newList.size());


                    boolean found = false;


                    Contact contact = new Contact();

                    for (int i = 0; i < newList.size(); i++) {
                        Log.e("element " + i, newList.get(i));
                    }

                    for (String searchprase : newList) {
                        Log.e("searching element ", searchprase);
                        String newNumber = searchprase;


                        if (Character.toString(searchprase.charAt(0)).equals("0")) {
                            Log.e("replacing a 0 ", "with +263");
                            newNumber = "+263" + newNumber.substring(1);
                            Log.e("new num ", newNumber);
                        }

                        for (Contact cont : contactPopulate) {

                            Log.e("Contact from contacts",cont.getName().replace(" ", ""));

                            if (cont.getName().replace(" ", "").toLowerCase().contains(searchprase.toLowerCase()) ||
                                    cont.getPhoneNumber().replace(" ", "").contains(searchprase)
                                    || cont.getPhoneNumber().replace(" ", "").contains(newNumber)
                            ) {
                                found = true;


                                Log.e("Found Contact ", "True");
                                Log.e("Contact Details", cont.getName() + " " + cont.getPhoneNumber());

                                contact.setName(cont.getName());
                                contact.setPhoneNumber(cont.getPhoneNumber());
                                break;

                            }
                        }
                    }


                    if (found) {
                        Voice.speak(contactActivity.this, "Calling" + contact.getName() + " Please wait", false);

                        Intent intent = new Intent(Intent.ACTION_CALL);
                        intent.setData(Uri.parse("tel:" + contact.getPhoneNumber()));

                        if (ActivityCompat.checkSelfPermission(contactActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                            // Request permission to make phone calls
                            ActivityCompat.requestPermissions(contactActivity.this, new String[]{Manifest.permission.CALL_PHONE}, 1);
                        } else {
                            // Permission already granted, make the phone call
                            startActivity(intent);
                        }
                    } else {


                        String inputText = newList.get(0);

                        boolean containsStringChars = false;

                        for (int i = 0; i < inputText.length(); i++) {
                            if (Character.isLetter(inputText.charAt(i))) {
                                containsStringChars = true;
                                break;
                            }
                        }

                        if (containsStringChars) {
                            Voice.speak(contactActivity.this, "Name not found or you are trying to call an invalid number, Try again", false);
                        } else {
                            Voice.speak(contactActivity.this, "Starting Your call", false);

                            Intent intent = new Intent(Intent.ACTION_CALL);
                            intent.setData(Uri.parse("tel:" + newList.get(0)));

                            if (ActivityCompat.checkSelfPermission(contactActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                                // Request permission to make phone calls
                                ActivityCompat.requestPermissions(contactActivity.this, new String[]{Manifest.permission.CALL_PHONE}, 1);
                            } else {
                                // Permission already granted, make the phone call
                                startActivity(intent);
                            }
                        }


                    }


                }
        }

    }

    private String purifyText(String name) {
        String Name = name.toLowerCase().trim();
        Name = Name.replace(" ", "");
        Log.i("test", Name);
        return Name;
    }

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

    private void InitWidget() {
        recyclerView = findViewById(R.id.contactList);
        btnAssist = findViewById(R.id.assistBtn);
        battery = findViewById(R.id.tv_battery_indicator);

        battery.requestFocus();

        //Initializing Text to Speech
        textToSpeech = new TextToSpeech(contactActivity.this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result;
                    switch (pref.getString("lang", "en")) {
                        case "hi":
                            result = textToSpeech.setLanguage(new Locale("hi"));
                            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                                Log.e("TTS", "Language is not supported");
                                textToSpeech.setLanguage(new Locale("en"));
                            } else {
                                textToSpeech.setLanguage(new Locale("hi", "in"));
                            }
                            break;
                        case "ur":
                            result = textToSpeech.setLanguage(new Locale("ur"));
                            if (result == TextToSpeech.LANG_MISSING_DATA
                                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                                Log.e("TTS", "Language is not supported");
                                textToSpeech.setLanguage(new Locale("en"));
                                // else you ask the system to install it
                            } else {
                                textToSpeech.setLanguage(new Locale("ur", "pk"));
                            }
                            break;
                        default:
                            result = textToSpeech.setLanguage(new Locale("en"));
                            if (result == TextToSpeech.LANG_MISSING_DATA
                                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                                Log.e("TTS", "Language is not supported");
                            }
                            break;
                    }
                }

            }
        });


    }

    private void filter(String text) {

        List<Contact> filtered = new ArrayList<>();
        for (Contact item : contactPopulate) {
            if (item.getName().toLowerCase().contains(text.toLowerCase()) || item.getPhoneNumber().toLowerCase().trim().contains(text.toLowerCase())) {
                filtered.add(item);

            } else {

            }
        }

        Voice.speak(contactActivity.this, "Found " +filtered.size() +" results", false);
        contactPopulate=filtered;
        adapter.filterList(filtered);

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

                                if (!(swipeStep >= contactPopulate.size())) {
                                    Voice.speak(contactActivity.this, contactPopulate.get(swipeStep).getName(), false);
                                    Log.e("Name ", contactPopulate.get(swipeStep).getName());
                                    Log.e("Phone ", contactPopulate.get(swipeStep).getPhoneNumber());

                                    nameToCall = contactPopulate.get(swipeStep).getName();
                                    numberToCall = contactPopulate.get(swipeStep).getPhoneNumber();

                                    for (int i = 0; i < contactPopulate.size() - 1; i++) {
                                        int color = Color.WHITE; // the color to set as the background color

                                        RecyclerView.ViewHolder holder = recyclerView.findViewHolderForAdapterPosition(i);
                                        if (holder != null) {
                                            holder.itemView.setBackgroundColor(color);
                                        }
                                    }


                                    // the position of the element to change the background color of
                                    int color = Color.rgb(191, 191, 186); // the color to set as the background color

                                    RecyclerView.ViewHolder holder = recyclerView.findViewHolderForAdapterPosition(swipeStep);
                                    if (holder != null) {
                                        holder.itemView.setBackgroundColor(color);
                                        recyclerView.scrollToPosition(swipeStep); // update based on adapter
                                    }


                                } else {

                                    Log.e("Error", "Index Out of Bounce ");
                                }


                            }



                               /* Log.e("Error","opening speech rec");

                                timer = new Timer();
                                timer.schedule(new TimerTask() {
                                    @Override
                                    public void run() {
                                        if (!isConnected(contactActivity.this)) {
                                            Log.e("Error","opening speech recognition because there is not internet");
                                            textToSpeech.speak(getString(R.string.network_connectivity_off), TextToSpeech.QUEUE_ADD, null, null);
                                        } else {
                                            switch (pref.getString("lang", "en")) {
                                                case "ur":
                                                    APP_LANG = "ur-pk";
                                                    openAssistant(APP_LANG);
                                                    break;
                                                default:
                                                    APP_LANG = "en-us";
                                                    openAssistant(APP_LANG);
                                                    break;
                                            }
                                        }
                                    }
                                }, 1000);*/

                        } else {
                            if (Math.abs(yDiff) > threshold && Math.abs(velocityY) > velocity_threshold) {
                                //When X differ is greater than threshold and X velocity is greater than velocity threshold
                                if (yDiff > 0) {
                                    String todo = "Nothing";
                                } else {
                                    Log.e("Error", "opening speech rec");

                                    timer = new Timer();
                                    timer.schedule(new TimerTask() {
                                        @Override
                                        public void run() {
                                            if (!isConnected(contactActivity.this)) {
                                                Log.e("Error", "opening speech recognition because there is not internet");
                                                textToSpeech.speak(getString(R.string.network_connectivity_off), TextToSpeech.QUEUE_ADD, null, null);
                                            } else {
                                                switch (pref.getString("lang", "en")) {
                                                    case "ur":
                                                        APP_LANG = "ur-pk";
                                                        openAssistant(APP_LANG);
                                                        break;
                                                    default:
                                                        APP_LANG = "en-us";
                                                        openAssistant(APP_LANG);
                                                        break;
                                                }
                                            }
                                        }
                                    }, 1000);
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

    private void doubleTapToOpenModule() {
        btnAssist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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

        btnAssist.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {


                Intent in = new Intent(contactActivity.this, dashboardActivity.class); //Manipulating it and sending after splash to dashboardintroductoryActivity
                // Intent in = new Intent(splashScreen.this, introductoryActivity.class); //Manipulating it and sending after splash to dashboardintroductoryActivity
                in.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(in);
                finish();


                return true;
            }
        });
    }

    private void callActivityOnDoubleTap() {

        if (nameToCall.isEmpty() || numberToCall.isEmpty()) {
            Log.e("Number not found", "no number found , please select a contact first");
            Voice.speak(contactActivity.this, "please select a contact first before calling", false);
        } else {
            Log.e("Calling >>>>   >>>>", nameToCall);
            Voice.speak(contactActivity.this, "Calling" + nameToCall + " Please wait", false);

            Intent intent = new Intent(Intent.ACTION_CALL);
            intent.setData(Uri.parse("tel:" + numberToCall));

            if (ActivityCompat.checkSelfPermission(contactActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                // Request permission to make phone calls
                ActivityCompat.requestPermissions(contactActivity.this, new String[]{Manifest.permission.CALL_PHONE}, 1);
            } else {
                // Permission already granted, make the phone call
                startActivity(intent);
            }
        }
    }
}