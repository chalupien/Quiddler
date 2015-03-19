package com.nicklupien.cardshufflendeal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

public class JoinGame extends Activity {

    String path = "http://nicklupien.com/quiddler/index.php";
    String imageUrl = "http://nicklupien.com/quiddler/images/new/";
    String playerAction = "http://nicklupien.com/quiddler/playerfunctions.php";

    String gameId = "";
    String gameName = "";
    String gameGame = "";
    int gameRound = 0;
    String userNickName = "";
    String userID = "";
    int skipToPlay = 0;
    TextView status = null;
    String shownCards = "";
    String blindCards = "";
    String usedExtra = "";
    String newExtraBlind = "";
    String newExtraShown = "";
    int[] imgs = new int[11];

    private SensorManager mSensorManager;

    private ShakeEventListener mSensorListener;

    Timer timer = new Timer();
    Timer extraCardTimer = new Timer();

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_game);

        ImageView mChart = (ImageView) findViewById(R.id.imview);
        new DownloadImageFromServer(mChart).execute(imageUrl + "png3.png");

        Bundle i = getIntent().getExtras();

        gameId = i.getString("gameId");
        gameName = i.getString("gameName");
        gameGame = i.getString("gameGame");
        gameRound = i.getInt("gameRound");
        userNickName = i.getString("userNickName");
        userID = i.getString("userID");
        skipToPlay = i.getInt("skipToPlay");

        status = (TextView) this.findViewById(R.id.mainString);

        if (gameRound == 8) {

            Intent finishGame = new Intent(getApplicationContext(),
                    FinishGame.class);

            finishGame.putExtra("gameId", gameId);
            finishGame.putExtra("userNickName", userNickName);

            JoinGame.this.finish();

            startActivity(finishGame);
        }

        else {

            if (skipToPlay == 0) {
                new JoinThisGame(JoinGame.this).execute();

            }

            if (skipToPlay == 1) {

                seeIfGameHasStarted(JoinGame.this);

            }

        }

        /*
         * SHAKE IT UP
         */
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensorListener = new ShakeEventListener();

        mSensorListener
                .setOnShakeListener(new ShakeEventListener.OnShakeListener() {

                    public void onShake() {
                        Toast.makeText(JoinGame.this, "Shake it Up!",
                                Toast.LENGTH_SHORT).show();
                        shuffleCards();
                    }
                });

    }

    protected void shuffleCards() {

        imgs = new int[11];
        imgs[0] = R.id.ImageView1;
        imgs[1] = R.id.ImageView2;
        imgs[2] = R.id.ImageView3;
        imgs[3] = R.id.ImageView4;
        imgs[4] = R.id.ImageView5;
        imgs[5] = R.id.ImageView6;
        imgs[6] = R.id.ImageView7;
        imgs[7] = R.id.ImageView8;
        imgs[8] = R.id.ImageView9;
        imgs[9] = R.id.ImageView10;
        imgs[10] = R.id.ImageView11;

        for (int i = 0; i < imgs.length; i++) {

            ImageView imgViewItem = null;

            LayoutParams par = null;

            if ((i % 2) == 0) {

                imgViewItem = (ImageView) findViewById(imgs[i]);
                par = (LayoutParams) imgViewItem.getLayoutParams();

                par.leftMargin += 50;
            } else {

                imgViewItem = (ImageView) findViewById(imgs[i]);
                par = (LayoutParams) imgViewItem.getLayoutParams();

                par.leftMargin -= 110;
            }

            imgViewItem.setLayoutParams(par);

        }

    }

    public void seeIfExtraCardsHaveChanged(final String gameIdz,
            final int gameRoundz) {
        TimerTask seeIfExtraCardsHaveChangedTask;
        final Handler handler = new Handler();

        seeIfExtraCardsHaveChangedTask = new TimerTask() {
            @Override
            public void run() {

                handler.post(new Runnable() {
                    public void run() {

                        try {

                            new RetrieveNewExtraCards(gameIdz, gameRoundz)
                                    .execute();

                        } catch (Exception e) {

                        }

                    }
                });

            }

        };

        extraCardTimer.schedule(seeIfExtraCardsHaveChangedTask, 0, 5000);// execute
                                                                         // in
        // every
        // 50000
        // ms

    }

    public void seeIfGameHasStarted(final JoinGame joinGame) {
        TimerTask seeIfGameHasStartedTask;
        final Handler handler = new Handler();

        seeIfGameHasStartedTask = new TimerTask() {
            @Override
            public void run() {

                handler.post(new Runnable() {
                    public void run() {

                        try {

                            new RetrieveCards(JoinGame.this).execute();

                        } catch (Exception e) {

                        }

                    }
                });

            }

        };

        timer.schedule(seeIfGameHasStartedTask, 0, 20000);// execute in every
                                                          // 50000
        // ms

    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(mSensorListener,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        mSensorManager.unregisterListener(mSensorListener);
        super.onStop();
    }

    // Join Game
    class JoinThisGame extends AsyncTask<String, Void, JSONObject> {

        private final ProgressDialog dialog;
        private final JoinGame activity;

        public JoinThisGame(JoinGame joinGame) {
            this.activity = joinGame;
            JoinGame context = activity;

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

            seeIfGameHasStarted(JoinGame.this);

            if (dialog.isShowing()) {
                dialog.dismiss();
            }

        }

    }

    // End Join Game

    public void playWords(View v) {

        TextView wordsToPlay = (TextView) this.findViewById(R.id.wordsToPlay);
        String wordsToPlayString = wordsToPlay.getText().toString();

        // Check if player before has played?

        new PlayWords(JoinGame.this, gameId, gameRound, userNickName,
                wordsToPlayString, playerAction).execute();
    }

    class PlayWords extends AsyncTask<String, Void, String> {

        private final ProgressDialog dialog;
        private final JoinGame activity;

        String wordsToPlay = "";
        String userNickName = "";
        String gameId = "";
        String playerAction = "";
        int gameRound = 0;

        public PlayWords(JoinGame joinGame, String gId, int gRound,
                String userNick, String words, String playerActionUrl) {
            this.activity = joinGame;
            JoinGame context = activity;
            dialog = new ProgressDialog(context);

            wordsToPlay = words;
            gameId = gId;
            gameRound = gRound;
            userNickName = userNick;
            playerAction = playerActionUrl;

        }

        @Override
        protected void onPreExecute() {
            this.dialog.setMessage("Playing Words");
            this.dialog.show();
        }

        @Override
        protected String doInBackground(String... urls) {

            String a = "";

            try {

                JSONObject json = new JSONObject();

                final TextView usedExtraCard = (TextView) findViewById(R.id.usedExtra);

                json.put("nickname", userNickName);
                json.put("gameid", gameId);
                json.put("words", wordsToPlay.trim());
                json.put("round", gameRound);
                json.put("shownCards", shownCards);
                json.put("blindCards", blindCards);
                json.put("usedExtra", usedExtraCard.getText().toString().trim());

                TextView stringOfCardsDealtToPlayer = (TextView) findViewById(R.id.stringOfCardsDealtToPlayer);
                json.put("cardssenttoplayer",
                        stringOfCardsDealtToPlayer.getText());

                HttpParams httpParams = new BasicHttpParams();
                HttpConnectionParams.setConnectionTimeout(httpParams, 6500);
                HttpConnectionParams.setSoTimeout(httpParams, 6500);
                HttpClient client = new DefaultHttpClient(httpParams);

                HttpPost request = new HttpPost(playerAction
                        + "?action=playerPlayWords");
                request.setEntity(new ByteArrayEntity(json.toString().getBytes(
                        "UTF8")));
                request.setHeader("json", json.toString());
                HttpResponse response = client.execute(request);
                HttpEntity entity = response.getEntity();
                // If the response does not enclose an entity, there is no need
                if (entity != null) {

                    InputStream in = response.getEntity().getContent();
                    a = convertStreamToString(in);

                }
            } catch (Throwable e) {
                // // Log.e("log_tag", "Error parsing data " + e.toString());
            }
            return a;

        }

        @Override
        protected void onPostExecute(String a) {

            if (dialog.isShowing()) {
                dialog.dismiss();
            }

            try {

                @SuppressWarnings("unused")
                String userInfo = "";
                JSONObject topobj = new JSONObject(a);

                String i_wordsCorrect = topobj.getString("goodwords");
                String i_userNickName = topobj.getString("nickname");
                String i_error = topobj.getString("error");

                int i_userPoints = topobj.getInt("points");

                if ((i_wordsCorrect != "0") || (i_wordsCorrect != "")) {
                    String[] n_wordsCorrect = i_wordsCorrect.split(",");
                    int num_wordsCorrect = n_wordsCorrect.length;

                    TextView playWordsString = (TextView) findViewById(R.id.playWordsString);

                    for (int u = 0; u < num_wordsCorrect; u++) {
                        if (!n_wordsCorrect[u].contains("http")) {
                            playMp3("http://www.gstatic.com/dictionary/static/sounds/de/0/sorry.mp3",
                                    0, 0);
                            playMp3("http://www.gstatic.com/dictionary/static/sounds/de/0/no.mp3",
                                    0, 0);
                            playMp3("http://www.gstatic.com/dictionary/static/sounds/de/0/definition.mp3",
                                    0, 0);

                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException e) {

                                // Log.e("log_tag", "Error parsing data " +
                                // e.toString());
                            }

                        } else {
                            playMp3(n_wordsCorrect[u], 1, i_userPoints);

                        }
                    }

                    TextView errorString = (TextView) findViewById(R.id.errorString);
                    errorString.setText(i_error);
                    playWordsString.setText("Words Correct: "
                            + num_wordsCorrect + " for " + i_userNickName
                            + " Points: " + i_userPoints);

                    new addPointsAndGotoNextRound(JoinGame.this, i_userPoints)
                            .execute();

                }

            } catch (JSONException e) {

                // // Log.e("log_tag", "Error parsing data " + e.toString());
            }

        }
    }

    class RetrieveCards extends AsyncTask<String, Void, String> {

        private final ProgressDialog dialog;
        private final JoinGame activity;

        public RetrieveCards(JoinGame joinGame) {
            this.activity = joinGame;
            JoinGame context = activity;
            dialog = new ProgressDialog(context);
        }

        @Override
        protected void onPreExecute() {

            this.dialog.setMessage("Dealing Cards...");
            this.dialog.show();
        }

        @Override
        protected String doInBackground(String... urls) {
            String a = "";

            try {

                JSONObject json = new JSONObject();
                json.put("gameid", gameId);
                json.put("roundno", gameRound);
                json.put("nickname", userNickName);

                HttpParams httpParams = new BasicHttpParams();
                HttpConnectionParams.setConnectionTimeout(httpParams, 6500);
                HttpConnectionParams.setSoTimeout(httpParams, 6500);
                HttpClient client = new DefaultHttpClient(httpParams);

                HttpPost request = new HttpPost(playerAction
                        + "?action=getCards");
                request.setEntity(new ByteArrayEntity(json.toString().getBytes(
                        "UTF8")));
                request.setHeader("json", json.toString());
                HttpResponse response = client.execute(request);

                InputStream in = response.getEntity().getContent();
                a = convertStreamToString(in);

            } catch (Throwable e) {
                // // Log.e("log_tag", "Error parsing data " + e.toString());
            }

            return a;
        }

        @Override
        protected void onPostExecute(String a) {

            if (a != "") {

                if (dialog.isShowing()) {
                    dialog.dismiss();
                }

                try {

                    JSONArray jsonArray = null;

                    jsonArray = new JSONArray(a);

                    timer.cancel();

                    int[] imgs = new int[11];
                    imgs[0] = R.id.ImageView1;
                    imgs[1] = R.id.ImageView2;
                    imgs[2] = R.id.ImageView3;
                    imgs[3] = R.id.ImageView4;
                    imgs[4] = R.id.ImageView5;
                    imgs[5] = R.id.ImageView6;
                    imgs[6] = R.id.ImageView7;
                    imgs[7] = R.id.ImageView8;
                    imgs[8] = R.id.ImageView9;
                    imgs[9] = R.id.ImageView10;
                    imgs[10] = R.id.ImageView11;

                    final TextView wordsToPlay = (TextView) findViewById(R.id.wordsToPlay);
                    final TextView wordsLeftToPlay = (TextView) findViewById(R.id.wordsLeftToPlay);
                    final TextView usedExtraCard = (TextView) findViewById(R.id.usedExtra);
                    final TextView usedExtraCardWhat = (TextView) findViewById(R.id.usedExtraWhat);

                    final TextView LettersInSpaces = (TextView) findViewById(R.id.LettersInSpaces);

                    wordsLeftToPlay.setText(String.valueOf(gameRound + 3));

                    final String[] multipleStackShown = jsonArray
                            .getJSONObject(0).getString("stackshown")
                            .toString().split(",");

                    final String[] multipleStackBlind = jsonArray
                            .getJSONObject(0).getString("stackleft").toString()
                            .split(",");

                    shownCards = multipleStackShown[multipleStackShown.length - 1];

                    blindCards = multipleStackBlind[multipleStackBlind.length - 1];

                    final ImageView imgViewItemExtra = (ImageView) findViewById(imgs[(gameRound + 3)]);

                    int CardToGetShown = R.id.CardToGetShown;
                    final ImageView imgViewItemShown = (ImageView) findViewById(CardToGetShown);
                    new DownloadImageFromServer(imgViewItemShown)
                            .execute("http://nicklupien.com/quiddler/images/new/"
                                    + shownCards + ".png");

                    imgViewItemShown.setTag(shownCards.toUpperCase().trim());

                    int CardToGetBlind = R.id.CardToGetBlind;
                    final ImageView imgViewItemBlind = (ImageView) findViewById(CardToGetBlind);
                    new DownloadImageFromServer(imgViewItemBlind)
                            .execute("http://nicklupien.com/quiddler/images/new/hidden.png");

                    imgViewItemBlind.setTag(blindCards.toUpperCase().trim());

                    //

                    imgViewItemShown
                            .setOnClickListener(new ImageView.OnClickListener() {
                                public void onClick(View v) {

                                    if (Integer.parseInt(wordsLeftToPlay
                                            .getText().toString()) > 0) {

                                        // new DownloadImageFromServer(
                                        // imgViewItemShown)
                                        // .execute("http://nicklupien.com/quiddler/images/new/hidden.png");

                                        new DownloadImageFromServer(
                                                imgViewItemExtra)
                                                .execute("http://nicklupien.com/quiddler/images/new/"
                                                        + shownCards + ".png");

                                        imgViewItemExtra.setTag(shownCards
                                                .toUpperCase().trim());

                                        imgViewItemExtra
                                                .setVisibility(View.VISIBLE);

                                        new DownloadImageFromServer(
                                                imgViewItemBlind)
                                                .execute("http://nicklupien.com/quiddler/images/new/hidden.png");
                                        imgViewItemBlind.setClickable(false);

                                        new DownloadImageFromServer(
                                                imgViewItemShown)
                                                .execute("http://nicklupien.com/quiddler/images/new/hidden.png");
                                        imgViewItemShown.setClickable(false);

                                        usedExtraCard.setText(imgViewItemShown
                                                .getTag().toString());
                                        usedExtraCardWhat.setText("shown");

                                        new TakeCardFromExtra(gameId,
                                                gameRound, userNickName,
                                                shownCards, "shown").execute();

                                    }

                                }
                            });

                    imgViewItemBlind
                            .setOnClickListener(new ImageView.OnClickListener() {
                                public void onClick(View v) {

                                    if (Integer.parseInt(wordsLeftToPlay
                                            .getText().toString()) > 0) {

                                        new DownloadImageFromServer(
                                                imgViewItemExtra)
                                                .execute("http://nicklupien.com/quiddler/images/new/"
                                                        + blindCards + ".png");

                                        imgViewItemExtra.setTag(blindCards
                                                .toUpperCase().trim());

                                        imgViewItemExtra
                                                .setVisibility(View.VISIBLE);

                                        new DownloadImageFromServer(
                                                imgViewItemBlind)
                                                .execute("http://nicklupien.com/quiddler/images/new/hidden.png");
                                        imgViewItemBlind.setClickable(false);

                                        new DownloadImageFromServer(
                                                imgViewItemShown)
                                                .execute("http://nicklupien.com/quiddler/images/new/hidden.png");
                                        imgViewItemShown.setClickable(false);

                                        usedExtraCard.setText(imgViewItemBlind
                                                .getTag().toString());
                                        usedExtraCardWhat.setText("blind");

                                        new TakeCardFromExtra(gameId,
                                                gameRound, userNickName,
                                                blindCards, "blind").execute();

                                    }

                                }
                            });

                    String cardsDealtToPlayer = "";
                    String[] theCard = jsonArray.getJSONObject(0)
                            .getString("cards").toString().split(",");

                    int userHasPulledCardPreviously = 0;

                    if (theCard.length > (gameRound + 3)) {
                        new DownloadImageFromServer(imgViewItemBlind)
                                .execute("http://nicklupien.com/quiddler/images/new/hidden.png");
                        imgViewItemBlind.setClickable(false);

                        new DownloadImageFromServer(imgViewItemShown)
                                .execute("http://nicklupien.com/quiddler/images/new/hidden.png");
                        imgViewItemShown.setClickable(false);

                        userHasPulledCardPreviously++;
                    }

                    else {
                        seeIfExtraCardsHaveChanged(gameId, gameRound);
                    }

                    for (int i = 0; i < (gameRound + 3 + userHasPulledCardPreviously); i++) {

                        final ImageView imgViewItem = (ImageView) findViewById(imgs[i]);

                        cardsDealtToPlayer = cardsDealtToPlayer + ","
                                + theCard[i];

                        String thisImageUrl = "http://nicklupien.com/quiddler/images/new/"
                                + theCard[i] + ".png";

                        // buttonLetterViewItem.setVisibility(View.VISIBLE);
                        // buttonLetterViewItem.setText(" "
                        // + theCard[i].toUpperCase().trim() + " ");
                        //
                        // buttonLetterViewItem.setHint(theCard[i].toUpperCase()
                        // .trim());

                        imgViewItem.setTag(theCard[i].toUpperCase().trim());

                        imgViewItem
                                .setOnClickListener(new ImageView.OnClickListener() {

                                    public void onClick(View v) {

                                        if (Integer.parseInt(wordsLeftToPlay
                                                .getText().toString()) > 0) {

                                            wordsToPlay.setText(wordsToPlay
                                                    .getText().toString()
                                                    + imgViewItem.getTag()
                                                            .toString());

                                            wordsLeftToPlay.setText(String.valueOf(Integer
                                                    .parseInt(wordsLeftToPlay
                                                            .getText()
                                                            .toString()) - 1));

                                            LettersInSpaces.setText(LettersInSpaces
                                                    .getText()
                                                    + String.valueOf(imgViewItem
                                                            .getId())
                                                    + ","
                                                    + imgViewItem.getTag()
                                                            .toString()
                                                    + ","
                                                    + wordsLeftToPlay.getText()
                                                            .toString() + "|");

                                            new DownloadImageFromServer(
                                                    imgViewItem)
                                                    .execute("http://nicklupien.com/quiddler/images/new/hidden.png");
                                            imgViewItem.setClickable(false);

                                        }

                                    }
                                });

                        new DownloadImageFromServer(imgViewItem)
                                .execute(thisImageUrl);

                    }

                    imgViewItemExtra
                            .setOnClickListener(new ImageView.OnClickListener() {

                                public void onClick(View v) {

                                    if (Integer.parseInt(wordsLeftToPlay
                                            .getText().toString()) > 0) {

                                        wordsToPlay.setText(wordsToPlay
                                                .getText().toString()
                                                + imgViewItemExtra.getTag()
                                                        .toString());

                                        wordsLeftToPlay.setText(String.valueOf(Integer
                                                .parseInt(wordsLeftToPlay
                                                        .getText().toString()) - 1));

                                        LettersInSpaces.setText(LettersInSpaces
                                                .getText()
                                                + String.valueOf(imgViewItemExtra
                                                        .getId())
                                                + ","
                                                + imgViewItemExtra.getTag()
                                                        .toString()
                                                + ","
                                                + wordsLeftToPlay.getText()
                                                        .toString() + "|");

                                        new DownloadImageFromServer(
                                                imgViewItemExtra)
                                                .execute("http://nicklupien.com/quiddler/images/new/hidden.png");
                                        imgViewItemExtra.setClickable(false);

                                    }

                                }
                            });

                    final Button buttonLetterSpace = (Button) findViewById(R.id.ButtonLetterSpace);
                    buttonLetterSpace.setVisibility(View.VISIBLE);
                    buttonLetterSpace.setText("Space");
                    buttonLetterSpace.setHint(" ");

                    buttonLetterSpace
                            .setOnClickListener(new Button.OnClickListener() {
                                public void onClick(View v) {

                                    wordsToPlay.setText(wordsToPlay.getText()
                                            .toString()
                                            + buttonLetterSpace.getHint()
                                                    .toString());
                                }
                            });

                    final Button ButtonLetterDelete = (Button) findViewById(R.id.ButtonLetterDelete);
                    ButtonLetterDelete.setVisibility(View.VISIBLE);
                    ButtonLetterDelete.setText("Delete");
                    ButtonLetterDelete.setHint(" ");

                    ButtonLetterDelete
                            .setOnClickListener(new Button.OnClickListener() {

                                public void onClick(View v) {

                                    if ((wordsToPlay.getText().toString()
                                            .endsWith(" "))) {

                                        String contents = wordsToPlay.getText()
                                                .toString();
                                        wordsToPlay.setText(contents.substring(
                                                0, contents.length() - 1));

                                    }

                                    else {

                                        if (Integer.parseInt(wordsLeftToPlay
                                                .getText().toString()) <= (gameRound + 3)) {

                                            if (wordsToPlay.getText().length() > 0) {

                                                findViewById(R.id.usedExtraWhat);

                                                String contents = wordsToPlay
                                                        .getText().toString();

                                                String[] theseLettersInSpace = LettersInSpaces
                                                        .getText().toString()
                                                        .split("\\|");

                                                for (int c = 0; c < theseLettersInSpace.length; c++) {

                                                    if (theseLettersInSpace[c]
                                                            .contains(",")) {
                                                        String[] tLINthisSpace = theseLettersInSpace[c]
                                                                .split(",");
                                                        // id,letter,space

                                                        if ((tLINthisSpace[2] != null)
                                                                || (wordsLeftToPlay
                                                                        .getText()
                                                                        .toString() != null)) {

                                                            if (Integer
                                                                    .parseInt(tLINthisSpace[2]) == Integer
                                                                    .parseInt(wordsLeftToPlay
                                                                            .getText()
                                                                            .toString())) {

                                                                int id = getResources()
                                                                        .getIdentifier(
                                                                                tLINthisSpace[0],
                                                                                "id",
                                                                                getPackageName());
                                                                ImageView thisLetter2 = (ImageView) findViewById(id);

                                                                new DownloadImageFromServer(
                                                                        thisLetter2)
                                                                        .execute("http://nicklupien.com/quiddler/images/new/"
                                                                                + tLINthisSpace[1]
                                                                                        .toLowerCase()
                                                                                + ".png");

                                                                thisLetter2
                                                                        .setClickable(true);

                                                                LettersInSpaces
                                                                        .setText(LettersInSpaces
                                                                                .getText()
                                                                                .toString()
                                                                                .replace(
                                                                                        tLINthisSpace[0]
                                                                                                + ","
                                                                                                + tLINthisSpace[1]
                                                                                                + ","
                                                                                                + tLINthisSpace[2]
                                                                                                + "|",
                                                                                        ""));

                                                            }

                                                        }

                                                    }

                                                }

                                                wordsToPlay.setText(contents
                                                        .substring(0, contents
                                                                .length() - 1));

                                                wordsLeftToPlay.setText(String.valueOf(Integer
                                                        .parseInt(wordsLeftToPlay
                                                                .getText()
                                                                .toString()) + 1));

                                            }

                                        }

                                    }

                                }
                            });

                    TextView stringOfCardsDealtToPlayer = (TextView) findViewById(R.id.stringOfCardsDealtToPlayer);
                    stringOfCardsDealtToPlayer.setText(cardsDealtToPlayer);

                    TextView playerScoretxt = (TextView) findViewById(R.id.playerScore);
                    new GetPlayerScore(playerScoretxt, playerAction, gameId,
                            userNickName).execute();

                    TextView gameRoundtxt = (TextView) findViewById(R.id.gameRoundtxt);
                    gameRoundtxt.setText("Round "
                            + Integer.toString(gameRound + 1));

                } catch (JSONException e) {

                    // Log.e("log_tag", "Error parsing data " + e.toString());
                }

            } else {
                // Wait for Cards...
            }

        }

        private String convertStreamToString(InputStream is) {

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();

            String line = null;
            try {
                while ((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }
            } catch (IOException e) {
                // Log.e("log_tag", "Error parsing data " + e.toString());
            } finally {
                try {
                    is.close();
                } catch (IOException e) {
                    // Log.e("log_tag", "Error parsing data " + e.toString());
                }
            }
            return sb.toString();
        }
    }

    class RetrieveNewExtraCards extends AsyncTask<String, Void, JSONObject> {

        private String thisGameID = "";
        private int thisRound = 0;

        public RetrieveNewExtraCards(String gameId2, int round) {

            thisGameID = gameId2;
            thisRound = round;

        }

        @Override
        protected JSONObject doInBackground(String... urls) {

            JSONObject topobj = null;

            try {

                JSONObject json = new JSONObject();

                json.put("gameid", thisGameID);
                json.put("round", thisRound);

                HttpParams httpParams = new BasicHttpParams();
                HttpConnectionParams.setConnectionTimeout(httpParams, 6500);
                HttpConnectionParams.setSoTimeout(httpParams, 6500);
                HttpClient client = new DefaultHttpClient(httpParams);

                HttpPost request = new HttpPost(playerAction
                        + "?action=checkExtraCards");
                request.setEntity(new ByteArrayEntity(json.toString().getBytes(
                        "UTF8")));
                request.setHeader("json", json.toString());
                HttpResponse response = client.execute(request);

                InputStream in = response.getEntity().getContent();
                String a = convertStreamToString(in);
                if ((a != "") || (a != null)) {
                    topobj = new JSONObject(a);
                }

            } catch (Throwable e) {
                // // Log.e("log_tag", "Error parsing data " + e.toString());
            }
            return topobj;

        }

        @Override
        protected void onPostExecute(JSONObject jsonobj) {

            imgs = new int[11];
            imgs[0] = R.id.ImageView1;
            imgs[1] = R.id.ImageView2;
            imgs[2] = R.id.ImageView3;
            imgs[3] = R.id.ImageView4;
            imgs[4] = R.id.ImageView5;
            imgs[5] = R.id.ImageView6;
            imgs[6] = R.id.ImageView7;
            imgs[7] = R.id.ImageView8;
            imgs[8] = R.id.ImageView9;
            imgs[9] = R.id.ImageView10;
            imgs[10] = R.id.ImageView11;

            final ImageView imgViewItemExtra = (ImageView) findViewById(imgs[(gameRound + 3)]);
            final TextView wordsLeftToPlay = (TextView) findViewById(R.id.wordsLeftToPlay);
            final TextView usedExtraCard = (TextView) findViewById(R.id.usedExtra);
            final TextView usedExtraCardWhat = (TextView) findViewById(R.id.usedExtraWhat);

            int CardToGetShown = R.id.CardToGetShown;
            final ImageView imgViewItemShown = (ImageView) findViewById(CardToGetShown);

            int CardToGetBlind = R.id.CardToGetBlind;
            final ImageView imgViewItemBlind = (ImageView) findViewById(CardToGetBlind);

            try {

                newExtraBlind = jsonobj.getString("extrablind");

                new DownloadImageFromServer(imgViewItemBlind)
                        .execute("http://nicklupien.com/quiddler/images/new/hidden.png");
                imgViewItemBlind.setTag(newExtraBlind.toUpperCase().trim());

                imgViewItemBlind
                        .setOnClickListener(new ImageView.OnClickListener() {
                            public void onClick(View v) {

                                if (Integer.parseInt(wordsLeftToPlay.getText()
                                        .toString()) > 0) {

                                    new DownloadImageFromServer(
                                            imgViewItemExtra)
                                            .execute("http://nicklupien.com/quiddler/images/new/"
                                                    + newExtraBlind + ".png");

                                    imgViewItemExtra.setTag(newExtraBlind
                                            .toUpperCase().trim());

                                    imgViewItemExtra
                                            .setVisibility(View.VISIBLE);

                                    new DownloadImageFromServer(
                                            imgViewItemBlind)
                                            .execute("http://nicklupien.com/quiddler/images/new/hidden.png");
                                    imgViewItemBlind.setClickable(false);

                                    new DownloadImageFromServer(
                                            imgViewItemShown)
                                            .execute("http://nicklupien.com/quiddler/images/new/hidden.png");
                                    imgViewItemShown.setClickable(false);

                                    usedExtraCard.setText(imgViewItemBlind
                                            .getTag().toString());
                                    usedExtraCardWhat.setText("blind");

                                    new TakeCardFromExtra(gameId, gameRound,
                                            userNickName, newExtraBlind,
                                            "blind").execute();

                                }

                            }
                        });

                newExtraShown = jsonobj.getString("extrashown");

                new DownloadImageFromServer(imgViewItemShown)
                        .execute("http://nicklupien.com/quiddler/images/new/"
                                + newExtraShown + ".png");
                imgViewItemShown.setTag(newExtraShown.toUpperCase().trim());

                imgViewItemShown
                        .setOnClickListener(new ImageView.OnClickListener() {
                            public void onClick(View v) {

                                if (Integer.parseInt(wordsLeftToPlay.getText()
                                        .toString()) > 0) {

                                    // new DownloadImageFromServer(
                                    // imgViewItemShown)
                                    // .execute("http://nicklupien.com/quiddler/images/new/hidden.png");

                                    new DownloadImageFromServer(
                                            imgViewItemExtra)
                                            .execute("http://nicklupien.com/quiddler/images/new/"
                                                    + newExtraShown + ".png");

                                    imgViewItemExtra.setTag(newExtraShown
                                            .toUpperCase().trim());

                                    imgViewItemExtra
                                            .setVisibility(View.VISIBLE);

                                    new DownloadImageFromServer(
                                            imgViewItemBlind)
                                            .execute("http://nicklupien.com/quiddler/images/new/hidden.png");
                                    imgViewItemBlind.setClickable(false);

                                    new DownloadImageFromServer(
                                            imgViewItemShown)
                                            .execute("http://nicklupien.com/quiddler/images/new/hidden.png");
                                    imgViewItemShown.setClickable(false);

                                    usedExtraCard.setText(imgViewItemShown
                                            .getTag().toString());
                                    usedExtraCardWhat.setText("shown");

                                    new TakeCardFromExtra(gameId, gameRound,
                                            userNickName, newExtraShown,
                                            "shown").execute();

                                }

                            }
                        });

            } catch (JSONException e) {
                // Log.e("log_tag", "Error parsing data " + e.toString());
                // new RetreiveGames().execute();

            }

        }

    }

    class TakeCardFromExtra extends AsyncTask<String, Void, JSONObject> {

        private String thisGameID = "";
        private int thisRound = 0;
        private String thisUserNickName = "";

        private String cardUsed = "";
        private String stack = "";

        public TakeCardFromExtra(String gameId2, int round,
                String userNickName2, String card, String pile) {

            thisGameID = gameId2;
            thisRound = round;
            thisUserNickName = userNickName2;
            cardUsed = card;
            stack = pile;

        }

        @Override
        protected JSONObject doInBackground(String... urls) {

            try {

                JSONObject json = new JSONObject();

                json.put("gameid", thisGameID);
                json.put("round", thisRound);
                json.put("nickname", thisUserNickName);
                json.put("cardpulled", cardUsed);
                json.put("stack", stack);

                HttpParams httpParams = new BasicHttpParams();
                HttpConnectionParams.setConnectionTimeout(httpParams, 6500);
                HttpConnectionParams.setSoTimeout(httpParams, 6500);
                HttpClient client = new DefaultHttpClient(httpParams);

                HttpPost request = new HttpPost(playerAction
                        + "?action=pullExtraCard");
                request.setEntity(new ByteArrayEntity(json.toString().getBytes(
                        "UTF8")));
                request.setHeader("json", json.toString());
                HttpResponse response = client.execute(request);

                InputStream in = response.getEntity().getContent();
                convertStreamToString(in);

            } catch (Throwable e) {
                // // Log.e("log_tag", "Error parsing data " + e.toString());
            }

            extraCardTimer.cancel();

            return null;

        }

    }

    class addPointsAndGotoNextRound extends AsyncTask<String, Void, JSONObject> {

        private int thisUserPoints = 0;

        public addPointsAndGotoNextRound(JoinGame joinGame, int i_userPoints) {

            thisUserPoints = i_userPoints;

        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected JSONObject doInBackground(String... urls) {

            JSONObject topobj = null;

            try {

                JSONObject json = new JSONObject();

                json.put("round", gameRound + 1);
                json.put("gameid", gameId);
                json.put("nickname", userNickName);
                json.put("points", thisUserPoints);

                HttpParams httpParams = new BasicHttpParams();
                HttpConnectionParams.setConnectionTimeout(httpParams, 6500);
                HttpConnectionParams.setSoTimeout(httpParams, 6500);
                HttpClient client = new DefaultHttpClient(httpParams);

                HttpPost request = new HttpPost(playerAction
                        + "?action=playNextRound");
                request.setEntity(new ByteArrayEntity(json.toString().getBytes(
                        "UTF8")));
                request.setHeader("json", json.toString());
                HttpResponse response = client.execute(request);
                HttpEntity entity = response.getEntity();
                // If the response does not enclose an entity, there is no need
                if (entity != null) {

                    InputStream in = response.getEntity().getContent();
                    String a = convertStreamToString(in);

                    topobj = new JSONObject(a);

                }
            } catch (Throwable e) {
                // // Log.e("log_tag", "Error parsing data " + e.toString());
            }
            return topobj;

        }

        @Override
        protected void onPostExecute(JSONObject jsonobj) {

            Intent joinGametoPlay = new Intent(getApplicationContext(),
                    JoinGame.class);

            joinGametoPlay.putExtra("gameId", gameId);
            joinGametoPlay.putExtra("gameName", "");
            joinGametoPlay.putExtra("gameGame", "");
            joinGametoPlay.putExtra("gameRound", gameRound + 1);
            joinGametoPlay.putExtra("userNickName", userNickName);
            joinGametoPlay.putExtra("userID", userID);
            joinGametoPlay.putExtra("skipToPlay", 1);

            JoinGame.this.finish();

            startActivity(joinGametoPlay);

        }

    }

    public void playMp3(String mp3, int toast, int points) {

        if (toast == 1) {
            String[] thisWord = mp3.split("0/");
            thisWord = thisWord[1].split(".mp3");

            String thisWordToast = thisWord[0].toUpperCase()
                    .replace("%401", "").replace("%403", "");

            Toast.makeText(getApplicationContext(),
                    "" + thisWordToast + " is a word! \n\nScore +" + points,
                    Toast.LENGTH_LONG).show();
        }

        Object mediaPlayer = MediaPlayer.create(this, Uri.parse(mp3));
        ((MediaPlayer) mediaPlayer).start();

        try {
            Thread.sleep(250);
        } catch (InterruptedException e) {

            // Log.e("log_tag", "Error parsing data " + e.toString());

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
            // Log.e("log_tag", "Error parsing data " + e.toString());
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                // Log.e("log_tag", "Error parsing data " + e.toString());
            }
        }
        return sb.toString();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    public void onPause(View v) {
        timer.cancel();
        extraCardTimer.cancel();
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
