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

import com.google.gwt.user.client.Window;

class CustomTransformerElm extends CircuitElm {
	double coilCurrents[], coilInductances[], coilCurCounts[], coilCurSourceValues[], coilPolarities[];
	double nodeCurrents[], nodeCurCounts[];
        public static final int FLAG_FLIP = 1;
	int flip;
	
	// 每个线圈第一个节点的节点编号 n（第二个节点为 n+1）
	int coilNodes[];
	
	int coilCount, nodeCount;
	
	// 初级线圈的数量
	int primaryCoils;
	
	Point nodePoints[], nodeTaps[], ptCore[];
	String description;
	double inductance, couplingCoef;
	boolean needDots;
	
	Point dots[];
	int width;
	
	public CustomTransformerElm(int xx, int yy) {
	    super(xx, yy);
	    inductance = 4;
	    width = 32;
	    noDiagonal = true;
	    couplingCoef = .999;
	    description = "1,1:1";
	    parseDescription(description);
	}
	public CustomTransformerElm(int xa, int ya, int xb, int yb, int f,
			      StringTokenizer st) {
	    super(xa, ya, xb, yb, f);
	    width = 32; // max(32, abs(yb-ya));
	    inductance = new Double(st.nextToken()).doubleValue();
	    couplingCoef = new Double(st.nextToken()).doubleValue();
	    String str = st.nextToken();
	    description = CustomLogicModel.unescape(str);
	    coilCount = new Integer(st.nextToken()).intValue();
	    int i;
	    coilCurrents = new double[coilCount];
	    for (i = 0; i != coilCount; i++)
		coilCurrents[i] = new Double(st.nextToken()).doubleValue();
	    noDiagonal = true;
	    parseDescription(description);
	}
	void drag(int xx, int yy) {
	    xx = sim.snapGrid(xx);
	    yy = sim.snapGrid(yy);
//	    width = max(32, abs(yy-y));
	    if (xx == x)
	        yy = y;
	    x2 = xx; y2 = yy;
	    setPoints();
	}
	int getDumpType() { return 406; }
	String dump() {
	    String s = super.dump() + " " + inductance + " " + couplingCoef + " " + CustomLogicModel.escape(description) + " " + coilCount + " ";
	    int i;
	    for (i = 0; i != coilCount; i++) {
		s += coilCurrents[i] + " ";
	    }
	    return s;
	}
	
	void parseDescription() {
	    parseDescription(description);
	}
	
	boolean parseDescription(String desc) {
	    // 数字表示一个线圈（数字 = 相对于基础电感线圈的匝数比）
	    // （负数表示极性相反）
	    // : 分隔初级和次级
	    // , 分隔两个线圈
	    // + 分隔两个相连的线圈（抽头）
	    StringTokenizer st = new StringTokenizer(desc, ",:+", true);
	    
	    // 统计线圈/节点数量
	    coilCount = nodeCount = 0;
	    while (st.hasMoreTokens()) {
		String s = st.nextToken();
		if (s == "+")
		    nodeCount--;
		if (s == "," || s == "+" || s == ":")
		    continue;
		nodeCount += 2;
		coilCount++;
	    }
	    
	    coilNodes = new int[coilCount];
	    coilInductances = new double[coilCount];
	    // 尽可能保存线圈电流（反序列化时需要）
	    if (coilCurrents == null || coilCurrents.length != coilCount)
		coilCurrents = new double[coilCount];
	    coilCurCounts = new double[coilCount];
	    coilCurSourceValues = new double[coilCount];
	    coilPolarities = new double[coilCount];
	    nodePoints = newPointArray(nodeCount);
	    nodeTaps = newPointArray(nodeCount);
	    nodeCurrents = new double[nodeCount];
	    nodeCurCounts = new double[nodeCount];
	    
	    // 重新开始
	    st = new StringTokenizer(desc, ",:+", true);
	    int nodeNum = 0;
	    int coilNum = 0;
	    primaryCoils = 0;
	    boolean secondary = false;
	    needDots = false;
	    while (true) {
		String tok = st.nextToken();
		double n = 0;
		try {
		    n = Double.parseDouble(tok);
		} catch (Exception e) { return false; }
		if (n == 0)
		    return false;
		// 创建新线圈
		coilNodes[coilNum] = nodeNum;
		coilInductances[coilNum] = n*n*inductance;
		coilPolarities[coilNum] = 1;
		if (n < 0) {
		    coilPolarities[coilNum] = -1;
		    needDots = true;
		}
		nodeNum += 2;
		coilNum++;
		if (!secondary)
		    primaryCoils = coilNum;
		if (!st.hasMoreTokens())
		    break;
		tok = st.nextToken();
		if (tok == ",")
		    continue;
		if (tok == "+") {
		    nodeNum--;
		    continue;
		}
		if (tok == ":") {
		    // 切换到次级
		    if (secondary)
			return false;
		    secondary = true;
		    continue;
		}
		return false;
	    }
	    allocNodes();
	    setPoints();
	    xformMatrix = null;
	    return true;
	}
	
