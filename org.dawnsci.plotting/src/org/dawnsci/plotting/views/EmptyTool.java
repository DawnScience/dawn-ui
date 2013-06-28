package org.dawnsci.plotting.views;

import java.util.Collection;

import org.dawnsci.plotting.Activator;
import org.dawnsci.plotting.api.region.IRegion;
import org.dawnsci.plotting.api.tool.AbstractToolPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class EmptyTool extends AbstractToolPage {

	private Composite composite;
	private ToolPageRole role;

	public EmptyTool(ToolPageRole role) {
		setTitle("Empty Tool");
		this.role = role;
		setImageDescriptor(Activator.getImageDescriptor("icons/axis.png"));
		setToolId("empty."+role.getId());
	}
	@Override
	public void createControl(Composite parent) {
		this.composite = new Composite(parent, SWT.NONE);
	}

	@Override
	public Control getControl() {
		return composite;
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}
	public void activate() {
		super.activate();
		
		// Make any mouse following regions, inactive
		if (getPlottingSystem()!=null) {
			final Collection<IRegion> regions = getPlottingSystem().getRegions();
			for (IRegion iRegion : regions) {
				if (iRegion.isTrackMouse()) {
					getPlottingSystem().removeRegion(iRegion);
				}
			}
		}
	}

	@Override
	public ToolPageRole getToolPageRole() {
		return role;
	}

}
