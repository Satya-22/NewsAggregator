package com.satyayoganand.newsaggregator;

import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.widget.ImageView;

import com.android.volley.Response;
import com.android.volley.toolbox.ImageRequest;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;

import javax.net.ssl.HttpsURLConnection;

public class NewsVolleyRunnable implements Runnable{
        private static final String TAG = "NewsLoaderRunnable";
        private String news_ID;
        private static MainActivity mainActivity;
        private static final String DATA_URL = "https://newsapi.org/v2/top-headlines";
        private static final String api ="0379c93975d84db6bed77c96a7241b31";
        public static ArrayList<String> categoryList = new ArrayList<>();
        public static String[] drawerData = new String[200];
        public static String iconUrl;
        public static News news;

        NewsVolleyRunnable(MainActivity mainActivity,String id) {
            this.mainActivity = mainActivity;
            news_ID = id;
        }

        @Override
        public void run() {
            Uri.Builder buildURL = Uri.parse(DATA_URL).buildUpon();
            buildURL.appendQueryParameter("sources",news_ID);
            buildURL.appendQueryParameter("apiKey", api);
            String urlToUse = buildURL.build().toString();
            Log.d(TAG, "run: " + urlToUse);

            StringBuilder sb = new StringBuilder();
            try {
                URL url = new URL(urlToUse);

                HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
                conn.setRequestProperty("User-Agent", "News-App");
                conn.setRequestProperty("X-Api-Key", api);
                Log.d(TAG, "run: " +conn);
                conn.setRequestMethod("GET");
                conn.connect();

                if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    Log.d(TAG, "run: HTTP ResponseCode NOT OK: " + conn.getResponseCode());
                    // handleResults(null);
                    return;
                }
                Log.d(TAG, "run: Response Code "+ conn.getResponseCode());
                InputStream is = conn.getInputStream();
                String inpst = is.toString();
                BufferedReader bR = new BufferedReader(  new InputStreamReader(is));
                String line = "";

                StringBuilder responseStrBuilder = new StringBuilder();
                while((line =  bR.readLine()) != null){

                    responseStrBuilder.append(line);
                }
                is.close();

                System.out.println("Response data : "+ responseStrBuilder);

                JSONObject result= new JSONObject(responseStrBuilder.toString());
                System.out.println("satya : "+ is);
                JSONTokener newsTokener = new JSONTokener(responseStrBuilder.toString());
                System.out.println("satya 1: "+ newsTokener);

                JSONObject newsObject = new JSONObject(newsTokener);
                parseJSON(newsObject);

            } catch (Exception e) {
                Log.e(TAG, "run: ", e);
                //  handleResults(null);
                return;
            }

            // handleResults(sb.toString());

        }
        //    private void handleResults(String s) {
//
//        if (s == null) {
//            Log.d(TAG, "handleResults: Failure in data download");
//           // mainActivity.runOnUiThread(mainActivity::downloadFailed);
//            return;
//        }
//
//        final ArrayList<Country> countryList = parseJSON(s);
//        if (countryList == null) {
//            mainActivity.runOnUiThread(mainActivity::downloadFailed);
//            return;
//        }
//
//        mainActivity.runOnUiThread(() -> mainActivity.updateData(countryList));
//    }
        private void parseJSON(JSONObject jsonObject)  {
            ArrayList<News> newsList = new ArrayList<>();
            Log.d(TAG, "parseJSON: Print js " + jsonObject);
            try {
                JSONArray js = jsonObject.getJSONArray("articles");
                if(js.equals(null)){
                    return;
                }
                for (int i = 0; i < js.length(); i++) {
                    JSONObject jNews = (JSONObject) js.get(i);
                    String author = jNews.getString("author");
                    String title = jNews.getString("title");
                    String description = jNews.getString("description");
                    iconUrl = jNews.getString("urlToImage");
                    String publishedAt = jNews.getString("publishedAt");
                    String webUrl = jNews.getString("url");

                    news = new News(author, title, description, iconUrl, publishedAt,webUrl);
                    newsList.add(news);
                }
                    mainActivity.runOnUiThread(() ->
                            mainActivity.acceptResults(newsList));

            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
