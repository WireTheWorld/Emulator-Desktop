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

    class TappedTransformerElm extends CircuitElm {
	double inductance, ratio, couplingCoef;
	int flip;
	public static final int FLAG_FLIP = 1;
	Point ptEnds[], ptCoil[], ptCore[];
	double current[], curcount[];
	public TappedTransformerElm(int xx, int yy) {
	    super(xx, yy);
	    inductance = 4;
	    ratio = 1;
	    noDiagonal = true;
	    couplingCoef = .99;
	    current  = new double[4];
	    curcount = new double[4];
	    voltdiff = new double[3];
	    curSourceValue = new double[3];
	    a = new double[9];
	}
	public TappedTransformerElm(int xa, int ya, int xb, int yb, int f,
			      StringTokenizer st) {
	    super(xa, ya, xb, yb, f);
	    inductance = new Double(st.nextToken()).doubleValue();
	    ratio = new Double(st.nextToken()).doubleValue();
	    current  = new double[4];
	    curcount = new double[4];
	    current[0] = new Double(st.nextToken()).doubleValue();
	    current[1] = new Double(st.nextToken()).doubleValue();
	    try {
		current[2] = new Double(st.nextToken()).doubleValue();
	    } catch (Exception e) { }
	    couplingCoef = .99;
	    try {
		couplingCoef = new Double(st.nextToken()).doubleValue();
	    } catch (Exception e) { }
	    voltdiff = new double[3];
	    curSourceValue = new double[3];
	    noDiagonal = true;
	    a = new double[9];
	}
	int getDumpType() { return 169; }
	String dump() {
	    return super.dump() + " " + inductance + " " + ratio + " " +
		current[0] + " " + current[1] + " " + current[2] + " " + couplingCoef;
	}
	void draw(Graphics g) {
	    int i;
	    for (i = 0; i != 5; i++) {
		setVoltageColor(g, volts[i]);
		drawThickLine(g, ptEnds[i], ptCoil[i]);
	    }
	    for (i = 0; i != 4; i++) {
		if (i == 1)
		    continue;
		setPowerColor(g, current[i]*(volts[i]-volts[i+1]));
		drawCoil(g, i > 1 ? -6*flip : 6*flip,
			 ptCoil[i], ptCoil[i+1], volts[i], volts[i+1]);
	    }
	    g.setColor(needsHighlight() ? selectColor : lightGrayColor);
	    for (i = 0; i != 4; i += 2) {
		drawThickLine(g, ptCore[i], ptCore[i+1]);
	    }
	    for (i = 0; i != 4; i++)
		curcount[i] = updateDotCount(current[i], curcount[i]);

	    // 初级绕组圆点
	    drawDots(g, ptEnds[0], ptCoil[0], curcount[0]);
	    drawDots(g, ptCoil[0], ptCoil[1], curcount[0]);
	    drawDots(g, ptCoil[1], ptEnds[1], curcount[0]);

	    // 次级绕组圆点
	    drawDots(g, ptEnds[2], ptCoil[2], curcount[1]);
	    drawDots(g, ptCoil[2], ptCoil[3], curcount[1]);
	    drawDots(g, ptCoil[3], ptEnds[3], curcount[3]);
	    drawDots(g, ptCoil[3], ptCoil[4], curcount[2]);
	    drawDots(g, ptCoil[4], ptEnds[4], curcount[2]);
	    
	    drawPosts(g);
	    setBbox(ptEnds[0], ptEnds[4], 0);
	}
	
	void setPoints() {
	    super.setPoints();
	    flip = hasFlag(FLAG_FLIP) ? -1 : 1;
	    int hs = 32*flip;
	    ptEnds = newPointArray(5);
	    ptCoil = newPointArray(5);
	    ptCore = newPointArray(4);
	    ptEnds[0] = point1;
	    ptEnds[2] = point2;
	    interpPoint(point1, point2, ptEnds[1], 0, -hs*2);
	    interpPoint(point1, point2, ptEnds[3], 1, -hs);
	    interpPoint(point1, point2, ptEnds[4], 1, -hs*2);
	    double ce = .5-12/dn;
	    double cd = .5-2/dn;
	    int i;
	    interpPoint(ptEnds[0], ptEnds[2], ptCoil[0], ce);
	    interpPoint(ptEnds[0], ptEnds[2], ptCoil[1], ce, -hs*2);
	    interpPoint(ptEnds[0], ptEnds[2], ptCoil[2], 1-ce);
	    interpPoint(ptEnds[0], ptEnds[2], ptCoil[3], 1-ce, -hs);
	    interpPoint(ptEnds[0], ptEnds[2], ptCoil[4], 1-ce, -hs*2);
	    for (i = 0; i != 2; i++) {
		int b = -hs*i*2;
		interpPoint(ptEnds[0], ptEnds[2], ptCore[i],   cd,   b);
		interpPoint(ptEnds[0], ptEnds[2], ptCore[i+2], 1-cd, b);
	    }
	}
	Point getPost(int n) {
	    return ptEnds[n];
	}
	int getPostCount() { return 5; }
	void reset() {
	    current[0] = current[1] = current[2] = current[3] = volts[0] = volts[1] = volts[2] =
		volts[3] = volts[4] = curcount[0] = curcount[1] = curcount[2] = 0;
	    // 需要在此设置电流源值，以防某个节点是节点 0。在这种情况下，
	    // 分析电路时可能会（从 setNodeVoltage()）调用 calculateCurrent()，而
	    // startIteration() 尚未被调用
	    curSourceValue[0] = curSourceValue[1] = curSourceValue[2] = 0;
	}
	double a[];
	void stamp() {
	    // 变压器的方程：
	    //   v1 = L1 di1/dt + M1 di2/dt + M1 di3/dt
	    //   v2 = M1 di1/dt + L2 di2/dt + M2 di3/dt
	    //   v3 = M1 di1/dt + M2 di2/dt + L2 di3/dt
	    // 我们对其求逆得到：
	    //   di1/dt = a1 v1 + a2 v2 + a3 v3
	    //   di2/dt = a4 v1 + a5 v2 + a6 v3
	    //   di3/dt = a7 v1 + a8 v2 + a9 v3
	    // 使用梯形法近似对 di1/dt 积分，得到：
	    //   i1(t2) = i1(t1) + dt/2 (i1(t1) + i1(t2))
	    //          = i1(t1) + a1 dt/2 v1(t1)+a2 dt/2 v2(t1)+a3 dt/2 v3(t1) +
	    //                     a1 dt/2 v1(t2)+a2 dt/2 v2(t2)+a3 dt/2 v3(t2)
	    // 其 i1 的诺顿等效为：
	    //  a. 电流源，I = i1(t1) + a1 dt/2 v1(t1) + a2 dt/2 v2(t1)
	    //                                + a3 dt/2 v3(t1)
	    //  b. 电阻，G = a1 dt/2
	    //  c. 由电压 v2 控制的电流源，G = a2 dt/2
	    //  d. 由电压 v3 控制的电流源，G = a3 dt/2
	    // i2、i3 同理
	    // 
	    // 第一个绕组从节点 0 到 1，第二个绕组从 2 到 3 再到 4
	    double l1 = inductance;
	    // 第二个绕组被分成两半，因此每部分只有一半的匝数；
	    // 我们将 1/2 平方，即除以 4
	    double l2 = inductance*ratio*ratio/4;
	    double m1 = couplingCoef*Math.sqrt(l1*l2);
	    // 第二个绕组两半之间的互感
	    // 等于其中任何一半的自感（略小，
	    // 因为耦合不是理想的）
	    double m2 = couplingCoef*l2;
	    // 加载预求逆的矩阵
	    a[0] = l2+m2;
	    a[1] = a[2] = a[3] = a[6] = -m1;
	    a[4] = a[8] = (l1*l2-m1*m1)/(l2-m2);
	    a[5] = a[7] = (m1*m1-l1*m2)/(l2-m2);
	    int i;
	    double det = l1*(l2+m2)-2*m1*m1;
	    for (i = 0; i != 9; i++)
		a[i] *= (isTrapezoidal() ? sim.timeStep/2 : sim.timeStep)/det;
	    sim.stampConductance(nodes[0], nodes[1], a[0]);
	    sim.stampVCCurrentSource(nodes[0], nodes[1], nodes[2], nodes[3], a[1]);
	    sim.stampVCCurrentSource(nodes[0], nodes[1], nodes[3], nodes[4], a[2]);
	    
	    sim.stampVCCurrentSource(nodes[2], nodes[3], nodes[0], nodes[1], a[3]);
	    sim.stampConductance    (nodes[2], nodes[3], a[4]);
	    sim.stampVCCurrentSource(nodes[2], nodes[3], nodes[3], nodes[4], a[5]);
	    
	    sim.stampVCCurrentSource(nodes[3], nodes[4], nodes[0], nodes[1], a[6]);
	    sim.stampVCCurrentSource(nodes[3], nodes[4], nodes[2], nodes[3], a[7]);
	    sim.stampConductance    (nodes[3], nodes[4], a[8]);

	    for (i = 0; i != 5; i++)
		sim.stampRightSide(nodes[i]);
	}
	boolean isTrapezoidal() { return (flags & Inductor.FLAG_BACK_EULER) == 0; }
	void startIteration() {
	    voltdiff[0] = volts[0]-volts[1];
	    voltdiff[1] = volts[2]-volts[3];
	    voltdiff[2] = volts[3]-volts[4];
	    int i, j;
	    for (i = 0; i != 3; i++) {
		curSourceValue[i] = current[i];
		if (isTrapezoidal())
		    for (j = 0; j != 3; j++)
			curSourceValue[i] += a[i*3+j]*voltdiff[j];
	    }
	}
	double curSourceValue[], voltdiff[];
	void doStep() {
	    sim.stampCurrentSource(nodes[0], nodes[1], curSourceValue[0]);
	    sim.stampCurrentSource(nodes[2], nodes[3], curSourceValue[1]);
	    sim.stampCurrentSource(nodes[3], nodes[4], curSourceValue[2]);
 	}
	void calculateCurrent() {
	    voltdiff[0] = volts[0]-volts[1];
	    voltdiff[1] = volts[2]-volts[3];
	    voltdiff[2] = volts[3]-volts[4];
	    int i, j;
	    for (i = 0; i != 3; i++) {
		current[i] = curSourceValue[i];
		for (j = 0; j != 3; j++)
		    current[i] += a[i*3+j]*voltdiff[j];
	    }
	    // 计算抽头引线的电流
	    current[3] = current[1]-current[2];
	}
	void getInfo(String arr[]) {
	    arr[0] = "transformer";
	    arr[1] = "L = " + getUnitText(inductance, "H");
	    arr[2] = "Ratio = 1:" + ratio;
	    //arr[3] = "I1 = " + getCurrentText(current1);
	    arr[3] = "Vd1 = " + getVoltageText(volts[0]-volts[2]);
	    //arr[5] = "I2 = " + getCurrentText(current2);
	    arr[4] = "Vd2 = " + getVoltageText(volts[1]-volts[3]);
	}
	@Override double getCurrentIntoNode(int n) {
	    if (n == 0)
		return -current[0];
	    if (n == 1)
		return current[0];
	    if (n == 2)
		return -current[1];
	    if (n == 3)
		return current[3];
	    return current[2];
	}
	boolean getConnection(int n1, int n2) {
	    if (comparePair(n1, n2, 0, 1))
		return true;
	    if (comparePair(n1, n2, 2, 3))
		return true;
	    if (comparePair(n1, n2, 3, 4))
		return true;
	    if (comparePair(n1, n2, 2, 4))
		return true;
	    return false;
	}
	public EditInfo getEditInfo(int n) {
	    if (n == 0)
		return new EditInfo("Primary Inductance (H)", inductance, .01, 5);
	    if (n == 1)
		return new EditInfo("Ratio (N1/N2)", 1/ratio, 1, 10).setDimensionless();
	    if (n == 2)
		return new EditInfo("Coupling Coefficient", couplingCoef, 0, 1).setDimensionless();
	    if (n == 3) {
		EditInfo ei = new EditInfo("", 0, -1, -1);
		ei.checkbox = new Checkbox("Trapezoidal Approximation",
					   isTrapezoidal());
		return ei;
	    }
	    return null;
	}
	public void setEditValue(int n, EditInfo ei) {
	    if (n == 0 && ei.value > 0)
		inductance = ei.value;
	    if (n == 1 && ratio > 0)
		ratio = 1/ei.value;
	    if (n == 2 && ei.value > 0 && ei.value < 1)
		couplingCoef = ei.value;
	    if (n == 3) {
		if (ei.checkbox.getState())
		    flags &= ~Inductor.FLAG_BACK_EULER;
		else
		    flags |= Inductor.FLAG_BACK_EULER;
	    }
	}

	void flipX(int c2, int count) {
	    flags ^= FLAG_FLIP;
	    super.flipX(c2, count);
	}
	void flipY(int c2, int count) {
	    flags ^= FLAG_FLIP;
	    super.flipY(c2, count);
	}
	void flipXY(int c2, int count) {
	    flags ^= FLAG_FLIP;
	    super.flipXY(c2, count);
	}
    }
