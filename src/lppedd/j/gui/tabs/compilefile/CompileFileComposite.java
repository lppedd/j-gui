package lppedd.j.gui.tabs.compilefile;

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Text;

import lppedd.j.api.JType;
import lppedd.j.api.misc.Pair;
import lppedd.j.gui.Application;
import lppedd.j.gui.actions.CompileFileAction;
import lppedd.j.gui.popups.progress.ProgressDialog;

public class CompileFileComposite extends Composite
{
   private final Group groupSource;
   private final Group groupDestination;
   private final List listWorkLibraries;
   private final Text textWorkLibrary;
   private final Text textSourceMember;
   private final Text textSourceObject;
   private final Text textSourceLibrary;
   private final Button buttonAddLibrary;
   private final Button buttonConfirm;
   private final Button checkCopyData;
   private final Menu menu;
   private final MenuItem menuItemRemoveSelected;
   private List listPtfLibraries;
   private Text textPtfLibrary;

   /**
    * Create the composite.
    *
    * @param parent
    * @param style
    */
   public CompileFileComposite(final Composite parent) {
      super(parent, SWT.NONE);
      setLayout(new GridLayout(2, false));

      groupSource = new Group(this, SWT.NONE);
      groupSource.setLayout(new GridLayout(1, false));
      groupSource.setText("Sorgente");

      final GridData gdGroupSource = new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1);
      gdGroupSource.widthHint = 160;

      groupSource.setLayoutData(gdGroupSource);

      textSourceMember = new Text(groupSource, SWT.BORDER);
      textSourceMember.setToolTipText("Membro");
      textSourceMember.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
      textSourceMember.setMessage("Membro");
      textSourceMember.setTextLimit(10);
      textSourceMember.setTextCase(SWT.UPPERCASE);
      textSourceMember.setTextType(SWT.ALPHANUMERIC);

      textSourceObject = new Text(groupSource, SWT.BORDER);
      textSourceObject.setToolTipText("Oggetto");
      textSourceObject.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
      textSourceObject.setMessage("Oggetto");
      textSourceObject.setTextLimit(10);
      textSourceObject.setTextCase(SWT.UPPERCASE);
      textSourceObject.setTextType(SWT.ALPHANUMERIC);

      textSourceLibrary = new Text(groupSource, SWT.BORDER);
      textSourceLibrary.setToolTipText("Libreria");
      textSourceLibrary.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
      textSourceLibrary.setMessage("Libreria");
      textSourceLibrary.setTextLimit(10);
      textSourceLibrary.setTextCase(SWT.UPPERCASE);
      textSourceLibrary.setTextType(SWT.ALPHANUMERIC);

      groupDestination = new Group(this, SWT.NONE);
      groupDestination.setText("Oggetto");
      groupDestination.setLayout(new GridLayout(2, true));

      final GridData gdGroupDestination = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
      gdGroupDestination.heightHint = 220;

      groupDestination.setLayoutData(gdGroupDestination);

      textWorkLibrary = new Text(groupDestination, SWT.BORDER);
      textWorkLibrary.setToolTipText("Libreria di work");
      textWorkLibrary.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
      textWorkLibrary.setMessage("Libreria di work");
      textWorkLibrary.setTextLimit(10);
      textWorkLibrary.setTextCase(SWT.UPPERCASE);
      textWorkLibrary.setTextType(SWT.ALPHANUMERIC);

      textPtfLibrary = new Text(groupDestination, SWT.BORDER);
      textPtfLibrary.setToolTipText("Libreria PTF");
      textPtfLibrary.setMessage("Libreria PTF");
      textPtfLibrary.setTextLimit(10);
      textPtfLibrary.setTextCase(SWT.UPPERCASE);
      textPtfLibrary.setTextType(SWT.ALPHANUMERIC);
      textPtfLibrary.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

      buttonAddLibrary = new Button(groupDestination, SWT.NONE);
      buttonAddLibrary.setText("Aggiungi");
      buttonAddLibrary.addSelectionListener(new SelectionAdapter() {
         @Override
         public void widgetSelected(final SelectionEvent e) {
            final String workLibrary = textWorkLibrary.getText().trim();

            if (workLibrary.isEmpty()) {
               return;
            }

            // Check if this work library already exists in the list
            for (final String library : listWorkLibraries.getItems()) {
               if (library.equals(workLibrary)) {
                  return;
               }
            }

            // Check if this work library exists in the system
            if (!Application.getConnection().exists("QSYS", workLibrary, JType.LIB, null)) {
               return;
            }

            final String ptfLibrary = textPtfLibrary.getText().trim();

            if (!ptfLibrary.isEmpty() && !Application.getConnection().exists("QSYS", ptfLibrary, JType.LIB, null)) {
               return;
            }

            listPtfLibraries.add(ptfLibrary, 0);
            listWorkLibraries.add(workLibrary, 0);
            textPtfLibrary.setText("");
            textWorkLibrary.setText("");
            textWorkLibrary.setFocus();
         }
      });

