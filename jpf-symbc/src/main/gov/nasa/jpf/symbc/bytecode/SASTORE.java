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
import gov.nasa.jpf.symbc.numeric.Comparator;
import gov.nasa.jpf.symbc.numeric.IntegerExpression;
import gov.nasa.jpf.symbc.numeric.PCChoiceGenerator;
import gov.nasa.jpf.symbc.numeric.PathCondition;
import gov.nasa.jpf.vm.ArrayIndexOutOfBoundsExecutiveException;
import gov.nasa.jpf.vm.ElementInfo;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.StackFrame;
import gov.nasa.jpf.vm.ThreadInfo;

/**
 * Store into short array ..., array, index, value => ...
 */
public class SASTORE extends gov.nasa.jpf.jvm.bytecode.SASTORE {

	@Override
	public Instruction execute(ThreadInfo threadInfo) {
		if (peekIndexAttr(threadInfo) == null || !(peekIndexAttr(threadInfo) instanceof IntegerExpression)) {
			return super.execute(threadInfo);
		}
		StackFrame stackFrame = threadInfo.getModifiableTopFrame();
		int arrayref = peekArrayRef(threadInfo); // need to be polymorphic, could be LongArrayStore
		ElementInfo arrayElementInfo = threadInfo.getElementInfo(arrayref);

		if (arrayref == MJIEnv.NULL) {
			return threadInfo.createAndThrowException("java.lang.NullPointerException");
		}

		int len = (arrayElementInfo.getArrayFields()).arrayLength(); // assumed concrete

		if (!threadInfo.isFirstStepInsn()) {
			PCChoiceGenerator arrayChoiceGenerator = new PCChoiceGenerator(0, len + 1); // add
																						// 2
																						// error
																						// cases:
																						// <0,
																						// >=len
			threadInfo.getVM().getSystemState().setNextChoiceGenerator(arrayChoiceGenerator);

			// ti.reExecuteInstruction();
			if (SymbolicInstructionFactory.debugMode) {
				System.out.println("# array cg registered: " + arrayChoiceGenerator);
			}
			
			return this;
		} else { 
			// index = frame.peek();
			PCChoiceGenerator lastChoiceGenerator = threadInfo.getVM().getSystemState()
					.getLastChoiceGeneratorOfType(PCChoiceGenerator.class);
			assert (lastChoiceGenerator != null);
			PCChoiceGenerator prevChoiceGenerator = lastChoiceGenerator.getPreviousChoiceGeneratorOfType(PCChoiceGenerator.class);

			index = lastChoiceGenerator.getNextChoice();
			IntegerExpression symIndex = (IntegerExpression) peekIndexAttr(threadInfo);
			// check the constraint

			PathCondition pathCondition;

			if (prevChoiceGenerator == null) {
				pathCondition = new PathCondition();
			} else {
				pathCondition = ((PCChoiceGenerator) prevChoiceGenerator).getCurrentPC();
			}
			assert pathCondition != null;

			if (index < len) {
				pathCondition._addDet(Comparator.EQ, index, symIndex);
				if (pathCondition.simplify()) {  // satisfiable
					((PCChoiceGenerator) lastChoiceGenerator).setCurrentPC(pathCondition);
				} else {
					threadInfo.getVM().getSystemState().setIgnored(true);  // backtrack
					
					return getNext(threadInfo);
				}
			} else if (index == len) {
				pathCondition._addDet(Comparator.LT, symIndex, 0);
				if (pathCondition.simplify()) {  // satisfiable
					((PCChoiceGenerator) lastChoiceGenerator).setCurrentPC(pathCondition);
					
					return threadInfo.createAndThrowException("java.lang.ArrayIndexOutOfBoundsException");
				} else {
					threadInfo.getVM().getSystemState().setIgnored(true);  // backtrack
					
					return getNext(threadInfo);
				}
			} else if (index == len + 1) {
				pathCondition._addDet(Comparator.GE, symIndex, len);
				if (pathCondition.simplify()) { // satisfiable
					((PCChoiceGenerator) lastChoiceGenerator).setCurrentPC(pathCondition);
					
					return threadInfo.createAndThrowException("java.lang.ArrayIndexOutOfBoundsException");
				} else {
					threadInfo.getVM().getSystemState().setIgnored(true);  // backtrack
					
					return getNext(threadInfo);
				}
			}

			// original code for concrete execution

			// int idx = peekIndex(ti);
			int aref = peekArrayRef(threadInfo); // need to be polymorphic, could be LongArrayStore

			arrayOperandAttr = peekArrayAttr(threadInfo);
			indexOperandAttr = peekIndexAttr(threadInfo);

			// --- shared access CG
			/*
			 * ignore POR for now TODO Scheduler scheduler = ti.getScheduler();
			 * if (scheduler.canHaveSharedarrayChoiceGenerator(ti, this, eiArray, idx)){
			 * eiArray = scheduler.updateArraySharedness(ti, eiArray, idx); if
			 * (scheduler.setsSharedarrayChoiceGenerator(ti, this, eiArray, idx)){ return
			 * this; } } }
			 */

			try {
				// setArrayElement(ti, frame, eiArray); 
				// this pops operands
				int elementSize = getElementSize();
				Object attr = elementSize == 1 ? stackFrame.getOperandAttr() : stackFrame.getLongOperandAttr();

				popValue(stackFrame);
				stackFrame.pop();
				// don't set 'arrayRef' before we do the CG checks (would kill
				// loop optimization)
				arrayRef = stackFrame.pop();

				arrayElementInfo = arrayElementInfo.getModifiableInstance();
				setField(arrayElementInfo, index);
				arrayElementInfo.setElementAttrNoClone(index, attr); 
				// TODO: what if the value is the same but not the attr?

			} catch (ArrayIndexOutOfBoundsExecutiveException e) { 
				// at this point, the AIOBX is already processed
				return e.getInstruction();
			}

			return getNext(threadInfo);
		}
	}
}
