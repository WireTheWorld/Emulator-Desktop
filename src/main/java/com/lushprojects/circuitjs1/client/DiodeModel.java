package com.lushprojects.circuitjs1.client;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import com.lushprojects.circuitjs1.client.util.Locale;

public class DiodeModel implements Editable, Comparable<DiodeModel> {

    static HashMap<String, DiodeModel> modelMap;
    
    int flags;
    String name, description;
    double saturationCurrent, seriesResistance, emissionCoefficient, breakdownVoltage;
    
    // 用于 UI 代码，不保证会被设置
    double forwardVoltage, forwardCurrent;
    
    boolean dumped;
    boolean readOnly;
    boolean builtIn;
    boolean oldStyle;
    boolean internal;
    static final int FLAGS_SIMPLE = 1; 
    
    // SPICE 默认温度 27°C (300.15 K) 下的电子热电压：
    static final double vt = 0.025865;
    // 二极管的"标度电压"，即使电流增大 e 倍所需的电压增量。
    double vscale;
    // 除以 vscale 的乘法等价形式（为了速度）。
    double vdcoef;
    // 1A 时的压降
    double fwdrop;
    
    protected DiodeModel(double sc, double sr, double ec, double bv, String d) {
	saturationCurrent = sc;
	seriesResistance = sr;
	emissionCoefficient = ec;
	breakdownVoltage = bv;
	description = d;
//	CirSim.console("creating diode model " + this);
//	CirSim.debugger();
	updateModel();
    }
    
    static DiodeModel getModelWithName(String name) {
	createModelMap();
	DiodeModel lm = modelMap.get(name);
	if (lm != null)
	    return lm;
	lm = new DiodeModel();
	lm.name = name;
	modelMap.put(name, lm);
	return lm;
    }
    
    static DiodeModel getModelWithNameOrCopy(String name, DiodeModel oldmodel) {
	createModelMap();
	DiodeModel lm = modelMap.get(name);
	if (lm != null)
	    return lm;
	if (oldmodel == null) {
	    CirSim.console("model not found: " + name);
	    return getDefaultModel();
	}
//	CirSim.console("copying to " + name);
	lm = new DiodeModel(oldmodel);
	lm.name = name;
	modelMap.put(name, lm);
	return lm;
    }
    
    static void createModelMap() {
	if (modelMap != null)
	    return;
	modelMap = new HashMap<String,DiodeModel>();
	addDefaultModel("spice-default", new DiodeModel(1e-14, 0, 1, 0, null));
	addDefaultModel("default", new DiodeModel(1.7143528192808883e-7, 0, 2, 0, null));
	addDefaultModel("default-zener", new DiodeModel(1.7143528192808883e-7, 0, 2, 5.6, null));
	
	// 旧的默认 LED 模型，饱和电流太小（会导致数值误差）
	addDefaultModel("old-default-led", new DiodeModel(2.2349907006671927e-18, 0, 2, 0, null).setInternal());
	
	// 新建 LED 的默认模型，https://www.diyaudio.com/forums/software-tools/25884-spice-models-led.html
	addDefaultModel("default-led", new DiodeModel(93.2e-12, .042, 3.73, 0, null));

	// https://www.allaboutcircuits.com/textbook/semiconductors/chpt-3/spice-models/
	addDefaultModel("1N5711", new DiodeModel(315e-9, 2.8, 2.03, 70, "Schottky"));
	addDefaultModel("1N5712", new DiodeModel(680e-12, 12, 1.003, 20, "Schottky"));

	// 模型不准确
	addDefaultModel("1N34", new DiodeModel(200e-12, 84e-3, 2.19, 60, "germanium").setInternal());

	addDefaultModel("1N4004", new DiodeModel(18.8e-9, 28.6e-3, 2, 400, "general purpose"));
//	addDefaultModel("1N3891", new DiodeModel(63e-9, 9.6e-3, 2, 0));  // doesn't match datasheet very well
	
	// http://users.skynet.be/hugocoolens/spice/diodes/1n4148.htm
	addDefaultModel("1N4148", new DiodeModel(4.352e-9, .6458, 1.906, 75, "switching"));
	addDefaultModel("x2n2646-emitter", new DiodeModel(2.13e-11, 0, 1.8, 0, null).setInternal());
	
	// 用于 TL431
	loadInternalModel("~tl431ed-d_ed 0 1e-14 5 1 0 0");
	
	// 用于 LM317
	loadInternalModel("~lm317-dz 0 1e-14 0 1 6.3 0");
    }

