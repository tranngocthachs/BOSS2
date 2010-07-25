package uk.ac.warwick.dcs.boss.plugins;

public class PluginNotConfigurableException extends Exception {


	private static final long serialVersionUID = 1L;

	public PluginNotConfigurableException() {
		super();
	}

	public PluginNotConfigurableException(String message) {
		super(message);
	}

	public PluginNotConfigurableException(Throwable cause) {
		super(cause);
	}

	public PluginNotConfigurableException(String message, Throwable cause) {
		super(message, cause);
	}

}
