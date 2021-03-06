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
package com.yuchting.yuchberry.client.im;

import local.yblocalResource;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Keypad;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.component.AutoTextEditField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.container.PopupScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

import com.yuchting.yuchberry.client.recvMain;

public class SearchStatus extends PopupScreen implements FieldChangeListener{
	
	AutoTextEditField	m_editTextArea = new AutoTextEditField();
	
	int					m_currSelected	= 0;
	MainIMScreen		m_mainScreen = null;
	
	public SearchStatus(MainIMScreen _screen){
		super(new VerticalFieldManager(Manager.VERTICAL_SCROLL));
		m_mainScreen = _screen;
	
		add(m_editTextArea);	
		add(new LabelField(recvMain.sm_local.getString(yblocalResource.IM_SEARCH_PROMPT_LABEL)));

		m_editTextArea.setChangeListener(this);
	}
		
	public void fieldChanged(Field field,int _context){
		
		if(_context != FieldChangeListener.PROGRAMMATIC){
			
			String t_keyword = m_editTextArea.getText();
			if(t_keyword.length() != 0){
				m_mainScreen.selectedByKeyword(t_keyword,m_currSelected);
			}		
		}
	}
	
	public boolean onClose(){
		close();
		return true;
	}
	
	public void close(){
		m_mainScreen.m_searchStatus = null;
		super.close();
	}
	
	public void searchNext(){
		
		String t_keyword = m_editTextArea.getText();
		if(t_keyword.length() != 0){
			
			if(!m_mainScreen.selectedByKeyword( t_keyword, ++m_currSelected )){

				// can't find or find the final matched
				//
				m_currSelected = 0;
				m_mainScreen.selectedByKeyword(t_keyword,m_currSelected);
			}
		}	
	}
	
	protected boolean keyDown(int keycode,int time){
		final int key = Keypad.key(keycode);
		
		if(key == 10){
			searchNext();
			return true;
		}
		
		return super.keyDown(keycode,time);
		
	}
}
