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

import com.google.gwt.user.client.ui.Button;
import com.lushprojects.circuitjs1.client.util.Locale;

// 0 = 开关
// 1 = 开关端 1
// 2 = 开关端 2
// ...
// 3n   = 线圈
// 3n+1 = 线圈
// 3n+2 = 线圈电阻端

class RelayElm extends CircuitElm {
    final int FLAG_SWAP_COIL = 1;
    final int FLAG_SHOW_BOX = 2;
    final int FLAG_BOTH_SIDES_COIL = 4;
    final int FLAG_FLIP = 8;
	
    double inductance;
    Inductor ind;
    double r_on, r_off, onCurrent, offCurrent;
    Point coilPosts[], coilLeads[], swposts[][], swpoles[][], ptSwitch[];
    Point lines[];
    Point outline[] = newPointArray(4);
    double coilCurrent, switchCurrent[], coilCurCount, switchCurCount[];
    
    // 分数位置，介于 0 到 1 之间（含）
    double d_position;
    
    // 整数位置，可为 0（断开）、1（接通）、2（中间状态）
    int i_position;
    
    double coilR;
    
    // 切换时间（秒），旧模型的切换时间不是恒定的，此时为 0
    double switchingTime;
    
    int poleCount;
    int openhs, dflip;
    boolean onState;
    final int nSwitch0 = 0;
    final int nSwitch1 = 1;
    final int nSwitch2 = 2;
    int nCoil1, nCoil2, nCoil3;
    double currentOffset1, currentOffset2;
    
    public RelayElm(int xx, int yy) {
	super(xx, yy);
	ind = new Inductor(sim);
	inductance = .2;
	ind.setup(inductance, 0, Inductor.FLAG_BACK_EULER);
	noDiagonal = true;
	onCurrent = .02;
	offCurrent = .015;
	r_on = .05;
	r_off = 1e6;
	coilR = 20;
	switchingTime = 5e-3;
	coilCurrent = coilCurCount = 0;
	poleCount = 1;
	flags |= FLAG_SHOW_BOX | FLAG_BOTH_SIDES_COIL;
	setupPoles();
    }
    public RelayElm(int xa, int ya, int xb, int yb, int f,
		    StringTokenizer st) {
	super(xa, ya, xb, yb, f);
	poleCount = new Integer(st.nextToken()).intValue();
	inductance = new Double(st.nextToken()).doubleValue();
	coilCurrent = new Double(st.nextToken()).doubleValue();
	r_on = new Double(st.nextToken()).doubleValue();
	r_off = new Double(st.nextToken()).doubleValue();
	onCurrent = new Double(st.nextToken()).doubleValue();
	coilR = new Double(st.nextToken()).doubleValue();
	try {
	    offCurrent = onCurrent;
	    switchingTime = 0;
	    offCurrent = new Double(st.nextToken()).doubleValue();
	    switchingTime = Double.parseDouble(st.nextToken());
	    d_position = i_position = Integer.parseInt(st.nextToken());
	} catch (Exception e) {}	
	if (i_position == 1)
	    onState = true;
	// 中间状态？
	if (i_position == 2)
	    d_position = .5;
	noDiagonal = true;
	ind = new Inductor(sim);
	ind.setup(inductance, coilCurrent, Inductor.FLAG_BACK_EULER);
	setupPoles();
        allocNodes();
    }
    
    void setupPoles() {
	nCoil1 = 3*poleCount;
	nCoil2 = nCoil1+1;
	nCoil3 = nCoil1+2;
	if (switchCurrent == null || switchCurrent.length != poleCount) {
	    switchCurrent = new double[poleCount];
	    switchCurCount = new double[poleCount];
	}
    }
    
    int getDumpType() { return 178; }
    
    String dump() {
	return super.dump() + " " + poleCount + " " +
	    inductance + " " + coilCurrent + " " +
	    r_on + " " + r_off + " " + onCurrent + " " + coilR + " " + offCurrent + " " + switchingTime + " " + i_position;
    }
    
