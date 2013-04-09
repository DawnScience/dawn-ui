package org.dawnsci.plotting.api;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Display;


public class PlottingSelectionProvider implements ISelectionProvider {

	private Set<ISelectionChangedListener> listeners;
	private ISelection currentSelection = new StructuredSelection();
	private BlockingDeque<ISelection> selectionQueue;
	private Thread                    selectionJob;
	
	public PlottingSelectionProvider() {
		listeners      = new HashSet<ISelectionChangedListener>(11);
		selectionQueue = new LinkedBlockingDeque<ISelection>(1);
		
		selectionJob   = new Thread("Plot selection thread") {		
			@Override
			public void run() {
				
				while(listeners!=null&&selectionQueue!=null) {
					try {
						currentSelection = selectionQueue.take();
						if (currentSelection instanceof DoneSelection) return;
 						
						Display.getDefault().asyncExec(new Runnable() {
							@Override
							public void run() {
								SelectionChangedEvent e = new SelectionChangedEvent(PlottingSelectionProvider.this, currentSelection);
								for (ISelectionChangedListener s : listeners) s.selectionChanged(e);
							}
						});
						Thread.sleep(100);
						
					} catch (InterruptedException e1) {
						continue;
					}
				}
			}
		};
		selectionJob.setDaemon(true);
		selectionJob.setPriority(Thread.MIN_PRIORITY);
		selectionJob.start();
	}
	
	@Override
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		listeners.add(listener);
	}

	@Override
	public ISelection getSelection() {
		return currentSelection;
	}

	@Override
	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		listeners.remove(listener);
	}

	/**
	 * Method calls listener in background thread mto make frequent updates possible.
	 */
	@Override
	public void setSelection(ISelection selection) {
		
		selectionQueue.clear();
		selectionQueue.add(selection);
		
	}

	public void clear() {
		if (listeners!=null) listeners.clear();
		selectionQueue.clear();
		selectionQueue.add(new DoneSelection());
	}
	
	private final class DoneSelection extends StructuredSelection {
		
	}

}
