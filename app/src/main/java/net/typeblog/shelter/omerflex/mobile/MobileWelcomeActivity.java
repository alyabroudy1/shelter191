package net.typeblog.shelter.omerflex.mobile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import net.typeblog.shelter.R;
import net.typeblog.shelter.omerflex.ui.OmarflexMainActivity;


public class MobileWelcomeActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mobile_welcome);
        Intent mainActivity = new Intent(this, OmarflexMainActivity.class);
        startActivity(mainActivity);
        finish();
    }
}
