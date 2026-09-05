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

import java.util.HashMap;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.storage.client.Storage;
import com.google.gwt.user.client.Window;
import com.lushprojects.circuitjs1.client.util.Locale;

public class circuitjs1 implements EntryPoint {

    public static final String versionString = "3.1.3js";

    // 如果服务器在与电路模拟器相同的目录中
    // 运行 shortrelay.php 文件，则设为 true
    public static final boolean shortRelaySupported = false;

    static CirSim mysim;

    // 这是程序入口点！ 
    // 由 gtw 自动调用（参见 circuitjs1.gwt.xml）
    public void onModuleLoad() {
        // loadLocale() 在确定语言后启动模拟器（见下文）
        loadLocale();
    }

    native String language() /*-{
        if (navigator.languages) {
            if (navigator.languages.length > 0) {
                return navigator.languages[0];
            } else {
                // 在 Electron 中，navigator.languages 返回一个空数组
                return "en-US";
            }
        } else {
            return (navigator.language || navigator.userLanguage);  
        }
    }-*/;

    void loadLocale() {
        String url;
        QueryParameters qp = new QueryParameters();
        String lang = qp.getValue("lang");
        if (lang == null) {
            Storage stor = Storage.getLocalStorageIfSupported();
            if (stor != null)
                lang = stor.getItem("language");
            if (lang == null)
                lang = language();
        }

        GWT.log("got language " + lang);

        // 检查台湾中文。否则，去掉地区代码
        if (lang.equalsIgnoreCase("zh-tw") || lang.equalsIgnoreCase("zh-cht"))
            lang = "zh-tw";
        else
            lang = lang.replaceFirst("-.*", "");

        if (lang.startsWith("en")) {
            // 英文无需加载语言文件
            HashMap<String, String> localizationMap = new HashMap<String, String>();
            loadSimulator(localizationMap);
            return;
        }
        
        url = GWT.getModuleBaseURL() + "locale_" + lang + ".txt";
        RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.GET, url);
        try {
            requestBuilder.sendRequest(null, new RequestCallback() {
                public void onError(Request request, Throwable exception) {
                    GWT.log("File Error Response", exception);
                }

                public void onResponseReceived(Request request, Response response) {
                    HashMap<String, String> localizationMap;
                    if (response.getStatusCode() == Response.SC_OK) {
                        String text = response.getText();
                        localizationMap = processLocale(text);
                    } else {
                        GWT.log("Bad file server response: " + response.getStatusText());
                        // 如果获取语言时出现错误， 
                        // 则默认使用英文（空映射）
                        localizationMap = new HashMap<String, String>();
                    }
                    loadSimulator(localizationMap);
                }
            });
        } catch (RequestException e) {
            GWT.log("failed file reading", e);
        }

    }

    static String convertUnicodeEscapes(String input) {
	if (input.indexOf("\\u") < 0)
	    return input;
        StringBuilder result = new StringBuilder();
        int length = input.length();
        int i = 0;

        while (i < length) {
            if (i + 5 < length && input.charAt(i) == '\\' && input.charAt(i + 1) == 'u') {
                // 找到 Unicode 转义序列
                String hexCode = input.substring(i + 2, i + 6);
                try {
                    // 将十六进制代码转换为 Unicode 字符
                    int codePoint = Integer.parseInt(hexCode, 16);
                    result.append((char) codePoint);
                    i += 6;  // 跳过转义序列
                } catch (NumberFormatException e) {
                    // 如果十六进制代码无效，则按原样追加
                    result.append("\\u").append(hexCode);
                    i += 6;
                }
            } else {
                // 普通字符，直接追加
                result.append(input.charAt(i));
                i++;
            }
        }
        return result.toString();
    }

    HashMap<String, String> processLocale(String data) {
        HashMap<String, String> localizationMap = new HashMap<String, String>();
        String lines[] = data.split("\r?\n");
        for (int i = 0; i != lines.length; i++) {
            String line = lines[i];
            if (line.length() == 0)
                continue;
            if (line.charAt(0) != '"') {
                CirSim.console("ignoring line in string catalog: " + line);
                continue;
            }
	    line = convertUnicodeEscapes(line);
            int q2 = line.indexOf('"', 1);
            if ((q2 < 0)
                || (line.charAt(q2 + 1) != '=')
                || (line.charAt(q2 + 2) != '"')
                || (line.charAt(line.length() - 1) != '"')) {
                CirSim.console("ignoring line in string catalog: " + line);
                continue;
            }
            String str1 = line.substring(1, q2);
            String str2 = line.substring(q2 + 3, line.length() - 1);
            localizationMap.put(str1, str2);
        }
        return localizationMap;
    }

    public void loadSimulator(HashMap<String, String> localizationMap) {
        Locale.localizationMap = localizationMap;
        mysim = new CirSim();
        mysim.init();

        Window.addResizeHandler(new ResizeHandler() {
            public void onResize(ResizeEvent event) {
                mysim.setCanvasSize();
                mysim.setSlidersPanelHeight();
            }
        });

        mysim.updateCircuit();
    }

}
