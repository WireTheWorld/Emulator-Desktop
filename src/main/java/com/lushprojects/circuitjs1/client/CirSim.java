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

// GWT conversion (c) 2015 by Iain Sharp

// 有关此电路仿真背后理论的说明，请参阅 Electronic Circuit & System Simulation Methods by Pillage
// or https://github.com/sharpie7/circuitjs1/blob/master/INTERNALS.md

import java.util.Vector;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.lang.Math;
import java.util.Date;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CellPanel;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.Context2d.LineCap;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseEvent;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.ContextMenuEvent;
import com.google.gwt.event.dom.client.ContextMenuHandler;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Event.NativePreviewHandler;
import com.google.gwt.event.dom.client.MouseWheelEvent;
import com.google.gwt.event.dom.client.MouseWheelHandler;
import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.ScriptInjector;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.DoubleClickHandler;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.dom.client.CanvasElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.MetaElement;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.storage.client.Storage;
import com.google.gwt.user.client.ui.PopupPanel;
import static com.google.gwt.event.dom.client.KeyCodes.*;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.Widget;
import com.lushprojects.circuitjs1.client.util.Locale;
import com.lushprojects.circuitjs1.client.util.PerfMonitor;
import com.google.gwt.user.client.Window.ClosingEvent;
import com.google.gwt.user.client.Window.Navigator;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.i18n.client.DateTimeFormat;

