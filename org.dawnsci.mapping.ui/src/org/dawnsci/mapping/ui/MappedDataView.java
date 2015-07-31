package org.dawnsci.mapping.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.dawnsci.mapping.ui.datamodel.AssociatedImage;
import org.dawnsci.mapping.ui.datamodel.MapObject;
import org.dawnsci.mapping.ui.datamodel.MappedData;
import org.dawnsci.mapping.ui.datamodel.MappedDataArea;
import org.dawnsci.mapping.ui.datamodel.MappedDataBlock;
import org.dawnsci.mapping.ui.datamodel.MappedDataFile;
import org.dawnsci.mapping.ui.datamodel.MappedFileDescription;
import org.dawnsci.mapping.ui.datamodel.MappedFileFactory;
import org.dawnsci.mapping.ui.wizards.ImportMappedDataWizard;
import org.eclipse.dawnsci.analysis.api.conversion.IConversionContext.ConversionScheme;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyDataset;
import org.eclipse.dawnsci.analysis.api.metadata.AxesMetadata;
import org.eclipse.dawnsci.analysis.api.metadata.MaskMetadata;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.axis.ClickEvent;
import org.eclipse.dawnsci.plotting.api.axis.IClickListener;
import org.eclipse.dawnsci.plotting.api.trace.ICompositeTrace;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.part.ViewPart;

public class MappedDataView extends ViewPart {

