package yoga1290.twtblog;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.*;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.content.*;


public class Twtblog extends Activity implements  android.view.View.OnClickListener {
//	TextView query;
	EditText query;
	LinearLayout ll,dll;
	Button ok,refresh;
	private data d;
	
	private int n=0;
	
	private TextView hostv;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ll=new LinearLayout(this);
        
        ll.setOrientation(LinearLayout.VERTICAL); 
        ll.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
        
        query=new EditText(ll.getContext());
        query.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
        
        
	    ok=new Button(ll.getContext());
	    ok.setText("+");
	    ok.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
	    ok.setOnClickListener(this);

	   ll.addView(query);
       ll.addView(ok);
 
        
        d=new data(this);        
        setContentView(ll);
        
        
        new HTTPServer(this,d).start();
   //     new Thread(this).start();
        hostv=new TextView(this);
        try{
        	hostv.setText("http://"+InetAddress.getLocalHost().getHostAddress()+":1290");
        }catch(Exception e){}
        ll.addView(hostv);
    }
    
	public void onClick(View v) {
		if(v.equals(ok))
		{
			boolean pass=false;
			ok.setEnabled(false);
			
			 String twt,re,entries[],inputLine,res="",tags[];
		        BufferedReader in;
		        int i,j;
		        String er="";
//		    while(!pass)
//		    {
		    	pass=true;
		    	hostv.setText("Connecting...");
					try {
		                 in = new BufferedReader(
		                                         new InputStreamReader(
		                                        new URL("http://search.twitter.com/search.atom?q="+(""+query.getText()).replace("#", "%23").replace("@","@")).openConnection().getInputStream() ));
		                hostv.setText("Strat reading..");
		                while ((inputLine = in.readLine()) != null)
		                    res+=inputLine;
		                in.close();
		                entries=res.split("</entry>");
		                er=entries.length+"";
		                for(i=0;i<entries.length-1;i++)
		                {
		                    tags=new String[]{};
		                    twt=entries[i].substring(entries[i].lastIndexOf("<title>")+"<title>".length(),entries[i].lastIndexOf("</title>") );
		                    twt=twt.replace("\r\n","<br>");
		                    twt=twt.replace("\n","<br>");
		                    System.out.println(">>>"+twt);
		                    re=entries[i].substring(0,entries[i].lastIndexOf("\" rel=\"alternate\"/>"));
		                    re=re.substring(re.lastIndexOf("href=\"")+"href=\"".length());
		                    tags=SplitTags(twt);
		     //               twt=twt.replace("'","\'");
		    //                twt=twt.replace("\"","\\"+"\"");
		                    for(j=0;j<tags.length;j++)
		                        d.add(tags[j].toLowerCase(), re+"\n"+twt);
		                }
		                
		                hostv.setTextColor(Color.WHITE);
		                hostv.setText("http://"+InetAddress.getLocalHost().getHostAddress()+":1290");
		                d.save();
		                
					}catch(Exception e){er="Err"+e.getMessage();pass=false;}
					hostv.setText(er+d.getTagNames());
		    //}
			
			ok.setEnabled(true);
		}
	}
	
    public static String[] SplitTags(String txt)
    {
        int eInd;
        String t;
        HashSet<String> hs=new HashSet<String>();
        Vector<String> v=new Vector<String>();
        while( ( eInd = txt.indexOf('#') ) >-1)
        {
            eInd++;
            if(
                    (eInd-2>=0 && !( 'a'<=txt.charAt(eInd-2)&&txt.charAt(eInd-2)<='z' )
                         && !( 'A'<=txt.charAt(eInd-2)&&txt.charAt(eInd-2)<='Z' )
                         && !( '0'<=txt.charAt(eInd-2)&&txt.charAt(eInd-2)<='9' )
                         && txt.charAt(eInd-2)!='&' )
               || eInd<=1     )
            {
                while(eInd<txt.length()&&
                            (
                                    ('a'<=txt.charAt(eInd)&&txt.charAt(eInd)<='z' )
                                ||  ('A'<=txt.charAt(eInd)&&txt.charAt(eInd)<='Z' )
                                ||  ('0'<=txt.charAt(eInd)&&txt.charAt(eInd)<='9' )
                            )
                        )
                    eInd++;
                
                t=txt.substring(txt.indexOf('#')+1,eInd);
               if(eInd-txt.indexOf('#')+1>0 && !hs.contains(t))
                    {
                        hs.add(t);
                        v.add(t);
                    }
            }
            txt=txt.substring( eInd );
        }
        String res[]=new String[v.size()];
        v.toArray(res);
        return res;
    }
    
}



