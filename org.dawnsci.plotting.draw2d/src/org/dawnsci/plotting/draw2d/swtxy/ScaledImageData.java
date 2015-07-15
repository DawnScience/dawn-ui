package org.dawnsci.plotting.draw2d.swtxy;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;

/**
 * Class to hold data and dispose of images.
 * 
 * @author fcp94556
 */
class ScaledImageData {

	private ImageData downsampledImageData;
	private Image scaledImage;
	private double xoffset;
	private double yoffset;
	private double x;
	private double y;
	
	/**
	 * Must call this prior to setting more images such that widget can be cleared up.
	 */
	public void disposeImage() {
		if (scaledImage!=null && !scaledImage.isDisposed()) scaledImage.dispose(); // IMPORTANT		
		scaledImage = null;
	}
	
	public Image getScaledImage() {
		return scaledImage;
	}
	public void setScaledImage(Image scaledImage) {
		this.scaledImage = scaledImage;
	}
	public double getXoffset() {
		return xoffset;
	}
	public void setXoffset(double xoffset) {
		this.xoffset = xoffset;
	}
	public double getYoffset() {
		return yoffset;
	}
	public void setYoffset(double yoffset) {
		this.yoffset = yoffset;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((downsampledImageData == null) ? 0 : downsampledImageData
						.hashCode());
		result = prime * result
				+ ((scaledImage == null) ? 0 : scaledImage.hashCode());
		long temp;
		temp = Double.doubleToLongBits(x);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(xoffset);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(y);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(yoffset);
		result = prime * result + (int) (temp ^ (temp >>> 32));
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
		ScaledImageData other = (ScaledImageData) obj;
		if (downsampledImageData == null) {
			if (other.downsampledImageData != null)
				return false;
		} else if (!downsampledImageData.equals(other.downsampledImageData))
			return false;
		if (scaledImage == null) {
			if (other.scaledImage != null)
				return false;
		} else if (!scaledImage.equals(other.scaledImage))
			return false;
		if (Double.doubleToLongBits(x) != Double.doubleToLongBits(other.x))
			return false;
		if (Double.doubleToLongBits(xoffset) != Double
				.doubleToLongBits(other.xoffset))
			return false;
		if (Double.doubleToLongBits(y) != Double.doubleToLongBits(other.y))
			return false;
		if (Double.doubleToLongBits(yoffset) != Double
				.doubleToLongBits(other.yoffset))
			return false;
		return true;
	}

	public ImageData getDownsampledImageData() {
		return downsampledImageData;
	}

	public void setDownsampledImageData(ImageData downsampledImageData) {
		this.downsampledImageData = downsampledImageData;
	}

	public int getXPosition() {
		return (int)getX()-(int)getXoffset();
	}
	
	public int getYPosition() {
		return (int)getY()-(int)getYoffset();
	}

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}

	@Override
	public String toString() {
		return "ScaledImageData [xoffset=" + xoffset + ", yoffset=" + yoffset
				+ ", x=" + x + ", y=" + y + "]";
	}
}