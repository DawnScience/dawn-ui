/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.tools.preference.detector;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class DiffractionDetectors implements Serializable {
	

	private static final long serialVersionUID = 7297838000309018668L;
	
	private List<DiffractionDetector> diffractionDetectors = new ArrayList<DiffractionDetector>();
	public List<DiffractionDetector> getDiffractionDetectors() {
		return diffractionDetectors;
	}

	public void setDiffractionDetectors(
			List<DiffractionDetector> diffractionDetectors) {
		this.diffractionDetectors = diffractionDetectors;
	}

	private DiffractionDetector selected;
	
	public void addDiffractionDetector(DiffractionDetector detector) {
		diffractionDetectors.add(detector);
	}
	
	public void removeDiffractionDetector(DiffractionDetector detector) {
		diffractionDetectors.remove(detector);
	}

	public DiffractionDetector getSelected() {
		return selected;
	}

	public void setDiffractionDetector(DiffractionDetector selected) {
		this.selected = selected;
	}


	public void clear() {
		if (diffractionDetectors!=null) diffractionDetectors.clear();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((diffractionDetectors == null) ? 0 : diffractionDetectors
						.hashCode());
		result = prime * result
				+ ((selected == null) ? 0 : selected.hashCode());
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
		DiffractionDetectors other = (DiffractionDetectors) obj;
		if (diffractionDetectors == null) {
			if (other.diffractionDetectors != null)
				return false;
		} else if (!diffractionDetectors.equals(other.diffractionDetectors))
			return false;
		if (selected == null) {
			if (other.selected != null)
				return false;
		} else if (!selected.equals(other.selected))
			return false;
		return true;
	}
}
