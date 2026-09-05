package com.lushprojects.circuitjs1.client;

import com.google.gwt.user.client.ui.Button;
import com.lushprojects.circuitjs1.client.util.Locale;

public class CustomLogicElm extends ChipElm {
    String modelName;
    int postCount;
    int inputCount, outputCount;
    CustomLogicModel model;
    boolean lastValues[];
    boolean patternValues[];
    boolean highImpedance[];
    static String lastModelName = "default";
    
    public CustomLogicElm(int xx, int yy) {
	super(xx, yy);
	modelName = lastModelName;
	setupPins();
    }

    public CustomLogicElm(int xa, int ya, int xb, int yb, int f,
            StringTokenizer st) {
	super(xa, ya, xb, yb, f, st);
	modelName = CustomLogicModel.unescape(st.nextToken());
	updateModels();
	int i;
	for (i = 0; i != getPostCount(); i++) {
	    if (pins[i].output) {
		volts[i] = new Double(st.nextToken()).doubleValue();
		pins[i].value = volts[i] > getThreshold();
	    }
	}
    }
    
    String dump() {
	String s = super.dump();
	s += " " + CustomLogicModel.escape(modelName);

	// 在 ChipElm 中执行此操作的代码在这里不适用，因为在读取模型名称之前
	// 我们不知道要读取多少个引脚！因此我们必须在
	// 这里重复该代码。
        int i;
        for (i = 0; i != getPostCount(); i++) {
            if (pins[i].output)
                s += " " + volts[i];
        }
	return s;
    }
    
    String dumpModel() {
	if (model.dumped)
	    return "";
	return model.dump();
    }
    
    public void updateModels() {
	model = CustomLogicModel.getModelWithNameOrCopy(modelName, model);
	setupPins();
	allocNodes();
	setPoints();
    }
    
    @Override
    void setupPins() {
	if (modelName == null) {
	    postCount = bits;
	    allocNodes();
	    return;
	}
	
	model = CustomLogicModel.getModelWithName(modelName);
	inputCount = model.inputs.length;
	outputCount = model.outputs.length;
	sizeY = inputCount > outputCount ? inputCount : outputCount;
	if (sizeY == 0)
	    sizeY = 1;
	sizeX = 2;
	postCount = inputCount+outputCount;
	pins = new Pin[postCount];
	int i;
	for (i = 0; i != inputCount; i++) {
	    pins[i] = new Pin(i, SIDE_W, model.inputs[i]);
	    pins[i].fixName();
	}
	for (i = 0; i != outputCount; i++) {
	    pins[i+inputCount] = new Pin(i, SIDE_E, model.outputs[i]);
	    pins[i+inputCount].output = true;
	    pins[i+inputCount].fixName();
	}
	lastValues = new boolean[postCount];
	patternValues = new boolean[26];
	highImpedance = new boolean[postCount];
    }

    int getPostCount() { return postCount; }
    
    @Override
    int getVoltageSourceCount() {
	return outputCount;
    }

    // 跟踪是否存在三态输出。如果没有，则可以大幅简化，使模拟更快
    boolean hasTriState() { return model == null ? false : model.triState; }
    
    boolean nonLinear() { return hasTriState(); }
    
    int getInternalNodeCount() {
	// 对于三态输出，我们需要一个内部节点来连接电压源，然后从那里连接一个电阻到输出。
	// 只要有任何输出是三态的，我们就对所有输出都这样做
	return (hasTriState()) ? outputCount : 0; 
    }
    
    void stamp() {
	int i;
	int add = (hasTriState()) ? outputCount : 0;
	for (i = 0; i != getPostCount(); i++) {
	    Pin p = pins[i];
	    if (p.output) {
		sim.stampVoltageSource(0, nodes[i+add], p.voltSource);
		if (hasTriState()) {
		    sim.stampNonLinear(nodes[i+add]);
		    sim.stampNonLinear(nodes[i]);
		}
	    }
	}
    }
    
    void doStep() {
	int i;
	for (i = 0; i != getPostCount(); i++) {
	    Pin p = pins[i];
	    if (!p.output)
		p.value = volts[i] > getThreshold();
	}
	execute();
	int add = (hasTriState()) ? outputCount : 0;
	for (i = 0; i != getPostCount(); i++) {
	    Pin p = pins[i];
	    if (p.output) {
		// 连接输出电压源（三态时连接到内部节点，否则直接连接到输出）
		sim.updateVoltageSource(0, nodes[i+add], p.voltSource, p.value ? highVoltage : 0);
		
		// 如有必要，为三态添加电阻
		if (hasTriState())
		    sim.stampResistor(nodes[i+add], nodes[i], highImpedance[i] ? 1e8 : 1e-3);
	    }
	}
    }

    void execute() {
	int i;
	for (i = 0; i != model.rulesLeft.size(); i++) {
	    // 检查是否匹配
	    String rl = model.rulesLeft.get(i);
	    int j;
	    for (j = 0; j != rl.length(); j++) {
		char x = rl.charAt(j);
		if (x == '0' || x == '1') {
		    if (pins[j].value == (x == '1'))
			continue;
		    break;
		}
		
		// 任意值，忽略
		if (x == '?')
		    continue;
		
		// 上升沿转换
		if (x == '+') {
		    if (pins[j].value && !lastValues[j])
			continue;
		    break;
		}
		
		// 下降沿转换
		if (x == '-') {
		    if (!pins[j].value && lastValues[j])
			continue;
		    break;
		}
		
		// 保存模式值
		if (x >= 'a' && x <= 'z') {
		    patternValues[x-'a'] = pins[j].value;
		    continue;
		}
		
		// 比较模式值
		if (x >= 'A' && x <= 'z') {
		    if (patternValues[x-'A'] != pins[j].value)
			break;
		    continue;
		}
	    }
	    if (j != rl.length())
		continue;
	    
	    // 匹配成功
	    String rr = model.rulesRight.get(i);
	    for (j = 0; j != rr.length(); j++) {
		char x = rr.charAt(j);
		highImpedance[j+inputCount] = false;
		if (x >= 'a' && x <= 'z')
		    pins[j+inputCount].value = patternValues[x-'a'];
		else if (x == '_')
		    highImpedance[j+inputCount] = true;
		else
		    pins[j+inputCount].value = (x == '1');
	    }
	    break;
	}
	
	// 保存值用于转换检测
	int j;
	for (j = 0; j != postCount; j++)
	    lastValues[j] = pins[j].value;
    }
    
    public EditInfo getChipEditInfo(int n) {
	if (n == 0) {
	    EditInfo ei = new EditInfo("Model Name", 0, -1, -1);
	    ei.text = modelName;
	    ei.disallowSliders();
	    return ei;
	}
	if (n == 1) {
            EditInfo ei = new EditInfo("", 0, -1, -1);
            ei.button = new Button(Locale.LS("Edit Model"));
            return ei;
	}
	return null;
    }
    
    public void setChipEditValue(int n, EditInfo ei) {
	if (n == 0) {
	    String newModelName = ei.textf.getText();
	    if (modelName.equals(newModelName))
		return;
	    modelName = lastModelName = ei.textf.getText();
	    model = CustomLogicModel.getModelWithNameOrCopy(modelName, model);
	    setupPins();
	    allocNodes();
	    setPoints();
	    return;
	}
	if (n == 1) {
	    EditDialog editDialog = new EditDialog(model, sim);
	    CirSim.customLogicEditDialog = editDialog;
	    editDialog.show();
	    return;
	}
    }
    
    int getDumpType() { return 208; }

    void getInfo(String arr[]) {
	super.getInfo(arr);
	arr[0] = model.infoText;
    }
}
