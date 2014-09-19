/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.plotting.jreality.impl;

import static de.jreality.shader.CommonAttributes.POLYGON_SHADER;
import static de.jreality.shader.CommonAttributes.TEXTURE_2D;
import static de.jreality.shader.CommonAttributes.TEXTURE_2D_1;
import static de.jreality.shader.CommonAttributes.TEXTURE_2D_2;
import static de.jreality.shader.CommonAttributes.TEXTURE_2D_3;
import static de.jreality.shader.CommonAttributes.TEXTURE_2D_4;
import static de.jreality.shader.CommonAttributes.TEXTURE_2D_5;
import static de.jreality.shader.CommonAttributes.TEXTURE_2D_6;
import static de.jreality.shader.CommonAttributes.TEXTURE_2D_7;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.dawnsci.plotting.jreality.tool.PanningTool;
import org.dawnsci.plotting.jreality.util.JOGLChecker;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.CompoundByteDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.CompoundDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.CompoundIntegerDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.CompoundLongDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.CompoundShortDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetUtils;
import org.eclipse.dawnsci.analysis.dataset.impl.FloatDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.RGBDataset;
import org.eclipse.dawnsci.plotting.api.jreality.compositing.CompositeEntry;
import org.eclipse.dawnsci.plotting.api.jreality.data.ColourImageData;
import org.eclipse.dawnsci.plotting.api.jreality.util.JOGLGLSLShaderGenerator;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jreality.geometry.PointSetFactory;
import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.data.AttributeEntityUtility;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.DefaultGeometryShader;
import de.jreality.shader.DefaultPointShader;
import de.jreality.shader.DefaultTextShader;
import de.jreality.shader.GlslProgram;
import de.jreality.shader.ShaderUtility;
import de.jreality.shader.Texture2D;
import de.jreality.ui.viewerapp.AbstractViewerApp;
import de.jreality.util.SceneGraphUtility;

/**
 *
 */
public class DataSet3DPlot2DMulti extends DataSet3DPlot2D {

	public static final int MAX_IMAGES = 7;
	private Map<Texture2D, Object> imageDatas = new HashMap<Texture2D,Object>();
	private int isRGB[] = new int[]{0,0,0,0,0,0,0};
	private static final Logger logger = LoggerFactory
	.getLogger(DataSet3DPlot2DMulti.class);
	
	public DataSet3DPlot2DMulti(AbstractViewerApp app, 
			                    Composite plotArea, 
			                    Cursor defaultCursor, 
			                    PanningTool tool,
			                    boolean useJOGL, boolean useJOGLshaders) 
	{
		super(app, plotArea, defaultCursor, tool, useJOGL, useJOGLshaders);
	}

	private void loadGLSLProgram(Appearance graphApp, 
 							     double min, double max) {
		if (tableProg == null) {
			tableProg = new GlslProgram(graphApp,"polygonShader",null,
					JOGLGLSLShaderGenerator.generateCompositeShader(false,numGraphs > MAX_IMAGES ? MAX_IMAGES :numGraphs),
					JOGLGLSLShaderGenerator.generateCompositeShaderName(false,numGraphs > MAX_IMAGES ? MAX_IMAGES : numGraphs));
		} else {
			tableProg.setShaders(null, 
								 JOGLGLSLShaderGenerator.generateCompositeShader(false,numGraphs > MAX_IMAGES ? MAX_IMAGES :numGraphs),
								 JOGLGLSLShaderGenerator.generateCompositeShaderName(false,numGraphs > MAX_IMAGES ? MAX_IMAGES : numGraphs));
		}
		tableProg.setUniform("maxValue", max);
		tableProg.setUniform("minValue",min);
	}
	