    void draw(Graphics g) {
	int i, p;
	for (i = 0; i != 2; i++) {
	    setVoltageColor(g, volts[nCoil1+i]);
	    drawThickLine(g, coilLeads[i], coilPosts[i]);
	}
	int x = ((flags & FLAG_SWAP_COIL) != 0) ? 1 : 0;
	setPowerColor(g, coilCurrent * (volts[nCoil1]-volts[nCoil2]));
	drawCoil(g, dflip*6, coilLeads[x], coilLeads[1-x],
		 volts[nCoil1+x], volts[nCoil2-x]);

	// 绘制矩形
	if ((flags & FLAG_SHOW_BOX) != 0) {
		g.setColor(needsHighlight() ? selectColor : lightGrayColor);
		drawThickLine(g, outline[0], outline[1]);
		drawThickLine(g, outline[1], outline[2]);
		drawThickLine(g, outline[2], outline[3]);
		drawThickLine(g, outline[3], outline[0]);
	}
	
	// 绘制线条
	g.setColor(Color.darkGray);
	for (i = 0; i != poleCount; i++) {
	    if (i == 0) {
		int off = ((flags & FLAG_BOTH_SIDES_COIL) == 0) ? 0 : 4;
		interpPoint(point1, point2, lines[i*2  ], .5,
			    openhs*2+5*dflip-i*openhs*3+off);
	    } else
		interpPoint(point1, point2, lines[i*2], .5,
			    (int) (openhs*(-i*3+3-.5+d_position))+5*dflip);
	    interpPoint(point1, point2, lines[i*2+1], .5,
			(int) (openhs*(-i*3-.5+d_position))-5*dflip);
	    g.setLineDash(4, 4);
	    g.drawLine(lines[i*2].x, lines[i*2].y, lines[i*2+1].x, lines[i*2+1].y);
	    g.setLineDash(0,  0);
	}
	
	for (p = 0; p != poleCount; p++) {
	    int po = p*3;
	    for (i = 0; i != 3; i++) {
		// 绘制引线
		setVoltageColor(g, volts[nSwitch0+po+i]);
		drawThickLine(g, swposts[p][i], swpoles[p][i]);
	    }
	    
	    interpPoint(swpoles[p][1], swpoles[p][2], ptSwitch[p], d_position);
	    //setVoltageColor(g, volts[nSwitch0]);
	    g.setColor(Color.lightGray);
	    drawThickLine(g, swpoles[p][0], ptSwitch[p]);
	    switchCurCount[p] = updateDotCount(switchCurrent[p],
					       switchCurCount[p]);
	    drawDots(g, swposts[p][0], swpoles[p][0], switchCurCount[p]);
	    
	    if (i_position != 2)
		drawDots(g, swpoles[p][i_position+1], swposts[p][i_position+1],
			 switchCurCount[p]);
	}
	
	coilCurCount = updateDotCount(coilCurrent, coilCurCount);
	
	if (coilCurCount != 0) {
	    drawDots(g, coilPosts[0], coilLeads[0], coilCurCount);
	    drawDots(g, coilLeads[0], coilLeads[1], addCurCount(coilCurCount, currentOffset1));
	    drawDots(g, coilLeads[1], coilPosts[1], addCurCount(coilCurCount, currentOffset2));
	}
	    
	drawPosts(g);
	setBbox(outline[0], outline[2], 0);
	adjustBbox(coilPosts[0], coilPosts[1]);
	adjustBbox(swposts[0][0], swposts[0][1]);
    }
	
    double getCurrentIntoNode(int n) {
	if (n < 3*poleCount) {
	    int p = n/3;
	    int k = n%3;
	    if (k == 0)
		return -switchCurrent[p];
	    if (k == 1+i_position)
		return switchCurrent[p];
	    return 0;
	}
	if (n == 3*poleCount)
	    return -coilCurrent;
	return coilCurrent;
    }

