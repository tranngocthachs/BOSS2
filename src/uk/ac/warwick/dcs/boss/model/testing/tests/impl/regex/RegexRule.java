package uk.ac.warwick.dcs.boss.model.testing.tests.impl.regex;

import java.util.Vector;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Information about a regex rule
 *
 * Implements the following oracle features:
 * (http://www.cs.nott.ac.uk/~ceilidh/papers/Oracle.html [11 Aug 2005])
 *
 * 5:RE	5 marks awarded if RE found.
 *            The default is 10 marks.
 * 15:>5:RE	15 marks awarded if > 5 occurrences found,
 *	        zero marks if <= 5.
 * 10:>=5:RE  10 marks awarded if >= 5 occurrences found,
 *            zero marks if < 5.
 * <5:RE      Default marks awarded if < 5 occurrences found,
 *            zero marks if >= 5.
 * <=5:RE	Default marks awarded if <= 5 occurrences found,
 *            zero marks if > 5.
 * ==1:RE	Default marks awarded if exactly 1 occurrence found.
 * !=1:RE	Default marks awarded if more or less than 1 occurrence.
 * >=4-10:RE	Default marks if >=10, zero marks if <=4,
 *    	interpolated marks if between 4 and 10.
 * ~10:RE	If the RE is found, 10 marks are taken away.
 *    	The 10 marks are not included in the maximum total
 *    	out of which the mark gained is scaled as a percentage.
 *    	Marks will never go negative.
 *
 * The following Ceilidh oracle features are not implemented:
 *
 * +:RE	This RE must occur AFTER the previous RE.
 * -:RE	The previous RE must NOT have been found before this one.
 *
 * The following features are BOSS extensions that are not available
 * with ceilidh.
 *
 * ==4-10:RE    Default marks awarded if 4 to 10 occurances found.
 * !=4-10:RE    Default marks awarded if less than 4 or more than 10
 *              occurances found.
 *
 * Piecewise scaling can be applied to the overall score by a line
 * beginning with a colon.
 * Example:
 *
 * :0,0;50,20;90,60;100,100
 *
 * 0 will be scaled to 0
 * 50 will be scaled to 20
 * 90 will be scaled to 60
 * 100 will be scaled to 100
 *
 * And scores in between will be linearily interpolated in between.
 *
 * As a Boss extension, comments can be added by lines starting with
 * a hash mark (#).
 */
class RegexRule {
	/**
	 * The rule (for toString)
	 */
	private String rule;
	
	/**
	 * The regex string to match against
	 */
	private String patternString;

	/**
	 * The regex matcher compiled from patternString
	 */
	private Pattern pattern;

	/**
	 * The score associated with this rule.
	 * Negative score is the "~" in oracle syntax.
	 * Negative scores are not to be included in the maximum total score.
	 */
	private int score;

	/**
	 * Minimum number of times the item has to occur in order to be scored
	 */
	private int rangeStart;

	/**
	 * Maximum number of times the item has to occur in order to be scored
	 */
	private int rangeEnd;

	/**
	 * The operator used for rangeStart and rangeEnd.
	 */
	private int operator;

	/**
	 * If this is true, the regex has to match the whole line.
	 */
	private boolean matchWholeLine = false;

	// LESS and GREATER are converted into LESS_OR_EQUAL and GREATER_OR_EQUAL
	// respectively in scoring

	public static final int UNKNOWN_OPERATOR = 0;

	public static final int LESS = 1;
	public static final int LESS_OR_EQUAL = 2;
	public static final int GREATER = 3;
	public static final int GREATER_OR_EQUAL = 4;

	public static final int EQUAL = 5;
	public static final int NOT_EQUAL = 6;

	/**
	 * This regular expression must be the next match after the previous
	 * regular expression's match in order to score.
	 *
	 * This information is checked by the user of this object if implemented.
	 */
	public static final int AFTER = 7;

	/**
	 * This regular expression may not be the next match after the previous
	 * regular expression's match in order to score.
	 *
	 * This information is checked by the user of this object if implemented.
	 */
	public static final int NOT_AFTER = 8;

	public static final int DEFAULT_OPERATOR = GREATER_OR_EQUAL;

	public static final int DEFAULT_SCORE = 10;
	public static final int DEFAULT_RANGE_START = 1;
	public static final int DEFAULT_RANGE_END = 1;

	/**
	 * Generate a RegexRule from a string representation
	 *
	 * @param rule String representing the rule in format of oracle
	 * @throws RegexRuleSyntaxException in case of syntax error
	 */
	public static RegexRule parseRule(String rule)
	throws RegexRuleSyntaxException {
		return new RegexRule(rule);
	}

	/**
	 * Set whether to match whole lines
	 *
	 * When set to true, regex has to match the whole line. When false,
	 * it will suffice if the regex matches part of the line.
	 * @param setting true, to match whole lines only
	 */
	public void setMatchWholeLine(boolean setting) {
		matchWholeLine = setting;
	}

	/**
	 * Return the operator
	 * @return operator (EQUAL, GREATER, etc)
	 */
	public int getOperator() {
		return operator;
	}

	/**
	 * Return the score of this rule
	 *
	 * Returns the score that can be score from this rule. Negative scores
	 * won't be taken into the total maximum score from a ruleset.
	 * @return the score
	 */
	public int getScore() {
		return score;
	}

	/**
	 * Counts the score
	 *
	 * The score will be counted according to the rule's operators
	 * and the parameter "occurances".
	 * @param occurances The number of times the rule matched
	 */
	public int countScore(int occurances) {
		// GREATER and LESS are converted into GREATER_OR_EQUAL and LESS_OR_EQUAL.
		// minOccurs and maxOccurs are rangeStart and rangeEnd fixed into those
		// operators. This is made to simplify the code.
		int minOccurs = rangeStart;
		int maxOccurs = rangeEnd;

		int fixedOperator = operator;

		if (fixedOperator == LESS) {
			operator = LESS_OR_EQUAL;
			minOccurs = minOccurs - 1;
			maxOccurs = maxOccurs - 1;
		}
		if (fixedOperator == GREATER) {
			operator = GREATER_OR_EQUAL;
			minOccurs = minOccurs + 1;
			maxOccurs = maxOccurs + 1;
		}

		if (fixedOperator == GREATER_OR_EQUAL) {
			if (occurances >= maxOccurs) {
				// Clearly good
				return score;
			}
			else if (occurances >= minOccurs /*&& occurances < maxOccurs*/) {
				// Within range of interpolated score
				return (occurances - minOccurs) * score / (maxOccurs - minOccurs);
			}
			else {
				// Not within range
				return 0;
			}
		}
		else if (fixedOperator == LESS_OR_EQUAL) {
			if (occurances <= minOccurs) {
				// Clearly good
				return score;
			}
			else if (occurances <= maxOccurs /*&& occurances > minOccurs*/) {
				// Within range of interpolated score
				return (occurances - minOccurs) * score / (maxOccurs - minOccurs);
			}
		}
		else if (fixedOperator == EQUAL) {
			if (occurances >= minOccurs && occurances <= maxOccurs) {
				return score;
			}
		}
		else if (fixedOperator == NOT_EQUAL) {
			if (occurances < minOccurs || occurances > maxOccurs) {
				return score;
			}
		}
		else {
			// Unknown operator
			return 0;
		}

		return 0;
	}

	/**
	 * Count the total score from occurance count list
	 *
	 * @param rules Vector of RegexRule objects
	 * @param occurances list of occurances for each rule in rules
	 * @return the total score in range of 0 to 100
	 */
	public static int countScore(Vector<RegexRule> rules, int[] occurances) {
		int totalScore = 0;

		if (rules.size() != occurances.length) {
			throw new IndexOutOfBoundsException("countTotalScore parameters don't " +
			"match");
		}

		for (int ruleNum = 0; ruleNum < rules.size(); ruleNum++) {
			RegexRule rule = (RegexRule)rules.get(ruleNum);
			// Score from this rule
			int subScore = rule.countScore(occurances[ruleNum]);
			totalScore = totalScore + subScore;
		}

		return totalScore;
	}

	/**
	 * Count the max score from occurance count list
	 *
	 * @param rules Vector of RegexRule objects
	 * @param occurances list of occurances for each rule in rules
	 * @return the total score in range of 0 to 100
	 */
	public static int countMaxScore(Vector<RegexRule> rules, int[] occurances) {
		int maxScore = 0;

		if (rules.size() != occurances.length) {
			throw new IndexOutOfBoundsException("countTotalScore parameters don't " +
			"match");
		}

		for (int ruleNum = 0; ruleNum < rules.size(); ruleNum++) {
			RegexRule rule = (RegexRule)rules.get(ruleNum);
			if (rule.getScore() > 0) {
				maxScore = maxScore + rule.getScore();
			}
		}

		return maxScore;
	}

	
	/**
	 * Count the total score from occurance count list
	 *
	 * @param rules Vector of RegexRule objects
	 * @param occurances list of occurances for each rule in rules
	 * @return feedback string
	 */
	public static String feedbackString(Vector<RegexRule> rules, int[] occurances) {
		int cumulativeScore = 0;
		int cumulativeMaxScore = 0;

		StringBuffer feedback = new StringBuffer();
		feedback.append("test min max cnt mrk oof cum oof lost RE\n");
		for (int i = 0; i < rules.size(); i++) {
			RegexRule rule = (RegexRule) rules.get(i);

			// Max score from this rule
			int thisMaxScore = rule.getScore();
			int thisScore = rule.countScore(occurances[i]);
			if (thisMaxScore > 0) {
				cumulativeMaxScore = cumulativeMaxScore + thisMaxScore;
			}
			cumulativeScore = cumulativeScore + thisScore;

			int lostScore = 0;
			if (thisMaxScore >= 0) {
				lostScore = thisMaxScore - thisScore;
			}
			else if (thisScore < 0) {
				lostScore = 0 - thisScore;
			}

			// test number
			addNum(feedback, 4, i + 1);
			feedback.append(" ");

			// min
			addNum(feedback, 3, rule.rangeStart);
			feedback.append(" ");
			// max
			addNum(feedback, 3, rule.rangeEnd);
			feedback.append(" ");
			// match count for this rule
			addNum(feedback, 3, occurances[i]);
			feedback.append(" ");
			// score from this rule
			addNum(feedback, 3, thisScore);
			feedback.append(" ");
			// max score from this rule
			addNum(feedback, 3, thisMaxScore);
			feedback.append(" ");
			// total score this far
			addNum(feedback, 3, cumulativeScore);
			feedback.append(" ");
			// max total score this far
			addNum(feedback, 3, cumulativeMaxScore);
			feedback.append(" ");
			// score missed from this rule (very redundant information)
			addNum(feedback, 3, lostScore);
			feedback.append(" ");

			// The pattern string, truncate at 48 chars to fit on a line
			String patternStr = rule.patternString;
			if (patternStr.length() > 48) {
				patternStr = patternStr.substring(0, 48);
			}
			feedback.append(patternStr);

			feedback.append("\n");
		}

		feedback.append("Awarded " + cumulativeScore + "/" + cumulativeMaxScore
				+ " marks\n");



		return feedback.toString();
	}

	private static void addNum(StringBuffer line, int width, int num) {
		String numString = String.valueOf(num);
		int numWidth = numString.length();

		if (numWidth > width) {
			// Let's not allow too wide strings
			if (num < 0) {
				numString = "<-10000000000000".substring(0, width);
			}
			if (num > 0) {
				numString = ">100000000000000".substring(0, width);
			}
			numWidth = numString.length();
		}

		if (numWidth < width) {
			line.append("                     ".substring(0, width - numWidth));
		}

		line.append(numString);
	}

	/**
	 * Hidden Constructor
	 *
	 * @param rule String representing the rule in format of oracle
	 * @throws RegexRuleSyntaxException
	 */
	protected RegexRule(String rule)
	throws RegexRuleSyntaxException {

		this.rule = rule;
		
		Vector<String> parts = split(rule);

		// TODO: is it necessary to unescape escaped colons or other characters?

		// Set defaults
		score = DEFAULT_SCORE;
		operator = DEFAULT_OPERATOR;
		rangeStart = DEFAULT_RANGE_START;
		rangeEnd = DEFAULT_RANGE_END;

		if (parts.size() == 1) {
			// No options. Basic RE
			patternString = (String) parts.get(0);
		}
		else if (parts.size() == 2) {
			// score or operator
			int pos = parseOperator((String) parts.get(0));
			if (operator == UNKNOWN_OPERATOR) {
				// Set default operator, it was a score field.
				operator = DEFAULT_OPERATOR;
				score = parseScore((String) parts.get(0));
			}
			else {
				parseOperands((String) parts.get(0), pos);
			}
			patternString = (String) parts.get(1);
		}
		else if (parts.size() == 3) {
			// Score, operator, Regular expression
			score = parseScore((String) parts.get(0));
			int pos = parseOperator((String) parts.get(1));
			if (operator == UNKNOWN_OPERATOR) {
				throw new RegexRuleSyntaxException("Unknown operator: "
						+ (String) parts.get(0));
			}
			parseOperands((String) parts.get(1), pos);
			patternString = (String) parts.get(2);
		}
		else if (parts.size() > 3) {
			// Invalid syntax
			throw new RegexRuleSyntaxException("Too many colons on a line");
		}

		try {
			pattern = Pattern.compile(patternString);
		}
		catch (PatternSyntaxException rse) {
			throw new RegexRuleSyntaxException("Syntax error in Regex", rse);
		}

		// Check that the ocurrance range is sensible
		if (rangeStart > rangeEnd) {
			throw new RegexRuleSyntaxException("Range minimum is less than maximum");
		}

	}

	/**
	 * Matches the input against the pattern saved in the rule
	 *
	 * If the line matches, true will be returned.
	 *
	 * If setMatchWholeLine(true) has been called,
	 * match will happen if the pattern matches the whole input string.
	 *
	 * If setMatchWholeLine(false) has been called, the Ceilidh's oracle
	 * behaviour can be achieved by adding ".*" into the beginning and
	 * end of the input string.
	 *
	 * When the setting is false, adding "^" to the beginning and "$" to
	 * the end of the regex has the same effect as as if the setting was
	 * set.
	 *
	 * @param input string to match the pattern with
	 * @return true, if the pattern/rule matches the input, false if not
	 */
	public boolean matches(String input) {
		if (matchWholeLine == true) {
			// Match only whole lines
			if (pattern.matcher(input).matches()) {
				return true;
			}
			else {
				return false;
			}
		}
		else {
			// Matches inside the line are accepted too
			if (pattern.matcher(input).find()) {
				return true;
			}
			else {
				return false;
			}
		}
	}

	/**
	 * @param rule String to split by unescaped colons
	 * @return Vector of Strings
	 */
	private Vector<String> split(String rule) {

		// Splitting of the rule string is made by hand becose
		// String's split-method cannot be used, because it's not possible
		// to bypass backslash-escaped colons with it.

		// The parts of the rule. Vector of Strings
		Vector<String> parts = new Vector<String>();

		// Split at nonescaped colons.
		int i = 1;

		// Beginning of the latest part
		int beg = 0;
		while (i > -1 && i < rule.length()) {
			i = rule.indexOf(':', i);
			if (i < 0) {
				break;
			}
			if (i > 0 && rule.charAt(i - 1) == '\\') {
				// It's an escaped colon. Let's skip
				i++;
				continue;
			}
			// Extract the current part
			String part = rule.substring(beg, i);

			// Add to the vector
			parts.add(part);

			// Skip the colon itself
			i++;
			beg = i;
		}

		// Add the final part
		String part = rule.substring(beg, rule.length());
		parts.add(part);

		return parts;
	}


	/**
	 * Parses the operator part of the string
	 *
	 * After calling this function operator will hold the
	 * operator specified in the string and rangeStart and rangeEnd
	 * set appropriately for the operator. If an operator is not
	 * detected, operator == UNKNOWN_OPERATOR
	 *
	 * The string given to this function doesn't have to be the operator
	 * part. This function may be called to determine that the string
	 * isn't an operator.
	 *
	 * @param part operator part of the string
	 * @return position inside the string after the operator string
	 * @throws RegexRuleSyntaxException if there is a syntactic error
	 */
	protected int parseOperator(String part) {
		int pos = 0;
		int op = UNKNOWN_OPERATOR;
		if (part.startsWith("<=")) {
			op = LESS_OR_EQUAL;
			pos = 2;
		}
		else if (part.startsWith("<")) {
			op = LESS;
			pos = 1;
		}
		else if (part.startsWith(">=")) {
			op = GREATER_OR_EQUAL;
			pos = 2;
		}
		else if (part.startsWith(">")) {
			op = GREATER;
			pos = 1;
		}
		else if (part.startsWith("==")) {
			op = EQUAL;
			pos = 2;
		}
		else if (part.startsWith("!=")) {
			op = NOT_EQUAL;
			pos = 2;
		}
		else if (part.equals("+")) {
			op = AFTER;
			// No parameters
			pos = -1;
		}
		else if (part.equals("-")) {
			op = NOT_AFTER;
			// No parameters
			pos = -1;
		}
		else {
			// It's not a known operator
			op = UNKNOWN_OPERATOR;
			pos = 0;
		}

		operator = op;

		return pos;
	}

	/**
	 * Parses the operands part of the operator string
	 *
	 * Parses the numbers after the operand (>, >=, ==, etc) and
	 * saves them in rangeStart and rangeEnd.
	 *
	 * @param part The operand part
	 * @param pos the position of the beginning of the operands
	 * @throws RegexRuleSyntaxException if there is a syntactic error
	 */
	protected void parseOperands(String part, int pos)
	throws RegexRuleSyntaxException {

		if (pos < 0) {
			// No operands expected
			return;
		}

		if (part.length() <= pos) {
			throw new RegexRuleSyntaxException("Invalid operand");
		}

		String operandString = part.substring(pos);

		try {
			int dashPos = operandString.indexOf('-');
			if (dashPos >= 0) {
				// It's a range
				String[] parts = operandString.split("-");
				if (parts.length != 2) {
					throw new RegexRuleSyntaxException("Too many dashes in range");
				}
				int min = Integer.parseInt(parts[0]);
				int max = Integer.parseInt(parts[1]);
				rangeStart = min;
				rangeEnd = max;
			}
			else {
				int num = Integer.parseInt(operandString);
				rangeStart = num;
				rangeEnd = num;
			}
		}
		catch (NumberFormatException nfe) {
			throw new RegexRuleSyntaxException("Syntax error after operator. " +
			"Number or range expected");
		}
	}

	/**
	 * Parses score
	 *
	 * @param part containing the score
	 * @return the score
	 * @throws RegexRuleSyntaxException if there is a syntactic error
	 */
	protected int parseScore(String part) {
		try {
			int sign = 1;
			if (part.startsWith("~")) {
				sign = -1;
				part = part.substring(1);
			}
			return sign * Integer.parseInt(part);
		}
		catch (NumberFormatException nfe) {
			throw new RegexRuleSyntaxException("Syntax error in score");
		}
	}

	/**
	 * Gives string representation of the rule
	 * @return the string representation of the rule
	 */
	public String toString() {
		return rule;
	}
}