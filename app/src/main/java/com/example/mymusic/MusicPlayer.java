package com.example.mymusic;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import list.Song;

public class MusicPlayer extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MusicPlayer";
    private MediaPlayer mediaPlayer = new MediaPlayer();
    private Timer timer;
    private boolean isSeekBarChanging;//互斥变量，防止进度条与定时器冲突。
    SimpleDateFormat format;

    private TextView musicName, musicLength, musicCur;
    private SeekBar seekBar;

    String uri;
    private List<Song> songList;
    private int position;

    Button previous;
    Button play;
    Button next;

    private ObjectAnimator animator;//运用ObjectAnimator实现转动
    private ImageView pic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        //获取到intent传过来的数据
        Intent intent = getIntent();
        songList = (List<Song>) intent.getSerializableExtra("list");
        position = intent.getIntExtra("position", 0);

        format = new SimpleDateFormat("mm:ss");

        //唱片旋转效果
        pic = (ImageView) findViewById(R.id.pic);
        pic.setImageResource(songList.get(position).getPlayUri());
        animator = ObjectAnimator.ofFloat(pic, "rotation", 0f, 360.0f);
        animator.setDuration(100000/2);
        animator.setInterpolator(new LinearInterpolator());//匀速
        animator.setRepeatCount(-1);//设置动画重复次数（-1代表一直转）
        animator.setRepeatMode(ValueAnimator.RESTART);//动画重复模式

        //监听按钮点击事件
        previous = (Button) findViewById(R.id.previous);
        play = (Button) findViewById(R.id.play);
        next = (Button) findViewById(R.id.next);

        previous.setOnClickListener(this);
        play.setOnClickListener(this);
        next.setOnClickListener(this);

        musicName = (TextView) findViewById(R.id.music_name);
        musicLength = (TextView) findViewById(R.id.music_length);
        musicCur = (TextView) findViewById(R.id.music_cur);

        seekBar = (SeekBar) findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(new MySeekBar());

        //检查有无权限，没有的话申请权限
        if (ContextCompat.checkSelfPermission(MusicPlayer.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MusicPlayer.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        } else {
            //初始化MediaPlayer
            initMediaPlayer(position);

            //开始播放
            play.setBackgroundResource(R.drawable.pause);
            mediaPlayer.start();
            animator.start();


            //监听播放时回调函数
            timer = new Timer();
            timer.schedule(new TimerTask() {

                Runnable updateUI = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            musicCur.setText(format.format(mediaPlayer.getCurrentPosition()) + "");
                        }catch (Exception e){
                            return;
                        }

                    }
                };

                @Override
                public void run() {
                    if (!isSeekBarChanging) {
                        seekBar.setProgress(mediaPlayer.getCurrentPosition());
                        runOnUiThread(updateUI);
                    }
                }
            }, 0, 50);
        }


    }

    private void initMediaPlayer(final int position) {
        try {
            mediaPlayer.reset();
            Log.d(TAG, "initMediaPlayer: " + position);
            //指定音频文件的路径
            Log.d(TAG, "initMediaPlayer: " + songList.get(position).getUri());
            mediaPlayer.setDataSource(songList.get(position).getUri());
            //让MediaPlayer进入到准备状态
            mediaPlayer.prepare();
            mediaPlayer.setLooping(true);
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                public void onPrepared(MediaPlayer mp) {
                    seekBar.setMax(mediaPlayer.getDuration());
                    musicLength.setText(format.format(mediaPlayer.getDuration()) + "");
                    musicCur.setText("00:00");
                    musicName.setText(songList.get(position).getName());
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //判断有无权限，没有权限的话退出程序
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "拒绝权限无法使用程序", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.previous:
                //上一首
                if (position == 0) {
                    Toast.makeText(MusicPlayer.this, "已经是第一首了", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    position--;
                    //没有上一首
                    String fileName = songList.get(position).getDownUri().split("/")[4] + ".mp3";
                    if (searchFile(fileName) == null){
                        Toast.makeText(MusicPlayer.this, "上一首还未下载", Toast.LENGTH_SHORT).show();
                        position++;
                        return;
                    }
                    initMediaPlayer(position);
                    play.setBackgroundResource(R.drawable.pause);
                    pic.setImageResource(songList.get(position).getPlayUri());
                    mediaPlayer.start();
                    animator.start();
                }
                break;
            case R.id.play:
                if (!mediaPlayer.isPlaying()) {
                    //开始播放
                    v.setBackgroundResource(R.drawable.pause);
                    mediaPlayer.start();
                    animator.resume();
                } else {
                    //暂停播放
                    v.setBackgroundResource(R.drawable.play);
                    mediaPlayer.pause();
                    animator.pause();
                }
                break;
            case R.id.next:
                //下一首
                if (position == songList.size()) {
                    Toast.makeText(MusicPlayer.this, "已经是最后一首了", Toast.LENGTH_SHORT).show();
                } else {
                    position++;
                    //没有下一首
                    String fileName = songList.get(position).getDownUri().split("/")[4] + ".mp3";
                    if (searchFile(fileName) == null){
                        Toast.makeText(MusicPlayer.this, "下一首还未下载", Toast.LENGTH_SHORT).show();
                        position--;
                        return;
                    }
                    initMediaPlayer(position);
                    play.setBackgroundResource(R.drawable.pause);
                    pic.setImageResource(songList.get(position).getPlayUri());
                    mediaPlayer.start();
                    animator.start();
                }
                break;
            default:
                break;

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //活动销毁的时候释放mediaPlayer
        isSeekBarChanging = true;
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (timer != null) {
            timer.cancel();
            timer = null;
        }

    }

    /*进度条处理*/
    public class MySeekBar implements SeekBar.OnSeekBarChangeListener {

        public void onProgressChanged(SeekBar seekBar, int progress,
                                      boolean fromUser) {
        }

        /*滚动时,应当暂停后台定时器*/
        public void onStartTrackingTouch(SeekBar seekBar) {
            isSeekBarChanging = true;
        }

        /*滑动结束后，重新设置值*/
        public void onStopTrackingTouch(SeekBar seekBar) {
            isSeekBarChanging = false;
            mediaPlayer.seekTo(seekBar.getProgress());
        }
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