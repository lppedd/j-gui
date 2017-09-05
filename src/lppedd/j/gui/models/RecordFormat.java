package lppedd.j.gui.models;

/**
 * @author Edoardo Luppi
 */
public class RecordFormat
{
   public final String recordFormat;
   public final String description;
   public final String dataType;
   public final String used;
   public final RecordFormat parent;
   public final RecordFormat[] children;

   public RecordFormat(
         final String recordFormat,
         final String description,
         final String dataType,
         final String used,
         final RecordFormat parent,
         final RecordFormat[] children) {
      this.recordFormat = recordFormat == null ? "" : recordFormat;
      this.description = description == null ? "" : description;
      this.dataType = dataType == null ? "" : dataType;
      this.used = used == null ? "" : used;
      this.parent = parent;
      this.children = children;
   }

   @Override
   public String toString() {
      return recordFormat + " " + description + " " + dataType + " " + used;
   }
}
