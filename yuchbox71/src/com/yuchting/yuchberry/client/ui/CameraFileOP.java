/**
 *  Dear developer:
 *  
 *   If you want to modify this file of project and re-publish this please visit:
 *  
 *     http://code.google.com/p/yuchberry/wiki/Project_files_header
 *     
 *   to check your responsibility and my humble proposal. Thanks!
 *   
 *  -- 
 *  Yuchs' Developer    
 *  
 *  
 *  
 *  
 *  尊敬的开发者：
 *   
 *    如果你想要修改这个项目中的文件，同时重新发布项目程序，请访问一下：
 *    
 *      http://code.google.com/p/yuchberry/wiki/Project_files_header
 *      
 *    了解你的责任，还有我卑微的建议。 谢谢！
 *   
 *  -- 
 *  语盒开发者
 *  
 */
package com.yuchting.yuchberry.client.ui;

import java.io.InputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

import net.rim.device.api.io.file.FileSystemJournal;
import net.rim.device.api.io.file.FileSystemJournalEntry;
import net.rim.device.api.io.file.FileSystemJournalListener;
import net.rim.device.api.math.Fixed32;
import net.rim.device.api.system.EncodedImage;
import net.rim.device.api.system.JPEGEncodedImage;
import net.rim.device.api.ui.XYPoint;
import net.rim.device.api.util.MathUtilities;

import com.yuchting.yuchberry.client.recvMain;
import com.yuchting.yuchberry.client.sendReceive;
import com.yuchting.yuchberry.client.weibo.fetchWeibo;

public abstract class CameraFileOP implements FileSystemJournalListener{
	
	static private long 			sm_lastUSN = 0;

	public void fileJournalChanged() {
		long nextUSN = FileSystemJournal.getNextUSN();
		
		for(long lookUSN = nextUSN - 1; lookUSN >= sm_lastUSN ; --lookUSN) {
			
			FileSystemJournalEntry entry = FileSystemJournal.getEntry(lookUSN);
			if (entry == null) {
			    break; 
			}
			
			if(entry.getEvent() == FileSystemJournalEntry.FILE_ADDED){
				
				String entryPath = entry.getPath();
				
				if (entryPath != null && canAdded()){
					
					if(entryPath.indexOf(recvMain.fsm_mailAttachDir) != -1
						|| entryPath.indexOf(recvMain.fsm_weiboImageDir) != -1 
						|| entryPath.indexOf(recvMain.fsm_IMImageDir) != -1){
						
						// is not photo
						// is weibo/IM head image
						//
						continue;
					}
					
					
					if(addUploadingPic("file://" + entryPath)){
						sm_lastUSN = lookUSN + 1;
						break;
					}					
				}
			}
		}
	}
		
	public boolean addUploadingPic(String _file){
		
		String t_path = _file.toLowerCase();
		
		int t_type = 0;
		if(t_path.endsWith("png")){
			t_type = fetchWeibo.IMAGE_TYPE_PNG;
		}else if(t_path.endsWith("jpg")){
			t_type = fetchWeibo.IMAGE_TYPE_JPG;
		}else if(t_path.endsWith("bmp")){
			t_type = fetchWeibo.IMAGE_TYPE_BMP;
		}else if(t_path.endsWith("gif")){
			t_type = fetchWeibo.IMAGE_TYPE_GIF;
		}else{
			return false;
		}
		
		onAddUploadingPic(_file,t_type);
		
		return true;
	}
	
	static public byte[] resizePicFile(String _imageFile,XYPoint _point)throws Exception{
		
		if(_imageFile != null){
			byte[] t_content = null;
			
			FileConnection t_file = (FileConnection)Connector.open(_imageFile,Connector.READ_WRITE);
			
			if(t_file.exists()){
				try{
					InputStream t_fileIn = t_file.openInputStream();
					try{
						
						byte[] t_buffer = new byte[(int)t_file.fileSize()];
						sendReceive.ForceReadByte(t_fileIn, t_buffer, t_buffer.length);
						
						t_content = resizePicBytes(t_buffer,_point);
														
					}finally{
						t_fileIn.close();
						t_fileIn = null;
					}
					
					
				}finally{
					t_file.close();
					t_file = null;
				}
			}
			
			return t_content;			
		}
		
		return null;
	}
	
	static public byte[] resizePicBytes(byte[] _imageBytes,XYPoint _point)throws Exception{
		
		if(_imageBytes != null){
			
			EncodedImage t_origImage = EncodedImage.createEncodedImage(_imageBytes, 0, _imageBytes.length);
			
			if(t_origImage == null){
				throw new Exception("Can't createEncodedImage for this bytes!");
			}
			
			int t_origWidth = t_origImage.getWidth();
			int t_origHeight = t_origImage.getHeight();
			
			XYPoint t_scaleSize = null;
			
			if(_point != null){
				t_scaleSize = new XYPoint(_point);
			}else{
				t_scaleSize = new XYPoint(t_origWidth,t_origHeight);
			}			
			
			float t_orgRate = (float)t_origWidth / (float)t_origHeight;
			float t_scaleRate = (float)t_scaleSize.x / (float)t_scaleSize.y;
			
			if(Math.abs(t_orgRate - t_scaleRate) > 1e-5){
				// the rate is note same, convert the scale size to keep orig rate
				//
				t_scaleSize.y = (int)(t_scaleSize.x / t_orgRate);
			}
			
			try{
				
				JPEGEncodedImage finalJPEG;
				
				if(t_origWidth > t_scaleSize.x && t_origHeight > t_scaleSize.y){
					
					int scaleX = Fixed32.div(Fixed32.toFP(t_origWidth), Fixed32.toFP(t_scaleSize.x));
					int scaleY = Fixed32.div(Fixed32.toFP(t_origHeight), Fixed32.toFP(t_scaleSize.y));
														
					finalJPEG = JPEGEncodedImage.encode(t_origImage.scaleImage32(scaleX, scaleY).getBitmap(), 55);
					
				}else{
					
					finalJPEG = JPEGEncodedImage.encode(t_origImage.getBitmap(), 55);
				}
				
				if(finalJPEG == null){
					throw new Exception("Can't JPEGEncodedImage for this bytes!");
				}
				
				_imageBytes = finalJPEG.getData();
													
			}finally{
				
				t_origImage = null;
			}
		}
		
		return _imageBytes;
	}
	
	/**
	 * sub-class override to set state wether image can be added 
	 * @return
	 */
	public abstract boolean canAdded();
	
	/**
	 * sub-lcass override to add uploading Pic
	 * @param _file		filename (full-path)
	 * @param _type		@see fetchWeibo.IMAGE_TYPE_PNG
	 */
	public abstract void onAddUploadingPic(String _file,int _type);
	
}
