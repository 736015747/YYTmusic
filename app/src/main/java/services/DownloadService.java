package services;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.mymusic.MainActivity;
import com.example.mymusic.R;

import java.io.File;

public class DownloadService extends Service {

    private static final String CHANNEL_ONE_ID = "1" ;
    private static final CharSequence CHANNEL_ONE_NAME = "yyt";
    private DownloadTask downloadTask;

    private String downloadUrl;
    private String name;


    //匿名类
    private DownloadListener listener = new DownloadListener() {


        @Override
        public void onProgress(int progress) {
            getNotificationManager().notify(1,getNotification("歌曲下载中...",progress));
        }

        @Override
        public void onSucess() {
            downloadTask = null;
            //下载成功时将前台服务通知关闭，并创建一个下载成功的通知
            stopForeground(true);
            getNotificationManager().notify(1,getNotification("歌曲下载完成",-1));
            Toast.makeText(DownloadService.this,"歌曲下载完成",Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onFailed() {
            downloadTask = null;
            //下载失败时将前台服务通知关闭，并创建一个下载失败的通知
            stopForeground(true);
            getNotificationManager().notify(1,getNotification("歌曲下载失败",-1));
            Toast.makeText(DownloadService.this,"歌曲下载失败",Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onPaused() {
            downloadTask = null;
            Toast.makeText(DownloadService.this,"歌曲暂停下载",Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCanceled() {
            downloadTask = null;
            stopForeground(true);
            Toast.makeText(DownloadService.this,"歌曲取消下载",Toast.LENGTH_SHORT).show();
        }
    };

    //为了要让DownloadService可以和活动进行通信，故创建DownloadBinder
    private DownloadBinder mBinder = new DownloadBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
//        //获取从intent获取activity传递过来的数据
//        name = intent.getStringExtra("name");
        return mBinder;
    }

    public class DownloadBinder extends Binder{
        //开始下载
        public void startDownload(String url){
            if (downloadTask == null){
                downloadUrl = url;
                downloadTask = new DownloadTask(listener);
                downloadTask.execute(downloadUrl);
                startForeground(1,getNotification("歌曲下载中...",0));
                Toast.makeText(DownloadService.this,"歌曲下载中...",Toast.LENGTH_SHORT).show();
            }
        }
        //暂停下载
        public void pauseDownLoad(){
            if (downloadTask != null){
                downloadTask.pauseDownload();
            }
        }
        //取消下载
        public void cancelDownload(){
            if (downloadTask != null){
                downloadTask.cancelDownload();
            }else {
                if (downloadUrl != null){
                    //取消下载时需将文件删除，并将通知关闭
                    String fileName = downloadUrl.split("/")[4];
                    String directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getPath();
                    File file = new File(directory,fileName + ".mp3");
                    if (file.exists()){
                        file.delete();
                    }
                    getNotificationManager().cancel(1);
                    stopForeground(true);
                    Toast.makeText(DownloadService.this,"歌曲取消下载",Toast.LENGTH_SHORT).show();
                }
            }
        }

    }

    //实现通知
    private NotificationManager getNotificationManager(){
        return (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    private Notification getNotification(String title, int progress){
        Intent intent = new Intent(this,MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this,0,intent,0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher));
        builder.setContentIntent(pi);
        builder.setContentTitle(title);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            //修改安卓8.1以上系统报错
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ONE_ID, CHANNEL_ONE_NAME,                    NotificationManager.IMPORTANCE_MIN);
            notificationChannel.enableLights(false);//如果使用中的设备支持通知灯，则说明此通知通道是否应显示灯
            notificationChannel.setShowBadge(false);//是否显示角标
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_SECRET);
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            manager.createNotificationChannel(notificationChannel);
            builder.setChannelId(CHANNEL_ONE_ID);
        }

        if (progress > 0){
            //当progress大于或等于0时才需显示下载进度
            builder.setContentText(progress + "%");
            builder.setProgress(100,progress,false);
        }
        return builder.build();
    }
}
