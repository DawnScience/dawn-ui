package org.dawnsci.common.widgets.mx;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Widget;

/**
 * Widget designed for choosing a space group.
 * 
 * Widget does not extend Group, rather uses delegation 
 * 
 * @author Matthew Gerring
 *
 */
public class SpaceGroup extends Widget {
	
	/**
	 * This algorithm was done by someone whom had not used Maps before.
	 * However because it works well we have no reason to update it.
	 */
	private final String primitiveTriclinic = "primitive triclinic";
	private final String primitiveMonoclinic = "primitive monoclinic";
	private final String centeredMonoclinic = "centered monoclinic";
	private final String primitiveOrthorhombic = "primitive orthorhombic";
	private final String ccenteredOrthorhombic = "C centered orthorhombic";
	private final String icenteredOrthorhombic = "I centered orthorhombic";
	private final String fcenteredOrthorhombic = "F centered orthorhombic";
	private final String primitiveTetragonal = "primitive tetragonal";
	private final String icenteredTetragonal = "I centered tetragonal";
	private final String primitiveTrigonal = "primitive trigonal";
	private final String primitiveHexagonal = "primitive hexagonal";
	private final String primitiveRhombohedral = "primitive rhombohedral";
	private final String primitiveCubic = "primitive cubic";
	private final String icenteredCubic = "I centered cubic";
	private final String fcenteredCubic = "F centered cubic";
	
	private String [] lattices = {primitiveTriclinic, primitiveMonoclinic, centeredMonoclinic, primitiveOrthorhombic, 
			icenteredOrthorhombic, ccenteredOrthorhombic, fcenteredOrthorhombic, primitiveTetragonal, icenteredTetragonal,
			primitiveTrigonal, primitiveHexagonal, primitiveRhombohedral, primitiveCubic, icenteredCubic, fcenteredCubic};

	/**
	 * This algorithm was done by someone whom had not used Maps before.
	 * However because it works well we have no reason to update it.
	 */
	private final String[] primitiveTriclinicSG = {"P1"};
	private final String[] primitiveMonoclinicSG = {"P2", "P21"};
	private final String[] centeredMonoclinicSG = {"C2"};
	private final String[] primitiveOrthorhombicSG = {"P222", "P2221", "P21212", "P212121"};
	private final String[] ccenteredOrthorhombicSG = {"C222", "C2221"};
	private final String[] icenteredOrthorhombicSG = {"I222", "I212121"};
	private final String[] fcenteredOrthorhombicSG = {"F222"};
	private final String[] primitiveTetragonalSG = {"P4", "P41", "P42", "P43", "P422", "P4212", "P4122", "P41212", "P4222", "P42212", 
			"P4322", "P43212"};
	private final String[] icenteredTetragonalSG = {"I4", "I41", "I422", "I4122"};
	private final String[] primitiveTrigonalSG = {"P3", "P31", "P32", "R3", "P312", "P321", "P3112", "P3121", "P3212", "P3221"};
	private final String[] primitiveHexagonalSG = {"P6", "P61", "P65", "P62", "P64", "P63", "P622", "P6122", "P6522",
	         "P6222", "P6422", "P6322"};
	private final String[] primitiveRhombohedralSG = {"R3", "R32", "H3", "H32"};
	private final String[] primitiveCubicSG = {"P23", "P213", "P432", "P4232", "P4332", "P4132"};
	private final String[] icenteredCubicSG = {"I23", "I213", "I432", "I4132"};
	private final String[] fcenteredCubicSG = {"F23", "F432", "F4132"};
	
	private final String [][] spacegroupsForLattices = {primitiveTriclinicSG, primitiveMonoclinicSG, centeredMonoclinicSG, primitiveOrthorhombicSG, 
			icenteredOrthorhombicSG, ccenteredOrthorhombicSG, fcenteredOrthorhombicSG, primitiveTetragonalSG, icenteredTetragonalSG,
			primitiveTrigonalSG, primitiveHexagonalSG, primitiveRhombohedralSG, primitiveCubicSG, icenteredCubicSG, fcenteredCubicSG};


