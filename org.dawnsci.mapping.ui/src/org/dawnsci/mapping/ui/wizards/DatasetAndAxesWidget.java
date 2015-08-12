package org.dawnsci.mapping.ui.wizards;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class DatasetAndAxesWidget {

	private CheckboxTableViewer cviewer;
	private Map<String,int[]> datasetNames;
	private Map<String, Dimension[]> nameToDimensions;
	private DataConfigurationTable dataTable;
	private static final String[] OPTIONS = new String[]{"map Y", "map X",""};
	
	public void createControl(Composite parent) {
		Composite main = new Composite(parent, SWT.None);
		main.setLayout(new GridLayout(2,false));
//		CCombo cc = new CCombo(main, SWT.READ_ONLY);
		cviewer = CheckboxTableViewer.newCheckList(main, SWT.BORDER);
		cviewer.setContentProvider(new BasicContentProvider());
		cviewer.setLabelProvider(new ViewLabelProvider());
		cviewer.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
		dataTable = new DataConfigurationTable();
		dataTable.createControl(main);
		dataTable.setLayout(new GridData(GridData.FILL_BOTH));
		nameToDimensions = new HashMap<String, Dimension[]>();
		
		cviewer.addSelectionChangedListener(new ISelectionChangedListener() {
			
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				
				Object element = ((StructuredSelection)event.getSelection()).getFirstElement();
				Entry<String,int[]> entry = (Entry<String,int[]>)element;
				String key = entry.getKey();
				if (!cviewer.getChecked(element)) {
					if (nameToDimensions.containsKey(key)) nameToDimensions.remove(key);
					dataTable.clearAll();
					return;
				}

				int[] shape = datasetNames.get(key);
				Dimension[] dims;
				if (nameToDimensions.containsKey(key)) dims = nameToDimensions.get(key);
				else dims = new Dimension[shape.length];
				nameToDimensions.put(key, dims);
				dataTable.setInput(shape, OPTIONS, getAxesNameOptions(shape,key),dims);
				
			}
		});
	}
	
	public Map<String,String[]> getAxesMaps() {
		Map<String,String[]> out = new HashMap<String, String[]>();
		for (Entry<String, Dimension[]> entry : nameToDimensions.entrySet()) {
			Dimension[] dim = entry.getValue();
			String[] axes = new String[dim.length];
			for (int i = 0; i < dim.length ; i++) {
				axes[i] = dim[i].getAxis();
			}
			out.put(entry.getKey(), axes);
			
		}
		
		return out;
	}
	
	public String[] getMapXAndYAxesNames() {
		String[] out = new String[2];
		if (nameToDimensions.isEmpty()) return out;
		Entry<String, Dimension[]> next = nameToDimensions.entrySet().iterator().next();
		for (Dimension d : next.getValue()) {
			if (OPTIONS[0].equals(d.getDescription())) out[1] = d.getAxis();
			if (OPTIONS[1].equals(d.getDescription())) out[0] = d.getAxis();
		}
		
		return out;
		
	}
	
	public void setDatasetMaps(Map<String,int[]> datasetNames, Map<String,int[]> nexusDatasetNames) {
		this.datasetNames = datasetNames;
		if (nexusDatasetNames.isEmpty()) {
			cviewer.setInput(datasetNames);
		} else {
			cviewer.setInput(nexusDatasetNames);
		}
	}
	
	public Control getControl() {
		return cviewer.getTable().getParent();
	}
	
	private String[][] getAxesNameOptions(int[] shape, String key) {
		
		String[][] names = new String[shape.length][];
		AxisSuggestions as = new AxisSuggestions(shape.length);
		
		for (int i = 0; i < shape.length; i++) {
			int size = shape[i];
			for (Entry<String, int[]> entry : datasetNames.entrySet()){
				String k = entry.getKey();
				int[] s = entry.getValue();
				if (!key.equals(k) && contains(size, s)) as.addAxis(i, k);
			}
			names[i] = as.getSuggestions(i);
		}
		
		return names;
	}
	
	private boolean contains(int val, int[] vals) {
		for (int i : vals) if (i==val) return true;
		return false;
	}
	
	private class BasicContentProvider implements IStructuredContentProvider {

		@Override
		public Object[] getElements(Object inputElement) {
			Map<String, int[]> vals = (Map<String, int[]>)inputElement;
			Set<Entry<String, int[]>> entrySet = vals.entrySet();
			
			return entrySet.toArray();
		}

		@Override
		public void dispose() {
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}
	
	private class ViewLabelProvider extends ColumnLabelProvider {
	
		@Override
		public String getText(Object obj) {
			Entry<String, int[]> ent = (Entry<String, int[]>)obj;
			String name = ent.getKey();
			return name + " " + Arrays.toString(ent.getValue());
		}
		
		@Override
		public String getToolTipText(Object obj) {
			return "";
		}
		
	}
	
}
