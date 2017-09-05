package lppedd.j.gui.actions;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import org.eclipse.jface.viewers.TreeViewer;

import com.ibm.as400.access.AS400SecurityException;
import com.ibm.as400.access.IFSFile;
import com.ibm.as400.access.IFSFileOutputStream;
import com.ibm.as400.access.QSYSObjectPathName;

import lppedd.j.api.JConnection;
import lppedd.j.api.JType;
import lppedd.j.api.factories.JObjectFactory;
import lppedd.j.api.files.device.JSaveFile;
import lppedd.j.api.files.device.JSaveFile.JSavedObject;
import lppedd.j.api.misc.JUtil;
import lppedd.j.api.objects.JLibrary;
import lppedd.j.gui.Application;

/**
 * @author Edoardo Luppi
 */
public final class LocalSaveFileAnalysisAction extends ProgressAction
{
   private final TreeViewer treeViewer;
   private final String localFilePath;
   
   public LocalSaveFileAnalysisAction(final TreeViewer treeSavedObjectsViewer, final String localFilePath) {
      treeViewer = treeSavedObjectsViewer;
      this.localFilePath = localFilePath;
   }

   @Override
   protected void main() {
      final JConnection connection = Application.getConnection();
      final JLibrary tempLibrary = new JLibrary(connection, JUtil.getRandomString(10));
      tempLibrary.setText("Libreria temperanea per SAVFILE");
      
      if (!tempLibrary.create()) {
         return;
      }

      BufferedInputStream localInput = null;
      BufferedOutputStream remoteOutput = null;
      
      try {
         final IFSFile remoteFile = new IFSFile(tempLibrary.getConnection().getAs400(), QSYSObjectPathName.toPath(tempLibrary.getName(), "SAVFILE", "FILE"));
         remoteFile.createNewFile();
         
         final File localFile = new File(localFilePath);
         final int localFileSize = (int) localFile.length();
         
         updateTotal(localFileSize);

         final StringBuilder builder = new StringBuilder();
         builder.append("Caricamento del save file... 0/");
         builder.append((float) localFileSize / 1000);
         builder.append(" Kb");

         updateText(builder.toString());
         
         localInput = new BufferedInputStream(new FileInputStream(localFile));
         remoteOutput = new BufferedOutputStream(new IFSFileOutputStream(remoteFile));
         
         for (int i = 0, j = 1, value = 0; (value = localInput.read()) != -1; i++) {
            setProgress(i);
            
            if (getPercentage() > j) {
               j++;

               builder.replace(29, builder.length(), String.valueOf((float) i / 1000));
               builder.append("/");
               builder.append(String.valueOf((float) getTotal() / 1000));
               builder.append(" Kb");
               
               updateText(builder.toString());
               updateProgress(i);
            }
            
            remoteOutput.write(value);
         }
      } catch (final IOException | AS400SecurityException e) {
         e.printStackTrace();
         interrupt();
      } finally {
         try {
            if (localInput != null) {
               localInput.close();
            }
            
            if (remoteOutput != null) {
               remoteOutput.close();
            }
         } catch (final IOException e) {
            e.printStackTrace();
         }
      }
      
      if (isInterrupted()) {
         tempLibrary.delete();
         return;
      }
      
      updateText("Analisi del save file...");
      
      final List<JSavedObject> savedObjects = ((JSaveFile) JObjectFactory.get(connection, "SAVFILE", tempLibrary.getName(), JType.FILE)).getSavedObjects();
      tempLibrary.delete();
      
      treeViewer.getTree().getDisplay().asyncExec(new Runnable() {
         @Override
         public void run() {
            treeViewer.setInput(savedObjects);
         }
      });
   }
}
