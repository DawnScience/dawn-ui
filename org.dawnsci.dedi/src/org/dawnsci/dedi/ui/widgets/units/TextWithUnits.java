package org.dawnsci.dedi.ui.widgets.units;

import javax.measure.quantity.Quantity;

import org.dawnsci.dedi.ui.TextUtil;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.jscience.physics.amount.Amount;

public class TextWithUnits<T extends Quantity> extends WidgetWithUnits<T> implements IAmountChangeListener {
	private IAmountInputValidator<T> validator;
	private Text text;
	private boolean isEdited = true;
	
	
	public TextWithUnits(Composite parent, String name, IUnitsProvider<T> provider){
		this(parent, name, provider, input -> true);
	}
	
	
	public TextWithUnits(Composite parent, String name, IUnitsProvider<T> provider, IAmountInputValidator<T> validator) {
		super(parent, name, provider);
		GridLayoutFactory.fillDefaults().numColumns(2).margins(0, 5).spacing(30, 15).equalWidth(true).applyTo(this);
		
		text = new Text(this, SWT.SINGLE | SWT.BORDER);
		GridDataFactory.fillDefaults().align(SWT.END, SWT.CENTER).hint(70, 20).applyTo(text);
		this.validator = validator;
		
		addAmountChangeListener(this);
		
		text.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				if(!isEdited) return;
				isEdited = false;
				try{
					Amount<T> newValue = Amount.valueOf(Double.parseDouble(text.getText()), unitsProvider.getCurrentUnit());
					if(validator.isValid(newValue)){
						text.setForeground(new Color(Display.getCurrent(), new RGB(0, 0, 0)));
						setValue(newValue);
					} else throw new NumberFormatException();
				} catch(NumberFormatException ex){
					text.setForeground(new Color(Display.getCurrent(), new RGB(255, 0, 0)));
					setValue(null);
				} finally {
					isEdited = true;
				}
			}
		});
	}
	
	
	@Override
	public void setToolTipText(String ttt){
		checkWidget();
		text.setToolTipText(ttt);
	}
	
	
	public void clearText(){
		checkWidget();
		text.setText("");
	}
	

	@Override
	public void amountChanged() {
		if(!isEdited) return;
		try{
			isEdited = false;
			if(currentAmount == null || !validator.isValid(currentAmount))
				text.setForeground(new Color(Display.getCurrent(), new RGB(255, 0, 0)));
			else
				text.setForeground(new Color(Display.getCurrent(), new RGB(0, 0, 0)));
			if(currentAmount != null)
				text.setText(TextUtil.format(currentAmount.getEstimatedValue()));
			else 
				text.setText("");
		} catch(NullPointerException e){
			e.printStackTrace();
		} finally {
			isEdited = true;
		}
		
	}
}