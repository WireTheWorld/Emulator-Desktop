package com.lushprojects.circuitjs1.client;

import com.google.gwt.user.client.ui.Label;
import com.lushprojects.circuitjs1.client.util.Locale;
import com.google.gwt.user.client.Command;
import com.google.gwt.canvas.dom.client.CanvasGradient;
import com.google.gwt.event.dom.client.MouseWheelEvent;
import com.google.gwt.event.dom.client.MouseWheelHandler;

/*Bill Collis - 2015年6月 */

class LDRElm extends CircuitElm implements Command, MouseWheelHandler {
    double position; //滑块的当前位置 0.005 到 0.995
    double resistance; //根据滑块位置计算出的电阻
    double minLux, maxLux;
    double lux;

    Scrollbar slider; 
    Label label;
    String sliderText;

    //构造函数 - 最初创建时调用
    public LDRElm(int xx, int yy) {
	super(xx, yy);
	//setup();
	minLux = 0.1; //黑暗环境
	maxLux = 10000; // 阳光直射
	position = .34; 

	lux = LuxFromSliderPos();
	resistance = calcResistance(lux); 
	sliderText = Locale.LS("Light Brightness");
	createSlider();
    }

    //构造函数 - 从文件读取时调用
    public LDRElm(int xa, int ya, int xb, int yb, int f,
	    StringTokenizer st) {
	super(xa, ya, xb, yb, f);
	minLux = 0.1; //黑暗环境
	maxLux = 10000; // 阳光直射
	position = new Double(st.nextToken()).doubleValue();
	lux = LuxFromSliderPos();
	resistance = calcResistance(lux); 
	sliderText = CustomLogicModel.unescape(st.nextToken());
	createSlider(); //使用 position 来设置滑块   
    }

    //void setup() {
    //}

    int getPostCount() { return 2; }
    int getDumpType() { return 374; } //光敏电阻（LDR）

    //用于文件保存的数据 - 确保与文件输入构造函数中的项目顺序一致
    String dump() { 
	return super.dump() + " " + position  + " " + CustomLogicModel.escape(sliderText); 
    }

    void createSlider() {
	sim.addWidgetToVerticalPanel(label = new Label(sliderText));
	label.addStyleName("topSpace");
	int value = (int) (position*100);
	sim.addWidgetToVerticalPanel(slider = new Scrollbar(Scrollbar.HORIZONTAL, value, 1, 0, 101, this, this));
    }

    public void execute() {
	sim.analyzeFlag = true;
	setPoints();
    }

    void delete() {
	sim.removeWidgetFromVerticalPanel(label);
	sim.removeWidgetFromVerticalPanel(slider);
    }
    Point ps3, ps4;   

