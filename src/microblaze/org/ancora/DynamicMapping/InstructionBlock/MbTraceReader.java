/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ancora.DynamicMapping.InstructionBlock;

import static org.ancora.MicroBlaze.Trace.TraceDefinitions.TRACE_PREFIX;

import java.io.File;
import java.util.logging.Logger;
import org.ancora.MicroBlaze.InstructionName;
import org.ancora.SharedLibrary.ParseUtils;
import org.ancora.common.LineReader;

/**
 * Reads MicroBlaze traces as if they where executing in an instruction bus.
 *
 * @author Joao Bispo
 */
public class MbTraceReader implements InstructionBusReader {

   /**
    * Private constructor for static creator method.
    *
    * @param reader
    */
    private MbTraceReader(LineReader reader) {
       this.reader = reader;
    }

   /**
    * Builds a TraceReader from the given file. If the object could
    * not be created, returns null.
    *
    * <p>Creating a TraceReader involves File operations which can lead
    * to failure in creation of the object. That is why we use a public
    * static method instead of a constructor.
    *
    * @param traceFile a file representing a MicroBlaze Trace, as in the format
    * of the tool of the Ancora Group (not avaliable yet).
    * @return a TraceReader If the object could not be created, returns null.
    */
   public static MbTraceReader createTraceReader(File traceFile) {

      LineReader reader = LineReader.createLineReader(traceFile);
      if(reader == null) {
         Logger.getLogger(MbTraceReader.class.getName()).
                    warning("Could not create MbTraceReader.");
      }

      return new MbTraceReader(reader);

   }

   public static MbInstruction createMicroBlazeInstruction(String line) {
      /// Split the trace instruction in parts
      int whiteSpaceIndex = ParseUtils.indexOfFirstWhiteSpace(line);

      /// Get Address
      String addressString = line.substring(0, whiteSpaceIndex);
      // Remove prefix
      addressString = addressString.substring(TRACE_PREFIX.length());
      // Parse to integer
      int instructionAddress = Integer.valueOf(addressString, 16);

      /// Get Instruction
      String instruction = line.substring(whiteSpaceIndex).trim();

      /// Get InstructionName
      whiteSpaceIndex = ParseUtils.indexOfFirstWhiteSpace(instruction);
      String instNameString = instruction.substring(0, whiteSpaceIndex);
      InstructionName instName = InstructionName.getEnum(instNameString);

      return new MbInstruction(instructionAddress, instruction, instName);
   }


    /**
     * @return the next line in the file which qualifies as an instruction, or
     * null if the end of the stream has been reached.
     * A line is considered as a trace instruction if it starts with "0x".
     */
   public GenericInstruction nextInstruction() {

      // While there are lines and a trace instruction was not found, loop.
      String line = null;
      
      while (true) {
         line = reader.nextLine();

         if(line == null) {
            return null;
         }

         // Check if current line is a trace instruction
         if (line.startsWith(TRACE_PREFIX)) {
            // Create MicroBlazeInstruction
            MbInstruction instruction = createMicroBlazeInstruction(line);
            return instruction;
         } 
      }
   }

   /**
    * INSTANCE VARIABLES
    */
    private final LineReader reader;

}
