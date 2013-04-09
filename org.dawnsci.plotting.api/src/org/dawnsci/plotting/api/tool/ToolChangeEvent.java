package org.dawnsci.plotting.api.tool;

import java.util.EventObject;

import org.eclipse.ui.IWorkbenchPart;

public class ToolChangeEvent extends EventObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8805347445621628990L;

	private IToolPage oldPage;
	private IToolPage newPage;
	private IWorkbenchPart part;

	public ToolChangeEvent(Object source, IToolPage oldPage, IToolPage newPage, IWorkbenchPart part) {
		super(source);
		this.oldPage = oldPage;
		this.newPage = newPage;
		this.part    = part;
	}

	public IToolPage getOldPage() {
		return oldPage;
	}

	public IToolPage getNewPage() {
		return newPage;
	}

	public IWorkbenchPart getPart() {
		return part;
	}
}
