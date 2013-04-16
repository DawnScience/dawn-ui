package org.dawnsci.plotting.tools.expressions;

import java.util.EventListener;

public interface IExpressionVariableListener extends EventListener {

	public void variableCreated(ExpressionVariableEvent evt);
}
