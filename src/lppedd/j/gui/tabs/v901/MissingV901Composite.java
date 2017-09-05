package lppedd.j.gui.tabs.v901;

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.regex.PatternSyntaxException;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import lppedd.j.api.JConnection;
import lppedd.j.api.JType;
import lppedd.j.api.factories.JMemberFactory;
import lppedd.j.api.factories.JObjectFactory;
import lppedd.j.api.files.database.JDatabaseFile;
import lppedd.j.api.members.JMember;
import lppedd.j.api.misc.Util;
import lppedd.j.api.objects.JObject;
import lppedd.j.gui.Application;
import lppedd.j.gui.actions.MissingV901Action;
import lppedd.j.gui.comparators.V901Comparator;
import lppedd.j.gui.models.V901;
import lppedd.j.gui.popups.progress.ProgressDialog;

/**
 * @author Edoardo Luppi
 */
public class MissingV901Composite extends Composite
{
   private static Clipboard clipboard;
   
   private final Text textFilter;
   private final Button buttonLoad;
   private final Table tableMissingV901;
   private final TableColumn tableColumnFile;
   private final TableColumn tableColumnUser;
   private final TableColumn tableColumnWi;
   private final TableViewer tableMissingV901Viewer;
   private final TableViewerColumn tableColumnFileViewer;
   private final TableViewerColumn tableColumnUserViewer;
   private final TableViewerColumn tableColumnWiViewer;
   private final V901Comparator tableMissingV901comparator = new V901Comparator();
   private final MenuItem menuItemCopyRows;
   private final Menu menuMissingV901;
   private final MenuItem menuItemCopyElement;
   
   /**
    * Contains the actual text of the {@link #textFilter} widget
    */
   private String textFilterValue = "";

   private Point selectedCellPoint;
   private MenuItem menuItemCreateV901;

