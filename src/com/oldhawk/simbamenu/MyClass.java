package com.oldhawk.simbamenu;

import java.io.IOException;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.view.Gravity;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MyClass {
	public Toast pToast;

	/*
	 * ����һ���Զ���view�ķ�ģ̬��Ϣ�� hintmsg: ��ʾ����Ϣ���� con: �����context
	 */
	public void AlertMsg(String hintmsg, Context con) {
		Intent alertIntent = new Intent();
		alertIntent.putExtra("hintmsg", hintmsg);
		System.out.println("hitmsg=" + hintmsg);
		alertIntent.setClass(con, AlertMsgActivity.class);
		con.startActivity(alertIntent);
	}

	/*
	 * ����һ��toast hingmsg: ��ʾ������ loca: ������λ�ã�Gravity.CENTER sTime:
	 * ������ʾ��ʱ�䳤��,Toast.LENGTH_SHORT,Toast.LENGTH_LONG con: �����context
	 */
	public void AlertToast(String hintmsg, int loca, int sTime, Context con) {
		if (pToast != null)
			pToast.cancel();
		pToast = Toast.makeText(con, hintmsg, sTime);
		pToast.setGravity(loca, 0, 200);
		// LinearLayout toastView = (LinearLayout) toast.getView();
		// toastView.setOrientation(LinearLayout.HORIZONTAL);
		// ImageView imageCodeProject = new ImageView(con);
		// imageCodeProject.setImageResource(R.drawable.app_logo);
		// toastView.addView(imageCodeProject, 0);
		// toastView.setGravity(Gravity.CENTER);
		pToast.show();
	}
	public void AlertToast(String hintmsg, Context con){
		AlertToast(hintmsg,Gravity.CENTER,Toast.LENGTH_LONG,con);
	}
	
	/*
	 * �ر��ѵ�����toast
	 */
	public void CancelToast() {
		if (pToast != null)
			pToast.cancel();
	}

	/*
	 * ����һ��ģ̬�Ի��� titleMsg: ���� hingmsg: ��ʾ����Ϣ���� loca: ������λ�ã�Gravity.CENTER �� con:
	 * �����context
	 */
	public void AlertDialog(String titleMsg, String hintmsg, int loca, Context con) {
		// ����title��ʾ����
		TextView cView = new TextView(con);
		cView.setText(titleMsg);
		cView.setTextSize(16);
		cView.setTextColor(Color.rgb(0, 0, 0));
		cView.setPadding(20, 20, 20, 20);
		LinearLayout lView = new LinearLayout(con);
		lView.setOrientation(LinearLayout.HORIZONTAL);
		ImageView imageCodeProject = new ImageView(con);
		imageCodeProject.setImageResource(R.drawable.ic_launcher);
		imageCodeProject.setMaxHeight(20);
		imageCodeProject.setPadding(0, 0, 0, 0);
		lView.addView(imageCodeProject, 0);
		lView.addView(cView, 1);
		lView.setGravity(Gravity.LEFT);

		// ����������ʾ����view
		TextView tView = new TextView(con);
		tView.setLayoutParams(new LayoutParams(
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
		tView.setText(hintmsg);
		tView.setLineSpacing(10, 1);
		tView.setTextSize(16);
		tView.setTextColor(Color.rgb(0, 0, 0));
		tView.setPadding(20, 20, 20, 20);

		// �����Ի���
		AlertDialog.Builder altDialog = new AlertDialog.Builder(con);
		altDialog.setTitle(titleMsg);
		// setIcon(R.drawable.app_logo).
		// setMessage(hintmsg).
		//altDialog.setCustomTitle(lView); //change by setTitle()
		altDialog.setView(tView);
		
		//altDialog.setInverseBackgroundForced(true);
		altDialog.setPositiveButton(R.string.string_button_OK,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
							}
						});
		altDialog.create();
		AlertDialog dlg=altDialog.show();
		
	    Button b=(Button)dlg.getButton(DialogInterface.BUTTON_POSITIVE);
	    b.setTextSize(16);
		// ���öԻ����ĳЩ����
		WindowManager.LayoutParams params = dlg.getWindow()
				.getAttributes();
		// params.width = 200;
		// params.height = 200 ;
		params.alpha = 1;// f;
		dlg.getWindow().setAttributes(params);
		dlg.getWindow().setGravity(loca);
		// dlg.getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
		// WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
	}
	public void AlertDialog(String titleMsg, String hintmsg, Context con) {
		AlertDialog(titleMsg, hintmsg, Gravity.CENTER , con);
	}
	
   	public String getHttpServerUrl(){
   		try{
	   		IniFile f=new IniFile();
	   		f.IniReaderHasSection();
	   		String serv=f.getValue("HTTPSERV", "serv","http://192.168.0.1");
	   		String port=f.getValue("HTTPSERV", "port","80");
	   		
	   		return serv+":"+port;
   		}catch(IOException e){
   			e.printStackTrace();
   			return "http://192.168.0.1:80";
   		}
   	}
}
