package com.linchaolong.android.imagepicker.demo;

import android.app.Application;
import com.facebook.drawee.backends.pipeline.Fresco;

/**
 * Created by linchaolong on 2017/3/21.
 */
public class MyApplication extends Application {
  @Override public void onCreate() {
    super.onCreate();
    Fresco.initialize(this);
  }
}
