package com.example.mylauncher;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class AppListActivity extends AppCompatActivity {

    private PackageManager manager;
    private List<Item> apps;
    private ListView list;
    HttpHandler sh;
    public final String TAG = "AppListActivity";
    HashMap<String, Integer> blackList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_list);

        populateDenyList();

        loadApps();
        loadListView();
        addItemClickListener();
    }
    private void populateDenyList()
    {
        blackList = new HashMap<String,Integer>();
        final String[] jsonStr = new String[1];
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpHandler sh = new HttpHandler();
                String url = "https://api.jsonbin.io/v3/b/6323a7995c146d63ca9d124f";
                String jsonStr = sh.makeServiceCall(url);

                if(jsonStr != null) {
                    try {
                        JSONObject jsonObj = new JSONObject(jsonStr);

                        JSONObject jsonObject = jsonObj.getJSONObject("record");
                        JSONArray blackListArray =  jsonObject.getJSONArray("denylist");
                        Log.e(TAG, "run: "+blackListArray.length() );
                        for(int i = 0; i < blackListArray.length(); i++)
                        {
                            Log.e(TAG, "populateDenyList:2 "+blackListArray.getString(i) );
                            blackList.put(blackListArray.getString(i),0);
                        }
                    }
                    catch (final JSONException e) {
                        Log.e(TAG, "Json parsing error: " + e.getMessage());
                    }
                }
                else
                {

                }
            }


        }).start();

        }
        

    private void loadApps()  {
        manager = getPackageManager();
        apps = new ArrayList<>();
        Intent i =new Intent(Intent.ACTION_MAIN,null);
        i.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> availableApps = manager.queryIntentActivities(i,0);

        for(ResolveInfo ri:availableApps)
        {


            Item app = new Item();
            app.label = ri.activityInfo.packageName;
            app.name = ri.loadLabel(manager);
            app.icon = ri.loadIcon(manager);


            apps.add(app);
        }

    }
    private void loadListView(){
        list=(ListView) findViewById(R.id.list);
        ArrayAdapter<Item> adapter = new ArrayAdapter<Item>(this,R.layout.item,apps){
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                if(convertView  == null)
                {
                    convertView = getLayoutInflater().inflate(R.layout.item,null);
                }
                if(blackList.containsKey(apps.get(position).label.toString()))
                {
                    convertView.setBackgroundColor(getResources().getColor(R.color.background_item));
                }
                ImageView appIcon = (ImageView) convertView.findViewById(R.id.icon);
                appIcon.setImageDrawable(apps.get(position).icon);

                TextView appName = (TextView) convertView.findViewById(R.id.name);
                appName.setText(apps.get(position).name);

                return convertView;
            }
        };

        list.setAdapter(adapter);
    }

    private void addItemClickListener(){
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(blackList.containsKey(apps.get(i).label.toString()))
                    return;
                Intent intent =manager.getLaunchIntentForPackage(apps.get(i).label.toString());
                startActivity(intent);
            }
        });
    }

    @Override
    public void onBackPressed() {
        return;
    }
}