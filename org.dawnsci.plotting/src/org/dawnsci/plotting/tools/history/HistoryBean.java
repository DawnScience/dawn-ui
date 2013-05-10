package org.dawnsci.plotting.tools.history;

import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dawb.common.gpu.Operator;
import org.dawb.common.services.IExpressionObject;
import org.dawb.common.services.IExpressionObjectService;
import org.dawb.common.services.IVariableManager;
import org.dawb.common.services.ServiceManager;
import org.eclipse.swt.graphics.RGB;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.monitor.IMonitor;

class HistoryBean {
	
	public enum FORCE_TYPE {
		NONE, IFKEYNULL, FORCE;
	}
	
	// Image compare
	private AbstractDataset       data;
	private List<IDataset>        axes;
	private Operator              operator;
	private int                   weighting=100;
	
	// Expressions
	private IExpressionObject     expression;
	private String                variable;
	
	// 1D history
	private AbstractDataset xdata;
	private RGB             plotColour;
	
	// Anyone
	private String          plotName; // Often the file name
	private String          traceName;
	private boolean         selected;
	private boolean         modifiable=true;
	
	private final IVariableManager         variableManager;
	private       IExpressionObjectService service;
	
	private static int count=0;
	HistoryBean(IVariableManager varManager) {
		this.variableManager = varManager;
		try {
			this.service = (IExpressionObjectService)ServiceManager.getService(IExpressionObjectService.class);
		} catch (Exception e) {
			this.service = null; // allowed to be null.
		}
		
		while(true) { 
			// With UI threads, while(true) better than set range because it obviously identifies a problem.
			// With multiple threads w
			try {
				count++;
				if (count>2000000) throw new RuntimeException("More than 2m variable names assigned! Algorithm is misbehaving.");
				
				variable = "var"+count;
				this.variable = service.validate(variableManager, variable);
				break;
			} catch (Exception e) {
				continue; // try another name
			}
		}
	}
	
	/**
	 * Human readable trace name
	 * @return
	 */
	public String createTraceName() {
		return getTraceName()+" ("+getPlotName()+")";
	}
	public String getTraceKey() {
		if (fixedImageKey!=null)   return fixedImageKey;
		createFixedKey(FORCE_TYPE.IFKEYNULL);
		return fixedImageKey;
	}
	
	private String fixedImageKey; // Once the key is generated, it is used in a map and cannot change.
	public void setFixedImageKey(String key) {
		this.fixedImageKey = key;
	}

	public AbstractDataset getXdata() {
		if (xdata==null) {
			AbstractDataset data = getData();
			if (data!=null && data.getRank()==1) {
				xdata = AbstractDataset.arange(data.getSize(), AbstractDataset.INT32);
			}
		}
		return xdata;
	}

	public AbstractDataset getData() {
		if (expression!=null)
			try {
				return (AbstractDataset)expression.getDataSet(null, new IMonitor.Stub());
			} catch (Exception e) {
				// Allowed
			}
		return data;
	}
	public void setData(AbstractDataset data) {
		this.data = data;
	}
	public void setXdata(AbstractDataset xdata) {
		this.xdata = xdata;
	}
	public boolean isSelected() {
		return selected;
	}
	public void setSelected(boolean selected) {
		this.selected = selected;
	}
	public String getPlotName() {
		return plotName;
	}
	public void setPlotName(String parentPlotName) {
		this.plotName = parentPlotName;
		createFixedKey(FORCE_TYPE.NONE);
	}
	private String createFixedKey(FORCE_TYPE type) {
		
		final String originalKey = fixedImageKey;
		if (fixedImageKey==null && getTraceName()!=null && getPlotName()!=null) {
			fixedImageKey = getTraceName()+":"+getPlotName();
		}
		if (type==FORCE_TYPE.IFKEYNULL && fixedImageKey==null) { // Nulls allowed
			fixedImageKey = getTraceName()+":"+getPlotName();
		}
		if (type==FORCE_TYPE.FORCE) { // Nulls allowed
			fixedImageKey = getTraceName()+":"+getPlotName();
		}
		if (expression!=null && !fixedImageKey.equals(originalKey)) {
			fixedImageKey = variable+fixedImageKey;
		}

		return fixedImageKey;
	}
	public String getTraceName() {
		if (expression!=null) return expression.getExpressionString();
		return traceName;
	}
	public String setTraceName(String name) {
		return setTraceName(name, false);
	}
	
	private final static Pattern DEF_VAR_NAME = Pattern.compile("var(\\d+)");
	
