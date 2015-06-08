## User Interface Markup Language for Java using SWT ##
This project was created as a small framework for quick user interface generation. Its initial implementation was related to ECLIPSE FormToolkit and supported also sections.
Current implementation is only based on SWT widgets but can quickly be updated to FormToolkit.

## Code Example ##
Lets imagine we need to create a Zodiac calculator interface with a simple label, date picker and a button. We need to define the following uiml:

### demo uiml (SimpleUIML.xml) ###
```
<layout columns="3" width="300" align="FILL" valign="FILL">
    <label id="label" text="date:" align="BEGINING" valign="CENTER"/>
    <datetime id="dateTime" type="date" 
              align="BEGINING" valign="CENTER" height="20"/>
    <button id="button" text="Zodiac" height="20" align="BEGINING" valign="CENTER"/>
</layout>
```

### demo code (SimpleUIML.java) ###
In the java class to create the interface we need to call UIMLFactory.createUI with parent component where interface will be rendered and the class instance from where the package and class name will define the UIML file.
If we want to extract all components that UIMLFactory generated in the result Map, we need to call UIMLFactory.mapContext and to give class instance with public members that has the same type if the interface components.
```
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
```

## Widgets markups ##
All supported markup widgets with attributes that are implemented and could be defined through UI xml.


### Layout attributes ###
Each widget supports the following layout parameters
```
<xxxxxx colspan="2" rowspan="3" width="100" height="20" 
        align="BEGINING|CENTER|END|FILL" 
        valign="BEGIN|CENTER|END|FILL"/>
```


### Label ###
Defines text label component
```
<label id="javaName" 
       text="label text" 
       [layout attributes] />
```


### Text ###
Defines text component
```
<text id="javaName" 
      text="labelText" 
      readonly="true" 
      multi="true" 
      wrap="true" 
      hscroll="true" 
      vscroll="true" 
      [layout attributes] />
```


### Button ###
Defines Button component
```
<button id="javaName" 
        text="buttonText" 
        check="true" 
        default="true"
        [layout attributes] />
```


### DateTime ###
Defines DateTime component
```
<datetime id="javaName" 
          type="calendar|date|time"/>
          [layout attributes] />
```


### Combo ###
Defines Combo dropdown component
```
<combo id="javaName" 
       readonly="true"
       [layout attributes] />
```


### Spinner ###
Defines Spinner component
```
<spinner id="javaName"
         [layout attributes] />
```


### List ###
Defines List component
```
<list id="javaName" 
      multiselection="true"
      [layout attributes] />
```


### Table ###
Defines Table component
```
<table id="javaName" 
       fullselection="true" 
       checkbox="true"
       [layout attributes] />
```


### Container ###
Define container. Each container is a new Grid layout with defined columns. It is suitable for embedding components.
```
<container columns=""
           [layout attributes]>
           more components inside 
</container>
```


### Splitter ###
Defines Splitter component with predefined percentage ratio.
```
<splitter id="javaName" 
          [ horizontal="30" or vertical="40" ] 
          [layout attributes] >
          // two components inside or two containers inside
</container>
```


### Tabs ###
Defines tabs wrapper for tabbed component.
```
<tabs [layout attributes] >
      <tab name="tab name" columns="1" > 
           // tab components 
      </tab> 
      ...
</tabs>
```