    void setPoints() {
	super.setPoints();
	setupPoles();
	allocNodes();
	dflip = hasFlag(FLAG_FLIP) ? -dsign : dsign;
	openhs = -dflip*16;

	// 开关
	calcLeads(32);
	swposts = new Point[poleCount][3];
	swpoles = new Point[poleCount][3];
	int i, j;
	for (i = 0; i != poleCount; i++) {
	    for (j = 0; j != 3; j++) {
		swposts[i][j] = new Point();
		swpoles[i][j] = new Point();
	    }
	    interpPoint(lead1,  lead2, swpoles[i][0], 0, -openhs*3*i);
	    interpPoint(lead1,  lead2, swpoles[i][1], 1, -openhs*3*i-openhs);
	    interpPoint(lead1,  lead2, swpoles[i][2], 1, -openhs*3*i+openhs);
	    interpPoint(point1, point2, swposts[i][0], 0, -openhs*3*i);
	    interpPoint(point1, point2, swposts[i][1], 1, -openhs*3*i-openhs);
	    interpPoint(point1, point2, swposts[i][2], 1, -openhs*3*i+openhs);
	}

	// 线圈
	coilPosts = newPointArray(2);
	coilLeads   = newPointArray(2);
	ptSwitch = newPointArray(poleCount);

	int x = ((flags & FLAG_SWAP_COIL) != 0) ? 1 : 0;
	int boxSize;
	if ((flags & FLAG_BOTH_SIDES_COIL) == 0) {
	    interpPoint(point1, point2, coilPosts[0],  x, openhs*2);
	    interpPoint(point1, point2, coilPosts[1],  x, openhs*3);
	    interpPoint(point1, point2, coilLeads[0], .5, openhs*2);
	    interpPoint(point1, point2, coilLeads[1], .5, openhs*3);
	    boxSize = 56;
	} else {
	    interpPoint(point1, point2, coilPosts[0], 0, openhs*2);
	    interpPoint(point1, point2, coilPosts[1], 1, openhs*2);
	    interpPoint(point1, point2, coilLeads[0], .5-16/dn, openhs*2);
	    interpPoint(point1, point2, coilLeads[1], .5+16/dn, openhs*2);
	    boxSize = 40;
	}

	// 线条
	lines = newPointArray(poleCount*2);
	
	// 轮廓
	double boxWScale = Math.min(0.4, 25.0 / dn);
	interpPoint(point1, point2, outline[0], 0.5 - boxWScale, -boxSize * dflip);
	interpPoint(point1, point2, outline[1], 0.5 + boxWScale, -boxSize * dflip);
	interpPoint(point1, point2, outline[2], 0.5 + boxWScale, -(openhs*3*poleCount) - (24.0 * dflip));
	interpPoint(point1, point2, outline[3], 0.5 - boxWScale, -(openhs*3*poleCount) - (24.0 * dflip));
	
	currentOffset1 = distance(coilPosts[0], coilLeads[0]);
	currentOffset2 = currentOffset1 + distance(coilLeads[0], coilLeads[1]);
    }
    
    Point getPost(int n) {
	if (n < 3*poleCount)
	    return swposts[n / 3][n % 3];
	return coilPosts[n-3*poleCount];
    }
    int getPostCount() { return 2+poleCount*3; }
    int getInternalNodeCount() { return 1; }
    void reset() {
	super.reset();
	ind.reset();
	coilCurrent = coilCurCount = 0;
	int i;
	for (i = 0; i != poleCount; i++)
	    switchCurrent[i] = switchCurCount[i] = 0;
	d_position = i_position = 0;

	// 保留 onState，否则复位时继电器触发器会处于异常状态。
	// onState = false;
    }
    double a1, a2, a3, a4;
    void stamp() {
	// 从线圈端 1 到内部节点的电感
	ind.stamp(nodes[nCoil1], nodes[nCoil3]);
	// 从内部节点到线圈端 2 的电阻
	sim.stampResistor(nodes[nCoil3], nodes[nCoil2], coilR);

	int i;
	for (i = 0; i != poleCount*3; i++)
	    sim.stampNonLinear(nodes[nSwitch0+i]);
    }
    
