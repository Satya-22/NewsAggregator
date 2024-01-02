package com.satyayoganand.newsaggregator;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager2.widget.ViewPager2;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final String newsURl = "https://newsapi.org/v2/sources";
    private static final String yourAPIKey = "0379c93975d84db6bed77c96a7241b31";
    private ViewPager2 viewPager;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private static String[] drawerItems;
    private Menu menu;
    private final ArrayList<String> country_names = new ArrayList<>();
    private final ArrayList<String> language_names = new ArrayList<>();
    public static  ArrayList<String> topics = new ArrayList<>();
    public static HashMap<String,ArrayList<String>> categoryHash = new HashMap<String,ArrayList<String>>();
    public static HashMap<String,ArrayList<String>> countryHashMap = new HashMap<>();
    public static HashMap<String,ArrayList<String>> languagesHashMap = new HashMap<>();
    public static HashMap<String,String> newsIDHashMap = new HashMap<>();
    private final HashMap<String, String> countryCodes_hash_map = new HashMap<String, String>();
    private final HashMap<String, String> languageCodes_hash_map = new HashMap<String, String>();
    private static ArrayList<HashMap<String,Integer>> drawerDetails = new ArrayList<>();

    private static List<String> list = new ArrayList<String>();
    private static List<String> tempList = new ArrayList<String>();
    public static ArrayList<String> filters = new ArrayList<>();
    public String parentMenu;
    private NewsAdapter newsAdapter;
    private final ArrayList<News>  newsList = new ArrayList<>();

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        viewPager = findViewById(R.id.view_pager);

        try {
            list.clear();
            loadDataFromFile();

            NewsLoaderRunnable nlr = new NewsLoaderRunnable(this);
            Thread t = new Thread(nlr);
            t.start();
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            drawerItems = NewsLoaderRunnable.getDrawerData();
            categoryHash = NewsLoaderRunnable.getTopicsHashMap();
            countryHashMap = NewsLoaderRunnable.getCountriesHashMap();
            languagesHashMap = NewsLoaderRunnable.getLanguagesHashMap();
            newsIDHashMap = NewsLoaderRunnable.getNewsIDHashMap();
            drawerDetails = NewsLoaderRunnable.getDrawerDetails();



            for (Map.Entry<String, ArrayList<String>> set :
                    countryHashMap.entrySet()) {
                for (Map.Entry<String, String> set1 :
                        countryCodes_hash_map.entrySet()) {
                    if(set.getKey().toUpperCase(Locale.ROOT).equals(set1.getKey().toUpperCase(Locale.ROOT))){
                        country_names.add(set1.getValue());
                    }
                }
            }
            for (Map.Entry<String, ArrayList<String>> set :
                    languagesHashMap.entrySet()) {
                for (Map.Entry<String, String> set1 :
                        languageCodes_hash_map.entrySet()) {
                    if(set.getKey().toUpperCase(Locale.ROOT).equals(set1.getKey().toUpperCase(Locale.ROOT))){
                        language_names.add(set1.getValue());
                    }
                }
            }
            System.out.println("Print Updated HashMap : "+ country_names);

            Collection<String> keys = categoryHash.keySet();
            topics = new ArrayList<String>(keys);


            System.out.println("Topics ############ "+ drawerItems);

            for(String s : drawerItems) {
                if(s != null && s.length() > 0) {
                    list.add(s);
                }
            }
            drawerItems = list.toArray(new String[list.size()]);

        } catch (JSONException e) {
            e.printStackTrace();
        }

            mDrawerLayout = findViewById(R.id.drawer_layout);
            ListView mDrawerList = findViewById(R.id.left_drawer);


        ArrayAdapter<String> adapter=new ArrayAdapter<String>(this, R.layout.drawer_list_item, drawerItems){

            @SuppressLint("ResourceAsColor")
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view =super.getView(position, convertView, parent);

                TextView textView=(TextView) view.findViewById(R.id.drawer_item_layout);
                textView.setTextColor(getColorDetails(textView.getText().toString()));

                return view;
            }
        };
        mDrawerList.setAdapter(adapter);

            mDrawerList.setOnItemClickListener(
                    (parent, view, position, id) -> selectItem(position));

            mDrawerToggle = new ActionBarDrawerToggle(
                    this,                  /* host Activity */
                    mDrawerLayout,         /* DrawerLayout object */
                    R.string.drawer_open,  /* "open drawer" description for accessibility */
                    R.string.drawer_close  /* "close drawer" description for accessibility */
            );

            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setHomeButtonEnabled(true);
            }

        setTitle("NewsAggregator ("+drawerItems.length+")");
    }

    public void acceptResults(ArrayList<News> newsList) {
        if (newsList == null) {
            Toast.makeText(this, "Data loader failed", Toast.LENGTH_LONG).show();
        } else {

            this.newsList.clear();
            this.newsList.addAll(newsList);
            newsAdapter.notifyItemRangeChanged(0, newsList.size());
            Log.d(TAG, "acceptResults: "+ newsList);
        }
    }
    private void loadDataFromFile() throws JSONException {
        InputStream countryCodesInputStream;
        InputStream languageCodesInputStream;
        try {
            languageCodesInputStream = getResources().openRawResource(getResources().getIdentifier("language_codes", "raw", getPackageName()));
            countryCodesInputStream = getResources().openRawResource(getResources().getIdentifier("country_codes", "raw", getPackageName()));

            String cis = readInputStream(countryCodesInputStream);
            String lis = readInputStream(languageCodesInputStream);

            JSONTokener countryTokenizer = new JSONTokener(cis);
            JSONTokener languageTokenizer = new JSONTokener(lis);

            JSONObject countryObject = new JSONObject(countryTokenizer);
            JSONObject languageObject = new JSONObject(languageTokenizer);

            JSONArray countriesJsonArray = countryObject.getJSONArray("countries");
            JSONArray languagesJsonArray = languageObject.getJSONArray("languages");

            for(int i = 0 ; i < countriesJsonArray.length() ; i++){
                countryCodes_hash_map.put(countriesJsonArray.getJSONObject(i).getString("code"),countriesJsonArray.getJSONObject(i).getString("name"));
            }
            for(int j = 0 ; j < languagesJsonArray.length() ; j++){
                languageCodes_hash_map.put(languagesJsonArray.getJSONObject(j).getString("code"),languagesJsonArray.getJSONObject(j).getString("name"));
            }
            Log.d(TAG, "loadDataFromFile: Countries Hashmap " + countryCodes_hash_map);
            Log.d(TAG, "loadDataFromFile: Languages Hashmap " + languageCodes_hash_map);
        } catch (Exception e) {
            Log.d(TAG, "loadDataFromFile: " + e.getMessage());
            e.printStackTrace();
            return;
        }
    }

    public String readInputStream(InputStream inputStream) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        byte buf[] = new byte[1024];
        int len;
        try {
            while ((len = inputStream.read(buf)) != -1) {
                outputStream.write(buf, 0, len);
            }
            outputStream.close();
            inputStream.close();
        } catch (IOException e) {

        }
        return outputStream.toString();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @SuppressLint("ResourceType")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        menu.clear();
        SubMenu subMenu0 = menu.addSubMenu("Topics");
        SubMenu subMenu1 = menu.addSubMenu("Countries");
        SubMenu subMenu2 = menu.addSubMenu("languages");
        menu.add("Clear All");


        Collections.sort(country_names);
        country_names.add(0,"All");
        Collections.sort(language_names);
        language_names.add(0,"All");
        Collections.sort(topics);
        topics.add(0,"All");


        for(int j = 0;j<topics.size();j++){
            subMenu0.add(0,j,j,topics.get(j));
            SpannableString s = new SpannableString(topics.get(j));
            if(topics.get(j).equals("general")){
                s.setSpan(new ForegroundColorSpan(Color.YELLOW), 0, s.length(), 0);
            }
            if(topics.get(j).equals("business")){
                s.setSpan(new ForegroundColorSpan(Color.MAGENTA), 0, s.length(), 0);
            }
            if(topics.get(j).equals("sports")){
                s.setSpan(new ForegroundColorSpan(Color.LTGRAY), 0, s.length(), 0);
            }
            if(topics.get(j).equals("health")){
                s.setSpan(new ForegroundColorSpan(Color.GREEN), 0, s.length(), 0);
            }
            if(topics.get(j).equals("science")){
                s.setSpan(new ForegroundColorSpan(Color.GRAY), 0, s.length(), 0);
            }
            if(topics.get(j).equals("technology")){
                s.setSpan(new ForegroundColorSpan(Color.CYAN), 0, s.length(), 0);
            }
            if(topics.get(j).equals("entertainment")){
                s.setSpan(new ForegroundColorSpan(Color.RED), 0, s.length(), 0);
            }
            subMenu0.getItem(j).setTitle(s);

        }
        for (int j = 0; j < country_names.size(); j++) {
            subMenu1.add(1, j, j,country_names.get(j));
        }
        for (int j = 0; j < language_names.size(); j++) {
            subMenu2.add(2, j, j,language_names.get(j));
        }
        return true;
    }

    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        boolean sizeCheck = filters.size() >1;
        String filter1;
        String filter2;
        if(sizeCheck){
            filter1 = filters.get(0);
            filter2 = filters.get(1);
            System.out.println("Filter 1 : "+ filter1 +" "+"Filter 2 :"+filter2);
        }
        if (item.hasSubMenu()) {
            filters.add(item.toString());
            parentMenu = item.toString();
            return true;
        }

        if (mDrawerToggle.onOptionsItemSelected(item)) {
            Log.d(TAG, "onOptionsItemSelected: mDrawerToggle " + mDrawerToggle.onOptionsItemSelected(item));
            return true;
        }
        //drawerItems = new String[200];
        //String[] temp = new String[200];
        boolean allItems = item.toString().trim().equals("All");
        if(parentMenu.toUpperCase(Locale.ROOT).equals("TOPICS")) {
            for (Map.Entry<String, ArrayList<String>> set :
                    categoryHash.entrySet()) {
                if(allItems) {
                    //temp = drawerItems;
                    drawerItems = NewsLoaderRunnable.getDrawerData();
                    break;
                }
                else if (item.toString().trim().equals(set.getKey().trim())) {
//                    temp = set.getValue().toArray(new String[0]);
                    drawerItems = set.getValue().toArray(new String[0]);
                    break;
                }
            }
        }
        if(parentMenu.toUpperCase(Locale.ROOT).equals("COUNTRIES")) {
            for (Map.Entry<String, ArrayList<String>> countrySet :
                    countryHashMap.entrySet()) {
                for (Map.Entry<String, String> set3 :
                        countryCodes_hash_map.entrySet()) {
                    if(allItems) {
                     //   temp = drawerItems;
                        drawerItems = NewsLoaderRunnable.getDrawerData();
                        break;
                    }
                    else if (item.toString().trim().equals(set3.getValue().trim())) {
                        if (set3.getKey().toUpperCase(Locale.ROOT).trim().equals(countrySet.getKey().toUpperCase(Locale.ROOT).trim())) {
//                            temp = countrySet.getValue().toArray(new String[0]);
                            drawerItems = countrySet.getValue().toArray(new String[0]);
                            break;
                        }
                    }
                }
            }
        }
        if(parentMenu.toUpperCase(Locale.ROOT).equals("LANGUAGES")) {
            for (Map.Entry<String, ArrayList<String>> languageSet :
                    languagesHashMap.entrySet()) {
                for (Map.Entry<String, String> set3 :
                        languageCodes_hash_map.entrySet()) {
                    if(allItems) {
                       // temp = drawerItems;
                        drawerItems = NewsLoaderRunnable.getDrawerData();
                        break;
                    }
                    else if (item.toString().trim().equals(set3.getValue().trim())) {
                           if (set3.getKey().toUpperCase(Locale.ROOT).trim().equals(languageSet.getKey().toUpperCase(Locale.ROOT).trim())) {
//                            temp = languageSet.getValue().toArray(new String[0]);
                               drawerItems = languageSet.getValue().toArray(new String[0]);
                            break;
                        }
                    }
                }
            }
        }
