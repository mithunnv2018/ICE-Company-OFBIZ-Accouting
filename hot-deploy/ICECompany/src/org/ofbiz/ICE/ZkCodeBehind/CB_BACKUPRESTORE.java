package org.ofbiz.ICE.ZkCodeBehind;
import java.util.zip.*;
import java.awt.Desktop;
import java.io.*;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;

import com.ibm.icu.util.Calendar;

public class CB_BACKUPRESTORE  extends GenericForwardComposer {

	public Button btnBackupDB;
	public String derbyhome="derby.system.home";
	
	public void doAfterCompose(Component comp) throws Exception {
		super.doAfterCompose(comp);
	}

	//DELETE THIS METHOD
	public void mybackup(String source,String destination)
	{
		try {
			ZipOutputStream zos=new ZipOutputStream(new FileOutputStream(destination));
			FileInputStream is=new FileInputStream(source);
			
			
//			ZipEntry zip2=new ZipEntry(arg0)
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void zipDir(String dir2zip, ZipOutputStream zos) 
	{ File zipDir;
	    try 
	   { 
	        //create a new File object based on the directory we 
//	        have to zip File    
	           zipDir = new File(dir2zip); 
	        //get a listing of the directory content 
	        String[] dirList = zipDir.list(); 
	        byte[] readBuffer = new byte[2156]; 
	        int bytesIn = 0; 
	        //loop through dirList, and zip the files 
	        for(int i=0; i<dirList.length; i++) 
	        { 
	            File f = new File(zipDir, dirList[i]); 
	        if(f.isDirectory()) 
	        { 
	                //if the File object is a directory, call this 
	                //function again to add its content recursively 
	            String filePath = f.getPath(); 
	            zipDir(filePath, zos); 
	                //loop again 
	            continue; 
	        } 
	            //if we reached here, the File object f was not 
//	            a directory 
	            //create a FileInputStream on top of f 
	            FileInputStream fis = new FileInputStream(f); 
//	            create a new zip entry 
	        ZipEntry anEntry = new ZipEntry(f.getPath()); 
	            //place the zip entry in the ZipOutputStream object 
	        zos.putNextEntry(anEntry); 
	            //now write the content of the file to the ZipOutputStream 
	            while((bytesIn = fis.read(readBuffer)) != -1) 
	            { 
	                zos.write(readBuffer, 0, bytesIn); 
	            } 
	           //close the Stream 
	           fis.close(); 
	    } 
	} 
	catch(Exception e) 
	{ 
	    alert(e.getMessage()); 
	} 
	}
	
	public String getFileLocation() {
		String ofbizlocation=System.getProperty("ofbiz.home");
		String filename2=ofbizlocation+"/location.txt";
		if(new File(filename2).exists()==false)
		{
			File f=new File(filename2);
			try {
				f.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		//alert(" location"+filename2);	 
//		BufferedInputStream br=new BufferedInputStream(new FileInputStream(ofbizlocaltion+"/"+filename2));
		
		try {
			
			FileInputStream fr= new FileInputStream(filename2);
			//FileReader fr=new FileReader(filename2);
			FileReader fr2=new FileReader(filename2);
			
			byte []cbuf=new byte[fr.available()];
//			alert(" Madeit");	 
			fr.read(cbuf);
			
			//alert("DONE"+cbuf.length);
			long c2= Calendar.getInstance().getTimeInMillis();
			String retvalue=new String(cbuf);
			retvalue+=c2;
			retvalue+="derby.zip";
			return retvalue;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return "";
		
	}
	public void onClick$btnBackupDB(Event e22)
	{
		String derbylocation=System.getProperty(derbyhome);
		String ofbizlocation=System.getProperty("ofbiz.home");
		String outputfile=getFileLocation();
		if(outputfile.equals(""))
		{
			alert("enter zip filename and location in file location.txt");
			return;
		}
//		alert(ofbizlocation);
//		if(true)return;
		 try {
			ZipOutputStream zos = new   ZipOutputStream(new FileOutputStream(outputfile));
			zipDir(derbylocation, zos);
			zos.close();
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 boolean status=new File(outputfile).exists();
		 alert("File Created");
		
//		for (Object key : System.getProperties().keySet()) {
//			alert(String.valueOf(key)+System.getProperty(String.valueOf(key)));
//			
//			
//		}
	}
}
