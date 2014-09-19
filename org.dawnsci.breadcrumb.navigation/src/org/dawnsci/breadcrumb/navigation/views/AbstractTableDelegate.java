/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.breadcrumb.navigation.views;

import java.awt.Desktop;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dawb.common.services.ServiceManager;
import org.dawb.common.services.expressions.IExpressionEngine;
import org.dawb.common.services.expressions.IExpressionService;
import org.dawb.common.ui.actions.ExportTableAction;
import org.dawb.common.ui.menu.CheckableActionGroup;
import org.dawb.common.ui.util.GridUtils;
import org.dawnsci.breadcrumb.navigation.Activator;
import org.dawnsci.breadcrumb.navigation.preference.NavigationConstants;
import org.dawnsci.breadcrumb.navigation.table.AbstractLazyContentProvider;
import org.dawnsci.breadcrumb.navigation.table.AbstractLazyLabelProvider;
import org.dawnsci.breadcrumb.navigation.table.DirectionalIndexedColumnEnum;
import org.dawnsci.breadcrumb.navigation.table.ISortableLazyContentProvider;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.TableColumn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class which encapsulates code for generically adding a table to the visit navigation view.
 * 
 * @author fcp94556
 *
 */
public abstract class AbstractTableDelegate implements INavigationDelegate {
	
	private static final Logger logger = LoggerFactory.getLogger(AbstractTableDelegate.class);

	// The view
	protected AbstractNavigationView     view;

	// UI
	protected TableViewer                 tableViewer;
	protected Map<String, List<Action>>   actions;
	protected AbstractLazyLabelProvider   labelProvider;

	private Composite               contents;
	private Composite               searchComposite;
	private CLabel                  errorLabel;
	private Button                  executeButton;
	private Combo                   searchText;
	
	// Services
	private IExpressionService     expressionService;

	// Other
	private DirectionalIndexedColumnEnum firstColumn;
	private String                       tableId;

	// Jobs
	protected AbstractTableUpdateJob    updateJob;

	
	public AbstractTableDelegate(AbstractNavigationView view, DirectionalIndexedColumnEnum firstColumn, String tableId) {
		
		this.actions       = new HashMap<String,List<Action>>();
		this.view          = view;
		this.firstColumn   = firstColumn;
		this.tableId       = tableId;
		
		this.updateJob = createUpdateJob();
		try {
			this.expressionService = (IExpressionService)ServiceManager.getService(IExpressionService.class);
		} catch (Exception e) {
			logger.error("Cannot get IExpressionService - ISPyB must have an IExpressionService in order to function!", e);
		}
	}
	

	protected abstract AbstractTableUpdateJob createUpdateJob();


