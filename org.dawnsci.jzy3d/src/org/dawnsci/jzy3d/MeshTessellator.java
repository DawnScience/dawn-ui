package org.dawnsci.jzy3d;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import org.jzy3d.colors.Color;
import org.jzy3d.colors.ColorMapper;
import org.jzy3d.maths.BoundingBox3d;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.plot3d.primitives.AbstractDrawable;
import org.jzy3d.plot3d.primitives.Point;
import org.jzy3d.plot3d.primitives.Polygon;
import org.jzy3d.plot3d.primitives.Shape;
import org.jzy3d.plot3d.primitives.vbo.buffers.FloatVBO;
import org.jzy3d.plot3d.primitives.vbo.builders.VBOBuilder;
import org.jzy3d.plot3d.primitives.vbo.drawable.DrawableVBO;
import org.jzy3d.plot3d.rendering.view.Camera;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.glu.GLU;

public class MeshTessellator {
	
	
	public static Shape buildShape(float[] x, float[] y, float[] z) {
		Shape s = new Shape();
		s.add(getSquarePolygonsOnCoordinates(x,y,z));
		return s;
	}
	
	public static List<AbstractDrawable> getSquarePolygonsOnCoordinates(float[] x, float[] y, float[] z){
		List<AbstractDrawable> polygons = new ArrayList<AbstractDrawable>();
		
		for(int yi=0; yi<y.length-1; yi++){
			for(int xi=0; xi<x.length-1; xi++){
				// Compute quad making a polygon 
				Point p[] = getRealQuadStandingOnPoint(xi, yi, x, y, z);
				// Store quad
				AbstractDrawable quad = newQuad(p);
                polygons.add(quad);
			}
		}	
		return polygons;
	}
	
	private static Point[] getRealQuadStandingOnPoint(int xi, int yi, float[] x, float[] y, float[] z){
		Point p[]  = new Point[4];
		
		int pos = xi+yi*x.length;
		int posNext = xi+(yi+1)*x.length;
		
		p[0] = new Point(new Coord3d(x[xi],   y[yi],   z[pos]    ));
		p[1] = new Point(new Coord3d(x[xi+1], y[yi],   z[pos+1]  ));
		p[2] = new Point(new Coord3d(x[xi+1], y[yi+1], z[posNext+1]));
		p[3] = new Point(new Coord3d(x[xi],   y[yi+1], z[posNext]  ));

		
		
		return p;
	}
	
	private static AbstractDrawable newQuad(Point p[]){
	    Polygon quad = new Polygon();
        for(int pi=0; pi<p.length; pi++)
            quad.add(p[pi]);
        return quad;
	}
	
