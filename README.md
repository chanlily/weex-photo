# weex-photo
wxphoto扩展WXModule组件，提供选择拍照或者选择照片并返回结果，或者上传照片

# 功能
1、选择拍照

2、选择照片

3、上传照片


# 代码展示
## 1、选择拍照


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
    
## 2、选择照片，打开手机图库


private void openPhoto () {

     Intent intentToPickPic = new Intent(Intent.ACTION_PICK, null);
     
     // 如果限制上传到服务器的图片类型时可以直接写如："image/jpeg 、 image/png等的类型" 所有类型则写 "image/*"
     
     intentToPickPic.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/jpeg");
     
     mActivity.startActivityForResult(intentToPickPic, GALLERY_REQUEST_CODE);
     
}