      final GridData gdButtonAddLibrary = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
      gdButtonAddLibrary.horizontalIndent = -1;
      gdButtonAddLibrary.heightHint = 23;

      buttonAddLibrary.setLayoutData(gdButtonAddLibrary);

      listWorkLibraries = new List(groupDestination, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI);
      listWorkLibraries.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 0, 1));
      listWorkLibraries.setItems("WRK90PRO", "WRK90PIP", "WRK90BIL", "WRK90ANG", "WRK90CAT");
      listWorkLibraries.addSelectionListener(new SelectionAdapter() {
         @Override
         public void widgetSelected(final SelectionEvent e) {
            listPtfLibraries.setSelection(listWorkLibraries.getSelectionIndices());
         }
      });

      listWorkLibraries.addMenuDetectListener(new MenuDetectListener() {
         @Override
         public void menuDetected(final MenuDetectEvent e) {
            if (listWorkLibraries.getSelectionCount() < 1) {
               e.doit = false;
            }
         }
      });

      menu = new Menu(listWorkLibraries);
      listWorkLibraries.setMenu(menu);

      menuItemRemoveSelected = new MenuItem(menu, SWT.NONE);
      menuItemRemoveSelected.setText("Rimuovi selezionati");
      menuItemRemoveSelected.addSelectionListener(new SelectionAdapter() {
         @Override
         public void widgetSelected(final SelectionEvent e) {
            if (listWorkLibraries.getSelectionCount() > 0) {
               final int[] indexes = listWorkLibraries.getSelectionIndices();

               listWorkLibraries.remove(indexes);
               listPtfLibraries.remove(indexes);
            }
         }
      });

      listPtfLibraries = new List(groupDestination, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI);
      listPtfLibraries.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
      listPtfLibraries.setItems("PRO90DAT", "PIP90DAT", "BIL90DAT", "ANG90DAT", "CAT90DAT");
      listPtfLibraries.addSelectionListener(new SelectionAdapter() {
         @Override
         public void widgetSelected(final SelectionEvent e) {
            if (listWorkLibraries.getSelectionCount() > 0) {
               listPtfLibraries.setSelection(listWorkLibraries.getSelectionIndices());
            }
         }
      });

      checkCopyData = new Button(groupDestination, SWT.CHECK);
      checkCopyData.setText("Copia dati");

      new Label(groupDestination, SWT.NONE);

      buttonConfirm = new Button(this, SWT.NONE);
      buttonConfirm.setText("Conferma");
      buttonConfirm.addSelectionListener(new SelectionAdapter() {
         @Override
         public void widgetSelected(final SelectionEvent e) {
            final String sourceMember = textSourceMember.getText().trim();
            final String sourceObject = textSourceObject.getText().trim();
            final String sourceLibrary = textSourceLibrary.getText().trim();

            if (sourceMember.isEmpty() || sourceObject.isEmpty() || sourceLibrary.isEmpty()) {
               return;
            }

            final java.util.List<Pair<String, String>> libraries = new ArrayList<>(5);

            for (int i = 0; i < listWorkLibraries.getItemCount(); i++) {
               libraries.add(Pair.of(listWorkLibraries.getItem(i), listPtfLibraries.getItem(i)));
            }

            if (libraries.isEmpty()) {
               return;
            }

            new ProgressDialog(getShell()).open(
                  new CompileFileAction(
                        sourceMember,
                        sourceObject,
                        sourceLibrary,
                        checkCopyData.getSelection(),
                        libraries),
                  SWT.NONE);
         }
      });

      final GridData gdButtonConfirm = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 2, 1);
      gdButtonConfirm.widthHint = 100;
      gdButtonConfirm.heightHint = 23;

      buttonConfirm.setLayoutData(gdButtonConfirm);
   }

   @Override
   protected void checkSubclass() {
      //
   }
}
