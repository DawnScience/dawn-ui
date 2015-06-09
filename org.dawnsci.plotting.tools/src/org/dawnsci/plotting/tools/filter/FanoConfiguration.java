package org.dawnsci.plotting.tools.filter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.filter.AbstractPlottingFilter;
import org.eclipse.dawnsci.plotting.api.filter.FilterConfiguration;
import org.eclipse.dawnsci.plotting.api.filter.IPlottingFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

public class FanoConfiguration implements FilterConfiguration {
	
	private final static String[] BOX_OPTIONS;
	static {
		BOX_OPTIONS = new String[]{"3x3", "5x5", "7x7", "9x9"};
	}

	private IPlottingSystem        system;
	private AbstractPlottingFilter filter;

	@Override
	public void init(IPlottingSystem system, IPlottingFilter filter) {
		this.system = system;
		this.filter = (AbstractPlottingFilter)filter;
	}

	@Override
	public Control createControl(Composite parent) {
		
       final Composite content = new Composite(parent, SWT.NONE);
       content.setLayout(new GridLayout(2, false));
       
       Label label = new Label(content, SWT.NONE);
       label.setText("Fano Box");
       
       final CCombo boxOptions = new CCombo(content, SWT.NONE);
       boxOptions.setItems(BOX_OPTIONS);
       
       boxOptions.addSelectionListener(new SelectionAdapter() {
    	   public void widgetSelected(SelectionEvent e) {
    		   setBox(boxOptions.getItems()[boxOptions.getSelectionIndex()]);
    	   }
       });
       
       boxOptions.addModifyListener(new ModifyListener() {	
    	   @Override
    	   public void modifyText(ModifyEvent e) {
    		   setBox(boxOptions.getText());
    	   }
       });
             
       return content;
	}

	private static final Pattern BOX_SIZE_PATTERN = Pattern.compile("(\\d+)x(\\d+)");
	
	private void setBox(String string) {
		Matcher matcher = BOX_SIZE_PATTERN.matcher(string);
		int w = Integer.parseInt(matcher.group(1));
		int h = Integer.parseInt(matcher.group(2));
		filter.putConfiguration("box", new int[]{w,h});
		system.repaint(); // TODO is this enough to apply the filter?
	}

}
