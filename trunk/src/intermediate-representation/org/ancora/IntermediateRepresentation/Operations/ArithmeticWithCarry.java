/*
 *  Copyright 2010 Ancora Research Group.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */

package org.ancora.IntermediateRepresentation.Operations;

import java.util.logging.Logger;
import org.ancora.IntermediateRepresentation.Operand;
import org.ancora.IntermediateRepresentation.OperandUtils;
import org.ancora.IntermediateRepresentation.Operands.Literal;
import org.ancora.IntermediateRepresentation.Operands.OperandType;
import org.ancora.IntermediateRepresentation.Operation;

/**
 *
 * @author Joao Bispo
 */
public class ArithmeticWithCarry extends Operation {

   public ArithmeticWithCarry(int address, ArithmeticWithCarry.Op operation, Operand input1, Operand input2, Operand output1, Operand carryIn, Operand carryOut) {
      super(address);
      this.operation = operation;
      this.input1 = input1;
      this.input2 = input2;
      this.output1 = output1;
      this.carryIn = carryIn;
      this.carryOut = carryOut;

      // Connect Inputs
      connectToInput(input1);
      connectToInput(input2);
      if(carryIn == null) {
         hasCarryIn = false;
      } else {
         connectToInput(carryIn);
         hasCarryIn = true;
      }

      // Connect Outputs
      connectToOutput(output1);
      if(carryOut == null) {
         hasCarryOut = false;
      } else {
         connectToOutput(carryOut);
         hasCarryOut = true;
      }
   }



   @Override
   public Enum getType() {
      return OperationType.IntegerArithmeticWithCarry;
   }

   @Override
   public String toString() {
      //return "ir-"+operation.name();
      return operation.name();
   }

   @Override
   public boolean hasSideEffects() {
      return false;
   }

   public Integer getInput1() {
      if(input1.getType() != OperandType.literal) {
         return null;
      }

      return Literal.getInteger((Literal)input1);
   }

   public Integer getInput2() {
      if(input2.getType() != OperandType.literal) {
         return null;
      }

      return Literal.getInteger((Literal)input2);
   }

   public Operand getCarryIn() {
      return carryIn;
   }

   public Operand getCarryOut() {
      return carryOut;
   }

   public Operand getOutput() {
      return output1;
   }

   public Op getOperation() {
      return operation;
   }

   public Integer resolveOutput() {
      // Get first operand
      Integer firstOperand = getInput1();
      if(firstOperand == null) {
         return null;
      }

      // Get second operand
      Integer secondOperand = getInput2();
      if(secondOperand == null) {
         return null;
      }

      // Get carry
      Integer carryInValue = null;
      if (operation == Op.add) {
         carryInValue = 0;
      } else if (operation == Op.rsub) {
         carryInValue = 1;
      }
      
      if(carryIn != null) {
        Literal lit = OperandUtils.transformOperandToLiteral(carryIn);
        if(lit == null) {
           return null;
        }
        else {
           carryInValue = Literal.getInteger(lit);
        }
      }

      if(operation == Op.add) {
         return firstOperand + secondOperand + carryInValue;
      } else if(operation == Op.rsub) {
         return secondOperand - firstOperand + carryInValue;
      }
      
      Logger.getLogger(ArithmeticWithCarry.class.getName()).
              warning("Not defined:"+operation);

      return null;
   }

   public Integer resolveCarryOut() {
      if(carryOut == null) {
         return null;
      }

      // Get first operand
      Integer firstOperand = getInput1();
      if(firstOperand == null) {
         return null;
      }

      // Get second operand
      Integer secondOperand = getInput2();
      if(secondOperand == null) {
         return null;
      }

      // Get carry
      Integer carryInValue = null;
      if (operation == Op.add) {
         carryInValue = 0;
      } else if (operation == Op.rsub) {
         carryInValue = 1;
      }

      if(carryIn != null) {
        Literal lit = OperandUtils.transformOperandToLiteral(carryIn);
        if(lit == null) {
           return null;
        }
        else {
           carryInValue = Literal.getInteger(lit);
        }
      }

      if(operation == Op.add) {
         return getCarryOutAdd(firstOperand, secondOperand, carryInValue);
      } else if(operation == Op.rsub) {
         return getCarryOutRsub(firstOperand, secondOperand, carryInValue);
      }

      Logger.getLogger(ArithmeticWithCarry.class.getName()).
              warning("Not defined:"+operation);

      return null;
   }




   

   public enum Op {
      add,
      rsub;
   }

   /**
    * INSTANCE VARIABLES
    */
   private Operand input1;
   private Operand input2;
   private Operand output1;
   private Operand carryIn;
   private Operand carryOut;
   private boolean hasCarryIn;
   private boolean hasCarryOut;

   private ArithmeticWithCarry.Op operation;

      /**
     * Calculates the carryOut of the sum of rA with rB and carry.
     * Operation is rA + rB + carry.
     *
     * @param rA
     * @param rB
     * @param carry the carry from the previous operation. Should be 0 or 1.
     * @return 1 if there is carry out, or 0 if not.
     */
    public static int getCarryOutAdd(int rA, int rB, int carry) {
        if(carry != 0 && carry != 1) {
            Logger.getLogger(ArithmeticWithCarry.class.getName()).
                    warning("Carry is different than 0 or 1 ("+
                    carry+")");
        }

        //System.out.println("rA:"+Integer.toBinaryString(rA));
        //System.out.println("rB:"+Integer.toBinaryString(rB));

        // Extend operands to long and mask them
        long lRa = rA & MASK_32_BITS;
        long lRb = rB & MASK_32_BITS;
        // Carry must be 0 or 1, it shouldn't need to be masked.
        long lCarry = carry;


        //System.out.println("lRa:"+Long.toBinaryString(lRa));
        //System.out.println("lRb:"+Long.toBinaryString(lRb));

        // Do the summation
        long result = lRa + lRb + lCarry;

        //System.out.println("Result:"+Long.toBinaryString(result));

        // Get the carry bit
        int carryOut = (int) ((result & MASK_BIT_33) >>> 32);
        return carryOut;
    }

    /**
     * Calculates the carryOut of the reverse subtraction of rA with rB and
     * carry. Operation is rB + ~rA + carry.
     *
     * @param rA
     * @param rB
     * @param carry the carry from the previous operation. Should be 0 or 1.
     * @return 1 if there is carry out, or 0 if not.
     */
    public static int getCarryOutRsub(int rA, int rB, int carry) {
        if(carry != 0 && carry != 1) {
            Logger.getLogger(ArithmeticWithCarry.class.getName()).
                    warning("Carry is different than 0 or 1 ("+
                    carry+")");
        }

        //System.out.println("rA:"+Integer.toBinaryString(rA));
        //System.out.println("rB:"+Integer.toBinaryString(rB));

        // Extend operands to long and mask them
        long lRa = rA & MASK_32_BITS;
        long lRb = rB & MASK_32_BITS;
        // Carry must be 0 or 1, it shouldn't need to be masked.
        long lCarry = carry;


        //System.out.println("lRa:"+Long.toBinaryString(lRa));
        //System.out.println("lRb:"+Long.toBinaryString(lRb));

        // Do the summation
        long result = lRb + ~lRa + lCarry;

        //System.out.println("Result:"+Long.toBinaryString(result));

        // Get the carry bit
        int carryOut = (int) ((result & MASK_BIT_33) >>> 32);
        return carryOut;
    }

        private static final long MASK_32_BITS = 0xFFFFFFFFL;
    private static final long MASK_BIT_33 = 0x100000000L;
}
