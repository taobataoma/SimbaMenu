package com.oldhawk.simbamenu;  
  
import java.io.BufferedReader;  
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;  
import java.io.FileWriter;
import java.io.IOException;  
import java.util.HashMap;  
import java.util.Map;  
import java.util.Map.Entry;
import java.util.Properties; 
import java.util.Set;
import android.os.Environment;
  
public class IniFile {  
	private String filename=Environment.getExternalStorageDirectory().getAbsolutePath()+"/simba/dataconfig/config.ini";
	private Map<String,Properties> sections=new HashMap<String, Properties>();;      
    private String section;      
    private Properties properties;
    
	public boolean GetConfigFileIsExist(){
    	File file=new File(filename);
    	return file.exists();
	}
      
    public void IniReaderHasSection() throws IOException{  
        //sections = new HashMap<String, Properties>();  
        BufferedReader reader = new BufferedReader(new FileReader(filename));  
        read(reader);  
        reader.close(); 
    }  

    private void read(BufferedReader reader)  throws IOException{  
        String line;  
        while((line = reader.readLine())!=null){  
            parseLine(line);  
        }  
    }  
  
    private void parseLine(String line) {  
        line = line.trim();  
        if(line.matches("\\[.*\\]")==true){  
            section = line.replaceFirst("\\[(.*)\\]", "$1");  
            properties = new Properties();  
            sections.put(section, properties);  
        }else{  
            if(properties!=null  
                    &&!line.startsWith(";")  
                    &&line.length()>0){  
                int i = line.indexOf('=');  
                String name = line.substring(0,i).trim();  
                String value = line.substring(i+1).trim();  
                properties.setProperty(name, value);  
            }  
        }  
    }  
      
    public String getValue(String section,String name,String def){  
        Properties p = sections.get(section);  
          
        if(p == null)return null;  
          
        if(p.getProperty(name)!=null)
        	return p.getProperty(name);
        else
        	return def;
    }
    
    public boolean setValue(String section, String name, String value){
        Properties p = sections.get(section);
        if (p == null){
            p = new Properties();
            sections.put(section, p);
        }
        String val = p.getProperty(name);
        if(val == null){
            //TODO:add a new name
        }
        p.setProperty(name, value);
        return true;
    }
    
    public boolean flush() throws IOException{
        if (filename.trim().length()==0)
        	return false;
    	
    	FileWriter fw = null;
        BufferedWriter bw = null;
        fw = new FileWriter(filename);
        bw = new BufferedWriter(fw);
        if(sections == null || sections.isEmpty()){
            //TODO: just empty the file 
            bw.flush();
            bw.close();
            return true;
        }
        Set<Entry<String, Properties>> entryset = sections.entrySet();
        for(Entry<String, Properties> entry: entryset){
            //Write sections
            String strSection = (String) entry.getKey();
            Properties p = (Properties) sections.get(strSection);
            bw.write("[" +strSection +"]");
            bw.newLine();
            if(p == null || p.isEmpty()){
                //TODO: just empty the file         
                continue;
            }
            //Write Properties
            for(Object obj: p.keySet()){
                String key = (String)obj;
                bw.write(key +"=");
                String value = p.getProperty(key);
                bw.write(value);
                bw.newLine();
            }
        }
        bw.flush();
        bw.close();
        return true;
    }
} 
