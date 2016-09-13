package org.jiayun.commons4e.handlers.menuhandles;

import static org.jiayun.commons4e.internal.util.JdbcUtils.cols;
import static org.jiayun.commons4e.internal.util.JdbcUtils.getConnection;
import static org.jiayun.commons4e.internal.util.NameUtils.camelCase;

import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.handlers.HandlerUtil;
import org.jiayun.commons4e.handlers.MenuItemHandler;

public class MysqlTable2Pojo extends MenuItemHandler {
	private IPackageFragment packagFragment;

	@Override
	protected boolean init(ExecutionEvent event) throws Exception {
		ISelection selection = HandlerUtil.getActiveSite(event).getSelectionProvider().getSelection();
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection structuredSelection = (IStructuredSelection) selection;
			Object firstElement = structuredSelection.getFirstElement();
			if (firstElement == null)
				return false;

			if (firstElement instanceof IPackageFragment) {

				this.packagFragment = (IPackageFragment) firstElement;

				return true;
			}
		}
		return false;
	}

	@Override
	protected void initMenuItem(MenuItem menuItem) throws Exception {
		menuItem.setText("db&2pojo...");
	}

	@Override
	protected void onClick(SelectionEvent event) throws Throwable {
		DbInfoDialog td = new DbInfoDialog(parentShell);
		td.setBlockOnOpen(true);
		td.open();
		if (td.getReturnCode() != DbInfoDialog.OK) {
			return;
		}
		// packagFragment.createCompilationUnit(arg0, arg1, arg2, arg3)
		
		String tableName = td.tableNames;
		Map<String,String> fields = (cols(getConnection(td.ip, td.username, td.password), tableName));
		
		if(tableName.indexOf(".")>-1){
			tableName = tableName.substring(tableName.indexOf(".")+1);
		}
		
		tableName = camelCase(tableName,true);
		
		StringBuffer javaCode = new StringBuffer();
		javaCode.append("package ").append(packagFragment.getElementName()).append(";\r\n")
				.append("import java.io.Serializable;\r\n")
				.append("public class ").append(tableName).append(" implements Serializable{\r\n")
				.append("\tprivate static final long serialVersionUID = 1L;\r\n");
		for (Entry<String, String> entry : fields.entrySet()) {
			javaCode.append("\tprivate ").append(entry.getValue()).append(" ").append(camelCase(entry.getKey(), false)).append(";\r\n");
		}
		//getter setter
		for (Entry<String, String> entry : fields.entrySet()) {
			javaCode.append("\tpublic ").append(entry.getValue()).append(" get").append(camelCase(entry.getKey(), true)).append("() {\r\n");
			javaCode.append("\t\treturn ").append(camelCase(entry.getKey(), false)).append(";\r\n");
			javaCode.append("\t}").append("\r\n");
			javaCode.append("\tpublic void set").append(camelCase(entry.getKey(), true)).append("(").append(entry.getValue()).append(" ").append(camelCase(entry.getKey(), false)).append(") {\r\n");
			javaCode.append("\t\tthis.").append(camelCase(entry.getKey(), false)).append("=").append(camelCase(entry.getKey(), false)).append(";\r\n");
			javaCode.append("\t}").append("\r\n");
		}
		javaCode.append("}");
		packagFragment.createCompilationUnit(tableName+".java", javaCode.toString(), false, null);
	}
}