	public String setTraceName(String name, boolean force) {
		this.traceName = name;
		
		String ret = null;
		if (force) {
			ret = createFixedKey(FORCE_TYPE.FORCE);
		} else {
			ret = createFixedKey(FORCE_TYPE.NONE);
		}
		if (variable==null || DEF_VAR_NAME.matcher(variable).matches()) {
			
			try {
				this.variable = service.validate(variableManager, name);			
			} catch (Exception e) {
				// Not an error.
			}
		}
		
		return ret;
	}
	public AbstractDataset getYdata() {
		return getData();
	}
	public void setYdata(AbstractDataset data) {
		setData(data);
	}
	public RGB getPlotColour() {
		return plotColour;
	}
	public void setPlotColour(RGB plotColour) {
		this.plotColour = plotColour;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((axes == null) ? 0 : axes.hashCode());
		result = prime * result + ((data == null) ? 0 : data.hashCode());
		result = prime * result
				+ ((expression == null) ? 0 : expression.hashCode());
		result = prime * result
				+ ((fixedImageKey == null) ? 0 : fixedImageKey.hashCode());
		result = prime * result + (modifiable ? 1231 : 1237);
		result = prime * result
				+ ((operator == null) ? 0 : operator.hashCode());
		result = prime * result
				+ ((plotColour == null) ? 0 : plotColour.hashCode());
		result = prime * result
				+ ((plotName == null) ? 0 : plotName.hashCode());
		result = prime * result + (selected ? 1231 : 1237);
		result = prime * result
				+ ((traceName == null) ? 0 : traceName.hashCode());
		result = prime * result
				+ ((variable == null) ? 0 : variable.hashCode());
		result = prime * result + weighting;
		result = prime * result + ((xdata == null) ? 0 : xdata.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		HistoryBean other = (HistoryBean) obj;
		if (axes == null) {
			if (other.axes != null)
				return false;
		} else if (!axes.equals(other.axes))
			return false;
		if (data == null) {
			if (other.data != null)
				return false;
		} else if (!data.equals(other.data))
			return false;
		if (expression == null) {
			if (other.expression != null)
				return false;
		} else if (!expression.equals(other.expression))
			return false;
		if (fixedImageKey == null) {
			if (other.fixedImageKey != null)
				return false;
		} else if (!fixedImageKey.equals(other.fixedImageKey))
			return false;
		if (modifiable != other.modifiable)
			return false;
		if (operator != other.operator)
			return false;
		if (plotColour == null) {
			if (other.plotColour != null)
				return false;
		} else if (!plotColour.equals(other.plotColour))
			return false;
		if (plotName == null) {
			if (other.plotName != null)
				return false;
		} else if (!plotName.equals(other.plotName))
			return false;
		if (selected != other.selected)
			return false;
		if (traceName == null) {
			if (other.traceName != null)
				return false;
		} else if (!traceName.equals(other.traceName))
			return false;
		if (variable == null) {
			if (other.variable != null)
				return false;
		} else if (!variable.equals(other.variable))
			return false;
		if (weighting != other.weighting)
			return false;
		if (xdata == null) {
			if (other.xdata != null)
				return false;
		} else if (!xdata.equals(other.xdata))
			return false;
		return true;
	}
	public List<IDataset> getAxes() {
		return axes;
	}
	public void setAxes(List<IDataset> axes) {
		this.axes = axes;
	}
	public Operator getOperator() {
		return operator;
	}
	public void setOperator(Operator operator) {
		this.operator = operator;
	}
	public boolean isModifiable() {
		return modifiable;
	}
	public void setModifiable(boolean modifiable) {
		this.modifiable = modifiable;
	}
	public int getWeighting() {
		return weighting;
	}
	public void setWeighting(int weighting) {
		this.weighting = weighting;
	}
	
	private static final Pattern pattern = Pattern.compile("(.+)(\\d+)");
	
	/**
	 * Manipulates the original trace name in an attempt to get something 
	 * unique.
	 * 
	 * @param keySet
	 */
	public void generateUniqueKey(Set<String> keySet) {
		createFixedKey(FORCE_TYPE.IFKEYNULL);
		if (!keySet.contains(this.fixedImageKey)) return;
		final Matcher matcher = pattern.matcher(traceName);
		if (matcher.matches()) {
			int index = Integer.parseInt(matcher.group(2));		
			String traceName = matcher.group(1)+index;
			
			while(traceName.equals(getTraceName()) || keySet.contains(traceName+":"+getPlotName())) {
				index++;
				traceName = matcher.group(1)+index;
			}
			setTraceName(traceName);
			this.fixedImageKey = traceName+":"+getPlotName();
			return;
		}
		
		int index = 1;
		while(keySet.contains(fixedImageKey+"("+index+")")) index++;
		this.fixedImageKey = fixedImageKey+"("+index+")";

	}

	public IExpressionObject getExpression() {
		return expression;
	}
	public void setExpression(IExpressionObject expression) {
		this.expression = expression;
	}
	public void setExpression(boolean requireExpression) {
		if (!requireExpression) {
			this.expression = null;
			return;
		} else {
			expression = service.createExpressionObject(variableManager, null, "");
		}
	}

	public String getVariable() {
		return variable;
	}
	public void setVariable(String variable) {
		this.variable = variable;
	}

	public IVariableManager getVariableManager() {
		return variableManager;
	}

	public boolean isExpression() {
		return expression!=null;
	}

}
