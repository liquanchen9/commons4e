package org.jiayun.commons4e.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * 
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class SampleHandler extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		Shell shell = HandlerUtil.getActivePart(event).getSite().getShell();
		Menu menu = new Menu(shell);
		MenuItemHandler[]  handler = MenuItemHandler.getAllHandler();
		int count = 0;
		for (int i = 0; i < handler.length; i++) {
			SafeMenuItemHandlerDelegate generator = new SafeMenuItemHandlerDelegate(handler[i]);
			if (generator.init(event)) {
				generator.initMenuItem(new MenuItem(menu, SWT.PUSH));
				count++;
			}
		}
		if(count==0)return null;
		menu.setVisible(true);
		return null;
	}
}