//        System.out.println("When Clicked Clear all : "+parentMenu.toUpperCase(Locale.ROOT));
        if(item.toString().toUpperCase(Locale.ROOT).equals("CLEAR ALL")) {
           // temp = drawerItems;
            drawerItems = NewsLoaderRunnable.getDrawerData();
            filters.clear();
        }

//        if(sizeCheck){
//            System.out.println("Before : "+drawerItems.length);
//            for(int j = 0;j<drawerItems.length;j++){
//                System.out.println(drawerItems[j]);
//            }
//            ArrayList<String> temp1 = new ArrayList(drawerItems.length);
//
//            if(filters.get(0).toUpperCase(Locale.ROOT).equals("TOPICS") && filters.get(1).toUpperCase(Locale.ROOT).equals("COUNTRIES")) {
//                System.out.println("Passed ....");
//                for (int i = 0; i < drawerItems.length; i++) {
//                    System.out.println(categoryHash);
//                    if(categoryHash.containsValue(drawerItems[i]) && countryHashMap.containsValue(drawerItems[i])){
//                        System.out.println("Passed ....");
//                        temp1.add(drawerItems[i]);
//                        System.out.println("drawerItems[i] : "+ drawerItems[i]);
//                    }
//
//                    for (Map.Entry<String, ArrayList<String>> categorySet :
//                            categoryHash.entrySet()) {
//                        for (Map.Entry<String, ArrayList<String>> countrySet :
//                                countryHashMap.entrySet()) {
//                         if((drawerItems[i].toUpperCase(Locale.ROOT).equals(categorySet.getValue().toString().toUpperCase(Locale.ROOT))) && (drawerItems[0].toUpperCase(Locale.ROOT).equals(countrySet.getValue().toString().toUpperCase(Locale.ROOT)))){
//                             System.out.println("Passed ....");
//                               temp1.add(drawerItems[i]);
//                             System.out.println("drawerItems[i] : "+ drawerItems[i]);
//                         }
//                        }
//                    }
//                }
//                drawerItems = new String[temp1.size()];
//                drawerItems = temp1.toArray(new String[tempList.size()]);
//            }
//            else if(filters.get(0).toUpperCase(Locale.ROOT).equals("TOPICS") && filters.get(1).toUpperCase(Locale.ROOT).equals("LANGUAGES")){
//                for (Map.Entry<String, ArrayList<String>> countrySet :
//                        countryHashMap.entrySet()) {
//
//                }
//            }
//            else if(filters.get(0).toUpperCase(Locale.ROOT).equals("LANGUAGES") && filters.get(1).toUpperCase(Locale.ROOT).equals("COUNTRIES")){
//                for (Map.Entry<String, ArrayList<String>> languageSet :
//                        languagesHashMap.entrySet()) {
//
//                }
//            }
//        }