public class CirSim implements MouseDownHandler, MouseMoveHandler, MouseUpHandler,
ClickHandler, DoubleClickHandler, ContextMenuHandler, NativePreviewHandler,
MouseOutHandler, MouseWheelHandler {

    Random random;
    Button resetButton;
    Button runStopButton;
    Button dumpMatrixButton;
    MenuItem aboutItem;
    MenuItem helpItem;
    MenuItem licenseItem;
    // MenuItem testItem;
    MenuItem aboutCircuitsItem;
    MenuItem aboutCircuitsPLItem;
    MenuItem closeItem;
    //CheckboxMenuItem fullscreenCheckItem;
    MenuItem importFromLocalFileItem, importFromTextItem, exportAsUrlItem, exportAsLocalFileItem, exportAsTextItem,
            printItem, recoverItem, saveFileItem;
    //MenuItem importFromDropboxItem;
    MenuItem undoItem, redoItem, cutItem, copyItem, pasteItem, selectAllItem, optionsItem, flipXItem, flipYItem, flipXYItem, modItem;
    MenuBar optionsMenuBar;
    CheckboxMenuItem dotsCheckItem;
    CheckboxMenuItem voltsCheckItem;
    CheckboxMenuItem powerCheckItem;
    CheckboxMenuItem smallGridCheckItem;
    CheckboxMenuItem crossHairCheckItem;
    CheckboxMenuItem showValuesCheckItem;
    CheckboxMenuItem conductanceCheckItem;
    CheckboxMenuItem euroResistorCheckItem;
    CheckboxMenuItem euroGatesCheckItem;
    CheckboxMenuItem printableCheckItem;
    CheckboxMenuItem conventionCheckItem;
    CheckboxMenuItem noEditCheckItem;
    CheckboxMenuItem mouseWheelEditCheckItem;
    CheckboxMenuItem toolbarCheckItem;
    CheckboxMenuItem mouseModeCheckItem;
    private Label powerLabel;
    private Label titleLabel;
    private Scrollbar speedBar;
    private Scrollbar currentBar;
    private Scrollbar powerBar;
    MenuBar elmMenuBar;
    MenuItem elmEditMenuItem;
    MenuItem elmCutMenuItem;
    MenuItem elmCopyMenuItem;
    MenuItem elmDeleteMenuItem;
    MenuItem elmScopeMenuItem;
    MenuItem elmFloatScopeMenuItem;
    MenuItem elmAddScopeMenuItem;
    MenuItem elmSplitMenuItem;
    MenuItem elmSliderMenuItem;
    MenuItem elmFlipXMenuItem, elmFlipYMenuItem, elmFlipXYMenuItem;
    MenuItem elmSwapMenuItem;
    MenuItem stackAllItem;
    MenuItem unstackAllItem;
    MenuItem combineAllItem;
    MenuItem separateAllItem;
    MenuBar mainMenuBar;
    boolean hideMenu = false;
    MenuBar selectScopeMenuBar;
    Vector<MenuItem> selectScopeMenuItems;
    MenuBar subcircuitMenuBar[];
    MenuItem scopeRemovePlotMenuItem;
    MenuItem scopeSelectYMenuItem;
    ScopePopupMenu scopePopupMenu;
    Element sidePanelCheckboxLabel;
   
    String lastCursorStyle;
    boolean mouseWasOverSplitter = false;

    // Class addingClass;
    PopupPanel contextPanel = null;
    int mouseMode = MODE_SELECT;
    int tempMouseMode = MODE_SELECT;
    String mouseModeStr = "Select";
    static final double pi = 3.14159265358979323846;
    static final int MODE_ADD_ELM = 0;
    static final int MODE_DRAG_ALL = 1;
    static final int MODE_DRAG_ROW = 2;
    static final int MODE_DRAG_COLUMN = 3;
    static final int MODE_DRAG_SELECTED = 4;
    static final int MODE_DRAG_POST = 5;
    static final int MODE_SELECT = 6;
    static final int MODE_DRAG_SPLITTER = 7;
    static final int infoWidth = 160;
    int dragGridX, dragGridY, dragScreenX, dragScreenY, initDragGridX, initDragGridY;
    long mouseDownTime;
    long zoomTime;
    int mouseCursorX = -1;
    int mouseCursorY = -1;
    Rectangle selectedArea;
    int gridSize, gridMask, gridRound;
    boolean dragging;
    boolean analyzeFlag, needsStamp, savedFlag;
    boolean dumpMatrix;
    boolean dcAnalysisFlag;
    // boolean useBufferedImage;
    boolean isMac;
    String ctrlMetaKey;
    double t;
    int pause = 10;
    int scopeSelected = -1;
    int scopeMenuSelected = -1;
    int menuScope = -1;
    int menuPlot = -1;
    int hintType = -1, hintItem1, hintItem2;
    String stopMessage;

    // 当前时间步长（迭代之间的时间间隔）
    double timeStep;

    // 最大时间步长（== timeStep，除非因收敛出现问题而减小
    //）
    double maxTimeStep;
    double minTimeStep;

    double wheelSensitivity = 1;

    // 自上次递增 timeStepCount 以来累积的时间
    double timeStepAccum;

    // 每次 t 前进 maxTimeStep 时递增
    int timeStepCount;

    double minFrameRate = 20;
    boolean adjustTimeStep;
    boolean developerMode;
    static final int HINT_LC = 1;
    static final int HINT_RC = 2;
    static final int HINT_3DB_C = 3;
    static final int HINT_TWINT = 4;
    static final int HINT_3DB_L = 5;
    Vector<CircuitElm> elmList;
    Vector<Adjustable> adjustables;
    // Vector setupList;
    CircuitElm dragElm, menuElm, stopElm;
    CircuitElm elmArr[];
    ScopeElm scopeElmArr[];
    private CircuitElm mouseElm = null;
    boolean didSwitch = false;
    int mousePost = -1;
    CircuitElm plotXElm, plotYElm;
    int draggingPost;
    SwitchElm heldSwitchElm;
    double circuitMatrix[][], circuitRightSide[], lastNodeVoltages[], nodeVoltages[], origRightSide[], origMatrix[][];
    RowInfo circuitRowInfo[];
    int circuitPermute[];
    boolean simRunning;
    boolean circuitNonLinear;
    int voltageSourceCount;
    int circuitMatrixSize, circuitMatrixFullSize;
    boolean circuitNeedsMap;
    // public boolean useFrame;
    int scopeCount;
    Scope scopes[];
    boolean showResistanceInVoltageSources;
    boolean hideInfoBox;
    int scopeColCount[];
    static EditDialog editDialog, customLogicEditDialog, diodeModelEditDialog;
    static HelpDialog helpDialog;
    static LicenseDialog licenseDialog;
	static ModDialog modDialog;
    static ScrollValuePopup scrollValuePopup;
    static Dialog dialogShowing;
    static AboutBox aboutBox;
    // Class dumpTypes[], shortcuts[];
    String shortcuts[];
    String clipboard;
    String recovery;
    Rectangle circuitArea;
    Vector<UndoItem> undoStack, redoStack;
    double transform[];
    static boolean unsavedChanges;
    static String filePath;
    static String fileName;
    static String lastFileName;
    HashMap<String, String> classToLabelMap;
    Toolbar toolbar;

    DockLayoutPanel layoutPanel;
    MenuBar menuBar;
    MenuBar fileMenuBar;
    VerticalPanel verticalPanel;
    VerticalPanel verticalPanel2;
    ScrollPanel slidersPanel;
    CellPanel buttonPanel;
    private boolean mouseDragging;
    double scopeHeightFraction = 0.2;

    Vector<CheckboxMenuItem> mainMenuItems = new Vector<CheckboxMenuItem>();
    Vector<String> mainMenuItemNames = new Vector<String>();

    LoadFile loadFileInput;
    Frame iFrame=null;

	static Button absResetBtn;
	static Button absRunStopBtn;

    Canvas cv;
    Context2d cvcontext;

    // 画布宽/高（像素），未应用设备像素比缩放
    int canvasWidth, canvasHeight;

    static int MENUBARHEIGHT = 30;
    static final int TOOLBARHEIGHT = 40;
    static int VERTICALPANELWIDTH = 166; // 默认值
    static final int POSTGRABSQ = 25;
    static final int MINPOSTGRABSIZE = 256;
    final Timer timer = new Timer() {
        public void run() {
            updateCircuit();
        }
    };
    final int FASTTIMER = 16;

    int getrand(int x) {
        int q = random.nextInt();
        if (q < 0)
            q = -q;
        return q % x;
    }

    static native float devicePixelRatio() /*-{
        return window.devicePixelRatio;
    }-*/;

	void redrawCanvasSize() {
		layoutPanel.setWidgetSize(menuBar, MENUBARHEIGHT);
		if (MENUBARHEIGHT<30) menuBar.addStyleName("modSmallMenuBar");
		else menuBar.removeStyleName("modSmallMenuBar");
		setCanvasSize();
		repaint();
	}

    void checkCanvasSize() {
        if (cv.getCoordinateSpaceWidth() != (int) (canvasWidth * devicePixelRatio()))
            setCanvasSize();
    }

    native boolean isMobile(Element element) /*-{
	if (!element)
	    return false;
	var style = getComputedStyle(element);
	return style.display != 'none';
    }-*/;
    
    public void setCanvasSize(){

    	Storage lstor = Storage.getLocalStorageIfSupported();

    	int width, height;
    	width=(int)RootLayoutPanel.get().getOffsetWidth();
    	height=(int)RootLayoutPanel.get().getOffsetHeight();
    	height=height-(hideMenu?0:MENUBARHEIGHT);

    	if (isSidePanelCheckboxChecked() && lstor.getItem("MOD_overlayingSidebar")=="false")
    	    width=width-VERTICALPANELWIDTH;
	if (toolbarCheckItem.getState())
	    height -= TOOLBARHEIGHT;

    	width = Math.max(width, 0);   // 避免设置为负宽度时发生异常
    	height = Math.max(height, 0);
    	
		if (cv != null) {
			cv.setWidth(width + "PX");
			cv.setHeight(height + "PX");
			canvasWidth = width;
			canvasHeight = height;
			float scale = devicePixelRatio();
			cv.setCoordinateSpaceWidth((int)(width*scale));
			cv.setCoordinateSpaceHeight((int)(height*scale));
		}

    	setCircuitArea();

	// 若画布在启动时被隐藏，则重新居中电路
    	if (transform[0] == 0)
    	    centreCircuit();
    }
    
    void setCircuitArea() {
    	int height = canvasHeight;
    	int width = canvasWidth;
    	int h = (int) ((double)height * scopeHeightFraction);
    	/*if (h < 128 && winSize.height > 300)
		  h = 128;*/
    	if (scopeCount == 0)
    	    h = 0;
    	circuitArea = new Rectangle(0, 0, width, height-h);
    }
    
    native String decompress(String dump) /*-{
        return $wnd.LZString.decompressFromEncodedURIComponent(dump);
    }-*/;

	public static void executeJS(String js){
		ScriptInjector.fromString(js)
			.setWindow(ScriptInjector.TOP_WINDOW)
			.inject();
	}

	// 此代码取自原始 ExportAsLocalFileDialog.java：

	public static void setLastFileName(String s) {
	    // 记住文件名，以便保存新文件时使用。
	    // 如果 s 为 null 或自动生成，则直接清除旧文件名。
	    if (s == null || s.startsWith("circuitjs-"))
		lastFileName = null;
	    else
		lastFileName = s;
	}

	public String getLastFileName() {
		Date date = new Date();
		String fname;
		if (lastFileName != null)
		    fname = lastFileName;
		else {
		    DateTimeFormat dtf = DateTimeFormat.getFormat("yyyyMMdd-HHmmss");
		    fname = "circuitjs-" + dtf.format(date) + ".txt";
		}
		return fname;
	}

	static native float getDefaultScale() /*-{
		$wnd.nw.Screen.Init();
		var dwidth = $wnd.nw.Screen.screens[0].bounds.width;
		var defaultScale;
		if (dwidth >= 1960)
			defaultScale = 1.6; // 2-0.4 and etc.
		else if (dwidth >= 1752 && dwidth < 1960)
			defaultScale = 1.1; // -0.4
		else if (dwidth >= 1600 && dwidth < 1752)
			defaultScale = 0.7; // -0.3
		else if (dwidth >= 1460 && dwidth < 1600)
			defaultScale = 0.3; // -0.2
		else if (dwidth >= 1200 && dwidth < 1460)
			defaultScale = -0.1; // -0.1
		else if (dwidth < 1200)
			defaultScale = -0.3;
		return defaultScale;
	}-*/;

	public static native void setSidebarAnimation(String duration,String speedcurve) /*-{
		var triggerLabel = $doc.querySelector(".triggerLabel");
		var sidebar = $doc.querySelector(".trigger+.triggerLabel+div");
		// 属性名 | 时长 | 缓动函数 | 延迟
		var split = " "+duration+"ms "+speedcurve;
		triggerLabel.style.transition = (duration=="none") ? duration : "right"+split;
		sidebar.style.transition = (duration=="none") ? duration : "width"+split;
	}-*/;

	static int getAbsBtnsTopPos() {
		Storage lstor = Storage.getLocalStorageIfSupported();
		int top = 50;
		if (lstor.getItem("MOD_TopMenuBar")=="small") top -= 11;
		if (lstor.getItem("toolbar")!="false") top += TOOLBARHEIGHT;
		return top;
	}

	void modSetDefault(){
		
		Storage lstor = Storage.getLocalStorageIfSupported();
		// 键：
		String MOD_UIScale=lstor.getItem("MOD_UIScale");
		String MOD_TopMenuBar=lstor.getItem("MOD_TopMenuBar");
		String MOD_absBtnTheme=lstor.getItem("MOD_absBtnTheme");
		String MOD_absBtnIcon=lstor.getItem("MOD_absBtnIcon");
		String MOD_hideAbsBtns=lstor.getItem("MOD_hideAbsBtns");
		String MOD_overlayingSidebar=lstor.getItem("MOD_overlayingSidebar");
		String MOD_showSidebaronStartup=lstor.getItem("MOD_showSidebaronStartup");
		String MOD_overlayingSBAnimation=lstor.getItem("MOD_overlayingSBAnimation");
		String MOD_SBAnim_duration=lstor.getItem("MOD_SBAnim_duration");
		String MOD_SBAnim_SpeedCurve=lstor.getItem("MOD_SBAnim_SpeedCurve");
		String MOD_setPauseWhenWinUnfocused=lstor.getItem("MOD_setPauseWhenWinUnfocused");

		if (MOD_UIScale==null){
			lstor.setItem("MOD_UIScale", Float.toString(getDefaultScale()));
			executeJS("nw.Window.get().zoomLevel = "+getDefaultScale());
		}
		else executeJS("nw.Window.get().zoomLevel = "+MOD_UIScale);
		if (MOD_TopMenuBar==null) lstor.setItem("MOD_TopMenuBar","standart");
		else if (MOD_TopMenuBar=="small"){
			MENUBARHEIGHT = 20;
			redrawCanvasSize();
		}
		if (MOD_absBtnTheme==null) lstor.setItem("MOD_absBtnTheme","default");
		else if (MOD_absBtnTheme=="classic"){
			absRunStopBtn.removeStyleName("modDefaultRunStopBtn");
			absRunStopBtn.addStyleName("gwt-Button");
			absRunStopBtn.addStyleName("modClassicButton");
			absResetBtn.removeStyleName("modDefaultResetBtn");
			absResetBtn.addStyleName("gwt-Button");
			absResetBtn.addStyleName("modClassicButton");
		}
		if (MOD_absBtnIcon==null) lstor.setItem("MOD_absBtnIcon","stop");
		else if (MOD_absBtnIcon=="pause"){
			absRunStopBtn.getElement().setInnerHTML("&#xE802;");
		}
		if (MOD_hideAbsBtns==null) lstor.setItem("MOD_hideAbsBtns","false");
		else if (MOD_hideAbsBtns=="true"){
			absRunStopBtn.setVisible(false);
			absResetBtn.setVisible(false);
		}
		if (MOD_overlayingSidebar==null) lstor.setItem("MOD_overlayingSidebar","false");
		if (MOD_showSidebaronStartup==null) lstor.setItem("MOD_showSidebaronStartup","false");
		else if (MOD_showSidebaronStartup=="true") executeJS("document.getElementById(\"trigger\").checked = true");
		if (MOD_SBAnim_duration==null || MOD_SBAnim_SpeedCurve==null){
			lstor.setItem("MOD_SBAnim_duration","500");
			lstor.setItem("MOD_SBAnim_SpeedCurve","ease");
			//if (lstor.getItem("MOD_overlayingSBAnimation")) setSidebarAnimation("500","ease");
		}
		if (MOD_overlayingSBAnimation==null) lstor.setItem("MOD_overlayingSBAnimation","false");
		if (MOD_overlayingSidebar=="true" && MOD_overlayingSBAnimation=="true"){
			setSidebarAnimation(lstor.getItem("MOD_SBAnim_duration"),lstor.getItem("MOD_SBAnim_SpeedCurve"));
		} else setSidebarAnimation("none","");
		if (MOD_setPauseWhenWinUnfocused==null) lstor.setItem("MOD_setPauseWhenWinUnfocused","true");
	}

//    Circuit applet;

    CirSim() {
//	super("Circuit Simulator v1.6d");
//	applet = a;
//	useFrame = false;
	theSim = this;
    }

    String startCircuit = null;
    String startLabel = null;
    String startCircuitText = null;
    String startCircuitLink = null;
//    String baseURL = "http://www.falstad.com/circuit/";
    
    public void init() {

	//设置 meta 标签，以允许 css 媒体查询生效
	MetaElement meta = Document.get().createMetaElement();
	meta.setName("viewport");
	meta.setContent("width=device-width");
	NodeList<com.google.gwt.dom.client.Element> node = Document.get().getElementsByTagName("head");
	node.getItem(0).appendChild(meta);

	
	boolean printable = false;
	boolean convention = true;
	boolean euroRes = false;
	boolean usRes = false;
	boolean running = true;
	boolean hideSidebar = false;
	boolean noEditing = false;
	boolean mouseWheelEdit = false;
	MenuBar m;

	CircuitElm.initClass(this);
	readRecovery();

	QueryParameters qp = new QueryParameters();
	String positiveColor = null;
	String negativeColor = null;
	String neutralColor = null;
	String selectColor = null;
	String currentColor = null;
	String mouseModeReq = null;
	boolean euroGates = false;

	try {
	    //baseURL = applet.getDocumentBase().getFile();
	    // 查找嵌入在 URL 中的电路
	    //		String doc = applet.getDocumentBase().toString();
	    String cct=qp.getValue("cct");
	    if (cct!=null)
		startCircuitText = cct.replace("%24", "$");
	    if (startCircuitText == null)
		startCircuitText = getElectronStartCircuitText();
	    String ctz=qp.getValue("ctz");
	    if (ctz!= null)
		startCircuitText = decompress(ctz);
	    startCircuit = qp.getValue("startCircuit");
	    startLabel   = qp.getValue("startLabel");
	    startCircuitLink = qp.getValue("startCircuitLink");
	    euroRes = qp.getBooleanValue("euroResistors", false);
	    euroGates = qp.getBooleanValue("IECGates", getOptionFromStorage("euroGates", weAreInGermany()));
	    usRes = qp.getBooleanValue("usResistors",  false);
	    running = qp.getBooleanValue("running", true);
	    hideSidebar = qp.getBooleanValue("hideSidebar", false);
	    hideMenu = qp.getBooleanValue("hideMenu", false);
	    printable = qp.getBooleanValue("whiteBackground", getOptionFromStorage("whiteBackground", false));
	    convention = qp.getBooleanValue("conventionalCurrent",
		    getOptionFromStorage("conventionalCurrent", true));
	    noEditing = !qp.getBooleanValue("editable", true);
	    mouseWheelEdit = qp.getBooleanValue("mouseWheelEdit", getOptionFromStorage("mouseWheelEdit", true));
	    positiveColor = qp.getValue("positiveColor");
	    negativeColor = qp.getValue("negativeColor");
	    neutralColor = qp.getValue("neutralColor");
	    selectColor = qp.getValue("selectColor");
	    currentColor = qp.getValue("currentColor");
	    mouseModeReq = qp.getValue("mouseMode");
	    hideInfoBox = qp.getBooleanValue("hideInfoBox", false);
	} catch (Exception e) { }

	boolean euroSetting = false;
	if (euroRes)
	    euroSetting = true;
	else if (usRes)
	    euroSetting = false;
	else
	    euroSetting = getOptionFromStorage("euroResistors", !weAreInUS(true));

	transform = new double[6];
	String os = Navigator.getPlatform();
	isMac = (os.toLowerCase().contains("mac"));
	ctrlMetaKey = (isMac) ? Locale.LS("Cmd-") : Locale.LS("Ctrl-");

	shortcuts = new String[127];

	RootLayoutPanel.get().add(absResetBtn = new Button("&#8634;",
		new ClickHandler() {
			public void onClick(ClickEvent event) {
				resetAction();
			}
		}));

	RootLayoutPanel.get().add(absRunStopBtn = new Button("&#xE800;",
		new ClickHandler() {
			public void onClick(ClickEvent event) {
				setSimRunning(!simIsRunning());
				executeJS("SetBtnsStyle()");
			}
		}));

	absResetBtn.setStyleName("btn-top-pos reset-btn reset-btn-pos modDefaultResetBtn");
	absRunStopBtn.setStyleName("btn-top-pos run-stop-btn run-stop-btn-pos modDefaultRunStopBtn");
	absResetBtn.getElement().setTitle("Reset");
	absRunStopBtn.getElement().setTitle("Run/Stop");

	layoutPanel = new DockLayoutPanel(Unit.PX);

	fileMenuBar = new MenuBar(true);
	fileMenuBar.addItem(menuItemWithShortcut("window", "New Window...", Locale.LS(ctrlMetaKey + "N"),
		new MyCommand("file", "newwindow")));
	fileMenuBar.addItem(iconMenuItem("doc-new", "New Blank Circuit", new MyCommand("file", "newblankcircuit")));
	importFromLocalFileItem = menuItemWithShortcut("folder", "Open File...", Locale.LS(ctrlMetaKey + "O"),
		new MyCommand("file","importfromlocalfile"));
	importFromLocalFileItem.setEnabled(LoadFile.isSupported());
	fileMenuBar.addItem(importFromLocalFileItem);
	importFromTextItem = iconMenuItem("doc-text", "Import From Text...", new MyCommand("file","importfromtext"));
	fileMenuBar.addItem(importFromTextItem);
	//importFromDropboxItem = iconMenuItem("dropbox", "Import From Dropbox...", new MyCommand("file", "importfromdropbox"));
	//fileMenuBar.addItem(importFromDropboxItem);
	//if (isElectron()) {
	    saveFileItem = fileMenuBar.addItem(menuItemWithShortcut("floppy", "Save", Locale.LS(ctrlMetaKey + "S"),
		    new MyCommand("file", "save")));
	    fileMenuBar.addItem(iconMenuItem("floppy", "Save As...", new MyCommand("file", "saveas")));
	/*} else {
	    exportAsLocalFileItem = menuItemWithShortcut("floppy", "Save As...", Locale.LS(ctrlMetaKey + "S"),
		    new MyCommand("file","exportaslocalfile"));
	    exportAsLocalFileItem.setEnabled(ExportAsLocalFileDialog.downloadIsSupported());
	    fileMenuBar.addItem(exportAsLocalFileItem);
	}*/
	exportAsUrlItem = iconMenuItem("export", "Export As Link...", new MyCommand("file","exportasurl"));
	fileMenuBar.addItem(exportAsUrlItem);
	exportAsTextItem = iconMenuItem("export", "Export As Text...", new MyCommand("file","exportastext"));
	fileMenuBar.addItem(exportAsTextItem);
	fileMenuBar.addItem(iconMenuItem("image", "Export As Image...", new MyCommand("file","exportasimage")));
	fileMenuBar.addItem(iconMenuItem("image", "Copy Circuit Image to Clipboard", new MyCommand("file","copypng")));
	fileMenuBar.addItem(iconMenuItem("image", "Export As SVG...", new MyCommand("file","exportassvg")));    	
	fileMenuBar.addItem(iconMenuItem("microchip", "Create Subcircuit...", new MyCommand("file","createsubcircuit")));
	fileMenuBar.addItem(iconMenuItem("magic", "Find DC Operating Point", new MyCommand("file", "dcanalysis")));
	recoverItem = iconMenuItem("back-in-time", "Recover Auto-Save", new MyCommand("file","recover"));
	recoverItem.setEnabled(recovery != null);
	fileMenuBar.addItem(recoverItem);
	printItem = menuItemWithShortcut("print", "Print...", Locale.LS(ctrlMetaKey + "P"), new MyCommand("file","print"));
	fileMenuBar.addItem(printItem);
	fileMenuBar.addSeparator();
	fileMenuBar.addItem(iconMenuItem("resize-full-alt", "Toggle Full Screen", new MyCommand("view", "fullscreen")));
	fileMenuBar.addSeparator();
	fileMenuBar.addItem(iconMenuItem("exit", "Exit",
		new Command() { public void execute(){
			executeJS("nw.Window.get().close(true)");
		}}));
	/* 
	aboutItem = iconMenuItem("info-circled", "About...", (Command)null);
	fileMenuBar.addItem(aboutItem);
	aboutItem.setScheduledCommand(new MyCommand("file","about"));
	*/
	int width=(int)RootLayoutPanel.get().getOffsetWidth();
	VERTICALPANELWIDTH = 166; /* = width/5;
	if (VERTICALPANELWIDTH > 166)
	    VERTICALPANELWIDTH = 166;
	if (VERTICALPANELWIDTH < 128)
	    VERTICALPANELWIDTH = 128;*/

	menuBar = new MenuBar();
	menuBar.addItem(Locale.LS("File"), fileMenuBar);
	verticalPanel=new VerticalPanel();
	slidersPanel = new ScrollPanel();
	verticalPanel2=new VerticalPanel();

	verticalPanel.getElement().addClassName("verticalPanel");
	verticalPanel.getElement().setId("painel");
	Element sidePanelCheckbox = DOM.createInputCheck();
	sidePanelCheckboxLabel = DOM.createLabel();
	sidePanelCheckboxLabel.addClassName("triggerLabel");
	sidePanelCheckbox.setId("trigger");
	sidePanelCheckboxLabel.setAttribute("for", "trigger" );
	sidePanelCheckbox.addClassName("trigger");
	Event.sinkEvents(sidePanelCheckbox, Event.ONCLICK);
	Event.setEventListener(sidePanelCheckbox, new EventListener() {
		public void onBrowserEvent(Event event) {
			if(Event.ONCLICK == event.getTypeInt()) {
				Storage lstor = Storage.getLocalStorageIfSupported();
				setupScopes();
				executeJS("SetBtnsStyle();");
				setCanvasSize();
				if (lstor.getItem("MOD_overlayingSidebar")=="false") {
					if (isSidePanelCheckboxChecked()) transform[4] -= VERTICALPANELWIDTH/2;
					else transform[4] += VERTICALPANELWIDTH/2;
				}
			}
		}
	});
	Element topPanelCheckbox = DOM.createInputCheck(); 
	Element topPanelCheckboxLabel = DOM.createLabel();
	topPanelCheckbox.setId("toptrigger");
	topPanelCheckbox.addClassName("toptrigger");
	topPanelCheckboxLabel.addClassName("toptriggerlabel");
	topPanelCheckboxLabel.setAttribute("for", "toptrigger");

	// 如果有空间，则让按钮并排显示
	buttonPanel=(VERTICALPANELWIDTH == 166) ? new HorizontalPanel() : new VerticalPanel();

	m = new MenuBar(true);
	m.addItem(undoItem = menuItemWithShortcut("ccw", "Undo", Locale.LS(ctrlMetaKey + "Z"), new MyCommand("edit","undo")));
	m.addItem(redoItem = menuItemWithShortcut("cw", "Redo", Locale.LS(ctrlMetaKey + "Y"), new MyCommand("edit","redo")));
	m.addSeparator();
	m.addItem(cutItem = menuItemWithShortcut("scissors", "Cut", Locale.LS(ctrlMetaKey + "X"), new MyCommand("edit","cut")));
	m.addItem(copyItem = menuItemWithShortcut("copy", "Copy", Locale.LS(ctrlMetaKey + "C"), new MyCommand("edit","copy")));
	m.addItem(pasteItem = menuItemWithShortcut("paste", "Paste", Locale.LS(ctrlMetaKey + "V"), new MyCommand("edit","paste")));
	pasteItem.setEnabled(false);

	m.addItem(menuItemWithShortcut("clone", "Duplicate", Locale.LS(ctrlMetaKey + "D"), new MyCommand("edit","duplicate")));

	m.addSeparator();
	m.addItem(selectAllItem = menuItemWithShortcut("select-all", "Select All", Locale.LS(ctrlMetaKey + "A"), new MyCommand("edit","selectAll")));
	m.addSeparator();
	m.addItem(menuItemWithShortcut("search", "Find Component...", "/", new MyCommand("edit", "search")));
	m.addItem(iconMenuItem("target", weAreInUS(false) ? "Center Circuit" : "Centre Circuit", new MyCommand("edit", "centrecircuit")));
	m.addItem(menuItemWithShortcut("zoom-11", "Zoom 100%", "0", new MyCommand("zoom", "zoom100")));
	m.addItem(menuItemWithShortcut("zoom-in", "Zoom In", "+", new MyCommand("zoom", "zoomin")));
	m.addItem(menuItemWithShortcut("zoom-out", "Zoom Out", "-", new MyCommand("zoom", "zoomout")));
	m.addItem(flipXItem = iconMenuItem("flip-x", "Flip X", new MyCommand("edit", "flipx")));
	m.addItem(flipYItem = iconMenuItem("flip-y", "Flip Y", new MyCommand("edit", "flipy")));
	m.addItem(flipXYItem = iconMenuItem("flip-x-y", "Flip XY", new MyCommand("edit", "flipxy")));
	menuBar.addItem(Locale.LS("Edit"),m);

	MenuBar drawMenuBar = new MenuBar(true);
	drawMenuBar.setAutoOpen(true);

	menuBar.addItem(Locale.LS("Draw"), drawMenuBar);

	m = new MenuBar(true);
	m.addItem(stackAllItem = iconMenuItem("lines", "Stack All", new MyCommand("scopes", "stackAll")));
	m.addItem(unstackAllItem = iconMenuItem("columns", "Unstack All", new MyCommand("scopes", "unstackAll")));
	m.addItem(combineAllItem = iconMenuItem("object-group", "Combine All", new MyCommand("scopes", "combineAll")));
	m.addItem(separateAllItem = iconMenuItem("object-ungroup", "Separate All", new MyCommand("scopes", "separateAll")));
	menuBar.addItem(Locale.LS("Scopes"), m);

	optionsMenuBar = m = new MenuBar(true );
	menuBar.addItem(Locale.LS("Options"), optionsMenuBar);
	m.addItem(dotsCheckItem = new CheckboxMenuItem(Locale.LS("Show Current")));
	dotsCheckItem.setState(true);
	m.addItem(voltsCheckItem = new CheckboxMenuItem(Locale.LS("Show Voltage"),
		new Command() { public void execute(){
		    if (voltsCheckItem.getState())
			powerCheckItem.setState(false);
		    setPowerBarEnable();
		}
	}));
	voltsCheckItem.setState(true);
	m.addItem(powerCheckItem = new CheckboxMenuItem(Locale.LS("Show Power"),
		new Command() { public void execute(){
		    if (powerCheckItem.getState())
			voltsCheckItem.setState(false);
		    setPowerBarEnable();
		}
	}));
	m.addItem(showValuesCheckItem = new CheckboxMenuItem(Locale.LS("Show Values")));
	showValuesCheckItem.setState(true);
	//m.add(conductanceCheckItem = getCheckItem(LS("Show Conductance")));
	m.addItem(smallGridCheckItem = new CheckboxMenuItem(Locale.LS("Small Grid"),
		new Command() { public void execute(){
		    setGrid();
		}
	}));
	m.addItem(toolbarCheckItem = new CheckboxMenuItem(Locale.LS("Toolbar"),
		new Command() { public void execute(){
			setOptionInStorage("toolbar", toolbarCheckItem.getState());
		    setToolbar();
		}
	}));
	toolbarCheckItem.setState(getOptionFromStorage("toolbar", true));
	m.addItem(mouseModeCheckItem = new CheckboxMenuItem(Locale.LS("Show Mode"),
		new Command() { public void execute(){
			setOptionInStorage("showMouseMode", mouseModeCheckItem.getState());
		}
	}));
	mouseModeCheckItem.setState(getOptionFromStorage("showMouseMode", true));
	m.addItem(crossHairCheckItem = new CheckboxMenuItem(Locale.LS("Show Cursor Cross Hairs"),
		new Command() { public void execute(){
		    setOptionInStorage("crossHair", crossHairCheckItem.getState());
		}
	}));
	crossHairCheckItem.setState(getOptionFromStorage("crossHair", false));
	m.addItem(euroResistorCheckItem = new CheckboxMenuItem(Locale.LS("European Resistors"),
		new Command() { public void execute(){
		    setOptionInStorage("euroResistors", euroResistorCheckItem.getState());
		    toolbar.setEuroResistors(euroResistorCheckItem.getState());
		}
	}));
	euroResistorCheckItem.setState(euroSetting);
	m.addItem(euroGatesCheckItem = new CheckboxMenuItem(Locale.LS("IEC Gates"),
		new Command() { public void execute(){
		    setOptionInStorage("euroGates", euroGatesCheckItem.getState());
		    int i;
		    for (i = 0; i != elmList.size(); i++)
			getElm(i).setPoints();
		}
	}));
	euroGatesCheckItem.setState(euroGates);
	m.addItem(printableCheckItem = new CheckboxMenuItem(Locale.LS("White Background"),
		new Command() { public void execute(){
		    int i;
		    for (i=0;i<scopeCount;i++)
			scopes[i].setRect(scopes[i].rect);
		    setOptionInStorage("whiteBackground", printableCheckItem.getState());
		}
	}));
	printableCheckItem.setState(printable);

	m.addItem(conventionCheckItem = new CheckboxMenuItem(Locale.LS("Conventional Current Motion"),
		new Command() { public void execute(){
		    setOptionInStorage("conventionalCurrent", conventionCheckItem.getState());
		    String cc = CircuitElm.currentColor.getHexValue();
		    // 如果当前颜色尚未从默认值改变，则更改之
		    if (cc.equals("#ffff00") || cc.equals("#00ffff"))
			CircuitElm.currentColor = conventionCheckItem.getState() ? Color.yellow : Color.cyan;
		}
	}));
	conventionCheckItem.setState(convention);
	m.addItem(noEditCheckItem = new CheckboxMenuItem(Locale.LS("Disable Editing")));
	noEditCheckItem.setState(noEditing);

	m.addItem(mouseWheelEditCheckItem = new CheckboxMenuItem(Locale.LS("Edit Values With Mouse Wheel"),
		new Command() { public void execute(){
		    setOptionInStorage("mouseWheelEdit", mouseWheelEditCheckItem.getState());
		}
	}));
	mouseWheelEditCheckItem.setState(mouseWheelEdit);

	m.addItem(new CheckboxAlignedMenuItem(Locale.LS("Shortcuts..."), new MyCommand("options", "shortcuts")));
	m.addItem(new CheckboxAlignedMenuItem(Locale.LS("Subcircuits..."), new MyCommand("options", "subcircuits")));
	m.addItem(optionsItem = new CheckboxAlignedMenuItem(Locale.LS("Other Options..."), new MyCommand("options","other")));
	m.addItem(modItem = new CheckboxAlignedMenuItem("Modification Setup...", new MyCommand("options","modsetup")));
	modItem.addStyleName("modItem");
	if (isElectron())
	    m.addItem(new CheckboxAlignedMenuItem(Locale.LS("Toggle Dev Tools"), new MyCommand("options","devtools")));

	mainMenuBar = new MenuBar(true);
	mainMenuBar.setAutoOpen(true);
	composeMainMenu(mainMenuBar, 0);
	composeMainMenu(drawMenuBar, 1);
	loadShortcuts();

	DOM.appendChild(layoutPanel.getElement(), topPanelCheckbox);
	DOM.appendChild(layoutPanel.getElement(), topPanelCheckboxLabel);	

	toolbar = new Toolbar();
	toolbar.setEuroResistors(euroSetting);
	if (!hideMenu)
	    layoutPanel.addNorth(menuBar, MENUBARHEIGHT);

	// 在 menuBar 之后立即添加工具栏
	layoutPanel.addNorth(toolbar, TOOLBARHEIGHT);

	if (hideSidebar)
	    VERTICALPANELWIDTH = 0;
	else {
		DOM.appendChild(layoutPanel.getElement(), sidePanelCheckbox);
		DOM.appendChild(layoutPanel.getElement(), sidePanelCheckboxLabel);
	    layoutPanel.addEast(verticalPanel, VERTICALPANELWIDTH);
	}
	menuBar.getElement().insertFirst(menuBar.getElement().getChild(1));
	menuBar.getElement().getFirstChildElement().setAttribute("onclick", "document.getElementsByClassName('toptrigger')[0].checked = false");
	RootLayoutPanel.get().add(layoutPanel);

	cv =Canvas.createIfSupported();
	if (cv==null) {
	    RootPanel.get().add(new Label("Not working. You need a browser that supports the CANVAS element."));
	    return;
	}

	modSetDefault();

	Window.addResizeHandler(new ResizeHandler() {
	    public void onResize(ResizeEvent event) {
		repaint();
		setSlidersPanelHeight();
	    }
	});

	cvcontext=cv.getContext2d();
	setToolbar(); // 调用 setCanvasSize()
	layoutPanel.add(cv);
	verticalPanel.add(buttonPanel);
	buttonPanel.addStyleName("sidePanelElm");
	buttonPanel.add(resetButton = new Button(Locale.LS("Reset")));
	resetButton.addClickHandler(new ClickHandler() {
	    public void onClick(ClickEvent event) {
		resetAction();
	    }
	});
	resetButton.setStylePrimaryName("topButton");
	buttonPanel.add(runStopButton = new Button(Locale.LSHTML("<Strong>RUN</Strong>&nbsp;/&nbsp;Stop")));
	runStopButton.addClickHandler(new ClickHandler() {
	    public void onClick(ClickEvent event) {
		setSimRunning(!simIsRunning());
	    }
	});

	
/*
	dumpMatrixButton = new Button("Dump Matrix");
	dumpMatrixButton.addClickHandler(new ClickHandler() {
	    public void onClick(ClickEvent event) { dumpMatrix = true; }});
	verticalPanel.add(dumpMatrixButton);// IES for debugging
*/
	

	if (LoadFile.isSupported()){
	    verticalPanel.add(loadFileInput = new LoadFile(this));
		loadFileInput.addStyleName("sidePanelElm");
		setSlidersPanelHeight();
	}

	Label l;
	verticalPanel.add(l = new Label(Locale.LS("Simulation Speed")));
	l.addStyleName("topSpace");
	l.addStyleName("sidePanelElm");

	// 原最大值是 140
	verticalPanel.add( speedBar = new Scrollbar(Scrollbar.HORIZONTAL, 3, 1, 0, 260));
	speedBar.addStyleName("sidePanelElm");

	verticalPanel.add( l = new Label(Locale.LS("Current Speed")));
	l.addStyleName("topSpace");
	l.addStyleName("sidePanelElm");

	currentBar = new Scrollbar(Scrollbar.HORIZONTAL, 50, 1, 1, 100);
	verticalPanel.add(currentBar);
	currentBar.addStyleName("sidePanelElm");

	verticalPanel.add(powerLabel = new Label (Locale.LS("Power Brightness")));
	powerLabel.addStyleName("topSpace");
	powerLabel.addStyleName("sidePanelElm");

	verticalPanel.add(powerBar = new Scrollbar(Scrollbar.HORIZONTAL,
		50, 1, 1, 100));
	powerBar.addStyleName("sidePanelElm");
	setPowerBarEnable();

	//	verticalPanel.add(new Label(""));
	//        Font f = new Font("SansSerif", 0, 10);
	l = new Label(Locale.LS("Current Circuit:"));
	l.addStyleName("topSpace");
	l.addStyleName("sidePanelElm");
	//        l.setFont(f);
	titleLabel = new Label("Label");
	titleLabel.addStyleName("sidePanelElm");
	//        titleLabel.setFont(f);
	verticalPanel.add(l);
	verticalPanel.add(titleLabel);

	Label sab;
	sab = new Label(Locale.LS("Sliders and buttons")+":");
	sab.addStyleName("sabLabel");
	verticalPanel.add(sab);

	verticalPanel.add(slidersPanel);
	slidersPanel.add(verticalPanel2);
	verticalPanel2.addStyleName("sidePanelvp2");
	verticalPanel2.setWidth("150px");

	//slidersPanel.setAlwaysShowScrollBars(true);
	slidersPanel.getElement().getStyle().setOverflowX(Overflow.HIDDEN);
	slidersPanel.getElement().getStyle().setOverflowY(Overflow.SCROLL);

	setGrid();
	elmList = new Vector<CircuitElm>();
	adjustables = new Vector<Adjustable>();
	//	setupList = new Vector();
	undoStack = new Vector<UndoItem>();
	redoStack = new Vector<UndoItem>();


	scopes = new Scope[20];
	scopeColCount = new int[20];
	scopeCount = 0;

	random = new Random();
	//	cv.setBackground(Color.black);
	//	cv.setForeground(Color.lightGray);

	elmMenuBar = new MenuBar(true);
	elmMenuBar.setAutoOpen(true);
	selectScopeMenuBar = new MenuBar(true) {
	    @Override
	    
	    // 当鼠标悬停在示波器菜单项上时，选中关联的示波器
	    public void onBrowserEvent(Event event) {
		int currentItem = -1;
		int i;
		for (i = 0; i != selectScopeMenuItems.size(); i++) {
		    MenuItem item = selectScopeMenuItems.get(i);
		    if (DOM.isOrHasChild(item.getElement(), DOM.eventGetTarget(event))) {
			//在此处找到菜单项
			currentItem = i;
		    }
		}
		switch (DOM.eventGetType(event)) {
		case Event.ONMOUSEOVER:
		    scopeMenuSelected = currentItem; 
		    break;              
		case Event.ONMOUSEOUT:
		    scopeMenuSelected = -1;
		    break;              
		}
		super.onBrowserEvent(event);
	    }
	};
	
	elmMenuBar.addItem(elmEditMenuItem = new MenuItem(Locale.LS("Edit..."),new MyCommand("elm","edit")));
	elmMenuBar.addItem(elmScopeMenuItem = new MenuItem(Locale.LS("View in New Scope"), new MyCommand("elm","viewInScope")));
	elmMenuBar.addItem(elmFloatScopeMenuItem  = new MenuItem(Locale.LS("View in New Undocked Scope"), new MyCommand("elm","viewInFloatScope")));
	elmMenuBar.addItem(elmAddScopeMenuItem = new MenuItem(Locale.LS("Add to Existing Scope"), new MyCommand("elm", "addToScope0")));
	elmMenuBar.addItem(elmCutMenuItem = new MenuItem(Locale.LS("Cut"),new MyCommand("elm","cut")));
	elmMenuBar.addItem(elmCopyMenuItem = new MenuItem(Locale.LS("Copy"),new MyCommand("elm","copy")));
	elmMenuBar.addItem(elmDeleteMenuItem = new MenuItem(Locale.LS("Delete"),new MyCommand("elm","delete")));
	elmMenuBar.addItem(                    new MenuItem(Locale.LS("Duplicate"),new MyCommand("elm","duplicate")));
	elmMenuBar.addItem(elmSwapMenuItem = new MenuItem(Locale.LS("Swap Terminals"),new MyCommand("elm","flip")));
	elmMenuBar.addItem(elmFlipXMenuItem =  new MenuItem(Locale.LS("Flip X"),new MyCommand("elm","flipx")));
	elmMenuBar.addItem(elmFlipYMenuItem =  new MenuItem(Locale.LS("Flip Y"),new MyCommand("elm","flipy")));
	elmMenuBar.addItem(elmFlipXYMenuItem =  new MenuItem(Locale.LS("Flip XY"),new MyCommand("elm","flipxy")));
	elmMenuBar.addItem(elmSplitMenuItem = menuItemWithShortcut("", "Split Wire", Locale.LS(ctrlMetaKey + "click"), new MyCommand("elm","split")));
	elmMenuBar.addItem(elmSliderMenuItem = new MenuItem(Locale.LS("Sliders..."),new MyCommand("elm","sliders")));

	scopePopupMenu = new ScopePopupMenu();

	setColors(positiveColor, negativeColor, neutralColor, selectColor, currentColor);
	setWheelSensitivity();

	if (startCircuitText != null) {
	    getSetupList(false);
	    readCircuit(startCircuitText);
	    unsavedChanges = false;
	    changeWindowTitle(unsavedChanges);
	} else {
	    if (stopMessage == null && startCircuitLink!=null) {
		readCircuit("");
		getSetupList(false);
		//ImportFromDropboxDialog.setSim(this);
		//ImportFromDropboxDialog.doImportDropboxLink(startCircuitLink, false);
	    } else {
		readCircuit("");
		if (stopMessage == null && startCircuit != null) {
		    getSetupList(false);
		    readSetupFile(startCircuit, startLabel);
		}
		else
		    getSetupList(true);
	    }
	}

	if (mouseModeReq != null)
	    menuPerformed("main", mouseModeReq);

	enableUndoRedo();
	enablePaste();
	enableDisableMenuItems();
	setSlidersPanelHeight();
	cv.addMouseDownHandler(this);
	cv.addMouseMoveHandler(this);
	cv.addMouseOutHandler(this);
	cv.addMouseUpHandler(this);
	cv.addClickHandler(this);
	cv.addDoubleClickHandler(this);
	doTouchHandlers(this, cv.getCanvasElement());
	cv.addDomHandler(this, ContextMenuEvent.getType());	
	menuBar.addDomHandler(new ClickHandler() {
	    public void onClick(ClickEvent event) {
		doMainMenuChecks();
	    }
	}, ClickEvent.getType());	
	Event.addNativePreviewHandler(this);
	cv.addMouseWheelHandler(this);

	Window.addWindowClosingHandler(new Window.ClosingHandler() {
	    public void onWindowClosing(ClosingEvent event) {
		// electron 中有一个 bug：如果给出此警告，应用将无法关闭
		if (unsavedChanges && !isElectron())
		    event.setMessage(Locale.LS("Are you sure?  There are unsaved changes."));
	    }
	});
	setupJSInterface();
	
	setSimRunning(running);
    }

    void setColors(String positiveColor, String negativeColor, String neutralColor, String selectColor, String currentColor) {
        Storage stor = Storage.getLocalStorageIfSupported();
        if (stor != null) {
            if (positiveColor == null)
        	positiveColor = stor.getItem("positiveColor");
            if (negativeColor == null)
        	negativeColor = stor.getItem("negativeColor");
            if (neutralColor == null)
        	neutralColor = stor.getItem("neutralColor");
            if (selectColor == null)
        	selectColor = stor.getItem("selectColor");
            if (currentColor == null)
        	currentColor = stor.getItem("currentColor");
        }
        
	if (positiveColor != null)
	    CircuitElm.positiveColor = new Color(URL.decodeQueryString(positiveColor));
	else if (getOptionFromStorage("alternativeColor", false))
	    CircuitElm.positiveColor = Color.blue;
	
	if (negativeColor != null)
	    CircuitElm.negativeColor = new Color(URL.decodeQueryString(negativeColor));
	if (neutralColor != null)
	    CircuitElm.neutralColor = new Color(URL.decodeQueryString(neutralColor));

	if (selectColor != null)
	    CircuitElm.selectColor = new Color(URL.decodeQueryString(selectColor));
	else
	    CircuitElm.selectColor = Color.cyan;
	
	if (currentColor != null)
	    CircuitElm.currentColor = new Color(URL.decodeQueryString(currentColor));
	else
	    CircuitElm.currentColor = conventionCheckItem.getState() ? Color.yellow : Color.cyan;
	    
	CircuitElm.setColorScale();
    }
    
    void setWheelSensitivity() {
	wheelSensitivity = 1;
	try {
	    Storage stor = Storage.getLocalStorageIfSupported();
	    wheelSensitivity = Double.parseDouble(stor.getItem("wheelSensitivity"));
	} catch (Exception e) {}
    }

    MenuItem menuItemWithShortcut(String icon, String text, String shortcut, MyCommand cmd) {
	final String edithtml="<div style=\"white-space:nowrap\"><div style=\"display:inline-block;width:100%;\"><i class=\"cirjsicon-";
	String nbsp = "&nbsp;";
	if (icon=="") nbsp="";
	String sn=edithtml + icon + "\"></i>" + nbsp + Locale.LS(text) + "</div>" + shortcut + "</div>";
	return new MenuItem(SafeHtmlUtils.fromTrustedString(sn), cmd);
    }
    
    MenuItem iconMenuItem(String icon, String text, Command cmd) {
        String icoStr = "<i class=\"cirjsicon-" + icon + "\"></i>&nbsp;" + Locale.LS(text); //<i class="cirjsicon-"></i>&nbsp;
        return new MenuItem(SafeHtmlUtils.fromTrustedString(icoStr), cmd);
    }
    
    boolean getOptionFromStorage(String key, boolean val) {
        Storage stor = Storage.getLocalStorageIfSupported();
        if (stor == null)
            return val;
        String s = stor.getItem(key);
        if (s == null)
            return val;
        return s == "true";
    }

    void setOptionInStorage(String key, boolean val) {
        Storage stor = Storage.getLocalStorageIfSupported();
        if (stor == null)
            return;
        stor.setItem(key,  val ? "true" : "false");
    }
    
    // 将快捷键保存到本地存储
    void saveShortcuts() {
        Storage stor = Storage.getLocalStorageIfSupported();
        if (stor == null)
            return;
        String str = "1";
        int i;
        // 格式：version;code1=ClassName;code2=ClassName;等等
        for (i = 0; i != shortcuts.length; i++) {
            String sh = shortcuts[i];
            if (sh == null)
        		continue;
            str += ";" + i + "=" + sh;
        }
        stor.setItem("shortcuts", str);
    }
    
    // 从本地存储加载快捷键
    void loadShortcuts() {
        Storage stor = Storage.getLocalStorageIfSupported();
        if (stor == null)
            return;
        String str = stor.getItem("shortcuts");
        if (str == null)
            return;
        String keys[] = str.split(";");
        
        // 清除现有快捷键
        int i;
        for (i = 0; i != shortcuts.length; i++)
            shortcuts[i] = null;
        
        // 从菜单中清除快捷键
        for (i = 0; i != mainMenuItems.size(); i++) {
            CheckboxMenuItem item = mainMenuItems.get(i);
            // 遇到拖动菜单项时停止
            if (item.getShortcut().length() > 1)
        		break;
            item.setShortcut("");
        }
        
        // 遍历各键（跳过开头的版本号）
        for (i = 1; i < keys.length; i++) {
            String arr[] = keys[i].split("=");
            if (arr.length != 2)
        	continue;
            int c = Integer.parseInt(arr[0]);
            String className = arr[1];
            shortcuts[c] = className;
            
            // 找到菜单项并修正它
            int j;
            for (j = 0; j != mainMenuItems.size(); j++) {
        		if (mainMenuItemNames.get(j) == className) {
        		    CheckboxMenuItem item = mainMenuItems.get(j);
        		    item.setShortcut(Character.toString((char)c));
        		    break;
        		}
            }
        }
    }
    
    // 安装触摸事件处理器
    // 不想用 java 重写这些代码。再说，java 也不允许我们创建鼠标
    // 事件并分发它们。
    native static void doTouchHandlers(CirSim sim, CanvasElement cv) /*-{
	// 为移动设备等设置触摸事件
	var lastTap;
	var tmout;
	var lastScale;
	
	cv.addEventListener("touchstart", function (e) {
        	mousePos = getTouchPos(cv, e);
  		var touch = e.touches[0];
  		
  		var etype = "mousedown";
  		lastScale = 1;
  		clearTimeout(tmout);
  		e.preventDefault();
  		
  		if (e.timeStamp-lastTap < 300) {
     		    etype = "dblclick";
  		} else {
  		    tmout = setTimeout(function() {
  		        sim.@com.lushprojects.circuitjs1.client.CirSim::longPress()();
  		    }, 500);
  		}
  		lastTap = e.timeStamp;
  		
  		var touch1 = e.touches[0];
  		var touch2 = e.touches[e.touches.length-1];
  		lastScale = Math.hypot(touch1.clientX-touch2.clientX, touch1.clientY-touch2.clientY);
  		var mouseEvent = new MouseEvent(etype, {
    			clientX: .5*(touch1.clientX+touch2.clientX),
    			clientY: .5*(touch1.clientY+touch2.clientY)
  		});
  		cv.dispatchEvent(mouseEvent);
  		if (e.touches.length > 1)
  		    sim.@com.lushprojects.circuitjs1.client.CirSim::twoFingerTouch(II)(mouseEvent.clientX, mouseEvent.clientY - cv.getBoundingClientRect().y);
	}, false);
	cv.addEventListener("touchend", function (e) {
  		var mouseEvent = new MouseEvent("mouseup", {});
  		e.preventDefault();
  		clearTimeout(tmout);
  		cv.dispatchEvent(mouseEvent);
	}, false);
	cv.addEventListener("touchmove", function (e) {
  		e.preventDefault();
  		clearTimeout(tmout);
  		var touch1 = e.touches[0];
  		var touch2 = e.touches[e.touches.length-1];
	        if (e.touches.length > 1) {
  		    var newScale = Math.hypot(touch1.clientX-touch2.clientX, touch1.clientY-touch2.clientY);
	            sim.@com.lushprojects.circuitjs1.client.CirSim::zoomCircuit(D)(40*(Math.log(newScale)-Math.log(lastScale)));
	            lastScale = newScale;
	        }
  		var mouseEvent = new MouseEvent("mousemove", {
    			clientX: .5*(touch1.clientX+touch2.clientX),
    			clientY: .5*(touch1.clientY+touch2.clientY)
  		});
  		cv.dispatchEvent(mouseEvent);
	}, false);

	// 获取触摸点相对于画布的位置
	function getTouchPos(canvasDom, touchEvent) {
  		var rect = canvasDom.getBoundingClientRect();
  		return {
    			x: touchEvent.touches[0].clientX - rect.left,
    			y: touchEvent.touches[0].clientY - rect.top
  		};
	}
	
    }-*/;
    
    boolean shown = false;
    
    // 此方法被调用两次：一次用于 Draw 菜单，一次用于鼠标右键弹出菜单
    public void composeMainMenu(MenuBar mainMenuBar, int num) {
    	mainMenuBar.addItem(getClassCheckItem(Locale.LS("Add Wire"), "WireElm"));
    	mainMenuBar.addItem(getClassCheckItem(Locale.LS("Add Resistor"), "ResistorElm"));

    	MenuBar passMenuBar = new MenuBar(true);
    	passMenuBar.addItem(getClassCheckItem(Locale.LS("Add Capacitor"), "CapacitorElm"));
    	passMenuBar.addItem(getClassCheckItem(Locale.LS("Add Capacitor (polarized)"), "PolarCapacitorElm"));
    	passMenuBar.addItem(getClassCheckItem(Locale.LS("Add Inductor"), "InductorElm"));
    	passMenuBar.addItem(getClassCheckItem(Locale.LS("Add Switch"), "SwitchElm"));
    	passMenuBar.addItem(getClassCheckItem(Locale.LS("Add Push Switch"), "PushSwitchElm"));
    	passMenuBar.addItem(getClassCheckItem(Locale.LS("Add SPDT Switch"), "Switch2Elm"));
    	passMenuBar.addItem(getClassCheckItem(Locale.LS("Add DPDT Switch"), "DPDTSwitchElm"));
    	passMenuBar.addItem(getClassCheckItem(Locale.LS("Add Make-Before-Break Switch"), "MBBSwitchElm"));
    	passMenuBar.addItem(getClassCheckItem(Locale.LS("Add Potentiometer"), "PotElm"));
    	passMenuBar.addItem(getClassCheckItem(Locale.LS("Add Transformer"), "TransformerElm"));
    	passMenuBar.addItem(getClassCheckItem(Locale.LS("Add Tapped Transformer"), "TappedTransformerElm"));
    	passMenuBar.addItem(getClassCheckItem(Locale.LS("Add Custom Transformer"), "CustomTransformerElm"));
    	passMenuBar.addItem(getClassCheckItem(Locale.LS("Add Transmission Line"), "TransLineElm"));
    	passMenuBar.addItem(getClassCheckItem(Locale.LS("Add Relay"), "RelayElm"));
    	passMenuBar.addItem(getClassCheckItem(Locale.LS("Add Relay Coil"), "RelayCoilElm"));
    	passMenuBar.addItem(getClassCheckItem(Locale.LS("Add Relay Contact"), "RelayContactElm"));
    	passMenuBar.addItem(getClassCheckItem(Locale.LS("Add Photoresistor"), "LDRElm"));
    	passMenuBar.addItem(getClassCheckItem(Locale.LS("Add Thermistor"), "ThermistorNTCElm"));
    	passMenuBar.addItem(getClassCheckItem(Locale.LS("Add Memristor"), "MemristorElm"));
    	passMenuBar.addItem(getClassCheckItem(Locale.LS("Add Spark Gap"), "SparkGapElm"));
    	passMenuBar.addItem(getClassCheckItem(Locale.LS("Add Fuse"), "FuseElm"));
    	passMenuBar.addItem(getClassCheckItem(Locale.LS("Add Crystal"), "CrystalElm"));
    	passMenuBar.addItem(getClassCheckItem(Locale.LS("Add Cross Switch"), "CrossSwitchElm"));
    	mainMenuBar.addItem(SafeHtmlUtils.fromTrustedString(CheckboxMenuItem.checkBoxHtml+Locale.LS("&nbsp;</div>Passive Components")), passMenuBar);

    	MenuBar inputMenuBar = new MenuBar(true);
    	inputMenuBar.addItem(getClassCheckItem(Locale.LS("Add Ground"), "GroundElm"));
    	inputMenuBar.addItem(getClassCheckItem(Locale.LS("Add Voltage Source (2-terminal)"), "DCVoltageElm"));
    	inputMenuBar.addItem(getClassCheckItem(Locale.LS("Add A/C Voltage Source (2-terminal)"), "ACVoltageElm"));
    	inputMenuBar.addItem(getClassCheckItem(Locale.LS("Add Voltage Source (1-terminal)"), "RailElm"));
    	inputMenuBar.addItem(getClassCheckItem(Locale.LS("Add A/C Voltage Source (1-terminal)"), "ACRailElm"));
    	inputMenuBar.addItem(getClassCheckItem(Locale.LS("Add Square Wave Source (1-terminal)"), "SquareRailElm"));
    	inputMenuBar.addItem(getClassCheckItem(Locale.LS("Add Clock"), "ClockElm"));
    	inputMenuBar.addItem(getClassCheckItem(Locale.LS("Add A/C Sweep"), "SweepElm"));
    	inputMenuBar.addItem(getClassCheckItem(Locale.LS("Add Variable Voltage"), "VarRailElm"));
    	inputMenuBar.addItem(getClassCheckItem(Locale.LS("Add Antenna"), "AntennaElm"));
    	inputMenuBar.addItem(getClassCheckItem(Locale.LS("Add AM Source"), "AMElm"));
    	inputMenuBar.addItem(getClassCheckItem(Locale.LS("Add FM Source"), "FMElm"));
    	inputMenuBar.addItem(getClassCheckItem(Locale.LS("Add Current Source"), "CurrentElm"));
    	inputMenuBar.addItem(getClassCheckItem(Locale.LS("Add Noise Generator"), "NoiseElm"));
    	inputMenuBar.addItem(getClassCheckItem(Locale.LS("Add Audio Input"), "AudioInputElm"));
    	inputMenuBar.addItem(getClassCheckItem(Locale.LS("Add Data Input"), "DataInputElm"));
    	inputMenuBar.addItem(getClassCheckItem(Locale.LS("Add External Voltage (JavaScript)"), "ExtVoltageElm"));

    	mainMenuBar.addItem(SafeHtmlUtils.fromTrustedString(CheckboxMenuItem.checkBoxHtml+Locale.LS("&nbsp;</div>Inputs and Sources")), inputMenuBar);
    	
    	MenuBar outputMenuBar = new MenuBar(true);
    	outputMenuBar.addItem(getClassCheckItem(Locale.LS("Add Analog Output"), "OutputElm"));
    	outputMenuBar.addItem(getClassCheckItem(Locale.LS("Add LED"), "LEDElm"));
    	outputMenuBar.addItem(getClassCheckItem(Locale.LS("Add Lamp"), "LampElm"));
    	outputMenuBar.addItem(getClassCheckItem(Locale.LS("Add Text"), "TextElm"));
    	outputMenuBar.addItem(getClassCheckItem(Locale.LS("Add Box"), "BoxElm"));
    	outputMenuBar.addItem(getClassCheckItem(Locale.LS("Add Line"), "LineElm"));
    	outputMenuBar.addItem(getClassCheckItem(Locale.LS("Add Labeled Node"), "LabeledNodeElm"));
    	outputMenuBar.addItem(getClassCheckItem(Locale.LS("Add Voltmeter/Scope Probe"), "ProbeElm"));
    	outputMenuBar.addItem(getClassCheckItem(Locale.LS("Add Ohmmeter"), "OhmMeterElm"));
    	outputMenuBar.addItem(getClassCheckItem(Locale.LS("Add Ammeter"), "AmmeterElm"));
    	outputMenuBar.addItem(getClassCheckItem(Locale.LS("Add Wattmeter"), "WattmeterElm"));
    	outputMenuBar.addItem(getClassCheckItem(Locale.LS("Add Test Point"), "TestPointElm"));
    	outputMenuBar.addItem(getClassCheckItem(Locale.LS("Add Decimal Display"), "DecimalDisplayElm"));
    	outputMenuBar.addItem(getClassCheckItem(Locale.LS("Add LED Array"), "LEDArrayElm"));
    	outputMenuBar.addItem(getClassCheckItem(Locale.LS("Add Data Export"), "DataRecorderElm"));
    	outputMenuBar.addItem(getClassCheckItem(Locale.LS("Add Audio Output"), "AudioOutputElm"));
    	outputMenuBar.addItem(getClassCheckItem(Locale.LS("Add Stop Trigger"), "StopTriggerElm"));
    	outputMenuBar.addItem(getClassCheckItem(Locale.LS("Add DC Motor"), "DCMotorElm"));
    	outputMenuBar.addItem(getClassCheckItem(Locale.LS("Add 3-Phase Motor"), "ThreePhaseMotorElm"));
    	mainMenuBar.addItem(SafeHtmlUtils.fromTrustedString(CheckboxMenuItem.checkBoxHtml+Locale.LS("&nbsp;</div>Outputs and Labels")), outputMenuBar);
    	
    	MenuBar activeMenuBar = new MenuBar(true);
    	activeMenuBar.addItem(getClassCheckItem(Locale.LS("Add Diode"), "DiodeElm"));
    	activeMenuBar.addItem(getClassCheckItem(Locale.LS("Add Zener Diode"), "ZenerElm"));
    	activeMenuBar.addItem(getClassCheckItem(Locale.LS("Add Transistor (bipolar, NPN)"), "NTransistorElm"));
    	activeMenuBar.addItem(getClassCheckItem(Locale.LS("Add Transistor (bipolar, PNP)"), "PTransistorElm"));
    	activeMenuBar.addItem(getClassCheckItem(Locale.LS("Add MOSFET (N-Channel)"), "NMosfetElm"));
    	activeMenuBar.addItem(getClassCheckItem(Locale.LS("Add MOSFET (P-Channel)"), "PMosfetElm"));
    	activeMenuBar.addItem(getClassCheckItem(Locale.LS("Add JFET (N-Channel)"), "NJfetElm"));
    	activeMenuBar.addItem(getClassCheckItem(Locale.LS("Add JFET (P-Channel)"), "PJfetElm"));
    	activeMenuBar.addItem(getClassCheckItem(Locale.LS("Add SCR"), "SCRElm"));
    	activeMenuBar.addItem(getClassCheckItem(Locale.LS("Add DIAC"), "DiacElm"));
    	activeMenuBar.addItem(getClassCheckItem(Locale.LS("Add TRIAC"), "TriacElm"));
    	activeMenuBar.addItem(getClassCheckItem(Locale.LS("Add Darlington Pair (NPN)"), "NDarlingtonElm"));
    	activeMenuBar.addItem(getClassCheckItem(Locale.LS("Add Darlington Pair (PNP)"), "PDarlingtonElm"));
    	activeMenuBar.addItem(getClassCheckItem(Locale.LS("Add Varactor/Varicap"), "VaractorElm"));
    	activeMenuBar.addItem(getClassCheckItem(Locale.LS("Add Tunnel Diode"), "TunnelDiodeElm"));
    	activeMenuBar.addItem(getClassCheckItem(Locale.LS("Add Triode"), "TriodeElm"));
    	activeMenuBar.addItem(getClassCheckItem(Locale.LS("Add Unijunction Transistor"), "UnijunctionElm"));
    	mainMenuBar.addItem(SafeHtmlUtils.fromTrustedString(CheckboxMenuItem.checkBoxHtml+Locale.LS("&nbsp;</div>Active Components")), activeMenuBar);

    	MenuBar activeBlocMenuBar = new MenuBar(true);
    	activeBlocMenuBar.addItem(getClassCheckItem(Locale.LS("Add Op Amp (ideal, - on top)"), "OpAmpElm"));
    	activeBlocMenuBar.addItem(getClassCheckItem(Locale.LS("Add Op Amp (ideal, + on top)"), "OpAmpSwapElm"));
    	activeBlocMenuBar.addItem(getClassCheckItem(Locale.LS("Add Op Amp (real)"), "OpAmpRealElm"));
    	activeBlocMenuBar.addItem(getClassCheckItem(Locale.LS("Add Analog Switch (SPST)"), "AnalogSwitchElm"));
    	activeBlocMenuBar.addItem(getClassCheckItem(Locale.LS("Add Analog Switch (SPDT)"), "AnalogSwitch2Elm"));
    	activeBlocMenuBar.addItem(getClassCheckItem(Locale.LS("Add Tristate Buffer"), "TriStateElm"));
    	activeBlocMenuBar.addItem(getClassCheckItem(Locale.LS("Add Schmitt Trigger"), "SchmittElm"));
    	activeBlocMenuBar.addItem(getClassCheckItem(Locale.LS("Add Schmitt Trigger (Inverting)"), "InvertingSchmittElm"));
    	activeBlocMenuBar.addItem(getClassCheckItem(Locale.LS("Add Delay Buffer"), "DelayBufferElm"));
    	activeBlocMenuBar.addItem(getClassCheckItem(Locale.LS("Add CCII+"), "CC2Elm"));
    	activeBlocMenuBar.addItem(getClassCheckItem(Locale.LS("Add CCII-"), "CC2NegElm"));
    	activeBlocMenuBar.addItem(getClassCheckItem(Locale.LS("Add Comparator (Hi-Z/GND output)"), "ComparatorElm"));
    	activeBlocMenuBar.addItem(getClassCheckItem(Locale.LS("Add OTA (LM13700 style)"), "OTAElm"));
    	activeBlocMenuBar.addItem(getClassCheckItem(Locale.LS("Add Voltage-Controlled Voltage Source (VCVS)"), "VCVSElm"));
    	activeBlocMenuBar.addItem(getClassCheckItem(Locale.LS("Add Voltage-Controlled Current Source (VCCS)"), "VCCSElm"));
    	activeBlocMenuBar.addItem(getClassCheckItem(Locale.LS("Add Current-Controlled Voltage Source (CCVS)"), "CCVSElm"));
    	activeBlocMenuBar.addItem(getClassCheckItem(Locale.LS("Add Current-Controlled Current Source (CCCS)"), "CCCSElm"));
    	activeBlocMenuBar.addItem(getClassCheckItem(Locale.LS("Add Optocoupler"), "OptocouplerElm"));
    	activeBlocMenuBar.addItem(getClassCheckItem(Locale.LS("Add Time Delay Relay"), "TimeDelayRelayElm"));
    	activeBlocMenuBar.addItem(getClassCheckItem(Locale.LS("Add LM317"), "CustomCompositeElm:~LM317-v2"));
    	activeBlocMenuBar.addItem(getClassCheckItem(Locale.LS("Add TL431"), "CustomCompositeElm:~TL431"));
    	activeBlocMenuBar.addItem(getClassCheckItem(Locale.LS("Add Motor Protection Switch"), "MotorProtectionSwitchElm"));
    	activeBlocMenuBar.addItem(getClassCheckItem(Locale.LS("Add Subcircuit Instance"), "CustomCompositeElm"));
    	mainMenuBar.addItem(SafeHtmlUtils.fromTrustedString(CheckboxMenuItem.checkBoxHtml+Locale.LS("&nbsp;</div>Active Building Blocks")), activeBlocMenuBar);
    	
    	MenuBar gateMenuBar = new MenuBar(true);
    	gateMenuBar.addItem(getClassCheckItem(Locale.LS("Add Logic Input"), "LogicInputElm"));
    	gateMenuBar.addItem(getClassCheckItem(Locale.LS("Add Logic Output"), "LogicOutputElm"));
    	gateMenuBar.addItem(getClassCheckItem(Locale.LS("Add Inverter"), "InverterElm"));
    	gateMenuBar.addItem(getClassCheckItem(Locale.LS("Add NAND Gate"), "NandGateElm"));
    	gateMenuBar.addItem(getClassCheckItem(Locale.LS("Add NOR Gate"), "NorGateElm"));
    	gateMenuBar.addItem(getClassCheckItem(Locale.LS("Add AND Gate"), "AndGateElm"));
    	gateMenuBar.addItem(getClassCheckItem(Locale.LS("Add OR Gate"), "OrGateElm"));
    	gateMenuBar.addItem(getClassCheckItem(Locale.LS("Add XOR Gate"), "XorGateElm"));
    	mainMenuBar.addItem(SafeHtmlUtils.fromTrustedString(CheckboxMenuItem.checkBoxHtml+Locale.LS("&nbsp;</div>Logic Gates, Input and Output")), gateMenuBar);

    	MenuBar chipMenuBar = new MenuBar(true);
    	chipMenuBar.addItem(getClassCheckItem(Locale.LS("Add D Flip-Flop"), "DFlipFlopElm"));
    	chipMenuBar.addItem(getClassCheckItem(Locale.LS("Add JK Flip-Flop"), "JKFlipFlopElm"));
    	chipMenuBar.addItem(getClassCheckItem(Locale.LS("Add T Flip-Flop"), "TFlipFlopElm"));
    	chipMenuBar.addItem(getClassCheckItem(Locale.LS("Add 7 Segment LED"), "SevenSegElm"));
    	chipMenuBar.addItem(getClassCheckItem(Locale.LS("Add 7 Segment Decoder"), "SevenSegDecoderElm"));
    	chipMenuBar.addItem(getClassCheckItem(Locale.LS("Add Multiplexer"), "MultiplexerElm"));
    	chipMenuBar.addItem(getClassCheckItem(Locale.LS("Add Demultiplexer"), "DeMultiplexerElm"));
    	chipMenuBar.addItem(getClassCheckItem(Locale.LS("Add SIPO shift register"), "SipoShiftElm"));
    	chipMenuBar.addItem(getClassCheckItem(Locale.LS("Add PISO shift register"), "PisoShiftElm"));
    	chipMenuBar.addItem(getClassCheckItem(Locale.LS("Add Counter"), "CounterElm"));
    	chipMenuBar.addItem(getClassCheckItem(Locale.LS("Add Counter w/ Load"), "Counter2Elm"));
    	chipMenuBar.addItem(getClassCheckItem(Locale.LS("Add Ring Counter"), "DecadeElm"));
    	chipMenuBar.addItem(getClassCheckItem(Locale.LS("Add Latch"), "LatchElm"));
    	chipMenuBar.addItem(getClassCheckItem(Locale.LS("Add Sequence generator"), "SeqGenElm"));
    	chipMenuBar.addItem(getClassCheckItem(Locale.LS("Add Adder"), "FullAdderElm"));
    	chipMenuBar.addItem(getClassCheckItem(Locale.LS("Add Half Adder"), "HalfAdderElm"));
    	chipMenuBar.addItem(getClassCheckItem(Locale.LS("Add Custom Logic"), "UserDefinedLogicElm")); // 不要更改此项，否则会破坏用户保存的快捷键
    	chipMenuBar.addItem(getClassCheckItem(Locale.LS("Add Static RAM"), "SRAMElm"));
    	mainMenuBar.addItem(SafeHtmlUtils.fromTrustedString(CheckboxMenuItem.checkBoxHtml+Locale.LS("&nbsp;</div>Digital Chips")), chipMenuBar);
    	
    	MenuBar achipMenuBar = new MenuBar(true);
    	achipMenuBar.addItem(getClassCheckItem(Locale.LS("Add 555 Timer"), "TimerElm"));
    	achipMenuBar.addItem(getClassCheckItem(Locale.LS("Add Phase Comparator"), "PhaseCompElm"));
    	achipMenuBar.addItem(getClassCheckItem(Locale.LS("Add DAC"), "DACElm"));
    	achipMenuBar.addItem(getClassCheckItem(Locale.LS("Add ADC"), "ADCElm"));
    	achipMenuBar.addItem(getClassCheckItem(Locale.LS("Add VCO"), "VCOElm"));
    	achipMenuBar.addItem(getClassCheckItem(Locale.LS("Add Monostable"), "MonostableElm"));
    	mainMenuBar.addItem(SafeHtmlUtils.fromTrustedString(CheckboxMenuItem.checkBoxHtml+Locale.LS("&nbsp;</div>Analog and Hybrid Chips")), achipMenuBar);
    	
    	if (subcircuitMenuBar == null)
    	    subcircuitMenuBar = new MenuBar[2];
    	subcircuitMenuBar[num] = new MenuBar(true);
    	mainMenuBar.addItem(SafeHtmlUtils.fromTrustedString(CheckboxMenuItem.checkBoxHtml+Locale.LS("&nbsp;</div>Subcircuits")), subcircuitMenuBar[num]);
    	
    	MenuBar otherMenuBar = new MenuBar(true);
    	CheckboxMenuItem mi;
    	otherMenuBar.addItem(mi=getClassCheckItem(Locale.LS("Drag All"), "DragAll"));
    	mi.setShortcut(Locale.LS("(Alt-drag)"));
    	otherMenuBar.addItem(mi=getClassCheckItem(Locale.LS("Drag Row"), "DragRow"));
    	mi.setShortcut(Locale.LS("(A-S-drag)"));
    	otherMenuBar.addItem(mi=getClassCheckItem(Locale.LS("Drag Column"), "DragColumn"));
    	mi.setShortcut(isMac ? Locale.LS("(A-Cmd-drag)") : Locale.LS("(A-M-drag)"));
    	otherMenuBar.addItem(getClassCheckItem(Locale.LS("Drag Selected"), "DragSelected"));
    	otherMenuBar.addItem(mi=getClassCheckItem(Locale.LS("Drag Post"), "DragPost"));
    	mi.setShortcut("(" + ctrlMetaKey + "drag)");

    	mainMenuBar.addItem(SafeHtmlUtils.fromTrustedString(CheckboxMenuItem.checkBoxHtml+Locale.LS("&nbsp;</div>Drag")), otherMenuBar);

    	mainMenuBar.addItem(mi=getClassCheckItem(Locale.LS("Select/Drag Sel"), "Select"));
	mi.setShortcut(Locale.LS("(space or Shift-drag)"));
    }
    
    void composeSubcircuitMenu() {
	if (subcircuitMenuBar == null)
	    return;
	int mi;
	
	// 有两个菜单需要更新：Draw 菜单中的那个，以及鼠标右键菜单中的那个
	for (mi = 0; mi != 2; mi++) {
	    MenuBar menu = subcircuitMenuBar[mi];
	    menu.clearItems();
	    Vector<CustomCompositeModel> list = CustomCompositeModel.getModelList();
	    int i;
	    for (i = 0; i != list.size(); i++) {
		String name = list.get(i).name;
		menu.addItem(getClassCheckItem(Locale.LS("Add ") + name, "CustomCompositeElm:" + name));
	    }
	}
	lastSubcircuitMenuUpdate = CustomCompositeModel.sequenceNumber;
    }
    
    public void composeSelectScopeMenu(MenuBar sb) {
	sb.clearItems();
	selectScopeMenuItems = new Vector<MenuItem>();
	for( int i = 0; i < scopeCount; i++) {
	    String s, l;
	    s = Locale.LS("Scope")+" "+ Integer.toString(i+1);
	    l=scopes[i].getScopeLabelOrText();
	    if (l!="")
		s+=" ("+SafeHtmlUtils.htmlEscape(l)+")";
	    selectScopeMenuItems.add(new MenuItem(s ,new MyCommand("elm", "addToScope"+Integer.toString(i))));
	}
	int c = countScopeElms();
	for (int j = 0; j < c; j++) {
	    String s,l;
	    s = Locale.LS("Undocked Scope")+" "+ Integer.toString(j+1);
	    l = getNthScopeElm(j).elmScope.getScopeLabelOrText();
	    if (l!="")
		s += " ("+SafeHtmlUtils.htmlEscape(l)+")";
	    selectScopeMenuItems.add(new MenuItem(s, new MyCommand("elm", "addToScope"+Integer.toString(scopeCount+j))));
	}
	for (MenuItem mi : selectScopeMenuItems)
	    sb.addItem(mi);
    }
    
    public void setSlidersPanelHeight() {
    	int i;
    	int cumheight=0;
    	for (i=0; i < verticalPanel.getWidgetIndex(slidersPanel); i++) {
    		if (verticalPanel.getWidget(i) !=loadFileInput) {
    			cumheight=cumheight+verticalPanel.getWidget(i).getOffsetHeight();
    			if (verticalPanel.getWidget(i).getStyleName().contains("topSpace"))
    					cumheight+=12;
    		}
    	}
    	int ih=RootLayoutPanel.get().getOffsetHeight()-MENUBARHEIGHT-cumheight;
    	if (toolbarCheckItem.getState())
    		ih-=TOOLBARHEIGHT;
    	if (ih<0)
    		ih=0;
    	slidersPanel.setHeight(ih+"px");
    }
    


    


    CheckboxMenuItem getClassCheckItem(String s, String t) {
	if (classToLabelMap == null)
	    classToLabelMap = new HashMap<String, String>();
	classToLabelMap.put(t, s);

    	// try {
    	//   Class c = Class.forName(t);
    	String shortcut="";
    	CircuitElm elm = null;
    	try {
    	    elm = constructElement(t, 0, 0);
    	} catch (Exception e) {}
    	CheckboxMenuItem mi;
    	//  register(c, elm);
    	if ( elm!=null ) {
    		if (elm.needsShortcut() ) {
    			shortcut += (char)elm.getShortcut();
    			if (shortcuts[elm.getShortcut()] != null && !shortcuts[elm.getShortcut()].equals(t))
    			    console("already have shortcut for " + (char)elm.getShortcut() + " " + elm);
    			shortcuts[elm.getShortcut()]=t;
    		}
    		elm.delete();
    	}
//    	else
//    		GWT.log("Coudn't create class: "+t);
    	//	} catch (Exception ee) {
    	//	    ee.printStackTrace();
    	//	}
    	if (shortcut=="")
    		mi= new CheckboxMenuItem(s);
    	else
    		mi = new CheckboxMenuItem(s, shortcut);
    	mi.setScheduledCommand(new MyCommand("main", t) );
    	mainMenuItems.add(mi);
    	mainMenuItemNames.add(t);
    	return mi;
    }
    
    

    
    void centreCircuit() {
	if (elmList == null)  // 若在初始化期间被调用则避免异常
	    return;
	
	Rectangle bounds = getCircuitBounds();
    	setCircuitArea();
	
    	double scale = 1;
    	int cheight = circuitArea.height;
    	
    	// 如果没有示波器且窗口不太宽，那么居中时不要占满整个电路区域，
    	// 因为角落里的信息可能不会碍事。不过我们仍然希望 circuitArea 保持
    	// 完整高度，以便用户手动把东西放到那里。
    	if (scopeCount == 0 && circuitArea.width < 800) {
    	    int h = (int) ((double)cheight * scopeHeightFraction);
    	    cheight -= h;
    	}
    	
    	if (bounds != null)
    	    // 在边缘留出一些空间，因为边界计算并不精确
    	    scale = Math.min(circuitArea.width /(double)(bounds.width+140),
    			     cheight/(double)(bounds.height+100));
    	scale = Math.min(scale, 1.5); // 限制缩放比例，以免在大窗口中创建过大的电路

    	// 计算变换矩阵，使电路填满屏幕的大部分区域
    	transform[0] = transform[3] = scale;
    	transform[1] = transform[2] = transform[4] = transform[5] = 0;
    	if (bounds != null) {
    	    transform[4] = (circuitArea.width -bounds.width *scale)/2 - bounds.x*scale;
    	    transform[5] = (cheight-bounds.height*scale)/2 - bounds.y*scale;
    	}
    }

    // 获取电路边界。注意这里不使用 setBbox()，那是在绘制电路时才计算的，
    // 但此边界在首次绘制之前就需要就绪，因此我们使用这种粗略的方法
    Rectangle getCircuitBounds() {
    	int i;
    	int minx = 30000, maxx = -30000, miny = 30000, maxy = -30000;
    	for (i = 0; i != elmList.size(); i++) {
    		CircuitElm ce = getElm(i);
    		// 居中文本在尝试居中电路时会引起问题，
    		// 因此在这里对它们进行特殊处理
    		if (!ce.isCenteredText()) {
    			minx = min(ce.x, min(ce.x2, minx));
    			maxx = max(ce.x, max(ce.x2, maxx));
    		}
    		miny = min(ce.y, min(ce.y2, miny));
    		maxy = max(ce.y, max(ce.y2, maxy));
    	}
    	if (minx > maxx)
    	    return null;
    	return new Rectangle(minx, miny, maxx-minx, maxy-miny);
    }

    long lastTime = 0, lastFrameTime, lastIterTime, secTime = 0;
    int frames = 0;
    int steps = 0;
    int framerate = 0, steprate = 0;
    static CirSim theSim;

    
    public void setSimRunning(boolean s) {
    	if (s) {
    	    	if (stopMessage != null)
    	    	    return;
    		simRunning = true;
    		runStopButton.setHTML(Locale.LSHTML("<strong>RUN</strong>&nbsp;/&nbsp;Stop"));
    		runStopButton.setStylePrimaryName("topButton");
    		timer.scheduleRepeating(FASTTIMER);
    	} else {
    		simRunning = false;
    		runStopButton.setHTML(Locale.LSHTML("Run&nbsp;/&nbsp;<strong>STOP</strong>"));
    		runStopButton.setStylePrimaryName("topButton-red");
    		timer.cancel();
		repaint();
    	}
    }
    
    public boolean simIsRunning() {
    	return simRunning;
    }
    
    boolean needsRepaint;
    
    void repaint() {
	if (!needsRepaint) {
	    needsRepaint = true;
	    Scheduler.get().scheduleFixedDelay(new Scheduler.RepeatingCommand() {
		public boolean execute() {
		      updateCircuit();
		      needsRepaint = false;
		      return false;
		  }
	    }, FASTTIMER);
	}
    }
    
    // *****************************************************************
    //                     更新电路
    
    public void updateCircuit() {
        PerfMonitor perfmon = new PerfMonitor();
        perfmon.startContext("updateCircuit()");

        checkCanvasSize();
        
        // 分析电路
        boolean didAnalyze = analyzeFlag;
        if (analyzeFlag || dcAnalysisFlag) {
            perfmon.startContext("analyzeCircuit()");
            analyzeCircuit();
            analyzeFlag = false;
            perfmon.stopContext();
        }
        
        // 填充电路矩阵（stamp）
        if (needsStamp && simRunning) {
            perfmon.startContext("stampCircuit()");
            try {
                preStampAndStampCircuit();
            } catch (Exception e) {
                stop("Exception in stampCircuit()", null);
		GWT.log("Exception in stampCircuit", e);
            }
            perfmon.stopContext();
        }
        
        if (stopElm != null && stopElm != mouseElm)
            stopElm.setMouseElm(true);
        
        setupScopes();

        Graphics g = new Graphics(cvcontext);

        if (printableCheckItem.getState()) {
            CircuitElm.whiteColor = Color.black;
            CircuitElm.lightGrayColor = Color.black;
            g.setColor(Color.white);
            cv.getElement().getStyle().setBackgroundColor("#fff");
        } else {
            CircuitElm.whiteColor = Color.white;
            CircuitElm.lightGrayColor = Color.lightGray;
            g.setColor(Color.black);
            cv.getElement().getStyle().setBackgroundColor("#000");
        }

        // 清除帧
        g.fillRect(0, 0, canvasWidth, canvasHeight);

        // 运行电路
        if (simRunning) {
            if (needsStamp)
                console("needsStamp while simRunning?");

            perfmon.startContext("runCircuit()");
            try {                
                runCircuit(didAnalyze);
            } catch (Exception e) {
                debugger();
                console("exception in runCircuit " + e);
                e.printStackTrace();
            }
            perfmon.stopContext();
        }

        long sysTime = System.currentTimeMillis();
        if (simRunning) {
            if (lastTime != 0) {
                int inc = (int) (sysTime - lastTime);
                double c = currentBar.getValue();
                c = java.lang.Math.exp(c / 3.5 - 14.2);
                CircuitElm.currentMult = 1.7 * inc * c;
                if (!conventionCheckItem.getState())
                    CircuitElm.currentMult = -CircuitElm.currentMult;
            }
            lastTime = sysTime;
        } else {
            lastTime = 0;
        }

        if (sysTime - secTime >= 1000) {
            framerate = frames;
            steprate = steps;
            frames = 0;
            steps = 0;
            secTime = sysTime;
        }

        CircuitElm.powerMult = Math.exp(powerBar.getValue() / 4.762 - 7);

        perfmon.startContext("graphics");

        g.setFont(CircuitElm.unitsFont);

        g.context.setLineCap(LineCap.ROUND);

        if (noEditCheckItem.getState())
            g.drawLock(20, 30);
        
        g.setColor(Color.white);
        
        // 设置图形变换以处理缩放和偏移
        double scale = devicePixelRatio();
        cvcontext.setTransform(transform[0] * scale, 0, 0, transform[3] * scale, transform[4] * scale, transform[5] * scale);

        // 绘制每个元件
        perfmon.startContext("elm.draw()");
        for (int i = 0; i != elmList.size(); i++) {
            if (powerCheckItem.getState())
                g.setColor(Color.gray);
            
            getElm(i).draw(g);
        }
        perfmon.stopContext();

        // 正常绘制连接点（post）
        if (mouseMode != CirSim.MODE_DRAG_ROW && mouseMode != CirSim.MODE_DRAG_COLUMN) {
            for (int i = 0; i != postDrawList.size(); i++)
                CircuitElm.drawPost(g, postDrawList.get(i));
        }

        // 对于某些鼠标模式，重要的不是连接点而是端点（端点
        // 只有对于两端元件才与连接点相同）。如有需要，我们现在就绘制它们
        if (tempMouseMode == MODE_DRAG_ROW || 
            tempMouseMode == MODE_DRAG_COLUMN || 
            tempMouseMode == MODE_DRAG_POST || 
            tempMouseMode == MODE_DRAG_SELECTED) {
            for (int i = 0; i != elmList.size(); i++) {

                CircuitElm ce = getElm(i);
                // ce.drawPost(g, ce.x , ce.y );
                // ce.drawPost(g, ce.x2, ce.y2);
                if (ce != mouseElm || tempMouseMode != MODE_DRAG_POST) {
                    g.setColor(Color.gray);
                    g.fillOval(ce.x - 3, ce.y - 3, 7, 7);
                    g.fillOval(ce.x2 - 3, ce.y2 - 3, 7, 7);
                } else {
                    ce.drawHandles(g, CircuitElm.selectColor);
                }
            }
        }
        
        // 为正在创建的元件绘制手柄
        if (tempMouseMode == MODE_SELECT && mouseElm != null) {
            mouseElm.drawHandles(g, CircuitElm.selectColor);
        }

        // 为正在拖动的元件绘制手柄
        if (dragElm != null && (dragElm.x != dragElm.x2 || dragElm.y != dragElm.y2)) {
            dragElm.draw(g);
            dragElm.drawHandles(g, CircuitElm.selectColor);
        }

        // 绘制错误连接。最后再绘制它们，以免被其他内容覆盖。
        for (int i = 0; i != badConnectionList.size(); i++) {
            Point cn = badConnectionList.get(i);
            g.setColor(Color.red);
            g.fillOval(cn.x - 3, cn.y - 3, 7, 7);
        }

        // 绘制选择矩形
        if (selectedArea != null) {
            g.setColor(CircuitElm.selectColor);
            g.drawRect(selectedArea.x, selectedArea.y, selectedArea.width, selectedArea.height);
        }

        // 绘制十字光标
        if (crossHairCheckItem.getState() && mouseCursorX >= 0
                && mouseCursorX <= circuitArea.width && mouseCursorY <= circuitArea.height) {
            g.setColor(Color.gray);
            int x = snapGrid(inverseTransformX(mouseCursorX));
            int y = snapGrid(inverseTransformY(mouseCursorY));
            g.drawLine(x, inverseTransformY(0), x, inverseTransformY(circuitArea.height));
            g.drawLine(inverseTransformX(0), y, inverseTransformX(circuitArea.width), y);
        }

        // 重置图形缩放和平移
        cvcontext.setTransform(scale, 0, 0, scale, 0, 0);

        // 绘制底部区域，即示波器和信息部分
        perfmon.startContext("drawBottomArea()");
        drawBottomArea(g);
        perfmon.stopContext();

        g.setColor(Color.white);
        
        perfmon.stopContext(); // 图形
        
        if (stopElm != null && stopElm != mouseElm)
            stopElm.setMouseElm(false);
        
        frames++;

        // 如果我们执行了直流分析，则需要在清除该标志后
        // 重新分析电路。
        if (dcAnalysisFlag) {
            dcAnalysisFlag = false;
            analyzeFlag = true;
        }

        lastFrameTime = lastTime;

        perfmon.stopContext(); // 更新电路
        
        if (developerMode) {
            int height = 45;
            int increment = 15;
            g.drawString("Framerate: " + CircuitElm.showFormat.format(framerate), 10, height);
            g.drawString("Steprate: " + CircuitElm.showFormat.format(steprate), 10, height += increment);
            g.drawString("Steprate/iter: " + CircuitElm.showFormat.format(steprate / getIterCount()), 10, height += increment);
            g.drawString("iterc: " + CircuitElm.showFormat.format(getIterCount()), 10, height += increment);
            g.drawString("Frames: " + frames, 10, height += increment);
            
            height += (increment * 2);
            
            String perfmonResult = PerfMonitor.buildString(perfmon).toString();
            String[] splits = perfmonResult.split("\n");
            for (int x = 0; x < splits.length; x++) {
                g.drawString(splits[x], 10, height + (increment * x));
            }
        }

        // 在图形中显示鼠标模式信息
        if (mouseModeCheckItem.getState()){
            if (printableCheckItem.getState()) g.setColor(Color.black);
            g.drawString(Locale.LS("Mode: ") + classToLabelMap.get(mouseModeStr), 10, 29);
        }
        
        // 这应该始终是 updateCircuit()
        // 最后调用的内容；
        callUpdateHook();
    }

    void drawBottomArea(Graphics g) {
	int leftX = 0;
	int h = 0;
	if (stopMessage == null && scopeCount == 0) {
	    leftX = max(canvasWidth-infoWidth, 0);
	    int h0 = (int) (canvasHeight * scopeHeightFraction);
	    h = (mouseElm == null) ? 70 : h0;
	    if (hideInfoBox)
		h = 0;
	}
	if (stopMessage != null && circuitArea.height > canvasHeight-30)
	    h = 30;
	g.setColor(printableCheckItem.getState() ? "#eee" : "#111");
	g.fillRect(leftX, circuitArea.height-h, circuitArea.width, canvasHeight-circuitArea.height+h);
	g.setFont(CircuitElm.unitsFont);
	int ct = scopeCount;
	if (stopMessage != null)
	    ct = 0;
	int i;
	Scope.clearCursorInfo();
	for (i = 0; i != ct; i++)
	    scopes[i].selectScope(mouseCursorX, mouseCursorY);
	if (scopeElmArr != null)
	    for (i=0; i != scopeElmArr.length; i++)
		scopeElmArr[i].selectScope(mouseCursorX, mouseCursorY);
	for (i = 0; i != ct; i++)
	    scopes[i].draw(g);
	if (mouseWasOverSplitter) {
		g.setColor(CircuitElm.selectColor);
		g.setLineWidth(4.0);
		g.drawLine(0, circuitArea.height-2, circuitArea.width, circuitArea.height-2);
		g.setLineWidth(1.0);
	}
	g.setColor(CircuitElm.whiteColor);

	if (stopMessage != null) {
	    g.drawString(stopMessage, 10, canvasHeight-10);
	} else if (!hideInfoBox) {
	    // 在 JS 中，这个数组多大都无所谓，不存在越界异常
	    String info[] = new String[10];
	    if (mouseElm != null) {
		if (mousePost == -1) {
		    mouseElm.getInfo(info);
		    info[0] = Locale.LS(info[0]);
		    if (info[1] != null)
			info[1] = Locale.LS(info[1]);
		} else
		    info[0] = "V = " +
			CircuitElm.getUnitText(mouseElm.getPostVoltage(mousePost), "V");
//		/* //shownodes
//		for (i = 0; i != mouseElm.getPostCount(); i++)
//		    info[0] += " " + mouseElm.nodes[i];
//		if (mouseElm.getVoltageSourceCount() > 0)
//		    info[0] += ";" + (mouseElm.getVoltageSource()+nodeList.size());
//		*/
		
	    } else {
	    	info[0] = "t = " + CircuitElm.getTimeText(t);
	    	double timerate = 160*getIterCount()*timeStep;
	    	if (timerate >= .1)
	    	    info[0] += " (" + CircuitElm.showFormat.format(timerate) + "x)";
	    	info[1] = Locale.LS("time step = ") + CircuitElm.getTimeText(timeStep);
	    }
	    if (hintType != -1) {
		for (i = 0; info[i] != null; i++)
		    ;
		String s = getHint();
		if (s == null)
		    hintType = -1;
		else
		    info[i] = s;
	    }
	    int x = leftX + 5;
	    if (ct != 0)
		x = scopes[ct-1].rightEdge() + 20;
//	    x = max(x, canvasWidth*2/3);
	  //  x=cv.getCoordinateSpaceWidth()*2/3;
	    
	    // 统计数据行数
	    for (i = 0; info[i] != null; i++)
		;
	    int badnodes = badConnectionList.size();
	    if (badnodes > 0)
		info[i++] = badnodes + ((badnodes == 1) ?
					Locale.LS(" bad connection") : Locale.LS(" bad connections"));
	    if (savedFlag)
		info[i++] = "(saved)";

	    int ybase = circuitArea.height-h;
	    for (i = 0; info[i] != null; i++)
		g.drawString(info[i], x, ybase+15*(i+1));
	}
    }
    
    Color getBackgroundColor() {
	if (printableCheckItem.getState())
	    return Color.white;
	return Color.black;
    }
    
    int oldScopeCount = -1;
    
    boolean scopeMenuIsSelected(Scope s) {
	if (scopeMenuSelected < 0)
	    return false;
	if (scopeMenuSelected < scopeCount)
	    return scopes[scopeMenuSelected] == s;
	return getNthScopeElm(scopeMenuSelected-scopeCount).elmScope == s; 
    }
    
	native boolean isSidePanelCheckboxChecked() /*-{
		return $doc.getElementById("trigger").checked;
    }-*/;

    void setupScopes() {
    	int i;
    	Storage lstor = Storage.getLocalStorageIfSupported();
    	// 检查示波器，确保其元件仍然存在，并移除
    	// 未使用的示波器/列
    	int pos = -1;
    	for (i = 0; i < scopeCount; i++) {
    	    	if (scopes[i].needToRemove()) {
    			int j;
    			for (j = i; j != scopeCount; j++)
    				scopes[j] = scopes[j+1];
    			scopeCount--;
    			i--;
    			continue;
    		}
    		if (scopes[i].position > pos+1)
    			scopes[i].position = pos+1;
    		pos = scopes[i].position;
    	}
    	while (scopeCount > 0 && scopes[scopeCount-1].getElm() == null)
    		scopeCount--;
    	int h = canvasHeight - circuitArea.height;
    	pos = 0;
    	for (i = 0; i != scopeCount; i++)
    		scopeColCount[i] = 0;
    	for (i = 0; i != scopeCount; i++) {
    		pos = max(scopes[i].position, pos);
    		scopeColCount[scopes[i].position]++;
    	}
    	int colct = pos+1;
    	int iw = infoWidth;
    	if (colct <= 2)
    		iw = iw*3/2;
    	int w = (canvasWidth-iw) / colct; // Оно!
    	if (isSidePanelCheckboxChecked() && lstor.getItem("MOD_overlayingSidebar")=="true")
    		w = (canvasWidth-iw-VERTICALPANELWIDTH) / colct;
    	int marg = 10;
    	if (w < marg*2)
    		w = marg*2;
    	pos = -1;
    	int colh = 0;
    	int row = 0;
    	int speed = 0;
    	for (i = 0; i != scopeCount; i++) {
    		Scope s = scopes[i];
    		if (s.position > pos) {
    			pos = s.position;
    			colh = h / scopeColCount[pos];
    			row = 0;
    			speed = s.speed;
    		}
    		s.stackCount = scopeColCount[pos];
    		if (s.speed != speed) {
    			s.speed = speed;
    			s.resetGraph();
    		}
    		Rectangle r = new Rectangle(pos*w, canvasHeight-h+colh*row, w-marg, colh);
    		row++;
    		if (!r.equals(s.rect))
    			s.setRect(r);
    	}
    	if (oldScopeCount != scopeCount) {
    	    setCircuitArea();
    	    oldScopeCount = scopeCount;
    	}
		repaint();
    }
    
    String getHint() {
	CircuitElm c1 = getElm(hintItem1);
	CircuitElm c2 = getElm(hintItem2);
	if (c1 == null || c2 == null)
	    return null;
	if (hintType == HINT_LC) {
	    if (!(c1 instanceof InductorElm))
		return null;
	    if (!(c2 instanceof CapacitorElm))
		return null;
	    InductorElm ie = (InductorElm) c1;
	    CapacitorElm ce = (CapacitorElm) c2;
	    return Locale.LS("res.f = ") + CircuitElm.getUnitText(1/(2*pi*Math.sqrt(ie.inductance*
						    ce.capacitance)), "Hz");
	}
	if (hintType == HINT_RC) {
	    if (!(c1 instanceof ResistorElm))
		return null;
	    if (!(c2 instanceof CapacitorElm))
		return null;
	    ResistorElm re = (ResistorElm) c1;
	    CapacitorElm ce = (CapacitorElm) c2;
	    return "RC = " + CircuitElm.getUnitText(re.resistance*ce.capacitance,
					 "s");
	}
	if (hintType == HINT_3DB_C) {
	    if (!(c1 instanceof ResistorElm))
		return null;
	    if (!(c2 instanceof CapacitorElm))
		return null;
	    ResistorElm re = (ResistorElm) c1;
	    CapacitorElm ce = (CapacitorElm) c2;
	    return Locale.LS("f.3db = ") +
		CircuitElm.getUnitText(1/(2*pi*re.resistance*ce.capacitance), "Hz");
	}
	if (hintType == HINT_3DB_L) {
	    if (!(c1 instanceof ResistorElm))
		return null;
	    if (!(c2 instanceof InductorElm))
		return null;
	    ResistorElm re = (ResistorElm) c1;
	    InductorElm ie = (InductorElm) c2;
	    return Locale.LS("f.3db = ") +
		CircuitElm.getUnitText(re.resistance/(2*pi*ie.inductance), "Hz");
	}
	if (hintType == HINT_TWINT) {
	    if (!(c1 instanceof ResistorElm))
		return null;
	    if (!(c2 instanceof CapacitorElm))
		return null;
	    ResistorElm re = (ResistorElm) c1;
	    CapacitorElm ce = (CapacitorElm) c2;
	    return Locale.LS("fc = ") +
		CircuitElm.getUnitText(1/(2*pi*re.resistance*ce.capacitance), "Hz");
	}
	return null;
    }

//    public void toggleSwitch(int n) {
//	int i;
//	for (i = 0; i != elmList.size(); i++) {
//	    CircuitElm ce = getElm(i);
//	    if (ce instanceof SwitchElm) {
//		n--;
//		if (n == 0) {
//		    ((SwitchElm) ce).toggle();
//		    analyzeFlag = true;
//		    cv.repaint();
//		    return;
//		}
//	    }
//	}
//    }
    
    void needAnalyze() {
	analyzeFlag = true;
    	repaint();
	enableDisableMenuItems();
    }
    
    Vector<CircuitNode> nodeList;
    Vector<Point> postDrawList = new Vector<Point>();
    Vector<Point> badConnectionList = new Vector<Point>();
    CircuitElm voltageSources[];

    public CircuitNode getCircuitNode(int n) {
	if (n >= nodeList.size())
	    return null;
	return nodeList.elementAt(n);
    }

    public CircuitElm getElm(int n) {
	if (n >= elmList.size())
	    return null;
	return elmList.elementAt(n);
    }
    
    public Adjustable findAdjustable(CircuitElm elm, int item) {
	int i;
	for (i = 0; i != adjustables.size(); i++) {
	    Adjustable a = adjustables.get(i);
	    if (a.elm == elm && a.editItem == item)
		return a;
	}
	return null;
    }
    
    public static native void console(String text)
    /*-{
	    console.log(text);
	}-*/;

    public static native void debugger() /*-{ debugger; }-*/;
    
    class NodeMapEntry {
	int node;
	NodeMapEntry() { node = -1; }
	NodeMapEntry(int n) { node = n; }
    }
    // 将坐标点映射到节点编号
    HashMap<Point,NodeMapEntry> nodeMap;
    
    class WireInfo {
	CircuitElm wire;
	Vector<CircuitElm> neighbors;
	int post;
	WireInfo(CircuitElm w) {
	    wire = w;
	}
    }
    
    // 关于每条导线及其相邻元件的信息，用于计算导线电流
    Vector<WireInfo> wireInfoList;
    
    // 找出由导线等效物连接在一起的节点组，并将它们映射到同一个节点。这会通过减小矩阵规模
    // 显著加快速度。我们对导线、带标签节点和地线都这样做。
    // 实际映射到的节点尚未分配。相反，我们映射到同一个 NodeMapEntry。
    void calculateWireClosure() {
	int i;
	LabeledNodeElm.resetNodeList();
	GroundElm.resetNodeList();
	nodeMap = new HashMap<Point,NodeMapEntry>();
//	int mergeCount = 0;
	wireInfoList = new Vector<WireInfo>();
	for (i = 0; i != elmList.size(); i++) {
	    CircuitElm ce = getElm(i);
	    if (!ce.isRemovableWire())
		continue;
	    ce.hasWireInfo = false;
	    wireInfoList.add(new WireInfo(ce));
	    Point p0 = ce.getPost(0);
	    NodeMapEntry cn  = nodeMap.get(p0);
	    
	    // 我们连接到哪个端点
	    Point p1 = ce.getConnectedPost();
	    if (p1 == null) {
		// 没有连接的端点（首次遇到的带标签节点或地线即为这种情况）
		if (cn == null) {
		    cn = new NodeMapEntry();
		    nodeMap.put(p0, cn);
		}
		continue;
	    }
	    NodeMapEntry cn2 = nodeMap.get(p1);
	    if (cn != null && cn2 != null) {
		// 合并节点；遍历映射，把所有指向 cn2 的键改为指向 cn
		for (Map.Entry<Point, NodeMapEntry> entry : nodeMap.entrySet()) {
		    if (entry.getValue() == cn2)
			entry.setValue(cn);
		}
//		mergeCount++;
		continue;
	    }
	    if (cn != null) {
		nodeMap.put(p1, cn);
		continue;
	    }
	    if (cn2 != null) {
		nodeMap.put(p0, cn2);
		continue;
	    }
	    // 新条目
	    cn = new NodeMapEntry();
	    nodeMap.put(p0, cn);
	    nodeMap.put(p1, cn);
	}
	
//	console("got " + (groupCount-mergeCount) + " groups with " + nodeMap.size() + " nodes " + mergeCount);
    }
    
    // 生成计算导线电流所需的信息。大多数其他元件使用其端点上的电压来计算电流，
    // 但导线两端电压相同，因此我们需要改用相邻元件的电流。我们过去把导线当作零电压源来
    // 简化这一过程，但这样做效率极低，因为每条导线都会使矩阵多出 2 行。
    // 我们改为创建一组 WireInfo 对象来帮助计算导线电流，
    // 这样矩阵更简单，而且我们只在需要时才计算导线电流
    //（每帧一次，而不是每次子迭代一次）。我们需要按正确顺序排列 WireInfo，
    // 每个都包含邻居列表以及应使用哪一端
    //（因为一端可能比
    // 另一端先就绪）
    boolean calcWireInfo() {
	int i;
	int moved = 0;
	
	for (i = 0; i != wireInfoList.size(); i++) {
	    WireInfo wi = wireInfoList.get(i);
	    CircuitElm wire = wi.wire;
	    CircuitNode cn1 = nodeList.get(wire.getNode(0));  // 导线两端具有相同的节点编号
	    int j;

	    Vector<CircuitElm> neighbors0 = new Vector<CircuitElm>();
	    Vector<CircuitElm> neighbors1 = new Vector<CircuitElm>();
	    
	    // 假设每一端都已就绪（单端的地线节点除外）
	    // 带标签节点被视为有 2 个端子，见下文
	    boolean isReady0 = true, isReady1 = !(wire instanceof GroundElm);

	    // 遍历与这条导线共享节点的元件（可能通过其他导线间接连接，
	    // 但至少比遍历所有元件要快）
	    for (j = 0; j != cn1.links.size(); j++) {
		CircuitNodeLink cnl = cn1.links.get(j);
		CircuitElm ce = cnl.elm;
		if (ce == wire)
		    continue;
		Point pt = ce.getPost(cnl.num);
		
		// 这是否是一条还没有导线信息的导线？如果是，我们暂时还不能使用它。
		// 那会造成循环依赖。因此这一侧尚未就绪。
		boolean notReady = (ce.isRemovableWire() && !ce.hasWireInfo);
		
		// 该元件连接到哪个端点（如果有的话）？
		if (pt.x == wire.x && pt.y == wire.y) {
		    neighbors0.add(ce);
		    if (notReady) isReady0 = false;
		} else if (wire.getPostCount() > 1) {
		    Point p2 = wire.getConnectedPost();
		    if (pt.x == p2.x && pt.y == p2.y) { 
			neighbors1.add(ce);
			if (notReady) isReady1 = false;
		    }
		} else if (ce instanceof LabeledNodeElm && wire instanceof LabeledNodeElm &&
			((LabeledNodeElm) ce).text == ((LabeledNodeElm) wire).text) {
		    // ce 和 wire 都是带标签节点且标签匹配。将它们视为邻居
		    neighbors1.add(ce);
		    if (notReady) isReady1 = false;
		}
	    }

	    // 其中一个端点是否拥有计算电流所需的全部信息？
	    if (isReady0) {
		wi.neighbors = neighbors0;
		wi.post = 0;
		wire.hasWireInfo = true;
		moved = 0;
	    } else if (isReady1) {
		wi.neighbors = neighbors1;
		wi.post = 1;
		wire.hasWireInfo = true;
		moved = 0;
	    } else {
		// 否，将其移到列表末尾稍后再试
		wireInfoList.add(wireInfoList.remove(i--));
		moved++;
		if (moved > wireInfoList.size() * 2) {
		    stop("wire loop detected", wire);
		    return false;
		}
	    }
	}
	
	return true;
    }

    // 查找或分配地线节点
    void setGroundNode(boolean subcircuit) {
	int i;
	boolean gotGround = false;
	boolean gotRail = false;
	CircuitElm volt = null;
	    
	//System.out.println("ac1");
	// 查找电压源或地线元件
	for (i = 0; i != elmList.size(); i++) {
	    CircuitElm ce = getElm(i);
	    if (ce instanceof GroundElm) {
		gotGround = true;
		
		// 将地线节点设为 0
		NodeMapEntry nme = nodeMap.get(ce.getPost(0));
		nme.node = 0;
		break;
	    }
	    if (ce instanceof RailElm)
	    	gotRail = true;
	    if (volt == null && ce instanceof VoltageElm)
	    	volt = ce;
	}

	// 如果没有地线也没有电源轨，则电压源元件的第一个端子
	// 即视为地线（但子电路除外）
	if (!subcircuit && !gotGround && volt != null && !gotRail) {
	    CircuitNode cn = new CircuitNode();
	    Point pt = volt.getPost(0);
	    nodeList.addElement(cn);

	    // 更新节点映射
	    NodeMapEntry cln = nodeMap.get(pt);
	    if (cln != null)
		cln.node = 0;
	    else
		nodeMap.put(pt, new NodeMapEntry(0));
	} else {
	    // 否则为地线额外分配一个节点
	    CircuitNode cn = new CircuitNode();
	    nodeList.addElement(cn);
	}
    }

    // 生成节点列表
    void makeNodeList() {
	int i, j;
	int vscount = 0;
	for (i = 0; i != elmList.size(); i++) {
	    CircuitElm ce = getElm(i);
	    int inodes = ce.getInternalNodeCount();
	    int ivs = ce.getVoltageSourceCount();
	    int posts = ce.getPostCount();
	    
	    // 为每个端点分配一个节点，并将端点与节点对应起来
	    for (j = 0; j != posts; j++) {
		Point pt = ce.getPost(j);
		NodeMapEntry cln = nodeMap.get(pt);
		
		// 这个节点是否还不在映射中？或者节点编号是否尚未分配？
		//（在此之前我们不分配节点，因为改变节点的分配顺序
		// 会改变电路行为并破坏向后兼容性；
		// 下面连接未连接节点的代码可能会把不同的节点接到地线上）
		if (cln == null || cln.node == -1) {
		    CircuitNode cn = new CircuitNode();
		    CircuitNodeLink cnl = new CircuitNodeLink();
		    cnl.num = j;
		    cnl.elm = ce;
		    cn.links.addElement(cnl);
		    ce.setNode(j, nodeList.size());
		    if (cln != null)
			cln.node = nodeList.size();
		    else
			nodeMap.put(pt, new NodeMapEntry(nodeList.size()));
		    nodeList.addElement(cn);
		} else {
		    int n = cln.node;
		    CircuitNodeLink cnl = new CircuitNodeLink();
		    cnl.num = j;
		    cnl.elm = ce;
		    getCircuitNode(n).links.addElement(cnl);
		    ce.setNode(j, n);
		    // 如果是地线节点，确保节点电压为 0，
		    // 因为稍后可能不会设置它
		    if (n == 0)
			ce.setNodeVoltage(j, 0);
		}
	    }
	    for (j = 0; j != inodes; j++) {
		CircuitNode cn = new CircuitNode();
		cn.internal = true;
		CircuitNodeLink cnl = new CircuitNodeLink();
		cnl.num = j+posts;
		cnl.elm = ce;
		cn.links.addElement(cnl);
		ce.setNode(cnl.num, nodeList.size());
		nodeList.addElement(cn);
	    }
	    
	    // 同时统计电压源数量，以便分配数组
	    vscount += ivs;
	}
	
        voltageSources = new CircuitElm[vscount];
    }
    
    Vector<Integer> unconnectedNodes;
    Vector<CircuitElm> nodesWithGroundConnection;
    int nodesWithGroundConnectionCount;
    
    void findUnconnectedNodes() {
	int i, j;
	
	// 找出未间接连接到地线的节点。
	// 所有节点都必须以某种方式连接到地线，否则我们
	// 会得到矩阵错误。
	boolean closure[] = new boolean[nodeList.size()];
	boolean changed = true;
	unconnectedNodes = new Vector<Integer>();
	nodesWithGroundConnection = new Vector<CircuitElm>();
	closure[0] = true;
	while (changed) {
	    changed = false;
	    for (i = 0; i != elmList.size(); i++) {
		CircuitElm ce = getElm(i);
		if (ce instanceof WireElm)
		    continue;
		// 遍历 ce 的所有节点，检查它们是否连接到
		// 不在闭包中的其他节点
		boolean hasGround = false;
		for (j = 0; j < ce.getConnectionNodeCount(); j++) {
		    boolean hg = ce.hasGroundConnection(j);
		    if (hg)
			hasGround = true;
		    if (!closure[ce.getConnectionNode(j)]) {
			if (hg)
			    closure[ce.getConnectionNode(j)] = changed = true;
			continue;
		    }
		    int k;
		    for (k = 0; k != ce.getConnectionNodeCount(); k++) {
			if (j == k)
			    continue;
			int kn = ce.getConnectionNode(k);
			if (ce.getConnection(j, k) && !closure[kn]) {
			    closure[kn] = true;
			    changed = true;
			}
		    }
		}
		if (hasGround)
		    nodesWithGroundConnection.add(ce);
	    }
	    if (changed)
		continue;

	    // 用一个大电阻把某个未连接节点接到地线，然后重试
	    for (i = 0; i != nodeList.size(); i++)
		if (!closure[i] && !getCircuitNode(i).internal) {
		    unconnectedNodes.add(i);
		    console("node " + i + " unconnected");
//		    stampResistor(0, i, 1e8);   // do this later in connectUnconnectedNodes()
		    closure[i] = true;
		    changed = true;
		    break;
		}
	}
    }
    
    // 取之前识别出的未连接节点列表，用大电阻将它们接到地线。
    // 否则我们会得到矩阵错误。这个电阻必须足够大，
    // 否则像 555 方波发生器等电路会出问题
    void connectUnconnectedNodes() {
	int i;
	for (i = 0; i != unconnectedNodes.size(); i++) {
	    int n = unconnectedNodes.get(i);
	    stampResistor(0, n, 1e8);
	}
    }
    
    boolean validateCircuit() {
	int i, j;
	
	for (i = 0; i != elmList.size(); i++) {
	    CircuitElm ce = getElm(i);
	    // 查找没有电流路径的电感
	    if (ce instanceof InductorElm) {
		FindPathInfo fpi = new FindPathInfo(FindPathInfo.INDUCT, ce,
						    ce.getNode(1));
		if (!fpi.findPath(ce.getNode(0))) {
//		    console(ce + " no path");
		    ce.reset();
		}
	    }
	    // 查找没有电流路径的电流源
	    if (ce instanceof CurrentElm) {
		CurrentElm cur = (CurrentElm) ce;
		FindPathInfo fpi = new FindPathInfo(FindPathInfo.INDUCT, ce,
						    ce.getNode(1));
		cur.setBroken(!fpi.findPath(ce.getNode(0)));
	    }
	    if (ce instanceof VCCSElm) {
		VCCSElm cur = (VCCSElm) ce;
		FindPathInfo fpi = new FindPathInfo(FindPathInfo.INDUCT, ce,
						    cur.getOutputNode(0));
		if (cur.hasCurrentOutput() && !fpi.findPath(cur.getOutputNode(1))) {
		    cur.broken = true;
		} else
		    cur.broken = false;
	    }
	    
	    // 查找电压源或导线环路。我们对电压源执行此检查
	    if (ce.getPostCount() == 2) {
		if (ce instanceof VoltageElm) {
		    FindPathInfo fpi = new FindPathInfo(FindPathInfo.VOLTAGE, ce,
						    ce.getNode(1));
		    if (fpi.findPath(ce.getNode(0))) {
			stop("Voltage source/wire loop with no resistance!", ce);
			return false;
		    }
		}
	    }

	    // 查找从电源轨到地线的通路
	    if (ce instanceof RailElm || ce instanceof LogicInputElm) {
		FindPathInfo fpi = new FindPathInfo(FindPathInfo.VOLTAGE, ce, ce.getNode(0));
		if (fpi.findPath(0)) {
		    stop("Path to ground with no resistance!", ce);
		    return false;
		}
	    }
	    
	    // 查找短路的电容，或有电压但无电阻的电容
	    if (ce.isIdealCapacitor()) {
		FindPathInfo fpi = new FindPathInfo(FindPathInfo.SHORT, ce,
						    ce.getNode(1));
		if (fpi.findPath(ce.getNode(0))) {
		    console(ce + " shorted");
		    ((CapacitorElm) ce).shorted();
		} else {
		    fpi = new FindPathInfo(FindPathInfo.CAP_V, ce, ce.getNode(1));
		    if (fpi.findPath(ce.getNode(0))) {
			// 理想电容环路；设置一个小的串联电阻，以避免
			// 某个电容上有电压时产生振荡
			((CapacitorElm) ce).setSeriesResistance(.1);

			// 返回 false 以重新填充电路矩阵
			return false;
		    }
		}
	    }
	}
	return true;
    }
    
    // 当电路发生变化时对其进行分析，以便能够进行仿真。
    // 大部分工作已移到 preStampCircuit() 中，这样在仿真停止时可以跳过。
    void analyzeCircuit() {
	stopMessage = null;
	stopElm = null;
	if (elmList.isEmpty()) {
	    postDrawList = new Vector<Point>();
	    badConnectionList = new Vector<Point>();
	    return;
	}
	makePostDrawList();

	needsStamp = true;
    }

    // 完成剩余的 pre-stamp 电路分析
    boolean preStampCircuit(boolean subcircuit) {
	int i, j;
	nodeList = new Vector<CircuitNode>();

	calculateWireClosure();
	setGroundNode(subcircuit);

	// 分配节点和电压源
	makeNodeList();
	
	if (!calcWireInfo())
	    return false;
	nodeMap = null; // 已不再需要此映射
	
	int vscount = 0;
	circuitNonLinear = false;

	// 判断电路是否为非线性，同时设置电压源
	for (i = 0; i != elmList.size(); i++) {
	    CircuitElm ce = getElm(i);
	    if (ce.nonLinear())
		circuitNonLinear = true;
	    int ivs = ce.getVoltageSourceCount();
	    for (j = 0; j != ivs; j++) {
		voltageSources[vscount] = ce;
		ce.setVoltageSource(j, vscount++);
	    }
	}
	voltageSourceCount = vscount;

	// 如果只有一个电压源，则显示其内阻。
	// 这里不能使用 voltageSourceCount，因为它计入内部电压源，例如 GroundElm 中的那个
	boolean gotVoltageSource = false;
	showResistanceInVoltageSources = true;
	for (i = 0; i != elmList.size(); i++) {
	    CircuitElm ce = getElm(i);
	    if (ce instanceof VoltageElm) {
		if (gotVoltageSource)
		    showResistanceInVoltageSources = false;
		else
		    gotVoltageSource = true;
	    }
	}

	findUnconnectedNodes();
	if (!validateCircuit())
	    return false;
	
	nodesWithGroundConnectionCount = nodesWithGroundConnection.size();
	// 仅在校验时需要此列表
	nodesWithGroundConnection = null;
	
	timeStep = maxTimeStep;
	needsStamp = true;
	
	callAnalyzeHook();
	return true;
    }

    // 先进行 pre-stamp 处理，然后填充电路矩阵
    void preStampAndStampCircuit() {
	int i;

	// preStampCircuit 在出错时返回 false。存在电容环路时它也会返回 false，
	// 但那种情况下我们只需重试。重试 10 次以避免无限循环。
	for (i = 0; i != 10; i++)
	    if (preStampCircuit(false) || stopMessage != null)
		break;
	if (stopMessage != null)
	    return;
	if (i == 10) {
	    stop("failed to stamp circuit", null);
	    return;
	}

	stampCircuit();
    }

    // 填充矩阵，即按照仿真电路所需填充矩阵（至少对所有线性元件而言）。
    // 此方法在电路发生变化后调用，也在自动调整时间步长时调用
    void stampCircuit() {
	int i;
	int matrixSize = nodeList.size()-1 + voltageSourceCount;
	circuitMatrix = new double[matrixSize][matrixSize];
	circuitRightSide = new double[matrixSize];
	nodeVoltages = new double[nodeList.size()-1];
	if (lastNodeVoltages == null || lastNodeVoltages.length != nodeVoltages.length)
	    lastNodeVoltages = new double[nodeList.size()-1];
	origMatrix = new double[matrixSize][matrixSize];
	origRightSide = new double[matrixSize];
	circuitMatrixSize = circuitMatrixFullSize = matrixSize;
	circuitRowInfo = new RowInfo[matrixSize];
	circuitPermute = new int[matrixSize];
	for (i = 0; i != matrixSize; i++)
	    circuitRowInfo[i] = new RowInfo();
	circuitNeedsMap = false;
	
	connectUnconnectedNodes();

	// 填充线性电路元件
	for (i = 0; i != elmList.size(); i++) {
	    CircuitElm ce = getElm(i);
	    ce.setParentList(elmList);
	    ce.stamp();
	}

	if (!simplifyMatrix(matrixSize))
	    return;
	
	// 检查是否已调用 stop()
	if (circuitMatrix == null)
	    return;
	
	// 如果矩阵是线性的，我们可以在这里做 lu_factor 分解，
	// 而不必每帧都做
	if (!circuitNonLinear) {
	    if (!lu_factor(circuitMatrix, circuitMatrixSize, circuitPermute)) {
		stop("Singular matrix!", null);
		return;
	    }
	}
	
	// 将 elmList 复制到数组，以避免仿真时大量调用 canCast()
	elmArr = new CircuitElm[elmList.size()];
	int scopeElmCount = 0;
	for (i = 0; i != elmList.size(); i++) {
	    elmArr[i] = elmList.get(i);
	    if (elmArr[i] instanceof ScopeElm)
		scopeElmCount++;
	}
	
	// 将 ScopeElm 复制到数组，以避免仿真时对整个元件列表进行第二次遍历
	scopeElmArr = new ScopeElm[scopeElmCount];
	int j = 0;
	for (i = 0; i != elmList.size(); i++) {
	    if (elmArr[i] instanceof ScopeElm)
		scopeElmArr[j++] = (ScopeElm) elmArr[i];
	}	

	needsStamp = false;
    }

    // 简化矩阵；这能显著加快速度，尤其是对数字电路而言。
    // 至少在我们加入导线移除功能之前是这样的
    boolean simplifyMatrix(int matrixSize) {
	int i, j;
	for (i = 0; i != matrixSize; i++) {
	    int qp = -1;
	    double qv = 0;
	    RowInfo re = circuitRowInfo[i];
	    /*System.out.println("row " + i + " " + re.lsChanges + " " + re.rsChanges + " " +
			       re.dropRow);*/
	    
	    //if (qp != -100) continue;   // uncomment this line to disable matrix simplification for debugging purposes
	    
	    if (re.lsChanges || re.dropRow || re.rsChanges)
		continue;
	    double rsadd = 0;

	    // 看看这一行是否可以被移除
	    for (j = 0; j != matrixSize; j++) {
		double q = circuitMatrix[i][j];
		if (circuitRowInfo[j].type == RowInfo.ROW_CONST) {
		    // 持续累加已经被
		    // 移除的常量值
		    rsadd -= circuitRowInfo[j].value*q;
		    continue;
		}
		// 忽略零元素
		if (q == 0)
		    continue;
		// 记录第一个非零且非 ROW_CONST 的元素
		if (qp == -1) {
		    qp = j;
		    qv = q;
		    continue;
		}
		// 有多个非零元素？放弃
		break;
	    }
	    if (j == matrixSize) {
		if (qp == -1) {
		    // 可能是奇异矩阵，可尝试禁用上面的矩阵简化来检查这一点
		    stop("Matrix error", null);
		    return false;
		}
		RowInfo elt = circuitRowInfo[qp];
		// 我们找到一行只有一个非零非常量元素；该值
		// 就是一个常量
		if (elt.type != RowInfo.ROW_NORMAL) {
		    System.out.println("type already " + elt.type + " for " + qp + "!");
		    continue;
		}
		elt.type = RowInfo.ROW_CONST;
//		console("ROW_CONST " + i + " " + rsadd);
		elt.value = (circuitRightSide[i]+rsadd)/qv;
		circuitRowInfo[i].dropRow = true;
		// 找到引用刚删除元素的第一行
		for (j = 0; j != i; j++)
		    if (circuitMatrix[j][qp] != 0)
			break;
		// 从那一行之前重新开始
		i = j-1;
	    }
	}
	//System.out.println("ac7");

	// 计算新矩阵的大小
	int nn = 0;
	for (i = 0; i != matrixSize; i++) {
	    RowInfo elt = circuitRowInfo[i];
	    if (elt.type == RowInfo.ROW_NORMAL) {
		elt.mapCol = nn++;
		//System.out.println("col " + i + " maps to " + elt.mapCol);
		continue;
	    }
	    if (elt.type == RowInfo.ROW_CONST)
		elt.mapCol = -1;
	}

	// 构建新的简化矩阵
	int newsize = nn;
	double newmatx[][] = new double[newsize][newsize];
	double newrs  []   = new double[newsize];
	int ii = 0;
	for (i = 0; i != matrixSize; i++) {
	    RowInfo rri = circuitRowInfo[i];
	    if (rri.dropRow) {
		rri.mapRow = -1;
		continue;
	    }
	    newrs[ii] = circuitRightSide[i];
	    rri.mapRow = ii;
	    //System.out.println("Row " + i + " maps to " + ii);
	    for (j = 0; j != matrixSize; j++) {
		RowInfo ri = circuitRowInfo[j];
		if (ri.type == RowInfo.ROW_CONST)
		    newrs[ii] -= ri.value*circuitMatrix[i][j];
		else
		    newmatx[ii][ri.mapCol] += circuitMatrix[i][j];
	    }
	    ii++;
	}

//	console("old size = " + matrixSize + " new size = " + newsize);
	
	circuitMatrix = newmatx;
	circuitRightSide = newrs;
	matrixSize = circuitMatrixSize = newsize;
	for (i = 0; i != matrixSize; i++)
	    origRightSide[i] = circuitRightSide[i];
	for (i = 0; i != matrixSize; i++)
	    for (j = 0; j != matrixSize; j++)
		origMatrix[i][j] = circuitMatrix[i][j];
	circuitNeedsMap = true;
	return true;
    }
    
    // 生成需要绘制的端点列表。被 2 个元件共享的端点应隐藏，其余
    // 端点都应绘制。我们不能再为此使用节点列表，因为导线
    // 两端的节点编号相同。
    void makePostDrawList() {
        HashMap<Point,Integer> postCountMap = new HashMap<Point,Integer>();
	int i, j;
	for (i = 0; i != elmList.size(); i++) {
	    CircuitElm ce = getElm(i);
	    int posts = ce.getPostCount();
	    for (j = 0; j != posts; j++) {
		Point pt = ce.getPost(j);
		Integer g = postCountMap.get(pt);
		postCountMap.put(pt, g == null ? 1 : g+1);
	    }
	}

	postDrawList = new Vector<Point>();
	badConnectionList = new Vector<Point>();
	for (Map.Entry<Point, Integer> entry : postCountMap.entrySet()) {
	    if (entry.getValue() != 2)
		postDrawList.add(entry.getKey());
	    
	    // 查找错误连接，即未连接到其他元件、却与其他元件的
	    // 边界框相交的端点
	    if (entry.getValue() == 1) {
		boolean bad = false;
		Point cn = entry.getKey();
		for (j = 0; j != elmList.size() && !bad; j++) {
		    CircuitElm ce = getElm(j);
		    if ( ce instanceof GraphicElm )
			continue;
		    // 这个端点是否与元件的边界框相交？
		    if (!ce.boundingBox.contains(cn.x, cn.y))
			continue;
		    int k;
		    // 这个端点是否属于该元件？
		    int pc = ce.getPostCount();
		    for (k = 0; k != pc; k++)
			if (ce.getPost(k).equals(cn))
			    break;
		    if (k == pc)
			bad = true;
		}
		if (bad)
		    badConnectionList.add(cn);
	    }
	}
    }

    class FindPathInfo {
	static final int INDUCT  = 1;
	static final int VOLTAGE = 2;
	static final int SHORT   = 3;
	static final int CAP_V   = 4;
	boolean visited[];
	int dest;
	CircuitElm firstElm;
	int type;

	// 状态对象，用于在满足各种条件时（取决于 type_）帮助查找电路中的环路
	// elm_ = 起点和终点元件。dest_ = 终点节点。
	FindPathInfo(int type_, CircuitElm elm_, int dest_) {
	    dest = dest_;
	    type = type_;
	    firstElm = elm_;
	    visited  = new boolean[nodeList.size()];
	}

	// 在电路中查找环路：从 firstElm 的节点 n1 出发，寻找一条能回到
	// firstElm 的 dest 节点的路径
	boolean findPath(int n1) {
	    if (n1 == dest)
		return true;

	    // 深度优先搜索，无需再次访问已访问过的节点！
	    if (visited[n1])
		return false;

	    visited[n1] = true;
	    CircuitNode cn = getCircuitNode(n1);
	    int i;
	    if (cn == null)
		return false;
	    for (i = 0; i != cn.links.size(); i++) {
		CircuitNodeLink cnl = cn.links.get(i);
		CircuitElm ce = cnl.elm;
		if (checkElm(n1, ce))
		    return true;
	    }
	    if (n1 == 0) {
		for (i = 0; i != nodesWithGroundConnection.size(); i++)
		    if (checkElm(0, nodesWithGroundConnection.get(i)))
			return true;
	    }
	    return false;
	}
	
	boolean checkElm(int n1, CircuitElm ce) {
		if (ce == firstElm)
		    return false;
		if (type == INDUCT) {
		    // 电感需要一条不含电流源的通路
		    if (ce instanceof CurrentElm)
			return false;
		}
		if (type == VOLTAGE) {
		    // 检查电压环路时，我们只关心电压源/导线/地线
		    if (!(ce.isWireEquivalent() || ce instanceof VoltageElm || ce instanceof GroundElm))
			return false;
		}
		// 检查短路时，只检查导线
		if (type == SHORT && !ce.isWireEquivalent())
		    return false;
		if (type == CAP_V) {
		    // 检查电容/电压源环路
		    if (!(ce.isWireEquivalent() || ce.isIdealCapacitor() || ce instanceof VoltageElm))
			return false;
		}
		if (n1 == 0) {
		    // 查找与地线有连接的端点；
		    // 我们的路径可以经过地线
		    int j;
		    for (j = 0; j != ce.getConnectionNodeCount(); j++)
			if (ce.hasGroundConnection(j) && findPath(ce.getConnectionNode(j)))
			    return true;
		}
		int j;
		for (j = 0; j != ce.getConnectionNodeCount(); j++) {
		    if (ce.getConnectionNode(j) == n1) {
			if (ce.hasGroundConnection(j) && findPath(0))
			    return true;
			if (type == INDUCT && ce instanceof InductorElm) {
			    // 电感可以利用其他电流匹配的电感的路径
			    double c = ce.getCurrent();
			    if (j == 0)
				c = -c;
			    if (Math.abs(c-firstElm.getCurrent()) > 1e-10)
				continue;
			}
			int k;
			for (k = 0; k != ce.getConnectionNodeCount(); k++) {
			    if (j == k)
				continue;
			    if (ce.getConnection(j, k) && findPath(ce.getConnectionNode(k))) {
				//System.out.println("got findpath " + n1);
				return true;
			    }
			}
		    }
		}
	    return false;
	}
    }

    void stop(String s, CircuitElm ce) {
	stopMessage = Locale.LS(s);
	circuitMatrix = null;  // 会导致异常
	stopElm = ce;
	setSimRunning(false);
	analyzeFlag = false;
//	cv.repaint();
    }
    
    // 控制电压源 vs，其电压为 n1 到 n2 之间的电压（必须
    // 同时调用 stampVoltageSource()）
    void stampVCVS(int n1, int n2, double coef, int vs) {
	int vn = nodeList.size()+vs;
	stampMatrix(vn, n1, coef);
	stampMatrix(vn, n2, -coef);
    }
    
    // 填充独立电压源 #vs，从 n1 到 n2，电压值为 v
    void stampVoltageSource(int n1, int n2, int vs, double v) {
	int vn = nodeList.size()+vs;
	stampMatrix(vn, n1, -1);
	stampMatrix(vn, n2, 1);
	stampRightSide(vn, v);
	stampMatrix(n1, vn, 1);
	stampMatrix(n2, vn, -1);
    }

    // 如果电压值将在 doStep() 中通过 updateVoltageSource() 更新，请使用此方法
    void stampVoltageSource(int n1, int n2, int vs) {
	int vn = nodeList.size()+vs;
	stampMatrix(vn, n1, -1);
	stampMatrix(vn, n2, 1);
	stampRightSide(vn);
	stampMatrix(n1, vn, 1);
	stampMatrix(n2, vn, -1);
    }
    
    // 在 doStep() 中更新电压源
    void updateVoltageSource(int n1, int n2, int vs, double v) {
	int vn = nodeList.size()+vs;
	stampRightSide(vn, v);
    }
    
    void stampResistor(int n1, int n2, double r) {
	double r0 = 1/r;
	if (Double.isNaN(r0) || Double.isInfinite(r0)) {
	    System.out.print("bad resistance " + r + " " + r0 + "\n");
	    int a = 0;
	    a /= a;
	}
	stampMatrix(n1, n1, r0);
	stampMatrix(n2, n2, r0);
	stampMatrix(n1, n2, -r0);
	stampMatrix(n2, n1, -r0);
    }

    void stampConductance(int n1, int n2, double r0) {
	stampMatrix(n1, n1, r0);
	stampMatrix(n2, n2, r0);
	stampMatrix(n1, n2, -r0);
	stampMatrix(n2, n1, -r0);
    }

    // 指定从 cn1 到 cn2 的电流等于从 vn1 到 2 的电压除以 g
    void stampVCCurrentSource(int cn1, int cn2, int vn1, int vn2, double g) {
	stampMatrix(cn1, vn1, g);
	stampMatrix(cn2, vn2, g);
	stampMatrix(cn1, vn2, -g);
	stampMatrix(cn2, vn1, -g);
    }

    void stampCurrentSource(int n1, int n2, double i) {
	stampRightSide(n1, -i);
	stampRightSide(n2, i);
    }

    // 填充一个电流源，其 n1 到 n2 的电流取决于流过 vs 的电流
    void stampCCCS(int n1, int n2, int vs, double gain) {
	int vn = nodeList.size()+vs;
	stampMatrix(n1, vn, gain);
	stampMatrix(n2, vn, -gain);
    }

    // 在第 i 行第 j 列填入值 x，含义是：节点 j 上的电压变化
    // dv 将使流入节点 i 的电流增加 x dv。
    //（除非 i 或 j 是电压源节点。）
    void stampMatrix(int i, int j, double x) {
	if (Double.isInfinite(x))
	    debugger();
	if (i > 0 && j > 0) {
	    if (circuitNeedsMap) {
		i = circuitRowInfo[i-1].mapRow;
		RowInfo ri = circuitRowInfo[j-1];
		if (ri.type == RowInfo.ROW_CONST) {
		    //System.out.println("Stamping constant " + i + " " + j + " " + x);
		    circuitRightSide[i] -= x*ri.value;
		    return;
		}
		j = ri.mapCol;
		//System.out.println("stamping " + i + " " + j + " " + x);
	    } else {
		i--;
		j--;
	    }
	    circuitMatrix[i][j] += x;
	}
    }

    // 在第 i 行的右侧填入值 x，表示一个
    // 流入节点 i 的独立电流源
    void stampRightSide(int i, double x) {
	if (i > 0) {
	    if (circuitNeedsMap) {
		i = circuitRowInfo[i-1].mapRow;
		//System.out.println("stamping " + i + " " + x);
	    } else
		i--;
	    circuitRightSide[i] += x;
	}
    }

    // 表示第 i 行右侧的值会在 doStep() 中变化
    void stampRightSide(int i) {
	//System.out.println("rschanges true " + (i-1));
	if (i > 0)
	    circuitRowInfo[i-1].rsChanges = true;
    }
    
    // 表示第 i 行左侧的值会在 doStep() 中变化
    void stampNonLinear(int i) {
	if (i > 0)
	    circuitRowInfo[i-1].lsChanges = true;
    }

    double getIterCount() {
    	// IES - 移除交互
	if (speedBar.getValue() == 0)
	   return 0;

	 return .1*Math.exp((speedBar.getValue()-61)/24.);

    }

    // 如果有人正在示波器中查看导线，我们需要在每次迭代时都计算导线电流。
    // 否则我们可以每帧只计算一次。
    boolean canDelayWireProcessing() {
	int i;
	for (i = 0; i != scopeCount; i++)
	    if (scopes[i].viewingWire())
		return false;
	for (i=0; i != elmList.size(); i++)
	    if (getElm(i) instanceof ScopeElm && ((ScopeElm)getElm(i)).elmScope.viewingWire())
		return false;
	return true;
    }
    
    boolean converged;
    int subIterations;
    
    void runCircuit(boolean didAnalyze) {
	if (circuitMatrix == null || elmList.size() == 0) {
	    circuitMatrix = null;
	    return;
	}
	int iter;
	//int maxIter = getIterCount();
	boolean debugprint = dumpMatrix;
	dumpMatrix = false;
	long steprate = (long) (160*getIterCount());
	long tm = System.currentTimeMillis();
	long lit = lastIterTime;
	if (lit == 0) {
	    lastIterTime = tm;
	    return;
	}
	
	// 检查我们是否不需要运行仿真（针对非常慢的仿真速度）。
	// 如果电路已更改，则至少执行一次迭代，以确保一切一致。
	if (1000 >= steprate*(tm-lastIterTime) && !didAnalyze)
	    return;
	
	boolean delayWireProcessing = canDelayWireProcessing();
	
	int timeStepCountAtFrameStart = timeStepCount;
	
	// 跟踪无收敛问题完成的迭代次数
	int goodIterations = 100;
	
	int frameTimeLimit = (int) (1000/minFrameRate);
	
	for (iter = 1; ; iter++) {
	    if (goodIterations >= 3 && timeStep < maxTimeStep) {
		// 一切顺利，将时间步长加倍
		timeStep = Math.min(timeStep*2, maxTimeStep);
		console("timestep up = " + timeStep + " at " + t);
		stampCircuit();
		goodIterations = 0;
	    }
	    
	    int i, j, subiter;
	    for (i = 0; i != elmArr.length; i++)
		elmArr[i].startIteration();
	    steps++;
	    int subiterCount = (adjustTimeStep && timeStep/2 > minTimeStep) ? 100 : 5000;
	    for (subiter = 0; subiter != subiterCount; subiter++) {
		converged = true;
		subIterations = subiter;
//		if (t % .030 < .002 && timeStep > 1e-6)  // force nonconvergence for debugging
//		    converged = false;
		for (i = 0; i != circuitMatrixSize; i++)
		    circuitRightSide[i] = origRightSide[i];
		if (circuitNonLinear) {
		    for (i = 0; i != circuitMatrixSize; i++)
			for (j = 0; j != circuitMatrixSize; j++)
			    circuitMatrix[i][j] = origMatrix[i][j];
		}
		for (i = 0; i != elmArr.length; i++)
		    elmArr[i].doStep();
		if (stopMessage != null)
		    return;
		boolean printit = debugprint;
		debugprint = false;
		if (circuitMatrixSize < 8) {
		    // 我们仅出于调试目的需要它，因此对大矩阵跳过此检查 
		    for (j = 0; j != circuitMatrixSize; j++) {
			for (i = 0; i != circuitMatrixSize; i++) {
			    double x = circuitMatrix[i][j];
			    if (Double.isNaN(x) || Double.isInfinite(x)) {
				stop("nan/infinite matrix!", null);
				console("circuitMatrix " + i + " " + j + " is " + x);
				return;
			    }
			}
		    }
		}
		if (printit) {
		    for (j = 0; j != circuitMatrixSize; j++) {
			String x = "";
			for (i = 0; i != circuitMatrixSize; i++)
			    x += circuitMatrix[j][i] + ",";
			x += "\n";
			console(x);
		    }
		    console("done");
		}
		if (circuitNonLinear) {
		    // 若已收敛则停止（元件在 doStep() 中检查收敛）
		    if (converged && subiter > 0)
			break;
		    if (!lu_factor(circuitMatrix, circuitMatrixSize,
				  circuitPermute)) {
			stop("Singular matrix!", null);
			return;
		    }
		}
		lu_solve(circuitMatrix, circuitMatrixSize, circuitPermute,
			 circuitRightSide);
		applySolvedRightSide(circuitRightSide);
		if (!circuitNonLinear)
		    break;
	    }
	    if (subiter == subiterCount) {
		// 收敛失败
		goodIterations = 0;
		if (adjustTimeStep) {
		    timeStep /= 2;
		    console("timestep down to " + timeStep + " at " + t);
		}
		if (timeStep < minTimeStep || !adjustTimeStep) {
		    console("convergence failed after " + subiter + " iterations");
		    stop("Convergence failed!", null);
		    break;
		}
		// 我们减小了时间步长。将电路状态重置为迭代开始时的状态
		setNodeVoltages(lastNodeVoltages);
		stampCircuit();
		continue;
	    }
	    if (subiter > 5 || timeStep < maxTimeStep)
		console("converged after " + subiter + " iterations, timeStep = " + timeStep);
	    if (subiter < 3)
		goodIterations++;
	    else
		goodIterations = 0;
	    t += timeStep;
	    timeStepAccum += timeStep;
	    if (timeStepAccum >= maxTimeStep) {
		timeStepAccum -= maxTimeStep;
		timeStepCount++;
	    }
	    for (i = 0; i != elmArr.length; i++)
		elmArr[i].stepFinished();
	    if (!delayWireProcessing)
		calcWireCurrents();
	    for (i = 0; i != scopeCount; i++)
	    	scopes[i].timeStep();
	    for (i=0; i != scopeElmArr.length; i++)
		scopeElmArr[i].stepScope();
	    callTimeStepHook();
	    // 保存上一次的节点电压，以便必要时能重启下一次迭代
	    for (i = 0; i != lastNodeVoltages.length; i++)
		lastNodeVoltages[i] = nodeVoltages[i];
//	    console("set lastrightside at " + t + " " + lastNodeVoltages);
		
	    tm = System.currentTimeMillis();
	    lit = tm;
	    // 检查是否已过去足够的时间，以便在已完成迭代之后
	    // 再执行一次*额外*迭代。但默认将总计算时间限制在 50ms（20fps）内
	    if ((timeStepCount-timeStepCountAtFrameStart)*1000 >= steprate*(tm-lastIterTime) || (tm-lastFrameTime > frameTimeLimit))
		break;
	    if (!simRunning)
		break;
	} // for (iter = 1; ; iter++)
	lastIterTime = lit;
	if (delayWireProcessing)
	    calcWireCurrents();
//	System.out.println((System.currentTimeMillis()-lastFrameTime)/(double) iter);
    }

    // 根据求解矩阵得到的右侧值设置节点电压
    void applySolvedRightSide(double rs[]) {
//	console("setvoltages " + rs);
	int j;
	for (j = 0; j != circuitMatrixFullSize; j++) {
	    RowInfo ri = circuitRowInfo[j];
	    double res = 0;
	    if (ri.type == RowInfo.ROW_CONST)
		res = ri.value;
	    else
		res = rs[ri.mapCol];
	    if (Double.isNaN(res)) {
		converged = false;
		break;
	    }
	    if (j < nodeList.size()-1) {
		nodeVoltages[j] = res;
	    } else {
		int ji = j-(nodeList.size()-1);
		voltageSources[ji].setCurrent(ji, res);
	    }
	}
	
	setNodeVoltages(nodeVoltages);
    }
    
    // 给定节点电压数组，在每个元件中设置节点电压
    void setNodeVoltages(double nv[]) {
	int j, k;
	for (j = 0; j != nv.length; j++) {
	    double res = nv[j];
	    CircuitNode cn = getCircuitNode(j+1);
	    for (k = 0; k != cn.links.size(); k++) {
		CircuitNodeLink cnl = cn.links.elementAt(k);
		cnl.elm.setNodeVoltage(cnl.num, res);
	    }
	}
    }
    
    // 为了加快速度，我们从矩阵中移除了导线。为了显示导线电流，
    // 我们现在需要计算它们。
    void calcWireCurrents() {
	int i;
	
	// 用于调试
	//for (i = 0; i != wireInfoList.size(); i++)
	 //   wireInfoList.get(i).wire.setCurrent(-1, 1.23);
	
	for (i = 0; i != wireInfoList.size(); i++) {
	    WireInfo wi = wireInfoList.get(i);
	    double cur = 0;
	    int j;
	    Point p = wi.wire.getPost(wi.post);
	    for (j = 0; j != wi.neighbors.size(); j++) {
		CircuitElm ce = wi.neighbors.get(j);
		int n = ce.getNodeAtPoint(p.x, p.y);
		cur += ce.getCurrentIntoNode(n);
	    }
	    // 获取正确的电流极性
	    //（LabeledNode 的 wi.post 可能为 1，此时我们需要翻转电流符号）
	    if (wi.post == 0 || (wi.wire instanceof LabeledNodeElm))
		wi.wire.setCurrent(-1, cur);
	    else
		wi.wire.setCurrent(-1, -cur);
	}
    }
    
    int min(int a, int b) { return (a < b) ? a : b; }
    int max(int a, int b) { return (a > b) ? a : b; }
    
    public void resetAction(){
    	int i;
    	analyzeFlag = true;
    	if (t == 0)
    	    setSimRunning(true);
    	t = timeStepAccum = 0;
    	timeStepCount = 0;
    	for (i = 0; i != elmList.size(); i++)
		getElm(i).reset();
	for (i = 0; i != scopeCount; i++)
		scopes[i].resetGraph(true);
    	repaint();
    }

	static native void changeWindowTitle(boolean isCircuitChanged)/*-{
		var newTitle = "CircuitJS1 Desktop Mod";
		var filename = @com.lushprojects.circuitjs1.client.CirSim::fileName;
		var changed = (isCircuitChanged) ? "*" : "";
		if (filename!=null) $doc.title = changed+filename+" - "+newTitle;
		else $doc.title = $wnd.nw.App.manifest.window.title;
	}-*/;

	static native void nodeSave(String path, String dump) /*-{
		var fs = $wnd.nw.require('fs');
		fs.writeFile(path, dump, function(err) {
			if(err) {
						return console.log(err);
					}
			console.log("The file was saved!");
			});
    }-*/;

	static native void nodeSaveAs(String dump, String fileName) /*-{
		var saveasInput = $doc.createElement("input");
		saveasInput.setAttribute('type', 'file');
		saveasInput.setAttribute('nwsaveas', fileName);
		saveasInput.style = "display:none";
		$doc.body.appendChild(saveasInput);
		saveasInput.click();
		saveasInput.addEventListener('cancel', function(){
		// oncancel 事件不起作用。元素不会被删除，但我们仍然可以使用它
		// https://github.com/nwjs/nw.js/issues/7658
			saveasInput.remove()
		});
		saveasInput.addEventListener('change', function(){
			@com.lushprojects.circuitjs1.client.CirSim::filePath = saveasInput.value;
			@com.lushprojects.circuitjs1.client.CirSim::fileName = saveasInput.files[0].name;
			@com.lushprojects.circuitjs1.client.CirSim::lastFileName = saveasInput.files[0].name;
			@com.lushprojects.circuitjs1.client.CirSim::nodeSave(Ljava/lang/String;Ljava/lang/String;)(saveasInput.value, dump);
			console.log(saveasInput.value);
			console.log(saveasInput.files[0].name);
			if (saveasInput.value!=null) $wnd.CircuitJS1.allowSave(true);
			saveasInput.remove();
			@com.lushprojects.circuitjs1.client.CirSim::changeWindowTitle(Z)(false);
		});
    }-*/;
    
    static void electronSaveAsCallback(String s) {
	s = s.substring(s.lastIndexOf('/')+1);
	s = s.substring(s.lastIndexOf('\\')+1);
	theSim.setCircuitTitle(s);
	theSim.allowSave(true);
	theSim.savedFlag = true;
	theSim.repaint();
    }

    static void electronSaveCallback() {
	theSim.savedFlag = true;
	theSim.repaint();
    }
        
    static native void electronSaveAs(String dump) /*-{
        $wnd.showSaveDialog().then(function (file) {
            if (file.canceled)
            	return;
            $wnd.saveFile(file, dump);
            @com.lushprojects.circuitjs1.client.CirSim::electronSaveAsCallback(Ljava/lang/String;)(file.filePath.toString());
        });
    }-*/;

    static native void electronSave(String dump) /*-{
        $wnd.saveFile(null, dump);
        @com.lushprojects.circuitjs1.client.CirSim::electronSaveCallback()();
    }-*/;
    
    static void electronOpenFileCallback(String text, String name) {
	LoadFile.doLoadCallback(text, name);
	theSim.allowSave(true);
    }
    
    static native void electronOpenFile() /*-{
        $wnd.openFile(function (text, name) {
            @com.lushprojects.circuitjs1.client.CirSim::electronOpenFileCallback(Ljava/lang/String;Ljava/lang/String;)(text, name);
        });
    }-*/;
    
    static native void toggleDevTools() /*-{
        $wnd.toggleDevTools();
    }-*/;
    
    static native boolean isElectron() /*-{
        return ($wnd.openFile != undefined);
    }-*/;    

    static native String getElectronStartCircuitText() /*-{
    	return $wnd.startCircuitText;
    }-*/;    
    
    void allowSave(boolean b) {
	if (saveFileItem != null)
	    saveFileItem.setEnabled(b);
    }
    
    public void menuPerformed(String menu, String item) {
	if ((menu=="edit" || menu=="main" || menu=="scopes") && noEditCheckItem.getState()) {
	    Window.alert(Locale.LS("Editing disabled.  Re-enable from the Options menu."));
	    return;
	}
    	if (item=="help")
    	helpDialog = new HelpDialog();
    	if (item=="license")
    	    licenseDialog = new LicenseDialog();
    	if (item=="about")
    		aboutBox = new AboutBox(circuitjs1.versionString);
    	if (item=="modsetup")
    	    modDialog = new ModDialog();
    	if (item=="importfromlocalfile") {
    		pushUndo();
    		loadFileInput.click();
    	}
    	if (item=="newwindow") {
    	    //Window.open(Document.get().getURL(), "_blank", "");
    	    //也许这有助于减少卡顿：
    	    executeJS("nw.Window.open('circuitjs.html', {new_instance: true, mixed_context: false});");
    	}
    	if (item=="save"){
			if (filePath!=null) nodeSave(filePath,dumpCircuit());
			else nodeSaveAs(dumpCircuit(), getLastFileName());
			unsavedChanges = false;
			changeWindowTitle(unsavedChanges);
		}
    	    
    	if (item=="saveas"){
			nodeSaveAs(dumpCircuit(), getLastFileName());
			unsavedChanges = false;
			changeWindowTitle(unsavedChanges);
		}

    	if (item=="importfromtext") {
    		dialogShowing = new ImportFromTextDialog(this);
    	}
    	/*if (item=="importfromdropbox") {
    		dialogShowing = new ImportFromDropboxDialog(this);
    	}*/
    	if (item=="exportasurl") {
    		doExportAsUrl();
    		unsavedChanges = false;
    	}
    	/*if (item=="exportaslocalfile") {
    		doExportAsLocalFile();
    		unsavedChanges = false;
    	}*/
    	if (item=="exportastext") {
    		doExportAsText();
    		unsavedChanges = false;
    	}
    	if (item=="exportasimage")
		doExportAsImage();
    	if (item=="copypng") {
		doImageToClipboard();
    		if (contextPanel!=null)
			contextPanel.hide();
    	}
    	if (item=="exportassvg")
		doExportAsSVG();
    	if (item=="createsubcircuit")
		doCreateSubcircuit();
    	if (item=="dcanalysis")
    	    	doDCAnalysis();
    	if (item=="print")
    	    	doPrint();
    	if (item=="recover")
    	    	doRecover();

    	if ((menu=="elm" || menu=="scopepop") && contextPanel!=null)
    		contextPanel.hide();
    	if (menu=="options" && item=="shortcuts") {
    	    	dialogShowing = new ShortcutsDialog(this);
    	    	dialogShowing.show();
    	}
    	if (menu=="options" && item=="subcircuits") {
    	    	dialogShowing = new SubcircuitDialog(this);
    	    	dialogShowing.show();
    	}
    	if (item=="search") {
    	    	dialogShowing = new SearchDialog(this);
    	    	dialogShowing.show();
    	}
    	if (menu=="options" && item=="other")
    		doEdit(new EditOptions(this));
    	if (item=="devtools")
    	    toggleDevTools();
    	if (item=="undo")
    		doUndo();
    	if (item=="redo")
    		doRedo();
    	
    	// 如果鼠标悬停在某个元件上且按下了快捷键，则对该元件执行操作（将其视为右键菜单项的选择）
    	if (menu == "key" && mouseElm != null) {
    	    menuElm = mouseElm;
    	    menu = "elm";
    	}
	if (menu != "elm")
		menuElm = null;

    	if (item == "cut") {
    		doCut();
    	}
    	if (item == "copy") {
    		doCopy();
    	}
    	if (item=="paste")
    		doPaste(null);
    	if (item=="duplicate") {
    	    	doDuplicate();
    	}
    	if (item=="flip")
    	    doFlip();
    	if (item=="split")
    	    doSplit(menuElm);
    	if (item=="selectAll")
    		doSelectAll();
    	//	if (e.getSource() == exitItem) {
    	//	    destroyFrame();
    	//	    return;
    	//	}
    	
    	if (item=="centrecircuit") {
    		pushUndo();
    		centreCircuit();
    	}
    	if (item=="flipx") {
	    pushUndo();
	    flipX();
    	}
    	if (item=="flipy") {
	    pushUndo();
	    flipY();
    	}
    	if (item=="flipxy") {
	    pushUndo();
	    flipXY();
    	}
    	if (item=="stackAll")
    		stackAll();
    	if (item=="unstackAll")
    		unstackAll();
    	if (item=="combineAll")
		combineAll();
    	if (item=="separateAll")
		separateAll();
    	if (item=="zoomin")
    	    zoomCircuit(20, true);
    	if (item=="zoomout")
    	    zoomCircuit(-20, true);
    	if (item=="zoom100")
    	    setCircuitScale(1, true);
    	if (menu=="elm" && item=="edit")
    		doEdit(menuElm);
    	if (item=="delete") {
    		if (menu!="elm")
    			menuElm = null;
    		pushUndo();
    		doDelete(true);
    	}
    	if (item=="sliders")
    	    doSliders(menuElm);

    	if (item=="viewInScope" && menuElm != null) {
    		int i;
    		for (i = 0; i != scopeCount; i++)
    			if (scopes[i].getElm() == null)
    				break;
    		if (i == scopeCount) {
    			if (scopeCount == scopes.length)
    				return;
    			scopeCount++;
    			scopes[i] = new Scope(this);
    			scopes[i].position = i;
    			//handleResize();
    		}
    		scopes[i].setElm(menuElm);
    		if (i > 0)
    		    scopes[i].speed = scopes[i-1].speed;
    	}
    	
    	if (item=="viewInFloatScope" && menuElm != null) {
    	    ScopeElm newScope = new ScopeElm(snapGrid(menuElm.x+50), snapGrid(menuElm.y+50));
    	    elmList.addElement(newScope);
    	    newScope.setScopeElm(menuElm);
    	    
    	    // 需要重建 scopeElmArr
    	    needAnalyze();
	}
    	
    	if (item.startsWith("addToScope") && menuElm != null) {
    	    int n;
    	    n = Integer.parseInt(item.substring(10));
    	    if (n < scopeCount + countScopeElms()) {
    		if (n < scopeCount )
    		    scopes[n].addElm(menuElm);
    		else
    		    getNthScopeElm(n-scopeCount).elmScope.addElm(menuElm);
    	    }
    	    scopeMenuSelected = -1;
    	}
    	
    	if (menu=="scopepop") {
    		pushUndo();
    		Scope s;
		if (menuScope != -1 )
		    	s= scopes[menuScope];
		else
		    	s= ((ScopeElm)mouseElm).elmScope;

    		if (item=="dock") {
            		if (scopeCount == scopes.length)
            			return;
            		scopes[scopeCount] = ((ScopeElm)mouseElm).elmScope;
            		((ScopeElm)mouseElm).clearElmScope();
            		scopes[scopeCount].position = scopeCount;
            		scopeCount++;
            		doDelete(false);
    		}
    		if (item=="undock") {
		    CircuitElm elm = s.getElm();
    	    	    ScopeElm newScope = new ScopeElm(snapGrid(elm.x+50), snapGrid(elm.y+50));
    	    	    elmList.addElement(newScope);
    	    	    newScope.setElmScope(scopes[menuScope]);
    	    	    
    	    	    int i;
    	    	    // 从列表中移除示波器。setupScopes() 会修正位置
    	    	    for (i = menuScope; i < scopeCount; i++)
    	    		scopes[i] = scopes[i+1];
    	    	    scopeCount--;

    	            needAnalyze();      // 需要重建 scopeElmArr
    		}
    		if (item=="remove")
    		    	s.setElm(null);  // setupScopes() 会清理此项
    		if (item=="removeplot")
			s.removePlot(menuPlot);
    		if (item=="speed2")
    			s.speedUp();
    		if (item=="speed1/2")
    			s.slowDown();
//    		if (item=="scale")
//    			scopes[menuScope].adjustScale(.5);
    		if (item=="maxscale")
    			s.maxScale();
    		if (item=="stack")
    			stackScope(menuScope);
    		if (item=="unstack")
    			unstackScope(menuScope);
    		if (item=="combine")
			combineScope(menuScope);
    		if (item=="selecty")
    			s.selectY();
    		if (item=="reset")
    			s.resetGraph(true);
    		if (item=="properties")
			s.properties();
    		deleteUnusedScopeElms();
    	}
    	if (menu=="circuits" && item.indexOf("setup ") ==0) {
    		pushUndo();
    		int sp = item.indexOf(' ', 6);
    		readSetupFile(item.substring(6, sp), item.substring(sp+1));
    	}
    	if (item=="newblankcircuit") {
    	    pushUndo();
    	    readSetupFile("blank.txt", "Blank Circuit");
    	}
    		
    	//	if (ac.indexOf("setup ") == 0) {
    	//	    pushUndo();
    	//	    readSetupFile(ac.substring(6),
    	//			  ((MenuItem) e.getSource()).getLabel());
    	//	}

    	// IES：从 itemStateChanged() 移来
    	if (menu=="main") {
    		if (contextPanel!=null)
    			contextPanel.hide();
    		//	MenuItem mmi = (MenuItem) mi;
    		//		int prevMouseMode = mouseMode;
    		setMouseMode(MODE_ADD_ELM);
    		String s = item;
    		if (s.length() > 0)
    			mouseModeStr = s;
    		if (s.compareTo("DragAll") == 0)
    			setMouseMode(MODE_DRAG_ALL);
    		else if (s.compareTo("DragRow") == 0)
    			setMouseMode(MODE_DRAG_ROW);
    		else if (s.compareTo("DragColumn") == 0)
    			setMouseMode(MODE_DRAG_COLUMN);
    		else if (s.compareTo("DragSelected") == 0)
    			setMouseMode(MODE_DRAG_SELECTED);
    		else if (s.compareTo("DragPost") == 0)
    			setMouseMode(MODE_DRAG_POST);
    		else if (s.compareTo("Select") == 0)
    			setMouseMode(MODE_SELECT);

		updateToolbar();

    		//		else if (s.length() > 0) {
    		//			try {
    		//				addingClass = Class.forName(s);
    		//			} catch (Exception ee) {
    		//				ee.printStackTrace();
    		//			}
    		//		}
    		//		else
    		//			setMouseMode(prevMouseMode);
    		tempMouseMode = mouseMode;
    	}
    	if (item=="fullscreen") {
    	    if (! Graphics.isFullScreen){
    		Graphics.viewFullScreen();
			setSlidersPanelHeight();
			}
    	    else{
    		Graphics.exitFullScreen();
    	    centreCircuit();
			setSlidersPanelHeight();
			}
    	}
    
	repaint();
    }
    
    int countScopeElms() {
	int c = 0;
	for (int i = 0; i != elmList.size(); i++) {
	    if ( elmList.get(i) instanceof ScopeElm)
		c++;
	}
	return c;
    }
    
    ScopeElm getNthScopeElm(int n) {
	for (int i = 0; i != elmList.size(); i++) {
	    if ( elmList.get(i) instanceof ScopeElm) {
		n--;
		if (n<0)
		    return (ScopeElm) elmList.get(i);
	    }
	}
	return (ScopeElm) null;
    }
    
    
    boolean canStackScope(int s) {
	if (scopeCount < 2) 
	    return false;
	if (s==0)
	    s=1;
    	if (scopes[s].position == scopes[s-1].position)
    	    return false;
	return true;
    }
    
    boolean canCombineScope(int s) {
	return scopeCount >=2;
    }
    
    boolean canUnstackScope(int s) {
	if (scopeCount < 2) 
	    return false;
	if (s==0)
	    s=1;
    	if (scopes[s].position != scopes[s-1].position) {
        	if ( s + 1 < scopeCount && scopes[s+1].position == scopes[s].position) // 允许你通过选择堆栈中最顶部的示波器来取消堆叠
        	    return true;
        	else
        	    return false;
    	}
	return true;
    }

    void stackScope(int s) {
	if (! canStackScope(s) )
	    return;
    	if (s == 0) {
    		s = 1;
    	}
    	scopes[s].position = scopes[s-1].position;
    	for (s++; s < scopeCount; s++)
    		scopes[s].position--;
    	unsavedChanges = true;
    	changeWindowTitle(unsavedChanges);
    }

    void unstackScope(int s) {
	if (! canUnstackScope(s) )
	    return;
    	if (s == 0) {
    		s = 1;
    	}
    	if (scopes[s].position != scopes[s-1].position) // 允许你通过选择堆栈中最顶部的示波器来取消堆叠
    	    s++;
    	for (; s < scopeCount; s++)
    		scopes[s].position++;
    	unsavedChanges = true;
    	changeWindowTitle(unsavedChanges);
    }

    void combineScope(int s) {
	if (! canCombineScope(s))
	    return;
    	if (s == 0) {
    		s = 1;
    	}
    	scopes[s-1].combine(scopes[s]);
    	scopes[s].setElm(null);
    	unsavedChanges = true;
    	changeWindowTitle(unsavedChanges);
    }
    

    void stackAll() {
    	int i;
    	for (i = 0; i != scopeCount; i++) {
    		scopes[i].position = 0;
    		scopes[i].showMax = scopes[i].showMin = false;
    	}
    	unsavedChanges = true;
    	changeWindowTitle(unsavedChanges);
    }

    void unstackAll() {
    	int i;
    	for (i = 0; i != scopeCount; i++) {
    		scopes[i].position = i;
    		scopes[i].showMax = true;
    	}
    	unsavedChanges = true;
    	changeWindowTitle(unsavedChanges);
    }

    void combineAll() {
    	int i;
    	for (i = scopeCount-2; i >= 0; i--) {
    	    scopes[i].combine(scopes[i+1]);
    	    scopes[i+1].setElm(null);
    	}
    	unsavedChanges = true;
    	changeWindowTitle(unsavedChanges);
    }
    
    void separateAll() {
    	int i;
	Scope newscopes[] = new Scope[20];
	int ct = 0;
    	for (i = 0; i < scopeCount; i++)
    	    ct = scopes[i].separate(newscopes, ct);
	scopes = newscopes;
	scopeCount = ct;
	unsavedChanges = true;
	changeWindowTitle(unsavedChanges);
    }

    void doEdit(Editable eable) {
    	clearSelection();
    	pushUndo();
    	if (editDialog != null) {
    //		requestFocus();
    		editDialog.setVisible(false);
    		editDialog = null;
    	}
    	editDialog = new EditDialog(eable, this);
    	editDialog.show();
    }
    
    void doSliders(CircuitElm ce) {
	clearSelection();
	pushUndo();
	dialogShowing = new SliderDialog(ce, this);
	dialogShowing.show();
    }


    void doExportAsUrl()
    {
    	String dump = dumpCircuit();
	dialogShowing = new ExportAsUrlDialog(dump);
	dialogShowing.show();
    }
    
    void doExportAsText()
    {
    	String dump = dumpCircuit();
    	dialogShowing = new ExportAsTextDialog(this, dump);
    	dialogShowing.show();
    }

    void doExportAsImage()
    {
    	dialogShowing = new ExportAsImageDialog(CAC_IMAGE);
    	dialogShowing.show();
    }

    private static native void clipboardWriteImage(CanvasElement cv) /*-{
	cv.toBlob(function(blob) {
	    var promise = parent.navigator.clipboard.write([new ClipboardItem({ "image/png": blob })]);
	    promise.then(function(x) { console.log(x); });
	});
    }-*/;

    void doImageToClipboard()
    {
	Canvas cv = CirSim.theSim.getCircuitAsCanvas(CAC_IMAGE);
	clipboardWriteImage(cv.getCanvasElement());
    }
    
    void doCreateSubcircuit()
    {
    	EditCompositeModelDialog dlg = new EditCompositeModelDialog();
    	if (!dlg.createModel())
    	    return;
    	dlg.createDialog();
    	dialogShowing = dlg;
    	dialogShowing.show();
    }
    /*
    void doExportAsLocalFile() {
    	String dump = dumpCircuit();
    	dialogShowing = new ExportAsLocalFileDialog(dump);
    	dialogShowing.show();
    }
*/
    public void importCircuitFromText(String circuitText, boolean subcircuitsOnly) {
		int flags = subcircuitsOnly ? (CirSim.RC_SUBCIRCUITS | CirSim.RC_RETAIN) : 0;
		if (circuitText != null) {
			readCircuit(circuitText, flags);
			allowSave(false);
			filePath = null;
			fileName = null;
			changeWindowTitle(false);
		}
    }

    String dumpOptions() {
	int f = (dotsCheckItem.getState()) ? 1 : 0;
	f |= (smallGridCheckItem.getState()) ? 2 : 0;
	f |= (voltsCheckItem.getState()) ? 0 : 4;
	f |= (powerCheckItem.getState()) ? 8 : 0;
	f |= (showValuesCheckItem.getState()) ? 0 : 16;
	// 32 = afilter 中的线性刻度
	f |= adjustTimeStep ? 64 : 0;
	String dump = "$ " + f + " " +
	    maxTimeStep + " " + getIterCount() + " " +
	    currentBar.getValue() + " " + CircuitElm.voltageRange + " " +
	    powerBar.getValue() + " " + minTimeStep + "\n";
	return dump;
    }
    
    String dumpCircuit() {
	int i;
	CustomLogicModel.clearDumpedFlags();
	CustomCompositeModel.clearDumpedFlags();
	DiodeModel.clearDumpedFlags();
	TransistorModel.clearDumpedFlags();
	
	String dump = dumpOptions();
		
	for (i = 0; i != elmList.size(); i++) {
	    CircuitElm ce = getElm(i);
	    String m = ce.dumpModel();
	    if (m != null && !m.isEmpty())
		dump += m + "\n";
	    dump += ce.dump() + "\n";
	}
	for (i = 0; i != scopeCount; i++) {
	    String d = scopes[i].dump();
	    if (d != null)
		dump += d + "\n";
	}
	for (i = 0; i != adjustables.size(); i++) {
	    Adjustable adj = adjustables.get(i);
	    dump += "38 " + adj.dump() + "\n";
	}
	if (hintType != -1)
	    dump += "h " + hintType + " " + hintItem1 + " " +
		hintItem2 + "\n";
	return dump;
    }

    void getSetupList(final boolean openDefault) {

    	String url;
    	url = GWT.getModuleBaseURL()+"setuplist.txt"; // +"?v="+random.nextInt();
		RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.GET, url);
		try {
			requestBuilder.sendRequest(null, new RequestCallback() {
				public void onError(Request request, Throwable exception) {
					Window.alert(Locale.LS("Can't load circuit list!"));
					GWT.log("File Error Response", exception);
				}

				public void onResponseReceived(Request request, Response response) {
					// 在此处进行处理
					if (response.getStatusCode()==Response.SC_OK) {
					String text = response.getText();
					processSetupList(text.getBytes(), openDefault);
					// 处理结束
					}
					else { 
						Window.alert(Locale.LS("Can't load circuit list!"));
						GWT.log("Bad file server response:"+response.getStatusText() );
					}
				}
			});
		} catch (RequestException e) {
			GWT.log("failed file reading", e);
		}
    }
		
    void processSetupList(byte b[], final boolean openDefault) {
	int len = b.length;
    	MenuBar currentMenuBar;
    	MenuBar stack[] = new MenuBar[6];
    	int stackptr = 0;
    	currentMenuBar=new MenuBar(true);
    	currentMenuBar.setAutoOpen(true);
    	menuBar.addItem(Locale.LS("Circuits"), currentMenuBar);
		
		MenuBar h = new MenuBar(true);
		helpItem=iconMenuItem("book-open", "User Guide", (Command)null);
		h.addItem(helpItem);
		helpItem.setScheduledCommand(new MyCommand("file","help"));
		licenseItem=iconMenuItem("license", "License",(Command)null);
		h.addItem(licenseItem);
		licenseItem.setScheduledCommand(new MyCommand("file","license"));
		aboutItem = iconMenuItem("info-circled", "About...", (Command)null);
		h.addItem(aboutItem);
		aboutItem.setScheduledCommand(new MyCommand("file","about"));
		h.addSeparator();
		h.addItem(aboutCircuitsItem = iconMenuItem("link", "About Circuits",
		new Command() {
			public void execute(){
				executeJS("nw.Shell.openExternal('https://www.falstad.com/circuit/e-index.html')");
			}
		}));
		h.addItem(aboutCircuitsPLItem = iconMenuItem("link", "About Circuits (Polish ver.)",
		new Command() {
			public void execute(){
				executeJS("nw.Shell.openExternal('https://www.falstad.com/circuit/polish/e-index.html');");
			}
		}));

		menuBar.addItem(Locale.LS("Help"), h);
		
    	stack[stackptr++] = currentMenuBar;
    	int p;
    	for (p = 0; p < len; ) {
    		int l;
    		for (l = 0; l != len-p; l++)
    			if (b[l+p] == '\n' || b[l+p] == '\r') {
    				l++;
    				break;
    			}
    		String line = new String(b, p, l-1);
    		if (line.isEmpty() || line.charAt(0) == '#')
    			;
    		else if (line.charAt(0) == '+') {
    		//	MenuBar n = new Menu(line.substring(1));
    			MenuBar n = new MenuBar(true);
    			n.setAutoOpen(true);
    			currentMenuBar.addItem(Locale.LS(line.substring(1)),n);
    			currentMenuBar = stack[stackptr++] = n;
    		} else if (line.charAt(0) == '-') {
    			currentMenuBar = stack[--stackptr-1];
    		} else {
    			int i = line.indexOf(' ');
    			if (i > 0) {
    				String title = Locale.LS(line.substring(i+1));
    				boolean first = false;
    				if (line.charAt(0) == '>')
    					first = true;
    				String file = line.substring(first ? 1 : 0, i);
    				currentMenuBar.addItem(new MenuItem(title,
    					new MyCommand("circuits", "setup "+file+" " + title)));
    				if (file.equals(startCircuit) && startLabel == null) {
    				    startLabel = title;
    				    titleLabel.setText(title);
    				    setSlidersPanelHeight();
    				}
    				if (first && startCircuit == null) {
    					startCircuit = file;
    					startLabel = title;
    					if (openDefault && stopMessage == null)
    						readSetupFile(startCircuit, startLabel);
    				}
    			}
    		}
    		p += l;
    	}
}

    void readCircuit(String text, int flags) {
	readCircuit(text.getBytes(), flags);
	if ((flags & RC_KEEP_TITLE) == 0)
	    titleLabel.setText(null);
	    setSlidersPanelHeight();
    }

    void readCircuit(String text) {
	readCircuit(text.getBytes(), 0);
	titleLabel.setText(null);
	setSlidersPanelHeight();
    }

    void setCircuitTitle(String s) {
	if (s != null)
	    titleLabel.setText(s);
	    setSlidersPanelHeight();
    }
    
	void readSetupFile(String str, String title) {
		System.out.println(str);
		// 这里不要禁用缓存，没必要，而且会使离线 PWA 无法工作
		String url=GWT.getModuleBaseURL()+"circuits/"+str; // +"?v="+random.nextInt(); 
		loadFileFromURL(url);
		if (title != null)
		    titleLabel.setText(title);
		    setSlidersPanelHeight();
		unsavedChanges = false;
		filePath = null;
		fileName = null;
		changeWindowTitle(unsavedChanges);
	}
	
	void loadFileFromURL(String url) {
	    RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.GET, url);
	    
	    try {
		requestBuilder.sendRequest(null, new RequestCallback() {
		    public void onError(Request request, Throwable exception) {
			Window.alert(Locale.LS("Can't load circuit!"));
			GWT.log("File Error Response", exception);
		    }

		    public void onResponseReceived(Request request, Response response) {
			if (response.getStatusCode()==Response.SC_OK) {
			    String text = response.getText();
			    readCircuit(text, RC_KEEP_TITLE);
			    allowSave(false);
			    unsavedChanges = false;
				filePath = null;
				fileName = null;
				changeWindowTitle(unsavedChanges);
			}
			else { 
			    Window.alert(Locale.LS("Can't load circuit!"));
			    GWT.log("Bad file server response:"+response.getStatusText() );
			}
		    }
		});
	    } catch (RequestException e) {
		GWT.log("failed file reading", e);
	    }

	}

    static final int RC_RETAIN = 1;
    static final int RC_NO_CENTER = 2;
    static final int RC_SUBCIRCUITS = 4;
    static final int RC_KEEP_TITLE = 8;

    void readCircuit(byte b[], int flags) {
	int i;
	int len = b.length;
	if ((flags & RC_RETAIN) == 0) {
	    clearMouseElm();
	    for (i = 0; i != elmList.size(); i++) {
		CircuitElm ce = getElm(i);
		ce.delete();
	    }
	    t = timeStepAccum = 0;
	    elmList.removeAllElements();
	    hintType = -1;
	    maxTimeStep = 5e-6;
	    minTimeStep = 50e-12;
	    dotsCheckItem.setState(false);
	    smallGridCheckItem.setState(false);
	    powerCheckItem.setState(false);
	    voltsCheckItem.setState(true);
	    showValuesCheckItem.setState(true);
	    setGrid();
	    speedBar.setValue(117); // 57
	    currentBar.setValue(50);
	    powerBar.setValue(50);
	    CircuitElm.voltageRange = 5;
	    scopeCount = 0;
	    lastIterTime = 0;
	}
	boolean subs = (flags & RC_SUBCIRCUITS) != 0;
	//cv.repaint();
	int p;
	for (p = 0; p < len; ) {
	    int l;
	    int linelen = len-p; // IES - 修改为允许最后一行不以分隔符结尾。
	    for (l = 0; l != len-p; l++)
		if (b[l+p] == '\n' || b[l+p] == '\r') {
		    linelen = l++;
		    if (l+p < b.length && b[l+p] == '\n')
			l++;
		    break;
		}
	    String line = new String(b, p, linelen);
	    StringTokenizer st = new StringTokenizer(line, " +\t\n\r\f");
	    while (st.hasMoreTokens()) {
		String type = st.nextToken();
		int tint = type.charAt(0);
		try {
		    if (subs && tint != '.')
			continue;
		    if (tint == 'o') {
			Scope sc = new Scope(this);
			sc.position = scopeCount;
			sc.undump(st);
			scopes[scopeCount++] = sc;
			break;
		    }
		    if (tint == 'h') {
			readHint(st);
			break;
		    }
		    if (tint == '$') {
			readOptions(st, flags);
			break;
		    }
		    if (tint == '!') {
			CustomLogicModel.undumpModel(st);
			break;
		    }
		    if (tint == '%' || tint == '?' || tint == 'B') {
			// 忽略 afilter 特有的内容
			break;
		    }
		    // 未经“导出为链接”测试，不要在此添加新符号
		    
		    // 如果第一个字符是数字，则将类型解析为数字
		    if (tint >= '0' && tint <= '9')
			tint = new Integer(type).intValue();
		    
		    if (tint == 34) {
			DiodeModel.undumpModel(st);
			break;
		    }
		    if (tint == 32) {
			TransistorModel.undumpModel(st);
			break;
		    }
		    if (tint == 38) {
			Adjustable adj = new Adjustable(st, this);
			if (adj.elm != null)
			    adjustables.add(adj);
			break;
		    }
		    if (tint == '.') {
			CustomCompositeModel.undumpModel(st);
			break;
		    }
		    int x1 = new Integer(st.nextToken()).intValue();
		    int y1 = new Integer(st.nextToken()).intValue();
		    int x2 = new Integer(st.nextToken()).intValue();
		    int y2 = new Integer(st.nextToken()).intValue();
		    int f  = new Integer(st.nextToken()).intValue();
		    
		    CircuitElm newce = createCe(tint, x1, y1, x2, y2, f, st);
		    if (newce==null) {
			System.out.println("unrecognized dump type: " + type);
			break;
		    }
		    /*
		     * 调试代码，用于检查构造函数中是否调用了 allocNodes()。它在
		     * setPoints() 中被调用，但 setPoints() 不会为子电路调用。
		    double vv[] = newce.volts;
		    int vc = newce.getPostCount() + newce.getInternalNodeCount();
		    if (vv.length != vc)
			console("allocnodes not called! " + tint);
		     */
		    newce.setPoints();
		    elmList.addElement(newce);
		} catch (Exception ee) {
		    ee.printStackTrace();
		    console("exception while undumping " + ee);
		    break;
		}
		break;
	    }
	    p += l;
	    
	}
	setPowerBarEnable();
	enableItems();
	if ((flags & RC_RETAIN) == 0) {
	    // 按需创建滑块
	    for (i = 0; i < adjustables.size(); i++) {
		if (!adjustables.get(i).createSlider(this))
		    adjustables.remove(i--);
	    }
	}
//	if (!retain)
	//    handleResize(); // for scopes
	needAnalyze();
	if ((flags & RC_NO_CENTER) == 0)
		centreCircuit();
	if ((flags & RC_SUBCIRCUITS) != 0)
	    updateModels();
	
	AudioInputElm.clearCache();  // 以节省内存
	DataInputElm.clearCache();  // 以节省内存
    }

    // 删除某个元件的滑块
    void deleteSliders(CircuitElm elm) {
	int i;
	if (adjustables == null)
	    return;
	for (i = adjustables.size()-1; i >= 0; i--) {
	    Adjustable adj = adjustables.get(i);
	    if (adj.elm == elm) {
		adj.deleteSlider(this);
		adjustables.remove(i);
	    }
	}
    }
    
    void readHint(StringTokenizer st) {
	hintType  = new Integer(st.nextToken()).intValue();
	hintItem1 = new Integer(st.nextToken()).intValue();
	hintItem2 = new Integer(st.nextToken()).intValue();
    }

    void readOptions(StringTokenizer st, int importFlags) {
	int flags = new Integer(st.nextToken()).intValue();
	
	if ((importFlags & RC_RETAIN) != 0) {
            // 如果粘贴的电路使用小网格，则需要设置小网格
	    if ((flags & 2) != 0)
		smallGridCheckItem.setState(true);
	    return;
	}
	
	dotsCheckItem.setState((flags & 1) != 0);
	smallGridCheckItem.setState((flags & 2) != 0);
	voltsCheckItem.setState((flags & 4) == 0);
	powerCheckItem.setState((flags & 8) == 8);
	showValuesCheckItem.setState((flags & 16) == 0);
	adjustTimeStep = (flags & 64) != 0;
	maxTimeStep = timeStep = new Double (st.nextToken()).doubleValue();
	double sp = new Double(st.nextToken()).doubleValue();
	int sp2 = (int) (Math.log(10*sp)*24+61.5);
	//int sp2 = (int) (Math.log(sp)*24+1.5);
	speedBar.setValue(sp2);
	currentBar.setValue(new Integer(st.nextToken()).intValue());
	CircuitElm.voltageRange = new Double (st.nextToken()).doubleValue();

	try {
	    powerBar.setValue(new Integer(st.nextToken()).intValue());
	    minTimeStep = Double.parseDouble(st.nextToken());
	} catch (Exception e) {
	}
	setGrid();
    }
    
    int snapGrid(int x) {
	return (x+gridRound) & gridMask;
    }

	boolean doSwitch(int x, int y) {
		if (mouseElm == null || !(mouseElm instanceof SwitchElm))
			return false;
		SwitchElm se = (SwitchElm) mouseElm;
		if (!se.getSwitchRect().contains(x, y))
		    return false;
		se.toggle();
		if (se.momentary)
		    heldSwitchElm = se;
		if (!(se instanceof LogicInputElm))
		    needAnalyze();
		unsavedChanges = true;
		changeWindowTitle(unsavedChanges);
		return true;
	}

    int locateElm(CircuitElm elm) {
	int i;
	for (i = 0; i != elmList.size(); i++)
	    if (elm == elmList.elementAt(i))
		return i;
	return -1;
    }
    
    public void mouseDragged(MouseMoveEvent e) {
    	// 忽略不带修饰键的鼠标右键（在 PC 上需要）
    	if (e.getNativeButton()==NativeEvent.BUTTON_RIGHT) {
    		if (!(e.isMetaKeyDown() ||
    				e.isShiftKeyDown() ||
    				e.isControlKeyDown() ||
    				e.isAltKeyDown()))
    			return;
    	}
    	
    	if (tempMouseMode==MODE_DRAG_SPLITTER) {
    		dragSplitter(e.getX(), e.getY());
    		return;
    	}
    	int gx = inverseTransformX(e.getX());
    	int gy = inverseTransformY(e.getY());
    	if (!circuitArea.contains(e.getX(), e.getY()))
    	    return;
    	boolean changed = false;
    	if (dragElm != null)
    	    dragElm.drag(gx, gy);
    	boolean success = true;
    	switch (tempMouseMode) {
    	case MODE_DRAG_ALL:
    		dragAll(e.getX(), e.getY());
    		break;
    	case MODE_DRAG_ROW:
    		dragRow(snapGrid(gx), snapGrid(gy));
    		changed = true;
    		break;
    	case MODE_DRAG_COLUMN:
		dragColumn(snapGrid(gx), snapGrid(gy));
    		changed = true;
    		break;
    	case MODE_DRAG_POST:
    		if (mouseElm != null) {
    		    dragPost(snapGrid(gx), snapGrid(gy), e.isShiftKeyDown());
    		    changed = true;
    		}
    		break;
    	case MODE_SELECT:
    		if (mouseElm == null)
    		    selectArea(gx, gy, e.isShiftKeyDown());
    		else if (!noEditCheckItem.getState()) {
    		    // 拖动前稍作延迟。这是为了修复在移动设备上点按时
    		    // 开关被意外拖动的问题
    		    if (System.currentTimeMillis()-mouseDownTime < 150)
    			return;
    		
    		    tempMouseMode = MODE_DRAG_SELECTED;
    		    changed = success = dragSelected(gx, gy);
    		}
    		break;
    	case MODE_DRAG_SELECTED:
    		changed = success = dragSelected(gx, gy);
    		break;

    	}
    	dragging = true;
    	if (success) {
    	    dragScreenX = e.getX();
    	    dragScreenY = e.getY();
    //	    console("setting dragGridx in mousedragged");
    	    dragGridX = inverseTransformX(dragScreenX);
    	    dragGridY = inverseTransformY(dragScreenY);
    	    if (!(tempMouseMode == MODE_DRAG_SELECTED && onlyGraphicsElmsSelected())) {
    		dragGridX = snapGrid(dragGridX);
    		dragGridY = snapGrid(dragGridY);
    	    }
   	}
    	if (changed){
    	    writeRecoveryToStorage();
    	    unsavedChanges = true;
    	    changeWindowTitle(unsavedChanges);
		}

    	repaint();
    }
    
    void dragSplitter(int x, int y) {
    	double h = (double) canvasHeight;
    	if (h<1)
    		h=1;
    	scopeHeightFraction=1.0-(((double)y)/h);
    	if (scopeHeightFraction<0.1)
    		scopeHeightFraction=0.1;
    	if (scopeHeightFraction>0.9)
    		scopeHeightFraction=0.9;
    	setCircuitArea();
    	repaint();
    }

    void dragAll(int x, int y) {
    	int dx = x-dragScreenX;
    	int dy = y-dragScreenY;
    	if (dx == 0 && dy == 0)
    		return;
    	transform[4] += dx;
    	transform[5] += dy;
    	dragScreenX = x;
    	dragScreenY = y;
    }

    void dragRow(int x, int y) {
    	int dy = y-dragGridY;
    	if (dy == 0)
    		return;
    	int i;
    	for (i = 0; i != elmList.size(); i++) {
    		CircuitElm ce = getElm(i);
    		if (ce.y  == dragGridY)
    			ce.movePoint(0, 0, dy);
    		if (ce.y2 == dragGridY)
    			ce.movePoint(1, 0, dy);
    	}
    	removeZeroLengthElements();
    }

    void dragColumn(int x, int y) {
    	int dx = x-dragGridX;
    	if (dx == 0)
    		return;
    	int i;
    	for (i = 0; i != elmList.size(); i++) {
    		CircuitElm ce = getElm(i);
    		if (ce.x  == dragGridX)
    			ce.movePoint(0, dx, 0);
    		if (ce.x2 == dragGridX)
    			ce.movePoint(1, dx, 0);
    	}
    	removeZeroLengthElements();
    }

    boolean onlyGraphicsElmsSelected() {
	if (mouseElm!=null && !(mouseElm instanceof GraphicElm))
	    return false;
    	int i;
    	for (i = 0; i != elmList.size(); i++) {
    	    CircuitElm ce = getElm(i);
    	    if ( ce.isSelected() && !(ce instanceof GraphicElm) )
    		return false;
    	}
    	return true;
    }
    
    boolean dragSelected(int x, int y) {
    	boolean me = false;
    	int i;
    	if (mouseElm != null && !mouseElm.isSelected())
    	    mouseElm.setSelected(me = true);

    	if (! onlyGraphicsElmsSelected()) {
    //	    console("Snapping x and y");
    	    x = snapGrid(x);
    	    y = snapGrid(y);
    	}

    	int dx = x-dragGridX;
  //  	console("dx="+dx+"dragGridx="+dragGridX);
    	int dy = y-dragGridY;
    	if (dx == 0 && dy == 0) {
    	    // 如果上面选中了 mouseElm，则不要让它保持选中状态
    	    if (me)
    		mouseElm.setSelected(false);
    	    return false;
    	}
    	boolean allowed = true;

    	// 检查移动是否允许
    	for (i = 0; allowed && i != elmList.size(); i++) {
    	    CircuitElm ce = getElm(i);
    	    if (ce.isSelected() && !ce.allowMove(dx, dy))
    		allowed = false;
    	}

    	if (allowed) {
    	    for (i = 0; i != elmList.size(); i++) {
    		CircuitElm ce = getElm(i);
    		if (ce.isSelected())
    		    ce.move(dx, dy);
    	    }
    	    needAnalyze();
    	}

    	// 如果上面选中了 mouseElm，则不要让它保持选中状态
    	if (me)
    		mouseElm.setSelected(false);

    	return allowed;
    }

    void dragPost(int x, int y, boolean all) {
    	if (draggingPost == -1) {
    		draggingPost =
    				(Graphics.distanceSq(mouseElm.x , mouseElm.y , x, y) >
    				Graphics.distanceSq(mouseElm.x2, mouseElm.y2, x, y)) ? 1 : 0;
    	}
    	int dx = x-dragGridX;
    	int dy = y-dragGridY;
    	if (dx == 0 && dy == 0)
    		return;
    	
    	if (all) {
    	    // 遍历所有元件
    	    int i;
    	    for (i = 0; i != elmList.size(); i++) {
    		CircuitElm e = elmList.get(i);
    		
    		// 我们要移动哪个端点？
    		int p = 0;
    		if (e.x == dragGridX && e.y == dragGridY)
    		    p = 0;
    		else if (e.x2 == dragGridX && e.y2 == dragGridY)
    		    p = 1;
    		else
    		    continue;
    		e.movePoint(p, dx, dy);
    	    }
    	} else
    	    mouseElm.movePoint(draggingPost, dx, dy);
    	needAnalyze();
    }

    void doFlip() {
	menuElm.flipPosts();
    	needAnalyze();
    }
    
    void doSplit(CircuitElm ce) {
	int x = snapGrid(inverseTransformX(menuX));
	int y = snapGrid(inverseTransformY(menuY));
	if (ce == null || !(ce instanceof WireElm))
	    return;
	if (ce.x == ce.x2)
	    x = ce.x;
	else
	    y = ce.y;
	
	// 不要创建零长度导线
	if (x == ce.x && y == ce.y || x == ce.x2 && y == ce.y2)
	    return;
	
	WireElm newWire = new WireElm(x, y);
	newWire.drag(ce.x2, ce.y2);
	ce.drag(x, y);
	elmList.addElement(newWire);
	needAnalyze();
    }
    
    void selectArea(int x, int y, boolean add) {
    	int x1 = min(x, initDragGridX);
    	int x2 = max(x, initDragGridX);
    	int y1 = min(y, initDragGridY);
    	int y2 = max(y, initDragGridY);
    	selectedArea = new Rectangle(x1, y1, x2-x1, y2-y1);
    	int i;
    	for (i = 0; i != elmList.size(); i++) {
    		CircuitElm ce = getElm(i);
    		ce.selectRect(selectedArea, add);
    	}
	enableDisableMenuItems();
    }

    void enableDisableMenuItems() {
	boolean canFlipX = true;
	boolean canFlipY = true;
	boolean canFlipXY = true;
	int selCount = countSelected();
	for (CircuitElm elm : elmList)
	    if (elm.isSelected() || selCount == 0) {
		if (!elm.canFlipX())
		    canFlipX = false;
		if (!elm.canFlipY())
		    canFlipY = false;
		if (!elm.canFlipXY())
		    canFlipXY = false;
	    }
	cutItem.setEnabled(selCount > 0);
	copyItem.setEnabled(selCount > 0);
	flipXItem.setEnabled(canFlipX);
	flipYItem.setEnabled(canFlipY);
	flipXYItem.setEnabled(canFlipXY);
    }

    void setMouseElm(CircuitElm ce) {
    	if (ce!=mouseElm) {
    		if (mouseElm!=null)
    			mouseElm.setMouseElm(false);
    		if (ce!=null)
    			ce.setMouseElm(true);
    		mouseElm=ce;
    		int i;
    		for (i = 0; i < adjustables.size(); i++)
    		    adjustables.get(i).setMouseElm(ce);
    	}
    }

    void removeZeroLengthElements() {
    	int i;
    	boolean changed = false;
    	for (i = elmList.size()-1; i >= 0; i--) {
    		CircuitElm ce = getElm(i);
    		if (ce.x == ce.x2 && ce.y == ce.y2) {
    			elmList.removeElementAt(i);
    			ce.delete();
    			changed = true;
    		}
    	}
    	needAnalyze();
    }
    
    boolean mouseIsOverSplitter(int x, int y) {
    	boolean isOverSplitter;
    	if (scopeCount == 0)
    	    return false;
    	isOverSplitter =((x>=0) && (x<circuitArea.width) && 
    			(y>=circuitArea.height-5) && (y<circuitArea.height));
    	if (isOverSplitter!=mouseWasOverSplitter){
    		if (isOverSplitter)
    			setCursorStyle("cursorSplitter");
    		else
    			setMouseMode(mouseMode);
    	}
    	mouseWasOverSplitter=isOverSplitter;
    	return isOverSplitter;
    }

    public void onMouseMove(MouseMoveEvent e) {
    	e.preventDefault();
    	mouseCursorX=e.getX();
    	mouseCursorY=e.getY();
    	if (mouseDragging) {
    		mouseDragged(e);
    		return;
    	}
    	mouseSelect(e);
    	scopeMenuSelected = -1;
    }
    
    // 通过反转电路变换，将屏幕坐标转换为网格坐标
    int inverseTransformX(double x) {
	return (int) ((x-transform[4])/transform[0]);
    }

    int inverseTransformY(double y) {
	return (int) ((y-transform[5])/transform[3]);
    }
    
    // 将网格坐标转换为屏幕坐标
    int transformX(double x) {
	return (int) ((x*transform[0]) + transform[4]);
    }
    
    int transformY(double y) {
	return (int) ((y*transform[3]) + transform[5]);
    }
    
    

    // 需要把它拆分为一个单独的过程来处理选择，
    // 因为在移动设备上我们收不到鼠标移动事件
    public void mouseSelect(MouseEvent<?> e) {
    	//	以下内容在原始版本中存在，但在 GWT 中似乎不工作/不需要
    	//    	if (e.getNativeButton()==NativeEvent.BUTTON_LEFT)
    	//	    return;
    	CircuitElm newMouseElm=null;
    	mouseCursorX=e.getX();
    	mouseCursorY=e.getY();
    	int sx = e.getX();
    	int sy = e.getY();
    	int gx = inverseTransformX(sx);
    	int gy = inverseTransformY(sy);
   // 	console("Settingd draggridx in mouseEvent");
    	dragGridX = snapGrid(gx);
    	dragGridY = snapGrid(gy);
    	dragScreenX = sx;
    	dragScreenY = sy;
    	draggingPost = -1;
    	int i;
    	//	CircuitElm origMouse = mouseElm;

    	mousePost = -1;
    	plotXElm = plotYElm = null;
    	
    	if (mouseIsOverSplitter(sx, sy)) {
    		setMouseElm(null);
    		return;
    	}
    	
    	if (circuitArea.contains(sx, sy)) {
    	    if (mouseElm!=null && ( mouseElm.getHandleGrabbedClose(gx, gy, POSTGRABSQ, MINPOSTGRABSIZE)>=0)) {
    		newMouseElm=mouseElm;
    	    } else {
    		int bestDist = 100000000;
    		for (i = 0; i != elmList.size(); i++) {
		    CircuitElm ce = getElm(i);
		    if (ce.boundingBox.contains(gx, gy)) {
			int dist = ce.getMouseDistance(gx, gy);
			if (dist >= 0 && dist < bestDist) {
			    bestDist = dist;
			    newMouseElm = ce;
			}
		    }
    		} // for
    	    }
    	}
    	scopeSelected = -1;
    	if (newMouseElm == null) {
    	    for (i = 0; i != scopeCount; i++) {
    		Scope s = scopes[i];
    		if (s.rect.contains(sx, sy)) {
    		    newMouseElm=s.getElm();
    		    if (s.plotXY) {
    			plotXElm = s.getXElm();
    			plotYElm = s.getYElm();
    		    }
    		    scopeSelected = i;
    		}
    	    }
    		//	    // 鼠标指针不在任何边界框内，但我们
    		//	    // 可能仍然靠近某个端点
    		for (i = 0; i != elmList.size(); i++) {
    			CircuitElm ce = getElm(i);
    			if (mouseMode==MODE_DRAG_POST ) {
    				if (ce.getHandleGrabbedClose(gx, gy, POSTGRABSQ, 0)> 0)
    				{
    					newMouseElm = ce;
    					break;
    				}
    			}
    			int j;
    			int jn = ce.getPostCount();
    			for (j = 0; j != jn; j++) {
    				Point pt = ce.getPost(j);
    				//   int dist = Graphics.distanceSq(x, y, pt.x, pt.y);
    				if (Graphics.distanceSq(pt.x, pt.y, gx, gy) < 26) {
    					newMouseElm = ce;
    					mousePost = j;
    					break;
    				}
    			}
    		}
    	} else {
    		mousePost = -1;
    		// 查找靠近鼠标指针的端点
    		for (i = 0; i != newMouseElm.getPostCount(); i++) {
    			Point pt = newMouseElm.getPost(i);
    			if (Graphics.distanceSq(pt.x, pt.y, gx, gy) < 26)
    				mousePost = i;
    		}
    	}
    	repaint();
    	setMouseElm(newMouseElm);
    }



    public void onContextMenu(ContextMenuEvent e) {
    	e.preventDefault();
    	if (!dialogIsShowing()) {
        	menuClientX = e.getNativeEvent().getClientX();
        	menuClientY = e.getNativeEvent().getClientY();
        	doPopupMenu();
    	}
    }
    
    @SuppressWarnings("deprecation")
    void doPopupMenu() {
	if (noEditCheckItem.getState() || dialogIsShowing())
	    return;
    	menuElm = mouseElm;
    	menuScope=-1;
    	menuPlot=-1;
    	int x, y;
    	if (scopeSelected!=-1) {
    	    	if (scopes[scopeSelected].canMenu()) {
    	    	    menuScope=scopeSelected;
    	    	    menuPlot=scopes[scopeSelected].selectedPlot;
    	    	    scopePopupMenu.doScopePopupChecks(false, canStackScope(scopeSelected), canCombineScope(scopeSelected), 
    	    		    canUnstackScope(scopeSelected), scopes[scopeSelected]);
    	    	    contextPanel=new PopupPanel(true);
    	    	    contextPanel.add(scopePopupMenu.getMenuBar());
    	    	    y=Math.max(0, Math.min(menuClientY,canvasHeight-160));
    	    	    contextPanel.setPopupPosition(menuClientX, y);
    	    	    contextPanel.show();
    		}
    	} else if (mouseElm != null) {
    	    	if (! (mouseElm instanceof ScopeElm)) {
    	    	    elmScopeMenuItem.setEnabled(mouseElm.canViewInScope());
    	    	    elmFloatScopeMenuItem.setEnabled(mouseElm.canViewInScope());
    	    	    if ((scopeCount + countScopeElms()) <= 1) {
    	    		elmAddScopeMenuItem.setCommand(new MyCommand("elm", "addToScope0"));
    	    		elmAddScopeMenuItem.setSubMenu(null);
    	    	    	elmAddScopeMenuItem.setEnabled(mouseElm.canViewInScope() && (scopeCount + countScopeElms())> 0);
    	    	    }
    	    	    else {
    	    		composeSelectScopeMenu(selectScopeMenuBar);
    	    		elmAddScopeMenuItem.setCommand(null);
    	    		elmAddScopeMenuItem.setSubMenu(selectScopeMenuBar);
    	    	    	elmAddScopeMenuItem.setEnabled(mouseElm.canViewInScope() );
    	    	    }
    	    	    elmEditMenuItem .setEnabled(mouseElm.getEditInfo(0) != null);
		    elmSwapMenuItem .setEnabled(mouseElm.getPostCount() == 2);
    	    	    elmSplitMenuItem.setEnabled(canSplit(mouseElm));
    	    	    elmSliderMenuItem.setEnabled(sliderItemEnabled(mouseElm));
		    boolean canFlipX = mouseElm.canFlipX();
		    boolean canFlipY = mouseElm.canFlipY();
		    boolean canFlipXY = mouseElm.canFlipXY();
		    for (CircuitElm elm : elmList)
			if (elm.isSelected()) {
			    if (!elm.canFlipX())
				canFlipX = false;
			    if (!elm.canFlipY())
				canFlipY = false;
			    if (!elm.canFlipXY())
				canFlipXY = false;
			}
    	    	    elmFlipXMenuItem.setEnabled(canFlipX);
    	    	    elmFlipYMenuItem.setEnabled(canFlipY);
    	    	    elmFlipXYMenuItem.setEnabled(canFlipXY);
    	    	    contextPanel=new PopupPanel(true);
    	    	    contextPanel.add(elmMenuBar);
    	    	    contextPanel.setPopupPosition(menuClientX, menuClientY);
    	    	    contextPanel.show();
    	    	} else {
    	    	    ScopeElm s = (ScopeElm) mouseElm;
    	    	    if (s.elmScope.canMenu()) {
    	    		menuPlot = s.elmScope.selectedPlot;
    	    		scopePopupMenu.doScopePopupChecks(true, false, false, false, s.elmScope);
    			contextPanel=new PopupPanel(true);
    			contextPanel.add(scopePopupMenu.getMenuBar());
    			contextPanel.setPopupPosition(menuClientX, menuClientY);
    			contextPanel.show();
    	    	    }
    	    	}
    	} else {
    		doMainMenuChecks();
    		contextPanel=new PopupPanel(true);
    		contextPanel.add(mainMenuBar);
    		x=Math.max(0, Math.min(menuClientX, canvasWidth-400));
    		y=Math.max(0, Math.min(menuClientY, canvasHeight-450));
    		contextPanel.setPopupPosition(x,y);
    		contextPanel.show();
    	}
    }

    boolean canSplit(CircuitElm ce) {
	if (!(ce instanceof WireElm))
	    return false;
	WireElm we = (WireElm) ce;
	if (we.x == we.x2 || we.y == we.y2)
	    return true;
	return false;
    }
    
    // 检查用户能否为此元件创建滑块
    boolean sliderItemEnabled(CircuitElm elm) {
	int i;
	
	// 防止混淆
	if (elm instanceof VarRailElm || elm instanceof PotElm)
	    return false;
	
	for (i = 0; ; i++) {
	    EditInfo ei = elm.getEditInfo(i);
	    if (ei == null)
		return false;
	    if (ei.canCreateAdjustable())
		return true;
	}
    }

    void longPress() {
	doPopupMenu();
    }
    
    void twoFingerTouch(int x, int y) {
	tempMouseMode = MODE_DRAG_ALL;
	dragScreenX = x;
	dragScreenY = y;
    }
    
