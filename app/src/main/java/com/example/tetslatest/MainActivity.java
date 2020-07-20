package com.example.tetslatest;
import java.net.URL;
import java.net.HttpURLConnection;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import android.os.StrictMode;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.ArrayList;


public class MainActivity extends Activity {

    ListView list;
    CustomListAdapter adapter;
    ArrayList<Model> modelList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        modelList = new ArrayList<Model>();
        adapter = new CustomListAdapter(getApplicationContext(), modelList);
        list=(ListView)findViewById(R.id.list);
        list.setAdapter(adapter);
        LocalBroadcastManager.getInstance(this).registerReceiver(onNotice, new IntentFilter("Msg"));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);//Menu Resource, Menu
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            Intent intent = new Intent(
                    "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private BroadcastReceiver onNotice= new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
           // String pack = intent.getStringExtra("package");
            String title = intent.getStringExtra("title");
            String text = intent.getStringExtra("text");
            //int id = intent.getIntExtra("icon",0);

            Context remotePackageContext = null;
            try {
//                remotePackageContext = getApplicationContext().createPackageContext(pack, 0);
//                Drawable icon = remotePackageContext.getResources().getDrawable(id);
//                if(icon !=null) {
//                    ((ImageView) findViewById(R.id.imageView)).setBackground(icon);
//                }
                byte[] byteArray =intent.getByteArrayExtra("icon");
                Bitmap bmp = null;
                if(byteArray !=null) {
                    bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
                }
                Model model = new Model();
                model.setName(title +" " +text);
                model.setImage(bmp);

                if(modelList !=null) {
                    modelList.add(model);
                    adapter.notifyDataSetChanged();
                }else {
                    modelList = new ArrayList<Model>();
                    modelList.add(model);
                    adapter = new CustomListAdapter(getApplicationContext(), modelList);
                    list=(ListView)findViewById(R.id.list);
                    list.setAdapter(adapter);
                }
                PostInfo(title, text);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    @SuppressLint("WrongConstant")
    public void PostInfo(String title, String content) {
        try {
            String path = "http://192.168.1.104:8888/post?title="
                    + title + "&content=" + content;
            // 1.定义请求url
            URL url = new URL(path);
            // 2.建立一个http的连接
            HttpURLConnection conn = (HttpURLConnection) url
                    .openConnection();
            // 3.设置一些请求的参数
            conn.setRequestMethod("POST");
            conn.setRequestProperty(
                    "User-Agent",
                    "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.101 Safari/537.36");
            conn.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded");
            String data = "username=" + title + "&password=" + content;
           // conn.setRequestProperty("Content-Length", data.length() + "");
            conn.setConnectTimeout(5000);//设置连接超时时间
            conn.setReadTimeout(5000); //设置读取的超时时间

            // 4.一定要记得设置 把数据以流的方式写给服务器
            conn.setDoOutput(true); // 设置要向服务器写数据
            conn.getOutputStream().write(data.getBytes());

            int code = conn.getResponseCode(); // 服务器的响应码 200 OK //404 页面找不到
            // // 503服务器内部错误
            if (code == 200) {
                InputStream is = conn.getInputStream();
                // 把is的内容转换为字符串
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int len = -1;
                while ((len = is.read(buffer)) != -1) {
                    bos.write(buffer, 0, len);
                }
                String result = new String(bos.toByteArray());
                is.close();
                Toast.makeText(this, result, 0).show();

            } else {
                Toast.makeText(this, "请求失败，失败原因: " + code, 0).show();
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "请求失败，请检查logcat日志控制台", 0).show();
        }
    }
}
