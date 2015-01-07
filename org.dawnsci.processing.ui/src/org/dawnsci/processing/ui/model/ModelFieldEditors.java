package org.dawnsci.processing.ui.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.dawb.common.ui.util.EclipseUtils;
import org.dawnsci.common.widgets.celleditor.CComboCellEditor;
import org.dawnsci.common.widgets.celleditor.ClassCellEditor;
import org.dawnsci.common.widgets.celleditor.FileDialogCellEditor;
import org.dawnsci.common.widgets.celleditor.TextCellEditorWithContentProposal;
import org.dawnsci.plotting.roi.RegionCellEditor;
import org.dawnsci.processing.ui.slice.SlicedDataUtils;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.dawnsci.analysis.api.io.ILoaderService;
import org.eclipse.dawnsci.analysis.api.processing.model.FileType;
import org.eclipse.dawnsci.analysis.api.processing.model.ModelField;
import org.eclipse.dawnsci.analysis.api.processing.model.OperationModelField;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.fieldassist.ContentProposal;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ICellEditorListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.window.DefaultToolTip;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;

public class ModelFieldEditors {

	private static ISelectionListener selectionListener;
	private static ToolTip            currentHint;
	
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
    	
        if (clazz == Boolean.class) {
        	ed = new CheckboxCellEditor(parent, SWT.NONE);
        	
        } else if (Number.class.isAssignableFrom(clazz) || isNumberArray(clazz)) {        	
        	ed = getNumberEditor(field, clazz, parent);
        	
        } else if (IROI.class.isAssignableFrom(clazz)) {        	
        	ed = new RegionCellEditor(parent);
        	
        } else if (Enum.class.isAssignableFrom(clazz)) {
        	ed = getChoiceEditor((Class<? extends Enum>)clazz, parent);
        	
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

	private static CellEditor getChoiceEditor(final Class<? extends Enum> clazz, Composite parent) {
		
		final Enum[]   values = clazz.getEnumConstants();
	    final String[] items  = Arrays.toString(values).replaceAll("^.|.$", "").split(", ");
		
		CComboCellEditor cellEd = new CComboCellEditor(parent, items) {
    	    protected void doSetValue(Object value) {
                if (value instanceof Enum) value = ((Enum) value).ordinal();
                super.doSetValue(value);
    	    }
    		protected Object doGetValue() {
    			Integer ordinal = (Integer)super.doGetValue();
    			return values[ordinal];
    		}
		};
		
		return cellEd;
	}

	private static CellEditor getNumberEditor(ModelField field, final Class<? extends Object> clazz, Composite parent) {
    	
		OperationModelField anot = field.getAnnotation();
		CellEditor textEd = null;
	    if (anot!=null) {
	    	textEd = new ClassCellEditor(parent, clazz, anot.min(), anot.max(), anot.unit(), SWT.NONE);
	    } else {
	    	textEd = new ClassCellEditor(parent, clazz, SWT.NONE);
	    }

    	return textEd;
	}
	
	private static TextCellEditor getDatasetEditor(final ModelField field, Composite parent) {
		
		final TextCellEditorWithContentProposal ed = new TextCellEditorWithContentProposal(parent, null, null);
		
		Job job = new Job("dataset name read") {
			
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				String fileField = field.getAnnotation().dataset();
				Object object;
				try {
					object = field.getModel().get(fileField);
				} catch (Exception e) {
					return Status.CANCEL_STATUS;
				}
				
				if (object == null) return Status.CANCEL_STATUS;
				final Map<String, int[]> datasetInfo = SlicedDataUtils.getDatasetInfo(object.toString(), null);
				datasetInfo.toString();
				
				final IContentProposalProvider cpp = new IContentProposalProvider() {
					
					@Override
					public IContentProposal[] getProposals(String contents, int position) {
						List<IContentProposal> prop = new ArrayList<IContentProposal>();
						
						for (String key : datasetInfo.keySet()) {
							if (key.startsWith(contents)) prop.add(new ContentProposal(key));
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
			
		return ed;
	}

}
