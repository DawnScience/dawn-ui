package org.dawnsci.plotting.tools.finding;

import java.util.Collection;

import org.dawnsci.common.widgets.spinner.FloatSpinner;
import org.dawnsci.plotting.tools.Activator;
import org.dawnsci.plotting.tools.preference.PeakFindingConstants;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Scale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.peakfinding.IPeakFindingService;
import uk.ac.diamond.scisoft.analysis.peakfinding.PeakFindingData;

/**
 * 
 * Widget to handle all the components that would go into using a peak finder algorithm.
 * 
 * @author Dean P. Ottewell
 */
public class PeakFindingWidget {
	
	private static final Logger logger = LoggerFactory.getLogger(PeakFindingWidget.class);

	PeakFindingController controller;

	private FloatSpinner uprBndVal;
	private FloatSpinner lwrBndVal;	

	private FloatSpinner searchIntensity;
	public double searchScaleVal;
	private Scale searchScale;
	
	Button runPeakSearch;

	public PeakFindingWidget(PeakFindingController controller){
		this.controller = controller;
	}
	
	public void createControl(final Composite parent){

		Group configure = new Group(parent, SWT.NONE);
		configure.setText("Peakfinding Configuration ");
		configure.setLayout(new GridLayout(1, false));
		configure.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		configure.setBackground(parent.getBackground());

		Composite configureComposite = new Composite(configure, SWT.NONE);
		configureComposite.setLayoutData(new GridData(SWT.FILL, SWT.LEFT, true, false));
		configureComposite.setLayout(new GridLayout(2, false));

		// GET SAMPLE CONFIGURATION TODO: refactor and extract earlier
		//Where else am i going to get this?
		ILineTrace sampleTrace = controller.getPeakfindingtool().sampleTrace;
		
		
		
	
		Label lwrBndLab = new Label(configureComposite, SWT.NONE);
		lwrBndLab.setText("Lower Bound");
		lwrBndLab.setToolTipText("As shown by the second vertical line");
		
		lwrBndVal = new FloatSpinner(configureComposite, SWT.BORDER,  sampleTrace.getXData().getSize(), 3);
		lwrBndVal.setLayoutData(new GridData(SWT.FILL, SWT.LEFT, true, false));
		lwrBndVal.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
			
					
				//controller.setLowerBnd(lwrBndVal.getDouble());
			
			
			}
		});
	
		Label upperBndLab = new Label(configureComposite, SWT.NONE);
		upperBndLab.setText("Upper Bound");
		upperBndLab.setToolTipText("As shown by the vertical line");

	
		uprBndVal = new FloatSpinner(configureComposite, SWT.BORDER, sampleTrace.getXData().getSize(), 3);
		uprBndVal.setLayoutData(new GridData(SWT.FILL, SWT.LEFT, true, false));
		uprBndVal.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
			
				
				//controller.setUpperBnd(uprBndVal.getDouble());
			
			
			}
		});

		
		/* Adjust Peak Finding searchIntensity */
		// TODO: quick tmp implementation
		Label searchIntensityLab = new Label(configureComposite, SWT.NONE);
		searchIntensityLab.setText("Search Intensity");
		searchIntensityLab.setToolTipText("Higher values tend to lead to less peaks");

		searchIntensity = new FloatSpinner(configureComposite, SWT.BORDER, 1000, 3);
		searchIntensity.setMaximum(300);
		searchIntensity.setMinimum(1);
		searchIntensity.setLayoutData(new GridData(SWT.FILL, SWT.LEFT, true, false));
		searchIntensity.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				// Adjust parameter to be passed to appropriate peak finder
				searchScaleVal = searchIntensity.getDouble();
				controller.setSearchScaleIntensity(searchScaleVal);
			}
		});	
		
		searchScale = new Scale(configure, SWT.HORIZONTAL);
		searchScale.setBounds(0, 0, 40, 200);
		searchScale.setOrientation(SWT.LEFT_TO_RIGHT);

		searchScale.setMaximum(300);
		searchScale.setMinimum(1);
		
		searchScale.setIncrement(1);
		searchScale.setPageIncrement(30);
		
		searchScale.setLayoutData(new GridData(SWT.FILL, SWT.LEFT, true, false));
		searchScale.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				int perspectiveValue = searchScale.getSelection(); 
				searchIntensity.setDouble(perspectiveValue); 
				//TODO: does this trigger event as have selection listener so should have a on change
				searchScaleVal = searchIntensity.getDouble();
				controller.setSearchScaleIntensity(searchScaleVal);
			}
		});
		/*Default setups*/
		searchScaleVal = searchIntensity.getDouble();
		controller.setSearchScaleIntensity(searchScaleVal);
		
		/*
		 * Swap out peak finders		
		 * */
		Label peakFinderLab = new Label(configure, SWT.NONE);
		peakFinderLab.setLayoutData(new GridData(SWT.CENTER, SWT.LEFT, true, false));
		peakFinderLab.setText("Peak Finder:");
		
		final Combo peakfinderCombo = new Combo(configure, SWT.READ_ONLY);
		peakfinderCombo.setToolTipText("Select a type of peakfinder");
		peakfinderCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		peakfinderCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				controller.setPeakFinderID(peakfinderCombo.getText());			
			}
		});
		
		controller.setPeakFindServ((IPeakFindingService) Activator.getService(IPeakFindingService.class));
		//Load in peak finders
		Collection<String> peakFinders = controller.getPeakFindServ().getRegisteredPeakFinders();
		controller.setPeakFindData(new PeakFindingData(controller.getPeakFindServ()));

		for (String pfID : peakFinders) {
			String nameID = controller.getPeakFindServ().getPeakFinderName(pfID);
			// peakFindData.activatePeakFinder(pfID);
			peakfinderCombo.add(nameID);
		
			controller.setPeakFinderID(pfID); // TODO: another tmp fix
		}			
		peakfinderCombo.select(0);
		controller.setPeakFinderID(peakfinderCombo.getText().toString()); 
		Activator.getPlottingPreferenceStore().setValue(PeakFindingConstants.PeakAlgorithm, peakfinderCombo.getText().toString());		
		
		runPeakSearch = new Button(configure, SWT.PUSH);
		runPeakSearch.setImage(Activator.getImageDescriptor("icons/peakSearch.png").createImage());
		runPeakSearch.setText("Run Peak Finder");
		runPeakSearch.setLayoutData(new GridData(GridData.FILL_BOTH));
		runPeakSearch.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				runPeakSearch.setEnabled(false);
				runPeakSearch.setImage(Activator.getImageDescriptor("icons/peakSearching.png").createImage());			
				// Run peakSearch
				controller.peakSearchJob= new PeakSearchJob(controller);
				//TODO:Auto schedule in controller func
				controller.peakSearchJob.schedule();
				//controller.formatPeakSearch();
			}
		});
	}
	
	public void setLwrBndVal(double lowerVal) {
		lwrBndVal.setDouble(lowerVal);
		lwrBndVal.update();
	}
	
	public void setUprBndVal(double upperVal) {
		uprBndVal.setDouble(upperVal);
		uprBndVal.update();
	}

}
