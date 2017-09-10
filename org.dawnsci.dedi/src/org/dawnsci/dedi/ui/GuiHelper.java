package org.dawnsci.dedi.ui;

import java.util.List;

import javax.measure.quantity.Quantity;
import javax.measure.unit.Unit;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * Utility class for instantiating certain SWT elements with a predefined style and layout properties.
 * Allows to achieve a consistent look and feel across the DEDI perspective while avoiding code repetition. 
 * Makes it easy to modify a property for all UI elements of the same class across the entire perspective.
 */
public class GuiHelper {
	
	private GuiHelper() {
		throw new IllegalStateException("This class is not meant to be instantiated.");
	}
	
	private static Font boldFont = JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT); 
	
	public static Font getBoldFont(){
		return boldFont;
	}
	
	
	public static Group createGroup(Composite parent, String name, int numOfCols){
		Group group = new Group(parent, SWT.NONE);
		group.setText(name);
		group.setFont(getBoldFont());
		
		GridLayout layout = new GridLayout(numOfCols, false);
		layout.marginBottom = 5;
		layout.horizontalSpacing = 30;
		layout.verticalSpacing = 15;
		layout.marginHeight = 5;
		layout.marginWidth = 0;
		group.setLayout(layout);
		
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		group.setLayoutData(data);
		
		return group;
	}
	
	
	public static Text createText(Composite parent){
		return new Text(parent, SWT.SINGLE | SWT.BORDER | SWT.RIGHT);
	}
	
	
	public static Label createLabel(Composite parent, String name){
		Label label = new Label(parent, SWT.NONE);
		label.setText(name);
		label.setLayoutData(new GridData(SWT.BEGINNING));
		return label;
	}
	
	
	public static <T extends Quantity> ComboViewer createUnitsCombo(Composite parent, List<Unit<T>> units){
		Combo unitsCombo = new Combo(parent, SWT.READ_ONLY);
		
		ComboViewer unitsComboViewer = new ComboViewer(unitsCombo);
		unitsComboViewer.setContentProvider(ArrayContentProvider.getInstance());
		unitsComboViewer.setLabelProvider(new LabelProvider() {
				@Override
				public String getText(Object element){
					if(element instanceof Unit<?>){
						@SuppressWarnings("unchecked")
						Unit<T> unit = (Unit<T>) element;
						return unit.toString();
					}
					return super.getText(element);
				}
		});
		
		unitsComboViewer.setInput(units);
		if(units != null && !units.isEmpty())
			unitsComboViewer.setSelection(new StructuredSelection(units.get(0)));
		
		return unitsComboViewer;
	}
}
