package org.dawnsci.plotting.tools.fitting;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.dawnsci.common.widgets.decorator.IntegerDecorator;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import uk.ac.diamond.scisoft.analysis.fitting.functions.APeak;

public class PeakPrepopulateTool extends Dialog {
	
	private Combo peakTypeCombo;
	private Composite dialogContainer;
	private Text nrPeaksTxtBox;
	private Integer nrPeaks = null;
	
	private Map<String, String> peakFnMap = new TreeMap<String, String>();
	private String[] availPeakTypes;
	
	private FunctionFittingTool parentFittingTool;
	
	public PeakPrepopulateTool(Shell parentShell, FunctionFittingTool parentFittingTool) {
		super(parentShell);
		this.parentFittingTool = parentFittingTool;
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		//Create/get the base containers and set up the grid layout
		Composite windowArea = (Composite) super.createDialogArea(parent);
		dialogContainer = new Composite(windowArea, SWT.NONE);
		GridLayout toolGridLayout = new GridLayout(2, false);
		dialogContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		dialogContainer.setLayout(toolGridLayout);
		
		//Number of peaks text field
		GridData nrPeaksGridData = new GridData();
		nrPeaksGridData.grabExcessHorizontalSpace = true;
		nrPeaksGridData.horizontalAlignment = GridData.FILL;
		
		Label nrPeaksTxtLbl = new Label(dialogContainer, SWT.None);
		nrPeaksTxtLbl.setText("Number of Peaks:");
		
		nrPeaksTxtBox = new Text(dialogContainer, SWT.BORDER); 
		nrPeaksTxtBox.setLayoutData(nrPeaksGridData);
		
		IntegerDecorator nrPeaksIDec = new IntegerDecorator(nrPeaksTxtBox);
		nrPeaksIDec.setMinimum(0);
		
		//Profile type combo box
		GridData peakTypeGridData = new GridData();
		peakTypeGridData.grabExcessHorizontalSpace = true;
		peakTypeGridData.horizontalAlignment = GridData.FILL;
		
		Label peakTypeCmbLbl = new Label(dialogContainer, SWT.None);
		peakTypeCmbLbl.setText("Peak Function Type:");
		
		peakTypeCombo = new Combo(dialogContainer, SWT.READ_ONLY);
		setAvailPeakFunctions();
		setDefaultPeakFunction();
		peakTypeCombo.setLayoutData(peakTypeGridData);
		
		
		return windowArea;
	}
	
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		//Create Close & Find Peaks buttons.
		createButton(parent, IDialogConstants.PROCEED_ID, "Find Peaks", true);
		createButton(parent, IDialogConstants.CLOSE_ID, IDialogConstants.CLOSE_LABEL, false);
	}
	
	@Override
	protected void buttonPressed(int buttonId){
		if (IDialogConstants.PROCEED_ID == buttonId) {
			parentFittingTool.findInitialPeaks(nrPeaks);
			System.out.println("Find Peaks pressed");
		}
		else if (IDialogConstants.CLOSE_ID == buttonId) {
			setReturnCode(OK); //This is just returning 0 - might not be needed
			close();
		}
	}
	
	private void setAvailPeakFunctions() {
		//Get the list of available function types and set default value
		
		for (final Class<? extends APeak> peak : FittingUtils.getPeakOptions().values()) {
			peakFnMap.put(peak.getSimpleName(), peak.getName());
		}
		Set<String> availPeakTypeSet = peakFnMap.keySet();
		availPeakTypes = (String[]) availPeakTypeSet.toArray(new String[availPeakTypeSet.size()]);
		peakTypeCombo.setItems(availPeakTypes);
	}
	
	private void setDefaultPeakFunction() {
		int defaultPeakFnIndex = Arrays.asList(availPeakTypes).indexOf("PseudoVoigt") == -1 ? Arrays.asList(availPeakTypes).indexOf("Gaussian") : Arrays.asList(availPeakTypes).indexOf("PseudoVoigt");
		if (defaultPeakFnIndex != -1) {
			peakTypeCombo.select(defaultPeakFnIndex);
		}
	}
	
//	private Integer getNrPeaksInteger() {
//		
//		IntegerDecorator text2Integer = new IntegerDecorator(nrPeaksTxtBox);
//		
//		return nrPeaks;
//	}

}
