package lppedd.j.gui.actions;

import java.util.List;

import org.eclipse.jface.viewers.TreeViewer;

import lppedd.j.api.JConnection;
import lppedd.j.api.JType;
import lppedd.j.api.factories.JMemberFactory;
import lppedd.j.api.factories.JObjectFactory;
import lppedd.j.api.files.JField;
import lppedd.j.api.files.JFile;
import lppedd.j.api.files.JRecordFormat;
import lppedd.j.api.members.JMember;
import lppedd.j.gui.Application;
import lppedd.j.gui.models.RecordFormat;

/**
 * @author Edoardo Luppi
 */
public final class PrinterFileAnalysisAction extends ProgressAction
{
   private final TreeViewer tableViewer;
   private final String printerFileName;
   private final String programName;

   public PrinterFileAnalysisAction(final TreeViewer tablePrinterFileViewer, final String printerFileName, final String programName) {
      tableViewer = tablePrinterFileViewer;
      this.printerFileName = printerFileName;
      this.programName = programName;
   }
   
   @Override
   protected void main() {
      // Controllo se il printer file ed il programma esistono nel sistema
      final JConnection connection = Application.getConnection();

      if (!connection.exists("*LIBL", printerFileName, JType.FILE, "")) {
         return;
      }

      updateText("Scansione...");

      final JFile printer = (JFile) JObjectFactory.get(connection, printerFileName, "*LIBL", JType.ALL);
      final JMember program = JMemberFactory.get(connection, programName, "QRPGLESRC");
      program.loadSource();

      final List<String> source = program.getSource();
      final List<JRecordFormat> recordFormats = printer.getRecordFormats();
      final int recordFormatsSize = recordFormats.size();
      final RecordFormat[] list = new RecordFormat[recordFormatsSize];

      for (int i = 0; i < recordFormatsSize; i++) {
         final JRecordFormat format = recordFormats.get(i);
         final String formatName = format.getName();
         boolean found = false;

         // Verifico se il formato e' utilizzato nel programma
         for (final String line : source) {
            if (line.contains(formatName + " ") || line.contains(formatName + "'")) {
               found = true;
               break;
            }
         }

         final List<JField> fields = format.getFields();
         final int fieldsSize = fields.size();
         final RecordFormat[] listFields = new RecordFormat[fieldsSize];

         for (int j = 0; j < fieldsSize; j++) {
            final JField field = fields.get(j);
            listFields[j] = new RecordFormat(field.getName(), field.getDescription(), field.getDataType().toString(), "", list[i], new RecordFormat[0]);
         }

         list[i] = new RecordFormat(format.getName(), format.getDescription(), "", found ? "Si" : "", null, listFields);
      }

      // Popolo la tabella.
      tableViewer.getTree().getDisplay().asyncExec(new Runnable() {
         @Override
         public void run() {
            tableViewer.setInput(list);
            tableViewer.getTree().getParent().layout();
         }
      });
   }
}
