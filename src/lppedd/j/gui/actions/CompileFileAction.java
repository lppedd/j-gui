package lppedd.j.gui.actions;

import java.util.Collections;
import java.util.List;

import lppedd.j.api.JConnection;
import lppedd.j.api.JType;
import lppedd.j.api.factories.JMemberFactory;
import lppedd.j.api.factories.JObjectFactory;
import lppedd.j.api.files.database.JDatabaseFile;
import lppedd.j.api.files.database.JPhysicalFile;
import lppedd.j.api.members.JMember;
import lppedd.j.api.members.JNullMember;
import lppedd.j.api.misc.JUtil;
import lppedd.j.api.misc.Pair;
import lppedd.j.api.objects.JNullObject;
import lppedd.j.api.objects.JObject;
import lppedd.j.gui.Application;

/**
 * @author Edoardo Luppi
 */
public class CompileFileAction extends ProgressAction
{
   private static final String[] OBJECTS = {
         "QDDSPF",
         "QDDSLF",
         "QSQLSRC"
   };

   private final String sourceMember;
   private final String sourceObject;
   private final String sourceLibrary;
   private final boolean copyData;
   private final List<Pair<String, String>> libraries;

   public CompileFileAction(
         final String sourceMember,
         final String sourceObject,
         final String sourceLibrary,
         final boolean copyData,
         final List<Pair<String, String>> libraries) {
      this.sourceMember = sourceMember;
      this.sourceObject = sourceObject;
      this.sourceLibrary = sourceLibrary;
      this.copyData = copyData;
      this.libraries = libraries;
   }
   
   @Override
   protected void main() {
      updateTotal(libraries.size() + 1);
      updateText("Reperimento sorgente");
      
      final JConnection connection = Application.getConnection();
      final JMember fileMember = JMemberFactory.get(connection, sourceMember, sourceObject, sourceLibrary);
      
      if (fileMember instanceof JNullMember) {
         return;
      }

      int i = 0;
      updateProgress(++i);

      // Genero il nome di backup
      final String backup = JUtil.getRandomString(10);

      for (final Pair<String, String> library : libraries) {
         updateText("Esecuzione in " + library.first);
         
         List<JObject> relations = Collections.EMPTY_LIST;
         JObject file = JObjectFactory.get(connection, sourceMember, library.first, JType.FILE);

         if (!(file instanceof JNullObject)) {
            // Il file esiste in work, quindi, dopo everne preso nota, cancello le sue relazioni di database
            // e lo rinomino.
            relations = ((JDatabaseFile) file).getDatabaseRelations();

            for (final JObject object : relations) {
               if (library.first.equals(object.getLibrary())) {
                  object.delete();
               }
            }

            file.setName(backup);
            file.persist();
         } else {
            if (!library.second.isEmpty()) {
               file = JObjectFactory.get(connection, sourceMember, library.second, JType.FILE);
            }
         }

         if (fileMember.compile(library.first)) {
            // Ricreo le sue relazioni di database.
            for (final JObject object : relations) {
               JMemberFactory.get(connection, object.getName(), OBJECTS).compile(library.first);
            }

            // Se il file e' fisico, copio eventuali dati dal file di backup
            if (file instanceof JPhysicalFile && copyData) {
               ((JPhysicalFile) file).copyFile((JPhysicalFile) JObjectFactory.get(connection, sourceMember, library.first, JType.FILE));
            }
         }

         updateProgress(++i);
      }
   }
}
