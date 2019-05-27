package com.example.soundplayer;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.annotation.SuppressLint;
import android.graphics.Color;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;


import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    //Text Recognition
    public TextView correspondance;

    // Enregistrement audio
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private MediaRecorder micro = null;
    private String recordFile = null;
    private boolean inRecord = false;
    public HttpEntity entity;

    // Requesting permission to RECORD_AUDIO
    private boolean permissionToRecordAccepted = false;
    private String[] permissions = {Manifest.permission.RECORD_AUDIO, Manifest.permission.INTERNET};

    //Player Ice
    private AsyncPlayer asyncPlayer;
    public Ice.Communicator communicator;
    public Player.PlayerServeurPrx player;
    public Ice.ObjectPrx base;
    public ListView infoMusique;

    // Player vlc
    public LibVLC lVlcPlayer;
    public MediaPlayer mediaPlayer;

    // @ip
    public static String ip_interpret = "192.168.1.6";
    public static String ip_speech = "192.168.1.6";
    public static String ip_ice = "192.168.43.181";

    // Enregistrement audio
    // Demande de permission d'enregistrer
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToRecordAccepted) finish();

    }

    /**
     *  Enregistrement audio, dans un format compatible avec celui du serveur speech to text
     */
    private void recordStart() {
        micro = new MediaRecorder();
        micro.setAudioSource(MediaRecorder.AudioSource.MIC);
        micro.setOutputFormat(MediaRecorder.OutputFormat.AMR_WB);
        micro.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_WB);
        micro.setAudioSamplingRate(16000);
        micro.setOutputFile(recordFile);
        System.out.println(recordFile);
        try {
            micro.prepare();
        } catch (IOException e) {
        }

        micro.start();
        inRecord = true;
    }

    /**
     * Arret de l'enregistrement
     */
    private void stopRecord() {
        System.out.println("Stop record (in record?): " + inRecord);
        try {
            micro.stop();
        } catch (java.lang.RuntimeException e) {

        }
        micro.release();
        micro = null;
        inRecord = false;
    }
    //fin action enregistrement audio

    /**
     * Initialisation de l'activité principale
     */
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        // Emplacement fichier audio temporaire
        recordFile = getExternalCacheDir().getAbsolutePath() + "/recTmp.amr";
        System.out.println(recordFile);

        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        //items recupération
        correspondance = findViewById(R.id.textReturnCorrespondance);


        // Settings
        android.support.v7.preference.PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        SharedPreferences sharedPref = android.support.v7.preference.PreferenceManager.getDefaultSharedPreferences(this);

        //Sow text_box?
        Boolean show_saisie_text = sharedPref.getBoolean(SettingsActivity.saisie_text_choice, false);

        Button button_text_commande = findViewById(R.id.textButton);
        EditText correspondanceText = findViewById(R.id.correspondanceText);
        if (show_saisie_text == true) {
            button_text_commande.setVisibility(View.VISIBLE);
            correspondanceText.setVisibility(View.VISIBLE);
        } else {
            button_text_commande.setVisibility(View.GONE);
            correspondanceText.setVisibility(View.GONE);
        }

        infoMusique = findViewById(R.id.info_musique);

        //Settings serveur's ip
        ip_interpret = sharedPref.getString(SettingsActivity.ip_interpret, "");
        ip_speech = sharedPref.getString(SettingsActivity.ip_speech, "");
        ip_ice = sharedPref.getString(SettingsActivity.ip_ice, "");
        if ((ip_interpret == null || ip_interpret.equals(" ") || ip_interpret.equals("")) || (ip_speech == null || ip_speech.equals(" ") || ip_speech.equals("")) || (ip_ice == null || ip_ice.equals(" ") || ip_ice.equals(""))) {
            Toast.makeText(this, "!ip des serveurs non définie!", Toast.LENGTH_SHORT).show();
        }

        /*-------------------
                Events
          ---------------------
         */
        //Event click on listening button
        // Lancer le recording
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if(mediaPlayer != null && mediaPlayer.isPlaying()) {
                        mediaPlayer.pause();
                    }
                    TextView t_listen = findViewById(R.id.text_listen);
                    t_listen.setVisibility(View.VISIBLE);
                    recordStart();
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    if(mediaPlayer != null) {
                        mediaPlayer.play();
                    }
                    TextView t_listen = findViewById(R.id.text_listen);
                    t_listen.setVisibility(View.INVISIBLE);
                    stopRecord();
                    getAudioRecognition();
                }
                return true;
            }
        });


        //Event submit text for Recognition
        // Saisie de la commande par texte
        button_text_commande.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                EditText correspondanceText = findViewById(R.id.correspondanceText);
                String text = correspondanceText.getText().toString();
                getRecognition(text);
                return true;
            }
        });

        /*-------------------
                End Events
          ---------------------
         */
    }

    /**
     * Fait une reqête http pour envoyer le fichier audio de la commande à l'aide de volley
     * Récupère la transcription et l'envoie pour analyse
     */
    public void getAudioRecognition() {
        MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
        entityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        entity = new MultipartEntity();
        Response.Listener<String> listener = null;
        Response.ErrorListener eListener;
        File record = new File(recordFile);
        String url = "http://" + MainActivity.ip_speech + ":3101/transcribe";

        ContentType contentType = ContentType.create("audio/AMR-WB");
        entityBuilder.addBinaryBody("file", record, contentType, "recorFile");

        entity = entityBuilder.build();
        System.out.println("Post for recognition: " + url + " file: " + recordFile);
        StringRequest req = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        System.out.println("Post response: " + response);
                        correspondance.setText(response);
                        correspondance.invalidate();
                        JSONObject jObject = null;
                        String transcription = "";
                        try {
                            jObject = new JSONObject(response);
                            transcription = jObject.getString("transcription");

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        getRecognition(transcription);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        System.out.println("Post error: " + error.getMessage());
                        correspondance.setText(error.getMessage());
                        correspondance.invalidate();
                    }
                }
        ) {
            @Override
            public String getBodyContentType() {
                System.out.println("Post content: " + entity.getContentType().getValue());
                return entity.getContentType().getValue();
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                System.out.println("Post getBody: ");

                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                try {
                    entity.writeTo(bos);
                } catch (IOException e) {
                    VolleyLog.e("IOException writing to ByteArrayOutputStream");
                }
                return bos.toByteArray();
            }
        };
        RequestQueue volleyRequestQueue = Volley.newRequestQueue(getApplicationContext());
        volleyRequestQueue.add(req);
    }

    /**
     * Appel à l'analyseur d'entités nommées
     * @param text texte à analyser
     */
    public void getRecognition(String text) {
        RequestQueue volleyRequestQueue = Volley.newRequestQueue(getApplicationContext());
        String url = null;
        try {
            url = "http://" + MainActivity.ip_interpret + ":8080/correspondance/rest/action/" + "?text=" + URLEncoder.encode(text, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        ;
        StringRequest getRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        System.out.println("------> Analyse: " + response);
                        correspondance.setText(response);
                        correspondance.invalidate();
                        //performAction(response);
                        if (asyncPlayer == null) {
                            asyncPlayer = new AsyncPlayer();
                        } else {
                            asyncPlayer.cancel(true);
                            asyncPlayer = new AsyncPlayer();
                        }
                        asyncPlayer.execute(response);

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        correspondance.setText(error.getMessage());
                        correspondance.invalidate();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("text", text);

                return params;
            }
        };
        volleyRequestQueue.add(getRequest);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     * Génération de l'activité settings
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Classe gérant les activitées asynchones
     *  Gestion du player de musiques ICE
     */
    public class AsyncPlayer extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String[] args) {
                //Ice
            try {
                //---Initialisation
                System.out.println("-----------> Init ice: ");
                communicator = Ice.Util.initialize();

                base = communicator.stringToProxy("MonServeur:default -h " + ip_ice + " -p 10000");

                player = Player.PlayerServeurPrxHelper.checkedCast(base);

                if (player == null) {
                    //test si null en cas d'echec de co
                    System.out.println("-----------> Error: ");
                }

                //LibVlc
                System.out.println("-----------> Init Vlc");
                final ArrayList<String> argsVlc = new ArrayList<>();
                argsVlc.add("--input-repeat=-1");
                argsVlc.add("--fullscreen");
                lVlcPlayer = new LibVLC(getApplicationContext(), argsVlc);
                mediaPlayer = new MediaPlayer(lVlcPlayer);

                //---End of initialisation

                //--Paramétrage de l'action à effectuer
                performAction(args[0]);

                return "playing";
            } catch (Exception e) {
                System.out.println("-----------> Error: " + e.getMessage());
                e.printStackTrace();
            }
            return "Not playing";
        }

        /**
         * Réalise une action à partir de paramètres passés en arguments
         * @param jsonAction Json contenant les information de l'action à réaliser
         */
        public void performAction(String jsonAction) {
        /* ex format:
        {
            "action": "play_musique",
            "param": {
                "musique": "le petit bonhome en mousse"
            }
        }*/
            JSONObject jObject;
            String action = "";
            JSONObject jOParams = null;
            String paramType = "";
            String param = "";
            try {
                jObject = new JSONObject(jsonAction);
                action = jObject.getString("action");
                jOParams = jObject.getJSONObject("param");
                if(jOParams.has("musique")) {
                    paramType = "musique";
                    param = jOParams.getString("musique");
                } else if(jOParams.has("album")) {
                    paramType = "album";
                    param = jOParams.getString("album");
                } else if(jOParams.has("artiste")) {
                    paramType = "artiste";
                    param = jOParams.getString("artiste");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            switch (action) {
                case "play_musique":
                    String streamInfo ="";
                    switch (paramType) {
                        case "musique":
                            streamInfo = player.playMusique(param);
                            if(!streamInfo.equals("")) {
                                validCorrespondance(param, "musique");
                            }
                            break;
                        case "album":
                            streamInfo = player.playMusiqueAlbum(param);
                            if(!streamInfo.equals("")) {
                                validCorrespondance(param, "album");
                            }
                            break;
                        case "artiste":
                            streamInfo = player.playMusiqueArtiste(param);
                            if(!streamInfo.equals("")) {
                                validCorrespondance(param, "artiste");
                            }
                            break;
                        default:
                            break;
                    }
                    if(!streamInfo.equals("")) {
                        /*
                        Ex format:
                        {
                            "url":"http://192.168.1.6:8083/stream0.mp3",
                            "info":[
                                {
                                    "titre":"lose yourself to dance",
                                    "artiste":"daft punk",
                                    "album":"random acces memory",
                                    "path_musique":"/home/casanova/Documents/Master-ilsen/s2/Application-Architecture/Player/musicDB/daft_punk/random_acces_memory/lose_yourself_to_dance.mp3"
                                },
                                {
                                    "titre":"get lucky",
                                    "artiste":"daft punk",
                                    "album":"random acces memory",
                                    "path_musique":"/home/casanova/Documents/Master-ilsen/s2/Application-Architecture/Player/musicDB/daft_punk/random_acces_memory/get_lucky.mp3"
                                }
                            ]
                        }
                         */
                        System.out.println("Réponse: " + streamInfo);
                        String urlStream = "";
                        JSONObject jOStreamInfo = null;
                        JSONArray jAInfos = null;
                        //List<MusiqueInfo> infosMusique = new ArrayList<MusiqueInfo>();
                        try {
                            jOStreamInfo = new JSONObject(streamInfo);
                            urlStream = jOStreamInfo.getString("url");
                            jAInfos = jOStreamInfo.getJSONArray("info");
                            afficheMusiqueList(jAInfos);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        playVlc(urlStream);
                    }
                    break;
                case "affiche_musique":
                    String infoMusique ="";
                    switch (paramType) {
                        case "musique":
                            infoMusique = player.printMusique(param);
                            if(!infoMusique.equals("")) {
                                validCorrespondance(param, "musique");
                            }
                            break;
                        case "album":
                            infoMusique = player.printMusiqueByAlbum(param);
                            if(!infoMusique.equals("")) {
                                validCorrespondance(param, "album");
                            }
                            break;
                        case "artiste":
                            infoMusique = player.printMusiqueByArtiste(param);
                            if(!infoMusique.equals("")) {
                                validCorrespondance(param, "artiste");
                            }
                            break;
                        default:
                            infoMusique = player.printAllMusique();
                            if(!infoMusique.equals("")) {
                            }
                            break;
                    }
                    if(!infoMusique.equals("")) {
                        System.out.println("Réponse: " + infoMusique);
                        JSONObject jOMusiqueInfo = null;
                        JSONArray jAMusiqueInfos = null;
                        try {
                            jOMusiqueInfo = new JSONObject(infoMusique);
                            jAMusiqueInfos = jOMusiqueInfo.getJSONArray("info");
                            afficheMusiqueList(jAMusiqueInfos);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    break;
                case "affiche_album":
                    String infoAlbum ="";
                    switch (paramType) {
                        case "artiste":
                            infoAlbum = player.printAlbumByArtiste(param);
                            if(!infoAlbum.equals("")) {
                                validCorrespondance(param, "artiste");
                            }
                            break;
                        case "album":
                            infoAlbum = player.printAlbum(param);
                            if(!infoAlbum.equals("")) {
                                validCorrespondance(param, "album");
                            }
                            break;
                        default:
                            infoAlbum = player.printAllAlbum();
                            if(!infoAlbum.equals("")) {
                            }
                            break;
                    }
                    if(!infoAlbum.equals("")) {
                        System.out.println("Réponse: " + infoAlbum);
                        JSONObject jOAlbumInfo = null;
                        JSONArray jAAlbumInfos = null;
                        try {
                            jOAlbumInfo = new JSONObject(infoAlbum);
                            jAAlbumInfos = jOAlbumInfo.getJSONArray("info");
                            afficheMusiqueList(jAAlbumInfos);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case "next_musique":
                    player.next();
                    break;
                case "previous_musique":
                    player.previous();
                    break;
                case "stop":
                    player.stop();
                    break;
                case "pause":
                    player.pause();
                    break;
                case "resume":
                    player.reprendre();
                    break;
                case "not_found":
                    break;
                case "error":
                    break;
            }
        }

        /**
         * Appel à l'url Rest permettant d'enregistrer une correspondance,
         * Appelé que si une des fonction ice (affuche / joue) retourne des résultats
         * @param text
         * @param type
         */
        public void validCorrespondance(String text, String type) {
            RequestQueue volleyRequestQueue = Volley.newRequestQueue(getApplicationContext());
            String url = null;
            try {
                url = "http://" + MainActivity.ip_interpret + ":8080/correspondance/rest/validCorrespondance" + "?text=" + URLEncoder.encode(text, "utf-8") + "&type=" + URLEncoder.encode(type, "utf-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            ;
            StringRequest getRequest = new StringRequest(Request.Method.GET, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            //Toast.makeText(getApplicationContext(), response, Toast.LENGTH_SHORT).show();
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            //Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    }
            ) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("text", text);

                    return params;
                }
            };
            volleyRequestQueue.add(getRequest);
        }

        /**
         * Lancement de al récupération d'un stream à partir d'une url
         * @param url
         */
        public void playVlc(String url) {
            Media m = new Media(lVlcPlayer, Uri.parse(url));
            mediaPlayer.setMedia(m);
            mediaPlayer.play();
        }

        /**
         * Affichage des musiques qui sont en train d'être jouées ou des musiques trouvées.
         * fait appel à un adapteur de listeView custom pour gérer l'affichage des immages
         * @param infos
         */
        public void afficheMusiqueList(JSONArray infos) {
            List<MusiqueInfo> infosMusique = new ArrayList<MusiqueInfo>();
            try {
                for(int i=0; i<infos.length(); i++) {
                    JSONObject jOInfo = infos.getJSONObject(i);
                    MusiqueInfo mi = new MusiqueInfo(
                        jOInfo.getString("titre"),
                        jOInfo.getString("album"),
                        jOInfo.getString("artiste"),
                        jOInfo.getString("cover")
                    );
                    infosMusique.add(mi);
                }
                MusiqueInfosAdapter mia = new MusiqueInfosAdapter(getApplicationContext(), R.layout.info_musique, infosMusique);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        infoMusique.setAdapter(mia);
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}