	boolean isTrapezoidal() { return (flags & Inductor.FLAG_BACK_EULER) == 0; }
	void draw(Graphics g) {
	    int i;
	    
	    // 绘制抽头
	    for (i = 0; i != getPostCount(); i++) {
		setVoltageColor(g, volts[i]);
		drawThickLine(g, nodePoints[i], nodeTaps[i]);
	    }
	    
	    // 绘制线圈
	    for (i = 0; i != coilCount; i++) {
		int n = coilNodes[i];
		setVoltageColor(g, volts[n]);
		setPowerColor(g, coilCurrents[i]*(volts[n]-volts[n+1]));
		drawCoil(g, (i >= primaryCoils ? -6*flip : 6*flip), nodeTaps[n], nodeTaps[n+1], volts[n], volts[n+1]);
		if (dots != null) {
		    g.setColor(needsHighlight() ? selectColor : lightGrayColor);
		    g.fillOval(dots[i].x-2, dots[i].y-2, 5, 5);
		}
	    }
	    g.setColor(needsHighlight() ? selectColor : lightGrayColor);
	    
	    // 绘制铁芯
	    for (i = 0; i != 2; i++) {
		drawThickLine(g, ptCore[i], ptCore[i+2]);
	    }
	    
	    // 绘制线圈电流
	    for (i = 0; i != coilCount; i++) {
		coilCurCounts[i] = updateDotCount(coilCurrents[i], coilCurCounts[i]);
		int ni = coilNodes[i];
		drawDots(g, nodeTaps[ni], nodeTaps[ni+1], coilCurCounts[i]);
	    }
	    
	    // 绘制抽头电流
	    for (i = 0; i != nodeCount; i++) {
		nodeCurCounts[i] = updateDotCount(nodeCurrents[i], nodeCurCounts[i]);
		drawDots(g, nodePoints[i], nodeTaps[i], nodeCurCounts[i]);
	    }
	    
	    drawPosts(g);
	    setBbox(nodePoints[0], nodePoints[nodeCount-1], 0);
	    adjustBbox(ptCore[0], ptCore[3]);
	}
	