    static void addDefaultModel(String name, DiodeModel dm) {
	modelMap.put(name, dm);
	dm.readOnly = dm.builtIn = true;
	dm.name = name;
    }

    DiodeModel setInternal() {
	internal = true;
	return this;
    }
    
    // 使用给定参数创建新模型，保持向后兼容。我们使用的方法存在问题，但我们不想
    // 改变电路行为。我们不再这样做，因为我们发现通过改变漏电流来获得给定的压降
    // 效果不佳；漏电流可能过高或过低。
    static DiodeModel getModelWithParameters(double fwdrop, double zvoltage) {
	createModelMap();
	
	final double emcoef = 2;

	// 查找具有相同参数的现有模型
	Iterator it = modelMap.entrySet().iterator();
	while (it.hasNext()) {
	    Map.Entry<String,DiodeModel> pair = (Map.Entry)it.next();
	    DiodeModel dm = pair.getValue();
	    if (Math.abs(dm.fwdrop-fwdrop) < 1e-8 && dm.seriesResistance == 0 && Math.abs(dm.breakdownVoltage-zvoltage) < 1e-8 && dm.emissionCoefficient == emcoef)
		return dm;
	}

	// 创建一个新模型，转换为新的参数值
	final double vscale = emcoef * vt;
	final double vdcoef = 1 / vscale;
	double leakage = 1 / (Math.exp(fwdrop * vdcoef) - 1);
	String name = "fwdrop=" + fwdrop;
	if (zvoltage != 0)
	    name = name + " zvoltage=" + zvoltage;
	DiodeModel dm = getModelWithName(name);
//	CirSim.console("got model with name " + name);
	dm.saturationCurrent = leakage;
	dm.emissionCoefficient = emcoef;
	dm.breakdownVoltage = zvoltage;
	dm.readOnly = dm.oldStyle = true;
//	CirSim.console("at drop current is " + (leakage*(Math.exp(fwdrop*vdcoef)-1)));
//	CirSim.console("sat " + leakage + " em " + emcoef);
	dm.updateModel();
	return dm;
    }
    
    static DiodeModel getDefaultModel() {
	return getModelWithName("default");
    }
    
    static void loadInternalModel(String s) {
        StringTokenizer st = new StringTokenizer(s);
        DiodeModel dm = undumpModel(st);
        dm.builtIn = dm.internal = true;
    }

    static void clearDumpedFlags() {
	if (modelMap == null)
	    return;
	Iterator it = modelMap.entrySet().iterator();
	while (it.hasNext()) {
	    Map.Entry<String,DiodeModel> pair = (Map.Entry)it.next();
	    pair.getValue().dumped = false;
	}
    }
    
    static Vector<DiodeModel> getModelList(boolean zener) {
	Vector<DiodeModel> vector = new Vector<DiodeModel>();
	Iterator it = modelMap.entrySet().iterator();
	while (it.hasNext()) {
	    Map.Entry<String,DiodeModel> pair = (Map.Entry)it.next();
	    DiodeModel dm = pair.getValue();
	    if (dm.internal)
		continue;
	    if (zener && dm.breakdownVoltage == 0)
		continue;
	    if (!vector.contains(dm))
		vector.add(dm);
	}
	Collections.sort(vector);
	return vector;
    }

    public int compareTo(DiodeModel dm) {
	return name.compareTo(dm.name);
    }
    
    String getDescription() {
	if (description == null)
	    return name;
	return name + " (" + Locale.LS(description) + ")";
    }
    
    DiodeModel() {
	saturationCurrent = 1e-14;
	seriesResistance = 0;
	emissionCoefficient = 1;
	breakdownVoltage = 0;
	updateModel();
    }
    
    DiodeModel(DiodeModel copy) {
	flags = copy.flags;
	saturationCurrent = copy.saturationCurrent;
	seriesResistance = copy.seriesResistance;
	emissionCoefficient = copy.emissionCoefficient;
	breakdownVoltage = copy.breakdownVoltage;
	forwardCurrent = copy.forwardCurrent;
	updateModel();
    }

