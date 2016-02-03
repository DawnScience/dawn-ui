package org.dawnsci.plotting.histogram.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class MatplotLibDataReader {

	public static void main(String[] args) {
		System.out.println("Enter Path to file: ");
		String path = "";
		try {
			BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
			path = bufferRead.readLine();

			System.out.println(path);

			URI uri = null;
			uri = new URI(path);
			File file = new File(uri.getPath());
			List<String> lines = Files.readAllLines(file.toPath(), Charset.defaultCharset());
			List<String> reds = new ArrayList<String>();
			List<String> greens = new ArrayList<String>();
			List<String> blues = new ArrayList<String>();
			boolean isRed = false, isGreen = false, isBlue = false;
			for (String line : lines) {
				if (line.contains("red")) {
					reds = fillList(reds, line);
					isRed = true; isGreen = false; isBlue = false;
					continue;
				}
				if (line.contains("green")) {
					greens = fillList(greens, line);
					isRed = false; isGreen = true; isBlue = false;
					continue;
				}
				if (line.contains("blue")) {
					blues = fillList(blues, line);
					isRed = false; isGreen = false; isBlue = true;
					continue;
				}
				if (isRed) {
					fillList(reds, line);
				} else if (isGreen) {
					fillList(greens, line);
				} else if (isBlue) {
					fillList(blues, line);
				}
			}
			int size = reds.size() / 3;
			size = size * 4;
			if (reds.size() == blues.size() && reds.size() == greens.size())
				for (int i = 2; i < blues.size(); i = i + 2) {
					reds.remove(i);
					greens.remove(i);
					blues.remove(i);
				}
			else
				return;
			List<String> finalList = new ArrayList<String>(size);
			for (int i = 0; i < reds.size(); i = i + 2) {
				if (!reds.get(i).equals(blues.get(i)) || !reds.get(i).equals(greens.get(i)) || !greens.get(i).equals(blues.get(i))) {
					System.err.println();
				}
				finalList.add(reds.get(i));
				finalList.add(reds.get(i+1));
				finalList.add(greens.get(i+1));
				finalList.add(blues.get(i+1));
				
			}
			System.out.println("Result formatted data:");
			for (int i = 0; i < finalList.size(); i = i + 4) {
				System.out.println("{" + finalList.get(i) + ", " + finalList.get(i+1) + ", " + finalList.get(i + 2) + ", " + finalList.get(i + 3) +"},");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}

	private static List<String> fillList(List<String> list, String line) {
		String[] str=line.trim().split(":");String line1 = "";
		if (str.length > 1)
			line1 = str[1];
		else
			line1 = line.trim();
			
		String[] lineArray1 = line1.split(",");
		for (int i = 0; i < lineArray1.length; i++) {
			String tmp = lineArray1[i].replace("(", "");
			tmp = tmp.replace(")", "");
			list.add(tmp);
		}
		return list;
	}

}
