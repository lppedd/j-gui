package lppedd.j.gui.tabs.savefile;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;

import lppedd.j.api.JType;
import lppedd.j.api.files.device.JSaveFile.JSavedObject;
import lppedd.j.gui.actions.LocalSaveFileAnalysisAction;
import lppedd.j.gui.actions.RemoteSaveFileAnalysisAction;
import lppedd.j.gui.comparators.JSavedObjectComparator;
import lppedd.j.gui.contentproviders.SavedObjectContentProvider;
import lppedd.j.gui.popups.progress.ProgressDialog;
import lppedd.j.gui.tabs.remote.RemoteFilesDialog;

/**
 * @author Edoardo Luppi
 */
public class SaveFileComposite extends Composite
{
   private final Text textFilePath;
   private final Button buttonLocalFile;
   private final Tree treeSavedObjects;
   private final TreeViewer treeSavedObjectsViewer;
   private final TreeColumn treeColumnObject;
   private final TreeViewerColumn treeColumnObjectViewer;
   private final TreeColumn treeColumnDescription;
   private final TreeViewerColumn treeColumnDescriptionViewer;
   private final TreeColumn treeColumnType;
   private final TreeViewerColumn treeColumnTypeViewer;
   private final JSavedObjectComparator treeComparator = new JSavedObjectComparator();
   private DropTarget dropTarget;
   private Button buttonRemoteFile;
   
