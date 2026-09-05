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

// 由 Edward Calver 贡献

class TriStateElm extends CircuitElm {
    double resistance, r_on, r_off, r_off_ground, highVoltage;

    // 不幸的是，我们需要全部三个标志来跟踪翻转。
    // 如果元件长度为奇数个网格，FLAG_FLIP_X/Y 会影响取整方向。
    // FLAG_FLIP 不会。
    final int FLAG_FLIP = 1;
    final int FLAG_FLIP_X = 2;
    final int FLAG_FLIP_Y = 4;

    public TriStateElm(int xx, int yy) {
	super(xx, yy);
	r_on = 0.1;
	r_off = 1e10;
	r_off_ground = 1e8;
	noDiagonal = true;
            
        // 从上次编辑的门电路复制默认值
        highVoltage = GateElm.lastHighVoltage;
    }

    public TriStateElm(int xa, int ya, int xb, int yb, int f, StringTokenizer st) {
	super(xa, ya, xb, yb, f);
	r_on = 0.1;
	r_off = 1e10;
	r_off_ground = 0;
	noDiagonal = true;
	highVoltage = 5;
	try {
	    r_on = new Double(st.nextToken()).doubleValue();
	    r_off = new Double(st.nextToken()).doubleValue();
	    r_off_ground = new Double(st.nextToken()).doubleValue();
            highVoltage = new Double (st.nextToken()).doubleValue();
	} catch (Exception e) {
	}

    }

    String dump() {
	return super.dump() + " " + r_on + " " + r_off + " " + r_off_ground + " " + highVoltage;
    }

    int getDumpType() {
	return 180;
    }

    boolean open;

    Point ps, point3, lead3;

    Polygon gatePoly;

    void setPoints() {
	super.setPoints();
	int len = 32;
	calcLeads(len);
	adjustLeadsToGrid((flags & FLAG_FLIP_X) != 0, (flags & FLAG_FLIP_Y) != 0);

	ps = new Point();
	int hs = 16;

	int ww = 16;
	if (ww > dn / 2)
	    ww = (int) (dn / 2);
	Point triPoints[] = newPointArray(3);
	interpPoint2(lead1, lead2, triPoints[0], triPoints[1], 0, hs + 2);
	triPoints[2] = interpPoint(lead1, lead2, .5 + (ww - 2) / (double)len);
	gatePoly = createPolygon(triPoints);

	int sign = ((flags & FLAG_FLIP) == 0) ? -1 : 1;
	point3 = interpPoint(lead1, lead2, .5, sign*hs);
	lead3 = interpPoint(lead1, lead2, .5, sign*hs/2);
    }

    void draw(Graphics g) {
	int hs = 16;
	setBbox(point1, point2, hs);

	draw2Leads(g);

	g.setColor(lightGrayColor);
	drawThickPolygon(g, gatePoly);
	setVoltageColor(g, volts[2]);
	drawThickLine(g, point3, lead3);
	curcount = updateDotCount(current, curcount);
	drawDots(g, lead2, point2, curcount);
	drawPosts(g);
    }

    void calculateCurrent() {
	// 从节点 3 到节点 1 的电流
	double current31 = (volts[3]-volts[1])/resistance;
	
	// 从节点 1 经下拉电阻的电流
	double current10 = (r_off_ground == 0) ? 0 : volts[1]/r_off_ground;

	// 输出电流是这两者的差值
	current = current31-current10;
    }

    double getCurrentIntoNode(int n) {
	if (n == 1)
	    return current;
	return 0;
    }

    // 我们需要这个以便能在每一步更改矩阵
    boolean nonLinear() {
	return true;
    }

    // 节点 0：输入
    // 节点 1：输出
    // 节点 2：控制输入
    // 节点 3：内部节点
    // 有一个电压源连接到节点 3，还有一个电阻（r_off 或 r_on）从节点 3 连接到节点 1。
    // 然后有一个下拉电阻从节点 1 连接到地。
    void stamp() {
	sim.stampVoltageSource(0, nodes[3], voltSource);
	sim.stampNonLinear(nodes[3]);
	sim.stampNonLinear(nodes[1]);
    }

    void doStep() {
	open = (volts[2] < highVoltage*.5);
	resistance = (open) ? r_off : r_on;
	sim.stampResistor(nodes[3], nodes[1], resistance);
	
	// 为输出添加下拉电阻，这样当没有其他器件驱动输出时，
	// 禁用的三态缓冲器输出会接近地电位。否则会让人困惑。
	if (r_off_ground > 0)
	    sim.stampResistor(nodes[1], 0, r_off_ground);
	
	sim.updateVoltageSource(0, nodes[3], voltSource, volts[0] > highVoltage*.5 ? highVoltage : 0);
    }

    void drag(int xx, int yy) {
	// 用鼠标选择缓冲器使能端应位于哪一侧
	boolean flip = (xx < x) == (yy < y);
	
	xx = sim.snapGrid(xx);
	yy = sim.snapGrid(yy);
	if (abs(x - xx) < abs(y - yy))
	    xx = x;
	else {
	    flip = !flip;
	    yy = y;
	}
	flags = flip ? (flags | FLAG_FLIP) : (flags & ~FLAG_FLIP);
	super.drag(xx, yy);
    }

    int getPostCount() {
	return 3;
    }
    
    int getInternalNodeCount() {
	return 1;
    }

    int getVoltageSourceCount() {
	return 1;
    }

    Point getPost(int n) {
	return (n == 0) ? point1 : (n == 1) ? point2 : point3;
    }

    void getInfo(String arr[]) {
	arr[0] = "tri-state buffer";
	arr[1] = open ? "open" : "closed";
	arr[2] = "Vd = " + getVoltageDText(getVoltageDiff());
	arr[3] = "I = " + getCurrentDText(getCurrent());
	arr[4] = "Vc = " + getVoltageText(volts[2]);
    }

    // 输入没有电流通路，但存在
    // 一条经过输出到地的间接通路。
    boolean getConnection(int n1, int n2) {
	return false;
    }

    boolean hasGroundConnection(int n1) {
	return (n1 == 1);
    }

    public EditInfo getEditInfo(int n) {
	if (n == 0)
	    return new EditInfo("On Resistance (ohms)", r_on, 0, 0);
	if (n == 1)
	    return new EditInfo("Off Resistance (ohms)", r_off, 0, 0);
	if (n == 2)
	    return new EditInfo("Output Pulldown Resistance (ohms)", r_off_ground, 0, 0);
        if (n == 3)
            return new EditInfo("High Logic Voltage", highVoltage, 1, 10);
	return null;
    }

    public void setEditValue(int n, EditInfo ei) {

	if (n == 0 && ei.value > 0)
	    r_on = ei.value;
	if (n == 1 && ei.value > 0)
	    r_off = ei.value;
	if (n == 2 && ei.value > 0)
	    r_off_ground = ei.value;
	if (n == 3)
            highVoltage = GateElm.lastHighVoltage = ei.value;
    }

    void flipX(int c2, int count) {
	flags ^= FLAG_FLIP|FLAG_FLIP_X;
	super.flipX(c2, count);
    }

    void flipY(int c2, int count) {
	flags ^= FLAG_FLIP|FLAG_FLIP_Y;
	super.flipY(c2, count);
    }

    void flipXY(int c2, int count) {
	flags ^= FLAG_FLIP;
	super.flipXY(c2, count);
    }
}

