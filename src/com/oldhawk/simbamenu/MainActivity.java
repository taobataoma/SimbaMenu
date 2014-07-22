package com.oldhawk.simbamenu;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import org.w3c.dom.Node;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("HandlerLeak")
public class MainActivity extends Activity {
	private MyClass myCls=new MyClass();
	private SelectMenuXML smx=null;
	private ActivityXML avx=null;
	private long exitTime = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
	    EditText et=(EditText)findViewById(R.id.editTextDownlog);
	    et.setVisibility(View.GONE);
	    
	    ScrollView sv=(ScrollView)findViewById(R.id.scrollview_main);
	    sv.setOnTouchListener(new onScrollViewTouched());
		
		smx=new SelectMenuXML(this);
		avx=new ActivityXML(this);
		
		if(checkConfigFile()==false)
    		return;
		
		initMenuTypeList();
		showFirstPage(null);
    }
	
	class onScrollViewTouched implements View.OnTouchListener{
        private int lastY = 0;
    	private int touchEventId = -9983761;
    	Handler handler = new Handler() {
	    	@Override
	    	public void handleMessage(Message msg) {
	    	    super.handleMessage(msg);
	    	    View scroller = (View)msg.obj;
	    	    if(msg.what==touchEventId) {
	    	        if(lastY ==scroller.getScrollY()) {
	    	        	handleStop(scroller);
	    	        }else {
	    	        	handler.sendMessageDelayed(handler.obtainMessage(touchEventId,scroller), 1);
	    	        	lastY = scroller.getScrollY();
	    	        }
	    	    }
	    	}
    	};
    	@Override
    	public boolean onTouch(View v, MotionEvent event) {
    	   	int eventAction = event.getAction();
    	   	switch (eventAction) {
    	   		case MotionEvent.ACTION_UP:
   	   				handler.sendMessageDelayed(handler.obtainMessage(touchEventId,v), 5);
    	   			break;
    	   	}
    	   	return false;
    	}
   	    //这里写真正的事件
    	private void handleStop(Object view) {
    		updateImages();
    	}
	}

	public void updateImages(){
		ScrollView sv=(ScrollView)findViewById(R.id.scrollview_main);
		//System.out.println("ACTION:"+sv.getScrollX()+"/"+sv.getScrollY());
		Integer top=sv.getScrollY();
		if(top<0){
			top=0;
		}
		Integer bottom=top+sv.getHeight();
		
	    LinearLayout table=(LinearLayout)findViewById(R.id.table_detail);
	    for(int i=0;i<table.getChildCount();i++){
	    	View v=(View)table.getChildAt(i);
	    	String cls=v.getClass().toString();
	    	if(cls.equalsIgnoreCase("class android.widget.GridView")){
	    		if((v.getY()<bottom && v.getY()>top) || (v.getY()+v.getHeight()<bottom && v.getY()+v.getHeight()>top)){
    				GridView gv=(GridView)v;
	    			if(gv.getTag().equals("0")){
	    				gv.setTag("1");
	    				menuShowListAdapter adpAdapter=(menuShowListAdapter)gv.getAdapter();
	    				adpAdapter.notifyDataSetChanged();
	    			}
	    		}
	    	}
	    }
		
	}
	
	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN){   
            if((System.currentTimeMillis()-exitTime) > 2000){  
            	myCls.AlertToast(getString(R.string.string_press_quit), Gravity.BOTTOM, Toast.LENGTH_LONG, MainActivity.this);
                exitTime = System.currentTimeMillis();   
            } else {
            	this.finish();
            	System.exit(0);
            }
            return true;   
        }
        return super.onKeyDown(keyCode, event);
    }
   	
   	private boolean checkConfigFile(){
   		SelectMenuXML smx=new SelectMenuXML(this);
   		if(!smx.GetConfigFileIsExist()){
    		String message=getString(R.string.string_update_tips_menu);
    		myCls.AlertToast(message, Gravity.BOTTOM, Toast.LENGTH_LONG, MainActivity.this);
  			return false;
   		}
   		return true;
   	}
    final class DownloadMenuImagesRunnable implements Runnable{
    	public ArrayList<String> imageArrayList=new ArrayList<String>();
    	private Message message;
    	public ImageView iv;
    	@Override  
    	public void run() {
    		Looper.prepare();
			DownloadImages di=new DownloadImages();
			for(int i=0;i<imageArrayList.size();i++){
				String u=imageArrayList.get(i);
				String d="/simba/menuimages/";
				if(u.startsWith("a")){
					d="/simba/activityimages/";
				}
				if(di.Download(myCls.getHttpServerUrl()+d+u,u)){
					message = Message.obtain();
					message.obj="download over: "+u;
					msgdownhandler.sendMessage(message);
					if(iv!=null){
						iv.setTag("1");
					}
				}else{
					message = Message.obtain();
					message.obj="download error: "+u;
					msgdownhandler.sendMessage(message);
					if(iv!=null){
						iv.setTag("0");
					}
				}
			}
			if(imageArrayList.size()>1){
				message = Message.obtain();
				message.obj="download finished!";
				msgdownhandler.sendMessage(message);
			}
    	}
    };
    	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.action_about) {
			MyClass cls = new MyClass();
			String hintmsg = getString(R.string.app_name);
			hintmsg = hintmsg + "\n" + getString(R.string.string_copyright);
			hintmsg = hintmsg + "\n" + getString(R.string.string_version);
			String titleMsg = getString(R.string.action_about);
			cls.AlertDialog(titleMsg, hintmsg, Gravity.BOTTOM,
					MainActivity.this);
		}else if(item.getItemId() == R.id.action_updatemenus){
			doUpdateMenus();
		}else if(item.getItemId()==R.id.action_updateconfig){
    		doConfigServer();
    	}else if(item.getItemId()==R.id.action_downloadimages){
    		doDownloadAllMenuImages();
    	}else if(item.getItemId()==R.id.action_updateactivity){
    		doUpdateActivity();
    	}else if(item.getItemId()==R.id.action_downloadactivityimages){
    		doDownloadActivityImages();
    	}
		return super.onOptionsItemSelected(item);
	}

	private void doDownloadAllMenuImages(){
		final AlertDialog.Builder altDB = new AlertDialog.Builder(this);
		altDB.setTitle(getString(R.string.string_confimdialog_title));
		altDB.setInverseBackgroundForced(true);
		altDB.setMessage(getString(R.string.string_action_downallimages));
	    altDB.setPositiveButton(R.string.string_button_Cancel,null);
		altDB.setNegativeButton(R.string.string_button_OK,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog,
							int which) {
						beginDownloadAllMenuImages();
					}
				}).create();
	    AlertDialog dlg=altDB.show();
	    Button b=(Button)dlg.getButton(DialogInterface.BUTTON_POSITIVE);
	    b.setTextSize(16);
	    b.setBackgroundResource(R.drawable.publicalertbuttonbackground);
	    b=(Button)dlg.getButton(DialogInterface.BUTTON_NEGATIVE);
	    b.setTextSize(16);
	    b.setBackgroundResource(R.drawable.publicalertbuttonbackground);
	}
	
	private void doDownloadActivityImages(){
		final AlertDialog.Builder altDB = new AlertDialog.Builder(this);
		altDB.setTitle(getString(R.string.string_confimdialog_title));
		altDB.setInverseBackgroundForced(true);
		altDB.setMessage(getString(R.string.string_action_downactivityimages));
	    altDB.setPositiveButton(R.string.string_button_Cancel,null);
		altDB.setNegativeButton(R.string.string_button_OK,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog,
							int which) {
						beginDownloadActivityImages();
					}
				}).create();
	    AlertDialog dlg=altDB.show();
	    Button b=(Button)dlg.getButton(DialogInterface.BUTTON_POSITIVE);
	    b.setTextSize(16);
	    b.setBackgroundResource(R.drawable.publicalertbuttonbackground);
	    b=(Button)dlg.getButton(DialogInterface.BUTTON_NEGATIVE);
	    b.setTextSize(16);
	    b.setBackgroundResource(R.drawable.publicalertbuttonbackground);
	}
	
	public void beginDownloadAllMenuImages(){
	    EditText et=(EditText)findViewById(R.id.editTextDownlog);
	    et.setVisibility(View.VISIBLE);
	    et.bringToFront();
		DownloadMenuImagesRunnable dmir=new DownloadMenuImagesRunnable();
		
		ArrayList<Node> al=smx.GetMenuTypeArray(this);
		if(al!=null){
			for(int i=0;i<al.size();i++){
				ArrayList<Node> ts=smx.GetMenuSubTypeArray(al.get(i), MainActivity.this);
				for( int j=0; j<ts.size(); j++){
					ArrayList<Node> tn=smx.GetMenuSubTypeItemsArray(ts.get(j), MainActivity.this);
					for( int k=0; k<tn.size(); k++){
					    Node n=tn.get(k);
			            dmir.imageArrayList.add(n.getAttributes().getNamedItem("img").getNodeValue()+".jpg");  
					}  
				}  
		    }
			Thread th=new Thread(dmir);
			th.start();	
		}else{
			Message message;
			message = Message.obtain();
			message.obj="No menu image can download!";
			msghandler.sendMessage(message);
		}
	}
	
	public void beginDownloadActivityImages(){
	    EditText et=(EditText)findViewById(R.id.editTextDownlog);
	    et.setVisibility(View.VISIBLE);
	    et.bringToFront();
	    DownloadMenuImagesRunnable dmir=new DownloadMenuImagesRunnable();
		
		ArrayList<Node> al=avx.GetActivityList();
		if(al!=null){
			for( int i=0; i<al.size(); i++){
			    Node n=al.get(i);
	            dmir.imageArrayList.add(n.getAttributes().getNamedItem("img").getNodeValue()+".jpg");  
			}  
			Thread th=new Thread(dmir);
			th.start();		
		}else{
			Message message;
			message = Message.obtain();
			message.obj="No activity image can download!";
			msghandler.sendMessage(message);
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	private void initMenuTypeList(){
		//添加菜品大类
		ArrayList<Node> al=smx.GetMenuTypeArray(this);
		final LinearLayout typelayout=(LinearLayout)findViewById(R.id.select_layout_type);
		typelayout.removeAllViewsInLayout();
		
		for(int i=0;i<al.size();i++){
		    Node n=al.get(i);
			//添加子类按钮
			Button b=new Button(MainActivity.this);
			//if(i==0){
			//  	b.setBackgroundResource(R.drawable.menusubtypecheckedbackground);
			//   	b.setTag(n);
			//  	initMenuListItem(n);
			//}else{
			    b.setBackgroundResource(R.drawable.layoutrightborderbackground);
			    b.setTag(n);
			//}

			Drawable bleft=getResources().getDrawable(R.drawable.dot);
			bleft.setBounds(0, 0, bleft.getMinimumWidth(), bleft.getMinimumHeight());
			b.setCompoundDrawables(bleft, null, null, null);

			b.setText(n.getAttributes().getNamedItem("name").getNodeValue());
			LinearLayout.LayoutParams paButton = new LinearLayout.LayoutParams(210,50);//LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			b.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
			b.setPadding(15, 5, 5, 5);
			b.setTextSize(22);
			b.setTextColor(Color.rgb(240, 240, 240));
			b.setLayoutParams(paButton);
			b.setHint("0");
			b.setOnClickListener(new View.OnClickListener() {						
				@Override
				public void onClick(View v) {
					Button b=(Button)v;
					if(b.getHint().equals("1"))
						return;
					System.out.println(b.getText().toString());
					b.setBackgroundResource(R.drawable.menusubtypecheckedbackground);

					Drawable bleft=getResources().getDrawable(R.drawable.dot);
					bleft.setBounds(0, 0, bleft.getMinimumWidth(), bleft.getMinimumHeight());
					b.setCompoundDrawables(bleft, null, null, null);

				    LinearLayout.LayoutParams paButton = new LinearLayout.LayoutParams(210,50);//LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
					b.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
					b.setPadding(15, 5, 5, 5);
					b.setTextSize(22);
					b.setTextColor(Color.rgb(240, 240, 240));
					b.setLayoutParams(paButton);
					b.setHint("1");    
					for(int i=0;i<typelayout.getChildCount();i++){
						Button b1=(Button)typelayout.getChildAt(i);
						if(b1!=b){
							b1.setBackgroundResource(R.drawable.layoutrightborderbackground);

							bleft=getResources().getDrawable(R.drawable.dot);
							bleft.setBounds(0, 0, bleft.getMinimumWidth(), bleft.getMinimumHeight());
							b1.setCompoundDrawables(bleft, null, null, null);

							LinearLayout.LayoutParams plButton = new LinearLayout.LayoutParams(210,50);//LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
							b1.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
							b1.setPadding(15, 5, 5, 5);
							b1.setTextSize(22);
							b1.setTextColor(Color.rgb(240, 240, 240));
							b1.setLayoutParams(plButton);
							b1.setHint("0");
						}
					}
				    EditText et=(EditText)MainActivity.this.findViewById(R.id.editTextDownlog);
				    et.setVisibility(View.GONE);
				    LinearLayout table=(LinearLayout)MainActivity.this.findViewById(R.id.table_detail);
					table.removeAllViewsInLayout();
					table.invalidate();
					
					TextView tv=new TextView(MainActivity.this);
					tv.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
					tv.setGravity(Gravity.CENTER);
					tv.setText(getString(R.string.string_loading_wait));
					tv.setTextColor(Color.rgb(240, 240, 240));
					tv.setTextSize(22);
					tv.setPadding(0, 200, 0, 0);
					table.addView(tv);
					
					
					Node n=(Node)b.getTag();	
		    		Message message;
		    		message = Message.obtain();
		    		message.arg1=1;
		    		message.obj=n;
		    		handler.sendMessage(message);
				}
			});
			typelayout.addView(b);
		}
	}
	
	private void initMenuListItem(Node node){
		EditText et=(EditText)findViewById(R.id.editTextDownlog);
	    et.setVisibility(View.GONE);
	    
	    LinearLayout table=(LinearLayout)findViewById(R.id.table_detail);
		table.removeAllViewsInLayout();
		table.invalidate();
				
		ArrayList<Node> al=smx.GetMenuSubTypeArray(node, MainActivity.this);
		for(int i=0;i<al.size();i++){
			Node sn=al.get(i);
			System.out.println(sn.getAttributes().getNamedItem("name").getNodeValue());
			//row1
			TextView tv=new TextView(this);
			tv.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT));
			tv.setBackgroundResource(R.drawable.layoutbackground2);
			tv.setGravity(Gravity.CENTER_VERTICAL);
			
			Drawable dleft=getResources().getDrawable(R.drawable.close);
			dleft.setBounds(0, 0, dleft.getMinimumWidth(), dleft.getMinimumHeight());
			tv.setCompoundDrawables(dleft, null, null, null);
			
			tv.setText(sn.getAttributes().getNamedItem("name").getNodeValue());
			tv.setTextColor(Color.rgb(240, 240, 240));
			tv.setTextSize(22);
			tv.setPadding(0, 0, 0, 3);

			table.addView(tv);
		
			//row2
			GridView gv=new GridView(this);
			gv.setNumColumns(4);
			
			menuShowListAdapter adp=new menuShowListAdapter(this);
			gv.setAdapter(adp);
			gv.setOnItemClickListener(new onGridViewItemClickListener());
			ArrayList<Node> als=smx.GetMenuSubTypeItemsArray(sn, this);
			System.out.println("als.size()="+als.size()+"");
			
			/*
			for(int j=0;j<als.size();j++){
				adp.arrNodes.add(als.get(j));
			}
			gv.setTag("0");
			adp.notifyDataSetChanged();
			*/
			
			LoadMenuTypeItemImageRunnable li=new LoadMenuTypeItemImageRunnable();
			li.adpAdapter=adp;
			li.snList=als;
			gv.setTag("0");
			new Thread(li).start();
			//gv.setTag(li);
			
			System.out.println(als.size()/4f+"");
			int rows= (int) Math.ceil(als.size()/4f);
			gv.setLayoutParams(new GridView.LayoutParams(LayoutParams.MATCH_PARENT,rows*195+40));//LayoutParams.WRAP_CONTENT));
			gv.setPadding(0, 0, 0, 40);
			
			table.addView(gv);
		}
		ScrollView sv=(ScrollView)findViewById(R.id.scrollview_main);
		sv.scrollTo(0, 0);
		
		Message message = Message.obtain();
		message.arg1=4;
		handler.sendMessage(message);
	}

	class onGridViewItemClickListener implements OnItemClickListener{
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			// TODO Auto-generated method stub
			GridView gv=(GridView)arg0;
			menuShowListAdapter adp=(menuShowListAdapter)gv.getAdapter();
			Node n=adp.arrNodes.get(arg2);
			//myCls.AlertToast(n.getAttributes().getNamedItem("name").getNodeValue(), MainActivity.this);
			
			ImageView iv=(ImageView)arg1.findViewById(R.id.imageView_itempic);
			if(iv.getTag().equals("0")){
				String filename=n.getAttributes().getNamedItem("img").getNodeValue()+".jpg";
				DownloadMenuImagesRunnable dmir=new DownloadMenuImagesRunnable();
	            dmir.imageArrayList.add(filename);
	            dmir.iv=iv;
				Thread th=new Thread(dmir);
				th.start();
				
				iv.setTag("1");
			}
			doShowItemInfo(n,arg1);	
		}		
	}

    private void doShowItemInfo(Node n, View v){
    	
    	
		Intent intent=new Intent();
		intent.setClass(MainActivity.this, MenuItemInfoDialog.class);
		intent.putExtra("itemidx", n.getAttributes().getNamedItem("idx").getNodeValue());
		startActivity(intent);


		/*
    	 // 加载popupWindow的布局文件
    	 final View vPopupWindow = getLayoutInflater().inflate(R.layout.dialog_menuiteminfo, null,false);  
    	 // 设置popupWindow的背景颜色
    	 vPopupWindow.setBackgroundColor(Color.rgb(23, 121, 87)); 
    	 vPopupWindow.setBackgroundResource(R.drawable.popupwindowbackground2);

    	//创建PopupWindow实例
    	// 声明一个弹出框 ，最后一个参数和setFocusable对应
    	final PopupWindow pw = new PopupWindow(vPopupWindow, 900, LayoutParams.WRAP_CONTENT,true);
    	// 为弹出框设定自定义的布局
    	pw.setContentView(vPopupWindow);
    	//设置整个popupwindow的样式。   
    	//pw.setBackgroundDrawable(null);
    	@SuppressWarnings("deprecation")
		BitmapDrawable bm = new BitmapDrawable();
        pw.setBackgroundDrawable(bm);
    	// 设置动画效果
    	pw.setAnimationStyle(R.style.AnimationPreview); 
    	//默认为false,如果不设置为true，PopupWindow里面是获取不到焦点的，那么如果PopupWindow里面有输入框等的话就无法输入。
    	pw.setFocusable(true);
    	//设置PopupWindow可触摸
    	pw.setTouchable(true); 
    	//设置非PopupWindow区域可触摸 
    	pw.setOutsideTouchable(true);
    	
    	
    	
    	pw.update();
    	// 以屏幕中心为参照，不偏移 
    	pw.showAtLocation(getWindow().getDecorView(), Gravity.CENTER, 0, 0);*/
	}
    
	private class menuShowListAdapter extends BaseAdapter {  
        
        private Context context;  
        private LayoutInflater inflater;  
        public ArrayList<Node> arrNodes;  
        public menuShowListAdapter(Context con) {  
            super();  
            this.context = con;  
            inflater = LayoutInflater.from(context);  
            arrNodes = new ArrayList<Node>();  
        }  
        @Override  
        public int getCount() {  
            // TODO Auto-generated method stub  
            return arrNodes.size();  
        }  
        @Override  
        public Object getItem(int arg0) {  
            // TODO Auto-generated method stub  
            return arg0;  
        }  
        @Override  
        public long getItemId(int arg0) {  
            // TODO Auto-generated method stub  
            return arg0;  
        }
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
            // TODO Auto-generated method stub  
            if(convertView == null){  
            	convertView = inflater.inflate(R.layout.view_maintypeitem, null);  
            }
            Node n=arrNodes.get(position);
            
            GridView gv=(GridView)parent;
            if(gv.getTag().equals("1")){
	            ImageView iv=(ImageView)convertView.findViewById(R.id.imageView_itempic);
	            
	            final String filename=n.getAttributes().getNamedItem("img").getNodeValue()+"_s.jpg";
				String filepath="";
				filepath=Environment.getExternalStorageDirectory().getAbsolutePath()+"/simba/menuimages/";
				filepath=filepath+filename;
				//Log.d("getView D",filepath);			
				
	            DownloadImages di=new DownloadImages();
				try{
					if(di.imageFileIsExist(filename)){
			    		Log.d("D",filepath+",exist!");
	
			    		Bitmap bm = null; 
			    		FileInputStream f = new FileInputStream(filepath); 
			    		BitmapFactory.Options opt = new BitmapFactory.Options(); 
			    		//opt.inSampleSize = 2;//图片的长宽都是原来的1/8 
			            opt.inPreferredConfig = Bitmap.Config.RGB_565;  
			            opt.inPurgeable = true; 
			            opt.inInputShareable = true;
			           
			            BufferedInputStream bis = new BufferedInputStream(f, 1*1024*1024); 
			            bm = BitmapFactory.decodeStream(bis, null, opt);
			    		
			    		iv.setImageBitmap(bm);
			    		bm=null;
			    		iv.setTag("1");
					}else{
						iv.setTag("0");
					}
				}catch(Exception e){
					e.printStackTrace();
				}			
            }
            
			TextView tv1=(TextView)convertView.findViewById(R.id.textView_itemtitle);
            TextView tv2=(TextView)convertView.findViewById(R.id.textView_itemprice);

            String tit=n.getAttributes().getNamedItem("name").getNodeValue();
			tv1.setText(tit);
			
			ImageView iv=(ImageView)convertView.findViewById(R.id.imageView_sale);
			int price=Integer.parseInt(n.getAttributes().getNamedItem("saleprice").getNodeValue());
			if (price==0){
				price=Integer.parseInt(n.getAttributes().getNamedItem("price").getNodeValue());
				iv.setVisibility(View.GONE);
			}else{
				iv.setVisibility(View.VISIBLE);
			}
			tv2.setText("￥"+price+"/"+n.getAttributes().getNamedItem("unit").getNodeValue());
			
            return convertView;  
		}  
    }
	
    private void doUpdateMenus(){
		final AlertDialog.Builder altDB = new AlertDialog.Builder(this);
		altDB.setTitle(getString(R.string.string_confimdialog_title));
		altDB.setInverseBackgroundForced(true);
		altDB.setMessage(getString(R.string.string_action_updatemenus));
	    altDB.setPositiveButton(R.string.string_button_Cancel,null);
		altDB.setNegativeButton(R.string.string_button_OK,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog,
							int which) {
						System.out.println("begin update menus");
						new Thread(UpdateMenusConfigRunnable).start();
					}
				}).create();
	    AlertDialog dlg=altDB.show();
	    Button b=(Button)dlg.getButton(DialogInterface.BUTTON_POSITIVE);
	    b.setTextSize(16);
	    b.setBackgroundResource(R.drawable.publicalertbuttonbackground);
	    b=(Button)dlg.getButton(DialogInterface.BUTTON_NEGATIVE);
	    b.setTextSize(16);
	    b.setBackgroundResource(R.drawable.publicalertbuttonbackground);
    }
	
    private void doUpdateActivity(){
		final AlertDialog.Builder altDB = new AlertDialog.Builder(this);
		altDB.setTitle(getString(R.string.string_confimdialog_title));
		altDB.setInverseBackgroundForced(true);
		altDB.setMessage(getString(R.string.string_action_updateactivity));
	    altDB.setPositiveButton(R.string.string_button_Cancel,null);
		altDB.setNegativeButton(R.string.string_button_OK,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog,
							int which) {
						System.out.println("begin update activities");
						new Thread(UpdateActivityConfigRunnable).start();
					}
				}).create();
	    AlertDialog dlg=altDB.show();
	    Button b=(Button)dlg.getButton(DialogInterface.BUTTON_POSITIVE);
	    b.setTextSize(16);
	    b.setBackgroundResource(R.drawable.publicalertbuttonbackground);
	    b=(Button)dlg.getButton(DialogInterface.BUTTON_NEGATIVE);
	    b.setTextSize(16);
	    b.setBackgroundResource(R.drawable.publicalertbuttonbackground);
    }
    
    Runnable UpdateMenusConfigRunnable = new Runnable(){  
 		@Override  
    	public void run() {
 	    	Message message;
 	    	String hintmsg;
			
 	    	hintmsg=getString(R.string.string_updatemenus_wait);
			message = Message.obtain();
			message.obj=hintmsg;
			msghandler.sendMessage(message);
 			
    		Looper.prepare();
    		try{
    	    	String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
    	    	URL url = new URL(myCls.getHttpServerUrl()+"/simba/getconfig.php?method=getmenus");
    	    	System.out.println("url="+url.toString());
    	    	byte[] entity = xml.getBytes("UTF-8");
    	    	HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    	    	conn.setConnectTimeout(5000);
    	    	conn.setRequestMethod("POST");
    	    	conn.setDoOutput(true);
    	    	conn.setDoInput(true);
    	    	conn.setUseCaches(false);
    	    	
    	    	//指定发送的内容类型为xml
    	    	conn.setRequestProperty("Content-Type", "text/xml; charset=UTF-8");
    	    	conn.setRequestProperty("Content-Length", String.valueOf(entity.length));
    	    	conn.setRequestProperty("Charset", "utf-8");
    	    	OutputStream outStream = conn.getOutputStream();
    	    	outStream.write(entity);
    	    	outStream.flush();
    	    	outStream.close();
    	    	
    	    	if(conn.getResponseCode() == 200){
    	    		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream(),"utf-8"));
    	    		String resultString = "";
    	    		String readLine = "";
    	    		while((readLine=bufferedReader.readLine())!= null){
    	    			resultString += readLine;
    	    		}
    	    		bufferedReader.close();
    	    		conn.disconnect();
    	    		
    	    		if(resultString.equals("NULL")){
    	    			hintmsg=getString(R.string.string_updatemenus_null);
    	    			message = Message.obtain();
    	    			message.obj=hintmsg;
    	    			msghandler.sendMessage(message);
    	    		}else{
        	    		System.out.println(resultString);
        	    		writeXMLToFile("menuconfig.xml",resultString,getString(R.string.string_updatemenus_ok));
    	    		}    		
    	    	}else{
	    			hintmsg=getString(R.string.string_updatemenus_faild);
	    			message = Message.obtain();
	    			message.obj=hintmsg;
	    			msghandler.sendMessage(message);
    	    	}
        	}catch(Exception e){
        		System.out.println(e.toString());
    			hintmsg=getString(R.string.string_update_check_net);
    			message = Message.obtain();
    			message.obj=hintmsg;
    			msghandler.sendMessage(message);
        	}
    	}  
    };
    
    Runnable UpdateActivityConfigRunnable = new Runnable(){  
 		@Override  
    	public void run() {
 	    	Message message;
 	    	String hintmsg;
			
 	    	hintmsg=getString(R.string.string_searchactivity_wait);
			message = Message.obtain();
			message.obj=hintmsg;
			msghandler.sendMessage(message);
 			
    		Looper.prepare();
    		try{
    	    	String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
    	    	URL url = new URL(myCls.getHttpServerUrl()+"/simba/menu.php?method=searchactivity");
    	    	System.out.println("url="+url.toString());
    	    	byte[] entity = xml.getBytes("UTF-8");
    	    	HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    	    	conn.setConnectTimeout(5000);
    	    	conn.setRequestMethod("POST");
    	    	conn.setDoOutput(true);
    	    	conn.setDoInput(true);
    	    	conn.setUseCaches(false);
    	    	
    	    	//指定发送的内容类型为xml
    	    	conn.setRequestProperty("Content-Type", "text/xml; charset=UTF-8");
    	    	conn.setRequestProperty("Content-Length", String.valueOf(entity.length));
    	    	conn.setRequestProperty("Charset", "utf-8");
    	    	OutputStream outStream = conn.getOutputStream();
    	    	outStream.write(entity);
    	    	outStream.flush();
    	    	outStream.close();
    	    	
    	    	if(conn.getResponseCode() == 200){
    	    		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream(),"utf-8"));
    	    		String resultString = "";
    	    		String readLine = "";
    	    		while((readLine=bufferedReader.readLine())!= null){
    	    			resultString += readLine;
    	    		}
    	    		bufferedReader.close();
    	    		conn.disconnect();
    	    		
    	    		if(resultString.equals("NULL")){
    	    			hintmsg=getString(R.string.string_updateactivity_null);
    	    			message = Message.obtain();
    	    			message.obj=hintmsg;
    	    			msghandler.sendMessage(message);
    	    		}else{
        	    		System.out.println(resultString);
        	    		writeXMLToFile("activityconfig.xml",resultString,getString(R.string.string_updateactivity_ok));
    	    		}    		
    	    	}else{
	    			hintmsg=getString(R.string.string_updateactivity_faild);
	    			message = Message.obtain();
	    			message.obj=hintmsg;
	    			msghandler.sendMessage(message);
    	    	}
        	}catch(Exception e){
        		System.out.println(e.toString());
    			hintmsg=getString(R.string.string_update_check_net);
    			message = Message.obtain();
    			message.obj=hintmsg;
    			msghandler.sendMessage(message);
        	}
    	}  
    };
    
    public void writeXMLToFile(String filename, String XML, String tip){
    	Message message;
    	String hintmsg;
    	try {
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            	File appHome = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"//simba//"); 
            	File subPath = new File(appHome+"//dataconfig//"); 
            	appHome.mkdir(); 
            	subPath.mkdir(); 
            	// 写入
            	File file = new File(subPath.toString(), filename);
            	FileOutputStream fos = new FileOutputStream(file);
            	fos.write(XML.getBytes("UTF-8"));
            	fos.close();
    			
           		hintmsg=tip;
    			message = Message.obtain();
    			message.obj=hintmsg;
    			msghandler.sendMessage(message);
            }
        }catch (IOException e) {
            e.printStackTrace();
        }  
    }
   	
	public Handler msghandler=new Handler(){
 		public void handleMessage(Message msg){
    		String message=(String)msg.obj;
    		myCls.AlertToast(message, Gravity.BOTTOM, Toast.LENGTH_LONG, MainActivity.this);
    	}
   	};
   	
	public Handler msgdownhandler=new Handler(){
 		public void handleMessage(Message msg){
    		String message=(String)msg.obj;
    		myCls.AlertToast(message, Gravity.BOTTOM, Toast.LENGTH_LONG, MainActivity.this);
    	    EditText et=(EditText)findViewById(R.id.editTextDownlog);
    	    if(et.getVisibility()==View.VISIBLE){
    	    	et.append(message+"\r\n");
    	    }
    	}
   	};

    private void doConfigServer(){
		final AlertDialog.Builder altDB = new AlertDialog.Builder(this);
		altDB.setTitle(getString(R.string.config_title));
		altDB.setInverseBackgroundForced(true);
		// 创建内容显示区域view
		LayoutInflater inflater=LayoutInflater.from(this);    
	    final View tView=inflater.inflate(R.layout.dialog_configserver, null);
		EditText ets=(EditText)tView.findViewById(R.id.config_server);
		EditText etp=(EditText)tView.findViewById(R.id.config_port);
		IniFile ini=new IniFile();
		if(ini.GetConfigFileIsExist()){
			try{
				ini.IniReaderHasSection();
				ets.setText(ini.getValue("HTTPSERV", "serv", "http://192.168.0.1"));
				etp.setText(ini.getValue("HTTPSERV", "port", "80"));
			}catch(IOException e){
				e.printStackTrace();
			}
		}		
		
	    altDB.setPositiveButton(R.string.string_button_Cancel,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog,
							int which) {
						try {						 
	                        Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
	                        field.setAccessible(true);
	                        field.set(dialog,true);//关闭
	                     } catch (Exception e) {
	                        e.printStackTrace();
	                     } 
					}
				});
		altDB.setNegativeButton(R.string.string_button_OK,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog,
							int which) {
						EditText et=(EditText)tView.findViewById(R.id.config_server);
						EditText etn=(EditText)tView.findViewById(R.id.config_port);
                        
						try{
							Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
	                        field.setAccessible(true);
	                        if(et.getText().toString().trim().length()==0){  
	                        	myCls.AlertToast(getString(R.string.config_noserver), Gravity.TOP, Toast.LENGTH_LONG, MainActivity.this);
	                            field.set(dialog,false);//不关闭
	                            return;
	                        }else if(etn.getText().toString().trim().length()==0){  
	                        	myCls.AlertToast(getString(R.string.config_noport), Gravity.TOP, Toast.LENGTH_LONG, MainActivity.this);
	                            field.set(dialog,false);//不关闭
	                            return;
	                        }else{
					    		Message message;
					    		message = Message.obtain();
					    		message.arg1=2;
					    		Bundle bd=new Bundle();
					    		bd.putString("cmd", "change_config");
					    		bd.putString("server", et.getText().toString().trim());
					    		bd.putString("port",etn.getText().toString().trim());
					    		message.setData(bd);
					    		handler.sendMessage(message);
	                        	field.set(dialog, true);//关闭
	                        }
						}catch(Exception ex){
							ex.printStackTrace();
						}
					}
				}).create();
	    //设置dialog的view
	    altDB.setView(tView);
	    //显示对话框
	    final AlertDialog dlg=altDB.show();
	    Button b=(Button)dlg.getButton(DialogInterface.BUTTON_POSITIVE);
	    b.setTextSize(16);
	    b.setBackgroundResource(R.drawable.publicalertbuttonbackground);
	    b=(Button)dlg.getButton(DialogInterface.BUTTON_NEGATIVE);
	    b.setTextSize(16);
	    b.setBackgroundResource(R.drawable.publicalertbuttonbackground);
    }
    
	public Handler handler=new Handler(){
 		public void handleMessage(Message msg){
 			if(msg.arg1==1){
 				Node n=(Node)msg.obj;
 				initMenuListItem(n);
 			}else if(msg.arg1==2){
 				try{
 		            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
 		            	File appHome = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"//simba//"); 
 		            	File subPath = new File(appHome+"//dataconfig//"); 
 		            	appHome.mkdir(); 
 		            	subPath.mkdir();
 		            	
		 				Bundle bd=msg.getData();
		 				String serv=bd.getString("server");
		 				String port=bd.getString("port");
		 				
		 				IniFile ini=new IniFile();
		 				//ini.IniReaderHasSection();
		 				ini.setValue("HTTPSERV", "serv", serv);
		 				ini.setValue("HTTPSERV", "port", port);
		 				ini.flush();
 		            }
	 				return;
 				}catch(IOException e){
 					e.printStackTrace();
 					return;
 				}
 			}else if(msg.arg1==3){
 				clsItemImage ci=(clsItemImage)(msg.obj);
 				menuShowListAdapter adp=(menuShowListAdapter)ci.adpAdapter;
 				adp.arrNodes.add(ci.node);
 				adp.notifyDataSetChanged();
				ScrollView sv=(ScrollView)MainActivity.this.findViewById(R.id.scrollview_main);
				sv.scrollTo(0, 0);
 			}else if(msg.arg1==4){
 				updateImages();
 				//ScrollView sv=(ScrollView)MainActivity.this.findViewById(R.id.scrollview_main);
 				//sv.scrollTo(0, 0);
 			}else{
 				String message=(String)msg.obj;
 				myCls.AlertToast(message, Gravity.BOTTOM, Toast.LENGTH_LONG, MainActivity.this);
 			}
 			super.handleMessage(msg);            
    	}
   	};
   	
   	public void showFirstPage(View v){
	    EditText et=(EditText)findViewById(R.id.editTextDownlog);
	    et.setVisibility(View.GONE);
   		DownloadImages di=new DownloadImages();
   		
   		LinearLayout typelayout=(LinearLayout)findViewById(R.id.select_layout_type);
   		for(int i=0;i<typelayout.getChildCount();i++){
			Button b1=(Button)typelayout.getChildAt(i);
			b1.setBackgroundResource(R.drawable.layoutrightborderbackground);

			Drawable bleft=getResources().getDrawable(R.drawable.dot);
			bleft.setBounds(0, 0, bleft.getMinimumWidth(), bleft.getMinimumHeight());
			b1.setCompoundDrawables(bleft, null, null, null);
			
			LinearLayout.LayoutParams plButton = new LinearLayout.LayoutParams(210,50);//LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			b1.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
			b1.setPadding(15, 5, 5, 5);
			b1.setTextSize(22);
			b1.setTextColor(Color.rgb(240, 240, 240));
			b1.setLayoutParams(plButton);
			b1.setHint("0");
		}
	    LinearLayout table=(LinearLayout)findViewById(R.id.table_detail);
		table.removeAllViewsInLayout();
		table.invalidate();

		ArrayList<Node> al=avx.GetActivityList();
		if(al!=null){
			for( int i=0; i<al.size(); i++){
			    Node n=al.get(i);
				//add title
	            String title=n.getAttributes().getNamedItem("title").getNodeValue();
				TextView tv=new TextView(this);
				tv.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT));
				tv.setBackgroundResource(R.drawable.layoutbackground2);
				tv.setGravity(Gravity.CENTER_VERTICAL);
				
				Drawable dleft=getResources().getDrawable(R.drawable.close);
				dleft.setBounds(0, 0, dleft.getMinimumWidth(), dleft.getMinimumHeight());
				tv.setCompoundDrawables(dleft, null, null, null);
				
				tv.setText(title);
				tv.setTextColor(Color.rgb(240, 240, 240));
				tv.setTextSize(22);
				tv.setPadding(0, 0, 0, 3);
	
				table.addView(tv);
				
				//add home image
				ImageView iv=new ImageView(this);
				LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
				lp.topMargin=5;
				lp.bottomMargin=20;
				iv.setLayoutParams(lp);
				iv.setBackgroundResource(R.drawable.layoutbackground3);
				iv.setPadding(1, 0, 7, 6);

				//.setScaleType(ImageView.ScaleType.FIT_XY);
				String fname=n.getAttributes().getNamedItem("img").getNodeValue()+".jpg"; 
				try{
					if(di.imageFileIsExist(fname)){
			    		FileInputStream f = new FileInputStream(di.getImageFileLongName(fname)); 
			    		Bitmap bm = null; 
			    		BitmapFactory.Options opt = new BitmapFactory.Options(); 
			    		//opt.inSampleSize = 2;//图片的长宽都是原来的1/8 
			            opt.inPreferredConfig = Bitmap.Config.RGB_565;  
			            opt.inPurgeable = true; 
			            opt.inInputShareable = true;
			           
			            BufferedInputStream bis = new BufferedInputStream(f, 1*1024*1024); 
			            bm = BitmapFactory.decodeStream(bis, null, opt);
			    		
			    		//bm=di.drawTextToBitmap(bm, n, this);
			    		iv.setImageBitmap(bm);
					}
				}catch(Exception e){
					e.printStackTrace();
				}
				table.addView(iv);
			}  
			ScrollView sv=(ScrollView)findViewById(R.id.scrollview_main);
			sv.scrollTo(0, 0);
		}		
   	}
   	
    class LoadMenuTypeItemImageRunnable implements Runnable{
    	public menuShowListAdapter adpAdapter=null;
    	ArrayList<Node> snList=null;
    	public int itemidx=1;
    	@Override  
    	public void run() {
    		Looper.prepare();
    		
			for(int j=0;j<snList.size();j++){
				Message message = Message.obtain();
				message.arg1=3;
				message.obj = new clsItemImage(adpAdapter, snList.get(j));
				handler.sendMessageDelayed(message, 50);
			}
    	}
    };
    
    public class clsItemImage{
    	public menuShowListAdapter adpAdapter;
    	public Node node;
    	
    	public clsItemImage(menuShowListAdapter adapter,Node n){
    		adpAdapter=adapter;
    		node=n;
    	}
    }
}