//    public void mouseClicked(MouseEvent e) {
    public void onClick(ClickEvent e) {
    	e.preventDefault();
//    	//IES - 移除交互
////	if ( e.getClickCount() == 2 && !didSwitch )
////	    doEditMenu(e);
//	if (e.getNativeButton() == NativeEvent.BUTTON_LEFT) {
//	    if (mouseMode == MODE_SELECT || mouseMode == MODE_DRAG_SELECTED)
//		clearSelection();
//	}	
    	if ((e.getNativeButton() == NativeEvent.BUTTON_MIDDLE))
    		scrollValues(e.getNativeEvent().getClientX(), e.getNativeEvent().getClientY(), 0);
    }
    
    public void onDoubleClick(DoubleClickEvent e){
    	e.preventDefault();
 //   	if (!didSwitch && mouseElm != null)
    	if (mouseElm != null && !(mouseElm instanceof SwitchElm) && !noEditCheckItem.getState())
    		doEdit(mouseElm);
    }
    
//    public void mouseEntered(MouseEvent e) {
//    }
    
    public void onMouseOut(MouseOutEvent e) {
    	mouseCursorX=-1;
    }

    void clearMouseElm() {
    	scopeSelected = -1;
    	setMouseElm(null);
    	plotXElm = plotYElm = null;
    }
    
    int menuClientX, menuClientY;
    int menuX, menuY;
    
    public void onMouseDown(MouseDownEvent e) {
//    public void mousePressed(MouseEvent e) {
    	e.preventDefault();
    	
    	// 确保画布获得焦点，而不是停止按钮或其他控件，这样所有快捷键才能生效
    	cv.setFocus(true);
    	
	stopElm = null; // 如果已停止，允许用户选择其他元件来修复电路 
    	menuX = menuClientX = e.getX();
    	menuY = menuClientY = e.getY();
    	mouseDownTime = System.currentTimeMillis();
    	
    	// 也许有人在另一个窗口执行了复制？其实应该在
    	// 窗口获得焦点时做这件事
    	enablePaste();
    	
    	if (e.getNativeButton() != NativeEvent.BUTTON_LEFT && e.getNativeButton() != NativeEvent.BUTTON_MIDDLE)
    		return;
    	
    	// 设置 mouseElm，以防我们在移动设备上
    	mouseSelect(e);
    	
    	mouseDragging=true;
    	didSwitch = false;
	
    	if (mouseWasOverSplitter) {
    		tempMouseMode = MODE_DRAG_SPLITTER;
    		return;
    	}
	if (e.getNativeButton() == NativeEvent.BUTTON_LEFT) {
//	    // 鼠标左键
	    tempMouseMode = mouseMode;
	    if (e.isAltKeyDown() && e.isMetaKeyDown())
		tempMouseMode = MODE_DRAG_COLUMN;
	    else if (e.isAltKeyDown() && e.isShiftKeyDown())
		tempMouseMode = MODE_DRAG_ROW;
	    else if (e.isShiftKeyDown())
		tempMouseMode = MODE_SELECT;
	    else if (e.isAltKeyDown())
		tempMouseMode = MODE_DRAG_ALL;
	    else if (e.isControlKeyDown() || e.isMetaKeyDown())
		tempMouseMode = MODE_DRAG_POST;
	} else
	    tempMouseMode = MODE_DRAG_ALL;
	

	if (noEditCheckItem.getState())
	    tempMouseMode = MODE_SELECT;
	
	if (!(dialogIsShowing()) && ((scopeSelected != -1 && scopes[scopeSelected].cursorInSettingsWheel()) ||
		( scopeSelected == -1 && mouseElm instanceof ScopeElm && ((ScopeElm)mouseElm).elmScope.cursorInSettingsWheel()))){
	    if (noEditCheckItem.getState())
		return;
	    Scope s;
	    if (scopeSelected != -1)
		s=scopes[scopeSelected];
	    else 
		s=((ScopeElm)mouseElm).elmScope;
	    s.properties();
	    clearSelection();
	    mouseDragging=false;
	    return;
	}

	int gx = inverseTransformX(e.getX());
	int gy = inverseTransformY(e.getY());
	if (doSwitch(gx, gy)) {
	    // 在把鼠标模式改为 MODE_DRAG_POST 之前先做这件事！否则点击逻辑输入
	    // 会给整个电路加上电流点！
            didSwitch = true;
	    return;
	}
	
	// IES - 在选择模式下，如果调整大小手柄相距足够远且鼠标位于其上，则抓取手柄
	if (tempMouseMode == MODE_SELECT && mouseElm!=null && !noEditCheckItem.getState() &&
		mouseElm.getHandleGrabbedClose(gx, gy, POSTGRABSQ, MINPOSTGRABSIZE) >=0 &&
		!anySelectedButMouse())
	    tempMouseMode = MODE_DRAG_POST;
	
	if (tempMouseMode != MODE_SELECT && tempMouseMode != MODE_DRAG_SELECTED)
	    clearSelection();

	pushUndo();
	initDragGridX = gx;
	initDragGridY = gy;
	dragging = true;
	if (tempMouseMode !=MODE_ADD_ELM)
		return;
//	
	int x0 = snapGrid(gx);
	int y0 = snapGrid(gy);
	if (!circuitArea.contains(e.getX(), e.getY()))
	    return;

	try {
	    dragElm = constructElement(mouseModeStr, x0, y0);
	} catch (Exception ex) {
	    debugger();
	}
    }

    static int lastSubcircuitMenuUpdate;
    
    // 当菜单栏被点击或右键菜单被打开时，相应地勾选/取消勾选/启用/禁用菜单项。
    // 副作用是还会显示快捷键
    void doMainMenuChecks() {
    	int c = mainMenuItems.size();
    	int i;
    	for (i=0; i<c ; i++) {
    	    	String s = mainMenuItemNames.get(i);
    		mainMenuItems.get(i).setState(s==mouseModeStr);

	        // 当电路不可编辑时禁用 Draw 菜单项的代码，但本版本中未使用，
	        // 因为取而代之的是弹出对话框（见 menuPerformed）。
    		//if (s.length() > 3 && s.substring(s.length()-3)=="Elm")
    		    //mainMenuItems.get(i).setEnabled(!noEditCheckItem.getState());
    	}
    	stackAllItem.setEnabled(scopeCount > 1 && scopes[scopeCount-1].position > 0);
    	unstackAllItem.setEnabled(scopeCount > 1 && scopes[scopeCount-1].position != scopeCount -1);
    	combineAllItem.setEnabled(scopeCount > 1);
    	separateAllItem.setEnabled(scopeCount > 0);
    	
    	// 如有必要，同时更新子电路菜单
    	if (lastSubcircuitMenuUpdate != CustomCompositeModel.sequenceNumber)
    	    composeSubcircuitMenu();
    }
    
 
    public void onMouseUp(MouseUpEvent e) {
    	e.preventDefault();
    	mouseDragging=false;
    	
    	// 单击以清除选择
    	if (tempMouseMode == MODE_SELECT && selectedArea == null)
    	    clearSelection();

    	// cmd-单击 = 拆分导线
    	if (tempMouseMode == MODE_DRAG_POST && draggingPost == -1)
    	    doSplit(mouseElm);
    	
    	tempMouseMode = mouseMode;
    	selectedArea = null;
    	dragging = false;
    	boolean circuitChanged = false;
    	if (heldSwitchElm != null) {
    		heldSwitchElm.mouseUp();
    		heldSwitchElm = null;
    		circuitChanged = true;
    	}
    	if (dragElm != null) {
    		// 如果元件尺寸为零，则不创建它
    		// IES - 并取消之前的任何选择
    	    	if (dragElm.creationFailed()) {
    			dragElm.delete();
    			if (mouseMode == MODE_SELECT || mouseMode == MODE_DRAG_SELECTED)
    				clearSelection();
    		}
    		else {
    			elmList.addElement(dragElm);
    			dragElm.draggingDone();
    			circuitChanged = true;
    			writeRecoveryToStorage();
    		}
    		dragElm = null;
    	}
    	if (circuitChanged) {
    	    needAnalyze();
    	    pushUndo();
    	    unsavedChanges = true;
    	    changeWindowTitle(unsavedChanges);
    	}
    	if (dragElm != null)
    		dragElm.delete();
    	dragElm = null;
    	repaint();
    }
    
    public void onMouseWheel(MouseWheelEvent e) {
    	e.preventDefault();
    	
    	// 一旦开始缩放，短时间内不允许鼠标滚轮的其他用途，
    	// 以免在缩放时意外修改电阻值
    	boolean zoomOnly = System.currentTimeMillis() < zoomTime+1000;
    	
    	if (noEditCheckItem.getState() || !mouseWheelEditCheckItem.getState())
    	    zoomOnly = true;
    	
    	if (!zoomOnly)
    	    scrollValues(e.getNativeEvent().getClientX(), e.getNativeEvent().getClientY(), e.getDeltaY());
    	
    	if (mouseElm instanceof MouseWheelHandler && !zoomOnly)
    		((MouseWheelHandler) mouseElm).onMouseWheel(e);
    	else if (scopeSelected != -1 && !zoomOnly)
    	    scopes[scopeSelected].onMouseWheel(e);
    	else if (!dialogIsShowing()) {
    	    mouseCursorX=e.getX();
    	    mouseCursorY=e.getY();
    	    zoomCircuit(-e.getDeltaY()*wheelSensitivity, false);
    	    zoomTime = System.currentTimeMillis();
   	}
    	repaint();
    }

    void zoomCircuit(double dy) { zoomCircuit(dy, false); }

    void zoomCircuit(double dy, boolean menu) {
	double newScale;
    	double oldScale = transform[0];
    	double val = dy*.01;
    	newScale = Math.max(oldScale+val, .2);
    	newScale = Math.min(newScale, 2.5);
    	setCircuitScale(newScale, menu);
    }
    
    void setCircuitScale(double newScale, boolean menu) {
	int constX = !menu ? mouseCursorX : circuitArea.width/2;
	int constY = !menu ? mouseCursorY : circuitArea.height/2;
	int cx = inverseTransformX(constX);
	int cy = inverseTransformY(constY);
	transform[0] = transform[3] = newScale;

	// 调整平移量，使屏幕中心保持不变
	// 逆变换 = (x-t4)/t0
	transform[4] = constX - cx*newScale;
	transform[5] = constY - cy*newScale;
    }
    
    void setPowerBarEnable() {
    	if (powerCheckItem.getState()) {
    	    powerLabel.setStyleName("disabled", false);
    	    powerBar.enable();
    	} else {
    	    powerLabel.setStyleName("disabled", true);
    	    powerBar.disable();
    	}
    }

    void scrollValues(int x, int y, int deltay) {
    	if (mouseElm!=null && !dialogIsShowing() && scopeSelected == -1)
    		if (mouseElm instanceof ResistorElm || mouseElm instanceof CapacitorElm ||  mouseElm instanceof InductorElm) {
    			scrollValuePopup = new ScrollValuePopup(x, y, deltay, mouseElm, this);
    			unsavedChanges = true;
    			changeWindowTitle(unsavedChanges);
    		}
    }
    
    void enableItems() {
    }
    
    void setGrid() {
	gridSize = (smallGridCheckItem.getState()) ? 8 : 16;
	gridMask = ~(gridSize-1);
	gridRound = gridSize/2-1;
    }

    void setToolbar() {
	layoutPanel.setWidgetHidden(toolbar, !toolbarCheckItem.getState());
	executeJS("setAllAbsBtnsTopPos(\""+getAbsBtnsTopPos()+"px\")");
	setSlidersPanelHeight();
	setCanvasSize();
    }

    void pushUndo() {
    	redoStack.removeAllElements();
    	String s = dumpCircuit();
    	if (undoStack.size() > 0 &&
    			s.compareTo(undoStack.lastElement().dump) == 0)
    	    return;
    	undoStack.add(new UndoItem(s));
    	enableUndoRedo();
    	savedFlag = false;
    }

    void doUndo() {
    	if (undoStack.size() == 0)
    		return;
    	redoStack.add(new UndoItem(dumpCircuit()));
    	UndoItem ui = undoStack.remove(undoStack.size()-1);
    	loadUndoItem(ui);
    	enableUndoRedo();
    	unsavedChanges = true;
    	changeWindowTitle(unsavedChanges);
    }

    void doRedo() {
    	if (redoStack.size() == 0)
    		return;
    	undoStack.add(new UndoItem(dumpCircuit()));
    	UndoItem ui = redoStack.remove(redoStack.size()-1);
    	loadUndoItem(ui);
    	enableUndoRedo();
    	unsavedChanges = true;
    	changeWindowTitle(unsavedChanges);
    }

    void loadUndoItem(UndoItem ui) {
	readCircuit(ui.dump, RC_NO_CENTER);
	transform[0] = transform[3] = ui.scale;
	transform[4] = ui.transform4;
	transform[5] = ui.transform5;
    }
    
    void doRecover() {
	pushUndo();
	readCircuit(recovery);
	allowSave(false);
	recoverItem.setEnabled(false);
	filePath = null;
	fileName = null;
	changeWindowTitle(unsavedChanges);
    }
    
    void enableUndoRedo() {
    	redoItem.setEnabled(redoStack.size() > 0);
    	undoItem.setEnabled(undoStack.size() > 0);
    }

    void setMouseMode(int mode)
    {
    	mouseMode = mode;
    	if ( mode == MODE_ADD_ELM ) {
    		setCursorStyle("cursorCross");
    	} else {
    		setCursorStyle("cursorPointer");
    	}
    }
    
    void setCursorStyle(String s) {
    	if (lastCursorStyle!=null)
    		cv.removeStyleName(lastCursorStyle);
    	cv.addStyleName(s);
    	lastCursorStyle=s;
    }
    


    void setMenuSelection() {
    	if (menuElm != null) {
    		if (menuElm.selected)
    			return;
    		clearSelection();
    		menuElm.setSelected(true);
    	}
    }

    int countSelected() {
	int count = 0;
	for (CircuitElm ce: elmList)
	    if (ce.isSelected())
		count++;
	return count;
    }

    class FlipInfo { public int cx, cy, count; }

    FlipInfo prepareFlip() {
    	int i;
    	pushUndo();
    	setMenuSelection();
    	int minx = 30000, maxx = -30000;
    	int miny = 30000, maxy = -30000;
	int count = countSelected();
    	for (i = 0; i != elmList.size(); i++) {
	    CircuitElm ce = getElm(i);
	    if (ce.isSelected() || count == 0) {
		minx = min(ce.x, min(ce.x2, minx));
		maxx = max(ce.x, max(ce.x2, maxx));
		miny = min(ce.y, min(ce.y2, miny));
		maxy = max(ce.y, max(ce.y2, maxy));
	    }
    	}
	FlipInfo fi = new FlipInfo();
	fi.cx = (minx+maxx)/2;
	fi.cy = (miny+maxy)/2;
	fi.count = count;	
	return fi;
    }

    void flipX() {
	FlipInfo fi = prepareFlip();
	int center2 = fi.cx*2;
	for (CircuitElm ce : elmList) {
	    if (ce.isSelected() || fi.count == 0)
		ce.flipX(center2, fi.count);
    	}
	needAnalyze();
    }

    void flipY() {
	FlipInfo fi = prepareFlip();
	int center2 = fi.cy*2;
	for (CircuitElm ce : elmList) {
	    if (ce.isSelected() || fi.count == 0)
		ce.flipY(center2, fi.count);
    	}
	needAnalyze();
    }

    void flipXY() {
	FlipInfo fi = prepareFlip();
	int xmy = snapGrid(fi.cx-fi.cy);
	console("xmy " + xmy + " grid " + gridSize + " " + fi.cx + " " + fi.cy);
	for (CircuitElm ce : elmList) {
	    if (ce.isSelected() || fi.count == 0)
		ce.flipXY(xmy, fi.count);
    	}
	needAnalyze();
    }

    void doCut() {
    	int i;
    	pushUndo();
    	setMenuSelection();
    	clipboard = "";
    	for (i = elmList.size()-1; i >= 0; i--) {
    		CircuitElm ce = getElm(i);
    		// ScopeElm 不太适合剪切-粘贴，因为它们对父元件的数字引用
    		// 在导出（dump）中会被打乱。目前我们先忽略它们，
    		// 直到我想到更好的办法
    		if (willDelete(ce) && !(ce instanceof ScopeElm) ) {
    			clipboard += ce.dump() + "\n";
    		}
    	}
    	writeClipboardToStorage();
    	doDelete(true);
    	enablePaste();
    }

    void writeClipboardToStorage() {
    	Storage stor = Storage.getLocalStorageIfSupported();
    	if (stor == null)
    		return;
    	stor.setItem("circuitClipboard", clipboard);
    }
    
    void readClipboardFromStorage() {
    	Storage stor = Storage.getLocalStorageIfSupported();
    	if (stor == null)
    		return;
    	clipboard = stor.getItem("circuitClipboard");
    }

    void writeRecoveryToStorage() {
	console("write recovery");
    	Storage stor = Storage.getLocalStorageIfSupported();
    	if (stor == null)
    		return;
    	String s = dumpCircuit();
    	stor.setItem("circuitRecovery", s);
    }

    void readRecovery() {
	Storage stor = Storage.getLocalStorageIfSupported();
	if (stor == null)
		return;
	recovery = stor.getItem("circuitRecovery");
    }


    void deleteUnusedScopeElms() {
	// 移除指向已不存在元件的所有 ScopeElm
	for (int i = elmList.size()-1; i >= 0; i--) {
    		CircuitElm ce = getElm(i);
    		if (ce instanceof ScopeElm && (((ScopeElm) ce).elmScope.needToRemove() )) {
    			ce.delete();
    			elmList.removeElementAt(i);
    			
    			// 需要重建 scopeElmArr
    			needAnalyze();
    		}
    	}
	
    }
    
    void doDelete(boolean pushUndoFlag) {
    	int i;
    	if (pushUndoFlag)
    	    pushUndo();
    	boolean hasDeleted = false;

    	for (i = elmList.size()-1; i >= 0; i--) {
    		CircuitElm ce = getElm(i);
    		if (willDelete(ce)) {
    		    	if (ce.isMouseElm())
    		    	    setMouseElm(null);
    			ce.delete();
    			elmList.removeElementAt(i);
    			hasDeleted = true;
    		}
    	}
    	if ( hasDeleted ) {
    	    deleteUnusedScopeElms();
    	    needAnalyze();
    	    writeRecoveryToStorage();
    	    unsavedChanges = true;
    	    changeWindowTitle(unsavedChanges);
    	}    
    }
    
    boolean willDelete( CircuitElm ce ) {
	// 该元件是否在要删除的列表中。
	// 这与以前版本的逻辑不同：旧版本起初只
	// 删除选中的元件（可能包含 mouseElm），如果没有选中的元件，
	// 则再删除 mouseElm。不确定这对用户体验
	// 有什么实际帮助。
	//
	// 顺便说一句，旧逻辑还可能让 mouseElm 指向已被删除的元件。
	return ce.isSelected() || ce.isMouseElm();
    }
    
    String copyOfSelectedElms() {
	String r = dumpOptions();
	CustomLogicModel.clearDumpedFlags();
	CustomCompositeModel.clearDumpedFlags();
	DiodeModel.clearDumpedFlags();
	TransistorModel.clearDumpedFlags();
	for (int i = elmList.size()-1; i >= 0; i--) {
	    CircuitElm ce = getElm(i);
	    String m = ce.dumpModel();
	    if (m != null && !m.isEmpty())
		r += m + "\n";
	    // 为什么我们不复制 ScopeElm，请参阅 doCut 中的说明。
	    if (ce.isSelected() && !(ce instanceof ScopeElm))
		r += ce.dump() + "\n";
	}
	return r;
    }
    
    void doCopy() {
    	// 如果使用右键菜单复制单个元件，完成后清除选择
    	boolean clearSel = (menuElm != null && !menuElm.selected);
    	
    	setMenuSelection();
    	clipboard=copyOfSelectedElms();
    	
    	if (clearSel)
    	    clearSelection();
    	
    	writeClipboardToStorage();
    	enablePaste();
    }

    void enablePaste() {
    	if (clipboard == null || clipboard.length() == 0)
    		readClipboardFromStorage();
    	pasteItem.setEnabled(clipboard != null && clipboard.length() > 0);
    }

    void doDuplicate() {
    	String s;
    	setMenuSelection();
    	s=copyOfSelectedElms();
    	doPaste(s);
    }
    
    void doPaste(String dump) {
    	pushUndo();
    	clearSelection();
    	int i;
    	Rectangle oldbb = null;
    	
    	// 获取旧的包围盒
    	for (i = 0; i != elmList.size(); i++) {
    		CircuitElm ce = getElm(i);
    		Rectangle bb = ce.getBoundingBox();
    		if (oldbb != null)
    			oldbb = oldbb.union(bb);
    		else
    			oldbb = bb;
    	}
    	
    	// 添加新元件
    	int oldsz = elmList.size();
    	int flags = RC_RETAIN;
    	
    	// 如果我们要在原位置粘贴，则不要重新居中电路，因为那会改变变换矩阵
//	if (mouseCursorX > 0 && circuitArea.contains(mouseCursorX, mouseCursorY))
    	
    	// 事实上，永远不要重新居中电路，除非原电路为空
    	if (oldsz > 0)
    	    flags |= RC_NO_CENTER;
	
    	if (dump != null)
    	    readCircuit(dump, flags);
    	else {
    	    readClipboardFromStorage();
    	    readCircuit(clipboard, flags);
    	}

    	// 选中新元件并获取它们的包围盒
    	Rectangle newbb = null;
    	for (i = oldsz; i != elmList.size(); i++) {
    		CircuitElm ce = getElm(i);
    		ce.setSelected(true);
    		Rectangle bb = ce.getBoundingBox();
    		if (newbb != null)
    			newbb = newbb.union(bb);
    		else
    			newbb = bb;
    	}
    	
    	if (oldbb != null && newbb != null /*&& oldbb.intersects(newbb)*/) {
    		// 在边缘为新元件找一个位置
    		int dx = 0, dy = 0;
    		int spacew = circuitArea.width - oldbb.width - newbb.width;
    		int spaceh = circuitArea.height - oldbb.height - newbb.height;
    		
    		if (!oldbb.intersects(newbb)) {
    		    // 旧坐标可能非常远，因此把它们移动到与当前电路相同的原点
    		    dx = snapGrid(oldbb.x - newbb.x);
    		    dy = snapGrid(oldbb.y - newbb.y);
    		}
    		
    		if (spacew > spaceh) {
    			dx = snapGrid(oldbb.x + oldbb.width  - newbb.x + gridSize);
    		} else {
    			dy = snapGrid(oldbb.y + oldbb.height - newbb.y + gridSize);
    		}
    		
    		// 如果可能，将新元件移动到鼠标附近
    		if (mouseCursorX > 0 && circuitArea.contains(mouseCursorX, mouseCursorY)) {
    	    	    int gx = inverseTransformX(mouseCursorX);
    	    	    int gy = inverseTransformY(mouseCursorY);
    	    	    int mdx = snapGrid(gx-(newbb.x+newbb.width/2));
    	    	    int mdy = snapGrid(gy-(newbb.y+newbb.height/2));
    	    	    for (i = oldsz; i != elmList.size(); i++) {
    	    		if (!getElm(i).allowMove(mdx, mdy))
    	    		    break;
    	    	    }
    	    	    if (i == elmList.size()) {
    	    		dx = mdx;
    	    		dy = mdy;
    	    	    }
    		}
    		
    		// 移动新元件
    		for (i = oldsz; i != elmList.size(); i++) {
    			CircuitElm ce = getElm(i);
    			ce.move(dx, dy);
    		}
    		
    		// 居中电路
    	//	handleResize();
    	}
    	needAnalyze();
    	writeRecoveryToStorage();
    	unsavedChanges = true;
    	changeWindowTitle(unsavedChanges);
    }

    void clearSelection() {
	int i;
	for (i = 0; i != elmList.size(); i++) {
	    CircuitElm ce = getElm(i);
	    ce.setSelected(false);
	}
	enableDisableMenuItems();
    }
    
    void doSelectAll() {
    	int i;
    	for (i = 0; i != elmList.size(); i++) {
    		CircuitElm ce = getElm(i);
    		ce.setSelected(true);
    	}
	enableDisableMenuItems();
    }
    
    boolean anySelectedButMouse() {
    	for (int i=0; i != elmList.size(); i++)
    		if (getElm(i)!= mouseElm && getElm(i).selected)
    			return true;
    	return false;
    }

