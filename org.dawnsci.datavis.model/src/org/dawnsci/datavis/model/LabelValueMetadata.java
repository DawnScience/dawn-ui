package org.dawnsci.datavis.model;

import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.metadata.MetadataType;

public class LabelValueMetadata implements MetadataType {
	private static final long serialVersionUID = -161688858357592857L;
	private Dataset labelValue;

	public LabelValueMetadata() {
	}

	public LabelValueMetadata(Dataset value) {
		this.labelValue = value;
	}

	public LabelValueMetadata(LabelValueMetadata lv) {
		labelValue = lv.getLabelValue();
	}

	@Override
	public MetadataType clone() {
		return new LabelValueMetadata(this);
	}

	public void setLabelValue(Dataset value) {
		this.labelValue = value;
	}

	public Dataset getLabelValue() {
		return labelValue;
	}
}
