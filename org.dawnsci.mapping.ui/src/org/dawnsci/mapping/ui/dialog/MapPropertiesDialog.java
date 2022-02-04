package org.dawnsci.mapping.ui.dialog;

import java.util.List;

import org.dawnsci.mapping.ui.api.IMapFileController;
import org.dawnsci.mapping.ui.datamodel.AbstractMapData;
import org.dawnsci.mapping.ui.datamodel.AssociatedImageStack;
import org.dawnsci.mapping.ui.datamodel.IMapPlotController;
import org.dawnsci.mapping.ui.datamodel.MapScanDimensions;
import org.dawnsci.mapping.ui.datamodel.MappedDataBlock;
import org.dawnsci.mapping.ui.datamodel.PlottableMapObject;
import org.dawnsci.mapping.ui.datamodel.ReMappedData;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;

public class MapPropertiesDialog extends Dialog {
	
	private PlottableMapObject map;
	private IMapPlotController manager;
	private IMapFileController fileManager;

	public MapPropertiesDialog(Shell parentShell, PlottableMapObject map, IMapPlotController manager, IMapFileController fileController) {
		super(parentShell);
		this.fileManager = fileController;
		this.map = map;
		this.manager = manager;
	}
	
	@Override
	public Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		container.setLayoutData(new GridData(GridData.FILL_BOTH));
		container.setLayout(new GridLayout(2, false));
		
		Label transLabel = new Label(container, SWT.NONE);
		transLabel.setText("Transparency");
		
		final Scale transScale = new Scale(container, SWT.NONE);
		transScale.setMaximum(255);
		transScale.setMinimum(15);
		transScale.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		int t = map.getTransparency();
		transScale.setSelection(t);
		transScale.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				int s = transScale.getSelection();
				map.setTransparency(s);
				manager.setTransparency(map);
			}
		});
		
		if (map instanceof AbstractMapData) {
			
			AbstractMapData amd = (AbstractMapData)map;

			Label parentLabel = new Label(container, SWT.NONE);
			parentLabel.setText("Root data");

			final Combo combo = new Combo(container,SWT.READ_ONLY);
			final ComboViewer comboViewer = new ComboViewer(combo);
			comboViewer.setContentProvider(new ArrayContentProvider());
			comboViewer.setLabelProvider(new ColumnLabelProvider() {
				@Override
				public String getText(Object obj) {
					return obj.toString();
				}

				@Override
				public String getToolTipText(Object obj) {
					if (obj instanceof MappedDataBlock) {
						return ((MappedDataBlock)obj).getPath();
					}
					return "";
				}
			});


			List<MappedDataBlock> suitableParents = fileManager.getArea().findSuitableParentBlocks(amd);
			String[] dataBlockNames = new String[suitableParents.size()];
			for (int i = 0; i < dataBlockNames.length; i++) dataBlockNames[i] = suitableParents.get(i).toString();
			comboViewer.setInput(suitableParents);
			for (int i = 0; i<suitableParents.size(); i++) {
				if(suitableParents.get(i).equals(amd.getParent())) {
					comboViewer.getCombo().select(i);
					combo.setToolTipText(suitableParents.get(i).getPath());
					break;
				}
			}

			comboViewer.addSelectionChangedListener( new ISelectionChangedListener() {

				@Override
				public void selectionChanged(SelectionChangedEvent event) {
					ISelection s = event.getSelection();
					if (s instanceof StructuredSelection) {
						StructuredSelection ss = (StructuredSelection)s;
						Object ob = ss.getFirstElement();
						if (ob instanceof MappedDataBlock){
							amd.setParent((MappedDataBlock)ob);
							combo.setToolTipText(((MappedDataBlock)ob).getPath());
						}
					}
				}
			});
		}
		
		
		//Remapped data of size 1 is not remapped, just shown as a point
		// so changing the shape makes no sense
		if (map instanceof ReMappedData && ((ReMappedData)map).getData().getSize() != 1) {
			final ReMappedData rm = (ReMappedData)map;
			final int[] shape = rm.getShape();

			Label xLabel = new Label(container, SWT.NONE);
			xLabel.setText("X shape");
			final Spinner xspin = new Spinner(container, SWT.NONE);
			xspin.setMinimum(1);
			xspin.setMaximum(Integer.MAX_VALUE);
			xspin.setSelection(shape[0]);
			xspin.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					shape[0] = xspin.getSelection();
					rm.setShape(shape);
					fileManager.registerUpdates(null);
				}
			});

			Label yLabel = new Label(container, SWT.NONE);
			yLabel.setText("Y shape");
			final Spinner yspin = new Spinner(container, SWT.NONE);
			yspin.setMinimum(1);
			yspin.setMaximum(Integer.MAX_VALUE);
			yspin.setSelection(shape[1]);
			yspin.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					shape[1] = yspin.getSelection();
					rm.setShape(shape);
					fileManager.registerUpdates(null);
				}
			});
			
		}
		
		//For now only consider scans with 1 extra dimension
		//i.e. xanes or tomography maps
		//Higher dimensions currently not nicely supported by the core code
		
		if (map instanceof AbstractMapData) {
			AbstractMapData amd = (AbstractMapData)map;
			final MapScanDimensions mapDims = amd.getParent().getMapDims();

			int[] nonXYScanDimensions = mapDims.getNonXYScanDimensions();

			if (nonXYScanDimensions != null) {
				Label sliceLabel = new Label(container, SWT.NONE);
				sliceLabel.setText("Slice");

				int i = amd.getData().getShape()[nonXYScanDimensions[0]];

				int current = mapDims.getNonXYScanSlice(nonXYScanDimensions[0]);

				current = current == -1 ? 0 : current;
				int max = i -1;

				final Scale slice = new Scale(container, SWT.NONE);
				slice.setMaximum(max);
				slice.setMinimum(0);
				slice.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
				slice.setSelection(current);
				slice.addSelectionListener(new SelectionAdapter() {

					@Override
					public void widgetSelected(SelectionEvent e) {

						int s = slice.getSelection();

						//grow with scan when slider at end
						mapDims.setNonScanLocked(!(s == max));

						mapDims.updateNonXYScanSlice(nonXYScanDimensions[0], slice.getSelection());
						amd.clearCachedMap();
						fileManager.registerUpdates(null);
					}
				});
			}
		} else if (map instanceof AssociatedImageStack) {
			AssociatedImageStack ais = (AssociatedImageStack)map;
			
			Label sliceLabel = new Label(container, SWT.NONE);
			sliceLabel.setText("Slice");

			int current = ais.getCurrentImageNumber();

			current = current == -1 ? 0 : current;
			int max = ais.getNImages();

			final Scale slice = new Scale(container, SWT.NONE);
			slice.setMaximum(max);
			slice.setMinimum(0);
			slice.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			slice.setSelection(current);
			slice.addSelectionListener(new SelectionAdapter() {

				@Override
				public void widgetSelected(SelectionEvent e) {

					int s = slice.getSelection();

					ais.setCurrentImageNumber(s);
					fileManager.registerUpdates(null);
				}
			});
		}
		
		return container;
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Map Properties");
	}

	@Override
	protected Point getInitialSize() {
		return new Point(400, 300);
	}
	
	@Override
	  protected boolean isResizable() {
	    return true;
	  }
}
