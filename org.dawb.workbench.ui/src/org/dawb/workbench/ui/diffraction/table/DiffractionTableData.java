/*-
 * Copyright 2013 Diamond Light Source Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dawb.workbench.ui.diffraction.table;

import java.util.List;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.diffraction.IPowderCalibrationInfo;
import org.eclipse.dawnsci.analysis.api.metadata.IDiffractionMetadata;
import org.eclipse.dawnsci.analysis.api.roi.IROI;

import uk.ac.diamond.scisoft.analysis.diffraction.QSpace;

/**
 * Data item used in the table viewer of the Diffraction calibration views
 */
public class DiffractionTableData {
	
	// Member field should be private or
	// protected and accessed with getters/setters.
	//
	// This some what long winded approach is used to
	// encapsulate data and stop unwanted side effects
	// in the code. Ie we try to keep operations atomic,
	// local and encapsulated :)
	// 
	// This is long winded and stupid seeming if you are
	// used to python. However long term ownership of the 
	// code is cheaper - honest! :)
	private String path;
	private String name;
	private IDiffractionMetadata metaData;
	private IDataset image;
	private QSpace q;
	private double od = Double.NaN;
	private double distance;
	private boolean use = false;
	private IPowderCalibrationInfo calibrationInfo;
	
	/**
	 *  TODO FIXME rois is not encapsulated, external classes can edit it.
	 *  This leads to complexity in the software design. Instead no getter/setter
	 *  should exist for rois, create add, remove, iterable methods to
	 *  keep the list local to this class. 
	 */
	private List<IROI> rois; // can contain null entries as placeholders
	private int nrois = -1; // number of actual ROIs found

	
	// Auto-generated stuff - thanks eclipse!
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((calibrationInfo == null) ? 0 : calibrationInfo.hashCode());
		long temp;
		temp = Double.doubleToLongBits(distance);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((image == null) ? 0 : image.hashCode());
		result = prime * result
				+ ((metaData == null) ? 0 : metaData.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + nrois;
		temp = Double.doubleToLongBits(od);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((path == null) ? 0 : path.hashCode());
		result = prime * result + ((q == null) ? 0 : q.hashCode());
		result = prime * result + ((rois == null) ? 0 : rois.hashCode());
		result = prime * result + (use ? 1231 : 1237);
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
		DiffractionTableData other = (DiffractionTableData) obj;
		if (calibrationInfo == null) {
			if (other.calibrationInfo != null)
				return false;
		} else if (!calibrationInfo.equals(other.calibrationInfo))
			return false;
		if (Double.doubleToLongBits(distance) != Double
				.doubleToLongBits(other.distance))
			return false;
		if (image == null) {
			if (other.image != null)
				return false;
		} else if (!image.equals(other.image))
			return false;
		if (metaData == null) {
			if (other.metaData != null)
				return false;
		} else if (!metaData.equals(other.metaData))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (nrois != other.nrois)
			return false;
		if (Double.doubleToLongBits(od) != Double.doubleToLongBits(other.od))
			return false;
		if (path == null) {
			if (other.path != null)
				return false;
		} else if (!path.equals(other.path))
			return false;
		if (q == null) {
			if (other.q != null)
				return false;
		} else if (!q.equals(other.q))
			return false;
		if (rois == null) {
			if (other.rois != null)
				return false;
		} else if (!rois.equals(other.rois))
			return false;
		if (use != other.use)
			return false;
		return true;
	}
	
	
	public String getPath() {
		return path;
	}
	
	public void setPath(String path) {
		this.path = path;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public IDiffractionMetadata getMetaData() {
		return metaData;
	}
	public void setMetaData(IDiffractionMetadata md) {
		this.metaData = md;
	}
	public IDataset getImage() {
		return image;
	}
	public void setImage(IDataset image) {
		this.image = image;
	}
	public List<IROI> getRois() {
		return rois;
	}
	public void setRois(List<IROI> rois) {
		this.rois = rois;
	}
	public QSpace getQ() {
		return q;
	}
	public void setQ(QSpace q) {
		this.q = q;
	}
	public double getOd() {
		return od;
	}
	public void setOd(double od) {
		this.od = od;
	}
	public double getDistance() {
		return distance;
	}
	public void setDistance(double distance) {
		this.distance = distance;
	}
	public int getNrois() {
		return nrois;
	}
	public void setNrois(int nrois) {
		this.nrois = nrois;
	}
	public boolean isUse() {
		return use;
	}
	public void setUse(boolean use) {
		this.use = use;
	}
	public double getResidual() {
		
		if (calibrationInfo != null) return calibrationInfo.getResidual();
		else return 0;
	}

	public IPowderCalibrationInfo getCalibrationInfo() {
		return calibrationInfo;
	}
	public void setCalibrationInfo(IPowderCalibrationInfo calibrationInfo) {
		this.calibrationInfo = calibrationInfo;
	}
}
