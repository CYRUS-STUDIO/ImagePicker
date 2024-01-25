package com.linchaolong.android.imagepicker.demo;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.linchaolong.android.imagepicker.ImagePicker;
import com.linchaolong.android.imagepicker.cropper.CropImageView;

/**
 * Created by linchaolong on 2017/3/21.
 */
public class TestFragment extends Fragment {

  private ImagePicker imagePicker = new ImagePicker();
  private ImageView imageView;
  private CropImageView cropImageView;

  @Nullable @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    View contentView = inflater.inflate(R.layout.fragment_test, null);
    init(contentView);
    return contentView;
  }

  @Override public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    imagePicker.onActivityResult(this, requestCode, resultCode, data);
  }

  @Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
      @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    imagePicker.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
  }

  private void init(View contentView) {
    imageView = (ImageView) contentView.findViewById(R.id.imageView);
    cropImageView = (CropImageView) contentView.findViewById(R.id.cropImageView);
    contentView.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        startCameraOrGallery();
      }
    });
  }

  private void startCameraOrGallery() {
    new AlertDialog.Builder(getActivity()).setTitle("设置头像")
        .setItems(new String[] { "选择器(Chooser)", "从相册中选取图片(Gallery)", "拍照(Camera)" }, new DialogInterface.OnClickListener() {
          @Override public void onClick(DialogInterface dialog, int which) {
            // 回调
            ImagePicker.Callback callback = new ImagePicker.Callback() {
              @Override public void onPickImage(Uri imageUri) {
              }

              @Override public void onCropImage(Uri imageUri) {
                imageView.setImageURI(imageUri);
                cropImageView.setImageUriAsync(imageUri);
              }
            };
            if (which == 0) {
              imagePicker.startChooser(TestFragment.this, callback);
            } else if (which == 1) {
              // 从相册中选取图片
              imagePicker.startGallery(TestFragment.this, callback);
            } else {
              // 拍照
              imagePicker.startCamera(TestFragment.this, callback);
            }
          }
        })
        .show()
        .getWindow()
        .setGravity(Gravity.BOTTOM);
  }
}
