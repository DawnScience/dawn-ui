package org.dawnsci.mapping.ui.wizards;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.dawnsci.mapping.ui.datamodel.MappedBlockBean;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class DatasetAndAxesWidget {

	private CheckboxTableViewer cviewer;
	private Map<String,int[]> datasetNames;
	private Map<String,int[]> nexusDatasetNames;
	private Map<String, Dimension[]> nameToDimensions;
	private List<MappedBlockBean> beans;
	private DataConfigurationTable dataTable;
	private Button remappable;
	private Combo remapXAxis;
	private static final String[] OPTIONS = new String[]{"map Y", "map X",""};
	private boolean reMap = false;
	private PropertyChangeListener listener;
	private HashSet<IDataWidgetCompleteListener> listeners;
	
	public DatasetAndAxesWidget() {
		this.listeners = new HashSet<IDataWidgetCompleteListener>();
		listener = new PropertyChangeListener() {
			
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				System.err.println(evt.toString());
				updateBeans();
				
			}

		};
	}
	
	public void addCompleteListener(IDataWidgetCompleteListener listener) {
		listeners.add(listener);
	}
	
	public void setBeanList(List<MappedBlockBean> beans) {
		this.beans = beans;
		updateUIFromBeans();
	}

	public void removeCompleteListener(IDataWidgetCompleteListener listener) {
		listeners.remove(listener);
	}
	
	public void fireCompleteListeners(boolean complete) {
		for(IDataWidgetCompleteListener l : listeners) l.dataComplete(complete);
	}
	
	public void createControl(Composite parent) {
		Composite main = new Composite(parent, SWT.None);
		main.setLayout(new GridLayout(2,false));
		main.setLayoutData(new GridData(GridData.FILL_BOTH));
		cviewer = CheckboxTableViewer.newCheckList(main, SWT.BORDER);
		cviewer.setContentProvider(new BasicContentProvider());
		cviewer.setLabelProvider(new ViewLabelProvider());
		cviewer.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
		dataTable = new DataConfigurationTable();
		dataTable.createControl(main);
		dataTable.setLayout(new GridData(GridData.FILL_BOTH));
		
		
		cviewer.addCheckStateListener(new ICheckStateListener() {
			
			@Override
			public void checkStateChanged(CheckStateChangedEvent event) {

				Entry<String,int[]> entry = (Entry<String,int[]>)event.getElement();
				String key = entry.getKey();
				if (!event.getChecked()) {
					if (nameToDimensions.containsKey(key)) nameToDimensions.remove(key);
					Iterator<MappedBlockBean> it = beans.iterator();
					while (it.hasNext()) {
						if (it.next().getName().equals(key)) it.remove();
					}
					dataTable.clearAll();
					updateBeans();
					return;
				}

				int[] shape = datasetNames.get(key);
				
				String[][] axes = getAxesNameOptions(shape,key);
				
				
				Dimension[] dims;
				if (nameToDimensions.containsKey(key)) dims = nameToDimensions.get(key);
				else {
					dims = new Dimension[shape.length];
					for (int i = 0; i < dims.length ; i++) {
						if (dims[i] != null) continue;
						dims[i] = new Dimension(i);
						dims[i].setSize(shape[i]);
						dims[i].setAxisOptions(axes[i]);
						dims[i].addPropertyChangeListener(listener);
						if (i < OPTIONS.length) dims[i].setDescription(OPTIONS[i]);
					}
				}
				nameToDimensions.put(key, dims);
				dataTable.setInput(OPTIONS,dims);
				
				MappedBlockBean bean = new MappedBlockBean();
				bean.setName(key);
				
			}
		});
		
		cviewer.addSelectionChangedListener(new ISelectionChangedListener() {
			
			@SuppressWarnings("unchecked")
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				
				if (((StructuredSelection)event.getSelection()).isEmpty()) return;
				
				Object element = ((StructuredSelection)event.getSelection()).getFirstElement();
				Entry<String,int[]> entry = (Entry<String,int[]>)element;
				String key = entry.getKey();
				if (!cviewer.getChecked(element)) {
					dataTable.clearAll();
					return;
				}

				if (!nameToDimensions.containsKey(key))return;
				Dimension[] dims = nameToDimensions.get(key);

				dataTable.setInput(OPTIONS,dims);

				
			}
		});
		
		remappable = new Button(main, SWT.CHECK);
		remappable.setText("Data needs remapping (Select x axis)");
		remappable.setSelection(true);
		remappable.setLayoutData(new GridData());
		remappable.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		remappable.setSelection(false);
		
		
		remapXAxis = new Combo(main, SWT.READ_ONLY);
		remapXAxis.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		remapXAxis.setEnabled(false);
		remapXAxis.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				Dimension d = getSelectedYDimension();
				if (d != null) d.setSecondaryAxis(remapXAxis.getText());
				
				updateBeans();
			}
		});
		
		remappable.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				reMap = remappable.getSelection();
				remapXAxis.setEnabled(reMap);
				String[] axes = getAxisOptionsOfSelectedYAxis();
				
				if (axes == null) return;
				
				remapXAxis.setItems(axes);
			}
		});
		
	}
	
	private void updateBeans() {
		
		beans.clear();
		
		for (Entry<String, Dimension[]> entry : nameToDimensions.entrySet()) {
			beans.add(updateBean(entry.getKey(), entry.getValue()));
		}
		
		boolean complete = !beans.isEmpty();
		
		for (MappedBlockBean b: beans) {
			if (!b.checkValid()) {
				complete = false;
				break;
			}
		}
		
		fireCompleteListeners(complete);
	}
	
	private void updateUIFromBeans(){
		nameToDimensions = new HashMap<String, Dimension[]>();
		nameToDimensions.clear();
		
		Iterator<MappedBlockBean> iterator = beans.iterator();
		
		while (iterator.hasNext()) {
			if (!updateUIFromBean(iterator.next())) iterator.remove();
		}
		
		for (MappedBlockBean bean : beans) {
			updateUIFromBean(bean);
		}
	}
	
	private boolean updateUIFromBean(MappedBlockBean bean) {
		
		if (!datasetNames.containsKey(bean.getName())) {
			return false;
		}
		
		String[] axes = bean.getAxes();
		
		for (String a :axes) {
			if (a != null && !datasetNames.containsKey(a)) {
				return false;
			}
		}
		
		String rm = bean.getxAxisForRemapping();
		
		if (bean.getxDim() ==  bean.getyDim() && rm == null) return false;
		
		if (rm != null && !datasetNames.containsKey(rm)) {
			return false;
		}
		
		int[] shape = datasetNames.get(bean.getName());
		
		if (bean.getxDim() > shape.length || bean.getyDim() > shape.length) {
			return false;
		}
		
		createAndDimension(bean);
		return true;
	}
	
	private void createAndDimension(MappedBlockBean bean){
		int[] shape = datasetNames.get(bean.getName());
		String[][] axes = getAxesNameOptions(shape,bean.getName());
		Dimension[] dims = new Dimension[shape.length];
		for (int i = 0; i < dims.length ; i++) {
			dims[i] = new Dimension(i);
			dims[i].setSize(shape[i]);
			dims[i].setAxisOptions(axes[i]);
			if (i == bean.getxDim()) {
				dims[i].setDescription(OPTIONS[1]);
			}
			if (i == bean.getyDim()) {
				dims[i].setDescription(OPTIONS[0]);
			}
			dims[i].setAxis(bean.getAxes()[i]);
			if (bean.getyDim() == i && bean.getxDim() == bean.getyDim()) {
				dims[i].setSecondaryAxis(bean.getxAxisForRemapping());
				remappable.setSelection(true);
				remapXAxis.setItems(axes[i]);
				for (int j = 0; j < axes[i].length; j++) {
					if (bean.getxAxisForRemapping().equals(axes[i][j])){
						remapXAxis.select(j);
						break;
					}
				}
				remapXAxis.setEnabled(true);
			}
 			dims[i].addPropertyChangeListener(listener);
		}
		
		for (Entry<String,int[]> entry : datasetNames.entrySet()) {
			if (entry.getKey().equals(bean.getName())) {
				cviewer.setChecked(entry, true);
			}
		}

		
		nameToDimensions.put(bean.getName(), dims);
	}
	
	private MappedBlockBean updateBean(String name, Dimension[] dims) {
		
		MappedBlockBean bean = new MappedBlockBean();
		bean.setName(name);
		bean.setRank(dims.length);
		String[] axes = new String[dims.length];

		for (int i = 0; i < dims.length; i++) {
			Dimension d = dims[i];
			axes[i] = d.getAxis();
			if (OPTIONS[0].equals(d.getDescription())) {
				bean.setyDim(i);
			} else if (OPTIONS[1].equals(d.getDescription())) {
				bean.setxDim(i);
			}
		}

		for (int i = 0; i < dims.length; i++) {

			Dimension d = dims[i];

			if (d.getSecondaryAxis() != null) {
				bean.setxDim(i);
				bean.setxAxisForRemapping(d.getSecondaryAxis());
			}
		}
		
		bean.setAxes(axes);
		
		return bean;
	}
	
	
	private String[] getAxisOptionsOfSelectedYAxis(){
		
		Dimension dim = getSelectedYDimension();
		
		if (dim == null) return null;
		
		return dim.getAxisOptions();
		
	}
	
	@SuppressWarnings("unchecked")
	private Dimension getSelectedYDimension(){
		
		StructuredSelection current = (StructuredSelection)cviewer.getSelection();
		if (current.isEmpty()) return null;
		
		Object element = current.getFirstElement();
		
		if (!cviewer.getChecked(element)) return null;
		
		Entry<String,int[]> entry = (Entry<String,int[]>)element;
		
		Dimension[] dims = nameToDimensions.get(entry.getKey());
		
		if (dims == null) return null;
		
		for (Dimension d : dims) {
			if (OPTIONS[0].equals(d.getDescription())){
				return d;
			}
		}
		
		return null;
	}
	
	
	public void setDatasetMaps(Map<String,int[]> datasetNames, Map<String,int[]> nexusDatasetNames) {
		this.datasetNames = datasetNames;
		this.nexusDatasetNames = nexusDatasetNames;
		if (nexusDatasetNames.isEmpty()) {
			cviewer.setInput(datasetNames);
		} else {
			cviewer.setInput(nexusDatasetNames);
		}
	}
	
	public void onlySignalTagged(boolean onlySignal) {
		if (onlySignal) cviewer.setInput(nexusDatasetNames);
		else cviewer.setInput(datasetNames);
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

		@SuppressWarnings("unchecked")
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
	
		@SuppressWarnings("unchecked")
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
