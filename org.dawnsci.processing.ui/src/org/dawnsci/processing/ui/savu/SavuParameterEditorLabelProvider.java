package org.dawnsci.processing.ui.savu;

import java.text.DecimalFormat;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 * Table viewer label provider
 *
 */
class SavuParameterEditorLabelProvider extends ColumnLabelProvider {

	private int column;

	public SavuParameterEditorLabelProvider(int column) {
		this.column = column;
	}

	@Override
	public Image getImage(Object element) {
		return null;
	}

	@Override
	public String getText(Object element) {
		final SavuParameterEditorRowDataModel model = (SavuParameterEditorRowDataModel) element;
		DecimalFormat pointFormat = new DecimalFormat("##0.0###");
		return switch (column) {
		case 0:
			yield model.getKey();
		case 1:
			Object outVal = model.getValue();
			if (outVal instanceof Double) {
				yield pointFormat.format(outVal);
			}
			if (outVal instanceof Integer || outVal instanceof Boolean) {
				yield outVal.toString();
			}
			if (outVal instanceof String str) {
				yield str;
			}
			/* FALLTHROUGH */
		case 2:
			yield model.getDescription();
		default:
			yield "Index out of bounds";
		};
	}

	@Override
	public String getToolTipText(Object element) {
		return "";
	}

}