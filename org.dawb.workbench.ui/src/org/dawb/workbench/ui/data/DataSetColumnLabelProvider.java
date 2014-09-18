package org.dawb.workbench.ui.data;

import java.util.Arrays;

import org.dawb.common.services.IExpressionObject;
import org.dawb.workbench.ui.Activator;
import org.dawnsci.plotting.AbstractPlottingSystem;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.slicing.api.data.ITransferableDataObject;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PlatformUI;

import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;
import uk.ac.diamond.scisoft.analysis.io.IMetadata;
import uk.ac.diamond.scisoft.analysis.monitor.IMonitor;

/**
 * Label provider for PlotDataComponent
 * @author fcp94556
 *
 */
class DataSetColumnLabelProvider extends ColumnLabelProvider implements IStyledLabelProvider {
	
	private Color RED   = PlatformUI.getWorkbench().getDisplay().getSystemColor(SWT.COLOR_RED);
	private Color BLUE  = PlatformUI.getWorkbench().getDisplay().getSystemColor(SWT.COLOR_BLUE);
	private Color BLACK = PlatformUI.getWorkbench().getDisplay().getSystemColor(SWT.COLOR_BLACK);
	private Image checkedIcon;
	private Image uncheckedIcon;
	private Image filteredIcon;
	
	private int columnIndex;
	private PlotDataComponent component;
	public DataSetColumnLabelProvider(int columnIndex, PlotDataComponent component) {
		
		this.columnIndex = columnIndex;
		this.component   = component;
		
		if (columnIndex == 0) {
			ImageDescriptor id = Activator.getImageDescriptor("icons/ticked.png");
			checkedIcon   = id.createImage();
			id = Activator.getImageDescriptor("icons/unticked.gif");
			uncheckedIcon =  id.createImage();
		}
		
		if (columnIndex == 1) {
			ImageDescriptor id = Activator.getImageDescriptor("icons/filter.png");
			filteredIcon = id.createImage();
		}
	}
	
	public void dispose() {
		if (checkedIcon!=null)   checkedIcon.dispose();
		if (uncheckedIcon!=null) uncheckedIcon.dispose();
		if (filteredIcon!=null)  filteredIcon.dispose();
		checkedIcon=null;
		uncheckedIcon=null;
		filteredIcon=null;
	}
	
	public Image getImage(Object ob) {
		
		final ITransferableDataObject element = (ITransferableDataObject)ob;
		if (columnIndex==0) {
		    return element.isChecked() ? checkedIcon : uncheckedIcon;
		} if (columnIndex==1) {
			if (element.getFilterPath()!=null) {
				return filteredIcon;
			}
		}
		
		return null;
	}
	

	@Override
	public StyledString getStyledText(Object element) {
		final String text = getText(element);
		if (text==null) return null;
		
		StyledString ret = new StyledString(text);
		
		final ITransferableDataObject ob = (ITransferableDataObject)element;
		if (ob.getFilterPath()!=null) {
			String name = ob.getFilterPath().substring(ob.getFilterPath().lastIndexOf('/')+1);
			ret.append(new StyledString("   ["+name+"]", StyledString.QUALIFIER_STYLER));
		}
		return ret;
	}
	
	@Override
	public String getText(Object ob) {
		
		final ITransferableDataObject element = (ITransferableDataObject)ob;
		
		// Get shape (should be fast as does not evaluate expressions).
		final int[] shape  = element.getShape(false);
		
		switch (columnIndex) {
		case 0:
			return null;
		case 1:
			return element.getDisplayName(component.getRootName());
		case 2:
			final IPlottingSystem system = component.getPlottingSystem();
			return element.getAxis(component.getSelections(), system.is2D(), ((AbstractPlottingSystem)system).isXFirst());

		case 3:
			if (shape!=null) {
				int size = 1;
				for (int i : shape) size*=i;
				return size+"";
			}
		    return "";

		case 4:
			if (shape!=null) {
				return shape.length+"";
			}
		    return "";
		
		case 5:
			if (shape!=null) {
				return Arrays.toString(shape);
			}
		    return "";

		case 6:
			return element.getVariable();
		default:
			return element.toString();
		}
	}
    
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IColorProvider#getForeground(java.lang.Object)
	 */
	public Color getForeground(Object ob) {
		
		final ITransferableDataObject element = (ITransferableDataObject)ob;
    		    	
		switch (columnIndex) {
		case 1:
			if (element.isExpression()) {
				final IExpressionObject o = element.getExpression();
				return o.isValid(new IMonitor.Stub()) ? BLUE : RED;
			} else if (element.isChecked()) {
				return component.get1DPlotColor(element);
			}
			return BLACK;
		case 6:
			return BLUE;
		default:
			return BLACK;
		}
    }
}
