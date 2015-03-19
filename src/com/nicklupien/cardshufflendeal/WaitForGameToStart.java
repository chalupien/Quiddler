package com.nicklupien.cardshufflendeal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

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
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class WaitForGameToStart extends Activity {

    String imageUrl = "http://nicklupien.com/quiddler/images/";
    String userNickName = "";
    String userID = "";
    String gameId = "";
    String gameRound = "";
    String playerAction = "http://nicklupien.com/quiddler/playerfunctions.php";
    String gameAction = "http://nicklupien.com/quiddler/index.php";

    Timer timer = new Timer();

    ArrayList<HashMap<String, String>> mylist = null;

    ListAdapter adapter = null;
    TextView gameStatusOfPlayers = null;
    ListView lv = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wait_game);

        Intent i = getIntent();
        // String email = i.getStringExtra("email");
        userNickName = i.getStringExtra("userNickName");
        gameId = i.getStringExtra("gameId");

        ImageView mChart = (ImageView) findViewById(R.id.imview);
        new DownloadImageFromServer(mChart).execute(imageUrl + "png1.png");

        gameStatusOfPlayers = (TextView) this.findViewById(R.id.user_label);

        new JoinThisGame(WaitForGameToStart.this).execute();

    }

    // Join Game
    class JoinThisGame extends AsyncTask<String, Void, JSONObject> {

        private final ProgressDialog dialog;
        private final WaitForGameToStart activity;

        public JoinThisGame(WaitForGameToStart joinGame) {
            this.activity = joinGame;
            WaitForGameToStart context = activity;

            dialog = new ProgressDialog(context);

        }

        @Override
        protected void onPreExecute() {
            this.dialog.setMessage("Joining Game...");
            this.dialog.show();
        }

        @Override
        protected JSONObject doInBackground(String... urls) {

            try {

                JSONObject json = new JSONObject();
                json.put("nickname", userNickName);
                json.put("gameid", gameId);
                HttpParams httpParams = new BasicHttpParams();
                HttpConnectionParams.setConnectionTimeout(httpParams, 6500);
                HttpConnectionParams.setSoTimeout(httpParams, 6500);
                HttpClient client = new DefaultHttpClient(httpParams);

                HttpPost request = new HttpPost(playerAction
                        + "?action=joinGame");
                request.setEntity(new ByteArrayEntity(json.toString().getBytes(
                        "UTF8")));
                request.setHeader("json", json.toString());
                HttpResponse response = client.execute(request);
                response.getEntity();

            } catch (Throwable e) {

            }
            return null;

        }

        @Override
        protected void onPostExecute(JSONObject jsonobj) {

            callRepeatingGetAvailPlayers(WaitForGameToStart.this);

            if (dialog.isShowing()) {
                dialog.dismiss();
            }

        }

    }

    // End Join Game

    public void callRepeatingGetAvailPlayers(
            final WaitForGameToStart findAvailPlayers) {
        TimerTask callRepeatingGetAvailPlayersTask;
        final Handler handler = new Handler();

        callRepeatingGetAvailPlayersTask = new TimerTask() {
            @Override
            public void run() {

                handler.post(new Runnable() {
                    public void run() {

                        try {

                            new RetreiveJoinedPlayers(findAvailPlayers)
                                    .execute();

                        } catch (Exception e) {

                        }

                    }
                });

            }

        };

        timer.schedule(callRepeatingGetAvailPlayersTask, 0, 10000);

    }

    class RetreiveJoinedPlayers extends AsyncTask<String, Void, JSONObject> {

        private final ProgressDialog dialog;
        private final WaitForGameToStart activity;

        public RetreiveJoinedPlayers(WaitForGameToStart newGame) {

            this.activity = newGame;
            WaitForGameToStart context = activity;
            dialog = new ProgressDialog(context);

        }

        @Override
        protected void onPreExecute() {
            this.dialog.setMessage("Updating Player List...");
            this.dialog.show();
        }

        @Override
        protected JSONObject doInBackground(String... urls) {

            JSONObject topobj = new JSONObject();

            try {

                JSONObject json = new JSONObject();
                json.put("gameid", gameId);
                HttpParams httpParams = new BasicHttpParams();
                HttpConnectionParams.setConnectionTimeout(httpParams, 6500);
                HttpConnectionParams.setSoTimeout(httpParams, 6500);
                HttpClient client = new DefaultHttpClient(httpParams);

                // "json={\"UserName\":1,\"FullName\":2}";

                HttpPost request = new HttpPost(playerAction
                        + "?action=viewPlayers");
                request.setEntity(new ByteArrayEntity(json.toString().getBytes(
                        "UTF8")));
                request.setHeader("json", json.toString());
                HttpResponse response = client.execute(request);
                InputStream in = response.getEntity().getContent();
                String a = convertStreamToString(in);

                topobj = new JSONObject(a);

            } catch (Throwable e) {

            }

            return topobj;
        }

        @Override
        protected void onPostExecute(JSONObject json) {

            if (dialog.isShowing()) {
                dialog.dismiss();
            }

            try {

                JSONArray selectGameById = json.getJSONArray("playersingame");

                for (int i = 0; i < selectGameById.length(); i++) {

                    JSONObject e = selectGameById.getJSONObject(i);

                    gameStatusOfPlayers.setText(e.getString("game") + "\n\n"
                            + e.getString("desc"));

                    if (Integer.parseInt(e.getString("started")) == 1) {

                        Intent joinGametoPlay = new Intent(
                                getApplicationContext(), JoinGame.class);

                        joinGametoPlay.putExtra("gameId", gameId);
                        joinGametoPlay.putExtra("gameRound",
                                Integer.parseInt(e.getString("round")));
                        joinGametoPlay.putExtra("userNickName", userNickName);
                        joinGametoPlay.putExtra("userID", userID);
                        joinGametoPlay.putExtra("skipToPlay", 1);

                        timer.cancel();
                        WaitForGameToStart.this.finish();
                        startActivity(joinGametoPlay);

                    }

                }
            } catch (JSONException e) {
                // Log.e("log_tag", "Error parsing data " + e.toString());
                new RetreiveGames().execute();
            }

        }
    }

    class BeginGameAndDealCards extends AsyncTask<String, Void, JSONObject> {

        private final ProgressDialog dialog;
        private final WaitForGameToStart activity;
        private final int roundNo;

        public BeginGameAndDealCards(WaitForGameToStart newGame, int round) {

            roundNo = round;
            this.activity = newGame;
            WaitForGameToStart context = activity;
            dialog = new ProgressDialog(context);

        }

        @Override
        protected void onPreExecute() {
            this.dialog.setMessage("Dealing Cards for round " + (roundNo + 1));
            this.dialog.show();
        }

        @Override
        protected JSONObject doInBackground(String... urls) {

            JSONObject topobj = new JSONObject();

            try {

                JSONObject json = new JSONObject();
                json.put("gameid", gameId);
                json.put("roundno", roundNo);
                HttpParams httpParams = new BasicHttpParams();
                HttpConnectionParams.setConnectionTimeout(httpParams, 6500);
                HttpConnectionParams.setSoTimeout(httpParams, 6500);
                HttpClient client = new DefaultHttpClient(httpParams);

                HttpPost request = new HttpPost(gameAction
                        + "?action=startRound");
                request.setEntity(new ByteArrayEntity(json.toString().getBytes(
                        "UTF8")));
                request.setHeader("json", json.toString());
                HttpResponse response = client.execute(request);
                InputStream in = response.getEntity().getContent();
                String a = convertStreamToString(in);

                topobj = new JSONObject(a);

            } catch (Throwable e) {

            }

            return topobj;
        }

        @Override
        protected void onPostExecute(JSONObject json) {

            dialog.dismiss();

            Intent joinGametoPlay = new Intent(getApplicationContext(),
                    JoinGame.class);

            joinGametoPlay.putExtra("gameId", gameId);
            joinGametoPlay.putExtra("gameName", "");
            joinGametoPlay.putExtra("gameGame", "");
            joinGametoPlay.putExtra("gameRound", 0);
            joinGametoPlay.putExtra("userNickName", userNickName);
            joinGametoPlay.putExtra("userID", userID);
            joinGametoPlay.putExtra("skipToPlay", 1);

            timer.cancel();

            WaitForGameToStart.this.finish();
            startActivity(joinGametoPlay);

        }
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
                        WaitForGameToStart.this.finish();
                        startActivity(selectGametoPlay);

                    } catch (JSONException e) {

                    }

                }
            } catch (Throwable e) {

            }
            return null;

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

    public void onPause(View v) {
        timer.cancel();

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
