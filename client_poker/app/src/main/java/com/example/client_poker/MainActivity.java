package com.example.client_poker;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    ImageView play;
    Button changeName;
    Button about;
    Button learning;
    EditText username;
    public static ClientSideConnection csc;
    static String message;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


        play = findViewById(R.id.play);
        about = findViewById(R.id.about);
        username = findViewById(R.id.username);
        changeName = findViewById(R.id.changename);
        learning = findViewById(R.id.learning);
        if (csc==null) {

            SetUpStreams setUpStreams = new SetUpStreams();
            setUpStreams.execute();
        }


        SharedPreferences sharedPref=getSharedPreferences("sharePref",Context.MODE_PRIVATE);
        username.setText(sharedPref.getString("username","Guest"));
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putString("username",username.getText().toString());
        editor.apply();



        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendMessage now = new SendMessage();
                now.execute("guest");
                startActivity(new Intent(MainActivity.this, Pokeroom.class));
            }
        });
        about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, About.class));
            }
        });
        changeName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPref=getSharedPreferences("sharePref",Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("username",username.getText().toString());
                editor.commit();
            }
        });
        learning.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, Learning.class));
            }
        });


    }
    public void receive_text(){
        System.out.println("Hello");


    }
    static class SetUpStreams extends AsyncTask<Void,Void,Void>{

        @Override
        protected Void doInBackground(Void... Voids) {
            csc = new ClientSideConnection();
            return null;
        }

    }

    static class SendMessage extends AsyncTask<String,Void,Void>{


        @Override
        protected Void doInBackground(String... args) {
            try {
                System.out.println(args[0]);
                csc.getOut().writeObject(args[0]);
                csc.getOut().flush();
            }catch (IOException e){
                e.printStackTrace();
            }

            return null;
        }


    }








}

