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

import com.lushprojects.circuitjs1.client.util.Locale;

class MosfetElm extends CircuitElm {
	int pnp;
	int FLAG_PNP = 1;
	int FLAG_SHOWVT = 2;
	int FLAG_DIGITAL = 4;
	int FLAG_FLIP = 8;
	int FLAG_HIDE_BULK = 16;
	int FLAG_BODY_DIODE = 32;
	int FLAG_BODY_TERMINAL = 64;
	int FLAGS_GLOBAL = (FLAG_HIDE_BULK|FLAG_DIGITAL);
	int bodyTerminal;
	
	double vt;
	// beta = 1/(RdsON*(Vgs-Vt))
	double beta;
	static int globalFlags;
	Diode diodeB1, diodeB2;
	double diodeCurrent1, diodeCurrent2, bodyCurrent;
	double curcount_body1, curcount_body2;
	static double lastBeta;
	
	MosfetElm(int xx, int yy, boolean pnpflag) {
	    super(xx, yy);
	    pnp = (pnpflag) ? -1 : 1;
	    flags = (pnpflag) ? FLAG_PNP : 0;
	    flags |= FLAG_BODY_DIODE;
	    noDiagonal = true;
	    setupDiodes();
	    beta = getDefaultBeta();
	    vt = getDefaultThreshold();
	}
	
	public MosfetElm(int xa, int ya, int xb, int yb, int f,
			 StringTokenizer st) {
	    super(xa, ya, xb, yb, f);
	    pnp = ((f & FLAG_PNP) != 0) ? -1 : 1;
	    noDiagonal = true;
	    setupDiodes();
	    vt = getDefaultThreshold();
	    beta = getBackwardCompatibilityBeta();
	    try {
		vt = new Double(st.nextToken()).doubleValue();
		beta = new Double(st.nextToken()).doubleValue();
	    } catch (Exception e) {}
	    globalFlags = flags & (FLAGS_GLOBAL);
	    allocNodes(); // 确保当 hasBodyTerminal() 为 true 时 volts[] 有正确的元素数量 
	}

	// 设置体二极管
	void setupDiodes() {
	    // 从节点 1 到体端子的二极管 
	    diodeB1 = new Diode(sim);
	    diodeB1.setupForDefaultModel();
	    // 从节点 2 到体端子的二极管
	    diodeB2 = new Diode(sim);
	    diodeB2.setupForDefaultModel();
	}
	
	double getDefaultThreshold() { return 1.5; }
	
	// 新元件的默认 beta
	double getDefaultBeta() { return lastBeta == 0 ? getBackwardCompatibilityBeta() : lastBeta; }
	
	// 旧文件中无 beta 配置项的元件的默认值。JfetElm 会覆盖此方法。
	// 不确定这个值从何而来，但 ZVP3306A 的 beta 约为 .027。功率 MOSFET 的 beta 要高得多（如 80 或更高）
	double getBackwardCompatibilityBeta() { return .02; }
	
	boolean nonLinear() { return true; }
	boolean drawDigital() { return (flags & FLAG_DIGITAL) != 0; }
	boolean showBulk() { return (flags & (FLAG_DIGITAL|FLAG_HIDE_BULK)) == 0; }
	boolean hasBodyTerminal() { return (flags & FLAG_BODY_TERMINAL) != 0 && doBodyDiode(); }
	boolean doBodyDiode() { return (flags & FLAG_BODY_DIODE) != 0 && showBulk(); }
	void reset() {
	    lastv1 = lastv2 = volts[0] = volts[1] = volts[2] = curcount = 0;
	    curcount_body1 = curcount_body2 = 0;
	    diodeB1.reset();
	    diodeB2.reset();
	    if (doBodyDiode())
		volts[bodyTerminal] = 0;
	}
	String dump() {
	    return super.dump() + " " + vt + " " + beta;
	}
	int getDumpType() { return 'f'; }
	final int hs = 16;
	
