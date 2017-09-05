package lppedd.j.gui.actions;

import org.eclipse.jface.viewers.TableViewer;

import lppedd.j.api.JConnection;
import lppedd.j.api.factories.JMemberFactory;
import lppedd.j.api.members.JMember;
import lppedd.j.gui.Application;
import lppedd.j.gui.popups.workitem.WorkItemComposite.CopyOptions;

/**
 * @author Edoardo Luppi
 */
public class UsersCopyAction extends ProgressAction
{
   private final TableViewer tableUsersViewer;
   private final CopyOptions copyOptions;

   public UsersCopyAction(final TableViewer tableUsersViewer, final CopyOptions copyOptions) {
      this.tableUsersViewer = tableUsersViewer;
      this.copyOptions = copyOptions;
   }

   @Override
   protected void main() {
      if (copyOptions == null || copyOptions.getProgramsLibrary().isEmpty() || copyOptions.getFilesLibrary().isEmpty()) {
         return;
      }
      
      final JConnection connection = Application.getConnection();
      
      tableUsersViewer.getTable().getDisplay().syncExec(new Runnable() {
         @Override
         public void run() {
            final Object[] selectedObjects = tableUsersViewer.getStructuredSelection().toArray();
            int i = 0;
            
            updateTotal(selectedObjects.length);
            updateProgress(i = 0);
            
            for (final Object object : selectedObjects) {
               JMember member = (JMember) object;
               updateText("Membro " + member.getName());
               
               final String memberAttribute = member.getAttribute();
               
               // I membri CLLE non necessitano di ricompilazione, quindi posso passare al successivo.
               if ("CLLE".equals(memberAttribute)) {
                  continue;
               }
               
               final boolean program = "PF".equals(memberAttribute) || "LF".equals(memberAttribute) || "SQL".equals(memberAttribute);
               String targetLibrary = program ? copyOptions.getProgramsLibrary() : copyOptions.getFilesLibrary();
               
               if (targetLibrary.isEmpty()) {
                  targetLibrary = member.getLibrary();
               }
               
               updateText("Membro " + member.getName() + " - copia");
               
               // Non posso toccare i membri nelle librerie PTF. Al massimo posso compilare, se il profilo ha l'autorizzazione.
               if (!("S90SRC".equals(targetLibrary) || "S90PTFOBJ".equals(targetLibrary))) {
                  if (!member.copy(targetLibrary, member.getObject(), member.getName(), false)) {
                     continue;
                  }

                  member = JMemberFactory.get(connection, member.getName(), member.getObject(), targetLibrary);

                  if (copyOptions.includeWorkItem()) {
                     updateText("Membro " + member.getName() + " - inserimento work item");
                     member.loadSource();
                     member.putWorkItem(
                           copyOptions.getWorkItemNumber(),
                           connection.getAs400().getUserId().substring(3),
                           0,
                           copyOptions.getWorkItemText(),
                           true);
                     member.persist();
                  }
               }
               
               if (copyOptions.getCompile()) {
                  updateText("Membro " + member.getName() + " - compilazione");
                  member.compile(targetLibrary);
               }
               
               updateProgress(i++);
            }
         }
      });
   }
}