	@Override
	protected void setShaderOnAppearance(Appearance graphApp) {
		if (hasJOGLshaders)
		{
			Texture2D lookupTex = null;			
			if (tableProg != null) {	
				graphApp.setAttribute("useGLSL", true);
				graphApp.setAttribute(POLYGON_SHADER+"::glsl-source", tableProg.getSource());
				
				switch(numGraphs) {
					case 1:
						lookupTex = (Texture2D)
						AttributeEntityUtility.createAttributeEntity(Texture2D.class,
																	 POLYGON_SHADER+"."+TEXTURE_2D_1,
																	 graphApp,true);
					break;
					case 2:
						lookupTex = (Texture2D)
						AttributeEntityUtility.createAttributeEntity(Texture2D.class,
																	 POLYGON_SHADER+"."+TEXTURE_2D_2,
																	 graphApp,true);
					break;
					case 3:
						lookupTex = (Texture2D)
						AttributeEntityUtility.createAttributeEntity(Texture2D.class,
																	 POLYGON_SHADER+"."+TEXTURE_2D_3,
																	 graphApp,true);
					break;
					case 4:
						lookupTex = (Texture2D)
						AttributeEntityUtility.createAttributeEntity(Texture2D.class,
																	 POLYGON_SHADER+"."+TEXTURE_2D_4,
																	 graphApp,true);
					break;
					case 5:
						lookupTex = (Texture2D)
						AttributeEntityUtility.createAttributeEntity(Texture2D.class,
																	 POLYGON_SHADER+"."+TEXTURE_2D_5,
																	 graphApp,true);
					break;
					case 6:
						lookupTex = (Texture2D)
						AttributeEntityUtility.createAttributeEntity(Texture2D.class,
																	 POLYGON_SHADER+"."+TEXTURE_2D_6,
																	 graphApp,true);
					break;
					default:
						lookupTex = (Texture2D)
						AttributeEntityUtility.createAttributeEntity(Texture2D.class,
																	 POLYGON_SHADER+"."+TEXTURE_2D_7,
																	 graphApp,true);
					break;						
				}
				for (int i = 0; i < (numGraphs < MAX_IMAGES ? numGraphs : MAX_IMAGES); i++)
					tableProg.setUniform("sampler"+i, i);
				tableProg.setUniform("tableSampler", numGraphs < MAX_IMAGES ? numGraphs : MAX_IMAGES);
				// initial weights and compositing operators
				float[] weights = new float[]{1.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f};
				float[] masks = new float[21];
				for (int i = 0; i < 21; i++) masks[i] = 1.0f;
				
				int[] ops = new int[]{0,0,0,0,0,0,0};
				
				tableProg.setUniform("weight", weights);
				tableProg.setUniform("ops", ops);
				tableProg.setUniform("isRGB", isRGB);
				tableProg.setUniform("mask", masks);
				lookupTex.setImage(lookupTableImg);
				lookupTex.setMagFilter(Texture2D.GL_LINEAR_MIPMAP_LINEAR);
				lookupTex.setMinFilter(Texture2D.GL_LINEAR_MIPMAP_LINEAR);
				lookupTex.setRepeatS(Texture2D.GL_CLAMP_TO_EDGE);
				lookupTex.setRepeatT(Texture2D.GL_CLAMP_TO_EDGE);
			} 
		}		
	}	
	private void buildMessage(SceneGraphComponent graph) {
		PointSetFactory factory = new PointSetFactory();
		factory.setVertexCount(1);
		double[] coords = new double[3];
		String[] edgeLabels = new String[]{"NO GLSL SHADER SUPPORT!\n"+
			                               "YOU SHOULD BUY A NEWER\n"+
			                               "GRAPHICS CARD IT IS ABOUT\n"+
			                               "TIME!!1"};
		coords[0] = 0;
		coords[1] = 0;
		coords[2] = 0;
		factory.setVertexCoordinates(coords);
		factory.setVertexLabels(edgeLabels);
		factory.update();
		graph.setGeometry(factory.getPointSet());
		Appearance app = new Appearance();
		graph.setAppearance(app);
		app.setAttribute(CommonAttributes.POINT_SHADER+ "." + CommonAttributes.SPHERES_DRAW,false);
		DefaultGeometryShader dgsLabel =
			ShaderUtility.createDefaultGeometryShader(app, true);
		dgsLabel.setShowFaces(false);
		dgsLabel.setShowLines(false);
		dgsLabel.setShowPoints(true);
		DefaultPointShader dps =
			(DefaultPointShader)dgsLabel.createPointShader("default");
		dps.setPointSize(1.0);
		dps.setDiffuseColor(java.awt.Color.white);
		DefaultTextShader dtsLabel = (DefaultTextShader)dps.getTextShader();
		dtsLabel.setDiffuseColor(java.awt.Color.black);
		dtsLabel.setTextdirection(0);
		dtsLabel.setScale(FONT_AXIS_SCALE);		
	}
	
