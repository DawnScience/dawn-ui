package org.dawb.workbench.plotting.tools.diffraction;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.measure.quantity.Quantity;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.StyledString;

public class DiffractionLabelProvider extends ColumnLabelProvider implements IStyledLabelProvider{

	private int icolumn;
	private NumberFormat format;

	public DiffractionLabelProvider(int icolumn) {
		this.icolumn = icolumn;
		this.format  = new DecimalFormat("#0.####");
	}


	@Override
	public StyledString getStyledText(Object element) {
		
		final StyledString ret = new StyledString();
		if (!(element instanceof LabelNode)) {
			return ret;
		}

		if (element instanceof NumericNode) {
			getStyledText(ret, (NumericNode)element);
			
		} else  if (element instanceof ObjectNode) {
			getStyledText(ret, (ObjectNode)element);
		}
		return ret;
	}
	
	private StyledString getStyledText(StyledString ret, ObjectNode node) {
		
		switch(icolumn) {
		
		case 1:
			return ret.append(node.getValue()+"", StyledString.QUALIFIER_STYLER);
			
		case 2:
			return ret.append(node.getValue()+"");
			
		case 3:
			return ret.append("-", StyledString.QUALIFIER_STYLER);

		}
		return ret;
	}


	private StyledString getStyledText(StyledString ret, NumericNode<? extends Quantity> node) {
		
		switch(icolumn) {
		
		case 1:
			return ret.append(format.format(node.getDefaultValue()), StyledString.QUALIFIER_STYLER);
			
		case 2:
			return node.isEditable()
				  ? ret.append(format.format(node.getValue()), StyledString.DECORATIONS_STYLER)
				  : ret.append(format.format(node.getValue()));
			
		case 3:
			return ret.append(node.getUnit().toString());

		}
		return ret;
	}


}
