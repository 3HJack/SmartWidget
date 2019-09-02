package com.hhh.onepiece;

import android.os.Bundle;

import com.hhh.smartwidget.immersive.Immersive;

import androidx.appcompat.app.AppCompatActivity;

public class ImmersiveActivity extends AppCompatActivity implements Immersive {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_immersive);
  }
}
