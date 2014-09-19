/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.breadcrumb.navigation.views;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import org.dawb.common.ui.util.GridUtils;
import org.dawb.common.ui.viewers.TreeNodeContentProvider;
import org.dawnsci.common.widgets.breadcrumb.BreadcrumbViewer;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

public abstract class StyledTreeBreadcrumbViewer extends BreadcrumbViewer {

	
	private boolean isOn = true;
	private boolean isPrimaryViewer;
	private Label searchLabel;
	
	public StyledTreeBreadcrumbViewer(Composite parent, int switches) {
		super(parent, switches);
	}
	
	/**
	 * 
	 * @return the implementation of a styled label provider to label the breadcrmbs
	 */
	protected abstract IStyledTreeLabelProvider createBeadcrumbLabelProvider();
	
	/**
	 * Define if a given object is a branch, wether or not it has children.
	 * @param object
	 * @return
	 */
	protected abstract boolean isBranch(Object object);


	@Override
	protected void configureDropDownViewer(final TreeViewer viewer, Object input) {
		
		// We add a label to view the text seaches with
		this.searchLabel = new Label(viewer.getControl().getParent(), SWT.NONE);
		searchLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		GridUtils.setVisible(searchLabel, false);
		searchLabel.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));

		viewer.setContentProvider(new TreeNodeContentProvider((TreeNode)input));
		
		final IStyledTreeLabelProvider lprov = createBeadcrumbLabelProvider();
		viewer.setLabelProvider(new DelegatingStyledCellLabelProvider(lprov));
		viewer.setInput(input);
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			boolean first = true;
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				
				if (!isOn) return;
				try {
					isOn = false;
				    if (first) {
						first = false;
						return;
					}
					DefaultMutableTreeNode sel = (DefaultMutableTreeNode)((StructuredSelection)event.getSelection()).getFirstElement();
					StyledTreeBreadcrumbViewer.this.setInput(sel);
					StyledTreeBreadcrumbViewer.this.setSelection(new StructuredSelection(sel));
					if (!viewer.getControl().isDisposed()) {
						if (!viewer.getControl().getShell().isDisposed()) {
							viewer.getControl().getShell().setVisible(false);
						}
					}
					
					boolean isBranch = isBranch(sel.getUserObject());
					if (sel.getChildCount()>0 && isBranch) {
						StyledTreeBreadcrumbViewer.this.showMenu();
					}
				} finally {
					isOn = true;
				}
			}
		});
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				Object sel = ((StructuredSelection)viewer.getSelection()).getFirstElement();
				StyledTreeBreadcrumbViewer.this.setInput(sel);
				StyledTreeBreadcrumbViewer.this.setSelection(new StructuredSelection(sel));
				if (!viewer.getControl().isDisposed()) {
					if (!viewer.getControl().getShell().isDisposed()) {
						viewer.getControl().getShell().setVisible(false);
					}
				}
			}
		});
		
		
		/**
		 * This listener is rather complex. It gives filtering
		 * by search key and pressing enter to make the breadcrumb
		 * like a context proposing text field.
		 */
		viewer.getControl().addKeyListener(new KeyListener() {
			
			private Job          textBufferJob;
			private StringBuffer textBuffer = new StringBuffer();
			Job getJob() {
				if (textBufferJob!=null) return textBufferJob;
				textBufferJob = new Job("Text Buffer") {
	
					public IStatus run(IProgressMonitor monitor) {
						if (monitor.isCanceled()) return Status.CANCEL_STATUS;
						textBuffer.delete(0, textBuffer.length());
						Display.getDefault().syncExec(new Runnable() {
							public void run() {
								if (searchLabel.isDisposed() || searchLabel.getParent().isDisposed()) return;
								GridUtils.setVisible(searchLabel, false);
								searchLabel.setText("");
								searchLabel.getParent().layout();
								if (!viewer.getControl().isDisposed()) {
									viewer.getControl().setFocus();
								}
							}
						});
						return Status.OK_STATUS;
					}
				};
				textBufferJob.setPriority(Job.INTERACTIVE);
				textBufferJob.setSystem(true);
				return textBufferJob;
			}
			
			@Override
			public void keyReleased(KeyEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
				
				if (e.character=='\r') {
					TreeNode child  = (TreeNode)((StructuredSelection)viewer.getSelection()).getFirstElement();
					if (child!=null) {
						setSelection(new StructuredSelection(child));
						Display.getDefault().asyncExec(new Runnable() {
							public void run() {
								final Object node = ((StructuredSelection)getSelection()).getFirstElement();
								if (isBranch(node)) showMenu();
							}
						});
					}
					return;
				}
				
				getJob().cancel();
				try {
					
					if (!isOn) return;
					isOn   = false;
				    e.doit = false;

				    switch (e.keyCode) {
					case SWT.HOME:
					case SWT.END:
					case SWT.PAGE_UP:
					case SWT.PAGE_DOWN:
					case SWT.ARROW_UP:
					case SWT.ARROW_DOWN:
						position(viewer, e.keyCode);
						return;
					}

				    if (e.character=='\b') {
				    	if (textBuffer.length()>0) textBuffer.deleteCharAt(textBuffer.length()-1);
				    } else {
				        textBuffer.append(e.character);
				    }

				    
				    TreeNode           child  = (TreeNode)((StructuredSelection)viewer.getSelection()).getFirstElement();
				    TreeNode           parent = child!=null ? (TreeNode)child.getParent() : null;
					if (child==null && parent==null) return;
					
					Map<String, Object> uObs   = getUserObjects(lprov, parent);
					for (String key : uObs.keySet()) {
						
						Object node = uObs.get(key);
						if (key.toLowerCase().contains(textBuffer.toString())) {
							viewer.setSelection(new StructuredSelection(node));							
							return;
						}
					}
						
				} finally {
					isOn=true;
					getJob().schedule(2000);
					
					if (searchLabel.isDisposed() || searchLabel.getParent().isDisposed()) return;
					GridUtils.setVisible(searchLabel, textBuffer.length()>0);
					searchLabel.setText(textBuffer.toString());
					searchLabel.getParent().layout();
				}
			}
		});
	}
	
	protected void position(TreeViewer viewer, int keyCode) {
		
		TreeNode           child  = (TreeNode)((StructuredSelection)viewer.getSelection()).getFirstElement();
		TreeNode           parent = child!=null ? (TreeNode)child.getParent() : null;
		if (child==null && parent==null) return;
		
		if (keyCode==SWT.HOME) {
			viewer.setSelection(new StructuredSelection(parent.getChildAt(0)));	
			return;
		} else if  (keyCode == SWT.END) {
			viewer.setSelection(new StructuredSelection(parent.getChildAt(parent.getChildCount()-1)));	
			return;
		}

		int index = parent.getIndex(child);
		if(keyCode==SWT.ARROW_UP && index>0) {
			viewer.setSelection(new StructuredSelection(parent.getChildAt(index-1)));
			return;
		}else if(keyCode==SWT.ARROW_DOWN && index<parent.getChildCount()-1) {
			viewer.setSelection(new StructuredSelection(parent.getChildAt(index+1)));
			return;
		}
	}

	protected Map<String, Object> getUserObjects(IStyledTreeLabelProvider prov, TreeNode parent) {
		
		if (parent.getChildCount()<1) return null;
		final Map<String, Object> ret = new LinkedHashMap<String, Object>(parent.getChildCount());
		for (int i = 0; i <parent.getChildCount(); i++) {
			final Object node  = parent.getChildAt(i);
			final String label = prov.getText(node);
			ret.put(label, node);
		}
		return ret;
	}


	public boolean isPrimaryViewer() {
		return isPrimaryViewer;
	}

	public void setPrimaryViewer(boolean isPrimaryViewer) {
		this.isPrimaryViewer = isPrimaryViewer;
	}

}
