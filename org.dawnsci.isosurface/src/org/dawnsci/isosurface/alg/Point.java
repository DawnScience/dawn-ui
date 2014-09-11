package org.dawnsci.isosurface.alg;
/**
 * 
 * @author nnb55016
 * The class Point defines a three dimensional point characterised by the Cartesian coordinates
 */
public class Point extends Object{

	private double xCoord;
	private double yCoord;
	private double zCoord;
	
	public Point(double x, double y, double z){
		this.xCoord = x;
		this.yCoord = y;
		this.zCoord = z;
	}
	
	public Point(){
		
	}
	
	public double getxCoord() {
		return xCoord;
	}
	public void setxCoord(double xCoord) {
		this.xCoord = xCoord;
	}
	public double getyCoord() {
		return yCoord;
	}
	public void setyCoord(double yCoord) {
		this.yCoord = yCoord;
	}
	public double getzCoord() {
		return zCoord;
	}
	public void setzCoord(double zCoord) {
		this.zCoord = zCoord;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(xCoord);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(yCoord);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(zCoord);
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
		Point other = (Point) obj;
		if (Double.doubleToLongBits(xCoord) != Double
				.doubleToLongBits(other.xCoord))
			return false;
		if (Double.doubleToLongBits(yCoord) != Double
				.doubleToLongBits(other.yCoord))
			return false;
		if (Double.doubleToLongBits(zCoord) != Double
				.doubleToLongBits(other.zCoord))
			return false;
		return true;
	}

	@Override
	public String toString(){
		return "[" + getxCoord() + ", " + getyCoord() + ", " + getzCoord() + "]";
	}
}
