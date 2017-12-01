package org.dawnsci.surfacescatter.ui;

import org.dawnsci.surfacescatter.GeometricParametersModel;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;

public class GeometricParametersWindows extends Composite {

	private Button beamCorrection;
	private Button radio;
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
	private Text energy;
	private TabFolder folder;
	private SurfaceScatterPresenter ssp;
	private GeometricParametersWindows gpw;
	private Composite geometricParametersSX;
	private Combo theta;
	private Combo selectedOption;
	private boolean updateOn = true;
	private boolean useNegativeQ = false;
	private Button useNegativeQButton;

	public GeometricParametersWindows(Composite parent, int style, SurfaceScatterPresenter ssp) {

		super(parent, style);

		this.ssp = ssp;
		this.gpw = this;

		this.createContents();

	}
	
//	public GeometricParametersWindows(Composite parent, int style, SurfaceScatterPresenter ssp, boolean in) {
//
//		super(parent, style);
//
//		this.ssp = ssp;
//		this.gpw = this;
//
//		this.createContents(true);
//
//	}

	public void createContents() {

		folder = new TabFolder(this, SWT.NONE);
		folder.setLayoutData(new GridData(GridData.FILL_BOTH));

		// Tab 1
		TabItem paramsSXRD = new TabItem(folder, SWT.NONE);
		paramsSXRD.setText("SXRD Parameters");

		ScrolledComposite sc1 = new ScrolledComposite(folder, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);

		geometricParametersSX = new Composite(sc1, SWT.NULL);
		GridLayout geometricParametersSXLayout = new GridLayout(2, true);
		GridData geometricParametersSXData = new GridData(GridData.FILL_BOTH);
		geometricParametersSX.setLayout(geometricParametersSXLayout);
		geometricParametersSX.setLayoutData(geometricParametersSXData);

		Label beamCorrectionLabel = new Label(geometricParametersSX, SWT.LEFT);
		beamCorrectionLabel.setText("beamCorrection");

		beamCorrection = new Button(geometricParametersSX, SWT.CHECK);
		beamCorrection.setSelection(false);

		Label beamInPlaneLabel = new Label(geometricParametersSX, SWT.LEFT);
		beamInPlaneLabel.setText("beamInPlane");

		beamInPlane = new Text(geometricParametersSX, SWT.SINGLE);
		beamInPlane.setText("0.3");
		beamInPlane.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label beamOutPlaneLabel = new Label(geometricParametersSX, SWT.LEFT);
		beamOutPlaneLabel.setText("beamOutPlane");

		beamOutPlane = new Text(geometricParametersSX, SWT.SINGLE);
		beamOutPlane.setText("0.3");
		beamOutPlane.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		new Label(geometricParametersSX, SWT.LEFT).setText("covar");
		covar = new Text(geometricParametersSX, SWT.SINGLE);
		covar.setText("1.0");
		covar.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		new Label(geometricParametersSX, SWT.LEFT).setText("detectorSlits");
		detectorSlits = new Text(geometricParametersSX, SWT.SINGLE);
		detectorSlits.setText("0.2");
		detectorSlits.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		new Label(geometricParametersSX, SWT.LEFT).setText("inPlaneSlits");
		inPlaneSlits = new Text(geometricParametersSX, SWT.SINGLE);
		inPlaneSlits.setText("0.5");
		inPlaneSlits.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		new Label(geometricParametersSX, SWT.LEFT).setText("inplanePolarisation");
		inplanePolarisation = new Text(geometricParametersSX, SWT.SINGLE);
		inplanePolarisation.setText("0.0");
		inplanePolarisation.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		new Label(geometricParametersSX, SWT.LEFT).setText("outPlaneSlits");
		outPlaneSlits = new Text(geometricParametersSX, SWT.SINGLE);
		outPlaneSlits.setText("0.5");
		outPlaneSlits.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		new Label(geometricParametersSX, SWT.LEFT).setText("outplanePolarisation");
		outplanePolarisation = new Text(geometricParametersSX, SWT.SINGLE);
		outplanePolarisation.setText("1.0");
		outplanePolarisation.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		new Label(geometricParametersSX, SWT.LEFT).setText("reflectivityA");
		reflectivityA = new Text(geometricParametersSX, SWT.SINGLE);
		reflectivityA.setText("1.0");
		reflectivityA.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		new Label(geometricParametersSX, SWT.LEFT).setText("sampleSize");
		sampleSize = new Text(geometricParametersSX, SWT.SINGLE);
		sampleSize.setText("10.0");
		sampleSize.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		new Label(geometricParametersSX, SWT.LEFT).setText("normalisationFactor");
		scalingFactor = new Text(geometricParametersSX, SWT.SINGLE);
		scalingFactor.setText("10.0");
		scalingFactor.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		new Label(geometricParametersSX, SWT.LEFT).setText("scalingFactor");
		normalisationFactor = new Text(geometricParametersSX, SWT.SINGLE);
		normalisationFactor.setText("10.0");
		normalisationFactor.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		new Label(geometricParametersSX, SWT.LEFT).setText("specular");
		specular = new Button(geometricParametersSX, SWT.CHECK);
		specular.setSelection(false);
		new Label(geometricParametersSX, SWT.LEFT).setText("imageName");
		imageName = new Text(geometricParametersSX, SWT.SINGLE);
		imageName.setText("file_image");
		imageName.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		sc1.setContent(geometricParametersSX);
		//
		sc1.setExpandHorizontal(true);
		sc1.setExpandVertical(true);

		sc1.setMinSize(geometricParametersSX.computeSize(geometricParametersSX.getSize().x, SWT.DEFAULT));

		// sc1.setAlwaysShowScrollBars(true);
		sc1.pack();

		paramsSXRD.setControl(sc1);

		// Tab 2
		TabItem paramsReflecTab = new TabItem(folder, SWT.NONE);
		paramsReflecTab.setText("Reflectivity Parameters");

		ScrolledComposite sc2 = new ScrolledComposite(folder, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);

		Composite geometricParametersReflec = new Composite(sc2, SWT.NULL);
		GridLayout geometricParametersLayoutReflec = new GridLayout(2, true);
		GridData geometricParametersDataReflec = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		geometricParametersReflec.setLayout(geometricParametersLayoutReflec);
		geometricParametersReflec.setLayoutData(geometricParametersDataReflec);

		new Label(geometricParametersReflec, SWT.LEFT).setText("Beam Height /mm");
		beamHeight = new Text(geometricParametersReflec, SWT.SINGLE);
		beamHeight.setText("0.06");
		beamHeight.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		new Label(geometricParametersReflec, SWT.LEFT).setText("Footprint /mm");
		footprint = new Text(geometricParametersReflec, SWT.SINGLE);
		footprint.setText("190");
		footprint.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		new Label(geometricParametersReflec, SWT.LEFT).setText("Angular Adjustment");
		angularFudgeFactor = new Text(geometricParametersReflec, SWT.SINGLE);
		angularFudgeFactor.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		angularFudgeFactor.setText("0");

		new Label(geometricParametersReflec, SWT.LEFT).setText("Use Negative q?");
		useNegativeQButton = new Button(geometricParametersReflec, SWT.CHECK);
		useNegativeQButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		useNegativeQButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				GeometricParametersWindows.this.useNegativeQ = !GeometricParametersWindows.this.useNegativeQ;
			}

		});

		beamHeight.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				geometricParametersUpdate();
			}

		});

		footprint.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				geometricParametersUpdate();
			}

		});

		angularFudgeFactor.addModifyListener(new ModifyListener() {

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

		beamInPlane.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				geometricParametersUpdate();
			}

		});

		beamOutPlane.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				geometricParametersUpdate();
			}

		});

		covar.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				geometricParametersUpdate();
			}

		});

		detectorSlits.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				geometricParametersUpdate();
			}

		});

		inPlaneSlits.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				geometricParametersUpdate();
			}

		});

		inplanePolarisation.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				geometricParametersUpdate();
			}

		});

		outPlaneSlits.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				geometricParametersUpdate();
			}

		});

		outplanePolarisation.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				geometricParametersUpdate();
			}

		});

		scalingFactor.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				geometricParametersUpdate();
			}

		});

		reflectivityA.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				geometricParametersUpdate();
			}

		});

		sampleSize.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				geometricParametersUpdate();
			}

		});

		normalisationFactor.addModifyListener(new ModifyListener() {

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

		imageName.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				geometricParametersUpdate();
			}

		});

		InputTileGenerator tile0 = new InputTileGenerator("Compute q", geometricParametersReflec, true);
		radio = tile0.getRadio();

		InputTileGenerator tile1 = new InputTileGenerator("Beam Energy / KeV:", "12.50", geometricParametersReflec);

		energy = tile1.getText();

		String[] thetas = new String[] { "Theta", "2*Theta" };

		InputTileGenerator tile2 = new InputTileGenerator("Angle:", thetas, geometricParametersReflec);
		theta = tile2.getCombo();

		InputTileGenerator tile3 = new InputTileGenerator("Coded Parameter:", ssp.getOptions(),
				geometricParametersReflec);

		selectedOption = tile3.getCombo();

		for (Control l : tile1.getGroup().getChildren()) {
			l.setEnabled(false);
		}

		for (Control l : tile2.getGroup().getChildren()) {
			l.setEnabled(false);
		}

		for (Control l : tile3.getGroup().getChildren()) {
			l.setEnabled(false);
		}

		radio.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				for (Control l : tile1.getGroup().getChildren()) {
					l.setEnabled(radio.getSelection());
				}

				for (Control l : tile2.getGroup().getChildren()) {
					l.setEnabled(radio.getSelection());
				}

				for (Control l : tile3.getGroup().getChildren()) {
					l.setEnabled(radio.getSelection());
				}

				ssp.setqConvert(radio.getSelection());
			}
		});

		sc2.setContent(geometricParametersReflec);

		sc2.setExpandHorizontal(true);
		sc2.setExpandVertical(true);

		paramsReflecTab.setControl(sc2);

		sc2.setMinSize(geometricParametersReflec.computeSize(geometricParametersReflec.getSize().x, SWT.DEFAULT));

		sc2.pack();

		paramsSXRD.setControl(sc1);

		this.pack();
	}

	public Button getRadio() {
		return radio;
	}

	public void setRadio(Button radio) {
		this.radio = radio;
	}

	public Combo getTheta() {
		return theta;
	}

	public void setTheta(Combo theta) {
		this.theta = theta;
	}

	public Combo getSelectedOption() {
		return selectedOption;
	}

	public void setSelectedOption(Combo selectedOption) {
		this.selectedOption = selectedOption;
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
		updateDisplayFromGm(ssp.getGm());
	}

	public GeometricParametersWindows getGpw() {
		return gpw;
	}

	public void setGpw(GeometricParametersWindows gpw) {
		this.gpw = gpw;
	}

	public TabFolder getTabFolder() {
		return folder;
	}

	public void setEnabled(boolean enabled) {

		for (Control o : geometricParametersSX.getChildren()) {
			o.setEnabled(enabled);
		}

		for (Control i : folder.getChildren()) {
			i.setEnabled(enabled);
		}

		folder.setEnabled(enabled);
	}

	public void geometricParametersUpdate() {
		if (updateOn) {
			ssp.geometricParametersUpdate((Double.parseDouble(beamHeight.getText())),
					(Double.parseDouble(footprint.getText())), (Double.parseDouble(angularFudgeFactor.getText())),
					beamCorrection.getSelection(), (Double.parseDouble(beamInPlane.getText())),
					(Double.parseDouble(beamOutPlane.getText())), (Double.parseDouble(covar.getText())),
					(Double.parseDouble(detectorSlits.getText())), (Double.parseDouble(inPlaneSlits.getText())),
					(Double.parseDouble(inplanePolarisation.getText())), (Double.parseDouble(outPlaneSlits.getText())),
					(Double.parseDouble(outplanePolarisation.getText())), (Double.parseDouble(scalingFactor.getText())),
					(Double.parseDouble(reflectivityA.getText())), (Double.parseDouble(sampleSize.getText())),
					(Double.parseDouble(normalisationFactor.getText())), (Double.parseDouble(energy.getText())),
					(specular.getSelection()), (imageName.getText()), selectedOption.getText(),
					useNegativeQButton.getSelection());
		}
	}

	public void getGeometricParameters() {

		ssp.geometricParametersUpdate((Double.parseDouble(beamHeight.getText())),
				(Double.parseDouble(footprint.getText())), (Double.parseDouble(angularFudgeFactor.getText())),
				beamCorrection.getSelection(), (Double.parseDouble(beamInPlane.getText())),
				(Double.parseDouble(beamOutPlane.getText())), (Double.parseDouble(covar.getText())),
				(Double.parseDouble(detectorSlits.getText())), (Double.parseDouble(inPlaneSlits.getText())),
				(Double.parseDouble(inplanePolarisation.getText())), (Double.parseDouble(outPlaneSlits.getText())),
				(Double.parseDouble(outplanePolarisation.getText())), (Double.parseDouble(scalingFactor.getText())),
				(Double.parseDouble(reflectivityA.getText())), (Double.parseDouble(sampleSize.getText())),
				(Double.parseDouble(normalisationFactor.getText())), (Double.parseDouble(energy.getText())),
				(specular.getSelection()), (imageName.getText()), selectedOption.getText(),
				useNegativeQButton.getSelection());

	}

	public void localGeometricParametersUpdate(GeometricParametersModel gm) {

		gm.setBeamHeight((Double.parseDouble(beamHeight.getText())));
		gm.setFootprint(Double.parseDouble(footprint.getText()));
		gm.setAngularFudgeFactor(Double.parseDouble(angularFudgeFactor.getText()));
		gm.setBeamCorrection(beamCorrection.getSelection());
		gm.setBeamInPlane(Double.parseDouble(beamInPlane.getText()));
		gm.setBeamOutPlane(Double.parseDouble(beamOutPlane.getText()));
		gm.setCovar((Double.parseDouble(covar.getText())));
		gm.setDetectorSlits((Double.parseDouble(detectorSlits.getText())));
		gm.setInPlaneSlits((Double.parseDouble(inPlaneSlits.getText())));
		gm.setInplanePolarisation(Double.parseDouble(inplanePolarisation.getText()));
		gm.setOutPlaneSlits(Double.parseDouble(outPlaneSlits.getText()));
		gm.setOutplanePolarisation((Double.parseDouble(outplanePolarisation.getText())));
		gm.setScalingFactor((Double.parseDouble(scalingFactor.getText())));
		gm.setReflectivityA((Double.parseDouble(reflectivityA.getText())));
		gm.setSampleSize((Double.parseDouble(sampleSize.getText())));
		gm.setNormalisationFactor(Double.parseDouble(sampleSize.getText()));
		gm.setSpecular(specular.getSelection());
		gm.setImageName(imageName.getText());
		gm.setUseNegativeQ(useNegativeQButton.getSelection());

	}

	public void updateDisplayFromGm(GeometricParametersModel gm) {

		beamHeight.setText(String.valueOf(gm.getBeamHeight()));
		angularFudgeFactor.setText(String.valueOf(gm.getAngularFudgeFactor()));
		footprint.setText(String.valueOf(gm.getFootprint()));
		beamCorrection.setSelection(gm.getBeamCorrection());
		beamInPlane.setText(String.valueOf(gm.getBeamInPlane()));
		beamOutPlane.setText(String.valueOf(gm.getBeamOutPlane()));
		covar.setText(String.valueOf(gm.getCovar()));
		detectorSlits.setText(String.valueOf(gm.getDetectorSlits()));
		inPlaneSlits.setText(String.valueOf(gm.getInPlaneSlits()));
		inplanePolarisation.setText(String.valueOf(gm.getInplanePolarisation()));
		outplanePolarisation.setText(String.valueOf(gm.getOutplanePolarisation()));
		scalingFactor.setText(String.valueOf(gm.getScalingFactor()));
		reflectivityA.setText(String.valueOf(gm.getReflectivityA()));
		sampleSize.setText(String.valueOf(gm.getSampleSize()));
		normalisationFactor.setText(String.valueOf(gm.getNormalisationFactor()));
		specular.setSelection(gm.getSpecular());
		imageName.setText(gm.getImageName());
		selectedOption.setText(gm.getxNameRef());
		energy.setText(String.valueOf(gm.getEnergy()));
		theta.select(gm.getTheta());
		useNegativeQButton.setSelection(gm.getUseNegativeQ());

	}

	public Text getEnergy() {
		return energy;
	}

	public void setEnergy(Text energy) {
		this.energy = energy;
	}

	public void setUpdateOn(boolean b) {
		this.updateOn = b;
	}

	public boolean isUseNegativeQ() {
		return useNegativeQ;
	}

	public void setUseNegativeQ(boolean useNegativeQ) {
		this.useNegativeQ = useNegativeQ;
	}

}
