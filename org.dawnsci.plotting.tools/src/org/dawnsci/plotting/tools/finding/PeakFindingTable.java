package org.dawnsci.plotting.tools.finding;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.january.dataset.IDataset;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

import uk.ac.diamond.scisoft.analysis.peakfinding.Peak;

/**
 * @author Dean P. Ottewell
 */
public class PeakFindingTable {
	
	public TableViewer viewer;
	
	private PeakFindingManager controller;

	public PeakFindingTable(PeakFindingManager controller){
			this.controller = controller;
	}
	
	public void createTableControl(Composite parent) {

		viewer = new TableViewer(parent , SWT.FULL_SELECTION | SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		createPeakDataColumns(viewer);

		viewer.getTable().setLinesVisible(true);
		viewer.getTable().setHeaderVisible(true);
		viewer.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
		viewer.setContentProvider(new ArrayContentProvider());
		
		/*Table Content Manipulation*/
		viewer.getControl().addKeyListener( new org.eclipse.swt.events.KeyAdapter() {
		      public void keyPressed(KeyEvent e) {
		          if (e.keyCode == SWT.DEL){
		        	  //Delete the peak the refresh should fix itself
		        	  //TODO:These index should line up because of how they are populated... If a comparator is set we are doomed.
		        	  int idx = viewer.getTable().getSelectionIndex();
		        	  
		        	  //TODO: controller.updatePeakTrace(controller.peaks); 
		        	  //TODO: UPDATE PEAKS CONTROLLER FUNCTION CHANGE. EVERYONE IS LISTENING FOR THIS CHANGE
		        	  List<Peak> peaks = (List<Peak>) viewer.getInput();
		        	  peaks.remove(idx);
		        	  controller.setPeaks(peaks);
		        	  	
		        	  viewer.refresh();
		          }
		      }
		});
			
		viewer.refresh();
		
		controller.addPeakListener(new IPeakOpportunityListener() {
			@Override
			public void peaksChanged(PeakOpportunityEvent evt) {
				viewer.setInput(evt.getPeaks());
			}

			@Override
			public void boundsChanged(double upper, double lower) {
				// TODO Auto-generated method stub
			}

			@Override
			public void dataChanged(IDataset nXData, IDataset nYData) {
				// TODO Auto-generated method stub
				
			}


		});
	
	}
	
	private List<TableViewerColumn> createPeakDataColumns(final TableViewer viewer) {

		List<TableViewerColumn> ret = new ArrayList<TableViewerColumn>(2);

		// TODO: different selectiosnneed to trigger certain events..
		// TODO: lablerprivuider for peaks ans might want to know algorithm or more data
		// TODO: selection listener table.getColumn().addSelectionListener();
		TableViewerColumn table = new TableViewerColumn(viewer, SWT.NONE, 0);
		table.getColumn().setText("x");
		table.getColumn().setAlignment(SWT.CENTER);
		table.getColumn().setWidth(200);
		table.getColumn().setResizable(false);
		table.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				Peak peakNo = (Peak) element;
				return peakNo.getX().toString();
			}
		});
		ret.add(table);

		table = new TableViewerColumn(viewer, SWT.NONE, 1);
		table.getColumn().setText("y");
		table.getColumn().setAlignment(SWT.CENTER);
		table.getColumn().setWidth(200);
		table.getColumn().setResizable(false);
		table.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				Peak peakNo = (Peak) element;
				return peakNo.getY().toString();
			}
		});
		
		ret.add(table);
		
		/* Pack the columns */
	    for (TableViewerColumn column : ret)
	        column.getColumn().setWidth(200);
	    
		return ret;
	}

}
