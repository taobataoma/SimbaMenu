package com.oldhawk.simbamenu;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.app.Application;
import android.content.Context;
import android.os.Environment;

public class ActivityXML extends Application {
	private String filename=Environment.getExternalStorageDirectory().getAbsolutePath()+"/simba/dataconfig/activityconfig.xml";
	private Document document;
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
	}
	
	public boolean GetConfigFileIsExist(){
    	File file=new File(filename);
    	return file.exists();
	}
	
	public ActivityXML(Context con){
		try{
			File file=new File(filename);
    		FileInputStream inputStream = new FileInputStream(file);  
    		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();  
	    	DocumentBuilder builder = factory.newDocumentBuilder();
	    	document = builder.parse(inputStream);
    	}catch(Exception ex){
    		System.out.println(ex.toString());
    	}
	}
    
    public ArrayList<Node> GetActivityList(){
     	try{
     		ArrayList<Node> al=new ArrayList<Node>();
     		Element root = document.getDocumentElement();
	    	NodeList nsList=root.getElementsByTagName("item");
	    	for(int i=0;i<nsList.getLength();i++){
	    		Node n=nsList.item(i);
	    		al.add(n);
	    	}
	    	return al;
    	}catch(Exception ex){
    		System.out.println(ex.toString());
    	}
   	
    	return null;
    }
};