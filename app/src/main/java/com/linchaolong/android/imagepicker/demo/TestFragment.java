package com.linchaolong.android.imagepicker.demo;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.view.SimpleDraweeView;
import com.linchaolong.android.imagepicker.ImagePicker;

/**
 * Created by linchaolong on 2017/3/21.
 */
public class TestFragment extends Fragment {

  private ImagePicker imagePicker = new ImagePicker();
  private SimpleDraweeView draweeView;

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
    imagePicker.setCropImage(false); // 不裁剪图片
    draweeView = (SimpleDraweeView) contentView.findViewById(R.id.draweeView);
    draweeView.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        startCameraOrGallery();
      }
    });
  }

  private void startCameraOrGallery() {
    new AlertDialog.Builder(getActivity()).setTitle("设置头像")
        .setItems(new String[] { "从相册中选取图片", "拍照" }, new DialogInterface.OnClickListener() {
          @Override public void onClick(DialogInterface dialog, int which) {
            if (which == 0) {
              // 选择图片
              imagePicker.startGallery(TestFragment.this, new ImagePicker.Callback() {
                @Override public void onPickImage(Uri imageUri) {
                  draweeView.setImageURI(imageUri);
                  draweeView.getHierarchy().setRoundingParams(RoundingParams.asCircle());
                }
              });
            } else {
              // 拍照
              imagePicker.startCamera(TestFragment.this, new ImagePicker.Callback() {
                @Override public void onPickImage(Uri imageUri) {
                  draweeView.setImageURI(imageUri);
                  draweeView.getHierarchy().setRoundingParams(RoundingParams.asCircle());
                }
              });
            }
          }
        })
        .show().getWindow().setGravity(Gravity.BOTTOM);
  }

}
