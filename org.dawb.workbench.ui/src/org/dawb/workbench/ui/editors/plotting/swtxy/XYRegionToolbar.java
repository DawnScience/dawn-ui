package org.dawb.workbench.ui.editors.plotting.swtxy;

import java.util.List;

import org.csstudio.swt.xygraph.figures.XYGraph;
import org.csstudio.swt.xygraph.toolbar.CheckableActionGroup;
import org.csstudio.swt.xygraph.toolbar.GrayableButton;
import org.csstudio.swt.xygraph.toolbar.XYGraphToolbar;
import org.eclipse.draw2d.ButtonGroup;
import org.eclipse.draw2d.ButtonModel;
import org.eclipse.draw2d.ChangeEvent;
import org.eclipse.draw2d.ChangeListener;
import org.eclipse.draw2d.Clickable;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.ImageFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.ToggleButton;
import org.eclipse.draw2d.ToggleModel;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;

public class XYRegionToolbar extends XYGraphToolbar {

	private ButtonGroup roiGroup;

	public XYRegionToolbar(XYGraph xyGraph) {
		super(xyGraph);
	}

	public XYRegionToolbar(XYGraph xyGraph, int flags) {
		super(xyGraph, flags);
	}

	/**
	 * We would like the region buttons here before the zoom buttons,
	 * so we hack in creation of some line, profile, etc buttons.
	 */
	protected void createZoomButtons(final int flags) {
        
		roiGroup = new ButtonGroup();

		addSeparator();
		
		for(final ROIType roiType : ROIType.values()){
		    
			final ImageFigure imageFigure =  new ImageFigure(roiType.getIconImage());
			final Label tip = new Label(roiType.getDescription());
			final ToggleButton button = new ToggleButton(imageFigure);
			button.setBackgroundColor(ColorConstants.button);
			button.setOpaque(true);
			final ToggleModel model = new ToggleModel();
			model.addChangeListener(new ChangeListener(){
				public void handleStateChanged(ChangeEvent event) {
					if(event.getPropertyName().equals("selected") && 
							button.isSelected()){
						((XYRegionGraph)xyGraph).setROIType(roiType);
					}				
				}
			});
			
			button.setModel(model);
			button.setToolTip(tip);
			addButton(button);
			roiGroup.add(model);
			
			if (roiType == ROIType.NONE) roiGroup.setDefault(model);
		}

		addSeparator();
	
		super.createZoomButtons(flags);
	}
	
	
	/**
	 * Bodges up a normal toolbar from the Figure toolbar.
	 * @param xyGraph
	 * @param man
	 */
	public void createGraphActions(final IContributionManager tool, final IContributionManager men) {
        
        final CheckableActionGroup zoomG = new CheckableActionGroup();
        final CheckableActionGroup roiG  = new CheckableActionGroup();
        
        for (Object child : getChildren()) {
			
        	if (!(child instanceof Figure)) continue;
        	final Figure c = (Figure)child;
        	if (c instanceof Clickable) {
        		
        		final Clickable button = (Clickable)c;
        		final int flag = button instanceof ToggleButton
        		               ? IAction.AS_CHECK_BOX
        		               : IAction.AS_PUSH_BUTTON;
        		
        		final String text  = ((Label)button.getToolTip()).getText();
        		
        		final Object cont  = button.getChildren().get(0);
        		final Image  image = cont instanceof ImageFigure
        		                   ? ((ImageFigure)cont).getImage()
        		                   : ((Label)cont).getIcon();
        		                   
        		final Action action = new Action(text, flag) {
        			public void run() {
        				if (button.getModel() instanceof ToggleModel) {
        					((ToggleModel)button.getModel()).fireActionPerformed();
        				} else {
        				    button.doClick();
        				}        				
        			}
				};
				 
				if (flag == IAction.AS_CHECK_BOX) {
					final boolean isSel = button.isSelected();
					action.setChecked(isSel);
				}

				if (button instanceof GrayableButton) {
					final GrayableButton gb = (GrayableButton)button;
					gb.addChangeListener(new ChangeListener() {	
						@Override
						public void handleStateChanged(ChangeEvent event) {
							if (event.getPropertyName().equals(ButtonModel.ENABLED_PROPERTY)) {
                                action.setEnabled(gb.isEnabled());
							}
						};
					});

				};
        				
				action.setImageDescriptor(new ImageDescriptor() {			
					@Override
					public ImageData getImageData() {
						return image.getImageData();
					}
				});
				
				tool.add(action);
				men.add(action);
				
				final List rois = roiGroup.getElements();
        	    if (rois.contains(button.getModel())) {
        	    	roiG.add(action);
        	    }
				
        	    final List models = zoomGroup.getElements();
        	    if (models.contains(button.getModel())) {
        	    	zoomG.add(action);
        	    }
        	    
        	} else if (c instanceof ToolbarSeparator) {
        		
           		tool.add(new Separator(ToolbarSeparator.class.getName()+Math.random()));
           		men.add(new Separator(ToolbarSeparator.class.getName()+Math.random()));
        	}
		}
	}
}
