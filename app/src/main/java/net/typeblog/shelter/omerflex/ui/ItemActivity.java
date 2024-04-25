package net.typeblog.shelter.omerflex.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import net.typeblog.shelter.omerflex.entity.Artist;
import net.typeblog.shelter.R;
import net.typeblog.shelter.omerflex.entity.ArtistList;
import net.typeblog.shelter.omerflex.servers.AkwamController;
import net.typeblog.shelter.omerflex.servers.ControllableServer;

import java.util.ArrayList;
import java.util.List;

/**
 * starts if:
 * - search result is an item
 * - after a GroupActivity result is selected
 */
public class ItemActivity extends AppCompatActivity {


    //variable for back button confirmation
    private long backPressedTime;
    String TAG = "ItemActivity";
    ControllableServer server;

    static List<Artist> itemArtistList;
    ArtistList itemAdapter;
    Intent receivedIntent;
    TextView descriptionTextView;

    ListView listViewArtists;

    Artist artist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.omarflex_activity_item);

        listViewArtists = (ListView) findViewById(R.id.listViewArtist);

        itemArtistList = new ArrayList<>();
        itemAdapter = new ArtistList(ItemActivity.this, itemArtistList);
        listViewArtists.setAdapter(itemAdapter);

        descriptionTextView = (TextView) findViewById(R.id.textViewDesc);
        descriptionTextView.setMovementMethod(new ScrollingMovementMethod());

        receivedIntent = getIntent();
        artist = new Artist();
        artist.setUrl(receivedIntent.getStringExtra("ARTIST_URL"));
        artist.setName(receivedIntent.getStringExtra("ARTIST_NAME"));
        artist.setImage(receivedIntent.getStringExtra("ARTIST_IMAGE"));
        artist.setServer(receivedIntent.getStringExtra("ARTIST_SERVER"));
        artist.setIsVideo(receivedIntent.getExtras().getBoolean("ARTIST_IS_VIDEO"));

        server = determineServer(artist, itemArtistList,ItemActivity.this, itemAdapter); //determine which server to use

        listViewArtists.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Artist artist = itemArtistList.get(position);
                //server.fetchResolutions(artist);
                server.fetch(artist);
            }
        });

        start();
    }
    private void start() {
        server.fetchItem(artist);
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
        }
        activityIntent.putExtra("ARTIST_URL", artist.getUrl());
        activityIntent.putExtra("ARTIST_NAME", artist.getName());
        activityIntent.putExtra("ARTIST_IMAGE", artist.getImage());
        activityIntent.putExtra("ARTIST_SERVER", artist.getServer());
        activityIntent.putExtra("ARTIST_STATE", artist.getState());
        activityIntent.putExtra("ARTIST_IS_VIDEO", artist.getIsVideo());
        startActivity(activityIntent);
    }

}