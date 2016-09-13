package org.jiayun.commons4e.handlers.menuhandles;

import java.awt.Robot;
import java.awt.event.KeyEvent;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.MenuItem;
import org.jiayun.commons4e.handlers.MenuItemHandler;

public class CloneKeyHelper extends MenuItemHandler{

	@Override
	protected boolean init(ExecutionEvent event) throws Exception {
		return true;
	}

	@Override
	protected void initMenuItem(MenuItem menuItem) throws Exception {
		menuItem.setText(" editor &clone editor...");
	}

	@Override
	protected void onClick(SelectionEvent event) throws Throwable {
		Thread.sleep(200);
		java.awt.Robot robot = new Robot();
		robot.keyPress(KeyEvent.VK_ALT);
		robot.keyRelease(KeyEvent.VK_ALT);
		Thread.sleep(80);
		robot.keyPress(KeyEvent.VK_W);
		Thread.sleep(80);
		robot.keyRelease(KeyEvent.VK_W);
		robot.keyPress(KeyEvent.VK_E);
		Thread.sleep(80);
		robot.keyRelease(KeyEvent.VK_E);
		robot.keyPress(KeyEvent.VK_E);
		Thread.sleep(80);
		robot.keyRelease(KeyEvent.VK_E);
		
	}
}
