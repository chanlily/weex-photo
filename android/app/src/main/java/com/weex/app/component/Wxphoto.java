package com.weex.app.component;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;

import com.taobao.weex.annotation.JSMethod;
import com.taobao.weex.bridge.JSCallback;
import com.taobao.weex.common.WXModule;
import com.weex.app.util.DataCleanUtil;
import com.weex.app.util.PhotoUtil;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class Wxphoto extends WXModule {
    // 拍照的照片的存储位置
    private String mTempPhotoPath;
    // 照片所在的Uri地址
//    private Uri imageUri;
    public static JSCallback myCallback = null;

    @JSMethod
    public void openCamera(JSCallback callback){
        PhotoUtil photoUtil = new PhotoUtil((Activity) mWXSDKInstance.getContext());
        photoUtil.showCamera();
        if(callback!=null){
            myCallback = callback;
        }
    }
    @JSMethod
    public void openGallerySingle (JSCallback callback) {
        PhotoUtil photoUtil = new PhotoUtil((Activity) mWXSDKInstance.getContext());
        photoUtil.choosePhoto();
        if(callback!=null){
            myCallback = callback;
        }
    }
    @JSMethod
    public void uploadImg (String picPath, String actionUrl,String fileMd5, String fileName) {

        PhotoUtil photoUtil = new PhotoUtil((Activity) mWXSDKInstance.getContext());
        photoUtil.uploadFile(callback,mWXSDKInstance.getContext(),picPath, actionUrl,fileMd5,fileName);
    }
    /**
     * 用来处理startActivityForResult返回的数据
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (resultCode == -1) {
            switch (requestCode) {
                case PhotoUtil.CAMERA_REQUEST_CODE ://拍照完回调
                    File fileCropUri = new File(Environment.getExternalStorageDirectory().getPath() + "/Pictures/temp.jpg");
                    Uri imageUri = PhotoUtil.getImageContentUri(mWXSDKInstance.getContext(), fileCropUri);
                    String Camera_url = imageUri.getPath();
                    File Camera_file = PhotoUtil.uriToFile(imageUri,mWXSDKInstance.getContext());
                    String Camera_fileMd5 = PhotoUtil.getMD5(Camera_file);
                    String Camera_filePath  = Camera_file.getAbsolutePath();
                    Map<String, Object> Camera_map = new HashMap<>();
                    Camera_map.put("filePath", Camera_filePath);
                    Camera_map.put("fileMd5", Camera_fileMd5);
                    Camera_map.put("url","content://media" +  Camera_url);
                    myCallback.invoke(Camera_map);
                    break;
                case  PhotoUtil.GALLERY_REQUEST_CODE://访问相册后回调
                    Uri imageUri1 = intent.getData();
                    String img_url = imageUri1.getPath();
                    File file = PhotoUtil.uriToFile(imageUri1,mWXSDKInstance.getContext());
                    String fileMd5 = PhotoUtil.getMD5(file);
                    String fileName = file.getName();
                    String filePath  = file.getAbsolutePath();
                    Map<String, Object> map = new HashMap<>();
                    map.put("filePath", filePath);
                    map.put("url", "content://media" + img_url);
                    map.put("fileMd5", fileMd5);
                    map.put("fileName", fileName);
                    myCallback.invoke(map);
                    break;
            }
        }
    }
    /***
     * 释放图片资源
     */

}
