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
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 *
 * @author Admin
 */
public class checkConnection {
public static String main(String KeyFile, String host, String user, String AuthMethod){
    try{
JSch newJsch=new JSch();
if(AuthMethod.equals("KEY")){
newJsch.addIdentity(KeyFile);
}
Session newSession=newJsch.getSession(user, host, 22);
UserInfo newUi=new CopyToEC2.MyUserInfo();
newSession.setUserInfo(newUi);
if(AuthMethod.equals("PASS")){
newSession.setPassword(KeyFile);    
}
newSession.connect();
newSession.disconnect();
return "success";
    }
    catch(Exception e){
try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
                }
        SwingUtilities.invokeLater(new Runnable() {
        public void run() {
            JOptionPane.showMessageDialog(null, "Could not connect to server", "Connection failed", JOptionPane.INFORMATION_MESSAGE);
        }
    });

return "connection_failed";
    }
}    
}
