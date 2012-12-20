package org.dawnsci.common.widgets.tree;

import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider;
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

}
