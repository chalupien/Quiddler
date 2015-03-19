package com.nicklupien.cardshufflendeal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Random;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

/*
 * Author Nick Lupien Dec 2012
 * 
 * */

public class MainActivity extends Activity {

    String path = "http://nicklupien.com/quiddler/index.php";
    String playerAction = "http://nicklupien.com/quiddler/playerfunctions.php";

    String imageUrl = "http://nicklupien.com/quiddler/images/";
    Random r = new Random();

    Bitmap bmImg;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // if (isNetworkConnected()) {

        ImageView mChart = (ImageView) findViewById(R.id.imview);
        new DownloadImageFromServer(mChart).execute(imageUrl + "png1.png");

        try {

            Object mediaPlayer1 = MediaPlayer
                    .create(this,
                            Uri.parse("http://www.gstatic.com/dictionary/static/sounds/de/0/quid.mp3"));
            ((MediaPlayer) mediaPlayer1).start();

            Thread.sleep(150);

            Object mediaPlayer2 = MediaPlayer
                    .create(this,
                            Uri.parse("http://www.gstatic.com/dictionary/static/sounds/de/0/ill.mp3"));

            ((MediaPlayer) mediaPlayer2).start();

            Thread.sleep(60);

            Object mediaPlayer3 = MediaPlayer
                    .create(this,
                            Uri.parse("http://www.gstatic.com/dictionary/static/sounds/de/0/yer.mp3"));

            ((MediaPlayer) mediaPlayer3).start();

        } catch (Exception e) {

            e.printStackTrace();
        }

        // } else {
        // Toast.makeText(this,
        // "Error: Need Internet Connection",Toast.LENGTH_LONG).show();
        // }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
        case R.id.menu_settings:
            Intent showInstructions = new Intent(getApplicationContext(),
                    InfoScreen.class);
            startActivity(showInstructions);
            return true;
        case R.id.menu_quit:
            this.finish();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    public void searchForGame(View v) {

        new RetreiveGames(MainActivity.this).execute();

    }

    class RetreiveGames extends AsyncTask<String, Void, JSONObject> {

        private final ProgressDialog dialog;
        private final MainActivity activity;

        public RetreiveGames(MainActivity mainActivity) {
            this.activity = mainActivity;
            MainActivity context = activity;
            dialog = new ProgressDialog(context);
        }

        @Override
        protected void onPreExecute() {
            this.dialog.setMessage("Connecting...");
            this.dialog.show();
        }

        @Override
        protected JSONObject doInBackground(String... urls) {

            try {

                EditText email = (EditText) findViewById(R.id.editText1);
                EditText nickname = (EditText) findViewById(R.id.editText2);

                JSONObject json = new JSONObject();
                json.put("email", email.getText().toString());
                json.put("nickname", nickname.getText().toString());
                HttpParams httpParams = new BasicHttpParams();
                HttpConnectionParams.setConnectionTimeout(httpParams, 6500);
                HttpConnectionParams.setSoTimeout(httpParams, 6500);
                HttpClient client = new DefaultHttpClient(httpParams);

                // "json={\"UserName\":1,\"FullName\":2}";

                HttpPost request = new HttpPost(playerAction
                        + "?action=playerLogin");
                request.setEntity(new ByteArrayEntity(json.toString().getBytes(
                        "UTF8")));
                request.setHeader("json", json.toString());
                HttpResponse response = client.execute(request);
                HttpEntity entity = response.getEntity();
                // If the response does not enclose an entity, there is no need
                if (entity != null) {

                    InputStream in = response.getEntity().getContent();
                    String a = convertStreamToString(in);

                    try {

                        String userInfo = "";
                        JSONObject topobj = new JSONObject(a);

                        String userEmail = topobj.getString("email");
                        String userNickName = topobj.getString("nickname");
                        String userID = topobj.getString("userid");
                        String userWinLossRecord = topobj
                                .getString("winlossrec");
                        int activePLayers = topobj.getInt("activeplayers");
                        int getPlayerStatus = topobj.getInt("playerstatus");

                        switch (getPlayerStatus) {

                        case 0:
                            userInfo = "Welcome " + userNickName
                                    + ", There are " + activePLayers
                                    + " active players online.";
                            break;

                        case 1:
                            userInfo = " Your record is: " + userWinLossRecord
                                    + ", There are " + activePLayers
                                    + " active players.";
                            break;
                        }

                        //
                        Intent selectGametoPlay = new Intent(
                                getApplicationContext(), SelectGame.class);
                        selectGametoPlay.putExtra("userEmail", userEmail);
                        selectGametoPlay.putExtra("userID", userID);
                        selectGametoPlay.putExtra("userNickName", userNickName);
                        selectGametoPlay.putExtra("userInfo", userInfo);

                        MainActivity.this.finish();
                        startActivity(selectGametoPlay);

                    } catch (JSONException e) {

                        // e.printStackTrace();
                    }

                }
            } catch (Throwable e) {
                // e.printStackTrace();
            }
            return null;

        }

        @Override
        protected void onPostExecute(JSONObject jsonobj) {

            if (dialog.isShowing()) {
                dialog.dismiss();
            }

        }

    }

    /*
     * I would like to move this function to a class soon
     * 
     * 
     * 
     * 
     * NOW
     */

    private String convertStreamToString(InputStream is) {

        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

}
