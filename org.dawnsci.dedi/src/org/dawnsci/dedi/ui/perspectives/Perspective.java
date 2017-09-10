package org.dawnsci.dedi.ui.perspectives;

import org.dawnsci.dedi.ui.views.configuration.ConfigurationView;
import org.dawnsci.dedi.ui.views.plot.BeamlineConfigurationPlotView;
import org.dawnsci.dedi.ui.views.results.ResultsView;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;


public class Perspective implements IPerspectiveFactory {

	@Override
	public void createInitialLayout(IPageLayout layout) { 
		 String editorArea = layout.getEditorArea();
         layout.setEditorAreaVisible(false);
         
         IFolderLayout topLeft = layout.createFolder("topLeft", IPageLayout.LEFT, 0.30f,
     		    editorArea);
         
         layout.addView(BeamlineConfigurationPlotView.ID, IPageLayout.TOP, 0.7f, editorArea);
         
		 topLeft.addView(ConfigurationView.ID);
		 layout.getViewLayout(ConfigurationView.ID).setCloseable(false);
		 topLeft.addPlaceholder(IPageLayout.ID_BOOKMARKS);
		 
		 layout.addView(ResultsView.ID, IPageLayout.BOTTOM, 0.05f, editorArea);
	}

}
