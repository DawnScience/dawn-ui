package org.dawnsci.isosurface.test;

import org.dawnsci.isosurface.isogui.IsoBean;
import org.dawnsci.isosurface.isogui.IsoComposite;
import org.dawnsci.isosurface.isogui.IsoHandler;
import org.dawnsci.isosurface.tool.IsosurfaceJob;
import org.eclipse.richbeans.widgets.util.SWTUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class GUIRunner 
{
	
	public static void main(String[] args) throws Exception {

		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setLayout(new GridLayout(1, false));
		shell.setText("Change a value to see bean as JSON");
		
		ScrolledComposite sc = new ScrolledComposite(shell, SWT.H_SCROLL | SWT.V_SCROLL);
		sc.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		IsoComposite ui = new IsoComposite(
						sc, 
						SWT.FILL);
		ui.setSize(ui.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		
		sc.setContent(ui);
		
		ui.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
				
		IsoBean isoBean = new IsoBean();
		
		ui.setMinMaxIsoValueAndCubeSize(new double[]{0,100}, new int[]{5,5,5});
		
		try 
		{
			
			IsoHandler isoController = new IsoHandler(
					ui, 
					isoBean, 
					new IsosurfaceJob(
							"isoSurfaceJob" , 
							null, 
							null, 
							null));
			
		}
		catch (Exception e)
		{
			
			System.out.println("IsoController not initilised");
			e.printStackTrace();
			
		}
		
		SWTUtils.showCenteredShell(shell);
	}
}
