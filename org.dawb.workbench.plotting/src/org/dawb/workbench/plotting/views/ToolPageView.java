/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawb.workbench.plotting.views;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.dawb.common.ui.plot.IPlottingSystem;
import org.dawb.common.ui.plot.tool.AbstractToolPage;
import org.dawb.common.ui.plot.tool.IToolChangeListener;
import org.dawb.common.ui.plot.tool.IToolPage;
import org.dawb.common.ui.plot.tool.IToolPageSystem;
import org.dawb.common.ui.plot.tool.ToolChangeEvent;
import org.dawb.common.util.text.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IURIEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.IPage;
import org.eclipse.ui.part.MessagePage;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.PageBookView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This view can be shown at the side of a plotting part. The
 * plotting part contributes a tool page which is shown by the view.
 * 
 * For instance fitting, derviatives etc.
 * 
 * 
 * @author fcp94556
 *
 */
public class ToolPageView extends PageBookView implements IToolChangeListener { // Important: whole part must be IToolChangeListener

	private static final Logger logger = LoggerFactory.getLogger(ToolPageView.class);

	public static final String ID = "org.dawb.workbench.plotting.views.ToolPageView";
	
	private Collection<IToolPageSystem> systems;
	private Map<String,Map<IPage,PageRec>> recs;
	private String unique_id;
	
	public ToolPageView() {
		super();
		systems        = new HashSet<IToolPageSystem>(7);
		this.unique_id = StringUtils.getUniqueId(ToolPageView.class);
		this.recs      = new HashMap<String,Map<IPage,PageRec>>(7);
	}

	@Override
	protected IPage createDefaultPage(PageBook book) {
		MessagePage messagePage = new MessagePage();
		initPage(messagePage);
		messagePage.createControl(book);
		return messagePage;
	}
	
	@Override
	protected synchronized PageRec doCreatePage(IWorkbenchPart part) {
		
		final IToolPageSystem sys = (IToolPageSystem)part.getAdapter(IToolPageSystem.class);
        
		if (sys!=null) {
			systems.add(sys);
			sys.addToolChangeListener(this);

			final IToolPage      tool = sys.getCurrentToolPage();	

	        final PageRec existing = getPageRec(part);
	        
	        if (tool!=null && existing!=null&&existing.page!=null && existing.page.equals(tool)) {
	        	if (!tool.isActive()) tool.activate();
	        	return existing;
	        }

			if (tool == null) {
				PageRec rec = getBlankPageRec(part, sys); // They did not make a selection yet or want no tool active.
				recordPage(part, null, rec);
				return rec;
			}

			setPartName(tool.getTitle());
			initPage(tool);
			tool.createControl(getPageBook());	
						
			PageRec rec = new NamedPageRec(part, tool);
			recordPage(part, tool, rec);
            return rec;
		}
		
		return null;
	}
	
	private void recordPage(IWorkbenchPart part, IToolPage tool, PageRec rec) {
		Map<IPage,PageRec> pages = recs.get(getString(part));
		if (pages==null) {
			pages = new HashMap<IPage, PageRec>(3);
			recs.put(getString(part), pages);
		}
		pages.put(tool, rec);
	}
	
	private String getString(IWorkbenchPart part) {
		if (!(part instanceof EditorPart)) return null;
		final IEditorInput input = ((EditorPart)part).getEditorInput();
		return input instanceof IURIEditorInput 
			   ? ((IURIEditorInput)input).getURI().getRawPath()
			   : input.getName(); // TODO Not very secure
	}
	
	private boolean updatingActivated = false;

	@Override
	public void toolChanged(ToolChangeEvent evt) {
		if (updatingActivated) return;
		partActivated(evt.getPart());
	}
	
