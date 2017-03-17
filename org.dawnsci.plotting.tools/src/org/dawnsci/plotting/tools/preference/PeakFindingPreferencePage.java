package org.dawnsci.plotting.tools.preference;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.dawnsci.analysis.api.peakfinding.IPeakFinder;
import org.eclipse.dawnsci.analysis.api.peakfinding.IPeakFinderParameter;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.peakfinding.IPeakFindingService;
import uk.ac.diamond.scisoft.analysis.peakfinding.PeakFindingServiceImpl;

import org.dawnsci.common.widgets.spinner.FloatSpinner;
import org.dawnsci.plotting.tools.Activator;
import org.dawnsci.plotting.tools.finding.PeakFindingTool;
import org.dawnsci.plotting.tools.preference.PeakFindingConstants;


/**
 * //TODO: is the workbench preference page still the best page...
 * 
 * removed the algorithm list
 * 
 * @author Dean P. Ottewell
 *
 */
public class PeakFindingPreferencePage extends PreferencePage implements IWorkbenchPreferencePage { 

	public static final String ID = "org.dawb.workbench.plotting.PeakFindingPreferencePage";
	
	private static final Logger logger = LoggerFactory.getLogger(PeakFindingPreferencePage.class);
	
	private CCombo algorithmCombo; 	 	

	private Composite contentPanel;
	private StackLayout controlsLayout;
	List<Composite> controlPages = new ArrayList<Composite>();
	
	
	private Collection<String> peakFindersID;
	Map<String,Number> params = new HashMap<String, Number>();

	public PeakFindingPreferencePage() {
		
	}
	
	/**
	 * @wbp.parser.constructor
	 */
	public PeakFindingPreferencePage(String title) {
		super(title);	
	}

	public PeakFindingPreferencePage(String title, ImageDescriptor image) {
		super(title, image);
	}

	@Override
	public void init(IWorkbench workbench) {
			setPreferenceStore(Activator.getPlottingPreferenceStore()); 
	}

