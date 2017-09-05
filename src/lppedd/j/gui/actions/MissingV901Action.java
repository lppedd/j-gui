package lppedd.j.gui.actions;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.jface.viewers.TableViewer;

import lppedd.j.api.JConnection;
import lppedd.j.api.JType;
import lppedd.j.api.factories.JMemberFactory;
import lppedd.j.api.factories.JObjectFactory;
import lppedd.j.api.files.database.JDatabaseFile;
import lppedd.j.api.files.database.JSourcePhysicalFile;
import lppedd.j.api.members.JMember;
import lppedd.j.api.members.JProgramMember;
import lppedd.j.api.misc.JUtil;
import lppedd.j.api.objects.JNullObject;
import lppedd.j.api.objects.JObject;
import lppedd.j.gui.Application;
import lppedd.j.gui.models.V901;
import smi.workitem.SmiAbstractWorkItem;

/**
 * @author Edoardo Luppi
 */
public final class MissingV901Action extends ProgressAction
{
   private static final int TYPE_PHYSICAL = 1;
   private static final int TYPE_LOGICAL = 2;
   private static final int TYPE_SQL = 3;
   private static final int TYPE_PROCEDURE = 4;
   private static final String[] SOURCE_FILES = {
         "QDDSPF",
         "QDDSLF",
         "QSQLSRC"
   };
   
   private final TableViewer tableViewer;
   private final Map<Integer, List<String>> physicals;
   private final Map<Integer, List<String>> logicals;
   private final Set<String> procedures;
   private final Set<String> chgpf;
   private final Map<Integer, String> workItems;
   
   public MissingV901Action(final TableViewer tableViewer) {
      this.tableViewer = tableViewer;
      physicals = new HashMap<>(16);
      logicals = new HashMap<>(16);
      procedures = new HashSet<>(8);
      chgpf = new HashSet<>(8);
      workItems = new HashMap<>(32);
   }
   
   @Override
   protected void main() {
      final JConnection connection = Application.getConnection();
      JSourcePhysicalFile sourceFile = null;
      
      for (final String sourceFileName : SOURCE_FILES) {
         if (isInterrupted()) {
            return;
         }

         sourceFile = new JSourcePhysicalFile(connection, sourceFileName, "WRK90PRO");

         switch (sourceFileName) {
            case "QDDSPF":
               scanPhysicalFile(sourceFile, TYPE_PHYSICAL);
               break;
            case "QDDSLF":
               scanPhysicalFile(sourceFile, TYPE_LOGICAL);
               break;
            case "QSQLSRC":
               scanPhysicalFile(sourceFile, TYPE_SQL);
               break;
            default:
               break;
         }
      }
      
      final List<V901> rows = new ArrayList<>(30);
      
      // Check if, for every physical file, every related file has a V901
      for (final Map.Entry<Integer, List<String>> entry : physicals.entrySet()) {
         if (isInterrupted()) {
            return;
         }
         
         final Integer workItemNumber = entry.getKey();
         final List<String> physicalsNames = entry.getValue();
         final List<String> logicalsNames = logicals.get(workItemNumber);
         
         final int physicalsSize = physicalsNames.size();
         updateTotal(physicalsSize);
         
         for (int i = 0; i < physicalsSize; i++) {
            final String fileName = physicalsNames.get(i);
            
            updateText("Analisi dei file collegati a " + fileName);
            updateProgress(i + 1);
            
            final JObject file = JObjectFactory.get(connection, fileName, "*LIBL", JType.FILE);
            
            if (file instanceof JNullObject) {
               continue;
            }

            for (final JObject lfEntry : ((JDatabaseFile) file).getDatabaseRelations()) {
               if (!chgpf.contains(fileName) && (logicalsNames == null || !logicalsNames.contains(lfEntry.getName()))) {
                  rows.add(new V901(lfEntry.getName(), String.valueOf(workItemNumber), workItems.get(workItemNumber)));
               }
            }
         }
      }
      
      // Join the two maps. It's easier for the upcoming part
      for (final Entry<Integer, List<String>> entry : logicals.entrySet()) {
         final Integer workItemNumber = entry.getKey();
         final List<String> logicalsNames = entry.getValue();
         final List<String> physicalsNames = physicals.get(workItemNumber);

         if (physicalsNames != null) {
            physicalsNames.addAll(logicalsNames);
         } else {
            physicals.put(workItemNumber, logicalsNames);
         }
      }
      
      final Connection dbConnection = connection.getConnection();
      final List<String> correspondences = new ArrayList<>(8);
      
      sourceFile = new JSourcePhysicalFile(connection, "QWCLLESRC", "WRKINS90");
      
      for (final Entry<Integer, List<String>> entry : physicals.entrySet()) {
         if (isInterrupted()) {
            return;
         }

         final String workItemNumber = String.valueOf(entry.getKey());
         final List<String> modifiedFiles = entry.getValue();
         final List<String> okayFiles = new ArrayList<>(modifiedFiles.size());
         
         updateText("Analisi del work item " + workItemNumber);
         updateTotal(0);
         updateProgress(0);
         
         correspondences.clear();
         sourceFile.scan(correspondences, workItemNumber, 0, 0);
         
         final int correspondecesSize = correspondences.size();
         updateTotal(correspondecesSize);
         
         for (int i = 0; i < correspondecesSize; i++) {
            if (isInterrupted()) {
               return;
            }
            
            updateProgress(i + 1);
            
            final JMember v901Member = new JProgramMember(null, correspondences.get(i), "QWCLLESRC", "WRKINS90");
            v901Member.loadSource();
            
            final List<String> v901MemberSource = v901Member.getSource();
            
            for (final String file : modifiedFiles) {
               for (String line : v901MemberSource) {
                  line = line.toUpperCase();
                  
                  if (JUtil.isComment(line)) {
                     continue;
                  }
                  
                  if (line.contains(file)) {
                     okayFiles.add(file);
                     break;
                  }
                  
                  if (line.contains("ABOSQ") && procedures.contains(file)) {
                     final int abosqlIndex = line.indexOf("ABOSQ");
                     final String abosqlFileName = line.substring(abosqlIndex, abosqlIndex + 10);
                     
                     if (!Character.isDigit(abosqlFileName.charAt(9))) {
                        continue;
                     }
                     
                     PreparedStatement statement = null;
                     ResultSet resultSet = null;
                     
                     try {
                        statement = dbConnection.prepareStatement("SELECT COUNT(NOMESQ) AS I FROM WRKINS90." + abosqlFileName + " WHERE NOMESQ = ?");
                        
                        for (final String innerFile : modifiedFiles) {
                           statement.setString(1, innerFile);
                           resultSet = statement.executeQuery();
                           
                           if (resultSet.next() && resultSet.getInt("I") > 0) {
                              okayFiles.add(innerFile);
                           }
                           
                           statement.clearParameters();
                           resultSet.close();
                        }
                     } catch (final SQLException e) {
                        e.printStackTrace();
                     } finally {
                        try {
                           if (resultSet != null) {
                              resultSet.close();
                           }
                           
                           if (statement != null) {
                              statement.close();
                           }
                        } catch (final SQLException e) {
                           e.printStackTrace();
                        }
                     }
                  }
               }
            }
            
            v901Member.dispose();
            modifiedFiles.removeAll(okayFiles);
         }
         
         final Integer integerWorkItemNumber = Integer.valueOf(workItemNumber);
         
         for (final String file : modifiedFiles) {
            rows.add(new V901(file, workItemNumber, workItems.get(integerWorkItemNumber)));
         }
      }
      
      tableViewer.getTable().getDisplay().asyncExec(new Runnable() {
         @Override
         public void run() {
            tableViewer.setInput(rows);
         }
      });
   }
   
