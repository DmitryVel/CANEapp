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
import java.io.FileInputStream;
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
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.JTabbedPane;
import javax.swing.JFrame;
import javax.swing.JPanel;


public class SwingWorkerProgress {
    int numOfFiles = 0;
    ArrayList lfiles=new ArrayList();
    ArrayList rfiles=new ArrayList();
    String KeyFile = new String();
    String host = new String();
    String projectName = new String();
    String AuthMethod = new String();
    String user = new String();
    String homeDir = new String();
    public JFrame frame;
    long overallSize=0;
    public static void main(ArrayList filesToTransfer, ArrayList fileNames, String KeyFile_1,String host_1, String projectName_1, String user_1, String AuthMethod_1, String homeDir_1) {
        new SwingWorkerProgress(filesToTransfer,fileNames,KeyFile_1,host_1,projectName_1,user_1,AuthMethod_1,homeDir_1);
    }

    public SwingWorkerProgress(ArrayList filesToTransfer, ArrayList fileNames, String KeyFile_1,String host_1, String projectName_1, String user_1, String AuthMethod_1, String homeDir_1) {
        lfiles=filesToTransfer;
        rfiles=fileNames;
        host=host_1;
        KeyFile=KeyFile_1;
        projectName=projectName_1;
        user=user_1;
        AuthMethod=AuthMethod_1;
        homeDir=homeDir_1;
        numOfFiles = lfiles.size();
        int overallSize=0;
        for (int i = 0; i < numOfFiles; i++) {
            String lfilename=(String) lfiles.get(i);
            File file=new File(lfilename);
            long size=file.length();
            overallSize+=size;
        }
        System.out.print(overallSize);
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
                }

                frame = new JFrame("Transferring data files...");
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

    public class TestPane extends JPanel {

        private JProgressBar pbProgress;

        public TestPane() {
            setPreferredSize(new Dimension(250, 40));
            setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
            pbProgress = new JProgressBar();
            add(pbProgress);
            numOfFiles = lfiles.size();
            for (int i = 0; i < numOfFiles; i++) {
            String lfilename=(String) lfiles.get(i);
            File file=new File(lfilename);
            long size=file.length();
            overallSize+=size;
            }
            System.out.print(overallSize);
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
                            }
                        }

                    });
                    pw.execute();

        }
    }

    public class ProgressWorker extends SwingWorker<Object, Object> {
        long total=0;
        @Override
        protected Object doInBackground() throws Exception {
            
            for (int i = 0; i < numOfFiles; i++) {        
                String lfile=(String) lfiles.get(0);
                String rfile=(String) rfiles.get(0);
                lfiles.remove(0);
                rfiles.remove(0);
                CopyTo(lfile, rfile, KeyFile, host,user,AuthMethod);
                try {
                    Thread.sleep(25);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            CopyToEC2.Initiate(projectName, KeyFile, host, user, AuthMethod, homeDir);
            setProgress(100);
            JOptionPane.showMessageDialog(null, "Analysis initiated!", "Success!", JOptionPane.INFORMATION_MESSAGE);
            frame.setVisible(false);
            return null;
        }
    
public void CopyTo(String lfile, String rfile, String KeyFile, String host, String user, String AuthMethod){
 
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
// JButton it is not accessible with JavaAPI ;-<
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
total+=len;
//System.out.print(total);
int percent = (int)((total * 100.0) / overallSize);
//System.out.print(percent);
setProgress(percent);
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
}
public static class MyUserInfo implements UserInfo, UIKeyboardInteractive{
public String getPassword(){ return passwd; }
public boolean promptYesNo(String str){ return true; }
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
}
