package lppedd.j.gui.popups.workitem;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import lppedd.j.gui.popups.workitem.WorkItemComposite.CopyOptions;
import lppedd.j.gui.utils.UIUtils;

/**
 * @author Edoardo Luppi
 */
public class WorkItemDialog extends Dialog
{
   private Shell dialogWorkItem;
   private WorkItemComposite compositeWorkItem;

   /**
    * Contains the work item number and text.
    */
   private CopyOptions options;

   /**
    * Create the dialog.
    *
    * @param parent
    *        The parent shell
    */
   public WorkItemDialog(final Shell parent) {
      super(parent, SWT.NONE);
   }
   
   /**
    * Open the dialog.
    *
    * @return the result
    */
   public CopyOptions open() {
      createContents();
      
      dialogWorkItem.layout();
      dialogWorkItem.pack();
      UIUtils.centerInDisplay(dialogWorkItem);
      dialogWorkItem.open();
      
      final Display display = getParent().getDisplay();

      while (!dialogWorkItem.isDisposed()) {
         if (!display.readAndDispatch()) {
            display.sleep();
         }
      }

      return options;
   }
   
   /**
    * Create contents of the dialog.
    */
   private void createContents() {
      dialogWorkItem = new Shell(getParent(), SWT.DIALOG_TRIM | SWT.RESIZE | SWT.APPLICATION_MODAL);
      dialogWorkItem.setMinimumSize(340, 290);
      dialogWorkItem.setText("Opzioni di copia");

      final FillLayout flDialogWorkItem = new FillLayout(SWT.HORIZONTAL);
      flDialogWorkItem.marginHeight = 5;
      flDialogWorkItem.marginWidth = 5;
      
      dialogWorkItem.setLayout(flDialogWorkItem);
      
      compositeWorkItem = new WorkItemComposite(dialogWorkItem, SWT.NONE);
      compositeWorkItem.getButtonConfirm().addSelectionListener(new SelectionAdapter() {
         @Override
         public void widgetSelected(final SelectionEvent e) {
            final String programLibrary = compositeWorkItem.getTextProgramsLibrary().getText().trim();
            final String filesLibrary = compositeWorkItem.getTextFilesLibrary().getText().trim();

            options = new CopyOptions();
            options.setIncludeWorkItem(compositeWorkItem.getButtonInclude().getSelection());

            if (options.includeWorkItem()) {
               options.setWorkItemNumber(Integer.parseInt(compositeWorkItem.getTextNumber().getText().trim()));
               options.setWorkItemText(compositeWorkItem.getTextDescription().getText().trim());
            }
            
            options.setProgramsLibrary(programLibrary);
            options.setFilesLibrary(filesLibrary);
            options.compile(compositeWorkItem.getButtonCompile().getSelection());

            dialogWorkItem.close();
         }
      });
   }
}
