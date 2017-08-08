package org.dawnsci.surfacescatter.ui;

import java.util.EnumMap;
import org.apache.commons.lang.ArrayUtils;
import org.dawnsci.surfacescatter.ReflectivityAngleAliasEnum;
import org.dawnsci.surfacescatter.ReflectivityFluxParametersAliasEnum;
import org.dawnsci.surfacescatter.SXRDAngleAliasEnum;
import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;

import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;

public class AnglesAliasWindow extends Composite{

	
	private TabFolder folder;
	private SurfaceScatterPresenter ssp;
	private Combo[] angleCombosSXRD;
	private Combo[] angleCombosReflectivity;
	private Combo[] fluxCombosReflectivity;
	private EnumMap<SXRDAngleAliasEnum, Combo> sXRDMap;
	private EnumMap<ReflectivityAngleAliasEnum, Combo> reflectivityMap;
	private EnumMap<ReflectivityFluxParametersAliasEnum, Combo> reflectivityFluxMap;
	private Text fluxPath;
	private String fluxpathStorage = " ";
	private SurfaceScatterViewStart ssvs;

	public AnglesAliasWindow(Composite parent, 
									  int style,
									  SurfaceScatterPresenter ssp,
									  SurfaceScatterViewStart ssvs){
		
		super(parent, style);
        
        this.ssp = ssp;
        this.ssvs = ssvs;
        this.createContents();
        
	}
	
	public void createContents() {
		
		folder = new TabFolder(this, SWT.NONE);
		folder.setLayoutData(new GridData(GridData.FILL_BOTH));
		
	    //Tab 1
	    TabItem paramsSXRD = new TabItem(folder, SWT.NONE);
	    paramsSXRD.setText("SXRD Parameters");
	   
		Group geometricParametersSX = new Group(folder, SWT.NULL);
		GridLayout geometricParametersSXLayout = new GridLayout(2,true);
		GridData geometricParametersSXData = new GridData(GridData.FILL_BOTH);
		geometricParametersSX.setLayout(geometricParametersSXLayout);
		geometricParametersSX.setLayoutData(geometricParametersSXData);
		
		sXRDMap = new EnumMap<SXRDAngleAliasEnum, Combo>(SXRDAngleAliasEnum.class);
		angleCombosSXRD = new Combo[SXRDAngleAliasEnum.values().length-1];
	    
		for(int i = 0; i<angleCombosSXRD.length; i++){
			SXRDAngleAliasEnum key = SXRDAngleAliasEnum.values()[i];
			if(key != SXRDAngleAliasEnum.NULL){
				InputTileGenerator aT = new InputTileGenerator(key.getAngleVariable(), geometricParametersSX);
				angleCombosSXRD[i]= aT.getCombo();
				sXRDMap.put(key, aT.getCombo()); 
				aT.getCombo().addSelectionListener(new SelectionListener() {
					
					@Override
					public void widgetSelected(SelectionEvent e) {
						anglesUpdate(e, key);
						
					}
					
					@Override
					public void widgetDefaultSelected(SelectionEvent e) {
						// TODO Auto-generated method stub
						
					}
				});
			}	
		}
		
		paramsSXRD.setControl(geometricParametersSX);
	   	    
	    //Tab 2
	    TabItem paramsReflec = new TabItem(folder, SWT.NONE);
	    paramsReflec.setText("Reflectivity Parameters");
	    
		Group geometricParametersReflecTiles = new Group(folder, SWT.NULL);
		GridLayout geometricParametersReflecTilesLayout = new GridLayout(2,true);
		GridData geometricParametersReflecTilesData= new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		geometricParametersReflecTiles.setLayout(geometricParametersReflecTilesLayout);
		geometricParametersReflecTiles.setLayoutData(geometricParametersReflecTilesData);
		
		Button fluxPathSelection = new Button(geometricParametersReflecTiles, SWT.PUSH | SWT.FILL);
		fluxPathSelection.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		fluxPathSelection.setText("Select flux correction file");
		fluxPath = new Text (geometricParametersReflecTiles, SWT.CHECK);
		fluxPath.setText(fluxpathStorage);
		fluxPath.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	    
		
		fluxPathSelection.addSelectionListener(new SelectionListener() {
				
				@Override
				public void widgetSelected(SelectionEvent e) {
					getFluxFile();
				}
				
				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
					// TODO Auto-generated method stub
					
				}
			});
		    