	public static DrawableVBO buildShape(float[] x, float[] y, float[] z, ColorMapper mapper) {
		
		DrawableVBO v =  new DrawableVBO(new ColoredMeshVBOBuilder(x, y, z, mapper)) {
			
			boolean disposed = false;
			private GLSLProgram shaderProgram; 
			private ColormapTexture colormapTexure;
			
			@Override
		    public void draw(GL gl, GLU glu, Camera cam) {
				
				if (!hasMountedOnce) {
					mount(gl);
				}
				
				if (disposed) {
					
					gl.glDeleteBuffers(1, arrayName, 0);
					gl.glDeleteBuffers(1, elementName, 0);
					return;
				}
				
//				if (!hasMountedOnce) {
//					badmount(gl);
//				}
//				
				gl.getGL2().glDisable(GL.GL_BLEND);
				shaderProgram.bind(gl.getGL2());
				shaderProgram.setUniform(gl.getGL2(), "min_max", new float[] {(float) mapper.getMin(), (float) mapper.getMax(),(float) mapper.getMin(), (float) mapper.getMax()},4);
				
				super.draw(gl, glu, cam);
				shaderProgram.unbind(gl.getGL2());
				gl.getGL2().glEnable(GL.GL_BLEND);
			}
			
			 @Override
			    public void mount(GL gl) {
			        try {
			            loader.load(gl, this);
			            hasMountedOnce = true;
			            shaderProgram = new GLSLProgram();
				        ShaderFilePair sfp = new ShaderFilePair(this.getClass(), "colour_mapped_surface.vert", "colour_mapped_surface.frag");
				        shaderProgram.loadAndCompileShaders(gl.getGL2(),sfp);
				        shaderProgram.link(gl.getGL2());
				        colormapTexure = new ColormapTexture(mapper);
				        colormapTexure.bind(gl);
			        } catch (Exception e) {
			            e.printStackTrace();
//			            Logger.getLogger(DrawableVBO.class).error(e, e);
			        }
			    }
			 
//			 public void badmount(GL gl) {
//			        try {
//			            loader.load(gl, this);
//			            hasMountedOnce = true;
//			        } catch (Exception e) {
//			            e.printStackTrace();
////			            Logger.getLogger(DrawableVBO.class).error(e, e);
//			        }
//			 }
			
			@Override
			public void dispose() {
				disposed = true;
			}
			
		};
		v.setGeometry(GL2.GL_QUADS);
		v.setColor( new Color(1f, 0f, 0f, 1f));
		float minY = Float.MAX_VALUE;
        float maxY = -Float.MAX_VALUE;
        float minX = Float.MAX_VALUE;
        float maxX = -Float.MAX_VALUE;
        float minZ = Float.MAX_VALUE;
        float maxZ = -Float.MAX_VALUE;
        
        for (int i = 0; i < x.length;i++) {
        	float t = x[i];
        	if (t < minX) minX = t;
        	if (t > maxX) maxX = t;
        }
        
        for (int i = 0; i < y.length;i++) {
        	float t = y[i];
        	if (t < minY) minY = t;
        	if (t > maxY) maxY = t;
        }
        
        for (int i = 0; i < z.length;i++) {
        	float t = z[i];
        	if (t < minZ) minZ = t;
        	if (t > maxZ) maxZ = t;
        }
		
		v.doSetBoundingBox(new BoundingBox3d(minX, maxX, minY, maxY, minZ, maxZ));
//		v.setWidth(1f);
		
		return v;
		
	}

	public static class ColoredPointVBO extends VBOBuilder {
		
		private float[] x;
		private float[] y;
		private float[] z;
		private ColorMapper mapper;
		
		public ColoredPointVBO(float[] x, float[] y, float[] z, ColorMapper mapper) {
			this.x = x;
			this.y = y;
			this.z = z;
			this.mapper = mapper;
		}
		

		@Override
		public void load(GL gl, DrawableVBO drawable) throws Exception {
			FloatVBO vbo = initFloatVBO(drawable, true, z.length);
			fillFromArray(drawable, x,y,z,mapper, vbo);
	        drawable.setData(gl, vbo);
//			List<Coord3d>  coordinates = buildCoord();
//			 FloatVBO vbo = initFloatVBO(drawable, true, coordinates.size());
//		        fillWithCollection(drawable, coordinates, vbo, mapper);
//		        drawable.setData(gl, vbo);
			
		}
		
		private List<Coord3d> buildCoord(){
			int size = 0;
	        List<Coord3d> co = new ArrayList<>(z.length);
	        for(int yi=0; yi<y.length-1; yi++){
				for(int xi=0; xi<x.length-1; xi++){
					// Compute quad making a polygon 
//					Point p[] = getRealQuadStandingOnPoint(xi, yi, x, y, z);
					// Store quad
					Coord3d c = new Coord3d();
					c.x = x[xi];
					c.y = y[yi];
					c.z = z[xi+yi*x.length];
//		            putCoord(vertices, c);
//		            putColor(vertices, colors.getColor(c));
		            co.add(c);
//					AbstractDrawable quad = newQuad(p);
//	                polygons.add(quad);
	                
				}
			}	
	        
	        return co;
		}
		
