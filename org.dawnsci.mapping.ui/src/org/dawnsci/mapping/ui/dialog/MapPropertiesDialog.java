package org.dawnsci.mapping.ui.dialog;

import java.util.List;

import org.dawnsci.mapping.ui.MapPlotManager;
import org.dawnsci.mapping.ui.datamodel.AbstractMapData;
import org.dawnsci.mapping.ui.datamodel.MappedDataArea;
import org.dawnsci.mapping.ui.datamodel.MappedDataBlock;
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
	
	private AbstractMapData map;
	private MappedDataArea area;
	private MapPlotManager manager;

	public MapPropertiesDialog(Shell parentShell, AbstractMapData map, MappedDataArea area, MapPlotManager manager) {
		super(parentShell);
//		setShellStyle(getShellStyle() | SWT.RESIZE);
		
		this.map = map;
		this.area = area;
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
		if (t == -1) t = 255;
		transScale.setSelection(t);
		transScale.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				int s = transScale.getSelection();
				if (s == 255) s = -1;
				//-1 is no transparency
				map.setTransparency(s);
				manager.setTransparency(map);
			}
		});
		
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
		
		
		 List<MappedDataBlock> suitableParents = area.findSuitableParentBlocks(map);
		String[] dataBlockNames = new String[suitableParents.size()];
		for (int i = 0; i < dataBlockNames.length; i++) dataBlockNames[i] = suitableParents.get(i).toString();
		comboViewer.setInput(suitableParents);
		for (int i = 0; i<suitableParents.size(); i++) {
			if(suitableParents.get(i).equals(map.getParent())) {
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
						map.setParent((MappedDataBlock)ob);
						combo.setToolTipText(((MappedDataBlock)ob).getPath());
					}
				}
			}
		});
		
		
		if (map instanceof ReMappedData) {
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
					manager.updatePlot();
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
					manager.updatePlot();
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
