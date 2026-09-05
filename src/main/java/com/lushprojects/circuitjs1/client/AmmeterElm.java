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
    
    AmmeterElm by Bill Collis
    
*/

package com.lushprojects.circuitjs1.client;

import com.lushprojects.circuitjs1.client.util.Locale;

class AmmeterElm extends CircuitElm {
        
        int meter;
	int scale;
        final int AM_VOL = 0;
        final int AM_RMS = 1;
        int zerocount=0;
        double rmsI=0, total, count;
        double maxI=0, lastMaxI;
        double minI=0, lastMinI;
        double selectedValue=0;
        
        double currents[];
        boolean increasingI=true, decreasingI=true;

    public AmmeterElm(int xx, int yy) { 
        super(xx, yy); 
        flags = FLAG_SHOWCURRENT|FLAG_CIRCLE;
        scale = SCALE_AUTO;
    }
    public AmmeterElm(int xa, int ya, int xb, int yb, int f,
               StringTokenizer st) {
        super(xa, ya, xb, yb, f);
        scale = SCALE_AUTO;
        meter = Integer.parseInt(st.nextToken());
        try {
            scale = Integer.parseInt(st.nextToken());
        } catch (Exception e) {}
    }
    String dump() {
            return super.dump() + " " + meter + " " + scale;
    }
    String getMeter(){
        switch (meter) {
        case AM_VOL:
            return "I";
        case AM_RMS:
            return "Irms";
        }
        return "";
    }
    void setPoints(){
        super.setPoints();
        mid = interpPoint(point1,point2,0.6);
        center = interpPoint(point1,point2,0.5);
        arrowPoly = calcArrow(point1, mid, 14, 7);
    }
    Point center;
    Point mid;
    static final int FLAG_SHOWCURRENT = 1;
    static final int FLAG_CIRCLE = 2;  // 添加此行

    void stepFinished(){
        count++;// 一个周期内的计数次数    
        total += current*current; // 平方和
        if (current>maxI && increasingI){
            maxI = current;
            increasingI = true;
            decreasingI = false;
        }
        if (current<maxI && increasingI){// 方向改变，电流现在开始下降——位于波形起始处
            lastMaxI=maxI; // 记录上一次最大值 
            // 记录时间间隔
            minI=current; // 跟踪最小值 
            increasingI=false;
            decreasingI=true;
            
            // rms 数据
            total = total/count;
            rmsI = Math.sqrt(total);
            if (Double.isNaN(rmsI))
                rmsI=0;
            count=0;
            total=0;
            
        }
        if (current<minI && decreasingI){ // 电流下降，跟踪最小值
            minI=current;
            increasingI=false;
            decreasingI=true;
        }

        if (current>minI && decreasingI){ // 方向改变，电流现在开始上升
            lastMinI=minI; // 记录上一次最小值

            maxI = current;
            increasingI = true;
            decreasingI = false;
            
            // rms 数据
            total = total/count;
            rmsI = Math.sqrt(total);
            if (Double.isNaN(rmsI))
                rmsI=0;
            count=0;
            total=0;

            
        }
        // 如果 rms 值在一段时间内保持为 0，需要将其清零
        if (current==0){
            zerocount++;
            if (zerocount > 5){
                total=0;
                rmsI=0;
                maxI=0;
                minI=0;
            }
        }else{
            zerocount=0;
        }
        switch (meter) {
        case AM_VOL:
            selectedValue = current;
            break;
        case AM_RMS:
            selectedValue = rmsI;
            break;
        }
    }
    
    Polygon arrowPoly;
    void draw(Graphics g) {
        super.draw(g);// 高亮显示所需（BC）
        setVoltageColor(g, volts[0]);
	double width = 4;
        if (!drawAsCircle()) {
            drawThickLine(g, point1, point2);
            g.fillPolygon(arrowPoly);
        } else {
            g.setColor(needsHighlight() ? selectColor : lightGrayColor);
            drawThickCircle(g, center.x, center.y, circleSize);
            drawCenteredText(g, "A", center.x, center.y, true);

	    calcLeads(circleSize*2);
	    setVoltageColor(g, volts[0]);
	    drawThickLine(g, point1, lead1);
	    drawThickLine(g, lead2, point2);

            g.setColor(whiteColor);
            g.setFont(unitsFont);
            double len = circleSize*2;
            Point plusPoint = interpPoint(point1, point2, (dn/2-len/2-4)/dn, -10*dsign );
            if (y2 > y)
                 plusPoint.y += 4;
             if (y > y2)
                 plusPoint.y += 3;
            int w = (int)g.context.measureText("+").getWidth();
            g.drawString("+", plusPoint.x-w/2, plusPoint.y);
	    width = circleSize;
        }

        doDots(g);
        setBbox(point1, point2, width);
        String s = "A";
        switch (meter) {
        case AM_VOL:
            s = getUnitTextWithScale(getCurrent(), "A", scale);
            break;
        case AM_RMS:
            s = getUnitTextWithScale(rmsI, "A(rms)", scale);
            break;
        }

        drawValues(g, s, width);
        drawPosts(g);
    }

    final int circleSize = 12;

    int getDumpType() { return 370; }
    void stamp() {
        sim.stampVoltageSource(nodes[0], nodes[1], voltSource, 0);
    }
    boolean mustShowCurrent() {
        return (flags & FLAG_SHOWCURRENT) != 0;
    }
    int getVoltageSourceCount() { return 1; }
    void getInfo(String arr[]) {
        arr[0] = "Ammeter";
        switch (meter) {
            case AM_VOL:
                arr[1] = "I = " + getUnitText(current, "A");
                break;
            case AM_RMS:
                arr[1] = "Irms = " + getUnitText(rmsI, "A");
                break;
        }    
    }
    double getPower() { return 0; }
    double getVoltageDiff() { return volts[0]; }
    
    // 即使 isWireEquivalent() 返回 true 也不要将其优化掉
    // （因为我们需要每个时间步都计算电流）    
    boolean isWireEquivalent() { return true; }
    
    boolean drawAsCircle() {
        return (flags & FLAG_CIRCLE) != 0;
    }

    public EditInfo getEditInfo(int n) {
        if (n==0){
            EditInfo ei =  new EditInfo("Value", selectedValue, -1, -1);
            ei.choice = new Choice();
            ei.choice.add("Current");
            ei.choice.add("RMS Current");
            ei.choice.select(meter);
            return ei;
        }
        if (n == 1) {
            EditInfo ei =  new EditInfo("Scale", 0);
            ei.choice = new Choice();
            ei.choice.add("Auto");
            ei.choice.add("A");
            ei.choice.add("mA");
            ei.choice.add(Locale.muString + "A");
            ei.choice.select(scale);
            return ei;
        }
        if (n == 2) {
            return EditInfo.createCheckbox("Circular Symbol", drawAsCircle());
        }
        return null;
    }

    public void setEditValue(int n, EditInfo ei) {
        if (n==0)
            meter = ei.choice.getSelectedIndex();
        if (n==1)
            scale = ei.choice.getSelectedIndex();
        if (n==2)
            flags = ei.changeFlag(flags, FLAG_CIRCLE);
    }

}

