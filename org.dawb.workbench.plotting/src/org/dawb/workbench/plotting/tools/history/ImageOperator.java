package org.dawb.workbench.plotting.tools.history;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;

public enum ImageOperator {

	//NO_OPERATOR("None"),
	ADD("     +     "),
	SUBTRACT("     -     "),
	MULTIPLY("     x     "),
	DIVIDE("     ÷     ");
	//MEDIAN("Median");
	
	private String name;

	ImageOperator(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public int getIndex() {
		final ImageOperator[] ops = ImageOperator.values();
		for (int i = 0; i < ops.length; i++) if (ops[i]==this) return i;
		return -1;
	}

	public static String[] getOperators() {
		final ImageOperator[] ops = ImageOperator.values();
		final String[] names = new String[ops.length];
		for (int i = 0; i < ops.length; i++) {
			names[i] = ops[i].getName();
		}
		return names;
	}

	public static ImageOperator getOperator(int index) {
		final ImageOperator[] ops = ImageOperator.values();
		return ops[index];
	}
	
	/**
	 * Does an in-place operation on 'a'. Hence ensure that start has been copied
	 * if you don't want to change it.
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public void process(AbstractDataset a, AbstractDataset b) {

		switch (this) {

//		case NO_OPERATOR:
//			return;

		case ADD:
			a.iadd(b);
			return;

		case SUBTRACT:
			a.isubtract(b);
			return;

		case MULTIPLY:
			a.imultiply(b);
			return;

		case DIVIDE:
			a.idivide(b);
			return;
		}
	}
}