   /**
    * Creates the composite.
    *
    * @param parent
    *        The parent composite
    */
   public MissingV901Composite(final Composite parent) {
      super(parent, SWT.NONE);
      setLayout(new GridLayout(2, false));

      clipboard = new Clipboard(getDisplay());

      textFilter = new Text(this, SWT.BORDER);
      textFilter.setTextLimit(10);
      textFilter.setTextCase(SWT.UPPERCASE);
      textFilter.setTextType(SWT.ALPHANUMERIC);
      textFilter.setFurtherAllowedCharacters('_');
      textFilter.setMessage("Filtro");
      textFilter.setToolTipText("Filtro");
      textFilter.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
      textFilter.addKeyListener(new KeyAdapter() {
         @Override
         public void keyReleased(final KeyEvent e) {
            textFilterValue = textFilter.getText();
            tableMissingV901Viewer.refresh();
         }
      });

      buttonLoad = new Button(this, SWT.NONE);
      buttonLoad.setText("Carica");
      buttonLoad.addSelectionListener(new SelectionAdapter() {
         @Override
         public void widgetSelected(final SelectionEvent e) {
            new ProgressDialog(getShell()).open(new MissingV901Action(tableMissingV901Viewer), SWT.NONE);
         }
      });

      final GridData gdButtonLoad = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
      gdButtonLoad.heightHint = 23;
      gdButtonLoad.widthHint = 100;
      buttonLoad.setLayoutData(gdButtonLoad);

      tableMissingV901Viewer = new TableViewer(this, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
      tableMissingV901Viewer.setContentProvider(ArrayContentProvider.getInstance());
      tableMissingV901Viewer.setComparator(tableMissingV901comparator);
      tableMissingV901Viewer.addFilter(new ViewerFilter() {
         @Override
         public boolean select(final Viewer viewer, final Object parentElement, final Object element) {
            if (textFilterValue.isEmpty()) {
               return true;
            }

            final V901 row = (V901) element;
            try {
               return row.file.contains(textFilterValue) ||
                     row.workItem.contains(textFilterValue) ||
                     row.user.contains(textFilterValue);
            } catch (final PatternSyntaxException e) {
               e.printStackTrace();
               return true;
            }
         }
      });

      tableMissingV901 = tableMissingV901Viewer.getTable();
      tableMissingV901.setLinesVisible(true);
      tableMissingV901.setHeaderVisible(true);
      tableMissingV901.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
      tableMissingV901.addMenuDetectListener(new MenuDetectListener() {
         @Override
         public void menuDetected(final MenuDetectEvent e) {
            if (tableMissingV901.getSelectionCount() < 1) {
               e.doit = false;
            }
         }
      });

      tableMissingV901.addListener(SWT.MouseDown, new Listener() {
         @Override
         public void handleEvent(final Event e) {
            selectedCellPoint = new Point(e.x, e.y);
         }
      });

      tableColumnFileViewer = new TableViewerColumn(tableMissingV901Viewer, SWT.NONE);
      tableColumnFileViewer.setLabelProvider(new ColumnLabelProvider() {
         @Override
         public String getText(final Object element) {
            return ((V901) element).file;
         }
      });

      tableColumnFile = tableColumnFileViewer.getColumn();
      tableColumnFile.setResizable(false);
      tableColumnFile.setWidth(120);
      tableColumnFile.setText("File");
      tableColumnFile.addSelectionListener(new SelectionAdapter() {
         @Override
         public void widgetSelected(final SelectionEvent e) {
            tableMissingV901comparator.setColumn(0);
            tableMissingV901.setSortColumn(tableColumnFile);
            tableMissingV901.setSortDirection(tableMissingV901comparator.getDirection());
            tableMissingV901Viewer.refresh();
         }
      });

      tableMissingV901comparator.setColumn(0);
      tableMissingV901.setSortColumn(tableColumnFile);
      tableMissingV901.setSortDirection(SWT.UP);

      tableColumnWiViewer = new TableViewerColumn(tableMissingV901Viewer, SWT.NONE);
      tableColumnWiViewer.setLabelProvider(new ColumnLabelProvider() {
         @Override
         public String getText(final Object element) {
            return ((V901) element).workItem;
         }
      });

      tableColumnWi = tableColumnWiViewer.getColumn();
      tableColumnWi.setResizable(false);
      tableColumnWi.setWidth(120);
      tableColumnWi.setText("Work item");
      tableColumnWi.addSelectionListener(new SelectionAdapter() {
         @Override
         public void widgetSelected(final SelectionEvent e) {
            tableMissingV901comparator.setColumn(1);
            tableMissingV901.setSortColumn(tableColumnWi);
            tableMissingV901.setSortDirection(tableMissingV901comparator.getDirection());
            tableMissingV901Viewer.refresh();
         }
      });

      tableColumnUserViewer = new TableViewerColumn(tableMissingV901Viewer, SWT.NONE);
      tableColumnUserViewer.setLabelProvider(new ColumnLabelProvider() {
         @Override
         public String getText(final Object element) {
            return ((V901) element).user;
         }
      });

      tableColumnUser = tableColumnUserViewer.getColumn();
      tableColumnUser.setResizable(false);
      tableColumnUser.setWidth(120);
      tableColumnUser.setText("Utente");

      menuMissingV901 = new Menu(tableMissingV901);
      tableMissingV901.setMenu(menuMissingV901);

      menuItemCreateV901 = new MenuItem(menuMissingV901, SWT.NONE);
      menuItemCreateV901.addSelectionListener(new SelectionAdapter() {
         @Override
         public void widgetSelected(final SelectionEvent e) {
            final IStructuredSelection selectedRows = tableMissingV901Viewer.getStructuredSelection();
            final JConnection connection = Application.getConnection();

            for (final Iterator<V901> iterator = selectedRows.iterator(); iterator.hasNext();) {
               final V901 row = iterator.next();

               final JDatabaseFile file = (JDatabaseFile) JObjectFactory.get(connection, row.file, "*LIBL", JType.FILE);
               final List<JObject> relatedFiles = file.getDatabaseRelations();

               final JMember member = JMemberFactory.get(connection, "V901TABLE", "QCLLESRC", "WRKEDOLUP");
               member.loadSource();
               
               final List<String> source = member.getSource();

               for (final ListIterator<String> listIterator = source.listIterator(); listIterator.hasNext();) {
                  final String line = listIterator.next();

                  if (line.contains("&USER")) {
                     final StringBuilder builder = new StringBuilder(line.substring(0, 8));
                     builder.append(row.user);
                     builder.append(" ");
                     builder.append(20170515);
                     builder.append(" ");
                     builder.append(" WORKITEM ");
                     builder.append(row.workItem);

                     listIterator.set(builder.toString().trim());
                  }

                  if (line.contains("&FILE")) {
                     listIterator.set(Util.rtrim(line.replace("&FILE", row.file)));
                  }

                  if (line.contains("&VIEWOBJ")) {
                     if (relatedFiles.size() != 0) {
                        listIterator.set(Util.rtrim(line.replace("&VIEWOBJ", relatedFiles.get(0).getName())));

                        for (int i = 1; i < relatedFiles.size(); i++) {
                           listIterator.add(Util.rtrim(line.replace("&VIEWOBJ", relatedFiles.get(i).getName())));
                        }
                     } else {

                     }
                  }
               }

               member.setSource(source);
               member.persist();
            }
         }
      });
                     
      menuItemCreateV901.setText("Crea V901");

      new MenuItem(menuMissingV901, SWT.SEPARATOR);

      menuItemCopyElement = new MenuItem(menuMissingV901, SWT.NONE);
      menuItemCopyElement.setText("Copia elemento");
      menuItemCopyElement.addSelectionListener(new SelectionAdapter() {
         @Override
         public void widgetSelected(final SelectionEvent e) {
            if (tableMissingV901.getSelectionCount() < 1) {
               return;
            }

            final TableItem item = tableMissingV901.getItem(selectedCellPoint);

            if (item != null) {
               for (int i = 0; i < tableMissingV901.getColumnCount(); i++) {
                  final Rectangle rectangle = item.getBounds(i);

                  if (rectangle.contains(selectedCellPoint)) {
                     clipboard.setContents(new String[] {
                           item.getText(i)
                     }, new Transfer[] {
                           TextTransfer.getInstance()
                     });
                  }
               }
            }
         }
      });

      menuItemCopyRows = new MenuItem(menuMissingV901, SWT.NONE);
      menuItemCopyRows.setText("Copia righe");
      menuItemCopyRows.addSelectionListener(new SelectionAdapter() {
         @Override
         public void widgetSelected(final SelectionEvent e) {
            final IStructuredSelection selection = tableMissingV901Viewer.getStructuredSelection();

            if (!selection.isEmpty()) {
               final Object[] rows = selection.toArray();
               final StringBuilder builder = new StringBuilder(20 * rows.length);

               for (final Object row : rows) {
                  builder.append(((V901) row).toString());
                  builder.append(System.getProperty("line.separator"));
               }

               final String[] string = new String[] {
                     builder.toString()
               };

               final Transfer[] transfer = new Transfer[] {
                     TextTransfer.getInstance()
               };

               new Clipboard(getDisplay()).setContents(string, transfer);
            }
         }
      });

      tableColumnUser.addSelectionListener(new SelectionAdapter() {
         @Override
         public void widgetSelected(final SelectionEvent e) {
            tableMissingV901comparator.setColumn(2);
            tableMissingV901.setSortColumn(tableColumnUser);
            tableMissingV901.setSortDirection(tableMissingV901comparator.getDirection());
            tableMissingV901Viewer.refresh();
         }
      });
   }

   @Override
   protected void checkSubclass() {
      //
   }
}