	void draw(Graphics g) {
	    // 获取全局标志的更改
	    if ((flags & FLAGS_GLOBAL) != globalFlags)
		setPoints();
	    
		setBbox(point1, point2, hs);
		
		// 绘制源极/漏极端子
		setVoltageColor(g, volts[1]);
		drawThickLine(g, src[0], src[1]);
		setVoltageColor(g, volts[2]);
		drawThickLine(g, drn[0], drn[1]);
		
		// 绘制连接源极和漏极的线段
		int segments = 6;
		int i;
		setPowerColor(g, true);
		boolean power = sim.powerCheckItem.getState();
		double segf = 1./segments;
		boolean enhancement = vt > 0 && showBulk();
		for (i = 0; i != segments; i++) {
		    if ((i == 1 || i == 4) && enhancement) continue;
		    double v = volts[1]+(volts[2]-volts[1])*i/segments;
		    if (!power)
			setVoltageColor(g, v);
		    interpPoint(src[1], drn[1], ps1, i*segf);
		    interpPoint(src[1], drn[1], ps2, (i+1)*segf);
		    drawThickLine(g, ps1, ps2);
		}
		
		// 绘制该线段的末端小延伸
		if (!power)
		    setVoltageColor(g, volts[1]);
		drawThickLine(g, src[1], src[2]);
		if (!power)
		    setVoltageColor(g, volts[2]);
		drawThickLine(g, drn[1], drn[2]);
		
		// 绘制衬底连接
		if (showBulk()) {
		    setVoltageColor(g, volts[bodyTerminal]);
		    if (!hasBodyTerminal())
			drawThickLine(g, pnp == -1 ? drn[0] : src[0], body[0]);
		    drawThickLine(g, body[0], body[1]);
		}
		
		// 绘制箭头
		if (!drawDigital()) {
		    setVoltageColor(g, volts[bodyTerminal]);
		    g.fillPolygon(arrowPoly);
		}
		if (power)
		    g.setColor(Color.gray);
		
		// 绘制栅极
		setVoltageColor(g, volts[0]);
		drawThickLine(g, point1, gate[1]);
		drawThickLine(g, gate[0], gate[2]);
		if (drawDigital() && pnp == -1)
			drawThickCircle(g, pcircle.x, pcircle.y, pcircler);
		
		if ((flags & FLAG_SHOWVT) != 0) {
			String s = "" + (vt*pnp);
			g.setColor(whiteColor);
			g.setFont(unitsFont);
			drawCenteredText(g, s, x2+2, y2, false);
		}
		curcount = updateDotCount(-ids, curcount);
		drawDots(g, src[0], src[1], curcount);
		drawDots(g, src[1], drn[1], curcount);
		drawDots(g, drn[1], drn[0], curcount);
		
		if (showBulk()) {
		    curcount_body1 = updateDotCount(diodeCurrent1, curcount_body1);
		    curcount_body2 = updateDotCount(diodeCurrent2, curcount_body2);
		    drawDots(g, src [0], body[0], -curcount_body1);
		    drawDots(g, body[0], drn [0],  curcount_body2);
		}
		
		// 高亮显示时标注引脚
		if (needsHighlight() || sim.dragElm == this) {
		    g.setColor(whiteColor);
		    g.setFont(unitsFont);

		    // 根据方向对引脚标签位置做精细调整
		    int dsx = sign(dx);
		    int dsy = sign(dy);
		    int dsyn = dy == 0 ? 0 : 1;

		    g.drawString("G", gate[1].x - (dx < 0 ? -2 : 12), gate[1].y + ((dy > 0) ? -5 : 12));
		    g.drawString(pnp == -1 ? "D" : "S", src[0].x-3+9*(dsx-dsyn*pnp), src[0].y+4);
		    g.drawString(pnp == -1 ? "S" : "D", drn[0].x-3+9*(dsx-dsyn*pnp), drn[0].y+4);
		    if (hasBodyTerminal())
			g.drawString("B",  body[0].x-3+9*(dsx-dsyn*pnp),  body[0].y+4);
		}	    
		
		drawPosts(g);
	}
	
	// 端点 0 = 栅极，NPN 的 1 = 源极，NPN 的 2 = 漏极，3 = 衬底（若存在）
	// 对于 PNP，1 是漏极，2 是源极
	Point getPost(int n) {
	    return (n == 0) ? point1 : (n == 1) ? src[0] :
		(n == 2) ? drn[0] : body[0];
	}
	
	double getCurrent() { return ids; }
	double getPower() {
	    return ids*(volts[2]-volts[1]) - diodeCurrent1*(volts[1]-volts[bodyTerminal]) - diodeCurrent2*(volts[2]-volts[bodyTerminal]);
	    }
	int getPostCount() { return hasBodyTerminal() ? 4 : 3; }

	int pcircler;
	
	// 源极和漏极的点（在 PNP MOSFET 上会互换）
	Point src[], drn[];
	
