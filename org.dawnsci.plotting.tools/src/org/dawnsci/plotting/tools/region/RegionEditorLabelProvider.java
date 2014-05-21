/*
 * Copyright (c) 2013 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.tools.region;

import javax.measure.quantity.Quantity;

import org.dawnsci.common.widgets.Activator;
import org.dawnsci.common.widgets.tree.ComboNode;
import org.dawnsci.common.widgets.tree.LabelNode;
import org.dawnsci.common.widgets.tree.NumericNode;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.swt.graphics.Image;

public class RegionEditorLabelProvider extends ColumnLabelProvider implements IStyledLabelProvider {

	private int column;
	private Image ticked, unticked;

	public RegionEditorLabelProvider(int column) {
		this.column = column;
	}

	@Override
	public StyledString getStyledText(Object element) {
		final StyledString ret = new StyledString();
		if (!(element instanceof LabelNode)) {
			return ret;
		}
		if (element instanceof RegionNode) {
			getStyledText(ret, (RegionNode)element);
		} else if (element instanceof NumericNode) {
			getStyledText(ret, (NumericNode<?>)element);
		} else if (element instanceof ComboNode){
			getStyledText(ret, (ComboNode)element);
		}
		return ret;
	}

	private StyledString getStyledText(StyledString ret, ComboNode node) {
		if (column == 2) { // Unit
			if (node.isEditable()) {
				ret.append(node.getStringValue());
				ret.append(" *", StyledString.QUALIFIER_STYLER);
			} else {
				ret.append(node.getStringValue(), StyledString.DECORATIONS_STYLER);
			}
		}
		return ret;
	}

	private StyledString getStyledText(StyledString ret, RegionNode node) {
		if(column == 0) { // Name
			if (node.isEditable()) {
				ret.append(node.getLabel());
				ret.append(" *", StyledString.QUALIFIER_STYLER);
			} else {
				ret.append(node.getLabel(), StyledString.QUALIFIER_STYLER);
			}
		}
		return ret;
	}

	private StyledString getStyledText(StyledString ret, NumericNode<? extends Quantity> node) {
		switch(column) {
		case 0: // Name
			return ret.append(node.getLabel(), StyledString.QUALIFIER_STYLER);
		case 1: // Value
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
		case 2: // Unit
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
		if (!(element instanceof LabelNode))
			return super.getToolTipText(element);

		LabelNode ln = (LabelNode) element;
		StringBuilder buf = new StringBuilder();
		// buf.append("'");
		// buf.append(ln.getPath());
		// buf.append("'\n");

		if (ln.getTooltip() != null) {
			buf.append(ln.getTooltip());
			buf.append("\n");
		}

		if (ln.isEditable()) {
			if (column == 3)
				buf.append(" Enable/Disable the visibility of a region.\n");
			else if (column == 4)
				buf.append(" Enable/Disable the ROI active flag.\n");
			else if (column == 5)
				buf.append(" Enable/Disable the mobility of a region.\n");
			else
				buf.append(" Click to edit the value or the units.\n");
		}
		buf.append(" Right click to copy or reset value.");
		return buf.toString();
	}

	public int getToolTipDisplayDelayTime(Object object) {
		return 500;
	}

	@Override
	public Image getImage(Object element) {
		switch (column) {
		case 3:// Visible
			if (element instanceof RegionNode)
				return getCheckBoxImage(((RegionNode) element).isVisible());
			break;
		case 4://active
			if (element instanceof RegionNode)
				return getCheckBoxImage(((RegionNode) element).isActive());
			break;
		case 5://mobile
			if (element instanceof RegionNode)
				return getCheckBoxImage(((RegionNode) element).isMobile());
			break;
		}
		return null;
	}

	private Image getCheckBoxImage(boolean isChecked) {
			if (ticked == null)
				ticked = Activator.getImage("icons/ticked.png");
			if (unticked == null)
				unticked = Activator.getImage("icons/unticked.gif");
			return isChecked ? ticked : unticked;
	}

	@Override
	public void dispose() {
		if (ticked != null)
			ticked.dispose();
		if (unticked != null)
			unticked.dispose();
	}
}
