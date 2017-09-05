package lppedd.j.gui.tabs.remote;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

import lppedd.j.api.JType;
import lppedd.j.api.objects.JLibrary;
import lppedd.j.api.objects.JNullObject;
import lppedd.j.api.objects.JObject;
import lppedd.j.gui.Application;

/**
 * @author Edoardo Luppi
 */
public class RemoteFilesComposite extends Composite
{
   private final Tree tree;
   private final TreeViewer treeViewer;
   private final TreeColumn treeColumnObject;
   private final TreeViewerColumn treeViewerColumnObject;
   private Button buttonOpen;

   /**
    * Creates the composite.
    *
    * @param parent
    *        The parent composite
    */
   public RemoteFilesComposite(final Composite parent) {
      super(parent, SWT.NONE);
      setLayout(new GridLayout(1, false));

      treeViewer = new TreeViewer(this, SWT.BORDER);
      tree = treeViewer.getTree();
      tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
      tree.addListener(SWT.Expand, new Listener() {
         @Override
         public void handleEvent(final Event event) {
            final TreeItem item = (TreeItem) event.item;
            final JObject object = (JObject) item.getData();

            if (!(object instanceof JLibrary)) {
               return;
            }

            for (final JObject file : ((JLibrary) object).getObjects(JType.FILE)) {
               if (file instanceof JNullObject || !"SAVF".equals(file.getAttribute())) {
                  continue;
               }

               final TreeItem childItem = new TreeItem(item, SWT.NONE);
               childItem.setData(file);
               childItem.setText(file.getName());
            }
         }
      });

      for (final JObject library : new JLibrary(Application.getConnection(), "QSYS").getObjects(JType.LIB)) {
         if (library instanceof JNullObject) {
            continue;
         }

         final TreeItem item = new TreeItem(tree, SWT.NONE);
         item.setData(library);
         item.setText(library.getName());
         item.setItemCount(1);
      }

      treeViewerColumnObject = new TreeViewerColumn(treeViewer, SWT.NONE);
      treeColumnObject = treeViewerColumnObject.getColumn();
      treeColumnObject.setWidth(300);
      treeColumnObject.setText("Oggetto");
      
      buttonOpen = new Button(this, SWT.NONE);
      buttonOpen.setText("Apri");
      
      final GridData gdButtonOpen = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
      gdButtonOpen.widthHint = 100;
      
      buttonOpen.setLayoutData(gdButtonOpen);
   }

   public Tree getTree() {
      return tree;
   }

   public Button getButtonOpen() {
      return buttonOpen;
   }

   @Override
   protected void checkSubclass() {
      //
   }
}
