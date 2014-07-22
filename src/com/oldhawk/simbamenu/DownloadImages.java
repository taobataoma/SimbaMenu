package com.oldhawk.simbamenu;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import org.w3c.dom.Node;
import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Environment;
import android.util.Log;

public class DownloadImages extends Application {
	private String filepathm=Environment.getExternalStorageDirectory().getAbsolutePath()+"/simba/menuimages/";
	private String filepatha=Environment.getExternalStorageDirectory().getAbsolutePath()+"/simba/activityimages/";
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
	}
	
	public boolean imageFileIsExist(String filename){
		File file=null;
		if(filename.startsWith("a")){
			file=new File(filepatha+filename);
		}else{
			file=new File(filepathm+filename);
		}
    	return file.exists();
	}
	
	public String getImageFileLongName(String filename){
		if(filename.startsWith("a")){
			return filepatha+filename;
		}else {
			return filepathm+filename;
		}
	}
	
	public boolean Download(String url,String savename){
		try{
            /*
    		BitmapFactory.Options opt = new BitmapFactory.Options(); 
    		//options.inSampleSize = 8;//图片的长宽都是原来的1/8 
    		opt.inPreferredConfig = Bitmap.Config.RGB_565;  
            opt.inPurgeable = true; 
            opt.inInputShareable = true;
            opt.outHeight=1024;
            opt.outWidth=682;
            */
			Bitmap bitmap=null;
			if(savename.startsWith("a")){
				bitmap=safeDecodeStream(url, 1024, 682);
			}else{
				bitmap=safeDecodeStream(url, 540,360);//1024, 682);
			}
			if (bitmap != null) {   
				if(savename.startsWith("a")){
					saveFile(bitmap,savename,1024);
				}else{
					saveFile(bitmap,savename,540);
					saveFile(bitmap,savename.replace(".jpg", "_s.jpg"),240);
				}
				return true;
			} 
		}catch(Exception ex){
    		Log.i("error",ex.toString());
    		return false;
		}
		return false;
	}
	
	public InputStream getImageStream(String path) throws Exception{      
	    URL url = new URL(path);      
	    HttpURLConnection conn = (HttpURLConnection) url.openConnection();      
	    conn.setConnectTimeout(5 * 1000);      
	    conn.setRequestMethod("GET");   
	    if(conn.getResponseCode() == HttpURLConnection.HTTP_OK){      
	        return conn.getInputStream();         
	    }else{      
	    	return null;
	    }
	}
	
	public void saveFile(Bitmap bm, String fileName, int sWidth) throws IOException {   
	    String filepath="";
		if(fileName.startsWith("a")){
	    	filepath=filepatha;
	    }else {
	    	filepath=filepathm;
		}
		File dirFile = new File(filepath);   
	    if(!dirFile.exists()){   
	        dirFile.mkdir();   
	    }   
	    
	    int width = bm.getWidth();
        int height = bm.getHeight();
        //计算缩放率，新尺寸除原始尺寸
        float scaleWidth = ((float) sWidth) / width;
        //float scaleHeight = ((float) sHeight) / height;        
        // 创建操作图片用的matrix对象
        Matrix matrix = new Matrix();        
        // 缩放图片动作
        matrix.postScale(scaleWidth, scaleWidth);//scaleHeight);
        //旋转图片 动作
        //matrix.postRotate(45);        
        // 创建新的图片
        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true);
	    
        File myCaptureFile = new File(filepath + fileName);   
	    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(myCaptureFile));   
	    resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);   
	    bos.flush();   
	    bos.close();
    } 

	public Bitmap drawTextToBitmap(Bitmap bm,  Node n, Context con) {  
		String dtext="";
		
		android.graphics.Bitmap.Config bitmapConfig = bm.getConfig();  
		if(bitmapConfig == null) {  
			bitmapConfig = android.graphics.Bitmap.Config.ARGB_8888;  
		}
	  	bm = bm.copy(bitmapConfig, true);	   
	  	Canvas canvas = new Canvas(bm);  
	  	Paint paint = new Paint();
	  	Typeface tf = Typeface.create("sans", Typeface.NORMAL);  
	  	paint.setTypeface(tf);
	  	paint.setColor(Color.rgb(255, 255, 255));  
	  	
	  	dtext=n.getAttributes().getNamedItem("idx").getNodeValue();	  	
	  	paint.setTextSize(50);
	  	paint.setShadowLayer(3f, 3f, 3f, Color.BLACK);
	  	canvas.drawText(dtext, 10, 50, paint);
	  	
	  	dtext=n.getAttributes().getNamedItem("name").getNodeValue();
	  	paint.setTextSize(20);
	  	paint.setShadowLayer(2f, 1f, 1f, Color.BLACK);
	  	canvas.drawText(dtext, 140, 20, paint);
	   
	  	return bm;  
	}
	/** 
	 * A safer decodeStream method 
	 * rather than the one of {@link BitmapFactory} 
	 * which will be easy to get OutOfMemory Exception 
	 * while loading a big image file. 
	 *  
	 * @param uri 
	 * @param width 
	 * @param height 
	 * @return 
	 * @throws FileNotFoundException 
	 */  
	public Bitmap safeDecodeStream(String url, int width, int height){  
	   try{
			int scale = 1;  
		    BitmapFactory.Options options = new BitmapFactory.Options();
		    InputStream is=getImageStream(url);
		    if(is!=null){ 
	        	BufferedInputStream bis=new BufferedInputStream(is,4*1024*1024);
			    if(width>0 || height>0){  
			        // Decode image size without loading all data into memory  
			        options.inJustDecodeBounds = true;
			        BitmapFactory.decodeStream(bis , null, options);  
			          
			        int w = options.outWidth;  
			        int h = options.outHeight;  
			        while (true) {  
			            if ((width>0 && w/2 < width) || (height>0 && h/2 < height)){  
			                break;  
			            }  
			            w /= 2;  
			            h /= 2;  
			            scale *= 2;  
			        }  
			    }  
			    
			    // Decode with inSampleSize option
			    options.inJustDecodeBounds = false;  
			    options.inSampleSize = scale;
			    options.inPreferredConfig = Bitmap.Config.RGB_565;  
			    options.inPurgeable = true; 
			    options.inInputShareable = true;
			    BufferedInputStream biss=new BufferedInputStream(getImageStream(url),4*1024*1024);
			    return BitmapFactory.decodeStream(biss, null, options); 
		    }else{
		    	return null;
		    }
	   }catch(Exception e){
		   e.printStackTrace();
		   return null;
	   }
	}
};