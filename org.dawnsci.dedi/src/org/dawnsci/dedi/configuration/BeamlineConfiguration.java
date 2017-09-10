package org.dawnsci.dedi.configuration;

import java.util.Objects;
import java.util.Observable;

import org.dawnsci.dedi.configuration.calculations.results.models.ResultsService;
import org.dawnsci.dedi.configuration.devices.Beamstop;
import org.dawnsci.dedi.configuration.devices.CameraTube;
import org.dawnsci.plotting.tools.preference.detector.DiffractionDetector;


/**
 * This class represents the configuration of an X-ray scattering beamline. 
 * It stores all the parameters that are needed for the determination of the range of scattering vectors q 
 * that can be observed on the detector in a particular direction along the surface of the detector.
 * 
 * To get hold of the currently used BeamlineConfiguration use the {@link ResultsService} class' static getter methods.
 */
public final class BeamlineConfiguration extends Observable {
	private DiffractionDetector detector;
	private Beamstop beamstop;
	private CameraTube cameraTube;
	private Double angle;                   // The angle (in rad) that specifies the direction along the surface of the detector for which the q ranges should be calculated.
	private Double cameraLength;            // Sample-detector distance in m.
	private Integer clearance; 
	private Double wavelength;
	private Double minWavelength;            // Minimum allowed wavelength of the X-ray beam in m.
	private Double maxWavelength;            // Maximum allowed wavelength of the X-ray beam in m.
	private Double minCameraLength;          // Minimum sample-detector distance in m.
	private Double maxCameraLength;          // Maximum sample-detector distance in m.
	
	
	
	// Getters and Setters

	public DiffractionDetector getDetector() {
		return detector;
	}

	
	/**
	 * A convenience method that returns the width of the detector in millimetres.
	 * (As the {@link DiffractionDetector} class provides it in pixels only).
	 * 
	 * @return The detector width in millimetres. Returns null if the detector is null.
	 */
	public Double getDetectorWidthMM(){
		if(detector == null) return null;
		return detector.getNumberOfPixelsX()*detector.getXPixelMM();
	}
	
	
	/**
	 * A convenience method that provides the height of the detector in millimetres.
	 * (As the {@link DiffractionDetector} class provides it in pixels only).
	 * 
	 * @return The detector height in millimetres. Returns null if the detector is null.
	 */
	public Double getDetectorHeightMM(){
		if(detector == null) return null;
		return detector.getNumberOfPixelsY()*detector.getYPixelMM();
	}
	
	
	/**
	 * Sets the current detector. 
	 * If the new detector equals the current detector (including if they're both null), 
	 * no notification is sent to Observers of this BeamlineConfiguration.
	 * 
	 * @param detector - the new detector.
	 */
	public void setDetector(DiffractionDetector detector) {
		if(Objects.equals(detector, this.detector)) return;
		this.detector = detector;
		setChanged();
		notifyObservers();
	}


	/**
	 * @return - angle (in rad) that specifies the direction along the surface of the detector 
	 *           for which the q ranges should be calculated.
	 */
	public Double getAngle() {
		return angle;
	}

	
	/**
	 * Sets the current angle. 
	 * If the new angle equals the current angle (including if they're both null), 
	 * no notification is sent to Observers.
	 * 
	 * @param angle - the new angle in radians.
	 */
	public void setAngle(Double angle) {
		if(Objects.equals(angle, this.angle)) return;
		this.angle = angle;
		setChanged();
		notifyObservers();
	}

	
	/**
	 * @return - camera length in metres.
	 */
	public Double getCameraLength() {
		return cameraLength;
	}

	
	/**
	 * Sets the current camera length. 
	 * If the new camera length equals the current camera length (including if they're both null),  
	 * no notification is sent to Observers.
	 * 
	 * @param cameraLength - the new camera length in metres.
	 */
	public void setCameraLength(Double cameraLength) {
		if(Objects.equals(cameraLength, this.cameraLength)) return;
		this.cameraLength = cameraLength;
		setChanged();
		notifyObservers();
	}

	
	/**
	 * @return - clearance in pixels.
	 */
	public Integer getClearance() {
		return clearance;
	}
	
	
	/** 
	 * Since clearance is specified in pixels and the pixels of the detector are allowed to have unequal height and width, 
	 * the clearance region is, in the general case, an ellipse. 
	 * This method returns the length of this ellipse's semi-major axis.
	 * 
	 * @return The length of the semi-major axis of the clearance in millimetres, 
	 *         or null if the clearance or the detector is null. 
	 */
	public Double getClearanceMajorMM() {
		if(detector == null || clearance == null) return null;
		return clearance*detector.getXPixelMM();
	}
	
	
	/**
	 * Since clearance is specified in pixels and the pixels of the detector are allowed to have unequal height and width, 
	 * the clearance region is, in the general case, an ellipse. 
	 * This method returns the length of this ellipse's semi-minor axis.
	 * 
	 * @return The length of the semi-minor axis of the clearance in millimetres,
	 *         or null if the clearance or the detector is null. 
	 */
	public Double getClearanceMinorMM() {
		if(detector == null || clearance == null) return null;
		return clearance*detector.getYPixelMM();
	}
	
    
	
