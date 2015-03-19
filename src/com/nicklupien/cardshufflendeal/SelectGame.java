package com.nicklupien.cardshufflendeal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class SelectGame extends Activity {

    String imageUrl = "http://nicklupien.com/quiddler/images/";
    String playerAction = "http://nicklupien.com/quiddler/playerfunctions.php?";

    Timer timer = new Timer();
    Timer timer2 = new Timer();

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_game);

        ImageView mChart = (ImageView) findViewById(R.id.imview);
        new DownloadImageFromServer(mChart).execute(imageUrl + "png2.png");

        TextView userLabel = (TextView) findViewById(R.id.user_label);

        Intent i = getIntent();
        // String email = i.getStringExtra("email");
        final String userNickName = i.getStringExtra("userNickName");
        final String userID = i.getStringExtra("userID");
        final String userInfo = i.getStringExtra("userInfo");

        userLabel.setText("Hey " + userNickName
                + ", select a game or start a new one. \n" + userInfo);

        ArrayList<HashMap<String, String>> mylist = new ArrayList<HashMap<String, String>>();

        ListAdapter adapter = new SimpleAdapter(this, mylist,
                R.layout.playablegames, new String[] { "game", "name" },
                new int[] { R.id.item_title, R.id.item_subtitle });

        ArrayList<HashMap<String, String>> mylist2 = new ArrayList<HashMap<String, String>>();

        ListAdapter adapter2 = new SimpleAdapter(this, mylist2,
                R.layout.mygames, new String[] { "game", "name" }, new int[] {
                        R.id.item_title2, R.id.item_subtitle2 });

        // new RetreiveAvailableGames(SelectGame.this, userNickName, adapter,
        // mylist).execute();

        toCallAsynchronous(SelectGame.this, userNickName, userID, adapter,
                mylist);

        toCallAsynchronous2(SelectGame.this, userNickName, userID, adapter2,
                mylist2);

        Button newGame = (Button) findViewById(R.id.NewGameButton);
        newGame.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                Intent createNewGameIntent = new Intent(
                        getApplicationContext(), CreateNewGame.class);
                createNewGameIntent.putExtra("nickname", userNickName);
                createNewGameIntent.putExtra("userID", userID);
                createNewGameIntent.putExtra("gamename", "");
                // createNewGameIntent.putExtra("id", o.get("id"));
                startActivity(createNewGameIntent);
            }

        });

    }

    public void toCallAsynchronous(final SelectGame selectGame,
            final String userNickName, final String userID,
            final ListAdapter adapter,
            final ArrayList<HashMap<String, String>> mylist) {
        TimerTask doAsynchronousTask;
        final Handler handler = new Handler();

        doAsynchronousTask = new TimerTask() {
            @Override
            public void run() {

                handler.post(new Runnable() {
                    public void run() {

                        try {
                            new RetreiveAvailableGames(selectGame,
                                    userNickName, userID, adapter, mylist)
                                    .execute();

                        } catch (Exception e) {

                        }

                    }
                });

            }

        };

        timer.schedule(doAsynchronousTask, 0, 10000);// execute in every 50000
                                                     // ms

    }

    class RetreiveAvailableGames extends AsyncTask<String, Void, JSONObject> {

        private final ProgressDialog dialog;
        private final SelectGame activity;

        ArrayList<HashMap<String, String>> thisMylist = null;
        ListAdapter thisAdapter = null;
        String userNickName = "";
        String userID = "";

        public RetreiveAvailableGames(SelectGame selectGame, String userNick,
                String user, ListAdapter adapter,
                ArrayList<HashMap<String, String>> mylist) {

            this.activity = selectGame;
            SelectGame context = activity;
            dialog = new ProgressDialog(context);

            thisAdapter = adapter;
            thisMylist = mylist;
            thisMylist.clear();
            userNickName = userNick;
            userID = user;
        }

        @Override
        protected void onPreExecute() {
            // this.dialog.setMessage("Loading Games...");
            // this.dialog.show();
        }

        @Override
        protected JSONObject doInBackground(String... urls) {

            JSONObject json = JSONfunctions
                    .getJSONfromURL("http://nicklupien.com/quiddler/playerfunctions.php?action=viewAvailableGames");

            return json;
        }

        @Override
        protected void onPostExecute(JSONObject json) {

            if (dialog.isShowing()) {
                // dialog.dismiss();
            }

            if (json != null) {
                try {

                    JSONArray selectGameById = json.getJSONArray("games");

                    for (int i = 0; i < selectGameById.length(); i++) {
                        HashMap<String, String> map = new HashMap<String, String>();
                        JSONObject e = selectGameById.getJSONObject(i);

                        map.put("id", e.getString("id"));
                        map.put("game", e.getString("game"));
                        map.put("name", e.getString("desc"));
                        map.put("round", e.getString("round"));
                        map.put("players", e.getString("players"));
                        // index.php?action=startRound&gameid=1001&roundnum=8&players=nick|mike|greg|kelly
                        thisMylist.add(map);
                    }
                } catch (JSONException e) {
                    Log.e("log_tag", "Error parsing data " + e.toString());
                }

                final ListView lv = (ListView) findViewById(R.id.listopengames);

                lv.setAdapter(thisAdapter);

                // AVAILBLE GAMES TO JOIN

                lv.setTextFilterEnabled(true);
                lv.setOnItemClickListener(new OnItemClickListener() {
                    public void onItemClick(AdapterView<?> parent, View view,
                            int position, long id) {
                        @SuppressWarnings("unchecked")
                        HashMap<String, String> o = (HashMap<String, String>) lv
                                .getItemAtPosition(position);

                        Intent joinGametoWait = new Intent(
                                getApplicationContext(),
                                WaitForGameToStart.class);

                        joinGametoWait.putExtra("gameId", o.get("id"));
                        joinGametoWait.putExtra("gameName", o.get("name"));
                        joinGametoWait.putExtra("gameGame", o.get("game"));
                        joinGametoWait.putExtra("gameRound",
                                Integer.parseInt(o.get("round")));
                        joinGametoWait.putExtra("userNickName", userNickName);
                        joinGametoWait.putExtra("userID", userID);

                        String getPlayerList = o.get("players").toString();
                        int skippedOrNot = 0;

                        if (getPlayerList.contains(userNickName)) {
                            skippedOrNot = 1;
                            // && (Integer.parseInt(o.get("round")) > 0)
                        }

                        joinGametoWait.putExtra("skipToPlay", skippedOrNot);

                        timer.cancel();
                        timer2.cancel();

                        startActivity(joinGametoWait);

                        // Toast.makeText(Main.this, "ID '" + o.get("id") +
                        // "' was clicked.", Toast.LENGTH_SHORT).show();

                    }
                });

            }

        }

    }

    public void toCallAsynchronous2(final SelectGame selectGame,
            final String userNickName, final String userID,
            final ListAdapter adapter2,
            final ArrayList<HashMap<String, String>> mylist2) {
        TimerTask doAsynchronousTask2;
        final Handler handler2 = new Handler();

        doAsynchronousTask2 = new TimerTask() {
            @Override
            public void run() {

                handler2.post(new Runnable() {
                    public void run() {

                        try {
                            new RetreiveAvailableGames2(selectGame,
                                    userNickName, userID, adapter2, mylist2)
                                    .execute();

                        } catch (Exception e) {

                        }

                    }
                });

            }

        };

        timer2.schedule(doAsynchronousTask2, 0, 10000);// execute in every 50000
                                                       // ms

    }

    class RetreiveAvailableGames2 extends AsyncTask<String, Void, JSONObject> {

        private final ProgressDialog dialog;
        private final SelectGame activity;

        ArrayList<HashMap<String, String>> thisMylist2 = null;
        ListAdapter thisAdapter2 = null;
        private String thisUserNickName2 = "";
        String userID = "";
        String thisURL = "";

        public RetreiveAvailableGames2(SelectGame selectGame, String userNick,
                String user, ListAdapter adapter,
                ArrayList<HashMap<String, String>> mylist) {

            this.activity = selectGame;
            SelectGame context = activity;
            dialog = new ProgressDialog(context);

            thisAdapter2 = adapter;
            thisMylist2 = mylist;
            thisMylist2.clear();
            thisUserNickName2 = userNick;
            userID = user;
            thisURL = "http://nicklupien.com/quiddler/playerfunctions.php?action=viewAvailableGames&nickname="
                    + thisUserNickName2;
        }

        @Override
        protected void onPreExecute() {
            // this.dialog.setMessage("Loading Games...");
            // this.dialog.show();
        }

        @Override
        protected JSONObject doInBackground(String... urls) {

            JSONObject json = JSONfunctions.getJSONfromURL(thisURL);

            return json;
        }

        @Override
        protected void onPostExecute(JSONObject json) {

            if (dialog.isShowing()) {
                // dialog.dismiss();
            }

            if (json != null) {
                try {

                    JSONArray selectGameById2 = json.getJSONArray("games");

                    for (int i = 0; i < selectGameById2.length(); i++) {
                        HashMap<String, String> map2 = new HashMap<String, String>();
                        JSONObject e2 = selectGameById2.getJSONObject(i);

                        map2.put("id", e2.getString("id"));
                        map2.put("game", e2.getString("game"));
                        map2.put("name", e2.getString("desc"));
                        map2.put("round", e2.getString("round"));
                        map2.put("players", e2.getString("players"));
                        map2.put("started", e2.getString("started"));

                        thisMylist2.add(map2);
                    }
                } catch (JSONException e) {
                    Log.e("log_tag", "Error parsing data " + e.toString());
                }

                final ListView lv2 = (ListView) findViewById(R.id.listmygames);

                lv2.setAdapter(thisAdapter2);

                lv2.setTextFilterEnabled(true);
                lv2.setOnItemClickListener(new OnItemClickListener() {
                    public void onItemClick(AdapterView<?> parent, View view,
                            int position, long id) {
                        @SuppressWarnings("unchecked")
                        HashMap<String, String> o = (HashMap<String, String>) lv2
                                .getItemAtPosition(position);

                        if (Integer.parseInt(o.get("started")) == 1) {

                            Intent joinGametoPlay = new Intent(
                                    getApplicationContext(), JoinGame.class);

                            joinGametoPlay.putExtra("gameId", o.get("id"));
                            joinGametoPlay.putExtra("gameName", o.get("name"));
                            joinGametoPlay.putExtra("gameGame", o.get("game"));
                            joinGametoPlay.putExtra("gameRound",
                                    Integer.parseInt(o.get("round")));
                            joinGametoPlay.putExtra("userNickName",
                                    thisUserNickName2);
                            joinGametoPlay.putExtra("userID", userID);

                            String getPlayerList = o.get("players").toString();
                            int skippedOrNot = 0;

                            if (getPlayerList.contains(thisUserNickName2)) {
                                skippedOrNot = 1;
                                // && (Integer.parseInt(o.get("round")) > 0)
                            }

                            joinGametoPlay.putExtra("skipToPlay", skippedOrNot);

                            timer.cancel();
                            timer2.cancel();

                            startActivity(joinGametoPlay);

                        }

                        else {

                            Intent resumeGametoPlay = new Intent(
                                    getApplicationContext(),
                                    ResumeExistingGame.class);

                            resumeGametoPlay.putExtra("gameId", o.get("id"));
                            resumeGametoPlay.putExtra("gameName", o.get("name"));
                            resumeGametoPlay.putExtra("gameGame", o.get("game"));
                            resumeGametoPlay.putExtra("gameRound",
                                    Integer.parseInt(o.get("round")));
                            resumeGametoPlay.putExtra("userNickName",
                                    thisUserNickName2);
                            resumeGametoPlay.putExtra("userID", userID);

                            String getPlayerList = o.get("players").toString();
                            int skippedOrNot = 0;

                            if (getPlayerList.contains(thisUserNickName2)) {
                                skippedOrNot = 1;
                                // && (Integer.parseInt(o.get("round")) > 0)
                            }

                            resumeGametoPlay.putExtra("skipToPlay",
                                    skippedOrNot);

                            timer.cancel();
                            timer2.cancel();

                            startActivity(resumeGametoPlay);

                        }

                        // Toast.makeText(Main.this, "ID '" + o.get("id") +
                        // "' was clicked.", Toast.LENGTH_SHORT).show();

                    }
                });

            }

        }

    }

    public void onPause(View v) {
        timer.cancel();
        timer2.cancel();
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
