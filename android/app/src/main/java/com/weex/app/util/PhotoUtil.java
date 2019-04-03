package com.weex.app.util;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.github.dfqin.grantor.PermissionListener;
import com.github.dfqin.grantor.PermissionsUtil;
import com.taobao.weex.bridge.JSCallback;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public class PhotoUtil {
    private Activity mActivity;
    private static final String TAG = "uploadFile";
//    public static File fileCropUri = new File(Environment.getExternalStorageDirectory().getPath() + "/Pictures/temp.jpg");
    // 拍照回传码
    public final static int CAMERA_REQUEST_CODE = 0;
    // 相册选择回传吗
    public final static int GALLERY_REQUEST_CODE = 1;

    public PhotoUtil(Activity mActivity) {
        super();
        this.mActivity = mActivity;
    }
    public void choosePhoto(){
        if (PermissionsUtil.hasPermission(mActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            openPhoto();
        } else {
            PermissionsUtil.TipInfo tip = new PermissionsUtil.TipInfo("注意:", "需要允许应用进行读写才能进行打开相册", "拒绝", "去设置");
            PermissionsUtil.requestPermission((Activity)mActivity, new PermissionListener() {
                @Override
                public void permissionGranted(@NonNull String[] permissions) {
                    //用户授予权限
                    openPhoto();
                }
                @Override
                public void permissionDenied(@NonNull String[] permissions) {
                    //用户拒绝授予权限
                }
            }, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, true, tip);
        }
    }
    public void showCamera() {
        if (PermissionsUtil.hasPermission(mActivity, Manifest.permission.CAMERA)) {
            openCamera();
        } else {
            PermissionsUtil.TipInfo tip = new PermissionsUtil.TipInfo("注意:", "需要允许应用进行访问摄像才能进行拍照", "拒绝", "去设置");
            PermissionsUtil.requestPermission((Activity)mActivity, new PermissionListener() {
                @Override
                public void permissionGranted(@NonNull String[] permissions) {
                    //用户授予权限
                    openCamera();
                }
                @Override
                public void permissionDenied(@NonNull String[] permissions) {
                    //用户拒绝授予权限
                }
            }, new String[]{Manifest.permission.CAMERA}, true, tip);
        }
    }

    private void openCamera () {
        Intent intentCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File fileCropUri = new File(Environment.getExternalStorageDirectory().getPath() + "/Pictures/temp.jpg");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intentCamera.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); //添加这一句表示对目标应用临时授权该Uri所代表的文件
        }
        intentCamera.putExtra(MediaStore.EXTRA_OUTPUT, Uri
                .fromFile(fileCropUri));
        // temp为保存照片的文件名
        mActivity.startActivityForResult(intentCamera, CAMERA_REQUEST_CODE);
    }

    private void openPhoto () {
        Intent intentToPickPic = new Intent(Intent.ACTION_PICK, null);
        // 如果限制上传到服务器的图片类型时可以直接写如："image/jpeg 、 image/png等的类型" 所有类型则写 "image/*"
        intentToPickPic.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/jpeg");
        mActivity.startActivityForResult(intentToPickPic, GALLERY_REQUEST_CODE);
    }

    public static String getMD5(File file) {
        BigInteger bi = null;
        try {
            byte[] buffer = new byte[8192];
            int len = 0;
            MessageDigest md = MessageDigest.getInstance("MD5");
            File f = file;
            FileInputStream fis = new FileInputStream(f);
            while ((len = fis.read(buffer)) != -1) {
                md.update(buffer, 0, len);
            }
            fis.close();
            byte[] b = md.digest();
            bi = new BigInteger(1, b);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bi.toString(16);
    }

    public static File uriToFile(Uri uri,Context context) {
        String path = null;
        if ("file".equals(uri.getScheme())) {
            path = uri.getEncodedPath();
            if (path != null) {
                path = Uri.decode(path);
                ContentResolver cr = context.getContentResolver();
                StringBuffer buff = new StringBuffer();
                buff.append("(").append(MediaStore.Images.ImageColumns.DATA).append("=").append("'" + path + "'").append(")");
                Cursor cur = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new String[] { MediaStore.Images.ImageColumns._ID, MediaStore.Images.ImageColumns.DATA }, buff.toString(), null, null);
                int index = 0;
                int dataIdx = 0;
                for (cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext()) {
                    index = cur.getColumnIndex(MediaStore.Images.ImageColumns._ID);
                    index = cur.getInt(index);
                    dataIdx = cur.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                    path = cur.getString(dataIdx);
                }
                cur.close();
                if (index == 0) {
                } else {
                    Uri u = Uri.parse("content://media/external/images/media/" + index);
                    System.out.println("temp uri is :" + u);
                }
            }
            if (path != null) {
                return new File(path);
            }
        } else if ("content".equals(uri.getScheme())) {
            // 4.2.2以后
            String[] proj = { MediaStore.Images.Media.DATA };
            Cursor cursor = context.getContentResolver().query(uri, proj, null, null, null);
            if (cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                path = cursor.getString(columnIndex);
            }
            cursor.close();
            return new File(path);
        } else {
            Log.i(TAG, "Uri Scheme:" + uri.getScheme());
        }
        return null;
    }

    public static Uri getImageContentUri(Context context,File imageFile) {
        String filePath = imageFile.getAbsolutePath();
        Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[] { MediaStore.Images.Media._ID }, MediaStore.Images.Media.DATA + "=? ",
                new String[] { filePath }, null);
        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
            Uri baseUri = Uri.parse("content://media/external/images/media");
            return Uri.withAppendedPath(baseUri, "" + id);
        } else {
            if (imageFile.exists()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, filePath);
                return context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            } else {
                return null;
            }
        }
    }
    /*
     * 功能:上传照片并返回url
     * @param callback,picPath,actionUrl,token,fileMd5,fileName,username
     * @return map
     * */
    public static String uploadFile(JSCallback callback, Context context, String picPath, String actionUrl, String fileMd5, String fileName) {
        String end = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";//边界标识
        try {
            URL url = new URL(actionUrl);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            /* 允许Input、Output，不使用Cache */
            con.setDoInput(true);//允许输入流
            con.setDoOutput(true);//允许输出流
            con.setUseCaches(false);//不允许使用缓存
            /* 设置传送的method=POST */
            con.setRequestMethod("POST");
            /* setRequestProperty 设置编码 */
            con.setRequestProperty("Connection", "Keep-Alive");
            con.setRequestProperty("Charset", "UTF-8");
            con.setRequestProperty("Content-Type",// "multipart/form-data"这个参数来说明我们这传的是文件不是字符串了
                    "multipart/form-data;boundary=" + boundary);
            /* 设置DataOutputStream */
            DataOutputStream ds = new DataOutputStream(con.getOutputStream());
            ds.writeBytes(twoHyphens + boundary + end);
            ds.writeBytes("Content-Disposition: form-data; " +
                    "fileName=\"" + fileName + "\";fileMd5=\"" + fileMd5 + "\"" + end);
            ds.writeBytes(end);
            /* 取得文件的FileInputStream */
            FileInputStream fStream = new FileInputStream(picPath);
            /* 设置每次写入1024bytes */
            int bufferSize = 1024;
            byte[] buffer = new byte[bufferSize];
            int length = -1;
            /* 从文件读取数据至缓冲区 */
            while ((length = fStream.read(buffer)) != -1) {
                /* 将资料写入DataOutputStream中 */
                ds.write(buffer, 0, length);
            }
            ds.writeBytes(end);
            ds.writeBytes(twoHyphens + boundary + twoHyphens + end);
            /* close streams */
            fStream.close();
            ds.flush();
            /* 取得Response内容 */
            InputStream is = con.getInputStream();
            int ch;
            StringBuffer b = new StringBuffer();
            while ((ch = is.read()) != -1) {
                b.append((char) ch);
            }
            /* 将Response显示于Dialog */
            Toast.makeText(context, "上传成功", Toast.LENGTH_SHORT).show();
            /* 关闭DataOutputStream */
            ds.close();
            //返回客户端返回的信息
            Map<String, Object> map = new HashMap<>();
            map.put("url", b.toString().trim());
            callback.invoke(map);
            return b.toString().trim();
        } catch (Exception e) {
            Toast.makeText(context, "上传失败" + e, Toast.LENGTH_SHORT).show();
//            Log.d(TAG, "上传失败" +  e);
            Log.e(TAG, "上传失败 an error occured when collect crash info", e);
            return null;
        }
    }

    /*
     * 功能:根据网址获取图片对应的Bitmap对象
     * @param path
     * @return Bitmap
     * */
    public static Bitmap getPicture(String path) {
        Bitmap bm = null;
        URL url;
        try {
            url = new URL(path);//创建URL对象
            URLConnection conn = url.openConnection();//获取URL对象对应的连接
            conn.connect();//打开连接
            InputStream is = conn.getInputStream();//获取输入流对象
            bm = BitmapFactory.decodeStream(is);//根据输入流对象创建Bitmap对象
        } catch (MalformedURLException e1) {
            e1.printStackTrace();//输出异常信息
        } catch (IOException e) {
            e.printStackTrace();//输出异常信息
        }
        return bm;
    }
}
