package uiml.xml;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

public class XMLSAX {
	private Map entities;
	private boolean ignoreWhitespace;
	private char charReadTooMuch;
	private Reader reader;
	private int parserLineNr;
	private final XMLSAXListener listener;

	public static void parse(String xml, boolean ignoreWhitespace, XMLSAXListener listener) throws XMLParseException {
		try {
			new XMLSAX(new StringReader(xml), ignoreWhitespace, listener);
		} catch (IOException ioexception) {}
	}

	public static void parse(Reader reader, boolean ignoreWhitespace, XMLSAXListener listener) throws IOException,
			XMLParseException {
		new XMLSAX(reader, ignoreWhitespace, listener);

	}

	private XMLSAX(Reader reader, boolean ignoreWhitespace, XMLSAXListener listener) throws IOException,
			XMLParseException {
		this.ignoreWhitespace = ignoreWhitespace;
		this.listener = listener;

		entities = new HashMap();
		parserLineNr = 0;

		entities.put("amp", new char[] { '&' });
		entities.put("quot", new char[] { '"' });
		entities.put("apos", new char[] { '\'' });
		entities.put("lt", new char[] { '<' });
		entities.put("gt", new char[] { '>' });

		parseFromReader(reader);
	}

	private void parseFromReader(Reader reader) throws IOException, XMLParseException {
		charReadTooMuch = '\0';
		this.reader = reader;
		do {
			char c = scanWhitespace();
			if (c != '<') throw expectedInput("<");
			c = readChar();
			if (c == '!' || c == '?') {
				skipSpecialTag(0);
			} else {
				unreadChar(c);
				scanElement();
				return;
			}
		} while (true);
	}

	protected void scanElement() throws IOException, XMLParseException {
		String name = null;
		Map attributes = new HashMap(8);

		StringBuffer stringbuffer;
		String s;
		char c;
		continueScanning: {
			stringbuffer = new StringBuffer();
			scanIdentifier(stringbuffer);
			s = stringbuffer.toString();
			name = s;
			for (c = scanWhitespace(); c != '>' && c != '/'; c = scanWhitespace()) {
				stringbuffer.setLength(0);
				unreadChar(c);
				scanIdentifier(stringbuffer);
				String attributeName = stringbuffer.toString();
				c = scanWhitespace();
				if (c != '=') throw expectedInput("=");
				unreadChar(scanWhitespace());
				stringbuffer.setLength(0);
				scanString(stringbuffer);
				attributes.put(attributeName, stringbuffer.toString());
			}

			listener.startElement(name, attributes);

			if (c == '/') {
				c = readChar();
				if (c != '>') throw expectedInput(">");
				else {
					listener.endElement(name);
					return;
				}
			}
			stringbuffer.setLength(0);
			c = scanWhitespace(stringbuffer);
			if (c != '<') {
				unreadChar(c);
				scanPCData(stringbuffer);
				break continueScanning;
			}
			do {
				c = readChar();
				if (c != '!') {
					break;
				}
				if (checkCDATA(stringbuffer)) {
					scanPCData(stringbuffer);
					break continueScanning;
				}
				c = scanWhitespace(stringbuffer);
				if (c != '<') {
					unreadChar(c);
					scanPCData(stringbuffer);
					break continueScanning;
				}
			} while (true);
			if (c != '/' || ignoreWhitespace) stringbuffer.setLength(0);
			if (c == '/') unreadChar(c);
		}
		if (stringbuffer.length() == 0) {
			for (; c != '/'; c = readChar()) {
				if (c == '!') {
					c = readChar();
					if (c != '-') throw expectedInput("Comment or Element");
					c = readChar();
					if (c != '-') throw expectedInput("Comment or Element");
					skipComment();
				} else {
					unreadChar(c);
					scanElement();
				}
				c = scanWhitespace();
				if (c != '<') throw expectedInput("<");
			}

			unreadChar(c);
		} else if (ignoreWhitespace) {
			final String text = stringbuffer.toString().trim();
			if (text.length() > 0) {
				listener.text(text);
			}
		} else {
			listener.text(stringbuffer.toString());
		}
		c = readChar();
		if (c != '/') throw expectedInput("/");
		unreadChar(scanWhitespace());
		if (!checkLiteral(s)) throw expectedInput(s);
		if (scanWhitespace() != '>') throw expectedInput(">");
		else {
			listener.endElement(name);
		}
	}

	protected void scanIdentifier(StringBuffer stringbuffer) throws IOException, XMLParseException {
		do {
			char c = readChar();
			if ((c < 'A' || c > 'Z') && (c < 'a' || c > 'z') && (c < '0' || c > '9') && c != '_' && c != '.'
					&& c != ':' && c != '-' && c <= '~') {
				unreadChar(c);
				return;
			}
			stringbuffer.append(c);
		} while (true);
	}

	protected char scanWhitespace() throws IOException, XMLParseException {
		do {
			char c = readChar();
			switch (c) {
				default:
					return c;

				case 9: // '\t'
				case 10: // '\n'
				case 13: // '\r'
				case 32: // ' '
					break;
			}
		} while (true);
	}