	// 栅极、衬底的点，以及 PNP MOSFET 上的小圆圈
	Point gate[], body[], pcircle;
	Polygon arrowPoly;
	
	void setPoints() {
	    super.setPoints();

	    // 这两个标志适用于所有 mosfet
	    flags &= ~FLAGS_GLOBAL;
	    flags |= globalFlags;
	    
	    // 计算出我们绘制
	    // MOSFET 所需的各个点的坐标。
	    int hs2 = hs*dsign;
	    if ((flags & FLAG_FLIP) != 0)
	    	hs2 = -hs2;
	    src = newPointArray(3);
	    drn = newPointArray(3);
	    interpPoint2(point1, point2, src[0], drn[0], 1, -hs2);
	    interpPoint2(point1, point2, src[1], drn[1], 1-22/dn, -hs2);
	    interpPoint2(point1, point2, src[2], drn[2], 1-22/dn, -hs2*4/3);

	    gate = newPointArray(3);
	    interpPoint2(point1, point2, gate[0], gate[2], 1-28/dn, hs2/2); // 原为 1-20/dn
	    interpPoint(gate[0], gate[2], gate[1], .5);

	    if (showBulk()) {
		body = newPointArray(2);
		interpPoint(src[0], drn[0], body[0], .5);
		interpPoint(src[1], drn[1], body[1], .5);
	    }
	    
	    if (!drawDigital()) {
		if (pnp == 1) {
		    if (!showBulk())
			arrowPoly = calcArrow(src[1], src[0], 10, 4);
		    else
			arrowPoly = calcArrow(body[0], body[1], 12, 5);
		} else {
		    if (!showBulk())
			arrowPoly = calcArrow(drn[0], drn[1], 12, 5);
		    else
			arrowPoly = calcArrow(body[1], body[0], 12, 5);
		}
	    } else if (pnp == -1) {
		interpPoint(point1, point2, gate[1], 1-36/dn);
		int dist = (dsign < 0) ? 32 : 31;
		pcircle = interpPoint(point1, point2, 1-dist/dn);
		pcircler = 3;
	    }
	}

	double lastv1, lastv2;
	double ids;
	int mode = 0;
	double gm = 0;
	
	void stamp() {
	    sim.stampNonLinear(nodes[1]);
	    sim.stampNonLinear(nodes[2]);
	    
	    if (hasBodyTerminal())
		bodyTerminal = 3;
	    else
		bodyTerminal = (pnp == -1) ? 2 : 1;

	    if (doBodyDiode()) {
		if (pnp == -1) {
		    // pnp：当 S 或 D 的电位高于衬底时二极管导通
		    diodeB1.stamp(nodes[1], nodes[bodyTerminal]);
		    diodeB2.stamp(nodes[2], nodes[bodyTerminal]);
		} else {
		    // npn：当衬底电位高于 S 或 D 时二极管导通
		    diodeB1.stamp(nodes[bodyTerminal], nodes[1]);
		    diodeB2.stamp(nodes[bodyTerminal], nodes[2]);
		}
	    }
	}
	
	boolean nonConvergence(double last, double now) {
	    double diff = Math.abs(last-now);
	    
	    // 高 beta 的 MOSFET 对微小差异更敏感，因此我们对收敛测试要求更严格
	    if (beta > 1)
		diff *= 100;
	    
	    // 差值小于 10mV 是可以的
	    if (diff < .01)
		return false;
	    // 若数值较大，稍大的差值也可接受
	    if (sim.subIterations > 10 && diff < Math.abs(now)*.001)
		return false;
	    // 如果收敛困难，则放宽标准
	    if (sim.subIterations > 100 && diff < .01+(sim.subIterations-100)*.0001)
		return false;
	    return true;
	}
	
	void stepFinished() {
	    calculate(true);
	    
	    // 若衬底连接到源极或漏极，则修正电流
	    if (bodyTerminal == 1)
		diodeCurrent1 = -diodeCurrent2;
	    if (bodyTerminal == 2)
		diodeCurrent2 = -diodeCurrent1;
	}

	void doStep() {
	    calculate(false);
	}
	
	double lastv0;
	
