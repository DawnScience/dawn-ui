package org.dawnsci.processing.ui;

import java.util.ArrayList;
import java.util.Collection;

import org.dawnsci.common.widgets.table.ISeriesItemDescriptor;
import org.dawnsci.common.widgets.table.ISeriesItemDescriptorProvider;
import org.dawnsci.common.widgets.table.SeriesTable;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.processing.IOperationService;

/**
 * A view for constructing and executing a processing pipeline.
 * 
 * @author fcp94556
 *
 */
public class ProcessingView extends ViewPart {
	
	private static final Logger logger = LoggerFactory.getLogger(ProcessingView.class);
	
	private SeriesTable seriesTable;
	private IOperationService service;

	public ProcessingView() {
		this.seriesTable = new SeriesTable();
		this.service     = (IOperationService)Activator.getService(IOperationService.class);
	}

	@Override
	public void createPartControl(Composite parent) {
		
		seriesTable.createControl(parent, new LabelProvider());
		seriesTable.setInput(null/** TODO Save in memento last list?**/, createSeriesProvider());
	}

	@Override
	public void setFocus() {
		seriesTable.setFocus();
	}
	
	private ISeriesItemDescriptorProvider createSeriesProvider() {
		return new ISeriesItemDescriptorProvider() {

			@Override
			public Collection<ISeriesItemDescriptor> getDescriptors(ISeriesItemDescriptor itemDescriptor) {
				// TODO make descriptors for relative case
				try {
					final Collection<String>                ops = service.getRegisteredOperations();
					final Collection<ISeriesItemDescriptor> ret = new ArrayList<ISeriesItemDescriptor>(7);
					
					for (String id : ops) {
						// TODO We actually create an operation for the descriptor, not ideal.
						ret.add(new OperationDescriptor(id, service));
					}
					return ret;
					
				} catch (Exception e) {
					logger.error("Cannot get operations!", e);
					return null;
				}
			}
			
		};
	}

	private class LabelProvider extends ColumnLabelProvider implements IStyledLabelProvider {

		@Override
		public StyledString getStyledText(Object element) {
			return null;
		}
		public Image getImage(Object element) {
			return null; // TODO Icon for operation.
		}

	}
}
