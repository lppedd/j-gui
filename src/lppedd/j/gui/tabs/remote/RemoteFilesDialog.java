package lppedd.j.gui.tabs.remote;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TreeItem;

import lppedd.j.api.objects.JObject;
import lppedd.j.gui.utils.UIUtils;

/**
 * @author Edoardo
 */
public class RemoteFilesDialog extends Dialog
{
   private JObject result;
   private Shell dialogRemoteFile;
   private RemoteFilesComposite compositeRemoteFiles;
   
   /**
    * Creates the dialog.
    *
    * @param parent
    *        The parent shell
    */
   public RemoteFilesDialog(final Shell parent) {
      super(parent, SWT.NONE);
   }
   
   /**
    * Opens the dialog.
    *
    * @return the result
    */
   public JObject open() {
      createContents();

      dialogRemoteFile.layout();
      dialogRemoteFile.pack();
      UIUtils.centerInDisplay(dialogRemoteFile);
      dialogRemoteFile.open();
      
      final Display display = getParent().getDisplay();
      
      while (!dialogRemoteFile.isDisposed()) {
         if (!display.readAndDispatch()) {
            display.sleep();
         }
      }

      return result;
   }
   
   /**
    * Create contents of the dialog.
    */
   private void createContents() {
      dialogRemoteFile = new Shell(getParent(), SWT.DIALOG_TRIM | SWT.RESIZE | SWT.APPLICATION_MODAL);
      dialogRemoteFile.setText("File remoto");
      dialogRemoteFile.setLayout(new FillLayout(SWT.HORIZONTAL));

      compositeRemoteFiles = new RemoteFilesComposite(dialogRemoteFile);

      final Button buttonOpen = compositeRemoteFiles.getButtonOpen();
      buttonOpen.addSelectionListener(new SelectionAdapter() {
         @Override
         public void widgetSelected(final SelectionEvent e) {
            final TreeItem[] selectedItems = compositeRemoteFiles.getTree().getSelection();
            
            if (selectedItems.length == 0) {
               return;
            }
            
            result = (JObject) selectedItems[0].getData();
            dialogRemoteFile.close();
         }
      });
      
      dialogRemoteFile.setSize(450, 300);
      dialogRemoteFile.setDefaultButton(buttonOpen);
   }
}
