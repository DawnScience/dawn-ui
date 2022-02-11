package org.dawnsci.mapping.ui;

import org.dawnsci.mapping.ui.datamodel.AbstractMapData;
import org.dawnsci.mapping.ui.datamodel.AssociatedImage;
import org.dawnsci.mapping.ui.datamodel.AssociatedImageStack;
import org.dawnsci.mapping.ui.datamodel.LiveStreamMapObject;
import org.dawnsci.mapping.ui.datamodel.MappedDataBlock;
import org.dawnsci.mapping.ui.datamodel.MappedDataFile;
import org.dawnsci.mapping.ui.datamodel.PlottableMapObject;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

public class MapFileCellLabelProvider extends StyledCellLabelProvider {

	private final Image block = Activator.getImage("icons/layers-stack.png");
	private final Image map = Activator.getImage("icons/map.png");
	private final Image image = Activator.getImage("icons/image-resize-actual.png");
	private final Image file = Activator.getImage("icons/map-file.png");
	private final Image remote= Activator.getImage("icons/remote-file.png");
	private final Image camera= Activator.getImage("icons/camera-black.png");
	private final Image plotted= Activator.getImage("icons/file-plotted.png");
	private final Image remoteplotted= Activator.getImage("icons/live-plotted.png");
	private final Image stack = Activator.getImage("icons/stack.png");
	private Font italicFont = null;
	private Font initialFont = null;
	
	
	@Override
    public void update(ViewerCell cell) {

      Object element = cell.getElement();
      StyledString text = new StyledString();
      text.append(element instanceof LiveStreamMapObject ? ((LiveStreamMapObject)element).getLongName() : element.toString());
      
      if (element instanceof MappedDataBlock) {
    	  cell.setImage(block);
      } else if (element instanceof AbstractMapData) {
    	  cell.setImage(map);
      } else if (element instanceof AssociatedImageStack) {
        	  cell.setImage(stack);
      } else if (element instanceof AssociatedImage) {
    	  cell.setImage(image);
      } else if (element instanceof LiveStreamMapObject) {
        	  cell.setImage(camera);
      } else if (element instanceof MappedDataFile){
    	  
    	  boolean isLive = ((MappedDataFile)element).getLiveDataBean() != null;
    	  boolean hasPlotted = ((MappedDataFile) element).hasDataPlotted();
    	  
    	  if (isLive && hasPlotted) {
    		  cell.setImage(remoteplotted);
    	  } else if (isLive) {
    		  cell.setImage(remote);
    	  } else if (hasPlotted) {
    		  cell.setImage(plotted);
    	  } else {
    		  cell.setImage(file);
    	  }
      }

      cell.setText(text.toString());
      if (italicFont == null) {
    	  try {
    		  initialFont = cell.getFont();  
    		  final FontData shellFd = Display.getDefault().getActiveShell().getFont().getFontData()[0];
    		  FontData fd      = new FontData(shellFd.getName(), shellFd.getHeight(), SWT.ITALIC | SWT.BOLD);
    		  italicFont = new Font(null, fd);
    	  } catch (Exception e) {
    		  //TODO log
    	  }
    	
      }
      
      if ( element instanceof PlottableMapObject && ((PlottableMapObject)element).isPlotted()) cell.setFont(italicFont);
      else cell.setFont(initialFont);
      super.update(cell);

    }
	
	@Override
	public String getToolTipText(final Object element) {
		if (element instanceof MappedDataFile) {
			return ((MappedDataFile)element).getPath();
		} else if (element instanceof AbstractMapData) {
			MappedDataBlock parent = ((AbstractMapData)element).getParent();
			if (parent != null && parent.getLongName() != null) {
				return "Parent : "+ parent.getLongName();
			}
		}
		return null;
	}
	
	@Override
	public void dispose() {
		block.dispose();
		image.dispose();
		map.dispose();
		file.dispose();
		remote.dispose();
		camera.dispose();
		plotted.dispose();
		remoteplotted.dispose();
		stack.dispose();
		if (italicFont != null) italicFont.dispose();
		super.dispose();
	}
	
}
