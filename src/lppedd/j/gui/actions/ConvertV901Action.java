package lppedd.j.gui.actions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import lppedd.j.api.factories.JMemberFactory;
import lppedd.j.api.members.JMember;
import lppedd.j.api.members.JNullMember;
import lppedd.j.api.misc.Util;
import lppedd.j.gui.Application;
import smi.workitem.SmiAbstractWorkItem;

public class ConvertV901Action extends ProgressAction
{
   private final String member;
   private final String object;
   private final String library;
   
   public ConvertV901Action(final String member, final String object, final String library) {
      this.member = member;
      this.object = object;
      this.library = library;
   }
   
   @Override
   protected void main() {
      final JMember member = JMemberFactory.get(Application.getConnection(), this.member, object, library);
      
      if (member instanceof JNullMember) {
         return;
      }
      
      updateText("Conversione del programma...");
      
      member.loadSource();
      final List<String> source = member.getSource();
      final List<String> newSource = new ArrayList<>(300);
      final List<String> parameters = new ArrayList<>(11);
      
      for (final ListIterator<String> i = source.listIterator(); i.hasNext();) {
         String line = i.next();
         final int index = line.indexOf("PGM(CRTTBL1601) PARM(");
         line = line.substring(index + 21);
         
         if (index != -1) {
            final StringBuilder builder = new StringBuilder(60);
            
            while (i.hasNext()) {
               builder.append(line.trim());
               
               if (line.contains(")")) {
                  break;
               }
               
               line = i.next();
            }
            
            final String call = builder.toString();
            
            parameters.addAll(Arrays.asList(call.replaceAll("[+()]", "").trim().split(" ")));
            parameters.remove(0);
            parameters.remove(0);
            parameters.remove(4);
            parameters.remove(parameters.size() - 1);
            
            final Map<String, List<String>> parametersMap = new HashMap<>(11);
            
            for (final String parameter : parameters) {
               for (int j = i.nextIndex(); j >= 0; j--) {
                  final String innerLine = source.get(j);
                  
                  if ((innerLine.contains("CHGVAR") || innerLine.contains("DCL")) && innerLine.contains(parameter + ")")) {
                     if (parametersMap.containsKey(parameter)) {
                        continue;
                     }
                     
                     final String buildedLine = buildParameterString(source, j);
                     final int valueIndex = buildedLine.indexOf("VALUE(") + 6;
                     
                     if (valueIndex < 6) {
                        continue;
                     }
                     
                     parametersMap.put(
                           parameter,
                           Arrays.asList(buildedLine.substring(valueIndex, buildedLine.indexOf(")", valueIndex)).trim().replaceAll("['+]", "").split(" ")));
                  }
               }
            }
            
            final String tableObject = parametersMap.get("&OBJ").get(0);
            final String tableObjectBackup = parametersMap.get("&OBJBK").get(0);
            final SmiAbstractWorkItem wi = member.getWorkItems()[0];
            
            newSource.add("/******************************************************************************+");
            newSource.add(Util.rightPad(wi.getIndex() + (wi.inWork() ? "§" : " ") + " * " + wi.getUser() + " " + wi.getDate() + " WORKITEM " + wi.getNumber(), 79) + "+");
            newSource.add("    § * Creazione programma                                                    +");
            newSource.add("/******************************************************************************/");
            newSource.add("            PGM        PARM(&LIB &LIBIS &RTCDE &TEXT)");
            newSource.add("");
            newSource.add("            DCL        VAR(&LIB) TYPE(*CHAR) LEN(10)");
            newSource.add("            DCL        VAR(&LIBIS) TYPE(*CHAR) LEN(10)");
            newSource.add("            DCL        VAR(&RTCDE) TYPE(*CHAR) LEN(1)");
            newSource.add("            DCL        VAR(&TEXT) TYPE(*CHAR) LEN(200)");
            newSource.add("");
            newSource.add("            DCL        VAR(&FOUND) TYPE(*CHAR) LEN(1)");
            newSource.add("            DCL        VAR(&SQLCOD_A) TYPE(*CHAR) LEN(9)");
            newSource.add("");
            newSource.add("/* Parametri per ABCRTSQL */");
            newSource.add("            DCL        VAR(&FILE) TYPE(*CHAR) LEN(20)");
            newSource.add("            DCL        VAR(&MBR) TYPE(*CHAR) LEN(10)");
            newSource.add("            DCL        VAR(&OBJ) TYPE(*CHAR) LEN(20)");
            newSource.add("            DCL        VAR(&ERROR) TYPE(*CHAR) LEN(1)");
            newSource.add("            DCL        VAR(&SQLCOD) TYPE(*INT) LEN(4)");
            newSource.add("");
            newSource.add("/* Cancello le eventuali viste collegate */");
            newSource.add("      CALL       PGM(SMI21C) PARM(" + tableObject + " &LIB &ERROR)");
            newSource.add("");
            newSource.add("      IF         (&ERROR *EQ 'S') THEN(DO)");
            newSource.add("        CHGVAR     VAR(&RTCDE) VALUE('W')");
            newSource.add("        CHGVAR     VAR(&TEXT) VALUE('Errore cancellando le viste +");
            newSource.add("                       collegate a " + tableObject + " in ' *CAT &LIB)");
            newSource.add("        GOTO       FINE");
            newSource.add("      ENDDO");
            newSource.add("");
            newSource.add("/* Controllo se il file esiste gia' ed eventualmente lo rinomino */");
            newSource.add("      CHGVAR     VAR(&FOUND) VALUE('S')");
            newSource.add("      CHKOBJ     OBJ(&LIB/" + tableObject + ") OBJTYPE(*FILE)");
            newSource.add("");
            newSource.add("      MONMSG     MSGID(CPF9801) EXEC(DO)");
            newSource.add("        CHGVAR     VAR(&FOUND) VALUE(' ')");
            newSource.add("      ENDDO");
            newSource.add("");
            newSource.add("      IF         (&FOUND *EQ 'S') THEN(DO)");
            newSource.add("        RNMOBJ     OBJ(&LIB/" + tableObject + ") OBJTYPE(*FILE) NEWOBJ(" + tableObjectBackup + ")");
            newSource.add("");
            newSource.add("        MONMSG     MSGID(CPF0000) EXEC(DO)");
            newSource.add("          CHGVAR     VAR(&RTCDE) VALUE('W')");
            newSource.add("          CHGVAR     VAR(&TEXT) VALUE('Errore ridenominando il file +");
            newSource.add("                       " + tableObject + " in ' *CAT &LIB)");
            newSource.add("          GOTO       FINE");
            newSource.add("        ENDDO");
            newSource.add("");
            newSource.add("        CHGVAR     VAR(&TEXT) VALUE('Dati di " + tableObject + " salvati in +");
            newSource.add("                      " + tableObjectBackup + " in ' *CAT &LIB)");
            newSource.add("        CALL       PGM(SMILOG) PARM('V901AF381 ' &LIB &TEXT)");
            newSource.add("      ENDDO");
            newSource.add("");
            newSource.add("/* Creo il file */");
            newSource.add("      CHGVAR     VAR(&FILE) VALUE('" + Util.rightPad(parametersMap.get("&OBJIS").get(0), 10) + "' *CAT &LIBIS)");
            newSource.add("      CHGVAR     VAR(&OBJ) VALUE('" + Util.rightPad(tableObject, 10) + "' *CAT &LIB)");
            newSource.add("      CHGVAR     VAR(&MBR) VALUE('" + Util.rightPad(parametersMap.get("&MBR").get(0), 10) + "')");
            newSource.add("");
            newSource.add("      CALL       PGM(ABCRTSQL) PARM(&FILE &MBR &OBJ +");
            newSource.add("                   &ERROR &SQLCOD)");
            newSource.add("");
            newSource.add("      MONMSG     MSGID(CPF0000) EXEC(DO)");
            newSource.add("        CHGVAR     VAR(&RTCDE) VALUE('E')");
            newSource.add("        CHGVAR     VAR(&TEXT) VALUE('Errore creando " + tableObject + " in ' +");
            newSource.add("                      *CAT &LIB)");
            newSource.add("        GOTO       FINE");
            newSource.add("      ENDDO");
            newSource.add("");
            newSource.add("      IF         COND(&ERROR *EQ 'E') THEN(DO)");
            newSource.add("        IF         COND(&SQLCOD *NE 7905 *AND &SQLCOD *NE -601) THEN(DO)");
            newSource.add("          CHGVAR     VAR(&SQLCOD_A) VALUE(&SQLCOD)");
            newSource.add("          CHGVAR     VAR(&RTCDE) VALUE('E')");
            newSource.add("          CHGVAR     VAR(&TEXT) VALUE('Errore creando " + tableObject + " in ' +");
            newSource.add("                        *CAT &LIB *CAT '. Errore SQL = ' +");
            newSource.add("                        *CAT &SQLCOD_A)");
            newSource.add("          GOTO       FINE");
            newSource.add("        ENDDO");
            newSource.add("      ENDDO");
            newSource.add("");
            newSource.add("/* Ripristino i dati salvati */");
            newSource.add("      IF         COND(&FOUND *EQ 'S') THEN(DO)");
            newSource.add("        CPYF       FROMFILE(" + tableObjectBackup + ") TOFILE(&LIB/" + tableObject + ") +");
            newSource.add("                      MBROPT(*ADD) FMTOPT(*MAP *DROP) +");
            newSource.add("                      ERRLVL(*NOMAX)");
            newSource.add("");
            newSource.add("        MONMSG     MSGID(CPF0000) EXEC(DO)");
            newSource.add("          CHGVAR     VAR(&RTCDE) VALUE('E')");
            newSource.add("          CHGVAR     VAR(&TEXT) VALUE('Errore copiando i dati nel +");
            newSource.add("                        file " + tableObject + " in ' *CAT &LIB)");
            newSource.add("          GOTO       FINE");
            newSource.add("        ENDDO");
            newSource.add("      ENDDO");
            newSource.add("");
            
            final List<String> viewObjects = parametersMap.get("&VOBJ");
            
            if (viewObjects != null) {
               final List<String> viewMembers = parametersMap.get("&VSRC");
               
               newSource.add("/* Creo le viste logiche collegate */");
               newSource.add("      CHGVAR     VAR(&FILE) VALUE('" + Util.rightPad(parametersMap.get("&VOBJIS").get(0), 10) + "' *CAT &LIBIS)");
               newSource.add("");
               
               for (int k = 0; k < viewObjects.size(); k++) {
                  final String viewObject = viewObjects.get(k);
                  final String viewMember = viewMembers.get(k);
                  
                  newSource.add("/* Creo " + viewObject + " */");
                  newSource.add("      CHGVAR     VAR(&OBJ) VALUE('" + Util.rightPad(viewObject, 10) + "' *CAT &LIB)");
                  newSource.add("      CHGVAR     VAR(&MBR) VALUE('" + Util.rightPad(viewMember, 10) + "')");
                  newSource.add("");
                  newSource.add("      CALL       PGM(ABCRTSQL) PARM(&FILE &MBR &OBJ +");
                  newSource.add("                   &ERROR &SQLCOD)");
                  newSource.add("");
                  newSource.add("      MONMSG     MSGID(CPF0000) EXEC(DO)");
                  newSource.add("        CHGVAR     VAR(&RTCDE) VALUE('W')");
                  newSource.add("        CHGVAR     VAR(&TEXT) VALUE('Errore creando " + viewObject + " in +");
                  newSource.add("                     ' *CAT &LIB)");
                  newSource.add("        GOTO       FINE");
                  newSource.add("      ENDDO");
                  newSource.add("");
                  newSource.add("      IF         COND(&ERROR *EQ 'E') THEN(DO)");
                  newSource.add("        IF         COND(&SQLCOD *NE 7905 *AND &SQLCOD *NE -601) THEN(DO)");
                  newSource.add("          CHGVAR     VAR(&SQLCOD_A) VALUE(&SQLCOD)");
                  newSource.add("          CHGVAR     VAR(&RTCDE) VALUE('W')");
                  newSource.add("          CHGVAR     VAR(&TEXT) VALUE('Errore creando " + viewObject + " in ' +");
                  newSource.add("                        *CAT &LIB *CAT '. Errore SQL = ' +");
                  newSource.add("                        *CAT &SQLCOD_A)");
                  newSource.add("          GOTO       FINE");
                  newSource.add("        ENDDO");
                  newSource.add("      ENDDO");
                  newSource.add("");
               }
            }
            
            newSource.add("      CHGVAR     VAR(&RTCDE) VALUE('Y')");
            newSource.add("      CHGVAR     VAR(&TEXT) VALUE('" + tableObject + " allineato correttamente')");
            newSource.add("");
            newSource.add("FINE:");
            newSource.add("      ENDPGM");
            break;
         }
         
      }
      
      member.setSource(newSource);
      member.persist();
      member.dispose();
   }
   
   private static String buildParameterString(final List<String> source, final int initialPosition) {
      final StringBuilder builder = new StringBuilder(60);
      
      for (int i = initialPosition; i >= 0; i--) {
         final String line = source.get(i).trim();
         builder.append(source.get(i));
         
         if (!line.contains("VALUE(") && line.contains(")")) {
            break;
         }
      }
      
      return builder.toString();
   }
}
