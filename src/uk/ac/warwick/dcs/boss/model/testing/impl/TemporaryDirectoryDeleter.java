package uk.ac.warwick.dcs.boss.model.testing.impl;

import java.io.File;
import java.util.LinkedList;

public class TemporaryDirectoryDeleter extends Thread {
	private LinkedList<File> dirList = new LinkedList<File>();

	public synchronized void add(File dir)
	{
		dirList.add(dir);
	}

	public void run()
	{
		synchronized (this)
		{
			for (File dir : dirList) {
				TemporaryDirectory.deleteDirectory(dir);
			}
		}
	}
}
