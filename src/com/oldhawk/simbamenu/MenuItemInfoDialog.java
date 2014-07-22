package com.oldhawk.simbamenu;

import java.io.BufferedInputStream;
import java.io.FileInputStream;

import org.w3c.dom.Node;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

public class MenuItemInfoDialog extends Activity {
	//private MyClass myCls=new MyClass();
	private SelectMenuXML smx=null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_menuiteminfo);
		
		smx=new SelectMenuXML(this);
		
		WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
		WindowManager.LayoutParams params = this.getWindow()
				.getAttributes();
		Point point=new Point();
		wm.getDefaultDisplay().getSize(point);
		params.width = point.x-10;
		// params.height = 200 ;
		//params.x=0;
		//params.y=500;
		params.alpha = 1;// f;
		this.getWindow().setAttributes(params);
		this.getWindow().setGravity(Gravity.BOTTOM);
		// dlg.getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
		// WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
		
		
		Intent intent=getIntent();
		String idx=intent.getStringExtra("itemidx");
		Node n=smx.GetNodeByMenuItemIdx(idx, this);
		//myCls.AlertToast(n.getAttributes().getNamedItem("name").getNodeValue(), this);
		
		initItemInfo(n);
	}

	private void initItemInfo(Node n){
		//image
		DownloadImages di=new DownloadImages();
		final ImageView iv=(ImageView)findViewById(R.id.imageView_itempic);
		final String filename=n.getAttributes().getNamedItem("img").getNodeValue()+".jpg";
		String filepath="";
		filepath=Environment.getExternalStorageDirectory().getAbsolutePath()+"/simba/menuimages/";
		filepath=filepath+filename;
		Log.d("D",filepath);			
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
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		//sale image and price
		ImageView ivs=(ImageView)findViewById(R.id.imageView_sale);
		TextView tv1=(TextView)findViewById(R.id.textView_itemprice);
		TextView tv2=(TextView)findViewById(R.id.textView_oldprice);
		String pri="";
		String pri2="";
		int price=Integer.parseInt(n.getAttributes().getNamedItem("saleprice").getNodeValue());
		if (price==0){
			price=Integer.parseInt(n.getAttributes().getNamedItem("price").getNodeValue());
			ivs.setVisibility(View.GONE);
		}else{
			ivs.setVisibility(View.VISIBLE);
			pri2=getString(R.string.string_price_old);
			pri2=pri2+n.getAttributes().getNamedItem("price").getNodeValue();
			pri2=pri2+getString(R.string.string_money_unit);
			pri2=pri2+"/"+n.getAttributes().getNamedItem("unit").getNodeValue();
			tv2.setText(pri2);
		}
		pri=getString(R.string.string_price_today)+price;
		pri=pri+getString(R.string.string_money_unit);
		pri=pri+"/"+n.getAttributes().getNamedItem("unit").getNodeValue();
		tv1.setText(pri);
		//title
		TextView tv=(TextView)findViewById(R.id.iteminfo_itemtitle);
		String tit=n.getParentNode().getParentNode().getAttributes().getNamedItem("name").getNodeValue();
		tit=tit+" → "+n.getParentNode().getAttributes().getNamedItem("name").getNodeValue();
		tit=tit+" → "+n.getAttributes().getNamedItem("idx").getNodeValue();
		tit=tit+" → "+n.getAttributes().getNamedItem("name").getNodeValue();
		tv.setText(tit);
		//name
		tv=(TextView)findViewById(R.id.textView_itemname);
		tit=getString(R.string.string_prefix_name);
		tit=tit+n.getAttributes().getNamedItem("name").getNodeValue();
		tv.setText(tit);
		//salecount
		tv=(TextView)findViewById(R.id.textView_itemsalecount);
		tit=getString(R.string.string_prefix_salecount);
		tit=tit+n.getAttributes().getNamedItem("sellcount").getNodeValue();
		tit=tit+n.getAttributes().getNamedItem("unit").getNodeValue();
		tv.setText(tit);
		
		String subidx=n.getAttributes().getNamedItem("subitemidx").getNodeValue();
		String idx=n.getAttributes().getNamedItem("idx").getNodeValue();
		if(subidx.equals(idx)){		
			//desc
	    	tv=(TextView)findViewById(R.id.textView_itemdesc);
	    	String desc=n.getAttributes().getNamedItem("desc").getNodeValue();
	    	if(desc.trim().length()>0){
	    		tv.setText(desc);
	    		tv.setVisibility(View.VISIBLE);
	    	}else{
	    		tv.setVisibility(View.GONE);
	    	}
	    	//nutr
	    	tv=(TextView)findViewById(R.id.textView_itemnutr);
	    	String nutr=n.getAttributes().getNamedItem("nutrition").getNodeValue();
	    	if(nutr.trim().length()>0){
	    		tv.setText(nutr);
	    		tv.setVisibility(View.VISIBLE);
	    	}else{
	    		tv.setVisibility(View.GONE);
	    	}
	    	//feat
	    	tv=(TextView)findViewById(R.id.textView_itemfeat);
	    	String feat=n.getAttributes().getNamedItem("feature").getNodeValue();
	    	if(feat.trim().length()>0){
	    		tv.setText(feat);
	    		tv.setVisibility(View.VISIBLE);
	    	}else{
	    		tv.setVisibility(View.GONE);
	    	}
		}else{
			String seleidx=n.getAttributes().getNamedItem("selectidx").getNodeValue();
			String freeidx=n.getAttributes().getNamedItem("freeitemidx").getNodeValue();
			String desc="";
			String name="";
			int x=0;
			
			String[] id=subidx.split(",");
			for(int i=0;i<id.length;i++){
				name="";
				Node itemNode=smx.GetNodeByMenuItemIdx(id[i], this);
				if(itemNode!=null){
					x++;
					name=itemNode.getAttributes().getNamedItem("name").getNodeValue();
					desc=appendSubString(x,desc, name);
				}
			}
			if(!freeidx.equals("0")){
				id=freeidx.split(",");
				for(int i=0;i<id.length;i++){
					name="";
					Node itemNode=smx.GetNodeByMenuItemIdx(id[i], this);
					if(itemNode!=null){
						x++;
						name=itemNode.getAttributes().getNamedItem("name").getNodeValue();
						desc=appendSubString(x,desc, name);
					}
				}
			}
			if(!seleidx.equals("0")){
				String[] subs=seleidx.split(",");
				for(int i=0;i<subs.length;i++){
					name="";
					String onesub=subs[i];
					String snum=onesub.substring(onesub.length()-1, onesub.length());
					onesub=onesub.substring(1, onesub.length()-2);
					
					id=onesub.split("[|]");
					for(int j=0;j<id.length;j++){
						Node itemNode=smx.GetNodeByMenuItemIdx(id[j], this);
						if(itemNode!=null){
							String sname=itemNode.getAttributes().getNamedItem("name").getNodeValue();
							if(name.length()>0){
								name=name+"，"+sname;
							}else{
								name=sname;
							}
						}
					}
					x++;
					name=name+"， "+id.length+"选"+snum;
					desc=appendSubString(x,desc, name);
				}
			}
			tv=(TextView)findViewById(R.id.textView_itemdesc);
    		tv.setText(desc);
    		tv.setVisibility(View.VISIBLE);
	    	tv=(TextView)findViewById(R.id.textView_itemnutr);
	    	tv.setVisibility(View.GONE);
	    	tv=(TextView)findViewById(R.id.textView_itemfeat);
	    	tv.setVisibility(View.GONE);
		}
	}
	
	private String appendSubString(int number,String srcstr,String substr){
		if(srcstr.length()>0){
			return  srcstr+"\n"+number+"："+substr;
		}else{
			return number+"："+substr;
		}
	}
	
	public void ExitThisWindow(View v){
		finish();
	}
}
