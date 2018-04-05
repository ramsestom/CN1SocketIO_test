package com.tbdlab.cn1socketiotest;

import java.util.ArrayList;
import java.util.List;

import com.codename1.io.Log;
import com.codename1.javascript.JSFunction;
import com.codename1.javascript.JSObject;
import com.codename1.javascript.JavascriptContext;
import com.codename1.ui.BrowserComponent;
import com.codename1.ui.BrowserComponent.JSRef;
import com.codename1.ui.BrowserComponent.JSType;
import com.codename1.ui.events.ActionEvent;
import com.codename1.ui.events.ActionListener;
import com.codename1.util.SuccessCallback;

public class SocketIO {

	public static final String EVENT_CONNECT = "connect";
    public static final String EVENT_CONNECTING = "connecting";
    public static final String EVENT_CONNECT_ERROR = "connect_error";
    public static final String EVENT_CONNECT_TIMEOUT = "connect_timeout";
    public static final String EVENT_DISCONNECT = "disconnect";
    public static final String EVENT_RECONNECT = "reconnect";
    public static final String EVENT_RECONNECT_ATTEMPT = "reconnect_attempt";
    public static final String EVENT_RECONNECTING = "reconnecting";
    public static final String EVENT_RECONNECT_ERROR = "reconnect_error";
    public static final String EVENT_RECONNECT_FAILED = "reconnect_failed";
    public static final String EVENT_ERROR = "error";
    public static final String EVENT_MESSAGE = "message";
    public static final String EVENT_PING = "ping";
    public static final String EVENT_PONG = "pong";
   
    
	private BrowserComponent internalBrowser;
	
	
	public SocketIO(String server_url) 
	{
		internalBrowser = new BrowserComponent();
		//internalBrowser.setDebugMode(true);
		initBrowserComponent(server_url);
	}
	
	
	private void initBrowserComponent(final String server_url) 
	{
		try
		{
			/*
			String pagecont = "<html>\r\n" + 
			"	<head>\r\n" + 
			"		<script type=\"text/javascript\" src=\"http://192.168.8.100:3030/socket.io/socket.io.js\" />\r\n" + 
			"	</head>\r\n" + 
			"	<body>\r\n" + 
			"		Hello world\r\n" + 
			"   </body>\r\n" + 
			"</html>";
			internalBrowser.setPage(pagecont, "/");
			*/
			
			internalBrowser.setProperty("AllowFileAccessFromFileURLs", Boolean.TRUE);
			internalBrowser.setProperty("AllowUniversalAccessFromFileURLs", Boolean.TRUE);
						
			internalBrowser.setURL("jar:///socket_io.html");
			internalBrowser.addWebEventListener("onLoad", new ActionListener<ActionEvent>() {
	                public void actionPerformed(ActionEvent evt) {
	                	
	                	Log.p("Browser component onload called");
	                	
	                	
	                	JavascriptContext ctx = new JavascriptContext(internalBrowser);
	                	JSObject logger = (JSObject)ctx.get("{}");
	                	logger.set("log", new JSFunction() {
	                		public void apply(JSObject self, Object[] args) {
	                			String msg = (String)args[0];
	                			Log.p("[Javascript Logger] "+msg);
	                		}

	                	});
	                	ctx.set("window.logger", logger);

	                	internalBrowser.execute("logger.log(\"SocketIO bridge init\")");
	                	
	                	internalBrowser.execute("const socket = io('"+server_url+"')");
	                   	                    
	                    internalBrowser.execute("logger.log(\"SocketIO bridge started\")");
	                   	                   	                    
	                    browserBridge.ready = true;
	                    browserBridge.ready(null);
	                    
	                    Log.p("Browser component onload end");
	                }
			 });
		}
		catch (Exception ex) {
            ex.printStackTrace();
        }
	}
	
	
	public BrowserComponent getBrowserComponent() {
		return this.internalBrowser;
	}
	
	
	public String id() {
		if (browserBridge.ready) {
			return internalBrowser.executeAndWait("callback.onSuccess(socket.id)").getValue();
		}
		else {
			return null;
		}
	}
	
	public SocketIO open() {
		if (browserBridge.ready) {
			internalBrowser.execute("socket.open()");
		}
		else {
			browserBridge.onReady.add(new Runnable() {
			    public void run() {
			    	internalBrowser.execute("socket.open()");
			    }
			});
		}
		return this;
	}
	
	public SocketIO connect() {
		return this.open();
	}
	
	public SocketIO send(final String[] args, final SuccessCallback<String> callback) {
		return this.emit("message", args, callback);
	}
	
