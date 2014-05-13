package org.dawnsci.breadcrumb.navigation.views;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import org.dawb.common.ui.menu.CheckableActionGroup;
import org.dawb.common.ui.preferences.ViewConstants;
import org.dawb.common.ui.util.GridUtils;
import org.dawb.common.ui.viewers.TreeNodeContentProvider;
import org.dawb.common.ui.views.CompositeSelectionProvider;
import org.dawnsci.breadcrumb.navigation.Activator;
import org.dawnsci.breadcrumb.navigation.preference.NavigationConstants;
import org.dawnsci.breadcrumb.navigation.table.AbstractLazyLabelProvider;
import org.dawnsci.breadcrumb.navigation.table.ISortParticipant;
import org.dawnsci.common.widgets.action.SpacerContributionItem;
import org.dawnsci.common.widgets.breadcrumb.BreadcrumbViewer;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The main breadcrumb view allowing people to 
 * click their way down to the data collection required.
 * 
 * This view is the entry to navigating the visit. 
 * 
 * @author fcp94556
 *
 */
public abstract class AbstractNavigationView extends ViewPart implements ISelectionChangedListener, ISortParticipant {
	
	protected static final Logger logger = LoggerFactory.getLogger(AbstractNavigationView.class);

	// UI
	protected Map<INavigationDelegateMode, INavigationDelegate> pages;

	private List<StyledTreeBreadcrumbViewer>  bviewers;
	private Composite               crumbs;

	private StackLayout             contextStack;
	private Composite               card;
	
	private INavigationDelegateMode navigationMode;
 
	// Services
	protected IDataSourceManager connectionManager;
	
	
	public AbstractNavigationView() {
		this.bviewers          = new ArrayList<StyledTreeBreadcrumbViewer>(3);
		this.connectionManager = createConnectionManager();
		this.pages             = createPages();
		this.navigationMode    = getDefaultNavigationMode();
	}
	
	/**
	 * The mode which should be the table delegate by default
	 * @return
	 */
	protected abstract INavigationDelegateMode getDefaultNavigationMode();

	/**
	 * The various table delegate pages which will be the content of the view.
	 * @return
	 */
	protected abstract Map<INavigationDelegateMode, INavigationDelegate> createPages();

	/**
	 * The connection manager which mananges connection to the data source.
	 * @return
	 */
	protected abstract IDataSourceManager createConnectionManager();
	
	/**
	 * 
	 * @return the label provider for the breadcrumbs
	 */
	protected abstract IStyledTreeLabelProvider createBeadcrumbLabelProvider();

	/**
	 * Please implement to return your implementation of the StyledTreeBreadcrumbViewer
	 * @param container
	 * @param horizontal
	 * @return
	 */
	protected abstract StyledTreeBreadcrumbViewer createBreadcrumbViewer( Composite container, int horizontal);

	/**
	 * The preference page for the connection or null if there is not one.
	 * @return
	 */
	protected abstract String getPreferencePageId();
	
	@Override
	public void createPartControl(Composite ancestor) {
		createUI(ancestor);
		createActions();
		connectionManager.connect();
		
		setActiveUI(pages.get(navigationMode).getControl(), null);
	}

	private void createUI(Composite ancestor) {
		
		final Composite parent = new Composite(ancestor, SWT.NONE);
		parent.setLayout(new GridLayout(1, false));
		GridUtils.removeMargins(parent);

		// A Breadcrumb similar to eclipses one, which is cool.
		this.crumbs = new Composite(parent, SWT.NONE);
		crumbs.setLayout(new GridLayout(1, false));
		crumbs.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		GridUtils.removeMargins(crumbs);
		
		// Adds the primary viewer to the view. Others may be added to compare but these
		// can be removed again. This primary one cannot.
		StyledTreeBreadcrumbViewer bviewer = createBreadcrumbViewer(crumbs, false);
		bviewers.add(bviewer);
	
		this.card = new Composite(parent, SWT.NONE);
		this.contextStack = new StackLayout();
		card.setLayout(contextStack);
		card.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		// Create all the tables on the card.
		for (INavigationDelegateMode mode : pages.keySet()) {
			final INavigationDelegate vtp = pages.get(mode);
			vtp.createContent("", mode.getTooltip(), card);
		}
		
		// We create a compound selection provider that can 
		// amalgamate the two sources of selection.
		final CompositeSelectionProvider prov = new CompositeSelectionProvider();
		for (INavigationDelegateMode mode : navigationMode.allValues()) {
			if (pages.containsKey(mode)) {
				prov.addProvider(pages.get(mode).getSelectionProvider());
			}
		}
		getViewSite().setSelectionProvider(prov);

	}

