package org.dawnsci.common.widgets.tree;

import javax.measure.quantity.Quantity;

import org.dawnsci.common.widgets.Activator;
import org.dawnsci.common.widgets.tree.LabelNode;
import org.dawnsci.common.widgets.tree.NumericNode;
import org.dawnsci.common.widgets.tree.ObjectNode;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;

public class NodeLabelProvider extends ColumnLabelProvider implements IStyledLabelProvider{

	private int icolumn;

	public NodeLabelProvider(int icolumn) {
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
			
		} else if (element instanceof ComboNode){
			getStyledText(ret, (ComboNode)element);
		} else  if (element instanceof ObjectNode) {
			final ObjectNode on = (ObjectNode)element;
			if (on.isSubClass()) return ret;
			getStyledText(ret, on);
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
			//return ret.append("-", StyledString.QUALIFIER_STYLER);

		}
		return ret;
	}
	
	private StyledString getStyledText(StyledString ret, ComboNode node) {
		
		if (icolumn == 2) {
			if (node.isEditable()) {
				ret.append(node.getStringValue());
				ret.append(" *", StyledString.QUALIFIER_STYLER);
			} else {
				ret.append(node.getStringValue(), StyledString.DECORATIONS_STYLER);
			}
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
		
		if (ln.getTooltip()!=null) {
			buf.append(ln.getTooltip());
			buf.append("\n");
		}
		
		if (ln.isEditable()) buf.append(" Click to edit the value or the units.\n");

		buf.append(" Right click to copy or reset value.");
		
		return buf.toString();
	}

	public int getToolTipDisplayDelayTime(Object object) {
		return 500;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IColorProvider#getBackground(java.lang.Object)
	 */
	public Color getBackground(Object element) {
		if (element instanceof ColorNode && (icolumn==1 || icolumn==2)) {
			return ((ColorNode)element).getColor();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IColorProvider#getForeground(java.lang.Object)
	 */
	public Color getForeground(Object element) {
		if (element instanceof ColorNode  && (icolumn==1 || icolumn==2)) { // Value
			return ((ColorNode)element).getColor();
		}
		return null;
	}

	private Image ticked, unticked;
	public Image getImage(Object element) {
		if (element instanceof BooleanNode  && (icolumn==1 || icolumn==2)) { // Value
			if (ticked==null)   ticked   = Activator.getImage("icons/ticked.png");
			if (unticked==null) unticked = Activator.getImage("icons/unticked.gif");
			return ((BooleanNode)element).isValue() 
				   ? ticked
				   : unticked;
		}
		return null;
	}

	public void dispose() {
		if (ticked!=null)   ticked.dispose();
		if (unticked!=null) unticked.dispose();
	}
}
