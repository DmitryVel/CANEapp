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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 *
 * @author Admin
 */
public class DownloadResults {
    int numOfFiles = 0;
    ArrayList lfiles=new ArrayList();
    ArrayList rfiles=new ArrayList();
    String user = new String();
    String KeyFile = new String();
    String host = new String();
    String projectName = new String();
    String MethToAuth = new String();
    public JFrame frame;
    int overallSize;
    public static void main(String user_1, String host_1, String KeyFile_1, ArrayList outFiles, ArrayList inFiles, int overallSize_1, String MethToAuth_1) {
        new DownloadResults(user_1, host_1, KeyFile_1, outFiles, inFiles, overallSize_1, MethToAuth_1);
    }

    public DownloadResults(String user_1, String host_1, String KeyFile_1, ArrayList outFiles, ArrayList inFiles, int overallSize_1, String MethToAuth_1) {
        lfiles=outFiles;
        rfiles=inFiles;
        user=user_1;
        host=host_1;
        KeyFile=KeyFile_1;
        numOfFiles = lfiles.size();
        overallSize=overallSize_1;
        MethToAuth=MethToAuth_1;
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
                }

                frame = new JFrame("Getting result files...");
                frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
                frame.setLayout(new BorderLayout());
                frame.add(new TestPane());
                frame.pack();
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
            }
        });
    }

    public class TestPane extends JPanel {

        private JProgressBar pbProgress;

        public TestPane() {

            setPreferredSize(new Dimension(250, 40));
            setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
            pbProgress = new JProgressBar();
            add(pbProgress);
            numOfFiles = lfiles.size();
            pbProgress.setMaximum(100);
            pbProgress.setStringPainted(true);

                    ProgressWorker pw = new ProgressWorker();
                    pw.addPropertyChangeListener(new PropertyChangeListener() {

                        @Override
                        public void propertyChange(PropertyChangeEvent evt) {
                            String name = evt.getPropertyName();
                            if (name.equals("progress")) {
                                int progress = (int) evt.getNewValue();
                                pbProgress.setValue(progress);
                                repaint();
                            } else if (name.equals("state")) {
                                SwingWorker.StateValue state = (SwingWorker.StateValue) evt.getNewValue();
                                switch (state) {
                                    case DONE:
                                        break;
                                }
                            }
                        }

                    });
                    pw.execute();
                }
            }


    public class ProgressWorker extends SwingWorker<Object, Object> {
        int total=0;
        @Override
        protected Object doInBackground() throws Exception {
            for (int i = 0; i < numOfFiles; i++) {        
                String lfile=(String) lfiles.get(0);
                String rfile=(String) rfiles.get(0);
                lfiles.remove(0);
                rfiles.remove(0);
                CopyFrom(user, host, KeyFile, lfile, rfile,MethToAuth);
                try {
                    Thread.sleep(25);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            setProgress(100);
            JOptionPane.showMessageDialog(null, "Results downloaded!", "Success!", JOptionPane.INFORMATION_MESSAGE);
            frame.setVisible(false);
            return null;
        }
    
public void CopyFrom(String user, String host, String KeyFile, String lfile, String rfile, String AuthMeth){
FileOutputStream fos=null;
try{
String prefix=null;
if(new File(lfile).isDirectory()){
prefix=lfile+File.separator; 
}
JSch jsch=new JSch();
Session session=jsch.getSession(user, host, 22);
if(MethToAuth.equals("KEY")){
jsch.addIdentity(KeyFile);
}
// username and password will be given via UserInfo interface.
UserInfo ui=new MyUserInfo();
session.setUserInfo(ui);
if(MethToAuth.equals("PASS")){
session.setPassword(KeyFile);
}
session.connect();
 
// exec 'scp -f rfile' remotely
String command="scp -f "+rfile;
Channel channel=session.openChannel("exec");
((ChannelExec)channel).setCommand(command);
 
// get I/O streams for remote scp
OutputStream out=channel.getOutputStream();
InputStream in=channel.getInputStream();
 
channel.connect();
 
byte[] buf=new byte[1024];
 
// send '\0'
buf[0]=0; out.write(buf, 0, 1); out.flush();
 
while(true){
int c=checkAck(in);
if(c!='C'){
break;
}
 
// read '0644 '
in.read(buf, 0, 5);
 
long filesize=0L;
while(true){
if(in.read(buf, 0, 1)<0){
// error
break;
}
if(buf[0]==' ')break;
filesize=filesize*10L+(long)(buf[0]-'0');
}
 
String file=null;
for(int i=0;;i++){
in.read(buf, i, 1);
if(buf[i]==(byte)0x0a){
file=new String(buf, 0, i);
break;
}
}
 
//System.out.println("filesize="+filesize+", file="+file);
 
// send '\0'
buf[0]=0; out.write(buf, 0, 1); out.flush();
 
// read a content of lfile
fos=new FileOutputStream(prefix==null ? lfile : prefix+file);
int foo;
while(true){
if(buf.length<filesize) foo=buf.length;
else foo=(int)filesize;
foo=in.read(buf, 0, foo);
if(foo<0){
// error
break;
}
total+=foo;
int percent = (int)((total * 100.0) / overallSize);
setProgress(percent);
fos.write(buf, 0, foo);
filesize-=foo;
if(filesize==0L) break;
}
fos.close();
fos=null;
 
if(checkAck(in)!=0){
System.exit(0);
}
 
// send '\0'
buf[0]=0; out.write(buf, 0, 1); out.flush();
}
 
session.disconnect();
 
}
catch(Exception e){
System.out.println(e);
try{if(fos!=null)fos.close();}catch(Exception ee){}
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
 
public static class MyUserInfo implements UserInfo, UIKeyboardInteractive{
public String getPassword(){ return passwd; }
public boolean promptYesNo(String str){return true;}
String passwd;
JTextField passwordField=(JTextField)new JPasswordField(20);
 
public String getPassphrase(){ return null; }
public boolean promptPassphrase(String message){ return true; }
public boolean promptPassword(String message){
Object[] ob={passwordField};
int result=
JOptionPane.showConfirmDialog(null, ob, message,
JOptionPane.OK_CANCEL_OPTION);
if(result==JOptionPane.OK_OPTION){
passwd=passwordField.getText();
return true;
}
else{ return false; }
}
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