//        for(String s : temp) {
//            if(s != null && s.length() > 0) {
//                tempList.add(s);
//            }
//        }
        for(String s : drawerItems) {
            if(s != null && s.length() > 0) {
                tempList.add(s);
            }
        }
//        temp = tempList.toArray(new String[tempList.size()]);
        drawerItems = tempList.toArray(new String[tempList.size()]);

        ListView mDrawerList = findViewById(R.id.left_drawer);

//        mDrawerList.setAdapter(new ArrayAdapter<>(this,
//                R.layout.drawer_list_item, temp));

        ArrayAdapter<String> adapter=new ArrayAdapter<String>(this, R.layout.drawer_list_item, drawerItems){

            @SuppressLint("ResourceAsColor")
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view =super.getView(position, convertView, parent);



                TextView textView=(TextView) view.findViewById(R.id.drawer_item_layout);

                textView.setTextColor(getColorDetails(textView.getText().toString()));

                return view;
            }
        };
        mDrawerList.setAdapter(adapter);
        mDrawerList.setOnItemClickListener(
                (parent, view, position, id) -> selectItem(position));

        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
        );

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
        if(sizeCheck) {

        }
        tempList.clear();
//        // Check drawer first!
//        if (mDrawerToggle.onOptionsItemSelected(item)) {
//            Log.d(TAG, "onOptionsItemSelected: mDrawerToggle " + mDrawerToggle.onOptionsItemSelected(item));
//            return true;
//        }
//        System.out.println("Print Selected Item"+item);
//        // Now check for menu items
//        String selection = "";
//        if (item.getTitle().equals("Topics")){
//        if (item.getSubMenu().getItem(0).equals( "Topics")) {
//            selection = "You want to do A";
//            System.out.println(selection);
//            //makeMenu(item);
//        }}
//        else if (item.getItemId() == R.id.Countries) {
//            selection = "You have chosen B";
//
////            MenuItem netMenuItem = menu.findItem(R.id.Countries);
////            SubMenu subMenu = netMenuItem.getSubMenu();
////
////            for (int j = 0; j < country_codes.size(); j++) {
////                subMenu.add(item.getGroupId(),j,j,country_codes.get(j));
////            }
//           // makeMenu(item);
//        }
//        else if (item.getItemId() == R.id.Languages) {
//            selection = "C is your selection";
//           // makeMenu(item);
//        }
//        else{
//            System.out.println("Print");
//        }
//
//        textView.setText(selection);
        //setTitle("NewsAggregator ("+temp.length+")");
        setTitle("NewsAggregator ("+drawerItems.length+")");
        return super.onOptionsItemSelected(item);
    }

    private void selectItem(int position) {
        System.out.println(("You picked drawer:"+ drawerItems[position]));
        findViewById(R.id.drawer_layout).setBackgroundResource(0);
        String newsID = "";
        for (Map.Entry<String, String> set :
                newsIDHashMap.entrySet()) {
            if(set.getValue().toUpperCase(Locale.ROOT).equals(drawerItems[position].toUpperCase(Locale.ROOT))){
                newsID = set.getKey();
            }
        }
        new Thread(new NewsVolleyRunnable(this,newsID)).start();

        newsAdapter = new NewsAdapter(this, newsList);

        System.out.println("Print News List :"+ newsList);


        viewPager.setAdapter(newsAdapter);
        viewPager.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        System.out.println("You picked "+ newsID);

        mDrawerLayout.closeDrawer(findViewById(R.id.c_layout));
        setTitle(drawerItems[position]);

    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this,MainActivity.class));
        this.finish();
    }
    public static int getColorDetails(String newsID){
        for(int i = 0;i<drawerDetails.size();i++){
            if(drawerDetails.get(i).containsKey(newsID.trim())){
                return drawerDetails.get(i).get(newsID.trim());
            }
        }
        return Color.WHITE;
    }
}
