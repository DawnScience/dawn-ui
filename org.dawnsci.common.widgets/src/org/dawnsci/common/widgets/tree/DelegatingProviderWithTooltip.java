package org.dawnsci.common.widgets.tree;

import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

public class DelegatingProviderWithTooltip extends DelegatingStyledCellLabelProvider {

	public DelegatingProviderWithTooltip(IStyledLabelProvider labelProvider) {
		super(labelProvider);
	}
	@Override
	public String getToolTipText(Object element) {
		return ((CellLabelProvider)getStyledStringProvider()).getToolTipText(element);
	}

	@Override
	public Point getToolTipShift(Object element) {
		return ((CellLabelProvider)getStyledStringProvider()).getToolTipShift(element);
	}

	@Override
	public int getToolTipDisplayDelayTime(Object element) {
		return ((CellLabelProvider)getStyledStringProvider()).getToolTipDisplayDelayTime(element);
	}
	@Override
	public int getToolTipTimeDisplayed(Object element) {
		return ((CellLabelProvider)getStyledStringProvider()).getToolTipTimeDisplayed(element);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IColorProvider#getBackground(java.lang.Object)
	 */
	public Color getBackground(Object element) {
		return ((ColumnLabelProvider)getStyledStringProvider()).getBackground(element);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IColorProvider#getForeground(java.lang.Object)
	 */
	public Color getForeground(Object element) {
		return ((ColumnLabelProvider)getStyledStringProvider()).getForeground(element);
	}

	public Image getImage(Object element) {
		return ((ColumnLabelProvider)getStyledStringProvider()).getImage(element);
	}

}
