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

package org.dawnsci.common.richbeans.examples.example4.data;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.beanutils.BeanUtils;

/**
 * Example bean.
 * @author Matthew Gerring
 *
 */
public class ExampleBean {

	private List<ExampleItem> items;
	
	/**
	 * 
	 */
	public ExampleBean() {
		items = new ArrayList<ExampleItem>(31);
	}
	/**
	 * Must implement clear() method on beans being used with BeanUI.
	 */
	public void clear() {
		items.clear();
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

	