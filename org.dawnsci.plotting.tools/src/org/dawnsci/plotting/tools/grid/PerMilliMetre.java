package org.dawnsci.plotting.tools.grid;

import javax.measure.converter.RationalConverter;
import javax.measure.converter.UnitConverter;
import javax.measure.unit.Unit;

public class PerMilliMetre extends Unit<Resolution>{

	private static final long serialVersionUID = 1170501988959781081L;

	public PerMilliMetre() {
		super();
	}
	
	@Override
	public boolean equals(Object arg0) {
		return false;
	}

	@Override
	public Unit<? super Resolution> getStandardUnit() {
		return Resolution.UNIT;
	}

	@Override
	public int hashCode() {
		return 0;
	}

	@Override
	public UnitConverter toStandardUnit() {
		return new RationalConverter(1000,1);
	}


}
