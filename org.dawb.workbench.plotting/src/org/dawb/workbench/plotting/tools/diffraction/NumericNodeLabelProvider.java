package org.dawb.workbench.plotting.tools.diffraction;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.measure.quantity.Quantity;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.StyledString;

public class NumericNodeLabelProvider extends ColumnLabelProvider implements IStyledLabelProvider{

	private int icolumn;
	private NumberFormat format;

	public NumericNodeLabelProvider(int icolumn) {
		this.icolumn = icolumn;
		this.format  = new DecimalFormat("#0.####");
	}


	@Override
	public StyledString getStyledText(Object element) {
		
		final StyledString ret = new StyledString();
		if (!(element instanceof NumericNode)) {
			return ret;
		}

		NumericNode<? extends Quantity> node = (NumericNode)element;
		
		switch(icolumn) {
		
		case 1:
			return ret.append(format.format(node.getDefaultValue()), StyledString.QUALIFIER_STYLER);
			
		case 2:
			return ret.append(format.format(node.getValue()));
			
		case 3:
			return ret.append(node.getUnit().toString());

		}
		return ret;
	}


}
