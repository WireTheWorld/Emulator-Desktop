package com.lushprojects.circuitjs1.client;

import java.util.Vector;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Label;
import com.lushprojects.circuitjs1.client.util.Locale;

// 带滑块的数值
public class Adjustable implements Command {
    CircuitElm elm;
    double minValue, maxValue;
    int flags;
    String sliderText;
    
    // 如果该 Adjustable 有自己的滑块则为 null，如果它共享另一个滑块则为非 null。
    Adjustable sharedSlider;
    
    final int FLAG_SHARED = 1;
    
    // 该滑块控制的 getEditInfo() 列表中的值的索引
    int editItem;
    
    Label label;
    Scrollbar slider;
    boolean settingValue;
    
    Adjustable(CircuitElm ce, int item) {
	minValue = 1;
	maxValue = 1000;
	flags = 0;
	elm = ce;
	editItem = item;
        EditInfo ei = ce.getEditInfo(editItem);
        if (ei != null && ei.maxVal > 0) {
            minValue = ei.minVal;
            maxValue = ei.maxVal;
        }
    }

    // 反序列化
    Adjustable(StringTokenizer st, CirSim sim) {
	int e = Integer.parseInt(st.nextToken());
	if (e == -1)
	    return;
	try {
	    String ei = st.nextToken();

	    // 最初的代码忘记转储 "flags" 字段，所以我们必须这样做以支持向后兼容
	    if (ei.startsWith("F")) {
		flags = Integer.parseInt(ei.substring(1));
		ei = st.nextToken();
	    }
	    
	    editItem = Integer.parseInt(ei);
	    minValue = Double.parseDouble(st.nextToken());
	    maxValue = Double.parseDouble(st.nextToken());
	    if ((flags & FLAG_SHARED) != 0) {
		int ano = Integer.parseInt(st.nextToken());
		sharedSlider = ano == -1 ? null : sim.adjustables.get(ano);
	    }
	    sliderText = CustomLogicModel.unescape(st.nextToken());
	} catch (Exception ex) {}
	try {
	    elm = sim.getElm(e);
	} catch (Exception ex) {}
    }
    
    boolean createSlider(CirSim sim) {
	if (elm == null)
	    return false;
	EditInfo ei = elm.getEditInfo(editItem);
	if (ei == null)
	    return false;
	if (sharedSlider != null)
	    return true;
	if (sliderText.length() == 0)
	    return false;
	double value = ei.value;
	createSlider(sim, value);
	return true;
    }

    void createSlider(CirSim sim, double value) {
        sim.addWidgetToVerticalPanel(label = new Label(Locale.LS(sliderText)));
        label.addStyleName("topSpace");
        int intValue = (int) ((value-minValue)*100/(maxValue-minValue));
        sim.addWidgetToVerticalPanel(slider = new Scrollbar(Scrollbar.HORIZONTAL, intValue, 1, 0, 101, this, elm));
    }

    void setSliderValue(double value) {
	if (sharedSlider != null) {
	    sharedSlider.setSliderValue(value);
	    return;
	}
        int intValue = (int) ((value-minValue)*100/(maxValue-minValue));
        settingValue = true; // 不要在 execute() 中再次递归设置值
        slider.setValue(intValue);
        settingValue = false;
    }
    
    public void execute() {
	if (settingValue)
	    return;
	int i;
	CirSim sim = CirSim.theSim;
	for (i = 0; i != sim.adjustables.size(); i++) {
	    Adjustable adj = sim.adjustables.get(i);
	    if (adj == this || adj.sharedSlider == this)
		adj.executeSlider();
	}
    }
    
    void executeSlider() {
	elm.sim.analyzeFlag = true;
	EditInfo ei = elm.getEditInfo(editItem);
	ei.value = getSliderValue();
	elm.setEditValue(editItem, ei);
	elm.sim.repaint();
    }
    
    double getSliderValue() {
	double val = sharedSlider == null ? slider.getValue() : sharedSlider.slider.getValue();
	return minValue + (maxValue-minValue)*val/100;
    }
    
    void deleteSlider(CirSim sim) {
	try {
	    sim.removeWidgetFromVerticalPanel(label);
	    sim.removeWidgetFromVerticalPanel(slider);
	} catch (Exception e) {}
    }
    
    void setMouseElm(CircuitElm e) {
	if (slider != null)
	    slider.draw();
    }
    
    boolean sliderBeingShared() {
	int i;
	for (i = 0; i != CirSim.theSim.adjustables.size(); i++) {
	    Adjustable adj = CirSim.theSim.adjustables.get(i);
	    if (adj.sharedSlider == this)
		return true;
	}
	return false;
    }
    
    String dump() {
	int ano = -1;
	if (sharedSlider != null)
	    ano = CirSim.theSim.adjustables.indexOf(sharedSlider);
	
	return elm.sim.locateElm(elm) + " F1 " + editItem + " " + minValue + " " + maxValue + " " + ano + " " +
			CustomLogicModel.escape(sliderText);
    }
    
    // 重新排列可调项，使带滑块的项目排在列表前面，随后是引用它们的项目。
    // 这简化了 UI 代码，也使转储/反序列化可调项列表容易得多，因为我们总是
    // 先反序列化带滑块的可调项，然后再反序列化引用它们的可调项。
    static void reorderAdjustables() {
	Vector<Adjustable> newList = new Vector<Adjustable>();
	Vector<Adjustable> oldList = CirSim.theSim.adjustables;
	int i;
	for (i = 0; i != oldList.size(); i++) {
	    Adjustable adj = oldList.get(i);
	    if (adj.sharedSlider == null)
		newList.add(adj);
	}
	for (i = 0; i != oldList.size(); i++) {
	    Adjustable adj = oldList.get(i);
	    if (adj.sharedSlider != null)
		newList.add(adj);
	}
	CirSim.theSim.adjustables = newList;
    }
}