    //在加载 txt 文件后立即调用（在构造函数之后）
    void setPoints() {
	super.setPoints();
	calcLeads(32);
	position = slider.getValue()*.0099+.0001;
	lux = LuxFromSliderPos();
	resistance = calcResistance(lux); 
	ps3 = new Point();
	ps4 = new Point();
    }
    Polygon arrowPoly;
    void draw(Graphics g) { //借用 Resistor 的绘制方法
	//int segments = 16;
	int i;
	//int ox = 0;
	int hs=6; //宽度
	double v1 = volts[0];
	double v2 = volts[1];
	setBbox(point1, point2, hs); //器件创建时已存在的两个点
	draw2Leads(g); //从 point1 到 lead1，以及从 lead1 到 point2（lead1 和 lead2 位于本体上） 
	setPowerColor(g, true);
	double len = distance(lead1, lead2);
	g.context.save();
	g.context.setLineWidth(3.0);
	g.context.transform(((double)(lead2.x-lead1.x))/len, ((double)(lead2.y-lead1.y))/len, -((double)(lead2.y-lead1.y))/len,((double)(lead2.x-lead1.x))/len,lead1.x,lead1.y);
	CanvasGradient grad = g.context.createLinearGradient(0,0,len,0);
	grad.addColorStop(0, getVoltageColor(g,v1).getHexValue());
	grad.addColorStop(1.0, getVoltageColor(g,v2).getHexValue());
	g.context.setStrokeStyle(grad);
	if (!sim.euroResistorCheckItem.getState()) {
	    g.context.beginPath();
	    g.context.moveTo(0,0);
	    for (i=0;i<4;i++){
		g.context.lineTo((1+4*i)*len/16, hs);
		g.context.lineTo((3+4*i)*len/16, -hs);
	    }
	    g.context.lineTo(len, 0);
	    g.context.stroke();

	} else    {
	    g.context.strokeRect(0, -hs, len, 2.0*hs); //绘制欧式电阻的方框
	}

	g.context.beginPath(); //热敏电阻符号线条，0 位于电阻方框左侧边的中间
	//上方的箭头
	g.context.moveTo(-8,26);   //箭头1起点   （y,x 坐标以中心为原点？）
	g.context.lineTo(8,12);		//箭头终点   
	g.context.moveTo(2,12);  	//箭头1箭头尖
	g.context.lineTo(8,12);		//箭头终点
	g.context.lineTo(8,18);	
	g.context.moveTo(12,26);   //箭头2起点   （y,x 坐标以中心为原点？）
	g.context.lineTo(26,12);		//箭头终点   
	g.context.moveTo(20,12);  	//箭头2箭头尖
	g.context.lineTo(26,12);		//箭头终点
	g.context.lineTo(26,18);	

	g.context.stroke();


	g.context.restore();
	if (sim.showValuesCheckItem.getState()) {
	    lux = LuxFromSliderPos();
	    resistance = calcResistance(lux);
	    String s = getShortUnitText(resistance, "");
	    drawValues(g, s+"\u03A9", hs);
	}
	doDots(g);
	drawPosts(g);
    }

    void calculateCurrent() {
	current = (volts[0]-volts[1])/resistance;
    }
    void stamp() {
	lux = LuxFromSliderPos();
	resistance = calcResistance(lux); 
	sim.stampResistor(nodes[0], nodes[1], resistance); 
    }

    void getInfo(String arr[]) {
	arr[0] = "photoresistor";
	arr[1] = "I = "+ getCurrentDText(current); //getBasicInfo(arr);
	arr[2] = "Vd = "+ getVoltageDText(getVoltageDiff());
	arr[3] = "R = " + getUnitText(resistance, Locale.ohmString);
	arr[4] = "P = " + getUnitText(getPower(), "W");
    }
    public EditInfo getEditInfo(int n) {
	if (n == 0) {
	    EditInfo ei = new EditInfo("Slider Text", 0, -1, -1);
	    ei.text = sliderText;
	    return ei;
	}
	return null;
    }
    //元件已被编辑
    public void setEditValue(int n, EditInfo ei) {
	if (n == 0) {
	    sliderText = ei.textf.getText();
	    label.setText(sliderText);
	    sim.setSlidersPanelHeight();
	}
	lux = LuxFromSliderPos();
	resistance = calcResistance(lux); 
    }
    void setMouseElm(boolean v) {
	super.setMouseElm(v);
	if (slider!=null)
	    slider.draw();
    }

    public void onMouseWheel(MouseWheelEvent e) {
	if (slider!=null)
	    slider.onMouseWheel(e);
    }

    double calcResistance(double lux) //已知光照度
    {
	//double loglux = Math.log10(lux);
	//double slope = -1.4;
	//double intercept = 7.1;
	//double logR = 	(loglux-intercept)/slope;

	//return Math.round(Math.pow(10, logR));
	double r = (maxLux-lux+1)*10;

	r = Math.round(r);
	return r;
    }
    double LuxFromSliderPos() //已知滑块位置等
    {
	return maxLux * position + minLux ;
    }

}

