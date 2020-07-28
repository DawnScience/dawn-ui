package org.dawnsci.plotting.tools.imagecuts;

import org.dawnsci.multidimensional.ui.imagecuts.PerpendicularCutsHelper;
import org.dawnsci.multidimensional.ui.imagecuts.PerpendicularImageCutsComposite;
import org.dawnsci.plotting.tools.Activator;
import org.eclipse.dawnsci.plotting.api.IPlottingService;
import org.eclipse.dawnsci.plotting.api.tool.AbstractToolPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tool for displaying perpendicular cuts through an image for ARPES
 *
 */
public class PerpendicularImageCutsTool extends AbstractToolPage {

	private static final Logger logger = LoggerFactory.getLogger(PerpendicularImageCutsTool.class);

	private PerpendicularImageCutsComposite composite;
	
	private PerpendicularCutsHelper helper;

	@Override
	public void createControl(Composite parent) {

		IPlottingService service = Activator.getService(IPlottingService.class);
		try {
			composite = new PerpendicularImageCutsComposite(parent, SWT.NONE, service);
		} catch (Exception e) {
			logger.error("Could not create composite!");
			return;
		}
	}


	@Override
	public void activate() {
		super.activate();
		if (helper == null) {
			helper = new PerpendicularCutsHelper(getPlottingSystem());
		}
		helper.activate(composite);

	}

	@Override
	public void deactivate() {
		if (!isActive()) {
			return;
		}
		super.deactivate();
		helper.deactivate(composite);
	}

	@Override
	public void dispose() {
		super.dispose();
		composite.dispose();
		
	}

	@Override
	public ToolPageRole getToolPageRole() {
		return ToolPageRole.ROLE_2D;
	}

	@Override
	public Control getControl() {
		return composite;
	}

	@Override
	public void setFocus() {
		composite.setFocus();
	}
}
