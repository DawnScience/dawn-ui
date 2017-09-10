package org.dawnsci.dedi.ui.widgets.units;

import javax.measure.quantity.Quantity;

import org.dawnsci.dedi.ui.GuiHelper;
import org.dawnsci.dedi.ui.TextUtil;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class LabelWithUnits<T extends Quantity> extends WidgetWithUnits<T> implements IAmountChangeListener {
	private Label valueLabel;
	
	public LabelWithUnits(Composite parent, String name, IUnitsProvider<T> provider) {
		super(parent, name, provider);
		GridLayoutFactory.fillDefaults().numColumns(2).margins(0, 5).spacing(30, 15).equalWidth(true).applyTo(this);
		
		valueLabel = GuiHelper.createLabel(this, "");
		addAmountChangeListener(this);
		layout();
		getParent().layout();
	}
	
	
	@Override
	public void amountChanged() {
		if(currentAmount == null){
			valueLabel.setText("");
			layout();
			getParent().layout();
			return;
		}
		valueLabel.setText(TextUtil.format(currentAmount.getEstimatedValue()));
		layout();
		getParent().layout();
	}
}
