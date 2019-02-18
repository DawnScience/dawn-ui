/*-
 * Copyright (c) 2017 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.plotting.tools.powderlines;

import java.text.DecimalFormat;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * A class containing the specifics of a {@link PowderLineTool} with equation of state information.
 * @author Timothy Spain, timothy.spain@diamond.ac.uk
 *
 */
public class EoSLineTool extends PowderLineTool {

	/**
	 * The Composite that will display the model details when equation
	 * of state information is present.
	 * @author Timothy Spain, timothy.spain@diamond.ac.uk
	 *
	 */
	static class EosDetailsComposite extends Composite {
		static final String MODULUSSYMBOL = "B"; // Could also be K
		static final String MODULUSSTRING = MODULUSSYMBOL+"₀";
		static final String MODULUSDERIVATIVESTRING = MODULUSSYMBOL + "₀′";
		String pressureUnits = "Pa";
		double pressureMultiplier = 1;
		Text k0;
		Text k0prime;
		Text v;
		Text v0;
		Text ll0;
		Text p;
		Text v0exp;
		EoSLinesModel model;
		PowderLineTool tool;
		static final DecimalFormat ll0Format = new DecimalFormat("#.###");
		
		/**
		 * Constructor
		 * @param parent
		 * 				parent Composite
		 * @param style
		 * 				style to apply to the new Composite
		 */
		public EosDetailsComposite(Composite parent, int style) {
			super(parent, style);
			model = null;
		}
		
		/**
		 * Sets the scale of the pressure
		 * @param magnitude
		 * 					power of ten magnitude to scale the
		 * 					pressure by (for GPa, magnitude = 9).
		 */
		public void setPressureMultiplierMagnitude(int magnitude) {
			pressureMultiplier = Math.pow(10., magnitude);
			// "terapascals" is probably sufficient
			String[] prefices = new String[] {"", "da", "h", "k", "10⁴ ", "10⁵ ", "M", "10⁷ ", "10⁸ ", "G", "10¹⁰ ", "10¹¹ ", "T"};
			
			if (magnitude < 0 || magnitude > 12) {
				// not a valid multiplier
				System.err.println("10^" + magnitude + " is not a valid SI prefix scale.");
			} else {
				pressureUnits = prefices[magnitude]+"Pa";
			}
			
			redraw();
		}

		/**
		 * Sets the modulus.
		 * @param modulus
		 * 				value in Pa.
		 */
		public void setModulus(double modulus) {
			this.k0.setText(Double.toString(modulus/pressureMultiplier));
		}

		public void setModulusDerivative(double modulusDeriviative) {
			this.k0prime.setText(Double.toString(modulusDeriviative));
		}
		
		/**
		 * Sets the pressure at which to calculate the EoS.
		 * @param modulus
		 * 				value in Pa.
		 */
		public void setPressure(double pressure) {
			if (!Double.toString(pressure/pressureMultiplier).equals(p.getText())) {
				p.setText(Double.toString(pressure/pressureMultiplier));
			}
		}

		/**
		 * Sets the model that this Composite represents for callback purposes
		 * @param model
		 * 				model to call back to. 
		 */
		public void setModel(EoSLinesModel model) {
			this.model = model;
		}
		
		/**
		 * Sets the tool that this Composite represents for callback purposes
		 * @param tool
		 * 				tool to call back to. 
		 */
		public void setTool(PowderLineTool tool) {
			this.tool = tool;
		}
		
		@Override
		public void redraw() {
			if (k0 != null) k0.dispose();
			if (k0prime != null) k0prime.dispose();
			if (ll0 != null) ll0.dispose();
			if (p != null) p.dispose();
			if (v0exp != null) v0exp.dispose();
			
			
			GridLayout layout = new GridLayout(11, false);
			this.setLayout(layout);
			
			// Modulus
			Label modulusLabel = new Label(this, SWT.RIGHT);
			modulusLabel.setText(MODULUSSTRING);
			modulusLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));
			k0 = new Text(this, SWT.SINGLE | SWT.LEFT);
			k0.setLayoutData(new GridData(SWT.BEGINNING, SWT.TOP, false, false));
			k0.setEditable(false);
			Label modulusUnits = new Label(this, SWT.LEFT);
			modulusUnits.setText(pressureUnits);
			modulusUnits.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
			