	@Override
	protected Control createContents(final Composite parent) {
		final Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayout(new GridLayout(1, false));
		GridData gdc = new GridData(SWT.LEFT, SWT.FILL, false, true);
		comp.setLayoutData(gdc);

		Group algGroup = new Group(comp, SWT.NONE);
		algGroup.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
		algGroup.setLayout(new GridLayout(2, false));
		algGroup.setText("Peak Finding Algorithm Controls");
		
		contentPanel = new Composite(comp, SWT.BORDER);
		contentPanel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		contentPanel.setLayout(new GridLayout(1, false));
		
		controlsLayout  = new StackLayout();
	    contentPanel.setLayout(controlsLayout);
		

//		TODO: probably plae here the algorithm btu automatic peak detector is the one really but could use the others anway and 
//		just defautl to to this
		Label algoLabel = new Label(algGroup, SWT.NONE);
		algoLabel.setText("Peak Finder");
		algorithmCombo = new CCombo(algGroup, SWT.BORDER);
		algorithmCombo.setEditable(false);
		algorithmCombo.setListVisible(true);
		algorithmCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		algorithmCombo.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				if(algorithmCombo.getSelectionIndex() != -1){
					controlsLayout.topControl =  controlPages.get(algorithmCombo.getSelectionIndex());
		        	contentPanel.layout();
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
			}
		});

		getPreferenceStore().getString(PeakFindingConstants.PeakAlgorithm);
		
		//Needed to initilize this premtively because load in services	
		peakFindersID = PeakFindingConstants.PEAKFINDERS;
		for (String finderID : peakFindersID){
			algorithmCombo.add(finderID); //0th id is the page number
		    final Composite pageN = new Composite(contentPanel, SWT.NONE);
			pageN.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
			pageN.setLayout(new GridLayout(2, false));
			loadPeakFinderParams(pageN, finderID);
			controlPages.add(pageN);
		}
		algorithmCombo.select(0);
		controlsLayout.topControl =  controlPages.get(algorithmCombo.getSelectionIndex());
    	contentPanel.layout();
    	
		return comp;
	}
	
	private void loadPeakFinderParams(Composite parentLayout, String desiredFinder){
		String currPeakFinderID =  desiredFinder;//algorithmCombo.getText();

		//Well cannot clear preference values so therefore just load in every parameter as default
		getPreferenceStore().setValue(PeakFindingConstants.PeakAlgorithm, currPeakFinderID);
		
		//TODO: store params as constnats?
		IPeakFindingService peakFindServ = (IPeakFindingService)Activator.getService(IPeakFindingService.class);
		
		Map<String, IPeakFinderParameter> peakParams = peakFindServ.getPeakFinderParameters(currPeakFinderID);
		
		for (Entry<String, IPeakFinderParameter> peakParam : peakParams.entrySet()){
			IPeakFinderParameter param = peakParam.getValue();
			String curVal = getPreferenceStore().getString(peakParam.getKey());
			
			Number val = Double.parseDouble(curVal);
			if (param.isInt())
				val = (int) val.doubleValue();
			param.setValue(val);
			
			genParam(parentLayout, param);
		}
		
	}
	
	private void genParam(Composite paramSetting, final IPeakFinderParameter param){
		String value = param.getValue().toString();
		
		Label paramLab = new Label(paramSetting, SWT.NONE);
		paramLab.setText(param.getName());
		
		FloatSpinner valSpn = new FloatSpinner(paramSetting, SWT.BORDER, 6, 2);
		valSpn.setDouble(Double.parseDouble(value));
		valSpn.setLayoutData(new GridData(SWT.FILL, SWT.LEFT, true, false));
		valSpn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				Number val = valSpn.getDouble();
				if(param.isInt())
					val = (int)val.doubleValue();	
				params.put(param.getName(), val);
			}
		});
	
		valSpn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				getPreferenceStore().setValue(param.getName(), valSpn.getDouble());
			}
		});
	}
	
	@Override
	public boolean performOk() {
		return storePreferences();
	}
	
	private boolean storePreferences() {
		//checkState();
		if (!isValid())
			return false;
		
		getPreferenceStore().setValue(PeakFindingConstants.PeakAlgorithm, algorithmCombo.getText());
		
		Iterator<Entry<String, Number>> iterator = params.entrySet().iterator();
		while(iterator.hasNext()) {
			Entry<String, Number> param = iterator.next();
			//TOOD: check the type before castin
			getPreferenceStore().setValue(param.getKey(), param.getValue().doubleValue());
			params.remove(param.getKey());
		}
		
		return true;
	}
	
	private void initializePage() {
		//Load in defaults
		performDefaults();
	}

	@Override
	protected void performDefaults() {
		isValid();
			
		//getPreferenceStore().setValue(PeakFindingConstants.PeakAlgorithm, algorithmCombo.getText());
		
		//Clear the previous pages
		controlPages.clear();
		IPeakFindingService peakFindServ = (IPeakFindingService)Activator.getService(IPeakFindingService.class);
	
		Iterator<String> peakfinder = peakFindServ.getRegisteredPeakFinders().iterator();
		while(peakfinder.hasNext()){
			Map<String, IPeakFinderParameter> peakParams = peakFindServ.getPeakFinderParameters(peakfinder.next());
			for (Entry<String, IPeakFinderParameter> peakParam : peakParams.entrySet()){
				String defaultVal =getPreferenceStore().getDefaultString(peakParam.getKey());
				getPreferenceStore().setValue(peakParam.getKey(),defaultVal);
			}
		}
		
		//reload pages with default values
		peakFindersID = PeakFindingConstants.PEAKFINDERS;
		for (String finderID : peakFindersID){
		    final Composite pageN = new Composite(contentPanel, SWT.NONE);
			pageN.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
			pageN.setLayout(new GridLayout(2, false));
			loadPeakFinderParams(pageN, finderID);
		    controlPages.add(pageN);
		}
		
		Object choicee = algorithmCombo.getSelectionIndex();
		controlsLayout.topControl =  controlPages.get(algorithmCombo.getSelectionIndex());
    	contentPanel.layout();
	}

}
