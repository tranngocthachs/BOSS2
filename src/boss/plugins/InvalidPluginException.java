package boss.plugins;

public class InvalidPluginException extends Exception {


	private static final long serialVersionUID = 1L;

	public InvalidPluginException() {
		super();
	}

	public InvalidPluginException(String message) {
		super(message);
	}

	public InvalidPluginException(Throwable cause) {
		super(cause);
	}

	public InvalidPluginException(String message, Throwable cause) {
		super(message, cause);
	}

}
