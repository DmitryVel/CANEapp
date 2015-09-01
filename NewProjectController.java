package pipeline.ui;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import pipeline.ui.PipelineUIController.projectData;

/**
 * FXML Controller class
 *
 * @author Pat
 */
public class NewProjectController implements Initializable {
    String name = null;
    String location = null;
    public projectData currentProject;
    private Stage dialogStage;
    @FXML
    private TextField nameOfProject;
    @FXML
    private TextField locationOfProject;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }  
    /* Need to find a way to return currentProject from this controller to 
    /  PipelineUI.java so it can be returned to master controller.  Only then
    /  will the newly created project be written to the project_file.txt
    */
    public void setDialogStage(Stage dialogStage, projectData project) {
        this.dialogStage = dialogStage;
        this.currentProject = project;
        
    }
    
    public projectData getCurrentProject() {
        return currentProject;
    }
    
    @FXML
    private projectData submitName(ActionEvent event) {
        name = nameOfProject.getText();
        if (name != "" && name != currentProject.projectName && location != "" && location != currentProject.projectLocation)
        {
            currentProject.projectName = name;
            currentProject.projectLocation = location;
            new File(location + File.separator + name).mkdir();
            dialogStage.close();
        }
        else {
            currentProject.projectLocation = null;
            currentProject.projectName = null;
        }
        return currentProject;
    }

    @FXML
    private void createProjectDirectory(ActionEvent event) {
        DirectoryChooser projectDirectory = new DirectoryChooser();
        projectDirectory.setTitle("Create Project Directory:");
        File refLocation = projectDirectory.showDialog(null);
        if (refLocation != null) {
            locationOfProject.clear();
            location = refLocation.getPath();
            locationOfProject.appendText(location);
        }
    }
    
}
