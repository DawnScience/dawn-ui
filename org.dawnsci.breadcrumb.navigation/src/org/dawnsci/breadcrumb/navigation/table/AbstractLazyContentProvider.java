package org.dawnsci.breadcrumb.navigation.table;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dawb.common.services.ServiceManager;
import org.dawb.common.services.expressions.IExpressionEngine;
import org.dawb.common.services.expressions.IExpressionService;
import org.dawnsci.breadcrumb.navigation.Activator;
import org.dawnsci.breadcrumb.navigation.preference.NavigationConstants;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Because this content is pseudo lazy, we implement the 
 * search in the content provider. This is wrong but allows
 * the content to be lazy unless searched - which is right :)
 * 
 * @author fcp94556
 *
 */
public abstract class AbstractLazyContentProvider implements ISortableLazyContentProvider, IIndexProvider {

	protected static final Logger logger = LoggerFactory.getLogger(AbstractLazyContentProvider.class);
	
	private TableViewer         viewer;
	private List<Object>        fullData;
	private List<Object>        reducedSearch;
	private IExpressionService  expressionService;

	private JexlJob jexlJob;
	private WildJob wildJob;
	private SortJob sortJob;
	
	public AbstractLazyContentProvider() {
		
		this.jexlJob = new JexlJob();
		this.wildJob = new WildJob();
		this.sortJob = new SortJob();
		try {
			this.expressionService = (IExpressionService)ServiceManager.getService(IExpressionService.class);
		} catch (Exception e) {
			logger.error("Cannot get expression service - required for queary search in ISPyB client!", e);
		}
	}
	
	/**
	 * Puts the expression values for searching into the values array.
	 * 
	 * Does this by casting object to its actual type and then calling required methods.
	 * Could use bean reflection instead but doing this way makes things faster.
	 * 
	 * Required for expression search
	 * 
	 * @param object
	 * @param vars
	 * @param values
	 */
	protected abstract void populateExpressionValues(Object object, Collection<String> vars, Map<String, Object> values);
	
	/**
	 * Used for name wildcard search. Implement to return a search name (usually the name column value) for the data.
	 * @param data
	 * @return
	 */
	protected abstract String getDataName(Object data);
	
	/**
	 * Get the primary key value (or other key that increases with older data).
	 * @param data
	 * @return
	 */
	protected abstract int getDataId(Object data);
	
	protected abstract AbstractTableColumnComparator getComparitor(DirectionalIndexedColumnEnum col, AbstractLazyLabelProvider prov, IProgressMonitor monitor);

	@Override
	public void dispose() {

	}

