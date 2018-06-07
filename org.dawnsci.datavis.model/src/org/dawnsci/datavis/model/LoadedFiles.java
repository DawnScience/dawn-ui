package org.dawnsci.datavis.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

public class LoadedFiles implements IDataObject, Iterable<LoadedFile> {

	private final List<LoadedFile> fileList;
	private Comparator<LoadedFile> comparator;

	public LoadedFiles() {
		fileList = new CopyOnWriteArrayList<>();
	}

	public void addFile(LoadedFile f) {
		fileList.add(f);
	}

	public void addFiles(List<LoadedFile> f) {
		fileList.addAll(f);
	}

	public List<LoadedFile> getLoadedFiles() {
		List<LoadedFile> arrayList = new ArrayList<>(fileList);
		if (comparator != null) {
			arrayList.sort(comparator);
		}
		return arrayList;
	}

	public boolean contains(String path) {
		Optional<LoadedFile> findAny = fileList.stream()
				.filter(f -> path.equals(f.getFilePath())).findAny();
		return findAny.isPresent();
	}

	public Object[] getChildren() {
		return fileList.toArray();
	}

	@Override
	public String getName() {
		return "";
	}

	public void deselectOthers(String path) {
		for (LoadedFile file : fileList) {
			if (!path.equals(file.getFilePath())) {
				file.setSelected(false);
			}
		}
	}

	public LoadedFile getLoadedFile(String path) {
		for (LoadedFile file : fileList) {
			if (path.equals(file.getFilePath())) {
				return file;
			}
		}
		return null;
	}

	public void unloadFile(LoadedFile file) {
		fileList.remove(file);
	}

	public void setComparator(Comparator<LoadedFile> comparator) {
		this.comparator = comparator;
	}

	@Override
	public Iterator<LoadedFile> iterator() {
		return getLoadedFiles().iterator();
	}

	public void unloadAllFiles() {
		fileList.clear();
	}

	public void moveBefore(List<LoadedFile> files, LoadedFile marker) {

		if (files.contains(marker)) {
			return;
		}

		fileList.removeAll(files);

		if (marker == null) {
			fileList.addAll(files);
			return;
		}

		for (int i = 0; i < fileList.size(); i++) {
			if (fileList.get(i) == marker) {
				fileList.addAll(i, files);
				return;
			}
		}
	}
}