	@Override
	public void createContent(String groupLabel, String toolTip, Composite card) {
		
		this.contents = new Composite(card, SWT.NONE);
		contents.setLayout(new GridLayout(1, false));
		GridUtils.removeMargins(contents);
		
		this.searchComposite = new Composite(contents, SWT.NONE);
		searchComposite.setLayout(new GridLayout(3, false));
		searchComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));	
		GridUtils.removeMargins(searchComposite);
		
		searchText = new Combo(searchComposite, SWT.SINGLE | SWT.SEARCH | SWT.ICON_CANCEL);
		searchText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		searchText.setToolTipText("Search on data collection name." );
		boolean isQuery = NavigationConstants.QUERY.equals(Activator.getDefault().getPreferenceStore().getString(NavigationConstants.SEARCH_TYPE));
		if (isQuery) {
			searchText.setToolTipText("Write a query of variables: "+firstColumn.getVariableNames()+" e.g. 'complete>99 and auto eq true'");
		} else {
			searchText.setToolTipText("Wildcard search on name, matches any containing text.");
		}
		
		// TODO Autocomplete not useful
		//final Collection<String> expressionNames = getVariableNames();
		//new AutoCompleteField(searchText, new ComboContentAdapter(), expressionNames.toArray(new String[expressionNames.size()]));
		
		final Collection<String> searches = isQuery ? getPreviousSearches() : null;
		if (searches!=null && !searches.isEmpty()) searchText.setItems(searches.toArray(new String[searches.size()]));
	
		executeButton = new Button(searchComposite, SWT.PUSH);
		executeButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		executeButton.setText("Execute Query");
		executeButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				executeQuery();
			}
		});
		GridUtils.setVisible(executeButton, isQuery);
		
		
		final Link link = new Link(searchComposite, SWT.NONE);
		link.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		link.setText(" ( <a>Syntax</a> ) ");
		link.setToolTipText("When using a query the columns may be searching using inequality expressions.\n"
				           + "When using wildcards the name column is searched for containing text.");
		link.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				boolean isJexl = NavigationConstants.QUERY.equals(Activator.getDefault().getPreferenceStore().getString(NavigationConstants.SEARCH_TYPE));
				if (isJexl) {
					Desktop d = Desktop.getDesktop();
					try {
						d.browse(new URI("http://commons.apache.org/proper/commons-jexl/reference/syntax.html"));
					} catch (Exception e1) {
						logger.error("Problem opening url!",e1);
					}
				} else {
					MessageDialog.openConfirm(Display.getDefault().getActiveShell(), "Help with entering wildcards", "The text you enter is searched in the name column.\nIf that column contains your text, that row is maintained in the list.");
				}
			}
		});

		
		errorLabel = new CLabel(searchComposite, SWT.NONE);
		errorLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		errorLabel.setImage(Activator.getDefault().getImageDescriptor("icons/exclamation.png").createImage());
		GridUtils.setVisible(errorLabel, false);

		this.tableViewer   = new TableViewer(contents, SWT.FULL_SELECTION | SWT.MULTI | SWT.V_SCROLL | SWT.VIRTUAL);
		tableViewer.setUseHashlookup(true);

		tableViewer.getTable().setLinesVisible(true);
		tableViewer.getTable().setHeaderVisible(true);
		tableViewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		tableViewer.setContentProvider(createContentProvider());
		createColumns(tableViewer);

		tableViewer.getTable().addMouseListener(new MouseAdapter() {			
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				final ISelection selection = tableViewer.getSelection();
				if (selection instanceof StructuredSelection) {
					final Object sel = ((StructuredSelection)selection).getFirstElement();
					selectionChanged(sel);
				}
				
			}
		});
		
		
		// Because table is lazy, we do search in funny way.
		searchText.addModifyListener(new ModifyListener() {		
			@Override
			public void modifyText(ModifyEvent e) {
				if (searchText.getParent().isDisposed()) return;
				
				boolean isJexl = NavigationConstants.QUERY.equals(Activator.getDefault().getPreferenceStore().getString(NavigationConstants.SEARCH_TYPE));
				if (isJexl) {

					final String expression = searchText.getText();
					if (expression==null || "".equals(expression)) {
						executeQuery(null);
						GridUtils.setVisible(errorLabel, false);
						errorLabel.getParent().layout();
						return;
					}
					String errMsg = isExpressionValid(expression);
					if (errMsg==null) {
						GridUtils.setVisible(errorLabel, false);
						searchText.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLUE));
					} else {
						errorLabel.setText(errMsg);
						GridUtils.setVisible(errorLabel, true);
						searchText.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_RED));
					}
					errorLabel.getParent().layout();
					// NOTE They have to press enter in proper search mode.
				} else {
					searchText.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
					executeQuery();
				}
				
			}
		});
		
		searchText.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.character=='\n' || e.character=='\r') {
					executeQuery();
				}
			}
		});


	}
	
	/**
	 * Override to do something when the selection changes
	 * @param sel
	 */
	protected void selectionChanged(Object sel) {
		// TODO Auto-generated method stub
		
	}


	protected String isExpressionValid(String expressionString) {
		try {
			final IExpressionEngine engine = expressionService.getExpressionEngine();
			
			//if (engine==null) engine = JexlUtils.getDawnJexlEngine();
			engine.createExpression(expressionString);
			Collection<String> names = engine.getVariableNamesFromExpression();
			
			Collection<String> expressionNames = firstColumn.getVariableNames();
		    for (final String key : names) {
		    	if (!expressionNames.contains(key)) {
		    		return "The name '"+key+"' is invalid. Legal variables are: "+expressionString;
		    	}
			}
			return null;
		} catch (Exception ne) {
    		return "Cannot parse the search '"+expressionString+"'";
		}
	}
	
	private Map<DirectionalIndexedColumnEnum, TableViewerColumn> columnCache;

	protected void createColumns(TableViewer viewer) {
		
		ColumnViewerToolTipSupport.enableFor(viewer,ToolTip.NO_RECREATE);

		this.labelProvider = createLabelProvider();

		// There should be only one of these because it has a worker thread.
		DirectionalIndexedColumnEnum[] columns = firstColumn.allValues();
		
		this.columnCache = new HashMap<DirectionalIndexedColumnEnum, TableViewerColumn>(firstColumn.allValues().length);
		
		for (int i = 0; i < columns.length; i++) {
			
			DirectionalIndexedColumnEnum colEnum = columns[i];
	        TableViewerColumn column = new TableViewerColumn(viewer, SWT.LEFT, i);
	        column.getColumn().setText(colEnum.getName());
	        column.getColumn().setData(colEnum);
	        column.getColumn().setWidth(colEnum.getWidth());
	        column.getColumn().setResizable(colEnum.isExpandable());
	        if (labelProvider!=null) column.setLabelProvider(labelProvider);
	        column.getColumn().addSelectionListener(getSelectionAdapter(column.getColumn(), colEnum));
	        columnCache.put(colEnum, column);
		}
      
	}
	/**
	 * Thread safe
	 * 
	 * @param colEnum
	 * @param isVis
	 */
	public void setColumnsVisible(final boolean isVis, final DirectionalIndexedColumnEnum... enums) {
		for (DirectionalIndexedColumnEnum de : enums) {
			setColumnVisible(de, isVis);
		}
	}
	
	/**
	 * Thread safe
	 * 
	 * @param colEnum
	 * @param isVis
	 */
	public void setColumnVisible(final DirectionalIndexedColumnEnum colEnum, final boolean isVis) {
		
		if (Thread.currentThread()==Display.getDefault().getThread()) {
			setColumnVisibleInternal(colEnum, isVis);
		} else {
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					setColumnVisibleInternal(colEnum, isVis);
				}
			});
		}
	}

	private void setColumnVisibleInternal(final DirectionalIndexedColumnEnum colEnum, boolean isVis) {
		if (isVis) {
			columnCache.get(colEnum).getColumn().setWidth(150);
			columnCache.get(colEnum).getColumn().setResizable(true);
		} else {
			columnCache.get(colEnum).getColumn().setWidth(0);
			columnCache.get(colEnum).getColumn().setResizable(false);
		}
	}


	private SelectionAdapter getSelectionAdapter(final TableColumn column, final DirectionalIndexedColumnEnum colEnum) {
		SelectionAdapter selectionAdapter = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				final ISortableLazyContentProvider visitContent = (ISortableLazyContentProvider)tableViewer.getContentProvider();
				final int direction = visitContent.sort(view, colEnum);
				tableViewer.getTable().getSortColumn();
				tableViewer.getTable().setSortDirection(direction);
				tableViewer.getTable().setSortColumn(column);				
			}
		};
		return selectionAdapter;
	}


    /**
     * Please override this default method which does not provide content.
     * @return
     */
	protected abstract AbstractLazyContentProvider createContentProvider() ;

    /**
     * Please override this default method which does not provide good labels.
     * This is the label provider used on each column.
     * @return
     */
	protected abstract AbstractLazyLabelProvider createLabelProvider();

	
	public boolean isVisible() {
		return tableViewer.getControl().isVisible();
	}
	
	public void refresh() {
		tableViewer.refresh();
	}


	@Override
	public ISelectionProvider getSelectionProvider() {
		return tableViewer;
	}


	public void setActionsActive(boolean isActive, IContributionManager man) {

		for (String groupId : actions.keySet()) {
			final List<Action> as = actions.get(groupId);
			for (Action action : as) {
				if (isActive) {
					if (man.find(action.getId())==null) man.appendToGroup(groupId, action);
				} else {
					if (man.find(action.getId())!=null) man.remove(action.getId());
				}
			}
		}
	}

	@Override
	public Control getControl() {
		return contents;
	}

	
	protected void executeQuery() {
		if (!searchText.isEnabled()) return;
		executeQuery(searchText.getText());
	}
	protected void executeQuery(String query) {
		if (!searchText.isEnabled()) return;
		AbstractLazyContentProvider lcp = (AbstractLazyContentProvider)tableViewer.getContentProvider();
		lcp.setSearchText(view, query);
	}

	public void saveSearch(final String searchString) {
		final List<String> searches = getPreviousSearches();
		final List<String> save     = new ArrayList<String>(searches);
		save.add(0, searchString);
		while(save.size()>20) save.remove(save.size()-1);
		StringBuilder buf = new StringBuilder();
		for (String search : save) {
			buf.append(search);
			buf.append("#SEP#");
		}
		Activator.getDefault().getPreferenceStore().setValue(NavigationConstants.QUERY_HISTORY, buf.toString());
		
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				searchText.setItems(save.toArray(new String[save.size()]));
				searchText.setText(searchString);
				searchText.setFocus();
				int stringLength = searchText.getText ().length (); 
				searchText.setSelection (new Point (stringLength, stringLength));	
			}
		});
	}

	private List<String> getPreviousSearches() {
		final String searchP = Activator.getDefault().getPreferenceStore().getString(NavigationConstants.QUERY_HISTORY);
		if (searchP==null) return null;
		return Arrays.asList(searchP.split("#SEP#"));
	}
	
	public void dispose() {
		if (actions!=null) actions.clear();
		tableViewer.getTable().dispose();
	}
	
	public void setEnabled(boolean enabled) {
		if (searchText.isDisposed()) return;
		searchText.setEnabled(enabled);		
		tableViewer.getTable().setEnabled(enabled);
		executeButton.setEnabled(enabled);
		
		if (enabled) {
			searchText.setFocus();
			final int len = searchText.getText().length();
			searchText.setSelection(new Point(len, len));
			tableViewer.getTable().setCursor(null);
		} else {
			tableViewer.getTable().setCursor(Display.getDefault().getSystemCursor(SWT.CURSOR_WAIT));
		}		
	}
	
	protected void clearSearch() {
		searchText.setText(""); 
	}

	/**
	 * clears the content of the viewer.
	 */
	public void clear() {
		setContent(Collections.emptyList());
	}


	public void createActions(IContributionManager man, IContributionManager menuMan) {
		
		man.add(new Separator("search.group."+tableId));
		menuMan.add(new Separator("search.group."+tableId));
		
		final List<Action> searchActions = new ArrayList<Action>(7);
		actions.put("search.group."+tableId, searchActions);
		
		final CheckableActionGroup grp = new CheckableActionGroup();
		
		// Login, log out, sweep.
		Action nonSearch = new Action("No search", IAction.AS_CHECK_BOX) {
			public void run() {
				Activator.getDefault().getPreferenceStore().setValue(NavigationConstants.SEARCH_TYPE, NavigationConstants.NONE);
			    clearSearch();
				searchText.setToolTipText("No searching required");
				GridUtils.setVisible(searchComposite, false);
				contents.layout();
			}
		};
		nonSearch.setImageDescriptor(Activator.getImageDescriptor("icons/magnifier--minus.png"));
		grp.add(nonSearch);
		searchActions.add(nonSearch);
		nonSearch.setId("search.group.nonSearch."+tableId);
		
		Action searchWildcard = new Action("Search using wildcard", IAction.AS_CHECK_BOX) {
			public void run() {
				Activator.getDefault().getPreferenceStore().setValue(NavigationConstants.SEARCH_TYPE, NavigationConstants.WILD);
			    clearSearch();
				searchText.setToolTipText("Wildcard search on name, matches any containing text.");
				GridUtils.setVisible(searchComposite, true);
				GridUtils.setVisible(executeButton, false);
				contents.layout();
			}
		};
		searchWildcard.setImageDescriptor(Activator.getImageDescriptor("icons/magnifier--arrow.png"));
		grp.add(searchWildcard);
		searchActions.add(searchWildcard);
		searchWildcard.setId("search.group.wildCard."+tableId);

		Action searchJexl = new Action("Search using query", IAction.AS_CHECK_BOX) {
			public void run() {
				Activator.getDefault().getPreferenceStore().setValue(NavigationConstants.SEARCH_TYPE, NavigationConstants.QUERY);
			    clearSearch();
				searchText.setToolTipText("Write a query of variables: "+firstColumn.getVariableNames()+" e.g. 'complete>99 and auto eq true'");
				GridUtils.setVisible(searchComposite, true);
				GridUtils.setVisible(executeButton, true);
				contents.layout();
				
				List<String> searches = getPreviousSearches();
				searchText.setItems(searches.toArray(new String[searches.size()]));
			}
		};
		searchJexl.setImageDescriptor(Activator.getImageDescriptor("icons/magnifier--pencil.png"));
		grp.add(searchJexl);
		searchActions.add(searchJexl);
		searchJexl.setId("search.group.jexl."+tableId);
		

		if (NavigationConstants.NONE.equals(Activator.getDefault().getPreferenceStore().getString(NavigationConstants.SEARCH_TYPE))) {
			GridUtils.setVisible(searchComposite, false);
			nonSearch.setChecked(true);
		} else {
			GridUtils.setVisible(searchComposite, true);
			searchJexl.setChecked(NavigationConstants.QUERY.equals(Activator.getDefault().getPreferenceStore().getString(NavigationConstants.SEARCH_TYPE)));
			searchWildcard.setChecked(NavigationConstants.WILD.equals(Activator.getDefault().getPreferenceStore().getString(NavigationConstants.SEARCH_TYPE)));
		}
		
		man.add(new Separator("export.group"+tableId));
		menuMan.add(new Separator("export.group"+tableId));
		Action export = new ExportTableAction(tableViewer, firstColumn.allValues().length);
		export.setId("export.group.action"+tableId);
		actions.put("export.group"+tableId, Arrays.asList(export));


	}
	
	public void setCursor(Cursor cursor) {
		tableViewer.getTable().setCursor(cursor);		
	}


	/**
	 * Thread safe
	 * @param collections
	 */
	public void setContent(final List<Object> collections) {
		if (Thread.currentThread()==Display.getDefault().getThread()) {
			setContentInternal(collections);
		} else {
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					setContentInternal(collections);
				}
			});
		}
	}
	private void setContentInternal(List<Object> collections) {
		labelProvider.clear();
		tableViewer.setInput(collections);
		tableViewer.setItemCount(collections.size());
		view.setActiveUI(getControl(), getTableMode());
		tableViewer.refresh();	
	}

	
	protected abstract INavigationDelegateMode getTableMode();


	public void schedule(List<Object> selections, Object bean) {
		if (updateJob!=null) {
			updateJob.schedule(selections, bean);
		}
	}


	public String getTableId() {
		return tableId;
	}


	public void setMenu(MenuManager menuMan) {
		tableViewer.getControl().setMenu(menuMan.createContextMenu(tableViewer.getControl()));
	}

}