class data
{
    private TreeMap<String,tagTweets> map;
    private Vector<String> tagnames;
    public static long sleepTime=30*60*1000;
    private Context con;
    public data(Context con)
    {
    	this.con=con;
        map=new TreeMap<String, tagTweets>();
        tagnames=new Vector<String>();
    }
    public int getSizeOf(String tagName)
    {
    	if(map.containsKey(tagName.toLowerCase()))
    		return map.get(tagName.toLowerCase()).getSize();
    	return 0;
    }
    public int getSize()
    {
    	int res=0;
    	for(String t:tagnames)
    		if(map.containsKey(t))
    			res+=map.get(t).getSize();
    	return res;
    }
    public void add(String tag,String txt)
    {
        if(!map.containsKey(tag.toLowerCase()))
        {
            tagnames.add(tag.toLowerCase());
            map.put(tag.toLowerCase(), new tagTweets(tag.toLowerCase()));
        }
        map.get(tag.toLowerCase()).add(txt);
    }
    public String getTagText(String tag)
    {
    	if(!map.containsKey(tag.toLowerCase()))
    		return "";
    	String res="",a[]=map.get(tag.toLowerCase()).getTwts();
    	for(String t:a)
    		res+=t+"\n"; //TODO check the new line
    	return res;
    }
    public String getTagNames()
    {
    	String res="",ar[]=new String[tagnames.size()];
    	tagnames.toArray(ar);
    	for(String t:ar)
    		res+=t+"\n"; //TODO Check the new line
    	return res;
    }
    public void save()
    {
        try{
            FileInputStream fin;
            FileOutputStream fout;
            String twt[],re[],all="";
            DataInputStream dis;
            int i,j;
            HashSet<String> oldRe=new HashSet<String>();
            String allFiles[]=con.fileList();
            for(i=0;i<tagnames.size();i++)
            {
                all="";
                System.err.println("ok loop");
                re=new String[]{};
                
                for(String file:allFiles)
                	if(file.equals(tagnames.get(i).toLowerCase()))
		                {
		                    fin=con.openFileInput(tagnames.get(i).toLowerCase());
		                    byte res[]=new byte[fin.available()];
		                    all="";
		                    String t;
		                    dis=new DataInputStream(fin);
		                    while( (t=dis.readLine()) !=null)
		                        all+=t+"\n";
		                    dis.close();
		                   // fin.read(res);
		                   // all=new String(res);
		                    fin.close();
		                    re=all.split("\n");
		                }
                System.err.println("ok file read");
                // Memorize URLs of old tweets in this single file
                for(j=0;j<re.length;j+=2)
                    oldRe.add(re[j].toLowerCase());
                twt=map.get(tagnames.get(i).toLowerCase()).getTwts();
                
                
                for(j=0;j<twt.length;j++)
                    if(!oldRe.contains(twt[j].split("\n")[0])) // If this Tweet is New!
                    {
                        System.err.println(">>"+tagnames.get(i).toLowerCase()+":\n"+twt[j]+"\n\n");
                        all+=twt[j]+"\n";
                    }
                fout=con.openFileOutput(tagnames.get(i).toLowerCase(), Context.MODE_PRIVATE);//new FileOutputStream(tagnames.get(i).toLowerCase());
                fout.write(all.getBytes());
                fout.close();
            }
            
         //   String old[]=new String[]{};
            HashSet<String> oldTagNames=new HashSet<String>();
            for(String file:allFiles)
	            if(file.equals("_tags"))
	            {
	                    fin=con.openFileInput("_tags");
	                    //Add oldTags with the latest 1s
	                    all="";
	                    String t;
	                    dis=new DataInputStream(fin);
	                    while( (t=dis.readLine()) !=null)
	                    {
	                        if(!map.containsKey(t.toLowerCase()))
	                          tagnames.add(t.toLowerCase());
	                    }
	                    fin.close();
	            }
            //Add oldTags with the latest 1s
            String res="";
            for(i=0;i<tagnames.size();i++)
                res+=tagnames.get(i).toLowerCase()+"\n";
          //  new FileOutputStream("_tags").write(res.getBytes());
            fout=con.openFileOutput("_tags", Context.MODE_PRIVATE);
            fout.write(res.getBytes());
            fout.close();
        }catch(Exception e){System.err.println("Save:"+e);}
        ///
    }
}





