package org.dawnsci.mapping.ui.wizards;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.dawnsci.mapping.ui.datamodel.MapBean;
import org.dawnsci.mapping.ui.datamodel.MappedBlockBean;
import org.dawnsci.mapping.ui.datamodel.MappedDataFileBean;
import org.eclipse.jface.dialogs.IPageChangeProvider;
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;

public class ImportMapWizardPage extends WizardPage implements IDatasetWizard {

	private CheckboxTableViewer cviewer;
	private Map<String, Integer> mapToParent;
	private Map<String,int[]> datasetNames;
	private Map<String,int[]> nexusDatasetNames;
	private MappedDataFileBean mdfbean;
	
	private String[] options;
	
	protected ImportMapWizardPage(String name) {
		super(name);
		this.setTitle("Import Maps");
		this.setDescription("Select all maps and their parent data blocks");
		setPageComplete(false);
	}

	@Override
	public void createControl(Composite parent) {
		Composite main = new Composite(parent, SWT.None);
		setControl(main);
		main.setLayout(new GridLayout(2,false));
		cviewer = CheckboxTableViewer.newCheckList(main, SWT.BORDER);
		cviewer.setContentProvider(new BasicContentProvider());
		cviewer.setLabelProvider(new ViewLabelProvider());
		cviewer.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
		final Combo combo = new Combo(main, SWT.NONE);
		combo.setEnabled(false);
		
		final Button onlyNexusTagged = new Button(main, SWT.CHECK);
		onlyNexusTagged.setText("Only signal tagged datasets");
		onlyNexusTagged.setSelection(true);
		onlyNexusTagged.setLayoutData(new GridData());
		onlyNexusTagged.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (onlyNexusTagged.getSelection()) {
					cviewer.setInput(nexusDatasetNames);
				} else {
					cviewer.setInput(datasetNames);
				}
			}
		});
		
		mapToParent = new HashMap<String, Integer>();
		cviewer.addCheckStateListener(new ICheckStateListener() {
			
			@Override
			public void checkStateChanged(CheckStateChangedEvent event) {
				Entry<String,int[]> entry = (Entry<String,int[]>)event.getElement();
				String key = entry.getKey();
				if (!event.getChecked()) {
					combo.setEnabled(false);
					if (mapToParent.containsKey(key)) mapToParent.remove(key);
					updateBeans();
					return;
				}
				combo.setItems(options);
				combo.setEnabled(true);
				
				int index = 0;
				if (mapToParent.containsKey(key)) index = mapToParent.get(key);
				else mapToParent.put(key, index);
				combo.select(index);
				updateBeans();
				
			}
		});
		
		combo.addSelectionListener(new SelectionAdapter() {
			@SuppressWarnings("unchecked")
			@Override
			public void widgetSelected(SelectionEvent e) {
				Object element = ((StructuredSelection)cviewer.getSelection()).getFirstElement();
				if (!cviewer.getChecked(element)) {
					combo.setEnabled(false);
					return;
				}
				Entry<String,int[]> entry = (Entry<String,int[]>)element;
				String key = entry.getKey();
				mapToParent.put(key, combo.getSelectionIndex());
				updateBeans();
			}
		});
		
		
		final IWizardContainer container = this.getContainer();
		if (container instanceof IPageChangeProvider) {
			((IPageChangeProvider)container).addPageChangedListener(new IPageChangedListener() {
				
				@Override
				public void pageChanged(PageChangedEvent event) {
					updateOnPageChange();
					
				}
			});
		}
	}
	
	private void updateBeans() {
		List<MapBean> maps = mdfbean.getMaps();
		maps.clear();
		for (Entry<String, Integer> entry : mapToParent.entrySet()) {
			maps.add(updateBean(entry.getKey(), options[entry.getValue()]));
		}
		
		validatePage();
	}
	
	private void validatePage() {
		List<MapBean> maps = mdfbean.getMaps();
		if (maps.isEmpty())  {
			setPageComplete(false);
			return;
		}
		for (MapBean m : maps) {
			if (!m.checkValid()) {
				setPageComplete(false);
				return;
			}
		}
		
		setPageComplete(true);
	}
	
	private MapBean updateBean(String name, String parent) {
		MapBean bean = new MapBean();
		bean.setName(name);
		bean.setParent(parent);
		return bean;
	}
	
	protected void updateOnPageChange() {
		List<MappedBlockBean> blocks = mdfbean.getBlocks();
		if (blocks.isEmpty()) return;
		options = new String[blocks.size()];
		for (int i = 0; i < blocks.size(); i++) options[i] = blocks.get(i).getName();
		
		if (!mdfbean.getMaps().isEmpty()) {
			
			Iterator<MapBean> it = mdfbean.getMaps().iterator();
			
			List<String> l = Arrays.asList(options);
			
			while (it.hasNext()) {
				MapBean b = it.next();
				
				if (!datasetNames.containsKey(b.getName()) || !l.contains(b.getParent())) {
					it.remove();
					continue;
				}
				
				mapToParent.put(b.getName(), l.lastIndexOf(b.getParent()));
			
				for (Entry<String,int[]> entry : datasetNames.entrySet()) {
					if (entry.getKey().equals(b.getName())) {
						cviewer.setChecked(entry, true);
					}
				}

			}
			
		}
		
		validatePage();
		
//		if (description != null && description.getBlockNames() != null){
//			options = description.getBlockNames().toArray(new String[description.getBlockNames().size()]);
//
//			if (description.getDataBlockToMapMapping() != null) {
//				for (Entry<String, List<String>> e : description.getDataBlockToMapMapping().entrySet()) {
//					if (datasetNames.containsKey(e.getKey())) {
//
//						for (String a : e.getValue()) {
//							if (a != null && datasetNames.containsKey(a)) {
//								int i = 0;
//								for (;i< options.length; i++) if (e.getKey().equals(options[i])) break;
//								mapToParent.put(a, i);
//								for (Entry<String, int[]> ent : datasetNames.entrySet()) if (ent.getKey().equals(a)) cviewer.setChecked(ent, true);
//							}
//						}
//
//					}
//				}
//			}

//		}
	}

	@Override
	public void setDatasetMaps(Map<String, int[]> datasetNames,
			Map<String, int[]> nexusDatasetNames) {
		this.datasetNames = datasetNames;
		this.nexusDatasetNames = nexusDatasetNames;
		if (nexusDatasetNames.isEmpty()) {
			cviewer.setInput(datasetNames);
		} else {
			cviewer.setInput(nexusDatasetNames);
		}
		
	}
	
	@Override
	public void setMapBean(MappedDataFileBean bean) {
		this.mdfbean = bean;
	}
	
	private class BasicContentProvider implements IStructuredContentProvider {

		@Override
		@SuppressWarnings("unchecked")
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
