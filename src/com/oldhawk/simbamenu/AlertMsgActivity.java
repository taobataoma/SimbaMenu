package com.oldhawk.simbamenu;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class AlertMsgActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_alertmsg);

		Intent alertIntent = getIntent();
		String hitmsgString = alertIntent.getStringExtra("hintmsg");
		System.out.println("hitmsgString=" + hitmsgString);
		TextView alertTextView = (TextView) findViewById(R.id.hintmsg);
		alertTextView.setText(hitmsgString);

		Button okButton = (Button) findViewById(R.id.buttonOk);
		okButton.setOnClickListener(new buttonOKClickListner());
	}

	class buttonOKClickListner implements View.OnClickListener {

		@Override
		public void onClick(View v) {
			finish();
		}

	}
}
