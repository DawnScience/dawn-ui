package org.dawnsci.datavis.manipulation.aggregate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.dawnsci.analysis.api.expressions.IExpressionEngine;
import org.eclipse.dawnsci.analysis.api.expressions.IExpressionService;
import org.eclipse.dawnsci.plotting.api.IPlottingService;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace.PointStyle;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace.TraceType;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.widgets.WidgetFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.osgi.services.ServiceProvider;

public class ExpressionLabelDialog extends Dialog {
	private static final Logger logger = LoggerFactory.getLogger(ExpressionLabelDialog.class);

	private Map<String, Dataset> variables;
	private IPlottingSystem<Composite> system;
	private Text expression;

	private IExpressionEngine engine;

	private List<String> result;
	
	public ExpressionLabelDialog(Shell parentShell, Map<String, Dataset> datasets) {
		super(parentShell);

		try {
			system = ServiceProvider.getService(IPlottingService.class).createPlottingSystem();
		} catch (Exception e) {
			logger.error("Error creating Combine plotting system:", e);
		}
		this.variables = datasets;

		IExpressionService expressionService = ServiceProvider.getService(IExpressionService.class);
		engine = expressionService.getExpressionEngine();
		Map<String, Object> vars = new HashMap<>();
		for (Dataset v : variables.values()) {
			vars.put(v.getName(), v);
		}
		engine.addLoadedVariables(vars);
		result = new ArrayList<>();
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Control createDialogArea(Composite parent) {
		GridDataFactory gdf = GridDataFactory.fillDefaults().grab(true, true);
		GridLayoutFactory glf = GridLayoutFactory.fillDefaults();

		Composite container = (Composite) super.createDialogArea(parent);
		container.setLayoutData(gdf.create());
		container.setLayout(glf.create());

		system.createPlotPart(container, "Expression Plot", null, PlotType.XY, null);
		system.getPlotComposite().setLayoutData(gdf.copy().minSize(800, 300).create());
		system.setShowLegend(false);

		Composite bottomPane = WidgetFactory.composite(SWT.NONE).create(container);
		bottomPane.setLayoutData(gdf.create());
		bottomPane.setLayout(GridLayoutFactory.swtDefaults().numColumns(2).equalWidth(true).create());

		TableViewer viewer = new TableViewer(bottomPane, SWT.BORDER | SWT.V_SCROLL | SWT.SINGLE);
		viewer.getTable().setHeaderVisible(true);
		viewer.getTable().setLayoutData(gdf.copy().grab(false, true).create());

		viewer.setContentProvider(new IStructuredContentProvider() {
			@Override
			public Object[] getElements(Object inputElement) {
				return variables.entrySet().toArray();
			}

			@Override
			public void dispose() {
			}

			@Override
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}
		});

		TableViewerColumn vars = new TableViewerColumn(viewer, SWT.NONE);
		vars.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				String k = ((Entry<String, Dataset>) element).getValue().getName();
				return k;
			}

			@Override
			public String getToolTipText(Object element) {
				String k = ((Entry<String, Dataset>) element).getKey();
				return k;
			}
		});
		TableColumn vc = vars.getColumn();
		vc.setText("Variables");
		vc.setToolTipText("Double click in items to insert in expression");
		vc.setWidth(80);

		viewer.addDoubleClickListener(e -> {
			ISelection s = viewer.getSelection();
			if (s instanceof IStructuredSelection) {
				Object element = ((IStructuredSelection) s).getFirstElement();
				String k = ((Entry<String, Dataset>) element).getValue().getName();
				expression.insert(k);
			}
		});

		ColumnViewerToolTipSupport.enableFor(viewer);
		viewer.setInput(variables);
		viewer.refresh();

		Composite bottomRightPane = WidgetFactory.composite(SWT.NONE).create(bottomPane);
		bottomRightPane.setLayoutData(gdf.create());
		bottomRightPane.setLayout(glf.copy().numColumns(2).equalWidth(false).create());
		WidgetFactory.text(SWT.NONE).text("Expression").create(bottomRightPane);
		expression = WidgetFactory.text(SWT.BORDER | SWT.SINGLE).create(bottomRightPane);
		expression.setLayoutData(gdf.copy().grab(true, false).create());
		expression.setFocus();

		return container;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);
		createButton(parent, IDialogConstants.DETAILS_ID, "Apply", true);
	}

	@Override
	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.DETAILS_ID || buttonId == IDialogConstants.OK_ID) {

			if (!applyExpression()) {
				setReturnCode(CANCEL);
				if (buttonId == IDialogConstants.OK_ID) {
					close();
					return;
				}
			}
		}
		super.buttonPressed(buttonId);
	}

	private boolean applyExpression() {
		String text = expression.getText();
		result.clear();

		try {
			engine.createExpression(text);
		} catch (Exception e) {
			MessageDialog.openError(getShell(), "Invalid expression", e.getMessage());
			return false;
		}

		Collection<String> usedVars = engine.getVariableNamesFromExpression();
		for (String v : usedVars) {
			result.add(v);
		}
		try {
			Dataset r = DatasetFactory.createFromObject(engine.evaluate());
			r.setName(text);
			result.add(0, text);
			plotResult(r);
		} catch (Exception e) {
			MessageDialog.openError(getShell(), "Unable to evaluate expression", e.getMessage());
			return false;
		}
		return true;
	}

	private void plotResult(Dataset r) {
		try {
			system.reset();
			ILineTrace lt = system.createLineTrace("expression values");
			lt.setPointStyle(PointStyle.XCROSS);
			lt.setPointSize(6);
			lt.setTraceType(TraceType.DASH_LINE);
			lt.setData(null, r);
			system.addTrace(lt);
			system.autoscaleAxes();
		} catch (Exception e) {
			logger.error("Could not plot label values", e);
		}
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Create expression");
	}

	@Override
	public boolean close() {
		system.dispose();
		return super.close();
	}

	/**
	 * @return list containing expression and variables
	 */
	public List<String> getResult() {
		return result;
	}

	@Override
	protected Point getInitialSize() {
		return new Point(800, 600);
	}

	@Override
	protected boolean isResizable() {
		return true;
	}
}
