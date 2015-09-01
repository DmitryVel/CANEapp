/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pipeline.ui;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;
import java.io.IOException;

/**
 *
 * @author Admin
 */
public class deleteServerFiles {
public static void main(String projectName, String KeyFile, String host, String AuthMethod, String user, String homeDir)
{
    try{
JSch cdJsch=new JSch();
if (AuthMethod.equals("KEY")){
cdJsch.addIdentity(KeyFile);
}
Session cdSession=cdJsch.getSession(user, host, 22);
UserInfo cdUi=new CopyToEC2.MyUserInfo();
cdSession.setUserInfo(cdUi);
if (AuthMethod.equals("PASS")){
cdSession.setPassword(KeyFile);
}
cdSession.connect();
String mkCommand="rm -r "+"'"+homeDir+"/"+projectName+"'";
System.out.print(mkCommand);
Channel mkChannel=cdSession.openChannel("exec");
((ChannelExec)mkChannel).setCommand(mkCommand);
mkChannel.connect();
mkChannel.disconnect();    
}
catch(Exception e){
System.out.println(e);
}    
}
}