	private StyledTreeBreadcrumbViewer createBreadcrumbViewer(final Composite crumbs, boolean isSecondaryViewer) {
		
		final StyledTreeBreadcrumbViewer bviewer;
		if (isSecondaryViewer) {
			final Composite container = new Composite(crumbs, SWT.NONE);
			container.setLayout(new GridLayout(2, false));
			container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			
			GridUtils.removeMargins(container);
		    bviewer = createBreadcrumbViewer(container, SWT.HORIZONTAL);
		    bviewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		    
		    final ToolBar toolbar = new ToolBar(container, SWT.FLAT);
		    toolbar.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		    
		    final ToolItem  remove = new ToolItem(toolbar, SWT.PUSH);
		    remove.setImage(Activator.getImageDescriptor("icons/ui-tooltip--minus.png").createImage());
		    remove.setToolTipText("Remove this breadcrumb from the combined searach.");
		    
		    crumbs.layout(new Control[]{container});
		    crumbs.getParent().layout();
		    
		    remove.addSelectionListener(new SelectionAdapter() {
		    	public void widgetSelected(SelectionEvent e) {
		    		removeSelectedBreadcrumb(container, bviewer);
		    	}
		    });
		} else {
		    bviewer = createBreadcrumbViewer(crumbs, SWT.HORIZONTAL);
		    bviewer.setPrimaryViewer(true);
		}
		
		ITreeContentProvider prov = new TreeNodeContentProvider();
		bviewer.setLabelProvider(createBeadcrumbLabelProvider());
		bviewer.setToolTipLabelProvider(new ColumnLabelProvider());
		bviewer.setContentProvider(prov);
		
		bviewer.addSelectionChangedListener(this);
		
		return bviewer;
	}
	
    /**
     * You may override this to do more work when a breadcrub is removed.
     * @param container
     * @param bviewer
     */
	protected void removeSelectedBreadcrumb(final Composite container, StyledTreeBreadcrumbViewer bviewer) {
		bviewers.remove(bviewer);
		GridUtils.setVisible(container, false);
	    crumbs.layout(new Control[]{container});
	    crumbs.getParent().layout();
	    container.dispose();
	    
	    selectionChanged(null);
	}
	
	protected int getBreadcrumbCount() {
		return bviewers.size();
	}

	@Override
	public void saveSearch(final String searchString) {
    	AbstractTableDelegate dele = (AbstractTableDelegate)pages.get(navigationMode);
    	dele.saveSearch(searchString);
	}

