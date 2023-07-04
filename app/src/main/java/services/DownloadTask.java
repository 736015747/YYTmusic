package services;

import android.os.AsyncTask;
import android.os.Environment;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

//具体的下载功能
//泛型参数(执行asynctask的时候传入字符串参数，整型数据进度显示单位，整型数据反馈执行结果)
public class DownloadTask extends AsyncTask<String,Integer,Integer> {

    //四个整型常量用于表示下载的状态
    public static final int TYPE_SUCESS = 0;
    public static final int TYPE_FAILED = 1;
    public static final int TYPE_PAUSED = 2;
    public static final int TYPE_CANCELED = 3;

    private DownloadListener listener;

    private boolean isCanceled = false;

    private boolean isPaused = false;

    private int lastProgress;

    public DownloadTask(DownloadListener listener){
        this.listener = listener;
    }

    //重写，执行下载操作
    @Override
    protected Integer doInBackground(String... params) {
        InputStream is = null;
        RandomAccessFile savedFile = null;
        File file = null;
        try{
            long downloadedLength = 0;  //记录已下载的文件长度
            String downloadUrl = params[0];
            String fileName = downloadUrl.split("/")[4];
            String directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getPath();
            file = new File(directory,fileName + ".mp3");
            if (file.exists()){
                downloadedLength = file.length();
            }
            long contentLength = getContentLength(downloadUrl);
            if (contentLength == 0){
                return TYPE_FAILED;
            }else if (contentLength == downloadedLength){
                //已经下载字节和文件总字节相等，说明已经下载完成了
                return TYPE_SUCESS;
            }

            OkHttpClient client = new OkHttpClient();
            //断点下载，指定从哪个字节开始下载
            Request request = new Request.Builder().addHeader("RANGE","bytes=" + downloadedLength + "-").url(downloadUrl).build();
            Response response = client.newCall(request).execute();
            if (response != null){
                is = response.body().byteStream();
                savedFile = new RandomAccessFile(file,"rw");
                savedFile.seek(downloadedLength);    //跳过已经下载的字节
                byte[] b = new byte[1024];
                int total = 0;
                int len;
                while ((len = is.read(b)) != -1){
                    if (isCanceled){
                        return TYPE_CANCELED;
                    }else if (isPaused){
                        return TYPE_PAUSED;
                    }else {
                        total += len;
                        savedFile.write(b,0,len);
                        //计算已经下载的百分比
                        int progress = (int) ((total + downloadedLength) * 100 / contentLength);
                        publishProgress(progress);
                    }
                }
                response.body().close();
                return TYPE_SUCESS;
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try{
                if (is != null){
                    is.close();
                }
                if (savedFile != null){
                    savedFile.close();
                }
                if (isCanceled && file != null){
                    file.delete();
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return TYPE_FAILED;
    }

    //从参数中获取当前的下载进度，与上次有变化的话调用listener.onProgress()方法通知下载进度更新
    @Override
    protected void onProgressUpdate(Integer... values) {
        int progress = values[0];
        if (progress > lastProgress){
            listener.onProgress(progress);
            lastProgress = progress;
        }
    }

    //根据参数的下载状态来进行回调，成功调用onSucess()，失败调用....
    @Override
    protected void onPostExecute(Integer status) {
        switch (status){
            case TYPE_SUCESS:
                listener.onSucess();
                break;
            case TYPE_FAILED:
                listener.onFailed();
                break;
            case TYPE_PAUSED:
                listener.onPaused();
                break;
            case TYPE_CANCELED:
                listener.onCanceled();
                break;
            default:
                break;
        }
    }

    public void pauseDownload(){
        isPaused = true;
    }

    public void cancelDownload(){
        isCanceled = true;
    }

    private long getContentLength(String downloadUrl) throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(downloadUrl).build();
        Response response = client.newCall(request).execute();
        if (response != null && response.isSuccessful()){
            long contentLength = response.body().contentLength();
            response.close();
            return contentLength;
        }
        return 0;
    }
}
