package org.dawnsci.common.widgets.tree;

import org.dawnsci.common.widgets.tree.LabelNode;
import org.dawnsci.common.widgets.tree.NumericNode;
import org.dawnsci.common.widgets.tree.ObjectNode;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.dialogs.PatternFilter;

public class NodeFilter extends PatternFilter {
	
	private IResettableExpansion tool;

	public NodeFilter(IResettableExpansion tool){
		this.tool = tool;	
	}
	
	public void setPattern(String patternString) {


		try {
			super.setPattern(patternString);
			if (patternString == null || patternString.equals("")) {
				Display.getDefault().asyncExec(new Runnable() {
	        		public void run() {
	                	tool.resetExpansion();
	        		}
	        	});
	        }
		} catch (Throwable ignored) {
			// Intended
		}

    }
	
	@Override
    protected boolean isLeafMatch(Viewer viewer, Object element){
    	
		try {

			if (element instanceof LabelNode) {
				LabelNode ln = (LabelNode)element;
				if (wordMatches(ln.getLabel()))      return true;
			}
	
			if (element instanceof ObjectNode) {
				ObjectNode on = (ObjectNode)element;
				if (wordMatches(on.getValue().toString())) return true;
			}
			
			if (element instanceof NumericNode) {
				NumericNode on = (NumericNode)element;
				if (wordMatches(String.valueOf(on.getDoubleValue()))) return true;
				if (wordMatches(String.valueOf(on.getUnit())))        return true;
			}
		} catch (Throwable ne) {
			return true;
		}

		return false;
    }

}
