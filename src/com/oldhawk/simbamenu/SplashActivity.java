package com.oldhawk.simbamenu;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class SplashActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_splash);

	}
	
	public void startMenu(View v){
		Intent intent=new Intent();
		intent.setClass(SplashActivity.this, MainActivity.class);
		startActivity(intent);
		this.finish();
	}
}
