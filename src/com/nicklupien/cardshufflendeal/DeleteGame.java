package com.nicklupien.cardshufflendeal;

import java.io.InputStream;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONObject;

import android.os.AsyncTask;

public class DeleteGame extends AsyncTask<String, Void, String> {

    String gameId = "";
    String playerAction = "http://nicklupien.com/quiddler/playerfunctions.php";

    public DeleteGame(String gId) {
        gameId = gId;

    }

    @Override
    protected String doInBackground(String... urls) {
        return deleteThisGame(playerAction + "?action=deleteGame");
    }

    @Override
    protected void onPostExecute(String finalPlayerScore) {

    }

    private String deleteThisGame(String url) {
        String finalPlayerScore = null;
        String a = null;
        try {

            JSONObject json = new JSONObject();

            json.put("gameid", Integer.parseInt(gameId));

            HttpParams httpParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParams, 6500);
            HttpConnectionParams.setSoTimeout(httpParams, 6500);
            HttpClient client = new DefaultHttpClient(httpParams);

            HttpPost request = new HttpPost(url);
            request.setEntity(new ByteArrayEntity(json.toString().getBytes(
                    "UTF8")));
            request.setHeader("json", json.toString());
            HttpResponse response = client.execute(request);

            InputStream in = response.getEntity().getContent();

            finalPlayerScore = "1";

        } catch (Throwable e) {
            // e.printStackTrace();
        }

        return finalPlayerScore;

    }

}
