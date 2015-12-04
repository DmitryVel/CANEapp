/*
 * Copyright (C) 2015 labadmin
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package pipeline.ui;

import com.jcraft.jsch.JSchException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import static pipeline.ui.SCPFrom.readLines;


/**
 * FXML Controller class
 *
 * @author labadmin
 */


public class AddSpeciesController {
@FXML
private TextField speciesNameFiled;
@FXML
private TextField assemblyNameFiled;
@FXML
private TextField fastaURLFiled;
@FXML
private TextField gtfURLFiled;
@FXML
private Button addSpeciesButton;
@FXML
private Button cancelSpeciesButton;
private static Stage dialogStage = new Stage();
private static String speciesFileLocation;
private static speciesData newSpecies;

    /**
     * Initializes the controller class.
     * @param url
     */
    
    public speciesData start(String speciesFileName) throws Exception {
        speciesFileLocation=speciesFileName;
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("addSpecies.fxml"));
        AnchorPane page = (AnchorPane) loader.load();
        dialogStage.setTitle("Add species/assembly");
        Scene scene = new Scene(page);
        dialogStage.setScene(scene);
        dialogStage.showAndWait();
        return newSpecies;
    }    


@FXML
public void submitAddSpecies(ActionEvent event) throws IOException, JSchException {
        String speciesName = speciesNameFiled.getText();
        String assemblyName = assemblyNameFiled.getText();
        String fastaURL = fastaURLFiled.getText();
        String gtfURL = gtfURLFiled.getText();
        List allAssemblies = new ArrayList<>();
        if(!speciesName.equals("") && !assemblyName.equals("") && !fastaURL.equals("") && !gtfURL.equals("")){
            File outputSpeciesFile = new File(speciesFileLocation);
            List<String> lines = new ArrayList<>();
            FileReader fr = new FileReader(outputSpeciesFile);
            BufferedReader textReader1 = new BufferedReader(fr);
            int numOfLines = readLines(speciesFileLocation);
            for(int i = 0; i < numOfLines; i++) {
                String line = textReader1.readLine();
                String[] array = line.split("\t");
                String refSpeciesName=array[0];
                String refAssemblyName=array[1];
                if(refSpeciesName.equals(speciesName)){
                    allAssemblies.add(refAssemblyName);
                }
                lines.add(line);
                }
            FileWriter file = new FileWriter(outputSpeciesFile);
            BufferedWriter output = new BufferedWriter(file);
            output.flush();
            for(int i = 0; i < numOfLines; i++) {
                String line = lines.get(i);
                output.write(line);
                output.write(System.getProperty("line.separator"));
                }
            allAssemblies.add(assemblyName);
            output.write(speciesName+"\t"+assemblyName+"\t"+fastaURL+"\t"+gtfURL+System.getProperty("line.separator"));
            output.close();
            newSpecies=new speciesData(speciesName,assemblyName,allAssemblies);
            dialogStage.close(); 
}
}

public void cancelSpecies (ActionEvent event) {
    dialogStage.close();
}

public class speciesData {
        
        public String currentSpecies = null;
        public String currentAssembly = null;
        public List<String> currentAssemblies = new ArrayList<>();
        
        
        public speciesData (String name,  String assembly, List<String> assemblies) {
            currentSpecies = name;
            currentAssembly = assembly;
            currentAssemblies = assemblies;
        }
}
}

