package org.dawnsci.plotting.tools.grid;

import javax.measure.Measure;
import javax.measure.converter.UnitConverter;
import javax.measure.quantity.Length;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;
import javax.measure.unit.UnitFormat;

public class Pixel extends Unit<Length>{

	private static final long serialVersionUID = 1L;

	public static void main(String[] args) {
		
		Unit<Resolution> ppmm = new PerMilliMetre();
		Measure<Double, Resolution> res = Measure.valueOf(2.0, ppmm);
		
		Pixel pixel = new Pixel(res, 40);
		Measure<Double, Length> distInPixels = Measure.valueOf(1000.0, pixel);
		
		System.out.println(distInPixels.to(pixel));
		System.out.println(distInPixels.to(SI.METRE));
		System.out.println(distInPixels.to(SI.MILLI(SI.METRE)));
		System.out.println(distInPixels.to(NonSI.FOOT));

	}

	public static Pixel pixel() {
		return new Pixel();
	}
	
	private PixelConverter converter;
	
	public Pixel() {
		this(Measure.valueOf(1.0, new PerMilliMetre()), 0, "px");
	}
	
	public Pixel(String label) {
		this(Measure.valueOf(1.0, new PerMilliMetre()), 0, label);
	}
	
	public Pixel(Measure<Double, Resolution> resolution, double offset) {
		this(resolution, offset, "px");
	}
	
	public Pixel(Measure<Double, Resolution> resolution, double offset, String label) {
		this.converter = new PixelConverter(resolution, offset);
		UnitFormat.getInstance().label(this, label);
	}
	
	public Measure<Double, Resolution> getResolution() {
		return this.converter.getResolution();
	}
	public void setPixelsPerMm(double ppmm) {
		this.converter.setPixelsPerMm(ppmm);
	}
	public void setResolution(Measure<Double, Resolution> resolution) {
		this.converter.setResolution(resolution);
	}
	public void setOffset(double offset) {
		this.converter.setOffset(offset);
	}
	
	@Override
	public boolean equals(Object arg0) {
		if (!(arg0 instanceof Pixel))
			return false;
		if (arg0 == this)
			return true;
		return false;
	}

	@Override
	public Unit<? super Length> getStandardUnit() {
		return SI.METRE;
	}

	@Override
	public int hashCode() {
		return 0;
	}

	@Override
	public UnitConverter toStandardUnit() {
		return converter;
	}
}
