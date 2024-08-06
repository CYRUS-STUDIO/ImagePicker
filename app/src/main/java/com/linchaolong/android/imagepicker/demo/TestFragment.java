package com.linchaolong.android.imagepicker.demo;

import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.linchaolong.android.imagepicker.ImagePicker;
import com.linchaolong.android.imagepicker.cropper.CropImageView;

/**
 * Created by linchaolong on 2017/3/21.
 */
public class TestFragment extends Fragment {

    private ImagePicker imagePicker;
    private ImageView imageView;
    private CropImageView cropImageView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imagePicker = new ImagePicker(this, new ImagePicker.Callback() {
            @Override
            public void onCropImage(Uri imageUri) {
                imageView.setImageURI(imageUri);
                cropImageView.setImageUriAsync(imageUri);
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.fragment_test, null);
        init(contentView);
        return contentView;
    }

    private void init(View contentView) {
        imageView = (ImageView) contentView.findViewById(R.id.imageView);
        cropImageView = (CropImageView) contentView.findViewById(R.id.cropImageView);
        contentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCameraOrGallery();
            }
        });
    }

    private void startCameraOrGallery() {
        new AlertDialog.Builder(getActivity()).setTitle("设置头像")
                .setItems(new String[]{"选择器(Chooser)", "从相册中选取图片(Gallery)", "拍照(Camera)"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            imagePicker.startChooser();
                        } else if (which == 1) {
                            // 从相册中选取图片
                            imagePicker.startGallery();
                        } else {
                            // 拍照
                            imagePicker.startCamera();
                        }
                    }
                })
                .show()
                .getWindow()
                .setGravity(Gravity.BOTTOM);
    }
}
