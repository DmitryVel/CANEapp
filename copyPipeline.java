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
import com.jcraft.jsch.UIKeyboardInteractive;
import com.jcraft.jsch.UserInfo;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 *
 * @author Admin
 */
public class copyPipeline {
public static String sep = File.separator;
public JFrame frame;
String jarLocation1 = new String();
String projectName1 = new String();
String KeyFile1 = new String();
String host1 = new String();
String AuthMethod1 = new String();
String user1 = new String();
String homeDir1 = new String();
String projectFileName1 = new String();
Boolean Upload1;
public static void main(String jarLocation, String projectName, String KeyFile, String host, String AuthMethod, String user, String homeDir, String projectFileName, Boolean Upload)
{
new copyPipeline(jarLocation, projectName, KeyFile, host, AuthMethod, user, homeDir, projectFileName, Upload);
}

public copyPipeline(String jarLocation, String projectName, String KeyFile, String host, String AuthMethod, String user, String homeDir, String projectFileName, Boolean Upload){
jarLocation1=jarLocation;
projectName1=projectName;
KeyFile1=KeyFile;
host1=host;
AuthMethod1=AuthMethod;
user1=user;
homeDir1=homeDir;
projectFileName1=projectFileName;
Upload1=Upload;
String lfile=jarLocation+sep+"main.py";
String rfile="'"+homeDir+"/beta"+"/main.py"+"'";   
SCPFrom.CopyFrom(user, host, KeyFile, lfile, rfile, AuthMethod);
File f = new File(lfile);
if(!f.exists()){
SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
                }
frame = new JFrame("Preparing pipeline...");
frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
frame.setLayout(new BorderLayout());
frame.add(new TestPane());
frame.pack();
frame.setLocationRelativeTo(null);
frame.setVisible(true);
}
public final JFrame getMainFrame(){
return frame;
}
    }); 
}
else{
f.delete();
CopyToEC2.main(projectName1, projectFileName1, KeyFile1, host1, AuthMethod1, user1, homeDir1,Upload1);
}
}
public static void CopyTo(String lfile, String rfile, String KeyFile, String host, String user, String AuthMethod){
 
FileInputStream fis=null;
try{

        
JSch jsch=new JSch();
if(AuthMethod.equals("KEY")){
jsch.addIdentity(KeyFile);
}
Session session=jsch.getSession(user, host, 22);
 
// username and password will be given via UserInfo interface.
UserInfo ui=new MyUserInfo();
session.setUserInfo(ui);
if(AuthMethod.equals("PASS")){
session.setPassword(KeyFile);    
}
session.connect();
 
boolean ptimestamp = false;
 
// exec 'scp -t rfile' remotely
String command="scp " + "-o LogLevel=quiet"+ " -o UserKnownHostsFile=/dev/null"+" -o StrictHostKeyChecking=no "+(ptimestamp ? "-p" :"") +" -t "+rfile;
System.out.print(command);
Channel channel=session.openChannel("exec");
((ChannelExec)channel).setCommand(command);

// get I/O streams for remote scp
OutputStream out=channel.getOutputStream();
InputStream in=channel.getInputStream();
 
channel.connect();



if(checkAck(in)!=0){
//System.exit(0);
}
 
File _lfile = new File(lfile);
 
if(ptimestamp){
command="T "+(_lfile.lastModified()/1000)+" 0";
// The access time should be sent here,
// JButton it is not accessible with JavaAPI ;-<
command+=(" "+(_lfile.lastModified()/1000)+" 0\n");
out.write(command.getBytes()); out.flush();
if(checkAck(in)!=0){
//System.exit(0);
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
//System.exit(0);
}
 
// send a content of lfile
fis=new FileInputStream(lfile);
byte[] buf=new byte[1024];
while(true){
int len=fis.read(buf, 0, buf.length);
//System.out.print(total);
//System.out.print(percent);
if(len<=0) break;
out.write(buf, 0, len); //out.flush();
}
fis.close();
fis=null;
// send '\0'
buf[0]=0; out.write(buf, 0, 1); out.flush();
if(checkAck(in)!=0){
//System.exit(0);
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

public class TestPane extends JPanel {

        private JProgressBar pbProgress;

        public TestPane() {
            setPreferredSize(new Dimension(250, 40));
            setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
            pbProgress = new JProgressBar();
            add(pbProgress);
            pbProgress.setIndeterminate(true);
            copyPipeline.ProgressWorker pw = new copyPipeline.ProgressWorker();
            pw.execute();

        }
    }
public class ProgressWorker extends SwingWorker<Object, Object> {
        long total=0;
        @Override
        protected Object doInBackground() throws Exception {
                transferPipelineFiles();
                try {
                    Thread.sleep(25);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                frame.setVisible(false);
                CopyToEC2.main(projectName1, projectFileName1, KeyFile1, host1, AuthMethod1, user1, homeDir1,Upload1);
                return null;

}
  
}
public void transferPipelineFiles(){
try {
        JSch cdJsch=new JSch();
        if (AuthMethod1.equals("KEY")){
        cdJsch.addIdentity(KeyFile1);
        }
        Session cdSession=cdJsch.getSession(user1, host1, 22);
        UserInfo cdUi=new CopyToEC2.MyUserInfo();
        cdSession.setUserInfo(cdUi);
        if (AuthMethod1.equals("PASS")){
        cdSession.setPassword(KeyFile1);
        }
        cdSession.connect();
        String mkCommand="mkdir "+"'"+homeDir1+"/"+"beta"+"'";
        Channel mkChannel=cdSession.openChannel("exec");
        ((ChannelExec)mkChannel).setCommand(mkCommand);
        mkChannel.connect();
        mkChannel.disconnect();
        String cdCommand="cd "+"'"+homeDir1+"/"+"beta"+"'";
        Channel cdChannel=cdSession.openChannel("exec");
        ((ChannelExec)cdChannel).setCommand(cdCommand);
        cdChannel.connect();
        cdChannel.disconnect();
        cdSession.disconnect();
        File[] files = new File(jarLocation1+sep+"beta").listFiles();
        for (File file : files) {
        if (file.isFile()) {
        String localFileName = file.getName();
        String FulllocalFileName=jarLocation1+sep+"beta"+sep+localFileName;
        System.out.println("Local: " + FulllocalFileName);
        System.out.println("Server: " + "'"+homeDir1+"/"+"beta"+"/"+localFileName+"'");
        String serverFileName="'"+homeDir1+"/"+"beta"+"/"+localFileName+"'";
        CopyTo(FulllocalFileName, serverFileName, KeyFile1, host1, user1, AuthMethod1);
      }
    }
    frame.setVisible(false);      
      }
catch(Exception e){
System.out.println(e);
    }    
}
}
