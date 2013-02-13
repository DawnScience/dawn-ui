package org.dawnsci.plotting.tools.reduction;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import ncsa.hdf.object.Group;

import org.dawb.common.ui.monitor.ProgressMonitorWrapper;
import org.dawb.common.ui.plot.tool.IDataReductionToolPage;
import org.dawb.common.ui.plot.tool.IDataReductionToolPage.DataReductionInfo;
import org.dawb.common.ui.plot.tool.IDataReductionToolPage.DataReductionSlice;
import org.dawb.common.ui.slicing.DimsDataList;
import org.dawb.common.ui.slicing.SliceUtils;
import org.dawb.common.ui.util.EclipseUtils;
import org.dawb.hdf5.HierarchicalDataFactory;
import org.dawb.hdf5.IHierarchicalDataFile;
import org.dawb.hdf5.Nexus;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.common.ui.dialogs.WorkspaceResourceDialog;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.io.IMetaData;
import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;
import uk.ac.diamond.scisoft.analysis.io.SliceObject;

public class DataReductionWizard extends Wizard implements IExportWizard {

	public static final String ID = "org.dawb.workbench.plotting.dataReductionExportWizard";
	
	private static final Logger logger = LoggerFactory.getLogger(DataReductionWizard.class);
	
	private IFile                  source;
	private List<String>           h5Selections;
	private String                 h5Path;
	private IDataReductionToolPage dataReductionPage;
	private DimsDataList           sliceData;

	private Map<Integer, String> nexusAxes;
	
	public DataReductionWizard() {
		super();
		addPage(new ReductionPage("Data Reduction"));
		setWindowTitle("Export Reduced Data");
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
	}

	@Override
	public boolean performFinish() {
		
		 try {
			 getContainer().run(true, true, new IRunnableWithProgress() {

				 @Override
				 public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {

					 final ReductionPage rp = (ReductionPage)getPages()[0];
					
					 // We write to the reduction file here:
					 final IFile      export = rp.getPath();
					 
					 IMetaData meta;
					 try {
						 meta = LoaderFactory.getMetaData(source.getLocation().toOSString(), new ProgressMonitorWrapper(monitor));
					 } catch (Exception e1) {
						 logger.error("Cannot expand slices required!", e1);
						 return;
					 }

					 final String     regexPath     = rp.getH5Path();
					 final Collection<String> paths = new ArrayList<String>(7);
					 final Collection<String> names = meta.getDataNames();
					 for (String path : names) {
						 if (path.matches(regexPath)) paths.add(path);
					 }

					 IHierarchicalDataFile hf = null;
					 
					 try {						 
						 export.getParent().refreshLocal(IResource.DEPTH_ONE, monitor);
						 if (export.exists()) {
							 export.delete(true, monitor);
						 }
						 
						 hf = HierarchicalDataFactory.getWriter(export.getLocation().toOSString());
						 
						 // Generate 
						 for (String path : paths) {
							 
							 
							 final String[] rps  = 	path.split("/");
							 Group group=(Group)hf.getData(path);
							 
							 if (group==null) for (String stub : rps) {
								 if (stub==null || "".equals(stub)) continue;
								 if (group==null) {
									 group = hf.group(stub);
								 } else {
									 group = hf.group(stub, group);
								 }
								 hf.setNexusAttribute(group, Nexus.DATA);
						 	 }
							 
							 final int id = group.open();
							 
							 logger.info("Running stack tool on '"+path+"' in '"+source.getName()+"'");
							 // Generate slice data
							 final DimsDataList dl   = getSliceData();

							 final int[]             shape  = meta.getDataShapes().get(path);
							 List<SliceObject> slices = null;
							 try {
								 slices = SliceUtils.getExpandedSlices(shape, dl);
							 } catch (Throwable ne) {
								 Display.getDefault().syncExec(new Runnable() {
									 public void run() {
										 MessageDialog.openWarning(Display.getDefault().getActiveShell(), 
												 "Cannot run data reduction tool",
												 "The data reduction tool can only run on:\n"+
														 "   - 1D data being extracted from a 2D stack or\n"+
														 "   - 2D data being extracted from a 3D stack\n"+
														 "in this version of Dawn.\n\n"+
												 "Please contact your support representative for further information.");
									 }

								 });
								 rp.setOpen(false);
								 return;
							 }

							 monitor.beginTask("Reduction '"+getTool().getTitle()+"' on "+path, slices.size()*4);

							 // The axes the user extracted the data in (null, just x or x and y)
							 final SliceObject so = slices.get(0);
							 so.setPath(source.getLocation().toOSString());
							 so.setName(path);
							 final List<AbstractDataset> axes = getAxes(so, monitor);

							 // Iterate slice data
							 Object userData = null;
							 for (SliceObject slice : slices) {
								 slice.setPath(source.getLocation().toOSString());
								 slice.setName(path);

								 // y Data, in indices
								 final AbstractDataset set = SliceUtils.getSlice(slice, monitor);

								 DataReductionSlice sliceInfo = new DataReductionSlice(hf, group, set, userData, monitor);
								 sliceInfo.setAxes(axes);

								 DataReductionInfo info = getTool().export(sliceInfo);
								 monitor.worked(1);
								 if (info.getStatus().isOK()) userData = info.getUserData();

								 if (monitor.isCanceled()) break;
							 }

							 group.close(id);
							 monitor.done();
						 }
					 } catch (final Exception ne) {
						 
			            logger.error("Cannot run export process for data reduction from tool '"+getTool().getTitle()+"'", ne);
			 			Display.getDefault().syncExec(new Runnable() {
			 				public void run() {
			 					ErrorDialog.openError(Display.getDefault().getActiveShell(),
							              "Data Not Exported", 
							              "Cannot run export process for data reduction from tool "+getTool().getTitle()+".\n\nPlease contact your support representative.",
							              new Status(IStatus.WARNING, "org.edna.workbench.actions", ne.getMessage(), ne));
			 				}
			 			});
						rp.setOpen(false);

					 } finally {
						 
						 try {
							 if (hf!=null) hf.close();
	
							 monitor.done();
							 export.getParent().refreshLocal(IResource.DEPTH_ONE, monitor);
						 } catch (Exception ne) {
							 logger.error("Cannot run export process for data reduction from tool "+getTool().getTitle(), ne);
							 return;
						 }
						 
						 if (rp.isOpen()) {
							 Display.getDefault().asyncExec(new Runnable() {
								 public void run() {
									 try {
										 EclipseUtils.openEditor(rp.getPath());
									 } catch (PartInitException e) {
										 logger.error("Opening file "+rp.getPath(), e);
									 }
								 }
							 });
						 }

					 }
				 }
			 });
		 } catch (Exception ne) {
             logger.error("Cannot run export process for data reduction from tool "+getTool().getTitle(), ne);
		 }
	 
		 return true;
	}
	
