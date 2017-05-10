
[中文](README.md) | [English](README_en.md)

# ImagePicker

> 项目地址：https://github.com/linchaolong/ImagePicker

ImagePicker 是 Android 下的图片选择与裁剪开源库，基于 [Android-Image-Cropper](https://github.com/ArthurHub/Android-Image-Cropper) 并做了如下改进：

- 简单易用的api，调用示例如下

```
imagePicker.startChooser(context, new ImagePicker.Callback() {
  // 选择图片回调
  @Override public void onPickImage(Uri imageUri) {}
  // 裁剪图片回调
  @Override public void onCropImage(Uri imageUri) {
    FrescoUtils.circle(draweeView, imageUri);
  }
});
```

- 兼容 Fragment

```
public void startChooser(Activity activity, @NonNull Callback callback)

public void startChooser(Fragment fragment, @NonNull Callback callback)
```

- 部分机型选择图片返回 uri 为 content 协议，内部做了检测并自动转换为真实路径，防止 FileNotFoundException
- 内部做了运行时权限检查，调用者无需考虑权限检查问题
- 兼容 Android N+
- strings.xml 国际化

效果图：

![image](doc/demo.gif)

# 集成说明

`${latest.version}` 是 [![Download](https://api.bintray.com/packages/linchaolong/maven/imagepicker/images/download.svg)](https://bintray.com/linchaolong/maven/imagepicker/_latestVersion)

添加依赖到你的 build.gradle
```
compile 'com.linchaolong.android:imagepicker:${latest.version}'
```

1. 在 Androidmanifest.xml 中配置 `CropImageActivity`（裁剪图片需要）

```
<activity android:name="com.linchaolong.android.imagepicker.cropper.CropImageActivity"
        android:theme="@style/Base.Theme.AppCompat"/>
```

2. 在 Activity 或者 Fragment 中回调 `onActivityResult`，`onRequestPermissionsResult` 方法

```
  @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    imagePicker.onActivityResult(activity/fragment, requestCode, resultCode, data);
  }

  @Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
      @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    imagePicker.onRequestPermissionsResult(activity/fragment, requestCode, permissions, grantResults);
  }
```

# 使用说明

## 1. 调起图片选择器

```
ImagePicker imagePicker = new ImagePicker();
// 设置标题
imagePicker.setTitle("设置头像");
// 设置是否裁剪图片
imagePicker.setCropImage(true);
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
```
> 关于裁剪配置的详细说明可以参考[这里](https://github.com/ArthurHub/Android-Image-Cropper/wiki)

## 2. 从相册中选取图片

```
imagePicker.startGallery(activity/fragment, callback);
```

## 3. 拍照

```
imagePicker.startCamera(activity/fragment, callback);
```

## Callback

回调类接口定义如下，部分接口可选择实现：
```
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
```

> 更多细节请参考项目中的 app 模块的示例代码