	/**
	 * @return The length of the semi-major axis of the "clearance region" (beamstop + clearance) in millimeters,
	 *         or null if the clearance, beamstop or the detector are null. 
	 */
	public Double getClearanceAndBeamstopMajorMM(){
		if(getClearanceMajorMM() == null || beamstop == null) return null;
		return getClearanceMajorMM() + beamstop.getRadiusMM();
	}
	
	
	/**
	 * @return The length of the semi-minor axis of the "clearance region" (beamstop + clearance) in millimeters,
	 *         or null if the clearance, beamstop or the detector are null. 
	 */
	public Double getClearanceAndBeamstopMinorMM(){
		if(getClearanceMinorMM() == null || beamstop == null) return null;
		return getClearanceMinorMM() + beamstop.getRadiusMM();
	}
	
	
	/**
	 * @return The length of the semi-major axis of the "clearance region" (beamstop + clearance) in pixels,
	 *         or null if the clearance, beamstop or the detector are null. 
	 */
	public Double getClearanceAndBeamstopMajorPixels(){
		if(clearance == null || getBeamstopMajorPixels() == null) return null;
		return clearance + getBeamstopMajorPixels();
	}
	
	
	/**
	 * @return The length of the semi-minor axis of the "clearance region" (beamstop + clearance) in pixels,
	 *         or null if the clearance, beamstop or the detector are null. 
	 */
	public Double getClearanceAndBeamstopMinorPixels(){
		if(clearance == null || getBeamstopMinorPixels() == null) return null;
		return clearance + getBeamstopMinorPixels();
	}
	
	
	/**
	 * Sets the current clearance. 
	 * If the new clearance equals the current clearance, no notification is sent to Observers.
	 * 
	 * @param clearance - the new clearance in pixels.
	 */
	public void setClearance(Integer clearance) {
		if(Objects.equals(clearance, this.clearance)) return;
		this.clearance = clearance;
		setChanged();
		notifyObservers();
	}

	
	/**
	 * @return - wavelength of the X-ray beam in metres.
	 */
	public Double getWavelength() {
		return wavelength;
	}

	
	/**
	 * Sets the current wavelength. 
	 * If the new wavelength equals the current wavelength, no notification is sent to Observers.
	 * 
	 * @param wavelength - the new wavelength in metres.
	 */
	public void setWavelength(Double wavelength) {
		if(Objects.equals(wavelength, this.wavelength)) return;
		this.wavelength = wavelength;
		setChanged();
		notifyObservers();
	}

	
	public Beamstop getBeamstop() {
		return beamstop;
	}

	
	/**
	 * @return The length of the semi-major axis of the beamstop in pixels,
	 *         or null if the beamstop or the detector are null. 
	 */
	public Double getBeamstopMajorPixels(){
		if(beamstop == null || detector == null) return null;
		return beamstop.getRadiusMM()/detector.getXPixelMM();
	}
	
	
	/**
	 * @return The length of the semi-minor axis of the beamstop in pixels,
	 *         or null if the beamstop or the detector are null. 
	 */
	public Double getBeamstopMinorPixels(){
		if(beamstop == null || detector == null) return null;
		return beamstop.getRadiusMM()/detector.getYPixelMM();
	}
	
	
	/**
	 * @return - the x coordinate of the centre of the beamstop w.r.t. the top left corner of the detector in millimetres,
	 *           or null if the beamstop or detector are null.
	 */    
	public Double getBeamstopXCentreMM(){
		if(beamstop == null || detector == null) return null;
		return beamstop.getXCentre()*detector.getXPixelMM();
	}
	
	
	/**
	 * @return - the y coordinate of the centre of the beamstop w.r.t. the top left corner of the detector in millimetres,
	 *           or null if the beamstop or detector are null.
	 */
	public Double getBeamstopYCentreMM(){
		if(beamstop == null || detector == null) return null;
		return beamstop.getYCentre()*detector.getYPixelMM();
	}
	
	
	/**
	 * Sets the current beamstop. 
	 * If the new beamstop equals the current beamstop, no notification is sent to Observers.
	 * 
	 * @param beamstop - the new beamstop.
	 */
	public void setBeamstop(Beamstop beamstop) {
		if(Objects.equals(beamstop, this.beamstop)) return;
		this.beamstop = beamstop;
		setChanged();
		notifyObservers();
	}

	
	public CameraTube getCameraTube() {
		return cameraTube;
	}
	
	
	/**
	 * @return The length of the semi-major axis of the camera tube's projection onto detector in pixels,
	 *         or null if the camera tube or the detector are null. 
	 */
	public Double getCameraTubeMajorPixels(){
		if(cameraTube == null || detector == null) return null;
		return cameraTube.getRadiusMM()/detector.getXPixelMM();
	}
	
	
	/**
	 * @return The length of the semi-minor axis of the camera tube's projection onto detector in pixels,
	 *         or null if the camera tube or the detector are null. 
	 */
	public Double getCameraTubeMinorPixels(){
		if(cameraTube == null || detector == null) return null;
		return cameraTube.getRadiusMM()/detector.getYPixelMM();
	}
	
	
	/**
	 * @return - the x coordinate of the centre of the camera tube's projection onto detector
	 *           w.r.t. the top left corner of the detector in millimetres, or null if the camera tube or detector are null.
	 */
	public Double getCameraTubeXCentreMM(){
		if(cameraTube == null || detector == null) return null;
		return cameraTube.getXCentre()*detector.getXPixelMM();
	}

	
	/**
	 * @return - the y coordinate of the centre of the camera tube's projection onto detector
	 *           w.r.t. the top left corner of the detector in millimetres, or null if the camera tube or detector are null.
	 */
	public Double getCameraTubeYCentreMM(){
		if(cameraTube == null || detector == null) return null;
		return cameraTube.getYCentre()*detector.getYPixelMM();
	}
	
	
	/**
	 * Sets the current camera tube. 
	 * If the new camera tube equals the current camera tube, no notification is sent to Observers.
	 * 
	 * @param cameraTube - the new camera tube.
	 */
	public void setCameraTube(CameraTube cameraTube) {
		if(Objects.equals(cameraTube, this.cameraTube)) return;
		this.cameraTube = cameraTube;
		setChanged();
		notifyObservers();
	}
	
	
	/**
	 * @return - maximum achievable wavelength of the beam in metres.
	 */
	public Double getMaxWavelength() {
		return maxWavelength;
	}

	
	/**
	 * Sets the current the maximum allowed wavelength. 
	 * If the new the maximum allowed wavelength equals the current maximum allowed wavelength, no notification is sent to Observers.
	 * 
	 * @param wavelength - the maximum allowed wavelength in metres.
	 */
	public void setMaxWavelength(Double wavelength) {
		if(Objects.equals(wavelength, this.wavelength)) return;
		this.maxWavelength = wavelength;
		setChanged();
		notifyObservers();
	}
	
	
	/**
	 * @return - minimum achievable wavelength of the beam in metres.
	 */
	public Double getMinWavelength() {
		return minWavelength;
	}

	
	/**
	 * Sets the current the minimum allowed wavelength. 
	 * If the new the minimum allowed wavelength equals the current minimum allowed wavelength, no notification is sent to Observers.
	 * 
	 * @param wavelength - the minimum allowed wavelength in metres.
	 */
	public void setMinWavelength(Double wavelength) {
		if(Objects.equals(wavelength, this.wavelength)) return;
		this.minWavelength = wavelength;
		setChanged();
		notifyObservers();
	}
	
	
	/**
	 * @return - minimum possible camera length in metres.
	 */
	public Double getMinCameraLength() {
		return minCameraLength;
	}

	
	/**
	 * Sets the current the minimum sample-to-detector distance. 
	 * If the new the the minimum sample-to-detector distance equals the current minimum sample-to-detector distance, no notification is sent to Observers.
	 * 
	 * @param cameraLength - the minimum sample-to-detector distance in metres.
	 */
	public void setMinCameraLength(Double cameraLength) {
		if(Objects.equals(cameraLength, this.minCameraLength)) return;
		this.minCameraLength = cameraLength;
		setChanged();
		notifyObservers();
	}
	
	
	/**
	 * @return - maximum possible camera length in metres.
	 */
	public Double getMaxCameraLength() {
		return maxCameraLength;
	}

	
	/**
	 * Sets the current the maximum sample-to-detector distance. 
	 * If the new the the minimum sample-to-detector distance equals the current minimum sample-to-detector distance, no notification is sent to Observers.
	 * 
	 * @param cameraLength - the minimum sample-to-detector distance in metres.
	 */
	public void setMaxCameraLength(Double cameraLength) {
		if(Objects.equals(cameraLength, this.maxCameraLength)) return;
		this.maxCameraLength = cameraLength;
		setChanged();
		notifyObservers();
	}
}
