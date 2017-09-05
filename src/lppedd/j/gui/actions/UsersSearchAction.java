package lppedd.j.gui.actions;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Label;

import lppedd.j.api.JConnection;
import lppedd.j.api.JType;
import lppedd.j.api.commands.DSPPGMREF;
import lppedd.j.api.factories.JMemberFactory;
import lppedd.j.api.factories.JObjectFactory;
import lppedd.j.api.files.database.JDatabaseFile;
import lppedd.j.api.files.database.JSourcePhysicalFile;
import lppedd.j.api.members.JMember;
import lppedd.j.api.members.JNullMember;
import lppedd.j.api.misc.JUtil;
import lppedd.j.api.objects.JNullObject;
import lppedd.j.api.objects.JObject;
import lppedd.j.gui.Application;
import lppedd.j.gui.ewt.CompositeMapper;

/**
 * @author Edoardo Luppi
 */
public final class UsersSearchAction extends ProgressAction
{
   private static final String[] OBJECTS = {
         "QRPGLESRC",
         "QCLLESRC",
         "QDDSPF",
         "QDDSLF",
         "QSQLSRC"
   };
   private final JConnection connection = Application.getConnection();
   private final TableViewer _tableViewer;
   private final String _objectName;
   private final Set<String> _scanned;

   public UsersSearchAction(final TableViewer tableUsersViewer, final String objectName) {
      _tableViewer = tableUsersViewer;
      _objectName = objectName;
      _scanned = new HashSet<>(8192);
   }

   @Override
   protected void main() {
      // Controllo se l'oggetto esiste nel sistema.
      if (JObjectFactory.get(connection, _objectName, "*ALL", JType.ALL) instanceof JNullObject) {
         return;
      }
      
      updateText("Scansione...");

      // Reperisco gli utilizzatori.
      final Set<String> users = getObjectUsers(_objectName);

      if (users.size() < 1 || isInterrupted()) {
         return;
      }
      
      // Reperisco i membri dati i nomi degli utilizzatori.
      final Set<JMember> members = new HashSet<>(users.size());
      
      for (final String user : users) {
         final JMember member = JMemberFactory.get(connection, user, OBJECTS);

         if (!(member instanceof JNullMember)) {
            members.add(member);
         }
      }

      // Popolo la tabella.
      _tableViewer.getTable().getDisplay().asyncExec(new Runnable() {
         @Override
         public void run() {
            _tableViewer.setInput(members);
            
            final Label labelItemCount = (Label) CompositeMapper.get("compositeUsersSearch").get("labelItemCount");
            labelItemCount.setText("Utilizzatori: " + users.size());
            ((GridData) labelItemCount.getLayoutData()).exclude = false;
            
            _tableViewer.getTable().getParent().layout();
         }
      });
   }
   
   /**
    * Ritorna gli utilizzatori di un oggetto
    *
    * @param name
    *        Nome dell'oggetto
    */
   public Set<String> getObjectUsers(final String name) {
      final Set<String> users = new HashSet<>(64);

      {
         // Verifico se il nome dell'oggetto passato rappresenta un file.
         final JObject object = JObjectFactory.get(connection, name, "*LIBL", JType.ALL);

         if (object instanceof JDatabaseFile) {
            // Dato che e' un file uso anche il comando DSPDBR.
            updateText("L'oggetto e' un file. Trovo le relazioni di database");

            for (final JObject user : ((JDatabaseFile) object).getDatabaseRelations()) {
               users.add(user.getName());
            }
         }
      }

      if (isInterrupted()) {
         return Collections.EMPTY_SET;
      }

      final DSPPGMREF command = new DSPPGMREF();
      command.setProgram(DSPPGMREF.PROGRAM_ALL);
      command.getFilter().referencedObject = name;

      final String[] libraries = {
            "WRK90OBJ",
            "S90PTFOBJ",
            "S90OBJ"
      };

      // Lancio una serie di DSPPGMREF, per trovare le referenze tra oggetti
      for (final String library : libraries) {
         updateText("Trovo i riferimenti all'oggetto nella libreria " + library);

         if (isInterrupted()) {
            return Collections.EMPTY_SET;
         }

         command.setLibrary(library);
         
         for (final JObject user : command.execute(connection)) {
            users.add(user.getName());
         }
      }

      // Non bastano gli oggetti. Ad esempio, un file utilizzato per la definizione di una EXTDS
      // non viene rilevato. Quindi devo obbligatoriamente analizzare i membri sorgente
      libraries[2] = "S90SRC";

      for (final String librarie : libraries) {
         if (isInterrupted()) {
            return Collections.EMPTY_SET;
         }

         updateText("Scansiono i membri della libreria " + librarie);
         scanMembersForUse(name, librarie, users);
      }

      _scanned.clear();
      return users;
   }
   
   /**
    * Metodo di utilita' per {@link #getObjectUsers(String)}
    *
    * @param names
    * @param library
    * @param users
    */
   private void scanMembersForUse(final String name, final String library, final Set<String> users) {
      final String[] members = new JSourcePhysicalFile(connection, "QRPGLESRC", library).getMembers();

      for (final String memberName : members) {
         if (isInterrupted()) {
            return;
         }

         if (_scanned.contains(memberName)) {
            continue;
         }
         
         _scanned.add(memberName);

         if (users.contains(memberName)) {
            continue;
         }
         
         updateText(library + " - analisi di " + memberName);
         
         final JMember member = JMemberFactory.get(connection, memberName, "QRPGLESRC", library);
         member.loadSource();
         
         for (String line : member.getSource()) {
            line = line.toUpperCase();

            // Calculation specification always come after definition specifications,
            // so I can skip to the next source member
            if ((line.charAt(5) == 'C' || line.contains("/FREE")) && !JUtil.isComment(line)) {
               break;
            }

            // Not interested in anything that's not a definition specification
            if (line.charAt(5) != 'D' || JUtil.isComment(line)) {
               continue;
            }

            final String dsName = line.charAt(5) == 'D' && "E DS".equals(line.substring(21, 25)) ? line.substring(6, 16) : "";

            if (name.equals(dsName)) {
               users.add(memberName);
               break;
            }

            final int extNameIndex = line.indexOf("EXTNAME");

            if (extNameIndex < 1) {
               continue;
            }

            final int openParenthesisIndex = line.indexOf("(", extNameIndex);
            final int closedParenthesisIndex = line.indexOf(")", openParenthesisIndex);

            if (name.equals(line.substring(openParenthesisIndex + 1, closedParenthesisIndex).replace("'", ""))) {
               users.add(memberName);
               break;
            }
         }

         member.dispose();
      }
   }
}