		fluxPath.addModifyListener(new ModifyListener(){

				@Override
				public void modifyText(ModifyEvent e) {
					fluxpathStorage = fluxPath.getText();
					
					IDataHolder dh1 = null;
					
					try {
						dh1 = LoaderFactory.getData(fluxpathStorage);
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					
					String[] fluxOptions = dh1.getNames();
					updateFluxWithOptions(fluxOptions,
								  			  true);

				}
		    	
		    });
		    
		
		reflectivityMap = new EnumMap<ReflectivityAngleAliasEnum, Combo>(ReflectivityAngleAliasEnum.class);
		angleCombosReflectivity = new Combo[ReflectivityAngleAliasEnum.values().length-1];
	    
		for(int i = 0; i<angleCombosReflectivity.length; i++){
			ReflectivityAngleAliasEnum key = ReflectivityAngleAliasEnum.values()[i];
			if(key != ReflectivityAngleAliasEnum.NULL){
				InputTileGenerator aT = new InputTileGenerator(key.getAngleVariable(), geometricParametersReflecTiles);
				angleCombosReflectivity[i]= aT.getCombo();
				reflectivityMap.put(key, aT.getCombo());
				aT.getCombo().addSelectionListener(new SelectionListener() {
					
					@Override
					public void widgetSelected(SelectionEvent e) {
						anglesUpdate(e, key);
						
					}
					
					@Override
					public void widgetDefaultSelected(SelectionEvent e) {
						// TODO Auto-generated method stub
						
					}
				});
			}
		}
		
		reflectivityFluxMap = new EnumMap<ReflectivityFluxParametersAliasEnum, Combo>(ReflectivityFluxParametersAliasEnum.class);
		fluxCombosReflectivity = new Combo[ReflectivityFluxParametersAliasEnum.values().length-1];
	   
		
		for(int i = 0; i<fluxCombosReflectivity.length; i++){
			ReflectivityFluxParametersAliasEnum key = ReflectivityFluxParametersAliasEnum.values()[i];
			if(key != ReflectivityFluxParametersAliasEnum.NULL){
				InputTileGenerator aT = new InputTileGenerator(key.getFluxVariable(), geometricParametersReflecTiles);
				fluxCombosReflectivity[i]= aT.getCombo();
				reflectivityFluxMap.put(key, aT.getCombo());
				aT.getCombo().addSelectionListener(new SelectionListener() {
					
					@Override
					public void widgetSelected(SelectionEvent e) {
						fluxUpdate(e, key);
						
					}
					
					@Override
					public void widgetDefaultSelected(SelectionEvent e) {
						// TODO Auto-generated method stub
						
					}
				});
			}
		}
	
		paramsReflec.setControl(geometricParametersReflecTiles);
	 	
	}	

	public TabFolder getFolder() {
		return folder;
	}

	public void setFolder(TabFolder folder) {
		this.folder = folder;
	}

	public SurfaceScatterPresenter getSsp() {
		return ssp;
	}

	public void setSsp(SurfaceScatterPresenter ssp) {
		this.ssp = ssp;
	}
	
	public TabFolder getTabFolder(){
		return folder;
	}
	
	public void updateAllWithOptions(String[] options,
									 boolean useDefault){
		
			
		for(Combo c1 : angleCombosSXRD){
			
			for(String sg : options){
					c1.add(sg);
			}
			
			String alias = getCorrectSXRDAngleAliasEnum(c1).getAngleAlias();
			
			if(useDefault && ArrayUtils.contains(options, alias)){
				c1.setText(alias);
			}
		}
		
		for(Combo c2 : angleCombosReflectivity){
			
			for(String sg : options){
					c2.add(sg);
			}
			
			String alias = getCorrectReflectivityAngleAliasEnum(c2).getAngleAlias();
			
			if(useDefault && ArrayUtils.contains( options, alias)){
				  c2.setText(alias);
			}
		}
	}
	
	public void updateFluxWithOptions(String[] options,
			 						  boolean useDefault){


		for(Combo c1 : fluxCombosReflectivity){
		
			for(String sg : options){
				c1.add(sg);
			}
			
			String alias = getCorrectFluxAliasEnum(c1).getFluxAlias();
			String alias2 = getCorrectFluxAliasEnum(c1).getFluxSecondAlias();
			
			
			if(useDefault && ArrayUtils.contains(options, alias)){
				c1.setText(alias);
			}
			
			else if(useDefault && ArrayUtils.contains(options, alias2)){
				c1.setText(alias2);
			}
		}
		
	}
			
