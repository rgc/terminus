<%@ page language="java" contentType="video/mp4" %><%@ page language="java" import="java.io.*" errorPage="" %><%

InputStream instr = null;
try {
    instr = new BufferedInputStream( new FileInputStream("www/media/1367952470-1bbb2162738dd78.mp4") );
    for(int x=instr.read(); x!=-1; x=instr.read()){
        out.write(x);
    }
} finally {
    out.close();
    if( instr != null) instr.close();
}
%>