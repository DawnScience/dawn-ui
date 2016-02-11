package org.dawnsci.processing.ui.processing;

import org.dawnsci.processing.ui.slice.IOperationErrorInformer;
import org.dawnsci.processing.ui.slice.OperationInformerImpl;
import org.eclipse.richbeans.widgets.table.SeriesTable;
import org.eclipse.swt.widgets.Composite;

public class OperationTableUtils {
	
	/**
	 *  Create an operation series table without breaking the encapsulation of the
	 * validators etc being private to the package
	 * 
	 * @param table
	 * @param comp
	 * 
	 * @return informer
	 */
	public static IOperationErrorInformer initialiseOperationTable(SeriesTable table, Composite comp) {
		OperationValidator val = new OperationValidator();
		IOperationErrorInformer info = new OperationInformerImpl(table);
		val.setOperationErrorInformer(info);
		final OperationLabelProvider prov = new OperationLabelProvider(0);
		table.setValidator(val);
		table.createControl(comp, prov);
		OperationFilter f = new OperationFilter();
		f.setOperationErrorInformer(info);
		table.setInput(null, f);
		return info;
	}
	
}
