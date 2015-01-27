/*-
 * Copyright © 2009 Diamond Light Source Ltd.
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 */

package org.dawnsci.common.richbeans.examples.example2;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.beanutils.BeanUtils;

/**
 * Example bean.
 * @author Matthew Gerring
 *
 */
public class ExampleParameters {

	private String element;
	private String edge;
	private Double start;
	private Double stop;
	private List<ExampleItem> items;
	
	/**
	 * 
	 */
	public ExampleParameters() {
		items = new ArrayList<ExampleItem>(31);
	}
	/**
	 * Must implement clear() method on beans being used with BeanUI.
	 */
	public void clear() {
		element = null;
		edge    = null;
		start   = null;
		stop    = null;
		items.clear();
	}
	/**
	 * @return the element
	 */
	public String getElement() {
		return element;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((edge == null) ? 0 : edge.hashCode());
		result = prime * result + ((element == null) ? 0 : element.hashCode());
		result = prime * result + ((items == null) ? 0 : items.hashCode());
		result = prime * result + ((start == null) ? 0 : start.hashCode());
		result = prime * result + ((stop == null) ? 0 : stop.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		ExampleParameters other = (ExampleParameters) obj;
		if (edge == null) {
			if (other.edge != null) {
				return false;
			}
		} else if (!edge.equals(other.edge)) {
			return false;
		}
		if (element == null) {
			if (other.element != null) {
				return false;
			}
		} else if (!element.equals(other.element)) {
			return false;
		}
		if (items == null) {
			if (other.items != null) {
				return false;
			}
		} else if (!items.equals(other.items)) {
			return false;
		}
		if (start == null) {
			if (other.start != null) {
				return false;
			}
		} else if (!start.equals(other.start)) {
			return false;
		}
		if (stop == null) {
			if (other.stop != null) {
				return false;
			}
		} else if (!stop.equals(other.stop)) {
			return false;
		}
		return true;
	}
	/**
	 * @param element the element to set
	 */
	public void setElement(String element) {
		this.element = element;
	}
	/**
	 * @return the edge
	 */
	public String getEdge() {
		return edge;
	}
	/**
	 * @param edge the edge to set
	 */
	public void setEdge(String edge) {
		this.edge = edge;
	}
	/**
	 * @return the start
	 */
	public Double getStart() {
		return start;
	}
	/**
	 * @param start the start to set
	 */
	public void setStart(Double start) {
		this.start = start;
	}
	/**
	 * @return the stop
	 */
	public Double getStop() {
		return stop;
	}
	/**
	 * @param stop the stop to set
	 */
	public void setStop(Double stop) {
		this.stop = stop;
	}
	/**
	 * @return the items
	 */
	public List<ExampleItem> getItems() {
		return items;
	}
	/**
	 * @param items the items to set
	 */
	public void setItems(List<ExampleItem> items) {
		this.items = items;
	}
	/**
	 * 
	 * @param item
	 */
	public void addItem(ExampleItem item) {
		items.add(item);
	}
	
	/**
	 *
	 */
	@Override
	public String toString() {
		try {
			return BeanUtils.describe(this).toString();
		} catch (Exception e) {
			return e.getMessage();
		}
	}


}

	