	public void partActivated(IWorkbenchPart part) {

		if (!isImportant(part)) return;

		if (updatingActivated) return;
        try {
            updatingActivated = true;
        	IToolPageSystem sys = (IToolPageSystem)part.getAdapter(IToolPageSystem.class);
            if (sys!=null && sys.getCurrentToolPage().equals(getCurrentPage())) {
            	return;
            }
        
            super.partActivated(part);
 			setPartName(sys.getCurrentToolPage().getTitle());

        } catch (Throwable ne) {
        	logger.error("Problem updating activated state in "+getClass().getName()); // No stack required in log here.
        } finally {
        	updatingActivated = false;
        }
	}
	
	protected PageRec getPageRec(IWorkbenchPart part) {
		
        final Map<IPage, PageRec> pages = recs.get(getString(part));
        if (pages == null) return null;
        
		IToolPageSystem sys = (IToolPageSystem)part.getAdapter(IToolPageSystem.class);
        return sys!=null ? pages.get(sys.getCurrentToolPage()) : super.getPageRec(part);
	}	
	
	protected PageRec getPageRec(IPage page) {
		
		if (page instanceof IToolPage) {
						
	        final Map<IPage, PageRec> pages = recs.get(getString(((IToolPage)page).getPart()));
	        if (pages == null) return null;
	        
	        return pages.get(page);
		} else {
			return super.getPageRec(page);
		}
	}


	private PageRec blankPage;
	private PageRec getBlankPageRec(IWorkbenchPart part, IToolPageSystem sys) {
		
        if (blankPage!=null) return blankPage;
        
        final BlankPage page = new BlankPage();
        page.setToolSystem(sys);
        if (sys instanceof IPlottingSystem) page.setPlottingSystem((IPlottingSystem)sys);
		initPage(page);
		page.createControl(getPageBook());	
		
        blankPage = new NamedPageRec(part, page);
        
		return blankPage;
	}

	@Override
	protected void doDestroyPage(IWorkbenchPart part, PageRec pageRecord) {
		pageRecord.page.dispose();
	}
	
	public void partClosed(IWorkbenchPart part) {

		if (!isImportant(part)) return;
		
		super.partClosed(part);
				
		final Map<IPage, PageRec> pages = recs.remove(getString(part));
		if (pages!=null) {
			for (IPage page : pages.keySet()) {
				if (page!=null) page.dispose();
			}
		}
	}

	@Override
	protected IWorkbenchPart getBootstrapPart() {
		
		IWorkbenchPage page = getSite().getPage();
		if(page != null) {
			// check whether the active part is important to us
			IWorkbenchPart activePart = page.getActivePart();
			return isImportant(activePart)?activePart:null;
		}
		return null;	
	}

	@Override
	protected boolean isImportant(IWorkbenchPart part) {
		return part instanceof EditorPart;
	}
	
	public void dispose() {
		super.dispose();
		for (IToolPageSystem sys : systems) {
			sys.removeToolChangeListener(this);
		}
		systems.clear();
		
		for(Map<IPage,PageRec> pages : recs.values()) {
			for (IPage page : pages.keySet()) {
				try {
				    pages.get(page).dispose();
				} catch (Throwable ignored) {
					
				}
				try {
					page.dispose();
				} catch (Throwable ignored) {
					
				}
			}
			pages.clear();
		}
		recs.clear();
	}
	
	
	public class BlankPage extends AbstractToolPage {
		private Composite comp;

		@Override
		public void createControl(Composite parent) {
			this.comp = new Composite(parent, SWT.NONE);
		}

		@Override
		public void setFocus() {
			
		}
		
		public String toString() {
			return "No tool page";
		}

		@Override
		public Control getControl() {
			return comp;
		}
	}
	
	protected class NamedPageRec extends PageRec {


	    public NamedPageRec(IWorkbenchPart arg0, IPage arg1) {
			super(arg0, arg1);
		}
	    
	    public String toString() {
	    	if (page!=null) return page.toString();
	    	return super.toString();
	    }
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((unique_id == null) ? 0 : unique_id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ToolPageView other = (ToolPageView) obj;
		if (unique_id == null) {
			if (other.unique_id != null)
				return false;
		} else if (!unique_id.equals(other.unique_id))
			return false;
		return true;
	}

}
