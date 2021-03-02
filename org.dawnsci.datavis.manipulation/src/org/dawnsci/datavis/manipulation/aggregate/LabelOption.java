package org.dawnsci.datavis.manipulation.aggregate;

import org.dawnsci.datavis.model.DataOptionsUtils;
import org.eclipse.january.dataset.Dataset;

/**
 * Option for a label
 */
public class LabelOption {
	
	protected String label;
	protected String name;
	private Dataset values;
	private Dataset sortedValues;

	protected LabelOption() {
	}

	public LabelOption(String label) {
		this.label = label;
		this.name = DataOptionsUtils.shortenDatasetPath(label, true);
	}

	/**
	 * @return label (aka dataset path)
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * @return name (shortened from label)
	 */
	public String getName() {
		return name;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}

		return label.equals(obj);
	}

	public void setValues(Dataset values) {
		this.values = values;
	}

	public Dataset getValues() {
		return sortedValues == null ? values : sortedValues;
	}

	public void setSortedValues(Dataset values) {
		sortedValues  = values;
	}

	public Dataset getUnsortedValues() {
		return values;
	}

	static LabelOption createLabelOption(String label) {
		return label.isEmpty() ? null : new LabelOption(label);
	}
}