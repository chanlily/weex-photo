package com.weex.app.component;

import android.app.ProgressDialog;
import android.util.Log;

import com.nanchen.compresshelper.CompressHelper;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.taobao.weex.annotation.JSMethod;
import com.taobao.weex.bridge.JSCallback;
import com.taobao.weex.common.WXModule;
import com.weex.app.util.DataCleanUtil;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

//import java.io.File;

public class UploadImg extends WXModule {
    private static final String TAG = "uploadFile";
    private static String ActionUrl;
    private static String FileMd5;
    private static String PicPath;
    private static JSCallback myCallback = null;
    private static ProgressDialog dialog = null;

    @JSMethod
    public void uploadImg(String picPath, String actionUrl, String fileMd5, JSCallback callback) {
        ActionUrl = actionUrl;
        PicPath = picPath;
        FileMd5 = fileMd5;
        uploadFile();
        if(callback!=null){
            myCallback = callback;
        }
    }
    /**
     * 从本地获取图片上传
     */
    private void uploadFile() {
        dialog = new ProgressDialog(mWXSDKInstance.getContext());
        dialog.setMessage("正在上传图片，请稍候");
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setCancelable(true);
        dialog.show();
        //访问网络上传图片
        new Thread(new uploadFile(dialog)).start();
    }

    /**
     * 访问网络上传图片
     */
    private class uploadFile implements Runnable {
        private ProgressDialog dialog;
        private uploadFile(ProgressDialog dialog) {
            this.dialog = dialog;
        }
        @Override
        public void run() {
            OkHttpClient mOkHttpClient = new OkHttpClient();
            File file = new File(PicPath);
            if(!file.exists()){
                return;
            }
            //压缩图片
            // .setMaxWidth(720)  // 默认最大宽度为720
            // .setMaxHeight(960) // 默认最大高度为960
            // .setQuality(80)    // 默认压缩质量为80
            File newFile = CompressHelper.getDefault(mWXSDKInstance.getContext()).compressToFile(file);
            RequestBody fileBody = RequestBody.create(MediaType.parse("image/*"), newFile);
            RequestBody requestBody = new MultipartBuilder()
                    .type(MultipartBuilder.FORM)
                    .addFormDataPart("file", file.getName(),
                            fileBody)
                    .addFormDataPart("fileMd5",FileMd5)
                    .build();
            Request request = new Request.Builder()
                    .url(ActionUrl)
                    .addHeader("user-agent", "android")
                    .post(requestBody)
                    .build();
            try {
                Response response = mOkHttpClient.newCall(request).execute();
                if (response.isSuccessful()) {
                    String resultValue = response.body().string();
                    dialog.setProgress(90);
                    JSONObject object = new JSONObject(resultValue);
                    Map<String, Object> map = new HashMap<>();
					// object返回数据输出
                    map.put("code",object.get("code"));
                    map.put("msg",object.get("msg"));
                    map.put("url",object.get("url"));
                    myCallback.invoke(map);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "upload IOException ", e);
            }
            dialog.dismiss();
        }
    }
}
