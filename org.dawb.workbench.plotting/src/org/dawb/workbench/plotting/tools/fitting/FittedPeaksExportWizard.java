package org.dawb.workbench.plotting.tools.fitting;

import java.io.File;

import org.dawb.common.ui.util.EclipseUtils;
import org.dawb.workbench.plotting.views.ToolPageView;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FittedPeaksExportWizard extends Wizard implements IExportWizard {

	public static final String ID = "org.dawb.workbench.plotting.fittedPeaksExportWizard";
	
	private static final Logger logger = LoggerFactory.getLogger(FittedPeaksExportWizard.class);
	
	public FittedPeaksExportWizard() {
		super();
		addPage(new ExportPage("Fitted Peaks"));
		setWindowTitle("Export fitted peaks to CSV");
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
	}

	@Override
	public boolean performFinish() {
		
		final ToolPageView view = (ToolPageView)EclipseUtils.getPage().findView("org.dawb.workbench.plotting.views.toolPageView.1D");
		if (view == null || view.getCurrentPage()==null || !(view.getCurrentPage() instanceof FittingTool)) {
			MessageDialog.openError(Display.getDefault().getActiveShell(), "Cannot find active Fit Tool",
					                "Cannot find a fitting tool to export the fitted peaks from.\n\n"+
			                        "Please ensure that there is a fitting tool active with some\n"+
					                "peaks in the peak table.");
			return false;
		}
		
		final ExportPage ep = (ExportPage)getPages()[0];
		try {
			((FittingTool)view.getCurrentPage()).exportFittedPeaks(ep.getPath());
		} catch (Exception e) {
			logger.error("Cannot export peaks", e);
		}
		
		return true;
	}
	
	private final class ExportPage extends WizardPage {

		private Text    txtPath;
		private boolean overwrite = false;
		private String  path;

		protected ExportPage(String pageName) {
			super(pageName);
		}

		@Override
		public void createControl(Composite parent) {
			
			Composite container = new Composite(parent, SWT.NULL);
			GridLayout layout = new GridLayout();
			container.setLayout(layout);
			layout.numColumns = 3;
			layout.verticalSpacing = 9;

			Label label = new Label(container, SWT.NULL);
			label.setText("&File  ");
			txtPath = new Text(container, SWT.BORDER);
			txtPath.setEditable(true);
			txtPath.setEnabled(true);
			GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
			txtPath.setLayoutData(gd);
			txtPath.addModifyListener(new ModifyListener() {			
				@Override
				public void modifyText(ModifyEvent e) {
					pathChanged();
				}
			});

			Button button = new Button(container, SWT.PUSH);
			button.setText("...");
			button.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					handleBrowse();
				}
			});
			
			final Button over = new Button(container, SWT.CHECK);
			over.setText("Overwrite file if it exists.");
			over.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					overwrite = over.getSelection();
					pathChanged();
				}
			});

			pathChanged();
			setControl(container);

		}
		
		String getPath() {
			return path;
		}

		/**
		 * Uses the standard container selection dialog to choose the new value for the container field.
		 */

		private void handleBrowse() {
			FileDialog dirDialog = new FileDialog(getShell(), SWT.OPEN);
			dirDialog.setText("Export file (CSV)");
			if (getPath()!=null) {
				try {
					dirDialog.setFilterPath((new File(getPath())).getParent());
				} catch (Throwable ne) {
					logger.error("Cannot set path in file filter!", ne);
				}
			}
			dirDialog.setFilterExtensions(new String[]{"*.csv"});
			final String filepath = dirDialog.open();
			if (filepath != null) {
				txtPath.setText(filepath);
				pathChanged();
			}
		}

		/**
		 * Ensures that both text fields are set.
		 */

		private void pathChanged() {

            final String p = txtPath.getText();
			if (p==null || p.length() == 0) {
				updateStatus("Please select a file to export to.");
				return;
			}
			if (((new File(p)).exists() || (!p.toLowerCase().endsWith(".csv") && (new File(p+".csv")).exists())) 
					&& !overwrite) {
				updateStatus("Please confirm overwrite of the file.");
				return;
			}
			this.path = p;
			updateStatus(null);
		}

		private void updateStatus(String message) {
			setErrorMessage(message);
			setPageComplete(message == null);
		}

	}

}
