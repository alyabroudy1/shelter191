package net.typeblog.shelter.omerflex.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import net.typeblog.shelter.R;

import androidx.appcompat.app.AppCompatActivity;

import net.typeblog.shelter.omerflex.entity.Artist;
import net.typeblog.shelter.omerflex.entity.ArtistList;
import net.typeblog.shelter.omerflex.servers.AkwamController;
import net.typeblog.shelter.omerflex.servers.ControllableServer;
import net.typeblog.shelter.ui.MainActivity;

import java.util.ArrayList;
import java.util.List;

public class OmarflexMainActivity extends AppCompatActivity {

    //variable for back button confirmation
    private long backPressedTime;
    String TAG = "MainActivity";
    ControllableServer server; //akwamController
    ControllableServer serverMycima; //mycimaController

    static List<Artist> mainArtistList;
    ArtistList mainAdapter;

    EditText editTextName;
    Button buttonSearch;
    ListView listViewArtists;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.omarflex_activity_main);

        editTextName = (EditText) findViewById(R.id.editTextName);
        buttonSearch = (Button) findViewById(R.id.buttonSearch);

        listViewArtists = (ListView) findViewById(R.id.listViewArtist);

        mainArtistList = new ArrayList<>();
        mainAdapter = new ArtistList(OmarflexMainActivity.this, mainArtistList);

        listViewArtists.setAdapter(mainAdapter);

        server = new AkwamController(mainArtistList, OmarflexMainActivity.this, mainAdapter);
//        serverMycima = new MyCimaController(mainArtistList, OmarflexMainActivity.this, mainAdapter);


        listViewArtists.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Artist artist = mainArtistList.get(position);
                if (artist.getServer().equals(Artist.SERVER_MyCima)){
                    serverMycima.fetch(artist);
                }else
                server.fetch(artist);
            }
        });



        //click listener for the button
        buttonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String query = editTextName.getText().toString();
                handleClick(query);
            }
        });

        editTextName.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    String query = editTextName.getText().toString();
                    return handleClick(query);
                }
                return false;
            }
        });


        start();
    }

    private boolean handleClick(String query){
        Log.i(TAG, "onButtonClick." + "query: " + query);
        if (query.equals("192222")) {
            editTextName.setText("");
            Intent searchIntent = new Intent(OmarflexMainActivity.this, MainActivity.class);
            //start the activity
            startActivity(searchIntent);
            return true;
        }
        Intent searchIntent = new Intent(OmarflexMainActivity.this, SearchActivity.class);
        searchIntent.putExtra("QUERY", query);
        //start the activity
        startActivity(searchIntent);
        return true;
    }

    @Override
    protected void onStart() {
        editTextName.setText("");
        super.onStart();
        //start();
    }

    public void start() {
        if (OmarflexMainActivity.mainArtistList.isEmpty()) {

            String movies = "https://ak.sv/movies";
            String netflex = "https://mycima.wine/production/netflix/";

           server.search(movies);
           //serverMycima.search(movies);

            /*try {
                Thread.sleep(1990);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

             */

           // serverMycima.search(netflex);
           // Collections.shuffle(MainActivity.mainArtistList);
        }
    }

    //confirmation if the user click the back button to exit the app
    @Override
    public void onBackPressed() {
        //check if waiting time between the second click of back button is greater less than 2 seconds so we finish the app
        if (backPressedTime + 2000 > System.currentTimeMillis()) {
            finish();
        } else {
            Toast.makeText(this, "Press back 2 time to exit", Toast.LENGTH_SHORT).show();
            super.onBackPressed();
        }
        backPressedTime = System.currentTimeMillis();
    }

    @Override
    protected void onResume() {
        editTextName.setText("");
        super.onResume();
    }
}