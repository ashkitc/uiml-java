package demo;

import java.util.Calendar;
import java.util.Map;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;
import uiml.UIMLFactory;

/**
 * Demo class that demonstrates all UI interface components created via UIML
 */
public class DemoUIML extends SelectionAdapter {

	/**
	 * Contest need to be with public fields to give reflect access to them
	 */
	public static class Context {
		public DateTime dateTimeComponent;
		public Button buttonComponent;
		public Label labelComponent;
		public Text textComponent;

		public Table tableComponent;

		public Combo comboComponent;
		public Spinner spinnerComponent;
		public List listComponent;
	}

	private static String[] columnNames = { "column1", "column2", "column3" };

	/** create context that will last through the object lifetime */
	private final Context context = new Context();

	// Create interface
	public DemoUIML(Composite parent) {

		// generate interface and take the components as map that relates name
		// to component object
		Map componentsMap = UIMLFactory.createUI(parent, this.getClass());
		// maps components to context
		UIMLFactory.mapContext(componentsMap, context);

		// add listeners
		context.buttonComponent.addSelectionListener(this);

		context.labelComponent.setBackground(new Color(display, 0, 0, 0));
		context.labelComponent.setForeground(new Color(display, 255, 255, 255));

		{// define table columns
			for (int i = 0; i < columnNames.length; i++) {
				TableColumn column = new TableColumn(context.tableComponent, SWT.NONE);
				column.setText(columnNames[i]);
				if (i > 0) {
					column.setAlignment(SWT.RIGHT);
				}
			}
			context.tableComponent.setHeaderVisible(true);
			context.tableComponent.setLinesVisible(true);
			UIMLFactory.packAvailableColums(context.tableComponent);
		}

		// add lines to the combo
		context.comboComponent.add("line 1");
		context.comboComponent.add("line 2");
		context.comboComponent.add("line 3");

		// defines spinner precision
		context.spinnerComponent.setDigits(4);

		// add some list lines
		context.listComponent.add("row 1");
		context.listComponent.add("row 2");
		context.listComponent.add("row 3");
		context.listComponent.add("row 4");
	}

	/**
	 * handles all widget selections
	 */
	public void widgetSelected(SelectionEvent e) {
		final Widget widget = e.widget;
		if (widget == context.buttonComponent) {
			Calendar calendar = Calendar.getInstance();
			calendar.set(Calendar.YEAR, context.dateTimeComponent.getYear());
			calendar.set(Calendar.MONTH, context.dateTimeComponent.getMonth());
			calendar.set(Calendar.DAY_OF_MONTH, context.dateTimeComponent.getDay());
			calendar.set(Calendar.HOUR_OF_DAY, 0);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);

			context.textComponent.setText(String.valueOf(calendar.getTime().getTime()));
		}
	}

	public static final Display display = new Display();
	public static final Shell shell = new Shell(display);

	public static void main(String[] args) {
		shell.setText("PBM");
		new DemoUIML(shell);

		shell.pack();
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) display.sleep();
		}
		display.dispose();
	}
}
