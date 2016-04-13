package org.dawnsci.isosurface.isogui;

import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.After;
import org.junit.Before;

public abstract class SWTTest<T>{
	protected T composite;
	private Display display;

	@Before
	public void createComposite(){
		display = new Display();
		Shell shell = new Shell(display);
		shell.setLayout(new FillLayout());
		    
		composite = createComposite(shell);
	}
	
	@After
	public void disposeDisplay(){
		display.dispose();
	}
	
	abstract public T createComposite(Shell shell);
}