   /**
    * Creates the composite.
    *
    * @param parent
    *        The parent composite
    */
   public SaveFileComposite(final Composite parent, final int progressBarStyle) {
      super(parent, SWT.NONE);
      setLayout(new GridLayout(3, false));
      
      textFilePath = new Text(this, SWT.BORDER);
      textFilePath.setEditable(false);
      textFilePath.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
      textFilePath.setBackground(textFilePath.getDisplay().getSystemColor(SWT.COLOR_WHITE));
      
      buttonLocalFile = new Button(this, SWT.NONE);
      buttonLocalFile.setText("File locale");
      buttonLocalFile.addSelectionListener(new SelectionAdapter() {
         @Override
         public void widgetSelected(final SelectionEvent e) {
            final FileDialog fileDialog = new FileDialog(getShell());
            fileDialog.setFilterExtensions(new String[] {
                  "*.savf"
            });
            fileDialog.setFilterNames(new String[] {
                  "Save File"
            });
            
            final String firstFilePath = fileDialog.open();
            
            if (firstFilePath == null) {
               return;
            }
            
            textFilePath.setText(firstFilePath);

            new ProgressDialog(getShell()).open(
                  new LocalSaveFileAnalysisAction(treeSavedObjectsViewer, firstFilePath),
                  SWT.NONE);
         }
      });
      
      final GridData gdButtonLocalFile = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
      gdButtonLocalFile.heightHint = 23;
      gdButtonLocalFile.widthHint = 100;
      
      buttonLocalFile.setLayoutData(gdButtonLocalFile);
      
      buttonRemoteFile = new Button(this, SWT.NONE);
      buttonRemoteFile.setText("File remoto");
      buttonRemoteFile.addSelectionListener(new SelectionAdapter() {
         @Override
         public void widgetSelected(final SelectionEvent e) {
            new ProgressDialog(getShell()).open(
                  new RemoteSaveFileAnalysisAction(
                        treeSavedObjectsViewer,
                        new RemoteFilesDialog(getShell()).open()),
                  SWT.INDETERMINATE);
         }
      });
      
      final GridData gdButtonRemoteFile = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
      gdButtonRemoteFile.widthHint = 100;
      
      buttonRemoteFile.setLayoutData(gdButtonRemoteFile);
      
      treeSavedObjectsViewer = new TreeViewer(this, SWT.BORDER);
      treeSavedObjectsViewer.setContentProvider(new SavedObjectContentProvider());
      treeSavedObjectsViewer.setComparator(treeComparator);
      
      treeSavedObjects = treeSavedObjectsViewer.getTree();
      treeSavedObjects.setLinesVisible(true);
      treeSavedObjects.setHeaderVisible(true);
      treeSavedObjects.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
      
      treeColumnObjectViewer = new TreeViewerColumn(treeSavedObjectsViewer, SWT.NONE);
      treeColumnObjectViewer.setLabelProvider(new ColumnLabelProvider() {
         @Override
         public String getText(final Object element) {
            if (element instanceof JSavedObject) {
               return ((JSavedObject) element).getName();
            }
            
            return (String) element;
         }
      });
      
      treeColumnObject = treeColumnObjectViewer.getColumn();
      treeColumnObject.setWidth(140);
      treeColumnObject.setText("Oggetto");
      treeColumnObject.addSelectionListener(new SelectionAdapter() {
         @Override
         public void widgetSelected(final SelectionEvent e) {
            treeComparator.setSortedColumn(0);
            treeSavedObjects.setSortColumn(treeColumnObject);
            treeSavedObjects.setSortDirection(treeComparator.getSortDirection());
            treeSavedObjectsViewer.refresh();
         }
      });
      
      treeSavedObjects.setSortColumn(treeColumnObject);
      treeSavedObjects.setSortDirection(treeComparator.getSortDirection());
      treeComparator.setSortedColumn(0);
      
      treeColumnTypeViewer = new TreeViewerColumn(treeSavedObjectsViewer, SWT.NONE);
      treeColumnTypeViewer.setLabelProvider(new ColumnLabelProvider() {
         @Override
         public String getText(final Object element) {
            if (element instanceof JSavedObject) {
               return ((JSavedObject) element).getType().getObjectType();
            }
            
            return JType.MBR.getAttribute();
         }
      });
      
      treeColumnType = treeColumnTypeViewer.getColumn();
      treeColumnType.setWidth(70);
      treeColumnType.setText("Tipo");
      treeColumnType.addSelectionListener(new SelectionAdapter() {
         @Override
         public void widgetSelected(final SelectionEvent e) {
            treeComparator.setSortedColumn(1);
            treeSavedObjects.setSortColumn(treeColumnType);
            treeSavedObjects.setSortDirection(treeComparator.getSortDirection());
            treeSavedObjectsViewer.refresh();
         }
      });
      
      treeColumnDescriptionViewer = new TreeViewerColumn(treeSavedObjectsViewer, SWT.NONE);
      treeColumnDescriptionViewer.setLabelProvider(new ColumnLabelProvider() {
         @Override
         public String getText(final Object element) {
            if (element instanceof JSavedObject) {
               return ((JSavedObject) element).getText();
            }
            
            return "";
         }
      });
      
      treeColumnDescription = treeColumnDescriptionViewer.getColumn();
      treeColumnDescription.setWidth(230);
      treeColumnDescription.setText("Descrizione");
      treeColumnDescription.addSelectionListener(new SelectionAdapter() {
         @Override
         public void widgetSelected(final SelectionEvent e) {
            treeComparator.setSortedColumn(2);
            treeSavedObjects.setSortColumn(treeColumnDescription);
            treeSavedObjects.setSortDirection(treeComparator.getSortDirection());
            treeSavedObjectsViewer.refresh();
         }
      });
      
      final FileTransfer fileTransfer = FileTransfer.getInstance();
      
      dropTarget = new DropTarget(treeSavedObjects, DND.DROP_MOVE);
      dropTarget.setTransfer(new Transfer[] {
            fileTransfer
      });
      dropTarget.addDropListener(new DropTargetAdapter() {
         @Override
         public void drop(final DropTargetEvent event) {
            if (!fileTransfer.isSupportedType(event.currentDataType)) {
               return;
            }
            
            final String[] fileList = (String[]) event.data;
            
            if (fileList.length > 1) {
               return;
            }
            
            final String filePath = fileList[0];
            textFilePath.setText(filePath);
            
            new ProgressDialog(getShell()).open(
                  new LocalSaveFileAnalysisAction(treeSavedObjectsViewer, filePath),
                  SWT.NONE);
         }
      });
   }
   
   @Override
   protected void checkSubclass() {
      //
   }
}