		private void fillFromArray(DrawableVBO drawable, float[] x, float[] y, float[] z, ColorMapper colors, FloatVBO vbo) {
			FloatBuffer vertices= vbo.getVertices();
			IntBuffer indices = vbo.getIndices();
	        drawable.setHasColorBuffer(true);
	        int size = 0;
	        Coord3d c = new Coord3d();
	        float minY = Float.MAX_VALUE;
	        float maxY = -Float.MAX_VALUE;
	        float minX = Float.MAX_VALUE;
	        float maxX = -Float.MAX_VALUE;
	        float minZ = Float.MAX_VALUE;
	        float maxZ = -Float.MAX_VALUE;
	        
	        for (int i = 0; i < x.length;i++) {
	        	float t = x[i];
	        	if (t < minX) minX = t;
	        	if (t > maxX) maxX = t;
	        }
	        
	        for (int i = 0; i < y.length;i++) {
	        	float t = y[i];
	        	if (t < minY) minY = t;
	        	if (t > maxY) maxY = t;
	        }
	        
	        for(int yi=0; yi<y.length-1; yi++){
				for(int xi=0; xi<x.length-1; xi++){
					// Compute quad making a polygon 
//					Point p[] = getRealQuadStandingOnPoint(xi, yi, x, y, z);
					// Store quad
					indices.put(size++);
					c.x = x[xi];
					c.y = y[yi];
					c.z = z[xi+yi*x.length];
					
					if (c.z < minZ) minZ = c.z;
		        	if (c.z > maxZ) maxZ = c.z;
					
		            putCoord(vertices, c);
		            putColor(vertices, colors.getColor(c));
//					AbstractDrawable quad = newQuad(p);
//	                polygons.add(quad);
	                
				}
			}	
	        
	        
	        
//	        for (Coord3d c : coordinates) {
//	            indices.put(size++);
//	            putCoord(vertices, c);
//	            bounds.add(c);
//	            if (colors != null) {
//	                putColor(vertices, colors.getColor(c));
//	            }
//	        }
	        vertices.rewind();
	        indices.rewind();
	        vbo.setBounds(new BoundingBox3d(minX, maxX, minY, maxY, minZ, maxZ));
	    }
	}
		
		public static class ColoredMeshVBO extends VBOBuilder {
			
			private float[] x;
			private float[] y;
			private float[] z;
			private ColorMapper mapper;
			
			public ColoredMeshVBO(float[] x, float[] y, float[] z, ColorMapper mapper) {
				this.x = x;
				this.y = y;
				this.z = z;
				this.mapper = mapper;
			}
			

			@Override
			public void load(GL gl, DrawableVBO drawable) throws Exception {
				FloatVBO vbo = initFloatVBO(drawable, true, (y.length-1)*(x.length-1)*4);
				fillFromArray(drawable, x,y,z,mapper, vbo);
		        drawable.setData(gl, vbo);
//				List<Coord3d>  coordinates = buildCoord();
//				 FloatVBO vbo = initFloatVBO(drawable, true, coordinates.size());
//			        fillWithCollection(drawable, coordinates, vbo, mapper);
//			        drawable.setData(gl, vbo);
				
			}
			
			private List<Coord3d> buildCoord(){
				int size = 0;
		        List<Coord3d> co = new ArrayList<>(z.length);
		        for(int yi=0; yi<y.length-1; yi++){
					for(int xi=0; xi<x.length-1; xi++){
						// Compute quad making a polygon 
//						Point p[] = getRealQuadStandingOnPoint(xi, yi, x, y, z);
						// Store quad
						Coord3d c = new Coord3d();
						c.x = x[xi];
						c.y = y[yi];
						c.z = z[xi+yi*x.length];
//			            putCoord(vertices, c);
//			            putColor(vertices, colors.getColor(c));
			            co.add(c);
//						AbstractDrawable quad = newQuad(p);
//		                polygons.add(quad);
		                
					}
				}	
		        
		        return co;
			}
			
