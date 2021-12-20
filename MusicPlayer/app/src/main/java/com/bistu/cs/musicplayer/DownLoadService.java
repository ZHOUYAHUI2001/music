package com.bistu.cs.musicplayer;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.bistu.cs.musicplayer.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

/**
 * 下载服务
 *
 */

public class DownLoadService extends IntentService {
    private final String TAG="LOGCAT";
    private int fileLength, downloadLength;//文件大小
    private Handler handler = new Handler();
    private NotificationCompat.Builder builder;
    private NotificationManager manager;
    private int _notificationID = 1024;
    private String songName="";
    public DownLoadService() {
        super("DownLoadService");//name
    }

    @Override
    public void onCreate() {
        super.onCreate();
       //判断是否有权限
        if(!isOpenNotify(this)){
           goToSetNotify(this);
       }
        manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationManagerCompat.from(this).areNotificationsEnabled();

    }

    protected void onHandleIntent(Intent intent) {
        try {
            initNotification();

            Bundle bundle = intent.getExtras();
            String downloadUrl = bundle.getString("download_url");
            File dirs = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/Music","");//文件保存地址
            System.out.println(dirs.toString().trim());
            if (!dirs.exists()) {// 检查文件夹是否存在，不存在则创建
                dirs.mkdir();
            }
            songName = bundle.getString("song");
            File file = new File(dirs, bundle.getString("song")+".mp3");//输出文件名
            Log.d(TAG,"下载启动："+downloadUrl+" --to-- "+ file.getPath());
            manager.notify(_notificationID,builder.build());
            // 开始下载
            downloadFile(downloadUrl, file);
            // 下载结束
            builder.setProgress(0,0,false);//移除进度条
            builder.setContentText("下载结束");
            manager.notify(_notificationID,builder.build());
            manager.cancelAll();
            manager.cancel(_notificationID);

                // 广播下载完成事件，通过广播调起对文件的处理。
            Intent sendIntent = new Intent("downloadComplete");
            sendIntent.putExtra("downloadFile", file.getPath());
            sendBroadcast(sendIntent);
            Log.d(TAG,"下载结束");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 文件下载
     * @param downloadUrl
     * @param file
     */
    public void downloadFile(String downloadUrl, File file){
        FileOutputStream _outputStream;//文件输出流
        try {
            _outputStream = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "找不到目录！");
            e.printStackTrace();
            return;
        }
        InputStream _inputStream = null;//文件输入流
        try {
            URL url = new URL(downloadUrl);
            HttpURLConnection _downLoadCon = (HttpURLConnection) url.openConnection();
            _downLoadCon.setRequestMethod("GET");
            fileLength = Integer.parseInt(_downLoadCon.getHeaderField("Content-Length"));//文件大小
            _inputStream = _downLoadCon.getInputStream();
            int respondCode = _downLoadCon.getResponseCode();//服务器返回的响应码
            if (respondCode == 200) {
                handler.post(run);//更新下载进度
                byte[] buffer = new byte[1024*8];// 数据块，等下把读取到的数据储存在这个数组，这个东西的大小看需要定，不要太小。
                int len;
                while ((len = _inputStream.read(buffer)) != -1) {
                    _outputStream.write(buffer, 0, len);
                    downloadLength = downloadLength + len;
          Log.d(TAG, downloadLength + "/" + fileLength );
                }
            } else {
                Log.d(TAG, "respondCode:" + respondCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {//关流
                if (_outputStream != null) {
                    _outputStream.close();
                }
                if (_inputStream != null) {
                    _inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Runnable run = new Runnable() {
        public void run() {
            Intent resultIntent = new Intent(DownLoadService.this, MainActivity.class);
            resultIntent.setAction(Intent.ACTION_MAIN);
            resultIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            PendingIntent resultPendingIntent = PendingIntent.getActivity(DownLoadService.this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            //building the notification
            builder.setContentIntent(resultPendingIntent);
            //更新或创建通知,并注明通知的id
            //下面一句是悬浮通知与一般通知的唯一区别
            builder.setFullScreenIntent(resultPendingIntent,true);
            int _pec=(int) (downloadLength*100 / fileLength);
            builder.setContentText("下载中……"+_pec+"%");
            builder.setProgress(100, _pec, false);//显示进度条，参数分别是最大值、当前值、是否显示具体进度（false显示具体进度，true就只显示一个滚动色带）
            manager.notify(_notificationID,builder.build());
            System.out.println("下载完成");
             //下载后更新列表
            Toast.makeText(DownLoadService.this,"下载成功",Toast.LENGTH_SHORT).show();
            MainActivity.mDatas.clear();
            loadMusicData();
            handler.postDelayed(run, 1000);
        }
    };

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        handler.removeCallbacks(run);
        super.onDestroy();
    }

    public void initNotification(){
        builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.mipmap.ic_launcher).setContentTitle("下载文件").setContentText("下载中……");//图标、标题、内容这三个设置是必须要有的。
        manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    }


    /**
     *   判断下载是否有通知权限
     */

    public static boolean isOpenNotify(Context context) {
        boolean isOpened = false;

        try {
            NotificationManagerCompat from = NotificationManagerCompat.from(context);
            isOpened = from.areNotificationsEnabled();

        } catch (Exception e) {
            e.printStackTrace();

        }

        return isOpened;

    }


    /**
     * 如果没有则前往设置
     * @param context
     */

    public static void goToSetNotify(Context context) {
        if (Build.VERSION.SDK_INT >= 26) {
            Intent intent = new Intent();

            intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);

            intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.getPackageName());

            context.startActivity(intent);

        } else {
            Intent intent = new Intent();

            intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");

            intent.putExtra("app_package", context.getApplicationContext().getPackageName());

            intent.putExtra("app_uid", context.getApplicationInfo().uid);

            context.startActivity(intent);

        }
    }
    private void loadMusicData() {
        String sid=null;
        //加载本地存储当中的mp3文件到集合当中
        //1.获取ContentResolver对象
        ContentResolver resolver = getContentResolver();
        //2.获取本地音乐地址
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        //3.开始查询地址
        Cursor cursor = resolver.query(uri,null,null,null,null);
        //4.遍历Cursor
        int id = 0;
        while (cursor.moveToNext()){
            String song = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
            String singer = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
            String album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
            id++;
            sid = String.valueOf(id);
            String  path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
            System.out.println(path);
            long duration = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
            SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");
            String time  = sdf.format(new Date(duration));
            //          获取专辑图片主要是通过album_id进行查询
            @SuppressLint("Range") String album_id = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
            //将第一行数据封装到对象中
            MusicBean bean = new MusicBean(sid,song,singer,album,time,path,"");
            MainActivity.mDatas.add(bean);
        }
        Vector<File> mp3 = getAllFiles(Environment.getExternalStorageDirectory()+"/Music", "mp3");
        System.out.println(Environment.getDataDirectory().getAbsolutePath()+"/Music");
        for(int i=0;i<mp3.size();i++){
            String song = mp3.get(i).getName();
            String path = mp3.get(i).getPath();
            sid = String.valueOf(Integer.valueOf(sid)+1);
            MusicBean bean = new MusicBean(sid,song,"","","",path,"");
            MainActivity. mDatas.add(bean);
        }
//        数据源发生变化，提示更新
        MainActivity.adapter.notifyDataSetChanged();
    }
    /**
     * 获取指定目录内所有文件路径
     * @param dirPath 需要查询的文件目录
     * @param fileType   查询类型，比如mp3什么的
     */
    public Vector<File> getAllFiles(String dirPath, String fileType) {
        Vector<File> fileVector = new Vector<>();
        File f = new File(dirPath);
        if (!f.exists()) {//判断路径是否存在
            return fileVector;
        }
        File[] files = f.listFiles();
        if (files == null) {//判断权限
            return fileVector;
        }
        Vector<File> vecFile = new Vector<File>();
        for (File _file : files) {//遍历目录
            if (_file.isFile() && _file.getName().endsWith(fileType)) {
                vecFile.add(_file);
            } else if (_file.isDirectory()) {//查询子目录
                getAllFiles(_file.getAbsolutePath(), fileType);
            } else {
            }
        }
        return vecFile;
    }
}