    void startIteration() {
	// 使用旧模型？
	if (switchingTime == 0) {
	    startIterationOld();
	    return;
	}
	ind.startIteration(volts[nCoil1]-volts[nCoil3]);
	double absCurrent = Math.abs(coilCurrent);
	
	if (onState) {
	    // 已接通或正在接通。检查是否需要断开
	    if (absCurrent < offCurrent) {
		// 正在断开，将开关置于中间位置
		onState = false;
		i_position = 2;
	    } else {
		d_position += sim.timeStep/switchingTime;
		if (d_position >= 1)
		    d_position = i_position = 1;
	    }
	} else {
	    // 已断开或正在断开。检查是否需要接通
	    if (absCurrent > onCurrent) {
		// 正在接通，将开关置于中间位置
		onState = true;
		i_position = 2;
	    } else {
		d_position -= sim.timeStep/switchingTime;
		if (d_position <= 0)
		    d_position = i_position = 0;
	    }
	    
	}
    }
    
    void startIterationOld() {
	ind.startIteration(volts[nCoil1]-volts[nCoil3]);

	// 用于平衡动作速度与复位速度的魔数，并不完全符合实际
	double magic = 1.3;
	double pmult = Math.sqrt(magic+1);
	double c = onCurrent;
	double p = coilCurrent*pmult/c;
	d_position = Math.abs(p*p) - 1.3;
	if (d_position < 0)
	    d_position = 0;
	if (d_position > 1)
	    d_position = 1;
	if (d_position < .1)
	    i_position = 0;
	else if (d_position > .9)
	    i_position = 1;
	else
	    i_position = 2;
	//System.out.println("ind " + this + " " + current + " " + voltdiff);
    }
    	
    // 需要此方法以便能够每一步都修改矩阵
    boolean nonLinear() { return true; }

    void doStep() {
	double voltdiff = volts[nCoil1]-volts[nCoil3];
	ind.doStep(voltdiff);
	int p;
	for (p = 0; p != poleCount*3; p += 3) {
	    sim.stampResistor(nodes[nSwitch0+p], nodes[nSwitch1+p],
			      i_position == 0 ? r_on : r_off);
	    sim.stampResistor(nodes[nSwitch0+p], nodes[nSwitch2+p],
			      i_position == 1 ? r_on : r_off);
	}
    }
    void calculateCurrent() {
	double voltdiff = volts[nCoil1]-volts[nCoil3];
	coilCurrent = ind.calculateCurrent(voltdiff);

	// 实际上这并不正确，因为开关断开时仍有少量
	// 电流流过开关
	int p;
	for (p = 0; p != poleCount; p++) {
	    if (i_position == 2)
		switchCurrent[p] = 0;
	    else
		switchCurrent[p] =
		    (volts[nSwitch0+p*3]-volts[nSwitch1+p*3+i_position])/r_on;
	}
    }
    void getInfo(String arr[]) {
	arr[0] = Locale.LS("relay");
	if (i_position == 0)
	    arr[0] += " (" + Locale.LS("off") + ")";
	else if (i_position == 1)
	    arr[0] += " (" + Locale.LS("on") + ")";
	if (switchingTime == 0)
	    arr[0] += " (" + Locale.LS("old model") + ")";
	int i;
	int ln = 1;
	for (i = 0; i != poleCount; i++)
	    arr[ln++] = "I" + (i+1) + " = " + getCurrentDText(switchCurrent[i]);
	arr[ln++] = Locale.LS("coil I") + " = " + getCurrentDText(coilCurrent);
	arr[ln++] = Locale.LS("coil Vd") + " = " +
	    getVoltageDText(volts[nCoil1] - volts[nCoil2]);
    }
    public EditInfo getEditInfo(int n) {
	if (n == 0)
	    return new EditInfo("Inductance (H)", inductance, 0, 0);
	if (n == 1)
	    return new EditInfo("On Resistance (ohms)", r_on, 0, 0);
	if (n == 2)
	    return new EditInfo("Off Resistance (ohms)", r_off, 0, 0);
	if (n == 3)
	    return new EditInfo("On Current (A)", onCurrent, 0, 0);
	if (n == 4) {
	    if (switchingTime == 0) {
		// 仍在使用旧模型，因此隐藏不起作用的断开电流。
		// 创建切换到新模型的按钮
                EditInfo ei = new EditInfo("", 0, -1, -1);
                ei.button = new Button(Locale.LS("Use New Model"));
                return ei;
	    }
	    return new EditInfo("Off Current (A)", offCurrent, 0, 0);
	}
	if (n == 5)
	    return new EditInfo("Number of Poles", poleCount, 1, 4).
		setDimensionless();
	if (n == 6)
	    return new EditInfo("Coil Resistance (ohms)", coilR, 0, 0);
	if (n == 7) {
	    int style = 1;
	    if ((flags & FLAG_SWAP_COIL) != 0)
		style = 2;
	    else if ((flags & FLAG_BOTH_SIDES_COIL) != 0)
		style = 0;
	    EditInfo ei = new EditInfo("Coil Style", style, -1, -1);
	    ei.choice = new Choice();
	    ei.choice.add("Both Sides");
	    ei.choice.add("Side 1");
	    ei.choice.add("Side 2");
	    ei.choice.select(style);
	    return ei;
	}
	if (n == 8) {
	    EditInfo ei = new EditInfo("", 0, -1, -1);
	    ei.checkbox = new Checkbox("Show Box", (flags & FLAG_SHOW_BOX) != 0);
	    return ei;
	}
	
	// 仅对新模型显示切换时间，因为对旧模型没有意义
	if (n == 9 && switchingTime > 0)
	    return new EditInfo("Switching Time (s)", switchingTime, 0, 0);
	return null;
    }
    