    static DiodeModel undumpModel(StringTokenizer st) {
	String name = CustomLogicModel.unescape(st.nextToken());
	DiodeModel dm = DiodeModel.getModelWithName(name);
	dm.undump(st);
	return dm;
    }
    
    void undump(StringTokenizer st) {
	flags = new Integer(st.nextToken()).intValue();
	saturationCurrent = Double.parseDouble(st.nextToken());
	seriesResistance = Double.parseDouble(st.nextToken());
	emissionCoefficient = Double.parseDouble(st.nextToken());
	breakdownVoltage = Double.parseDouble(st.nextToken());
	try {
	    forwardCurrent = Double.parseDouble(st.nextToken());
	} catch (Exception e) {}
	updateModel();
    }
    
    public EditInfo getEditInfo(int n) {
	if (n == 0) {
	    EditInfo ei = new EditInfo("Model Name", 0);
	    ei.text = name == null ? "" : name;
	    return ei;
	}
	if (n == 1)
	    return new EditInfo("Saturation Current", saturationCurrent, -1, -1);
	if (isSimple()) {
	    if (n == 2)
		return new EditInfo("Forward Voltage", forwardVoltage, -1, -1);
	    if (n == 3)
		return new EditInfo("Current At Above Voltage (A)", forwardCurrent, -1, -1);
	} else {
	    if (n == 2)
		return new EditInfo("Series Resistance", seriesResistance, -1, -1);
	    if (n == 3)
		return new EditInfo(EditInfo.makeLink("diodecalc.html", "Emission Coefficient"), emissionCoefficient, -1, -1);
	}
	if (n == 4)
	    return new EditInfo("Breakdown Voltage", breakdownVoltage, -1, -1);
	return null;
    }

    public void setEditValue(int n, EditInfo ei) {
	if (n == 0) {
	    name = ei.textf.getText();
	    if (name.length() > 0)
		modelMap.put(name, this);
	}
	if (n == 1)
	    saturationCurrent = ei.value;
	if (isSimple()) {
	    if (n == 2)
		forwardVoltage = ei.value;
	    if (n == 3)
		forwardCurrent = ei.value;
	    setEmissionCoefficient();
	} else {
	    if (n == 2)
		seriesResistance = ei.value;
	    if (n == 3)
		emissionCoefficient = ei.value;
	}
	if (n == 4)
	    breakdownVoltage = Math.abs(ei.value);
	updateModel();
	CirSim.theSim.updateModels();
    }

    // 如果有足够的数据，为简单模式设置发射系数  
    void setEmissionCoefficient() {
	if (forwardCurrent > 0 && forwardVoltage > 0)
	    emissionCoefficient = (forwardVoltage/Math.log(forwardCurrent/saturationCurrent+1)) / vt;

	seriesResistance = 0;
    }
    
    public void setForwardVoltage() {
	if (forwardCurrent == 0)
	    forwardCurrent = 1;
	forwardVoltage = emissionCoefficient*vt * Math.log(forwardCurrent/saturationCurrent+1);
    }
    
    void updateModel() {
	vscale = emissionCoefficient * vt;
	vdcoef = 1/vscale;
	fwdrop = Math.log(1/saturationCurrent + 1) * emissionCoefficient * vt;
    }
    
    String dump() {
	dumped = true;
	return "34 " + CustomLogicModel.escape(name) + " " + flags + " " + saturationCurrent + " " + seriesResistance + " " + emissionCoefficient + " " + breakdownVoltage + " " + forwardCurrent;
    }
    
    boolean isSimple() {
	return (flags & FLAGS_SIMPLE) != 0;
    }
    
    void setSimple(boolean s) {
	flags = (s) ? FLAGS_SIMPLE : 0;
    }
    
    void pickName() {
	if (breakdownVoltage > 0 && breakdownVoltage < 20)
	    name = "zener-" + CircuitElm.showFormat.format(breakdownVoltage);
	else if (isSimple())
	    name = "fwdrop=" + CircuitElm.showFormat.format(forwardVoltage);
	else
	    name = "diodemodel";
	if (modelMap.get(name) != null) {
	    int num = 2;
	    for (; ; num++) {
		String n = name + "-" + num;
		if (modelMap.get(n) == null) {
		    name = n;
		    break;
		}
	    }
	}
    }
}
