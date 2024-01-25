package com.linchaolong.android.imagepicker;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcelable;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.core.content.FileProvider;
import android.text.TextUtils;
import android.util.Log;
import com.linchaolong.android.imagepicker.cropper.CropImage;
import com.linchaolong.android.imagepicker.cropper.CropImageView;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

/**
 * 图片选择，裁剪封装类
 *
 * Created by linchaolong on 2017/3/9.
 */
public class ImagePicker {

  private static final String TAG = "ImagePicker";

  private Callback callback;
  private boolean isCropImage = true;
  private CharSequence title;

  private Uri pickImageUri;
  private Uri cropImageUri;

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
   *
   * @param activity     {@link Activity}
   * @param callback    {@link Callback}
   */
  public void startChooser(Activity activity, @NonNull Callback callback) {
    this.callback = callback;
    if (CropImage.isExplicitCameraPermissionRequired(activity)) {
      ActivityCompat.requestPermissions(activity, new String[] { Manifest.permission.CAMERA },
          CropImage.CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE);
    } else {
      activity.startActivityForResult(
          CropImage.getPickImageChooserIntent(activity, getTitle(activity), false),
          CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE);
    }
  }

  /**
   * 启动图片选择器
   *
   * @param fragment     {@link Fragment}
   * @param callback    {@link Callback}
   */
  public void startChooser(Fragment fragment, @NonNull Callback callback) {
    this.callback = callback;
    if (CropImage.isExplicitCameraPermissionRequired(fragment.getActivity())) {
      fragment.requestPermissions(new String[] { Manifest.permission.CAMERA }, CropImage.CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE);
    } else {
      fragment.startActivityForResult(
          CropImage.getPickImageChooserIntent(fragment.getActivity(), getTitle(fragment.getActivity()), false),
          CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE);
    }
  }

  /**
   * 启动照相机
   *
   * @param activity
   * @param callback
   */
  public void startCamera(Activity activity, @NonNull Callback callback) {
    this.callback = callback;
    if (CropImage.isExplicitCameraPermissionRequired(activity)) {
      ActivityCompat.requestPermissions(activity, new String[] { Manifest.permission.CAMERA },
          CropImage.CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE);
    } else {
      activity.startActivityForResult(CropImage.getCameraIntent(activity, null), CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE);
    }
  }

  /**
   * 启动照相机
   *
   * @param fragment
   * @param callback
   */
  public void startCamera(Fragment fragment, @NonNull Callback callback) {
    this.callback = callback;
    if (CropImage.isExplicitCameraPermissionRequired(fragment.getActivity())) {
      fragment.requestPermissions(new String[] { Manifest.permission.CAMERA }, CropImage.CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE);
    } else {
      fragment.startActivityForResult(CropImage.getCameraIntent(fragment.getActivity(), null), CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE);
    }
  }

  /**
   * 启动图库选择器
   *
   * @param activity
   * @param callback
   */
  public void startGallery(Activity activity, @NonNull Callback callback) {
    this.callback = callback;
    activity.startActivityForResult(getGalleryIntent(activity, false), CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE);
  }

  /**
   * 启动图库选择器
   *
   * @param fragment
   * @param callback
   */
  public void startGallery(Fragment fragment, @NonNull Callback callback) {
    this.callback = callback;
    fragment.startActivityForResult(getGalleryIntent(fragment.getActivity(), false), CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE);
  }

  protected CharSequence getTitle(Context context){
    if(TextUtils.isEmpty(title)){
      return context.getString(R.string.pick_image_intent_chooser_title);
    }
    return title;
  }

  protected Intent getGalleryIntent(Context context, boolean includeDocuments){
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
   * 图片选择/裁剪结果回调，在 {@link Activity#onActivityResult(int, int, Intent)} 中调用
   *
   * @param activity
   * @param requestCode
   * @param resultCode
   * @param data
   */
  public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
    onActivityResultInner(activity, null, requestCode, resultCode, data);
  }

  /**
   * 图片选择/裁剪结果回调，在 {@link Fragment#onActivityResult(int, int, Intent)} 中调用
   *
   * @param fragment
   * @param requestCode
   * @param resultCode
   * @param data
   */
  public void onActivityResult(Fragment fragment, int requestCode, int resultCode, Intent data) {
    onActivityResultInner(null, fragment, requestCode, resultCode, data);
  }

  private void onActivityResultInner(Activity activity, Fragment fragment, int requestCode, int resultCode, Intent data) {
    if (resultCode == Activity.RESULT_OK) {
      Context context;
      if (activity != null) {
        context = activity;
      }else{
        context = fragment.getActivity();
      }
      if (requestCode == CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE) {
        pickImageUri = CropImage.getPickImageResultUri(context, data);
        // 检查读取文件权限
        if (CropImage.isReadExternalStoragePermissionsRequired(context, pickImageUri)) {
          if (activity != null) {
            ActivityCompat.requestPermissions(activity, new String[] { Manifest.permission.READ_EXTERNAL_STORAGE },
                CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE);
          }else{
            fragment.requestPermissions(new String[] { Manifest.permission.READ_EXTERNAL_STORAGE },
                CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE);
          }
        } else {
          // 选择图片回调
          if (activity != null) {
            handlePickImage(activity, pickImageUri);
          }else{
            handlePickImage(fragment, pickImageUri);
          }
        }
      } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
        // 裁剪图片回调
        handleCropResult(context, CropImage.getActivityResult(data));
      }
    }
  }

