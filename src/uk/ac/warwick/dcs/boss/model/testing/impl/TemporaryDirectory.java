package uk.ac.warwick.dcs.boss.model.testing.impl;

import java.io.File;
import java.io.IOException;

// Snarfed from http://forum.java.sun.com/thread.jspa?threadID=470197&messageID=2169110
public class TemporaryDirectory
{
    private static TemporaryDirectoryDeleter deleterThread;
    
    static
    {
        deleterThread = new TemporaryDirectoryDeleter();
        Runtime.getRuntime().addShutdownHook(deleterThread);
    }
        
    /**
     * Creates a temp directory with a generated name (given a certain prefix) in a given directory.
     * The directory (and all its content) will be destroyed on exit.
     */    
    public static File createTempDir(String prefix, File directory)
    throws IOException
    {
        File tempFile = File.createTempFile(prefix, "", directory);
        deleterThread.add(tempFile);
        if (!tempFile.delete())
            throw new IOException("Could not delete temporary file");
        if (!tempFile.mkdir())
            throw new IOException("Could not make temporary directory");
        return tempFile;        
    }
    
	public static void deleteDirectory(File dir)
	{
		File[] fileArray = dir.listFiles();

		if (fileArray != null)
		{
			for (int i = 0; i < fileArray.length; i++)
			{
				if (fileArray[i].isDirectory())
					deleteDirectory(fileArray[i]);
				else
					fileArray[i].delete();
			}
		}
		dir.delete();
	}

}