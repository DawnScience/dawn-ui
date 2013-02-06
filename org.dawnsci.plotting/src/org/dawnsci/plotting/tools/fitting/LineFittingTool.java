package org.dawnsci.plotting.tools.fitting;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import org.dawb.common.ui.image.IconUtils;
import org.dawb.common.ui.menu.CheckableActionGroup;
import org.dawb.common.ui.menu.MenuAction;
import org.dawb.common.ui.plot.annotation.AnnotationUtils;
import org.dawb.common.ui.plot.annotation.IAnnotation;
import org.dawb.common.ui.plot.region.IRegion;
import org.dawb.common.ui.plot.region.RegionUtils;
import org.dawb.common.ui.plot.region.IRegion.RegionType;
import org.dawb.common.ui.plot.trace.ILineTrace;
import org.dawb.common.ui.plot.trace.TraceUtils;
import org.dawb.common.ui.util.EclipseUtils;
import org.dawnsci.plotting.Activator;
import org.dawnsci.plotting.preference.FittingConstants;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.fitting.functions.Polynomial;
import uk.ac.diamond.scisoft.analysis.roi.LinearROI;
import uk.ac.diamond.scisoft.analysis.roi.RectangularROI;

public class LineFittingTool extends AbstractFittingTool {
	
	private static final Logger logger = LoggerFactory.getLogger(LineFittingTool.class);


	/**
	 * Columns in the UI Table.
	 */
	@Override
	protected List<TableViewerColumn> createColumns(TableViewer viewer) {
		// Columns for coefficients of polynomials maybe?
		ColumnViewerToolTipSupport.enableFor(viewer,ToolTip.NO_RECREATE);

		List<TableViewerColumn> ret = new ArrayList<TableViewerColumn>(9);
        TableViewerColumn var   = new TableViewerColumn(viewer, SWT.LEFT, 0);
		var.getColumn().setText("Trace");
		var.getColumn().setWidth(80);
		var.setLabelProvider(new LineLabelProvider(0));
		ret.add(var);

		var   = new TableViewerColumn(viewer, SWT.LEFT, 1);
		var.getColumn().setText("Name");
		var.getColumn().setWidth(150);
		var.setLabelProvider(new LineLabelProvider(1));
		ret.add(var);
	
        var   = new TableViewerColumn(viewer, SWT.LEFT, 2);
		var.getColumn().setText("Type");
		var.getColumn().setWidth(200);
		var.setLabelProvider(new LineLabelProvider(2));
		ret.add(var);
	
        var   = new TableViewerColumn(viewer, SWT.CENTER, 3);
		var.getColumn().setText("Equation");
		var.getColumn().setWidth(300);
		var.setLabelProvider(new LineLabelProvider(3));
		ret.add(var);
		
		return ret;

	}

	/**
	 * The actual algorithm run
	 */
	@Override
	protected FittedFunctions getFittedFunctions(FittedPeaksInfo fittedPeaksInfo) throws Exception {
		// Drive the fitting, maybe exactly the same as the peak fitting or maybe different options.
		return FittingUtils.getFittedPolynomial(fittedPeaksInfo);
	}

	/**
	 * What happens when the line is plotted
	 */
	@Override
	protected void createFittedFunctionUI(final FittedFunctions newBean) {
		if (newBean==null) {
			fittedFunctions = null;
			logger.error("Cannot fit the the given selection.");
			return;
		}
		composite.getDisplay().syncExec(new Runnable() {
			
		    public void run() {
		    	try {
		    		
		    		boolean warnLarge = false;
		    		
		    		boolean requireRange = Activator.getDefault().getPreferenceStore().getBoolean(FittingConstants.SHOW_POLY_RANGE);
		    		boolean requireTrace = Activator.getDefault().getPreferenceStore().getBoolean(FittingConstants.SHOW_POLY_TRACE);

		    		int ifit = 1;
					// Draw the regions
					for (FittedFunction fp : newBean.getFunctionList()) {
						
						RectangularROI rb = fp.getRoi();
						final IRegion area = RegionUtils.replaceCreateRegion(getPlottingSystem(), "Fit Area "+ifit, RegionType.XAXIS);
						area.setRegionColor(ColorConstants.orange);
						area.setROI(rb);
						area.setMobile(false);
						getPlottingSystem().addRegion(area);
						fp.setFwhm(area);
						if (!requireRange) area.setVisible(false);
												
						final AbstractDataset[] pair = fp.getPeakFunctions();
						final ILineTrace trace = TraceUtils.replaceCreateLineTrace(getPlottingSystem(), "Fit "+ifit);
						//set user trace false before setting data otherwise the trace sent to events will be a true by default
						trace.setUserTrace(false);
						trace.setData(pair[0], pair[1]);
						trace.setLineWidth(1);
						trace.setTraceColor(ColorConstants.black);
						getPlottingSystem().addTrace(trace);
						fp.setTrace(trace);
						if (!requireTrace) trace.setVisible(false);
						
						//No annotation used at the moment
						//But leaving it in since some functions in the abstract class
						//need one to be there
	                   	final IAnnotation ann = AnnotationUtils.replaceCreateAnnotation(getPlottingSystem(), "Fit "+ifit);
                    	ann.setLocation(0, 0);                  	
                    	getPlottingSystem().addAnnotation(ann);                   	
                    	fp.setAnnotation(ann);
                    	ann.setVisible(false);
                    	
						final IRegion line = RegionUtils.replaceCreateRegion(getPlottingSystem(), "Peak Line "+ifit, RegionType.XAXIS_LINE);
						line.setRegionColor(ColorConstants.black);
						line.setAlpha(150);
						line.setLineWidth(1);
						getPlottingSystem().addRegion(line);
						line.setROI(new LinearROI(rb.getMidPoint(), rb.getMidPoint()));
						line.setMobile(false);
						fp.setCenter(line);
						line.setVisible(false);
						
						//Fitting can go badly if x is too large and x step too small
						//Warn if this is the case
						double max = trace.getXData().max().doubleValue();
						double min = trace.getXData().min().doubleValue();
						double step = (max-min)/trace.getXData().getSize();
						double midVal = ((max+min)/2);
						
						if (midVal/step > 1E6)
							warnLarge = true;

					    ++ifit;
					}
				
					LineFittingTool.this.fittedFunctions = newBean;
					viewer.setInput(newBean);
                    viewer.refresh();
                    if (warnLarge){
                    	algorithmMessage.setText("Warning: Large x values. Consider subtracting constant from x before fitting.");
                    }
                    else
                    	algorithmMessage.setText("Polynomial Line Fit");
                    
                    algorithmMessage.getParent().layout();

                    
		    	} catch (Exception ne) {
		    		logger.error("Cannot create fitted peaks!", ne);
		    	}
		    } 
		});

	}

