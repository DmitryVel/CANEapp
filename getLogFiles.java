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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 *
 * @author Admin
 */
public class getLogFiles {
public String sep = File.separator;    
String KeyFile1 = new String();
String projectLocation1 = new String();
String host1 = new String();
String projectName1 = new String();
String AuthMethod1 = new String();
String user1 = new String();
String homeDir1 = new String();
public JFrame frame;
public static void main(String user, String host, String KeyFile, String ProjectName, String ProjectLocation, String MethToAut, String homeDir) {
    new getLogFiles(user, host, KeyFile, ProjectName, ProjectLocation, MethToAut, homeDir);
}

public getLogFiles(String user, String host, String KeyFile, String ProjectName, String ProjectLocation, String MethToAut, String homeDir){
    projectLocation1=ProjectLocation;
    host1=host;
    KeyFile1=KeyFile;
    projectName1=ProjectName;
    user1=user;
    AuthMethod1=MethToAut;
    homeDir1=homeDir;
    EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
                }

                frame = new JFrame("Getting logs...");
                frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
                frame.setLayout(new BorderLayout());
                frame.add(new getLogFiles.TestPane());
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
            getLogFiles.ProgressWorker pw = new getLogFiles.ProgressWorker();
            pw.execute();

        }
    }

public void tar(){
try{
                    JSch newJsch=new JSch();
                    if(AuthMethod1.equals("KEY")){
                    newJsch.addIdentity(KeyFile1);
                    }
                    Session newSession=newJsch.getSession(user1, host1, 22);
                    UserInfo newUi=new CopyToEC2.MyUserInfo();
                    newSession.setUserInfo(newUi);
                    if(AuthMethod1.equals("PASS")){
                    newSession.setPassword(KeyFile1);    
                    }
                    newSession.connect();
                    String Command="nohup python "+"'"+homeDir1+"/beta/logs.py"+"'"+" -f "+"'"+homeDir1+"/"+projectName1+"'";
                    System.out.print(Command);
                    Channel Channel=newSession.openChannel("exec");
                    ((ChannelExec)Channel).setCommand(Command);
                    Channel.connect();
                    String rfile="'"+homeDir1+"/"+projectName1+"/tar.log"+"'";
                    String lfile=projectLocation1+File.separator+"tar.log";
                    System.out.print(rfile);
                    System.out.print(lfile);
                    int exist = 0;
                    while (exist==0){
                    try{    
                    statusCheck.CopyFrom(user1, host1, KeyFile1, lfile, rfile,AuthMethod1);
                    File f = new File(lfile);
                    if(f.exists()){
                    exist=1;
                    f.delete();
                    Channel.disconnect();
                    String rvCommand="rm "+rfile;
                    System.out.print(rvCommand);
                    Channel rvChannel=newSession.openChannel("exec");
                    ((ChannelExec)rvChannel).setCommand(rvCommand);
                    rvChannel.connect();
                    rvChannel.disconnect();
                    newSession.disconnect();
                    String rfile1="'"+homeDir1+"/"+projectName1+"/logs.tar.gz"+"'";
                    String lfile1=projectLocation1+File.separator+"logs.tar.gz";
                    SCPFrom.CopyFrom(user1, host1, KeyFile1, lfile1, rfile1,AuthMethod1);
                    break;
                    }
                    }
                    catch(Exception e){
                    System.out.println(e);
                    }
                    } 
                        }
                    catch(Exception e){
                    JOptionPane.showMessageDialog(null, "Could not connect to server", "Connection failed", JOptionPane.INFORMATION_MESSAGE);
                        }    
}

public class ProgressWorker extends SwingWorker<Object, Object> {
        long total=0;
        @Override
        protected Object doInBackground() throws Exception {
                tar();
                try {
                    Thread.sleep(25);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                JOptionPane.showMessageDialog(null, "Logs retrieved!", "Success!", JOptionPane.INFORMATION_MESSAGE);
                frame.setVisible(false);
                return null;      

}
  
}
}
