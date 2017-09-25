package org.dawnsci.datavis.e4.addons;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.SideValue;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimBar;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimElement;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimmedWindow;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuFactory;
import org.eclipse.e4.ui.model.application.ui.menu.MToolControl;

public class PlotModifierOffsetAddon {

	@Inject
	MApplication application;
	
	private MToolControl control;
	
    @PostConstruct
    public void init(IEclipseContext context) {
        // injected IEclipseContext comes from the application

        control = getMyControl();
        
        if (control == null) {
			MTrimBar topTrimBar = getTopTrimBar();
			topTrimBar.getTags().add("Draggable");
			control = MMenuFactory.INSTANCE.createToolControl();
			control.setElementId(PlotModifierOffsetControl.ID);
			control.setContributionURI(PlotModifierOffsetControl.CLASS_URI);
			topTrimBar.getChildren().add(0, control);
		}
        control.setVisible(true);
        
    }
    

    
    private MToolControl getMyControl() {
		MTrimBar topTrimBar = getTopTrimBar();
		if (topTrimBar == null) {
			return null;
		}
		for (MTrimElement trimElement : topTrimBar.getChildren()) {
			if (PlotModifierOffsetControl.ID.equals(trimElement.getElementId())) {
				return (MToolControl) trimElement;
			}
		}
		return null;
	}
    
    private MTrimBar getTopTrimBar() {
		for (MWindow window : application.getChildren()) {
			if (window instanceof MTrimmedWindow) {
				for (MTrimBar trimBar : ((MTrimmedWindow) window).getTrimBars()) {
					if (trimBar.getSide() == SideValue.TOP) {
						return trimBar;
					}
				}
			}
		}
		return null;
	}
	
}
