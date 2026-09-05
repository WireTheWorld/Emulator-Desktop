
package com.lushprojects.circuitjs1.client;

import com.google.gwt.user.client.ui.Label;
import com.lushprojects.circuitjs1.client.util.Locale;
import com.google.gwt.user.client.Command;
import com.google.gwt.canvas.dom.client.CanvasGradient;
import com.google.gwt.event.dom.client.MouseWheelEvent;
import com.google.gwt.event.dom.client.MouseWheelHandler;

/*Bill Collis - 2015年6月

Vishay 热敏电阻 ntcle100 的数据手册
热敏电阻模型
view-source:http://www.giangrandi.ch/electronics/ntc/ntc.shtml
http://www.giangrandi.ch/electronics/ntc/ntcparam.html
不考虑发热效应
ID = 350
将 id 添加到 CirSim.constructElement、CirSim.createCe 和 CirSim.composeMainMenu 中
 */
class ThermistorNTCElm extends CircuitElm implements Command, MouseWheelHandler {
    double position; //滑块的当前位置 0.005 到 0.995
    double resistance; //根据滑块位置计算出的电阻
    double minTempr, maxTempr; //摄氏度 - 注意最小为 -40，最大为 +150 摄氏度
    double temperature; //根据滑块值（0.005 - 0.995）在 minTempr - maxTempr 之间的比例计算得出
    double r25, r50; //用户可根据数据手册输入的电阻值：25 摄氏度时的 R 和 50 摄氏度时的 R
    double rneg40; //最大电阻 - 在 -40 摄氏度时取得
    double b25100; //基于两个温度下的两个电阻值计算出的常数
    double t0 = 273.15;
    double t25 = t0 + 25;

    Scrollbar slider; //from Pot
    Label label;
    String sliderText;

    //构造函数 - 最初创建时调用
    public ThermistorNTCElm(int xx, int yy) {
	super(xx, yy);
	//setup();
	minTempr = -40;//摄氏度
	maxTempr = 150; 
	r25 = 10000; //默认 10k 热敏电阻，例如 NTCLE100E3010 Vishay
	r50 = 3605;
	position = .34; //对于 -40 到 150 摄氏度的范围，对应 25 摄氏度
	//热敏电阻计算
	rneg40 = calcResistance(minTempr); //对于 10k ntc 约为 400k	
	b25100 = calcB25100(); //	
	temperature = temprFromSliderPos();
	resistance = calcResistance(temperature); 
	sliderText = "Temperature";
	createSlider();
    }

    //构造函数 - 从文件读取时调用
    public ThermistorNTCElm(int xa, int ya, int xb, int yb, int f,
	    StringTokenizer st) {
	super(xa, ya, xb, yb, f);
	r25 = new Double(st.nextToken()).doubleValue();
	r50 = new Double(st.nextToken()).doubleValue();
	minTempr = new Double(st.nextToken()).doubleValue();
	maxTempr = new Double(st.nextToken()).doubleValue();
	position = new Double(st.nextToken()).doubleValue();
	//热敏电阻计算
	rneg40 = calcResistance(minTempr); //对于 10k ntc 约为 400k	
	b25100 = calcB25100(); //
	temperature = temprFromSliderPos();
	resistance = calcResistance(temperature); 
	sliderText = CustomLogicModel.unescape(st.nextToken());
	createSlider(); //使用 position 来设置滑块
    }

    //void setup() {
    //}

    int getPostCount() { return 2; }
    int getDumpType() { return 350; } //NTC 热敏电阻

    //用于文件保存的数据 - 确保与文件输入构造函数中的项目顺序一致
    String dump() { 
	return super.dump() + " " + r25 + " " + r50 + " " + minTempr + " " + maxTempr +" " + position  + " " + CustomLogicModel.escape(sliderText); 
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
	position = slider.getValue()*.0099+.005;
	temperature = temprFromSliderPos();
	resistance = calcResistance(temperature); 
	ps3 = new Point();
	ps4 = new Point();
    }

