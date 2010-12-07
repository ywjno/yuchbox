package com.yuchting.yuchberry.server.frame;

import java.awt.Dimension;
import java.util.Vector;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import com.yuchting.yuchberry.server.fetchMgr;

class accountTableModel extends DefaultTableModel{
	
	final static String[] 	fsm_tableCol = {"账户","端口","用户密码","剩余时间（小时）","SSL 状态","当前状态"};
	final static Object[][] fsm_tableData = {{}};
	
	public String getColumnName(int col) {
        return fsm_tableCol[col].toString();
    }
	    
    public int getColumnCount() { 
    	return fsm_tableCol.length; 
    }
        
    public boolean isCellEditable(int row, int col){
    	return false;
    }
    
}

public class accountTable extends JTable{
		
	mainFrame		m_mainFrame = null;
	
	final static accountTableModel m_defaultModel = new accountTableModel();
	
	Vector m_fetchMgrListRef	= new Vector();
	
	public accountTable(mainFrame _mainFrame){
		super(m_defaultModel);
				
		m_mainFrame = _mainFrame;
		
		setAutoscrolls(true);
		
		setPreferredScrollableViewportSize(new Dimension(500, 70));
		setFillsViewportHeight(true);
		
		TableColumn column = null;
		for (int i = 0; i < accountTableModel.fsm_tableCol.length; i++) {
		    column = getColumnModel().getColumn(i);
		    switch(i){
		    case 0:
		    	column.setPreferredWidth(200); 
		    	break;
		    case 1:
		    case 2:
		    	column.setPreferredWidth(100);
		    	break;
		    }
		}
	}
	
	public void AddAccount(final fetchThread _thread){
		
		fetchMgr _mgr = _thread.m_fetchMgr;
		
		Object[] t_row = {
				_mgr.GetAccountName(),
				new Integer(_mgr.GetServerPort()),
				_mgr.GetUserPassword(),
				new Long(_thread.m_expiredTime),
				new Boolean(_mgr.IsUseSSL()),
				"",
		};
		
		m_defaultModel.addRow(t_row);
		m_fetchMgrListRef.addElement(_thread);
	}
	
	public void DelAccount(final fetchThread _thread){
		final int t_rowNum = m_defaultModel.getRowCount();
		
		for(int i = 0;i < t_rowNum;i++){
			String name = (String)m_defaultModel.getValueAt(i, 0);
			if(_thread.m_fetchMgr.GetAccountName().equals(name)){
				
				m_fetchMgrListRef.remove(i);
				m_defaultModel.removeRow(i);
				
				break;
			}
		}
		
	}
}
