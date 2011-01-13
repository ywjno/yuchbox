package com.yuchting.yuchberry.client;

import java.util.Vector;

import net.rim.device.api.i18n.SimpleDateFormat;
import net.rim.device.api.system.Display;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.FontFamily;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Ui;
import net.rim.device.api.ui.component.RichTextField;
import net.rim.device.api.ui.container.MainScreen;


class ErrorLabelText extends Field{
	Vector m_stringList;
	static final int		fsm_space = 3;
	
	static int sm_fontHeight = 15;
	static int sm_lineHeight = sm_fontHeight + fsm_space;
	
	final static int fsm_display_width		= Display.getWidth();
	final static int fsm_display_height		= Display.getHeight();
	
	int			m_viewPixel_x		= 0;
	int			m_viewPixel_y		= 0;
	
	int			m_movePixel_y		= 0;
	int			m_movePixel_x		= 0;
		
	int			m_selectLine		= 0;
	

	
	public ErrorLabelText(Vector _stringList){
		super(Field.READONLY | Field.NON_FOCUSABLE | Field.USE_ALL_WIDTH);
		
		m_stringList = _stringList;
		try{
			Font myFont = FontFamily.forName("BBMillbankTall").getFont(Font.PLAIN,8,Ui.UNITS_pt);
			setFont(myFont);
			
			sm_fontHeight = myFont.getHeight() - 3;
			sm_lineHeight = sm_fontHeight + fsm_space;
			
		}catch(Exception _e){}
		
	}
	
	public void layout(int _width,int _height){
			
		final int t_size 	= m_stringList.size();
		final int t_height = t_size * sm_lineHeight;
		
		setExtent(fsm_display_width, t_height);
	}
	
	public void paint(Graphics _g){
		
		int t_y = 0;
		int t_startIdx = 0;
		if(m_viewPixel_y < 0){
			t_startIdx = Math.abs(m_viewPixel_y) / sm_lineHeight;
		}
		
		SimpleDateFormat t_format = new SimpleDateFormat("HH:mm:ss");
		
		for(int i = m_stringList.size() - 1 - t_startIdx ;i >= 0 ;i--){
			recvMain.ErrorInfo t_info = (recvMain.ErrorInfo)m_stringList.elementAt(i);
			
			if(m_stringList.size() - m_selectLine - 1 == i){
				
				String t_text = t_format.format(t_info.m_time) + ":" + t_info.m_info;
				_g.drawText(t_text.substring(m_movePixel_x),1,t_y + 1,Graphics.ELLIPSIS);
				
				_g.drawRoundRect(0,t_y,fsm_display_width,sm_lineHeight,1,1);
			}else{
				_g.drawText(t_format.format(t_info.m_time) + ":" + t_info.m_info,0,t_y,Graphics.ELLIPSIS);
			}
			
			t_y += sm_lineHeight;
		}
	}
	
	public void IncreaseRenderSize(int _dx,int _dy){
						
		_dy = _dy * sm_fontHeight;

		final int t_former_move_y = m_movePixel_y;
		
		final int t_former_view_y = m_viewPixel_y;		
		
		final int t_maxHeight = m_stringList.size() * sm_lineHeight;
		
		if(m_movePixel_y + _dy < Math.min(t_maxHeight,fsm_display_height)){
			if(m_movePixel_y + _dy < 0 ){
				m_viewPixel_y -= m_movePixel_y + _dy;
			}else{
				if(m_movePixel_y + _dy - m_viewPixel_y < t_maxHeight){
					m_movePixel_y += _dy;
				}
			}			
		}else{
			if(m_movePixel_y + _dy - m_viewPixel_y < t_maxHeight){
				m_viewPixel_y -= _dy;
			}				
		}
		
		if(m_viewPixel_y > 0){
			m_viewPixel_y = 0;
			m_movePixel_y = 0;
		}
		
		final boolean t_refreshFull = (t_former_view_y != m_viewPixel_y);
				
		final int t_formerLine = m_selectLine;
		m_selectLine = (m_movePixel_y - m_viewPixel_y) / sm_lineHeight;
		
		if(t_refreshFull){
			invalidate();
		}else{
			
			
			if(t_formerLine != m_selectLine){
				
				RefreshRect(m_movePixel_y,t_former_move_y);	
				
				m_movePixel_x = 0;
				
			}else{
				
				final int t_former_x = m_movePixel_x;
				
				if(m_movePixel_x + _dx >= 0){
					m_movePixel_x += _dx;
				}
				
				if(t_former_x != m_movePixel_x){
					RefreshRect(m_movePixel_y,t_former_move_y);	
				}				
			}
		}		
	}
	
	private void RefreshRect(int _y,int _y1){
		
		int t_pos_y			= _y;
		int t_former_pos_y	= _y1;
						
		if(t_pos_y < t_former_pos_y){
			final int t_tmp = t_pos_y;
			t_pos_y = t_former_pos_y;
			t_former_pos_y = t_tmp; 
		}
		
		t_pos_y 		+= sm_lineHeight - (t_pos_y % sm_lineHeight);
		t_former_pos_y	-= (t_former_pos_y % sm_lineHeight);
		
		invalidate(0,t_former_pos_y,Display.getWidth(),t_pos_y - t_former_pos_y);
	}
		
}


public class debugInfo extends MainScreen{
	
	RichTextField 	m_editText	= null;
	recvMain		m_mainApp	= null;
	
	ErrorLabelText  m_errorText = null;
	
	public debugInfo(recvMain _mainApp){
		m_mainApp = _mainApp;
		
		m_errorText = new ErrorLabelText(m_mainApp.GetErrorString());
        add(m_errorText);
        
        
	}
	
	public boolean onClose(){
		close();
		m_mainApp.m_debugInfoScreen = null;
		
		return true;
	}
	
	public void RefreshText(){
		m_mainApp.invokeLater(new Runnable() {
			public void run() {
				m_errorText.layout(0, 0);
				invalidate();			
			}
		});
	}
	
	protected boolean navigationMovement(int dx,int dy,int status,int time){
		m_errorText.IncreaseRenderSize(dx,dy);
		return true;
	}

}