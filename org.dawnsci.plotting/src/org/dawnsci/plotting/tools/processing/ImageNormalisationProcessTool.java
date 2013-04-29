package org.dawnsci.plotting.tools.processing;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.dawb.common.ui.plot.AbstractPlottingSystem;
import org.dawnsci.plotting.api.region.IRegion;
import org.dawnsci.plotting.api.region.IRegion.RegionType;
import org.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.DatasetUtils;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.roi.IROI;
import uk.ac.diamond.scisoft.analysis.roi.ROIProfile;
import uk.ac.diamond.scisoft.analysis.roi.RectangularROI;

/**
 * Tool to normalize an image with given specific parameters
 * @author wqk87977
 *
 */
public class ImageNormalisationProcessTool extends ImageProcessingTool {

	private final Logger logger = LoggerFactory.getLogger(ImageNormalisationProcessTool.class);
	private boolean isDirty = false;
	private AbstractDataset profile;

	public ImageNormalisationProcessTool() {
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void configurePlottingSystem(AbstractPlottingSystem plotter) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void configureDisplayPlottingSystem(AbstractPlottingSystem plotter) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void createControlComposite(Composite parent) {
		try {
			Group radioGroupNorm = new Group(parent, SWT.NONE);
			radioGroupNorm.setLayout(new GridLayout(1, false));
			radioGroupNorm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			radioGroupNorm.setText("Normalisation type");
			createRadioControls(radioGroupNorm, createNormActions());

			Group radioGroupOperation = new Group(parent, SWT.NONE);
			radioGroupOperation.setLayout(new GridLayout(1, false));
			radioGroupOperation.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			radioGroupOperation.setText("Operation");
			createRadioControls(radioGroupOperation, createOperationActions());

			Composite smoothingComp = new Composite(parent, SWT.NONE);
			smoothingComp.setLayout(new GridLayout(2, false));
			Label smoothingLabel = new Label(smoothingComp, SWT.NONE);
			smoothingLabel.setText("Smoothing:");
			Spinner smoothingSpinner = new Spinner(smoothingComp, SWT.BORDER);
			smoothingSpinner.setMinimum(1);
			smoothingSpinner.setMaximum(Integer.MAX_VALUE);
			smoothingSpinner.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					widgetDefaultSelected(e);
				}
				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
					// TODO Auto-generated method stub
					
				}
			});
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("Could not create controls:"+e);
		}
	}

	private List<Entry<String, Action>> createNormActions(){
		List<Entry<String, Action>> radioActions = new ArrayList<Entry<String, Action>>();
		Entry<String, Action> noNormalisation = new AbstractMap.SimpleEntry<String, Action>("None",
			new Action("None") {
				@Override
				public void run() {
					//TODO
					System.out.println("No normalisation");
				}
			}
		);
		Entry<String, Action> roiNormalisation = new AbstractMap.SimpleEntry<String, Action>("ROI normalisation",
				new Action("ROI normalisation") {
					@Override
					public void run() {
						//TODO
						System.out.println("ROI normalisation");
					}
				}
			);
		Entry<String, Action> auxNormalisation = new AbstractMap.SimpleEntry<String, Action>("Auxiliary normalisation",
				new Action("Auxiliary normalisation") {
					@Override
					public void run() {
						//TODO
						System.out.println("Auxiliary normalisation");
					}
				}
			);
		radioActions.add(noNormalisation);
		radioActions.add(roiNormalisation);
		radioActions.add(auxNormalisation);
		return radioActions;
	}

	private List<Entry<String, Action>> createOperationActions(){
		List<Entry<String, Action>> radioActions = new ArrayList<Entry<String, Action>>();
		
		Entry<String, Action> noOperation = new AbstractMap.SimpleEntry<String, Action>("None",
				new Action("None") {
					@Override
					public void run() {
						//TODO
						System.out.println("None");
					}
				}
			);
		Entry<String, Action> subtractOperation = new AbstractMap.SimpleEntry<String, Action>("Subtract",
			new Action("Subtract") {
				@Override
				public void run() {
					//TODO
					System.out.println("Subtract");
				}
			}
		);
		Entry<String, Action> divideOperation = new AbstractMap.SimpleEntry<String, Action>("Divide",
				new Action("Divide") {
					@Override
					public void run() {
						//TODO
						System.out.println("Divide");
					}
				}
			);
		radioActions.add(noOperation);
		radioActions.add(subtractOperation);
		radioActions.add(divideOperation);
		return radioActions;
	}

	@Override
	protected void createProfile(IImageTrace image, IRegion region, IROI roi,
			boolean tryUpdate, boolean isDrag, IProgressMonitor monitor) {
		if(isDirty){
			AbstractDataset ds = ((AbstractDataset)image.getData()).clone();
			AbstractDataset tile = profile.reshape(profile.getShape()[0],1);
			if(roi == null){
				isDirty = false;
				return;
			}
			double width = ((RectangularROI)roi).getLengths()[0];
			tile.idivide(width);
			AbstractDataset correction = DatasetUtils.tile(tile, ds.getShape()[1]);
			ds.idivide(correction);
			
			profilePlottingSystem.updatePlot2D(ds, image.getAxes(), monitor);
			isDirty = false;
		}
		

	}

	@Override
	protected void createDisplayProfile(IImageTrace image, IRegion region,
			IROI roi, boolean tryUpdate, boolean isDrag,
			IProgressMonitor monitor) {
		
		AbstractDataset ds = ((AbstractDataset)image.getData()).clone();
		if(roi == null){
			isDirty = true;
			return;
		}
		AbstractDataset[] profiles = ROIProfile.box(ds, (RectangularROI)roi);
		profile = profiles[1];
		List<IDataset> data = new ArrayList<IDataset>();
		data.add(profiles[1]);
		displayPlottingSystem.clear();
		displayPlottingSystem.createPlot1D(image.getAxes().get(1), data, monitor);
		isDirty = true;
	}

	@Override
	protected RegionType getCreateRegionType() {
		return RegionType.XAXIS;
	}
}