			// Empty text for a spacer
			Text spacer = new Text(this, SWT.SINGLE);
			spacer.setEditable(false);
			spacer.setLayoutData(new GridData(SWT.BEGINNING, SWT.TOP, true, false));
			
			// Modulus derivative
			Label derivLabel = new Label(this, SWT.RIGHT);
			derivLabel.setText(MODULUSDERIVATIVESTRING);
			derivLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));
			
			k0prime = new Text(this, SWT.SINGLE | SWT.LEFT);
			k0prime.setLayoutData(new GridData(SWT.BEGINNING, SWT.TOP, false, false));
			k0prime.setEditable(false);

			Label derivUnits = new Label(this, SWT.LEFT);
			derivUnits.setText("");
			derivUnits.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
			
			// Empty text for a spacer
			spacer = new Text(this, SWT.SINGLE);
			spacer.setEditable(false);
			spacer.setLayoutData(new GridData(SWT.BEGINNING, SWT.TOP, true, false));
			
			Label ll0Label = new Label(this, SWT.LEFT);
			ll0Label.setText("l/l₀");
			ll0Label.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
			
			ll0 = new Text(this, SWT.SINGLE | SWT.LEFT);
			ll0.setLayoutData(new GridData(SWT.BEGINNING, SWT.TOP, false, false));
			ll0.setEditable(false);
			
			setLL0();
			
			Label ll0Units = new Label(this, SWT.LEFT);
			ll0Units.setText("");
			ll0Units.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));

			// New line

			// pressure
			Label pressureLabel = new Label(this, SWT.RIGHT);
			pressureLabel.setText("Pressure");
			pressureLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));
			p = new Text(this, SWT.BORDER);
			p.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
			p.addModifyListener(new ModifyListener() {

				@Override
				public void modifyText(ModifyEvent e) {
					double pressure;
					try {
						pressure = Double.parseDouble(p.getText())* pressureMultiplier;
					} catch (NumberFormatException nfe) {
						pressure = 1e-5;
					}
					tool.setPressure(pressure);
					setLL0();
				}
				
			});
			Label pressureUnitsLabel = new Label(this, SWT.LEFT);
			pressureUnitsLabel.setText(pressureUnits);
			pressureUnitsLabel.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
			
			// Empty text for a spacer
			spacer = new Text(this, SWT.SINGLE);
			spacer.setEditable(false);
			spacer.setLayoutData(new GridData(SWT.BEGINNING, SWT.TOP, true, false, 5, 1));

			Label v0expLabel = new Label(this, SWT.RIGHT);
			v0expLabel.setText("V₀(exp)/V₀");
			v0expLabel.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
			
			v0exp = new Text(this, SWT.BORDER);
			v0exp.setLayoutData(new GridData(SWT.BEGINNING, SWT.TOP, false, false));
			v0exp.setText(Double.toString(model.getVexpV0()));
			v0exp.addModifyListener(new ModifyListener() {

				@Override
				public void modifyText(ModifyEvent e) {
					double v0expValue;
					try {
						v0expValue = Double.parseDouble(v0exp.getText());
					} catch (NumberFormatException nfe) {
						v0expValue = 1.0;
					}
					model.setVexpV0(v0expValue);
					setLL0();
					tool.refresh(true);
				}
				
			});
			
			Label v0expUnits = new Label(this, SWT.LEFT);
			v0expUnits.setText("");
			v0expUnits.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));
			
			
			super.redraw();

		}

		// Sets the length value to display.
		private void setLL0() {
			ll0.setText(ll0Format.format(model.getLengthRatio()));
			ll0.setSize(ll0.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		}
	}
}
