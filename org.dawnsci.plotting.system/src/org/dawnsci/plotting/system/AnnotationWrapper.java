package org.dawnsci.plotting.system;

import org.csstudio.swt.xygraph.figures.Annotation;
import org.csstudio.swt.xygraph.figures.Annotation.CursorLineStyle;
import org.csstudio.swt.xygraph.figures.Axis;
import org.dawnsci.plotting.api.annotation.IAnnotation;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;

class AnnotationWrapper implements IAnnotation {

	private Annotation annotation;

	AnnotationWrapper(String name, Axis xAxis, Axis yAxis) {
		this.annotation = new Annotation(name, xAxis, yAxis);
	}

	AnnotationWrapper(Annotation annotation) {
		this.annotation = annotation;
	}

	public void setName(String name) {
		annotation.setName(name);
	}

	public void setShowName(boolean showName) {
		annotation.setShowName(showName);
	}

	public void setShowInfo(boolean showSampleInfo) {
		annotation.setShowSampleInfo(showSampleInfo);
	}

	public void setShowPosition(boolean showPosition) {
		annotation.setShowPosition(showPosition);
	}

	public void setAnnotationColor(Color annotationColor) {
		annotation.setAnnotationColor(annotationColor);
	}

	public void setAnnotationFont(Font annotationFont) {
		annotation.setAnnotationFont(annotationFont);
	}

	public String getName() {
		return annotation.getName();
	}

	public boolean isShowName() {
		return annotation.isShowName();
	}

	public boolean isShowInfo() {
		return annotation.isShowSampleInfo();
	}

	public boolean isShowPosition() {
		return annotation.isShowPosition();
	}

	public Color getAnnotationColor() {
		return annotation.getAnnotationColor();
	}
	
	public Font getAnnotationFont() {
		return annotation.getAnnotationFont();
	}
	
	public LineStyle getLineStyle() {
		final CursorLineStyle style = annotation.getCursorLineStyle();
		switch(style) {
		case NONE:
			return LineStyle.NONE;
		case UP_DOWN:
			return LineStyle.UP_DOWN;
		case LEFT_RIGHT:
			return LineStyle.LEFT_RIGHT;
		case FOUR_DIRECTIONS:
			return LineStyle.FOUR_DIRECTIONS;
		default:
			return LineStyle.NONE;
		}
	}
	/**
	 * @param cursorLineStyle the cursorLineStyle to set
	 */
	public void setLineStyle(LineStyle ls) {
		switch(ls) {
		case NONE:
			annotation.setCursorLineStyle(CursorLineStyle.NONE);
			return;
		case UP_DOWN:
			annotation.setCursorLineStyle(CursorLineStyle.UP_DOWN);
			return;
		case LEFT_RIGHT:
			annotation.setCursorLineStyle(CursorLineStyle.LEFT_RIGHT);
			return;
		case FOUR_DIRECTIONS:
			annotation.setCursorLineStyle(CursorLineStyle.FOUR_DIRECTIONS);
			return;
		default:
			annotation.setCursorLineStyle(CursorLineStyle.NONE);
			return;
		}

	}

	protected Annotation getAnnotation() {
		return annotation;
	}

	@Override
	public void setLocation(double x, double y) {
		annotation.setLocation(x, y);
	}

	@Override
	public boolean isVisible() {
		return annotation.isVisible();
	}

	@Override
	public void setVisible(boolean isVis) {
		annotation.setVisible(isVis);
	}

}
