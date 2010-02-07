package uk.ac.warwick.dcs.boss.model.testing.executors.impl;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import uk.ac.warwick.dcs.boss.model.testing.ExecutionResult;
import uk.ac.warwick.dcs.boss.model.testing.TestingException;

public class ExecutorProcessRunner {

	public static final int BUFFER_SIZE = 4096;
	public static final int MAX_INPUT_SIZE = 1 * 1024 * 1024;

	private File testResourceDirectory;
	private File submissionDirectory;
	private List<String> whatToRun;
	private int maximumExecutionTime;
	
	public ExecutorProcessRunner(File testResource, File submissionDirectory,
			List<String> whatToRun, int maximumExecutionTime) throws TestingException {
		this.testResourceDirectory = testResource;
		this.submissionDirectory = submissionDirectory;
		this.whatToRun = whatToRun;
		this.maximumExecutionTime = maximumExecutionTime;
	}
	
	public ExecutionResult run() throws TestingException {
		// Build the process
		ProcessBuilder builder = new ProcessBuilder(whatToRun);
		builder.directory(submissionDirectory);
		Map<String, String> environ = builder.environment();
		environ.put("TEST_RESOURCE_DIRECTORY", testResourceDirectory.getAbsolutePath());
		environ.put("SUBMISSION_DIRECTORY", submissionDirectory.getAbsolutePath());
		Logger.getLogger("testing").log(Level.INFO, "Launching process: " + whatToRun);
		
		// Run.    
		Process process = null;
		try {
			process = builder.start();
		} catch (IOException e) {
			throw new TestingException("IO error", e);
		}

		// Now we just need to read and wait.
		ExecutionResult result = new ExecutionResult();
		result.setInterruptedByException(false);

		StringWriter output = new StringWriter();
		StringWriter errors = new StringWriter();

		BufferedInputStream outputBis = new BufferedInputStream(process.getInputStream());
		BufferedInputStream errorsBis = new BufferedInputStream(process.getErrorStream());

		try {
			process.getOutputStream().close();

			long startTime = System.currentTimeMillis();
			while (System.currentTimeMillis() - startTime < maximumExecutionTime * 1000) {

				long sleepTime = 10;
				final long minSleepTime = 10;
				final long maxSleepTime = 100;

				// Read the input, ensuring all data is captured or maximum
				// output size reached
				boolean eof = false;
				boolean quit = false;
				int count = 0;
				while (!eof && !quit) {

					// Let other threads to run while waiting for more to read
					Thread.sleep(10);

					boolean blocking = false;
					try {
						process.exitValue();
						// Process has ended, we can read the rest of the streams
						// with blocking.
						blocking = true;
					}
					catch (IllegalThreadStateException itse) {
						// The process hasn't ended yet
					}

					// Read
					int readStdout = readStream(outputBis, output, blocking);
					int readStderr = readStream(errorsBis, errors, blocking);

					if (readStdout == 0 && readStderr == 0) {
						// Nothing read during this cycle, lets sleep some more
						sleepTime = (long) (sleepTime * 2);
						if (sleepTime > maxSleepTime) {
							sleepTime = maxSleepTime;
						}
					} else {
						// We got something this cycle, lets sleep a bit shorter
						// next time
						sleepTime = (long) (sleepTime / 1.5);
						if (sleepTime < minSleepTime) {
							sleepTime = minSleepTime;
						}
					}

					if (readStdout == -1 && readStderr == -1) {
						eof = true;
					}
					if (output.getBuffer().length() > MAX_INPUT_SIZE) {
						quit = true;
					}
					else if (errors.getBuffer().length() > MAX_INPUT_SIZE) {
						quit = true;
					}

					count++;
				}

				// wait for the process to finish
				result.setExitCode(process.waitFor());
				result.setFinished(true);
			}

			try {
				// Let's close the streams
				process.getInputStream().close();
				process.getOutputStream().close();
				process.getErrorStream().close();
			}
			catch (IOException ioe) {
				// TODO: notify parent?
				// - 19 July 2005 HJ
			}

		} catch (IOException e) {
			result.setInterruptedByException(true);
			result.setFinished(false);
			result.setExitCode(-1);
			process.destroy();
		} catch (InterruptedException ie) {
			// The process has timed out
			result.setExitCode(-1);
			result.setFinished(false);
			process.destroy();
			try {
				result.setExitCode(process.exitValue());
			}
			catch (IllegalThreadStateException itse) {
				// only thrown when process has not yet terminated, but
				// previous lines destroyed it.
			}
		}

		result.setOutput(output.toString());
		result.setErrors(errors.toString());
		
		return result;
	}
	
	/**
	 * Reads available data from the stream and adds it as a byte arrays
	 * in the vector
	 *
	 * @param bStream the stream to read from
	 * @param outputStr the vector to add the values. Notice that the
	 *                  function modifies this parameter!
	 * @param blocking allow this function to block, if true
	 * @return -1 if the end of file was reached, otherwise the number
	 *         of bytes read
	 * @throws IOException if something goes wrong
	 */
	static private int readStream(BufferedInputStream bStream,
			StringWriter outStream,
			boolean blocking)
	throws IOException {

		int bytesReadTotal = 0;

		// True, while reading
		boolean reading = true;

		// Have we reached end of file?
		boolean eof = false;

		// Number of bytes available to read, or how many to read
		int len;

		// Temporary buffer
		byte[] buf = new byte[BUFFER_SIZE];

		// Number of times the stream has blocked
		int blocked = 0;

		while (reading) {

			if (blocking) {
				len = buf.length;
			}
			else {
				// Number of bytes available without blocking
				len = bStream.available();
				len = len > buf.length ? buf.length : len;
			}

			if (len != 0) {
				// Reset would block counter
				blocked = 0;

				// Read some data into the buffer
				int bytesRead = bStream.read(buf, 0, len);

				// End of stream reached?
				if (bytesRead == -1) {
					reading = false;
					eof = true;
				}
				if (bytesRead > 0) {
					bytesReadTotal += bytesRead;
					// Shove all this into outStream
					//outStream.write(buf, 0, buf.length);
					outStream.write(new String(buf, 0, bytesRead));
				}
			}
			else {
				// Increment the number of times this stream has blocked
				blocked++;

				// 2 would-blocks and you're out till next round
				if (blocked == 2) {
					reading = false;
				}
			}
		}

		if (eof) {
			return -1;
		}

		return bytesReadTotal;
	}
	
}
