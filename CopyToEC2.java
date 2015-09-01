/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pipeline.ui;

/**
 *
 * @author Admin
 */

import com.jcraft.jsch.*;
import java.awt.*;
import javax.swing.*;
import java.io.*;
import java.util.ArrayList;
import pipeline.ui.SwingWorkerProgress;

public class CopyToEC2{
public static void main(String projectName,String projectFileName, String KeyFile, String host, String AuthMethod, String user, String homeDir, Boolean Upload){
ArrayList filesToTransfer=  new ArrayList<String>();
ArrayList filesNames=  new ArrayList<String>();
    try {
      BufferedReader input =  new BufferedReader(new FileReader(projectFileName));
      try {
        JSch cdJsch=new JSch();
        if (AuthMethod.equals("KEY")){
        cdJsch.addIdentity(KeyFile);
        }
        Session cdSession=cdJsch.getSession(user, host, 22);
        UserInfo cdUi=new MyUserInfo();
        cdSession.setUserInfo(cdUi);
        if (AuthMethod.equals("PASS")){
        cdSession.setPassword(KeyFile);
        }
        cdSession.connect();
        String mkCommand="mkdir "+"'"+homeDir+"/"+projectName+"'";
        Channel mkChannel=cdSession.openChannel("exec");
        ((ChannelExec)mkChannel).setCommand(mkCommand);
        mkChannel.connect();
        mkChannel.disconnect();
        try {
            Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        String mkCommand1="mkdir "+"'"+homeDir+"/"+projectName+"/"+"logs"+"'";
        Channel mkChannel1=cdSession.openChannel("exec");
        ((ChannelExec)mkChannel1).setCommand(mkCommand1);
        mkChannel1.connect();
        mkChannel1.disconnect();
        String cdCommand="cd "+"'"+homeDir+"/"+projectName+"'";
        Channel cdChannel=cdSession.openChannel("exec");
        ((ChannelExec)cdChannel).setCommand(cdCommand);
        cdChannel.connect();
        cdChannel.disconnect();
        cdSession.disconnect();
        String line = null;
        input.readLine();
        input.readLine();
        input.readLine();
        input.readLine();
        filesToTransfer.add(projectFileName);
        filesNames.add("'"+homeDir+"/"+projectName+"/project.txt"+"'");
        if (Upload==true){
        while (( line = input.readLine()) != null){
            String[] array = line.split("\t");
            String oldstyle=array[2];
            String readFilePath = oldstyle.replaceAll("\\\\", "/");
            if(readFilePath.contains("|")){
            System.out.println("paired");
            String[] readFilePaths=readFilePath.split("\\|");
            String leftReadPath=readFilePaths[0];
            String rightReadPath=readFilePaths[1];
            String[] leftArrayName = leftReadPath.split("/");
            String leftReadFileName=leftArrayName[leftArrayName.length-1];
            String[] rightArrayName = rightReadPath.split("/");
            String rightReadFileName=rightArrayName[rightArrayName.length-1];
            filesToTransfer.add(leftReadPath);
            filesToTransfer.add(rightReadPath);
            filesNames.add("'"+homeDir+"/"+projectName+"/"+leftReadFileName+"'");
            filesNames.add("'"+homeDir+"/"+projectName+"/"+rightReadFileName+"'");
            }
        else{
            System.out.println("single");
            String[] ArrayName = readFilePath.split("/");
            String ReadFileName=ArrayName[ArrayName.length-1];
            filesToTransfer.add(readFilePath);
            filesNames.add("'"+homeDir+"/"+projectName+"/"+ReadFileName+"'");
            }    
        }
    }
        }
      finally {
        input.close();
      }
    }
    catch (IOException ex){
      ex.printStackTrace();
    }
    catch(Exception e){
    System.out.println(e);
    }
SwingWorkerProgress.main(filesToTransfer,filesNames,KeyFile,host,projectName,user,AuthMethod,homeDir);
FileInputStream fis=null;
try{

boolean ptimestamp = false;
}
catch(Exception e){
System.out.println(e);
try{if(fis!=null)fis.close();}catch(Exception ee){}
}
//System.exit(0);
}
public static void CopyTo(String lfile, String rfile, String KeyFile, String host){
 
FileInputStream fis=null;
try{

String user="ec2-user";
        
JSch jsch=new JSch();
jsch.addIdentity(KeyFile);
Session session=jsch.getSession(user, host, 22);
 
// username and password will be given via UserInfo interface.
UserInfo ui=new MyUserInfo();
session.setUserInfo(ui);
session.connect();
 
boolean ptimestamp = false;
 
// exec 'scp -t rfile' remotely
String command="scp " + "-o LogLevel=quiet"+ " -o UserKnownHostsFile=/dev/null"+" -o StrictHostKeyChecking=no "+(ptimestamp ? "-p" :"") +" -t "+rfile;
Channel channel=session.openChannel("exec");
((ChannelExec)channel).setCommand(command);

// get I/O streams for remote scp
OutputStream out=channel.getOutputStream();
InputStream in=channel.getInputStream();
 
channel.connect();



if(checkAck(in)!=0){
System.exit(0);
}
 
File _lfile = new File(lfile);
 
if(ptimestamp){
command="T "+(_lfile.lastModified()/1000)+" 0";
// The access time should be sent here,
// but it is not accessible with JavaAPI ;-<
command+=(" "+(_lfile.lastModified()/1000)+" 0\n");
out.write(command.getBytes()); out.flush();
if(checkAck(in)!=0){
System.exit(0);
}
}
 
// send "C0644 filesize filename", where filename should not include '/'
long filesize=_lfile.length();
command="C0644 "+filesize+" ";
if(lfile.lastIndexOf('/')>0){
command+=lfile.substring(lfile.lastIndexOf('/')+1);
}
else{
command+=lfile;
}
command+="\n";
out.write(command.getBytes()); out.flush();
if(checkAck(in)!=0){
System.exit(0);
}
 
// send a content of lfile
fis=new FileInputStream(lfile);
byte[] buf=new byte[1024];
while(true){
int len=fis.read(buf, 0, buf.length);
if(len<=0) break;
out.write(buf, 0, len); //out.flush();
}
fis.close();
fis=null;
// send '\0'
buf[0]=0; out.write(buf, 0, 1); out.flush();
if(checkAck(in)!=0){
System.exit(0);
}
out.close();
channel.disconnect();
session.disconnect();

}
catch(Exception e){
System.out.println(e);
try{if(fis!=null)fis.close();}catch(Exception ee){}
}
}

public static void Initiate(String projectName, String KeyFile, String host, String user, String AuthMethod, String homeDir){
    try{
JSch newJsch=new JSch();
if(AuthMethod.equals("KEY")){
newJsch.addIdentity(KeyFile);
}
Session newSession=newJsch.getSession(user, host, 22);
UserInfo newUi=new MyUserInfo();
newSession.setUserInfo(newUi);
if(AuthMethod.equals("PASS")){
newSession.setPassword(KeyFile);    
}
newSession.connect();
String shellCommand="nohup python "+"'"+homeDir+"/beta/install_crossplatform.py"+"'"+" -i "+"'"+homeDir+"/"+projectName+"/project.txt"+"'"+" -f "+"'"+homeDir+"'"+" > "+"'"+homeDir+"/"+projectName+"/"+"logs"+"/"+"main.log"+"'"+" 2>&1 &";
System.out.println(shellCommand);
Channel channel2=newSession.openChannel("exec");
((ChannelExec)channel2).setCommand(shellCommand);
channel2.connect();
    }
    catch(Exception e){
System.out.println(e);
}
}



static int checkAck(InputStream in) throws IOException{
int b=in.read();
// b may be 0 for success,
// 1 for error,
// 2 for fatal error,
// -1
if(b==0) return b;
if(b==-1) return b;
 
if(b==1 || b==2){
StringBuffer sb=new StringBuffer();
int c;
do {
c=in.read();
sb.append((char)c);
}
while(c!='\n');
if(b==1){ // error
System.out.print(sb.toString());
}
if(b==2){ // fatal error
System.out.print(sb.toString());
}
}
return b;
}
 
public static class MyUserInfo implements UserInfo, UIKeyboardInteractive{
public String getPassword(){ return passwd; }
public boolean promptYesNo(String str){ return true; }
String passwd;
JTextField passwordField=(JTextField)new JPasswordField(20);
 
public String getPassphrase(){ return null; }
public boolean promptPassphrase(String message){ return true; }
public boolean promptPassword(String message){ return false; }
public void showMessage(String message){
JOptionPane.showMessageDialog(null, message);
}
final GridBagConstraints gbc =
new GridBagConstraints(0,0,1,1,1,1,
GridBagConstraints.NORTHWEST,
GridBagConstraints.NONE,
new Insets(0,0,0,0),0,0);
private Container panel;
public String[] promptKeyboardInteractive(String destination,
String name,
String instruction,
String[] prompt,
boolean[] echo){
panel = new JPanel();
panel.setLayout(new GridBagLayout());
 
gbc.weightx = 1.0;
gbc.gridwidth = GridBagConstraints.REMAINDER;
gbc.gridx = 0;
panel.add(new JLabel(instruction), gbc);
gbc.gridy++;
 
gbc.gridwidth = GridBagConstraints.RELATIVE;
 
JTextField[] texts=new JTextField[prompt.length];
for(int i=0; i<prompt.length; i++){
gbc.fill = GridBagConstraints.NONE;
gbc.gridx = 0;
gbc.weightx = 1;
panel.add(new JLabel(prompt[i]),gbc);
 
gbc.gridx = 1;
gbc.fill = GridBagConstraints.HORIZONTAL;
gbc.weighty = 1;
if(echo[i]){
texts[i]=new JTextField(20);
}
else{
texts[i]=new JPasswordField(20);
}
panel.add(texts[i], gbc);
gbc.gridy++;
}
 
if(JOptionPane.showConfirmDialog(null, panel,
destination+": "+name,
JOptionPane.OK_CANCEL_OPTION,
JOptionPane.QUESTION_MESSAGE)
==JOptionPane.OK_OPTION){
String[] response=new String[prompt.length];
for(int i=0; i<prompt.length; i++){
response[i]=texts[i].getText();
}
return response;
}
else{
return null; // cancel
}
}
}
}
