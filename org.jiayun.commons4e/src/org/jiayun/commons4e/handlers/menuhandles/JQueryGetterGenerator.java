package org.jiayun.commons4e.handlers.menuhandles;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.ui.IWorkingCopyManager;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;
import org.jiayun.commons4e.handlers.MenuItemHandler;
import org.jiayun.commons4e.internal.util.JavaUtils;

public class JQueryGetterGenerator extends MenuItemHandler {

	private IType objectClass;

	@Override
	protected void initMenuItem(MenuItem menuItem) {
		menuItem.setText("JQuery &Getter ");
	}

	@Override
	protected boolean init(ExecutionEvent event) throws JavaModelException {
		ISelection currentSelection = HandlerUtil.getCurrentSelection(event);
		if(currentSelection instanceof ITextSelection){
			IEditorPart editor = HandlerUtil.getActiveEditor(event);
			IWorkingCopyManager manager = JavaUI.getWorkingCopyManager();
			ICompilationUnit cu = manager.getWorkingCopy(editor.getEditorInput());
			IType objectClass = null;
			ITextSelection selection = (ITextSelection) currentSelection;
			IJavaElement element = cu.getElementAt(selection.getOffset());
			if (element != null) {
				objectClass = (IType) element.getAncestor(IJavaElement.TYPE);
			}
			if (objectClass == null) {
				objectClass = cu.findPrimaryType();
			}
			this.objectClass = objectClass;
			return true;
		}else if(currentSelection instanceof IStructuredSelection){
			Object firstElement = ((IStructuredSelection)currentSelection).getFirstElement();
			
			if(firstElement==null)return false;
			
			if(firstElement instanceof ICompilationUnit){
				ICompilationUnit cu = (ICompilationUnit)firstElement;
				firstElement = cu.findPrimaryType();
			}
			
			if ( firstElement instanceof IType) {
                IType selected = (IType) firstElement;
                try {
                    if (selected.isClass() && !selected.isReadOnly()) {
                        objectClass = selected;
                        return true;
                    }
                } catch (JavaModelException e) {
                    MessageDialog.openError(parentShell, "Error", e
                            .getMessage());
                }
            }
		}
		return objectClass != null;
	}

	@Override
	protected void onClick(SelectionEvent event) throws PartInitException, JavaModelException  {
		IField[] fields = objectClass.getFields();
		
		for (int i = 0; i < fields.length; i++) {
			String fieldName = fields[i].getElementName();
			if (Flags.isStatic(fields[i].getFlags())
					|| objectClass.getMethod(fieldName,new String[0]).exists()  
					){
				  continue;
			}
			String fieldType = Signature.toString(fields[i].getTypeSignature());
			String formattedContent = JavaUtils.formatCode(parentShell, objectClass,
					"public " + fieldType + " "+fieldName+"(){" + "return this."+fieldName+";}");
			objectClass.createMethod(formattedContent, null, true, null);
			
			formattedContent = JavaUtils.formatCode(parentShell, objectClass,
					"public " + objectClass.getElementName() + " "+fieldName+"("+fieldType+"  "+fieldName+"){this." +fieldName+"="+fieldName+ " ; return this;}");
			objectClass.createMethod(formattedContent, null, true, null);
		}
	}
}