package org.dawb.workbench.ui.diffraction.table;

import java.util.EventObject;

public class DiffractionDataChanged extends EventObject{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public DiffractionDataChanged(DiffractionTableData source) {
		super(source);
	}

}
