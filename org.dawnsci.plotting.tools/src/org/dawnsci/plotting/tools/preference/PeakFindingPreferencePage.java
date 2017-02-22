package org.dawnsci.plotting.tools.preference;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.dawnsci.analysis.api.peakfinding.IPeakFinderParameter;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.peakfinding.IPeakFindingService;

import org.dawnsci.plotting.tools.Activator;
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
	private Group specificFinderParams;
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
	protected Control createContents(Composite parent) {
		final Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayout(new GridLayout(1, false));
		GridData gdc = new GridData(SWT.FILL, SWT.FILL, true, true);
		comp.setLayoutData(gdc);

		Group algGroup = new Group(comp, SWT.NONE);
		algGroup.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		algGroup.setLayout(new GridLayout(2, false));
		algGroup.setText("Peak Finding Algorithm Controls");
		
		specificFinderParams = new Group(comp, SWT.NONE);
		specificFinderParams.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		specificFinderParams.setLayout(new GridLayout(2, false));
		specificFinderParams.setText("Peak Finding Algorithm Controls");
		
//		TODO: probably plae here the algorithm btu automatic peak detector is the one really but could use the others anway and 
//		just defautl to to this
		Label algoLabel = new Label(algGroup, SWT.NONE);
		algoLabel.setText("Peak Finder");
		algorithmCombo = new CCombo(algGroup, SWT.BORDER);
		algorithmCombo.setEditable(false);
		algorithmCombo.setListVisible(true);
		algorithmCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		algorithmCombo.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				//Dispose of those nasty brats
				for (Control childControl : specificFinderParams.getChildren()){
					childControl.dispose();
				}
				
			loadPeakFinderParams(specificFinderParams, false);

				specificFinderParams.pack();
			}
		});
		
		getPreferenceStore().getString(PeakFindingConstants.PeakAlgorithm);
		
		//Needed to initilize this premtively because load in services	
		peakFindersID = PeakFindingConstants.PEAKFINDERS;
		for (String finderID : peakFindersID){
			algorithmCombo.add(finderID);
		}
		algorithmCombo.select(0);
		
		initializePage();
		return comp;
	}
	
	
	/**
	 * @param specificFinderSetting
	 * @param isDefault
	 * 
	 */
	private void loadPeakFinderParams(Group specificFinderSetting, boolean isDefault){
		String currPeakFinderID = algorithmCombo.getText();

		//Well cannot clear preference values so therefore just load in every parameter as default
	getPreferenceStore().setValue(PeakFindingConstants.PeakAlgorithm, currPeakFinderID);

		//TODO: store params as constnats?
		IPeakFindingService peakFindServ = (IPeakFindingService)Activator.getService(IPeakFindingService.class);
		Map<String, IPeakFinderParameter> peakParams = peakFindServ.getPeakFinderParameters(currPeakFinderID);


		for (Entry<String, IPeakFinderParameter> peakParam : peakParams.entrySet()){
			IPeakFinderParameter param = peakParam.getValue();
			
			if(!isDefault){
				String curVal = getPreferenceStore().getString(peakParam.getKey());
				Number val = Double.parseDouble(curVal);
				if (param.isInt())
					val = (int) val.doubleValue();
				param.setValue(val);
			}

			genParam(specificFinderSetting, param);
		}
		
	}
	
	private void genParam(Group paramSetting, final IPeakFinderParameter param){
		String value = param.getValue().toString();
		
		Label paramLab = new Label(paramSetting, SWT.NONE);
		paramLab.setText(param.getName());
		final Text valTxt = new Text(paramSetting, SWT.BORDER);
		valTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		valTxt.setText(value);
		
		//TODO: validate vals
		valTxt.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {				
				Number val = Double.parseDouble(valTxt.getText());
				if(param.isInt())
					val = (int)val.doubleValue();
				params.put(param.getName(), val);
				
			}
		});
		
		
		
		valTxt.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				getPreferenceStore().setValue(param.getName(), Double.parseDouble(valTxt.getText()));
				//getPreferenceStore().setValue(	FittingConstants.SMOOTHING, smooth);
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
			
		getPreferenceStore().setValue(PeakFindingConstants.PeakAlgorithm, algorithmCombo.getText());
		for (Control childControl : specificFinderParams.getChildren()){
			childControl.dispose();
		}
		
		loadPeakFinderParams(specificFinderParams,true);
		specificFinderParams.pack();
	}

}