			private void fillFromArray(DrawableVBO drawable, float[] x, float[] y, float[] z, ColorMapper colors, FloatVBO vbo) {
				FloatBuffer vertices= vbo.getVertices();
				IntBuffer indices = vbo.getIndices();
		        drawable.setHasColorBuffer(true);

		        int size = 0;
		        Coord3d c = new Coord3d();
		        float minY = Float.MAX_VALUE;
		        float maxY = -Float.MAX_VALUE;
		        float minX = Float.MAX_VALUE;
		        float maxX = -Float.MAX_VALUE;
		        float minZ = Float.MAX_VALUE;
		        float maxZ = -Float.MAX_VALUE;
		        
		        for (int i = 0; i < x.length;i++) {
		        	float t = x[i];
		        	if (t < minX) minX = t;
		        	if (t > maxX) maxX = t;
		        }
		        
		        for (int i = 0; i < y.length;i++) {
		        	float t = y[i];
		        	if (t < minY) minY = t;
		        	if (t > maxY) maxY = t;
		        }
		        
		        for(int yi=0; yi<y.length-1; yi++){
					for(int xi=0; xi<x.length-1; xi++){
						// Compute quad making a polygon 
//						Point p[] = getRealQuadStandingOnPoint(xi, yi, x, y, z);
						// Store quad
						
						int pos = xi+yi*x.length;
						int posNext = xi+(yi+1)*x.length;
						
						
//						p[0] = new Point(new Coord3d(x[xi],   y[yi],   z[pos]    ));
//						p[1] = new Point(new Coord3d(x[xi+1], y[yi],   z[pos+1]  ));
//						p[2] = new Point(new Coord3d(x[xi+1], y[yi+1], z[posNext+1]));
//						p[3] = new Point(new Coord3d(x[xi],   y[yi+1], z[posNext]  ));
						
						indices.put(size++);
						c.x = x[xi];
						c.y = y[yi+1];
						c.z = z[posNext];
						
						if (c.z < minZ) minZ = c.z;
			        	if (c.z > maxZ) maxZ = c.z;
						
			            putCoord(vertices, c);
			            putColor(vertices, colors.getColor(c));
						
						indices.put(size++);
						c.x = x[xi];
						c.y = y[yi];
						c.z = z[pos];
						
						if (c.z < minZ) minZ = c.z;
			        	if (c.z > maxZ) maxZ = c.z;
						
			            putCoord(vertices, c);
			            putColor(vertices, colors.getColor(c));
			            
			            indices.put(size++);
						c.x = x[xi+1];
						c.y = y[yi];
						c.z = z[pos+1];
						
						if (c.z < minZ) minZ = c.z;
			        	if (c.z > maxZ) maxZ = c.z;
						
			            putCoord(vertices, c);
			            putColor(vertices, colors.getColor(c));
			            
			            
			            indices.put(size++);
						c.x = x[xi+1];
						c.y = y[yi+1];
						c.z = z[posNext+1];
						
						if (c.z < minZ) minZ = c.z;
			        	if (c.z > maxZ) maxZ = c.z;
						
			            putCoord(vertices, c);
			            putColor(vertices, colors.getColor(c));
			            
			            
			            
			           
			            
			            
						
			            
			            
			            
			            
		                
					}
				}	
		        
		        
		        
//		        for (Coord3d c : coordinates) {
//		            indices.put(size++);
//		            putCoord(vertices, c);
//		            bounds.add(c);
//		            if (colors != null) {
//		                putColor(vertices, colors.getColor(c));
//		            }
//		        }
		        vertices.rewind();
		        indices.rewind();
		        vbo.setBounds(new BoundingBox3d(minX, maxX, minY, maxY, minZ, maxZ));
		    }
		}
		
		public static class ShaderMeshVBO extends VBOBuilder {
			
			private float[] x;
			private float[] y;
			private float[] z;
			private ColorMapper mapper;
			
			
			public ShaderMeshVBO(float[] x, float[] y, float[] z, ColorMapper mapper) {
				this.x = x;
				this.y = y;
				this.z = z;
				this.mapper = mapper;
			}
			

			@Override
			public void load(GL gl, DrawableVBO drawable) throws Exception {
				FloatVBO vbo = initFloatVBO(drawable, false, (y.length-1)*(x.length-1)*4);
//				FloatVBO vbo = new FloatVBO((y.length-1)*(x.length-1)*6*3, 6*(y.length-1)*(x.length-1));
				fillFromArrayWithIndexing(drawable, x,y,z, vbo);
				drawable.setHasColorBuffer(false);
		        drawable.setData(gl, vbo);
			}

