package com.lushprojects.circuitjs1.client;

public class ImportFromDropbox {

	
	static CirSim sim;
	
	ImportFromDropbox( CirSim asim ){
		sim=asim;
//		CirSim.console("importing");
		doDropboxImport();
//		CirSim.console("returned");
	}
	
	static public final native boolean isSupported() 
	/*-{
		try {
			// Firefox 中的 Bug 会阻止 Dropbox 对话框在此应用中正常工作，
			// 尽管 Dropbox chooser 支持 firefox
			// 参见 https://github.com/gwtproject/gwt/issues/7923
			if (/Firefox[\/\s](\d+\.\d+)/.test(navigator.userAgent))
				return false;
			return !!($wnd.Dropbox.isBrowserSupported());
		} 
		catch(err) {
			return false;
		}
 	}-*/;
	
	static public void doLoadCallback(String s) {
		sim.pushUndo();
		sim.readCircuit(s);
	}
	
	
	public final native void doDropboxImport() 
	/*-{
		var options = {

		    // 必需。当用户在 Chooser 中选择一个项目时调用。
		    success: function(files) {
		    	try {
			        //console.log("Here's the file link: " + files[0].link);
			        if (files[0].bytes < 100000) {
				        var xhr= new XMLHttpRequest();
				        xhr.addEventListener("load", function reqListener() { 
	//			        	console.log(xhr.responseText);
				        	var text = xhr.responseText;
	      					@com.lushprojects.circuitjs1.client.ImportFromDropbox::doLoadCallback(Ljava/lang/String;)(text);
				        });
			        }
			        xhr.open("GET", files[0].link, false);
			        xhr.send();
		    	}
		        catch(err) {
		        } 
		    },
		
		    // 可选。当用户未选择文件就关闭对话框时调用，
		    // 且不包含任何参数。
		    // cancel: function() {
		
		    //},
		
		    // 可选。"preview"（默认）是用于分享的文档预览链接，
		    // "direct" 是用于下载文件内容的过期链接。有关
		    // 链接类型的更多信息，请参见下面的 Link types。
		    linkType: "direct", // "preview" 或 "direct"
		
		    // 可选。false（默认）将选择限制为单个文件，而
		    // true 则允许多文件选择。
		    multiselect: false, // 或 true
		
		    // 可选。这是文件扩展名列表。如果指定，用户将
		    // 只能选择具有这些扩展名的文件。你也可以在列表中指定
		    // 文件类型，例如 "video" 或 "images"。更多信息
		    // 请参见下面的 File types。默认情况下，允许所有扩展名。
		    // extensions: ['.pdf', '.doc', '.docx'],
		};
		$wnd.Dropbox.choose(options);
	 }-*/;
}
