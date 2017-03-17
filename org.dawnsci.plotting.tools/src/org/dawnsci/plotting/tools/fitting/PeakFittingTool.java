/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.tools.fitting;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.dawb.common.ui.image.IconUtils;
import org.dawb.common.ui.menu.CheckableActionGroup;
import org.dawb.common.ui.menu.MenuAction;
import org.dawb.common.ui.plot.tools.IDataReductionToolPage;
import org.dawb.common.ui.util.EclipseUtils;
import org.dawnsci.plotting.tools.Activator;
import org.dawnsci.plotting.tools.preference.FittingPreferencePage;
import org.eclipse.core.runtime.Status;
import org.eclipse.dawnsci.analysis.api.fitting.functions.IPeak;
import org.eclipse.dawnsci.analysis.dataset.roi.LinearROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.plotting.api.annotation.IAnnotation;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
import org.eclipse.dawnsci.plotting.api.region.IRegionListener;
import org.eclipse.dawnsci.plotting.api.region.RegionUtils;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace;
import org.eclipse.dawnsci.plotting.api.views.ISettablePlotView;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.IntegerDataset;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.fitting.FittingConstants;
import uk.ac.diamond.scisoft.analysis.fitting.Generic1DFitter;
import uk.ac.diamond.scisoft.analysis.fitting.functions.FunctionFactory;
import uk.ac.diamond.scisoft.analysis.fitting.functions.FunctionSquirts;
import uk.ac.diamond.scisoft.analysis.fitting.functions.IdentifiedPeak;

public class PeakFittingTool extends AbstractFittingTool implements IRegionListener, IDataReductionToolPage {

	private static final Logger logger = LoggerFactory.getLogger(PeakFittingTool.class);
	private MenuAction numberPeaks;
	private FittedPeaksInfo fittedPeaksInfo;