	void setPoints() {
	    super.setPoints();
	    point2.y = point1.y;
	    flip = hasFlag(FLAG_FLIP) ? -1 : 1;
	    int i;
	    int primaryNodes = (primaryCoils == coilCount) ? nodeCount : coilNodes[primaryCoils];
	    dn = Math.abs(point1.x-point2.x);
	    double ce = .5-12/dn;
	    double cd = .5-2/dn;
	    double maxWidth = 0;
	    int step;
	    for (step = 0; step != 2; step++) {
		int c = 0;
		double offset = 0;
		for (i = 0; i != nodeCount; i++) {
		    if (i == primaryNodes)
			offset = 0;
		    if (step == 1) {
			if (i == primaryNodes-1 || i == nodeCount-1)
			    offset = maxWidth;
			interpPoint(point1, point2, nodePoints[i], i < primaryNodes ? 0 : 1,     -offset*flip);
			interpPoint(point1, point2, nodeTaps[i]  , i < primaryNodes ? ce : 1-ce, -offset*flip);
		    }
		    maxWidth = Math.max(maxWidth, offset); 
		    int nn = c < coilCount ? coilNodes[c] : -1;
		    if (nn == i) {
			// 这是线圈的第一个节点，留出空间
			c++;
			offset += width;
		    } else {
			// 这是线圈的最后一个节点，留出小间隙
			offset += 16;
		    }
		}
	    }
	    ptCore = newPointArray(4);
	    for (i = 0; i != 4; i += 2) {
		double h = (i == 2) ? -maxWidth*flip : 0;
		interpPoint(point1, point2, ptCore[i],   cd, h);
		interpPoint(point1, point2, ptCore[i+1], 1-cd, h);
	    }
	    
	    if (needDots) {
		dots = new Point[coilCount];
		double dotp = Math.abs(7./width);
		for (i = 0; i != coilCount; i++) {
		    int n = coilNodes[i];
		    dots[i] = interpPoint(nodeTaps[n], nodeTaps[n+1], coilPolarities[i] > 0 ? dotp : 1-dotp, i < primaryCoils ? -7 : 7);
		}
	    } else
		dots = null;
	}
	Point getPost(int n) {
	    return nodePoints[n];
	}
	int getPostCount() { return nodeCount; }
	void reset() {
	    int i;
	    for (i = 0; i != coilCount; i++)
		coilCurrents[i] = coilCurSourceValues[i] = coilCurCounts[i] = 0;
	    for (i = 0; i != nodeCount; i++)
		volts[i] = nodeCurrents[i] = nodeCurCounts[i] = 0;
	}
	double xformMatrix[][];
	
	void stamp() {
	    // 变压器的方程：
	    //   v1 = L1  di1/dt + M12  di2/dt + M13 di3/dt + ...
	    //   v2 = M21 di1/dt + L2 di2/dt   + M23 di3/dt + ...
	    //   v3 = ... （每个线圈对应一行）
	    // 我们对其求逆得到：
	    //   di1/dt = a1 v1 + a2 v2 + ...
	    //   di2/dt = a3 v1 + a4 v2 + ...
	    // 使用梯形近似对 di1/dt 积分，得到：
	    //   i1(t2) = i1(t1) + dt/2 (i1(t1) + i1(t2))
	    //          = i1(t1) + a1 dt/2 v1(t1) + a2 dt/2 v2(t1) + ... +
	    //                     a1 dt/2 v1(t2) + a2 dt/2 v2(t2) + ...
	    // 由此可得到 i1 的诺顿等效电路：
	    //  a. 电流源，I = i1(t1) + a1 dt/2 v1(t1) + a2 dt/2 v2(t1) + ...
	    //  b. 电阻，G = a1 dt/2
	    //  c. 由电压 v2 控制的电流源，G = a2 dt/2
	    // 对于 i2：
	    //  a. 电流源，I = i2(t1) + a3 dt/2 v1(t1) + a4 dt/2 v2(t1) + ...
	    //  b. 电阻，G = a3 dt/2
	    //  c. 由电压 v2 控制的电流源，G = a4 dt/2
	    //
	    // 对于向后欧拉法，电流源的值仅为 i1(t1)，电阻和 VCCS 使用
	    // dt 而非 dt/2。
	    xformMatrix = new double[coilCount][coilCount];
	    int i;
	    // 填充对角线
	    for (i = 0; i != coilCount; i++)
		xformMatrix[i][i] = coilInductances[i];
	    int j;
	    // 填充非对角线
	    for (i = 0; i != coilCount; i++)
		for (j = 0; j != i; j++)
		    xformMatrix[i][j] = xformMatrix[j][i] = couplingCoef*Math.sqrt(coilInductances[i]*coilInductances[j])*coilPolarities[i]*coilPolarities[j];

	    CirSim.invertMatrix(xformMatrix, coilCount);
	    
	    double ts = isTrapezoidal() ? sim.timeStep/2 : sim.timeStep;
	    for (i = 0; i != coilCount; i++)
		for (j = 0; j != coilCount; j++) {
		    // 乘以 dt/2（向后欧拉法时为 dt）
		    xformMatrix[i][j] *= ts;
		    int ni = coilNodes[i];
		    int nj = coilNodes[j];
		    if (i == j)
			sim.stampConductance(nodes[ni], nodes[ni+1], xformMatrix[i][i]);
		    else
			sim.stampVCCurrentSource(nodes[ni], nodes[ni+1], nodes[nj], nodes[nj+1], xformMatrix[i][j]);
		}
	    for (i = 0; i != nodeCount; i++)
		sim.stampRightSide(nodes[i]);
	}
	
