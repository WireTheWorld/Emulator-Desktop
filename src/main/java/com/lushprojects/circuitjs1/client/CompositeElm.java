package com.lushprojects.circuitjs1.client;

import java.util.Vector;
import java.util.HashMap;
import java.util.Map.Entry;

// 由其他电路元件组合而成的电路元件
// 使用这种方式在仿真性能上会（相对）低效，因为
// 元件的所有内部工作原理都是通过各个独立元件仿真出来的。
// 然而，它可能让某些类型的元件能够更快地编写进仿真器，
// 而不必从头编写每个元件。
//
// 它还提供了一种途径，让用户创建的电路能够
// 作为新的电路元件重新导入到仿真中。

// 实例化时应该：
// - 在构造函数中设置 "diagonal" 变量
// - 重写构造函数以设置元件的焊盘/引线等，并配置 CompositeElm 的内容
// - 重写 getDumpType、dump、draw、getInfo、setPoints、canViewInScope

public abstract class CompositeElm extends CircuitElm {

    // 需要使用 escape() 而不是把空格转换为 _，以便复合元件可以嵌套
    final int FLAG_ESCAPE = 1;
    
    // 此子电路中包含的元件列表
    Vector<CircuitElm> compElmList;
    
    // 节点列表，将每个节点映射到引用该节点的元件列表
    protected Vector<CircuitNode> compNodeList;
    
    protected int numPosts = 0;
    protected int numNodes = 0;
    protected Point posts[];
    protected Vector<VoltageSourceRecord> voltageSources;

    CompositeElm(int xx, int yy) {
	super(xx, yy);
    }
    
    public CompositeElm(int xa, int ya, int xb, int yb, int f) {
	super(xa, ya, xb, yb, f);
    }
    
    CompositeElm(int xx, int yy, String s, int externalNodes[]) {
	super(xx, yy);
	loadComposite(null, s, externalNodes);
	allocNodes();
    }

    public CompositeElm(int xa, int ya, int xb, int yb, int f, StringTokenizer st, String s, int externalNodes[]) {
	super(xa, ya, xb, yb, f);
	loadComposite(st, s, externalNodes);
	allocNodes();
    }

    boolean useEscape() { return (flags & FLAG_ESCAPE) != 0; }
    
    public void loadComposite(StringTokenizer stIn, String model, int externalNodes[]) {
	HashMap<Integer, CircuitNode> compNodeHash = new HashMap<Integer, CircuitNode>();
	StringTokenizer modelLinet = new StringTokenizer(model, "\r");
	CircuitNode cn;
	CircuitNodeLink cnLink;
	VoltageSourceRecord vsRecord;

	compElmList = new Vector<CircuitElm>();
	compNodeList = new Vector<CircuitNode>();
	voltageSources = new Vector<VoltageSourceRecord>();

	// 从输入字符串构建 compElmList 和 compNodeHash

	while (modelLinet.hasMoreTokens()) {
	    String line = modelLinet.nextToken();
	    StringTokenizer stModel = new StringTokenizer(line, " +\t\n\r\f");
	    String ceType = stModel.nextToken();
	    CircuitElm newce = CirSim.constructElement(ceType, 0, 0);
	    if (stIn!=null) {
		int tint = newce.getDumpType();
		String dumpedCe= stIn.nextToken();
		if (useEscape())
		    dumpedCe = CustomLogicModel.unescape(dumpedCe);
		StringTokenizer stCe = new StringTokenizer(dumpedCe, useEscape() ? " " : "_");
		int flags = new Integer(stCe.nextToken()).intValue();
		newce = CirSim.createCe(tint, 0, 0, 0, 0, flags, stCe);
	    }
	    if (newce instanceof GroundElm)
		((GroundElm) newce).setOldStyle();
	    compElmList.add(newce);

	    int thisPost = 0;
	    while (stModel.hasMoreTokens()) {
		int nodeOfThisPost = new Integer(stModel.nextToken()).intValue();

		// node = 0 表示接地
		if (nodeOfThisPost == 0) {
		    newce.setNode(thisPost, 0);
		    newce.setNodeVoltage(thisPost, 0);
		    thisPost++;
		    continue;
		}
		cnLink = new CircuitNodeLink();
		cnLink.num = thisPost;
		cnLink.elm = newce;
		if (!compNodeHash.containsKey(nodeOfThisPost)) {
		    cn = new CircuitNode();
		    cn.links.add(cnLink);
		    compNodeHash.put(nodeOfThisPost, cn);
		} else {
		    cn = compNodeHash.get(nodeOfThisPost);
		    cn.links.add(cnLink);
		}
		thisPost++;
	    }
	}

	// 将 compNodeHash 展开为 compNodeList
	numPosts = externalNodes.length;
	for (int i = 0; i < externalNodes.length; i++) { // 外部节点优先
	    if (compNodeHash.containsKey(externalNodes[i])) {
		compNodeList.add(compNodeHash.get(externalNodes[i]));
		compNodeHash.remove(externalNodes[i]);
	    } else
		throw new IllegalArgumentException();
	}
	for (Entry<Integer, CircuitNode> entry : compNodeHash.entrySet()) {
	    int key = entry.getKey();
	    compNodeList.add(compNodeHash.get(key));
	}

	// 为子元件的内部节点分配更多节点
	for (int i = 0; i != compElmList.size(); i++) {
	    CircuitElm ce = compElmList.get(i);
	    int inodes = ce.getInternalNodeCount();
	    for (int j = 0; j != inodes; j++) {
		cnLink = new CircuitNodeLink();
		cnLink.num = j + ce.getPostCount();
		cnLink.elm = ce;
		cn = new CircuitNode();
		cn.links.add(cnLink);
		compNodeList.add(cn);
	    }
	}

	numNodes = compNodeList.size();

//	CirSim.console("Dumping compNodeList");
//	for (int i = 0; i < numNodes; i++) {
//	    CirSim.console("New node" + i + " Size of links:" + compNodeList.get(i).links.size());
//	}

	posts = new Point[numPosts];
	
	// 枚举电压源
	for (int i = 0; i < compElmList.size(); i++) {
	    int cnt = compElmList.get(i).getVoltageSourceCount();
	    for (int j=0;j < cnt ; j++) {
		vsRecord = new VoltageSourceRecord();
		vsRecord.elm = compElmList.get(i);
		vsRecord.vsNumForElement = j;
		voltageSources.add(vsRecord);
	    }
	}
	
	// 使用 escape() 转储新电路
	flags |= FLAG_ESCAPE;
    }

