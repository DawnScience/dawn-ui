package org.dawnsci.processing.ui.fileactions;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionContext;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.actions.DeleteResourceAction;
import org.eclipse.ui.actions.TextActionHandler;
import org.eclipse.ui.ide.ResourceSelectionUtil;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.ICommonActionConstants;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.eclipse.ui.navigator.ICommonMenuConstants;
import org.eclipse.ui.navigator.ICommonViewerSite;
import org.eclipse.ui.navigator.ICommonViewerWorkbenchSite;

public class ProcessingActionProvider extends CommonActionProvider {
	
	OpenAction openAction;
	EditActionGroup editGroup;
	
	public ProcessingActionProvider() {
		
	}
	
	public void init(ICommonActionExtensionSite aSite) {
		ICommonViewerSite viewSite = aSite.getViewSite();
		
		if (viewSite instanceof ICommonViewerWorkbenchSite) {
			ICommonViewerWorkbenchSite workbenchSite = (ICommonViewerWorkbenchSite) viewSite;
			openAction = new OpenAction(workbenchSite.getPage(),workbenchSite.getSelectionProvider());
			editGroup = new EditActionGroup(aSite.getViewSite().getShell());
		}
	}
	
	public void fillActionBars(IActionBars actionBars) {
		
		if (openAction.isEnabled()) {
			actionBars.setGlobalActionHandler(ICommonActionConstants.OPEN, openAction);
		}
		editGroup.fillActionBars(actionBars);
		
	}
	
	public void fillContextMenu(IMenuManager menu) {
		if (openAction.isEnabled()) {
			//menu.appendToGroup(ICommonActionConstants.OPEN, openAction);
		}
		editGroup.fillContextMenu(menu);
	}
	
	public void setContext(ActionContext context) { 
		editGroup.setContext(context);
	}
	
	public class EditActionGroup extends ActionGroup {

		private DeleteResourceAction deleteAction;

		private TextActionHandler textActionHandler;

		private Shell shell;

		/**
		 * 
		 * @param aShell
		 */
		public EditActionGroup(Shell aShell) {
			shell = aShell;
			makeActions();
		}


		public void fillContextMenu(IMenuManager menu) {
			IStructuredSelection selection = (IStructuredSelection) getContext().getSelection();

			boolean anyResourceSelected = !selection.isEmpty()
					&& ResourceSelectionUtil.allResourcesAreOfType(selection, IResource.PROJECT | IResource.FOLDER | IResource.FILE);


			if (anyResourceSelected) {
				deleteAction.selectionChanged(selection);
				// menu.insertAfter(pasteAction.getId(), deleteAction);
				if (menu.find(deleteAction.getId()) == null) menu.appendToGroup(ICommonMenuConstants.GROUP_EDIT, deleteAction);
				
			}
		}

		public void fillActionBars(IActionBars actionBars) {

			if (textActionHandler == null) {
				textActionHandler = new TextActionHandler(actionBars); // hook
																		// handlers
			}
			textActionHandler.setDeleteAction(deleteAction);
			updateActionBars();

			textActionHandler.updateActionBars();
		}

		/**
		 * Handles a key pressed event by invoking the appropriate action.
		 * 
		 * @param event
		 *            The Key Event
		 */
		public void handleKeyPressed(KeyEvent event) {
			if (event.character == SWT.DEL && event.stateMask == 0) {
				if (deleteAction.isEnabled()) {
					deleteAction.run();
				}

				// Swallow the event.
				event.doit = false;
			}
		}

		protected void makeActions() {

			ISharedImages images = PlatformUI.getWorkbench().getSharedImages();

			IShellProvider sp = new IShellProvider() {
				public Shell getShell() {
					return shell;
				}
			};

			deleteAction = new DeleteResourceAction(sp);
			deleteAction.setDisabledImageDescriptor(images.getImageDescriptor(ISharedImages.IMG_TOOL_DELETE_DISABLED));
			deleteAction.setImageDescriptor(images.getImageDescriptor(ISharedImages.IMG_TOOL_DELETE));
			deleteAction.setActionDefinitionId(IWorkbenchCommandConstants.EDIT_DELETE);
		}

		public void updateActionBars() {
			IStructuredSelection selection = (IStructuredSelection) getContext().getSelection();
			deleteAction.selectionChanged(selection);
		}
	}
}
