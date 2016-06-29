package org.dawnsci.common.widgets.periodictable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.graphics.Font;

public class PeriodicTableComposite extends Composite {

	class PeriodicTableButton {

		private final int Z;
		private final String element;
		private final Button button;
		
		public PeriodicTableButton(Composite parent, int Z) {
			button = new Button(parent, SWT.PUSH | SWT.CENTER);
			this.Z = Z;
			element = MendelArray[Z];
			button.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					fireListeners(PeriodicTableButton.this.Z, element, button);
				}
			});
			// create a bigger font
			FontDescriptor descriptor = FontDescriptor.createFrom(button.getFont()).setHeight(14);
			Font bigFont = descriptor.createFont(button.getDisplay());
			button.setFont(bigFont);
			button.setText(element);
		}
		
		public int getZ() {
			return Z;
		}
		
		public String getElement() {
			return element;
		}
		
		public Button getButton() {
			return button;
		}
		
		public void setLayoutData(GridData gridData) {
			button.setLayoutData(gridData);
		}
	}
	
	// shamelessly copied from xraylib as this allows us to use
	// SymbolToAtomicNumber and AtomicNumberToSymbol without pulling
	// in the entire jar
	static final String[] MendelArray = { "",
		"H", "He", "Li", "Be", "B", "C", "N", "O", "F", "Ne",
	    "Na", "Mg", "Al", "Si", "P", "S", "Cl", "Ar", "K", "Ca",
	    "Sc", "Ti", "V", "Cr", "Mn", "Fe", "Co", "Ni", "Cu", "Zn",
	    "Ga", "Ge", "As", "Se", "Br", "Kr", "Rb", "Sr", "Y", "Zr",
	    "Nb", "Mo", "Tc", "Ru", "Rh", "Pd", "Ag", "Cd", "In", "Sn",
	    "Sb", "Te", "I", "Xe", "Cs", "Ba", "La", "Ce", "Pr", "Nd",
	    "Pm", "Sm", "Eu", "Gd", "Tb", "Dy", "Ho", "Er", "Tm", "Yb",
	    "Lu", "Hf", "Ta", "W", "Re", "Os", "Ir", "Pt", "Au", "Hg",
	    "Tl", "Pb", "Bi", "Po", "At", "Rn", "Fr", "Ra", "Ac", "Th",
	    "Pa", "U", "Np", "Pu", "Am", "Cm", "Bk", "Cf", "Es", "Fm",
	    "Md", "No", "Lr", "Rf", "Db", "Sg", "Bh"
	};
	
	private final PeriodicTableButton[] periodicTableButtons = new PeriodicTableButton[MendelArray.length];
	private final HashSet<IPeriodicTableButtonPressedListener> listeners = new HashSet<>();
	
	public PeriodicTableComposite(Composite parent) {
		super(parent, SWT.NONE);
	
		//start constructing the grid...
		GridLayout gridLayout = new GridLayout(18, true);
		this.setLayout(gridLayout);
		this.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
	
		// since the buttons are added on a row-by-row basis,
		// we cannot add all elements with a loop from 1 to 102
		// since the lanthanide and actinide series cause a discontinuity in the table
		List<Integer> allZ = new ArrayList<>();
		for (int Z = 1 ; Z <= 56 ; Z++)
			allZ.add(Z);
		//Lanthanides break -> jump to Lu
		for (int Z = 71 ; Z <= 88 ; Z++)
			allZ.add(Z);
		//Actinides break -> jump to Lr
		for (int Z = 103 ; Z <= 107 ; Z++)
			allZ.add(Z);
		//Lanthanides
		for (int Z = 57 ; Z <= 70 ; Z++)
			allZ.add(Z);
		//Actinides
		for (int Z = 89 ; Z <= 102 ; Z++)
			allZ.add(Z);
		
		for (int Z : allZ) {
			final PeriodicTableButton button = new PeriodicTableButton(this, Z);
			periodicTableButtons[Z] = button;
			int horizontalAlignment = SWT.FILL;
			int verticalAlignment = SWT.FILL;
			boolean grabExcessHorizontalSpace = true;
			boolean grabExcessVerticalSpace = true;
			int horizontalSpan = 1;
			int verticalSpan = 1;
			
			button.setLayoutData(
					new GridData(
							horizontalAlignment,
							verticalAlignment,
							grabExcessHorizontalSpace,
							grabExcessVerticalSpace,
							horizontalSpan,
							verticalSpan
							)
					);

			switch (Z) {
			case 1:
				new Label(this, SWT.NULL).setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 16, 1));
				break;
			case 4:
			case 12:
				new Label(this, SWT.NULL).setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 10, 1));
				break;
			case 107:
				new Label(this, SWT.NULL).setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 11, 1));
				new Label(this, SWT.NULL).setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 18, 1));
				new Label(this, SWT.NULL).setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
				break;
			case 70:
				new Label(this, SWT.NULL).setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
				new Label(this, SWT.NULL).setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
				break;
			default:
				break;
			}
			
		}
	}
	
	public Button getButton(int Z) {
		if (Z < 1)
			throw new ArrayIndexOutOfBoundsException();
		return periodicTableButtons[Z].getButton();
	}
	
	public Button getButton(String element) {
		int Z = SymbolToAtomicNumber(element);
		if (Z < 1)
			throw new ArrayIndexOutOfBoundsException();
		return periodicTableButtons[Z].getButton();
	}
	
	public void addPeriodicTableButtonPressedListener(IPeriodicTableButtonPressedListener listener){
		listeners.add(listener);
	}
	
	public void removePeriodicTableButtonPressedListener(IPeriodicTableButtonPressedListener listener){
		listeners.remove(listener);
	}
	
	private void fireListeners(int Z, String element, Button button) {
		PeriodicTableButtonPressedEvent event = new PeriodicTableButtonPressedEvent(this, Z, element, button);
		for (IPeriodicTableButtonPressedListener listener : listeners) listener.buttonPressed(event);
	}
	
	protected static String AtomicNumberToSymbol(int Z) {
	    if (Z < 1 || Z > MendelArray.length) {
	    	return null;
	    }
	    return MendelArray[Z];
	}

	protected static int SymbolToAtomicNumber(String symbol) {
	    for (int i = 1 ; i < MendelArray.length ; i++) {
	    	if (symbol.equals(MendelArray[i])) {
	    		return i;
	    	}
	    }
	    return 0;
	 }
}