class tagTweets
{
    private String name;
    private Vector<String> twts;
    public tagTweets(String tagName)
    {
        this.name=tagName;
        twts=new Vector<String>();
    }
    public void add(String txt)
    {
        twts.add(txt);
    }
    public String[] getTwts()
    {
        String res[]=new String[twts.size()];
        twts.toArray(res);
        return res;
    }
    public int getSize()
    {
    	return twts.size();
    }
}


//Sever -Starter
class HTTPServer extends Thread
{
	private data d;
	private Context con;
	public HTTPServer(Context con,data d)
	{
		this.d=d;
		this.con=con;
	}
	@Override
	public void run() {
        try{
        ServerSocket ss = new ServerSocket(1290);
        while(true){
            Socket s = ss.accept();
            new Thread(new FileRequest(con,s,d)).start();
        }
        }catch(Exception ex){System.err.println(ex);}
	}	
}
//Handling Client Requests:
class FileRequest implements Runnable{
    public boolean send=false;
    private data d;
    private Context con;
    FileRequest(Context con,Socket s,data d){
     //   this.app=app;
    	this.d=d;
    	this.con=con;
        client = s;
    } //*/
    public void run() {

        if(requestRead()){
            if(fileOpened()){
                constructHeader();
                if(fileSent()){
// app.display("*File: "+fileName+" File Transfer Complete*Bytes Sent:"+bytesSent+"\n");
                }
            }
        }
          try{
            dis.close();
            client.close();
        }catch(Exception e){System.err.println(e);}

    }


    private boolean fileSent()
    {
        try{
DataOutputStream clientStream = new DataOutputStream
(new BufferedOutputStream(client.getOutputStream()));
            clientStream.writeBytes(header);
      //      app.display("******** File Request *********\n"+
          //              "******* "+ fileName +"*********\n"+header);
            int i;
            bytesSent = 0;
            while((i=requestedFile.read()) != -1){
                clientStream.writeByte(i);
                bytesSent++;
            }
            clientStream.flush();
            clientStream.close();
                 }catch(IOException e){System.err.println("SEND>"+e);return false;}
                 return true;

    }
    private boolean fileOpened()
    {
//        ByteInputStream bis=new ByteInputStream("".getBytes(), 0);
  //      DataInputStream dis=new DataInputStream(bis);
    	send=true;//just to make sure
        if(send && !fileName.equals("index.html"))
        {
        try{
        	//TODO setIO
        	
            requestedFile =new DataInputStream(new DataInputStream(con.openFileInput(fileName)));
            		//new ByteArrayInputStream("Hisashi buri-yoga1290".getBytes()));// new DataInputStream(new BufferedInputStream(new FileInputStream(fileName)));
                            fileLength = requestedFile.available();
          //  requestedFile = new DataInputStream(new BufferedInputStream
           /// ((this.getClass().getResourceAsStream("/"+fileName))));
              //              fileLength = requestedFile.available();

                }catch(FileNotFoundException e){
                if(fileName.equals("filenfound.html")){return false;}
                fileName="filenfound.html";
                if(!fileOpened()){return false;}
            }catch(Exception e){System.err.println("Open>"+e);return false;}

        }
        else
        {
        	String res="";
        	if(fileName.equals("_tags"))
        		res=d.getTagNames();
        	else if(fileName.equals("index.html"))
        		res=getHomePage();
        	else //if(fileName.indexOf(".png")<0)
        		res=d.getTagText(fileName);
        	
            try{
            requestedFile = new DataInputStream(new ByteArrayInputStream(res.getBytes()));
                fileLength = requestedFile.available();
            }catch(Exception e){
                System.err.println("Open>>"+e);
                return false;}
        }
                        return true;

    }
    private boolean requestRead()
    {
         try{
            //Open inputStream and read(parse) the request
            dis = new DataInputStream(client.getInputStream());
            String line;
            send=false;
            while((line=dis.readLine())!=null){

                StringTokenizer tokenizer = new StringTokenizer(line," ");
                if(!tokenizer.hasMoreTokens()){ break;}

                        if(tokenizer.nextToken().equals("GET")){

                            fileName = tokenizer.nextToken();
                            if(fileName.equals("/")){
                                fileName = "index.html";
                            }else{
                                fileName = fileName.substring(1);
                                if(fileName.indexOf('?')!=-1)
                                {
                                    send=false; //DISABLE SENDING ANY FILES
/*                                    if(!fileName.substring(0,fileName.indexOf('?')).equals(keyy)) return false;
                                    
                                    try{
                                    if(rob==null)   rob=new Robot();
                                    if(fileName.indexOf('=')!=-1)   rob.mouseMove(Integer.parseInt(fileName.substring(fileName.indexOf('=')+1,fileName.indexOf(',')) )
                                            ,Integer.parseInt(fileName.substring(fileName.lastIndexOf('=')+1,fileName.length()) ) );
                                    else if(fileName.indexOf("lc")!=-1){ rob.mousePress(InputEvent.BUTTON1_MASK);rob.mouseRelease(InputEvent.BUTTON1_MASK);}
                                    else
                                        { rob.mousePress(InputEvent.BUTTON3_MASK);rob.mouseRelease(InputEvent.BUTTON3_MASK);}
                                    }catch(Exception e){System.err.println(e);}
                                         
                                         */
                                    fileName=fileName.substring(0,fileName.indexOf('?'));
                                }
                            }

                        }

            }

         }catch(Exception e){
                System.err.println("Read Request:"+e);
            return false;
         }
     //      app.display("finished file request");

         return true;
    }


