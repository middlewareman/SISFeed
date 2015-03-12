package com.middlewareman.sisfeed.test;

import java.io.File;
import java.io.Serializable;

/**
 * Java bean for coupling a file with a timestamp and sorting in ascending order
 * on timestamp. Used only in testing for ordering test data and to simulate
 * handling of message timestamps.
 * 
 * @author Andreas Nyberg
 */
public class FileTime implements Comparable<FileTime>, Serializable {

	private static final long serialVersionUID = 4988831217628581665L;

	/**
	 * Sort an array in order of ascending timestamp.
	 * 
	 * @param fileTimes
	 */
	public static void sort(FileTime[] fileTimes) {
		java.util.Arrays.sort(fileTimes);
	}

	private File file;

	private long timestamp;

	public FileTime(File file) {
		this.file = file;
		this.timestamp = file.lastModified();
	}

	public FileTime(File file, long timestamp) {
		this.file = file;
		this.timestamp = timestamp;
	}

	public File getFile() {
		return file;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public int compareTo(FileTime other) {
		return Long.signum(timestamp - other.timestamp);
	}

}
