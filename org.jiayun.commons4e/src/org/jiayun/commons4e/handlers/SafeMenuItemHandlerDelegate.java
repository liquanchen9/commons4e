package org.jiayun.commons4e.handlers;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.handlers.HandlerUtil;

public class SafeMenuItemHandlerDelegate extends MenuItemHandler {
	private MenuItemHandler realHandler;

	public SafeMenuItemHandlerDelegate(MenuItemHandler realHandler) {
		this.realHandler = realHandler;
	}

	@Override
	protected boolean init(ExecutionEvent event) {
		try {
			realHandler.parentShell = HandlerUtil.getActiveShell(event);
			return realHandler.init(event);
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	protected void initMenuItem(MenuItem menuItem) {
		try {
			realHandler.initMenuItem(menuItem);
		} catch (Exception e) {
			MessageDialog.openError(realHandler.parentShell, "Error", e.getClass()+"::"+e.getMessage());
		}
		menuItem.addSelectionListener(this);
	}

	@Override
	protected <E extends Throwable> void onClick(SelectionEvent event) throws E {
		try {
			realHandler.onClick(event);
		} catch (Throwable e) {
			MessageDialog.openError(realHandler.parentShell, "Error",  e.getClass()+"::"+e.getMessage());
		}
	}
}