    private void constructHeader(){
        String contentType;
        if((fileName.toLowerCase().endsWith(".jpg"))||(fileName.toLowerCase().endsWith(".jpeg"))
||(fileName.toLowerCase().endsWith(".jpe"))){contentType = "image/jpg";}
        else if((fileName.toLowerCase().endsWith(".gif"))){contentType = "image/gif";}
        else if((fileName.toLowerCase().endsWith(".htm"))||
                (fileName.toLowerCase().endsWith(".html"))){contentType = "text/html";}
        else if((fileName.toLowerCase().endsWith(".qt"))||
                (fileName.toLowerCase().endsWith(".mov"))){contentType = "video/quicktime";}
        else if((fileName.toLowerCase().endsWith(".class"))){contentType = "application/octet-stream";}
        else if((fileName.toLowerCase().endsWith(".mpg"))||
(fileName.toLowerCase().endsWith(".mpeg"))||(fileName.toLowerCase().endsWith(".mpe")))
{contentType = "video/mpeg";}
        else if((fileName.toLowerCase().endsWith(".au"))||(fileName.toLowerCase().endsWith(".snd")))
            {contentType = "audio/basic";}
        else if((fileName.toLowerCase().endsWith(".wav")))
            {contentType = "audio/x-wave";}
        else {contentType = "text/plain";} //default

        header = "HTTP/1.0 200 OK\n"+
                 "Allow: GET\n"+
                 "MIME-Version: 1.0\n"+
                 "Server : HMJ Basic HTTP Server\n"+
                 "Content-Type: "+contentType + "\n"+
                 "Content-Length: "+ fileLength +
                 "\n\n";
    }

  //  private Serve app;
    private Socket client;
    private String fileName,header;
    private DataInputStream requestedFile, dis;
        private int fileLength, bytesSent;
        