	@Override
	public void setEnabled(final boolean enabled) {
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				final ToolBarManager man = (ToolBarManager)getViewSite().getActionBars().getToolBarManager();
				man.getControl().setEnabled(enabled);
				
				for (BreadcrumbViewer bviewer : bviewers) bviewer.getControl().setEnabled(enabled);
			
				for(INavigationDelegateMode mode : pages.keySet()) {
					pages.get(mode).setEnabled(enabled);
				}
			}
		});
	}	
	
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		init(site);
		connectionManager.init(memento);
	}

	
	protected void createActions() {
		
		final IToolBarManager man     = getViewSite().getActionBars().getToolBarManager();
		final MenuManager     menuMan = new MenuManager();
		
		man.add(new Separator("breadcrumb.group"));
		menuMan.add(new Separator("breadcrumb.group"));
		
		Action add = new Action("Add another breadcrumb search", Activator.getImageDescriptor("icons/ui-tooltip--plus.png")) {
			public void run() {
				addBreadcrumb();
			}
		};
		man.add(add);
		menuMan.add(add);
		
		man.add(new Separator("refresh.group"));
		menuMan.add(new Separator("refresh.group"));

		Action refresh = new Action("Refresh table", Activator.getImageDescriptor("icons/refresh_16x16.png")) {
			public void run() {
                refreshDataCollections();
			}
		};
		man.add(refresh);
		
		refresh = new Action("Refresh connection", Activator.getImageDescriptor("icons/refresh_red.png")) {
			public void run() {
				boolean ok = MessageDialog.openConfirm(Display.getDefault().getActiveShell(), "Confirm Full Refresh", "Are you sure that you would like to do a complete refresh?\n\nThis will loose your selected visit(s) and ask you to start again.");
				if (!ok) return;
				connectionManager.logoff();
				connectionManager.connect();
			}
		};
		man.add(refresh);
		menuMan.add(refresh);

		// Actions to search data collection table
		for(INavigationDelegateMode mode : pages.keySet()) {
			INavigationDelegate delegate = pages.get(mode);
			if (delegate instanceof AbstractTableDelegate) {
			    ((AbstractTableDelegate)delegate).createActions(getViewSite().getActionBars().getToolBarManager(), menuMan);
			}
		}
        
        // Actions to log in and log out
		connectionManager.createLogActions(getViewSite().getActionBars().getToolBarManager());
		
		man.add(new Separator("preference.group"));
		menuMan.add(new Separator("preference.group"));

		if (getPreferencePageId()!=null) {
			Action ispyPref = new Action("Preferences... (Connection and polling)", Activator.getImageDescriptor("icons/data.gif")) {
				@Override
				public void run() {
					PreferenceDialog pref = PreferencesUtil.createPreferenceDialogOn(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), getPreferencePageId(), null, null);
					if (pref != null) pref.open();
				}
			};
			man.add(ispyPref);
			getViewSite().getActionBars().getMenuManager().add(ispyPref);
		}

		Action prefs = new Action("Image Preferences... (For data collection images in gallery)", Activator.getImageDescriptor("icons/data.gif")) {
			@Override
			public void run() {
				PreferenceDialog pref = PreferencesUtil.createPreferenceDialogOn(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), ViewConstants.PAGE_ID, null, null);
				if (pref != null) pref.open();
			}
		};
		
		getViewSite().getActionBars().getMenuManager().add(prefs);
		menuMan.add(prefs);

	    man.add(new SpacerContributionItem(50));
		
		final CheckableActionGroup grp = new CheckableActionGroup();
		for (final INavigationDelegateMode mode : getDefaultNavigationMode().allValues()) {
			if (mode.isInToolbar()) {
				final Action action = new Action(mode.getLabel(), IAction.AS_CHECK_BOX) {
					public void run() {
						navigationMode = mode;
						final INavigationDelegate prov = pages.get(mode);
						if (prov!=null) {
							setActiveUI(prov.getControl(), mode);
							selectionChanged(null);
							// TODO Might want to leave selection so that do not loose plots one day.
							prov.getSelectionProvider().setSelection(prov.getSelectionProvider().getSelection());
						}
					}
				};
				action.setId(mode.getId());
				action.setImageDescriptor(mode.getIcon());
				
				final ActionContributionItem item = new ActionContributionItem(action);
				if (Activator.getDefault().getPreferenceStore().getBoolean(NavigationConstants.SHOW_MODE_LABEL)) {
					item.setMode(ActionContributionItem.MODE_FORCE_TEXT);
				}
				man.add(item);
				grp.add(action);
				action.setChecked(navigationMode==mode);
			}
		}
		man.add(new Separator("tablemode.group"));
		menuMan.add(new Separator("tablemode.group"));
		 		
		Activator.getDefault().getPreferenceStore().addPropertyChangeListener(new IPropertyChangeListener() {
			
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				if (NavigationConstants.SHOW_MODE_LABEL.equals(event.getProperty())) {
					boolean isShowLabel = Activator.getDefault().getPreferenceStore().getBoolean(NavigationConstants.SHOW_MODE_LABEL);
					int modeCode = isShowLabel ? ActionContributionItem.MODE_FORCE_TEXT : 0;
					for (final INavigationDelegateMode mode : getDefaultNavigationMode().allValues()) {
						final IContributionItem item = man.find(mode.getId());
						if (item instanceof ActionContributionItem) {
							((ActionContributionItem)item).setMode(modeCode);
						}
					}
					man.update(true);
				}	
			}
		});

		for(INavigationDelegateMode mode : pages.keySet()) {
			INavigationDelegate delegate = pages.get(mode);
			if (delegate instanceof AbstractTableDelegate) {
			    ((AbstractTableDelegate)delegate).setMenu(menuMan);
			}
		}

	}


	protected boolean addBreadcrumb() {
		if (!connectionManager.isConnected()) {
			MessageDialog.openWarning(Display.getDefault().getActiveShell(), "There is no collection to the data", "DAWN cannot connect to the data at the moment.\n\nPlease press the reconnect button.");
		    return false;
		}
        final StyledTreeBreadcrumbViewer bviewer = createBreadcrumbViewer(crumbs, true);
        bviewer.setInput(connectionManager.createContent());
        bviewers.add(bviewer);
        
    	return true;
	}

	protected void refreshDataCollections() {
		refreshTable();
	    selectionChanged((SelectionChangedEvent)null);
	}
	
	public void refreshTable() {
	   	INavigationDelegate prov = pages.get(navigationMode);
	   	prov.refresh();
	}


	@Override
	public void setFocus() {
		bviewers.get(0).setFocus();
	}
	
	@Override
	public void dispose() {
		for (INavigationDelegate prov : pages.values()) prov.dispose();
 		for (BreadcrumbViewer bviewer : bviewers) bviewer.removeSelectionChangedListener(this);
		bviewers.clear();
		super.dispose();
	}

	/**
	 * event may be null to read selections from nodes.
	 */
	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		
		List<TreeNode> nodes = getNodes(event);
		
		try {
			if (nodes==null || nodes.isEmpty() || ((DefaultMutableTreeNode)nodes.get(0)).getUserObject()==null) return;
			
		    Object uObject = ((DefaultMutableTreeNode)nodes.get(0)).getUserObject();
		    INavigationDelegateMode specificMode = getSpecificNavigationMode(uObject);
		    INavigationDelegateMode mode = specificMode !=null
					    		         ? specificMode
					    		         : findSelectedMode(getDefaultNavigationMode());
		    updateSelectedNodes(nodes, mode);
		    
		} catch (Throwable ne) {
			logger.error("Unabled to select nodes!", ne);
		}
	}
	
	/**
	 * Implement to provide modes for specific user objects. For instance if a selection
	 * of this type, use a specific INavigationDelegate for this object.
	 * 
	 * @param uObject
	 * @return
	 */
	protected INavigationDelegateMode getSpecificNavigationMode(Object uObject) {
		return null;
	}

	private INavigationDelegateMode findSelectedMode(INavigationDelegateMode defaultMode) {
		final IContributionManager man = getViewSite().getActionBars().getToolBarManager();
		for (final INavigationDelegateMode mode : defaultMode.allValues()) {
            final IContributionItem item = man.find(mode.getId());
            if (item!=null && item instanceof ActionContributionItem) {
            	if (((ActionContributionItem)item).getAction().isChecked()) return mode;
		    }
		}
		return defaultMode;
	}


	private List<TreeNode> getNodes(SelectionChangedEvent event) {
		
		final Object sel     = event!=null
				             ? ((StructuredSelection)event.getSelection()).getFirstElement()
						     : null;

		final List<TreeNode> nodes;
		if (sel!=null && getSpecificNavigationMode(((DefaultMutableTreeNode)sel).getUserObject())!=null) {
			nodes = new ArrayList<TreeNode>();
			nodes.add((TreeNode)sel);
		} else {
			// Add them all
			nodes = getSelectedVisits();
		}
        return nodes;
	}
	
	/**
	 * The visits selected in the current viewers, thread safe
	 * @return
	 */
	public List<TreeNode> getSelectedVisits() {
		
		if (Thread.currentThread()==Display.getDefault().getThread()) {
			return getSelectedVisitsInternal();
		} else {
			final List<TreeNode> nodes = new ArrayList<TreeNode>();
            Display.getDefault().syncExec(new Runnable() {
            	public void run() {
            		List<TreeNode> ns = getSelectedVisitsInternal();
            		if (ns!=null) nodes.addAll(ns);
            	}
            });
            return nodes;
		}
	}

	private List<TreeNode> getSelectedVisitsInternal() {
		final List<TreeNode> nodes = new ArrayList<TreeNode>();
		for (BreadcrumbViewer bViewer : bviewers) {
			StructuredSelection ss = (StructuredSelection)bViewer.getSelection();
			final Object s = ss.getFirstElement();
			if (s == null)                 continue;
			if (!(s instanceof TreeNode)) continue;
			nodes.add((TreeNode)s);
		}	
		return nodes;
	}

	public INavigationDelegateMode getNavigationMode() {
		return navigationMode;
	}

	public void updateSelectedNodes(List<TreeNode> nodes, INavigationDelegateMode mode) {
		
		
		List<Object> selections = new ArrayList<Object>();
		for (TreeNode sel : nodes) {
			final Object uObject = ((DefaultMutableTreeNode)sel).getUserObject();
			if (uObject==null) continue;
			selections.add(uObject);
		}
		
		// Need a job for these, can be large
		AbstractTableDelegate dele = (AbstractTableDelegate)pages.get(mode);
		dele.schedule(selections, connectionManager.getBean());


		navigationMode = mode;

	}
	
	public void saveState(IMemento memento) {
        connectionManager.saveState(memento);
	}
		
	/**
	 * 
	 * @param control
	 * @param mode -  may be null. If null, toolbar will not be updated.
	 */
	public void setActiveUI(Control control, INavigationDelegateMode mode) {
		
		contextStack.topControl = control;
		card.layout();
		
		if (mode!=null) {
			IContributionManager man = getViewSite().getActionBars().getToolBarManager();
			for (INavigationDelegateMode key : pages.keySet()) pages.get(key).setActionsActive(false, man);
			pages.get(mode).setActionsActive(true, man);
			setPartName(mode.getLabel());
			if (mode.hasIcon()) {
				AbstractNavigationView.this.setTitleImage(mode.getIcon().createImage());
			}
			man.update(true);
		}
	}

	public List<StyledTreeBreadcrumbViewer> getViewers() {
		return new ArrayList<StyledTreeBreadcrumbViewer>(bviewers);
	}

	/**
	 * Thread safe
	 * @param cursorWait
	 */
	public void setCursor(final int cursorCode) {
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				
				final Cursor cursor = cursorCode>0 ? Display.getDefault().getSystemCursor(cursorCode) : null;
				for (BreadcrumbViewer bvewier : bviewers) bvewier.getControl().setCursor(cursor);
				for (INavigationDelegate prov : pages.values()) prov.setCursor(cursor);
			}
		});
	}

	@Override
	public AbstractLazyLabelProvider getLabelProvider() {
		AbstractTableDelegate dele = (AbstractTableDelegate)pages.get(navigationMode);
		return dele.labelProvider;
	}
	
	protected void setNavigationMode(INavigationDelegateMode mode) {
		this.navigationMode = mode;
	}
}
