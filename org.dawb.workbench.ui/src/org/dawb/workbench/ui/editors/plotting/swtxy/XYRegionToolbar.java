package org.dawb.workbench.ui.editors.plotting.swtxy;


import org.csstudio.swt.xygraph.figures.XYGraph;
import org.csstudio.swt.xygraph.toolbar.XYGraphToolbar;
import org.csstudio.swt.xygraph.undo.AddAnnotationCommand;
import org.csstudio.swt.xygraph.undo.RemoveAnnotationCommand;
import org.dawb.workbench.ui.Activator;
import org.dawb.workbench.ui.editors.plotting.dialog.AddRegionCommand;
import org.dawb.workbench.ui.editors.plotting.dialog.AddRegionDialog;
import org.dawb.workbench.ui.editors.plotting.dialog.RemoveRegionCommand;
import org.dawb.workbench.ui.editors.plotting.dialog.RemoveRegionDialog;
import org.eclipse.draw2d.ActionEvent;
import org.eclipse.draw2d.ActionListener;
import org.eclipse.draw2d.Button;
import org.eclipse.draw2d.Label;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;

public class XYRegionToolbar extends XYGraphToolbar {


	public XYRegionToolbar(XYRegionGraph xyGraph) {
		super(xyGraph);
	}

	public XYRegionToolbar(XYRegionGraph xyGraph, int flags) {
		super(xyGraph, flags);
	}

	/**
	 * We would like the region buttons here before the zoom buttons,
	 * so we hack in creation of some line, profile, etc buttons.
	 */
	protected void createExtraActions() {
        
		final Button addRegion = new Button(Activator.getImage("icons/ProfileLine.png"));
		addRegion.setToolTip(new Label("Add Region..."));		
		addButton(addRegion);
		addRegion.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event) {
				AddRegionDialog dialog = new AddRegionDialog(Display.getCurrent().getActiveShell(), (XYRegionGraph)xyGraph);
				if(dialog.open() == Window.OK){
					((XYRegionGraph)xyGraph).addRegion(dialog.getRegion());
					((XYRegionGraph)xyGraph).getOperationsManager().addCommand(
							new AddRegionCommand((XYRegionGraph)xyGraph, dialog.getRegion()));
				}
			}
		});
		
		final Button removeRegion = new Button(Activator.getImage("icons/ProfileBox.png"));
		removeRegion.setToolTip(new Label("Remove Region..."));
		addButton(removeRegion);
		removeRegion.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event) {
				RemoveRegionDialog dialog = new RemoveRegionDialog(Display.getCurrent().getActiveShell(), (XYRegionGraph)xyGraph);
				if(dialog.open() == Window.OK && dialog.getRegion() != null){
					((XYRegionGraph)xyGraph).removeRegion(dialog.getRegion());
					xyGraph.getOperationsManager().addCommand(
							new RemoveRegionCommand((XYRegionGraph)xyGraph, dialog.getRegion()));					
				}
			}
		});
		
		addSeparator();	
	}

}
