package org.dawnsci.datavis.view.parts;

import org.dawnsci.datavis.model.IDataObject;
import org.dawnsci.datavis.model.LoadedFile;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.ViewerCell;

public class FileTreeLabelProvider extends StyledCellLabelProvider {

	@Override
    public void update(ViewerCell cell) {
      Object element = cell.getElement();
      StyledString text = new StyledString();
      text.append(((IDataObject)element).getName());
      cell.setText(text.toString());
      super.update(cell);
	}
	
	@Override
	public String getToolTipText(Object element) {
		if (element instanceof LoadedFile) return ((LoadedFile)element).getLongName();
		return null;
		
	};
	
}