	@Override
	public SceneGraphComponent buildGraph(List<IDataset> datasets,
			SceneGraphComponent graph) {
		assert (datasets.size() > 0);
		maxWidth = maxHeight = 0;
		if (graph != null)
		{
			graphGroupNode = new SceneGraphComponent("Graph Group node");
			overlayPrimitiveGroupNode = new SceneGraphComponent("Overlay group node");
			this.graph = graph;
			if (hasJOGLshaders) {
				Iterator<IDataset> iter = datasets.iterator();
				while (iter.hasNext()) {
					IDataset currentData = iter.next();
					int width = currentData.getShape()[1];
					int height = currentData.getShape()[0];
					maxWidth = Math.max(width,maxWidth);
					maxHeight = Math.max(height,maxHeight);				
				}
				numGraphs = 0;
				boolean createdGeom = false;
				int width = 0;
				int height = 0;
				double[] sizes = new double[]{0,0};
				iter = datasets.iterator();
				
				while (iter.hasNext())
				{
					IDataset currentData = iter.next();
					width = currentData.getShape()[1];
					height = currentData.getShape()[0];
					if (!createdGeom) {
						sizes = determineXYsize();
					}
					int maxDimH = JOGLChecker.getMaxTextureHeight();
					int maxDimW = JOGLChecker.getMaxTextureWidth();
					double yStart = 0.0;
					double ySubSize = ((double)maxDimH/(double)maxHeight) < 1.0 ?
										sizes[1]*maxDimH/maxHeight : sizes[1]; 
					double xSubSize = ((double)maxDimW/(double)maxWidth) < 1.0 ?
										sizes[0]*maxDimW/maxWidth : sizes[0]; 
					
					float xScale = (float) (xSubSize / sizes[0]);
					float yScale = (float) (ySubSize / sizes[1]);
					maxDimH = (int)(maxDimH * yScale);
					maxDimW = (int)(maxDimW * xScale);
					int currentApp = 0;
					for (int y = 0; y < height; y+= maxDimH, yStart+=ySubSize) {
						double xStart = 0.0;
						for (int x = 0; x < width; x+= maxDimW, xStart+=xSubSize, currentApp++) {
							if (!createdGeom) {
		 						SceneGraphComponent subGraph =
									SceneGraphUtility.createFullSceneGraphComponent(GRAPHNODENAME+numGraphs+".sub"+x+"_"+y);
		
								subGraphs.add(subGraph);
								graphGroupNode.addChild(subGraph);
								
								subGraph.setGeometry(createGraphGeometry((xStart+xSubSize > sizes[0] ? sizes[0]-xStart : xSubSize),
																		 (yStart+ySubSize > sizes[1] ? sizes[1]-yStart : ySubSize),
																		  xStart,yStart));
								if (currentData instanceof CompoundDataset) {
									tableMin = 0;
									tableMax = 0;
								} else {
									tableMin = currentData.min().doubleValue();
									tableMax = currentData.max().doubleValue();
								}
								graphAppearance = createAppearance();
								subGraph.setAppearance(graphAppearance);
							} else {
								graphAppearance = graphApps.get(currentApp);
							}
							int texWidth = (width > maxDimW ? maxDimW : width);
							int texHeight = (height > maxDimH ? maxDimH : height);
							if (x+texWidth > width) texWidth = width-x;
							if (y+texHeight > height) texHeight = height-y;
							Texture2D texture = null;
							switch (numGraphs) {
								case 0 :
									texture = (Texture2D)
										AttributeEntityUtility.createAttributeEntity(Texture2D.class,
																					POLYGON_SHADER+"."+TEXTURE_2D,
																					graphAppearance,true);
								break;
								case 1 :
									texture = (Texture2D)
										AttributeEntityUtility.createAttributeEntity(Texture2D.class,
																					POLYGON_SHADER+"."+TEXTURE_2D_1,
																					graphAppearance,true);
								break;
								case 2 :
									texture = (Texture2D)
										AttributeEntityUtility.createAttributeEntity(Texture2D.class,
																					POLYGON_SHADER+"."+TEXTURE_2D_2,
																					graphAppearance,true);
								break;
								case 3 :
									texture = (Texture2D)
										AttributeEntityUtility.createAttributeEntity(Texture2D.class,
																					POLYGON_SHADER+"."+TEXTURE_2D_3,
																					graphAppearance,true);
								break;
								case 4 :
									texture = (Texture2D)
										AttributeEntityUtility.createAttributeEntity(Texture2D.class,
																					POLYGON_SHADER+"."+TEXTURE_2D_4,
																					graphAppearance,true);
								break;
								case 5 :
									texture = (Texture2D)
										AttributeEntityUtility.createAttributeEntity(Texture2D.class,
																					POLYGON_SHADER+"."+TEXTURE_2D_5,
																					graphAppearance,true);
								break;
								case 6 :
									texture = (Texture2D)
										AttributeEntityUtility.createAttributeEntity(Texture2D.class,
																					POLYGON_SHADER+"."+TEXTURE_2D_6,
																					graphAppearance,true);
								break;
							}
							if (texture != null) {
								boolean useRGB = 
									(currentData instanceof RGBDataset) ||
									(currentData instanceof CompoundDataset &&
									(currentData.getElementsPerItem() == 3 ||
					 				 currentData.getElementsPerItem() == 4));
								isRGB[numGraphs] = (useRGB ? 1 : 0);
								generateTexture(currentData, texture, x,y, texWidth, texHeight,useRGB);
							}
						}
					}	
					createdGeom = true;
					numGraphs++;
				}
				determineRanges(datasets);
				Iterator<Appearance> graphIter = graphApps.iterator();
				while (graphIter.hasNext()) {
					Appearance app = graphIter.next();
					loadGLSLProgram(app,tableMin,tableMax);
					setShaderOnAppearance(app);							
				}
				setupOtherNodes();
			} else {
				buildMessage(graph);
				logger.warn("No shader support! Get a new graphics card or install proper drivers, it's about time!");
			}
		}
		return graph;
	}	

