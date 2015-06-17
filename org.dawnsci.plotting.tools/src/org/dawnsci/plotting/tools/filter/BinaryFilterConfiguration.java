package org.dawnsci.plotting.tools.filter;

import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.filter.AbstractPlottingFilter;
import org.eclipse.dawnsci.plotting.api.filter.FilterConfiguration;
import org.eclipse.dawnsci.plotting.api.filter.FilterEvent;
import org.eclipse.dawnsci.plotting.api.filter.IFilterListener;
import org.eclipse.dawnsci.plotting.api.filter.IPlottingFilter;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public abstract class BinaryFilterConfiguration implements FilterConfiguration {

	protected AbstractPlottingFilter filter;

	@Override
	public void init(final IPlottingSystem system, IPlottingFilter filter) {

		this.filter = (AbstractPlottingFilter) filter;

		filter.addFilterListener(new IFilterListener() {
			@Override
			public void filterApplied(FilterEvent evt) {
				IImageTrace trace = (IImageTrace) system
						.getTraces(IImageTrace.class).iterator().next();
				if (trace != null) {
					trace.setImageUpdateActive(false);
					trace.setHistoType(trace.getHistoType());
					trace.setImageUpdateActive(true);
				}
			}

			@Override
			public void filterReset(FilterEvent evt) {

			}
		});
	}

	@Override
	public Composite createControl(Composite parent) {
		final Composite content = new Composite(parent, SWT.NONE);
		content.setLayout(new GridLayout(2, false));

		Label label = new Label(content, SWT.NONE);
		label.setText("Convert to binary");
		label.setToolTipText("");
		final Combo binaryCombo = new Combo(content, SWT.READ_ONLY);
		binaryCombo.setItems(new String[] {"Yes", "No"});
		binaryCombo.select(1);
		filter.putConfiguration("binary", false);
		binaryCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int idx = binaryCombo.getSelectionIndex();
				boolean isBinary = false;
				if (idx == 1)
					isBinary = false;
				else if (idx == 0)
					isBinary = true;
				filter.putConfiguration("binary", isBinary);
			}
		});
		return content;
	}

}