    public void setEditValue(int n, EditInfo ei) {
	if (n == 0 && ei.value > 0) {
	    inductance = ei.value;
	    ind.setup(inductance, coilCurrent, Inductor.FLAG_BACK_EULER);
	}
	if (n == 1 && ei.value > 0)
	    r_on = ei.value;
	if (n == 2 && ei.value > 0)
	    r_off = ei.value;
	if (n == 3 && ei.value > 0)
	    onCurrent = ei.value;
	if (n == 4) {
	    // 这里可能是“断开电流”的按钮或文本框
	    if (ei.button != null) {
		// 升级到新模型
		switchingTime = 5e-3;
		ei.newDialog = true;
	    } else if (ei.value > 0)
		offCurrent = ei.value;
	}
	if (n == 5 && ei.value >= 1) {
	    poleCount = (int) ei.value;
	    setPoints();
	}
	if (n == 6 && ei.value > 0)
	    coilR = ei.value;
	if (n == 7) {
	    int style = ei.choice.getSelectedIndex();
	    final int styles[] = { FLAG_BOTH_SIDES_COIL, 0, FLAG_SWAP_COIL };
	    flags &= ~(FLAG_SWAP_COIL|FLAG_BOTH_SIDES_COIL);
	    flags |= styles[style];
	    setPoints();
	}
	if (n == 8)
	    flags = ei.changeFlag(flags, FLAG_SHOW_BOX);
	if (n == 9 && ei.value > 0)
	    switchingTime = ei.value;
    }
    
    boolean getConnection(int n1, int n2) {
	return (n1 / 3 == n2 / 3);
    }
    
    int getShortcut() { return 'R'; }

    void flipX(int c2, int count) {
	if (dx == 0)
	    flags ^= FLAG_FLIP;
	super.flipX(c2, count);
    }

    void flipY(int c2, int count) {
	if (dy == 0)
	    flags ^= FLAG_FLIP;
	super.flipY(c2, count);
    }

    void flipXY(int xmy, int count) {
	flags ^= FLAG_FLIP;
	super.flipXY(xmy, count);
    }

}
    