	protected void generateFloatTexture(IDataset data, 
		   Texture2D tex, 
		    int xpos,
		    int ypos,
		    int width, 
		    int height,
		    boolean createNewTexture)
	{
		if (createNewTexture) {
			float[] imageData = new float[width * height];
			imageDatas.put(tex, imageData);
		}
		float[] imageData = (float[])imageDatas.get(tex);
		FloatDataset fdata = (FloatDataset)DatasetUtils.cast(DatasetUtils.convertToDataset(data), Dataset.FLOAT32);
		if (width == fdata.getShape()[1] &&	height == fdata.getShape()[0]) 
		{
			imageData = fdata.getData();
		} else {
			for (int y = 0; y < height; y++)
				for (int x = 0; x < width; x++) {
					imageData[x+y*width] = fdata.getFloat(fdata.getShape()[0]-1-(ypos+(height-1-y)),xpos+x);
				}
		}
		if (createNewTexture) {
			tex.setImage(new de.jreality.shader.ImageData(imageData,width,height));
			tex.setRepeatS(Texture2D.GL_CLAMP_TO_EDGE);
			tex.setRepeatT(Texture2D.GL_CLAMP_TO_EDGE);
			tex.setMagFilter(Texture2D.GL_NEAREST);
			tex.setMinFilter(Texture2D.GL_LINEAR);
			tex.setMipmapMode(true);
			System.gc();
		} else {
			de.jreality.shader.ImageData texImg = tex.getImage();
			texImg.updateData(imageData);
		}
	}	
	
