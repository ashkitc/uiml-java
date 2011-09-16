package demo;

import java.util.Calendar;
import java.util.Map;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;
import uiml.UIMLFactory;

public class SimpleUIML extends SelectionAdapter {
	public Label label;
	public DateTime dateTime;
	public Button button;

	// Create interface
	public SimpleUIML(Composite parent) {

		// generate interface and take the components as map that relates name
		// to component object
		Map componentsMap = UIMLFactory.createUI(parent, this.getClass());

		// maps components to context
		UIMLFactory.mapContext(componentsMap, this);

		// add listeners
		button.addSelectionListener(this);
	}

	/**
	 * handles all widget selections
	 */
	public void widgetSelected(SelectionEvent e) {
		final Widget widget = e.widget;
		if (widget == button) {
			Calendar calendar = Calendar.getInstance();
			calendar.set(Calendar.YEAR, dateTime.getYear());
			calendar.set(Calendar.MONTH, dateTime.getMonth());
			calendar.set(Calendar.DAY_OF_MONTH, dateTime.getDay());
			calendar.set(Calendar.HOUR_OF_DAY, 0);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);

			MessageBox messageBox = new MessageBox(shell, SWT.ICON_INFORMATION | SWT.OK);
			messageBox.setText("Warning");
			messageBox.setMessage(String.valueOf(calendar.getTime().getTime()));
			messageBox.open();
		}
	}

	public static final Display display = new Display();
	public static final Shell shell = new Shell(display);

	public static void main(String[] args) {
		shell.setText("SimpleUIML");
		new SimpleUIML(shell);

		shell.pack();
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) display.sleep();
		}
		display.dispose();
	}
}