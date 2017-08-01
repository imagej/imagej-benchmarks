
package net.imagej;

import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.TearDown;
import org.scijava.Context;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;

/**
 * TODO Documentation
 * 
 * @author Stefan Helfrich (University of Konstanz)
 */
public abstract class AbstractBenchmark {

	@Parameter
	protected Context context;

	@Parameter
	protected LogService logService;

	/** Subclasses can override to create a context with different services. */
	protected Context createContext() {
		return new Context(LogService.class);
	}

	/**
	 * Sets up a SciJava context with {@link LogService}.
	 */
	@Setup
	public void setUp() {
		createContext().inject(this);
	}

	/**
	 * Disposes of the {@link LogService} that was initialized in
	 * {@link #setUp()}.
	 */
	@TearDown
	public synchronized void cleanUp() {
		if (context != null) {
			context.dispose();
			context = null;
			logService = null;
		}
	}

}
