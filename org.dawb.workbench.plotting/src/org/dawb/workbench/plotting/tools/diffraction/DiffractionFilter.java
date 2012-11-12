package org.dawb.workbench.plotting.tools.diffraction;

import org.dawb.common.ui.tree.LabelNode;
import org.dawb.common.ui.tree.NumericNode;
import org.dawb.common.ui.tree.ObjectNode;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.dialogs.PatternFilter;

class DiffractionFilter extends PatternFilter {
	
	private DiffractionTool tool;

	DiffractionFilter(DiffractionTool tool){
		this.tool = tool;	
	}
	
    public void setPattern(String patternString) {
    	
        super.setPattern(patternString);
        
        if (patternString == null || patternString.equals("")) {
        	Display.getDefault().asyncExec(new Runnable() {
        		public void run() {
                	tool.resetExpansion();
        		}
        	});
        }

    }
	
	@Override
    protected boolean isLeafMatch(Viewer viewer, Object element){
    	

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

		return false;
    }

}
