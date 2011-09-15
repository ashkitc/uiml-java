package uiml.xml;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

public class XMLDOM {
	private Map  attributes;
	private Vector  children;
	private String name;
	private String contents;
	private Map  entities;
	private int lineNr;
	private boolean ignoreWhitespace;
	private char charReadTooMuch;
	private Reader reader;
	private int parserLineNr;

	public XMLDOM(boolean ignoreWhitespace) {
		this.ignoreWhitespace = ignoreWhitespace;
		name = null;
		contents = "";
		attributes = new HashMap ();
		children = new Vector ();
		entities = new HashMap ();
		lineNr = 0;

		entities.put("amp", new char[] { '&' });
		entities.put("quot", new char[] { '"' });
		entities.put("apos", new char[] { '\'' });
		entities.put("lt", new char[] { '<' });
		entities.put("gt", new char[] { '>' });
	}

	protected XMLDOM createAnotherElement() {
		return new XMLDOM(ignoreWhitespace);
	}

	public void addChild(XMLDOM xmlelement) {
		children.addElement(xmlelement);
	}

	public void setAttribute(String s, Object obj) {
		attributes.put(s, obj.toString());
	}

	public void setIntAttribute(String s, int i) {
		attributes.put(s, Integer.toString(i));
	}

	public void setDoubleAttribute(String s, double d) {
		attributes.put(s, Double.toString(d));
	}

	public int countChildren() {
		return children.size();
	}

	/** set of strings */
	public Set getAttributes() {
		return attributes.keySet();
	}

	/** Vector of XMLDOM */
	public Vector getChildren() {
		return children;
	}

	public String getContent() {
		return contents;
	}

	public int getLineNr() {
		return lineNr;
	}

	public String getAttribute(String s) {
		return getAttribute(s, null);
	}

	public String getAttribute(String s, String obj) {
		String obj1 = (String) attributes.get(s);
		if (obj1 == null) obj1 = obj;
		return obj1;
	}

	public String getStringAttribute(String s) {
		return getStringAttribute(s, null);
	}

	public String getStringAttribute(String s, String s1) {
		return (String) getAttribute(s, s1);
	}

	public int getIntAttribute(String s) {
		return getIntAttribute(s, 0);
	}

	public int getIntAttribute(String s, int i) {
		String s1 = (String) attributes.get(s);
		if (s1 == null) return i;
		try {
			return Integer.parseInt(s1);
		} catch (NumberFormatException numberformatexception) {
			return i;
		}
	}

	public double getDoubleAttribute(String s) {
		return getDoubleAttribute(s, 0.0D);
	}

	public double getDoubleAttribute(String s, double d) {
		String s1 = (String) attributes.get(s);
		if (s1 == null) return d;
		try {
			return Double.valueOf(s1).doubleValue();
		} catch (NumberFormatException numberformatexception) {
			return d;
		}
	}

	public boolean getBooleanAttribute(String s) {
		String obj = (String) attributes.get(s);
		if (obj == null) return false;
		return Boolean.valueOf(obj).booleanValue();
	}

	public String getName() {
		return name;
	}

	public void parseFromReader(Reader reader1) throws IOException, XMLParseException {
		name = null;
		contents = "";
		attributes = new HashMap();
		children = new Vector();
		charReadTooMuch = '\0';
		reader = reader1;
		do {
			char c = scanWhitespace();
			if (c != '<') throw expectedInput("<");
			c = readChar();
			if (c == '!' || c == '?') {
				skipSpecialTag(0);
			} else {
				unreadChar(c);
				scanElement(this);
				return;
			}
		} while (true);
	}

	public void parseString(String s) throws XMLParseException {
		try {
			parseFromReader(new StringReader(s));
		} catch (IOException ioexception) {}
	}

	public void removeChild(XMLDOM xmlelement) {
		children.removeElement(xmlelement);
	}

	public void removeAttribute(String s) {
		attributes.remove(s);
	}

	public void setContent(String s) {
		contents = s;
	}

	public void setName(String s) {
		name = s;
	}

	public String toString() {
		try {
			ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream();
			OutputStreamWriter outputstreamwriter = new OutputStreamWriter(bytearrayoutputstream);
			write(outputstreamwriter);
			outputstreamwriter.flush();
			return new String(bytearrayoutputstream.toByteArray());
		} catch (IOException ioexception) {
			return super.toString();
		}
	}

	public void write(Writer writer) throws IOException {
		if (name == null) {
			writeEncoded(writer, contents);
			return;
		}
		writer.write(60);
		writer.write(name);
		if (!attributes.isEmpty()) {
			Iterator it = attributes.keySet().iterator();
			while (it.hasNext()) {
				final String s = (String) it.next();
				writer.write(32);
				String s1 = (String) attributes.get(s);
				writer.write(s);
				writer.write(61);
				writer.write(34);
				writeEncoded(writer, s1);
				writer.write(34);
			}

		}
		if (contents != null && contents.length() > 0) {
			writer.write(62);
			writeEncoded(writer, contents);
			writer.write(60);
			writer.write(47);
			writer.write(name);
			writer.write(62);
		} else if (children.isEmpty()) {
			writer.write(47);
			writer.write(62);
		} else {
			writer.write(62);
			Iterator it = getChildren().iterator();
			while (it.hasNext()) {
				final XMLDOM element = (XMLDOM) it.next();
				element.write(writer);
			}
			writer.write(60);
			writer.write(47);
			writer.write(name);
			writer.write(62);
		}
	}

