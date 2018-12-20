package org.dawnsci.processing.ui.slice;

import org.dawnsci.common.widgets.dialog.FileSelectionDialog;
import org.eclipse.dawnsci.analysis.api.conversion.ProcessingOutputType;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class ExtendedFileSelectionDialog extends FileSelectionDialog {
		
		private boolean isHDF5 = false;
		private boolean canLoad = false;
		private boolean loadIntoDataVis = false;
		private ProcessingOutputType processingOutputType;
		
		public ExtendedFileSelectionDialog(Shell parentShell, boolean isH5, boolean canLoad, boolean loadDataVis,ProcessingOutputType processingOutputType) {
			super(parentShell);
			this.isHDF5 = isH5;
			this.canLoad = canLoad;
			this.loadIntoDataVis = loadDataVis;
			this.processingOutputType = processingOutputType != null ? processingOutputType : ProcessingOutputType.PROCESSING_ONLY;
		}
		
		@Override
		protected Control createDialogArea(Composite parent) {
			super.createDialogArea(parent);
			
			if (canLoad) {
				final Button load = new Button(parent, SWT.CHECK);
				load.setText("Automatically load data to DataVis perspective");
				load.setSelection(loadIntoDataVis);
				SelectionListener slload = new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
							loadIntoDataVis = load.getSelection();
						
					}
				};
				
				load.addSelectionListener(slload);
				
				if (isHDF5) {
					Label label = new Label(parent, SWT.HORIZONTAL | SWT.SEPARATOR);
					label.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
				}
			}
			
			if (isHDF5) {
				final Button button1 = new Button(parent, SWT.RADIO);
				final Button button2 = new Button(parent, SWT.RADIO);
				final Button button3 = new Button(parent, SWT.RADIO);
				button1.setText("Processed data only");
				button2.setText("Link original data (no data copied)");
				button3.setText("Process data into copy of original");

				switch (processingOutputType) {
				case PROCESSING_ONLY :{
					button1.setSelection(true);
					break;
				}
				case LINK_ORIGINAL :{
					button2.setSelection(true);
					break;
				}
				case ORIGINAL_AND_PROCESSED :{
					button3.setSelection(true);
					break;
				}
				default:
					button1.setSelection(true);
					processingOutputType = ProcessingOutputType.PROCESSING_ONLY;
				}

				SelectionListener l1 = new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						if (button1.getSelection()) {
							processingOutputType = ProcessingOutputType.PROCESSING_ONLY;
						} 
					}
				};

				button1.addSelectionListener(l1);

				SelectionListener l2 = new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						if (button2.getSelection()) {
							processingOutputType = ProcessingOutputType.LINK_ORIGINAL;
						} 
					}
				};

				button2.addSelectionListener(l2);

				SelectionListener l3 = new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						if (button3.getSelection()) {
							processingOutputType = ProcessingOutputType.ORIGINAL_AND_PROCESSED;
						} 
					}
				};

				button3.addSelectionListener(l3);
			}
			
			return parent;
		}
		
		public boolean isLoadIntoDataVis() {
			return loadIntoDataVis;
		}
		
		public ProcessingOutputType getProcessingOutputType() {
			return processingOutputType;
		}
		
		@Override
		  protected Point getInitialSize() {
		    return new Point(500, 250);
		  }
	}