	// 此方法在 doStep 中用于填充矩阵，也会在 stepFinished() 中被调用以计算电流
	void calculate(boolean finished) {
	    double vs[];
	    if (finished)
		vs = volts;
	    else {
		// 将电压变化限制在 .5V 以内
		vs = new double[3];
		vs[0] = volts[0];
		vs[1] = volts[1];
		vs[2] = volts[2];
		if (vs[1] > lastv1 + .5)
		    vs[1] = lastv1 + .5;
		if (vs[1] < lastv1 - .5)
		    vs[1] = lastv1 - .5;
		if (vs[2] > lastv2 + .5)
		    vs[2] = lastv2 + .5;
		if (vs[2] < lastv2 - .5)
		    vs[2] = lastv2 - .5;
	    }
	    
	    int source = 1;
	    int drain = 2;
	    
	    // 若源极电压 > 漏极电压（对于 NPN），则交换源极和漏极
	    //（PNP 则相反）
	    if (pnp*vs[1] > pnp*vs[2]) {
	    	source = 2;
	    	drain = 1;
	    }
	    int gate = 0;
	    double vgs = vs[gate ]-vs[source];
	    double vds = vs[drain]-vs[source];
	    if (!finished && (nonConvergence(lastv1, vs[1]) || nonConvergence(lastv2, vs[2]) || nonConvergence(lastv0, vs[0])))
		sim.converged = false;
	    lastv0 = vs[0];
	    lastv1 = vs[1];
	    lastv2 = vs[2];
	    double realvgs = vgs;
	    double realvds = vds;
	    vgs *= pnp;
	    vds *= pnp;
	    ids = 0;
	    gm = 0;
	    double Gds = 0;
	    if (vgs < vt) {
		// 本来应该全为零，但那会导致奇异矩阵，
		// 因此我们将其视为一个大电阻
		Gds = 1e-8;
		ids = vds*Gds;
		mode = 0;
	    } else if (vds < vgs-vt) {
		// 线性区
		ids = beta*((vgs-vt)*vds - vds*vds*.5);
		gm  = beta*vds;
		Gds = beta*(vgs-vds-vt);
		mode = 1;
	    } else {
		// 饱和区；Gds = 0
		gm  = beta*(vgs-vt);
		// 使用极小的 Gds 以避免不收敛
		Gds = 1e-8;
		ids = .5*beta*(vgs-vt)*(vgs-vt) + (vds-(vgs-vt))*Gds;
		mode = 2;
	    }
	    
	    if (doBodyDiode()) {
		diodeB1.doStep(pnp*(volts[bodyTerminal]-volts[1]));
		diodeCurrent1 = diodeB1.calculateCurrent(pnp*(volts[bodyTerminal]-volts[1]))*pnp;
		diodeB2.doStep(pnp*(volts[bodyTerminal]-volts[2]));
		diodeCurrent2 = diodeB2.calculateCurrent(pnp*(volts[bodyTerminal]-volts[2]))*pnp;
	    } else
		diodeCurrent1 = diodeCurrent2 = 0;

	    double ids0 = ids;
	    
	    // 如果上面交换了源极和漏极，则翻转 ids
	    if (source == 2 && pnp == 1 ||
		source == 1 && pnp == -1)
		ids = -ids;

	    if (finished)
		return;
	    
	    double rs = -pnp*ids0 + Gds*realvds + gm*realvgs;
	    sim.stampMatrix(nodes[drain],  nodes[drain],  Gds);
	    sim.stampMatrix(nodes[drain],  nodes[source], -Gds-gm); 
	    sim.stampMatrix(nodes[drain],  nodes[gate],   gm);
	    
	    sim.stampMatrix(nodes[source], nodes[drain],  -Gds);
	    sim.stampMatrix(nodes[source], nodes[source], Gds+gm); 
	    sim.stampMatrix(nodes[source], nodes[gate],  -gm);
	    
	    sim.stampRightSide(nodes[drain],  rs);
	    sim.stampRightSide(nodes[source], -rs);
	}
	
