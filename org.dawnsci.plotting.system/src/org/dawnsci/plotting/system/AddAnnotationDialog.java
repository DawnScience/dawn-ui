package org.dawnsci.plotting.system;

import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.nebula.visualization.xygraph.figures.Annotation;
import org.eclipse.nebula.visualization.xygraph.figures.IXYGraph;
import org.eclipse.nebula.visualization.xygraph.figures.Trace;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

// NOTE this is a lightly modified copy from xygraph 3.1.0 with embedded messages to work around an
// issue with AnnotationConfigPage setting of the font in its constructor

/**
 * The dialog for adding annotation.
 * 
 * @author Xihui Chen
 * @author Kay Kasemir Initial defaults
 */
public class AddAnnotationDialog extends Dialog {
	private AnnotationConfigPage configPage;

	private static final String Annotation_Add = "Add Annotation";
	private static final String Annotation_DefaultNameFmt = "Annotation %d";

	/**
	 * Construct an annotation dialog for the given XYGraph
	 *
	 * @param parentShell
	 *         parent shell of the dialog
	 * @param xyGraph
	 *         the XYGraph where the annotation is
	 */
	public AddAnnotationDialog(final Shell parentShell, final IXYGraph xyGraph) {
		super(parentShell);

		// Allow resize
		setShellStyle(getShellStyle() | SWT.RESIZE);

		// Unique annotation names help when trying to edit/delete annotations.
		// Default name: Annotation 1, Annotation 2, ...
		final int num = xyGraph.getPlotArea().getAnnotationList().size();
		final String name = Annotation_DefaultNameFmt.formatted(num + 1);

		// If there are traces, default to 'snapping' to the first trace
		final Annotation annotation;
		final List<Trace> traces = xyGraph.getPlotArea().getTraceList();
		if (traces.size() > 0)
			annotation = new Annotation(name, traces.get(0));
		else
			annotation = new Annotation(name, xyGraph.getPrimaryXAxis(), xyGraph.getPrimaryYAxis());

		// Need to add this here as the config page grabs the font in its constructor
		AnnotationWrapper.applyAnnotationPreferences(annotation);

		// Allow user to tweak the settings
		configPage = new AnnotationConfigPage(xyGraph, annotation);
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Annotation_Add);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		final Composite parent_composite = (Composite) super.createDialogArea(parent);
		final Composite composite = new Composite(parent_composite, SWT.NONE);
		configPage.createPage(composite);
		return parent_composite;
	}

	@Override
	protected void okPressed() {
		configPage.applyChanges();
		super.okPressed();
	}

	/**
	 * @return the annotation
	 */
	public Annotation getAnnotation() {
		return configPage.getAnnotation();
	}
}
