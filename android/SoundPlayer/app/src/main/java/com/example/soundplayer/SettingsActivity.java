package com.example.soundplayer;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class SettingsActivity extends AppCompatActivity {

    public static final String saisie_text_choice = "saisie_text";

    public static final String ip_speech = "ip_speach";

    public static final String ip_interpret = "ip_interpret";

    public static final String ip_ice = "ip_ice";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }
}
