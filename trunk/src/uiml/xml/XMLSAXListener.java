package uiml.xml;

import java.util.Map;

public abstract class XMLSAXListener {

	public abstract void startElement(String tag, Map attributes);

	public abstract void text(String text);

	public abstract void endElement(String tag);

	public static int toInt(String s) {
		return toInt(s, 0);
	}

	public static int toInt(String str, int i) {
		if (str == null) return i;
		try {
			return Integer.parseInt(str);
		} catch (NumberFormatException numberformatexception) {
			return i;
		}
	}

	public double toDouble(String s) {
		return toDouble(s, 0.0D);
	}

	public double toDouble(String s, double d) {
		if (s == null) return d;
		try {
			return Double.valueOf(s).doubleValue();
		} catch (NumberFormatException numberformatexception) {
			return d;
		}
	}

	public boolean toBoolean(String s) {
		if (s == null) return false;
		return Boolean.valueOf(s).booleanValue();
	}
}