   private void scanPhysicalFile(final JSourcePhysicalFile sourceFile, final int fileType) {
      final String[] sourceMembers = sourceFile.getMembers();
      final int membersCount = sourceMembers.length;
      
      updateTotal(membersCount);
      
      for (int i = 0; i < membersCount; i++) {
         if (isInterrupted()) {
            return;
         }

         updateText(sourceMembers[i]);
         updateProgress(i + 1);
         
         // Get a specific member from the list of members, load it and analyze it
         final JMember member = JMemberFactory.get(Application.getConnection(), sourceMembers[i], sourceFile.getName(), sourceFile.getLibrary());
         member.loadSource();
         
         int type = fileType;
         
         // If I'm reading SQL members, I must determine if they're tables, views or procedures
         if (TYPE_SQL == fileType) {
            for (String line : member.getSource()) {
               line = line.toUpperCase();
               
               if (line.contains("CREATE TABLE") || line.contains("CREATE OR REPLACE TABLE")) {
                  type = TYPE_PHYSICAL;
                  break;
               } else
                  if (line.contains("CREATE VIEW") || line.contains("CREATE OR REPLACE TABLE")) {
                     type = TYPE_LOGICAL;
                     break;
                  } else
                     if (line.contains("CREATE PROCEDURE") || line.contains("CREATE OR REPLACE PROCEDURE")) {
                        type = TYPE_PROCEDURE;
                        break;
                     }
            }
         }
         
         for (final SmiAbstractWorkItem workItem : member.getWorkItems()) {
            if (!workItem.inWork()) {
               continue;
            }
            
            final int workItemNumber = workItem.getNumber();
            
            // Check if the modification require only a CHGPF
            if (type == TYPE_PHYSICAL) {
               for (final String line : member.getSource()) {
                  if (!line.startsWith(String.valueOf(workItem.getIndex()) + " ")) {
                     continue;
                  }

                  if (line.charAt(18) != ' ') {
                     chgpf.remove(sourceMembers[i]);
                     break;
                  }

                  chgpf.add(sourceMembers[i]);
               }
            }
            
            if (chgpf.contains(sourceMembers[i])) {
               continue;
            }
            
            // Try to get the work item correspondent source members list
            List<String> modifiedFiles = null;
            
            switch (type) {
               case TYPE_PHYSICAL:
                  modifiedFiles = physicals.get(workItemNumber);
                  break;
               case TYPE_LOGICAL:
               case TYPE_PROCEDURE:
                  modifiedFiles = logicals.get(workItemNumber);
                  break;
               default:
                  break;
            }
            
            if (modifiedFiles == null) {
               // If I'm here, it means it's the first time I come across the work item
               modifiedFiles = new ArrayList<>(32);
               
               switch (type) {
                  case TYPE_PHYSICAL:
                     physicals.put(workItemNumber, modifiedFiles);
                     break;
                  case TYPE_LOGICAL:
                     logicals.put(workItemNumber, modifiedFiles);
                     break;
                  case TYPE_PROCEDURE:
                     logicals.put(workItemNumber, modifiedFiles);
                     procedures.add(sourceMembers[i]);
                     break;
                  default:
                     break;
               }
               
               // Save the user that own the work item
               workItems.put(workItemNumber, workItem.getUser());
            }
            
            modifiedFiles.add(sourceMembers[i]);
         }

         member.dispose();
      }
   }
}
