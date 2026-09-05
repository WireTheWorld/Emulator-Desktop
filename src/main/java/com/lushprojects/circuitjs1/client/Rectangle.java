//	  Extracted from file
//    Copyright 1995-2006 Sun Microsystems, Inc.  All Rights Reserved
//
//    This program is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 2 of the License, or
//    (at your option) any later version.
//
//    This program is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with this program.  If not, see <http://www.gnu.org/licenses/>.

package com.lushprojects.circuitjs1.client;

// 来自 http://grepcode.com/file_/repository.grepcode.com/java/root/jdk/openjdk/6-b14/java/awt/Rectangle.java/?v=source

public class Rectangle {
	int x;
	int y;
	int width;
	int height;
	
	public Rectangle(){
		x=0;
		y=0;
		width=0;
		height=0;
	}
	
    public Rectangle(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }
    
    public Rectangle(Point pt) {
	this.x = pt.x;
	this.y = pt.y;
	this.width = 0;
	this.height = 0;
    }
    
    public Rectangle(Rectangle r) {
        this(r.x, r.y, r.width, r.height);
    }
    
    public void setBounds(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }
    
    public void translate(int dx, int dy) {
        x += dx;
        y += dy;
    }
    
    public boolean contains(int X, int Y) {
        int w = this.width;
        int h = this.height;
        if ((w | h) < 0) {
            // 至少有一个维度为负数...
            return false;
        }
        // 注意：如果任一维度为零，下面的测试必须返回 false...
        int x = this.x;
        int y = this.y;
        if (X < x || Y < y) {
            return false;
        }
        w += x;
        h += y;
        //    溢出 || 相交
        return ((w < x || w > X) &&
                (h < y || h > Y));
    }
    
    /*
    public void move(int x, int y) {
        this.x = x;
        this.y = y;
    }
    */
    
    public boolean contains(Rectangle r) {
	return contains(r.x, r.y) && contains(r.x+r.width, r.y+r.height);
    }

    public boolean intersects(Rectangle r) {
        int tw = this.width;
        int th = this.height;
        int rw = r.width;
        int rh = r.height;
        if (rw <= 0 || rh <= 0 || tw <= 0 || th <= 0) {
            return false;
        }
        int tx = this.x;
        int ty = this.y;
        int rx = r.x;
        int ry = r.y;
        rw += rx;
        rh += ry;
        tw += tx;
        th += ty;
        //      溢出 || 相交
        return ((rw < rx || rw > tx) &&
                (rh < ry || rh > ty) &&
                (tw < tx || tw > rx) &&
                (th < ty || th > ry));
    }
    
    public Rectangle union(Rectangle r) {
        long tx2 = this.width;
        long ty2 = this.height;
        if ((tx2 | ty2) < 0) {
            // 该矩形具有负维度...
            // 如果 r 具有非负维度，则答案就是 r。
            // 如果 r 不存在（具有负维度），那么两者
            // 都不存在，我们可以返回任意不存在的矩形
            // 作为答案。因此，返回 r 符合该标准。
            // 无论哪种情况，r 都是我们的答案。
            return new Rectangle(r);
        }
        long rx2 = r.width;
        long ry2 = r.height;
        if ((rx2 | ry2) < 0) {
            return new Rectangle(this);
        }
        int tx1 = this.x;
        int ty1 = this.y;
        tx2 += tx1;
        ty2 += ty1;
        int rx1 = r.x;
        int ry1 = r.y;
        rx2 += rx1;
        ry2 += ry1;
        if (tx1 > rx1) tx1 = rx1;
        if (ty1 > ry1) ty1 = ry1;
        if (tx2 < rx2) tx2 = rx2;
        if (ty2 < ry2) ty2 = ry2;
        tx2 -= tx1;
        ty2 -= ty1;
        // tx2,ty2 永远不会下溢，因为两个原始矩形
        // 都已被证明是非空的
        // 但它们可能会溢出...
        if (tx2 > Integer.MAX_VALUE) tx2 = Integer.MAX_VALUE;
        if (ty2 > Integer.MAX_VALUE) ty2 = Integer.MAX_VALUE;
        return new Rectangle(tx1, ty1, (int) tx2, (int) ty2);
    }
    
    public String toString() { return "Rect(" + x + "," + y + "," + width + "," + height + ")"; }

    
    public boolean equals(Object obj) {
        if (obj instanceof Rectangle) {
            Rectangle r = (Rectangle)obj;
            return ((x == r.x) &&
                    (y == r.y) &&
                    (width == r.width) &&
                    (height == r.height));
        }
        return super.equals(obj);
    }
}
