package com.demo.myapplication;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.soundcloud.android.crop.Crop;

import java.io.File;
import java.util.ArrayList;

import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.UploadFileListener;
import me.iwf.photopicker.PhotoPicker;

public class MainActivity extends AppCompatActivity {

    private ImageView iv_select_image;
    //输出路径
    private String outPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        init();

    }

    private void initView() {
        iv_select_image = (ImageView) findViewById(R.id.iv_select_image);
        //这个需要申请sd卡权限
        outPath = Environment.getExternalStorageDirectory() + "/a.png";
    }

    private void init() {
        iv_select_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImage();
            }
        });
        getSd();
    }


    private void selectImage() {
        PhotoPicker.builder()
                .setPhotoCount(1)//可选择图片数量
                .setShowCamera(true)//是否显示拍照按钮
                .setShowGif(false)//是否显示动态图
                .setPreviewEnabled(false)//是否可以预览
                .start(this, PhotoPicker.REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PhotoPicker.REQUEST_CODE && resultCode == RESULT_OK) {
            ArrayList<String> callbackCoverImages = data.getStringArrayListExtra(PhotoPicker.KEY_SELECTED_PHOTOS);
            String inputPath = callbackCoverImages.get(0);
            //输入Uri
            Uri inputUri = Uri.fromFile(new File(inputPath));
            //输出Uri
            Uri outUri = Uri.fromFile(new File(outPath));
            //截取图片
            Crop.of(inputUri, outUri).asSquare().start(MainActivity.this);
        } else if (requestCode == Crop.REQUEST_CROP && resultCode == RESULT_OK) {
            //回调上传文件
            String tempPath = ImageDeal.bitmapToString(outPath);
            uploadFile(tempPath, outPath);
        }
    }

    private void uploadFile(final String tempPath, final String uploadImageUrl) {
        final BmobFile bmobFile = new BmobFile(new File(uploadImageUrl));
        bmobFile.uploadblock(new UploadFileListener() {
            @Override
            public void done(BmobException e) {
                if (e == null) {
                    Log.e("error", "文件：" + bmobFile.getFileUrl());
                    //bmobFile.getFileUrl()--返回的上传文件的完整地址
                    Glide.with(MainActivity.this)
                            .load(bmobFile.getFileUrl())
                            .dontAnimate()
                            .into(iv_select_image);
                    File file = new File(uploadImageUrl);
                    if (file.exists()) {
                        file.delete();
                    }
                    File file2 = new File(tempPath);
                    if (file2.exists()) {
                        file2.delete();
                    }
                } else {
                    Log.e("error", "异常：" + e.toString());
                }

            }

            @Override
            public void onProgress(Integer value) {
                // 返回的上传进度（百分比）
            }
        });


    }

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private void getSd() {
        int permissionWrite = ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permissionRead = ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE);

        if (permissionWrite != PackageManager.PERMISSION_GRANTED || permissionRead != PackageManager.PERMISSION_GRANTED) {
            //没有权限
            ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
        }
    }

}
