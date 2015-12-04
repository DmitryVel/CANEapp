/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pipeline.ui;

import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.controlsfx.dialog.Dialog;
import org.controlsfx.dialog.Dialog.Actions;
import org.controlsfx.dialog.Dialogs;
import pipeline.ui.PipelineUIController.projectData;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 *
 * @author Pat
 */
public class PipelineUI extends Application {
       
    public Stage stage;
    public String sep = File.separator;
    
    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("PipelineUI.fxml"));
        Scene scene = new Scene(root, 740, 560);
        
        stage.setMaxHeight(600);
        stage.setMinHeight(600);
        stage.setMaxWidth(760);
        stage.setMinWidth(760);
        File f = new File(System.getProperty("java.class.path"));
        File dir = f.getAbsoluteFile().getParentFile();
        String firstPath = dir.toString();
        //String secondpath = firstPath.substring(0,firstPath.lastIndexOf("CANE_v1.0"));
        String mainpath = firstPath + sep + "misc" + sep + "cane.png";
        Image icon = new Image("file:"+mainpath);
        stage.getIcons().add(icon);
        stage.setTitle("CANE analysis setup");
        stage.setScene(scene);
        stage.show();
    }
    
    public boolean isProjectEmpty(projectData currentProj) {
        if (currentProj.projectName != "") {
            if (currentProj.projectLocation != "") {
                if (currentProj.keyFileLocation != "") {
                    if (currentProj.EC2Address != "") {
                          return true;}
                }
            }
        }
        return false;
    }
    
    public projectData showAddProjectDialog(projectData project) throws IOException {
        // Load the fxml file and create a new stage for the popup dialog.
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("CreateNewProject.fxml"));
        AnchorPane page = (AnchorPane) loader.load();

        // Create the dialog Stage.
        Stage dialogStage = new Stage();
        dialogStage.setTitle("Create Project");
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.initOwner(stage);
        Scene scene = new Scene(page);
        dialogStage.setScene(scene);

        // Set the person into the controller.
        NewProjectController controller = loader.getController();
        controller.setDialogStage(dialogStage, project);
    
        // Show the dialog and wait until the user closes it
        dialogStage.showAndWait();
        project = controller.getCurrentProject();
        return project;
    }
    
    public boolean confirmDeleteProject() throws IOException {
        Actions response = (Actions) Dialogs.create()
        .owner(stage)
        .title("Confirm Delete")
        .masthead("Deleting a project will result in the loss of any unsaved files on your server, as well as will remove the project from this list.  Before you continue you should make sure you save any data you may want.")
        .message("Are you sure you want to delete the project?")
        .actions(Dialog.Actions.OK, Dialog.Actions.CANCEL)
        .showConfirm();

        if (Dialog.Actions.OK == response) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}
