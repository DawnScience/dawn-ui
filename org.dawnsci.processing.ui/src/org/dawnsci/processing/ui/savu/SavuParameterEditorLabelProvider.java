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

	public SavuParameterEditorLabelProvider(SavuParameterEditorTableViewModel viewModel, int column) {
		this.column = column;
	}

	@Override
	public Image getImage(Object element) {
		return null;
	}

	@Override
	public String getText(Object element) {
		final SavuParameterEditorRowDataModel model = (SavuParameterEditorRowDataModel)element;
		DecimalFormat pointFormat = new DecimalFormat("##0.0###");
		switch (column) {
		case 0:

			return model.getKey(); 
		case 1:

			Object outVal = model.getValue(); 
				if (outVal instanceof Double) {
					return pointFormat.format(outVal);

				}
				if (outVal instanceof Integer) {
					return outVal.toString();

				}

				if (outVal instanceof Boolean) {
					return outVal.toString();
				}

				if (outVal instanceof String) {
					return outVal.toString();
				}

		case 2:

			return model.getDescription();
		default:
			return "Index out of bounds";
		}
	}

	@Override
	public String getToolTipText(Object element) {
		return "";
	}

}