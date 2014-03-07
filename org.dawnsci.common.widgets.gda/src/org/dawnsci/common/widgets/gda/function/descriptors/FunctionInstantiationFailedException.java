package org.dawnsci.common.widgets.gda.function.descriptors;

public class FunctionInstantiationFailedException extends Exception {
	private static final long serialVersionUID = -1090396994436597929L;

	public FunctionInstantiationFailedException(String message) {
		super(message);
	}

	public FunctionInstantiationFailedException(Throwable cause) {
		super(cause);
	}

	public FunctionInstantiationFailedException(String message, Throwable cause) {
		super(message, cause);
	}

}