    void draw(Graphics g) { //借用 Resistor 的绘制方法
	//int segments = 16;
	int i;
	//int ox = 0;
	int hs=6; //这是宽度吗
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
	g.context.moveTo(0-hs,hs*2);
	g.context.lineTo(hs,hs*2);
	g.context.lineTo(len,-hs*2);
	g.context.stroke();


	g.context.restore();
	if (sim.showValuesCheckItem.getState()) {
	    temperature = temprFromSliderPos();
	    resistance = calcResistance(temperature);
	    String s = getShortUnitText(resistance, "");
	    String t = Character.toString((char)176);
	    //drawValues(g, "-t:"+s, hs);
	    drawValues(g, temperature+t+"C="+s+"\u03A9", hs);
	}
	doDots(g);
	drawPosts(g);
    }

    void calculateCurrent() {
	current = (volts[0]-volts[1])/resistance;
    }
    void stamp() {
	temperature = temprFromSliderPos(); //例如范围为 -40 到 +150 时，取值 190 - 40
	resistance = calcResistance(temperature); 
	sim.stampResistor(nodes[0], nodes[1], resistance); //也显示温度？？
    }

    void getInfo(String arr[]) {
	arr[0] = "thermistor";
	arr[1] = "I = "+ getCurrentDText(current); //getBasicInfo(arr);
	arr[2] = "Vd = "+ getVoltageDText(getVoltageDiff());
	arr[3] = "R = " + getUnitText(resistance, Locale.ohmString);
	arr[4] = "P = " + getUnitText(getPower(), "W");
	arr[5] = "T = " + getUnitText(temperature, "\u00b0C");
    }
    public EditInfo getEditInfo(int n) {
	// ohmString 在 linux 上在这里不起作用
	if (n == 0)
	    return new EditInfo("R at 25\u00b0C", r25, r50+100, 100000); //限制：r25 必须大于 r50
	if (n == 1)
	    return new EditInfo("R at 50\u00b0C", r50, 100, r25-100);
	if (n == 2)
	    return new EditInfo("Slider min temp (\u00b0C)", minTempr, -40, maxTempr); //限制：maxTempr 必须大于 minTempr
	if (n == 3)
	    return new EditInfo("Slider max temp (\u00b0C)", maxTempr, minTempr, 150);
	if (n == 4) {
	    EditInfo ei = new EditInfo("Slider Text", 0, -1, -1);
	    ei.text = sliderText;
	    return ei;
	}
	return null;
    }
    //元件已被编辑
    public void setEditValue(int n, EditInfo ei) {
	if (n == 0)
	    r25 = ei.value; 
	if (n == 1)
	    r50 = ei.value; 
	if (n == 2)
	    minTempr = ei.value; 
	if (n == 3)
	    maxTempr = ei.value; 
	if (n == 4) {
	    sliderText = ei.textf.getText();
	    label.setText(sliderText);
	    sim.setSlidersPanelHeight();
	}
	rneg40 = calcResistance(minTempr);
	b25100 = calcB25100(); //	
	temperature = temprFromSliderPos();
	resistance = calcResistance(temperature); 
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

    double calcResistance(double tempr) //已知温度
    {
	return Math.round(r25 * Math.exp(b25100 * ((1 / (tempr + t0)) - (1 / t25))));
    }
    double temprFromSliderPos() //已知滑块位置等
    {
	return Math.round( position * (maxTempr - minTempr) + minTempr);
    }
    //确定常数 B25100 - 已知两个温度下的两个电阻值时
    double calcB25100() //给定 R25=10000 和 R50=3605 时，B25100 将为 3932
    {
	double kelvin1 = t0 + 25;
	double kelvin2 = t0 + 50;
	return ( Math.log(r25) - Math.log(r50) ) / ( (1/kelvin1) - (1/kelvin2) );

    }
}

