package org.dawnsci.surfacescatter.ui;

import java.util.ArrayList;

import org.dawnsci.surfacescatter.GeometricParametersModel;
import org.dawnsci.surfacescatter.SuperModel;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;

public class GeometricParametersWindows extends Composite{

	private Button beamCorrection;
	private Text beamInPlane;
	private Text beamOutPlane;
	private Text covar;
	private Text detectorSlits;
	private Text inPlaneSlits;
	private Text inplanePolarisation;
	private Text outPlaneSlits;
	private Text outplanePolarisation;
	private Text reflectivityA;
	private Text sampleSize;
	private Text normalisationFactor;
	private Button specular;
	private Text imageName;
	private Text xName;
	private Text scalingFactor;
	private Text beamHeight;
	private Text footprint;
	private Text angularFudgeFactor;
	private Text savePath;
	private Text fluxPath;
	private TabFolder folder;
	private Text xNameRef;
	private SurfaceScatterPresenter ssp;
	private GeometricParametersWindows gpw;
	
	public GeometricParametersWindows(Composite parent, 
									  int style,
									  SurfaceScatterPresenter ssp){
		
		super(parent, style);
		
        new Label(this, SWT.NONE).setText("Geometric Parameters Window");
        
        this.ssp = ssp;
        this.gpw =this;
        
        this.createContents();
        
	}
	
