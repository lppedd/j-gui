package lppedd.j.gui.popups.workitem;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;

/**
 * @author Edoardo Luppi
 */
public class WorkItemComposite extends Composite
{
   public static class CopyOptions
   {
      private boolean _includeWorkItem;
      private int _workItemNumber;
      private String _workItemText = "";
      
      private String _programsLibrary = "";
      private String _filesLibrary = "";
      private boolean _compile;
      
      public boolean includeWorkItem() {
         return _includeWorkItem;
      }

      public int getWorkItemNumber() {
         return _workItemNumber;
      }

      public String getWorkItemText() {
         return _workItemText;
      }

      public String getProgramsLibrary() {
         return _programsLibrary;
      }

      public String getFilesLibrary() {
         return _filesLibrary;
      }
      
      public boolean getCompile() {
         return _compile;
      }

      public void setIncludeWorkItem(final boolean includeWorkItem) {
         _includeWorkItem = includeWorkItem;
      }

      public void setWorkItemNumber(final int workItemNumber) {
         _workItemNumber = workItemNumber;
      }
      
      public void setWorkItemText(final String workItemText) {
         _workItemText = workItemText;
      }

      public void setProgramsLibrary(final String programsLibrary) {
         _programsLibrary = programsLibrary;
      }
      
      public void setFilesLibrary(final String filesLibrary) {
         _filesLibrary = filesLibrary;
      }
      
      public void compile(final boolean compile) {
         _compile = compile;
      }
   }
   
   private final Group _groupWi;
   private final Group _groupDestination;
   private final Text _textNumber;
   private final Text _textDescription;
   private final Button _buttonInclude;
   private final Button _buttonConfirm;
   private final Button _buttonCompile;
   private final Text _textLibraryPrograms;
   private final Text _textLibraryFiles;
   
   /**
    * Create the composite.
    *
    * @param parent
    * @param style
    */
   public WorkItemComposite(final Composite parent, final int style) {
      super(parent, style);
      setLayout(new GridLayout(1, false));
      
      _groupDestination = new Group(this, SWT.NONE);
      _groupDestination.setText("Destinazione membri");
      
      final GridData gd__groupDestination = new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1);
      gd__groupDestination.widthHint = 300;
      
      _groupDestination.setLayoutData(gd__groupDestination);
      _groupDestination.setLayout(new GridLayout(1, false));
      
      _textLibraryPrograms = new Text(_groupDestination, SWT.BORDER);
      _textLibraryPrograms.setToolTipText("Libreria programmi");
      _textLibraryPrograms.setTextLimit(10);
      _textLibraryPrograms.setMessage("Libreria programmi");
      _textLibraryPrograms.setTextType(SWT.ALPHANUMERIC);
      _textLibraryPrograms.setTextCase(SWT.UPPERCASE);
      _textLibraryPrograms.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
      
      _textLibraryFiles = new Text(_groupDestination, SWT.BORDER);
      _textLibraryFiles.setToolTipText("Libreria file");
      _textLibraryFiles.setTextLimit(10);
      _textLibraryFiles.setMessage("Libreria file");
      _textLibraryFiles.setTextType(SWT.ALPHANUMERIC);
      _textLibraryFiles.setTextCase(SWT.UPPERCASE);
      _textLibraryFiles.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
      
      _buttonCompile = new Button(_groupDestination, SWT.CHECK);
      _buttonCompile.setText("Compila");

      _groupWi = new Group(this, SWT.NONE);
      
      final GridData gd__groupWi = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
      gd__groupWi.widthHint = 300;
      
      _groupWi.setLayoutData(gd__groupWi);
      _groupWi.setText("Work Item");
      _groupWi.setLayout(new GridLayout(1, false));

      _buttonInclude = new Button(_groupWi, SWT.CHECK);
      _buttonInclude.setText("Includi");
      _buttonInclude.addSelectionListener(new SelectionAdapter() {
         @Override
         public void widgetSelected(final SelectionEvent e) {
            if (_buttonInclude.getSelection()) {
               _textNumber.setEnabled(true);
               _textDescription.setEnabled(true);
            } else {
               _textNumber.setEnabled(false);
               _textDescription.setEnabled(false);
            }
         }
      });

      _textNumber = new Text(_groupWi, SWT.BORDER);
      _textNumber.setEnabled(false);
      _textNumber.setTextLimit(6);
      _textNumber.setMessage("Numero");
      _textNumber.setToolTipText("Numero");
      _textNumber.setTextType(SWT.NUMERIC);
      _textNumber.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

      _textDescription = new Text(_groupWi, SWT.BORDER);
      _textDescription.setEnabled(false);
      _textDescription.setTextLimit(80);
      _textDescription.setMessage("Descrizione");
      _textDescription.setToolTipText("Descrizione");
      _textDescription.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
      
      _buttonConfirm = new Button(this, SWT.NONE);
      _buttonConfirm.setText("Conferma");

      final GridData gd__buttonConfirm = new GridData(SWT.RIGHT, SWT.TOP, false, true, 1, 1);
      gd__buttonConfirm.widthHint = 100;

      _buttonConfirm.setLayoutData(gd__buttonConfirm);
   }

   public Button getButtonInclude() {
      return _buttonInclude;
   }

   public Text getTextNumber() {
      return _textNumber;
   }
   
   public Text getTextDescription() {
      return _textDescription;
   }

   public Button getButtonConfirm() {
      return _buttonConfirm;
   }
   
   public Text getTextProgramsLibrary() {
      return _textLibraryPrograms;
   }

   public Text getTextFilesLibrary() {
      return _textLibraryFiles;
   }

   public Button getButtonCompile() {
      return _buttonCompile;
   }
}