	protected void writeEncoded(Writer writer, String s) throws IOException {
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			switch (c) {
				case 60: // '<'
					writer.write(38);
					writer.write(108);
					writer.write(116);
					writer.write(59);
					break;

				case 62: // '>'
					writer.write(38);
					writer.write(103);
					writer.write(116);
					writer.write(59);
					break;

				case 38: // '&'
					writer.write(38);
					writer.write(97);
					writer.write(109);
					writer.write(112);
					writer.write(59);
					break;

				case 34: // '"'
					writer.write(38);
					writer.write(113);
					writer.write(117);
					writer.write(111);
					writer.write(116);
					writer.write(59);
					break;

				case 39: // '\''
					writer.write(38);
					writer.write(97);
					writer.write(112);
					writer.write(111);
					writer.write(115);
					writer.write(59);
					break;

				default:
					char c1 = c;
					if (c1 < ' ' || c1 > '~') {
						writer.write(38);
						writer.write(35);
						writer.write(120);
						writer.write(Integer.toString(c1, 16));
						writer.write(59);
					} else {
						writer.write(c);
					}
					break;
			}
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

	protected void scanElement(XMLDOM xmlelement) throws IOException, XMLParseException {
		StringBuffer stringbuffer;
		String s;
		char c;
		label0: {
			stringbuffer = new StringBuffer();
			scanIdentifier(stringbuffer);
			s = stringbuffer.toString();
			xmlelement.setName(s);
			for (c = scanWhitespace(); c != '>' && c != '/'; c = scanWhitespace()) {
				stringbuffer.setLength(0);
				unreadChar(c);
				scanIdentifier(stringbuffer);
				String s1 = stringbuffer.toString();
				c = scanWhitespace();
				if (c != '=') throw expectedInput("=");
				unreadChar(scanWhitespace());
				stringbuffer.setLength(0);
				scanString(stringbuffer);
				xmlelement.setAttribute(s1, stringbuffer);
			}

			if (c == '/') {
				c = readChar();
				if (c != '>') throw expectedInput(">");
				else
					return;
			}
			stringbuffer.setLength(0);
			c = scanWhitespace(stringbuffer);
			if (c != '<') {
				unreadChar(c);
				scanPCData(stringbuffer);
				break label0;
			}
			do {
				c = readChar();
				if (c != '!') break;
				if (checkCDATA(stringbuffer)) {
					scanPCData(stringbuffer);
					break label0;
				}
				c = scanWhitespace(stringbuffer);
				if (c != '<') {
					unreadChar(c);
					scanPCData(stringbuffer);
					break label0;
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
					XMLDOM xmlelement1 = createAnotherElement();
					scanElement(xmlelement1);
					xmlelement.addChild(xmlelement1);
				}
				c = scanWhitespace();
				if (c != '<') throw expectedInput("<");
			}

			unreadChar(c);
		} else if (ignoreWhitespace) xmlelement.setContent(stringbuffer.toString().trim());
		else {
			xmlelement.setContent(stringbuffer.toString());
		}
		c = readChar();
		if (c != '/') throw expectedInput("/");
		unreadChar(scanWhitespace());
		if (!checkLiteral(s)) throw expectedInput(s);
		if (scanWhitespace() != '>') throw expectedInput(">");
		else
			return;
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

	protected XMLParseException invalidValueSet(String s) {
		String s1 = "Invalid value set (entity name = \"" + s + "\")";
		return new XMLParseException(getName(), parserLineNr, s1);
	}

	protected XMLParseException invalidValue(String s, String s1) {
		String s2 = "Attribute \"" + s + "\" does not contain a valid " + "value (\"" + s1 + "\")";
		return new XMLParseException(getName(), parserLineNr, s2);
	}

	protected XMLParseException unexpectedEndOfData() {
		String s = "Unexpected end of data reached";
		return new XMLParseException(getName(), parserLineNr, s);
	}

	protected XMLParseException syntaxError(String s) {
		String s1 = "Syntax error while parsing " + s;
		return new XMLParseException(getName(), parserLineNr, s1);
	}

	protected XMLParseException expectedInput(String s) {
		String s1 = "Expected: " + s;
		return new XMLParseException(getName(), parserLineNr, s1);
	}

	protected XMLParseException unknownEntity(String s) {
		String s1 = "Unknown or invalid entity: &" + s + ";";
		return new XMLParseException(getName(), parserLineNr, s1);
	}
}
