package org.dawnsci.plotting.tools.expressions;

import java.util.EventObject;
import java.util.List;

public class ExpressionVariableEvent extends EventObject {

	private static final long serialVersionUID = 1L;
	private List<String> names;

	public ExpressionVariableEvent(Object source, List<String> names) {
		super(source);
		this.names = names;
	}

	public List<String> getNames() {
		return names;
	}
}
