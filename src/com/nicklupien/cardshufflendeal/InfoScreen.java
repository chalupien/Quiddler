package com.nicklupien.cardshufflendeal;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;
import android.widget.ImageView;

public class InfoScreen extends Activity {

    String imageUrl = "http://nicklupien.com/quiddler/images/";
    String playerAction = "http://nicklupien.com/quiddler/playerfunctions.php";
    String userNickName = "";
    String gameId = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instructions_game);

        ImageView mChart = (ImageView) findViewById(R.id.imview);
        new DownloadImageFromServer(mChart).execute(imageUrl + "png1.png");

        WebView webview = (WebView) findViewById(R.id.InfoWebview);

        String info = getResources().getString(R.string.game_instructions);

        webview.loadData(info, "text/html", "utf-8");

    }

}
