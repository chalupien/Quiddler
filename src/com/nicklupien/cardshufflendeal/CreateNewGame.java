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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class CreateNewGame extends Activity {

    String imageUrl = "http://nicklupien.com/quiddler/images/";
    String userNickName = "";
    String userID = "";
    String gameId = "";
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
        setContentView(R.layout.activity_create_game);

        Intent i = getIntent();
        // String email = i.getStringExtra("email");
        userNickName = i.getStringExtra("nickname");
        userID = i.getStringExtra("userID");

        ArrayList<HashMap<String, String>> mylist = new ArrayList<HashMap<String, String>>();

        adapter = new SimpleAdapter(this, mylist, R.layout.mygames,
                new String[] { "game", "name" }, new int[] { R.id.item_title,
                        R.id.item_subtitle });

        lv = (ListView) findViewById(android.R.id.list);

        ImageView mChart = (ImageView) findViewById(R.id.imview);
        new DownloadImageFromServer(mChart).execute(imageUrl + "png1.png");

        gameStatusOfPlayers = (TextView) this.findViewById(R.id.user_label);

        new CreateGame(CreateNewGame.this).execute();

        Button beginThisGame = (Button) findViewById(R.id.ButtonBeginGame);
        beginThisGame.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                new BeginGameAndDealCards(CreateNewGame.this, 0).execute();
            }

        });

        Button deleteThisGame = (Button) findViewById(R.id.ButtonDeleteGame);
        deleteThisGame.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                new DeleteGame(gameId).execute();
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
                        CreateNewGame.this.finish();
                        startActivity(selectGametoPlay);

                    } catch (JSONException e) {

                    }

                }
            } catch (Throwable e) {

            }
            return null;

        }

    }

    class CreateGame extends AsyncTask<String, Void, JSONObject> {

        private final ProgressDialog dialog;
        private final CreateNewGame activity;

        public CreateGame(CreateNewGame createNewGame) {
            this.activity = createNewGame;
            CreateNewGame context = activity;
            dialog = new ProgressDialog(context);
        }

        @Override
        protected void onPreExecute() {
            this.dialog.setMessage("Creating Game...");
            this.dialog.show();
        }

        @Override
        protected JSONObject doInBackground(String... urls) {

            try {

                JSONObject json = new JSONObject();
                json.put("nickname", userNickName);
                json.put("userid", userID);
                json.put("gamename", "");
                HttpParams httpParams = new BasicHttpParams();
                HttpConnectionParams.setConnectionTimeout(httpParams, 6500);
                HttpConnectionParams.setSoTimeout(httpParams, 6500);
                HttpClient client = new DefaultHttpClient(httpParams);

                HttpPost request = new HttpPost(
                        "http://nicklupien.com/quiddler/playerfunctions.php?action=createNewGame");
                request.setEntity(new ByteArrayEntity(json.toString().getBytes(
                        "UTF8")));
                request.setHeader("json", json.toString());
                HttpResponse response = client.execute(request);
                HttpEntity entity = response.getEntity();
                // If the response does not enclose an entity, there is no need
                if (entity != null) {

                    InputStream in = response.getEntity().getContent();
                    String a = convertStreamToString(in);

                    // TextView playerStatus = (TextView) this
                    // .findViewById(R.id.playerStatus);

                    try {

                        @SuppressWarnings("unused")
                        String setTexttoThis = "";
                        JSONObject topobj = new JSONObject(a);

                        JSONArray viewPlayersJoined = topobj
                                .getJSONArray("newgame");
                        JSONObject e = viewPlayersJoined.getJSONObject(0);

                        gameId = e.getString("id");

                    } catch (Throwable e) {
                    }

                }
            } catch (Throwable e) {
            }

            return null;

        }

        @Override
        protected void onPostExecute(JSONObject jsonobj) {

            if (dialog.isShowing()) {
                dialog.dismiss();
            }

            callRepeatingGetAvailPlayers(CreateNewGame.this);

        }

    }

    public void callRepeatingGetAvailPlayers(
            final CreateNewGame findAvailPlayers) {
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
        private final CreateNewGame activity;

        public RetreiveJoinedPlayers(CreateNewGame newGame) {

            this.activity = newGame;
            CreateNewGame context = activity;
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

                }
            } catch (JSONException e) {
                Log.e("log_tag", "Error parsing data " + e.toString());
            }

        }
    }

    class BeginGameAndDealCards extends AsyncTask<String, Void, JSONObject> {

        private final ProgressDialog dialog;
        private final CreateNewGame activity;
        private final int roundNo;

        public BeginGameAndDealCards(CreateNewGame newGame, int round) {

            roundNo = round;
            this.activity = newGame;
            CreateNewGame context = activity;
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

            CreateNewGame.this.finish();
            startActivity(joinGametoPlay);

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
