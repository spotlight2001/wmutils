package at.wm.util;

public class SpringExpressionLanguageUtils {

	private static final String EL_PREFIX = "#{";

	public static String getBeanName(String expressionString) {
		String dotSeparatedString = getDotSeparatedString(expressionString);
		return dotSeparatedString.subSequence(0,
				dotSeparatedString.indexOf(".")).toString();
	}

	public static String getKey(String expressionString) {
		String dotSeparatedString = getDotSeparatedString(expressionString);
		return dotSeparatedString
				.substring(dotSeparatedString.indexOf(".") + 1);
	}

	private static String getDotSeparatedString(String expressionString) {
		String errMsg = "Expect something like #{abc.xyz.123} but got: '"
				+ expressionString + "'.";
		if (expressionString == null || !expressionString.startsWith(EL_PREFIX)) {
			throw new IllegalArgumentException(errMsg);
		}
		String dotSeparatedString = expressionString.substring(
				EL_PREFIX.length(), expressionString.length() - 1);
		return dotSeparatedString;
	}
}