	private TreeViewer viewer;
	private IPlottingSystem map;
	private IPlottingSystem spectrum;
	private MappedDataArea area;
	private List<MapObject> layers;
	
	
	@Override
	public void createPartControl(Composite parent) {
		
		IAction run = new Action("Wizard") {
			
			public void run() {
				ImportMappedDataWizard wiz = new ImportMappedDataWizard("/dls/science/groups/das/ExampleData/OpusData/Nexus/MappingNexus/exampleFPA.nxs");
				wiz.setNeedsProgressMonitor(true);
				final WizardDialog wd = new WizardDialog(getSite().getShell(),wiz);
				wd.setPageSize(new Point(900, 500));
				wd.create();
		
		
				wd.open();
			}
			
		};
		
		IAction ftir = new Action("FTIR") {
			
			public void run() {
				MappedDataFile mdf = new MappedDataFile("/dls/science/groups/das/ExampleData/OpusData/Nexus/MappingNexus/exampleFPA.nxs");
				MappedDataBlock b = mdf.addFullDataBlock("/ftir1/absorbance/data", 1, 0);
				mdf.addMap("/map1/map/data",b);
				mdf.addNonMapImage("/microscope1/image/data");
				area = new MappedDataArea();
				area.addMappedDataFile(mdf);
				map.clear();
				spectrum.clear();
				viewer.setInput(area);
				plotMapData();
			}
			
		};
		
		IAction xrf = new Action("XRF") {
			
			public void run() {
				MappedFileDescription b = new MappedFileDescription();
				b.setyAxisName("/entry1/xspress3/sc_MicroFocusSampleY");
				b.setxAxisName("/entry1/xspress3/traj1ContiniousX");
				b.addDataBlock("/entry1/xspress3/AllElementSum", Arrays.asList(new String[]{"/entry1/xspress3/sc_MicroFocusSampleY","/entry1/xspress3/traj1ContiniousX",null}));
				b.addMap("/entry1/xspress3/AllElementSum", "/entry1/xspress3/FF");
				b.addMap("/entry1/xspress3/AllElementSum", "/entry1/xspress3/Ca");
				b.addMap("/entry1/xspress3/AllElementSum", "/entry1/xspress3/Cu");
				b.addMap("/entry1/xspress3/AllElementSum", "/entry1/xspress3/Sr");
				b.addMap("/entry1/xspress3/AllElementSum", "/entry1/xspress3/Zn");
				MappedDataFile mdf = MappedFileFactory.getMappedDataFile("/dls/science/groups/das/ExampleData/mapping/i18/58165_16A_Lr_Calciferous_Gland_His_res_1.nxs", b);
				area = new MappedDataArea();
				area.addMappedDataFile(mdf);
				map.clear();
				spectrum.clear();
				viewer.setInput(area);
				plotMapData();
			}
			
		};
		
		IAction arpes = new Action("ARPES") {
			
			public void run() {
				MappedFileDescription b = new MappedFileDescription();
				b.setyAxisName("/entry1/analyser/sax");
				b.setxAxisName("/entry1/analyser/saz");
				b.addDataBlock("/entry1/analyser/data", Arrays.asList(new String[]{"/entry1/analyser/sax","/entry1/analyser/saz","/entry1/analyser/angles","/entry1/analyser/energies"}));
				b.addMap("/entry1/analyser/data", "/entry1/instrument/analyser/cps");
		
				MappedDataFile mdf = MappedFileFactory.getMappedDataFile("/dls/science/groups/das/ExampleData/mapping/i05/i05-22764.nxs", b);
				area = new MappedDataArea();
				area.addMappedDataFile(mdf);
				viewer.setInput(area);
				map.clear();
				spectrum.clear();
				plotMapData();
			}
			
		};
		
		IAction ncd = new Action("NCD") {
			
			public void run() {
				MappedFileDescription b = new MappedFileDescription();
				b.setyAxisName("/entry1/detector/mfstage_y");
				b.setxAxisName("/entry1/detector/mfstage_x");
				b.addDataBlock("/entry1/detector/data", Arrays.asList(new String[]{"/entry1/detector/mfstage_y","/entry1/detector/mfstage_x",null,null,null}));
				b.addMap("/entry1/detector/data", "/entry1/strain/data");
		
				MappedDataFile mdf = MappedFileFactory.getMappedDataFile("/dls/science/groups/das/ExampleData/mapping/I22MappingData/i22-200594.nxs", b);
				area = new MappedDataArea();
				area.addMappedDataFile(mdf);
				map.clear();
				spectrum.clear();
				viewer.setInput(area);
				plotMapData();
			}
			
		};
		
		
		
		getViewSite().getActionBars().getToolBarManager().add(run);
		getViewSite().getActionBars().getToolBarManager().add(ftir);
		getViewSite().getActionBars().getToolBarManager().add(xrf);
		getViewSite().getActionBars().getToolBarManager().add(arpes);
		getViewSite().getActionBars().getToolBarManager().add(ncd);
		
		
		MappedDataFile mdf = new MappedDataFile("/dls/science/groups/das/ExampleData/OpusData/Nexus/MappingNexus/exampleFPA.nxs");
		MappedDataBlock b = mdf.addFullDataBlock("/ftir1/absorbance/data", 1, 0);
		mdf.addMap("/map1/map/data",b);
		mdf.addNonMapImage("/microscope1/image/data");
		area = new MappedDataArea();
		area.addMappedDataFile(mdf);
		
		area = new MappedDataArea();
		area.addMappedDataFile(mdf);
		
		IWorkbenchPage page = getSite().getPage();
		IViewPart view = page.findView("org.dawnsci.mapping.ui.mapview");
		map = (IPlottingSystem)view.getAdapter(IPlottingSystem.class);
		view = page.findView("org.dawnsci.mapping.ui.spectrumview");
		spectrum = (IPlottingSystem)view.getAdapter(IPlottingSystem.class);
		
		plotMapData();
		map.addClickListener(new IClickListener() {
			
			@Override
			public void doubleClickPerformed(ClickEvent evt) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void clickPerformed(ClickEvent evt) {
				
				for (int i = layers.size()-1; i >=0 ; i--) {
					MapObject l = layers.get(i);
					if (l instanceof MappedData) {
						int[] indices = ((MappedData)l).getIndices(evt.getxValue(), evt.getyValue());
						if (indices != null) {
							try {
								MappingUtils.plotDataWithMetadata(((MappedData)l).getSpectrum(evt.getxValue(), evt.getyValue()), spectrum, new int[]{0});
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				}
			}
		});

		viewer = new TreeViewer(parent);
		viewer.setContentProvider(new MapFileTreeContentProvider());
		viewer.setLabelProvider(new MapFileCellLabelProvider());
		viewer.setInput(area);
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			
			@Override
			public void doubleClick(DoubleClickEvent event) {
				Object e = ((StructuredSelection)event.getSelection()).getFirstElement();
				if (e instanceof MappedData) plotMapData((MappedData)e);
				
			}
		});

		// Add menu and action to treeviewer
		MenuManager menuMgr = new MenuManager();
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		menuMgr.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager manager) {
				if (viewer.getSelection().isEmpty())
					return;
				if (viewer.getSelection() instanceof IStructuredSelection) {
					IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
					Iterator<?> it = selection.iterator();
					List<MappedData> maps = new ArrayList<MappedData>();
					while(it != null && it.hasNext()) {
						Object obj = it.next();
						if (obj instanceof MappedData) {
							maps.add((MappedData)obj);
						}
					}
					manager.add(openRGBDialog(maps));
				}
			}
		});
		menuMgr.setRemoveAllWhenShown(true);
		viewer.getControl().setMenu(menu);
		viewer.getControl().setMenu(menu);

	}

	private IAction openRGBDialog(final List<MappedData> maps) {
		IAction action = new Action("Open RGB Mixer") {
			@Override
			public void run() {
				System.out.println(maps.size());
			}
		};
		return action;
	}

	@Override
	public void setFocus() {
		if (viewer != null && !viewer.getTree().isDisposed()) viewer.getTree().setFocus(); 

	}
	
	private void plotMapData(){
		MappedDataFile dataFile = area.getDataFile(0);
		MappedData map = dataFile.getMap();
		AssociatedImage image = dataFile.getAssociatedImage();
		int count = 0;
		try {
			ICompositeTrace comp = this.map.createCompositeTrace("composite1");
			layers = new ArrayList<MapObject>();
			if (image != null) {
				layers.add(image);
				comp.add(MappingUtils.buildTrace(image.getImage(), this.map),count++);
			}
			layers.add(map);
			comp.add(MappingUtils.buildTrace(map.getMap(), this.map),0);
			this.map.addTrace(comp);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	private void plotMapData(MappedData mapdata){
		map.clear();
		MappedDataFile dataFile = area.getDataFile(0);
		AssociatedImage image = dataFile.getAssociatedImage();
		int count = 0;
		try {
			ICompositeTrace comp = this.map.createCompositeTrace("composite1");
			layers = new ArrayList<MapObject>();
			if (image != null) {
				layers.add(image);
				comp.add(MappingUtils.buildTrace(image.getImage(), this.map),count++);
			}

			layers.add(mapdata);
			comp.add(MappingUtils.buildTrace(mapdata.getMap(), this.map),count++);
			this.map.addTrace(comp);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