	public void createContents() {
		
		
		folder = new TabFolder(this, SWT.NONE);
	    
	    //Tab 1
	    TabItem paramsSXRD = new TabItem(folder, SWT.NONE);
	    paramsSXRD.setText("SXRD Parameters");
	   
		Group geometricParametersSX = new Group(folder, SWT.NULL);
		GridLayout geometricParametersSXLayout = new GridLayout(2,true);
		GridData geometricParametersSXData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		geometricParametersSX.setLayout(geometricParametersSXLayout);
		geometricParametersSX.setLayoutData(geometricParametersSXData);
		
		new Label(geometricParametersSX, SWT.LEFT).setText("beamCorrection");
		beamCorrection = new Button(geometricParametersSX, SWT.CHECK);
		new Label(geometricParametersSX, SWT.LEFT).setText("beamInPlane");
		beamInPlane = new Text(geometricParametersSX, SWT.SINGLE);
		new Label(geometricParametersSX, SWT.LEFT).setText("beamOutPlane");
		beamOutPlane = new Text(geometricParametersSX, SWT.SINGLE);
		new Label(geometricParametersSX, SWT.LEFT).setText("covar");
		covar = new Text(geometricParametersSX, SWT.SINGLE);
		new Label(geometricParametersSX, SWT.LEFT).setText("detectorSlits");
		detectorSlits = new Text(geometricParametersSX, SWT.SINGLE);
		new Label(geometricParametersSX, SWT.LEFT).setText("inPlaneSlits");
		inPlaneSlits = new Text(geometricParametersSX, SWT.SINGLE);
		new Label(geometricParametersSX, SWT.LEFT).setText("inplanePolarisation");
		inplanePolarisation = new Text(geometricParametersSX, SWT.SINGLE);
		new Label(geometricParametersSX, SWT.LEFT).setText("outPlaneSlits");
		outPlaneSlits = new Text(geometricParametersSX, SWT.SINGLE);
		new Label(geometricParametersSX, SWT.LEFT).setText("outplanePolarisation");
		outplanePolarisation = new Text(geometricParametersSX, SWT.SINGLE);
		new Label(geometricParametersSX, SWT.LEFT).setText("reflectivityA");
		reflectivityA = new Text(geometricParametersSX, SWT.SINGLE);
		new Label(geometricParametersSX, SWT.LEFT).setText("sampleSize");
		sampleSize = new Text(geometricParametersSX, SWT.SINGLE);
		new Label(geometricParametersSX, SWT.LEFT).setText("normalisationFactor");
		scalingFactor = new Text(geometricParametersSX, SWT.SINGLE);
		new Label(geometricParametersSX, SWT.LEFT).setText("scalingFactor");
		normalisationFactor = new Text(geometricParametersSX, SWT.SINGLE);		
		new Label(geometricParametersSX, SWT.LEFT).setText("specular");
		specular = new Button (geometricParametersSX, SWT.CHECK);
		new Label(geometricParametersSX, SWT.LEFT).setText("imageName");
		imageName = new Text(geometricParametersSX, SWT.SINGLE);
		new Label(geometricParametersSX, SWT.LEFT).setText("xName");
		xName = new Text (geometricParametersSX, SWT.CHECK);
		new Label(geometricParametersSX, SWT.LEFT).setText("savePath");
		savePath = new Text (geometricParametersSX, SWT.CHECK);
		
		
		paramsSXRD.setControl(geometricParametersSX);
	   	    
	    //Tab 2
	    TabItem paramsReflec = new TabItem(folder, SWT.NONE);
	    paramsReflec.setText("Reflectivity Parameters");

		Group geometricParametersReflec = new Group(folder, SWT.NULL);
		GridLayout geometricParametersLayoutReflec = new GridLayout(2,true);
		GridData geometricParametersDataReflec = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		geometricParametersReflec.setLayout(geometricParametersLayoutReflec);
		geometricParametersReflec.setLayoutData(geometricParametersDataReflec);
		
		new Label(geometricParametersReflec, SWT.LEFT).setText("beamHeight");
		beamHeight = new Text(geometricParametersReflec, SWT.SINGLE);
		new Label(geometricParametersReflec, SWT.LEFT).setText("footprint");
		footprint = new Text(geometricParametersReflec, SWT.SINGLE);
		new Label(geometricParametersReflec, SWT.LEFT).setText("angularFudgeFactor");
		angularFudgeFactor = new Text(geometricParametersReflec, SWT.SINGLE);
		new Label(geometricParametersReflec, SWT.LEFT).setText("savePath");
		savePath = new Text (geometricParametersReflec, SWT.CHECK);
		new Label(geometricParametersReflec, SWT.LEFT).setText("fluxPath");
		fluxPath = new Text (geometricParametersReflec, SWT.CHECK);
		new Label(geometricParametersReflec, SWT.LEFT).setText("xNameRef");
		xNameRef = new Text (geometricParametersReflec, SWT.CHECK);
		
	    paramsReflec.setControl(geometricParametersReflec);
	    
	    xNameRef.addModifyListener(new ModifyListener(){

			@Override
			public void modifyText(ModifyEvent e) {
				geometricParametersUpdate();
			}
	    	
	    });
	    
	    fluxPath.addModifyListener(new ModifyListener(){

			@Override
			public void modifyText(ModifyEvent e) {
				geometricParametersUpdate();
			}
	    	
	    });
	    
	    beamHeight.addModifyListener(new ModifyListener(){

			@Override
			public void modifyText(ModifyEvent e) {
				geometricParametersUpdate();
			}
	    	
	    });
		
	    savePath.addModifyListener(new ModifyListener(){

			@Override
			public void modifyText(ModifyEvent e) {
				geometricParametersUpdate();
			}
	    	
	    });
	 
	    footprint.addModifyListener(new ModifyListener(){

			@Override
			public void modifyText(ModifyEvent e) {
				geometricParametersUpdate();
			}
	    	
	    });
	    
	    angularFudgeFactor.addModifyListener(new ModifyListener(){

			@Override
			public void modifyText(ModifyEvent e) {
				geometricParametersUpdate();
			}
	    	
	    });
	    
		beamCorrection.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				geometricParametersUpdate();
				
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				geometricParametersUpdate();
				
			}
		});
		
		beamInPlane.addModifyListener(new ModifyListener(){

			@Override
			public void modifyText(ModifyEvent e) {
				geometricParametersUpdate();
			}
	    	
	    });
		
		beamOutPlane.addModifyListener(new ModifyListener(){

			@Override
			public void modifyText(ModifyEvent e) {
				geometricParametersUpdate();
			}
	    	
	    });
		
		covar.addModifyListener(new ModifyListener(){

			@Override
			public void modifyText(ModifyEvent e) {
				geometricParametersUpdate();
			}
	    	
	    });
		
		detectorSlits.addModifyListener(new ModifyListener(){

			@Override
			public void modifyText(ModifyEvent e) {
				geometricParametersUpdate();
			}
	    	
	    });
		
		inPlaneSlits.addModifyListener(new ModifyListener(){

			@Override
			public void modifyText(ModifyEvent e) {
				geometricParametersUpdate();
			}
	    	
	    });
		
		inplanePolarisation.addModifyListener(new ModifyListener(){

			@Override
			public void modifyText(ModifyEvent e) {
				geometricParametersUpdate();
			}
	    	
	    });

		outPlaneSlits.addModifyListener(new ModifyListener(){

			@Override
			public void modifyText(ModifyEvent e) {
				geometricParametersUpdate();
			}
	    	
	    });

		outplanePolarisation.addModifyListener(new ModifyListener(){

			@Override
			public void modifyText(ModifyEvent e) {
				geometricParametersUpdate();
			}
	    	
	    });

		
		scalingFactor.addModifyListener(new ModifyListener(){

			@Override
			public void modifyText(ModifyEvent e) {
				geometricParametersUpdate();
			}
	    	
	    });
		
		reflectivityA.addModifyListener(new ModifyListener(){

			@Override
			public void modifyText(ModifyEvent e) {
				geometricParametersUpdate();
			}
	    	
	    });

		sampleSize.addModifyListener(new ModifyListener(){

			@Override
			public void modifyText(ModifyEvent e) {
				geometricParametersUpdate();
			}
	    	
	    });

		normalisationFactor.addModifyListener(new ModifyListener(){

			@Override
			public void modifyText(ModifyEvent e) {
				geometricParametersUpdate();
			}
	    	
	    });

		specular.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				geometricParametersUpdate();
				
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				geometricParametersUpdate();
				
			}
		});
		
		imageName.addModifyListener(new ModifyListener(){

			@Override
			public void modifyText(ModifyEvent e) {
				geometricParametersUpdate();
			}
	    	
	    });
		
		xName.addModifyListener(new ModifyListener(){

			@Override
			public void modifyText(ModifyEvent e) {
				geometricParametersUpdate();
			}
	    	
	    });
		
	}
	
	
	public TabFolder getTabFolder(){
		return folder;
	}
	
	
	public void geometricParametersUpdate() {
			
		ssp.geometricParametersUpdate(xNameRef.getText(),
				  fluxPath.getText(),
				  (Double.parseDouble(beamHeight.getText())),
				  (savePath.getText()),
				  (Double.parseDouble(footprint.getText())),
				  (Double.parseDouble(angularFudgeFactor.getText())),
				  beamCorrection.getSelection(),
				  (Double.parseDouble(beamInPlane.getText())),
				  (Double.parseDouble(beamOutPlane.getText())),
				  (Double.parseDouble(covar.getText())),
				  (Double.parseDouble(detectorSlits.getText())),
				  (Double.parseDouble(inPlaneSlits.getText())),
				  (Double.parseDouble(inplanePolarisation.getText())),
				  (Double.parseDouble(outPlaneSlits.getText())),
				  (Double.parseDouble(outplanePolarisation.getText())),
				  (Double.parseDouble(scalingFactor.getText())),
				  (Double.parseDouble(reflectivityA.getText())),
				  (Double.parseDouble(sampleSize.getText())),
				  (Double.parseDouble(normalisationFactor.getText())),
				  (specular.getSelection()),
				  (imageName.getText()),
				  (xName.getText()));
		
	}
	
}
