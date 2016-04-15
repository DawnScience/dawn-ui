package org.dawnsci.isosurface.isogui;


import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Arrays;
import java.util.List;

import org.eclipse.richbeans.api.widget.IFieldWidget;
import org.eclipse.richbeans.widgets.scalebox.NumberBox;
import org.eclipse.swt.widgets.Shell;
import org.junit.Test;

public class IsoItemCompositeTest extends SWTTest<IsoItemComposite>{
	@Override
	public IsoItemComposite createComposite(Shell shell) {
		return new IsoItemComposite(shell);
	}
	
	@Test
	public void testCompositeHasALayout() {
		assertThat(composite.getLayout(), not(nullValue()));
	}
	
	@Test
	public void testExpectedComponetsAreCreated(){
		List<IFieldWidget> widgets = Arrays.asList(
				composite.getValue(),
				composite.getX(),
				composite.getY(),
				composite.getZ(),
				composite.getColour(),
				composite.getOpacity()
		);
				
		widgets.forEach(widget -> assertThat(widget, not(nullValue())));
		assertThat(Arrays.asList(composite.getChildren()).containsAll(widgets), is(true));
	}
	
	@Test
	public void testIsoMaxAndMin(){
		composite.setMinMaxIsoValue(10,  20);
		
		NumberBox numberBox = (NumberBox)composite.getValue();
		assertThat(numberBox.getMaximum(), is(20.0));
		assertThat(numberBox.getMinimum(), is(10.0));
	}
}
