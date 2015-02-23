package org.dawnsci.plotting.tools.profile;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.dawnsci.plotting.tools.Activator;
import org.dawnsci.plotting.tools.preference.CrossProfileConstants;
import org.dawnsci.plotting.util.ColorUtility;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyDataset;
import org.eclipse.dawnsci.analysis.api.dataset.Slice;
import org.eclipse.dawnsci.analysis.api.metadata.OriginMetadata;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.roi.LinearROI;
import org.eclipse.dawnsci.analysis.dataset.roi.PointROI;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
import org.eclipse.dawnsci.plotting.api.region.IRegionListener;
import org.eclipse.dawnsci.plotting.api.region.RegionEvent;
import org.eclipse.dawnsci.plotting.api.region.RegionUtils;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace.PointStyle;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.roi.ROIProfile;

/**
 * A Line profile which is created by drawing a rectangle and it creates two lines
 * in the x and y 
 * @author fcp94556
 *
 */
public class CrossProfileTool extends LineProfileTool {

	private static final Logger logger = LoggerFactory.getLogger(CrossProfileTool.class);
	
	private IRegionListener pointListener;

	public CrossProfileTool() {
		super();
		
		this.pointListener = new IRegionListener.Stub() {
			@Override
			public void regionAdded(RegionEvent evt) {
				try {
					createLines(evt.getRegion());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
	}
	
	
	protected void createLines(IRegion region) throws Exception {
		
		IPlottingSystem sys = getPlottingSystem();
		if (sys==null) return; // Unlikely.
		if (isDisposed()) return;
		
		// Draws two lines, then deletes the box
		if (region.getRegionType()!=RegionType.POINT) return;
		PointROI roi = (PointROI)region.getROI();
		
				
		// X-line
		final double px =  Activator.getLocalPreferenceStore().getInt(CrossProfileConstants.PLUS_X);
		final double mx =  Activator.getLocalPreferenceStore().getInt(CrossProfileConstants.MINUS_X);
		LinearROI xline = new LinearROI(new double[]{roi.getPoint()[0]-mx,   roi.getPoint()[1]},
                                        new double[]{roi.getPoint()[0]+px,   roi.getPoint()[1]});

		xline.setCrossHair(true);
		
		add(xline, "Cross");
		
		sys.removeRegion(region);
		update(null, null, false);
	}
	
	protected Collection<ITrace> createProfile(	IImageTrace   image, 
									            final IRegion region, 
									            IROI          rbs, 
									            boolean       tryUpdate,
									            boolean       isDrag,
									            IProgressMonitor monitor) {

		final Collection<ITrace> traces = super.createProfile(image, region, rbs, tryUpdate, isDrag, monitor);
		syncColor(traces, region);

		boolean doZ = Activator.getLocalPreferenceStore().getBoolean(CrossProfileConstants.DO_Z);
		if (!doZ) return traces;

		// It's time to hack in z, let's do this thing...
		try {

			// Draw z if this is a lazy dataset slice and DO_Z is true.
			if (image!=null && image.getData()!=null && rbs instanceof LinearROI) {

				IDataset             data   = image.getData();
				List<OriginMetadata> origin = data.getMetadata(OriginMetadata.class);
				OriginMetadata       odata  = origin!=null && !origin.isEmpty() ? origin.get(0) : null;

				if (odata!=null) {
					// We have some z! but which dim is z?
					// It is poorly defined at the slice level
					// because we could have nD data, with several slices
					// to get down to the image. However one of those sliced
					// when the data > 3 dimensions must be chosen as z.
					// Therefore if data>3, user must give us the z via a 
					// system property.
					final ILazyDataset parent = odata.getParent();
					final Slice[]      slice  = odata.getSliceInOutput();

					int zDim = Activator.getLocalPreferenceStore().getInt(CrossProfileConstants.Z_DIM);
					if (parent.getRank()==3) // We find z
						for (int i = 0; i < slice.length; i++) {
							if (slice[i].isSliceComplete() && slice[i].getNumSteps()==1) {
								zDim = i;
								break;
							}
						}

					LinearROI lroi     = (LinearROI)rbs;
					double[]  cen      = lroi.getMidPoint(); // We assume y, then x. TODO this is not guaranteed, need a fix
					final int location = slice[zDim].getStart();
					int mz             = Activator.getLocalPreferenceStore().getInt(CrossProfileConstants.MINUS_Z);
					int pz             = Activator.getLocalPreferenceStore().getInt(CrossProfileConstants.PLUS_Z);

					int from = location-mz; 
					if (from<0) from = 0;
					int to  = location+pz;
					if (to>parent.getShape()[zDim]) to = parent.getShape()[zDim];

					// We take an image in the z-direction and use ROIProfile.line which
					// gives a better line than a direct slice.
					final Slice[] zSlice = new Slice[slice.length];
					boolean xset = false, yset = false;
					for (int i = 0; i < zSlice.length; i++) {
						if (i==zDim) {
							zSlice[i] = new Slice(from, to, 1);
							continue;
						}
						if (slice[i].getNumSteps()==1) {
							zSlice[i] = slice[i];
							continue;
						}
						// We assume y, then x. 
						// TODO this is not guaranteed, need a fix
						if (!yset) { // FIXME need to set which dim is x and which is y in metadata
							zSlice[i] =  slice[i];
							yset = true;
							continue;
						}
						if (!xset) { // FIXME need to set which dim is x and which is y in metadata
							zSlice[i] = new Slice((int)Math.round(cen[0]), (int)Math.round(cen[0])+1);
							xset = true;
							continue;
						}
					}
					IDataset zimage = parent.getSlice(zSlice); // Probably will be slow...
					zimage = zimage.squeeze();

					// TODO Not sure if this is generic enough.
					LinearROI zline     = new LinearROI(new double[]{cen[1], 0}, new double[]{cen[1], zimage.getShape()[0]});
					Dataset[] zprofiles = ROIProfile.line((Dataset)zimage, zline, 1d);
					if (zprofiles!=null && zprofiles.length>0) {
						IDataset zprofile = zprofiles[0];
						zprofile.setName(region.getName()+"(Z)");
						List<ITrace> ztrace = plotProfile(zprofile, tryUpdate, monitor);
						syncColor(ztrace, region);
					}
				}
			}

		} catch (Exception ne) {
			logger.error("Cannot process OriginMetadata", ne);
		}

		return traces;
	}

	private void syncColor(Collection<ITrace> traces, final IRegion region) {
		if (traces==null) return;
		for (ITrace trace : traces) {
			if (trace!=null && trace instanceof ILineTrace) {
				final ILineTrace ltrace = (ILineTrace)trace;
				if (ltrace.getTraceColor()!=region.getRegionColor()) {
					Display.getDefault().syncExec(new Runnable() {
						public void run() {
							ltrace.setTraceColor(region.getRegionColor());
							ltrace.setPointStyle(getPointStyle(ltrace.getName()));
							ltrace.setPointSize(6);
						}
					});
				}
			}
		}
	}

	protected PointStyle getPointStyle(String name) {
		if (name.contains("(Y)")) return PointStyle.TRIANGLE;
		if (name.contains("(Z)")) return PointStyle.CIRCLE;
		return PointStyle.XCROSS;
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
	
	private static int colourIndex = 0;
	private final IRegion add(final LinearROI line, String name) throws Exception {
		
		IPlottingSystem sys = getPlottingSystem();
		final IRegion   reg = sys.createRegion(RegionUtils.getUniqueName(name, sys), RegionType.LINE);
		reg.setROI(line);
		if (sys.getRegions()!=null) {
			reg.setRegionColor(ColorUtility.getSwtColour(colourIndex));
			colourIndex++;
		}
		sys.addRegion(reg);		
	
		return reg;
	}


	@Override
	protected RegionType getCreateRegionType() {
		return RegionType.POINT;
	}

	public void activate() {

		super.activate();
		if (getPlottingSystem()!=null) {
			getPlottingSystem().addRegionListener(pointListener);
			
			// TODO See if current data is part of an ILazyDataset, if is enable z actions
		}
	}
	
	public void deactivate() {

		super.deactivate();
		if (getPlottingSystem()!=null) {
			getPlottingSystem().removeRegionListener(pointListener);
		}
	}

}