	private void generateRGBTexture(CompoundDataset data,
			Texture2D currentTexture,
			int xpos,
			int ypos,
			int width,
			int height,
			boolean createNewTexture)
{
		if (createNewTexture) {
			byte[] softwareImageRGBAdata = new byte[width * height * 4];
			imageDatas.put(currentTexture, softwareImageRGBAdata);
		}
		byte[] softwareImageRGBAdata = (byte[])imageDatas.get(currentTexture);
		int si = 0;
		int srcHeight = data.getShape()[0];
		if (data instanceof RGBDataset) {
			RGBDataset rgbData = (RGBDataset)data;
			short[] rgbImgData = rgbData.getData();
			int srcWidth = rgbData.getShape()[1];
			for (int y = 0; y < height; y++) {
				int di = (xpos + (srcHeight -1 -ypos-(height-1-y))*srcWidth)*3;
				for (int x = 0; x < width; x++) {
					short sRed = rgbImgData[di++];
					short sGreen = rgbImgData[di++];
					short sBlue = rgbImgData[di++];
					softwareImageRGBAdata[si++] = (byte) sRed;
					softwareImageRGBAdata[si++] = (byte) sGreen;
					softwareImageRGBAdata[si++] = (byte) sBlue;
					softwareImageRGBAdata[si++] = ~0;
				}
			}
		} else {
			int isize = data.getElementsPerItem();
			switch(data.getDtype()) {

			case Dataset.ARRAYINT8:
			{
				CompoundByteDataset cbData = (CompoundByteDataset)data;
				for (int y = 0; y < height; y++) {
					int yDataPos = srcHeight -1 -ypos-(height-1-y);
					for (int x = 0; x < width; x++) {
						byte[] rgba = cbData.getByteArray(yDataPos, x + xpos);
						softwareImageRGBAdata[si++] = rgba[0];
						softwareImageRGBAdata[si++] = rgba[1];
						softwareImageRGBAdata[si++] = rgba[2];
						softwareImageRGBAdata[si++] = isize > 3 ? rgba[3] : ~0;
					}
				}
			}
			break;
			case Dataset.ARRAYINT16:
			{
				CompoundShortDataset csData = (CompoundShortDataset)data;
				double mins[] = data.minItem();
				double maxs[] = data.maxItem();
				short redRange = (short) Math.max(1,(short)(maxs[0]-mins[0]));
				short greenRange = (short)Math.max(1,(maxs[1]-mins[1]));
				short blueRange = (short)Math.max(1,(maxs[2]-mins[2]));
				short alphaRange = isize > 3 ? (short)(maxs[3]-mins[3]) : 0;
				
				for (int y = 0; y < height; y++) {
					int yDataPos = srcHeight -1 -ypos-(height-1-y);
					for (int x = 0; x < width; x++) {
						short[] rgba = csData.getShortArray(yDataPos, x + xpos);
						short sRed = rgba[0];
						short sGreen = rgba[1];
						short sBlue = rgba[2];
						short sAlpha = isize > 3 ? rgba[3] : 255;
						double temp = (sRed - mins[0]) / redRange;
						sRed = (short) (255 * temp);
						temp = (sGreen - mins[1]) / greenRange;
						sGreen = (short) (255 * temp);
						temp = (sBlue - mins[2]) / blueRange;
						sBlue = (short) (255 * temp);
						if (alphaRange != 0) {
							temp = (sAlpha - mins[3]) / alphaRange;
							sAlpha = (short) (255 * temp);
						}
						softwareImageRGBAdata[si++] = (byte) sRed;
						softwareImageRGBAdata[si++] = (byte) sGreen;
						softwareImageRGBAdata[si++] = (byte) sBlue;
						softwareImageRGBAdata[si++] = (byte) sAlpha;
					}
				}
			}
			break;
			case Dataset.ARRAYINT32:
			{
				CompoundIntegerDataset ciData = (CompoundIntegerDataset)data;
				double mins[] = data.minItem();
				double maxs[] = data.maxItem();
				int redRange = (int)Math.max(1, (maxs[0]-mins[0]));
				int greenRange = (int)Math.max(1,(maxs[1]-mins[1]));
				int blueRange = (int)Math.max(1,(maxs[2]-mins[2]));
				int alphaRange = isize > 3 ? (int)(maxs[3]-mins[3]) : 0;
				
				for (int y = 0; y < height; y++) {
					int yDataPos = srcHeight -1 -ypos-(height-1-y);
					for (int x = 0; x < width; x++) {
						int[] rgba = ciData.getIntArray(yDataPos, x + xpos);
						int sRed = rgba[0];
						int sGreen = rgba[1];
						int sBlue = rgba[2];
						int sAlpha = isize > 3 ? rgba[3] : 255;
						
						double temp = (sRed - mins[0]) / redRange;
						sRed = (int)(255 * temp);
						temp = (sGreen - mins[1]) / greenRange;							
						sGreen = (int)(255 * temp);
						temp = (sBlue - mins[2]) / blueRange;							
						sBlue = (int)(255 * temp);
						if (alphaRange != 0) {
							temp = (sBlue - mins[2]) / alphaRange;							
							sAlpha =  (int)(255 * temp);
						}
						softwareImageRGBAdata[si++] = (byte) sRed;
						softwareImageRGBAdata[si++] = (byte) sGreen;
						softwareImageRGBAdata[si++] = (byte) sBlue;
						softwareImageRGBAdata[si++] = (byte) sAlpha;
					}
				}
			}
			break;
			case Dataset.ARRAYINT64:
			{
				CompoundLongDataset clData = (CompoundLongDataset) data;
				double mins[] = data.minItem();
				double maxs[] = data.maxItem();
				long redRange = (long)Math.max(1, (maxs[0]-mins[0]));
				long greenRange = (long)Math.max(1,(maxs[1]-mins[1]));
				long blueRange = (long)Math.max(1,(maxs[2]-mins[2]));
				long alphaRange = isize > 3 ? (long)(maxs[3]-mins[3]) : 0;
				
				for (int y = 0; y < height; y++) {
					int yDataPos = srcHeight -1 -ypos-(height-1-y);
					for (int x = 0; x < width; x++) {
						long[] rgba = clData.getLongArray(yDataPos, x + xpos);
						long sRed = rgba[0];
						long sGreen = rgba[1];
						long sBlue = rgba[2];
						long sAlpha = isize > 3 ? rgba[3] : 255;

						double temp = (sRed - mins[0]) / redRange;
						sRed = (long)(255 * temp);
						temp = (sGreen - mins[1]) / greenRange;							
						sGreen = (long)(255 * temp);
						temp = (sBlue - mins[2]) / blueRange;							
						sBlue = (long)(255 * temp);
						if (alphaRange != 0) {
							temp = (sBlue - mins[2]) / alphaRange;							
							sAlpha =  (long)(255 * temp);
						}
						softwareImageRGBAdata[si++] = (byte) sRed;
						softwareImageRGBAdata[si++] = (byte) sGreen;
						softwareImageRGBAdata[si++] = (byte) sBlue;
						softwareImageRGBAdata[si++] = (byte) sAlpha;
					}
				}
			}
			break;
			case Dataset.ARRAYFLOAT32:
			case Dataset.ARRAYFLOAT64:
			{
				double mins[] = data.minItem();
				double maxs[] = data.maxItem();
				double redRange = (maxs[0]-mins[0]);
				double greenRange = (maxs[1]-mins[1]);
				double blueRange = (maxs[2]-mins[2]);
				double alphaRange = isize > 3 ? (maxs[3]-mins[3]) : 0;
				
				double[] rgba = new double[4];
				for (int y = 0; y < height; y++) {
					int yDataPos = srcHeight -1 -ypos-(height-1-y);
					for (int x = 0; x < width; x++) {
						data.getDoubleArray(rgba, yDataPos, x+xpos);
						double sRed = rgba[0];
						double sGreen = rgba[1];
						double sBlue = rgba[2];
						double sAlpha = isize > 3 ? rgba[3] : 255.0;
						
						sRed = (255.0 * ((sRed - mins[0]) / redRange));
						sGreen = (255.0 * ((sGreen - mins[1]) / greenRange));
						sBlue = (255.0 * ((sBlue - mins[2]) / blueRange));
						if (alphaRange != 0)
							sAlpha =  (255.0 * ((sAlpha - mins[3]) / alphaRange));
						
						softwareImageRGBAdata[si++] = (byte) sRed;
						softwareImageRGBAdata[si++] = (byte) sGreen;
						softwareImageRGBAdata[si++] = (byte) sBlue;
						softwareImageRGBAdata[si++] = (byte) sAlpha;
					}
				}
			}
			break;
			}
		}
		if (createNewTexture) {
			currentTexture.setImage(new de.jreality.shader.ImageData(softwareImageRGBAdata,width,height));			
			currentTexture.setRepeatS(Texture2D.GL_CLAMP_TO_EDGE);
			currentTexture.setRepeatT(Texture2D.GL_CLAMP_TO_EDGE);
			currentTexture.setMagFilter(Texture2D.GL_NEAREST);
			currentTexture.setMinFilter(Texture2D.GL_LINEAR);
			currentTexture.setMipmapMode(true);
			System.gc();
		} else if (currentTexture != null) {
			de.jreality.shader.ImageData texImg = currentTexture.getImage();
			texImg.updateData(softwareImageRGBAdata);			
		} 
	}
	private void generateTexture(IDataset data, 
								 Texture2D texture,
								 int xPos, int yPos, int width, int height, boolean useRGB)
	{
		boolean createNewTexture = false;

		if (!useRGB) {
			Object imageData = imageDatas.get(texture);
			if (imageData == null ||
				!(imageData instanceof float[]) ||
				((float[])imageData).length != width * height) {
				createNewTexture = true;
			}
			generateFloatTexture(data, texture, xPos, yPos, width, height, createNewTexture);
		} else {
			Object imageData = imageDatas.get(texture);
			if (imageData == null || 
				!(imageData instanceof byte[]) ||
				((byte[])imageData).length != (width * height * 4)) {
				createNewTexture = true;
			}
			generateRGBTexture((CompoundDataset) data,texture,xPos,yPos,width,height,createNewTexture);
		}
	} 
	
