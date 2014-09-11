package org.dawnsci.isosurface;

import java.util.List;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.MeshView;
import javafx.stage.Stage;

import org.dawnsci.isosurface.impl.MarchingCubesModel;
import org.dawnsci.isosurface.impl.Surface;

import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;
import uk.ac.diamond.scisoft.analysis.io.IDataHolder;
import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;
import uk.ac.diamond.scisoft.analysis.monitor.IMonitor;
import uk.ac.diamond.scisoft.analysis.processing.IOperation;
import uk.ac.diamond.scisoft.analysis.processing.IOperationService;

/**
 * 
 * @author nnb55016
 * The following class visualises the surface in a stand-alone JavaFX application
 * The slider for the isovalue enables the user to change the default value and visualise the changing 
 * surface. Moreover, the user can choose the dimensions of the cube used in the marching cubes algorithm.
 * The default size is fairly large which generates a smoother and less accurate surface. A 1x1x1 cube will
 * generate a more accurate surface, however it will make the visualisation much slower.
 * 
 * Transformations can be performed on the surface.
 * 
 * The class is similar to SurfaceDisplayer which is used in DAWN. However this one creates a JavaFX Application
 */
@SuppressWarnings("restriction")
public class SurfaceVisualisation extends Application{

	private IOperation<MarchingCubesModel, Surface> generator;
	
