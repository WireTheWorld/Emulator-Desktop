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

// 可嵌入其他元件中的二极管。串联电阻在 DiodeElm 中处理，不在这里处理。
class Diode {
    int nodes[];
    CirSim sim;
    
    Diode(CirSim s) {
	sim = s;
	nodes = new int[2];
    }
    void setup(DiodeModel model) {
	leakage = model.saturationCurrent;
	zvoltage = model.breakdownVoltage;
	vscale = model.vscale;
	vdcoef = model.vdcoef;
	
//	sim.console("setup " + leakage + " " + zvoltage + " " + model.emissionCoefficient + " " +  vdcoef);

	// 用于限制的临界电压；在此电压下
	// 电流为 vscale/sqrt(2)
	vcrit = vscale * Math.log(vscale/(Math.sqrt(2)*leakage));
	// 平移后的、用于齐纳击穿区限制的*正*临界电压；
	// limitstep() 以类似于 vcrit 的方式将其用于平移后的电压。
	vzcrit = vt * Math.log(vt/(Math.sqrt(2)*leakage));
	if (zvoltage == 0)
	    zoffset = 0;
	else {
	    // 计算在 zvoltage 下产生 5mA 电流所需的偏移量
	    double i = -.005;
	    zoffset = zvoltage-Math.log(-(1+i/leakage))/vzcoef;
	}
    }
	
    void setupForDefaultModel() {
	setup(DiodeModel.getDefaultModel());
    }
    
    void reset() {
	lastvoltdiff = 0;
    }
	
    // SPICE 默认温度 27 C (300.15 K) 下的电子热电压：
    static final double vt = 0.025865;
    // 二极管的“标度电压”，即使电流增大 e 倍所需的电压增量。
    double vscale;
    // 除以 vscale 的乘法等效形式（为了速度）。
    double vdcoef;
    // 齐纳击穿曲线用一条更陡的指数曲线表示，类似于理想的
    // Shockley 曲线，但经过翻转和平移。该曲线消除了 emcoef 的调节影响，
    // 用 vt 和 vzcoef 取代 vscale 和 vdcoef。
    // vzcoef 是除以 vt 的乘法等效形式（为了速度）。
    static final double vzcoef = 1 / vt;
    // 用户指定的二极管正向压降和齐纳电压参数。
    double fwdrop, zvoltage;
    // 二极管电流的标度因子，由用户指定的正向压降计算得出。
    double leakage;
    // 齐纳击穿指数曲线的电压偏移量，由用户指定的齐纳电压计算得出。
    double zoffset;
    // 用于限制普通二极管和齐纳击穿指数曲线的临界电压。
    double vcrit, vzcrit;
    double lastvoltdiff;
    
    double limitStep(double vnew, double vold) {
	double arg;
	double oo = vnew;

	// 检查新电压；电流是否改变了 e^2 倍？
	if (vnew > vcrit && Math.abs(vnew - vold) > (vscale + vscale)) {
	    if(vold > 0) {
		arg = 1 + (vnew - vold) / vscale;
		if(arg > 0) {
		    // 调整 vnew，使电流与上一次迭代的
		    // 线性化模型中的电流相同。
		    // vnew 处的电流 = 旧电流 * arg
		    vnew = vold + vscale * Math.log(arg);
		} else {
		    vnew = vcrit;
		}
	    } else {
		// 调整 vnew，使电流与上一次迭代的
		// 线性化模型中的电流相同。
		// (1/vscale = 负载线斜率)
		vnew = vscale *Math.log(vnew/vscale);
	    }
	    sim.converged = false;
	    //System.out.println(vnew + " " + oo + " " + vold);
	} else if (vnew < 0 && zoffset != 0) {
	    // 对于齐纳击穿，使用相同的逻辑但平移这些值，
	    // 并用齐纳专用值替换普通值，以
	    // 适应我们齐纳击穿曲线更陡的指数形式。
	    vnew = -vnew - zoffset;
	    vold = -vold - zoffset;
	    
	    if (vnew > vzcrit && Math.abs(vnew - vold) > (vt + vt)) {
		if(vold > 0) {
		    arg = 1 + (vnew - vold) / vt;
		    if(arg > 0) {
			vnew = vold + vt * Math.log(arg);
			//System.out.println(oo + " " + vnew);
		    } else {
			vnew = vzcrit;
		    }
		} else {
		    vnew = vt *Math.log(vnew/vt);
		}
		sim.converged = false;
	    }
	    vnew = -(vnew+zoffset);
	}
	return vnew;
    }
    
    void stamp(int n0, int n1) {
	nodes[0] = n0;
	nodes[1] = n1;
	sim.stampNonLinear(nodes[0]);
	sim.stampNonLinear(nodes[1]);
    }
    
    void doStep(double voltdiff) {
	// 这里原来用 .1，但峰值检测器需要 .01
	if (Math.abs(voltdiff-lastvoltdiff) > .01)
	    sim.converged = false;
	voltdiff = limitStep(voltdiff, lastvoltdiff);
	lastvoltdiff = voltdiff;

	// 为防止可能出现奇异矩阵或其他数值问题，在每个 P-N 结上
	// 并联一个极小的电导。
	double gmin = leakage * 0.01;
	if (sim.subIterations > 100) {
	    // 如果收敛困难，就在二极管上并联一个电导。
	    // 每次迭代逐渐增大电导值。
	    gmin = Math.exp(-9*Math.log(10)*(1-sim.subIterations/3000.));
	    if (gmin > .1)
		gmin = .1;
	}

	if (voltdiff >= 0 || zvoltage == 0) {
	    // 普通二极管或正向偏置的齐纳二极管
	    double eval = Math.exp(voltdiff*vdcoef);
	    double geq = vdcoef*leakage*eval + gmin;
	    double nc = (eval-1)*leakage - geq*voltdiff;
	    sim.stampConductance(nodes[0], nodes[1], geq);
	    sim.stampCurrentSource(nodes[0], nodes[1], nc);
	} else {
	    // 齐纳二极管
	    
	    // 对于反向偏置的齐纳二极管，用与理想 Shockley 曲线类似的
	    // 指数曲线来模拟齐纳击穿曲线。（真实的击穿曲线
	    // 不是简单的指数曲线，但这个近似应该没问题。）

	    /* 
	     * I(Vd) = Is * (exp[Vd*C] - exp[(-Vd-Vz)*Cz] - 1 )
	     *
	     * geq 是 I'(Vd)
	     * nc 是 I(Vd) + I'(Vd)*(-Vd)
	     */

	    double geq = leakage* ( 
		vdcoef*Math.exp(voltdiff*vdcoef) + vzcoef*Math.exp((-voltdiff-zoffset)*vzcoef)
		) + gmin;

	    double nc = leakage* (
		Math.exp(voltdiff*vdcoef) 
		- Math.exp((-voltdiff-zoffset)*vzcoef) 
		- 1
		) + geq*(-voltdiff);

	    sim.stampConductance(nodes[0], nodes[1], geq);
	    sim.stampCurrentSource(nodes[0], nodes[1],  nc);
	}
    }
    
    double calculateCurrent(double voltdiff) {
	if (voltdiff >= 0 || zvoltage == 0)
	    return leakage*(Math.exp(voltdiff*vdcoef)-1);
	return leakage* (
	    Math.exp(voltdiff*vdcoef)  
	    - Math.exp((-voltdiff-zoffset)*vzcoef)  
	    - 1
	    );
    }
}