	@Override
	public void handleColourCast(ColourImageData colourTable,
			 SceneGraphComponent graph,
			 double minValue,
			 double maxValue) {
		if (tableProg != null)
		{
			tableMin = minValue;
			tableMax = maxValue;
			tableProg.setUniform("minValue",tableMin);
			tableProg.setUniform("maxValue",tableMax);
			byte[] lookupTable = new byte[colourTable.getWidth()*4];
			for (int x = 0; x < colourTable.getWidth(); x++)
			{
				int RGBAvalue = colourTable.get(x,0);
				byte red = (byte) ((RGBAvalue >> 16) & 0xff);
				byte green = (byte) ((RGBAvalue >> 8) & 0xff);
				byte blue = (byte) ((RGBAvalue) & 0xff);
				lookupTable[x * 4] = red;
				lookupTable[x * 4 + 1] = green;
				lookupTable[x * 4 + 2] = blue;
				lookupTable[x * 4 + 3] = ~0;				
			}
			lookupTableImg = null;
			lookupTableImg = new de.jreality.shader.ImageData(lookupTable,colourTable.getWidth(),1);
			Iterator<Appearance> iter = graphApps.iterator();

			while (iter.hasNext())
			{
				Appearance graphAppearance = iter.next();
				Texture2D lookupTex = null;
				switch(numGraphs) {
					case 1:
						lookupTex = (Texture2D)
						AttributeEntityUtility.createAttributeEntity(Texture2D.class,
																	 POLYGON_SHADER+"."+TEXTURE_2D_1,
																	 graphAppearance,true);
					break;
					case 2:
						lookupTex = (Texture2D)
							AttributeEntityUtility.createAttributeEntity(Texture2D.class,
																		 POLYGON_SHADER+"."+TEXTURE_2D_2,
																		 graphAppearance,true);
					break;
					case 3:
						lookupTex = (Texture2D)
							AttributeEntityUtility.createAttributeEntity(Texture2D.class,
																		 POLYGON_SHADER+"."+TEXTURE_2D_3,
																		 graphAppearance,true);
					break;
					case 4:
						lookupTex = (Texture2D)
							AttributeEntityUtility.createAttributeEntity(Texture2D.class,
																		 POLYGON_SHADER+"."+TEXTURE_2D_4,
																		 graphAppearance,true);
					break;
					case 5:
						lookupTex = (Texture2D)
							AttributeEntityUtility.createAttributeEntity(Texture2D.class,
																		 POLYGON_SHADER+"."+TEXTURE_2D_5,
																		 graphAppearance,true);
					break;
					case 6:
						lookupTex = (Texture2D)
							AttributeEntityUtility.createAttributeEntity(Texture2D.class,
																 		POLYGON_SHADER+"."+TEXTURE_2D_6,
																 		graphAppearance,true);
					break;
					default:
						lookupTex = (Texture2D)
							AttributeEntityUtility.createAttributeEntity(Texture2D.class,
																		 POLYGON_SHADER+"."+TEXTURE_2D_7,
																		 graphAppearance,true);
					break;						
				}								
				lookupTex.setMagFilter(Texture2D.GL_LINEAR_MIPMAP_LINEAR);
				lookupTex.setMinFilter(Texture2D.GL_LINEAR_MIPMAP_LINEAR);
				lookupTex.setRepeatS(Texture2D.GL_CLAMP_TO_EDGE);
				lookupTex.setRepeatT(Texture2D.GL_CLAMP_TO_EDGE);
				lookupTex.setImage(lookupTableImg);
			}
		}
	}