	protected final List<AbstractDataset> getAxes(SliceObject slice, IProgressMonitor monitor) throws Exception {
		
		slice.setNexusAxes(nexusAxes);
		if (slice.getNexusAxes()==null || slice.getNexusAxes().isEmpty()) return null;
		int[] shape = slice.getSlicedShape();
		if (shape.length==1) {
			final AbstractDataset x = SliceUtils.getNexusAxis(slice, shape[0], slice.getX()+1, false, monitor);
			if (x==null) return null;
			return Arrays.asList(x);
		} else if (shape.length==2) {
			final AbstractDataset x = SliceUtils.getNexusAxis(slice, shape[1], slice.getX()+1, false, monitor);
			final AbstractDataset y = SliceUtils.getNexusAxis(slice, shape[0], slice.getY()+1, false, monitor);
			return Arrays.asList(x,y);
		}
		return null;
	}

	public boolean needsProgressMonitor() {
		return true;
	}
		
	private static IContainer exportFolder = null;
	private final class ReductionPage extends WizardPage {

		private Label   txtLabel;
		private Text    txtPath;
		private boolean overwrite = false;
		private boolean open      = true;
		private IFile   path;

		protected ReductionPage(String pageName) {
			super(pageName);
		}

		public void setOpen(boolean b) {
			open = b;
		}

