package com.linchaolong.android.imagepicker.demo;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.view.SimpleDraweeView;
import com.linchaolong.android.imagepicker.ImagePicker;
import com.linchaolong.android.imagepicker.cropper.CropImage;
import com.linchaolong.android.imagepicker.cropper.CropImageView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

  private ImagePicker imagePicker = new ImagePicker();
  private SimpleDraweeView draweeView;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // 设置标题
    imagePicker.setTitle("设置头像");
    // 设置是否裁剪图片
    imagePicker.setCropImage(true);

    findViewById(R.id.fragmentTest).setOnClickListener(this);
    draweeView = findViewById(R.id.draweeView);
    draweeView.setOnClickListener(this);
  }

  @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    imagePicker.onActivityResult(this, requestCode, resultCode, data);
  }

  @Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
      @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    imagePicker.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
  }

  private void startChooser() {
    // 启动图片选择器
    imagePicker.startChooser(this, new ImagePicker.Callback() {
      // 选择图片回调
      @Override public void onPickImage(Uri imageUri) {

      }

      // 裁剪图片回调
      @Override public void onCropImage(Uri imageUri) {
        draweeView.setImageURI(imageUri);
        draweeView.getHierarchy().setRoundingParams(RoundingParams.asCircle());
      }

      // 自定义裁剪配置
      @Override public void cropConfig(CropImage.ActivityBuilder builder) {
        builder
            // 是否启动多点触摸
            .setMultiTouchEnabled(false)
            // 设置网格显示模式
            .setGuidelines(CropImageView.Guidelines.OFF)
            // 圆形/矩形
            .setCropShape(CropImageView.CropShape.RECTANGLE)
            // 调整裁剪后的图片最终大小
            .setRequestedSize(960, 540)
            // 宽高比
            .setAspectRatio(16, 9);
      }

      // 用户拒绝授权回调
      @Override public void onPermissionDenied(int requestCode, String[] permissions,
          int[] grantResults) {
      }
    });
  }

  @Override public void onClick(View v) {
    if (v.getId() == R.id.draweeView) {
      startChooser();
    } else if (v.getId() == R.id.fragmentTest) {
      startActivity(new Intent(this, FragmentTestActivity.class));
    }
  }
}

