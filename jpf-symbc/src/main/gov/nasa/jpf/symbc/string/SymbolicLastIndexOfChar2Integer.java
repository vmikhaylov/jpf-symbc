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

package gov.nasa.jpf.symbc.string;

import gov.nasa.jpf.symbc.numeric.IntegerExpression;
import gov.nasa.jpf.symbc.numeric.SymbolicInteger;

public class SymbolicLastIndexOfChar2Integer extends SymbolicInteger{
	StringExpression source; 
	IntegerExpression expression, minDist;
	
	public SymbolicLastIndexOfChar2Integer (String name, int l, int u, StringExpression source, IntegerExpression expression, IntegerExpression minDist) {
		super (name, l, u);
		this.source = source;
		this.expression = expression;
		this.minDist = minDist;
	}
	
	public StringExpression getSource() {
		return source;
	}
	
	public IntegerExpression getExpression () {
		return expression;
	}
	
	public IntegerExpression getMinDist() {
		return minDist;
	}

}