	private void updateGraphNodes(Iterator<SceneGraphComponent> graphIter,
								  List<SceneGraphComponent> nodesToAdd,
								  double xSize, 
								  double ySize, 
								  int height, 
								  int width) {

		int maxDimH = JOGLChecker.getMaxTextureHeight();
		int maxDimW = JOGLChecker.getMaxTextureWidth();
		double yStart = 0.0;
		double ySubSize = ((double)maxDimH/(double)maxHeight) < 1.0 ?
							ySize*maxDimH/maxHeight : ySize; 
		double xSubSize = ((double)maxDimW/(double)maxWidth) < 1.0 ?
							xSize*maxDimW/maxWidth : xSize; 				

		for (int y = 0; y < height; y+= maxDimH, yStart+=ySubSize) {
			double xStart = 0.0;
			for (int x = 0; x < width; x+= maxDimW, xStart+=xSubSize) {
				SceneGraphComponent subGraph = null;
				if (graphIter.hasNext())
					subGraph = graphIter.next();
				else {
					subGraph =
						SceneGraphUtility.createFullSceneGraphComponent(GRAPHNODENAME+numGraphs+".sub"+x+"_"+y);

					nodesToAdd.add(subGraph);
					graphGroupNode.addChild(subGraph);	
					Appearance graphAppearance = createAppearance();
					graphApps.add(graphAppearance);
					subGraph.setAppearance(graphAppearance);	
				}		
				subGraph.setGeometry(createGraphGeometry((xStart+xSubSize > xSize ? xSize-xStart : xSubSize),
														 (yStart+ySubSize > ySize ? ySize-yStart : ySubSize),
														  xStart,yStart));

			}
		}
	}
	
	private void updateTextures(List<IDataset> datasets, double xSize, double ySize) {
		Iterator<IDataset> iter = datasets.iterator();
		numGraphs = 0;
		while (iter.hasNext())
		{
			IDataset currentData = iter.next();
			int maxDimH = JOGLChecker.getMaxTextureHeight();
			int maxDimW = JOGLChecker.getMaxTextureWidth();
			double ySubSize = ((double)maxDimH/(double)maxHeight) < 1.0 ?
								ySize*maxDimH/maxHeight : ySize; 
			double xSubSize = ((double)maxDimW/(double)maxWidth) < 1.0 ?
								xSize*maxDimW/maxWidth : xSize; 
			float xScale = (float) (xSubSize / xSize);
			float yScale = (float) (ySubSize / ySize);
			maxDimH = (int)(maxDimH * yScale);
			maxDimW = (int)(maxDimW * xScale);
			
			int width = currentData.getShape()[1];
			int height = currentData.getShape()[0];
			int currentApp = 0;
			for (int y = 0; y < height; y+= maxDimH) {
				for (int x = 0; x < width; x+= maxDimW, currentApp++) {
					graphAppearance = graphApps.get(currentApp);
					int texWidth = (width > maxDimW ? maxDimW : width);
					int texHeight = (height > maxDimH ? maxDimH : height);
					if (x+texWidth > width) texWidth = maxWidth-x;
					if (y+texHeight > height) texHeight = maxHeight-y;
					Texture2D texture = null;
					switch (numGraphs) {
						case 0 :
							texture = (Texture2D)
								AttributeEntityUtility.createAttributeEntity(Texture2D.class,
																			POLYGON_SHADER+"."+TEXTURE_2D,
																			graphAppearance,true);
						break;
						case 1 :
							texture = (Texture2D)
								AttributeEntityUtility.createAttributeEntity(Texture2D.class,
																			POLYGON_SHADER+"."+TEXTURE_2D_1,
																			graphAppearance,true);
						break;
						case 2 :
							texture = (Texture2D)
								AttributeEntityUtility.createAttributeEntity(Texture2D.class,
																			POLYGON_SHADER+"."+TEXTURE_2D_2,
																			graphAppearance,true);
						break;
						case 3 :
							texture = (Texture2D)
								AttributeEntityUtility.createAttributeEntity(Texture2D.class,
																			POLYGON_SHADER+"."+TEXTURE_2D_3,
																			graphAppearance,true);
						break;
						case 4 :
							texture = (Texture2D)
								AttributeEntityUtility.createAttributeEntity(Texture2D.class,
																			POLYGON_SHADER+"."+TEXTURE_2D_4,
																			graphAppearance,true);
						break;
						case 5 :
							texture = (Texture2D)
								AttributeEntityUtility.createAttributeEntity(Texture2D.class,
																			POLYGON_SHADER+"."+TEXTURE_2D_5,
																			graphAppearance,true);
						break;
						case 6 :
							texture = (Texture2D)
								AttributeEntityUtility.createAttributeEntity(Texture2D.class,
																			POLYGON_SHADER+"."+TEXTURE_2D_6,
																			graphAppearance,true);
						break;
					}
					if (texture != null) {
						boolean useRGB = 
							(currentData instanceof RGBDataset) ||
							(currentData instanceof CompoundDataset &&
							(currentData.getElementsPerItem() == 3 ||
			 				 currentData.getElementsPerItem() == 4));
						isRGB[numGraphs] = (useRGB ? 1 : 0);
						generateTexture(currentData, texture, x,y, texWidth, texHeight,useRGB);
					}
				}
			}	
			numGraphs++;
		}
	}
	
