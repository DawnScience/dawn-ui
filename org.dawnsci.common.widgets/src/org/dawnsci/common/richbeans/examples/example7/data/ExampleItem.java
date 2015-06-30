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

package org.dawnsci.common.richbeans.examples.example7.data;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;

/**
 *
 */
public class ExampleItem {
	
	public enum ItemChoice {
		XY, POLAR;

		public static Map<String, ItemChoice> names() {
			final Map<String,ItemChoice> ret = new HashMap<String,ItemChoice>(2);
			ret.put("X-Y Graph", XY);
			ret.put("Polar",     POLAR);
			return ret;
		}
	}

	private String     itemName;
	private ItemChoice choice = ItemChoice.XY;
	private Double x,y;
	private double r,theta;
	
	private double d0, d1,d2,d3,d4,d5,d6,d7,d8, d9;
	private double d10, d11,d12,d13,d14,d15,d16,d17,d18, d19;
	private double d20, d21,d22,d23,d24,d25,d26,d27,d28, d29;
	private double d30, d31,d32,d33,d34,d35,d36,d37,d38, d39;
	private double d40, d41,d42,d43,d44,d45,d46,d47,d48, d49;
	private double d50, d51,d52,d53,d54,d55,d56,d57,d58, d59;
	private double d60, d61,d62,d63,d64,d65,d66,d67,d68, d69;
	private double d70, d71,d72,d73,d74,d75,d76,d77,d78, d79;
	private double d80, d81,d82,d83,d84,d85,d86,d87,d88, d89;
	private double d90, d91,d92,d93,d94,d95,d96,d97,d98, d99;
	
    public ExampleItem() {
    	this(1,1);
    }
    private static int INDEX = 0;
    
	public ExampleItem(double i, double j) {
		x = i; y = j;
		itemName = "Fred"+(++INDEX);
	}
	
	public ExampleItem(double i, double j, ItemChoice choice) {
		this.choice = choice;
		if (choice == ItemChoice.POLAR) {
			r = i; theta = j;
		} else {
			x = i; y = j;
		}
		itemName = "Fred"+(++INDEX);
	}

	public ItemChoice getChoice() {
		return choice;
	}

	public void setChoice(ItemChoice choice) {
		this.choice = choice;
	}
	
	public String getItemName() {
		return itemName;
	}

