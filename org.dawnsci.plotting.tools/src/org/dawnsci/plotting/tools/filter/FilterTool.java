package org.dawnsci.plotting.tools.filter;

import java.util.Map;

import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.dawnsci.plotting.api.filter.IFilterDecorator;
import org.eclipse.dawnsci.plotting.api.filter.IPlottingFilter;
import org.eclipse.dawnsci.plotting.api.tool.AbstractToolPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * A tool which reads the plot filter extension point 
 * and allows the user to experiment with different filters.
 * 
 * For instance this tool can be used to apply the PHA algorithm 
 * and the fano factor.
 * 
 * @author fcp94556
 *
 */
public class FilterTool extends AbstractToolPage {

	private Composite        control;
	private IFilterDecorator deco;
	private Map<String, IPlottingFilter> filters;
	private IPlottingFilter  currentFilter;

	@Override
	public ToolPageRole getToolPageRole() {
		return ToolPageRole.ROLE_2D;
	}
	
	@Override
	public void createControl(Composite parent) {
		
		super.createControl(parent);
		
		// Create the UI for the filters
		this.control = new Composite(parent, SWT.NONE);
		control.setLayout(new GridLayout(2, false));	
		
		this.deco = PlottingFactory.createFilterDecorator(getPlottingSystem());
	}

	@Override
	public Control getControl() {
		return control;
	}

	@Override
	public void setFocus() {
		control.setFocus();
	}

	public void activate() {
		super.activate();
		if (deco!=null && currentFilter!=null) {
			deco.addFilter(currentFilter);
		}
	}

	public void deactivate() {
        super.deactivate();
		if (deco!=null && currentFilter!=null) {
			deco.removeFilter(currentFilter);
		}
	}
}
