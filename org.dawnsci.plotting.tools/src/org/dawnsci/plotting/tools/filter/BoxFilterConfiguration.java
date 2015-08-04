package org.dawnsci.plotting.tools.filter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.filter.AbstractPlottingFilter;
import org.eclipse.dawnsci.plotting.api.filter.FilterConfiguration;
import org.eclipse.dawnsci.plotting.api.filter.FilterEvent;
import org.eclipse.dawnsci.plotting.api.filter.IFilterListener;
import org.eclipse.dawnsci.plotting.api.filter.IPlottingFilter;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.richbeans.widgets.decorator.FloatDecorator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public abstract class BoxFilterConfiguration implements FilterConfiguration {
	private final static String[] BOX_OPTIONS;
	static {
		BOX_OPTIONS = new String[] { "3x3", "5x5", "7x7", "9x9" };
	}

	protected AbstractPlottingFilter filter;
	private FloatDecorator ldeco;
	private FloatDecorator udeco;

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
					if (hasHistoBounds()) {
						trace.setMin(ldeco.getValue());
						trace.setMax(udeco.getValue());
					}
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

		Label label = null;

		label = new Label(content, SWT.NONE);
		label.setText("Box Size");
		label.setToolTipText(getBoxToolTip());

		final CCombo boxOptions = new CCombo(content, SWT.BORDER);
		boxOptions.setItems(BOX_OPTIONS);
		boxOptions.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		boxOptions.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				setBoxString(boxOptions.getItems()[boxOptions
						.getSelectionIndex()]);
			}
		});

		boxOptions.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				setBoxString(boxOptions.getText());
			}
		});

		boxOptions.select(0);
		setBoxString(boxOptions.getItem(0));

		if (hasHistoBounds()) {
			label = new Label(content, SWT.HORIZONTAL);
			label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false,
					2, 1));

			label = new Label(content, SWT.NONE);
			label.setText("Histogram bounds");
			label.setToolTipText(getHistoToolTip());
			label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false,
					2, 1));

			label = new Label(content, SWT.NONE);
			label.setText("Lower");
			label.setToolTipText("The lower value for the histogram");

			final Text lower = new Text(content, SWT.BORDER);
			lower.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			lower.setText("0");

			label = new Label(content, SWT.NONE);
			label.setText("Upper");
			label.setToolTipText("The upper value for the histogram");

			final Text upper = new Text(content, SWT.BORDER);
			upper.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			upper.setText("25");

			ldeco = new FloatDecorator(lower);
			udeco = new FloatDecorator(upper);
			ldeco.setMinimum(-10d);
			ldeco.setMaximum(udeco);
			udeco.setMinimum(ldeco);
			ldeco.setMaximum(50);
		}

		return content;
	}

	private static final Pattern BOX_SIZE_PATTERN = Pattern
			.compile("(\\d+)x(\\d+)");

	private void setBoxString(String string) {
		Matcher matcher = BOX_SIZE_PATTERN.matcher(string);
		if (matcher.matches()) {
			int w = Integer.parseInt(matcher.group(1));
			int h = Integer.parseInt(matcher.group(2));
			filter.putConfiguration("box", new int[] { w, h });
		}
	}

	/**
	 * 
	 * @return
	 */
	abstract protected String getBoxToolTip();

	/**
	 * 
	 * @return
	 */
	private String getHistoToolTip() {
		return "The bounds will be reset after the filter is removed."
				+ "\nSpecific bounds allow the features the fano factor bring out to be visible.";
	}

	/**
	 * If Returns True, then the Box configuration will also show and use the
	 * Hostogram lower and upper limits UI
	 * 
	 * @return hasHisto
	 */
	protected boolean hasHistoBounds() {
		return false;
	}
}
