package org.dawnsci.datavis.e4.addons;

import java.util.Collection;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.trace.IPaletteTrace;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.di.extensions.EventTopic;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.SideValue;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainer;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MStackElement;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimBar;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimElement;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimmedWindow;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuFactory;
import org.eclipse.e4.ui.model.application.ui.menu.MToolControl;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.UIEvents.EventTags;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.osgi.service.event.Event;

public class HistogramToolbarAddon {

	@Inject
	MApplication application;
	
	private MToolControl control;
	
    @PostConstruct
    public void init(IEclipseContext context) {
        // injected IEclipseContext comes from the application
        context.set("test1", "Hello");
        
        control = getMyControl();
        
        if (control == null) {
			MTrimBar topTrimBar = getTopTrimBar();
			topTrimBar.getTags().add("Draggable");
			control = MMenuFactory.INSTANCE.createToolControl();
			control.setElementId(HistrogramToolbarControl.ID);
			control.setContributionURI(HistrogramToolbarControl.CLASS_URI);
			topTrimBar.getChildren().add(0, control);
		}
        control.setVisible(false);
        
    }
    
    
    @Inject
	@Optional
	public void subscribeTopicSelectedElement(
			@EventTopic(UIEvents.ElementContainer.TOPIC_SELECTEDELEMENT) Event event) {
		Object newValue = event.getProperty(EventTags.NEW_VALUE);
		
		if (newValue instanceof MPartSashContainer) {
			newValue = ((MPartSashContainer)newValue).getSelectedElement();
		}
		
		if (newValue instanceof MPartSashContainer) {
			newValue = ((MPartSashContainer)newValue).getSelectedElement();
		}
		
		if (newValue instanceof MPartStack){
			MStackElement se = ((MPartStack)newValue).getSelectedElement();
			String elementId = se.getElementId();
			try {
				IViewPart view = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(elementId);
				if (view != null) {
					IPlottingSystem system = view.getAdapter(IPlottingSystem.class);
					if (system != null) {
						system.toString();
						Collection<IPaletteTrace> traces = system.getTracesByClass(IPaletteTrace.class);
						if (traces.isEmpty()) {
							control.setVisible(false);
						} else {
							IPaletteTrace next = traces.iterator().next();
							next.isRescaleHistogram();
							Object object = control.getObject();
							if (object instanceof HistrogramToolbarControl) {
								((HistrogramToolbarControl)object).setSystemTrace(system, next);
							}
						}
						control.setVisible(true);
					} else {
						control.setVisible(false);
					}
				} else {
					control.setVisible(false);
				}
			} catch (PartInitException e) {

			}
			se.toString();
		} else {
			control.setVisible(false);
		}
		
	
		if (!(newValue instanceof MPerspective)) {
			return;
		}
		
	}
    
    private MToolControl getMyControl() {
		MTrimBar topTrimBar = getTopTrimBar();
		if (topTrimBar == null) {
			return null;
		}
		for (MTrimElement trimElement : topTrimBar.getChildren()) {
			if (HistrogramToolbarControl.ID.equals(trimElement.getElementId())) {
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
