/*    
    Copyright (C) Paul Falstad and Iain Sharp
    
    This file is part of CircuitJS1.

    CircuitJS1 is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 2 of the License, or
    (at your option) any later version.

    CircuitJS1 is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with CircuitJS1.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.lushprojects.circuitjs1.client;

class Inductor {
    public static final int FLAG_BACK_EULER = 2;
    int nodes[];
    int flags;
    CirSim sim;
    
    double inductance;
    double compResistance, current;
    double curSourceValue;
    Inductor(CirSim s) {
	sim = s;
	nodes = new int[2];
    }
    void setup(double ic, double cr, int f) {
	inductance = ic;
	current = cr;
	flags = f;
    }
    boolean isTrapezoidal() { return (flags & FLAG_BACK_EULER) == 0; }
    void reset() { resetTo(0); }
    void resetTo(double c) {
	// 需要在此处设置 curSourceValue，以防电感的某个节点是节点 0。在这种情况下，
	// 分析电路时可能会（从 setNodeVoltage()）调用 calculateCurrent()，而
	// startIteration() 此时尚未被调用
	curSourceValue = current = c;
    }
    void stamp(int n0, int n1) {
	// 电感的伴随模型使用梯形法或后向欧拉法
	// 近似（诺顿等效），由一个电流源
	// 与一个电阻并联组成。梯形法比后向欧拉法更
	// 精确，但可能引起振荡行为。
	// 在含有开关的电路中，这种振荡是一个实际问题。
	nodes[0] = n0;
	nodes[1] = n1;
	if (isTrapezoidal())
	    compResistance = 2*inductance/sim.timeStep;
	else // 后向欧拉
	    compResistance = inductance/sim.timeStep;
	sim.stampResistor(nodes[0], nodes[1], compResistance);
	sim.stampRightSide(nodes[0]);
	sim.stampRightSide(nodes[1]);
    }
    boolean nonLinear() { return false; }

    void startIteration(double voltdiff) {
	if (isTrapezoidal())
	    curSourceValue = voltdiff/compResistance+current;
	else // 后向欧拉
	    curSourceValue = current;
    }
    
    double calculateCurrent(double voltdiff) {
	// 我们检查 compResistance，因为此方法可能在
	// stamp() 之前被调用，而 stamp() 负责设置 compResistance，
	// 否则会导致电流无限大
	if (compResistance > 0)
	    current = voltdiff/compResistance + curSourceValue;
	return current;
    }
    void doStep(double voltdiff) {
	sim.stampCurrentSource(nodes[0], nodes[1], curSourceValue);
    }
}
