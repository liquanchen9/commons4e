package org.jiayun.commons4e.handlers.menuhandles;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.jiayun.commons4e.Commons4ePlugin;

public class DbInfoDialog extends Dialog implements ModifyListener ,SelectionListener{
	private Text txtForIP;
	private Text txtForUsername;
	private Text txtForPassword;
	private Text txtForTableNames;
	private Button btnForDaoService;
	
	public String ip;
	public String username;
	public String password;
	public String tableNames;
	public boolean createDaoService;
	

	protected DbInfoDialog(Shell parentShell) {
		super(parentShell);
	}
	
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		GridLayout layout = new GridLayout();
	    layout.numColumns = 2;
		container.setLayout(layout);
		new Label(container, SWT.NORMAL).setText(Commons4ePlugin.getResourceString("dialog.ip")); 
		txtForIP = new Text(container, SWT.BORDER);
		txtForIP.setText(defaultIp);
		txtForIP.addModifyListener(this);
		new Label(container, SWT.NORMAL).setText(Commons4ePlugin.getResourceString("dialog.username")); 
		txtForUsername = new Text(container, SWT.BORDER);
		txtForUsername.setText(defaultUsername);
		txtForUsername.addModifyListener(this);
		new Label(container, SWT.NORMAL).setText(Commons4ePlugin.getResourceString("dialog.password")); 
		txtForPassword = new Text(container, SWT.BORDER | SWT.PASSWORD);
		txtForPassword.setText(defaultPassword);
		txtForPassword.addModifyListener(this);
	 
		new Label(container, SWT.NORMAL).setText(Commons4ePlugin.getResourceString("dialog.tableNames")); 
		txtForTableNames = new Text(container, SWT.BORDER );
		txtForTableNames.setText(defaultTableNames);
		txtForTableNames.addModifyListener(this);
		
		new Label(container, SWT.NORMAL).setText(Commons4ePlugin.getResourceString("dialog.createDaoService")); 
		btnForDaoService = new Button(container, SWT.CHECK);
		btnForDaoService.addSelectionListener(this);
		return container;
	}
	
	@Override
	protected void okPressed() {
		this.password = txtForPassword.getText();
		this.username = txtForUsername.getText();
		this.ip = txtForIP.getText();
		this.tableNames = txtForTableNames.getText();
		super.okPressed();
	}

	@Override
	public void modifyText(ModifyEvent event) {
		if(event.widget == txtForIP){
			defaultIp = txtForIP.getText();
		}else if(event.widget == txtForUsername){
			defaultUsername = txtForUsername.getText();
		}else if(event.widget == txtForPassword){
			defaultPassword = txtForPassword.getText();
		}else if(event.widget == txtForTableNames){
			defaultTableNames = txtForTableNames.getText();
		}
	}
	
	private static String defaultIp = "localhost";
	private static String defaultUsername = "root";
	private static String defaultPassword = "!Q@W#E4r5t6y";
	private static String defaultTableNames = "sjtu_propagandist";


	public void widgetDefaultSelected(SelectionEvent event) {}
	public void widgetSelected(SelectionEvent event) {
		createDaoService = true;
	}
}