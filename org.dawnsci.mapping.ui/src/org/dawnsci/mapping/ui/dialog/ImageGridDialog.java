package org.dawnsci.mapping.ui.dialog;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.dawnsci.mapping.ui.AcquisitionServiceManager;
import org.dawnsci.mapping.ui.datamodel.AbstractMapData;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.dawnsci.plotting.api.trace.MetadataPlotUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.scanning.api.event.EventConstants;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.bean.BeanEvent;
import org.eclipse.scanning.api.event.bean.IBeanListener;
import org.eclipse.scanning.api.event.core.IPropertyFilter.FilterAction;
import org.eclipse.scanning.api.event.core.ISubscriber;
import org.eclipse.scanning.api.event.scan.IScanListener;
import org.eclipse.scanning.api.event.scan.ScanEvent;
import org.eclipse.scanning.api.event.status.StatusBean;
import org.eclipse.scanning.api.ui.CommandConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImageGridDialog extends Dialog{
	
	private List<IDataset> data;
	private List<IPlottingSystem<Composite>> systems = new ArrayList<IPlottingSystem<Composite>>();
	private static final int MIN_REFRESH_TIME = 5000;
	private MultiPlotJob job;
	private List<MapAndPlot> mapsWithPlots;
	private ISubscriber<EventListener> scanSubscriber;
	private ISubscriber<EventListener> beanSubscriber;
	private IScanListener scanListener;
	private IBeanListener<?> beanListener;
	
	private static Logger logger = LoggerFactory.getLogger(ImageGridDialog.class);

	public ImageGridDialog(Shell parentShell, List<AbstractMapData> maps) {
		super(parentShell);

		data = new ArrayList<IDataset>(maps.size());
		mapsWithPlots = new ArrayList<MapAndPlot>();
		for (AbstractMapData map : maps) {
			data.add(map.getMap());
			try {
				IPlottingSystem<Composite> plot = PlottingFactory.createPlottingSystem(Composite.class);
				if (map.isLive()) {
					mapsWithPlots.add(new MapAndPlot(map, plot));
				}
				systems.add(plot);
			} catch (Exception e) {
				logger.error("Error creating Image Grid plotting systems:", e);
			}

		}
		
		if (data.isEmpty()) return;

	}


	@Override
	public Control createDialogArea(Composite parent)  {
		Composite container = (Composite) super.createDialogArea(parent);
		Display display = Display.getDefault();
		Color white = new Color(display, 255, 255, 255);
		container.setLayout(new GridLayout(1, false));
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		container.setBackground(white);

		Composite plotsComp = new Composite(container, SWT.NONE);
		plotsComp.setLayout(new GridLayout(3, false));
		plotsComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		plotsComp.setBackground(white);
		try {
			int i = 0;
			for (IPlottingSystem<Composite> system : systems) {
				system.createPlotPart(plotsComp, "Plot " + i, null, PlotType.IMAGE, null);
				system.getPlotComposite().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
//				system.updatePlot2D(data.get(i), null, null);
				MetadataPlotUtils.plotDataWithMetadata(data.get(i), system);
				i++;
			}
		} catch (Exception e) {
			logger.error("Error plotting data:", e);
		}
		
		if (!mapsWithPlots.isEmpty()) {
			job = new MultiPlotJob("update multi-plots", true);
			job.setRunnable(() -> {
				mapsWithPlots.stream().forEach(MapAndPlot::update);
			});
			subscribeToOperationStatusTopic();
			subscribeToScanTopic();
		}
		
		return container;
	}
	
	@Override
	  protected boolean isResizable() {
	    return true;
	  }
	
	@Override
	protected Point getInitialSize() {
		Rectangle bounds = PlatformUI.getWorkbench().getWorkbenchWindows()[0].getShell().getBounds();
		return new Point((int)(bounds.width*0.8),(int)(bounds.height*0.8));
	}
	
	private void subscribeToOperationStatusTopic(){
		
		final String suri = CommandConstants.getScanningBrokerUri();
		if (suri==null) return; // Nothing to start, standard DAWN.


		// Check the service is available this should always be true!
		if (AcquisitionServiceManager.getEventService() == null) {
			return;
		}

		try {
			final URI uri = new URI(suri);
			beanSubscriber = AcquisitionServiceManager.getEventService().createSubscriber(uri, "scisoft.operation.STATUS_TOPIC");
			
			beanListener = new IBeanListener<StatusBean>() {
				@Override
				public void beanChangePerformed(BeanEvent<StatusBean> evt) {
					if (job != null) job.schedule();
				}
			};
			
			
			beanSubscriber.addListener(beanListener);
			
			
		} catch (URISyntaxException | EventException e) {
			
		}
		
	}
	
	private void subscribeToScanTopic(){
		
		final String suri = CommandConstants.getScanningBrokerUri();
		if (suri==null) return; // Nothing to start, standard DAWN.

		try {
			final URI uri = new URI(suri);
			scanSubscriber= AcquisitionServiceManager.getEventService().createSubscriber(uri, EventConstants.STATUS_TOPIC);

			
			
			scanSubscriber.addProperty("scanRequest", FilterAction.DELETE); 
			scanSubscriber.addProperty("position", FilterAction.DELETE); 		       
			
			scanListener = new IScanListener() {
				@Override
				public void scanEventPerformed(ScanEvent evt) {
					if (job != null) job.schedule();
				}
				
				@Override
				public void scanStateChanged(ScanEvent evt) {
					if (job != null) job.schedule();
				}
			};
			
			scanSubscriber.addListener(scanListener);


		} catch (URISyntaxException | EventException e) {

		}

	}
	
	private void unsubscribe(){
		
		if (scanListener == null && beanListener == null) return; 

		if (scanListener != null && scanSubscriber != null) {
			try {

				scanSubscriber.removeListener(scanListener);
				logger.debug("scan listener removed");
				
			} catch (Exception e) {
				logger.error("Could not remove listener",e);
			}
		}
		
		if (beanListener != null && beanSubscriber != null) {
			try {
				beanSubscriber.removeListener(beanListener);
				logger.debug("bean listener removed");
				
			} catch (Exception e) {
				logger.error("Could not remove listener",e);
			}
		}
		
	}
	
	@Override
	public boolean close() {
		unsubscribe();
		if (job != null) job.cancel();
		job = null;
		for (IPlottingSystem<Composite> system : systems) if (system != null && !system.isDisposed()) system.dispose();
		return super.close();
	}
	
	private class MapAndPlot {
		
		private AbstractMapData map;
		private IPlottingSystem<?> plot;

		public MapAndPlot(AbstractMapData map, IPlottingSystem<?> plot) {
			this.map = map;
			this.plot = plot;
		}
		
		public void update() {
			map.update();
			IDataset m = map.getMap();
			if (m == null) return;
			if (m.getSize() == 1) return;
			m.setName(map.toString());
			MetadataPlotUtils.plotDataWithMetadata(m, plot);
		}
		
	}
	
	
	
	private class MultiPlotJob extends Job {

		private final AtomicReference<Runnable> task =new AtomicReference<Runnable>();
		private boolean delayed = false;
		
		public MultiPlotJob(String name, boolean delayed) {
			super(name);
			this.delayed = delayed;
		}
		
		public void setRunnable(Runnable runnable) {
			this.task.set(runnable);
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			Runnable local = task.get();
			if (local == null) return Status.OK_STATUS;
			local.run();
			
			if (delayed) {
				try {
					Thread.sleep(MIN_REFRESH_TIME);
				} catch (InterruptedException e) {
					return Status.OK_STATUS;
				}
			}
			
			return Status.OK_STATUS;
		}
		
	}
}
