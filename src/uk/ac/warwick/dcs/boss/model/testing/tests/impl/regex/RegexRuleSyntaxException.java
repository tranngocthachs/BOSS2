package uk.ac.warwick.dcs.boss.model.testing.tests.impl.regex;

public class RegexRuleSyntaxException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public RegexRuleSyntaxException(String message) {
		super(message);
	}

	public RegexRuleSyntaxException(String message, Throwable cause) {
		super(message, cause);
	}
}