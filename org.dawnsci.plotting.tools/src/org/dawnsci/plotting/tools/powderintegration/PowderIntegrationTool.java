/*
 * Copyright (c) 2012, 2017 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.tools.powderintegration;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

import org.dawb.common.ui.menu.MenuAction;
import org.dawb.common.ui.util.EclipseUtils;
import org.dawnsci.common.widgets.dialog.FileSelectionDialog;
import org.dawnsci.plotting.tools.Activator;
import org.dawnsci.plotting.tools.diffraction.DiffractionUtils;
import org.eclipse.dawnsci.analysis.api.diffraction.DetectorProperties;
import org.eclipse.dawnsci.analysis.api.io.ILoaderService;
import org.eclipse.dawnsci.analysis.api.metadata.IDiffractionMetadata;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.dawnsci.plotting.api.tool.AbstractToolPage;
import org.eclipse.dawnsci.plotting.api.tool.IToolPageSystem;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITraceListener;
import org.eclipse.dawnsci.plotting.api.trace.TraceEvent;
import org.eclipse.dawnsci.plotting.api.trace.TraceWillPlotEvent;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.metadata.IMetadata;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.IPageSite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.osgi.services.ServiceProvider;
import uk.ac.diamond.scisoft.analysis.diffraction.powder.AbstractPixelIntegration;
import uk.ac.diamond.scisoft.analysis.diffraction.powder.PixelIntegrationUtils.IntegrationMode;
import uk.ac.diamond.scisoft.analysis.io.NexusDiffractionCalibrationReader;
import uk.ac.diamond.scisoft.analysis.roi.XAxis;

public class PowderIntegrationTool extends AbstractToolPage {
	private static final Logger logger = LoggerFactory.getLogger(PowderIntegrationTool.class);
	private IPlottingSystem<Composite> system;
	private ITraceListener traceListener;
	private IDiffractionMetadata metadata;
	private PowderIntegrationJob fullImageJob;
	private Label statusMessage;
	String[] statusString;
	ILoaderService service;
	boolean correctSolidAngle = false;
	IDiffractionMetadata importedMeta;
	SashForm sashForm;
	TableViewer viewer;
	PowderIntegrationModel model;
	PowderCorrectionModel corModel;
	IntegrationSetupWidget integratorSetup;
	String lastPath = null;

	public PowderIntegrationTool() {
		try {
			system = PlottingFactory.createPlottingSystem();
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

		this.traceListener = new ITraceListener.Stub() {
			@Override
			public void traceWillPlot(TraceWillPlotEvent evt) {
				PowderIntegrationTool.this.update(evt.getImage());
			}
			@Override
			public void traceRemoved(TraceEvent evt) {
				if (system!= null) system.clear();
			}
		};
		
		this.service = ServiceProvider.getService(ILoaderService.class);
		statusString = new String[1];
	}

	@Override
	public ToolPageRole getToolPageRole() {
		return ToolPageRole.ROLE_2D;
	}

	@Override
	public void activate() {
		if (isActive()) return;
		super.activate();
		getPlottingSystem().addTraceListener(traceListener);

		IImageTrace im = getImageTrace();

		if (im != null && im.getData() != null) update(im.getData()); 
	}

	@Override
	public void deactivate() {
		getPlottingSystem().removeTraceListener(traceListener);
		super.deactivate();
	}

	@Override
	public void createControl(Composite parent) {
		
		model = new PowderIntegrationModel();
		corModel = new PowderCorrectionModel();
		
		sashForm = new SashForm(parent, SWT.VERTICAL);
		Composite base = new Composite(sashForm, SWT.NONE);
		base.setLayout(new GridLayout(1,true));
		createActions();
		
		final IPageSite site = getSite();
		IActionBars actionbars = site!=null?site.getActionBars():null;

		system.createPlotPart(base, 
				getTitle(), 
				actionbars, 
				PlotType.XY,
				this.getViewPart());

		system.getSelectedYAxis().setAxisAutoscaleTight(true);
		system.getPlotComposite().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		getPlottingSystem().addTraceListener(traceListener);
		
		statusMessage = new Label(base, SWT.WRAP);
		statusMessage.setText("Status...");
		statusMessage.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
		statusMessage.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_DARK_GRAY));
		statusMessage.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
		
		//Call update here so we get the metadata
		update(null);
		
		CTabFolder folder = new CTabFolder (sashForm, SWT.NONE);
		folder.setBorderVisible(true);
		folder.setSimple(true);
		CTabItem tab1 = new CTabItem(folder, SWT.NONE);
	    tab1.setText("Integration Options");
		
		Composite setupComposite = new Composite(folder, SWT.None);
		setupComposite.setLayout(new GridLayout());
		Composite widget1 = new Composite(setupComposite, SWT.NONE);
		
		int longest = 1000;
		if (metadata != null) {
			int[] shape = new int[]{metadata.getDetector2DProperties().getPy(), metadata.getDetector2DProperties().getPx()};
			longest = AbstractPixelIntegration.calculateNumberOfBins(metadata.getDetector2DProperties().getBeamCentreCoords(), shape);
		}
		
		integratorSetup = new IntegrationSetupWidget(widget1, model,longest);

		integratorSetup.enableFor1D(true);
		tab1.setControl(setupComposite);
		
		model.addPropertyChangeListener(new PropertyChangeListener() {
			
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if (fullImageJob !=  null) {
					fullImageJob.cancel();
				}
				update(null);
			}
		});
		
		CTabItem tab2 = new CTabItem(folder, SWT.NONE);
		tab2.setText("Correction Options");
		
		Composite widget2 = new Composite(folder, SWT.None);
		
		new PowderCorrectionWidget(widget2, corModel);
		tab2.setControl(widget2);
		
		corModel.addPropertyChangeListener(new PropertyChangeListener() {
			
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if (fullImageJob !=  null) {
					fullImageJob.cancel();
				}
				update(null);
			}
		});
		folder.setSelection(0);
		sashForm.setWeights(new int[]{100,0});

		super.createControl(parent);
	}
	
	private void createActions() {
		
		final Action showOptions = new Action("Show Advanced Options", Action.AS_CHECK_BOX) {
			@Override
			public void run() {
				if (isChecked()) {
					sashForm.setWeights(new int[]{50,50});
				} else {
					sashForm.setWeights(new int[]{100,0});
				}
			}
		};
		
		showOptions.setImageDescriptor(Activator.getImageDescriptor("icons/graduation-hat.png"));
		
		final MenuAction modeSelect= new MenuAction("Select Mode");
		
		final Action nonAction = new Action("Non pixel splitting") {
			@Override
			public void run() {
				model.setIntegrationMode(IntegrationMode.NONSPLITTING);
				modeSelect.setSelectedAction(this);
			}
		};
		
		nonAction.setImageDescriptor(Activator.getImageDescriptor("icons/pixel.png"));
		
		final Action splitAction = new Action("Pixel splitting") {
			@Override
			public void run() {
				model.setIntegrationMode(IntegrationMode.SPLITTING);
				modeSelect.setSelectedAction(this);
			}
		};
		
		splitAction.setImageDescriptor(Activator.getImageDescriptor("icons/splitPixel.png"));
		
		final Action split2DAction = new Action("Pixel splitting 2D") {
			@Override
			public void run() {
				model.setIntegrationMode(IntegrationMode.SPLITTING2D);
				modeSelect.setSelectedAction(this);
			}
		};
		split2DAction.setImageDescriptor(Activator.getImageDescriptor("icons/splitCake.png"));
		
		final Action nonSplit2DAction = new Action("Non pixel splitting 2D") {
			@Override
			public void run() {
				model.setIntegrationMode(IntegrationMode.NONSPLITTING2D);
				modeSelect.setSelectedAction(this);
			}
		};
		nonSplit2DAction.setImageDescriptor(Activator.getImageDescriptor("icons/cake.png"));
		
		final MenuAction axisSelect= new MenuAction("Select Axis");

		final Action qAction = new Action("Q") {
			@Override
			public void run() {
				model.setAxisType(XAxis.Q);
				axisSelect.setSelectedAction(this);
			}
		};

		final Action tthAction = new Action("2\u03b8") {
			@Override
			public void run() {
				model.setAxisType(XAxis.ANGLE);
				axisSelect.setSelectedAction(this);
			}

		};
		
		final Action dAction = new Action("d") {
			@Override
			public void run() {
				model.setAxisType(XAxis.RESOLUTION);
				axisSelect.setSelectedAction(this);
			}

		};
		
		final Action pixelAction = new Action("pixel") {
			@Override
			public void run() {
				model.setAxisType(XAxis.PIXEL);
				axisSelect.setSelectedAction(this);
			}

		};
		
		final Action clearImported = new Action("Clear imported metadata") {
			@Override
			public void run() {
				importedMeta = null;
				fullImageJob = null;
				this.setEnabled(false);
				update(null);
			}
		};
		
		clearImported.setEnabled(false);
		
		final MenuAction loadMeta= new MenuAction("Experimental metadata");
		loadMeta.setImageDescriptor(Activator.getImageDescriptor("icons/detectorlight.png"));
		final Action loadMetaAction = new Action("Import metadata from NeXus file") {
			@Override
			public void run() {
				try {

					FileSelectionDialog dialog = new FileSelectionDialog(Display.getDefault().getActiveShell());
					dialog.setNewFile(false);
					dialog.setFolderSelector(false);
					if (lastPath != null) {
						File f = new File(lastPath);
						if (!f.isDirectory()) {
							lastPath = f.getParent();
						}
						dialog.setPath(lastPath);
					} else {
						dialog.setPath(System.getProperty("user.home"));
					}
					
					dialog.create();
					if (dialog.open() == Dialog.CANCEL ) return;
					lastPath = dialog.getPath();
					
					IDiffractionMetadata md = NexusDiffractionCalibrationReader.getDiffractionMetadataFromNexus(dialog.getPath(), null, null);
				
					if (md != null) {
						importedMeta = md;
						fullImageJob = null;
						logger.debug("meta loaded from file");
						clearImported.setEnabled(true);
						system.clear();
						update(null);
					} else {
						MessageDialog.openError(
								getViewPart().getSite().getShell(),
								"Error!",
								"Metadata could not be read from file!");
					}

				} catch (Exception e) {
					MessageDialog.openError(
							getViewPart().getSite().getShell(),
							"Error!",
							"Metadata could not be read from file: " + e.getMessage());
				}
				return;
			}

		};
		
		loadMetaAction.setImageDescriptor(Activator.getImageDescriptor("icons/import_wiz.png"));
		clearImported.setImageDescriptor(Activator.getImageDescriptor("icons/delete.gif"));
		
		loadMeta.add(loadMetaAction);
		loadMeta.add(clearImported);
		
		modeSelect.add(nonAction);
		modeSelect.add(splitAction);
		modeSelect.add(split2DAction);
		modeSelect.add(nonSplit2DAction);
		modeSelect.setSelectedAction(nonAction);
		
		axisSelect.add(qAction);
		axisSelect.add(tthAction);
		axisSelect.add(dAction);
		axisSelect.add(pixelAction);
		axisSelect.setSelectedAction(qAction);
		
		getSite().getActionBars().getToolBarManager().add(showOptions);
		getSite().getActionBars().getMenuManager().add(showOptions);
		
		getSite().getActionBars().getToolBarManager().add(modeSelect);
		getSite().getActionBars().getMenuManager().add(modeSelect);
		
		getSite().getActionBars().getToolBarManager().add(axisSelect);
		getSite().getActionBars().getMenuManager().add(axisSelect);
		
		getSite().getActionBars().getToolBarManager().add(loadMeta);
		getSite().getActionBars().getMenuManager().add(loadMeta);
	}

	@Override
	public Control getControl() {
		return sashForm;
	}

	@Override
	public void dispose() {
		//can hold lots of data so get rid of it
		fullImageJob = null;
		corModel = null;
		model = null;
		super.dispose();
	}

	@Override
	public void setFocus() {
		if (system != null) system.setFocus();
	}
	
	private void update(IDataset ds) {
		
		if (system == null) return; 
		if (system.getPlotComposite() == null) return;
		
		IImageTrace im = getImageTrace();
		if (ds == null && im == null) return;
		if (ds == null && im != null) ds = im.getData();
		
		logger.debug("Update");
		
		//Imported metadata first
		if (importedMeta != null) {
			DetectorProperties d = importedMeta.getDetector2DProperties();
			if(d.getPx() != ds.getShape()[1] || d.getPy() != ds.getShape()[0])  {
				statusMessage.setText("Data shape not compatible with current metadata!!!");
				statusMessage.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_RED));
			} else {
				statusMessage.setText("Using imported metadata");
				statusMessage.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_DARK_GRAY));
			}
			if (fullImageJob == null) {
				fullImageJob = new PowderIntegrationJob(importedMeta, system);
			}
		} else {
			
			IDiffractionMetadata m = getUpdatedMetadata(ds, statusString);
			
			if (m == null && metadata == null) return;

			if (metadata == null) {
				metadata = m;
				fullImageJob = new PowderIntegrationJob(metadata, system);
			} else {
				if (m != null && (!metadata.getDetector2DProperties().equals(m.getDetector2DProperties()) ||
						!metadata.getDiffractionCrystalEnvironment().equals(m.getDiffractionCrystalEnvironment()))) {
					metadata = m;
					fullImageJob = new PowderIntegrationJob(metadata, system);
					statusMessage.setText("Meta data updated");
					statusMessage.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_DARK_GRAY));
				}
			}
			
			if (fullImageJob == null) {
				fullImageJob = new PowderIntegrationJob(metadata, system);
			}
		}
		
		Dataset mask = null;
		
		if (im != null) mask = DatasetUtils.convertToDataset(im.getMask());
		
		fullImageJob.setData(DatasetUtils.convertToDataset(ds),
				mask,model, corModel);
		
		
		fullImageJob.schedule();
	}
	
	private IDiffractionMetadata getUpdatedMetadata(IDataset ds, String[] statusString) {
		//look in data set
		IDiffractionMetadata m = ds.getFirstMetadata(IDiffractionMetadata.class);
		if (m != null) {
			statusMessage.setText("Metadata from data set");
			statusMessage.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_DARK_GRAY));
		}

		//read from preferences first time
		if (m == null && metadata == null) m = getDiffractionMetaData(ds);

		if (m != null) {
			if (statusString[0] != null) {
				statusMessage.setText(statusString[0]);
				statusString[0] = null;
			}
		}
		return m;
	}

	private IDiffractionMetadata getDiffractionMetaData(IDataset image) {
		IWorkbenchPart part = getPart();
		String altPath = null;
		if(part instanceof IEditorPart){
			altPath = EclipseUtils.getFilePath(((IEditorPart) part).getEditorInput());
		} else if (part instanceof IViewPart){
			try {
				if (image == null) return null;
				IMetadata md = image.getFirstMetadata(IMetadata.class);
				if(md != null)
					altPath = md.getFilePath();
			} catch (Exception e) {
				logger.debug("Exception getting the image metadata", e);
			}
		}
		return DiffractionUtils.getDiffractionMetadata(image, altPath, service, statusString);
	}

	@Override
	public Object getAdapter(@SuppressWarnings("rawtypes") Class clazz) {
		if (clazz == IToolPageSystem.class) {
			return system;
		} else {
			return super.getAdapter(clazz);
		}
	}
}
