# ImagePicker

android image picker and cropper library, based on  [Android-Image-Cropper](https://github.com/ArthurHub/Android-Image-Cropper)  and made the following improvement:

- easy api

```
imagePicker.startChooser(context, new ImagePicker.Callback() {
  // select image callback
  @Override public void onPickImage(Uri imageUri) {}
  // clip callback
  @Override public void onCropImage(Uri imageUri) {
    FrescoUtils.circle(draweeView, imageUri);
  }
});
```

- Compatible with Fragment

```
public void startChooser(Activity activity, @NonNull Callback callback)

public void startChooser(Fragment fragment, @NonNull Callback callback)
```

- fix some devices select the image to return FileNotFoundException
- Runtime permissions check
- Support android N+
- Localization

![image](doc/demo.gif)

# Using ImagePicker in your application

`${latest.version}` is [![Download](https://api.bintray.com/packages/linchaolong/maven/imagepicker/images/download.svg)](https://bintray.com/linchaolong/maven/imagepicker/_latestVersion)

add the following line to the dependencies section of your build.gradle file:

```
compile 'com.linchaolong.android:imagepicker:${latest.version}'
```

1. add `CropImageActivity` in your Androidmanifest.xml:

```
<activity android:name="com.linchaolong.android.imagepicker.cropper.CropImageActivity"
        android:theme="@style/Base.Theme.AppCompat"/>
```

2. call  `onActivityResult` and `onRequestPermissionsResult` method in your Activity/Fragment:

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

# Usage

## 1. Start Image Chooser

```
ImagePicker imagePicker = new ImagePicker();
imagePicker.startChooser(this, new ImagePicker.Callback() {
  @Override public void onPickImage(Uri imageUri) {

  }

  @Override public void onCropImage(Uri imageUri) {
    draweeView.setImageURI(imageUri);
    draweeView.getHierarchy().setRoundingParams(RoundingParams.asCircle());
  }

  @Override public void cropConfig(CropImage.ActivityBuilder builder) {
    builder
        .setMultiTouchEnabled(false)
        .setGuidelines(CropImageView.Guidelines.OFF)
        .setCropShape(CropImageView.CropShape.RECTANGLE)
        .setRequestedSize(960, 540)
        .setAspectRatio(16, 9);
  }

  @Override public void onPermissionDenied(int requestCode, String[] permissions,
      int[] grantResults) {
  }
});
```
> [click here read more about config](https://github.com/ArthurHub/Android-Image-Cropper/wiki)

## 2. Get image from gallery

```
imagePicker.startGallery(activity/fragment, callback);
```

## 3. Get image from camera

```
imagePicker.startCamera(activity/fragment, callback);
```

## Callback interface

```
  public static abstract class Callback{

    /**
     * image pick callback

     * @param imageUri
     */
    public abstract void onPickImage(Uri imageUri);

    /**
     * clip image callback
     *
     * @param imageUri
     */
    public void onCropImage(Uri imageUri){}

    /**
     * clip config
     */
    public void cropConfig(CropImage.ActivityBuilder builder){
      // default config
      builder.setMultiTouchEnabled(false)
          .setCropShape(CropImageView.CropShape.OVAL)
          .setRequestedSize(640, 640)
          .setAspectRatio(5, 5);
    }

    /**
     * permission denied callback
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    public void onPermissionDenied(int requestCode, String permissions[], int[] grantResults){}
  }
```

> For more details, please refer to the app module in the project