        private String getHomePage()
        {
        	return "<html>    <head>        <title></title>        <meta http-equiv=\"Content-Type\" content=\"text/html;\n charset=UTF-8\">        <style>           .togglebutton {\n            cursor: pointer;\n            border: 1px solid #000;\n            margin: 10px;\n            padding: 10px;\n            display: inline-block;\n            background: -webkit-gradient(                linear, left top, left bottom, from(#fff), to(#ccc));\n          }\n          .togglebutton.toggled {\n            color: #fff;\n            background: -webkit-gradient(                linear, left top, left bottom, from(#333), to(#999));\n                      }\n        </style>     </head>    <body>        <p align=\"center\">                        <button tabindex=\"0\" id=\"submit\" onClick=\"popup();\n\" ><b><font size=\"100\">#</font></b></button></p>                <div  id=\"pop\" style=\"position: absolute;\n margin: 0 auto;\n width: 100%;\n display: none;\n \" >                  <table width=\"100%\" onmouseout=\"canClose=true;\n\" onmouseover=\"canclose=false;\n\"><tr><td align=\"center\">                <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\">                <tr>                    <td align=\"right\" valign=\"bottom\" background=\"http://a.yfrog.com/img615/1907/jnvk.png\"><img src=\"http://a.yfrog.com/img740/2695/ppx.png\"/></td>                    <td align=\"center\" valign=\"bottom\" background=\"http://a.yfrog.com/img734/7428/hd1v.png\"><img src=\"http://a.yfrog.com/img614/543/4cgs.png\"/></td>                    <td align=\"left\" valign=\"bottom\" background=\"http://a.yfrog.com/img610/196/6arw.png\"><img src=\"http://a.yfrog.com/img739/2241/wzrj.png\"/></td>                </tr>                <tr>                    <td align=\"right\" background=\"http://a.yfrog.com/img615/1907/jnvk.png\"></td>                    <td align=\"center\" background=\"http://a.yfrog.com/img614/8743/bmdo.png\">                        <div id=\"tags_div\">\n                                <div id=\"tb\"  class=\"togglebutton\" onclick=\"toggle('tb')\">Toggle</div> \n <div id=\"tb2\" class=\"togglebutton\" onclick=\"toggle('tb2')\">Toggle</div> \n </div></td><td align=\"left\" background=\"http://a.yfrog.com/img610/196/6arw.png\"></td>                </tr>                <tr>                    <td align=\"right\" valign=\"bottom\"> <img src=\"http://a.yfrog.com/img620/6344/6gkl.png\"/> </td>                    <td align=\"center\" valign=\"bottom\" background=\"http://a.yfrog.com/img640/5922/wxzk.png\"></td>                    <td align=\"left\" valign=\"bottom\" > <img src=\"http://a.yfrog.com/img610/3355/vdf.png\"/> </td>                </tr>                </table></td></tr></table>        </div>        <br>        <div id=\"result\">Loading...</div>        <script>            var firstLoad=true;\n            var tags=new Array();\n            var res=new Array();\n            var twt=new Array();\n            function loadTagsAJAX()            {\n                   document.getElementById(\"result\").innerHTML=\"Loading Tags...\";\n            var xmlhttp;\n            if (window.XMLHttpRequest)              {\n              xmlhttp=new XMLHttpRequest();\n              }\n            else              {\n             xmlhttp=new ActiveXObject(\"Microsoft.XMLHTTP\");\n              }\n            xmlhttp.onreadystatechange=function()              {\n              if ( xmlhttp.readyState==4 &&xmlhttp.status==200)                {\n                    var txt=\"\";\n                    tags=xmlhttp.responseText.split(\"\\n\");\n                    tags.splice(tags.length-1,1);\n                     for(i=0;\ni<tags.length;\ni++)                         txt+='<div id=\"'+tags[i]+'\" class=\"togglebutton\" onclick=\"toggle(\\''+tags[i]+'\\')\">'+tags[i]+'</div>';\n                    document.getElementById(\"tags_div\").innerHTML=txt;\ndocument.getElementById(\"result\").innerHTML=\"Done:Loading Tags...\";\n                }\n              }\n            xmlhttp.open(\"GET\",\"_tags\",true);\n            xmlhttp.send();\n            }\n          function loadTag(tag)            {\n                                   document.getElementById(\"result\").innerHTML=\"Loading Tweets...\";\n            var xmlhttp;\n            if (window.XMLHttpRequest)              {\nxmlhttp=new XMLHttpRequest();\n              }\n            else              {\nxmlhttp=new ActiveXObject(\"Microsoft.XMLHTTP\");\n              }\n            xmlhttp.onreadystatechange=function()              {\n              if (xmlhttp.readyState==4 && xmlhttp.status==200)                {\n                    var tmp=xmlhttp.responseText.split(\"\\n\");\n                    tmp.splice(tmp.length-1,1); \n                      twt=new Array();\n                      for(i=0;i<tmp.length/2;i++) res[i]=tmp[i*2];\n                      for(i=0;i<tmp.length/2;i++) twt[i]=tmp[1+i*2];\n                    draw();\n                }\n              }\n                    var Found;\n                    var i,j=0,k=0;\n                    for(i=0;\ni<tags.length;\ni++)                      if(document.getElementById(tags[i]).classList.contains('toggled')) {\nk++;\n}\n                    if(k>1)                    {\n                       document.getElementById(\"result\").innerHTML=\"Searching...\";\n                       for(i=0;\ni<twt.length;\ni++)                       {\n                          Found=true;\n                          for(j=0;\nj<tags.length;\nj++)                             if(document.getElementById(tags[j]).classList.contains('toggled'))                                    Found=Found && (twt[i].toLowerCase().indexOf(\"#\"+tags[j])>-1);\n                          if(!Found)                             {\nres.splice(i,1);\ntwt.splice(i,1);\n}\n                        }\n                        draw();\n                    }\n                    else{\n                         document.getElementById(\"result\").innerHTML=\"sending request...\";\n                         xmlhttp.open(\"GET\",tag,true);\n                         xmlhttp.send();\n                       }\n            }\n         function draw(){\n  var txt=\"\";\nfor(i=0;\ni<twt.length;\ni++)  txt+=getFrame(twt[i],res[i])+\"<br>\";\ndocument.getElementById(\"result\").innerHTML=txt;\nif(txt.length==0) document.getElementById(\"result\").innerHTML=\"Not yet!\";\n}\n         function getFrame(txt,re)        {\n            txt=txt+'<br><div id=\"fb-root\" ></div><script src=\"http://connect.facebook.net/en_US/all.js#appId=194978063887428&amp;xfbml=1\"><'+'/script><fb:like href=\"'+re+'\" send=\"true\" layout=\"button_count\" width=\"450\" show_faces=\"true\" font=\"\"></fb:like>';\n            txt+='<a href=\"http://twitter.com/home?status=RE : '+re+'\"target=\"_blank\">?</a></td>';\n            return '<table width=\"100%\"><tr><td align=\"center\"><table border=\"0\" cellpadding=\"0\" cellspacing=\"0\"><tr><td align=\"right\" valign=\"bottom\" background=\"http://a.yfrog.com/img615/1907/jnvk.png\"><img src=\"http://a.yfrog.com/img740/2695/ppx.png\"/></td><td align=\"center\" valign=\"bottom\" background=\"http://a.yfrog.com/img734/7428/hd1v.png\"></td><td align=\"left\" valign=\"bottom\" background=\"http://a.yfrog.com/img610/196/6arw.png\"><img src=\"http://a.yfrog.com/img739/2241/wzrj.png\"/></td></tr><tr><td align=\"right\" background=\"http://a.yfrog.com/img615/1907/jnvk.png\"></td><td align=\"center\" background=\"http://a.yfrog.com/img614/8743/bmdo.png\"><font color=\"white\">'+txt+'</font></td><td align=\"left\" background=\"http://a.yfrog.com/img610/196/6arw.png\"></td></tr><tr><td align=\"right\" valign=\"bottom\"> <img src=\"http://a.yfrog.com/img620/6344/6gkl.png\"/></td><td align=\"center\" valign=\"bottom\" background=\"http://a.yfrog.com/img640/5922/wxzk.png\"></td><td align=\"left\" valign=\"bottom\" > <img src=\"http://a.yfrog.com/img610/3355/vdf.png\"/> </td></tr></table></td></tr></table>';\n        }\n                function toggle(id) {\n                    canClose=false;\n                  var b = document.getElementById(id);\n                  if (b.classList.contains('toggled'))                    b.classList.remove('toggled');\n                   else                    b.classList.add('toggled');\n                loadTag(id);\n                }\n                                                var canClose=false;\n            function closepop()            {\n                if(canClose)                    popup();\n            }\n            function popup()            {\n                var nn=document.getElementById(\"pop\").style.display==\"none\";\n                if(nn) canClose=false;\n                document.getElementById(\"submit\").innerHTML=nn ? '<b><font size=\"100\">&#149;</font></b>':'<b><font size=\"100\">#</font></b>';\n                document.getElementById(\"pop\").style.display= document.getElementById(\"pop\").style.display==\"none\" ? \"\":\"none\";\n            }\n        loadTagsAJAX();\n                    </script>    </body></html>";
        }

}
