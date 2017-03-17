package org.dawnsci.plotting.tools.finding;

import java.util.List;

import org.dawb.common.ui.util.EclipseUtils;
import org.dawb.common.ui.wizard.ResourceChoosePage;
import org.dawnsci.plotting.views.ToolPageView;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.dawnsci.plotting.api.tool.IToolContainer;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbench;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.fitting.functions.IdentifiedPeak;

/**
 * @author Dean P. Ottewell
 */
public class PeakFindingExportWizard extends Wizard implements IExportWizard {

	public static final String ID = "org.dawb.workbench.plotting.peakFindingExportWizard";

	private static final Logger logger = LoggerFactory.getLogger(PeakFindingExportWizard.class);

	private static IPath  containerFullPath;
	private static String staticFileName;
	
	ExportPeaksPage exportPage;
	
	List<IdentifiedPeak> peaks;
	
	public PeakFindingExportWizard() {
		super();
		this.exportPage = new ExportPeaksPage();
		exportPage.setDescription("Please choose the location of the file to export. This file will be in a .xy format");
		addPage(exportPage);
		setWindowTitle("Export Found Peaks");
	}
	
	
	public PeakFindingExportWizard(List<IdentifiedPeak> peaks) {
		super();
		this.exportPage = new ExportPeaksPage();
		exportPage.setDescription("Please choose the location of the file to export. This file will be in a .xy format");
		addPage(exportPage);
		setWindowTitle("Export Found Peaks");
		this.peaks = peaks;
	}


	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
	}

	@Override
	public boolean performFinish() {

		final IViewPart view = (IViewPart)EclipseUtils.getPage().findView("org.dawb.workbench.plotting.views.toolPageView.1D");
		if (!(view instanceof IToolContainer))
			return false;

		final IToolContainer container = (IToolContainer)view;
		final IViewReference viewFixed = (IViewReference)EclipseUtils.getPage().findViewReference(ToolPageView.FIXED_VIEW_ID, "org.dawb.workbench.plotting.tools.PeakFindingTool");
		if (viewFixed != null && !(viewFixed.getView(false) instanceof IToolContainer))
			return false;
		final IToolContainer containerFixed = viewFixed != null ? (IToolContainer)viewFixed.getView(false) : null;

		boolean isFixed = false, isPeakToolActive = false;
		if (view != null && container.getActiveTool()!=null && (container.getActiveTool() instanceof PeakFindingTool)) {
			isFixed = false;
			isPeakToolActive = true;
		}
		if (viewFixed != null && containerFixed != null && containerFixed.getActiveTool()!=null && (containerFixed.getActiveTool() instanceof PeakFindingTool)) {
			isFixed = true;
			isPeakToolActive = true;
		}
		if (!isPeakToolActive) {
			MessageDialog
					.openError(
							Display.getDefault().getActiveShell(),
							"Cannot find active Peak Finding Tool",
							"Cannot find a peak finding tool to export the found peaks from.\n\n"
									+ "Please ensure that there is a peak tool active with some\n"
									+ "peaks in the peak table.");
			return false;
		}
		
		containerFullPath = Path.fromOSString(exportPage.getAbsoluteFilePath());
		staticFileName    = exportPage.getFileLabel();
	
		try {
			PeakFindingTool tool = ((PeakFindingTool)container.getActiveTool());
			tool.manager.exportFoundPeaks(containerFullPath.toOSString(), tool.getPeaksId());
		} catch (Exception e) {
			logger.error("Cannot export peaks", e);
		}

		return true;
	}

	public class ExportPeaksPage extends ResourceChoosePage {

		public ExportPeaksPage() {
			super("wizardPage", "Page exporting peak data", null);
			setTitle("Export peak data to file");
			setDirectory(true);
			
			this.setDirectory(false);
			this.setNewFile(true);
			this.setPathEditable(true);
		}
		
	}

}
