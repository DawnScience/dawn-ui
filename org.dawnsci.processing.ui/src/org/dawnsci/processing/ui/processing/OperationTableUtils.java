package org.dawnsci.processing.ui.processing;

import org.dawnsci.common.widgets.table.SeriesTable;
import org.eclipse.swt.widgets.Composite;

public class OperationTableUtils {
	
	/**
	 * Create an operation series table without breaking the encapsulation of the
	 * validators etc being private to the package
	 * 
	 * @param table
	 * @param comp
	 */
	public static void initialiseOperationTable(SeriesTable table, Composite comp) {
		OperationValidator val = new OperationValidator();
		final OperationLabelProvider prov = new OperationLabelProvider(0);
		table.setValidator(val);
		table.createControl(comp, prov);
		table.setInput(null, new OperationFilter());
	}
	
}