	void getFetInfo(String arr[], String n) {
	    arr[0] = Locale.LS(((pnp == -1) ? "p-" : "n-") + n);
	    arr[0] += " (Vt=" + getVoltageText(pnp*vt);
	    arr[0] += ", \u03b2=" + beta + ")";
	    arr[1] = ((pnp == 1) ? "Ids = " : "Isd = ") + getCurrentText(ids);
	    arr[2] = "Vgs = " + getVoltageText(volts[0]-volts[pnp == -1 ? 2 : 1]);
	    arr[3] = ((pnp == 1) ? "Vds = " : "Vsd = ") + getVoltageText(volts[2]-volts[1]);
	    arr[4] = Locale.LS((mode == 0) ? "off" :
		(mode == 1) ? "linear" : "saturation");
	    arr[5] = "gm = " + getUnitText(gm, "A/V");
	    arr[6] = "P = " + getUnitText(getPower(), "W");
	    if (showBulk())
		arr[7] = "Ib = " + getUnitText(bodyTerminal == 1 ? -diodeCurrent1 : bodyTerminal == 2 ? diodeCurrent2 : -pnp*(diodeCurrent1+diodeCurrent2), "A");
	}
	void getInfo(String arr[]) {
	    getFetInfo(arr, "MOSFET");
	}
	@Override String getScopeText(int v) { 
	    return Locale.LS(((pnp == -1) ? "p-" : "n-") + "MOSFET");
	}
	boolean canViewInScope() { return true; }
	double getVoltageDiff() { return volts[2] - volts[1]; }
	boolean getConnection(int n1, int n2) {
	    return !(n1 == 0 || n2 == 0);
	}
	public EditInfo getEditInfo(int n) {
		if (n == 0)
			return new EditInfo("Threshold Voltage", pnp*vt, .01, 5);
		if (n == 1)
			return new EditInfo(EditInfo.makeLink("mosfet-beta.html", "Beta"), beta, .01, 5);
		if (n == 2) {
			EditInfo ei = new EditInfo("", 0, -1, -1);
			ei.checkbox = new Checkbox("Show Bulk", showBulk());
			return ei;
		}
		if (n == 3) {
			EditInfo ei = new EditInfo("", 0, -1, -1);
			ei.checkbox = new Checkbox("Swap D/S", (flags & FLAG_FLIP) != 0);
			return ei;
		}
		if (n == 4 && !showBulk()) {
			EditInfo ei = new EditInfo("", 0, -1, -1);
			ei.checkbox = new Checkbox("Digital Symbol", drawDigital());
			return ei;
		}
		if (n == 4 && showBulk()) {
			EditInfo ei = new EditInfo("", 0, -1, -1);
			ei.checkbox = new Checkbox("Simulate Body Diode", (flags & FLAG_BODY_DIODE) != 0);
			return ei;
		}
		if (n == 5 && doBodyDiode()) {
			EditInfo ei = new EditInfo("", 0, -1, -1);
			ei.checkbox = new Checkbox("Body Terminal", (flags & FLAG_BODY_TERMINAL) != 0);
			return ei;
		}

		return null;
	}
	public void setEditValue(int n, EditInfo ei) {
		if (n == 0)
			vt = pnp*ei.value;
		if (n == 1 && ei.value > 0)
			beta = lastBeta = ei.value;	
		if (n == 2) {
		    globalFlags = (!ei.checkbox.getState()) ? (globalFlags|FLAG_HIDE_BULK) :
				(globalFlags & ~(FLAG_HIDE_BULK|FLAG_DIGITAL));
//		    setPoints();
		    ei.newDialog = true;
		}
		if (n == 3) {
			flags = (ei.checkbox.getState()) ? (flags | FLAG_FLIP) :
				(flags & ~FLAG_FLIP);
//			setPoints();
		}
		if (n == 4 && !showBulk()) {
		    globalFlags = (ei.checkbox.getState()) ? (globalFlags|FLAG_DIGITAL) :
				(globalFlags & ~FLAG_DIGITAL);
//		    setPoints();
		}
		if (n == 4 && showBulk()) {
		    flags = ei.changeFlag(flags, FLAG_BODY_DIODE);
		    ei.newDialog = true;
		}
		if (n == 5) {
		    flags = ei.changeFlag(flags, FLAG_BODY_TERMINAL);
		}

		// 体端子可能在各种不同情况下被移除/添加，因此每次都执行此操作
		allocNodes();
		setPoints();
	}
	double getCurrentIntoNode(int n) {
	    if (n == 0)
		return 0;
	    if (n == 3)
		return -diodeCurrent1 - diodeCurrent2;
	    if (n == 1)
		return ids + diodeCurrent1;
	    return -ids + diodeCurrent2;
	}

        void flipX(int c2, int count) {
            if (x == x2)
                flags ^= FLAG_FLIP;
            super.flipX(c2, count);
        }

        void flipY(int c2, int count) {
            if (y == y2)
                flags ^= FLAG_FLIP;
            super.flipY(c2, count);
        }

        void flipXY(int xmy, int count) {
	    flags ^= FLAG_FLIP;
            super.flipXY(xmy, count);
        }
    }
