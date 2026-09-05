package com.lushprojects.circuitjs1.client;

import com.lushprojects.circuitjs1.client.util.Locale;

// 测试元件：用于评估由单个晶体管构造复合元件是否可行

// Iain Sharp, Feb 2017


public class DarlingtonElm extends CompositeElm {

    private Polygon rectPoly, arrowPoly;
    private Point rect[], coll[], emit[], base, coll2[];
    

    private int pnp; // +1 表示 NPN，-1 表示 PNP；
    private double curcount_c, curcount_e, curcount_b;
    private static String modelString = "NTransistorElm 1 2 4\rNTransistorElm 4 2 3";
    private static int[] modelExternalNodes = {1, 2, 3};
    
    DarlingtonElm(int xx, int yy, boolean pnpflag) {
	super(xx, yy, modelString, modelExternalNodes);
	pnp = (pnpflag) ? -1 : 1;
	((TransistorElm) compElmList.get(0)).pnp=pnp;
	((TransistorElm) compElmList.get(1)).pnp=pnp;
	noDiagonal = true;
	
    }
    

    public DarlingtonElm(int xa, int ya, int xb, int yb, int f, StringTokenizer st) {
	super(xa, ya, xb, yb, f, st, modelString, modelExternalNodes);
	pnp = new Integer(st.nextToken()).intValue();
	noDiagonal = true;
    }

    public void reset() {
	super.reset();
	curcount_c = curcount_e = curcount_b = 0;
    }
    
    public int getDumpType() {
	return 400;
    }

    public String dump() {
	return super.dump()+" "+pnp;
    }

    
    void draw(Graphics g) {
	setBbox(point1, point2, 16);
	setPowerColor(g, true);
	// 绘制集电极
	setVoltageColor(g, volts[1]);
	drawThickLine(g, coll[0], coll[1]);
	drawThickLine(g, coll2[0], coll2[1]);
	drawThickLine(g, coll[0], coll2[0]);
	// 绘制发射极
	setVoltageColor(g, volts[2]);
	drawThickLine(g, emit[0], emit[1]);
	// 绘制箭头
	g.setColor(lightGrayColor);
	g.fillPolygon(arrowPoly);
	// 绘制基极
	setVoltageColor(g, volts[0]);
	if (sim.powerCheckItem.getState())
	    g.setColor(Color.gray);
	drawThickLine(g, point1, base);
	// 绘制电流流动点
	curcount_b = updateDotCount(getCurrentIntoNode(0), curcount_b);
	drawDots(g, base, point1, curcount_b);
	curcount_c = updateDotCount(getCurrentIntoNode(1), curcount_c);
	drawDots(g, coll[1], coll[0], curcount_c);
	curcount_e = updateDotCount(getCurrentIntoNode(2), curcount_e);
	drawDots(g, emit[1], emit[0], curcount_e);
	// 绘制基极矩形
	setVoltageColor(g, volts[0]);
	setPowerColor(g, true);
	g.fillPolygon(rectPoly);

	if ((needsHighlight() || sim.dragElm == this) && dy == 0) {
	    g.setColor(whiteColor);
	    // IES
	    // g.setFont(unitsFont);
	    int ds = sign(dx);
	    g.drawString("B", base.x - 10 * ds, base.y - 5);
	    g.drawString("C", coll[0].x - 3 + 9 * ds, coll[0].y + 4); // x+6 若
								      // ds=1，
								      // -12 若
								      // -1
	    g.drawString("E", emit[0].x - 3 + 9 * ds, emit[0].y + 4);
	}
	drawPosts(g);
    }




    void getInfo(String arr[]) {
	arr[0] = Locale.LS("darlington pair") + " (" + ((pnp == -1) ? "PNP)" : "NPN)");
	double vbc = volts[0] - volts[1];
	double vbe = volts[0] - volts[2];
	double vce = volts[1] - volts[2];
	arr[1] = "Ic = " + getCurrentText(-getCurrentIntoNode(1));
	arr[2] = "Ib = " + getCurrentText(-getCurrentIntoNode(0));
	arr[3] = "Vbe = " + getVoltageText(vbe);
	arr[4] = "Vbc = " + getVoltageText(vbc);
	arr[5] = "Vce = " + getVoltageText(vce);
    }

    void setPoints() {
	super.setPoints();
	int hs = 16;
	int hs2 = hs * dsign * pnp;
	// 计算集电极、发射极引脚
	coll = newPointArray(2);
	coll2 = newPointArray(2);
	emit = newPointArray(2);
	interpPoint2(point1, point2, coll[0], emit[0], 1, hs2);
	coll2[0]=interpPoint(point1, point2, 1, hs2-5*dsign*pnp);
	// 计算矩形边缘
	rect = newPointArray(4);
	interpPoint2(point1, point2, rect[0], rect[1], 1 - 16 / dn, hs);
	interpPoint2(point1, point2, rect[2], rect[3], 1 - 13 / dn, hs);
	// 计算集电极/发射极引线与矩形接触的点
	interpPoint2(point1, point2, coll[1], emit[1], 1 - 13 / dn, 6 * dsign * pnp);
	coll2[1]=interpPoint(point1, point2, 1-13/dn, dsign*pnp);
	// 计算基极引线与矩形接触的点
	base = new Point();
	interpPoint(point1, point2, base, 1 - 16 / dn);
	// 矩形
	rectPoly = createPolygon(rect[0], rect[2], rect[3], rect[1]);
	// 箭头
	    if (pnp == 1)
		arrowPoly = calcArrow(emit[1], emit[0], 8, 4);
	    else {
		Point pt = interpPoint(point1, point2, 1-11/dn, -5*dsign*pnp);
		arrowPoly = calcArrow(emit[0], pt, 8, 4);
	    }
	setPost(0, point1);
	setPost(1,coll[0]);
	setPost(2,emit[0]);

    }

    boolean canFlipY() { return false; }

}
