package com.example.eventsnapqr;

import android.os.AsyncTask;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class ReverseGeocodingTask extends AsyncTask<GeoPoint, Void, String> {
    private EditText addressTextBox;

    public ReverseGeocodingTask(EditText addressTextBox) {
        this.addressTextBox = addressTextBox;
    }

    @Override
    protected String doInBackground(GeoPoint... geoPoints) {
        GeoPoint geoPoint = geoPoints[0];
        String address = "";

        try {
            URL url = new URL("https://nominatim.openstreetmap.org/reverse?format=json&lat=" + geoPoint.getLatitude() + "&lon=" + geoPoint.getLongitude());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();

            InputStreamReader inputStreamReader = new InputStreamReader(conn.getInputStream());
            StringBuilder response = new StringBuilder();
            int data = inputStreamReader.read();
            while (data != -1) {
                response.append((char) data);
                data = inputStreamReader.read();
            }

            JSONObject jsonResponse = new JSONObject(response.toString());
            address = jsonResponse.getString("display_name");

            inputStreamReader.close();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        return address;
    }

    @Override
    protected void onPostExecute(String address) {
        addressTextBox.setText(address);
    }
}
