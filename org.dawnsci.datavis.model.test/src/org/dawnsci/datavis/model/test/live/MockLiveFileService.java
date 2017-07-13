package org.dawnsci.datavis.model.test.live;

import java.util.HashSet;
import java.util.Set;

import javax.swing.plaf.synth.SynthStyleFactory;

import org.dawnsci.datavis.model.ILiveFileListener;
import org.dawnsci.datavis.model.ILiveFileService;

public class MockLiveFileService implements ILiveFileService {

	private Set<ILiveFileListener> listeners = new HashSet<>();
	
	@Override
	public void addLiveFileListener(ILiveFileListener l) {
		listeners.add(l);
	}

	@Override
	public void removeLiveFileListener(ILiveFileListener l) {
		listeners.remove(l);
	}

	@Override
	public void runUpdate(Runnable runnable) {
		runnable.run();
		
	}
	
	public Set<ILiveFileListener> getListeners(){
		return listeners;
	}

}
