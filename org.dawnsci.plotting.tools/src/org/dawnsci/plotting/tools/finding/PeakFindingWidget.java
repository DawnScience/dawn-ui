package org.dawnsci.plotting.tools.finding;
	
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.dawnsci.common.widgets.spinner.FloatSpinner;
import org.dawnsci.plotting.tools.Activator;
import org.dawnsci.plotting.tools.preference.PeakFindingConstants;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Scale;

import uk.ac.diamond.scisoft.analysis.fitting.functions.IdentifiedPeak;
import uk.ac.diamond.scisoft.analysis.peakfinding.IPeakFindingService;
import uk.ac.diamond.scisoft.analysis.peakfinding.PeakFindingData;

/**
 * Widget to handle all the components that would go into using a peak finder algorithm.
 * 
 * @author Dean P. Ottewell
 */
public class PeakFindingWidget {

	PeakFindingManager manager;

	private FloatSpinner uprBndVal;
	private FloatSpinner lwrBndVal;	

	private FloatSpinner searchThreshold;
	public double searchThresVal;
	private Scale searchScale;
	
	Button runPeakSearch;
	
	List<IdentifiedPeak> peaks = new ArrayList<IdentifiedPeak>();
	Dataset xData = null;
	Dataset yData = null ;

	private Image searchImage;

	private Image searchingImage;
	
