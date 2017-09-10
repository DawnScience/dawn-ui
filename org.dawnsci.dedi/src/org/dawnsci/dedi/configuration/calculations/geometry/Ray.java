package org.dawnsci.dedi.configuration.calculations.geometry;

import javax.vecmath.Vector2d;

import org.dawnsci.dedi.configuration.calculations.NumericRange;

/**
 * <p> This class represent a (geometrical) ray (or half-line) - half of a line proceeding from an initial point 
 * in a direction specified by a non-zero vector.
 * </p>
 * 
 * <p> Internally, it uses the direction vector to parameterise the positions of the points lying on the ray
 * by a parameter (conventionally called t) that sweeps the interval [0, infinity].
 * </p>
 * 
 * <p> It provides useful methods for manipulating rays, such as getting the point that corresponds to 
 * a certain value of the parameter t, getting a point at a given distance from the initial point,
 * and finding the {@link NumericRange} of values of the parameter t that give the points that belong to the intersection
 * of the ray with various geometrical shapes such as rectangle, conic (circle, ellipse, ...), etc.
 * </p>
 */
public class Ray {
	private Vector2d direction;  // a vector pointing in the direction of the ray.  
	private Vector2d pt;         // the initial point.
	
	
	/**
	 * Constructs a new ray.
	 * 
	 * @param direction - a vector pointing in the direction of the ray.
	 * @param pt        - the initial point.
	 * 
	 * @throws IllegalArgumentException - if the direction vector is the zero vector.
	 */
	public Ray(Vector2d direction, Vector2d pt) {
		super();
		if(direction.length() == 0) throw new IllegalArgumentException("The direction vector of a ray cannot be the zero vector.");
		this.direction = direction;
		this.pt = pt;
	}

	
	public Vector2d getDirection() {
		return direction;
	}

	
	/**
	 * @param direction - a vector pointing in the direction of the ray.
	 * 
	 * @throws IllegalArgumentException - if the direction vector is the zero vector.
	 */
	public void setDirection(Vector2d direction) {
		if(direction.length() == 0) throw new IllegalArgumentException("The direction vector of a ray cannot be the zero vector.");
		this.direction = direction;
	}

	
	public Vector2d getInitialPt() {
		return pt;
	}

	
	public void setInitialPt(Vector2d pt) {
		this.pt = pt;
	}
	
	
	/**
	 * @param t - value of the parameter t.
	 * 
	 * @return The point on the ray that corresponds to the given value of the parameter t that parameterises the ray.
	 *         Returns null if t does not belong to [0, infinity].
	 */
	public Vector2d getPt(double t){
		if(t < 0) return null;
		Vector2d result = new Vector2d(direction);
		result.scale(t);
		result.add(pt);
		return result;
	}
	
	
	/**
	 * @param distance - distance from the initial point.
	 * 
	 * @return The point on the ray at the given distance from the initial point.
	 *         Returns null if the distance is negative.
	 */
	public Vector2d getPtAtDistance(double distance){
		if(distance < 0) return null;
		return getPt(distance/direction.length());
	}
	
	
	/**
	 * Returns the {@link NumericRange} of values of the parameter t that give the points that belong to the intersection
     * of the ray with the given conic, specified by an equation of the following form:
     * 
     * coeffOfx2*x^2 + coeffOfxy*x*y + coeffOfy2*y^2 + coeffOfx*x + coeffOfy*y + constant = 0.
	 * 
	 */
	private NumericRange getConicIntersectionParameterRange(double coeffOfx2, double coeffOfxy, double coeffOfy2,
														   double coeffOfx, double coeffOfy, double constant){
		
		double t1;
		double t2;
		
		double a = coeffOfx2*Math.pow(direction.x, 2) + coeffOfxy*direction.x*direction.y +
				   coeffOfy2*Math.pow(direction.y, 2);
		double b = 2*coeffOfx2*direction.x*pt.x + coeffOfxy*(direction.x*pt.y + direction.y*pt.x) +
				   2*coeffOfy2*direction.y*pt.y + coeffOfx*direction.x + coeffOfy*direction.y;
		double c = coeffOfx2*Math.pow(pt.x, 2) + coeffOfxy*pt.x*pt.y + coeffOfy2*Math.pow(pt.y, 2) + 
		           coeffOfx*pt.x + coeffOfy*pt.y + constant;
		
		double discriminant = Math.pow(b, 2) - 4*a*c;
		if (discriminant < 0) return null;
		if (a == 0){
			if(b == 0) return (c == 0) ? new NumericRange(0, Double.POSITIVE_INFINITY) : null;
			t1 = -c/b;
			t2 = -c/b;
		} else{
			t1 = 0.5*(-b - Math.sqrt(discriminant))/a;
			t2 = 0.5*(-b + Math.sqrt(discriminant))/a;
		}
		return getParameterRange(t1, t2);
	}
	
	
	/*
	 * Returns the {@link NumericRange} of values of the parameter t that give the points that belong to the intersection
	 * of the ray with the given ellipse.
	 */
	private NumericRange getEllipseIntersectionParameterRange(double a, double b, Vector2d centre){
		double xcentre = centre.x;
		double ycentre = centre.y;
		
		double coeffOfx2 = 1/Math.pow(a, 2);
		double coeffOfy2 = 1/Math.pow(b, 2);
		double coeffOfx = -2*xcentre/Math.pow(a, 2);
		double coeffOfy = -2*ycentre/Math.pow(b, 2);
		double constant = Math.pow(xcentre, 2)/Math.pow(a, 2) + Math.pow(ycentre, 2)/Math.pow(b, 2) - 1;
		
		return getConicIntersectionParameterRange(coeffOfx2, 0, coeffOfy2, coeffOfx, coeffOfy, constant);
	}
	
	
	/*
	 * Returns the {@link NumericRange} of values of the parameter t that give the points that belong to the intersection
	 * of the ray with the given circle.
	 */
	public NumericRange getCircleIntersectionParameterRange(double radius, Vector2d centre){
		return getEllipseIntersectionParameterRange(radius, radius, centre);
	}
	
	
	/*
	 * Returns the {@link NumericRange} of values of the parameter t that give the points that belong to the intersection
	 * of the ray with the given rectangle.
	 */
	public NumericRange getRectangleIntersectionParameterRange(Vector2d topLeftCorner, double width, double height){
		NumericRange result;
		
		double xmax = topLeftCorner.x + width;
		double xmin = topLeftCorner.x;
		double ymax = topLeftCorner.y;
		double ymin = topLeftCorner.y - height;
		
		if(direction.x == 0){
			if(! new NumericRange(xmin, xmax).contains(pt.x)) return null;
			result = new NumericRange(0, Double.POSITIVE_INFINITY);
		} else 
			result = new NumericRange((xmin-pt.x)/direction.x, (xmax-pt.x)/direction.x);
		
		if(direction.y == 0){
			if(! new NumericRange(ymin, ymax).contains(pt.y)) return null;
			return getParameterRange(result);
		}
		
		result = result.intersect(new NumericRange((ymin-pt.y)/direction.y, (ymax-pt.y)/direction.y));
		
		return getParameterRange(result);
	}
	
	
	/**
	 * Takes an arbitrary closed interval, specified by the end points, and restricts it to the interval [0, infinity].
	 */
	private NumericRange getParameterRange(double t1, double t2){
		if(t1 < 0 && t2 < 0) return null;
		
		double tMin = Math.min(t1, t2);
		double tMax = Math.max(t1, t2);
		
		if(tMin < 0) tMin = 0;
		
		return new NumericRange(tMin, tMax); 
	}
	
	
	/**
	 * Takes an arbitrary closed interval, specified as a {@link NumericRange}, and 
	 * returns a new NumericRange which is its restriction to the interval [0, infinity].
	 * Returns null if the given range is null.
	 */
	private NumericRange getParameterRange(NumericRange range) {
		if(range == null) return null;
		return getParameterRange(range.getMin(), range.getMax());
	}
}
