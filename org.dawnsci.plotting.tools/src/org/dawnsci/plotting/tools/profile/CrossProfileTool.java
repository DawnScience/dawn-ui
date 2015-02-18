package org.dawnsci.plotting.tools.profile;

import org.apache.commons.math3.genetics.CrossoverPolicy;
import org.dawnsci.plotting.tools.Activator;
import org.dawnsci.plotting.tools.preference.CrossProfileConstants;
import org.dawnsci.plotting.util.ColorUtility;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.api.roi.IRectangularROI;
import org.eclipse.dawnsci.analysis.dataset.roi.LinearROI;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
import org.eclipse.dawnsci.plotting.api.region.IRegionListener;
import org.eclipse.dawnsci.plotting.api.region.RegionEvent;
import org.eclipse.dawnsci.plotting.api.region.RegionUtils;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;

/**
 * A Line profile which is created by drawing a rectangle and it creates two lines
 * in the x and y 
 * @author fcp94556
 *
 */
public class CrossProfileTool extends LineProfileTool {

	private IRegionListener boxListener;

	public CrossProfileTool() {
		super();
		
		this.boxListener = new IRegionListener.Stub() {
			@Override
			public void regionAdded(RegionEvent evt) {
				try {
					createBoxLines(evt.getRegion());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
	}
	
	
	protected void createBoxLines(IRegion region) throws Exception {
		
		IPlottingSystem sys = getPlottingSystem();
		if (sys==null) return; // Unlikely.
		if (isDisposed()) return;
		
		// Draws two lines, then deletes the box
		if (region.getRegionType()!=RegionType.BOX) return;
		IRectangularROI roi = (IRectangularROI)region.getROI();
		
		
		// Y-line
		LinearROI yline = new LinearROI(new double[]{roi.getPoint()[0]+roi.getLength(0)/2d, roi.getPoint()[1]},
				                        new double[]{roi.getPoint()[0]+roi.getLength(0)/2d, roi.getPoint()[1]+roi.getLength(1)});
		
		add(yline, "Y Cross");
		
		// X-line
		LinearROI xline = new LinearROI(new double[]{roi.getPoint()[0],                  roi.getPoint()[1]+roi.getLength(1)/2d},
                                        new double[]{roi.getPoint()[0]+roi.getLength(0), roi.getPoint()[1]+roi.getLength(1)/2d});

		add(xline, "X Cross");
		
		
		sys.removeRegion(region);
		update(null, null, false);
	}
	
	protected ITrace createProfile(	IImageTrace   image, 
						            final IRegion region, 
						            IROI          rbs, 
						            boolean       tryUpdate,
						            boolean       isDrag,
						            IProgressMonitor monitor) {
		
		final ITrace trace = super.createProfile(image, region, rbs, tryUpdate, isDrag, monitor);
		if (trace!=null && trace instanceof ILineTrace) {
			final ILineTrace ltrace = (ILineTrace)trace;
			if (ltrace.getTraceColor()!=region.getRegionColor()) {
				Display.getDefault().syncExec(new Runnable() {
					public void run() {
						ltrace.setTraceColor(region.getRegionColor());
					}
				});
			}
		}
		
		// TODO Draw z if this is a lazy dataset slice and DO_Z is true.
		
		return trace;
	}

	private IAction showZ, zPrefs;
	@Override
	protected void configurePlottingSystem(IPlottingSystem plotter) {

		super.configurePlottingSystem(plotter);
		
		final IPreferenceStore store = Activator.getLocalPreferenceStore();
		
		// Now we can add the actions for z.
		showZ = new Action("Show z profile at cross intersection.", IAction.AS_CHECK_BOX) {
			public void run() {
				store.setValue(CrossProfileConstants.DO_Z, isChecked());
				update(null, null, false);
			}
		};
		showZ.setImageDescriptor(Activator.getImageDescriptor("icons/z-line.png"));
		showZ.setChecked(store.getBoolean(CrossProfileConstants.DO_Z));
		
		zPrefs = new Action("Edit z-profile...", Activator.getImageDescriptor("icons/z-pref.png")) {
			public void run() {
				PreferenceDialog pref = PreferencesUtil.createPreferenceDialogOn(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), 
						                              "org.dawnsci.plotting.tools.crossProfilePreference", null, null);
				if (pref != null) pref.open();
				
			}
		};

		getSite().getActionBars().getToolBarManager().add(new Separator(CrossProfileConstants.DO_Z+"start"));
		getSite().getActionBars().getToolBarManager().add(showZ);
		getSite().getActionBars().getToolBarManager().add(zPrefs);
		getSite().getActionBars().getToolBarManager().add(new Separator(CrossProfileConstants.DO_Z+"end"));
	}
	
	private final IRegion add(final LinearROI line, String name) throws Exception {
		
		IPlottingSystem sys = getPlottingSystem();
		final IRegion   reg = sys.createRegion(RegionUtils.getUniqueName(name, sys), RegionType.LINE);
		reg.setROI(line);
		if (sys.getRegions()!=null) {
			reg.setRegionColor(ColorUtility.getSwtColour(sys.getRegions().size()));
		}
		sys.addRegion(reg);		
	
		return reg;
	}


	@Override
	protected RegionType getCreateRegionType() {
		return RegionType.BOX;
	}

	public void activate() {

		super.activate();
		if (getPlottingSystem()!=null) {
			getPlottingSystem().addRegionListener(boxListener);
			
			// TODO See if current data is part of an ILazyDataset, if is enable z actions
		}
	}
	
	public void deactivate() {

		super.deactivate();
		if (getPlottingSystem()!=null) {
			getPlottingSystem().removeRegionListener(boxListener);
		}
	}

}