	public void anglesUpdate(SelectionEvent e,
							 SXRDAngleAliasEnum angle){
		
		Combo fire = (Combo) e.getSource();
		
		String angleAlias = fire.getText();
		angle.setAngleAlias(angleAlias);
		
	}
	
	public void anglesUpdate(SelectionEvent e,
			 				 ReflectivityAngleAliasEnum angle){

		Combo fire = (Combo) e.getSource();

		
		String angleAlias = fire.getText();
		angle.setAngleAlias(angleAlias);


	}
	
	public void fluxUpdate(SelectionEvent e,
			 			   ReflectivityFluxParametersAliasEnum angle){

		Combo fire = (Combo) e.getSource();
		
		
		String fluxAlias = fire.getText();
		angle.setFluxAlias(fluxAlias);
	
	}
		
	private SXRDAngleAliasEnum getCorrectSXRDAngleAliasEnum (Combo c1){
		
		for(SXRDAngleAliasEnum ae : SXRDAngleAliasEnum.values()){
			if(sXRDMap.get(ae) == c1){
				return ae;
			}
		}
		
		return SXRDAngleAliasEnum.NULL;
	}
	
	private ReflectivityAngleAliasEnum getCorrectReflectivityAngleAliasEnum (Combo c1){
		
		for(ReflectivityAngleAliasEnum ae : ReflectivityAngleAliasEnum.values()){
			if(reflectivityMap.get(ae) == c1){
				return ae;
			}
		}
		
		return ReflectivityAngleAliasEnum.NULL;
	}
	
	private ReflectivityFluxParametersAliasEnum getCorrectFluxAliasEnum (Combo c1){
		
		for(ReflectivityFluxParametersAliasEnum ae : ReflectivityFluxParametersAliasEnum.values()){
			if(reflectivityFluxMap.get(ae) == c1){
				return ae;
			}
		}
		
		return ReflectivityFluxParametersAliasEnum.NULL;
	}
	
	private void getFluxFile(){
		
		FileDialog dlg = new FileDialog(ssvs.getShell(), SWT.OPEN);
		
		if(fluxpathStorage != null){
		
			dlg.setFilterPath(fluxpathStorage);
		}

        dlg.setText("flux file");

        fluxpathStorage = dlg.open();
        
		fluxPath.setText(fluxpathStorage);
		
		IDataHolder dh1 = null;
		
		try {
			dh1 = LoaderFactory.getData(fluxpathStorage);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		String[] fluxOptions = dh1.getNames();
		updateFluxWithOptions(fluxOptions,
					  			  true);
	}
	
	public void writeOutValues(){
		
		EnumMap<SXRDAngleAliasEnum, String> sXRDStringMap = new EnumMap<>(SXRDAngleAliasEnum.class);
		EnumMap<ReflectivityAngleAliasEnum, String> reflectivityStringMap  = new EnumMap<>(ReflectivityAngleAliasEnum.class);
		EnumMap<ReflectivityFluxParametersAliasEnum, String> reflectivityFluxStringMap = new EnumMap<>(ReflectivityFluxParametersAliasEnum.class);

		
		
		for(SXRDAngleAliasEnum ae : SXRDAngleAliasEnum.values()){
			if(ae != SXRDAngleAliasEnum.NULL){
				String alias = sXRDMap.get(ae).getText();
				ae.setAngleAlias(alias);
				sXRDStringMap.put(ae, alias);
			}
		}
		
		for(ReflectivityAngleAliasEnum ae : ReflectivityAngleAliasEnum.values()){
			if(ae != ReflectivityAngleAliasEnum.NULL){
				String alias = reflectivityMap.get(ae).getText();
				ae.setAngleAlias(alias);
				reflectivityStringMap.put(ae, alias);
			}
		}
		
		for(ReflectivityFluxParametersAliasEnum ae : ReflectivityFluxParametersAliasEnum.values()){
			if(ae != ReflectivityFluxParametersAliasEnum.NULL){
				String alias = reflectivityFluxMap.get(ae).getText();
				ae.setFluxAlias(alias);
				reflectivityFluxStringMap.put(ae, alias);
			}
		}
		
		ssp.writeOutAngleAliases(sXRDStringMap, 
								 reflectivityStringMap, 
								 reflectivityFluxStringMap);
		
		ssp.writeFluxFilePathToGeometricModel(fluxpathStorage);
		
	}

	
	public void setFluxPath(String f){
		fluxPath.setText(f);
	}
}
