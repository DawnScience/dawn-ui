/*
 * Copyright (c) 2021 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.fileviewer.table;

import java.io.File;
import java.io.FileFilter;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.january.dataset.Slice;

/**
 * File filter that parses for file names containing scan number
 */
public class ScanNumberFileFilter implements FileFilter {
	
	private String prefix;
	private int start = 0;
	private int stop = Integer.MAX_VALUE;
	private int step = 1;
	private Pattern embeddedNumber;

	private final static String EMBEDDED_NUMBER_FORMAT = "^[^0-9]*([0-9]{%d,}).*";

	/**
	 * @param prefix prefix of name (can be null)
	 * @param scan expressed in Python slicing. E.g. ":10:2". A single number is treated as a start value
	 */
	public ScanNumberFileFilter(String prefix, String scan) {
		this.prefix = prefix;

		Slice[] slices = Slice.convertFromString(scan);
		if (slices.length != 1) {
			throw new IllegalArgumentException("Only a single slicing allowed");
		}
		Slice s = slices[0];
		if (!scan.contains(":")) { // single number case
			s.setStop(null);
		}
		step = s.getStep();
		if (step <= 0) {
			throw new IllegalArgumentException("Step must be positive");
		}

		if (s.getStart() != null) {
			start = s.getStart();
		}
		if (s.getStop() != null) {
			stop = s.getStop();
		}

		int digits = BigDecimal.valueOf(start).precision();
		embeddedNumber = Pattern.compile(String.format(EMBEDDED_NUMBER_FORMAT, digits));
	}

	private boolean inRange(int n) {
		if (n >= start && n < stop) {
			if (step == 1) {
				return true;
			}
			int r = (n - start) % step;
			return r == 0;
		}
		return false;
	}

	@Override
	public boolean accept(File file) {
		String name = file.getName();
		if (prefix != null) {
			if (!name.startsWith(prefix)) {
				return false;
			}
			name = name.substring(prefix.length());
		}

		Matcher m = embeddedNumber.matcher(name);
		if (m.matches() && m.groupCount() == 1) {
			int n = Integer.parseInt(m.group(1));
			return inRange(n);
		}

		return false;
	}

	public static void main(String[] args) {
		System.out.println(Arrays.toString(Slice.convertFromString("1234")) + " cf " + new Slice(1234, null)  + " and " + new Slice(1234));

		Pattern p = Pattern.compile(String.format(EMBEDDED_NUMBER_FORMAT, BigDecimal.valueOf(1234).precision()));
		Matcher m = p.matcher("i21-1234");
		System.out.println(m + " matches " + m.matches());

		ScanNumberFileFilter filter;

		filter = new ScanNumberFileFilter(null, "123");
		System.out.println("1234 should be true: " + filter.accept(new File("asd1234")));
		System.out.println("1235 should be true: " + filter.accept(new File("asd1235")));
		System.out.println("1237 should be true: " + filter.accept(new File("asd1237")));

		filter = new ScanNumberFileFilter(null, "1235");
		System.out.println("1234 should be false: " + filter.accept(new File("asd1234")));
		System.out.println("1235 should be true: " + filter.accept(new File("asd1235")));
		System.out.println("1237 should be true: " + filter.accept(new File("asd1237")));

		filter = new ScanNumberFileFilter(null, "1300");
		System.out.println("1234 should be false: " + filter.accept(new File("asd1234")));
		System.out.println("1235 should be false: " + filter.accept(new File("asd1235")));
		System.out.println("1237 should be false: " + filter.accept(new File("asd1237")));

		filter = new ScanNumberFileFilter(null, "1235::1");
		System.out.println("1234 should be false: " + filter.accept(new File("asd1234")));
		System.out.println("1235 should be true: " + filter.accept(new File("asd1235")));
		System.out.println("1237 should be true: " + filter.accept(new File("asd1237")));


		filter = new ScanNumberFileFilter(null, "1234::3");
		System.out.println("1234 should be true: " + filter.accept(new File("asd1234")));
		System.out.println("1235 should be false: " + filter.accept(new File("asd1235")));
		System.out.println("1237 should be true: " + filter.accept(new File("asd1237")));

		filter = new ScanNumberFileFilter(null, "1234:1239:3");
		System.out.println("1234 should be true: " + filter.accept(new File("asd1234")));
		System.out.println("1235 should be false: " + filter.accept(new File("asd1235")));
		System.out.println("1237 should be true: " + filter.accept(new File("asd1237")));
		System.out.println("1238 should be false: " + filter.accept(new File("asd1238")));
		System.out.println("1240 should be false: " + filter.accept(new File("asd1240")));

		System.out.println("1234 should be true: " + filter.accept(new File("asd1-1234")));
		System.out.println("1235 should be false: " + filter.accept(new File("asd1-1235")));
		System.out.println("1237 should be true: " + filter.accept(new File("asd1-1237")));

		filter = new ScanNumberFileFilter("asd1-", "1234::3");
		System.out.println("1234 should be true: " + filter.accept(new File("asd1-1234")));
		System.out.println("1235 should be false: " + filter.accept(new File("asd1-1235")));
		System.out.println("1237 should be true: " + filter.accept(new File("asd1-1237")));

		filter = new ScanNumberFileFilter("asd1-", "1234:1239:3");
		System.out.println("1234 should be true: " + filter.accept(new File("asd1-1234")));
		System.out.println("1235 should be false: " + filter.accept(new File("asd1-1235")));
		System.out.println("1237 should be true: " + filter.accept(new File("asd1-1237")));
		System.out.println("1238 should be false: " + filter.accept(new File("asd1-1238")));
		System.out.println("1240 should be false: " + filter.accept(new File("asd1-1240")));
	}
}
