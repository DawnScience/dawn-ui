package org.dawnsci.datavis.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public class LoadedFiles implements IDataObject, Iterable<LoadedFile> {

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
	
	public List<LoadedFile> getLoadedFiles() {
		return new ArrayList<LoadedFile>(fileList);
	}
	
	public boolean contains(String path) {
		Optional<LoadedFile> findAny = fileList.stream().filter(f -> path.equals(f.getFilePath())).findAny();
		return findAny.isPresent();
	}
	
	public Object[] getChildren(){
		return fileList.toArray();
	}

	@Override
	public String getName() {
		return "";
	}
	
	public void deselectOthers(String path) {
		for (LoadedFile file : fileList) if (!path.equals(file.getFilePath())) file.setSelected(false);
	}
	
	public LoadedFile getLoadedFile(String path) {
		for (LoadedFile file : fileList) if (path.equals(file.getFilePath())) return file;
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
