package com.ux;

import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import okhttp3.*;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

public class MainPage extends Application {
    public static final OkHttpClient client = new OkHttpClient();
    public static final String BaseUrl = "http://127.0.0.1:8080";  // localhost
    public static final int refreshTime = 5000;
    public static String apiKey;

    @Override
    public void start(Stage primaryStage) throws IOException {
        primaryStage.getIcons().add(new Image("com/ux/css/icon.jpg"));
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(500);

        LoginPage l = new LoginPage(primaryStage);
        l.startLogin();
    }

    public static String post(String url, Map<String, String> map) {
        FormBody.Builder b = new FormBody.Builder();
        for(String key : map.keySet())
            b.add(key, map.get(key));
        return post(url, b.build());
    }

    public static String post(String url, String[] columns, String[] answers) {
        FormBody.Builder b = new FormBody.Builder();
        for(int i = 0; i < columns.length; i++)
            b.add(columns[i], answers[i]);
        return post(url, b.build());
    }

    public static String post(String url, RequestBody formBody) {
        Request request = new Request.Builder()
                .url(BaseUrl + url)
                .post(formBody)
                .build();
        try (Response response = client.newCall(request).execute()) {
            return Objects.requireNonNull(response.body()).string();
        } catch (IOException ignore) { }
        return null;
    }

    public static void logout(String username) {
        String[] a = {"username"};
        String[] b = {username};
        post("/logout", a, b);
    }

    public static void main(String[] args) {
        launch(args);
    }
}

/*
* setInterval ->
* new Timer().scheduleAtFixedRate(new TimerTask(){
    @Override
    public void run(){
       Log.i("tag", "A Kiss every 5 seconds");
    }
},0,5000);
*
* setTimeout ->
* new android.os.Handler().postDelayed(
    new Runnable() {
        public void run() {
            Log.i("tag","A Kiss after 5 seconds");
        }
}, 5000);
* */