//    public void keyPressed(KeyEvent e) {}
//    public void keyReleased(KeyEvent e) {}
    
    boolean dialogIsShowing() {
    	if (editDialog!=null && editDialog.isShowing())
    		return true;
        if (customLogicEditDialog!=null && customLogicEditDialog.isShowing())
                return true;
        if (diodeModelEditDialog!=null && diodeModelEditDialog.isShowing())
                return true;
       	if (dialogShowing != null && dialogShowing.isShowing())
       		return true;
    	if (contextPanel!=null && contextPanel.isShowing())
    		return true;
    	if (scrollValuePopup != null && scrollValuePopup.isShowing())
    		return true;
    	if (aboutBox !=null && aboutBox.isShowing())
    		return true;
    	if (helpDialog !=null && helpDialog.isShowing())
    		return true;
    	if (licenseDialog !=null && licenseDialog.isShowing())
    		return true;
    	if (modDialog !=null && modDialog.isShowing())
    		return true;
    	return false;
    }
    
    public void onPreviewNativeEvent(NativePreviewEvent e) {
    	int cc=e.getNativeEvent().getCharCode();
    	int t=e.getTypeInt();
    	int code=e.getNativeEvent().getKeyCode();
    	if (dialogIsShowing()) {
    		if (scrollValuePopup != null && scrollValuePopup.isShowing() &&
    				(t & Event.ONKEYDOWN)!=0) {
    			if (code==KEY_ESCAPE || code==KEY_SPACE)
    				scrollValuePopup.close(false);
    			if (code==KEY_ENTER)
    				scrollValuePopup.close(true);
    		}
    		
    		// 处理对话框的 Esc/回车键
    		// 可能同时显示多个编辑对话框，选择最前面的一个
    		Dialog dlg = editDialog;
    		if (diodeModelEditDialog != null)
    		    dlg = diodeModelEditDialog;
    		if (customLogicEditDialog != null)
    		    dlg = customLogicEditDialog;
    		if (dialogShowing != null)
    		    dlg = dialogShowing;
    		if (dlg!=null && dlg.isShowing() &&
    				(t & Event.ONKEYDOWN)!=0) {
    			if (code==KEY_ESCAPE)
    			    dlg.closeDialog();
    			if (code==KEY_ENTER)
    			    dlg.enterPressed();
    		}
    		return;
    	}
    	
    	if ((t&Event.ONKEYPRESS)!=0) {
		if (cc=='-') {
    		    menuPerformed("key", "zoomout");
    		    e.cancel();
    		}
    		if (cc=='+' || cc == '=') {
    		    menuPerformed("key", "zoomin");
    		    e.cancel();
    		}
		if (cc=='0') {
    		    menuPerformed("key", "zoom100");
    		    e.cancel();
		}
		if (cc=='/' && shortcuts['/'] == null) {
		    menuPerformed("key", "search");
		    e.cancel();
		}
    	}
    	
    	// 禁用编辑时，忽略所有其他快捷键
    	if (noEditCheckItem.getState())
    	    return;

    	if ((t & Event.ONKEYDOWN)!=0) {
    		if (code==KEY_BACKSPACE || code==KEY_DELETE) {
    		    if (scopeSelected != -1) {
    			// 将选中示波器时的 DELETE 键视为“移除示波器”，而不是删除
    			scopes[scopeSelected].setElm(null);
    			scopeSelected = -1;
    		    } else {
    		    	menuElm = null;
    		    	pushUndo();
    			doDelete(true);
    			e.cancel();
    		    }
    		}
    		if (code==KEY_ESCAPE){
    			setMouseMode(MODE_SELECT);
    			mouseModeStr = "Select";
			updateToolbar();
    			tempMouseMode = mouseMode;
    			e.cancel();
    		}

    		if (e.getNativeEvent().getCtrlKey() || e.getNativeEvent().getMetaKey()) {
    			if (code==KEY_C) {
    				menuPerformed("key", "copy");
    				e.cancel();
    			}
    			if (code==KEY_X) {
    				menuPerformed("key", "cut");
    				e.cancel();
    			}
    			if (code==KEY_V) {
    				menuPerformed("key", "paste");
    				e.cancel();
    			}
    			if (code==KEY_Z) {
    				menuPerformed("key", "undo");
    				e.cancel();
    			}
    			if (code==KEY_Y) {
    				menuPerformed("key", "redo");
    				e.cancel();
    			}
    			if (code==KEY_D) {
    			    	menuPerformed("key", "duplicate");
    			    	e.cancel();
    			}
    			if (code==KEY_A) {
    				menuPerformed("key", "selectAll");
    				e.cancel();
    			}
    			if (code==KEY_P) {
				menuPerformed("key", "print");
				e.cancel();
			}
    			if (code==KEY_N) {
				menuPerformed("key", "newwindow");
				e.cancel();
			}
    			if (code==KEY_S) {
				String cmd = (filePath!=null) ? "save" : "saveas";
				menuPerformed("key", cmd);
				e.cancel();
			}
    			if (code==KEY_O) {
				menuPerformed("key", "importfromlocalfile");
				e.cancel();
			}    			
    		}
    	}
    	if ((t&Event.ONKEYPRESS)!=0) {
    		if (cc>32 && cc<127){
    			String c=shortcuts[cc];
    			e.cancel();
    			if (c==null)
    				return;
    			setMouseMode(MODE_ADD_ELM);
    			mouseModeStr=c;
			updateToolbar();
    			tempMouseMode = mouseMode;
    		}
    		if (cc==32) {
		    setMouseMode(MODE_SELECT);
		    mouseModeStr = "Select";
		    updateToolbar();
		    tempMouseMode = mouseMode;
		    e.cancel();
    		}
    	}
    }
    
    void updateToolbar() {
	//toolbar.setModeLabel(classToLabelMap.get(mouseModeStr));
	toolbar.highlightButton(mouseModeStr);
    }

    String getLabelTextForClass(String cls) {
	return classToLabelMap.get(cls);
    }

    // 通过高斯消元法把矩阵分解为上下三角矩阵。
    // 进入时，a[0..n-1][0..n-1] 是要分解的
    // 矩阵。ipvt[] 返回主元索引的整数向量，
    // 供 lu_solve() 例程使用。
    static boolean lu_factor(double a[][], int n, int ipvt[]) {
	int i,j,k;
	
	// 通过扫描是否有全为零的行，
	// 检查是否存在奇异矩阵
	for (i = 0; i != n; i++) { 
	    boolean row_all_zeros = true;
	    for (j = 0; j != n; j++) {
		if (a[i][j] != 0) {
		    row_all_zeros = false;
		    break;
		}
	    }
	    // 如果全为零，则为奇异矩阵
	    if (row_all_zeros)
		return false;
	}
	
        // 使用 Crout 方法；逐列循环
	for (j = 0; j != n; j++) {
	    
	    // 计算该列的上三角元素
	    for (i = 0; i != j; i++) {
		double q = a[i][j];
		for (k = 0; k != i; k++)
		    q -= a[i][k]*a[k][j];
		a[i][j] = q;
	    }

	    // 计算该列的下三角元素
	    double largest = 0;
	    int largestRow = -1;
	    for (i = j; i != n; i++) {
		double q = a[i][j];
		for (k = 0; k != j; k++)
		    q -= a[i][k]*a[k][j];
		a[i][j] = q;
		double x = Math.abs(q);
		if (x >= largest) {
		    largest = x;
		    largestRow = i;
		}
	    }
	    
	    // 选主元
	    if (j != largestRow) {
		if (largestRow == -1) {
		    console("largestRow == -1");
		    return false;
		}
		double x;
		for (k = 0; k != n; k++) {
		    x = a[largestRow][k];
		    a[largestRow][k] = a[j][k];
		    a[j][k] = x;
		}
	    }

	    // 记录行交换情况
	    ipvt[j] = largestRow;

	    // 检查零元素；如果发现一个，则矩阵为奇异矩阵。
	    // 我们过去曾设法避免它们，但那导致了奇怪的 bug。例如，
	    // 两个输出连在一起的反相器应被标记为
	    // 奇异矩阵，但过去却允许这种情况（并产生奇怪的电流）
	    if (a[j][j] == 0.0) {
		console("didn't avoid zero");
//		a[j][j]=1e-18;
		return false;
	    }

	    if (j != n-1) {
		double mult = 1.0/a[j][j];
		for (i = j+1; i != n; i++)
		    a[i][j] *= mult;
	    }
	}
	return true;
    }

    // 使用先前由 lu_factor 完成的 LU 分解
    // 求解 n 个线性方程组。输入时，b[0..n-1] 是方程组的右
    // 端向量，输出时包含解。
    static void lu_solve(double a[][], int n, int ipvt[], double b[]) {
	int i;

	// 找到第一个非零的 b 元素
	for (i = 0; i != n; i++) {
	    int row = ipvt[i];

	    double swap = b[row];
	    b[row] = b[i];
	    b[i] = swap;
	    if (swap != 0)
		break;
	}
	
	int bi = i++;
	for (; i < n; i++) {
	    int row = ipvt[i];
	    int j;
	    double tot = b[row];
	    
	    b[row] = b[i];
	    // 使用下三角矩阵进行前向代入
	    for (j = bi; j < i; j++)
		tot -= a[i][j]*b[j];
	    b[i] = tot;
	}
	for (i = n-1; i >= 0; i--) {
	    double tot = b[i];
	    
	    // 使用上三角矩阵进行回代
	    int j;
	    for (j = i+1; j != n; j++)
		tot -= a[i][j]*b[j];
	    b[i] = tot/a[i][i];
	}
    }

    
    void createNewLoadFile() {
    	// 这是一个权宜之计，用来修复我认为是 <INPUT FILE 元素中的 bug：
    	// 重新加载同一文件不会触发 change 事件，因此两次导入同一文件
    	// 无法工作，除非销毁原始 input 元素并用新元素替换它
    	int idx=verticalPanel.getWidgetIndex(loadFileInput);
    	filePath = loadFileInput.getPath();
    	console("filePath: " + filePath);
    	fileName = loadFileInput.getFileName();
    	console("fileName: " + fileName);
    	if (filePath!=null)
    		allowSave(true);
    	changeWindowTitle(false);
    	LoadFile newlf=new LoadFile(this);
    	verticalPanel.insert(newlf, idx);
    	verticalPanel.remove(idx+1);
    	loadFileInput=newlf;
    }

    void addWidgetToVerticalPanel(Widget w) {
    	if (iFrame!=null) {
    		int i=verticalPanel.getWidgetIndex(iFrame);
    		verticalPanel.insert(w, i);
    	}
    	else
    		verticalPanel2.add(w);
    }
    
    void removeWidgetFromVerticalPanel(Widget w){
    	verticalPanel2.remove(w);
    }
    
    public static CircuitElm createCe(int tint, int x1, int y1, int x2, int y2, int f, StringTokenizer st) {
	switch (tint) {
    	case 'A': return new AntennaElm(x1, y1, x2, y2, f, st);
    	case 'I': return new InverterElm(x1, y1, x2, y2, f, st);
    	case 'L': return new LogicInputElm(x1, y1, x2, y2, f, st);
    	case 'M': return new LogicOutputElm(x1, y1, x2, y2, f, st);
    	case 'O': return new OutputElm(x1, y1, x2, y2, f, st);
    	case 'R': return new RailElm(x1, y1, x2, y2, f, st);
    	case 'S': return new Switch2Elm(x1, y1, x2, y2, f, st);
    	case 'T': return new TransformerElm(x1, y1, x2, y2, f, st);
    	case 'a': return new OpAmpElm(x1, y1, x2, y2, f, st);
    	case 'b': return new BoxElm(x1, y1, x2, y2, f, st);
    	case 'c': return new CapacitorElm(x1, y1, x2, y2, f, st);   	
    	case 'd': return new DiodeElm(x1, y1, x2, y2, f, st);
    	case 'f': return new MosfetElm(x1, y1, x2, y2, f, st);
    	case 'g': return new GroundElm(x1, y1, x2, y2, f, st);
    	case 'i': return new CurrentElm(x1, y1, x2, y2, f, st);
    	case 'j': return new JfetElm(x1, y1, x2, y2, f, st);
    	case 'l': return new InductorElm(x1, y1, x2, y2, f, st);
    	case 'm': return new MemristorElm(x1, y1, x2, y2, f, st);
    	case 'n': return new NoiseElm(x1, y1, x2, y2, f, st);
    	case 'p': return new ProbeElm(x1, y1, x2, y2, f, st);
    	case 'r': return new ResistorElm(x1, y1, x2, y2, f, st);
    	case 's': return new SwitchElm(x1, y1, x2, y2, f, st);
    	case 't': return new TransistorElm(x1, y1, x2, y2, f, st);
    	case 'v': return new VoltageElm(x1, y1, x2, y2, f, st);
    	case 'w': return new WireElm(x1, y1, x2, y2, f, st);
    	case 'x': return new TextElm(x1, y1, x2, y2, f, st);
    	case 'z': return new ZenerElm(x1, y1, x2, y2, f, st);
    	case 150: return new AndGateElm(x1, y1, x2, y2, f, st);
    	case 151: return new NandGateElm(x1, y1, x2, y2, f, st);
    	case 152: return new OrGateElm(x1, y1, x2, y2, f, st);
    	case 153: return new NorGateElm(x1, y1, x2, y2, f, st);
    	case 154: return new XorGateElm(x1, y1, x2, y2, f, st);
    	case 155: return new DFlipFlopElm(x1, y1, x2, y2, f, st);
    	case 156: return new JKFlipFlopElm(x1, y1, x2, y2, f, st);
    	case 157: return new SevenSegElm(x1, y1, x2, y2, f, st);
    	case 158: return new VCOElm(x1, y1, x2, y2, f, st);
    	case 159: return new AnalogSwitchElm(x1, y1, x2, y2, f, st);
    	case 160: return new AnalogSwitch2Elm(x1, y1, x2, y2, f, st);
    	case 161: return new PhaseCompElm(x1, y1, x2, y2, f, st);
    	case 162: return new LEDElm(x1, y1, x2, y2, f, st);
    	case 163: return new RingCounterElm(x1, y1, x2, y2, f, st);
    	case 164: return new CounterElm(x1, y1, x2, y2, f, st);
    	case 165: return new TimerElm(x1, y1, x2, y2, f, st);
    	case 166: return new DACElm(x1, y1, x2, y2, f, st);
    	case 167: return new ADCElm(x1, y1, x2, y2, f, st);
    	case 168: return new LatchElm(x1, y1, x2, y2, f, st);
    	case 169: return new TappedTransformerElm(x1, y1, x2, y2, f, st);
    	case 170: return new SweepElm(x1, y1, x2, y2, f, st);
    	case 171: return new TransLineElm(x1, y1, x2, y2, f, st);
    	case 172: return new VarRailElm(x1, y1, x2, y2, f, st);
    	case 173: return new TriodeElm(x1, y1, x2, y2, f, st);
    	case 174: return new PotElm(x1, y1, x2, y2, f, st);
    	case 175: return new TunnelDiodeElm(x1, y1, x2, y2, f, st);
    	case 176: return new VaractorElm(x1, y1, x2, y2, f, st);
    	case 177: return new SCRElm(x1, y1, x2, y2, f, st);
    	case 178: return new RelayElm(x1, y1, x2, y2, f, st);
    	case 179: return new CC2Elm(x1, y1, x2, y2, f, st);
    	case 180: return new TriStateElm(x1, y1, x2, y2, f, st);
    	case 181: return new LampElm(x1, y1, x2, y2, f, st);
    	case 182: return new SchmittElm(x1, y1, x2, y2, f, st);
    	case 183: return new InvertingSchmittElm(x1, y1, x2, y2, f, st);
    	case 184: return new MultiplexerElm(x1, y1, x2, y2, f, st);
    	case 185: return new DeMultiplexerElm(x1, y1, x2, y2, f, st);
    	case 186: return new PisoShiftElm(x1, y1, x2, y2, f, st);
    	case 187: return new SparkGapElm(x1, y1, x2, y2, f, st);
    	case 188: return new SeqGenElm(x1, y1, x2, y2, f, st);
    	case 189: return new SipoShiftElm(x1, y1, x2, y2, f, st);
    	case 193: return new TFlipFlopElm(x1, y1, x2, y2, f, st);
    	case 194: return new MonostableElm(x1, y1, x2, y2, f, st);
    	case 195: return new HalfAdderElm(x1, y1, x2, y2, f, st);
    	case 196: return new FullAdderElm(x1, y1, x2, y2, f, st);
    	case 197: return new SevenSegDecoderElm(x1, y1, x2, y2, f, st);
    	case 200: return new AMElm(x1, y1, x2, y2, f, st);
    	case 201: return new FMElm(x1, y1, x2, y2, f, st);
    	case 203: return new DiacElm(x1, y1, x2, y2, f, st);
    	case 206: return new TriacElm(x1, y1, x2, y2, f, st);
    	case 207: return new LabeledNodeElm(x1, y1, x2, y2, f, st);
    	case 208: return new CustomLogicElm(x1, y1, x2, y2, f, st);
    	case 209: return new PolarCapacitorElm(x1, y1, x2, y2, f, st);   	
    	case 210: return new DataRecorderElm(x1, y1, x2, y2, f, st);
    	case 211: return new AudioOutputElm(x1, y1, x2, y2, f, st);
    	case 212: return new VCVSElm(x1, y1, x2, y2, f, st);
    	case 213: return new VCCSElm(x1, y1, x2, y2, f, st);
    	case 214: return new CCVSElm(x1, y1, x2, y2, f, st);
    	case 215: return new CCCSElm(x1, y1, x2, y2, f, st);
    	case 216: return new OhmMeterElm(x1, y1, x2, y2, f, st);
	case 350: return new ThermistorNTCElm(x1, y1, x2, y2, f, st);
    	case 368: return new TestPointElm(x1, y1, x2, y2, f, st);
    	case 370: return new AmmeterElm(x1, y1, x2, y2, f, st);
	case 374: return new LDRElm(x1, y1, x2, y2, f, st);
    	case 400: return new DarlingtonElm(x1, y1, x2, y2, f, st);
    	case 401: return new ComparatorElm(x1, y1, x2, y2, f, st);
    	case 402: return new OTAElm(x1, y1, x2, y2, f, st);
    	case 403: return new ScopeElm(x1, y1, x2, y2, f, st);
    	case 404: return new FuseElm(x1, y1, x2, y2, f, st);
    	case 405: return new LEDArrayElm(x1, y1, x2, y2, f, st);
    	case 406: return new CustomTransformerElm(x1, y1, x2, y2, f, st);
    	case 407: return new OptocouplerElm(x1, y1, x2, y2, f, st);
    	case 408: return new StopTriggerElm(x1, y1, x2, y2, f, st);
    	case 409: return new OpAmpRealElm(x1, y1, x2, y2, f, st);
    	case 410: return new CustomCompositeElm(x1, y1, x2, y2, f, st);
    	case 411: return new AudioInputElm(x1, y1, x2, y2, f, st);
    	case 412: return new CrystalElm(x1, y1, x2, y2, f, st);
    	case 413: return new SRAMElm(x1, y1, x2, y2, f, st);
    	case 414: return new TimeDelayRelayElm(x1, y1, x2, y2, f, st);
	case 415: return new DCMotorElm(x1, y1, x2, y2, f, st);
	case 416: return new MBBSwitchElm(x1, y1, x2, y2, f, st);
    	case 417: return new UnijunctionElm(x1, y1, x2, y2, f, st);
    	case 418: return new ExtVoltageElm(x1, y1, x2, y2, f, st);
    	case 419: return new DecimalDisplayElm(x1, y1, x2, y2, f, st);
    	case 420: return new WattmeterElm(x1, y1, x2, y2, f, st);
    	case 421: return new Counter2Elm(x1, y1, x2, y2, f, st);
    	case 422: return new DelayBufferElm(x1, y1, x2, y2, f, st);
    	case 423: return new LineElm(x1, y1, x2, y2, f, st);
    	case 424: return new DataInputElm(x1, y1, x2, y2, f, st);
    	case 425: return new RelayCoilElm(x1, y1, x2, y2, f, st);
    	case 426: return new RelayContactElm(x1, y1, x2, y2, f, st);
    	case 427: return new ThreePhaseMotorElm(x1, y1, x2, y2, f, st);
    	case 428: return new MotorProtectionSwitchElm(x1, y1, x2, y2, f, st);
    	case 429: return new DPDTSwitchElm(x1, y1, x2, y2, f, st);
    	case 430: return new CrossSwitchElm(x1, y1, x2, y2, f, st);
        }
    	return null;
    }

    public static CircuitElm constructElement(String n, int x1, int y1){
    	if (n=="GroundElm")
    		return (CircuitElm) new GroundElm(x1, y1);
    	if (n=="ResistorElm")
    		return (CircuitElm) new ResistorElm(x1, y1);
    	if (n=="RailElm")
    		return (CircuitElm) new RailElm(x1, y1);
    	if (n=="SwitchElm")
    		return (CircuitElm) new SwitchElm(x1, y1);
    	if (n=="Switch2Elm")
    		return (CircuitElm) new Switch2Elm(x1, y1);
    	if (n=="MBBSwitchElm")
    		return (CircuitElm) new MBBSwitchElm(x1, y1);
    	if (n=="NTransistorElm" || n == "TransistorElm")
    		return (CircuitElm) new NTransistorElm(x1, y1);
    	if (n=="PTransistorElm")
    		return (CircuitElm) new PTransistorElm(x1, y1);
    	if (n=="WireElm")
    		return (CircuitElm) new WireElm(x1, y1);
    	if (n=="CapacitorElm")
    		return (CircuitElm) new CapacitorElm(x1, y1);
    	if (n=="PolarCapacitorElm")
		return (CircuitElm) new PolarCapacitorElm(x1, y1);
    	if (n=="InductorElm")
    		return (CircuitElm) new InductorElm(x1, y1);
    	if (n=="DCVoltageElm" || n=="VoltageElm")
    		return (CircuitElm) new DCVoltageElm(x1, y1);
    	if (n=="VarRailElm")
    		return (CircuitElm) new VarRailElm(x1, y1);
    	if (n=="PotElm")
    		return (CircuitElm) new PotElm(x1, y1);
    	if (n=="OutputElm")
    		return (CircuitElm) new OutputElm(x1, y1);
    	if (n=="CurrentElm")
    		return (CircuitElm) new CurrentElm(x1, y1);
    	if (n=="ProbeElm")
    		return (CircuitElm) new ProbeElm(x1, y1);
    	if (n=="DiodeElm")
    		return (CircuitElm) new DiodeElm(x1, y1);
    	if (n=="ZenerElm")
    		return (CircuitElm) new ZenerElm(x1, y1);
    	if (n=="ACVoltageElm")
    		return (CircuitElm) new ACVoltageElm(x1, y1);
    	if (n=="ACRailElm")
    		return (CircuitElm) new ACRailElm(x1, y1);
    	if (n=="SquareRailElm")
    		return (CircuitElm) new SquareRailElm(x1, y1);
    	if (n=="SweepElm")
    		return (CircuitElm) new SweepElm(x1, y1);
    	if (n=="LEDElm")
    		return (CircuitElm) new LEDElm(x1, y1);
    	if (n=="AntennaElm")
    		return (CircuitElm) new AntennaElm(x1, y1);
    	if (n=="LogicInputElm")
    		return (CircuitElm) new LogicInputElm(x1, y1);
    	if (n=="LogicOutputElm")
    		return (CircuitElm) new LogicOutputElm(x1, y1);
    	if (n=="TransformerElm")
    		return (CircuitElm) new TransformerElm(x1, y1);
    	if (n=="TappedTransformerElm")
    		return (CircuitElm) new TappedTransformerElm(x1, y1);
    	if (n=="TransLineElm")
    		return (CircuitElm) new TransLineElm(x1, y1);
    	if (n=="RelayElm")
    		return (CircuitElm) new RelayElm(x1, y1);
    	if (n=="RelayCoilElm")
    		return (CircuitElm) new RelayCoilElm(x1, y1);
    	if (n=="RelayContactElm")
    		return (CircuitElm) new RelayContactElm(x1, y1);
    	if (n=="ThreePhaseMotorElm")
    		return (CircuitElm) new ThreePhaseMotorElm(x1, y1);
    	if (n=="MemristorElm")
    		return (CircuitElm) new MemristorElm(x1, y1);
    	if (n=="SparkGapElm")
    		return (CircuitElm) new SparkGapElm(x1, y1);
    	if (n=="ClockElm")
    		return (CircuitElm) new ClockElm(x1, y1);
    	if (n=="AMElm")
    		return (CircuitElm) new AMElm(x1, y1);
    	if (n=="FMElm")
    		return (CircuitElm) new FMElm(x1, y1);
    	if (n=="LampElm")
    		return (CircuitElm) new LampElm(x1, y1);
    	if (n=="PushSwitchElm")
    		return (CircuitElm) new PushSwitchElm(x1, y1);
    	if (n=="OpAmpElm")
    		return (CircuitElm) new OpAmpElm(x1, y1);
    	if (n=="OpAmpSwapElm")
    		return (CircuitElm) new OpAmpSwapElm(x1, y1);
    	if (n=="NMosfetElm" || n == "MosfetElm")
    		return (CircuitElm) new NMosfetElm(x1, y1);
    	if (n=="PMosfetElm")
    		return (CircuitElm) new PMosfetElm(x1, y1);
    	if (n=="NJfetElm" || n == "JfetElm")
    		return (CircuitElm) new NJfetElm(x1, y1);
    	if (n=="PJfetElm")
    		return (CircuitElm) new PJfetElm(x1, y1);
    	if (n=="AnalogSwitchElm")
    		return (CircuitElm) new AnalogSwitchElm(x1, y1);
    	if (n=="AnalogSwitch2Elm")
    		return (CircuitElm) new AnalogSwitch2Elm(x1, y1);
    	if (n=="SchmittElm")
    		return (CircuitElm) new SchmittElm(x1, y1);
    	if (n=="InvertingSchmittElm")
    		return (CircuitElm) new InvertingSchmittElm(x1, y1);
    	if (n=="TriStateElm")
    		return (CircuitElm) new TriStateElm(x1, y1);
    	if (n=="SCRElm")
    		return (CircuitElm) new SCRElm(x1, y1);
    	if (n=="DiacElm")
    		return (CircuitElm) new DiacElm(x1, y1);
    	if (n=="TriacElm")
    		return (CircuitElm) new TriacElm(x1, y1);
    	if (n=="TriodeElm")
    		return (CircuitElm) new TriodeElm(x1, y1);
    	if (n=="VaractorElm")
    	    	return (CircuitElm) new VaractorElm(x1, y1);
    	if (n=="TunnelDiodeElm")
    		return (CircuitElm) new TunnelDiodeElm(x1, y1);
    	if (n=="CC2Elm")
    		return (CircuitElm) new CC2Elm(x1, y1);
    	if (n=="CC2NegElm")
    		return (CircuitElm) new CC2NegElm(x1, y1);
    	if (n=="InverterElm")
    		return (CircuitElm) new InverterElm(x1, y1);
    	if (n=="NandGateElm")
    		return (CircuitElm) new NandGateElm(x1, y1);
    	if (n=="NorGateElm")
    		return (CircuitElm) new NorGateElm(x1, y1);
    	if (n=="AndGateElm")
    		return (CircuitElm) new AndGateElm(x1, y1);
    	if (n=="OrGateElm")
    		return (CircuitElm) new OrGateElm(x1, y1);
    	if (n=="XorGateElm")
    		return (CircuitElm) new XorGateElm(x1, y1);
    	if (n=="DFlipFlopElm")
    		return (CircuitElm) new DFlipFlopElm(x1, y1);
    	if (n=="JKFlipFlopElm")
    		return (CircuitElm) new JKFlipFlopElm(x1, y1);
    	if (n=="SevenSegElm")
    		return (CircuitElm) new SevenSegElm(x1, y1);
    	if (n=="MultiplexerElm")
    		return (CircuitElm) new MultiplexerElm(x1, y1);
    	if (n=="DeMultiplexerElm")
    		return (CircuitElm) new DeMultiplexerElm(x1, y1);
    	if (n=="SipoShiftElm")
    		return (CircuitElm) new SipoShiftElm(x1, y1);
    	if (n=="PisoShiftElm")
    		return (CircuitElm) new PisoShiftElm(x1, y1);
    	if (n=="PhaseCompElm")
    		return (CircuitElm) new PhaseCompElm(x1, y1);
    	if (n=="CounterElm")
    		return (CircuitElm) new CounterElm(x1, y1);
    	
	// 如果移除 RingCounterElm，会破坏子电路
    	// 如果移除 DecadeElm，会破坏菜单和用户保存的快捷键
    	if (n=="DecadeElm" || n=="RingCounterElm")
    		return (CircuitElm) new RingCounterElm(x1, y1);
    	
    	if (n=="TimerElm")
    		return (CircuitElm) new TimerElm(x1, y1);
    	if (n=="DACElm")
    		return (CircuitElm) new DACElm(x1, y1);
    	if (n=="ADCElm")
    		return (CircuitElm) new ADCElm(x1, y1);
    	if (n=="LatchElm")
    		return (CircuitElm) new LatchElm(x1, y1);
    	if (n=="SeqGenElm")
    		return (CircuitElm) new SeqGenElm(x1, y1);
    	if (n=="VCOElm")
    		return (CircuitElm) new VCOElm(x1, y1);
    	if (n=="BoxElm")
    		return (CircuitElm) new BoxElm(x1, y1);
    	if (n=="LineElm")
    		return (CircuitElm) new LineElm(x1, y1);
    	if (n=="TextElm")
    		return (CircuitElm) new TextElm(x1, y1);
    	if (n=="TFlipFlopElm")
    		return (CircuitElm) new TFlipFlopElm(x1, y1);
    	if (n=="SevenSegDecoderElm")
    		return (CircuitElm) new SevenSegDecoderElm(x1, y1);
    	if (n=="FullAdderElm")
    		return (CircuitElm) new FullAdderElm(x1, y1);
    	if (n=="HalfAdderElm")
    		return (CircuitElm) new HalfAdderElm(x1, y1);
    	if (n=="MonostableElm")
    		return (CircuitElm) new MonostableElm(x1, y1);
    	if (n=="LabeledNodeElm")
    		return (CircuitElm) new LabeledNodeElm(x1, y1);
    	
    	// 如果移除 UserDefinedLogicElm，会破坏用户保存的快捷键
    	if (n=="UserDefinedLogicElm" || n=="CustomLogicElm")
    	    	return (CircuitElm) new CustomLogicElm(x1, y1);
    	
    	if (n=="TestPointElm")
    	    	return new TestPointElm(x1, y1);
    	if (n=="AmmeterElm")
	    	return new AmmeterElm(x1, y1);
    	if (n=="DataRecorderElm")
		return (CircuitElm) new DataRecorderElm(x1, y1);
    	if (n=="AudioOutputElm")
		return (CircuitElm) new AudioOutputElm(x1, y1);
    	if (n=="NDarlingtonElm" || n == "DarlingtonElm")
		return (CircuitElm) new NDarlingtonElm(x1, y1);
    	if (n=="PDarlingtonElm")
		return (CircuitElm) new PDarlingtonElm(x1, y1);
    	if (n=="ComparatorElm")
		return (CircuitElm) new ComparatorElm(x1, y1);
    	if (n=="OTAElm")
		return (CircuitElm) new OTAElm(x1, y1);
    	if (n=="NoiseElm")
		return (CircuitElm) new NoiseElm(x1, y1);
    	if (n=="VCVSElm")
		return (CircuitElm) new VCVSElm(x1, y1);
    	if (n=="VCCSElm")
		return (CircuitElm) new VCCSElm(x1, y1);
    	if (n=="CCVSElm")
		return (CircuitElm) new CCVSElm(x1, y1);
    	if (n=="CCCSElm")
		return (CircuitElm) new CCCSElm(x1, y1);
    	if (n=="OhmMeterElm")
		return (CircuitElm) new OhmMeterElm(x1, y1);
    	if (n=="ScopeElm")
    	    	return (CircuitElm) new ScopeElm(x1,y1);
    	if (n=="FuseElm")
	    	return (CircuitElm) new FuseElm(x1,y1);
    	if (n=="LEDArrayElm")
    	    	return (CircuitElm) new LEDArrayElm(x1, y1);
    	if (n=="CustomTransformerElm")
    	    	return (CircuitElm) new CustomTransformerElm(x1, y1);
    	if (n=="OptocouplerElm")
		return (CircuitElm) new OptocouplerElm(x1, y1);
    	if (n=="StopTriggerElm")
		return (CircuitElm) new StopTriggerElm(x1, y1);
    	if (n=="OpAmpRealElm")
		return (CircuitElm) new OpAmpRealElm(x1, y1);
    	if (n=="CustomCompositeElm")
		return (CircuitElm) new CustomCompositeElm(x1, y1);
    	if (n=="AudioInputElm")
		return (CircuitElm) new AudioInputElm(x1, y1);
    	if (n=="CrystalElm")
		return (CircuitElm) new CrystalElm(x1, y1);
    	if (n=="SRAMElm")
		return (CircuitElm) new SRAMElm(x1, y1);
    	if (n=="TimeDelayRelayElm")
		return (CircuitElm) new TimeDelayRelayElm(x1, y1);
    	if (n=="DCMotorElm")
		return (CircuitElm) new DCMotorElm(x1, y1);
    	if (n=="LDRElm")
		return (CircuitElm) new LDRElm(x1, y1);
    	if (n=="ThermistorNTCElm")
		return (CircuitElm) new ThermistorNTCElm(x1, y1);
    	if (n=="UnijunctionElm")
		return (CircuitElm) new UnijunctionElm(x1, y1);
    	if (n=="ExtVoltageElm")
		return (CircuitElm) new ExtVoltageElm(x1, y1);
    	if (n=="DecimalDisplayElm")
		return (CircuitElm) new DecimalDisplayElm(x1, y1);
    	if (n=="WattmeterElm")
		return (CircuitElm) new WattmeterElm(x1, y1);
    	if (n=="Counter2Elm")
		return (CircuitElm) new Counter2Elm(x1, y1);
    	if (n=="DelayBufferElm")
		return (CircuitElm) new DelayBufferElm(x1, y1);
    	if (n=="DataInputElm")
		return (CircuitElm) new DataInputElm(x1, y1);
    	if (n=="MotorProtectionSwitchElm")
		return (CircuitElm) new MotorProtectionSwitchElm(x1, y1);
    	if (n=="DPDTSwitchElm")
		return (CircuitElm) new DPDTSwitchElm(x1, y1);
    	if (n=="CrossSwitchElm")
		return (CircuitElm) new CrossSwitchElm(x1, y1);
    	
    	// 处理 CustomCompositeElm:modelname 形式
    	if (n.startsWith("CustomCompositeElm:")) {
    	    int ix = n.indexOf(':')+1;
    	    String name = n.substring(ix);
    	    return (CircuitElm) new CustomCompositeElm(x1, y1, name);
    	}
    	return null;
    }
    
    public void updateModels() {
	int i;
	for (i = 0; i != elmList.size(); i++)
	    elmList.get(i).updateModels();
    }
    

    
    
    native boolean weAreInUS(boolean orCanada) /*-{
    try {
	l = navigator.languages ? navigator.languages[0] : (navigator.language || navigator.userLanguage) ;  
    	if (l.length > 2) {
    		l = l.slice(-2).toUpperCase();
    		return (l == "US" || (l=="CA" && orCanada));
    	} else {
    		return 0;
    	}

    } catch (e) { return 0;
    }
    }-*/;

    native boolean weAreInGermany() /*-{
    try {
	l = navigator.languages ? navigator.languages[0] : (navigator.language || navigator.userLanguage) ;
	return (l.toUpperCase().startsWith("DE"));
    } catch (e) { return 0;
    }
    }-*/;
    
    // 用于调试
    void dumpNodelist() {

	CircuitNode nd;
	CircuitElm e;
	int i,j;
	String s;
	String cs;
//
//	for(i=0; i<nodeList.size(); i++) {
//	    s="Node "+i;
//	    nd=nodeList.get(i);
//	    for(j=0; j<nd.links.size();j++) {
//		s=s+" " + nd.links.get(j).num + " " +nd.links.get(j).elm.getDumpType();
//	    }
//	    console(s);
//	}
	console("Elm list Dump");
	for (i=0;i<elmList.size(); i++) {
	    e=elmList.get(i);
	    cs = e.getDumpClass().toString();
	    int p = cs.lastIndexOf('.');
	    cs = cs.substring(p+1);
	    if (cs=="WireElm") 
		continue;
	    if (cs=="LabeledNodeElm")
		cs = cs+" "+((LabeledNodeElm)e).text;
	    if (cs=="TransistorElm") {
		if (((TransistorElm)e).pnp == -1)
		    cs= "PTransistorElm";
		else
		    cs = "NTransistorElm";
	    }
	    s=cs;
	    for(j=0; j<e.getPostCount(); j++) {
		s=s+" "+e.nodes[j];
	    }
	    console(s);
	}
    }
    
	native void printCanvas(CanvasElement cv) /*-{
		var img    = cv.toDataURL("image/png");
		var iframe = $doc.createElement("iframe");
		iframe.src = img;
		iframe.style = "display:none";
		$doc.body.appendChild(iframe);
		var contentWindow = iframe.contentWindow;
		contentWindow.print();
		contentWindow.addEventListener('afterprint', function(){iframe.remove()});
	}-*/;

	void doDCAnalysis() {
	    dcAnalysisFlag = true;
	    resetAction();
	}
	
	void doPrint() {
	    Canvas cv = getCircuitAsCanvas(CAC_PRINT);
	    printCanvas(cv.getCanvasElement());
	}

	boolean loadedCanvas2SVG = false;

	boolean initializeSVGScriptIfNecessary(final String followupAction) {
		// 如果尚未加载 canvas2svg，则加载它
		if (!loadedCanvas2SVG) {
			ScriptInjector.fromUrl("canvas2svg.js").setCallback(new Callback<Void,Exception>() {
				public void onFailure(Exception reason) {
					Window.alert("Can't load canvas2svg.js.");
				}
				public void onSuccess(Void result) {
					loadedCanvas2SVG = true;
					if (followupAction.equals("doExportAsSVG")) {
						doExportAsSVG();
					} else if (followupAction.equals("doExportAsSVGFromAPI")) {
						doExportAsSVGFromAPI();
					}
				}
			}).inject();
			return false;
		}
		return true;
	}

	void doExportAsSVG() {
		if (!initializeSVGScriptIfNecessary("doExportAsSVG")) {
			return;
		}
		dialogShowing = new ExportAsImageDialog(CAC_SVG);
		dialogShowing.show();
	}

	public void doExportAsSVGFromAPI() {
		if (!initializeSVGScriptIfNecessary("doExportAsSVGFromAPI")) {
			return;
		}
		String svg = getCircuitAsSVG();
		callSVGRenderedHook(svg);
	}

	static final int CAC_PRINT = 0;
	static final int CAC_IMAGE = 1;
	static final int CAC_SVG   = 2;
	
	public Canvas getCircuitAsCanvas(int type) {
	    	// 创建用于绘制电路的画布
	    	Canvas cv = Canvas.createIfSupported();
	    	Rectangle bounds = getCircuitBounds();
	    	
		// 在边缘留出一些空间，因为边界计算并不精确
	    	int wmargin = 140;
	    	int hmargin = 100;
	    	int w = (bounds.width*2+wmargin) ;
	    	int h = (bounds.height*2+hmargin) ;
	    	cv.setCoordinateSpaceWidth(w);
	    	cv.setCoordinateSpaceHeight(h);
	    
		Context2d context = cv.getContext2d();
		drawCircuitInContext(context, type, bounds, w, h);
		return cv;
	}
	
	// 使用 canvas2svg 创建 SVG 上下文
	native static Context2d createSVGContext(int w, int h) /*-{
	    return new C2S(w, h);
	}-*/;
	
	native static String getSerializedSVG(Context2d context) /*-{
	    return context.getSerializedSvg();
	}-*/;
	
	public String getCircuitAsSVG() {
	    Rectangle bounds = getCircuitBounds();

	    // 在边缘留出一些空间，因为边界计算并不精确
	    int wmargin = 140;
	    int hmargin = 100;
	    int w = (bounds.width+wmargin) ;
	    int h = (bounds.height+hmargin) ;
	    Context2d context = createSVGContext(w, h);
	    drawCircuitInContext(context, CAC_SVG, bounds, w, h);
	    return getSerializedSVG(context);
	}
	
	void drawCircuitInContext(Context2d context, int type, Rectangle bounds, int w, int h) {
		Graphics g = new Graphics(context);
		context.setTransform(1, 0, 0, 1, 0, 0);
	    	double oldTransform[] = Arrays.copyOf(transform, 6);
	        
	        double scale = 1;
	        
		// 开启白色背景，关闭电流显示
		boolean p = printableCheckItem.getState();
		boolean c = dotsCheckItem.getState();
		boolean print = (type == CAC_PRINT);
		if (print)
		    printableCheckItem.setState(true);
	        if (printableCheckItem.getState()) {
	            CircuitElm.whiteColor = Color.black;
	            CircuitElm.lightGrayColor = Color.black;
	            g.setColor(Color.white);
	        } else {
	            CircuitElm.whiteColor = Color.white;
	            CircuitElm.lightGrayColor = Color.lightGray;
	            g.setColor(Color.black);
	        }
	        g.fillRect(0, 0, w, h);
		dotsCheckItem.setState(false);

	    	int wmargin = 140;
	    	int hmargin = 100;
	        if (bounds != null)
	            scale = Math.min(w /(double)(bounds.width+wmargin),
	                             h/(double)(bounds.height+hmargin));
	        
	        // ScopeElm 需要更新变换数组
		transform[0] = transform[3] = scale;
		transform[4] = -(bounds.x-wmargin/2);
		transform[5] = -(bounds.y-hmargin/2);
		context.scale(scale, scale);
		context.translate(transform[4], transform[5]);
		context.setLineCap(Context2d.LineCap.ROUND);
		
		// 绘制元件
		int i;
		for (i = 0; i != elmList.size(); i++) {
		    getElm(i).draw(g);
		}
		for (i = 0; i != postDrawList.size(); i++) {
		    CircuitElm.drawPost(g, postDrawList.get(i));
		}

		// 恢复所有设置
		printableCheckItem.setState(p);
		dotsCheckItem.setState(c);
		transform = oldTransform;
	}
	
	boolean isSelection() {
	    for (int i = 0; i != elmList.size(); i++)
		if (getElm(i).isSelected())
		    return true;
	    return false;
	}
	
	public CustomCompositeModel getCircuitAsComposite() {
	    int i;
	    String nodeDump = "";
	    String dump = "";
//	    String models = "";
	    CustomLogicModel.clearDumpedFlags();
	    DiodeModel.clearDumpedFlags();
	    TransistorModel.clearDumpedFlags();
            Vector<LabeledNodeElm> sideLabels[] = new Vector[] {
                new Vector<LabeledNodeElm>(), new Vector<LabeledNodeElm>(),
                new Vector<LabeledNodeElm>(), new Vector<LabeledNodeElm>()
            };
	    Vector<ExtListEntry> extList = new Vector<ExtListEntry>();
	    boolean sel = isSelection();
	    
	    boolean used[] = new boolean[nodeList.size()];
	    boolean extnodes[] = new boolean[nodeList.size()];
	    
	    // 重新进行节点分配，以避免自动分配地线
	    if (!preStampCircuit(true))
		return null;

	    // 找出所有带标签节点，获取它们的列表，并创建节点编号映射
	    for (i = 0; i != elmList.size(); i++) {
		CircuitElm ce = getElm(i);
		if (sel && !ce.isSelected())
		    continue;
		if (ce instanceof LabeledNodeElm) {
		    LabeledNodeElm lne = (LabeledNodeElm) ce;
		    String label = lne.text;
		    if (lne.isInternal())
			continue;
		    
		    // 已经添加到列表中了？
		    if (extnodes[ce.getNode(0)])
			continue;
		    
                    int side = ChipElm.SIDE_W;
                    if (Math.abs(ce.dx) >= Math.abs(ce.dy) && ce.dx > 0) side = ChipElm.SIDE_E;
                    if (Math.abs(ce.dx) <= Math.abs(ce.dy) && ce.dy < 0) side = ChipElm.SIDE_N;
                    if (Math.abs(ce.dx) <= Math.abs(ce.dy) && ce.dy > 0) side = ChipElm.SIDE_S;
                    
		    // 为外部节点创建外部端子列表条目
                    sideLabels[side].add(lne);
		    extnodes[ce.getNode(0)] = true;
		    if (ce.getNode(0) == 0) {
		        Window.alert("Node \"" + lne.text + "\" can't be connected to ground");
			return null;
		    }
		}
	    }
	    
            Collections.sort(sideLabels[ChipElm.SIDE_W], (LabeledNodeElm a, LabeledNodeElm b) -> Integer.signum(a.y - b.y));
            Collections.sort(sideLabels[ChipElm.SIDE_E], (LabeledNodeElm a, LabeledNodeElm b) -> Integer.signum(a.y - b.y));
            Collections.sort(sideLabels[ChipElm.SIDE_N], (LabeledNodeElm a, LabeledNodeElm b) -> Integer.signum(a.x - b.x));
            Collections.sort(sideLabels[ChipElm.SIDE_S], (LabeledNodeElm a, LabeledNodeElm b) -> Integer.signum(a.x - b.x));

            for (int side = 0; side < sideLabels.length; side++) {
                for (int pos = 0; pos < sideLabels[side].size(); pos++) {
                    LabeledNodeElm lne = sideLabels[side].get(pos);
                    ExtListEntry ent = new ExtListEntry(lne.text, lne.getNode(0), pos, side);
                    extList.add(ent);
                }
            }

	    // 输出所有元件
	    for (i = 0; i != elmList.size(); i++) {
		CircuitElm ce = getElm(i);
		if (sel && !ce.isSelected())
		    continue;
		// 不需要导出这些元件
		if (ce instanceof WireElm || ce instanceof LabeledNodeElm || ce instanceof ScopeElm)
		    continue;
		if (ce instanceof GraphicElm || ce instanceof GroundElm)
		    continue;
		int j;
		if (nodeDump.length() > 0)
		    nodeDump += "\r";
		nodeDump += ce.getClass().getSimpleName();
		for (j = 0; j != ce.getPostCount(); j++) {
		    int n = ce.getNode(j);
		    used[n] = true;
		    nodeDump += " " + n;
		}
		
	        // 保存位置
                int x1 = ce.x;  int y1 = ce.y;
                int x2 = ce.x2; int y2 = ce.y2;
                
                // 将它们设为 0，便于后续移除
                ce.x = ce.y = ce.x2 = ce.y2 = 0;

                String tstring = ce.dump();
                tstring = tstring.replaceFirst("[A-Za-z0-9]+ 0 0 0 0 ", ""); // 移除内部元件未使用的 tint_x1 y1 x2 y2 坐标
                
                // 恢复位置
                ce.x = x1; ce.y = y1; ce.x2 = x2; ce.y2 = y2;
                if (dump.length() > 0)
                    dump += " ";
                dump += CustomLogicModel.escape(tstring);
	    }
	    
	    for (i = 0; i != extList.size(); i++) {
		ExtListEntry ent = extList.get(i);
		if (!used[ent.node]) {
		    Window.alert("Node \"" + ent.name + "\" is not used!");
		    return null;
		}
	    }
	
	    boolean first = true;
	    for (i = 0; i != unconnectedNodes.size(); i++) {
		int q = unconnectedNodes.get(i);
		if (!extnodes[q] && used[q]) {
		    if (nodesWithGroundConnectionCount == 0 && first) {
			first = false;
			continue;
		    }
		    Window.alert("Some nodes are unconnected!");
		    return null;
		}
	    }	    

	    CustomCompositeModel ccm = new CustomCompositeModel();
	    ccm.nodeList = nodeDump;
	    ccm.elmDump = dump;
	    ccm.extList = extList;
	    return ccm;
	}
	
	static void invertMatrix(double a[][], int n) {
	    int ipvt[] = new int[n];
	    lu_factor(a, n, ipvt);
	    int i, j;
	    double b[] = new double[n];
	    double inva[][] = new double[n][n];
	    
	    // 为单位矩阵的每一列求解
	    for (i = 0; i != n; i++) {
		for (j = 0; j != n; j++)
		    b[j] = 0;
		b[i] = 1;
		lu_solve(a, n, ipvt, b);
		for (j = 0; j != n; j++)
		    inva[j][i] = b[j];
	    }
	    
	    // 结果返回到原矩阵中
	    for (i = 0; i != n; i++)
		for (j = 0; j != n; j++)
		    a[i][j] = inva[i][j];
	}
	
	double getLabeledNodeVoltage(String name) {
	    Integer node = LabeledNodeElm.getByName(name);
	    if (node == null || node == 0)
		return 0;
	    // 减一是因为 nodeVoltages[] 中不包含地线
	    return nodeVoltages[node.intValue()-1];
	}
	
	void setExtVoltage(String name, double v) {
	    int i;
	    for (i = 0; i != elmList.size(); i++) {
		CircuitElm ce = getElm(i);
		if (ce instanceof ExtVoltageElm) {
		    ExtVoltageElm eve = (ExtVoltageElm) ce;
		    if (eve.getName().equals(name))
			eve.setVoltage(v);
		}
	    }
	}

	native JsArray<JavaScriptObject> getJSArray() /*-{ return []; }-*/;
	
	JsArray<JavaScriptObject> getJSElements() {
	    int i;
	    JsArray<JavaScriptObject> arr = getJSArray();
	    for (i = 0; i != elmList.size(); i++) {
		CircuitElm ce = getElm(i);
		ce.addJSMethods();
		arr.push(ce.getJavaScriptObject());
	    }
	    return arr;
	}
	
	native void setupJSInterface() /*-{
	    var that = this;
	    $wnd.CircuitJS1 = {
	        setSimRunning: $entry(function(run) { that.@com.lushprojects.circuitjs1.client.CirSim::setSimRunning(Z)(run); } ),
	        getTime: $entry(function() { return that.@com.lushprojects.circuitjs1.client.CirSim::t; } ),
	        getTimeStep: $entry(function() { return that.@com.lushprojects.circuitjs1.client.CirSim::timeStep; } ),
	        setTimeStep: $entry(function(ts) { that.@com.lushprojects.circuitjs1.client.CirSim::timeStep = ts; } ), // 不要使用此项，参见 #843
	        getMaxTimeStep: $entry(function() { return that.@com.lushprojects.circuitjs1.client.CirSim::maxTimeStep; } ),
	        setMaxTimeStep: $entry(function(ts) { that.@com.lushprojects.circuitjs1.client.CirSim::maxTimeStep = 
                                                      that.@com.lushprojects.circuitjs1.client.CirSim::timeStep = ts; } ),
	        isRunning: $entry(function() { return that.@com.lushprojects.circuitjs1.client.CirSim::simIsRunning()(); } ),
	        getNodeVoltage: $entry(function(n) { return that.@com.lushprojects.circuitjs1.client.CirSim::getLabeledNodeVoltage(Ljava/lang/String;)(n); } ),
	        setExtVoltage: $entry(function(n, v) { that.@com.lushprojects.circuitjs1.client.CirSim::setExtVoltage(Ljava/lang/String;D)(n, v); } ),
	        getElements: $entry(function() { return that.@com.lushprojects.circuitjs1.client.CirSim::getJSElements()(); } ),
	        getCircuitAsSVG: $entry(function() { return that.@com.lushprojects.circuitjs1.client.CirSim::doExportAsSVGFromAPI()(); } ),
	        exportCircuit: $entry(function() { return that.@com.lushprojects.circuitjs1.client.CirSim::dumpCircuit()(); } ),
	        importCircuit: $entry(function(circuit, subcircuitsOnly) { return that.@com.lushprojects.circuitjs1.client.CirSim::importCircuitFromText(Ljava/lang/String;Z)(circuit, subcircuitsOnly); }),
			redrawCanvasSize: $entry(function() { return that.@com.lushprojects.circuitjs1.client.CirSim::redrawCanvasSize()(); } ),
			allowSave: $entry(function(b) { return that.@com.lushprojects.circuitjs1.client.CirSim::allowSave(Z)(b);})
	    };
	    var hook = $wnd.oncircuitjsloaded;
	    if (hook)
	    	hook($wnd.CircuitJS1);
	}-*/;
	
	native void callUpdateHook() /*-{
	    var hook = $wnd.CircuitJS1.onupdate;
	    if (hook)
	    	hook($wnd.CircuitJS1);
	}-*/;
	
        native void callAnalyzeHook() /*-{
            var hook = $wnd.CircuitJS1.onanalyze;
            if (hook)
                hook($wnd.CircuitJS1);
    	}-*/;
    

	native void callTimeStepHook() /*-{
	    var hook = $wnd.CircuitJS1.ontimestep;
	    if (hook)
	    	hook($wnd.CircuitJS1);
	}-*/;
	
	native void callSVGRenderedHook(String svgData) /*-{
		var hook = $wnd.CircuitJS1.onsvgrendered;
		if (hook)
			hook($wnd.CircuitJS1, svgData);
	}-*/;

	class UndoItem {
	    public String dump;
	    public double scale, transform4, transform5;
	    UndoItem(String d) {
		dump = d;
		scale = transform[0];
		transform4 = transform[4];
		transform5 = transform[5];
	    }
	}

}

