package org.dawnsci.dedi.configuration.calculations;

import javax.vecmath.Vector2d;

import org.dawnsci.dedi.configuration.calculations.geometry.Ray;

public class BeamlineConfigurationUtil {
	
	private BeamlineConfigurationUtil(){
		throw new IllegalStateException("This class is not meant to be instantiated.");
	}
	
	
	/**
	 * <p> Calculates the q value assuming that the detector's normal vector is parallel to the beam direction. </p>
	 * <p> Note: the given cameraLength and distance should be in the same units. </p>
	 * 
	 * @param distance     - distance between the point at which the incident beam hits the detector, and 
	 *                       the point on the detector at which the q value should be calculated.
	 * @param cameraLength - distance between the detector and the sample.
	 * @param wavelength   - wavelength of the X-ray beam.
	 * 
	 * @return The magnitude q of the scattering vector corresponding to the given parameters. The units of the returned value
	 *         will be the inverse of the units used for the wavelength given.
	 *         
	 * @throws ArithmeticException if the given wavelength or camera length are zero.
	 *         IllegalArgumentException if any of the given parameters is negative.
	 */
	public static double calculateQValue(double distance, double cameraLength, double wavelength){
		if(cameraLength == 0 || wavelength == 0) throw new ArithmeticException();
		if(cameraLength < 0 || distance < 0 || wavelength < 0) throw new IllegalArgumentException();
		return 4*Math.PI*Math.sin(Math.atan(distance/cameraLength)/2)/wavelength;
	}
	
	
	/**
	 * <p> Calculates the distance between the point at which the incident beam hits the detector and the circle of points 
	 * for which q equals the given q value.
	 * Assumes that the detector's normal vector is parallel to the beam direction.
	 * </p>
	 * <p> Note: wavelength and qValue should have their units such that their product is unity 
	 * (e.g. m and 1/m, or mm and 1/mm, but not, say, m and 1/mm).
	 * The returned value will be in the same units as the given camera length.
	 * </p>
	 * 
	 * @param qValue       - magnitude q of the scattering vector.
	 * @param cameraLength - distance between the detector and the sample.
	 * @param wavelength   - wavelength of the X-ray beam.
	 * 
	 * @throws IllegalArgumentException if any of the given parameters is negative, 
	 *         or if the given q value cannot be achieved for the given camera length and wavelength.
	 */
	public static double calculateDistanceFromQValue(double qValue, double cameraLength, double wavelength){
		if(qValue < 0 || cameraLength < 0 || wavelength < 0) throw new IllegalArgumentException();
		double temp = wavelength*qValue/(4*Math.PI);
		if(Math.abs(temp) >= Math.sqrt(2)/2) throw new IllegalArgumentException();
		return Math.tan(2*Math.asin(temp))*cameraLength;
	}
	
	
	
	/**
	 * @param qvalue                 - magnitude q of the scattering vector in 1/m.
	 * @param angle                  - angle, in radians, giving the direction from the beamstop in which the point should lie.
	 * @param cameraLength           - camera length in m.
	 * @param wavelength             - wavelength in m.
	 * @param beamstopXCentreMM      - x coordinate of beamstop's centre in mm.
	 * @param beamstopYCentreMM      - y coordinate of beamstop's centre in mm.
	 * 
	 * @return The point at which q equals the given q value, with coordinates in mm and
	 *         calculated with respect to the origin used for the given beamstop position.
	 *         Returns null if the given q value cannot be achieved for the given configuration, or if the given camera length 
	 *         or wavelength were negative.
	 */
	public static Vector2d getPtForQ(double qvalue, double angle, double cameralength, double wavelength,
			                         double beamstopXCentreMM, double beamstopYCentreMM){
		Ray ray = new Ray(new Vector2d(Math.cos(angle), Math.sin(angle)), 
				          new Vector2d(beamstopXCentreMM, beamstopYCentreMM));
		
		try {
			return ray.getPtAtDistance(1.0e3*calculateDistanceFromQValue(qvalue, cameralength, wavelength));
		} catch(IllegalArgumentException e) {
			return null;
		}
	}
}
