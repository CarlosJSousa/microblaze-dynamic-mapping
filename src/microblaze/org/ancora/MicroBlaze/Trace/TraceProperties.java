/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ancora.MicroBlaze.Trace;

import java.io.File;
import java.util.Properties;
import org.ancora.SharedLibrary.IoUtils;

/**
 *
 * @author Joao Bispo
 */
public class TraceProperties {

   private TraceProperties(Properties properties) {
      this.properties = properties;
   }

   /*
   public static TraceProperties getTraceProperties(Properties props) {
      TraceProperties traceProps = new TraceProperties(props);
     
      return traceProps;
   }
    */

   /**
    * Given a File object representing a Trace Properties file, loads the
    * contents of the file into a TraceDefinitions object.
    *
    * <p>If an error occurs (ex.: the File argument does not represent a file,
    * could not load the Properties object) returns null.
    *
    * @param tracePropertiesFile
    * @return
    */
   public static TraceProperties getTraceProperties(File traceFile) {

      /*
      // Get Properties file corresponding to this trace file
      File tracePropertiesFile = TraceUtils.getTracePropertiesFile(traceFile, Key.values());
      // Check if file exists
      if(!tracePropertiesFile.exists()) {
         return null;
      }

      Properties properties = IoUtils.loadProperties(tracePropertiesFile);
*/
      Properties properties = TraceUtils.getPropertiesFromTrace(traceFile, Key.values());

      if(properties == null) {
         return null;
      }

      return new TraceProperties(properties);
   }


   public float getCpi() {
      String value = TraceUtils.safeGet(Key.cpi, properties);
      return Float.parseFloat(value);
   }

   public long getInstructions() {
     String value = TraceUtils.safeGet(Key.instructions, properties);
      return Long.parseLong(value);
   }

   public long getCycles() {
      String value = TraceUtils.safeGet(Key.cycles, properties);
      return Long.parseLong(value);
   }

   public enum Key {
      instructions,
      cycles,
      cpi
   }

   /**
    * INSTANCE VARIABLES
    */
      final private Properties properties;
}
