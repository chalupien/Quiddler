package com.nicklupien.cardshufflendeal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

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

import android.os.AsyncTask;
import android.widget.TextView;

public class GetPlayerScore extends AsyncTask<String, Void, String> {

    String playerAction = "";
    String gameId = "";
    String userNickName = "";
    TextView place = null;

    public GetPlayerScore(TextView text, String pA, String gId, String uNN) {
        place = text;
        playerAction = pA;
        gameId = gId;
        userNickName = uNN;

    }

    @Override
    protected String doInBackground(String... urls) {
        return getPlayerScore(playerAction + "?action=getFinalScore");
    }

    @Override
    protected void onPostExecute(String finalPlayerScore) {
        place.setText("Score: " + finalPlayerScore);

    }

    private String getPlayerScore(String url) {
        String finalPlayerScore = null;
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

            if (a != "") {

                try {

                    JSONArray jsonArray = null;

                    jsonArray = new JSONArray(a);

                    finalPlayerScore = jsonArray.getJSONObject(0)
                            .getString(userNickName).toString();

                    if (finalPlayerScore == "null") {
                        finalPlayerScore = "";
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

        } catch (Throwable e) {
            // e.printStackTrace();
            finalPlayerScore = "";
        }

        return finalPlayerScore;

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

}
