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
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JTabbedPane;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import static pipeline.ui.SwingWorkerProgress.checkAck;
/**
 *
 * @author Admin
 */
public class primer {
    public String sep = File.separator;
    String KeyFile = new String();
    String projectLocation = new String();
    String host = new String();
    String projectName = new String();
    String AuthMethod = new String();
    String user = new String();
    String homeDir = new String();
    public JFrame frame;
    long overallSize=0;
    public static void main(String projectLocation_1, String KeyFile_1,String host_1, String projectName_1, String user_1, String AuthMethod_1, String homeDir_1) {
        new primer(projectLocation_1,KeyFile_1,host_1,projectName_1,user_1,AuthMethod_1,homeDir_1);
    }
    public primer(String projectLocation_1, String KeyFile_1,String host_1, String projectName_1, String user_1, String AuthMethod_1, String homeDir_1) {
        projectLocation=projectLocation_1;
        host=host_1;
        KeyFile=KeyFile_1;
        projectName=projectName_1;
        user=user_1;
        AuthMethod=AuthMethod_1;
        homeDir=homeDir_1;
        projectName = projectName_1;
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
                }

                frame = new JFrame("Designing primers...");
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
            pbProgress.setIndeterminate(true);
            primer.ProgressWorker pw = new primer.ProgressWorker();
            pw.execute();

        }
    }
    public class ProgressWorker extends SwingWorker<Object, Object> {
        long total=0;
        @Override
        protected Object doInBackground() throws Exception {
                String lfile=projectLocation + sep +  "gene_list.txt";
                System.out.print(lfile);
                String rfile= "'" + homeDir + "/" + projectName + "/" + "gene_list.txt" +"'";
                System.out.print(rfile);
                CopyTo(lfile, rfile, KeyFile, host, user, AuthMethod);
                try {
                    JSch jsch=new JSch();
                    if(AuthMethod.equals("KEY")){
                    jsch.addIdentity(KeyFile);
                    }
                    Session session=jsch.getSession(user, host, 22);

                    // username and password will be given via UserInfo interface.
                    UserInfo ui=new SwingWorkerProgress.MyUserInfo();
                    session.setUserInfo(ui);
                    if(AuthMethod.equals("PASS")){
                    session.setPassword(KeyFile);    
                    }
                    session.connect();
                    String command = "nohup python " + "'" + homeDir + "/beta/primers.py" + "'" + " -p " + "'" + homeDir + "/" + projectName + "'" + " -f " + "'" + homeDir + "'"+" > "+"'"+homeDir+"/"+projectName+"/"+"primers_log.txt"+"'"+" 2>&1 &";
                    System.out.print(command);
                    Channel channel=session.openChannel("exec");
                    ((ChannelExec)channel).setCommand(command);
                    channel.connect();
                    channel.disconnect();
                    String lfile1 = projectLocation + sep +  "primer_status.txt";
                    String lfile2 = projectLocation + sep +  "Verify_Products.txt";
                    String rfile1 = "'" + homeDir + "/" + projectName + "/" + "primer_status.txt" +"'";
                    String rfile2 = "'" + homeDir + "/" + projectName + "/" + "Verify_Products.txt" +"'";
                    int exist = 0;
                    while (exist==0){
                    try{
                    SCPFrom.CopyFrom(user, host, KeyFile, lfile1, rfile1, AuthMethod);
                    File f = new File(projectLocation + sep +  "primer_status.txt");
                    if(f.exists()){
                    exist=1;
                    SCPFrom.CopyFrom(user, host, KeyFile, lfile2, rfile2, AuthMethod);
                    String Rmcommand = "rm " + rfile1;
                    Channel Rmchannel=session.openChannel("exec");
                    ((ChannelExec)Rmchannel).setCommand(Rmcommand);
                    Rmchannel.connect();
                    Rmchannel.disconnect();
                    f.delete();
                    session.disconnect();
                    break;
                    }    
                    }
                    catch(Exception e){
                    System.out.println(e);
                    }
                    }
                }
                catch(Exception e){
                    System.out.println(e);
                    }
                try {
                    Thread.sleep(25);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            JOptionPane.showMessageDialog(null, "Primers designed!", "Success!", JOptionPane.INFORMATION_MESSAGE);
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
UserInfo ui=new SwingWorkerProgress.MyUserInfo();
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
}
