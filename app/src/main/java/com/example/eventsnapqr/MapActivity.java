package com.example.eventsnapqr;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.views.MapView;

/**
 * activity that hosts the OSMaps, for viewing attendee check ins
 * I used OpenAI: chatGPT to get the structure of how to host a map fragment
 *  using osmdroid. Prompt "How to host a map fragment" and also had prompts where I
 *  entered errors I received on the way to ask for the reason and possible solution
 *  I used the video bellow to set up the MapActivity to host the map fragment
 * "https://www.youtube.com/watch?v=xoFtgcOoO1I", channel name: Mehdi Haghgoo
 */
public class MapActivity extends AppCompatActivity {

    /**
     * What should be executed when the fragment is created
     * This hosts the map fragment, using it so its faster for the map to
     * load, almost like a pre launching/populating the map
     * @param savedInstanceState If the fragment is being re-created from
     * a previous saved state, this is the state.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        Configuration.getInstance().load(getApplicationContext(), getSharedPreferences("osmdroid", MODE_PRIVATE));
        MapView mapView = (MapView) findViewById(R.id.mapView);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setBuiltInZoomControls(true);
    }

    /**
     * what actions to be taken when the activity is resumed, it loads up the configuration, I assume the
     * previous left out config of the map.
     */
    @Override
    protected void onResume() {
        super.onResume();
        Configuration.getInstance().load(getApplicationContext(), getSharedPreferences("osmdroid", MODE_PRIVATE));
    }
}
