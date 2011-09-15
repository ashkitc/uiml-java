package uiml.xml;

public class XMLParseException extends Exception {
	private static final long serialVersionUID = 1L;

	public static final int NO_LINE = -1;
	private int lineNr;

	public XMLParseException(String s, String s1) {
		super("XML Parse Exception during parsing of " + (s != null ? "a " + s + " element" : "the XML definition")
				+ ": " + s1);
		lineNr = -1;
	}

	public XMLParseException(String s, int i, String s1) {
		super("XML Parse Exception during parsing of " + (s != null ? "a " + s + " element" : "the XML definition")
				+ " at line " + i + ": " + s1);
		lineNr = i;
	}

	public int getLineNr() {
		return lineNr;
	}

}
