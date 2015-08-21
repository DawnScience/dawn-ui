package org.dawnsci.mapping.ui.dialog;

import java.sql.DatabaseMetaData;
import java.util.Collection;
import java.util.Map;

import org.dawnsci.mapping.ui.MapPlotManager;
import org.dawnsci.mapping.ui.datamodel.MappedData;
import org.dawnsci.mapping.ui.datamodel.MappedDataBlock;
import org.dawnsci.mapping.ui.datamodel.MappedDataFile;
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
		transScale.setSelection(map.getTransparency());
		transScale.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				map.setTransparency(transScale.getSelection());
				manager.plotMap(map);
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