	//public SocketIO emit(final String enventName, final SuccessCallback<String> callback, final String... args) {
	public SocketIO emit(final String eventName, final String[] args, final SuccessCallback<String> callback) {
		String jscmd = "socket.emit('"+eventName+"'";
		if (args!=null) {
			for(String arg : args){
				if (arg != null) {
					jscmd+=", "+arg;
				}
		    }
		}
		jscmd+=", (...data) => {logger.log(\"emit "+eventName+" callback data:\"+JSON.stringify(data)); callback.onSuccess(JSON.stringify(data))})"; //jscmd+=", (...data) => {callback.onSuccess(JSON.stringify(data))})"; //jscmd+=", (...data) => {logger.log(\"emit "+enventName+" callback data:\"+JSON.stringify(data)); callback.onSuccess(JSON.stringify(data))})";
				
		if (browserBridge.ready) {
			internalBrowser.addJSCallback(jscmd, wrapCallback(callback));
		}
		else {
			//System.out.println("Will add callback to ready queue");
			final String fjscmd = jscmd;
			browserBridge.onReady.add(new Runnable() {
			    public void run() {
			    	//System.out.println("Perform "+fjscmd+" js callback");
			    	internalBrowser.addJSCallback(fjscmd, wrapCallback(callback));
			    }
			});
		}
		return this;
	}
	
	public SocketIO on(final String eventName, final SuccessCallback<String> callback) {
		if (browserBridge.ready) {
			//internalBrowser.addJSCallback("socket.on('"+enventName+"', (...data) => {logger.log(\"data on "+enventName+" callback:\"+JSON.stringify(data)); callback.onSuccess(JSON.stringify(data))})", wrapCallback(callback));
			internalBrowser.addJSCallback("socket.on('"+eventName+"', (...data) => {callback.onSuccess(JSON.stringify(data))})", wrapCallback(callback));
		}
		else {
			browserBridge.onReady.add(new Runnable() {
			    public void run() {
			    	//internalBrowser.addJSCallback("socket.on('"+enventName+"', (...data) => {logger.log(\"data on "+enventName+" callback:\"+JSON.stringify(data)); callback.onSuccess(JSON.stringify(data))})", wrapCallback(callback));
			    	internalBrowser.addJSCallback("socket.on('"+eventName+"', (...data) => {callback.onSuccess(JSON.stringify(data))})", wrapCallback(callback));
			    }
			});
		}
		return this;
	}
	
	
	public SocketIO once(final String eventName, final SuccessCallback<String> callback) {
		if (browserBridge.ready) {
			internalBrowser.addJSCallback("socket.once('"+eventName+"', (...data) => {callback.onSuccess(JSON.stringify(data))})", wrapCallback(callback));
		}
		else {
			browserBridge.onReady.add(new Runnable() {
			    public void run() {
			    	internalBrowser.addJSCallback("socket.once('"+eventName+"', (...data) => {callback.onSuccess(JSON.stringify(data))})", wrapCallback(callback));
			    }
			});
		}
		return this;
	}
	
	
	public SocketIO compress(final boolean value) {
		if (browserBridge.ready) {
			internalBrowser.execute("socket.compress("+value+")");
		}
		else {
			browserBridge.onReady.add(new Runnable() {
			    public void run() {
			    	internalBrowser.execute("socket.compress("+value+")");
			    }
			});
		}
		return this;
	}
	
	public SocketIO close() {
		if (browserBridge.ready) {
			internalBrowser.execute("socket.close()");
		}
		else {
			browserBridge.onReady.add(new Runnable() {
			    public void run() {
			    	internalBrowser.execute("socket.close()");
			    }
			});
		}
		return this;
	}
	
	public SocketIO disconnect() {
		return this.close();
	}
	
	
	
	
	private static SuccessCallback<JSRef> wrapCallback(final SuccessCallback<String> callback){
		if (callback==null) {
			return null;
		}
		else {
			return new SuccessCallback<JSRef>() {
				public void onSucess(JSRef value) {
					//System.out.println("JSRef value: "+value.getValue());
					callback.onSucess((value==null)?null:value.getValue());
				}
			};
		}
	}
	
	
	private BrowserBridge browserBridge = new BrowserBridge();
		
	private class BrowserBridge {
        List<Runnable> onReady = new ArrayList<Runnable>();
        boolean ready;
         
        BrowserBridge() {
        }
        
        private void ready(Runnable r) {
            if (ready) {
            	//System.out.println("browser ready");
                if (!onReady.isEmpty()) {
                	//System.out.println(onReady.size()+" ready tasks to perform");
                    List<Runnable> tmp = new ArrayList<Runnable>();
                    synchronized(onReady) {
                        tmp.addAll(onReady);
                        onReady.clear();
                    }
                    for (Runnable tr : tmp) {
                    	tr.run();
                    }
                }
                if (r != null) {
                    r.run();
                }
            } else {
                if (r == null) {
                    return;
                }
                
                synchronized(onReady) {
                    onReady.add(r);
                }
            }
        }
        
    }
	
	
	private void checkBridgeReady(final SuccessCallback<Boolean> callback) {
        if (internalBrowser == null) {
            callback.onSucess(false);
            return;
        }
        internalBrowser.execute("callback.onSuccess(socket)", new SuccessCallback<JSRef>() {
            public void onSucess(JSRef value) {
            	if (value.getJSType() == JSType.OBJECT || value.getJSType() == JSType.FUNCTION) {
                    callback.onSucess(true);
                }
            }
        });
        
    }
	
}
