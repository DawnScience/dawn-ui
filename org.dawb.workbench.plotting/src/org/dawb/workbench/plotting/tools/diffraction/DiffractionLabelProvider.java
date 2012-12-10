package org.dawb.workbench.plotting.tools.diffraction;

import javax.measure.quantity.Quantity;

import org.dawb.common.ui.tree.LabelNode;
import org.dawb.common.ui.tree.NumericNode;
import org.dawb.common.ui.tree.ObjectNode;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.StyledString;

public class DiffractionLabelProvider extends ColumnLabelProvider implements IStyledLabelProvider{

	private int icolumn;

	public DiffractionLabelProvider(int icolumn) {
		this.icolumn = icolumn;
	}


	@Override
	public StyledString getStyledText(Object element) {
		
		final StyledString ret = new StyledString();
		if (!(element instanceof LabelNode)) {
			return ret;
		}

		if (element instanceof NumericNode) {
			getStyledText(ret, (NumericNode<?>)element);
			
		} else  if (element instanceof ObjectNode) {
			getStyledText(ret, (ObjectNode)element);
		}
		return ret;
	}
	
	private StyledString getStyledText(StyledString ret, ObjectNode node) {
		
		switch(icolumn) {
		
		case 1: // Default
			return ret.append(node.getValue()+"", StyledString.QUALIFIER_STYLER);
			
		case 2: // Value
			return ret.append(node.getValue()+"", StyledString.DECORATIONS_STYLER);
			 
		case 3: // Unit
			return ret.append("-", StyledString.QUALIFIER_STYLER);

		}
		return ret;
	}


	private StyledString getStyledText(StyledString ret, NumericNode<? extends Quantity> node) {
		
		switch(icolumn) {
		
		case 1: // Default
			return ret.append(node.getDefaultValue(true), StyledString.QUALIFIER_STYLER);
			
		case 2: // Value
			if (node.isNaN()) {
				if (node.isEditable()) {
					ret.append("N/A");
					ret.append(" *", StyledString.QUALIFIER_STYLER);
				} else {
					ret.append("N/A", StyledString.DECORATIONS_STYLER);
				}
			    return ret;
			}
			if (node.isEditable()) {
				ret.append(node.getValue(true));
				ret.append(" *", StyledString.QUALIFIER_STYLER);
			} else {
				ret.append(node.getValue(true), StyledString.DECORATIONS_STYLER);
			}
			return ret;
			
		case 3: // Unit
			if (node.isEditable()) {
				return ret.append(node.getUnit().toString());
			} else {
				return ret.append(node.getUnit().toString(), StyledString.DECORATIONS_STYLER);
			}

		}
		return ret;
	}


	@Override
	public String getToolTipText(Object element) {
		
		if (!(element instanceof LabelNode)) return super.getToolTipText(element);
		
		LabelNode ln = (LabelNode)element;
		StringBuilder buf = new StringBuilder();
//		buf.append("'");
//		buf.append(ln.getPath());
//		buf.append("'\n");
		
		if (ln.isEditable()) buf.append(" Click to edit the value or the units.\n");

		buf.append(" Right click to copy or reset value.");
		
		return buf.toString();
	}

	public int getToolTipDisplayDelayTime(Object object) {
		return 500;
	}
}
