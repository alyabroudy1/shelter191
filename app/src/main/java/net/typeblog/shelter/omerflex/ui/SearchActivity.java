package net.typeblog.shelter.omerflex.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import net.typeblog.shelter.R;

import androidx.appcompat.app.AppCompatActivity;

import net.typeblog.shelter.omerflex.entity.Artist;
import net.typeblog.shelter.omerflex.entity.ArtistList;
import net.typeblog.shelter.omerflex.servers.AkwamController;
import net.typeblog.shelter.omerflex.servers.ControllableServer;

import java.util.ArrayList;
import java.util.List;

/**
 * First activity
 * after the User enter his query in the MainActivity
 * MainActivity Start a SearchActivity in order to search in all
 * Available servers and add the search result in a list.
 * - search and return the result in a list
 * - start a new GroupOfGroupActivity if the result is a group of seasons
 * - start a new GroupActivity if the result is season of episode
 * - start a new ItemActivity if the result is an item
 */
public class SearchActivity extends AppCompatActivity {

    //variable for back button confirmation
    private long backPressedTime;
    String TAG = "SearchActivity";
    ControllableServer server;

    static List<Artist> searchArtistList;
    ArtistList searchAdapter;
    Intent receivedIntent;
    String query;

    ListView listViewArtists;

    Artist artist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.omarflex_activity_search);

        listViewArtists = (ListView) findViewById(R.id.listViewArtist);

        searchArtistList = new ArrayList<>();
        searchAdapter = new ArtistList(SearchActivity.this, searchArtistList);
        listViewArtists.setAdapter(searchAdapter);

        receivedIntent = getIntent();
        artist = new Artist();
        artist.setUrl(receivedIntent.getStringExtra("ARTIST_URL"));
        artist.setName(receivedIntent.getStringExtra("ARTIST_NAME"));
        artist.setImage(receivedIntent.getStringExtra("ARTIST_IMAGE"));
        artist.setServer(receivedIntent.getStringExtra("ARTIST_SERVER"));
        artist.setIsVideo(receivedIntent.getExtras().getBoolean("ARTIST_IS_VIDEO"));
        query = receivedIntent.getStringExtra("QUERY");

        listViewArtists.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Artist artist = searchArtistList.get(position);
                server = determineServer(artist, searchArtistList, SearchActivity.this, searchAdapter); //determine which server to use
                //determineNextActivity(artist, SearchActivity.this);
                server.fetch(artist);
            }
        });

        start();
    }

    private void start() {
        server = new AkwamController(searchArtistList, SearchActivity.this, searchAdapter);
        server.search(query);

/*
        server = new OldAkwamController(searchArtistList, SearchActivity.this, searchAdapter);
        server.search(query);

        try {
            Thread.sleep(400);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

     */

//      server = new MyCimaController(searchArtistList, SearchActivity.this, searchAdapter);
//        server.search(query);

       /* server = new Shahid4uController(searchArtistList, SearchActivity.this, searchAdapter);
        server.search(query);

        server = new Cima4uController(searchArtistList, SearchActivity.this, searchAdapter);
        server.search(query);

        server = new FaselHdController(searchArtistList, SearchActivity.this, searchAdapter);
        server.search(query);

        */

        listViewArtists.setAdapter(searchAdapter);
    }

    public void determineNextActivity(Artist artist, Activity activity){
        Intent activityIntent = null;

        switch (artist.getState()) {
            case Artist.GROUP_OF_GROUP_STATE:
                activityIntent = new Intent(activity, GroupOfGroupActivity.class);
                break;
            case Artist.GROUP_STATE:
                activityIntent = new Intent(activity, GroupActivity.class);
                break;
            case Artist.ITEM_STATE:
                activityIntent = new Intent(activity, ItemActivity.class);
                break;
            case Artist.RESOLUTION_STATE:
                activityIntent = new Intent(activity, ResolutionsListActivity.class);
                break;
            default:
                activityIntent = new Intent(activity, ItemActivity.class);
                break;
        }
        activityIntent.putExtra("ARTIST_URL", artist.getUrl());
        activityIntent.putExtra("ARTIST_NAME", artist.getName());
        activityIntent.putExtra("ARTIST_IMAGE", artist.getImage());
        activityIntent.putExtra("ARTIST_SERVER", artist.getServer());
        activityIntent.putExtra("ARTIST_STATE", artist.getState());
        activityIntent.putExtra("ARTIST_IS_VIDEO", artist.getIsVideo());
        startActivity(activityIntent);
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