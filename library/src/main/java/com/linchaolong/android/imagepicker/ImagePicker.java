package com.linchaolong.android.imagepicker;

import static androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia;
import static androidx.activity.result.contract.ActivityResultContracts.RequestPermission;
import static androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultCaller;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.linchaolong.android.imagepicker.cropper.CropImage;
import com.linchaolong.android.imagepicker.cropper.CropImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

/**
 * 图片选择，裁剪封装类
 * <p>
 * Created by linchaolong on 2017/3/9.
 */
public class ImagePicker {

    private static final String TAG = "ImagePicker";

    @NonNull
    private final ActivityResultCaller caller;
    @NonNull
    private final Callback callback;
    private boolean isCropImage = true;
    private CharSequence title;

    private Uri imageUrlForRequestPermission;
    private final ActivityResultLauncher<Void> galleryLauncher;
    private final ActivityResultLauncher<Intent> chooserLauncher;
    private final ActivityResultLauncher<Intent> cropLauncher;
    private final ActivityResultLauncher<Void> requestCameraPermissionForChooser;
    private final ActivityResultLauncher<Void> requestCameraPermissionForCamera;
    private ActivityResultLauncher<Void> requestReadPermission;

    private Context getContext() {
        if (caller instanceof ComponentActivity) {
            return (Context) caller;
        } else if (caller instanceof Fragment) {
            return ((Fragment) caller).requireContext();
        }
        throw new IllegalArgumentException("caller must be ComponentActivity or Fragment: " + caller);
    }

    public ImagePicker(ActivityResultCaller caller, @NonNull Callback callback) {
        this.caller = caller;
        this.callback = callback;
        galleryLauncher = caller.registerForActivityResult(
                new WithInputActivityResultContract<>(
                        new PickVisualMedia(),
                        new PickVisualMediaRequest.Builder()
                                .setMediaType(PickVisualMedia.ImageOnly.INSTANCE)
                                .build()
                ),
                imageUri -> {
                    if (imageUri == null) {
                        return;
                    }
                    handlePickImage(imageUri);
                }
        );
        chooserLauncher = caller.registerForActivityResult(new StartActivityForResult(), result -> {
            if (result.getResultCode() != Activity.RESULT_OK) {
                return;
            }
            Uri imageUri = CropImage.getPickImageResultUri(getContext(), result.getData());
            // 检查读取文件权限
            if (CropImage.isReadExternalStoragePermissionsRequired(getContext(), imageUri)) {
                this.imageUrlForRequestPermission = imageUri;
                requestReadPermission.launch(null);
            } else {
                // 选择图片回调
                handlePickImage(imageUri);
            }
        });
        cropLauncher = caller.registerForActivityResult(new StartActivityForResult(), result -> {
            if (result.getResultCode() != Activity.RESULT_OK) {
                return;
            }

            Intent data = result.getData();
            if (data != null) {
                handleCropResult(getContext(), CropImage.getActivityResult(data));
            }
        });
        requestReadPermission = caller.registerForActivityResult(
                new WithInputActivityResultContract<>(new RequestPermission(), Manifest.permission.READ_EXTERNAL_STORAGE),
                result -> {
                    if (!result) {
                        callback.onPermissionDenied(Manifest.permission.READ_EXTERNAL_STORAGE);
                        return;
                    }
                    if (imageUrlForRequestPermission != null) {
                        handlePickImage(imageUrlForRequestPermission);
                        imageUrlForRequestPermission = null;
                    }
                }
        );
        requestCameraPermissionForChooser = caller.registerForActivityResult(
                new WithInputActivityResultContract<>(new RequestPermission(), Manifest.permission.CAMERA),
                result -> {
                    if (!result) {
                        callback.onPermissionDenied(Manifest.permission.CAMERA);
                        return;
                    }
                    chooserLauncher.launch(CropImage.getPickImageChooserIntent(getContext(), getTitle(getContext()), false));
                }
        );
        requestCameraPermissionForCamera = caller.registerForActivityResult(
                new WithInputActivityResultContract<>(new RequestPermission(), Manifest.permission.CAMERA),
                result -> {
                    if (!result) {
                        callback.onPermissionDenied(Manifest.permission.CAMERA);
                        return;
                    }
                    chooserLauncher.launch(CropImage.getCameraIntent(getContext(), null));
                }
        );
    }

    /**
     * 设置是否裁剪图片
     *
     * @param cropImage 是否裁剪图片
     */
    public void setCropImage(boolean cropImage) {
        isCropImage = cropImage;
    }

    /**
     * 设置标题
     *
     * @param title
     */
    public void setTitle(CharSequence title) {
        this.title = title;
    }

