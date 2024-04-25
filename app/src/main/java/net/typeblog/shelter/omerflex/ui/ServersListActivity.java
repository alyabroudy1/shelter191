package net.typeblog.shelter.omerflex.ui;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import net.typeblog.shelter.R;

/**
 * starts after ItemActivity if item has server links
 */
public class ServersListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.omarflex_activity_servers_list);
    }
}