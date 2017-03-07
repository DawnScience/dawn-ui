package org.dawnsci.surfacescatter.ui;

import org.dawnsci.surfacescatter.GeometricParametersModel;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
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
	private Text scalingFactor;
	private Text beamHeight;
	private Text footprint;
	private Text angularFudgeFactor;
	private Text savePath;
	private Text fluxPath;
	private TabFolder folder;
	private SurfaceScatterPresenter ssp;
	private GeometricParametersWindows gpw;
	private Group geometricParametersSX;
	
	public GeometricParametersWindows(Composite parent, 
									  int style,
									  SurfaceScatterPresenter ssp){
		
		super(parent, style);
        
        this.ssp = ssp;
        this.gpw =this;
        
        this.createContents();
        
	}
	
	public void createContents() {
		
		folder = new TabFolder(this, SWT.NONE);
		folder.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		
		
		
	    //Tab 1
	    TabItem paramsSXRD = new TabItem(folder, SWT.NONE);
	    paramsSXRD.setText("SXRD Parameters");
	   
		geometricParametersSX = new Group(folder, SWT.NULL);
		GridLayout geometricParametersSXLayout = new GridLayout(2,true);
		GridData geometricParametersSXData = new GridData(GridData.FILL_BOTH);
		geometricParametersSX.setLayout(geometricParametersSXLayout);
		geometricParametersSX.setLayoutData(geometricParametersSXData);
		
		new Label(geometricParametersSX, SWT.LEFT).setText("beamCorrection");
		beamCorrection = new Button(geometricParametersSX, SWT.CHECK);
		beamCorrection.setSelection(false);
		new Label(geometricParametersSX, SWT.LEFT).setText("beamInPlane");
		beamInPlane = new Text(geometricParametersSX, SWT.SINGLE);
		beamInPlane.setText("0.3");
		new Label(geometricParametersSX, SWT.LEFT).setText("beamOutPlane");
		beamOutPlane = new Text(geometricParametersSX, SWT.SINGLE);
		beamOutPlane.setText("0.3");
		new Label(geometricParametersSX, SWT.LEFT).setText("covar");
		covar = new Text(geometricParametersSX, SWT.SINGLE);
		covar.setText("1.0");
		new Label(geometricParametersSX, SWT.LEFT).setText("detectorSlits");
		detectorSlits = new Text(geometricParametersSX, SWT.SINGLE);
		detectorSlits.setText("0.2");
		new Label(geometricParametersSX, SWT.LEFT).setText("inPlaneSlits");
		inPlaneSlits = new Text(geometricParametersSX, SWT.SINGLE);
		inPlaneSlits.setText("0.5");
		new Label(geometricParametersSX, SWT.LEFT).setText("inplanePolarisation");
		inplanePolarisation = new Text(geometricParametersSX, SWT.SINGLE);
		inplanePolarisation.setText("0.0");
		new Label(geometricParametersSX, SWT.LEFT).setText("outPlaneSlits");
		outPlaneSlits = new Text(geometricParametersSX, SWT.SINGLE);
		outPlaneSlits.setText("0.5");
		new Label(geometricParametersSX, SWT.LEFT).setText("outplanePolarisation");
		outplanePolarisation = new Text(geometricParametersSX, SWT.SINGLE);
		outplanePolarisation.setText("1.0");
		new Label(geometricParametersSX, SWT.LEFT).setText("reflectivityA");
		reflectivityA = new Text(geometricParametersSX, SWT.SINGLE);
		reflectivityA.setText("1.0");
		new Label(geometricParametersSX, SWT.LEFT).setText("sampleSize");
		sampleSize = new Text(geometricParametersSX, SWT.SINGLE);
		sampleSize.setText("10.0");
		new Label(geometricParametersSX, SWT.LEFT).setText("normalisationFactor");
		scalingFactor = new Text(geometricParametersSX, SWT.SINGLE);
		scalingFactor.setText("10.0");
		new Label(geometricParametersSX, SWT.LEFT).setText("scalingFactor");
		normalisationFactor = new Text(geometricParametersSX, SWT.SINGLE);
		normalisationFactor.setText("10.0");
		new Label(geometricParametersSX, SWT.LEFT).setText("specular");
		specular = new Button (geometricParametersSX, SWT.CHECK);
		specular.setSelection(false);
		new Label(geometricParametersSX, SWT.LEFT).setText("imageName");
		imageName = new Text(geometricParametersSX, SWT.SINGLE);
		imageName.setText("file_image");

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
		beamHeight.setText("0.06");
		new Label(geometricParametersReflec, SWT.LEFT).setText("footprint");
		footprint = new Text(geometricParametersReflec, SWT.SINGLE);
		footprint.setText("190");
		new Label(geometricParametersReflec, SWT.LEFT).setText("Angular Adjustment");
		angularFudgeFactor = new Text(geometricParametersReflec, SWT.SINGLE);
		angularFudgeFactor.setText("0");
		new Label(geometricParametersReflec, SWT.LEFT).setText("fluxPath");
		fluxPath = new Text (geometricParametersReflec, SWT.CHECK);

	    paramsReflec.setControl(geometricParametersReflec);
	    
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
	}
	
	
	public Button getBeamCorrection() {
		return beamCorrection;
	}

	public void setBeamCorrection(Button beamCorrection) {
		this.beamCorrection = beamCorrection;
	}

	public Text getBeamInPlane() {
		return beamInPlane;
	}

	public void setBeamInPlane(Text beamInPlane) {
		this.beamInPlane = beamInPlane;
	}

	public Text getBeamOutPlane() {
		return beamOutPlane;
	}

	public void setBeamOutPlane(Text beamOutPlane) {
		this.beamOutPlane = beamOutPlane;
	}

	public Text getCovar() {
		return covar;
	}

	public void setCovar(Text covar) {
		this.covar = covar;
	}

	public Text getDetectorSlits() {
		return detectorSlits;
	}

	public void setDetectorSlits(Text detectorSlits) {
		this.detectorSlits = detectorSlits;
	}

	public Text getInPlaneSlits() {
		return inPlaneSlits;
	}

	public void setInPlaneSlits(Text inPlaneSlits) {
		this.inPlaneSlits = inPlaneSlits;
	}

	public Text getInplanePolarisation() {
		return inplanePolarisation;
	}

	public void setInplanePolarisation(Text inplanePolarisation) {
		this.inplanePolarisation = inplanePolarisation;
	}

	public Text getOutPlaneSlits() {
		return outPlaneSlits;
	}

	public void setOutPlaneSlits(Text outPlaneSlits) {
		this.outPlaneSlits = outPlaneSlits;
	}

	public Text getOutplanePolarisation() {
		return outplanePolarisation;
	}

	public void setOutplanePolarisation(Text outplanePolarisation) {
		this.outplanePolarisation = outplanePolarisation;
	}

	public Text getReflectivityA() {
		return reflectivityA;
	}

	public void setReflectivityA(Text reflectivityA) {
		this.reflectivityA = reflectivityA;
	}

	public Text getSampleSize() {
		return sampleSize;
	}

	public void setSampleSize(Text sampleSize) {
		this.sampleSize = sampleSize;
	}

	public Text getNormalisationFactor() {
		return normalisationFactor;
	}

	public void setNormalisationFactor(Text normalisationFactor) {
		this.normalisationFactor = normalisationFactor;
	}

	public Button getSpecular() {
		return specular;
	}

	public void setSpecular(Button specular) {
		this.specular = specular;
	}

	public Text getImageName() {
		return imageName;
	}

	public void setImageName(Text imageName) {
		this.imageName = imageName;
	}
	
	public void setImageName(String imageName) {
		this.imageName.setText(imageName);
	}

	public Text getScalingFactor() {
		return scalingFactor;
	}

	public void setScalingFactor(Text scalingFactor) {
		this.scalingFactor = scalingFactor;
	}

	public Text getBeamHeight() {
		return beamHeight;
	}

	public void setBeamHeight(Text beamHeight) {
		this.beamHeight = beamHeight;
	}

	public Text getFootprint() {
		return footprint;
	}

	public void setFootprint(Text footprint) {
		this.footprint = footprint;
	}

	public Text getAngularFudgeFactor() {
		return angularFudgeFactor;
	}

	public void setAngularFudgeFactor(Text angularFudgeFactor) {
		this.angularFudgeFactor = angularFudgeFactor;
	}

	public Text getSavePath() {
		return savePath;
	}

	public void setSavePath(Text savePath) {
		this.savePath = savePath;
	}

	public Text getFluxPath() {
		return fluxPath;
	}

	public void setFluxPath(Text fluxPath) {
		this.fluxPath = fluxPath;
	}

	public TabFolder getFolder() {
		return folder;
	}

	public void setFolder(TabFolder folder) {
		this.folder = folder;
	}

	public SurfaceScatterPresenter getSsp() {
		return ssp;
	}

	public void setSsp(SurfaceScatterPresenter ssp) {
		this.ssp = ssp;
	}

	public GeometricParametersWindows getGpw() {
		return gpw;
	}

	public void setGpw(GeometricParametersWindows gpw) {
		this.gpw = gpw;
	}
	
	public TabFolder getTabFolder(){
		return folder;
	}
	
	public void setEnabled(boolean enabled){
		
		for (Control o :geometricParametersSX.getChildren()){
			o.setEnabled(enabled);
		}

		for(Control i :folder.getChildren()){
			i.setEnabled(enabled);
		}
		
		folder.setEnabled(enabled);
	}
	
	public void geometricParametersUpdate() {
			
		ssp.geometricParametersUpdate(
				  fluxPath.getText(),
				  (Double.parseDouble(beamHeight.getText())),
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
				  (imageName.getText())
				  );
		
	}
	
	
	public void localGeometricParametersUpdate(GeometricParametersModel gm) {
		
		gm.setFluxPath(fluxPath.getText());
		gm.setBeamHeight( (Double.parseDouble(beamHeight.getText())));
		gm.setFootprint(Double.parseDouble(footprint.getText()));
		gm.setAngularFudgeFactor (Double.parseDouble(angularFudgeFactor.getText()));
		gm.setBeamCorrection(beamCorrection.getSelection());
		gm.setBeamInPlane(Double.parseDouble(beamInPlane.getText()));
		gm.setBeamOutPlane(Double.parseDouble(beamOutPlane.getText()));
		gm.setCovar( (Double.parseDouble(covar.getText())));
		gm.setDetectorSlits((Double.parseDouble(detectorSlits.getText())));
		gm.setInPlaneSlits((Double.parseDouble(inPlaneSlits.getText())));
		gm.setInplanePolarisation(Double.parseDouble(inplanePolarisation.getText()));
		gm.setOutPlaneSlits (Double.parseDouble(outPlaneSlits.getText()));
		gm.setOutplanePolarisation((Double.parseDouble(outplanePolarisation.getText())));
		gm.setScalingFactor((Double.parseDouble(scalingFactor.getText())));
		gm.setReflectivityA( (Double.parseDouble(reflectivityA.getText())));
		gm.setSampleSize( (Double.parseDouble(sampleSize.getText())));
		gm.setNormalisationFactor (Double.parseDouble(sampleSize.getText()));
		gm.setSpecular(specular.getSelection());
		gm.setImageName (imageName.getText());		
		
		
	}
	
}
