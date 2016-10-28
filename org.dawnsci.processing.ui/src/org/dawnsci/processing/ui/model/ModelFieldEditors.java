package org.dawnsci.processing.ui.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.dawb.common.ui.util.DatasetNameUtils;
import org.dawb.common.ui.util.EclipseUtils;
import org.dawnsci.common.widgets.celleditor.TextCellEditorWithContentProposal;
import org.dawnsci.plotting.roi.RegionCellEditor;
import org.dawnsci.processing.ui.ServiceHolder;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.dawnsci.analysis.api.expressions.IExpressionEngine;
import org.eclipse.dawnsci.analysis.api.expressions.IExpressionService;
import org.eclipse.dawnsci.analysis.api.processing.IOperationInputData;
import org.eclipse.dawnsci.analysis.api.processing.model.FileType;
import org.eclipse.dawnsci.analysis.api.processing.model.IOperationModel;
import org.eclipse.dawnsci.analysis.api.processing.model.ModelField;
import org.eclipse.dawnsci.analysis.api.processing.model.ModelUtils;
import org.eclipse.dawnsci.analysis.api.processing.model.OperationModelField;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.slicer.SliceFromSeriesMetadata;
import org.eclipse.jface.fieldassist.ContentProposal;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.window.DefaultToolTip;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.richbeans.widgets.cell.CComboCellEditor;
import org.eclipse.richbeans.widgets.cell.CComboWithEntryCellEditor;
import org.eclipse.richbeans.widgets.cell.CComboWithEntryCellEditorData;
import org.eclipse.richbeans.widgets.cell.NumberCellEditor;
import org.eclipse.richbeans.widgets.file.FileDialogCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModelFieldEditors {
	
	private static final Logger logger = LoggerFactory.getLogger(ModelFieldEditors.class);

	private static ISelectionListener selectionListener;
	private static ToolTip            currentHint;
	private static DataReadyEventHandler handler;
	private static IOperationInputData recentData;
	
	static {
		BundleContext ctx = FrameworkUtil.getBundle(ModelFieldEditors.class).getBundleContext();
		handler = new ModelFieldEditors().new DataReadyEventHandler();
		Dictionary<String, String> props = new Hashtable<>();
		props.put(EventConstants.EVENT_TOPIC, "org/dawnsci/events/processing/DATAUPDATE");
		ctx.registerService(EventHandler.class, handler, props);
	}
	
	
	/**
	 * Create a new editor for a field.
	 * @param field
	 * @return
	 */
	public static CellEditor createEditor(ModelField field, Composite parent) {
        
		Object value;
		try {
			value = field.get();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
		Class<? extends Object> clazz = null;
		if (value!=null) {
			clazz = value.getClass();
		} else {
			try {
				clazz = field.getType();
			} catch (NoSuchFieldException | SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
        
  
		CellEditor ed = null;
    	final OperationModelField anot = field.getAnnotation();
    	if (!isEnabled(field.getModel(), anot)) return null;
   	
        if (clazz == Boolean.class) {
        	ed = new CheckboxCellEditor(parent, SWT.NONE);
        	
        } else if (Number.class.isAssignableFrom(clazz) || isNumberArray(clazz)) {        	
        	ed = getNumberEditor(field, clazz, parent);
        	
        } else if (IROI.class.isAssignableFrom(clazz)) {        	
        	ed = new RegionCellEditor(parent);
        	
        } else if (Enum.class.isAssignableFrom(clazz)) {
        	ed = getChoiceEditor((Class<? extends Enum<?>>)clazz, parent);
        	
        } else if (CComboWithEntryCellEditorData.class.isAssignableFrom(clazz)) {
        	ed = getChoiceWithEntryEditor((CComboWithEntryCellEditorData) value, parent);
        	
        } else if (FileDialogCellEditor.isEditorFor(clazz) || (anot!=null && anot.file()!=FileType.NONE)) {
        	FileDialogCellEditor fe = new FileDialogCellEditor(parent);
        	fe.setValueClass(clazz);
        	ed = fe;
        	if (anot!=null) {
        		fe.setDirectory(anot.file().isDirectory());
        		fe.setNewFile(anot.file().isNewFile());
        	}
        
        } else if (String.class.equals(clazz) && anot!=null && anot.dataset() != null &&!anot.dataset().isEmpty()) {
        	ed = getDatasetEditor(field, parent);
        	
        } else if (String.class.equals(clazz)) {
        	ed = new TextCellEditor(parent);
        }
        
        // Show the tooltip, if there is one
        if (ed!=null) {
        	if (anot!=null) {
        		String hint = anot.hint();
        		if (hint!=null && !"".equals(hint)) {
        			showHint(hint, parent);
        		}
        	}
        }
        
        return ed;

	}

	public static boolean isEnabled(ModelField field) {
    	final OperationModelField anot  = field.getAnnotation();
    	final IOperationModel     model = field.getModel();
    	return isEnabled(model, anot);
	}

	private static boolean isEnabled(IOperationModel model, OperationModelField anot) {

		if (anot == null) return true;
		if (!anot.editable()) return false;
    	
	   	String enableIf = anot.enableif();
	   	if (enableIf!=null && !"".equals(enableIf)) {
	   		
	   		try {
		   		final IExpressionService service = ServiceHolder.getExpressionService();
		   		final IExpressionEngine  engine  = service.getExpressionEngine();
		   		engine.createExpression(enableIf);
		   		
		   		final Map<String, Object>    values = new HashMap<>();
		   		final Collection<ModelField> fields = ModelUtils.getModelFields(model);
		   		for (ModelField field : fields) {
		   			Object value = field.get();
		   			if (value instanceof Enum) value = ((Enum<?>)value).name();
		   			values.put(field.getName(), value);
				}
		   		engine.setLoadedVariables(values);
		   		return (Boolean)engine.evaluate();
		   		
	   		} catch (Exception ne) {
	   			logger.error("Cannot evaluate expression "+enableIf, ne);
	   		}
	   	}
	   	
	    return true;
	}

	private static void showHint(final String hint, final Composite parent) {
		
		if (parent.isDisposed()) return;
		if (parent!=null) parent.getDisplay().asyncExec(new Runnable() {
			public void run() {
				
				currentHint = new DefaultToolTip(parent, ToolTip.NO_RECREATE, true);
				((DefaultToolTip)currentHint).setText(hint);
				currentHint.setHideOnMouseDown(true);
				currentHint.show(new Point(0, parent.getSize().y));
				
				if (selectionListener==null) {
					if (EclipseUtils.getPage()!=null) {
						selectionListener = new ISelectionListener() {
							@Override
							public void selectionChanged(IWorkbenchPart part, ISelection selection) {
								if (currentHint!=null) currentHint.hide();
							} 
						};
						
						EclipseUtils.getPage().addSelectionListener(selectionListener);
					}

				}
			}
		});
	}

	private static boolean isNumberArray(Class<? extends Object> clazz) {
		
		if (clazz==null)      return false;
		if (!clazz.isArray()) return false;
		
		return double[].class.isAssignableFrom(clazz) || float[].class.isAssignableFrom(clazz) ||
               int[].class.isAssignableFrom(clazz)    || long[].class.isAssignableFrom(clazz);
	}

	private static CellEditor getChoiceEditor(final Class<? extends Enum<?>> clazz, Composite parent) {
		
		final Enum<?>[]   values = clazz.getEnumConstants();
	    final String[] items  = Arrays.toString(values).replaceAll("^.|.$", "").split(", ");
		
		CComboCellEditor cellEd = new CComboCellEditor(parent, items) {
    	    protected void doSetValue(Object value) {
                if (value instanceof Enum) value = ((Enum<?>) value).ordinal();
                super.doSetValue(value);
    	    }
    		protected Object doGetValue() {
    			Integer ordinal = (Integer)super.doGetValue();
    			return values[ordinal];
    		}
		};
		
		return cellEd;
	}

	private static CellEditor getChoiceWithEntryEditor(final CComboWithEntryCellEditorData data, Composite parent) {
		
	    final String[] items  = data.getItems();
		
		CComboWithEntryCellEditor cellEd = new CComboWithEntryCellEditor(parent, items) {
    	    protected void doSetValue(Object value) {
                super.doSetValue(((CComboWithEntryCellEditorData)value).getActiveItem());
    	    }
    		protected Object doGetValue() {
    			return new CComboWithEntryCellEditorData(data, (String)super.doGetValue());
    		}
		};
		
		return cellEd;
	}
	
	private static CellEditor getNumberEditor(ModelField field, final Class<? extends Object> clazz, Composite parent) {
    	
		OperationModelField anot = field.getAnnotation();
		CellEditor textEd = null;
	    if (anot!=null) {
	    	textEd = new NumberCellEditor(parent, clazz, anot.min(), anot.max(), anot.unit(), SWT.NONE);
	    	
	    	if (anot.numberFormat()!=null && !"".equals(anot.numberFormat())) {
		    	((NumberCellEditor)textEd).setDecimalFormat(anot.numberFormat());
		    }
	    	
	    } else {
	    	textEd = new NumberCellEditor(parent, clazz, SWT.NONE);
	    }
	    
	    ((NumberCellEditor)textEd).setAllowInvalidValues(true);

    	return textEd;
	}
	
	private static TextCellEditor getDatasetEditor(final ModelField field, Composite parent) {
		
		final TextCellEditorWithContentProposal ed = new TextCellEditorWithContentProposal(parent, null, null);
		
		String fileField = field.getAnnotation().dataset();
		
		if (fileField == null || fileField.isEmpty()) return ed;
		
		try {

			String path = null;

			if (!field.getModel().isModelField(fileField) && 
					recentData != null && 
					recentData.getCurrentOperations().get(0) != null && 
					recentData.getCurrentOperations().get(0).getModel() == field.getModel()) {
				path = recentData.getInputData().getFirstMetadata(SliceFromSeriesMetadata.class).getFilePath();

			}

			if (!field.getModel().isModelField(fileField)) {
				handler.setCurrentModel(field.getModel(), ed);
			} else {
				path = field.getModel().get(fileField).toString();
			}

			if (path != null) triggerDatasetNameJob(ed,path);

		} catch (Exception e) {
			logger.error("Could not set up auto-complete", e);
		}
		
		return ed;
	}
	
	private static void triggerDatasetNameJob(final TextCellEditorWithContentProposal ed, final String path){
		
		if (ed == null) return;
		
		Job job = new Job("dataset name read") {
			
			@Override
			protected IStatus run(IProgressMonitor monitor) {

				
				if (path == null) return Status.CANCEL_STATUS;
				final Map<String, int[]> datasetInfo = DatasetNameUtils.getDatasetInfo(path, null);
				datasetInfo.toString();
				
				final IContentProposalProvider cpp = new IContentProposalProvider() {
					
					@Override
					public IContentProposal[] getProposals(String contents, int position) {
						List<IContentProposal> prop = new ArrayList<IContentProposal>();
						
						for (String key : datasetInfo.keySet()) {
							if (key.startsWith(contents)) prop.add(new ContentProposal(key));
						}
						
						if (prop.isEmpty()) {
							for(String key : datasetInfo.keySet()) prop.add(new ContentProposal(key));
						}
						
						return prop.toArray(new IContentProposal[prop.size()]);
					}
				};
				
				Display.getDefault().syncExec(new Runnable() {
					
					@Override
					public void run() {
						ed.setContentProposalProvider(cpp);
						ed.getContentProposalAdapter().setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);
						ed.getContentProposalAdapter().setAutoActivationCharacters(null);
					}
				});
				
				return Status.OK_STATUS;
			}
		};
		
		job.schedule();
	}

	private class DataReadyEventHandler implements EventHandler {
		
		private IOperationModel model;
		private TextCellEditorWithContentProposal ed;
		
		@Override
		public void handleEvent(Event event) {
			IOperationInputData data = (IOperationInputData)event.getProperty("data");
			recentData = data;
			if (data != null && data.getCurrentOperations().get(0).getModel() == model) {
				SliceFromSeriesMetadata md = data.getInputData().getFirstMetadata(SliceFromSeriesMetadata.class);
				if (md != null && md.getFilePath() != null) {
					triggerDatasetNameJob(ed, md.getFilePath());
				}
			}
			
		}
		
		public void setCurrentModel(IOperationModel model, TextCellEditorWithContentProposal ed) {
			this.model = model;
			this.ed = ed;
		}
		
	}
	
}