    public boolean nonLinear() {
	for (int i = 0; i < compElmList.size(); i++)
	    if (compElmList.get(i).nonLinear())
		return true;
	return false;
    }

    public String dump() {
	String dumpStr=super.dump();
	dumpStr += dumpElements();
	return dumpStr;
    }

    public String dumpElements() {
	String dumpStr = "";
	for (int i = 0; i < compElmList.size(); i++) {
	    String tstring = compElmList.get(i).dump();
	    tstring = tstring.replaceFirst("[A-Za-z0-9]+ 0 0 0 0 ", ""); // 移除内部元件未使用的 tint x1 y1 x2 y2 坐标
	    dumpStr += " "+ CustomLogicModel.escape(tstring);
	}
	return dumpStr;
    }

    // 转储元件的子集（其中一些可能没有任何状态，和/或可能非常长，因此为简洁起见我们避免转储它们）
    public String dumpWithMask(int mask) {
	String dumpStr=super.dump();
	return dumpStr + dumpElements(mask);
    }

    public String dumpElements(int mask) {
	String dumpStr = "";
	for (int i = 0; i < compElmList.size(); i++) {
	    if ((mask & (1<<i)) == 0)
		continue;
	    String tstring = compElmList.get(i).dump();
	    tstring = tstring.replaceFirst("[A-Za-z0-9]+ 0 0 0 0 ", ""); // 移除内部元件未使用的 tint x1 y1 x2 y2 坐标
	    dumpStr += " "+ CustomLogicModel.escape(tstring);
	}
	return dumpStr;
    }

    // n1 和 n2 在内部是否以某种方式相连？
    public boolean getConnectionSlow(int n1, int n2) {
	Vector<Integer> connectedNodes = new Vector<Integer>();

	// 维护与 n1 相连的节点列表
	connectedNodes.add(n1);
	int i;
	for (i = 0; i < connectedNodes.size(); i++) {
	    // 列表中的下一个节点
	    int n = connectedNodes.get(i);
	    if (n == n2)
		return true;
	    
	    // 查找与 n 相连的所有元件
	    Vector<CircuitNodeLink> cnLinks = compNodeList.get(n).links;
	    for (int j = 0; j < cnLinks.size(); j++) {
		CircuitNodeLink link = cnLinks.get(j);
		CircuitElm lelm = link.elm;
		// 遍历该元件拥有的所有其他节点
		for (int k = 0; k != lelm.getConnectionNodeCount(); k++)
		    // 它们相连吗？
		    if (k != link.num && lelm.getConnection(link.num, k)) {
			int kn = lelm.getConnectionNode(k);
			if (kn == 0)
			    return true;
			int m;
			// 查找本地节点编号（kn 是全局的）并将其添加到列表
			for (m = 0; m != nodes.length; m++)
			    if (nodes[m] == kn && !connectedNodes.contains(m))
				connectedNodes.add(m);
		    }
	    }
	}
	return false;
    }
    
    HashMap<IntPair, Boolean> connectionMap;
    HashMap<Integer, Boolean> groundConnectionMap;

    public boolean getConnection(int n1, int n2) {
	if (connectionMap == null)
	    connectionMap = new HashMap<IntPair, Boolean>();
	IntPair key = new IntPair(n1, n2);
	Boolean result = connectionMap.get(key);
	if (result != null)
	    return result;
	result = getConnectionSlow(n1, n2);
	connectionMap.put(key, result);
	return result;
    }

    // n1 是否以某种方式接地？
    public boolean hasGroundConnection(int n1) {
	if (groundConnectionMap == null)
	    groundConnectionMap = new HashMap<Integer, Boolean>();
	Integer key = n1;
	Boolean result = groundConnectionMap.get(key);
	if (result != null)
	   return result;
	result = hasGroundConnectionSlow(n1);
	groundConnectionMap.put(key, result);
	return result;
    }

