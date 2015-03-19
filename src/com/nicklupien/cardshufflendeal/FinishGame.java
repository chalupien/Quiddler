package com.nicklupien.cardshufflendeal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class FinishGame extends Activity {

    String imageUrl = "http://nicklupien.com/quiddler/images/";
    String playerAction = "http://nicklupien.com/quiddler/playerfunctions.php";
    String userNickName = "";
    String gameId = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finish_game);

        ImageView mChart = (ImageView) findViewById(R.id.imview);
        new DownloadImageFromServer(mChart).execute(imageUrl + "png1.png");

        TextView userLabel = (TextView) findViewById(R.id.user_label);

        Intent i = getIntent();
        // String email = i.getStringExtra("email");
        userNickName = i.getStringExtra("userNickName");
        gameId = i.getStringExtra("gameId");

        new getPlayerScore().execute();

        userLabel.setText("Hey " + userNickName + " good game! ");

        Button newGame = (Button) findViewById(R.id.ButtonFindGame);
        newGame.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                new RetreiveGames().execute();
            }

        });

    }

    class RetreiveGames extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... urls) {

            try {

                JSONObject json = new JSONObject();
                json.put("email", "");
                json.put("nickname", userNickName);
                HttpParams httpParams = new BasicHttpParams();
                HttpConnectionParams.setConnectionTimeout(httpParams, 6500);
                HttpConnectionParams.setSoTimeout(httpParams, 6500);
                HttpClient client = new DefaultHttpClient(httpParams);

                HttpPost request = new HttpPost(playerAction
                        + "?action=playerLogin");
                request.setEntity(new ByteArrayEntity(json.toString().getBytes(
                        "UTF8")));
                request.setHeader("json", json.toString());
                HttpResponse response = client.execute(request);
                HttpEntity entity = response.getEntity();

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
                        FinishGame.this.finish();
                        startActivity(selectGametoPlay);

                    } catch (JSONException e) {

                    }

                }
            } catch (Throwable e) {

            }
            return null;

        }

    }

    class getPlayerScore extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {

            String a = null;
            try {

                JSONObject json = new JSONObject();

                json.put("gameid", gameId);
                json.put("nickname", userNickName);

                HttpParams httpParams = new BasicHttpParams();
                HttpConnectionParams.setConnectionTimeout(httpParams, 6500);
                HttpConnectionParams.setSoTimeout(httpParams, 6500);
                HttpClient client = new DefaultHttpClient(httpParams);

                HttpPost request = new HttpPost(playerAction
                        + "?action=getFinalScore");
                request.setEntity(new ByteArrayEntity(json.toString().getBytes(
                        "UTF8")));
                request.setHeader("json", json.toString());
                HttpResponse response = client.execute(request);

                InputStream in = response.getEntity().getContent();
                a = convertStreamToString(in);

            } catch (Throwable e) {
                // e.printStackTrace();
            }

            return a;

        }

        @Override
        protected void onPostExecute(String a) {

            if (a != "") {

                try {

                    JSONArray jsonArray = null;

                    jsonArray = new JSONArray(a);

                    String finalPlayerScore = jsonArray.getJSONObject(0)
                            .getString(userNickName).toString();

                    TextView userLabel = (TextView) findViewById(R.id.user_label);

                    userLabel.setText(userLabel.getText()
                            + "\n\nYou finished with a score of "
                            + finalPlayerScore + " points");

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

        }

    }

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
}