	@Override
	public void updateGraph(IDataset newData) {
		logger.warn("This function is not supported!");
		// Not supported!
	}
	
	@Override
	public void updateGraph(List<IDataset> datasets) {
		if (datasets.size() > 0 && subGraphs.size() > 0 && hasJOGLshaders)
		{
			Iterator<IDataset> iter = datasets.iterator();	
		    subGraphs.get(subGraphs.size()-1).removeChild(overlayPrimitiveGroupNode);
			Iterator<SceneGraphComponent> graphIter = subGraphs.iterator();
			maxWidth = 0;
			maxHeight = 0;
			LinkedList<SceneGraphComponent> nodesToAdd = new LinkedList<SceneGraphComponent>(); 
			double xSize =0, ySize = 0;
			int width = 0;
			int height = 0;

			while (iter.hasNext())
			{
				IDataset currentData = iter.next();
				width = currentData.getShape()[1];
				height = currentData.getShape()[0];
			
				maxWidth = Math.max(width,maxWidth);
				maxHeight = Math.max(height,maxHeight);
			}
			if (maxWidth >= maxHeight)
			{
				xSize = MAXX;
				ySize = (MAXX * maxHeight)/maxWidth;
			} else {
				ySize = MAXX;
				xSize = (MAXX * maxWidth)/maxHeight;
			}
			xSize = Math.max(xSize, 3.0);
			ySize = Math.max(ySize, 3.0);
			
			updateGraphNodes(graphIter,nodesToAdd,xSize,ySize,height,width);
			// remove no longer used subgraphs
			LinkedList<SceneGraphComponent> nodesToBeDeleted = new LinkedList<SceneGraphComponent>();
			while (graphIter.hasNext()) {
				SceneGraphComponent subGraph = graphIter.next();
				nodesToBeDeleted.add(subGraph);
				subGraph.setGeometry(null);
				graphGroupNode.removeChild(subGraph);
				graphApps.remove(subGraph.getAppearance());
			}
			subGraphs.removeAll(nodesToBeDeleted);
			subGraphs.addAll(nodesToAdd);
		    subGraphs.get(subGraphs.size()-1).addChild(overlayPrimitiveGroupNode);			
			nodesToBeDeleted.clear();
			nodesToAdd.clear();
			posTool.setImageHeight(maxHeight);
			posTool.setImageWidth(maxWidth);
			double[] sizes = determineXYsize();
			updateClipPlanePositions(sizes);
			posTool.setMaxXY(sizes[0],sizes[1]);
			panTool.setDataDimension(sizes[0], sizes[1]);
			determineRanges(datasets);
			imageDatas.clear();
			updateTextures(datasets, xSize, ySize);
			Iterator<Appearance> graphApIter = graphApps.iterator();
			while (graphApIter.hasNext()) {
				Appearance app = graphApIter.next();
				loadGLSLProgram(app,tableMin,tableMax);
				setShaderOnAppearance(app);							
			}			
			if (xTickNode != null) 
				xTickNode.setGeometry(createXTicksGeometry());
			if (yTickNode != null)
				yTickNode.setGeometry(createYTicksGeometry());
			if (xLabelNode != null)
				xLabelNode.setGeometry(createXLabelsGeometry());
			if (yLabelNode != null)
				yLabelNode.setGeometry(createYLabelsGeometry());
			if (titleLabel != null)
				titleLabel.setGeometry(createTitleGeometry());			
			axis.setGeometry(createAxisGeometry(sizes[0],sizes[1]));
			createYAxisLabelGeom();
			createXAxisLabelGeom();
		} 
	}
	
	public void updateCompositingSettings(List<CompositeEntry> list) {
		if (tableProg != null) {
			float[] weights = new float[]{0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f};
			float[] masks = new float[21];
			int[] ops = new int[]{0,0,0,0,0,0,0};
			for (int i = 0; i < (list.size() < MAX_IMAGES ? list.size() : MAX_IMAGES); i++) {
				weights[i] = list.get(i).getWeight();
				masks[i*3] = (list.get(i).getChannelMask()&1) == 1 ? 1.0f : 0.0f;
				masks[i*3+1] = (list.get(i).getChannelMask()&2) == 2 ? 1.0f : 0.0f;
				masks[i*3+2] = (list.get(i).getChannelMask()&4) == 4 ? 1.0f : 0.0f;
				switch(list.get(i).getOperation()) {
					case ADD:
						ops[i] = 0;
					break;
					case SUBTRACT:
						ops[i] = 1;
					break;
					case MULTIPLY:
						ops[i] = 2;
					break;
					case DIVIDE:
						ops[i] = 3;
					break;
					case MAX:
						ops[i] = 4;
					break;
					case MIN:
						ops[i] = 5;
					break;
				}
			}
			tableProg.setUniform("weight", weights);
			tableProg.setUniform("ops", ops);
			tableProg.setUniform("mask",masks);
		}
	}
}
