package uiml;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import uiml.xml.XMLDOM;
import uiml.xml.XMLParseException;

public class UIMLFactory {

	private static final String ID_ATTRIBUTE_NAME = "id";

	/**
	 * Creates interface defined by a XML which is in the same package as the
	 * given class with name as the class name + ".xml".Then returns a map
	 * <String, Component> that relates SWT component id to component object.
	 * 
	 * @param parent
	 *            Component to which the UI need to be generated
	 * @param clazz
	 *            the class where XML file can be found
	 * @return a map <String, Component> that relates SWT component id to
	 *         component object.
	 */
	public final static Map createUI(Composite parent, Class clazz) {
		try {
			String[] tokens = split(clazz.getName(), '.');
			final String filename = tokens[tokens.length - 1] + ".xml";
			InputStream is = clazz.getResourceAsStream(filename);
			if (is != null) {
				InputStreamReader isr = new InputStreamReader(is);

				XMLDOM parser = new XMLDOM(true);

				parser.parseFromReader(isr);
				if ("layout".equals(parser.getName()) && parser.countChildren() > 0) {
					Map components = new HashMap();
					defineGrid(parent, parser);
					defineLayout(parent, parser);
					createSubComponents(components, parent, parser);
					return components;
				}
				isr.close();
				is.close();
			} else {
				throw new Exception("resource [" + filename + "] not found!");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Using reflect this functions tries to map all components that are in the
	 * map to the variable in he context
	 * 
	 * @param map
	 *            Map<String, Component>
	 * @param context
	 *            object with public fields that has the same name as the keys
	 *            in the map
	 */
	public static void mapContext(Map map, Object context) {
		if (context == null) {
			return;
		}

		Field[] fields = context.getClass().getFields();
		for (int i = 0, j = fields.length; i < j; i++) {
			Field field = fields[i];

			Object value = map.get(field.getName());
			if (value != null) {
				Class type = field.getType();
				if (type.isAssignableFrom(value.getClass())) {
					try {
						field.set(context, value);
					} catch (Throwable e) {}
				}
			}
		}
	}

	// <layout columns="4">
	// <label text />
	// <button text default icon/>
	// <text text readonly />
	// <table fullselection />
	// <list multiselection />
	// <combo readonly />
	// <spinner />
	// <container columns />--sub tags
	// <section text/> --sub tags
	// <splitter horizontal /> --sub tags
	// </layout>
	/**
	 * Creates UI defined by XML into given parent component .
	 * 
	 * @param parent
	 * @param componentXML
	 * @return
	 */
	public final static Map createUI(Composite parent, String componentXML) {
		XMLDOM parser = new XMLDOM(true);
		try {
			parser.parseString(componentXML);
			if ("layout".equals(parser.getName()) && parser.countChildren() > 0) {
				Map components = new HashMap();
				defineGrid(parent, parser);
				defineLayout(parent, parser);
				createSubComponents(components, parent, parser);
				return components;
			}
		} catch (XMLParseException e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Recursively generate components
	 * 
	 * @param components
	 * @param parent
	 * @param parser
	 */
	private final static void createSubComponents(Map components, Composite parent, XMLDOM parser) {

		Iterator it = parser.getChildren().iterator();
		while (it.hasNext()) {
			final XMLDOM tag = (XMLDOM) it.next();
			final String tagName = tag.getName();
			if ("label".equals(tagName)) {
				createLabel(components, parent, tag);
			} else if ("button".equals(tagName)) {
				createButton(components, parent, tag);
			} else if ("text".equals(tagName)) {
				createText(components, parent, tag);
			} else if ("table".equals(tagName)) {
				createTable(components, parent, tag);
			} else if ("list".equals(tagName)) {
				createList(components, parent, tag);
			} else if ("combo".equals(tagName)) {
				createCombo(components, parent, tag);
			} else if ("spinner".equals(tagName)) {
				createSpinner(components, parent, tag);
			} else if ("container".equals(tagName)) {
				createContainer(components, parent, tag);
			} else if ("splitter".equals(tagName)) {
				createSplitter(components, parent, tag);
			} else if ("tabs".equals(tagName)) {
				createTabs(components, parent, tag);
			} else if ("datetime".equals(tagName)) {
				createDateTime(components, parent, tag);
			}
		}
	}

	/**
	 * uses "columns" parameter to define Grid
	 */
	private final static void defineGrid(Composite component, XMLDOM tag) {
		GridLayout layout = new GridLayout();
		int value = tag.getIntAttribute("columns", 1);
		layout.numColumns = value;
		layout.marginWidth = 1;
		layout.marginHeight = 1;
		component.setLayout(layout);
	}

	/**
	 * returns BEGINING|CENTER|END|FILL defined by string
	 */
	private final static int getAlignment(String align) {
		final int len = (align == null) ? 0 : align.length();
		if (6 == len) {// CENTER
			return GridData.CENTER;
		} else if (3 == len) {// END
			return GridData.END;
		} else if (4 == len) {// FILL
			return GridData.FILL;
		}
		return GridData.BEGINNING;
	}

	/**
	 * use "align", "valign", "width", "height", "colspan", "rowspan" parameters
	 * to define Grid cell
	 * 
	 * <pre>
	 * <xxxxxx colspan="2" rowspan="2" width="100" height="20" align="BEGINING|CENTER|END|FILL" valign="BEGIN|CENTER|END|FILL"/>
	 * </pre>
	 */
	//
	private static final void defineLayout(Control control, XMLDOM tag) {

		int align = getAlignment(tag.getAttribute("align"));
		int valign = getAlignment(tag.getAttribute("valign"));
		GridData gdata = new GridData(align, valign, align == GridData.FILL, valign == GridData.FILL);
		gdata.horizontalIndent = 0;
		gdata.verticalIndent = 0;
		{
			final int value = tag.getIntAttribute("width", 0);
			if (value != 0) gdata.widthHint = value;
		}
		{
			final int value = tag.getIntAttribute("height", 0);
			if (value != 0) gdata.heightHint = value;
		}
		{
			final int value = tag.getIntAttribute("colspan", 0);
			if (value != 0) gdata.horizontalSpan = value;
		}
		{
			final int value = tag.getIntAttribute("rowspan", 0);
			if (value != 0) gdata.verticalSpan = value;
		}

		control.setLayoutData(gdata);
	}

	/**
	 * Defines button component
	 * 
	 * <pre>
	 * <button id="javaName" text="buttonText" check="true" default="true" />
	 * </pre>
	 * 
	 * @param components
	 * @param parent
	 * @param tag
	 */
	private final static void createButton(Map components, Composite parent, XMLDOM tag) {
		int style = SWT.PUSH;
		final boolean readonly = tag.getBooleanAttribute("check");
		if (readonly) style = SWT.CHECK;

		Button button = new Button(parent, style);
		defineLayout(button, tag);
		{
			final String value = tag.getAttribute(ID_ATTRIBUTE_NAME);
			if (value != null) {
				components.put(value, button);
			}
		}
		{
			final String value = tag.getAttribute("text");
			if (value != null) {
				button.setText(value);
			}
		}

		{
			final boolean value = tag.getBooleanAttribute("default");
			if (value) {
				Shell shell = button.getShell();
				if (shell != null) {
					shell.setDefaultButton(button);
				}
			}
		}
	}

	/**
	 * Defines button component
	 * 
	 * <pre>
	 * <label id="javaName" text="buttonText"/>
	 * </pre>
	 * 
	 * @param components
	 * @param parent
	 * @param tag
	 */
	private final static void createLabel(Map components, Composite parent, XMLDOM tag) {
		Label label = new Label(parent, 0);
		defineLayout(label, tag);
		{
			final String value = tag.getAttribute(ID_ATTRIBUTE_NAME);
			if (value != null) {
				components.put(value, label);
			}
		}
		{
			final String value = tag.getAttribute("text");
			if (value != null) {
				label.setText(value);
			}
		}
	}

	/**
	 * Defines Text component
	 * 
	 * <pre>
	 * <text id="javaName" text="labelText" readonly="true" multi="true" wrap="true" hscroll="true" vscroll="true"/>
	 * </pre>
	 * 
	 * @param components
	 * @param parent
	 * @param tag
	 */
	private final static void createText(Map components, Composite parent, XMLDOM tag) {
		final boolean readonly = tag.getBooleanAttribute("readonly");
		final boolean multi = tag.getBooleanAttribute("multi");
		final boolean wrap = tag.getBooleanAttribute("wrap");
		final boolean hscroll = tag.getBooleanAttribute("hscroll");
		final boolean vscroll = tag.getBooleanAttribute("vscroll");
		int style = SWT.BORDER;
		if (readonly) style |= SWT.READ_ONLY;
		if (multi) style |= SWT.MULTI;
		if (wrap) style |= SWT.WRAP;
		if (hscroll) style |= SWT.H_SCROLL;
		if (vscroll) style |= SWT.V_SCROLL;

		final Control text;
		text = new Text(parent, style);

		defineLayout(text, tag);
		{
			final String value = tag.getAttribute(ID_ATTRIBUTE_NAME);
			if (value != null) {
				components.put(value, text);
			}
		}
		{
			final String value = tag.getAttribute("text");
			if (value != null && text instanceof Text) {
				((Text) text).setText(value);
			}
		}
	}

	/**
	 * Defines Table component
	 * 
	 * <pre>
	 * <table id="javaName" fullselection="true" checkbox="true"/>
	 * </pre>
	 * 
	 * @param components
	 * @param parent
	 * @param tag
	 */
	private final static void createTable(Map components, Composite parent, XMLDOM tag) {
		int style = 0;

		if (tag.getBooleanAttribute("fullselection")) style |= SWT.FULL_SELECTION;
		if (tag.getBooleanAttribute("checkbox")) style |= SWT.CHECK;

		Table table = new Table(parent, style);
		defineLayout(table, tag);
		{
			final String value = tag.getAttribute(ID_ATTRIBUTE_NAME);
			if (value != null) {
				components.put(value, table);
			}
		}
	}

	/**
	 * Defines List component
	 * 
	 * <pre>
	 * <list id="javaName" multiselection="true"/>
	 * </pre>
	 * 
	 * @param components
	 * @param parent
	 * @param tag
	 */
	private final static void createList(Map components, Composite parent, XMLDOM tag) {
		int style = SWT.BORDER | SWT.V_SCROLL;
		final boolean readonly = tag.getBooleanAttribute("multiselection");
		if (readonly) style |= SWT.MULTI;

		List list = new List(parent, style);
		defineLayout(list, tag);
		{
			final String value = tag.getAttribute(ID_ATTRIBUTE_NAME);
			if (value != null) {
				components.put(value, list);
			}
		}
	}

	/**
	 * Defines Combo component
	 * 
	 * <pre>
	 * <combo id="javaName" readonly="true"/>
	 * </pre>
	 * 
	 * @param components
	 * @param parent
	 * @param tag
	 */
	private final static void createCombo(Map components, Composite parent, XMLDOM tag) {
		int style = SWT.BORDER;
		final boolean readonly = tag.getBooleanAttribute("readonly");
		if (readonly) style |= SWT.READ_ONLY;

		Combo combo = new Combo(parent, style);
		defineLayout(combo, tag);
		{
			final String value = tag.getAttribute(ID_ATTRIBUTE_NAME);
			if (value != null) {
				components.put(value, combo);
			}
		}
	}

	/**
	 * Defines Spinner component
	 * 
	 * <pre>
	 * <spinner id="javaName"/>
	 * </pre>
	 * 
	 * @param components
	 * @param parent
	 * @param tag
	 */
	private final static void createSpinner(Map components, Composite parent, XMLDOM tag) {
		Spinner spinner = new Spinner(parent, SWT.BORDER);
		defineLayout(spinner, tag);
		{
			final String value = tag.getAttribute(ID_ATTRIBUTE_NAME);
			if (value != null) {
				components.put(value, spinner);
			}
		}
	}

	/**
	 * Define container. Each container is a new Grid layout with defined
	 * columns
	 * 
	 * <pre>
	 * <container columns align valign width height colspan rowspan> more components inside </container>
	 * </pre>
	 * 
	 * @param components
	 * @param parent
	 * @param tag
	 */
	private final static void createContainer(Map components, Composite parent, XMLDOM tag) {
		Composite grid = new Composite(parent, 0);
		defineGrid(grid, tag);
		defineLayout(grid, tag);
		{
			final String value = tag.getAttribute(ID_ATTRIBUTE_NAME);
			if (value != null) {
				components.put(value, grid);
			}
		}
		createSubComponents(components, grid, tag);
	}

	// <splitter horizontal vertical/> --sub tags
	/**
	 * Defines Spinner component with predefined percentage ratio.
	 * 
	 * <pre>
	 * <splitter id="javaName" horizontal="30" / vertical="30" />
	 * </pre>
	 * 
	 * @param components
	 * @param parent
	 * @param tag
	 */
	private final static void createSplitter(Map components, Composite parent, XMLDOM tag) {
		final int horizontal = tag.getIntAttribute("horizontal", 0);
		final int vertical = tag.getIntAttribute("vertical", 0);

		int style;
		int size;
		if (vertical != 0) {
			style = SWT.VERTICAL;
			size = vertical;
		} else {
			style = SWT.HORIZONTAL;
			size = horizontal;
		}
		SashForm splitter = new SashForm(parent, style);
		splitter.setSashWidth(2);
		defineLayout(splitter, tag);
		{
			final String value = tag.getAttribute(ID_ATTRIBUTE_NAME);
			if (value != null) {
				components.put(value, splitter);
			}
		}

		createSubComponents(components, splitter, tag);

		if (size > 0 && size < 100) {
			splitter.setWeights(new int[] { size, 100 - size });
		} else {
			splitter.setWeights(new int[] { 50, 50 });
		}
	}

	/**
	 * Defines tabs wrapper for tabbed component.
	 * 
	 * <pre>
	 *   <tabs align valign width height colspan rowspan >
	 *      <tab name="tab name" columns > tab components </tab> 
	 *   </tabs>
	 * </pre>
	 * 
	 * @param components
	 * @param parent
	 * @param tag
	 */
	private final static void createTabs(Map components, Composite parent, XMLDOM tag) {
		TabFolder tabFolder = new TabFolder(parent, 0);
		defineLayout(tabFolder, tag);
		{
			final String id = tag.getAttribute(ID_ATTRIBUTE_NAME);
			if (id != null) {
				components.put(id, tabFolder);
			}
		}

		Iterator it = tag.getChildren().iterator();
		while (it.hasNext()) {
			final XMLDOM tab = (XMLDOM) it.next();
			final String tagName = tab.getName();
			if ("tab".equals(tagName)) {
				TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
				tabItem.setText(tab.getAttribute("name"));
				Composite grid = new Composite(tabFolder, SWT.NONE);
				defineGrid(grid, tab);
				defineLayout(grid, tab);
				createSubComponents(components, grid, tab);
				tabItem.setControl(grid);
				{
					final String id = tab.getAttribute(ID_ATTRIBUTE_NAME);
					if (id != null) {
						components.put(id, tabItem);
					}
				}
			}
		}
	}

	/**
	 * Defines DateTime component
	 * 
	 * <pre>
	 * <datetime id="javaName" type="calendar|date|time"/>
	 * </pre>
	 * 
	 * @param components
	 * @param parent
	 * @param tag
	 */
	private final static void createDateTime(Map components, Composite parent, XMLDOM tag) {
		final int style;
		final String type = tag.getStringAttribute("type");
		if ("date".equalsIgnoreCase(type)) {
			style = SWT.DATE;
		} else if ("time".equalsIgnoreCase(type)) {
			style = SWT.TIME;
		} else {
			style = SWT.CALENDAR;
		}

		DateTime dateTime = new DateTime(parent, style);
		defineLayout(dateTime, tag);
		{
			final String value = tag.getAttribute(ID_ATTRIBUTE_NAME);
			if (value != null) {
				components.put(value, dateTime);
			}
		}
	}

	public static final void packAvailableColums(Table table) {
		final TableColumn[] columns = table.getColumns();
		for (int i = 0; i < columns.length; i++) {
			columns[i].pack();
		}
	}

	/**
	 * Separate string by separator and skip empty tokens
	 * 
	 * @param string
	 * @param separator
	 * @return
	 */
	public static String[] split(final String string, final char separator) {
		if (string == null || string.length() <= 0) {
			return new String[0];
		}

		Vector tokens = new Vector();
		int i = 0, len = string.length(), s = 0;
		for (; i < len; i++) {
			final char c = string.charAt(i);
			if (c == separator) {
				if (i - s > 0) {
					final String addstr = string.substring(s, i).trim();
					if (addstr.length() > 0) tokens.addElement(addstr);
				}
				s = i + 1;
			}
		}
		// if there is remaining string - add it
		if (len - s > 0) {
			final String addstr = string.substring(s, i).trim();
			if (addstr.length() > 0) tokens.addElement(addstr);
		}

		final String[] result = new String[tokens.size()];
		tokens.copyInto(result);
		return result;
	}

	/**
	 * Separate string by separator by keeping empty tokens
	 * 
	 * @param string
	 * @param separator
	 * @return
	 */
	public static String[] tokenize(final String string, final char separator) {
		if (string == null || string.length() <= 0) {
			return new String[0];
		}

		Vector tokens = new Vector();
		int i = 0, len = string.length(), s = 0;
		for (; i < len; i++) {
			final char c = string.charAt(i);
			if (c == separator) {
				final String addstr = string.substring(s, i).trim();
				tokens.addElement(addstr);
				s = i + 1;
			}
		}
		final String addstr = string.substring(s, i).trim();
		tokens.addElement(addstr);

		final String[] result = new String[tokens.size()];
		tokens.copyInto(result);
		return result;
	}
}
