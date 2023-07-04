package com.example.mymusic;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.example.mymusic.R;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import list.Song;
import list.SongAdapter;
import services.DownloadService;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private DownloadService.DownloadBinder downloadBinder;

    //创建匿名类来在活动中调用服务提供的各种方法
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            downloadBinder = (DownloadService.DownloadBinder) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    private List<Song> songList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //没有权限的话去申请权限
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
        }

        //开启下载服务
        //注册下载服务intent
        Intent downloadIntent = new Intent(getApplicationContext(),DownloadService.class);
        bindService(downloadIntent,connection,BIND_AUTO_CREATE);


//        //使用ArrayAdapter适配器，泛型适用String，android.R.layout.simple_list_item_1作为ListView的子项布局。
//        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
//                MainActivity.this,android.R.layout.simple_list_item_1,data);
//        ListView listView = (ListView) findViewById(R.id.list_view);
//        listView.setAdapter(adapter);

        initSongs();   //初始化歌曲数据
        SongAdapter adapter = new SongAdapter(MainActivity.this,R.layout.song, songList);
        ListView listView = (ListView) findViewById(R.id.list_view);
        listView.setAdapter(adapter);

        //为每个item设置监听点击时间，获取到点击item的position
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Song song = songList.get(position);
//                Toast.makeText(MainActivity.this,position + "   " + song.getDownUri(),Toast.LENGTH_SHORT).show();

                //搜索歌曲文件是否存在
                String fileName = song.getDownUri().split("/")[4] + ".mp3";
                String uri = searchFile(fileName);
                Log.d(TAG, "onItemClick: " + uri);

                //不存在则下载
                if (uri == null){
                    //提示
                    Toast.makeText(MainActivity.this,"歌曲未下载",Toast.LENGTH_SHORT).show();
                    //开始下载歌曲
                    downloadBinder.startDownload(song.getDownUri());
                }else {
                    //存在则跳转到播放界面
                    Intent playIntent = new Intent(MainActivity.this,MusicPlayer.class);
                    playIntent.putExtra("list", (Serializable) songList);
                    playIntent.putExtra("position",position);
                    startActivity(playIntent);
                }
            }
        });

    }

    private void initSongs() {
        for (int i = 0;i < 1;i++){
            Song song1 = new Song("1","Minor Piano","/sdcard/Music/minor-piano.mp3","https://freemusicarchive.org/track/minor-piano/download","Polkavant","Box 100",R.drawable.pic9);
            songList.add(song1);
            Song song2 = new Song("2","Rain Man","/sdcard/Music/rain-man.mp3","https://freemusicarchive.org/track/rain-man/download","Ketsa","Cosmic Blossom ",R.drawable.pic9);
            songList.add(song2);
            Song song3 = new Song("3","Little Feet","/sdcard/Music/little-feet.mp3","https://freemusicarchive.org/track/little-feet/download","Beat Mekanik","NA",R.drawable.pic9);
            songList.add(song3);
            Song song4 = new Song("4","Come Together","/sdcard/Music/come-together-in-the-lounge.mp3","https://freemusicarchive.org/track/come-together-in-the-lounge/download","Kathrin Klimek","Lounge Sounds",R.drawable.pic9);
            songList.add(song4);
            Song song5 = new Song("5","Melody of Love","/sdcard/Music/melody-of-love.mp3","https://freemusicarchive.org/track/melody-of-love/download","TimTaj","Melody of Love",R.drawable.pic9);
            songList.add(song5);
            Song song6 = new Song("6","Liquid Sun","/sdcard/Music/liquid-sun.mp3","https://freemusicarchive.org/track/liquid-sun/download","Kathrin Klimek","Cinema",R.drawable.pic9);
            songList.add(song6);
            Song song7 = new Song("7","Lucky Tears","/sdcard/Music/lucky-tears.mp3","https://freemusicarchive.org/track/lucky-tears/download","Kathrin Klimek","Cinema",R.drawable.pic9);
            songList.add(song7);
            Song song8 = new Song("8","Lift Off","/sdcard/Music/lift-off.mp3","https://freemusicarchive.org/track/lift-off/download","Kathrin Klimek","Cinema",R.drawable.pic9);
            songList.add(song8);
        }
    }

    //判断有无权限，没有权限的话退出程序
    public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] grantResults){
        switch (requestCode){
            case 1:
                if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this,"拒绝权限无法使用程序",Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
                break;
        }
    }

    protected void onDestory(){
        super.onDestroy();
        unbindService(connection);
    }

    //搜索文件是否在目录下，成功返回路径
    private String searchFile(String keyword) {
        String result = null;
        File[] files = new File("/sdcard/Music").listFiles();
        for (File file : files) {
            if (file.getName().equals(keyword)) {
                result = file.getPath();
                break;
            }
        }
        return result;
    }
}