package org.dawnsci.datavis.manipulation.aggregate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.dawnsci.analysis.api.expressions.IExpressionEngine;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;

public class ExpressionLabelOption extends LabelOption {
	private List<LabelOption> variables;

	public ExpressionLabelOption(String label) {
		super();
		this.name = label;
		this.label = " Expression: " + label;
		variables = new ArrayList<>();
	}

	public void addVariable(LabelOption v) {
		if (!variables.contains(v)) {
			variables.add(v);
		}
	}

	public List<LabelOption> getVariables() {
		return variables;
	}

	public Dataset evaluate(IExpressionEngine engine, Map<String, Object> vars) {
		Dataset v = getValues();
		if (v == null) {
			engine.setLoadedVariables(vars);
			try {
				engine.createExpression(name);
				v = DatasetFactory.createFromObject(engine.evaluate());
				v.setName(getName());
				setValues(v);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return v;
	}
}
