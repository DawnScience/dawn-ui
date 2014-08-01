package org.dawnsci.plotting.tools.powderintegration;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import org.dawnsci.common.widgets.decorator.FloatDecorator;
import org.dawnsci.common.widgets.decorator.IntegerDecorator;
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
import org.eclipse.swt.widgets.Text;
import org.mihalis.opal.checkBoxGroup.CheckBoxGroup;

import uk.ac.diamond.scisoft.analysis.diffraction.powder.AbstractPixelIntegration;
import uk.ac.diamond.scisoft.analysis.diffraction.powder.AbstractPixelIntegration1D;
import uk.ac.diamond.scisoft.analysis.diffraction.powder.PixelIntegrationUtils.IntegrationMode;
import uk.ac.diamond.scisoft.analysis.io.IDiffractionMetadata;

public class IntegrationSetupWidget {
	
	PowderIntegrationModel model;
	List<Control> disableFor1D;
	List<Control> disableFor2D;
	int longestAxisInPixels = 0;
	
	public IntegrationSetupWidget(Composite composite, PowderIntegrationModel model) {
		this(composite,model, 1000);
	}
	
	public IntegrationSetupWidget(Composite composite, PowderIntegrationModel model,int longestAxisInPixels) {
		
		disableFor1D = new ArrayList<Control>();
		disableFor2D = new ArrayList<Control>();
		
		this.model = model;
		model.setAzimuthal(true);
		
		this.longestAxisInPixels = longestAxisInPixels;
		
//		if (metadata != null) {
//			int[] shape = new int[]{metadata.getDetector2DProperties().getPy(), metadata.getDetector2DProperties().getPx()};
//			longestAxisInPixels = AbstractPixelIntegration.calculateNumberOfBins(metadata.getDetector2DProperties().getBeamCentreCoords(), shape);
//		}
		model.setNumberOfPrimaryBins(longestAxisInPixels);
		model.setNumberOfSecondaryBins(longestAxisInPixels);
		
		composite.setLayout(new GridLayout(3, false));
		createOptionsGroup(composite, model);
		createRadialRangeGroup(composite, model);
		createAzimuthalRangeGroup(composite, model);
		
		model.addPropertyChangeListener("integrationMode", new PropertyChangeListener() {
			
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if (evt.getNewValue() == IntegrationMode.NONSPLITTING || evt.getNewValue() == IntegrationMode.SPLITTING) enableFor1D(true);
				else enableFor1D(false);
			}
		});
	}
	
	public PowderIntegrationModel getModel(){
		return model;
	}
	
	public void setLongestDistance(int distance) {
		longestAxisInPixels = distance;
	}
	
	public void enableFor1D(boolean for1D){
		for (Control c: disableFor1D) c.setEnabled(!for1D);
		for (Control c: disableFor2D) c.setEnabled(for1D);
	}
	
	private void resetNumberOfBins() {
		model.setNumberOfPrimaryBins(longestAxisInPixels);
		model.setNumberOfSecondaryBins(longestAxisInPixels);
	}
	
	private void createRadialRangeGroup(Composite composite, final PowderIntegrationModel model) {
		
		final CheckBoxGroup radialGroup = new CheckBoxGroup(composite, SWT.NONE);
		radialGroup.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));
		radialGroup.setText("Set Radial Range");
		Composite content = radialGroup.getContent();
		content.setLayout(new GridLayout(2,true));
		Label lbl = new Label(content, SWT.None);
		lbl.setText("Min:");
		lbl.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER,true, false));
		
		final Text startRadial = new Text(content, SWT.SINGLE | SWT.RIGHT);
		
		final FloatDecorator fd =new FloatDecorator(startRadial);
		fd.setMinimum(0);
		lbl = new Label(content, SWT.None);
		lbl.setText("Max:");
		lbl.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER,true, false));
		final Text endRadial = new Text(content, SWT.SINGLE | SWT.RIGHT);
		endRadial.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,true, false));
		
		final FloatDecorator fd2 =new FloatDecorator(endRadial);
		fd2.setMinimum(0);
		
		startRadial.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				if (!fd.isError() && !fd2.isError()) {
					try {
						double[] range = new double[] {Double.parseDouble(startRadial.getText()),
								Double.parseDouble(endRadial.getText())};
						model.setRadialRange(range);
					} catch (NumberFormatException nfe) {
						//do nothing
					}
				}
			}
		});
		
		
		endRadial.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				if (!fd.isError() && !fd2.isError()) {
					try {
						double[] range = new double[] {Double.parseDouble(startRadial.getText()),
								Double.parseDouble(endRadial.getText())};
						model.setRadialRange(range);
					} catch (NumberFormatException nfe) {
						//do nothing
					}
				}
			}
		});
		
		radialGroup.deactivate();
		radialGroup.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (radialGroup.isActivated()) {
					if (!fd.isError() && !fd2.isError()) {
						
						try {
						double[] range = new double[] {Double.parseDouble(startRadial.getText()),
								Double.parseDouble(endRadial.getText())};
						model.setRadialRange(range);
						} catch (NumberFormatException nfe) {
							//do nothing
						}
 					}
				} else {
					model.setRadialRange(null);
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
	}

	private void createAzimuthalRangeGroup(Composite composite, final PowderIntegrationModel model) {

		final CheckBoxGroup radialGroup = new CheckBoxGroup(composite, SWT.NONE);
		radialGroup.setText("Set Azimuthal Range");
		radialGroup.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));
		Composite content = radialGroup.getContent();
		content.setLayout(new GridLayout(2,true));
		
		Label lbl = new Label(content, SWT.None);
		lbl.setText("Min:");
		lbl.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER,true, false));
		final Text startRadial = new Text(content, SWT.SINGLE | SWT.RIGHT);
		final FloatDecorator fd =new FloatDecorator(startRadial);
		fd.setMinimum(-180);
		fd.setMaximum(180);
		startRadial.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,true, false));
		startRadial.setText(String.valueOf(-180));

		lbl = new Label(content, SWT.None);
		lbl.setText("Max:");
		lbl.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER,true, false));
		final Text endRadial = new Text(content, SWT.SINGLE | SWT.RIGHT);
		endRadial.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,true, false));
		endRadial.setText(String.valueOf(180));
		final FloatDecorator fd2 =new FloatDecorator(endRadial);
		fd2.setMinimum(-180);
		fd2.setMaximum(180);
		startRadial.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				if (!fd.isError() && !fd2.isError()) {
					try{
						double[] range = new double[] {Double.parseDouble(startRadial.getText()),
								Double.parseDouble(endRadial.getText())};

						model.setAzimuthalRange(range);
					} catch (NumberFormatException nfe) {
						//do nothing
					}
				}
			}
		});
		
		endRadial.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				if (!fd.isError() && !fd2.isError()) {
					try{
						double[] range = new double[] {Double.parseDouble(startRadial.getText()),
								Double.parseDouble(endRadial.getText())};

						model.setAzimuthalRange(range);
					} catch (NumberFormatException nfe) {
						//do nothing
					}
				}
			}
		});

		radialGroup.deactivate();
		radialGroup.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (radialGroup.isActivated()) {
					if (!fd.isError() && !fd2.isError()) {
						try{
							double[] range = new double[] {Double.parseDouble(startRadial.getText()),
									Double.parseDouble(endRadial.getText())};

							model.setAzimuthalRange(range);
						} catch (NumberFormatException nfe) {
							//do nothing
						}
					}
				} else {
					model.setAzimuthalRange(null);
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				
			}
		});
		
	}
	
	private void createOptionsGroup(Composite composite, final PowderIntegrationModel model) {
		
		Group group = new Group(composite, SWT.SHADOW_IN);
		group.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		group.setText("Integration Options");
		group.setLayout(new GridLayout(2, true));
		Button azbtn = new Button(group, SWT.RADIO);
		azbtn.setText("Azimuthal Integration");
		azbtn.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false,2,1));
		azbtn.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				model.setAzimuthal(true);
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		azbtn.setSelection(true);
		
		Button radbtn = new Button(group, SWT.RADIO);
		radbtn.setText("Radial Integration");
		radbtn.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false,2,1));
		radbtn.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				model.setAzimuthal(false);
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		
		disableFor2D.add(azbtn);
		disableFor2D.add(radbtn);
		
		Label lbl = new Label(group, SWT.None);
		lbl.setText("Number of Bins X:");
		lbl.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER,false, false));
		final Text binsTxt = new Text(group, SWT.SINGLE | SWT.RIGHT);
		binsTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,true, false));
		binsTxt.setText(String.valueOf(model.getNumberOfPrimaryBins()));
		final IntegerDecorator id = new IntegerDecorator(binsTxt);
		id.setMinimum(2);
		binsTxt.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				if (!id.isError()) {
					model.setNumberOfPrimaryBins(Integer.parseInt(binsTxt.getText()));
				}
			}
		});
		
		lbl = new Label(group, SWT.None);
		lbl.setText("Number of Bins Y:");
		lbl.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER,false, false));
		disableFor1D.add(lbl);
		final Text secondaryTxt = new Text(group, SWT.SINGLE | SWT.RIGHT);
		disableFor1D.add(secondaryTxt);
		secondaryTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,true, false));
		secondaryTxt.setText(String.valueOf(model.getNumberOfPrimaryBins()));
		final IntegerDecorator id2 = new IntegerDecorator(secondaryTxt);
		id2.setMinimum(2);
		secondaryTxt.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				if (!id2.isError()) {
					model.setNumberOfSecondaryBins(Integer.parseInt(secondaryTxt.getText()));
				}
			}
		});
		
		Button reset = new Button(group, SWT.NONE);
		reset.setText("Reset");
		reset.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				binsTxt.setText(String.valueOf(longestAxisInPixels));
				secondaryTxt.setText(String.valueOf(longestAxisInPixels));
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
	}
}