			private void fillFromArrayWithIndexing(DrawableVBO drawable, float[] x, float[] y, float[] z, FloatVBO vbo) {
				
				FloatBuffer vertices= vbo.getVertices();
				vertices.rewind();
				IntBuffer indices = vbo.getIndices();
				indices.rewind();
		        drawable.setHasColorBuffer(false);
				
		        float minY = Float.MAX_VALUE;
		        float maxY = -Float.MAX_VALUE;
		        float minX = Float.MAX_VALUE;
		        float maxX = -Float.MAX_VALUE;
		        float minZ = Float.MAX_VALUE;
		        float maxZ = -Float.MAX_VALUE;
		        
		        for (int i = 0; i < x.length;i++) {
		        	float t = x[i];
		        	if (t < minX) minX = t;
		        	if (t > maxX) maxX = t;
		        }
		        
		        for (int i = 0; i < y.length;i++) {
		        	float t = y[i];
		        	if (t < minY) minY = t;
		        	if (t > maxY) maxY = t;
		        }
		        
		        //fill vertex buffer with point co-ords
		        //build the triangles using the indices
		        int count = 0;
		        for(int yi=0; yi<y.length-1; yi++){
		        	for(int xi=0; xi<x.length-1; xi++){
		        		
		        		float zval00 = z[xi+yi*x.length];
		        		float zval10 = z[xi+1+yi*x.length];
		        		float zval11 = z[xi+1+(yi+1)*x.length];
		        		float zval01 = z[xi+(yi+1)*x.length];
		        		
		        		if (zval00 < minZ) minZ = zval00;
		        		if (zval00 > maxZ) maxZ = zval00;
		        		if (zval11 < minZ) minZ = zval11;
		        		if (zval11 > maxZ) maxZ = zval11;
		        		
		        		
		        		
		        		vertices.put(x[xi]);
		        		vertices.put(y[yi+1]);
		        		vertices.put(zval01);
		        		indices.put(count++);
		        		vertices.put(x[xi]);
		        		vertices.put(y[yi]);
		        		vertices.put(zval00);
		        		indices.put(count++);
		        		vertices.put(x[xi+1]);
		        		vertices.put(y[yi]);
		        		vertices.put(zval10);
		        		indices.put(count++);
//		        		vertices.put(x[xi]);
//		        		vertices.put(y[yi+1]);
//		        		vertices.put(zval01);
//		        		indices.put(count++);
//		        		vertices.put(x[xi+1]);
//		        		vertices.put(y[yi]);
//		        		vertices.put(zval10);
//		        		indices.put(count++);
		        		vertices.put(x[xi+1]);
		        		vertices.put(y[yi+1]);
		        		vertices.put(zval11);
		        		indices.put(count++);

		        	}
		        }
		        
//			indices.put(0);
//			indices.put(5);
//			indices.put(6);
			
//			indices.put(0);
//			indices.put(1);
//			indices.put(4);
//			
//			indices.put(1);
//			indices.put(5);
//			indices.put(4);
//			
//			indices.put(1);
//			indices.put(2);
//			indices.put(5);

//		        for(int yi=0; yi<y.length-1; yi++){
//		        	for(int xi=0; xi<x.length-1; xi++){
//		        		//build a quad
//
////		        		if (xi%2 == 0) {
//		        			int v0 = xi+yi*x.length;
//		        			int v1 = xi+1+yi*x.length;
//		        			int v2 = xi+(yi+1)*x.length;
////		        			indices.put(v0);
////		        			indices.put(v1);
////		        			indices.put(v2);
////		        			indices.put(0);
////		        			indices.put(1);
////		        			indices.put(2);
////		        		} else {
//		        			int v0a = xi+1+yi*x.length;
//		        			int v1b = xi+1+(yi+1)*x.length;
//		        			int v2c = xi+(yi+1)*x.length;
////		        			indices.put(v2);
////		        			indices.put(v0);
////		        			indices.put(v1b);
////		        			indices.put(v0a);
////		        			indices.put(v1b);
////		        			indices.put(v2c);
//		        			
//		        			
//		        			indices.put(v0a);
//		        			indices.put(v1b);
//		        			indices.put(v2c);
//		        			System.out.println("Triangle");
//		        			
//		        			System.out.println(v0a);
//		        			System.out.println(v1b);
//		        			System.out.println(v2c);
//		        			
////		        			indices.put(v0);
////		        			indices.put(v0a);
////		        			indices.put(v2);
////		        			
////		        			indices.put(v2c);
////		        			indices.put(v0a);
////		        			indices.put(v1b);
//		        			
////		        			indices.put(xi+yi*x.length);
////		        			indices.put(xi+(yi+1)*x.length);
////		        			indices.put((xi-1)+(yi+1)*x.length);
////		        		}
//		        		//						vertices.put(x[xi]);
//		        		//						vertices.put(y[yi]);
//		        		//						vertices.put(z[xi+yi*x.length]);
//		        	}
//
//		        	
//		        }
				
		        vertices.rewind();
	        	indices.rewind();
	        	vbo.setBounds(new BoundingBox3d(minX, maxX, minY, maxY, minZ, maxZ));
				
			}
			
