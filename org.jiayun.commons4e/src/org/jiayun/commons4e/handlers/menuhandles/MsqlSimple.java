package org.jiayun.commons4e.handlers.menuhandles;

import static org.jiayun.commons4e.internal.util.JdbcUtils.cols;
import static org.jiayun.commons4e.internal.util.JdbcUtils.getConnection;

import java.util.Collections;
import java.util.Map;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.jiayun.commons4e.Commons4ePlugin;
import org.jiayun.commons4e.handlers.MenuItemHandler;
import org.jiayun.commons4e.internal.util.NameUtils;

public class MsqlSimple extends MenuItemHandler {

	@Override
	protected boolean init(ExecutionEvent event) throws Exception {
		return true;
	}

	@Override
	protected void initMenuItem(MenuItem menuItem) throws Exception {
		menuItem.setText("MySQL &Simple sql generat");
	}

	@Override
	protected void onClick(SelectionEvent event) throws Throwable {
		SelectInfoDialog dt = new SelectInfoDialog(parentShell);
		dt.setBlockOnOpen(true);
		
		dt.open();
		
		if(dt.getReturnCode()==Dialog.CANCEL)return;
		
		String tableName = dt.tableNames;
		if(tableName.indexOf(".")>-1){
			tableName = tableName.substring(tableName.indexOf(".")+1);
		}
		Map<String,String> fields = Collections.emptyMap();
		StringBuffer sql = new StringBuffer();
		switch (dt.curd) {
		case 0://comboForCURD.add("insert");
			fields = (cols(getConnection(dt.ip, dt.username, dt.password), dt.tableNames));
			sql.append("insert into ")
			.append(tableName).append("(\r\n ");
			for (String col : fields.keySet()) {
				if(col.equalsIgnoreCase("id")){
					continue;
				}
				sql.append(col).append("\r\n,");
			}
			sql.deleteCharAt(sql.length()-1);
			sql.append(") values ( \r\n " );
			
			for (String col : fields.keySet()) {
				if(col.equalsIgnoreCase("id")){
					continue;
				}
				sql.append("#{").append(NameUtils.camelCase(col, false)).append("}").append("\r\n,");
			}
			sql.deleteCharAt(sql.length()-1);
			sql.append(")");
			break;
		case 1://comboForCURD.add("delete");
			sql.append("delete from  ")
			.append(tableName).append(" where id = #{id} ");
			break;
		case 2://	comboForCURD.add("update");
			sql.append("update  ")
			.append(tableName).append(" set \r\n ");
			fields = (cols(getConnection(dt.ip, dt.username, dt.password), dt.tableNames));
			for (String col : fields.keySet()) {
				if(col.equalsIgnoreCase("id")){
					continue;
				}
				sql.append((col)).append(" = #{").append(NameUtils.camelCase(col, false)).append("}").append("\r\n,");
			}
			sql.deleteCharAt(sql.length()-1);
			sql.append(" where id = #{id} ");
			break;
		case 3://	comboForCURD.add("select");
			sql.append("select  ");
			fields = (cols(getConnection(dt.ip, dt.username, dt.password), dt.tableNames));
			for (String col : fields.keySet()) {
				sql.append((col)).append(",");
			}
			sql.deleteCharAt(sql.length()-1);
			sql.append(" from  ").append(tableName);
			break;
		default:
			break;
		}
		
		Clipboard clipboard = new Clipboard(parentShell.getDisplay());  
		clipboard.setContents(new Object[] { sql.toString() },
	              new Transfer[] { TextTransfer.getInstance() }); 
		
	}
	
	private class SelectInfoDialog extends DbInfoDialog {

		private Combo comboForCURD;
		
		public int curd = -1;

		protected SelectInfoDialog(Shell parentShell) {
			super(parentShell);
		}
		
		@Override
		protected Control createDialogArea(Composite parent) {
			Composite container = (Composite) super.createDialogArea(parent);
			new Label(container, SWT.NORMAL).setText(Commons4ePlugin.getResourceString("dialog.selectCURD")); 
			comboForCURD = new Combo(container, SWT.BORDER | SWT.READ_ONLY);
			comboForCURD.add("insert");
			comboForCURD.add("delete");
			comboForCURD.add("update");
			comboForCURD.add("select");
			comboForCURD.select(0);
			return container;
		}
		
		@Override
		protected void okPressed() {
			curd = comboForCURD.getSelectionIndex();
			super.okPressed();
		}
	}
}
