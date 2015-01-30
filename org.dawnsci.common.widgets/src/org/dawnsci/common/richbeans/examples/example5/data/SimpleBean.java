package org.dawnsci.common.richbeans.examples.example5.data;

import java.io.Serializable;

import org.dawnsci.doe.DOEField;


/**
 * A simple bean defining an x and y value. 
 * 
 * @author fcp94556
 *
 */
public class SimpleBean implements Serializable { // In order to use beans with DOE

	@DOEField(value=1, type=java.lang.Double.class)
	private String x;
	@DOEField(value=1, type=java.lang.Integer.class)
	private String y;
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((x == null) ? 0 : x.hashCode());
		result = prime * result + ((y == null) ? 0 : y.hashCode());
		return result;
	}

	public String getX() {
		return x;
	}

	public void setX(String x) {
		this.x = x;
	}

	public String getY() {
		return y;
	}

	public void setY(String y) {
		this.y = y;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SimpleBean other = (SimpleBean) obj;
		if (x == null) {
			if (other.x != null)
				return false;
		} else if (!x.equals(other.x))
			return false;
		if (y == null) {
			if (other.y != null)
				return false;
		} else if (!y.equals(other.y))
			return false;
		return true;
	}
}