			private void fillFromArray(DrawableVBO drawable, float[] x, float[] y, float[] z, FloatVBO vbo) {
				FloatBuffer vertices= vbo.getVertices();
				IntBuffer indices = vbo.getIndices();
		        drawable.setHasColorBuffer(false);

		        int size = 0;
		        Coord3d c = new Coord3d();
		        float minY = Float.MAX_VALUE;
		        float maxY = -Float.MAX_VALUE;
		        float minX = Float.MAX_VALUE;
		        float maxX = -Float.MAX_VALUE;
		        float minZ = Float.MAX_VALUE;
		        float maxZ = -Float.MAX_VALUE;
		        
		        for (int i = 0; i < x.length;i++) {
		        	float t = x[i];
		        	if (t < minX) minX = t;
		        	if (t > maxX) maxX = t;
		        }
		        
		        for (int i = 0; i < y.length;i++) {
		        	float t = y[i];
		        	if (t < minY) minY = t;
		        	if (t > maxY) maxY = t;
		        }
		        
		        for(int yi=0; yi<y.length-1; yi++){
					for(int xi=0; xi<x.length-1; xi++){
						// Compute quad making a polygon 
//						Point p[] = getRealQuadStandingOnPoint(xi, yi, x, y, z);
						// Store quad
						
						int pos = xi+yi*x.length;
						int posNext = xi+(yi+1)*x.length;
						
						
//						p[0] = new Point(new Coord3d(x[xi],   y[yi],   z[pos]    ));
//						p[1] = new Point(new Coord3d(x[xi+1], y[yi],   z[pos+1]  ));
//						p[2] = new Point(new Coord3d(x[xi+1], y[yi+1], z[posNext+1]));
//						p[3] = new Point(new Coord3d(x[xi],   y[yi+1], z[posNext]  ));
						
						indices.put(size++);
						c.x = x[xi];
						c.y = y[yi+1];
						c.z = z[posNext];
						
						if (c.z < minZ) minZ = c.z;
			        	if (c.z > maxZ) maxZ = c.z;
						
			            putCoord(vertices, c);
//			            putColor(vertices, colors.getColor(c));
						
						indices.put(size++);
						c.x = x[xi];
						c.y = y[yi];
						c.z = z[pos];
						
						if (c.z < minZ) minZ = c.z;
			        	if (c.z > maxZ) maxZ = c.z;
						
			            putCoord(vertices, c);
//			            putColor(vertices, colors.getColor(c));
			            
			            indices.put(size++);
						c.x = x[xi+1];
						c.y = y[yi];
						c.z = z[pos+1];
						
						if (c.z < minZ) minZ = c.z;
			        	if (c.z > maxZ) maxZ = c.z;
						
			            putCoord(vertices, c);
//			            putColor(vertices, colors.getColor(c));
			            
			            
			            indices.put(size++);
						c.x = x[xi+1];
						c.y = y[yi+1];
						c.z = z[posNext+1];
						
						if (c.z < minZ) minZ = c.z;
			        	if (c.z > maxZ) maxZ = c.z;
						
			            putCoord(vertices, c);
//			            putColor(vertices, colors.getColor(c));
					}
				}	
		        
		        
		        
//		        for (Coord3d c : coordinates) {
//		            indices.put(size++);
//		            putCoord(vertices, c);
//		            bounds.add(c);
//		            if (colors != null) {
//		                putColor(vertices, colors.getColor(c));
//		            }
//		        }
		        vertices.rewind();
		        indices.rewind();
		        vbo.setBounds(new BoundingBox3d(minX, maxX, minY, maxY, minZ, maxZ));
		    }
		}
		
	
	
}