	@Override
	public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		this.viewer    = (TableViewer)v;
		List<Object> data  = (List<Object>)newInput;
		setData(data);
	}
	
    private void setData(List<Object> data) {
    	this.fullData = data;
		reducedSearch  = null;
		jexlJob.cancel();
		wildJob.cancel();
		sortJob.cancel();
		clearSort();
		
		search(false);
	}

	/**
     * Thread safe insert
     * @param newCollections
     * @return true if anything inserted
     */
	public boolean insert(Collection<Object> newData) {
		
       if (newData==null) return false;
       for (Object dd : newData) {
    	   if (!fullData.contains(dd)) {
    		   fullData.add(0, dd);
    	   }
       }
       if (Thread.currentThread()==Display.getDefault().getThread()) {
    	   setData(fullData);
    	   
       } else {
    	   Display.getDefault().syncExec(new Runnable() {
    		   public void run() {
    	    	   setData(fullData);
     		   }
    	   });
       }
       
       return true;
	}


	private void clearSort() {
		viewer.getTable().setSortColumn(null);
		if (currentSortColumnEnum!=null) currentSortColumnEnum.setDirection(SWT.NONE);
	}

	@Override
	public void updateElement(int index) {
		if (reducedSearch!=null ) {
			viewer.replace(reducedSearch.get(index), index);
			return;
		}
		if (index>=fullData.size()) return;
		viewer.replace(fullData.get(index), index); // This then causes the label to render which is more expensive.
	}

	private String            searchString = null;
	private ISortParticipant enablable = null;

	private DirectionalIndexedColumnEnum currentSortColumnEnum;
			
	public void setSearchText(final ISortParticipant enablable, final String searchString) {

		this.searchString = searchString;
		this.enablable    = enablable;
		
		search(true);
	}
	
	private void search(boolean requireSetItemsOnNull) {
		
		if (viewer==null)    return;
		if (enablable==null) return;
		
		if (searchString == null || searchString.length() < 1) {
			reducedSearch = null;
			enablable.setEnabled(true);
			if (!requireSetItemsOnNull) return;
			
			viewer.setItemCount(fullData.size());
			viewer.refresh();
			return;
		}

		boolean isWild = NavigationConstants.WILD.equals(Activator.getDefault().getPreferenceStore().getString(NavigationConstants.SEARCH_TYPE));
		if (isWild) {
			wildJob.schedule(enablable, searchString);
		} else {
			jexlJob.schedule(enablable, searchString);
		}
	}



	@Override
	public int getIndex(Object element) {
		if (reducedSearch!=null) return reducedSearch.indexOf(element);
		return fullData.indexOf(element);
	}

	
	/**
	 * Used to sort the current list, including if the list is filtered.
	 * @param colEnum
	 * @return
	 */
	public int sort(final ISortParticipant enablable, DirectionalIndexedColumnEnum colEnum) {
		final int direction = colEnum.toggleDirection();
		
		if (direction==SWT.NONE) {
			colEnum.getDateColumn().setDirection(SWT.DOWN);
			sortJob.schedule(enablable, colEnum.getDateColumn());
		} else {
		    sortJob.schedule(enablable, colEnum);
		}
		currentSortColumnEnum = colEnum;
		return direction;
	}

	class SortJob extends Job {
		
		private ISortParticipant          enablable;
		private DirectionalIndexedColumnEnum  visitCol;
		
		SortJob() {
			super("Sort ");
			setPriority(Job.INTERACTIVE);
			setUser(true);
			setSystem(false);
		}
		
		@Override
		protected IStatus run(IProgressMonitor monitor) {
			enablable.setEnabled(false);			
			try {
				final List<Object> search =  reducedSearch!=null ? reducedSearch : fullData;
				
				monitor.beginTask("Sort '"+visitCol.getName()+"' "+visitCol.getDirectionLabel(), search.size());
				
				// Can be expensive!
				Collections.sort(search, getComparitor(visitCol, enablable.getLabelProvider(), monitor));
				
				Display.getDefault().syncExec(new Runnable() {
					public void run() {
						viewer.setItemCount(search.size());
						viewer.refresh();
					}
				});
				
				monitor.done();
				
			} finally {
				enablable.setEnabled(true);

			}

			return Status.OK_STATUS;
		}
		
		public void schedule(ISortParticipant enablable, DirectionalIndexedColumnEnum visitCol) {
			cancel();
			this.enablable  = enablable;
			this.visitCol   = visitCol;
			schedule();
		}

	}
	
	
	class JexlJob extends Job {
		
		private String expression;
		private ISortParticipant enablable;
		JexlJob() {
			super("Query ");
			setPriority(Job.INTERACTIVE);
			setUser(true);
			setSystem(false);
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			
			try {
				enablable.setEnabled(false);
				
				monitor.beginTask("Query '"+expression+"'", fullData.size());
				if (reducedSearch==null) reducedSearch = new ArrayList<Object>(31);
				reducedSearch.clear();
	
				final IExpressionEngine   engine = expressionService.getExpressionEngine();
				engine.createExpression(expression);
	
				final Map<String, Object> values = new HashMap<String, Object>(9);
				if (fullData == null) return Status.CANCEL_STATUS;
				for (Object data : fullData) {
	
					monitor.worked(1);
					if (monitor.isCanceled()) {
						Display.getDefault().syncExec(new Runnable() {
							public void run() {
								reducedSearch = null;
								viewer.setItemCount(fullData.size());
								viewer.refresh();
							}
						});
						return Status.CANCEL_STATUS;
					}
					try {
						final Collection<String> vars = engine.getVariableNamesFromExpression();
                        						
						populateExpressionValues(data, vars, values);
						
						engine.setLoadedVariables(values);
						Object value = engine.evaluate();
						if (value instanceof Boolean && ((Boolean)value).booleanValue()) {
							reducedSearch.add(data);
						}
	
					} finally {
						values.clear();
					}
				}
				
				Display.getDefault().syncExec(new Runnable() {
					public void run() {
						viewer.setItemCount(reducedSearch.size());
						viewer.refresh();
					}
				});
				enablable.saveSearch(expression);
				
			} catch (final Exception ne) {
				logger.error("Cannot run jexl job!", ne);
				Display.getDefault().syncExec(new Runnable() {
					public void run() {
						if (viewer.getTable().isDisposed()) return;
						MessageDialog.openWarning(Display.getDefault().getActiveShell(), "Invalid Query", "Invalid query '"+expression+"'. Please enter a different search.\n\n"+ne.getMessage());
						reducedSearch = null;
						viewer.setItemCount(fullData.size());
						viewer.refresh();
					}
				});
			} finally {
				enablable.setEnabled(true);

			}

			return Status.OK_STATUS;
		}
		
		public void schedule(ISortParticipant enablable, String expr) {
			cancel();
			this.enablable  = enablable;
			this.expression = expr;
			schedule();
		}
		
	}


	class WildJob extends Job {

		private String searchString;
		private ISortParticipant enablable;
		WildJob() {
			super("Wildcard ");
			setPriority(Job.INTERACTIVE);
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {


			try {
				enablable.setEnabled(false);

				searchString = searchString.replaceAll("\\.\\*", "\\*");
				searchString = searchString.replaceAll("\\*", "\\.\\*");
				String regex = ".*" + searchString.toLowerCase() + ".*";
				monitor.beginTask("Wildcard '"+regex+"'", fullData.size());

				if (reducedSearch==null) reducedSearch = new ArrayList<Object>(31);
				reducedSearch.clear();

				for (Object data : fullData) {

					monitor.worked(1);
					if (monitor.isCanceled()) {
						Display.getDefault().syncExec(new Runnable() {
							public void run() {
								reducedSearch = null;
								viewer.setItemCount(fullData.size());
								viewer.refresh();
							}
						});
						return Status.CANCEL_STATUS;
					}
					String name = getDataName(data);

					try {
						if (name.matches(regex)) {
							reducedSearch.add(data);
						}
					} catch (Exception ne) {
						break; // probably bas wildcard.
					}

				}


				Display.getDefault().syncExec(new Runnable() {
					public void run() {
						viewer.setItemCount(reducedSearch.size());
						viewer.refresh();
					}
				});
			} finally {
				enablable.setEnabled(true);
			}

			return Status.OK_STATUS;
		}

		public void schedule(ISortParticipant enablable, String searchString) {
			cancel();
			this.searchString = searchString;
			this.enablable = enablable;
			schedule();
		}
		
	}


	public String getHighestDataCollectionId() {
		if (fullData==null || fullData.isEmpty()) return null;
		long max = Long.MIN_VALUE;
		for (Object dc : fullData) {
			int id = getDataId(dc);
			max = Math.max(max, id);
		} 
		return String.valueOf(max);
	}


}