	private ILazyDataset lazyLoaded;
	private Slider isovalue;
	private Label isovalueLabel;
	private TextField xDimenssion;
	private TextField yDimenssion;
	private TextField zDimenssion;
	private final SurfaceTransformations cam = new SurfaceTransformations();
	private final SurfaceTransformations camOffset = new SurfaceTransformations();
	private MeshView isosurface;
	private double surfaceX = 400; 
	private double surfaceY = 520; 
	private double surfaceZ = 150;
	private double surfaceScaleX = 3;
	private double surfaceScaleY = 3;
	private double surfaceScaleZ = 3;
    private double mousePosX;
    private double mousePosY;
    private double mouseOldX;
    private double mouseOldY;
    private double mouseDeltaX;
    private double mouseDeltaY;
	
	
	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage stage) throws Exception {
		// TODO Auto-generated method stub
		readData();
		
		camOffset.getChildren().add(cam);
		
		Group root = new Group();
		Scene scene = new Scene(root, 1000, 1000, true);
	
		scene.setCamera(new PerspectiveCamera());

		final IOperationService service = (IOperationService)Activator.getService(IOperationService.class);
		try {
			generator = (IOperation<MarchingCubesModel, Surface>) service.create("org.dawnsci.isosurface.marchingCubes");
		} catch (Exception e) {
			e.printStackTrace();
		}
		generator.getModel().setLazyData(lazyLoaded);
		
		cam.getChildren().add(surfaceAxisGenerator());
		
		cam.p.setX(scene.getWidth()/2.0);
        cam.ip.setX(-scene.getWidth()/2.0);
        cam.p.setY(scene.getHeight()/2.0);
        cam.ip.setY(-scene.getHeight()/2.0);
        
        
        frameCam(scene);

		root.getChildren().addAll(generateControls(), camOffset);
		
		isovalue.valueProperty().addListener(new ChangeListener<Number>(){

			@Override
			public void changed(ObservableValue<? extends Number> observable,
					Number oldValue, Number newValue) {
				// TODO Auto-generated method stub
				updateTransforms();
				cam.getChildren().remove(0);
				try {
					generator.getModel().setIsovalue(newValue.doubleValue());
					cam.getChildren().add(surfaceAxisGenerator());
			        isovalueLabel.textProperty().set("Isovalue of " + generator.getModel().getIsovalue());

			        getCurrentCoordinates();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		});
		
		xDimenssion.setOnAction(new EventHandler<ActionEvent>(){

			@Override
			public void handle(ActionEvent event) {
				// TODO Auto-generated method stub
				if (	xDimenssion.getText().matches("\\d+") &&
						(Integer.parseInt(xDimenssion.getText()) > 0
						&& Integer.parseInt(xDimenssion.getText()) < lazyLoaded.getShape()[2])){
					
					generator.getModel().setBoxSize(new int[] {
							Integer.parseInt(xDimenssion.getText()),
							generator.getModel().getBoxSize()[1],
							generator.getModel().getBoxSize()[2] });
					
					updateTransforms();
					cam.getChildren().remove(0);
					
					try {
						cam.getChildren().add(surfaceAxisGenerator());
						
				        getCurrentCoordinates();

					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
				
				else {
					xDimenssion.setText("" + generator.getModel().getBoxSize()[0]);
				}
			}
			
		});
		
		yDimenssion.setOnAction(new EventHandler<ActionEvent>(){

			@Override
			public void handle(ActionEvent event) {
				// TODO Auto-generated method stub
				if (	yDimenssion.getText().matches("\\d+") &&
						(Integer.parseInt(yDimenssion.getText()) > 0
						&& Integer.parseInt(yDimenssion.getText()) < lazyLoaded.getShape()[1])) {
					
					generator.getModel().setBoxSize(new int[] {
							generator.getModel().getBoxSize()[0],
							Integer.parseInt(yDimenssion.getText()),
							generator.getModel().getBoxSize()[2] });
					
					updateTransforms();
					cam.getChildren().remove(0);
					
					try {
						cam.getChildren().add(surfaceAxisGenerator());
						
						getCurrentCoordinates();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		
				}
				
				else {
					xDimenssion.setText("" + generator.getModel().getBoxSize()[1]);
				}
			}
			
		});
		
		zDimenssion.setOnAction(new EventHandler<ActionEvent>(){

			@Override
			public void handle(ActionEvent event) {
				// TODO Auto-generated method stub
				if (	zDimenssion.getText().matches("\\d+") &&
						(Integer.parseInt(zDimenssion.getText()) > 0
						&& Integer.parseInt(zDimenssion.getText()) < lazyLoaded.getShape()[0])){
					
					generator.getModel().setBoxSize(new int[] {
							generator.getModel().getBoxSize()[0],
							generator.getModel().getBoxSize()[1],
							Integer.parseInt(zDimenssion.getText()) });
					
					updateTransforms();
					cam.getChildren().remove(0);
					
					try {
						cam.getChildren().add(surfaceAxisGenerator());
						getCurrentCoordinates();

					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				else {
					xDimenssion.setText("" + generator.getModel().getBoxSize()[2]);
				}
			}
			
		});
		
		getCurrentCoordinates();
		
		scene.setOnMousePressed(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent me) {
                mousePosX = me.getX();
                mousePosY = me.getY();
            }
        });
		
		scene.setOnMouseDragged(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent me) {
                mouseOldX = mousePosX;
                mouseOldY = mousePosY;
                mousePosX = me.getX();
                mousePosY = me.getY();
                mouseDeltaX = mousePosX - mouseOldX;
                mouseDeltaY = mousePosY - mouseOldY;
                if (me.isControlDown() && me.isPrimaryButtonDown()) {
                    double rzAngle = cam.zRotation.getAngle()%360;
                    cam.zRotation.setAngle(rzAngle - mouseDeltaX);
 
                }
                else if (!me.isControlDown() && me.isPrimaryButtonDown()) {
                	
                    double ryAngle = cam.yRotation.getAngle()%360;
                    cam.yRotation.setAngle(ryAngle - mouseDeltaX);                   
                    
                    double rxAngle = cam.xRotation.getAngle()%360;
                    cam.xRotation.setAngle(rxAngle + mouseDeltaY);
                
                }

                else if (me.isMiddleButtonDown()) {
                    double tx = cam.translation.getX();
                    double ty = cam.translation.getY();
                    cam.translation.setX(tx + mouseDeltaX);
                    cam.translation.setY(ty + mouseDeltaY);
                }                
            }
        });
		
		scene.setOnScroll(new EventHandler<ScrollEvent>() {
        	@Override public void handle(ScrollEvent event){
        		cam.scaling.setX(cam.scaling.getX() + event.getDeltaY()/800);
        		cam.scaling.setY(cam.scaling.getY() + event.getDeltaY()/800);    
        		cam.scaling.setZ(cam.scaling.getZ() + event.getDeltaY()/800);
        	}
        });
		
        stage.setScene(scene);
        stage.setTitle("Isosurface Prototype");
        stage.show();
	}

	public void readData() throws Exception{
		
//		IDataHolder data = LoaderFactory.getData("C:/Users/nnb55016/Documents/test.nxs");
		IDataHolder data = LoaderFactory.getData("\\\\dls-science/science/groups/das/ExampleData/large test files/TomographyDataSet.hdf5");
//		IDataHolder data = LoaderFactory.getData("\\\\dls-science/science/groups/das/ExampleData/OpusData/brain.h5");
//		IDataHolder data = LoaderFactory.getData("\\\\dls-science/science/groups/das/ExampleData/i05/i05-1130-result_with_energyAxis.nxs");

		List<ILazyDataset> list = data.getList();
		for (ILazyDataset iLazyDataset : list) {
			if(iLazyDataset.getShape().length == 3 && iLazyDataset.getName().equals("data")) {
				this.lazyLoaded = iLazyDataset;
				break;
			}
		}
				
	}

	public Group surfaceAxisGenerator() throws Exception{
		Group group = new Group();
		
		Surface result = generator.execute(null, new IMonitor.Stub());
		this.isosurface = new MeshView(result.createTrangleMesh());

		isosurface.setMaterial(new PhongMaterial(Color.GOLDENROD));
		isosurface.setCullFace(CullFace.NONE);
        isosurface.setScaleX(surfaceScaleX);
        isosurface.setScaleY(surfaceScaleY);
        isosurface.setScaleZ(surfaceScaleZ);
      
		isosurface.setTranslateX(surfaceX);
        isosurface.setTranslateY(surfaceY);
        isosurface.setTranslateZ(surfaceZ);

        group.getChildren().add(isosurface);
		
        return group;
	}
	
	public VBox generateControls(){
		
		VBox controls = new VBox();	
		
		this.isovalue =  new Slider(generator.getModel().getIsovalueMin(), generator.getModel().getIsovalueMax(), generator.getModel().getIsovalue());
		isovalue.setMajorTickUnit(1);
        isovalue.setSnapToTicks(true);
        
        isovalueLabel = new Label("Isovalue of " + generator.getModel().getIsovalue(), isovalue);
        isovalueLabel.setContentDisplay(ContentDisplay.LEFT);
        
        Group slider = new Group();
        slider.getChildren().addAll(isovalue, isovalueLabel);
        
        xDimenssion = new TextField("" + generator.getModel().getBoxSize()[0]);
        xDimenssion.setPrefColumnCount(2);
        
        Label xLabel = new Label("Size of the cube in x direction", xDimenssion);
        xLabel.setContentDisplay(ContentDisplay.BOTTOM);
        
        Group x = new Group();
        x.getChildren().addAll(xDimenssion, xLabel);
        
        yDimenssion = new TextField("" + generator.getModel().getBoxSize()[1]);
        yDimenssion.setPrefColumnCount(2);
        
        Label yLabel = new Label("Size of the cube in y direction", yDimenssion);
        yLabel.setContentDisplay(ContentDisplay.BOTTOM);
        
        Group y = new Group();
        y.getChildren().addAll(yDimenssion, yLabel);
        
        zDimenssion = new TextField("" + generator.getModel().getBoxSize()[2]);
        zDimenssion.setPrefColumnCount(2);
        
        Label zLabel = new Label("Size of the cube in z direction", zDimenssion);
        zLabel.setContentDisplay(ContentDisplay.BOTTOM);
        
        Group z = new Group();
        z.getChildren().addAll(zDimenssion, zLabel);
		
        controls.getChildren().addAll(slider,x,y,z);
		
        
        return controls;
	}
	
	public void updateTransforms(){
		surfaceX = isosurface.getTranslateX();
		surfaceY = isosurface.getTranslateY();
		surfaceZ = isosurface.getTranslateZ();
		
		surfaceScaleX = isosurface.getScaleX();
		surfaceScaleY = isosurface.getScaleY();
		surfaceScaleZ = isosurface.getScaleZ();
	}
	
	public void getCurrentCoordinates(){
        isosurface.setOnMouseMoved(new EventHandler<MouseEvent>(){

			@Override
			public void handle(MouseEvent event) {
				// TODO Auto-generated method stub
	
				double temporaryX = event.getX();
				double temporaryY = event.getY();
				double temporaryZ = event.getZ();

//				System.out.println("point: " + temporaryX + ", " + temporaryY + ", " + temporaryZ);
			}
        	
        });

	}
	
	public void setCamOffset(final SurfaceTransformations camOffset, final Scene scene) {
        double width = scene.getWidth();
        double height = scene.getHeight();
        camOffset.translation.setX(width/2.0);
        camOffset.translation.setY(height/2.0);
    }
	
	public void setCamPivot(final SurfaceTransformations cam) {
        final Bounds bounds = cam.getBoundsInLocal();
        final double pivotX = bounds.getMinX() + bounds.getWidth()/2;
        final double pivotY = bounds.getMinY() + bounds.getHeight()/2;
        final double pivotZ = bounds.getMinZ() + bounds.getDepth()/2;
        cam.p.setX(pivotX);
        cam.p.setY(pivotY);
        cam.p.setZ(pivotZ);
        cam.ip.setX(-pivotX);
        cam.ip.setY(-pivotY);
        cam.ip.setZ(-pivotZ);
    }
	
    public void setCamTranslate(final SurfaceTransformations cam) {
        final Bounds bounds = cam.getBoundsInLocal();
        final double pivotX = bounds.getMinX() + bounds.getWidth()/2;
        final double pivotY = bounds.getMinY() + bounds.getHeight()/2;
        cam.translation.setX(-pivotX);
        cam.translation.setY(-pivotY);
            
    }
    
    public void frameCam(final Scene scene) {
        setCamOffset(camOffset, scene);
        setCamPivot(cam);
        setCamTranslate(cam);
    }
}