	public PeakFindingWidget(PeakFindingManager controller){
		this.manager = controller;
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
		
		Button adjustSearchBtn = new Button(configureComposite, SWT.BUTTON1);
		adjustSearchBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, true));
		adjustSearchBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				manager.activateSearchRegion();
			}
		});
		adjustSearchBtn.setImage(Activator.getImageAndAddDisposeListener(adjustSearchBtn, "icons/plot-tool-peak-fit.png"));
		
		Composite boundsConfig = new Composite(configureComposite, SWT.NONE);
		boundsConfig.setLayoutData(new GridData(SWT.FILL, SWT.LEFT, true, false));
		boundsConfig.setLayout(new GridLayout(2, false));
		//TODO: Base of trace size
		int max = 100000; 
		
		Label lwrBndLab = new Label(boundsConfig , SWT.NONE);
		lwrBndLab.setText("Lower Bound");
		lwrBndLab.setToolTipText("As shown by the second vertical line");
		
		
		lwrBndVal = new FloatSpinner(boundsConfig , SWT.BORDER,  max , 3);
		lwrBndVal.setLayoutData(new GridData(SWT.FILL, SWT.LEFT, true, false));
		lwrBndVal.setPrecision(4);
		lwrBndVal.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				PeakOpportunity peakOpp = new PeakOpportunity();
				peakOpp.setUpperBound(uprBndVal.getDouble());
				peakOpp.setLowerBound(lwrBndVal.getDouble());
				manager.loadPeakOpportunity(peakOpp);
			}
		});
		
		Label upperBndLab = new Label(boundsConfig , SWT.NONE);
		upperBndLab.setText("Upper Bound");
		upperBndLab.setToolTipText("As shown by the vertical line");
		
		uprBndVal = new FloatSpinner(boundsConfig , SWT.BORDER, max , 3);
		uprBndVal.setLayoutData(new GridData(SWT.FILL, SWT.LEFT, true, false));
		uprBndVal.setPrecision(4);
		uprBndVal.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				PeakOpportunity peakOpp = new PeakOpportunity();
				peakOpp.setUpperBound(uprBndVal.getDouble());
				peakOpp.setLowerBound(lwrBndVal.getDouble());
				manager.loadPeakOpportunity(peakOpp);
			}
		});

		/* Adjust Peak Finding searchIntensity */
		final Label searchThresholdLab = new Label(configureComposite, SWT.NONE);
		searchThresholdLab.setText("Search Threshold");
		searchThresholdLab.setToolTipText("Higher values tend to lead to less peaks");

		searchThreshold = new FloatSpinner(configureComposite, SWT.BORDER, 1000, 3);
		searchThreshold.setMaximum(300);
		searchThreshold.setMinimum(1);
		searchThreshold.setLayoutData(new GridData(SWT.FILL, SWT.LEFT, true, false));
		searchThreshold.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				
				searchThresVal = searchThreshold.getDouble();
				manager.setSearchThreshold(searchThresVal);
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
				searchThreshold.setDouble(perspectiveValue); 
				//TODO: need to change as changing the search intensity and using that trigger...
				searchThresVal = searchThreshold.getDouble();
				manager.setSearchThreshold(searchThresVal);
			}
		});
		
		/*Default setups*/
		searchThresVal = searchThreshold.getDouble();

		manager.setSearchThreshold(searchThresVal);
		
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
				//XXX: hot ui fix as wavelet only component that needs the intensity active
				if(peakfinderCombo.getText().equals("Wavelet Transform")){
					searchThreshold.setEnabled(true);
					searchScale.setEnabled(true);
					searchScale.setVisible(true);
					searchThresholdLab.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
				} else {
					searchThresholdLab.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_GRAY));
					searchThreshold.setEnabled(false);
					searchScale.setEnabled(false);
					searchScale.setVisible(false);
				}
				
				manager.setPeakFinderID(peakfinderCombo.getText());			
			}
		});
		
		PeakFindingManager.setPeakFindServ((IPeakFindingService) Activator.getService(IPeakFindingService.class));
		//Load in peak finders
		Collection<String> peakFinders = PeakFindingManager.getPeakFindServ().getRegisteredPeakFinders();

		manager.setPeakFindData(new PeakFindingData(PeakFindingManager.getPeakFindServ()));

		for (String pfID : peakFinders) {
			String nameID = PeakFindingManager.getPeakFindServ().getPeakFinderName(pfID);
			peakfinderCombo.add(nameID);
			manager.setPeakFinderID(pfID); 
		}			
		peakfinderCombo.select(1); 
		
		manager.setPeakFinderID(peakfinderCombo.getText().toString()); 
		Activator.getPlottingPreferenceStore().setValue(PeakFindingConstants.PeakAlgorithm, peakfinderCombo.getText().toString());		
		
		runPeakSearch = new Button(configure, SWT.PUSH);
		runPeakSearch.setImage(Activator.getImageAndAddDisposeListener(runPeakSearch, "icons/peakSearch.png"));
		runPeakSearch.setText("Run Peak Finder");
		runPeakSearch.setLayoutData(new GridData(GridData.FILL_BOTH));
		runPeakSearch.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				Double lower = lwrBndVal.getDouble();
				Double upper = uprBndVal.getDouble();
				//Bounds check 
				if(upper > lower){
					//Well they want a boudnd setup. So give them it swapped 
					PeakOpportunity peakOpp = new PeakOpportunity();
					peakOpp.setLowerBound(uprBndVal.getDouble());
					peakOpp.setUpperBound(lwrBndVal.getDouble());
					
					manager.setSearchThreshold(searchThreshold.getDouble());
					manager.loadPeakOpportunity(peakOpp);
				} 	
				
				// Run peakSearch
				manager.setPeakSearching();

			}
		});

		searchImage = Activator.getImageDescriptor("icons/peakSearch.png").createImage();
		searchingImage = Activator.getImageDescriptor("icons/peakSearching.png").createImage();

		//TODO: check is searching instead
		manager.addPeakListener(new IPeakOpportunityListener() {
			@Override
			public void peaksChanged(PeakOpportunityEvent evt) {
				peaks = evt.getPeakOpp().getPeaksId();
			}

			@Override
			public void boundsChanged(double upper, double lower) {
				setLowerBound(lower);
				setUpperBound(upper);
			}

			
			//TODO: trigger this intially on a base search
			@Override
			public void dataChanged(Dataset nXData, Dataset nYData) {
				xData = nXData;
				yData = nYData;
			}

			@Override
			public void isPeakFinding() {
				runPeakSearch.setEnabled(false);
				runPeakSearch.setImage(searchingImage);

	 			if(xData != null && yData != null){	 				
	 				//TODO: set the searching mode ...
					manager.peakSearchJob = new PeakFindingSearchJob(manager, xData, yData);
					manager.peakSearchJob.schedule();		
	 			} else {
					//No peak data set...
					peaks.clear();
					manager.setPeaksId(peaks);
					runPeakSearch.setEnabled(true);
					runPeakSearch.setImage(searchImage);
				}
			}

			@Override
			public void finishedPeakFinding() {
				runPeakSearch.setEnabled(true);
				runPeakSearch.setImage(searchImage);
			}

			@Override
			public void activateSearchRegion() {
				// TODO Auto-generated method stub
			}
			
		});
	
	}

	public void dispose() {
		if (searchImage != null) {
			searchImage.dispose();
		}
		if (searchingImage != null) {
			searchingImage.dispose();
		}
	}
	public void setLowerBound(double lowerVal) {
		lwrBndVal.setDouble(lowerVal);
		lwrBndVal.update();
	}
	
	public void setUpperBound(double upperVal) {
		uprBndVal.setDouble(upperVal);
		uprBndVal.update();
	}

}
