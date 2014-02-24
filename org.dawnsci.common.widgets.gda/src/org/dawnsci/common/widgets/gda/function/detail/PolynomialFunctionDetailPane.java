package org.dawnsci.common.widgets.gda.function.detail;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;

import uk.ac.diamond.scisoft.analysis.fitting.functions.Polynomial;

public class PolynomialFunctionDetailPane implements IFunctionDetailPane {

	private Label label;
	private Label labelDegree;
	private Spinner polynomialDegree;
	private Polynomial poly;
	private IDisplayModelSelection displayModel;

	@Override
	public Control createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(composite);
		label = new Label(composite, SWT.NONE);
		GridDataFactory.fillDefaults().span(2, 1).applyTo(label);

		labelDegree = new Label(composite, SWT.NONE);
		labelDegree.setText("Polynomial degree ");

		polynomialDegree = new Spinner(composite, SWT.NONE);
		polynomialDegree.setToolTipText("Polynomial degree");
		polynomialDegree.setMinimum(0);
		polynomialDegree.setMaximum(100);
		polynomialDegree.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				poly.setDegree(polynomialDegree.getSelection());
				displayModel.refreshElement();
			}
		});

		return composite;
	}

	@Override
	public void display(IDisplayModelSelection displayModel) {
		this.displayModel = displayModel;
		Object element = displayModel.getElement();
		if (element instanceof Polynomial) {
			poly = (Polynomial) element;
			StringBuilder toShow = new StringBuilder();
			toShow.append(poly.getName());
			toShow.append(" ");
			toShow.append(poly.getDescription());
			label.setText(toShow.toString());
			polynomialDegree.setSelection(poly.getNoOfParameters() - 1);
		}
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}
}
