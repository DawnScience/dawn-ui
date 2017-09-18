package org.dawnsci.datavis.model;

import org.eclipse.swt.graphics.RGB;

public class TraceColorProvider implements ITraceColourProvider {

	private String name;
	private RGB[] rgbs;

	public TraceColorProvider(String name, RGB[] rgbs) {
		this.name = name;
		this.rgbs = rgbs;
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public RGB[] getRGBs() {
		return rgbs;
	}

}