    public boolean hasGroundConnectionSlow(int n1) {
	Vector<Integer> connectedNodes = new Vector<Integer>();

	// 维护与 n1 相连的节点列表
	connectedNodes.add(n1);
	int i;
	for (i = 0; i < connectedNodes.size(); i++) {
	    // 列表中的下一个节点
	    int n = connectedNodes.get(i);	    
	    // 查找与 n 相连的所有元件
	    Vector<CircuitNodeLink> cnLinks = compNodeList.get(n).links;
	    for (int j = 0; j < cnLinks.size(); j++) {
		CircuitNodeLink link = cnLinks.get(j);
		CircuitElm lelm = link.elm;
		if (lelm.hasGroundConnection(link.num))
		    return true;
		// 遍历该元件拥有的所有其他节点
		for (int k = 0; k != lelm.getConnectionNodeCount(); k++)
		    // 它们相连吗？
		    if (k != link.num && lelm.getConnection(link.num, k)) {
			int kn = lelm.getConnectionNode(k);
			int m;
			// 查找本地节点编号（kn 是全局的）并将其添加到列表
			for (m = 0; m != nodes.length; m++)
			    if (nodes[m] == kn && !connectedNodes.contains(m))
				connectedNodes.add(m);
		    }
	    }
	}
	return false;
    }

    public void reset() {
	for (int i = 0; i < compElmList.size(); i++)
	    compElmList.get(i).reset();
    } 

    int getPostCount() {
	return numPosts;
    }

    int getInternalNodeCount() {
	return numNodes - numPosts;
    }

    Point getPost(int n) {
	return posts[n];
    }

    void setPost(int n, Point p) {
	posts[n] = p;
    }

    void setPost(int n, int x, int y) {
	posts[n].x = x;
	posts[n].y = y;
    }

    public double getPower() {
	double power;
	power = 0;
	for (int i = 0; i < compElmList.size(); i++)
	    power += compElmList.get(i).getPower();
	return power;
    }

    public void stamp() {
	for (int i = 0; i < compElmList.size(); i++) {
	    CircuitElm ce = compElmList.get(i);
	    ce.setParentList(compElmList);
	    ce.stamp();
	}
    }

    public void startIteration() {
	for (int i = 0; i < compElmList.size(); i++)
	    compElmList.get(i).startIteration();
    }
    
    public void doStep() {
	for (int i = 0; i < compElmList.size(); i++)
	    compElmList.get(i).doStep();
    }

    public void stepFinished() {
	for (int i = 0; i < compElmList.size(); i++)
	    compElmList.get(i).stepFinished();
    }

    // 调用此方法将节点 p（该元件的本地节点）设置为等于 n（全局节点）
    public void setNode(int p, int n) {
	// nodes[p] = n
	Vector<CircuitNodeLink> cnLinks;
	super.setNode(p, n);
	cnLinks = compNodeList.get(p).links;

        // 为使用该节点的所有元件调用 setNode()
	for (int i = 0; i < cnLinks.size(); i++) {
	    cnLinks.get(i).elm.setNode(cnLinks.get(i).num, n);
	}

    }

    public void setNodeVoltage(int n, double c) {
	// volts[n] = c;
	Vector<CircuitNodeLink> cnLinks;
	super.setNodeVoltage(n, c);
	cnLinks = compNodeList.get(n).links;
	for (int i = 0; i < cnLinks.size(); i++) {
	    cnLinks.get(i).elm.setNodeVoltage(cnLinks.get(i).num, c);
	}
	volts[n]=c;
    }

    public boolean canViewInScope() {
	return false;
    }

    public void delete() {
	for (int i = 0; i < compElmList.size(); i++)
	    compElmList.get(i).delete();
        super.delete();
    }

    public int getVoltageSourceCount() {
	return voltageSources.size();
    }

    // 找到第 n 个电压对应的元件
    // 并设置该元件中
    // 相应的电压源
    void setVoltageSource(int n, int v) {
	// voltSource(n) = v;
	VoltageSourceRecord vsr;
	vsr=voltageSources.get(n);
	vsr.elm.setVoltageSource(vsr.vsNumForElement, v);
	vsr.vsNode=v;
    }
    
    @Override
     public void   setCurrent(int vsn, double c) {
	for (int i=0;i<voltageSources.size(); i++)
	    if (voltageSources.get(i).vsNode == vsn) {
		voltageSources.get(i).elm.setCurrent(vsn, c);
	    }
	
    }

    double getCurrentIntoNode(int n) {
	double c=0;
	Vector<CircuitNodeLink> cnLinks;
	cnLinks = compNodeList.get(n).links;
	for (int i = 0; i < cnLinks.size(); i++) {
	    c+=cnLinks.get(i).elm.getCurrentIntoNode(cnLinks.get(i).num);
	}
	return c;
    }

}


class VoltageSourceRecord {
	int vsNumForElement;
	int vsNode;
	CircuitElm elm;
}
