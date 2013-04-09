package org.dawnsci.plotting.api.trace;

import java.util.EventObject;

public class DownSampleEvent extends EventObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3415791000678721675L;
	private final int bin;
	
	/**
	 * 
	 * @param source
	 */
	public DownSampleEvent(IImageTrace source, int bin) {
		super(source);
		this.bin = bin;
	}

	public int getBin() {
		return bin;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + bin;
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
		DownSampleEvent other = (DownSampleEvent) obj;
		if (bin != other.bin)
			return false;
		return true;
	}


}
