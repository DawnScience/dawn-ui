package org.dawnsci.surfacescatter.ui;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

public class OpenDialogHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection selection = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService().getSelection();
		if (selection instanceof StructuredSelection) {
//			StructuredSelection ss = (StructuredSelection)selection;
//			
//			List<String> selectedFile = new ArrayList<String>();
			
//			Iterator it = ss.iterator();
//			
//			while (it.hasNext()) {
//				Object ob = it.next();
//				if (ob instanceof IContain1DData) {
//					selectedFile.add(((IContain1DData)ob).getLongName());
//				}
//			}
//			
//			String[] sf = new String[selectedFile.size()];
//			
//			for (int i = 0; i<selectedFile.size(); i++){
//				sf[i]=selectedFile.get(i);
//			}
//			
//			if (!selectedFile.isEmpty()) {
//				String string = selectedFile.get(0);
//				MessageBox box = new MessageBox(Display.getCurrent().getActiveShell());
//				box.setMessage(string);
//				box.open();
//			}
			
			Shell s =PlatformUI.getWorkbench().getWorkbenchWindows()[0].getShell();
			
//			ExampleDialog ed =new ExampleDialog(s, sf);
//			ed.open();
			
//			SurfaceScatterPresenter ssp = new SurfaceScatterPresenter(s, sf); 
			
//			PresenterInitialSetup pis = new PresenterInitialSetup(s, sf);
//			pis.open();
			
			
			SurfaceScatterPresenter ssp = new SurfaceScatterPresenter();

			
			ssp.setImageFolderPath(null);
			
			SurfaceScatterViewStart ssvs = new SurfaceScatterViewStart(s, 
					   null, 
					   ssp.getNumberOfImages(), 
					   ssp.getImage(0),
					   ssp,
					   null);
//					   datFolderPath);
			
			ssp.setSsvs(ssvs);
			
			ssvs.open();
			
			
		}

		return null;
	}

}
