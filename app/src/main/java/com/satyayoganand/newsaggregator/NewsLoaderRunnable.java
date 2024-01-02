package com.satyayoganand.newsaggregator;

import android.graphics.Color;
import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import javax.net.ssl.HttpsURLConnection;

public class NewsLoaderRunnable implements Runnable{
    private static final String TAG = "NewsLoaderRunnable";
    private final MainActivity mainActivity;
    private static final String DATA_URL = "https://newsapi.org/v2/sources";
    private static final String api ="0379c93975d84db6bed77c96a7241b31";
    public static final HashMap<String,ArrayList<String>> topicsHashMap = new HashMap<>();
    public static final HashMap<String,ArrayList<String>> countriesHashMap = new HashMap<>();
    public static final HashMap<String,ArrayList<String>> languagesHashMap = new HashMap<>();
    public static final HashMap<String,String> newsIDHashMap = new HashMap<>();
    public static ArrayList<String> categoryList = new ArrayList<>();
    public static String[] drawerData = new String[200];
    public static ArrayList<HashMap<String, Integer>> drawerDetails = new ArrayList<>();

    NewsLoaderRunnable(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Override
    public void run() {
        Uri.Builder buildURL = Uri.parse(DATA_URL).buildUpon();
        buildURL.appendQueryParameter("q","keyword");
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
            return;
        }



    }
    public static ArrayList<HashMap<String,Integer>> getDrawerDetails(){
        return drawerDetails;
    }

    public static String[] getDrawerData(){
        return drawerData;
    }
    public static HashMap<String,ArrayList<String>> getTopicsHashMap(){
        return topicsHashMap;
    }


    public static HashMap<String,ArrayList<String>> getCountriesHashMap(){
        return countriesHashMap;
    }

    public static HashMap<String,ArrayList<String>> getLanguagesHashMap(){
        return languagesHashMap;
    }

    public static HashMap<String,String> getNewsIDHashMap(){
        return newsIDHashMap;
    }

    public static Integer getColor(String category){
        if(category.trim().equals("business")){
            return Color.MAGENTA;
        }
        else if(category.trim().equals("entertainment")){
            return Color.RED;
        }
        else if(category.trim().equals("general")){
            return Color.YELLOW;
        }
        else if(category.equals("health")){
            return Color.GREEN;
        }
        else if(category.trim().equals("science")){
            return Color.GRAY;
        }
        else if(category.trim().equals("sports")){
            return Color.LTGRAY;
        }
        else if(category.trim().equals("technology")){
            return Color.CYAN;
        }
        return Color.WHITE;
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
    private static void parseJSON(JSONObject jsonObject)  {
        topicsHashMap.clear();
        countriesHashMap.clear();
        languagesHashMap.clear();

        Log.d(TAG, "parseJSON: Print js " + jsonObject);
        try {
            JSONArray js = jsonObject.getJSONArray("sources");
            if(js.equals(null)){
                return;
            }
            for (int i = 0; i < js.length(); i++) {
                JSONObject jNews = (JSONObject) js.get(i);
                HashMap<String,Integer> temp_items = new HashMap<>();
                String newsID = jNews.getString("id");
                String newsName = jNews.getString("name");
                String category = jNews.getString("category");
                String language = jNews.getString("language");
                String country = jNews.getString("country");
                Integer Color = getColor(category);
                if(!newsIDHashMap.containsKey(newsID)){
                    newsIDHashMap.put(newsID,newsName);
                }
                if(!topicsHashMap.containsKey(category)){
                    ArrayList<String> n = new ArrayList<>();
                    n.add(newsName);
                    categoryList.add(newsName);
                    topicsHashMap.put(category,n);
                } else{
                    ArrayList<String> tempList = topicsHashMap.get(category);
                    tempList.add(newsName);
                }
                if(!languagesHashMap.containsKey(language)){
                    ArrayList<String> n = new ArrayList<>();
                    n.add(newsName);
                    languagesHashMap.put(language,n);
                } else{
                    ArrayList<String> tempList1 = languagesHashMap.get(language);
                    tempList1.add(newsName);

                }
                if(!countriesHashMap.containsKey(country)){
                    ArrayList<String> n = new ArrayList<>();
                    n.add(newsName);
                    countriesHashMap.put(country,n);
                } else{
                    ArrayList<String> tempList2 = countriesHashMap.get(country);
                    tempList2.add(newsName);

                }
                drawerData[i] = newsName.trim();

                temp_items.put(newsName.trim(),Color);
                drawerDetails.add(temp_items);
            }
            System.out.println("Topics News: "+topicsHashMap.keySet());
            System.out.println("Countries News: "+countriesHashMap.keySet());
            System.out.println("Languages News: "+languagesHashMap.keySet());

        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
