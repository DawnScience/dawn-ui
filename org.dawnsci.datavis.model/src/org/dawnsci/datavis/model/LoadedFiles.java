package org.dawnsci.datavis.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class LoadedFiles implements SimpleTreeObject, Iterable<LoadedFile> {

	private List<LoadedFile> fileList; 
	
	public LoadedFiles() {
		fileList = new ArrayList<LoadedFile>();
	}
	
	public void addFile(LoadedFile f){
		fileList.add(f);
	}
	
	public void addFiles(List<LoadedFile> f){
		fileList.addAll(f);
	}

	@Override
	public boolean hasChildren() {
		return true;
	}

	@Override
	public Object[] getChildren() {
		return fileList.toArray();
	}

	@Override
	public String getName() {
		return "";
	}
	
	public void deselectOthers(String path) {
		for (LoadedFile file : fileList) if (!path.equals(file.getLongName())) file.setSelected(false);
	}
	
	public LoadedFile getLoadedFile(String path) {
		for (LoadedFile file : fileList) if (path.equals(file.getLongName())) return file;
		return null;
	}
	
	public void unloadFile(LoadedFile file) {
		fileList.remove(file);
	}

	@Override
	public Iterator<LoadedFile> iterator() {
		return fileList.iterator();
	}

	public void unloadAllFiles() {
		fileList.clear();
		
	}
	
}
