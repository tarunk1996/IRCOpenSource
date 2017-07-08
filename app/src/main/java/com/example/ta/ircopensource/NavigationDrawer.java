package com.example.ta.ircopensource;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ui.User;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class NavigationDrawer extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    private static final int SIGN_IN_REQUEST_CODE = 1;
    private static final int RESULT_LOAD_IMAGE = 2 ;
    private FirebaseListAdapter<ChatMessage> adapter;

    private static final int REQ_CODE_SPEECH_INPUT = 100;
    private FloatingActionButton speech;

    private String currentChat;
    boolean flag=false;

    LinearLayout linerLayout;

    //SharedPreferences.Editor mEditor;
    SharedPreferences mPrefs;

    private EditText input;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_drawer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mPrefs = this.getSharedPreferences("label", 0);
        String mString = mPrefs.getString("background", "NO");


        currentChat = "general";
        if(FirebaseAuth.getInstance().getCurrentUser() == null) {
            // Start sign in/sign up activity
            startActivityForResult(
                    AuthUI.getInstance().createSignInIntentBuilder().build(),
                    SIGN_IN_REQUEST_CODE
            );

        } else {
            // User is already signed in. Therefore, display
            // a welcome Toast
            Toast.makeText(this,
                    "Welcome " + FirebaseAuth.getInstance()
                            .getCurrentUser()
                            .getDisplayName(),
                    Toast.LENGTH_LONG)
                    .show();
            loadHearder();
        }


        linerLayout = (LinearLayout) findViewById(R.id.linearlay) ;

        //String picturePath = DataManager.getInstance().getImageUrl();
        if(mString == "NO" ){
            getWindow().setBackgroundDrawableResource(R.drawable.chat_background) ;
            //Set some default image that will be visible before selecting image
        }else{
            Log.v("Check","Working"+mString);
            Bitmap bitmap = BitmapFactory.decodeFile(mString);
            BitmapDrawable background = new BitmapDrawable(bitmap);
            //View view = (View) findViewById(R.layout.content_navigation_drawer);
            //linerLayout.setBackgroundDrawable(background);
            getWindow().setBackgroundDrawable(background);
        }

            currentChat = "general";

            displayChatMessages();

            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawer.setDrawerListener(toggle);
            toggle.syncState();


            //navigationView.addHeaderView(nav_header);

            input = (EditText) findViewById(R.id.input);


            speech = (FloatingActionButton) findViewById(R.id.speech);

            speech.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {


                    Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                    intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Hello, How can I help you?");
                    try {
                        startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
                    } catch (ActivityNotFoundException a) {

                    }

                }
            });

            final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //input = (EditText)findViewById(R.id.input);

                    if(!input.getText().toString().matches("")) {
                        // Read the input field and push a new instance
                        // of ChatMessage to the Firebase database
                        FirebaseDatabase.getInstance()
                                .getReference().child(currentChat)
                                .push()
                                .setValue(new ChatMessage(input.getText().toString(),
                                        FirebaseAuth.getInstance()
                                                .getCurrentUser()
                                                .getDisplayName())
                                );

                    }
                    // Clear the input
                    input.setText("");
                }
            });






    }

    private void loadHearder() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        ColorGenerator generator = ColorGenerator.MATERIAL;
        int color = generator.getColor(FirebaseAuth.getInstance().getCurrentUser().getDisplayName().substring(0, 1));
        TextDrawable drawable = TextDrawable.builder()
                .buildRound(FirebaseAuth.getInstance().getCurrentUser().getDisplayName().substring(0, 1), color);
        View nav_header = LayoutInflater.from(this).inflate(R.layout.nav_header_navigation_drawer, null);
        ((TextView) nav_header.findViewById(R.id.name)).setText(FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
        ((TextView) nav_header.findViewById(R.id.email)).setText(FirebaseAuth.getInstance().getCurrentUser().getEmail());
        ((ImageView) nav_header.findViewById(R.id.imageView)).setImageDrawable(drawable);
        navigationView.addHeaderView(nav_header);
        navigationView.setNavigationItemSelectedListener(this);
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.



        getMenuInflater().inflate(R.menu.navigation_drawer, menu);
        getMenuInflater().inflate(R.menu.main_menu, menu);


        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if(requestCode == REQ_CODE_SPEECH_INPUT) {
            if (resultCode == RESULT_OK && null != data) {
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                input.setText(result.get(0));
            }
        }

        if(requestCode == SIGN_IN_REQUEST_CODE) {
            if(resultCode == RESULT_OK) {
                Toast.makeText(this,
                        "Successfully signed in. Welcome!",
                        Toast.LENGTH_LONG)
                        .show();
                loadHearder();
                displayChatMessages();
            } else {
                Toast.makeText(this,
                        "We couldn't sign you in. Please try again later.",
                        Toast.LENGTH_LONG)
                        .show();

                // Close the app
                finish();
            }
        }

        if(requestCode == RESULT_LOAD_IMAGE)
        {
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
                Uri selectedImage = data.getData();
                String[] filePathColumn = { MediaStore.Images.Media.DATA };
                Cursor cursor = getContentResolver().query(selectedImage,filePathColumn, null, null, null);
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String picturePath = cursor.getString(columnIndex);
                DataManager.getInstance().setImageUrl(picturePath);
                cursor.close();
                Bitmap bitmap = BitmapFactory.decodeFile(picturePath);
                BitmapDrawable background = new BitmapDrawable(bitmap);
                //View view = (View) findViewById(R.layout.content_navigation_drawer);
                //linerLayout.setBackgroundDrawable(background);
                getWindow().setBackgroundDrawable(background);
                mPrefs.edit().putString("background", picturePath.toString()).commit();
                //mEditor = mPrefs.edit();
                //mEditor.putString("background", picturePath).commit();
            }
            //Recreate this Activity

            //startActivity(new Intent(this,NavigationDrawer.class));
        }




    }



    private void displayChatMessages() {
        ListView listOfMessages = (ListView)findViewById(R.id.list_of_messages);

        adapter = new FirebaseListAdapter<ChatMessage>(this, ChatMessage.class,
                R.layout.message, FirebaseDatabase.getInstance().getReference().child(currentChat)) {
            @Override
            protected void populateView(View v, ChatMessage model, int position) {

                // Get references to the views of message.xml
                TextView messageText = (TextView)v.findViewById(R.id.message_text);
                TextView messageUser = (TextView)v.findViewById(R.id.message_user);
                TextView messageTime = (TextView)v.findViewById(R.id.message_time);
                ImageView image = (ImageView) v.findViewById(R.id.image_view);

                ColorGenerator generator = ColorGenerator.MATERIAL;
                int color = generator.getColor(model.getMessageUser().substring(0,1));
                TextDrawable drawable = TextDrawable.builder()
                        .buildRound(model.getMessageUser().substring(0,1), color);


                image.setImageDrawable(drawable);


                // Set their text
                messageText.setText(model.getMessageText());
                messageUser.setText(model.getMessageUser());
                messageUser.setTextColor(color);
                messageTime.setText(DateFormat.format("dd-MM (hh:mm a)",
                        model.getMessageTime()));

            }
        };

        listOfMessages.setAdapter(adapter);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        if(item.getItemId() == R.id.menu_sign_out) {
            AuthUI.getInstance().signOut(this)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(NavigationDrawer.this,
                                    "You have been signed out.",
                                    Toast.LENGTH_LONG)
                                    .show();

                            // Close activity
                            finish();
                        }
                    });
        }

        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {

            Intent i = new
                    Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(i, RESULT_LOAD_IMAGE);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_general) {
            currentChat = "general";
        } else if (id == R.id.nav_notice) {
            currentChat = "notice";
        } else if (id == R.id.nav_physics) {
            currentChat = "physics";
        } else if (id == R.id.nav_mathematics) {
            currentChat = "mathematics";
        }

        displayChatMessages();
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


}

