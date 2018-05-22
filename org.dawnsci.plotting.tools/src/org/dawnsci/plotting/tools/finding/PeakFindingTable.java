package org.dawnsci.plotting.tools.finding;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.january.dataset.Dataset;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

import uk.ac.diamond.scisoft.analysis.fitting.functions.IdentifiedPeak;

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
		
		viewer = new TableViewer(parent , SWT.FULL_SELECTION | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		createPeakDataColumns(viewer);

		viewer.getTable().setLinesVisible(true);
		viewer.getTable().setHeaderVisible(true);
		viewer.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
		viewer.setContentProvider(new ArrayContentProvider());
		/*Table Content Manipulation*/
		viewer.getControl().addKeyListener( new org.eclipse.swt.events.KeyAdapter() {
		      public void keyPressed(KeyEvent e) {
		          if (e.keyCode == SWT.DEL){
		        	  //XXX:These index should line up because of how they are populated... If a comparator is set we are doomed.
		        	  int[] selections = viewer.getTable().getSelectionIndices();
		        	  for (int i : selections){    
			        	  List<IdentifiedPeak> peaks = (List<IdentifiedPeak>) viewer.getInput();
			        	  peaks.remove(i);
			        	  controller.setPeaksId(peaks);
			        	  viewer.refresh();
		        	  }
		        	  
		          }
		      }
		});
		
		controller.addPeakListener(new IPeakOpportunityListener() {
			@Override
			public void peaksChanged(PeakOpportunityEvent evt) {
				List<IdentifiedPeak> peaksId = evt.getPeakOpp().getPeaksId();
				if(peaksId != null){
					viewer.setInput(peaksId);
					viewer.refresh();
				}
			}

			@Override
			public void boundsChanged(double upper, double lower) {
				// TODO Auto-generated method stub
			}

			@Override
			public void dataChanged(Dataset nXData, Dataset nYData) {
				// TODO Auto-generated method stub
			}

			@Override
			public void isPeakFinding() {
				// TODO Auto-generated method stub
			}

			@Override
			public void finishedPeakFinding() {
				// TODO Auto-generated method stub
			}

			@Override
			public void activateSearchRegion() {
				// TODO Auto-generated method stub
			}

		});
	
	}
	
	private List<TableViewerColumn> createPeakDataColumns(final TableViewer viewer) {
		List<TableViewerColumn> ret = new ArrayList<TableViewerColumn>(2);

		// TODO: label provider for peaks and might want to know algorithm or more data
		// TODO: selection listener table.getColumn().addSelectionListener();
		TableViewerColumn table = new TableViewerColumn(viewer, SWT.FILL, 0);
		table.getColumn().setText("x");
		table.getColumn().setAlignment(SWT.CENTER);
		table.getColumn().setResizable(true);
		table.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				//Format Identified peaks
				IdentifiedPeak peak = (IdentifiedPeak) element;
				return String.format("%.2f", peak.getPos());
			}
		});
		ret.add(table);

		table = new TableViewerColumn(viewer, SWT.FILL, 1);
		table.getColumn().setText("y");
		table.getColumn().setAlignment(SWT.CENTER);
		table.getColumn().setResizable(true);
		table.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				IdentifiedPeak peak = (IdentifiedPeak) element;
				return String.format("%.2f", peak.getHeight());
			}
		});
		
		ret.add(table);

		/* Pack the columns */
		for (TableViewerColumn column : ret) {
			column.getColumn().setWidth(200);
		}

		return ret;
	}

}
