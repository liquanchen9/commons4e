package org.jiayun.commons4e.handlers;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

public abstract class MenuItemHandler implements SelectionListener {
	protected Shell parentShell;

	protected abstract boolean init(ExecutionEvent event) throws Exception;

	protected abstract void initMenuItem(MenuItem menuItem) throws Exception;

	protected abstract <E extends Throwable> void onClick(SelectionEvent event) throws E;

	public final void widgetDefaultSelected(SelectionEvent event) {
		this.<RuntimeException>onClick(event);
	}

	public final void widgetSelected(SelectionEvent event) {
		this.<RuntimeException>onClick(event);
	}

	public final static MenuItemHandler[] getAllHandler() {
		return AllHandlersHolder.ALL;
	}
	
	private static final class AllHandlersHolder{
		private final static MenuItemHandler[] ALL = scanHandlers();
	}
	
	/**
	 * 扫描 这个包下面的menuhandles子包里的 所以类 调用无参数构造方法实例化
	 * @return
	 */
	private static final MenuItemHandler[] scanHandlers(){
		URL packageUrl =(MenuItemHandler.class.getResource("MenuItemHandler.class"));
		String pathUrl = packageUrl.toString();
		String protocol = packageUrl.getProtocol();
		List<Class<? extends MenuItemHandler> > classes = new ArrayList<Class<? extends MenuItemHandler> >();
		
		try {
			String basePath = null;
			//eclipse 加载时就会是 bundleresource
			if("bundleresource".equals(protocol)){
				Object bundleEntry = getPrivateField(packageUrl,"handler.bundleEntry");
				//packageUrl.handler.bundleEntry.basefile//commons4e.jar
				File file = getPrivateField(bundleEntry,"bundleFile.basefile");
				if(file!=null){
					protocol = "jar";
				}else{
					//调试的时候
					//packageUrl.handler.bundleEntry.file;//xxx/MenuItemHandler.class
					protocol = "file";
					file = getPrivateField(bundleEntry,"file");
				}
				basePath = file.getAbsolutePath();
			}else if("jar".equals(protocol)){
				int splitIndex = pathUrl.lastIndexOf('!');
				basePath = (pathUrl.substring(9,splitIndex));
			}else if("file".equals(protocol)){
				basePath = pathUrl.substring(6);
			}
			
			if("jar".equals(protocol)){
				ZipFile zipfile = new ZipFile(basePath);
				Enumeration<? extends ZipEntry> entries = zipfile.entries();
				while (entries.hasMoreElements()) {
					ZipEntry entry = entries.nextElement();
					if(!entry.isDirectory()
							&& entry.getName().startsWith(MenuItemHandler.class.getPackage().getName()
									.replaceAll("\\.", "/")+"/menuhandles/")
							&& entry.getName().endsWith(".class")){
						String className = (entry.getName().substring(0,entry.getName().length()-6).replaceAll("/", "."));
						Class<? extends MenuItemHandler> item = getMenuItemHandlerImplClass(className);
						if(item!=null){
							classes.add(item);
						}		
					}
				}
			}else if("file".equals(protocol) ){
				File  packageDir = new File(basePath).getParentFile();
				for (File classFile : new File(packageDir,"menuhandles").listFiles()) {
					if(classFile.getName().endsWith(".class")){
						String className = (MenuItemHandler.class.getPackage().getName()+".menuhandles."+classFile.getName().substring(0,classFile.getName().length()-6));
						Class<? extends MenuItemHandler> item = getMenuItemHandlerImplClass(className);
						if(item!=null){
							classes.add(item);
						}
					}
				}
			}else {
				MessageDialog.openError(null, "Protocol Error", packageUrl.getProtocol());
			}
		} catch (Throwable e) {
			//e.printStackTrace(getDebugStream());
			//忽略错误
			//MessageDialog.openError(null, "  Error", e.getClass()+":"+e.getMessage());
		}
		MenuItemHandler[] result = new MenuItemHandler[classes.size()];
		int i=0;
		for (Class<? extends MenuItemHandler> menuItemHandlerImplClass : classes) {
			try {
				result[i] = menuItemHandlerImplClass.newInstance();
				i++;
			} catch (Exception e) {
				//e.printStackTrace(getDebugStream());
				//忽略错误
				//MessageDialog.openError(null, " menuItemHandlerImplClass newInstance Error", e.getClass()+":"+e.getMessage());
			}
		}
		if(i!=result.length){
			return Arrays.copyOf(result, i);
		}
		return result;
	}

//	static PrintStream getDebugStream()  {
//		try {
//			return new PrintStream("D:\\mylog.log");
//		} catch (FileNotFoundException e) {
//			return System.err;
//		}
//	}
	
	@SuppressWarnings("unchecked")
	private static final Class<? extends MenuItemHandler> getMenuItemHandlerImplClass(String className) {
		try{
			Class<?> result = Class.forName(className);
			if(result.isInterface() || result.isMemberClass() || result.isAnonymousClass()){
				return null;
			}
			if(result.getConstructor()==null){
				return null;
			}
			if(MenuItemHandler.class.isAssignableFrom(result)){
				return (Class<? extends MenuItemHandler>) result;
			}
		} catch (Throwable e) {
			//e.printStackTrace(getDebugStream());
			//忽略错误
			//MessageDialog.openError(null, " getMenuItemHandlerImplClass Error", e.getClass()+":"+e.getMessage());
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	private static <T> T getPrivateField(Object obj,String path){
		if(obj==null)return null;
		
		int splitIndex = path.indexOf(".");
		if(splitIndex>-1){
			String subPath = path.substring(0, splitIndex);
			String oterPath = path.substring(splitIndex+1);
			return getPrivateField(getPrivateField(obj,subPath),oterPath);
		}
		
		Class<?> cls = obj.getClass();
		Field f = null;
		do{
			try {
				f = cls.getDeclaredField(path);
			} catch (Exception e) {
				cls = cls.getSuperclass();
			}
		}while(f==null && cls != Object.class );
		
		if(f==null)return null;
		
		try {
			f.setAccessible(true);
			return (T) f.get(obj);
		} catch (Exception e) {
			return null;
		}
	}
}