package net.typeblog.shelter.omerflex.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import net.typeblog.shelter.R;

import androidx.appcompat.app.AppCompatActivity;

import net.typeblog.shelter.omerflex.entity.Artist;
import net.typeblog.shelter.omerflex.entity.ArtistList;
import net.typeblog.shelter.omerflex.servers.AkwamController;
import net.typeblog.shelter.omerflex.servers.ControllableServer;

import java.util.ArrayList;
import java.util.List;

/**
 * starts after ItemActivity if item has resolutions links
 * starts after ServersListActivity if item has resolutions links
 */
public class ResolutionsListActivity extends AppCompatActivity {

    //variable for back button confirmation
    private long backPressedTime;
    String TAG = "ResolutionsListActivity";
    ControllableServer server;

    static List<Artist> resolutionArtistList;
    ArtistList resolutionAdapter;
    Intent receivedIntent;
    TextView descriptionTextView;
    static String descText = "";

    ListView listViewArtists;

    Artist artist;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.omarflex_activity_resolutions_list);

        listViewArtists = (ListView) findViewById(R.id.listViewArtist);

        resolutionArtistList = new ArrayList<>();
        resolutionAdapter = new ArtistList(ResolutionsListActivity.this, resolutionArtistList);
        listViewArtists.setAdapter(resolutionAdapter);

        descriptionTextView = (TextView) findViewById(R.id.textViewDesc);
        descriptionTextView.setMovementMethod(new ScrollingMovementMethod());

        receivedIntent = getIntent();
        artist = new Artist();
        artist.setUrl(receivedIntent.getStringExtra("ARTIST_URL"));
        artist.setName(receivedIntent.getStringExtra("ARTIST_NAME"));
        artist.setImage(receivedIntent.getStringExtra("ARTIST_IMAGE"));
        artist.setServer(receivedIntent.getStringExtra("ARTIST_SERVER"));
        artist.setIsVideo(receivedIntent.getExtras().getBoolean("ARTIST_IS_VIDEO"));

        server = determineServer(artist,  resolutionArtistList,ResolutionsListActivity.this, resolutionAdapter); //determine which server to use

        listViewArtists.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Artist artist = resolutionArtistList.get(position);
                //server.startVideo(artist.getUrl());
                server.fetch(artist);
            }
        });

        start();
    }


    private void start() {
        server.fetchResolutions(artist);
    }

    private ControllableServer determineServer(Artist artist, List<Artist> artistList, Activity activity, ArtistList adapter) {
        switch (artist.getServer()) {
            case Artist.SERVER_AKWAM:
                return new AkwamController(artistList, activity, adapter);
//            case Artist.SERVER_OLD_AKWAM:
//                return new OldAkwamController(artistList, activity, adapter);
//            case Artist.SERVER_FASELHD:
//                return new FaselHdController(artistList, activity, adapter);
//            case Artist.SERVER_CIMA4U:
//                return new Cima4uController(artistList, activity, adapter);
//            case Artist.SERVER_MyCima:
//                return new MyCimaController(artistList, activity, adapter);
//            case Artist.SERVER_SHAHID4U:
//                return new Shahid4uController(artistList, activity, adapter);
        }
        return null;
    }

}