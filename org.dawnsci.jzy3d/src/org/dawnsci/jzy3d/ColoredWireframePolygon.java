package org.dawnsci.jzy3d;

import org.jzy3d.colors.Color;
import org.jzy3d.colors.ColorMapper;
import org.jzy3d.colors.IMultiColorable;
import org.jzy3d.plot3d.primitives.LineStrip;
import org.jzy3d.plot3d.primitives.Point;
import org.jzy3d.plot3d.primitives.Polygon;
import org.jzy3d.plot3d.rendering.view.Camera;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GL2GL3;
import com.jogamp.opengl.glu.GLU;

public class ColoredWireframePolygon extends LineStrip implements IMultiColorable {

	private ColorMapper mapper;
	
	@Override
	public ColorMapper getColorMapper() {
		// TODO Auto-generated method stub
		return mapper;
	}

	@Override
	public void setColorMapper(ColorMapper mapper) {
		this.mapper = mapper;
		
	}

	public void drawLineGL2(GL gl) {
        if (stipple) {
            gl.getGL2().glPolygonMode(GL.GL_BACK, GL2GL3.GL_LINE);
            gl.glEnable(GL2.GL_LINE_STIPPLE);
            gl.getGL2().glLineStipple(stippleFactor, stipplePattern);
        }

        gl.getGL2().glBegin(GL.GL_LINE_STRIP);
        gl.getGL2().glLineWidth(wfwidth);

        // Trying to deal with line co-planar with polygons
//         gl.glEnable(GL.GL_POLYGON_OFFSET_FILL);
//         gl.glPolygonOffset(-1, -1);
         
        if (mapper != null) {
        	
        	for (Point p : points) {
        		Color color = mapper.getColor(p.xyz);
                gl.getGL2().glColor4f(color.r, color.g, color.b, color.a);
                gl.getGL2().glVertex3f(p.xyz.x, p.xyz.y, p.xyz.z);
            }
        	
        } else if (wfcolor == null) {
            for (Point p : points) {
                gl.getGL2().glColor4f(p.rgb.r, p.rgb.g, p.rgb.b, p.rgb.a);
                gl.getGL2().glVertex3f(p.xyz.x, p.xyz.y, p.xyz.z);
            }
        } else {
            for (Point p : points) {
                gl.getGL2().glColor4f(wfcolor.r, wfcolor.g, wfcolor.b, wfcolor.a);
                gl.getGL2().glVertex3f(p.xyz.x, p.xyz.y, p.xyz.z);
            }
        }

//         gl.glDisable(GL.GL_POLYGON_OFFSET_FILL);

        gl.getGL2().glEnd();

        if (stipple) {
            gl.glDisable(GL2.GL_LINE_STIPPLE);
        }
    }
	
    
 
	
}
