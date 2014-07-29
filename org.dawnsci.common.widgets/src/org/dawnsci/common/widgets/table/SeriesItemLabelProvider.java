package org.dawnsci.common.widgets.table;

import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

/**
 * This class may be extended to provide custom rendering.
 * @author fcp94556
 *
 */
public class SeriesItemLabelProvider extends BaseLabelProvider implements IStyledLabelProvider {


	private IStyledLabelProvider delegate;

	public SeriesItemLabelProvider(IStyledLabelProvider delegate) {
		this.delegate = delegate;
	}

	@Override
	public Image getImage(Object element) {
		final Image dele  = delegate.getImage(element);
		if (dele!=null) return dele;
		
		if (element == ISeriesItemDescriptor.NEW) {
			return PlatformUI.getWorkbench().getSharedImages()
					.getImage(ISharedImages.IMG_TOOL_NEW_WIZARD);

		}
		return null;
	}

	@Override
	public StyledString getStyledText(Object element) {
		
		final StyledString dele  = delegate.getStyledText(element);
		if (dele!=null) return dele;

		return new StyledString(((ISeriesItemDescriptor)element).getName());
	}

}