    /**
     * 启动图片选择器
     */
    public void startChooser() {
        if (CropImage.isExplicitCameraPermissionRequired(getContext())) {
            requestCameraPermissionForChooser.launch(null);
        } else {
            chooserLauncher.launch(CropImage.getPickImageChooserIntent(getContext(), getTitle(getContext()), false));
        }
    }

    /**
     * 启动照相机
     */
    public void startCamera() {
        if (CropImage.isExplicitCameraPermissionRequired(getContext())) {
            requestCameraPermissionForCamera.launch(null);
        } else {
            chooserLauncher.launch(CropImage.getCameraIntent(getContext(), null));
        }
    }

    /**
     * 启动图库选择器
     */
    public void startGallery() {
        if (PickVisualMedia.isPhotoPickerAvailable(getContext())) {
            galleryLauncher.launch(null);
        } else {
            chooserLauncher.launch(getGalleryIntent(getContext(), false));
        }
    }


    protected CharSequence getTitle(Context context) {
        if (TextUtils.isEmpty(title)) {
            return context.getString(R.string.pick_image_intent_chooser_title);
        }
        return title;
    }

    protected Intent getGalleryIntent(Context context, boolean includeDocuments) {
        PackageManager packageManager = context.getPackageManager();

        List<Intent> galleryIntents = CropImage.getGalleryIntents(packageManager, Intent.ACTION_GET_CONTENT, includeDocuments);
        if (galleryIntents.size() == 0) {
            // if no intents found for get-content try pick intent action (Huawei P9).
            galleryIntents = CropImage.getGalleryIntents(packageManager, Intent.ACTION_PICK, includeDocuments);
        }

        Intent target;
        if (galleryIntents.isEmpty()) {
            target = new Intent();
        } else {
            target = galleryIntents.get(galleryIntents.size() - 1);
            galleryIntents.remove(galleryIntents.size() - 1);
        }

        // Create a chooser from the main  intent
        Intent chooserIntent = Intent.createChooser(target, getTitle(context));

        // Add all other intents
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, galleryIntents.toArray(new Parcelable[galleryIntents.size()]));

        return chooserIntent;
    }


    /**
     * 获取文件的真实路径，比如：content://media/external/images/media/74275 的真实路径 file:///storage/sdcard0/Pictures/X.jpg
     * <p>
     * http://stackoverflow.com/questions/20028319/how-to-convert-content-media-external-images-media-y-to-file-storage-sdc
     *
     * @param context
     * @param contentUri
     * @return
     */
    private static String getRealPathFromUri(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }


    /**
     * 裁剪图片结果回调
     */
    private void handleCropResult(Context context, CropImageView.CropResult result) {
        if (result.getError() == null) {
            Uri cropImageUri = result.getUri();
            callback.onCropImage(handleUri(context, cropImageUri));
        } else {
            Log.e(TAG, "handleCropResult error", result.getError());
        }
    }

    /**
     * 选择图片结果回调
     */
    private void handlePickImage(Uri imageUri) {
        callback.onPickImage(handleUri(getContext(), imageUri));
        if (!isCropImage) {
            return;
        }
        // 打开裁剪图片界面
        CropImage.ActivityBuilder builder = CropImage.activity(imageUri);
        // 裁剪配置
        callback.cropConfig(builder);
        // 启动裁剪界面
        builder.start(getContext(), cropLauncher);
    }

    /**
     * 处理返回图片的 uri，content 协议自动转换 file 协议
     * ，避免 {@link FileNotFoundException}
     *
     * @param context
     * @param imageUri
     * @return
     */
    private Uri handleUri(Context context, Uri imageUri) {
        if ("content".equals(imageUri.getScheme())) {
            String realPathFromUri = getRealPathFromUri(context, imageUri);
            if (!TextUtils.isEmpty(realPathFromUri)) {
                return Uri.fromFile(new File(realPathFromUri));
            }
        }
        return imageUri;
    }

    public static abstract class Callback {

        /**
         * 图片选择回调
         *
         * @param imageUri
         */
        public void onPickImage(Uri imageUri) {
        }

        /**
         * 图片裁剪回调
         *
         * @param imageUri
         */
        public void onCropImage(Uri imageUri) {
        }

        /**
         * 图片裁剪配置
         */
        public void cropConfig(CropImage.ActivityBuilder builder) {
            // 默认配置
            builder.setMultiTouchEnabled(false)
                    .setCropShape(CropImageView.CropShape.OVAL)
                    .setRequestedSize(640, 640)
                    .setAspectRatio(5, 5);
        }

        /**
         * 用户拒绝授权回调
         */
        public void onPermissionDenied(String permission) {
        }
    }

}