  /**
   * 获取文件的真实路径，比如：content://media/external/images/media/74275 的真实路径 file:///storage/sdcard0/Pictures/X.jpg
   *
   * http://stackoverflow.com/questions/20028319/how-to-convert-content-media-external-images-media-y-to-file-storage-sdc
   *
   * @param context
   * @param contentUri
   * @return
   */
  private static String getRealPathFromUri(Context context, Uri contentUri) {
    Cursor cursor = null;
    try {
      String[] proj = { MediaStore.Images.Media.DATA };
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
   * 授权结果回调，在 {@link Activity#onRequestPermissionsResult(int, String[], int[])} 中调用
   *
   * @param activity
   * @param requestCode
   * @param permissions
   * @param grantResults
   */
  public void onRequestPermissionsResult(Activity activity, int requestCode, String permissions[],
      int[] grantResults) {
    onRequestPermissionsResultInner(activity, null, requestCode, permissions, grantResults);
  }

  /**
   * 授权结果回调，在 {@link Fragment#onRequestPermissionsResult(int, String[], int[])} 中调用
   *
   * @param fragment
   * @param requestCode
   * @param permissions
   * @param grantResults
   */
  public void onRequestPermissionsResult(Fragment fragment, int requestCode, String permissions[],
      int[] grantResults) {
    onRequestPermissionsResultInner(null, fragment, requestCode, permissions, grantResults);
  }

  private void onRequestPermissionsResultInner(Activity activity, Fragment fragment, int requestCode, String permissions[],
      int[] grantResults) {
    if (requestCode == CropImage.CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE) {
      if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        if (activity != null) {
          CropImage.startPickImageActivity(activity);
        }else{
          CropImage.startPickImageActivity(fragment);
        }
      } else {
        // 用户拒绝授权
        if(callback != null){
          callback.onPermissionDenied(requestCode, permissions, grantResults);
        }
      }
    }
    if (requestCode == CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE) {
      if (cropImageUri != null
          && grantResults.length > 0
          && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        if (activity != null) {
          handlePickImage(activity, cropImageUri);
        }else{
          handlePickImage(fragment, cropImageUri);
        }
      } else {
        // 用户拒绝授权
        if(callback != null){
          callback.onPermissionDenied(requestCode, permissions, grantResults);
        }
      }
    }
  }

  /**
   * 裁剪图片结果回调
   */
  private void handleCropResult(Context context, CropImageView.CropResult result) {
    if (result.getError() == null) {
      cropImageUri = result.getUri();
      if(callback != null){
        callback.onCropImage(handleUri(context, cropImageUri));
      }
    } else {
      Log.e(TAG, "handleCropResult error", result.getError());
    }
  }

  private void handlePickImage(Activity activity, Uri imageUri) {
    handlePickImageInner(activity, null, imageUri);
  }

  private void handlePickImage(Fragment fragment, Uri imageUri) {
    handlePickImageInner(null, fragment, imageUri);
  }

  /**
   * 选择图片结果回调
   */
  private void handlePickImageInner(Activity activity, Fragment fragment, Uri imageUri) {
    if(callback != null){
      Context context;
      if (activity != null) {
        context = activity;
      }else{
        context = fragment.getContext();
      }
      callback.onPickImage(handleUri(context, imageUri));
    }
    if(!isCropImage){
      return;
    }
    // 打开裁剪图片界面
    CropImage.ActivityBuilder builder = CropImage.activity(imageUri);
    // 裁剪配置
    callback.cropConfig(builder);
    // 启动裁剪界面
    if (activity != null) {
      builder.start(activity);
    }else{
      builder.start(fragment.getActivity(), fragment);
    }
  }

  /**
   * 处理返回图片的 uri，content 协议自动转换 file 协议
   * ，避免 {@link FileNotFoundException}
   *
   * @param context
   * @param imageUri
   * @return
   */
  private Uri handleUri(Context context, Uri imageUri){
    if("content".equals(imageUri.getScheme())){
      String realPathFromUri = getRealPathFromUri(context, imageUri);
      if (!TextUtils.isEmpty(realPathFromUri)) {
        return Uri.fromFile(new File(realPathFromUri));
      }
    }
    return imageUri;
  }

  public static abstract class Callback{

    /**
     * 图片选择回调
     * @param imageUri
     */
    public abstract void onPickImage(Uri imageUri);

    /**
     * 图片裁剪回调
     *
     * @param imageUri
     */
    public void onCropImage(Uri imageUri){}

    /**
     * 图片裁剪配置
     */
    public void cropConfig(CropImage.ActivityBuilder builder){
      // 默认配置
      builder.setMultiTouchEnabled(false)
          .setCropShape(CropImageView.CropShape.OVAL)
          .setRequestedSize(640, 640)
          .setAspectRatio(5, 5);
    }

    /**
     * 用户拒绝授权回调
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    public void onPermissionDenied(int requestCode, String permissions[], int[] grantResults){}
  }

}

