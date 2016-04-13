package org.dawnsci.isosurface.isogui;

import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.List;

import org.eclipse.richbeans.api.widget.IFieldWidget;
import org.eclipse.richbeans.widgets.wrappers.SpinnerWrapper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.junit.Test;

public class IncrementGroupSelectionListenerTest extends SWTTest<List<IFieldWidget>>{
	@Override
	public List<IFieldWidget> createComposite(Shell shell) {
		List<SpinnerWrapper> widgets =  range(0, 4).mapToObj(i -> new SpinnerWrapper(shell, SWT.BORDER)).collect(toList());
		widgets.forEach(widget -> widget.setMaximum(10));
		widgets.forEach(widget -> widget.setMinimum(-10));
		return widgets.stream().map(widget -> (IFieldWidget)widget).collect(toList());
	}

	@Test
	public void testIncrementsComponets(){
		new IncrementGroupSelectionListener(composite, 2).widgetSelected(null);
		
		composite.forEach(spinnerWrapper -> assertThat(spinnerWrapper.getValue(), is(2)));
	}
	
	@Test
	public void testdecrementsComponetsToLimit(){
		new IncrementGroupSelectionListener(composite, -100).widgetSelected(null);
		
		composite.forEach(spinnerWrapper -> assertThat(spinnerWrapper.getValue(), is(-10)));
	}
}