		@Override
		public void createControl(Composite parent) {
			
			Composite container = new Composite(parent, SWT.NULL);
			GridLayout layout = new GridLayout();
			container.setLayout(layout);
			layout.numColumns = 3;
			layout.verticalSpacing = 9;

			Label label = new Label(container, SWT.NULL);
			label.setText("Data File ");

			label = new Label(container, SWT.NULL);
			label.setText("'"+getSource().getLocation().toOSString()+"'");
			label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
			
			label = new Label(container, SWT.NULL);
			label.setText("Data ");

			final CCombo dataChoice = new CCombo(container, SWT.NONE);
			dataChoice.setItems(getSelections().toArray(new String[getSelections().size()]));
			dataChoice.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			dataChoice.select(0);
			h5Path = getSelections().get(0);
			dataChoice.addModifyListener(new ModifyListener() {			
				@Override
				public void modifyText(ModifyEvent e) {
					h5Path = dataChoice.getText();
					pathChanged();
				}
			});
			dataChoice.setToolTipText("Choose the data name or names to run the tool over.\nYou may enter regular expressions, for instance use '.+' as wildcard to match 1 or more characters.\nUse '\\d+' to match 1 or more numbers.");
			dataChoice.setEditable(false);
			
			final Button editable = new Button(container, SWT.CHECK);
			editable.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			editable.setToolTipText("Click to enter a regular expression for dataset name.");
			editable.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					dataChoice.setEditable(editable.getSelection());
				}
			});
			
			label = new Label(container, SWT.NULL);
			label.setText("Slice ");

			label = new Label(container, SWT.NULL);
			label.setText(getSliceText());
			label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));


			label = new Label(container, SWT.NULL);
			label.setText("Tool ");

			label = new Label(container, SWT.NULL);
			label.setText("'"+getTool().getTitle()+"'");
			label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
			
			txtLabel = new Label(container, SWT.NULL);
			txtLabel.setText("Export &File  ");
			txtPath = new Text(container, SWT.BORDER);
			txtPath.setEditable(false);
			txtPath.setEnabled(false);
			txtPath.setText(getPath().getFullPath().toOSString());
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
			over.setSelection(false);
			over.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					overwrite = over.getSelection();
					pathChanged();
				}
			});
			over.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));
			
			final Button open = new Button(container, SWT.CHECK);
			open.setText("Open file after export.");
			open.setSelection(true);
			open.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					ReductionPage.this.open = open.getSelection();
					pathChanged();
				}
			});
			open.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));


			pathChanged();
			setControl(container);

		}
		
		IFile getPath() {
			if (path==null) { // We make one up from the source
				IFile source = getSource();
				final String strPath = source.getName().substring(0, source.getName().indexOf("."))+
						               "_"+getShortToolName()+"_reduction.h5";
				if (exportFolder == null) {
				    this.path = source.getParent().getFile(new Path(strPath));
				} else {
				    this.path = exportFolder.getFile(new Path(strPath));
					
				}
			}
			return path;
		}

		/**
		 * Uses the standard container selection dialog to choose the new value for the container field.
		 */

		private void handleBrowse() {
			final IFile p = WorkspaceResourceDialog.openNewFile(PlatformUI.getWorkbench().getDisplay().getActiveShell(), 
					"Export location", "Please choose a location to export the reduced data to. This must be a hdf5 file.", 
					getPath().getFullPath(), null);
			if (p!=null) {
				this.path = p;
			    txtPath.setText(this.path.getFullPath().toOSString());
			    exportFolder = p.getParent();
			}
			pathChanged();
		}

		/**
		 * Ensures that both text fields are set.
		 */

		private void pathChanged() {

            final String p = txtPath.getText();
            txtLabel.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
			if (p==null || p.length() == 0) {
				updateStatus("Please select a file to export to.");
				return;
			}
			if (getPath().exists() && (!getPath().isAccessible() || getPath().isReadOnly())) {
				updateStatus("Please choose another location to export to; this one is read only.");
				txtLabel.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_RED));
				return;
			}
			if (getPath().exists() && !overwrite) {
				updateStatus("Please confirm overwrite of the file.");
				return;
			}
			if (!getPath().getName().toLowerCase().endsWith(".h5")) {
				updateStatus("Please set the file name to export as a file with the extension 'h5'.");
				return;
			}
			if (getTool().getPlottingSystem().getRegions().size()<1) {
				updateStatus("Please make a selection using '"+getTool().getTitle()+"' before running data reduction.");
				return;
				
			}
			
			// Check data name is in file.
			try {
				IMetaData meta = LoaderFactory.getMetaData(source.getLocation().toOSString(), null);
				final Collection<String> names = meta.getDataNames();
				boolean foundMatchingName = false;
				for (String namePath : names) {
					if (namePath.matches(h5Path)) foundMatchingName=true;
				}
				if (!foundMatchingName) {
					updateStatus("The dataset name (or pattern) '"+h5Path+"' does not match data in the file "+getSource().getName());
					return;
				}
			} catch (Exception e) {
				updateStatus("Cannot read file "+getSource());
				return;
			}

			updateStatus(null);
		}

		private void updateStatus(String message) {
			setErrorMessage(message);
			setPageComplete(message == null);
		}

		public boolean isOpen() {
			return open;
		}

		public String getH5Path() {
			return h5Path;
		}

	}

	public IFile getSource() {
		return source;
	}

	public String getSliceText() {
		try {
			IMetaData meta = LoaderFactory.getMetaData(source.getLocation().toOSString(), null);
			final int[]    shape = meta.getDataShapes().get(h5Path);
			return sliceData.toString(shape);
		} catch (Exception e) {
			logger.error("Cannot extract meta data from file "+getSource(), e);
			return null;
		}
	}

	private String getShortToolName() {
		final IDataReductionToolPage page = getTool();
		if (page==null) return null;
		return page.getTitle().replace(' ', '_');
	}

	public void setSource(IFile filePath) {
		this.source = filePath;
	}

	public List<String> getSelections() {
		return h5Selections;
	}

	public void setSelections(List<String> selections) {
		this.h5Selections = selections;
	}

	public IDataReductionToolPage getTool() {
		return dataReductionPage;
	}

	public void setTool(IDataReductionToolPage dataReductionPage) {
		this.dataReductionPage = dataReductionPage;
		getPages()[0].setDescription("This wizard runs '"+getTool().getTitle()+"' over a stack of data. Please check the data to slice, "+
		                             "confirm the export file and then press 'Finish' to run the tool on each slice.");

	}

	public DimsDataList getSliceData() {
		return sliceData;
	}

	public void setSliceData(DimsDataList sliceData) {
		this.sliceData = sliceData;
	}

	public void setNexusAxes(Map<Integer, String> nexusAxes) {
		this.nexusAxes = nexusAxes;
	}

}