	/**
	 * Actions appearing in the tool.
	 */
	@Override
	protected void createActions() {
		final Action createNewSelection = new Action("New fit selection.", IAction.AS_PUSH_BUTTON) {
			public void run() {
				createNewFit();
			}
		};
		
		//Stealing all the icons from the peak fitting tool
		createNewSelection.setImageDescriptor(Activator.getImageDescriptor("icons/plot-tool-peak-fit.png"));
		getSite().getActionBars().getToolBarManager().add(createNewSelection);
		getSite().getActionBars().getToolBarManager().add(new Separator());
		
		//Action to show/remove fit trace from plot
		final Action showTrace = new Action("Show fitting traces.", IAction.AS_CHECK_BOX) {
			public void run() {
				final boolean isChecked = isChecked();
				Activator.getDefault().getPreferenceStore().setValue(FittingConstants.SHOW_POLY_TRACE, isChecked);
				if (fittedFunctions!=null) fittedFunctions.setTracesVisible(isChecked);
			}
		};
		showTrace.setImageDescriptor(Activator.getImageDescriptor("icons/plot-tool-peak-fit-showFittingTrace.png"));
		getSite().getActionBars().getToolBarManager().add(showTrace);
		
		showTrace.setChecked(Activator.getDefault().getPreferenceStore().getBoolean(FittingConstants.SHOW_POLY_TRACE));;
		
		//Action to show/remove fit region from plot
		final Action showFitRegion = new Action("Show selection regions for fit", IAction.AS_CHECK_BOX) {
			public void run() {
				final boolean isChecked = isChecked();
				Activator.getDefault().getPreferenceStore().setValue(FittingConstants.SHOW_POLY_RANGE, isChecked);
				if (fittedFunctions!=null) fittedFunctions.setAreasVisible(isChecked);
			}
		};
		showFitRegion.setImageDescriptor(Activator.getImageDescriptor("icons/plot-tool-peak-fit-showFWHM.png"));
		getSite().getActionBars().getToolBarManager().add(showFitRegion);
		
		showFitRegion.setChecked(Activator.getDefault().getPreferenceStore().getBoolean(FittingConstants.SHOW_POLY_RANGE));
		
		final Separator sep = new Separator(getClass().getName()+".separator1");	
		getSite().getActionBars().getToolBarManager().add(sep);

		//Add select traces menu created in updateTracesChoice()
		this.tracesMenu = new MenuAction("Traces");
		tracesMenu.setToolTipText("Choose trace for fit.");
		tracesMenu.setImageDescriptor(Activator.getImageDescriptor("icons/plot-tool-trace-choice.png"));
		
		getSite().getActionBars().getToolBarManager().add(tracesMenu);
		getSite().getActionBars().getMenuManager().add(tracesMenu);
		
		//Add menu to choose polynomial order
		final MenuAction numberPeaks = new MenuAction("Polynomial order to fit");
		numberPeaks.setToolTipText("Polynomial order to fit");
				
		CheckableActionGroup group = new CheckableActionGroup();
		
		final int npeak = Activator.getDefault().getPreferenceStore().getDefaultInt(FittingConstants.POLY_CHOICES);
		for (int ipeak = 1; ipeak <= npeak; ipeak++) {
			
			final int peak = ipeak;
			final Action action = new Action("Polynomial Order: " + String.valueOf(ipeak), IAction.AS_CHECK_BOX) {
				public void run() {
					Activator.getDefault().getPreferenceStore().setValue(FittingConstants.POLY_ORDER, peak);
					numberPeaks.setSelectedAction(this);
					setChecked(true);
					if (isActive()) fittingJob.fit();
				}
			};
			
			action.setImageDescriptor(IconUtils.createIconDescriptor(String.valueOf(ipeak)));
			numberPeaks.add(action);
			group.add(action);
			action.setChecked(false);
			action.setToolTipText("Fit "+ipeak+" peak(s)");
			
		}

		final int ipeak = Activator.getDefault().getPreferenceStore().getInt(FittingConstants.POLY_ORDER);
		numberPeaks.setSelectedAction(ipeak-1);
		numberPeaks.setCheckedAction(ipeak-1, true);
		
		getSite().getActionBars().getToolBarManager().add(numberPeaks);
		
		//Clear All regions action
		final Action clear = new Action("Clear all", Activator.getImageDescriptor("icons/plot-tool-peak-fit-clear.png")) {
			public void run() {
				clearAll();
			}
		};
		clear.setToolTipText("Clear all regions found in the fitting");
		
		getSite().getActionBars().getToolBarManager().add(clear);
		getSite().getActionBars().getMenuManager().add(clear);
		
		//Clear All Fits action
		final Action delete = new Action("Delete fit selected", Activator.getImageDescriptor("icons/delete.gif")) {
			public void run() {
				if (!isActive()) return;
				if (fittedFunctions!=null) fittedFunctions.deleteSelectedFunction(getPlottingSystem());
				viewer.refresh();
			}
		};
		delete.setToolTipText("Delete line selected, if any");
		
		getSite().getActionBars().getToolBarManager().add(delete);
		
		//Added to right click menus
		//Export to .dat
		final Action export = new Action("Export...", IAction.AS_PUSH_BUTTON) {
			public void run() {
				try {
					EclipseUtils.openWizard(FittedPeaksExportWizard.ID, true);
				} catch (Exception e) {
					logger.error("Cannot open wizard "+FittedPeaksExportWizard.ID, e);
				}
			}
		};
		
		//Copy to clipboard
		final Action copy = new Action("Copy", IAction.AS_PUSH_BUTTON) {
			public void run() {
				try {
					StructuredSelection ss = (StructuredSelection)viewer.getSelection();
					Object element = ss.getFirstElement();
					
					if (!(element instanceof FittedFunction)) return;
					FittedFunction ff = (FittedFunction)element;
					
					if (ff.getFunction().getFunction(0) == null || !(ff.getFunction().getFunction(0) instanceof Polynomial)) return;
					
					String equation = ((Polynomial)ff.getFunction().getFunction(0)).getStringEquation();

					Clipboard cb = new Clipboard(Display.getDefault());
					cb.clearContents();
					TextTransfer textTransfer = TextTransfer.getInstance();
					cb.setContents(new Object[] { equation },
							new TextTransfer[] { textTransfer });
					cb.dispose();
				} catch (Exception e) {
					logger.error("Copy Unsuccessful", e);
				}
			}
		};


	    final MenuManager menuManager = new MenuManager();
	    menuManager.add(clear);
	    menuManager.add(delete);
	    menuManager.add(new Separator());
	    menuManager.add(showTrace);
	    menuManager.add(new Separator());
	    menuManager.add(export);
	    menuManager.add(copy);
		
	    viewer.getControl().setMenu(menuManager.createContextMenu(viewer.getControl()));

	}
	
	String exportFittedData(final String path) throws Exception {
		//Export file to .dat
		File file = new File(path);
		if (!file.getName().toLowerCase().endsWith(".dat")) file = new File(path+".dat");
		if (file.exists()) file.delete();

		final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
		try {
			writer.write("#Polynomial fitting coefficients");
			writer.newLine();
			for (FittedFunction peak : this.fittedFunctions.getFunctionList()) {
				Polynomial poly = (Polynomial)peak.getFunction().getFunction(0);
				writer.write("#For Trace: " + peak.getDataTrace().getName());
				writer.newLine();
				writer.write("#Equation: " + poly.getStringEquation());
				writer.newLine();
				writer.write("#High precision coefficients");
				writer.newLine();
				writer.write("#");
				for (int i = 0; i < poly.getNoOfParameters(); i++) {
					writer.write("X^" + String.valueOf((poly.getNoOfParameters()-i-1))  +"\t");
				}
				writer.newLine();
				for (int i = 0; i <poly.getNoOfParameters(); i++) {
					writer.write(String.valueOf(poly.getParameter(i).getValue()) +"\t");
				}
				writer.newLine();
				writer.newLine();
			}

		} finally {
			writer.close();
		}

		return file.getAbsolutePath();
	}

}
