package org.dawnsci.plotting.tools.masking;

import java.util.Collection;

import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.tool.AbstractToolPage;
import org.eclipse.dawnsci.plotting.api.tool.IToolPage.ToolPageRole;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

public class FastMaskTool extends AbstractToolPage {

	private Composite control;
	private MaskCircularBuffer buffer;
	
	
	public FastMaskTool() {
	}

	@Override
	public ToolPageRole getToolPageRole() {
		return ToolPageRole.ROLE_2D;
	}

	@Override
	public void createControl(Composite parent) {
		this.control = new Composite(parent, SWT.NONE);
		control.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
		control.setLayout(new GridLayout(1, false));
		removeMargins(control);
		
		Button b = new Button(control, SWT.PUSH);
		b.setText("Apply");
		b.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				IImageTrace imageTrace = getImageTrace();
				if (imageTrace != null) {
					IDataset data = imageTrace.getData();
					if (buffer == null) buffer = new MaskCircularBuffer(data.getShape());
					Collection<IRegion> regions = getPlottingSystem().getRegions();
					for (IRegion r : regions) buffer.maskROI(r.getROI());
					
					imageTrace.setMask(buffer.getMask());
				}
				
			}
			

		});
		
		Button b2 = new Button(control, SWT.PUSH);
		b2.setText("Undo");
		b2.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				buffer.undo();
				IImageTrace imageTrace = getImageTrace();
				imageTrace.setMask(buffer.getMask());
			}


		});


	}

	@Override
	public Control getControl() {
		return control;
	}

	@Override
	public void setFocus() {
		control.setFocus();
	}

	@Override
	public void activate() {
		super.activate();
		// Now add any listeners to the plotting providing getPlottingSystem()!=null
	}
	
	@Override
	public void deactivate() {
		super.deactivate();
		// Now remove any listeners to the plotting providing getPlottingSystem()!=null
	}
	@Override
	public void dispose() {
		super.dispose();
        // Anything to kill off? This page is part of a view which is now disposed and will not be used again.
	}
	
	private static void removeMargins(Composite area) {
		final GridLayout layout = (GridLayout)area.getLayout();
		if (layout==null) return;
		layout.horizontalSpacing=0;
		layout.verticalSpacing  =0;
		layout.marginBottom     =0;
		layout.marginTop        =0;
		layout.marginLeft       =0;
		layout.marginRight      =0;
		layout.marginHeight     =0;
		layout.marginWidth      =0;
	}

}
