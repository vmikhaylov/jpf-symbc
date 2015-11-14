/*
 * Copyright (C) 2014, United States Government, as represented by the
 * Administrator of the National Aeronautics and Space Administration.
 * All rights reserved.
 *
 * The Java Pathfinder core (jpf-core) platform is licensed under the
 * Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0. 
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

// author corina pasareanu corina.pasareanu@sv.cmu.edu

package gov.nasa.jpf.symbc.bytecode;

import gov.nasa.jpf.symbc.SymbolicInstructionFactory;
import gov.nasa.jpf.symbc.heap.HeapChoiceGenerator;
import gov.nasa.jpf.symbc.heap.SymbolicInputHeap;
import gov.nasa.jpf.symbc.numeric.Comparator;
import gov.nasa.jpf.symbc.numeric.IntegerExpression;
import gov.nasa.jpf.symbc.numeric.PCChoiceGenerator;
import gov.nasa.jpf.symbc.numeric.PathCondition;
import gov.nasa.jpf.vm.ArrayFields;
import gov.nasa.jpf.vm.ArrayIndexOutOfBoundsExecutiveException;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.ElementInfo;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.Scheduler;
import gov.nasa.jpf.vm.StackFrame;
import gov.nasa.jpf.vm.ThreadInfo;

/**
 * Load reference from array
 * ..., arrayref, index  => ..., value
 */
public class AALOAD extends gov.nasa.jpf.jvm.bytecode.AALOAD {

	
  @Override
  public Instruction execute (ThreadInfo ti) {
	
	  if (peekIndexAttr(ti)==null || !(peekIndexAttr(ti) instanceof IntegerExpression))
		  return super.execute(ti);
	  // index is symbolic
	  
	  StackFrame frame = ti.getModifiableTopFrame();
	  arrayRef = frame.peek(1); // ..,arrayRef,idx
	    if (arrayRef == MJIEnv.NULL) {
	      return ti.createAndThrowException("java.lang.NullPointerException");
	    }
	  //throw new RuntimeException("Arrays: symbolic index not handled");
	    ElementInfo eiArray = ti.getElementInfo(arrayRef);    
        int len=(eiArray.getArrayFields()).arrayLength(); // assumed concrete
	   // check for out of bounds exceptions 0 <= sym_index < len
      
        //original code for concrete execution
        arrayOperandAttr = peekArrayAttr(ti);
        indexOperandAttr = peekIndexAttr(ti);
        IntegerExpression sym_index=(IntegerExpression)indexOperandAttr;
        //check for out of bounds exceptions 0 <= sym_index < len
        
        // Ignore POR for now
        frame.pop(2); // now we can pop index and array reference
        try {
          push(frame, eiArray, index);
            
          Object elementAttr = eiArray.getElementAttr(index);
          if (elementAttr != null) {
            if (getElementSize() == 1) {
              frame.setOperandAttr(elementAttr);
            } else {
              frame.setLongOperandAttr(elementAttr);
            }
          }
          return getNext(ti);
          
        } catch (ArrayIndexOutOfBoundsExecutiveException ex) {
          return ex.getInstruction();
        }

  }
}
