package com.linchaolong.android.imagepicker.demo;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

public class FragmentTestActivity extends AppCompatActivity {

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_fragment_test);
    getSupportFragmentManager().beginTransaction()
        .add(R.id.fragment_container, new TestFragment())
        .commit();
  }

}
