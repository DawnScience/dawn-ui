package org.dawnsci.mapping.ui;

import org.dawnsci.mapping.ui.datamodel.AssociatedImage;
import org.dawnsci.mapping.ui.dialog.RectangleRegistrationDialog;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.RGBDataset;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

public class RegistrationHelperImpl implements IRegistrationHelper {

	
	private MapPlotManager plotManager;

	public RegistrationHelperImpl(MapPlotManager manager) {
		this.plotManager = manager;
	}
	
	@Override
	public void register(final String path, final IDataset image) {
		
		if (Display.getCurrent() == null) {
			PlatformUI.getWorkbench().getDisplay().asyncExec(() -> register(path, image));
			return;
		}
		
		RectangleRegistrationDialog dialog = new RectangleRegistrationDialog(PlatformUI.getWorkbench().getDisplay().getActiveShell(), plotManager.getTopMap().getMap(),image);
		if (dialog.open() != IDialogConstants.OK_ID) return;
		RGBDataset ds = (RGBDataset)dialog.getRegisteredImage();
		ds.setName("Registered");
		AssociatedImage asIm = new AssociatedImage("Registered", ds, path);
		FileManagerSingleton.getFileManager().addAssociatedImage(asIm);

	}

}