	protected char scanWhitespace(StringBuffer stringbuffer) throws IOException, XMLParseException {
		do {
			char c = readChar();
			switch (c) {
				case 9: // '\t'
				case 10: // '\n'
				case 32: // ' '
					stringbuffer.append(c);
					break;

				default:
					return c;

				case 13: // '\r'
					break;
			}
		} while (true);
	}

	protected void scanString(StringBuffer stringbuffer) throws IOException, XMLParseException {
		char c = readChar();
		if (c != '\'' && c != '"') throw expectedInput("' or \"");
		do {
			char c1 = readChar();
			if (c1 == c) return;
			if (c1 == '&') resolveEntity(stringbuffer);
			else
				stringbuffer.append(c1);
		} while (true);
	}

	protected void scanPCData(StringBuffer stringbuffer) throws IOException, XMLParseException {
		do {
			char c = readChar();
			if (c == '<') {
				c = readChar();
				if (c == '!') {
					checkCDATA(stringbuffer);
				} else {
					unreadChar(c);
					return;
				}
			} else if (c == '&') resolveEntity(stringbuffer);
			else
				stringbuffer.append(c);
		} while (true);
	}

	protected boolean checkCDATA(StringBuffer stringbuffer) throws IOException, XMLParseException {
		char c = readChar();
		if (c != '[') {
			unreadChar(c);
			skipSpecialTag(0);
			return false;
		}
		if (!checkLiteral("CDATA[")) {
			skipSpecialTag(1);
			return false;
		}
		for (int i = 0; i < 3;) {
			char c1 = readChar();
			switch (c1) {
				case 93: // ']'
					if (i < 2) {
						i++;
					} else {
						stringbuffer.append(']');
						stringbuffer.append(']');
						i = 0;
					}
					break;

				case 62: // '>'
					if (i < 2) {
						for (int j = 0; j < i; j++)
							stringbuffer.append(']');

						i = 0;
						stringbuffer.append('>');
					} else {
						i = 3;
					}
					break;

				default:
					for (int k = 0; k < i; k++)
						stringbuffer.append(']');

					stringbuffer.append(c1);
					i = 0;
					break;
			}
		}

		return true;
	}

	protected void skipComment() throws IOException, XMLParseException {
		for (int i = 2; i > 0;) {
			char c = readChar();
			if (c == '-') i--;
			else
				i = 2;
		}

		if (readChar() != '>') throw expectedInput(">");
		else
			return;
	}

	protected void skipSpecialTag(int i) throws IOException, XMLParseException {
		int j = 1;
		char c = '\0';
		if (i == 0) {
			char c1 = readChar();
			if (c1 == '[') i++;
			else if (c1 == '-') {
				char c2 = readChar();
				if (c2 == '[') i++;
				else if (c2 == ']') i--;
				else if (c2 == '-') {
					skipComment();
					return;
				}
			}
		}
		while (j > 0) {
			char c3 = readChar();
			if (c == 0) {
				if (c3 == '"' || c3 == '\'') c = c3;
				else if (i <= 0) if (c3 == '<') j++;
				else if (c3 == '>') j--;
				if (c3 == '[') i++;
				else if (c3 == ']') i--;
			} else if (c3 == c) c = '\0';
		}
	}

	protected boolean checkLiteral(String s) throws IOException, XMLParseException {
		int i = s.length();
		for (int j = 0; j < i; j++)
			if (readChar() != s.charAt(j)) return false;

		return true;
	}

	protected char readChar() throws IOException, XMLParseException {
		if (charReadTooMuch != 0) {
			char c = charReadTooMuch;
			charReadTooMuch = '\0';
			return c;
		}
		int i = reader.read();
		if (i < 0) throw unexpectedEndOfData();
		if (i == 10) {
			parserLineNr++;
			return '\n';
		} else {
			return (char) i;
		}
	}

	protected void resolveEntity(StringBuffer stringbuffer) throws IOException, XMLParseException {
		StringBuffer stringbuffer1 = new StringBuffer();
		do {
			char c = readChar();
			if (c == ';') break;
			stringbuffer1.append(c);
		} while (true);
		String s = stringbuffer1.toString();
		if (s.charAt(0) == '#') {
			char c1;
			try {
				if (s.charAt(1) == 'x') c1 = (char) Integer.parseInt(s.substring(2), 16);
				else
					c1 = (char) Integer.parseInt(s.substring(1), 10);
			} catch (NumberFormatException numberformatexception) {
				throw unknownEntity(s);
			}
			stringbuffer.append(c1);
		} else {
			char ac[] = (char[]) entities.get(s);
			if (ac == null) throw unknownEntity(s);
			stringbuffer.append(ac);
		}
	}

	protected void unreadChar(char c) {
		charReadTooMuch = c;
	}

	protected XMLParseException unexpectedEndOfData() {
		String s = "Unexpected end of data reached";
		return new XMLParseException("", parserLineNr, s);
	}

	protected XMLParseException syntaxError(String s, String name) {
		String s1 = "Syntax error while parsing " + s;
		return new XMLParseException(name, parserLineNr, s1);
	}

	protected XMLParseException expectedInput(String s) {
		String s1 = "Expected: " + s;
		return new XMLParseException("", parserLineNr, s1);
	}

	protected XMLParseException unknownEntity(String s) {
		String s1 = "Unknown or invalid entity: &" + s + ";";
		return new XMLParseException("", parserLineNr, s1);
	}
}