	public void setItemName(String name) {
		this.itemName = name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((choice == null) ? 0 : choice.hashCode());
		long temp;
		temp = Double.doubleToLongBits(d0);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(d1);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(d10);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(d11);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(d12);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(d13);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(d14);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(d15);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(d16);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(d17);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(d18);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(d19);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(d2);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(d20);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(d21);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(d22);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(d23);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(d24);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(d25);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(d26);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(d27);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(d28);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(d29);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(d3);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(d30);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(d31);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(d32);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(d33);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(d34);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(d35);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(d36);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(d37);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(d38);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(d39);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(d4);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(d40);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(d41);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(d42);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(d43);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(d44);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(d45);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(d46);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(d47);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(d48);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(d49);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(d5);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(d50);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(d51);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(d52);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(d53);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(d54);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(d55);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(d56);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(d57);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(d58);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(d59);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(d6);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(d60);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(d61);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(d62);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(d63);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(d64);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(d65);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(d66);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(d67);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(d68);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(d69);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(d7);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(d70);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(d71);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(d72);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(d73);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(d74);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(d75);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(d76);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(d77);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(d78);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(d79);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(d8);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(d80);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(d81);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(d82);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(d83);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(d84);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(d85);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(d86);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(d87);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(d88);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(d89);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(d9);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(d90);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(d91);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(d92);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(d93);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(d94);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(d95);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(d96);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(d97);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(d98);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(d99);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result
				+ ((itemName == null) ? 0 : itemName.hashCode());
		temp = Double.doubleToLongBits(r);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(theta);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((x == null) ? 0 : x.hashCode());
		result = prime * result + ((y == null) ? 0 : y.hashCode());
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
		ExampleItem other = (ExampleItem) obj;
		if (choice != other.choice)
			return false;
		if (Double.doubleToLongBits(d0) != Double.doubleToLongBits(other.d0))
			return false;
		if (Double.doubleToLongBits(d1) != Double.doubleToLongBits(other.d1))
			return false;
		if (Double.doubleToLongBits(d10) != Double.doubleToLongBits(other.d10))
			return false;
		if (Double.doubleToLongBits(d11) != Double.doubleToLongBits(other.d11))
			return false;
		if (Double.doubleToLongBits(d12) != Double.doubleToLongBits(other.d12))
			return false;
		if (Double.doubleToLongBits(d13) != Double.doubleToLongBits(other.d13))
			return false;
		if (Double.doubleToLongBits(d14) != Double.doubleToLongBits(other.d14))
			return false;
		if (Double.doubleToLongBits(d15) != Double.doubleToLongBits(other.d15))
			return false;
		if (Double.doubleToLongBits(d16) != Double.doubleToLongBits(other.d16))
			return false;
		if (Double.doubleToLongBits(d17) != Double.doubleToLongBits(other.d17))
			return false;
		if (Double.doubleToLongBits(d18) != Double.doubleToLongBits(other.d18))
			return false;
		if (Double.doubleToLongBits(d19) != Double.doubleToLongBits(other.d19))
			return false;
		if (Double.doubleToLongBits(d2) != Double.doubleToLongBits(other.d2))
			return false;
		if (Double.doubleToLongBits(d20) != Double.doubleToLongBits(other.d20))
			return false;
		if (Double.doubleToLongBits(d21) != Double.doubleToLongBits(other.d21))
			return false;
		if (Double.doubleToLongBits(d22) != Double.doubleToLongBits(other.d22))
			return false;
		if (Double.doubleToLongBits(d23) != Double.doubleToLongBits(other.d23))
			return false;
		if (Double.doubleToLongBits(d24) != Double.doubleToLongBits(other.d24))
			return false;
		if (Double.doubleToLongBits(d25) != Double.doubleToLongBits(other.d25))
			return false;
		if (Double.doubleToLongBits(d26) != Double.doubleToLongBits(other.d26))
			return false;
		if (Double.doubleToLongBits(d27) != Double.doubleToLongBits(other.d27))
			return false;
		if (Double.doubleToLongBits(d28) != Double.doubleToLongBits(other.d28))
			return false;
		if (Double.doubleToLongBits(d29) != Double.doubleToLongBits(other.d29))
			return false;
		if (Double.doubleToLongBits(d3) != Double.doubleToLongBits(other.d3))
			return false;
		if (Double.doubleToLongBits(d30) != Double.doubleToLongBits(other.d30))
			return false;
		if (Double.doubleToLongBits(d31) != Double.doubleToLongBits(other.d31))
			return false;
		if (Double.doubleToLongBits(d32) != Double.doubleToLongBits(other.d32))
			return false;
		if (Double.doubleToLongBits(d33) != Double.doubleToLongBits(other.d33))
			return false;
		if (Double.doubleToLongBits(d34) != Double.doubleToLongBits(other.d34))
			return false;
		if (Double.doubleToLongBits(d35) != Double.doubleToLongBits(other.d35))
			return false;
		if (Double.doubleToLongBits(d36) != Double.doubleToLongBits(other.d36))
			return false;
		if (Double.doubleToLongBits(d37) != Double.doubleToLongBits(other.d37))
			return false;
		if (Double.doubleToLongBits(d38) != Double.doubleToLongBits(other.d38))
			return false;
		if (Double.doubleToLongBits(d39) != Double.doubleToLongBits(other.d39))
			return false;
		if (Double.doubleToLongBits(d4) != Double.doubleToLongBits(other.d4))
			return false;
		if (Double.doubleToLongBits(d40) != Double.doubleToLongBits(other.d40))
			return false;
		if (Double.doubleToLongBits(d41) != Double.doubleToLongBits(other.d41))
			return false;
		if (Double.doubleToLongBits(d42) != Double.doubleToLongBits(other.d42))
			return false;
		if (Double.doubleToLongBits(d43) != Double.doubleToLongBits(other.d43))
			return false;
		if (Double.doubleToLongBits(d44) != Double.doubleToLongBits(other.d44))
			return false;
		if (Double.doubleToLongBits(d45) != Double.doubleToLongBits(other.d45))
			return false;
		if (Double.doubleToLongBits(d46) != Double.doubleToLongBits(other.d46))
			return false;
		if (Double.doubleToLongBits(d47) != Double.doubleToLongBits(other.d47))
			return false;
		if (Double.doubleToLongBits(d48) != Double.doubleToLongBits(other.d48))
			return false;
		if (Double.doubleToLongBits(d49) != Double.doubleToLongBits(other.d49))
			return false;
		if (Double.doubleToLongBits(d5) != Double.doubleToLongBits(other.d5))
			return false;
		if (Double.doubleToLongBits(d50) != Double.doubleToLongBits(other.d50))
			return false;
		if (Double.doubleToLongBits(d51) != Double.doubleToLongBits(other.d51))
			return false;
		if (Double.doubleToLongBits(d52) != Double.doubleToLongBits(other.d52))
			return false;
		if (Double.doubleToLongBits(d53) != Double.doubleToLongBits(other.d53))
			return false;
		if (Double.doubleToLongBits(d54) != Double.doubleToLongBits(other.d54))
			return false;
		if (Double.doubleToLongBits(d55) != Double.doubleToLongBits(other.d55))
			return false;
		if (Double.doubleToLongBits(d56) != Double.doubleToLongBits(other.d56))
			return false;
		if (Double.doubleToLongBits(d57) != Double.doubleToLongBits(other.d57))
			return false;
		if (Double.doubleToLongBits(d58) != Double.doubleToLongBits(other.d58))
			return false;
		if (Double.doubleToLongBits(d59) != Double.doubleToLongBits(other.d59))
			return false;
		if (Double.doubleToLongBits(d6) != Double.doubleToLongBits(other.d6))
			return false;
		if (Double.doubleToLongBits(d60) != Double.doubleToLongBits(other.d60))
			return false;
		if (Double.doubleToLongBits(d61) != Double.doubleToLongBits(other.d61))
			return false;
		if (Double.doubleToLongBits(d62) != Double.doubleToLongBits(other.d62))
			return false;
		if (Double.doubleToLongBits(d63) != Double.doubleToLongBits(other.d63))
			return false;
		if (Double.doubleToLongBits(d64) != Double.doubleToLongBits(other.d64))
			return false;
		if (Double.doubleToLongBits(d65) != Double.doubleToLongBits(other.d65))
			return false;
		if (Double.doubleToLongBits(d66) != Double.doubleToLongBits(other.d66))
			return false;
		if (Double.doubleToLongBits(d67) != Double.doubleToLongBits(other.d67))
			return false;
		if (Double.doubleToLongBits(d68) != Double.doubleToLongBits(other.d68))
			return false;
		if (Double.doubleToLongBits(d69) != Double.doubleToLongBits(other.d69))
			return false;
		if (Double.doubleToLongBits(d7) != Double.doubleToLongBits(other.d7))
			return false;
		if (Double.doubleToLongBits(d70) != Double.doubleToLongBits(other.d70))
			return false;
		if (Double.doubleToLongBits(d71) != Double.doubleToLongBits(other.d71))
			return false;
		if (Double.doubleToLongBits(d72) != Double.doubleToLongBits(other.d72))
			return false;
		if (Double.doubleToLongBits(d73) != Double.doubleToLongBits(other.d73))
			return false;
		if (Double.doubleToLongBits(d74) != Double.doubleToLongBits(other.d74))
			return false;
		if (Double.doubleToLongBits(d75) != Double.doubleToLongBits(other.d75))
			return false;
		if (Double.doubleToLongBits(d76) != Double.doubleToLongBits(other.d76))
			return false;
		if (Double.doubleToLongBits(d77) != Double.doubleToLongBits(other.d77))
			return false;
		if (Double.doubleToLongBits(d78) != Double.doubleToLongBits(other.d78))
			return false;
		if (Double.doubleToLongBits(d79) != Double.doubleToLongBits(other.d79))
			return false;
		if (Double.doubleToLongBits(d8) != Double.doubleToLongBits(other.d8))
			return false;
		if (Double.doubleToLongBits(d80) != Double.doubleToLongBits(other.d80))
			return false;
		if (Double.doubleToLongBits(d81) != Double.doubleToLongBits(other.d81))
			return false;
		if (Double.doubleToLongBits(d82) != Double.doubleToLongBits(other.d82))
			return false;
		if (Double.doubleToLongBits(d83) != Double.doubleToLongBits(other.d83))
			return false;
		if (Double.doubleToLongBits(d84) != Double.doubleToLongBits(other.d84))
			return false;
		if (Double.doubleToLongBits(d85) != Double.doubleToLongBits(other.d85))
			return false;
		if (Double.doubleToLongBits(d86) != Double.doubleToLongBits(other.d86))
			return false;
		if (Double.doubleToLongBits(d87) != Double.doubleToLongBits(other.d87))
			return false;
		if (Double.doubleToLongBits(d88) != Double.doubleToLongBits(other.d88))
			return false;
		if (Double.doubleToLongBits(d89) != Double.doubleToLongBits(other.d89))
			return false;
		if (Double.doubleToLongBits(d9) != Double.doubleToLongBits(other.d9))
			return false;
		if (Double.doubleToLongBits(d90) != Double.doubleToLongBits(other.d90))
			return false;
		if (Double.doubleToLongBits(d91) != Double.doubleToLongBits(other.d91))
			return false;
		if (Double.doubleToLongBits(d92) != Double.doubleToLongBits(other.d92))
			return false;
		if (Double.doubleToLongBits(d93) != Double.doubleToLongBits(other.d93))
			return false;
		if (Double.doubleToLongBits(d94) != Double.doubleToLongBits(other.d94))
			return false;
		if (Double.doubleToLongBits(d95) != Double.doubleToLongBits(other.d95))
			return false;
		if (Double.doubleToLongBits(d96) != Double.doubleToLongBits(other.d96))
			return false;
		if (Double.doubleToLongBits(d97) != Double.doubleToLongBits(other.d97))
			return false;
		if (Double.doubleToLongBits(d98) != Double.doubleToLongBits(other.d98))
			return false;
		if (Double.doubleToLongBits(d99) != Double.doubleToLongBits(other.d99))
			return false;
		if (itemName == null) {
			if (other.itemName != null)
				return false;
		} else if (!itemName.equals(other.itemName))
			return false;
		if (Double.doubleToLongBits(r) != Double.doubleToLongBits(other.r))
			return false;
		if (Double.doubleToLongBits(theta) != Double
				.doubleToLongBits(other.theta))
			return false;
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

	/**
	 * @return the x
	 */
	public Double getX() {
		return x;
	}

	/**
	 * @param x the x to set
	 */
	public void setX(Double x) {
		this.x = x;
	}

	/**
	 * @return the y
	 */
	public Double getY() {
		return y;
	}

	/**
	 * @param y the y to set
	 */
	public void setY(Double y) {
		this.y = y;
	}
	
	
	public double getR() {
		return r;
	}

	public void setR(double r) {
		this.r = r;
	}

	public double getTheta() {
		return theta;
	}

	public void setTheta(double theta) {
		this.theta = theta;
	}

	@Override
	public String toString() {
		try {
			return BeanUtils.describe(this).toString();
		} catch (Exception e) {
			return e.getMessage();
		}
	}

	public double getD0() {
		return d0;
	}

	public void setD0(double d0) {
		this.d0 = d0;
	}

	public double getD1() {
		return d1;
	}

	public void setD1(double d1) {
		this.d1 = d1;
	}

	public double getD2() {
		return d2;
	}

	public void setD2(double d2) {
		this.d2 = d2;
	}

	public double getD3() {
		return d3;
	}

	public void setD3(double d3) {
		this.d3 = d3;
	}

	public double getD4() {
		return d4;
	}

	public void setD4(double d4) {
		this.d4 = d4;
	}

	public double getD5() {
		return d5;
	}

	public void setD5(double d5) {
		this.d5 = d5;
	}

	public double getD6() {
		return d6;
	}

	public void setD6(double d6) {
		this.d6 = d6;
	}

	public double getD7() {
		return d7;
	}

	public void setD7(double d7) {
		this.d7 = d7;
	}

	public double getD8() {
		return d8;
	}

	public void setD8(double d8) {
		this.d8 = d8;
	}

	public double getD9() {
		return d9;
	}

	public void setD9(double d9) {
		this.d9 = d9;
	}

	public double getD10() {
		return d10;
	}

	public void setD10(double d10) {
		this.d10 = d10;
	}

	public double getD11() {
		return d11;
	}

	public void setD11(double d11) {
		this.d11 = d11;
	}

	public double getD12() {
		return d12;
	}

	public void setD12(double d12) {
		this.d12 = d12;
	}

	public double getD13() {
		return d13;
	}

	public void setD13(double d13) {
		this.d13 = d13;
	}

	public double getD14() {
		return d14;
	}

	public void setD14(double d14) {
		this.d14 = d14;
	}

	public double getD15() {
		return d15;
	}

	public void setD15(double d15) {
		this.d15 = d15;
	}

	public double getD16() {
		return d16;
	}

	public void setD16(double d16) {
		this.d16 = d16;
	}

	public double getD17() {
		return d17;
	}

	public void setD17(double d17) {
		this.d17 = d17;
	}

	public double getD18() {
		return d18;
	}

	public void setD18(double d18) {
		this.d18 = d18;
	}

	public double getD19() {
		return d19;
	}

	public void setD19(double d19) {
		this.d19 = d19;
	}

	public double getD20() {
		return d20;
	}

	public void setD20(double d20) {
		this.d20 = d20;
	}

	public double getD21() {
		return d21;
	}

	public void setD21(double d21) {
		this.d21 = d21;
	}

	public double getD22() {
		return d22;
	}

	public void setD22(double d22) {
		this.d22 = d22;
	}

	public double getD23() {
		return d23;
	}

	public void setD23(double d23) {
		this.d23 = d23;
	}

	public double getD24() {
		return d24;
	}

	public void setD24(double d24) {
		this.d24 = d24;
	}

	public double getD25() {
		return d25;
	}

	public void setD25(double d25) {
		this.d25 = d25;
	}

	public double getD26() {
		return d26;
	}

	public void setD26(double d26) {
		this.d26 = d26;
	}

	public double getD27() {
		return d27;
	}

	public void setD27(double d27) {
		this.d27 = d27;
	}

	public double getD28() {
		return d28;
	}

	public void setD28(double d28) {
		this.d28 = d28;
	}

	public double getD29() {
		return d29;
	}

	public void setD29(double d29) {
		this.d29 = d29;
	}

	public double getD30() {
		return d30;
	}

	public void setD30(double d30) {
		this.d30 = d30;
	}

	public double getD31() {
		return d31;
	}

	public void setD31(double d31) {
		this.d31 = d31;
	}

	public double getD32() {
		return d32;
	}

	public void setD32(double d32) {
		this.d32 = d32;
	}

	public double getD33() {
		return d33;
	}

	public void setD33(double d33) {
		this.d33 = d33;
	}

	public double getD34() {
		return d34;
	}

	public void setD34(double d34) {
		this.d34 = d34;
	}

	public double getD35() {
		return d35;
	}

	public void setD35(double d35) {
		this.d35 = d35;
	}

	public double getD36() {
		return d36;
	}

	public void setD36(double d36) {
		this.d36 = d36;
	}

	public double getD37() {
		return d37;
	}

	public void setD37(double d37) {
		this.d37 = d37;
	}

	public double getD38() {
		return d38;
	}

	public void setD38(double d38) {
		this.d38 = d38;
	}

	public double getD39() {
		return d39;
	}

	public void setD39(double d39) {
		this.d39 = d39;
	}

	public double getD40() {
		return d40;
	}

	public void setD40(double d40) {
		this.d40 = d40;
	}

	public double getD41() {
		return d41;
	}

	public void setD41(double d41) {
		this.d41 = d41;
	}

	public double getD42() {
		return d42;
	}

	public void setD42(double d42) {
		this.d42 = d42;
	}

	public double getD43() {
		return d43;
	}

	public void setD43(double d43) {
		this.d43 = d43;
	}

	public double getD44() {
		return d44;
	}

	public void setD44(double d44) {
		this.d44 = d44;
	}

	public double getD45() {
		return d45;
	}

	public void setD45(double d45) {
		this.d45 = d45;
	}

	public double getD46() {
		return d46;
	}

	public void setD46(double d46) {
		this.d46 = d46;
	}

	public double getD47() {
		return d47;
	}

	public void setD47(double d47) {
		this.d47 = d47;
	}

	public double getD48() {
		return d48;
	}

	public void setD48(double d48) {
		this.d48 = d48;
	}

	public double getD49() {
		return d49;
	}

	public void setD49(double d49) {
		this.d49 = d49;
	}

	public double getD50() {
		return d50;
	}

	public void setD50(double d50) {
		this.d50 = d50;
	}

	public double getD51() {
		return d51;
	}

	public void setD51(double d51) {
		this.d51 = d51;
	}

	public double getD52() {
		return d52;
	}

	public void setD52(double d52) {
		this.d52 = d52;
	}

	public double getD53() {
		return d53;
	}

	public void setD53(double d53) {
		this.d53 = d53;
	}

	public double getD54() {
		return d54;
	}

	public void setD54(double d54) {
		this.d54 = d54;
	}

	public double getD55() {
		return d55;
	}

	public void setD55(double d55) {
		this.d55 = d55;
	}

	public double getD56() {
		return d56;
	}

	public void setD56(double d56) {
		this.d56 = d56;
	}

	public double getD57() {
		return d57;
	}

	public void setD57(double d57) {
		this.d57 = d57;
	}

	public double getD58() {
		return d58;
	}

	public void setD58(double d58) {
		this.d58 = d58;
	}

	public double getD59() {
		return d59;
	}

	public void setD59(double d59) {
		this.d59 = d59;
	}

	public double getD60() {
		return d60;
	}

	public void setD60(double d60) {
		this.d60 = d60;
	}

	public double getD61() {
		return d61;
	}

	public void setD61(double d61) {
		this.d61 = d61;
	}

	public double getD62() {
		return d62;
	}

	public void setD62(double d62) {
		this.d62 = d62;
	}

	public double getD63() {
		return d63;
	}

	public void setD63(double d63) {
		this.d63 = d63;
	}

	public double getD64() {
		return d64;
	}

	public void setD64(double d64) {
		this.d64 = d64;
	}

	public double getD65() {
		return d65;
	}

	public void setD65(double d65) {
		this.d65 = d65;
	}

	public double getD66() {
		return d66;
	}

	public void setD66(double d66) {
		this.d66 = d66;
	}

	public double getD67() {
		return d67;
	}

	public void setD67(double d67) {
		this.d67 = d67;
	}

	public double getD68() {
		return d68;
	}

	public void setD68(double d68) {
		this.d68 = d68;
	}

	public double getD69() {
		return d69;
	}

	public void setD69(double d69) {
		this.d69 = d69;
	}

	public double getD70() {
		return d70;
	}

	public void setD70(double d70) {
		this.d70 = d70;
	}

	public double getD71() {
		return d71;
	}

	public void setD71(double d71) {
		this.d71 = d71;
	}

	public double getD72() {
		return d72;
	}

	public void setD72(double d72) {
		this.d72 = d72;
	}

	public double getD73() {
		return d73;
	}

	public void setD73(double d73) {
		this.d73 = d73;
	}

	public double getD74() {
		return d74;
	}

	public void setD74(double d74) {
		this.d74 = d74;
	}

	public double getD75() {
		return d75;
	}

	public void setD75(double d75) {
		this.d75 = d75;
	}

	public double getD76() {
		return d76;
	}

	public void setD76(double d76) {
		this.d76 = d76;
	}

	public double getD77() {
		return d77;
	}

	public void setD77(double d77) {
		this.d77 = d77;
	}

	public double getD78() {
		return d78;
	}

	public void setD78(double d78) {
		this.d78 = d78;
	}

	public double getD79() {
		return d79;
	}

	public void setD79(double d79) {
		this.d79 = d79;
	}

	public double getD80() {
		return d80;
	}

	public void setD80(double d80) {
		this.d80 = d80;
	}

	public double getD81() {
		return d81;
	}

	public void setD81(double d81) {
		this.d81 = d81;
	}

	public double getD82() {
		return d82;
	}

	public void setD82(double d82) {
		this.d82 = d82;
	}

	public double getD83() {
		return d83;
	}

	public void setD83(double d83) {
		this.d83 = d83;
	}

	public double getD84() {
		return d84;
	}

	public void setD84(double d84) {
		this.d84 = d84;
	}

	public double getD85() {
		return d85;
	}

	public void setD85(double d85) {
		this.d85 = d85;
	}

	public double getD86() {
		return d86;
	}

	public void setD86(double d86) {
		this.d86 = d86;
	}

	public double getD87() {
		return d87;
	}

	public void setD87(double d87) {
		this.d87 = d87;
	}

	public double getD88() {
		return d88;
	}

	public void setD88(double d88) {
		this.d88 = d88;
	}

	public double getD89() {
		return d89;
	}

	public void setD89(double d89) {
		this.d89 = d89;
	}

	public double getD90() {
		return d90;
	}

	public void setD90(double d90) {
		this.d90 = d90;
	}

	public double getD91() {
		return d91;
	}

	public void setD91(double d91) {
		this.d91 = d91;
	}

	public double getD92() {
		return d92;
	}

	public void setD92(double d92) {
		this.d92 = d92;
	}

	public double getD93() {
		return d93;
	}

	public void setD93(double d93) {
		this.d93 = d93;
	}

	public double getD94() {
		return d94;
	}

	public void setD94(double d94) {
		this.d94 = d94;
	}

	public double getD95() {
		return d95;
	}

	public void setD95(double d95) {
		this.d95 = d95;
	}

	public double getD96() {
		return d96;
	}

	public void setD96(double d96) {
		this.d96 = d96;
	}

	public double getD97() {
		return d97;
	}

	public void setD97(double d97) {
		this.d97 = d97;
	}

	public double getD98() {
		return d98;
	}

	public void setD98(double d98) {
		this.d98 = d98;
	}

	public double getD99() {
		return d99;
	}

	public void setD99(double d99) {
		this.d99 = d99;
	}
}	