	void startIteration() {
	    int i;
	    for (i = 0; i != coilCount; i++) {
		double val = coilCurrents[i];
		if (isTrapezoidal()) {
		    int j;
		    for (j = 0; j != coilCount; j++) {
			int n = coilNodes[j];
			double voltdiff = volts[n]-volts[n+1];
			val += voltdiff*xformMatrix[i][j];
		    }
		}
		coilCurSourceValues[i] = val;
	    }
	}
	
	void doStep() {
	    int i;
	    for (i = 0; i != coilCount; i++) {
		int n = coilNodes[i];
		sim.stampCurrentSource(nodes[n], nodes[n+1], coilCurSourceValues[i]);
	    }
 	}
	
	void calculateCurrent() {
	    int i;
	    for (i = 0; i != nodeCount; i++)
		nodeCurrents[i] = 0;
	    for (i = 0; i != coilCount; i++) {
		double val = coilCurSourceValues[i];
		if (xformMatrix != null) {
		    int j;
		    for (j = 0; j != coilCount; j++) {
			int n = coilNodes[j];
			double voltdiff = volts[n]-volts[n+1];
			val += voltdiff*xformMatrix[i][j];
		    }
		}
		coilCurrents[i] = val;
		int ni = coilNodes[i];
		nodeCurrents[ni] += val;
		nodeCurrents[ni+1] -= val;
	    }
	}
	
	@Override double getCurrentIntoNode(int n) {
	    return -nodeCurrents[n];
	}
	
	void getInfo(String arr[]) {
	    arr[0] = "transformer (custom)";
	    arr[1] = "L = " + getUnitText(inductance, "H");
	    int i;
	    for (i = 0; i != coilCount ; i++) {
		if (2+i*2 >= arr.length)
		    break;
		int ni = coilNodes[i];
		arr[2+i*2] = "Vd" + (i+1) + " = " + getVoltageText(volts[ni]-volts[ni+1]);
		arr[3+i*2] = "I" + (i+1) + " = " + getCurrentText(coilCurrents[i]);
	    }
	}
	
	boolean getConnection(int n1, int n2) {
	    int i;
	    for (i = 0; i != coilCount; i++)
		if (comparePair(n1, n2, coilNodes[i], coilNodes[i]+1))
		    return true;
	    return false;
	}
	
	public EditInfo getEditInfo(int n) {
	    if (n == 0)
		return new EditInfo("Base Inductance (H)", inductance, .01, 5);
	    if (n == 1) {
		EditInfo ei = new EditInfo(EditInfo.makeLink("customtransformer.html", "Description"), 0, -1, -1);
		ei.text = description;
		ei.disallowSliders();
		return ei;
	    }
	    if (n == 2)
		return new EditInfo("Coupling Coefficient", couplingCoef, 0, 1).
		    setDimensionless();
	    if (n == 3) {
		EditInfo ei = new EditInfo("", 0, -1, -1);
		ei.checkbox = new Checkbox("Trapezoidal Approximation",
					   isTrapezoidal());
		return ei;
	    }
	    return null;
	}
	public void setEditValue(int n, EditInfo ei) {
	    if (n == 0 && ei.value > 0) {
		inductance = ei.value;
		parseDescription();
	    }
	    if (n == 1) {
		String s = ei.textf.getText();
		if (s != description) {
		    if (!parseDescription(s)) {
			parseDescription(description);
			Window.alert("Parse error in description");
		    } else
			description = s;
		    setPoints();
		}
	    }
	    if (n == 2 && ei.value > 0 && ei.value < 1) {
		couplingCoef = ei.value;
		parseDescription();
	    }
	    if (n == 3) {
		if (ei.checkbox.getState())
		    flags &= ~Inductor.FLAG_BACK_EULER;
		else
		    flags |= Inductor.FLAG_BACK_EULER;
		parseDescription();
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

	// 不支持竖直方向
	boolean canFlipXY() { return false; }

    }
