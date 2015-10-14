package org.dawnsci.mapping.ui.dialog;

import java.util.Map;

import org.dawnsci.mapping.ui.MapPlotManager;
import org.dawnsci.mapping.ui.datamodel.MappedData;
import org.dawnsci.mapping.ui.datamodel.MappedDataBlock;
import org.dawnsci.mapping.ui.datamodel.MappedDataFile;
import org.dawnsci.mapping.ui.datamodel.ReMappedData;
import org.eclipse.jface.dialogs.Dialog;
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
	
	private MappedData map;
	private MappedDataFile file;
	private MapPlotManager manager;

	public MapPropertiesDialog(Shell parentShell, MappedData map, MappedDataFile file, MapPlotManager manager) {
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		
		this.map = map;
		this.file = file;
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
		transScale.setMinimum(0);
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
		final Map<String, MappedDataBlock> dataBlocks = file.getDataBlockMap();
		String[] dataBlockNames = dataBlocks.keySet().toArray(new String[dataBlocks.size()]);
		combo.setItems(dataBlockNames);
		for (int i = 0; i<dataBlockNames.length; i++) {
			if(dataBlockNames[i].equals(map.getParent().toString())) {
				combo.select(i);
				break;
			}
		}
		
		combo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				map.setParent(dataBlocks.get(combo.getText()));
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
					manager.plotMap(rm);
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
					manager.plotMap(rm);
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
}