	public PeakFittingTool() {
		super();

		Activator.getPlottingPreferenceStore().addPropertyChangeListener(new IPropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				if (isActive()) {
					if (isInterestedProperty(event)) {
						if (isActive()) fittingJob.fit(false);
						
						if (FittingConstants.PEAK_NUMBER.equals(event.getProperty())) {
							final int ipeak = Activator.getPlottingPreferenceStore().getInt(FittingConstants.PEAK_NUMBER);
							if (numberPeaks!=null) {
								if (ipeak<11) {
									numberPeaks.setSelectedAction(ipeak-1);
									numberPeaks.setCheckedAction(ipeak-1, true);
								} else {
									numberPeaks.setSelectedAction(10);
									numberPeaks.setCheckedAction(10, true);
								}
							}
						}
					} else if (FittingConstants.INT_FORMAT.equals(event.getProperty())||
							   FittingConstants.REAL_FORMAT.equals(event.getProperty())){

						viewer.refresh();
					}
				}
 			}

			private boolean isInterestedProperty(PropertyChangeEvent event) {
				final String propName = event.getProperty();
				return FittingConstants.PEAK_NUMBER.equals(propName) ||
					   FittingConstants.SMOOTHING.equals(propName)   ||
					   FittingConstants.QUALITY.equals(propName);
			}
		});
		

	}

	@Override
	protected List<TableViewerColumn> createColumns(final TableViewer viewer) {
		
		PeakColumnComparator cc = new PeakColumnComparator();
		viewer.setComparator(cc);
		
		ColumnViewerToolTipSupport.enableFor(viewer,ToolTip.NO_RECREATE);

		List<TableViewerColumn> ret = new ArrayList<TableViewerColumn>(9);
		
        TableViewerColumn var   = new TableViewerColumn(viewer, SWT.LEFT, 0);
		var.getColumn().setText("Trace");
		var.getColumn().setWidth(80);
		var.setLabelProvider(new PeakLabelProvider(0));
		var.getColumn().addSelectionListener(getSelectionAdapter(var.getColumn(), 0, cc));
		ret.add(var);

		var   = new TableViewerColumn(viewer, SWT.LEFT, 1);
		var.getColumn().setText("Name");
		var.getColumn().setWidth(150);
		var.setLabelProvider(new PeakLabelProvider(1));
		var.getColumn().addSelectionListener(getSelectionAdapter(var.getColumn(), 1, cc));
		ret.add(var);
		
        var   = new TableViewerColumn(viewer, SWT.CENTER, 2);
		var.getColumn().setText("Position");
		var.getColumn().setWidth(100);
		var.setLabelProvider(new PeakLabelProvider(2));
		var.getColumn().addSelectionListener(getSelectionAdapter(var.getColumn(), 2, cc));
		ret.add(var);
		
        var   = new TableViewerColumn(viewer, SWT.CENTER, 3);
		var.getColumn().setText("Data");
		var.getColumn().setToolTipText("The nearest data value of the fitted peak.");
		var.getColumn().setWidth(100);
		var.setLabelProvider(new PeakLabelProvider(3));
		var.getColumn().addSelectionListener(getSelectionAdapter(var.getColumn(), 3, cc));
		ret.add(var);
		
		// Data Column not that useful, do not show unless property set.
		if (!Boolean.getBoolean("org.dawb.workbench.plotting.tools.fitting.tool.data.column.required")) {
			var.getColumn().setWidth(0);
			var.getColumn().setResizable(false);
		}
		
        var   = new TableViewerColumn(viewer, SWT.CENTER, 4);
		var.getColumn().setText("Fit");
		var.getColumn().setToolTipText("The value of the fitted peak.");
		var.getColumn().setWidth(100);
		var.setLabelProvider(new PeakLabelProvider(4));
		var.getColumn().addSelectionListener(getSelectionAdapter(var.getColumn(), 4, cc));
		ret.add(var);

		var   = new TableViewerColumn(viewer, SWT.CENTER, 5);
		var.getColumn().setText("FWHM");
		var.getColumn().setWidth(100);
		var.setLabelProvider(new PeakLabelProvider(5));
		var.getColumn().addSelectionListener(getSelectionAdapter(var.getColumn(), 5, cc));
		ret.add(var);
		
        var   = new TableViewerColumn(viewer, SWT.CENTER, 6);
		var.getColumn().setText("Area");
		var.getColumn().setWidth(100);
		var.setLabelProvider(new PeakLabelProvider(6));
		var.getColumn().addSelectionListener(getSelectionAdapter(var.getColumn(), 6, cc));
		ret.add(var);

        var   = new TableViewerColumn(viewer, SWT.CENTER, 7);
		var.getColumn().setText("Type");
		var.getColumn().setWidth(100);
		var.setLabelProvider(new PeakLabelProvider(7));
		var.getColumn().addSelectionListener(getSelectionAdapter(var.getColumn(), 7, cc));
		ret.add(var);
		
        var   = new TableViewerColumn(viewer, SWT.CENTER, 8);
		var.getColumn().setText("Algorithm");
		var.getColumn().setWidth(100);
		var.setLabelProvider(new PeakLabelProvider(8));
		var.getColumn().addSelectionListener(getSelectionAdapter(var.getColumn(), 8, cc));
		ret.add(var);
		
		return ret;

	}

	private SelectionAdapter getSelectionAdapter(final TableColumn column, final int index, final PeakColumnComparator comparator) {
		SelectionAdapter selectionAdapter = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				comparator.setColumn(index);
				int dir = comparator.getDirection();
				viewer.getTable().setSortDirection(dir);
				viewer.getTable().setSortColumn(column);
				viewer.refresh();
			}
		};
		return selectionAdapter;
	}

	/**
	 * 
	 * @param fittedPeaksInfo
	 * @return
	 * @throws Exception 
	 */
	protected FittedFunctions getFittedFunctions(FittedPeaksInfo fittedPeaksInfo) throws Exception {
		FittedFunctions functions = FittingUtils.getFittedPeaks(fittedPeaksInfo);
		this.fittedPeaksInfo = fittedPeaksInfo;
		return functions;
	}
	

	@Override
	public Serializable getToolData() {
		
		final FunctionSquirts fs = (FunctionSquirts)super.getToolData();
		if (fittedPeaksInfo!=null) {
			fs.setIdentifiedPeaks(fittedPeaksInfo.getIdentifiedPeaks());
		}
		fs.setFitBounds(getFitBounds());
		return fs;
	}

	
	/**
	 * Thread safe
	 * @param peaks
	 */
	@Override
	protected synchronized void createFittedFunctionUI(final FittedFunctions newBean) {
		
		if (newBean==null) {
			fittedFunctions = null;
			logger.error("Cannot find peaks in the given selection.");
			return;
		}
		composite.getDisplay().syncExec(new Runnable() {
			
		    public void run() {
		    	try {
		    		
		    		
		    		boolean requireFWHMSelections = Activator.getPlottingPreferenceStore().getBoolean(FittingConstants.SHOW_FWHM_SELECTIONS);
		    		boolean requirePeakSelections = Activator.getPlottingPreferenceStore().getBoolean(FittingConstants.SHOW_PEAK_SELECTIONS);
		    		boolean requireTrace = Activator.getPlottingPreferenceStore().getBoolean(FittingConstants.SHOW_FITTING_TRACE);
		    		boolean requireAnnot = Activator.getPlottingPreferenceStore().getBoolean(FittingConstants.SHOW_ANNOTATION_AT_PEAK);

					// Draw the regions
					for (int i = 0; i < newBean.size(); i++) {

						FittedFunction fp = newBean.getFunctionList().get(i); // TODO proper encapsulation					

						if (fp.isSaved())       continue;
						if (fp.getFwhm()!=null) continue;  // Already got some UI
						
						RectangularROI rb = fp.getRoi();
						String areaName = RegionUtils.getUniqueName("Peak Area", getPlottingSystem());
						String suffix = areaName.replaceAll("Peak Area", "");

						final IRegion area = getPlottingSystem().createRegion(areaName, RegionType.XAXIS);
						area.setRegionColor(ColorConstants.orange);
						area.setROI(rb);
						area.setMobile(false);
						area.setUserObject(FittedFunction.class);
						getPlottingSystem().addRegion(area);
						fp.setFwhm(area);
						if (!requireFWHMSelections) area.setVisible(false);
												
						final Dataset[] pair = fp.getPeakFunctions();
						final ILineTrace trace = getPlottingSystem().createLineTrace("Peak"+suffix);
						//set user trace false before setting data otherwise the trace sent to events will be a true by default
						trace.setUserTrace(false);
						trace.setData(pair[0], pair[1]);
						trace.setLineWidth(1);
						trace.setTraceColor(ColorConstants.black);
						getPlottingSystem().addTrace(trace);
						fp.setTrace(trace);
						if (!requireTrace) trace.setVisible(false);

	                   	final IAnnotation ann = getPlottingSystem().createAnnotation("Peak"+suffix);
                    	ann.setLocation(fp.getPosition(), fp.getPeakValue());                  	
                    	getPlottingSystem().addAnnotation(ann);                   	
                    	fp.setAnnotation(ann);
                    	if (!requireAnnot) ann.setVisible(false);
                    	
						final IRegion line = getPlottingSystem().createRegion("Peak Line"+suffix, RegionType.XAXIS_LINE);
						line.setRegionColor(ColorConstants.black);
						line.setAlpha(150);
						line.setLineWidth(1);
						getPlottingSystem().addRegion(line);
						line.setROI(new LinearROI(rb.getMidPoint(), rb.getMidPoint()));
						line.setMobile(false);
						fp.setCenter(line);
						if (!requirePeakSelections) line.setVisible(false);

					}
				
					PeakFittingTool.this.fittedFunctions = newBean;
					viewer.setInput(newBean);
                    viewer.refresh();
                    
                    algorithmMessage.setText(getAlgorithmSummary());
                    algorithmMessage.getParent().layout();
                    //updatePlotServerConnection(newBean);
                    
		    	} catch (Exception ne) {
		    		logger.error("Cannot create fitted peaks!", ne);
		    	}
		    } 
		});
	}

	protected String getAlgorithmSummary() {
		StringBuilder buf = new StringBuilder("Fit attempted: '");
		buf.append(FittingUtils.getPeaksRequired());
		buf.append("' ");
		buf.append(FittingUtils.getPeakClass().getSimpleName());
		buf.append("'s using ");
		buf.append(FittingUtils.getOptimizer().getClass().getSimpleName());
		buf.append(" with smoothing of '");
		buf.append(FittingUtils.getSmoothing());
		buf.append("' (<a>configure smoothing</a>)");
		return buf.toString();
	}
	
	private Map<Integer,List<Double>> fit, xpos, fwhm, area;
	private Map<Integer,List<IDataset>> functions;
	private DataReductionSlice lastSlice;
	
	public DataReductionInfo export(DataReductionSlice slice) throws Exception {
				
		final RectangularROI roi = getFitBounds();
		if (roi==null) return new DataReductionInfo(Status.CANCEL_STATUS, null);

		final double[] p1 = roi.getPointRef();
		final double[] p2 = roi.getEndPoint();

		Dataset x  = slice.getAxes()!=null && !slice.getAxes().isEmpty()
				           ? DatasetUtils.convertToDataset(slice.getAxes().get(0))
				           : DatasetFactory.createRange(IntegerDataset.class, slice.getData().getSize());

		Dataset[] a= Generic1DFitter.selectInRange(x,DatasetUtils.convertToDataset(slice.getData()),p1[0],p2[0]);
		x = a[0]; Dataset y=a[1];
		
		// If the IdentifiedPeaks are null, we make them.
		@SuppressWarnings("unchecked")
		List<IdentifiedPeak> identifiedPeaks = (List<IdentifiedPeak>)slice.getUserData();
		if (slice.getUserData()==null) {
			identifiedPeaks = Generic1DFitter.parseDataDerivative(x, y, FittingUtils.getSmoothing());
		}

		if (fit==null)  fit  = new LinkedHashMap<Integer,List<Double>>(7);
		if (xpos==null) xpos = new LinkedHashMap<Integer,List<Double>>(7);
		if (fwhm==null) fwhm = new LinkedHashMap<Integer,List<Double>>(7);
		if (area==null) area = new LinkedHashMap<Integer,List<Double>>(7);
		if (functions==null) functions = new LinkedHashMap<Integer,List<IDataset>>(7); 
		lastSlice = slice;
		
		try {
			final FittedPeaksInfo info = new FittedPeaksInfo(x, y, slice.getMonitor());
			info.setIdentifiedPeaks(identifiedPeaks);
			
			FittedFunctions bean = FittingUtils.getFittedPeaks(info);
			
			int index = 1;
			for (FittedFunction fp : bean.getFunctionList()) {
				
				final String peakName = "Peak"+index;
				Dataset sdd = DatasetFactory.createFromObject(fp.getPeakValue(), 1);
				addValue(fit, index, fp.getPeakValue());
				sdd.setName(peakName+"_fit");
				slice.appendData(sdd);
				
				sdd = DatasetFactory.createFromObject(fp.getPosition(), 1);
				addValue(xpos, index, fp.getPosition());
				sdd.setName(peakName+"_xposition");
				slice.appendData(sdd);
				
				sdd = DatasetFactory.createFromObject(fp.getFWHM(), 1);
				addValue(fwhm, index, fp.getFWHM());
				sdd.setName(peakName+"_fwhm");
				slice.appendData(sdd);
				
				sdd = DatasetFactory.createFromObject(fp.getArea(), 1);
				addValue(area, index, fp.getArea());
				sdd.setName(peakName+"_area");
				slice.appendData(sdd);

				final Dataset[] pair = fp.getPeakFunctions();
				Dataset     function = pair[1];
				Dataset fc = function.clone();
				fc.setName(peakName+"_function");
				slice.appendData(fc);
				addList(functions, index, fc);

				++index;
			}
			
		} catch (Exception ne) {
			logger.error("Cannot fit peaks!", ne);
			return new DataReductionInfo(Status.CANCEL_STATUS, null);
		}
		
		DataReductionInfo status = new DataReductionInfo(Status.OK_STATUS, identifiedPeaks);
		return status;
	}
	

	private void addValue(Map<Integer, List<Double>> col, int index, double value) {

        List<Double> values = col.get(index);
        if (values == null) {
        	values = new ArrayList<Double>(31);
        	col.put(index, values);
        }
        values.add(value);
	}
	
	private void addList(Map<Integer, List<IDataset>> lists, int index, IDataset item) {
        List<IDataset> list = lists.get(index);
        if (list == null) {
        	list = new ArrayList<IDataset>(31);
        	lists.put(index, list);
        }
        list.add(item);
	}

	/**
	 * The user may enter a regular expression for their dataset name. In this case 
	 * all datasets matching it will be fitted for peaks. If they do this, we attempt to 
	 * detect that they selected more than one dataset and write a single list of the peaks
	 * and other scalars. This is used for EDXD to process individual separate elements in the
	 * results file which should be a single block, but isnt. Example /dls/i12/data/2014/cm4963-2/rawdata/36153.nxs
	 */
	public void exportFinished() throws Exception {
		
		if (fit==null || xpos==null || fwhm==null || area==null || functions==null || lastSlice==null) return;
		
		if (lastSlice.getExpandedDatasetNames()==null || lastSlice.getExpandedDatasetNames().size()<2) return;
		
		final String nodeName = getTitle().replace(' ', '_');
		
		String entry     = (String)lastSlice.getFile().getRoot();
		String container = (String)lastSlice.getFile().group(nodeName, entry);
		for (int i = 1; i <= fit.size(); i++) {
			
			final String peakName = "Peak"+i;

			final IDataset fits  = DatasetFactory.createFromList(fit.get(i));
			fits.setName(peakName+"_fit_byElement");
			lastSlice.appendData(fits, container);
			
			final IDataset xposi = DatasetFactory.createFromList(xpos.get(i));
			xposi.setName(peakName+"_xposition_byElement");
			lastSlice.appendData(xposi, container);

			final IDataset fwhms = DatasetFactory.createFromList(fwhm.get(i));
			fwhms.setName(peakName+"_fwhm_byElement");
			lastSlice.appendData(fwhms, container);

			final IDataset areas = DatasetFactory.createFromList(area.get(i));
			areas.setName(peakName+"_area_byElement");
			lastSlice.appendData(areas, container);
			
			final List<IDataset> fs = functions.get(i);
			for (IDataset iDataset : fs) {
				Dataset ds = DatasetUtils.convertToDataset(iDataset.squeeze());
				ds.setName(peakName+"_functions");
				
				
				// Append directly into file.
				lastSlice.getFile().appendDataset(ds.getName(), ds, container);
			}

		}
		
		fit.clear();
		xpos.clear();
		fwhm.clear();
		area.clear();
		functions.clear();
		lastSlice = null;

	}

	/**
	 * We use the old actions here for simplicity of configuration.
	 * 
	 * TODO consider moving to commands.
	 */
	protected void createActions() {
		
		final Action createNewSelection = new Action("New fit selection.", IAction.AS_PUSH_BUTTON) {
			public void run() {
				createNewFit();
			}
		};
		createNewSelection.setImageDescriptor(Activator.getImageDescriptor("icons/plot-tool-peak-fit.png"));
		getSite().getActionBars().getToolBarManager().add(createNewSelection);
		
		final Action addMode = new Action("Add peaks to those already found", IAction.AS_CHECK_BOX) {
			public void run() {
				setAddingPeaks(isChecked());
				Activator.getPlottingPreferenceStore().setValue(FittingConstants.ADD_PEAK_MODE, isChecked());
				if (!isChecked()) {
					if (fitRegion!=null) {
						getPlottingSystem().removeRegion(fitRegion);
						fitRegion = null;
					}
				}
			}
		};
		addMode.setImageDescriptor(Activator.getImageDescriptor("icons/add.png"));
		getSite().getActionBars().getToolBarManager().add(addMode);
		addMode.setChecked(Activator.getPlottingPreferenceStore().getBoolean(FittingConstants.ADD_PEAK_MODE));
		setAddingPeaks(addMode.isChecked());
		
		
		getSite().getActionBars().getToolBarManager().add(new Separator());
		
		final Action showAnns = new Action("Show annotations at the peak position.", IAction.AS_CHECK_BOX) {
			public void run() {
				final boolean isChecked = isChecked();
				Activator.getPlottingPreferenceStore().setValue(FittingConstants.SHOW_ANNOTATION_AT_PEAK, isChecked);
				if (fittedFunctions!=null) fittedFunctions.setAnnotationsVisible(isChecked);
			}
		};
		showAnns.setImageDescriptor(Activator.getImageDescriptor("icons/plot-tool-peak-fit-showAnnotation.png"));
		getSite().getActionBars().getToolBarManager().add(showAnns);
		
		showAnns.setChecked(Activator.getPlottingPreferenceStore().getBoolean(FittingConstants.SHOW_ANNOTATION_AT_PEAK));

		final Action showTrace = new Action("Show fitting traces.", IAction.AS_CHECK_BOX) {
			public void run() {
				final boolean isChecked = isChecked();
				Activator.getPlottingPreferenceStore().setValue(FittingConstants.SHOW_FITTING_TRACE, isChecked);
				if (fittedFunctions!=null) fittedFunctions.setTracesVisible(isChecked);
			}
		};
		showTrace.setImageDescriptor(Activator.getImageDescriptor("icons/plot-tool-peak-fit-showFittingTrace.png"));
		getSite().getActionBars().getToolBarManager().add(showTrace);

		showTrace.setChecked(Activator.getPlottingPreferenceStore().getBoolean(FittingConstants.SHOW_FITTING_TRACE));

		
		final Action showPeak = new Action("Show peak lines.", IAction.AS_CHECK_BOX) {
			public void run() {
				final boolean isChecked = isChecked();
				Activator.getPlottingPreferenceStore().setValue(FittingConstants.SHOW_PEAK_SELECTIONS, isChecked);
				if (fittedFunctions!=null) fittedFunctions.setPeaksVisible(isChecked);
			}
		};
		showPeak.setImageDescriptor(Activator.getImageDescriptor("icons/plot-tool-peak-fit-showPeakLine.png"));
		getSite().getActionBars().getToolBarManager().add(showPeak);
		
		showPeak.setChecked(Activator.getPlottingPreferenceStore().getBoolean(FittingConstants.SHOW_PEAK_SELECTIONS));

		final Action showFWHM = new Action("Show selection regions for full width, half max.", IAction.AS_CHECK_BOX) {
			public void run() {
				final boolean isChecked = isChecked();
				Activator.getPlottingPreferenceStore().setValue(FittingConstants.SHOW_FWHM_SELECTIONS, isChecked);
				if (fittedFunctions!=null) fittedFunctions.setAreasVisible(isChecked);
			}
		};
		showFWHM.setImageDescriptor(Activator.getImageDescriptor("icons/plot-tool-peak-fit-showFWHM.png"));
		getSite().getActionBars().getToolBarManager().add(showFWHM);
		
		showFWHM.setChecked(Activator.getPlottingPreferenceStore().getBoolean(FittingConstants.SHOW_FWHM_SELECTIONS));
		
		final Separator sep = new Separator(getClass().getName()+".separator1");	
		getSite().getActionBars().getToolBarManager().add(sep);
		
		final Action savePeak = new Action("Store peak.", IAction.AS_PUSH_BUTTON) {
			public void run() {
				try {
					fittedFunctions.saveSelectedPeak(getPlottingSystem());
				} catch (Exception e) {
					logger.error("Cannot rename stored peak ", e);
				}
				viewer.refresh();
			}
		};
		savePeak.setImageDescriptor(Activator.getImageDescriptor("icons/plot-tool-peak-fit-savePeak.png"));
		getSite().getActionBars().getToolBarManager().add(savePeak);

		final Action export = new Action("Export peak(s)", IAction.AS_PUSH_BUTTON) {
			public void run() {
				try {
					EclipseUtils.openWizard(FittedPeaksExportWizard.ID, true);
				} catch (Exception e) {
					logger.error("Cannot open wizard "+FittedPeaksExportWizard.ID, e);
				}
			}
		};
		export.setImageDescriptor(Activator.getImageDescriptor("icons/save_edit.png"));
		getSite().getActionBars().getToolBarManager().add(export);

		final Separator sep3 = new Separator(getClass().getName()+".separator3");	
		getSite().getActionBars().getToolBarManager().add(sep3);

		final MenuAction  peakType = new MenuAction("Peak type to fit");
		peakType.setToolTipText("Peak type to fit");
		peakType.setImageDescriptor(Activator.getImageDescriptor("icons/plot-tool-peak-fit-peak-type.png"));
		
		CheckableActionGroup group = new CheckableActionGroup();
		
		Action selectedPeakAction = null;
		for (final String peakName : FunctionFactory.getPeakFunctionNames()) {
			final Action action = new Action(peakName, IAction.AS_CHECK_BOX) {
				public void run() {
					Activator.getPlottingPreferenceStore().setValue(FittingConstants.PEAK_TYPE, peakName);
					setChecked(true);
					if (fittingJob!=null&&isActive()) fittingJob.fit(false);
					peakType.setSelectedAction(this);
				}
			};
			peakType.add(action);
			group.add(action);
			if (peakName.equals(Activator.getPlottingPreferenceStore().getString(FittingConstants.PEAK_TYPE))) {
				selectedPeakAction = action;
			}
		}
		
		if (selectedPeakAction!=null) {
			peakType.setSelectedAction(selectedPeakAction);
			selectedPeakAction.setChecked(true);
		}
		getSite().getActionBars().getToolBarManager().add(peakType);
		getSite().getActionBars().getMenuManager().add(peakType);

		
		final Separator sep2 = new Separator(getClass().getName()+".separator2");	
		getSite().getActionBars().getToolBarManager().add(sep2);

		this.tracesMenu = new MenuAction("Traces");
		tracesMenu.setToolTipText("Choose trace for fit.");
		tracesMenu.setImageDescriptor(Activator.getImageDescriptor("icons/plot-tool-trace-choice.png"));
		
		getSite().getActionBars().getToolBarManager().add(tracesMenu);
		getSite().getActionBars().getMenuManager().add(tracesMenu);
				
		this.numberPeaks = new MenuAction("Number peaks to fit");
		numberPeaks.setToolTipText("Number peaks to fit");
				
		group = new CheckableActionGroup();
		
		final int npeak = Activator.getPlottingPreferenceStore().getDefaultInt(FittingConstants.PEAK_NUMBER_CHOICES);
		for (int ipeak = 1; ipeak <= npeak; ipeak++) {
			
			final int peak = ipeak;
			final Action action = new Action("Fit "+String.valueOf(ipeak)+" Peaks", IAction.AS_CHECK_BOX) {
				public void run() {
					Activator.getPlottingPreferenceStore().setValue(FittingConstants.PEAK_NUMBER, peak);
				}
			};
			
			action.setImageDescriptor(IconUtils.createIconDescriptor(String.valueOf(ipeak)));
			numberPeaks.add(action);
			group.add(action);
			action.setChecked(false);
			action.setToolTipText("Fit "+ipeak+" peak(s)");
			
		}
		final Action preferences = new Action("Preferences...") {
			public void run() {
				if (!isActive()) return;
				PreferenceDialog pref = PreferencesUtil.createPreferenceDialogOn(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), FittingPreferencePage.ID, null, null);
				if (pref != null) pref.open();
			}
		};
		
		final Action npeaks = new Action("Fit n Peaks", IAction.AS_CHECK_BOX) {
			public void run() {
				preferences.run();
			}
		};
		
		npeaks.setImageDescriptor(IconUtils.createIconDescriptor("n"));
		numberPeaks.add(npeaks);
		group.add(npeaks);
		npeaks.setChecked(false);
		npeaks.setToolTipText("Fit n peaks");


		final int ipeak = Activator.getPlottingPreferenceStore().getInt(FittingConstants.PEAK_NUMBER);
		if (ipeak<11) {
			numberPeaks.setSelectedAction(ipeak-1);
			numberPeaks.setCheckedAction(ipeak-1, true);
		} else {
			numberPeaks.setSelectedAction(npeaks);
			npeaks.setChecked(true);
		}
			
		getSite().getActionBars().getToolBarManager().add(numberPeaks);
		//getSite().getActionBars().getMenuManager().add(numberPeaks);
		
		
		final Action clear = new Action("Clear all", Activator.getImageDescriptor("icons/plot-tool-peak-fit-clear.png")) {
			public void run() {
				clearAll();
			}
		};
		clear.setToolTipText("Clear all regions found in the fitting");
		
		getSite().getActionBars().getToolBarManager().add(clear);
		getSite().getActionBars().getMenuManager().add(clear);
		
		final Action delete = new Action("Delete peak selected", Activator.getImageDescriptor("icons/delete.gif")) {
			public void run() {
				if (!isActive()) return;
				if (fittedFunctions!=null) fittedFunctions.deleteSelectedFunction(getPlottingSystem());
				pushFunctionsToPlotter();
				viewer.refresh();
				createNewFit();
			}
		};
		delete.setToolTipText("Delete peak selected, if any");
		
		getSite().getActionBars().getToolBarManager().add(delete);


		getSite().getActionBars().getMenuManager().add(preferences);
		
	    final MenuManager menuManager = new MenuManager();
	    menuManager.add(clear);
	    menuManager.add(delete);
	    menuManager.add(savePeak);
	    menuManager.add(new Separator());
	    menuManager.add(showAnns);
	    menuManager.add(showTrace);
	    menuManager.add(showPeak);
	    menuManager.add(showFWHM);
	    menuManager.add(new Separator());
	    menuManager.add(export);
		
	    viewer.getControl().setMenu(menuManager.createContextMenu(viewer.getControl()));

	}

	
	@Override
	public Object getAdapter(@SuppressWarnings("rawtypes") Class key) {
		if (key == IPeak.class) {
			return fittedFunctions!=null && !fittedFunctions.isEmpty() ? fittedFunctions.getPeakFunctions() : null;
		}
		return super.getAdapter(key);
	}
	
	public String exportFittedData(final String path) throws Exception {
		return exportFittedPeaks(path);
	}

	/**
	 * Will export to file and overwrite.
	 * Will append ".csv" if it is not already there.
	 * 
	 * @param path
	 */
	String exportFittedPeaks(final String path) throws Exception {
		
		File file = new File(path);
		if (!file.getName().toLowerCase().endsWith(".dat")) file = new File(path+".dat");
		if (file.exists())
			file.delete();
		else
			file.createNewFile();
		
		final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
		try {
			writer.write(FittedFunction.getCVSTitle());
			writer.newLine();
			for (FittedFunction peak : getSortedFunctionList()) {
				writer.write(peak.getTabString());
				writer.newLine();
			}
		} finally {
			writer.close();
		}
		
		return file.getAbsolutePath();
    }

	@Override
	void pushFunctionsToPlotter() {
		if (getPart() instanceof ISettablePlotView) {
			ISettablePlotView plotView = (ISettablePlotView) getPart();
			ArrayList<IPeak> peaks = new ArrayList<IPeak>();
			if (fittedFunctions != null) {
				for (FittedFunction func : fittedFunctions.getFunctionList()) {
					IPeak peak = func.getPeak();
					peaks.add(peak);
				}
			}
			plotView.updateData(peaks, IPeak.class);
		}
	}

}