	private Combo lattice;
	private Combo spacegroup;
	private Composite content;

	public SpaceGroup(Composite parent, int style) {
		
		super(parent, style);
		
		this.content = new Composite(parent, style);
		
		GridLayout grid = new GridLayout(2, false);
		grid.horizontalSpacing = 1;
		grid.marginWidth = 0;
		content.setLayout(grid);
		
		lattice = new Combo(content, SWT.READ_ONLY);
		lattice.setItems(lattices);
		lattice.setEnabled(true);
		lattice.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		lattice.setToolTipText("Select a lattice");
		
		spacegroup = new Combo(content, SWT.READ_ONLY);
	    spacegroup.setItems(spacegroupsForLattices[0]);
	    spacegroup.setEnabled(false);
	    
	    GridData gd = new GridData(SWT.LEFT, SWT.CENTER, true, false);
	    gd.widthHint = 150;
	    spacegroup.setLayoutData(gd);
	    spacegroup.setToolTipText("Select a spacegroup");

	    lattice.addSelectionListener(new SelectionAdapter() {
		    @Override  
			public void widgetSelected(SelectionEvent e) {
		    	// Would not need this loop if maps were used however
		    	// the loop is a tiny number of items so not an issue.
		    	for (int i = 0; i<lattices.length; i++) {
		    		if (lattice.getText().equals(lattices[i])) {
		    			spacegroup.setItems(spacegroupsForLattices[i]);
		    			spacegroup.setEnabled(true);
		    			spacegroup.setText(spacegroupsForLattices[i][0]);
		    			break;
		    		}
		        } 
		      }
		    });

	    lattice.setText(primitiveTriclinic);
	    spacegroup.setText(primitiveTriclinicSG[0]);
	}

	
	/**
	 * 
	 * @return the lattice value or null if they did not choose.
	 * @throws NumberFormatException
	 */
	public String getLattice() throws NumberFormatException {
		String value = lattice.getText();
		if ("None".equals(value)) return null;
		return value;
	}
	public void setLattice(String l) {
		lattice.setText(l);
	}

	public String getSpacegroup() throws NumberFormatException {
		String value =  spacegroup.getText();
		if ("None".equals(value)) return null;
		return value;
	}

	public void setSpacegroup(String sg) {
		// Remove spaces which ISPyB sometimes has in its value.
    	sg = sg.replace(" ", "");
		
		// We try to set the lattice and the space group options
    	SG_LOOP: for (int ilat = 0; ilat<spacegroupsForLattices.length; ilat++) {
			for (int isg = 0; isg < spacegroupsForLattices[ilat].length; isg++) {
				if (sg.equals(spacegroupsForLattices[ilat][isg])) {
					
				    lattice.setText(lattices[ilat]);
				    spacegroup.setItems(spacegroupsForLattices[ilat]);
				    spacegroup.setText(spacegroupsForLattices[ilat][isg]);
				    break SG_LOOP;
				}
			}
		}
	}

	public void setLayoutData(Object data) {
		content.setLayoutData(data);
	}


	public void setVisible(boolean b) {
		content.setVisible(b);
	}


	public void setEnabled(boolean b) {
		content.setEnabled(b);
		lattice.setEnabled(b);
		spacegroup.setEnabled(b);	
	}


	public void layout() {
		content.getParent().layout();
	}

	protected void checkSubclass () {

	}
	
	public void dispose() {
		super.dispose();
		content.dispose();
	}

	public void addSelectionListener(SelectionListener s) {
		spacegroup.addSelectionListener(s);
	}
	
	public void removeSelectionListener(SelectionListener s) {
		spacegroup.removeSelectionListener(s);
	}
	
}
