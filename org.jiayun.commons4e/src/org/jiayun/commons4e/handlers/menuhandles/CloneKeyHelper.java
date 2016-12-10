package org.jiayun.commons4e.handlers.menuhandles;

import java.lang.reflect.Field;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.handlers.IHandlerService;
import org.jiayun.commons4e.handlers.MenuItemHandler;

public class CloneKeyHelper extends MenuItemHandler{
	
	private IHandlerService handlerService;
	private Command newEditorCommand;
	
	@Override
	protected boolean init(ExecutionEvent event) throws Exception {
		IWorkbenchWindow workbenchWindow = HandlerUtil.getActiveWorkbenchWindow(event);
		handlerService = workbenchWindow.getService(IHandlerService.class);
		newEditorCommand = workbenchWindow.getService(ICommandService.class).getCommand("org.eclipse.ui.window.newEditor");
		return true;
	}

	@Override
	protected void initMenuItem(MenuItem menuItem) throws Exception {
		menuItem.setText(" editor &clone editor...");
	}

	@Override
	protected void onClick(SelectionEvent event) throws Throwable {
		Event ev = new Event();
		copyFields(event,ev);
		this.handlerService.executeCommand(ParameterizedCommand.generateCommand(newEditorCommand, null), ev);
	}

	private void copyFields(Object src, Object target) {
		Field[] fields = src.getClass().getFields();
		Class<? extends Object> tc = target.getClass();
		for (Field field : fields) {
			try {
				tc.getField(field.getName()).set(target, field.get(src));
			} catch (NoSuchFieldException e) {
			} catch (SecurityException e) {
			} catch (IllegalArgumentException e) {
			} catch (IllegalAccessException e) {}
		}
	}
}
