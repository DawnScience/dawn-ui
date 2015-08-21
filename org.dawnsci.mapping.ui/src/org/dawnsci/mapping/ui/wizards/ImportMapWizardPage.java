package org.dawnsci.mapping.ui.wizards;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.dawnsci.mapping.ui.datamodel.MappedFileDescription;
import org.eclipse.jface.dialogs.IPageChangeProvider;
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;

public class ImportMapWizardPage extends WizardPage implements IDatasetWizard {

	protected MappedFileDescription description;
	private CheckboxTableViewer cviewer;
	private Map<String, Integer> mapToParent;
	private Map<String,int[]> datasetNames;
	private Map<String,int[]> nexusDatasetNames;
	
	private String[] options;
	
	protected ImportMapWizardPage(String name) {
		super(name);
		this.setTitle("Import Maps");
		this.setDescription("Select all maps, their axes, and which dimensions correspond to the map X and Y directions");
	}

	@Override
	public void createControl(Composite parent) {
		Composite main = new Composite(parent, SWT.None);
		setControl(main);
		main.setLayout(new GridLayout(2,false));
		//	CCombo cc = new CCombo(main, SWT.READ_ONLY);
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
		cviewer.addSelectionChangedListener(new ISelectionChangedListener() {
			
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				Object element = ((StructuredSelection)event.getSelection()).getFirstElement();
				Entry<String,int[]> entry = (Entry<String,int[]>)element;
				String key = entry.getKey();
				if (!cviewer.getChecked(element)) {
					combo.setEnabled(false);
					if (mapToParent.containsKey(key)) mapToParent.remove(key);
					return;
				}
				combo.setItems(options);
				combo.setEnabled(true);
				
				int index = 0;
				if (mapToParent.containsKey(key)) index = mapToParent.get(key);
				else mapToParent.put(key, index);
				combo.select(index);
			}
		});
		
		combo.addSelectionListener(new SelectionAdapter() {
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
	
	protected void updateOnPageChange() {
		if (description != null && description.getBlockNames() != null){
			options = description.getBlockNames().toArray(new String[description.getBlockNames().size()]);

			if (description.getDataBlockToMapMapping() != null) {
				for (Entry<String, List<String>> e : description.getDataBlockToMapMapping().entrySet()) {
					if (datasetNames.containsKey(e.getKey())) {
						boolean canAdd = true;

						for (String a : e.getValue()) {
							if (a != null && datasetNames.containsKey(a)) {
								int i = 0;
								for (;i< options.length; i++) if (e.getKey().equals(options[i])) break;
								mapToParent.put(a, i);
								for (Entry<String, int[]> ent : datasetNames.entrySet()) if (ent.getKey().equals(a)) cviewer.setChecked(ent, true);
							}
						}

//						if (canAdd) {
//							int i = 0;
//							for (;i< options.length; i++) if (e.getKey().equals(options[i])) break;
//							mapToParent.put(e.getKey(), i);
//							for (Entry<String, int[]> ent : datasetNames.entrySet()) if (ent.getKey().equals(e.getKey()))cviewer.setChecked(ent, true);
//
//						}
					}
				}
			}

		}
	}
	
	public void pushChanges(){
		if (mapToParent != null && !mapToParent.isEmpty()) {
			for (Entry<String, Integer> entry : mapToParent.entrySet()) {
				description.addMap(options[entry.getValue()], entry.getKey());
			}
		}
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
	public void setMappedDataDescription(MappedFileDescription description) {
		this.description = description;
		
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
