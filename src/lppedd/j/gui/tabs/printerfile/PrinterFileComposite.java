package lppedd.j.gui.tabs.printerfile;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;

import lppedd.j.gui.actions.PrinterFileAnalysisAction;
import lppedd.j.gui.contentproviders.RecordFormatContentProvider;
import lppedd.j.gui.ewt.CompositeMapper;
import lppedd.j.gui.models.RecordFormat;
import lppedd.j.gui.popups.progress.ProgressDialog;

/**
 * @author Edoardo Luppi
 */
public class PrinterFileComposite extends Composite
{
   private final Text textPrinterFile;
   private final Text textProgram;
   private final Tree tableRecordFormats;
   private final TreeColumn tableColumnRecordFormat;
   private final TreeColumn tableColumnDescription;
   private final TreeColumn tableColumnDataType;
   private final TreeColumn tableColumnUsed;
   private final TreeViewer tableRecordFormatsViewer;
   private final TreeViewerColumn tableColumnRecordFormatViewer;
   private final TreeViewerColumn tableColumnDescriptionViewer;
   private final TreeViewerColumn tableColumnDataTypeViewer;
   private final TreeViewerColumn tableColumnViewerUsed;
   private final TraverseListener traverseListener = new TraverseListener() {
      @Override
      public void keyTraversed(final TraverseEvent e) {
         if (SWT.TRAVERSE_RETURN == e.detail) {
            final String printerName = textPrinterFile.getText().trim();

            if (printerName.isEmpty()) {
               return;
            }

            final String programName = textProgram.getText().trim();

            if (programName.isEmpty()) {
               tableColumnUsed.setWidth(0);
               tableColumnUsed.setResizable(false);
            } else {
               tableColumnUsed.setWidth(70);
               tableColumnUsed.setResizable(true);
            }

            new ProgressDialog(getShell()).open(
                  new PrinterFileAnalysisAction(tableRecordFormatsViewer, printerName, programName),
                  SWT.INDETERMINATE);
         }
      }
   };

   /**
    * Creates the composite.
    *
    * @param parent
    *        The parent composite
    */
   public PrinterFileComposite(final Composite parent) {
      super(parent, SWT.NONE);
      setData(SWT.CID, "compositeUsersSearch");
      setLayout(new GridLayout(2, true));

      textPrinterFile = new Text(this, SWT.BORDER);
      textPrinterFile.setTextLimit(10);
      textPrinterFile.setTextCase(SWT.UPPERCASE);
      textPrinterFile.setTextType(SWT.ALPHANUMERIC);
      textPrinterFile.setFurtherAllowedCharacters('_');
      textPrinterFile.setMessage("Printer file");
      textPrinterFile.setToolTipText("Printer file");
      textPrinterFile.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
      textPrinterFile.addTraverseListener(traverseListener);

      textProgram = new Text(this, SWT.BORDER);
      textProgram.setTextLimit(10);
      textProgram.setTextCase(SWT.UPPERCASE);
      textProgram.setTextType(SWT.ALPHANUMERIC);
      textProgram.setFurtherAllowedCharacters('_', '*');
      textProgram.setMessage("Programma");
      textProgram.setToolTipText("Programma");
      textProgram.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
      textProgram.addTraverseListener(traverseListener);

      // Main table
      tableRecordFormatsViewer = new TreeViewer(this, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
      tableRecordFormatsViewer.setContentProvider(new RecordFormatContentProvider());

      tableRecordFormats = tableRecordFormatsViewer.getTree();
      tableRecordFormats.setLinesVisible(true);
      tableRecordFormats.setHeaderVisible(true);
      tableRecordFormats.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));

      // Object name column
      tableColumnRecordFormatViewer = new TreeViewerColumn(tableRecordFormatsViewer, SWT.NONE);
      tableColumnRecordFormatViewer.setLabelProvider(new ColumnLabelProvider() {
         @Override
         public String getText(final Object element) {
            return ((RecordFormat) element).recordFormat;
         }
      });

      tableColumnRecordFormat = tableColumnRecordFormatViewer.getColumn();
      tableColumnRecordFormat.setWidth(120);
      tableColumnRecordFormat.setText("Formato record");

      // Object type column
      tableColumnDataTypeViewer = new TreeViewerColumn(tableRecordFormatsViewer, SWT.NONE);
      tableColumnDataTypeViewer.setLabelProvider(new ColumnLabelProvider() {
         @Override
         public String getText(final Object element) {
            return ((RecordFormat) element).dataType;
         }
      });

      tableColumnDataType = tableColumnDataTypeViewer.getColumn();
      tableColumnDataType.setWidth(120);
      tableColumnDataType.setText("Tipo");

      // Object library column
      tableColumnDescriptionViewer = new TreeViewerColumn(tableRecordFormatsViewer, SWT.NONE);
      tableColumnDescriptionViewer.setLabelProvider(new ColumnLabelProvider() {
         @Override
         public String getText(final Object element) {
            return ((RecordFormat) element).description;
         }
      });

      tableColumnDescription = tableColumnDescriptionViewer.getColumn();
      tableColumnDescription.setWidth(160);
      tableColumnDescription.setText("Descrizione");

      tableColumnViewerUsed = new TreeViewerColumn(tableRecordFormatsViewer, SWT.NONE);
      tableColumnViewerUsed.setLabelProvider(new ColumnLabelProvider() {
         @Override
         public String getText(final Object element) {
            return ((RecordFormat) element).used;
         }
      });

      tableColumnUsed = tableColumnViewerUsed.getColumn();
      tableColumnUsed.setWidth(70);
      tableColumnUsed.setText("Utilizzato");

      CompositeMapper.map(this);
   }

   @Override
   protected void checkSubclass() {
      //
   }
}
