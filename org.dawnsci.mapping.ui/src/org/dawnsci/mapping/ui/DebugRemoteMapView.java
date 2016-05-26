package org.dawnsci.mapping.ui;

import org.dawnsci.mapping.ui.datamodel.LiveDataBean;
import org.dawnsci.mapping.ui.datamodel.MappedDataFileBean;
import org.dawnsci.mapping.ui.datamodel.MappedFileManager;
import org.eclipse.dawnsci.analysis.api.persistence.IPersistenceService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

public class DebugRemoteMapView extends ViewPart {

	private Composite parent;
	
	@Override
	public void createPartControl(Composite parent) {
		this.parent = parent;
		parent.setLayout(new GridLayout(2, false));
		
		Label l = new Label(parent, SWT.NONE);
		l.setText("host");
		
		final Text host = new Text(parent, SWT.NONE);
		host.setText("localhost");
		
		l = new Label(parent, SWT.NONE);
		l.setText("port");
		
		final Text port = new Text(parent, SWT.NONE);
		port.setText("8690");
		
		l = new Label(parent, SWT.NONE);
		l.setText("filename");
		
		final Text filename = new Text(parent, SWT.NONE);
		filename.setText("/scratch/SSD/junk/mapping_out.nxs");
		
	    final Text tbean = new Text(parent, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
	    tbean.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,true,2,1));
	    tbean.setText("{\"blocks\":\n"
	    		+ "[{\"name\":\"/entry/result/data\",\n"
	    		+ "\"rank\":3,\n"
	    		+ "\"axes\":[\"/entry/result/sc_MicroFocusSampleY\",\"/entry/result/traj1ContiniousX\",null],\n"
	    		+ "\"xAxisForRemapping\":null,\n"
	    		+ "\"xDim\":1,\n"
	    		+ "\"yDim\":0}],\n"
	    		+ "\"maps\":[{\"name\":\"/entry/auxiliary/0-Integrate Range/integrated/data\",\"parent\":\"/entry/result/data\"}],\n"
	    		+ "\"liveBean\":null}");

	    Button b1 = new Button(parent,SWT.PUSH);
	    b1.setText("View");
	    b1.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				IPersistenceService p = LocalServiceManager.getPersistenceService();
				try {
					
					if (tbean.getText() == null || tbean.getText().isEmpty()) {
						LiveDataBean l = new LiveDataBean();
						l.setPort(Integer.parseInt(port.getText()));
						l.setHost(host.getText());
						IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
						IViewPart view = page.findView("org.dawnsci.mapping.ui.mappeddataview");
						if (view==null) return;

						final MappedFileManager manager = (MappedFileManager)view.getAdapter(MappedFileManager.class);
						if (manager != null) {
							manager.importLiveFile(filename.getText(), l);
						}
					}
					
					MappedDataFileBean b = p.unmarshal(tbean.getText(),MappedDataFileBean.class);
					LiveDataBean l = new LiveDataBean();
					l.setPort(Integer.parseInt(port.getText()));
					l.setHost(host.getText());
					b.setLiveBean(l);
					IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
					IViewPart view = page.findView("org.dawnsci.mapping.ui.mappeddataview");
					if (view==null) return;

					final MappedFileManager manager = (MappedFileManager)view.getAdapter(MappedFileManager.class);
					if (manager != null) {
						manager.importFile(filename.getText(), b);
					}
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
	    
	    
	    
	}

	@Override
	public void setFocus() {
		parent.setFocus();

	}

}
