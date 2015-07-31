package org.dawnsci.mapping.ui;

import org.dawnsci.mapping.ui.datamodel.AssociatedImage;
import org.dawnsci.mapping.ui.datamodel.MappedData;
import org.dawnsci.mapping.ui.datamodel.MappedDataBlock;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.graphics.Image;

public class MapFileCellLabelProvider extends StyledCellLabelProvider {

	private final Image block = Activator.getImage("icons/layers-stack.png");
	private final Image map = Activator.getImage("icons/map.png");
	private final Image image = Activator.getImage("icons/image-resize-actual.png");

	@Override
    public void update(ViewerCell cell) {

      Object element = cell.getElement();
      StyledString text = new StyledString();
      text.append(element.toString());
      
      if (element instanceof MappedDataBlock) {
    	  cell.setImage(block);
      } else if (element instanceof MappedData) {
    	  cell.setImage(map);
      } else if (element instanceof AssociatedImage) {
    	  cell.setImage(image);
      }

      cell.setText(text.toString());
      cell.setStyleRanges(text.getStyleRanges());
      super.update(cell);

    }
	
	@Override
	public void dispose() {
		block.dispose();
		image.dispose();
		map.dispose();
		super.dispose();
	}
	
}
