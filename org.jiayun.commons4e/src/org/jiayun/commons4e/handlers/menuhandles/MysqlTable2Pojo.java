package org.jiayun.commons4e.handlers.menuhandles;

import static org.jiayun.commons4e.internal.util.JdbcUtils.cols;
import static org.jiayun.commons4e.internal.util.JdbcUtils.getConnection;
import static org.jiayun.commons4e.internal.util.NameUtils.camelCase;

import java.io.File;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.handlers.HandlerUtil;
import org.jiayun.commons4e.handlers.MenuItemHandler;

public class MysqlTable2Pojo extends MenuItemHandler {
	private IPackageFragment packageFragment;

	@Override
	protected boolean init(ExecutionEvent event) throws Exception {
		ISelection selection = HandlerUtil.getActiveSite(event).getSelectionProvider().getSelection();
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection structuredSelection = (IStructuredSelection) selection;
			Object firstElement = structuredSelection.getFirstElement();
			if (firstElement == null)
				return false;
		 
			if (firstElement instanceof IPackageFragment) {

				this.packageFragment = (IPackageFragment) firstElement;

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
		javaCode.append("package ").append(packageFragment.getElementName()).append(";\r\n")
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
		packageFragment.createCompilationUnit(tableName+".java", javaCode.toString(), false, null);
		
		if(!td.createDaoService){return;}
		
		String basePackage = packageFragment.getElementName();
		basePackage= basePackage.substring(0,basePackage.lastIndexOf('.'));
		
		IPackageFragmentRoot root = findRoot(packageFragment);
		
		//创建dao接口
		IPackageFragment daoPackage = root.getPackageFragment(basePackage+".persistence");
		if(daoPackage==null||!daoPackage.exists()){
			daoPackage = root.createPackageFragment(basePackage+".persistence", true, null);
		}
		StringBuffer daoInterfaceJavaCode = new StringBuffer();
		daoInterfaceJavaCode
				.append("package ").append(daoPackage.getElementName()).append(";\r\n")
				.append("public interface ").append(tableName).append("Mapper {\r\n")
				.append("}");
		daoPackage.createCompilationUnit(tableName+"Mapper.java", daoInterfaceJavaCode.toString(), false, null);

		//查找xml文件目录
		IPackageFragmentRoot[] all = (root.getJavaProject().getAllPackageFragmentRoots());
		for (IPackageFragmentRoot packageFragmentRoot : all) {
			if(packageFragmentRoot.isReadOnly()){
				continue;
			}
			IClasspathEntry classpathEntry = packageFragmentRoot.getRawClasspathEntry();
			if(classpathEntry.getEntryKind()!=IClasspathEntry.CPE_SOURCE){
				continue;
			}
			IPath projectPath = root.getJavaProject().getResource().getLocation();
			File hasDaoPackageDir = new File(projectPath.toFile(),".."+new File(classpathEntry.getPath().toFile(),daoPackage.getElementName().replaceAll("\\.", "/")).getPath());
			IPath[] exclusionPatterns = classpathEntry.getExclusionPatterns();
			if(hasDaoPackageDir.exists() && exclusionPatterns.length==1 && exclusionPatterns[0].toString().equals("**")){
				IPackageFragment dir = packageFragmentRoot.getPackageFragment(daoPackage.getElementName());
				StringBuffer daoInterfaceXmlCode = new StringBuffer();
				daoInterfaceXmlCode
						.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n")
						.append("<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\" \"http://mybatis.org/dtd/mybatis-3-mapper.dtd\">")
						.append("<mapper namespace=\"").append(daoPackage.getElementName()).append(".").append(tableName).append("Mapper\">")
						.append("</mapper>");
				PrintWriter w = new PrintWriter(new File(hasDaoPackageDir,tableName+"Mapper.xml"), "UTF-8");
				w.println(daoInterfaceXmlCode.toString());
				w.flush();
				w.close();
				dir.getResource().refreshLocal(IResource.DEPTH_INFINITE, null);
				break;
			}
		}
		
		//创建service接口
		IPackageFragment servicePackage = root.getPackageFragment(basePackage+".service");
		if(servicePackage==null||!servicePackage.exists()){
			servicePackage = root.createPackageFragment(basePackage+".service", true, null);
		}
		StringBuffer serviceInterfaceJavaCode = new StringBuffer();
		serviceInterfaceJavaCode
				.append("package ").append(servicePackage.getElementName()).append(";\r\n")
				.append("public interface ").append(tableName).append("Service {\r\n")
				.append("}");
		servicePackage.createCompilationUnit(tableName+"Service.java", serviceInterfaceJavaCode.toString(), false, null);
		
		//创建serviceImpl类
		StringBuffer serviceImplJavaCode = new StringBuffer();
		serviceImplJavaCode
				.append("package ").append(servicePackage.getElementName()).append(";\r\n\r\n")
				.append("import org.springframework.beans.factory.annotation.Autowired;\r\n")
				.append("import org.springframework.stereotype.Service;\r\n")
				.append("import ").append(daoPackage.getElementName()).append(".").append(tableName).append("Mapper;\r\n")
				.append("\r\n@Service\r\n")
				.append("public class ").append(tableName).append("ServiceImpl implements ").append(tableName).append("Service {\r\n")
				.append("\t@Autowired private ").append(tableName).append("Mapper ").append(camelCase(tableName, false)).append(";\r\n")
				.append("}");
		servicePackage.createCompilationUnit(tableName+"ServiceImpl.java", serviceImplJavaCode.toString(), false, null);
	}
	
	/**
	 * 获取root
	 * @param packageFragment
	 * @return
	 */
	public IPackageFragmentRoot findRoot(IPackageFragment packageFragment){
		IJavaElement r = null;
		while((r = packageFragment.getParent())!=null){
			if(r.equals(packageFragment)){
				return null;
			} else if(r instanceof IPackageFragmentRoot){
				return (IPackageFragmentRoot) r;
			}
		}
		return null;
	}
	
}
