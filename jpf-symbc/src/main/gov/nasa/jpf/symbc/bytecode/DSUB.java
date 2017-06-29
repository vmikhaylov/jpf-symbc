/*
 * Copyright (C) 2014, United States Government, as represented by the
 * Administrator of the National Aeronautics and Space Administration.
 * All rights reserved.
 *
 * Symbolic Pathfinder (jpf-symbc) is licensed under the Apache License, 
 * Version 2.0 (the "License"); you may not use this file except
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

//
// Copyright (C) 2006 United States Government as represented by the
// Administrator of the National Aeronautics and Space Administration
// (NASA).  All Rights Reserved.
// 
// This software is distributed under the NASA Open Source Agreement
// (NOSA), version 1.3.  The NOSA has been approved by the Open Source
// Initiative.  See the file NOSA-1.3-JPF at the top of the distribution
// directory tree for the complete NOSA document.
// 
// THE SUBJECT SOFTWARE IS PROVIDED "AS IS" WITHOUT ANY WARRANTY OF ANY
// KIND, EITHER EXPRESSED, IMPLIED, OR STATUTORY, INCLUDING, BUT NOT
// LIMITED TO, ANY WARRANTY THAT THE SUBJECT SOFTWARE WILL CONFORM TO
// SPECIFICATIONS, ANY IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR
// A PARTICULAR PURPOSE, OR FREEDOM FROM INFRINGEMENT, ANY WARRANTY THAT
// THE SUBJECT SOFTWARE WILL BE ERROR FREE, OR ANY WARRANTY THAT
// DOCUMENTATION, IF PROVIDED, WILL CONFORM TO THE SUBJECT SOFTWARE.
//
package gov.nasa.jpf.symbc.bytecode;

import gov.nasa.jpf.symbc.numeric.RealExpression;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.StackFrame;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.Types;

/**
 * Subtract double ..., value1, value2 => ..., result
 */
public class DSUB extends gov.nasa.jpf.jvm.bytecode.DSUB {

	@Override
	public Instruction execute(ThreadInfo threadInfo) {
		StackFrame stackFrame = threadInfo.getModifiableTopFrame();

		RealExpression symDoubleValue1 = (RealExpression) stackFrame.getLongOperandAttr();
		RealExpression symDoubleValue2 = (RealExpression) stackFrame.getLongOperandAttr();
		
		double doubleValue1 = Types.longToDouble(stackFrame.popLong());
		double doubleValue2 = Types.longToDouble(stackFrame.popLong());

		double doubleResult = doubleValue2 - doubleValue1;
		if (symDoubleValue1 == null && symDoubleValue2 == null) {
			stackFrame.pushLong(Types.doubleToLong(doubleResult));
		} else {
			stackFrame.pushLong(0);
		}

		RealExpression symResult = null;
		if (symDoubleValue2 != null) {
			if (symDoubleValue1 != null) {
				symResult = symDoubleValue2._minus(symDoubleValue1);
			} else {
				// v1 is concrete
				symResult = symDoubleValue2._minus(doubleValue1);
			}
		} else if (symDoubleValue1 != null) {
			symResult = symDoubleValue1._minus_reverse(doubleValue2);
		}

		stackFrame.setLongOperandAttr(symResult);

		return getNext(threadInfo);
	}
}
