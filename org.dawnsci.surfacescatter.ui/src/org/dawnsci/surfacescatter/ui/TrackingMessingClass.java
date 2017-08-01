//package org.dawnsci.surfacescatter.ui;
//
//import org.dawnsci.surfacescatter.LocationLenPtConverterUtils;
//import org.dawnsci.surfacescatter.PolynomialOverlap;
//import org.dawnsci.surfacescatter.TrackingMethodology.TrackerType1;
//import org.eclipse.january.dataset.Dataset;
//
//public class TrackingMessingClass {
//
//	public static void messing(int trackignMarker,
//							   int k,
//							   boolean seedRequired){
//		
//		
//		if(seedRequired && 
//				   frame.getTrackingMethodology() != TrackerType1.INTERPOLATION &&
//				   frame.getTrackingMethodology() != TrackerType1.SPLINE_INTERPOLATION &&
//				   frame.getTrackingMethodology() != TrackerType1.USE_SET_POSITIONS &&
//				   drm.isTrackerOn()) {
//
//					double myNum = drm.getSortedX().getDouble(k);
//					double distance = Math.abs(drm.getSortedX().getDouble(0) - myNum);
//					int nearestCompletedDatFileNo = findNearestDatNo(distance,
//																	 myNum);
//					
//					
//					Dataset[] xly = makeXLYArraysForInterpolator(nearestCompletedDatFileNo); 
//						
//					double[] seedLocation = PolynomialOverlap.extrapolatedLocation(drm.getSortedX().getDouble(k),
//																					   xly[1],
//																					   xly[0],
//																					   xly[2],
//																					   drm.getInitialLenPt()[0],
//																					   1);
//					drm.addSeedLocation(frame.getDatNo(),seedLocation);
//						
//					debug("!!!!!!!!!!!!!!!     }}}}}{{{{{{{{ seedlocation[0] : " + seedLocation[0] +" + " + "seedlocation[1] :" + seedLocation[1]);
//					
//				}	
//				
//				else if(frame.getTrackingMethodology() == TrackerType1.INTERPOLATION ||
//					    frame.getTrackingMethodology() == TrackerType1.SPLINE_INTERPOLATION){
//					
//					int[] len = new int[] {(int) Math.round(drm.getInterpolatedLenPts().get(k)[0][0]),(int) Math.round(drm.getInterpolatedLenPts().get(k)[0][1])};
//					int[]  pt = new int[] {(int) Math.round(drm.getInterpolatedLenPts().get(k)[1][0]),(int) Math.round(drm.getInterpolatedLenPts().get(k)[1][1])};
//					
//					double[] seedLocation = new double[] { (double) pt[0], (double) pt[1], (double) (pt[0] + len[0]),
//							(double) (pt[1]), (double) pt[0], (double) pt[1] + len[1], (double) (pt[0] + len[0]),
//							(double) (pt[1] + len[1]) };
//					
//					debug("!!!!!!!!!!!!!!!     }}}}}{{{{{{{{ seedlocation[0] : " + seedLocation[0] +" + " + "seedlocation[1] :" + seedLocation[1]);
//					
//					drm.addSeedLocation(frame.getDatNo(),seedLocation);
//				}
//				
//				
//				if(k == startFrame){
//					int[][] lenPt = ssp.getInitialLenPt();
//					double[] seedLocation = LocationLenPtConverterUtils.lenPtToLocationConverter(lenPt);
//					drm.addSeedLocation(frame.getDatNo(),seedLocation);
//				}
//	}
//	
//	
//	
//	
//}
