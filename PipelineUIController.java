/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pipeline.ui;

import com.jcraft.jsch.*;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import javafx.beans.binding.BooleanBinding;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.DirectoryChooser;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;


/**
 *
 * @author Pat
 */
public class PipelineUIController implements Initializable {

    // Initializes components needed for pipeline
    private Label label;
    @FXML
    public Tab groupsTab;
    @FXML
    public Tab DGE;
    @FXML
    private Button addGroup;
    @FXML
    private TextField groupName;
    @FXML
    private ListView<?> groups;
    private final ObservableList listViewItems = FXCollections.observableArrayList();
    private final ObservableList samplesListView = FXCollections.observableArrayList();
    @FXML
    private Button removeGroupBtn;
    @FXML
    private Button getLogsButton;
    @FXML        
    public Tab addSamples;
    @FXML
    TabPane tabPane;
    @FXML
    private Tab parameters;
    @FXML
    private RadioButton singleEnd;
    @FXML
    private RadioButton pairedEnd;
    @FXML
    private TextField singleReadLocation;
    @FXML
    private Button singleReadLocationBtn;
    @FXML
    private TextField leftReadLocation;
    @FXML
    private TextField rightReadLocation;
    @FXML
    private Button leftReadLocationBtn;
    @FXML
    private Button rightReadLocationBtn;
    @FXML
    private Label singleReadLabel;
    @FXML
    private Label leftReadLabel;
    @FXML
    private Label rightReadLabel;
    @FXML
    private RadioButton trimReadsYesBtn;
    @FXML
    private RadioButton trimReadsNoBtn;
    @FXML
    private RadioButton pairwiseCompBtn;
    @FXML
    private RadioButton timecourseCompBtn;
    @FXML
    private RadioButton filterGenesYesBtn;
    @FXML
    private RadioButton filterGenesNoBtn;
    @FXML
    private Label thresholdLabel;
    @FXML
    private TextField thresholdValue;
    @FXML
    private Tab primerSelection;
    private final String newline = "\n";
    @FXML
    private ListView<?> groups1;
    private static final String user = "core"; // TODO: Username of ssh account on remote machine
    private static final String host = "10.141.60.229"; // TODO: Hostname of the remote machine (eg: inst.eecs.berkeley.edu)
    private static final String password = "vanila"; // TODO: Password associated with your ssh account
    private static final String command = "ls -l\n";
    @FXML
    private TextField insertSize;
    @FXML
    private TextField cVar;
    @FXML
    private ListView<?> sampleList;
    @FXML
    private Label noGroupError;
    @FXML
    private TextField username;
    @FXML
    private TextField connectionAddress;
    @FXML
    private TextField keyFile;
    @FXML
    private Button keyFileBrowse;
    @FXML
    private Tab startTab;
    @FXML
    private ComboBox<String> libType;
    ObservableList<String> libTypes = FXCollections.observableArrayList();
    ObservableList directions = FXCollections.observableArrayList();
    ObservableList<String> projectNames = FXCollections.observableArrayList();
    ObservableList edgeRcorrections = FXCollections.observableArrayList();
    ObservableList selectableGroups1 = FXCollections.observableArrayList();
    ObservableList selectableGroups2 = FXCollections.observableArrayList();
    ObservableList selectableGroups1DESeq2 = FXCollections.observableArrayList();
    ObservableList selectableGroups2DESeq2 = FXCollections.observableArrayList();
    @FXML
    private RadioButton basicFilterYes;
    @FXML
    private RadioButton basicFilterNo;
    @FXML
    private TextField totalFilter;
    @FXML
    private TextField groupFilter;
    @FXML
    private RadioButton humanSpecies;
    @FXML
    private RadioButton mouseSpecies;
    @FXML
    private RadioButton splicingYes;
    @FXML
    private RadioButton splicingNo;
    @FXML
    private RadioButton cufflinksMaskYes;
    @FXML
    private RadioButton cufflinksMaskNo;
    @FXML
    private RadioButton cufflinksBiasYes;
    @FXML
    private RadioButton cufflinksBiasNo;
    @FXML
    private RadioButton cufflinksMultiYes;
    @FXML
    private RadioButton cufflinksMultiNo;
    @FXML
    private RadioButton cufflinksUpperYes;
    @FXML
    private RadioButton cufflinksUpperNo;
    @FXML
    private RadioButton cufflinksTotalYes;
    @FXML
    private RadioButton cufflinksTotalNo;
    @FXML
    private RadioButton cufflinksLengthYes;
    @FXML
    private RadioButton cufflinksLengthNo;
    @FXML
    private RadioButton cufflinksEffYes;
    @FXML
    private RadioButton cufflinksEffNo;
    @FXML
    private TextField cufflinksFraction;
    @FXML
    private TextField cufflinksOtherOptions;
    @FXML
    private RadioButton cuffdiffMaskYes;
    @FXML
    private RadioButton cuffdiffMaskNo;
    @FXML
    private RadioButton cuffdiffBiasYes;
    @FXML
    private RadioButton cuffdiffBiasNo;
    @FXML
    private RadioButton cuffdiffMultiYes;
    @FXML
    private RadioButton cuffdiffMultiNo;
    @FXML
    private RadioButton cuffdiffClassic;
    @FXML
    private RadioButton cuffdiffGeometric;
    @FXML
    private RadioButton cuffdiffQuartile;
    @FXML
    private RadioButton cuffdiffTotalYes;
    @FXML
    private RadioButton cuffdiffTotalNo;
    @FXML
    private RadioButton cuffdiffCompYes;
    @FXML
    private RadioButton cuffdiffCompNo;
    @FXML
    private TextField cuffdiffMinimum;
    @FXML
    private TextField cuffdiffFalse;
    @FXML
    private RadioButton cuffdiffPooled;
    @FXML
    private RadioButton cuffdiffPer;
    @FXML
    private RadioButton cuffdiffBlind;
    @FXML
    private RadioButton cuffdiffPoisson;
    @FXML
    private TextField cuffdiffOtherOptions;
    private Label advancedOptionsLabel;
    @FXML
    private Label cufflinksOptionsLabel;
    @FXML
    private Label cufflinksMaskLabel;
    @FXML
    private Label cufflinksBiasLabel;
    @FXML
    private Label cufflinksMultiLabel;
    @FXML
    private Label cufflinksUpperLabel;
    @FXML
    private Label cufflinksTotalLabel;
    @FXML
    private Label cufflinksLengthLabel;
    @FXML
    private Label cufflinksEffectiveLabel;
    @FXML
    private Label cufflinksFractionLabel;
    @FXML
    private Label cufflinksOtherLabel;
    @FXML
    private Label cuffdiffOptionsLabel;
    @FXML
    private Label cuffdiffMaskLabel;
    @FXML
    private Label cuffdiffBiasLabel;
    @FXML
    private Label cuffdiffMultiLabel;
    @FXML
    private Label cuffdiffMethodLabel;
    @FXML
    private Label cuffdiffTotalLabel;
    @FXML
    private Label cuffdiffCompLabel;
    @FXML
    private Label cuffdiffMinimumLabel;
    @FXML
    private Label cuffdiffFalseLabel;
    @FXML
    private Label cuffdiffDispersionLabel;
    @FXML
    private Label cuffdiffOtherLabel;
    @FXML
    private Pane cuffdiffAdvancedOptions;
    @FXML
    private Pane cufflinksAdvancedOptions;
    @FXML
    private RadioButton cuffdiffDefaultButtons;
    @FXML
    private RadioButton cuffdiffCustomButton;
    @FXML
    private RadioButton cufflinksCustomButton;
    @FXML
    private RadioButton cufflinksDefaultButton;
    @FXML
    private Label totalFilterLabel;
    @FXML
    private Label groupFilterLabel;
    @FXML
    private ListView<String> existingProjectView;
    @FXML
    private Label loadRecentFailed;
    @FXML
    private Pane starOptionsPane;
    @FXML
    private Label starOptionsLabel;
    @FXML
    private Pane starOptionPane;
    @FXML
    private TextField starOptions;
    @FXML
    private RadioButton starDefaultButton;
    @FXML
    private RadioButton starCustomButton;
    @FXML
    private RadioButton uploadFromComputer;
    @FXML
    private RadioButton useCloudFiles;
    @FXML
    private Label chooseLocationLabel;
    @FXML
    private Label tophatOptionsLabel;
    @FXML
    private RadioButton tophatAlignmentBtn;
    @FXML
    private RadioButton starAlignmentBtn;
    @FXML
    private Label projectNameLabel;
    @FXML
    private Button checkStatusBtn;
    @FXML
    private Pane projectStatusPane;
    @FXML
    private TextArea primerArea;
    @FXML
    private Button primerSubmit;
    @FXML
    private Label primerLabel;
    @FXML
    private RadioButton GRCh37;
    @FXML
    private RadioButton GRCh38;
    @FXML
    private RadioButton ratSpecies;
    @FXML
    private RadioButton GRCm38;
    @FXML
    private RadioButton NCBIM37;
    @FXML
    private RadioButton Rnor;
    @FXML
    private RadioButton RGSC3;
    @FXML
    private Pane customLibraryPane;
    @FXML
    private ChoiceBox<String> directionBox;
    @FXML
    private TextField adapterLength;
    @FXML
    private CheckBox adapterDefaultBtn;
    @FXML
    private Label mainSeqLabel;
    @FXML
    private TextField main3Seq;
    @FXML
    private TextField main5Seq;
    @FXML
    private TextField right3Seq;
    @FXML
    private TextField right5Seq;
    @FXML
    private Label rightSeqLabel;
    @FXML
    private TextField sampleName;
    @FXML
    private Label statusLabel;
    @FXML
    private Button retrieveResultsButton;
    @FXML
    private Pane authenticationPane;
    @FXML
    private Label authenticationLabel;
    @FXML
    private RadioButton passwordBtn;
    @FXML
    private RadioButton fileAuthBtn;
    @FXML
    private Label authLabel;
    @FXML
    private Button deleteProjectButton;
    @FXML
    private Pane secretSplicingPane;
    @FXML
    private Tab submitTab;
    @FXML
    private Button finalSubmitBtn;
    @FXML
    private CheckBox jobScheduleBtn;
    @FXML
    private Pane jobSchedPane;
    @FXML
    private Label queueLabel;
    @FXML
    private TextField queueName;
    @FXML
    private Label memLabel;
    @FXML
    private TextField memUsage;
    @FXML
    private Label processorLabel;
    @FXML
    private TextField processorNum;
    @FXML
    private Label timeLabel;
    @FXML
    private TextField maxTime;
    @FXML
    private TextField LSFproject;
    @FXML
    private Pane remotePane;
    @FXML
    private Label singlePairLabel;
    @FXML
    private Label libTypeLabel;
    @FXML
    private TextField homeFolder;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label insertLabel;
    @FXML
    private Label cvLabel;
    @FXML
    private CheckBox DECuffdiff;
    @FXML
    private CheckBox DEedgeR;
    @FXML
    private CheckBox DEDESeq2;
    @FXML
    private Label edgeRLabel;
    @FXML
    private CheckBox exactTest;
    @FXML
    private CheckBox GML;
    @FXML
    private Pane edgeRPane;
    @FXML
    private TextField edgeRFDR;
    @FXML
    private ComboBox<String> edgeRFDRMethod;
    @FXML
    private ComboBox<String> comparisonGroups1;
    @FXML
    private ComboBox<String> comparisonGroups2;
    @FXML
    private Button addComparisonButton;
    @FXML
    private ListView<String> comparisonsBox;
    @FXML
    private Button deleteComparisonButton;
    @FXML
    private Pane DESeq2Pane;
    @FXML
    private TextField DESeq2FDR;
    @FXML
    private ComboBox<String> DESeq2FDRMethod;
    @FXML
    private ComboBox<String> comparisonGroups1DESeq2;
    @FXML
    private ComboBox<String> comparisonGroups2DESeq2;
    @FXML
    private Button addComparisonButtonDESeq2;
    @FXML
    private ListView<String> comparisonsBoxDESeq2;
    @FXML
    private Button deleteComparisonButtonDESeq2;
    private int current_index;
    private String selectedGroup1;
    private String selectedGroup2;
    private String selectedGroup1DESeq2;
    private String selectedGroup2DESeq2;
    private final ObservableList<String> combinations = FXCollections.observableArrayList();
    private final ObservableList<String> combinationsDESeq2 = FXCollections.observableArrayList();
    private boolean edgeRComplete = true;
    private boolean DESeq2Complete = true;
    @FXML
    private Label cuffdiffDifferentialLabel1;
    @FXML
    private RadioButton pairwiseCompBtn1;
    @FXML
    private RadioButton timecourseCompBtn1;
    @FXML
    private Label DESeq2Label;
    @FXML
    private Label timeLabel1;
    
    @FXML
    private void DEDESeq2Selected(ActionEvent event) {
    if(DEDESeq2.isSelected()){
        if (!combinationsDESeq2.isEmpty()) {DESeq2Complete = true;}
        else {DESeq2Complete = false;}
        DESeq2Pane.setVisible(true);
        List groupItems = groups.getItems();
        selectableGroups1DESeq2.setAll(groupItems);
        selectableGroups2DESeq2.setAll(groupItems);
        comparisonGroups1DESeq2.getItems().setAll(selectableGroups1DESeq2);
        comparisonGroups2DESeq2.getItems().setAll(selectableGroups2DESeq2);
        }
    else{DESeq2Complete = true; DESeq2Pane.setVisible(false);}
    if (DEDESeq2.isSelected() && !combinationsDESeq2.isEmpty()) {
        DESeq2Complete = true;
    }
    if(edgeRComplete==true && DESeq2Complete==true){
        submitTab.setDisable(false);    
    }
    else {submitTab.setDisable(true);}
    if (!DECuffdiff.isSelected() && !DEDESeq2.isSelected() && !DEedgeR.isSelected()) {submitTab.setDisable(true);}
    }
    
    @FXML
    private void exactSelected(ActionEvent event) {
    if (GML.isSelected() || exactTest.isSelected()) {
        if (!combinations.isEmpty()) {
            edgeRComplete = true;
        }
        else {edgeRComplete = false;}
    }
    else {edgeRComplete = false;}
    if(edgeRComplete==true && DESeq2Complete==true){
        submitTab.setDisable(false);    
    }
    else {submitTab.setDisable(true);}
    }
    
    @FXML
    private void GMLselected(ActionEvent event) {
    if (GML.isSelected() || exactTest.isSelected()) {
        if (!combinations.isEmpty()) {
            edgeRComplete = true;
        }
        else {edgeRComplete = false;}
    }
    else {edgeRComplete = false;}
    if(edgeRComplete==true && DESeq2Complete==true){
        submitTab.setDisable(false);    
    }
    else {submitTab.setDisable(true);}
    }
    
    @FXML
    private void deleteComparison(ActionEvent event) {
    combinations.remove(comparisonsBox.getSelectionModel().getSelectedItem());
    comparisonsBox.setItems(combinations);
    if(!combinations.isEmpty() && (GML.isSelected() || exactTest.isSelected())){
        edgeRComplete=true;
    }
    else{edgeRComplete=false;}
    if(edgeRComplete==true && DESeq2Complete==true){
    submitTab.setDisable(false);    
    }
    else {submitTab.setDisable(true);}
    }
    
    @FXML
    private void deleteComparisonDESeq2(ActionEvent event) {
    combinationsDESeq2.remove(comparisonsBoxDESeq2.getSelectionModel().getSelectedItem());
    comparisonsBoxDESeq2.setItems(combinationsDESeq2);
    if(combinationsDESeq2.isEmpty()){
    DESeq2Complete=false;
    }
    else {DESeq2Complete = true;}
    if(edgeRComplete==true && DESeq2Complete==true){
    submitTab.setDisable(false);    
    }
    else {submitTab.setDisable(true);}
    }
    
    
    @FXML
    private void addComparison(ActionEvent event) {
    String combination = selectedGroup1+"vs"+selectedGroup2;
    combinations.add(combination);
    comparisonsBox.setItems(combinations);
    if(GML.isSelected() || exactTest.isSelected()){
    edgeRComplete=true;
    }
    if(edgeRComplete==true && DESeq2Complete==true){
        submitTab.setDisable(false);    
    }
    else {submitTab.setDisable(true);}  
    }
    
    
    @FXML
    private void addComparisonDESeq2(ActionEvent event) {
    String combination = selectedGroup1DESeq2+"vs"+selectedGroup2DESeq2;
    combinationsDESeq2.add(combination);
    comparisonsBoxDESeq2.setItems(combinationsDESeq2);
    DESeq2Complete=true;
    if(edgeRComplete==true && DESeq2Complete==true){
        submitTab.setDisable(false);    
    }
    else {submitTab.setDisable(true);}
    }
    
    @FXML
    private void DECuffdiffSelected(ActionEvent event) {
        if(DECuffdiff.isSelected()){
            cuffdiffOptionsLabel.setVisible(true);
            cuffdiffDefaultButtons.setVisible(true);
            cuffdiffCustomButton.setVisible(true);
            if (DESeq2Complete && edgeRComplete) {
                submitTab.setDisable(false);
            }
            else {submitTab.setDisable(true);}
        }
        else{
            cuffdiffOptionsLabel.setVisible(false);
            cuffdiffDefaultButtons.setVisible(false);
            cuffdiffCustomButton.setVisible(false);
            cuffdiffAdvancedOptions.setVisible(false);
            cuffdiffDefaultButtons.setSelected(true);
            cuffdiffCustomButton.setSelected(false);
            if (DESeq2Complete && edgeRComplete) {
                submitTab.setDisable(false);
            }
            else {submitTab.setDisable(true);}
        }
        if (!DECuffdiff.isSelected() && !DEDESeq2.isSelected() && !DEedgeR.isSelected()) {submitTab.setDisable(true);}
    }
    @FXML
    private void DEedgeRSelected(ActionEvent event) {
        if(DEedgeR.isSelected()){
        if (!combinations.isEmpty() && (GML.isSelected() || exactTest.isSelected())) {edgeRComplete = true;}
        else {edgeRComplete = false;}
        edgeRPane.setVisible(true);
        List groupItems = groups.getItems();
        selectableGroups1.setAll(groupItems);
        selectableGroups2.setAll(groupItems);
        comparisonGroups1.getItems().setAll(selectableGroups1);
        comparisonGroups2.getItems().setAll(selectableGroups2);
        }
        else{
        edgeRComplete = true;
        edgeRPane.setVisible(false);    
        }
        if(edgeRComplete==true && DESeq2Complete==true){
            submitTab.setDisable(false);    
        }
        else {submitTab.setDisable(true);}
        if (!DECuffdiff.isSelected() && !DEDESeq2.isSelected() && !DEedgeR.isSelected()) {submitTab.setDisable(true);}
    }
    @FXML
    private void comparisonGroups1Selected(ActionEvent event) {
    ObservableList groupItems1 = FXCollections.observableArrayList();
    String selected1 = comparisonGroups1.getSelectionModel().getSelectedItem();
    for(Object a: groups.getItems()){
    if(!a.equals(selected1)){
    groupItems1.add(a);
    }
    }
    System.out.print(groupItems1);
    selectableGroups2.setAll(groupItems1);
    comparisonGroups2.getItems().setAll(selectableGroups2);
    selectedGroup1 = comparisonGroups1.getSelectionModel().getSelectedItem();
    }
    
    @FXML
    private void comparisonGroups2Selected(ActionEvent event) {
    ObservableList groupItems2 = FXCollections.observableArrayList();  
    String selected2 = comparisonGroups2.getSelectionModel().getSelectedItem();
    for(Object a: groups.getItems()){
    if(!a.equals(selected2)){
    groupItems2.add(a);
    }
    }
    selectableGroups1.setAll(groupItems2);
    comparisonGroups1.getItems().setAll(selectableGroups1);
    selectedGroup2 = comparisonGroups2.getSelectionModel().getSelectedItem();
    }
    
    @FXML
    private void comparisonGroups1SelectedDESeq2(ActionEvent event) {
    ObservableList groupItems1 = FXCollections.observableArrayList();
    String selected1 = comparisonGroups1DESeq2.getSelectionModel().getSelectedItem();
    for(Object a: groups.getItems()){
    if(!a.equals(selected1)){
    groupItems1.add(a);
    }
    }
    System.out.print(groupItems1);
    selectableGroups2DESeq2.setAll(groupItems1);
    comparisonGroups2DESeq2.getItems().setAll(selectableGroups2DESeq2);
    selectedGroup1DESeq2 = comparisonGroups1DESeq2.getSelectionModel().getSelectedItem();
    }
    
    @FXML
    private void comparisonGroups2SelectedDESeq2(ActionEvent event) {
    ObservableList groupItems2 = FXCollections.observableArrayList();  
    String selected2 = comparisonGroups2DESeq2.getSelectionModel().getSelectedItem();
    for(Object a: groups.getItems()){
    if(!a.equals(selected2)){
    groupItems2.add(a);
    }
    }
    selectableGroups1DESeq2.setAll(groupItems2);
    comparisonGroups1DESeq2.getItems().setAll(selectableGroups1DESeq2);
    selectedGroup2DESeq2 = comparisonGroups2DESeq2.getSelectionModel().getSelectedItem();
    }
    
    @FXML
    private void passwordAuthClick(ActionEvent event) {
        passwordBtn.setSelected(true);
        fileAuthBtn.setSelected(false);
        keyFileBrowse.setVisible(false);
        keyFile.clear();
        keyFile.setVisible(false);
        passwordField.setVisible(true);
        authLabel.setText("Password:");
        authenticationPane.setVisible(true);
    }

    @FXML
    private void fileAuthClick(ActionEvent event) {
        passwordBtn.setSelected(false);
        fileAuthBtn.setSelected(true);
        keyFileBrowse.setVisible(true);
        keyFile.clear();
        keyFile.setVisible(true);
        passwordField.clear();
        passwordField.setVisible(false);
        authLabel.setText("Private Key File:");
        authenticationPane.setVisible(true);
    }
    
    void populateRecentProjectList() throws FileNotFoundException, IOException {
        FileReader readProjects = new FileReader(projectFile);
        BufferedReader textReader = new BufferedReader(readProjects);
        int numLines = readLines(projectFile.getPath());
        String[] recentProjects = new String[numLines];
        for(int i = 0; i < numLines; i++) {
            recentProjects[i] = textReader.readLine();
        }
        textReader.close();
        projectNames.clear();
        for(int j = 0; j < numLines; j++) {
            String locationIterator = recentProjects[j].split("\t")[0];
            String nameIterator = recentProjects[j].split("\t")[1];
            projectNames.add(nameIterator);
            if (locationIterator != null && nameIterator != null) {
                if (j == 0) {
                    projectWrapper.p1.projectLocation = locationIterator;
                    projectWrapper.p1.projectName = nameIterator;
                } else if (j == 1) {
                    projectWrapper.p2.projectLocation = locationIterator;
                    projectWrapper.p2.projectName = nameIterator;
                } else if (j == 2) {
                    projectWrapper.p3.projectLocation = locationIterator;
                    projectWrapper.p3.projectName = nameIterator;
                } else if (j == 3) {
                    projectWrapper.p4.projectLocation = locationIterator;
                    projectWrapper.p4.projectName = nameIterator;
                } else if (j == 4) {
                    projectWrapper.p5.projectLocation = locationIterator;
                    projectWrapper.p5.projectName = nameIterator;
                } else if (j == 5) {
                    projectWrapper.p6.projectLocation = locationIterator;
                    projectWrapper.p6.projectName = nameIterator;
                } else if (j == 6) {
                    projectWrapper.p7.projectLocation = locationIterator;
                    projectWrapper.p7.projectName = nameIterator;
                } else if (j == 7) {
                    projectWrapper.p8.projectLocation = locationIterator;
                    projectWrapper.p8.projectName = nameIterator;
                } else if (j == 8) {
                    projectWrapper.p9.projectLocation = locationIterator;
                    projectWrapper.p9.projectName = nameIterator;
                } else if (j == 9) {
                    projectWrapper.p10.projectLocation = locationIterator;
                    projectWrapper.p10.projectName = nameIterator;
                }
            }
        }
        existingProjectView.setItems(projectNames);
    }

    void clearProject(projectData currentProject) {
        currentProject.EC2Address = null;
        currentProject.keyFileLocation = null;
        currentProject.projectLocation = null;
        currentProject.projectName = null;
        currentProject.serverUser = null;
        currentProject.home = null;
    }
    
    void deleteLocalFiles(File file) {
        if(file.isDirectory()){
            //directory is empty, then delete it
            if(file.list().length==0){
               file.delete();
               System.out.println("Directory is deleted : " + file.getAbsolutePath());
            }
            else {
                //list all the directory contents
                String files[] = file.list();
                for (String temp : files) {
                    //construct the file structure
                    File fileDelete = new File(file, temp);
                    //recursive delete
                    deleteLocalFiles(fileDelete);
                }

                //check the directory again, if empty then delete it
                if(file.list().length==0){
                    file.delete();
                    System.out.println("Directory is deleted : " 
                                              + file.getAbsolutePath());
                }
            }
        }
        else {
    		//if file, then delete it
    		file.delete();
    		System.out.println("File is deleted : " + file.getAbsolutePath());
            }
        }
    
    
    @FXML
    private void deleteSelectedProject(ActionEvent event) throws IOException {
        int ind = existingProjectView.getSelectionModel().getSelectedIndex();
        if (ind != -1) {
            String projectPath = "";
            PipelineUI current = new PipelineUI();
            Boolean confirm = current.confirmDeleteProject();
            if(confirm) {
                File refLocation = new File (project.projectLocation);
                String projectName = project.projectName;
                String path = refLocation + sep + projectName + sep + "output.txt";
                File checkSubmit = new File(path);
                if (checkSubmit.exists()) {
                    FileReader fr = new FileReader(path);
                    BufferedReader textReader = new BufferedReader(fr);
                    int numOfLines = readLines(path);
                    String[] fileData = new String[numOfLines];
                    for(int i = 0; i < numOfLines; i++) {
                        fileData[i] = textReader.readLine();
                    }
                    textReader.close();
                    if (!fileData[1].equals("local:")) {
                        String[] cloudSettings = fileData[1].split("\t");
                        String user = cloudSettings[0];
                        String address = cloudSettings[1];
                        String homeFolder = cloudSettings[2];
                        String AuthMeth = cloudSettings[3];
                        String keyfile = cloudSettings[4];
                    System.out.print(project.projectName+keyfile+address+user+AuthMeth);
                    String connectionStatus=checkConnection.main(keyfile, address, user, AuthMeth);
                    if(connectionStatus.equals("success")){
                    deleteServerFiles.main(project.projectName, keyfile, address, AuthMeth, user, homeFolder);
                    }
                    }
                }
                if (ind == 0) {
                    projectPath = projectWrapper.p1.projectLocation + sep + projectWrapper.p1.projectName;
                    clearProject(projectWrapper.p1);
                    if (!isProjectEmpty(projectWrapper.p2)) {projectWrapper.p1.change(projectWrapper.p2); clearProject(projectWrapper.p2);}
                    if (!isProjectEmpty(projectWrapper.p3)) {projectWrapper.p2.change(projectWrapper.p3); clearProject(projectWrapper.p3);}
                    if (!isProjectEmpty(projectWrapper.p4)) {projectWrapper.p3.change(projectWrapper.p4); clearProject(projectWrapper.p4);}
                    if (!isProjectEmpty(projectWrapper.p5)) {projectWrapper.p4.change(projectWrapper.p5); clearProject(projectWrapper.p5);}
                    if (!isProjectEmpty(projectWrapper.p6)) {projectWrapper.p5.change(projectWrapper.p6); clearProject(projectWrapper.p6);}
                    if (!isProjectEmpty(projectWrapper.p7)) {projectWrapper.p6.change(projectWrapper.p7); clearProject(projectWrapper.p7);}
                    if (!isProjectEmpty(projectWrapper.p8)) {projectWrapper.p7.change(projectWrapper.p8); clearProject(projectWrapper.p8);}
                    if (!isProjectEmpty(projectWrapper.p9)) {projectWrapper.p8.change(projectWrapper.p9); clearProject(projectWrapper.p9);}
                    if (!isProjectEmpty(projectWrapper.p10)) {projectWrapper.p9.change(projectWrapper.p10); clearProject(projectWrapper.p10);}
                }
                if (ind == 1) {
                    projectPath = projectWrapper.p2.projectLocation + sep + projectWrapper.p2.projectName;
                    clearProject(projectWrapper.p2);
                    if (!isProjectEmpty(projectWrapper.p3)) {projectWrapper.p2.change(projectWrapper.p3); clearProject(projectWrapper.p3);}
                    if (!isProjectEmpty(projectWrapper.p4)) {projectWrapper.p3.change(projectWrapper.p4); clearProject(projectWrapper.p4);}
                    if (!isProjectEmpty(projectWrapper.p5)) {projectWrapper.p4.change(projectWrapper.p5); clearProject(projectWrapper.p5);}
                    if (!isProjectEmpty(projectWrapper.p6)) {projectWrapper.p5.change(projectWrapper.p6); clearProject(projectWrapper.p6);}
                    if (!isProjectEmpty(projectWrapper.p7)) {projectWrapper.p6.change(projectWrapper.p7); clearProject(projectWrapper.p7);}
                    if (!isProjectEmpty(projectWrapper.p8)) {projectWrapper.p7.change(projectWrapper.p8); clearProject(projectWrapper.p8);}
                    if (!isProjectEmpty(projectWrapper.p9)) {projectWrapper.p8.change(projectWrapper.p9); clearProject(projectWrapper.p9);}
                    if (!isProjectEmpty(projectWrapper.p10)) {projectWrapper.p9.change(projectWrapper.p10); clearProject(projectWrapper.p10);}
                }
                if (ind == 2) {
                    projectPath = projectWrapper.p3.projectLocation + sep + projectWrapper.p3.projectName;
                    clearProject(projectWrapper.p3);
                    if (!isProjectEmpty(projectWrapper.p4)) {projectWrapper.p3.change(projectWrapper.p4); clearProject(projectWrapper.p4);}
                    if (!isProjectEmpty(projectWrapper.p5)) {projectWrapper.p4.change(projectWrapper.p5); clearProject(projectWrapper.p5);}
                    if (!isProjectEmpty(projectWrapper.p6)) {projectWrapper.p5.change(projectWrapper.p6); clearProject(projectWrapper.p6);}
                    if (!isProjectEmpty(projectWrapper.p7)) {projectWrapper.p6.change(projectWrapper.p7); clearProject(projectWrapper.p7);}
                    if (!isProjectEmpty(projectWrapper.p8)) {projectWrapper.p7.change(projectWrapper.p8); clearProject(projectWrapper.p8);}
                    if (!isProjectEmpty(projectWrapper.p9)) {projectWrapper.p8.change(projectWrapper.p9); clearProject(projectWrapper.p9);}
                    if (!isProjectEmpty(projectWrapper.p10)) {projectWrapper.p9.change(projectWrapper.p10); clearProject(projectWrapper.p10);}
                }
                if (ind == 3) {
                    projectPath = projectWrapper.p4.projectLocation + sep + projectWrapper.p4.projectName;
                    clearProject(projectWrapper.p4);
                    if (!isProjectEmpty(projectWrapper.p5)) {projectWrapper.p4.change(projectWrapper.p5); clearProject(projectWrapper.p5);}
                    if (!isProjectEmpty(projectWrapper.p6)) {projectWrapper.p5.change(projectWrapper.p6); clearProject(projectWrapper.p6);}
                    if (!isProjectEmpty(projectWrapper.p7)) {projectWrapper.p6.change(projectWrapper.p7); clearProject(projectWrapper.p7);}
                    if (!isProjectEmpty(projectWrapper.p8)) {projectWrapper.p7.change(projectWrapper.p8); clearProject(projectWrapper.p8);}
                    if (!isProjectEmpty(projectWrapper.p9)) {projectWrapper.p8.change(projectWrapper.p9); clearProject(projectWrapper.p9);}
                    if (!isProjectEmpty(projectWrapper.p10)) {projectWrapper.p9.change(projectWrapper.p10); clearProject(projectWrapper.p10);}
                }
                if (ind == 4) {
                    projectPath = projectWrapper.p5.projectLocation + sep + projectWrapper.p5.projectName;
                    clearProject(projectWrapper.p5);
                    if (!isProjectEmpty(projectWrapper.p6)) {projectWrapper.p5.change(projectWrapper.p6); clearProject(projectWrapper.p6);}
                    if (!isProjectEmpty(projectWrapper.p7)) {projectWrapper.p6.change(projectWrapper.p7); clearProject(projectWrapper.p7);}
                    if (!isProjectEmpty(projectWrapper.p8)) {projectWrapper.p7.change(projectWrapper.p8); clearProject(projectWrapper.p8);}
                    if (!isProjectEmpty(projectWrapper.p9)) {projectWrapper.p8.change(projectWrapper.p9); clearProject(projectWrapper.p9);}
                    if (!isProjectEmpty(projectWrapper.p10)) {projectWrapper.p9.change(projectWrapper.p10); clearProject(projectWrapper.p10);}
                }
                if (ind == 5) {
                    projectPath = projectWrapper.p6.projectLocation + sep + projectWrapper.p6.projectName;
                    clearProject(projectWrapper.p6);
                    if (!isProjectEmpty(projectWrapper.p7)) {projectWrapper.p6.change(projectWrapper.p7); clearProject(projectWrapper.p7);}
                    if (!isProjectEmpty(projectWrapper.p8)) {projectWrapper.p7.change(projectWrapper.p8); clearProject(projectWrapper.p8);}
                    if (!isProjectEmpty(projectWrapper.p9)) {projectWrapper.p8.change(projectWrapper.p9); clearProject(projectWrapper.p9);}
                    if (!isProjectEmpty(projectWrapper.p10)) {projectWrapper.p9.change(projectWrapper.p10); clearProject(projectWrapper.p10);}
                }
                if (ind == 6) {
                    projectPath = projectWrapper.p7.projectLocation + sep + projectWrapper.p7.projectName;
                    clearProject(projectWrapper.p7);
                    if (!isProjectEmpty(projectWrapper.p8)) {projectWrapper.p7.change(projectWrapper.p8); clearProject(projectWrapper.p8);}
                    if (!isProjectEmpty(projectWrapper.p9)) {projectWrapper.p8.change(projectWrapper.p9); clearProject(projectWrapper.p9);}
                    if (!isProjectEmpty(projectWrapper.p10)) {projectWrapper.p9.change(projectWrapper.p10); clearProject(projectWrapper.p10);}
                }
                if (ind == 7) {
                    projectPath = projectWrapper.p8.projectLocation + sep + projectWrapper.p8.projectName;
                    clearProject(projectWrapper.p8);
                    if (!isProjectEmpty(projectWrapper.p9)) {projectWrapper.p8.change(projectWrapper.p9); clearProject(projectWrapper.p9);}
                    if (!isProjectEmpty(projectWrapper.p10)) {projectWrapper.p9.change(projectWrapper.p10); clearProject(projectWrapper.p10);}
                }
                if (ind == 8) {
                    projectPath = projectWrapper.p9.projectLocation + sep + projectWrapper.p9.projectName;
                    clearProject(projectWrapper.p9);
                    if (!isProjectEmpty(projectWrapper.p10)) {projectWrapper.p9.change(projectWrapper.p10); clearProject(projectWrapper.p10);}
                }
                if (ind == 9) {
                    projectPath = projectWrapper.p10.projectLocation + sep + projectWrapper.p10.projectName;
                    clearProject(projectWrapper.p10);
                }
                File localPath = new File(projectPath);
                if (localPath.exists()) {
                    //LocalFiles(localPath);
                    
                    }
                resetSettings();
                writeProjectFile();
                populateRecentProjectList();
                groupsTab.setDisable(true);
                addSamples.setDisable(true);
                parameters.setDisable(true);
                submitTab.setDisable(true);
                projectStatusPane.setVisible(false);
            }
        }
    }

    @FXML
    private void toggleJobSchedule(ActionEvent event) {
        if (jobScheduleBtn.isSelected()) {
            jobSchedPane.setVisible(true);
        }
        else {
            jobSchedPane.setVisible(false);
            queueName.setText("bigmem");
            memUsage.setText("60000");
            processorNum.setText("8");
            maxTime.setText("24:00");
            LSFproject.setText("default");
        }
    }

    private void localBtnClick(ActionEvent event) {
        remotePane.setVisible(false);
    }

    private void remoteBtnClick(ActionEvent event) {
        remotePane.setVisible(true);
    }
    
    public class adapterSettings {
        String name;
        String direction;
        String main5;
        String main3;
        String right5;
        String right3;
        String length;
        String output;
        
        public adapterSettings() {
            name = null;
            direction = null;
            main5 = null;
            main3 = null;
            right5 = null;
            right3 = null;
            length = null;
            output = null;
        }
        
        public adapterSettings(String preset) {
            if (preset.equals("Illumina mRNA single")) {
                name = "Illumina mRNA single";
                direction = "unstranded";
                main5 = "GCTCTTCCGATCT";
                main3 = "AGATCGGAAGAGC";
                length = "120";
                output = "Illumina mRNA single:single:GCTCTTCCGATCT:AGATCGGAAGAGC:120:unstranded";
            }
            if (preset.equals("Illumina small RNA single")) {
                name = "Illumina small RNA single";
                direction = "secondstrand";
                main5 = "AGTCCGACGATC";
                main3 = "ATCTCGTATGCC";
                length = "73";
                output = "Illumina small RNA single:single:AGTCCGACGATC:ATCTCGTATGCC:73:secondstrand";    
            }
            if (preset.equals("Epicenter paired")) {
                name = "Epicenter paired";
                direction = "secondstrand";
                main5 = "GCTCTTCCGATCT";
                main3 = "AGATCGGAAGAGC";
                right5 = "GCTCTTCCGATCT";
                right3 = "AGATCGGAAGAGC";
                length = "120";
                output = "Epicenter paired:paired:GCTCTTCCGATCT:AGATCGGAAGAGC:GCTCTTCCGATCT:AGATCGGAAGAGC:120:secondstrand";
            }
            if (preset.equals("NEB small RNA paired")) {
                name = "NEB small RNA paired";
                direction = "secondstrand";
                main5 = "TCCGACGATC";
                main3 = "AGATCGGAAG";
                right5 = "CTTCCGATCT";
                right3 = "GTCGGACTGT";
                length = "120";
                output = "NEB small RNA paired:paired:TCCGACGATC:AGATCGGAAG:CTTCCGATCT:GTCGGACTGT:120:secondstrand";
            }
            if (preset.equals("NEB Ultra paired")) {
                name = "NEB Ultra paired";
                direction = "firststrand";
                main5 = "";
                main3 = "AGATCGGAAGAGC";
                right5 = "";
                right3 = "AGATCGGAAGAGC";
                length = "120";
                output = "NEB Ultra paired:paired:NONE:AGATCGGAAGAGC:NONE:AGATCGGAAGAGC:120:firststrand";
            }
            if (preset.equals("Custom single")) {
                name = "Custom single";
                direction = directionBox.getValue();
                main5 = main5Seq.getText(); if (main5Seq.getText().isEmpty()) {main5 = "NONE";}
                main3 = main3Seq.getText(); if (main3Seq.getText().isEmpty()) {main3 = "NONE";}
                length = adapterLength.getText();
                output = "Custom single:single:" + main5 + ":" + main3 + ":" + length + ":" + direction;
            }
            if (preset.equals("Custom paired")) {
                name = "Custom paired";
                direction = directionBox.getValue();
                main5 = main5Seq.getText(); if (main5Seq.getText().isEmpty()) {main5 = "NONE";}
                main3 = main3Seq.getText(); if (main3Seq.getText().isEmpty()) {main3 = "NONE";}
                right5 = right5Seq.getText(); if (right5Seq.getText().isEmpty()) {right5 = "NONE";}
                right3 = right3Seq.getText(); if (right3Seq.getText().isEmpty()) {right3 = "NONE";}
                length = adapterLength.getText();
                output = "Custom paired:paired:" + main5 + ":" + main3 + ":" + right5 + ":" + right3 + ":" + length + ":" + direction;
            }
        }
        
        public void createFromOutput(String fromText) {
            String[] settings = fromText.split(":");
            this.name = settings[0];
            this.main5 = settings[2];
            this.main3 = settings[3];
            if (settings.length == 6) {
                this.length = settings[4];
                this.direction = settings[5];
                this.right5 = "";
                this.right3 = "";
            }
            else if (settings.length == 8) {
                this.right5 = settings[4];
                this.right3 = settings[5];
                this.length = settings[6];
                this.direction = settings[7];
            }
        }
        
        public boolean exists() {
            if (direction.equals("unstranded") || direction.equals("firststrand") || direction.equals("secondstrand")) {
                return true;
            }
            else {return false;}
        }
    }
    
    public class jobSchedule {
        
        String processorNumber;
        String maxMem;
        String queue;
        String wait;
        String output;
        String LSFprojectName;
        
        public jobSchedule() {
            queue = queueName.getText();
            maxMem = memUsage.getText();
            processorNumber = processorNum.getText();
            wait = maxTime.getText();
            LSFprojectName = LSFproject.getText();
            output = queue + "," + maxMem + "," + processorNumber + "," + wait + "," + LSFprojectName;
        }
        
        public jobSchedule(String input) {
            output = input;
            String[] settings = input.split(",");
            queue = settings[0];
            maxMem = settings[1];
            processorNumber = settings[2];
            wait = settings[3];
            LSFprojectName = settings[4];
        }
    }
    
    /* Class that contains strings for basic parameters ("yes" if raw reads yes
    /  is selected, for example).  Also contains a constructor method allowing
    / strings to be passed to the class.                                      */
    public class basicParameters {
        
        String rawReads;
        String transcriptFilter;
        String expressionFilter;
        String species;
        String genome;
        String pairwise;
        String splicing;
        String readFromWhere;
        String alignment;
        
        public basicParameters(String reads, String transcript, String expression, String animal, String genes, String timepair, String splice, String alignMethod) {
            rawReads = reads;
            transcriptFilter = transcript;
            expressionFilter = expression;
            species = animal;
            genome = genes;
            pairwise = timepair;
            splicing = splice;
            alignment = alignMethod;
        }
    }
    
    /* Same as above, but for the cufflinks parameters.                       */
    public class cufflinksParameters {
        String cufflinksMask;
        String cufflinksBias;
        String cufflinksMulti;
        String cufflinksUpper;
        String cufflinksTotal;
        String cufflinksEffective;
        String cufflinksLength;
        String cufflinksFraction;
        String cufflinksOther;
        
        public cufflinksParameters(String mask, String bias, String multi, String upper, String total, String effective, String length, String fraction, String other) {
            cufflinksMask = mask;
            cufflinksBias = bias;
            cufflinksMulti = multi;
            cufflinksUpper = upper;
            cufflinksTotal = total;
            cufflinksEffective = effective;
            cufflinksLength = length;
            cufflinksFraction = fraction;
            cufflinksOther = other;
        }
    }
    
    /* Again, same as above, this time for cuffdiff parameters.               */
    public class cuffdiffParameters {
        String cuffdiffMask;
        String cuffdiffBias;
        String cuffdiffMulti;
        String cuffdiffNormMethod;
        String cuffdiffTotal;
        String cuffdiffComp;
        String cuffdiffMin;
        String cuffdiffFalse;
        String cuffdiffDispersion;
        String cuffdiffOther;
        
        public cuffdiffParameters(String mask, String bias, String multi, String method, String total, String comp, String min, String discovery, String dispersion, String other) {
            cuffdiffMask = mask;
            cuffdiffBias = bias;
            cuffdiffMulti = multi;
            cuffdiffNormMethod = method;
            cuffdiffTotal = total;
            cuffdiffComp = comp;
            cuffdiffMin = min;
            cuffdiffFalse = discovery;
            cuffdiffDispersion = dispersion;
            cuffdiffOther = other;
        }
    }
    
    /* Class for sample information with constructor method included.         */
    public class sampleInfo {

        String groupName;
        String sampleName;
        String readLocation;
        adapterSettings library;
        String meanInsertSize;
        String CV;

        public sampleInfo(String group, String sample, String file, adapterSettings lib, String insert, String cv) {
            groupName = group;
            sampleName = sample;
            readLocation = file;
            library = lib;
            meanInsertSize = insert;
            CV = cv;
        }
    }
    
    /* Class and constructor method for creating a projectData type.          */
    public class projectData {
        
        public String projectName = null;
        public String projectLocation = null;
        public String keyFileLocation = null;
        public String EC2Address = null;
        public String serverUser = null;
        public String home = null;
        public jobSchedule jobSched = null;
        
        public projectData () {
        }
        
        public projectData (String name, String location) {
            projectName = name;
            projectLocation = location;
        }
        
        public void change (projectData oldProject) {
            this.EC2Address = oldProject.EC2Address;
            this.keyFileLocation = oldProject.keyFileLocation;
            this.projectLocation = oldProject.projectLocation;
            this.projectName = oldProject.projectName;
            this.serverUser = oldProject.serverUser;
            this.jobSched = oldProject.jobSched;
            this.home = oldProject.home;
        }
    }
    
    public boolean isProjectEmpty(projectData currentProj) {
        if (currentProj.projectName == null && currentProj.projectLocation == null) {
            return true;
        } else {return false;}
    }
    
    /* Wrapper class containing projectData variables initally set to null.    /
    /  These will be changed to reflect the recent projects that a user can    /
    /  load from.                                                                  */
    public class projectDataWrapper {
        projectData p1 = new projectData();
        projectData p2 = new projectData();
        projectData p3 = new projectData();
        projectData p4 = new projectData();
        projectData p5 = new projectData();
        projectData p6 = new projectData();
        projectData p7 = new projectData();
        projectData p8 = new projectData();
        projectData p9 = new projectData();
        projectData p10 = new projectData();
    }
    
    List allSamples = new ArrayList<>();
    public projectData project = new projectData();
    public projectDataWrapper projectWrapper = new projectDataWrapper();
    public basicParameters basic;
    public cufflinksParameters cufflinks;
    public cuffdiffParameters cuffdiff;
    public String sep = File.separator;
    File f = new File(System.getProperty("java.class.path"));
    File dir = f.getAbsoluteFile().getParentFile();
    String firstPath = dir.toString();
    String secondpath = firstPath.substring(0,firstPath.lastIndexOf("CANE"));
    String mainpath = secondpath + "CANE_v1.0" + sep + "project_file.txt";
    public File projectFile = new File(mainpath);
    
    @FXML
    private void computerUploadSelected(ActionEvent event) {
        singlePairLabel.setVisible(true);
        singleEnd.setVisible(true);
        pairedEnd.setVisible(true);
        uploadFromComputer.setSelected(true);
        useCloudFiles.setSelected(false);
        singleReadLocation.clear();
        singleReadLocation.setEditable(false);
        leftReadLocation.clear();
        leftReadLocation.setEditable(false);
        rightReadLocation.clear();
        rightReadLocation.setEditable(false);
        if (singleEnd.isSelected()) {
            singleReadLocation.setVisible(true);
            leftReadLocation.setVisible(false);
            rightReadLocation.setVisible(false);
            singleReadLocationBtn.setVisible(true);
            leftReadLocationBtn.setVisible(false);
            rightReadLocationBtn.setVisible(false);
        } else if (pairedEnd.isSelected()) {
            singleReadLocation.setVisible(false);
            singleReadLocationBtn.setVisible(false);
            leftReadLocation.setVisible(true);
            leftReadLocationBtn.setVisible(true);
            rightReadLocation.setVisible(true);
            rightReadLocationBtn.setVisible(true);
        } else {
            leftReadLocation.setVisible(false);
            rightReadLocation.setVisible(false);
            singleReadLocation.setVisible(false);
            singleReadLocationBtn.setVisible(false);
            leftReadLocationBtn.setVisible(false);
            rightReadLocationBtn.setVisible(false);
        }
    }
    
    void resetSample() {
        cuffdiffCustomButton.setSelected(false);
        cufflinksCustomButton.setSelected(false);
        singleReadLocation.clear();
        rightReadLocation.clear();
        leftReadLocation.clear();
        cVar.clear();
        insertSize.clear();
        sampleName.clear();
    }
    
    void resetSettings() {
        exactTest.setSelected(false);
        edgeRFDR.setText("0.05");
        DESeq2FDR.setText("0.05");
        comparisonGroups1.setValue("");
        comparisonGroups2.setValue("");
        comparisonGroups1DESeq2.setValue("");
        comparisonGroups2DESeq2.setValue("");
        combinations.setAll();
        comparisonsBox.setItems(combinations);
        combinationsDESeq2.setAll();
        comparisonsBoxDESeq2.setItems(combinationsDESeq2);
        DGE.setDisable(true);
        DESeq2Pane.setVisible(false);
        DESeq2FDRMethod.getSelectionModel().select("BH");
        edgeRFDRMethod.getSelectionModel().select("BH");
        DECuffdiff.setSelected(false);
        DEedgeR.setSelected(false);
        DEDESeq2.setSelected(false);
        cuffdiffOptionsLabel.setVisible(false);
        cuffdiffDefaultButtons.setVisible(false);
        cuffdiffCustomButton.setVisible(false);
        cuffdiffAdvancedOptions.setVisible(false);
        edgeRPane.setVisible(false);
        retrieveResultsButton.setDisable(true);
        singleEnd.setSelected(false);
        pairedEnd.setSelected(false);
        primerSelection.setDisable(true);
        authenticationPane.setVisible(false);
        libTypes.setAll("","Custom single", "Illumina mRNA single", "Illumina small RNA single", "Custom paired", "Epicenter paired", "NEB small RNA paired", "NEB Ultra paired");
        libType.getItems().setAll(libTypes);
        libType.getSelectionModel().getSelectedItem();
        libType.getSelectionModel().selectFirst();
        directions.setAll("unstranded", "firststrand", "secondstrand");
        directionBox.setItems(directions);
        edgeRcorrections.setAll("BH", "bonferroni", "fdr");
        edgeRFDRMethod.setItems(edgeRcorrections);
        edgeRFDRMethod.setValue("BH");
        DESeq2FDRMethod.setItems(edgeRcorrections);
        DESeq2FDRMethod.setValue("BH");
        jobSchedPane.setVisible(false);
        singleReadLocation.setVisible(false);
        rightReadLocation.setVisible(false);
        leftReadLocation.setVisible(false);
        noGroupError.setVisible(false);
        singleReadLocation.clear();
        rightReadLocation.clear();
        leftReadLocation.clear();
        cvLabel.setVisible(false);
        cVar.setVisible(false);
        cVar.clear();
        insertSize.setVisible(false);
        insertLabel.setVisible(false);
        insertSize.clear();
        sampleName.clear();
        singlePairLabel.setVisible(false);
        singleEnd.setVisible(false);
        pairedEnd.setVisible(false);
        libTypeLabel.setVisible(false);
        libType.setVisible(false);
        customLibraryPane.setVisible(false);
        singleReadLocationBtn.setVisible(false);
        rightReadLocationBtn.setVisible(false);
        leftReadLocationBtn.setVisible(false);
        singleReadLabel.setVisible(false);
        leftReadLabel.setVisible(false);
        rightReadLabel.setVisible(false);
        trimReadsYesBtn.setSelected(true);
        filterGenesYesBtn.setSelected(true);
        basicFilterYes.setSelected(true);
        totalFilter.setVisible(true);
        totalFilterLabel.setVisible(true);
        groupFilterLabel.setVisible(true);
        totalFilter.setText("75");
        groupFilter.setVisible(true);
        groupFilter.setText("75");
        splicingNo.setSelected(true);
        tophatAlignmentBtn.setSelected(false);
        starAlignmentBtn.setSelected(false);
        cufflinksFraction.setText("1.0");
        cufflinksMaskYes.setSelected(true);
        cufflinksMaskNo.setSelected(false);
        cufflinksBiasYes.setSelected(true);
        cufflinksBiasNo.setSelected(false);
        cufflinksMultiYes.setSelected(true);
        cufflinksMultiNo.setSelected(false);
        cufflinksUpperYes.setSelected(false);
        cufflinksUpperNo.setSelected(true);
        cufflinksTotalYes.setSelected(true);
        cufflinksTotalNo.setSelected(false);
        cufflinksLengthYes.setSelected(true);
        cufflinksLengthNo.setSelected(false);
        cufflinksEffYes.setSelected(true);
        cufflinksEffNo.setSelected(false);
        cuffdiffMaskYes.setSelected(true);
        cuffdiffMaskNo.setSelected(false);
        cuffdiffBiasYes.setSelected(true);
        cuffdiffBiasNo.setSelected(false);
        cuffdiffMultiYes.setSelected(true);
        cuffdiffMultiNo.setSelected(false);
        cuffdiffGeometric.setSelected(true);
        cuffdiffQuartile.setSelected(false);
        cuffdiffClassic.setSelected(false);
        cuffdiffTotalYes.setSelected(false);
        cuffdiffTotalNo.setSelected(true);
        cuffdiffCompYes.setSelected(true);
        cuffdiffCompNo.setSelected(false);
        cuffdiffPooled.setSelected(true);
        cuffdiffBlind.setSelected(false);
        cuffdiffPoisson.setSelected(false);
        cuffdiffPer.setSelected(false);
        cuffdiffMinimum.setText("10");
        cuffdiffFalse.setText("0.05");
        pairwiseCompBtn.setSelected(true);
        timecourseCompBtn.setSelected(false);
        starDefaultButton.setSelected(true);
        starCustomButton.setSelected(false);
        cufflinksDefaultButton.setSelected(true);
        cufflinksCustomButton.setSelected(false);
        cuffdiffDefaultButtons.setSelected(true);
        cuffdiffCustomButton.setSelected(false);
        starOptionPane.setVisible(false);
        cuffdiffAdvancedOptions.setVisible(false);
        cufflinksAdvancedOptions.setVisible(false);
        starOptionsPane.setVisible(false);
        humanSpecies.setSelected(false);
        mouseSpecies.setSelected(false);
        ratSpecies.setSelected(false);
        GRCh38.setVisible(false);
        GRCh37.setVisible(false);
        GRCm38.setVisible(false);
        NCBIM37.setVisible(false);
        RGSC3.setVisible(false);
        Rnor.setVisible(false);
        allSamples = new ArrayList<>();
        samplesListView.clear();
        listViewItems.clear();
        groups.setItems(listViewItems);
        groups1.setItems(listViewItems);
        sampleList.setItems(samplesListView);
        project = new projectData();
        username.setText("");
        connectionAddress.setText("");
        homeFolder.setText("");
        passwordBtn.setSelected(false);
        fileAuthBtn.setSelected(false);
        keyFileBrowse.setVisible(false);
        keyFile.setVisible(false);
        passwordField.setText("");
        keyFile.setText("");
        queueName.setText("bigmem");
        memUsage.setText("60000");
        processorNum.setText("8");
        maxTime.setText("24:00");
        LSFproject.setText("default");
        uploadFromComputer.setSelected(false);
        useCloudFiles.setSelected(false);
        jobScheduleBtn.setSelected(false);
        filterGenesYesBtn.setSelected(true);
        filterGenesNoBtn.setSelected(false);
        thresholdLabel.setVisible(true);
        thresholdValue.setVisible(true);
        thresholdValue.setText("20");
        trimReadsYesBtn.setSelected(true);
        trimReadsNoBtn.setSelected(false);
        basicFilterYes.setSelected(true);
        basicFilterNo.setSelected(false);
        statusLabel.setText("Status:");
    }

    @FXML
    private void cloudUploadSelected(ActionEvent event) {
        singlePairLabel.setVisible(true);
        singleEnd.setVisible(true);
        pairedEnd.setVisible(true);
        uploadFromComputer.setSelected(false);
        useCloudFiles.setSelected(true);
        singleReadLocation.clear();
        singleReadLocationBtn.setVisible(false);
        leftReadLocation.clear();
        leftReadLocationBtn.setVisible(false);
        rightReadLocation.clear();
        rightReadLocationBtn.setVisible(false);
        if (singleEnd.isSelected()) {
            singleReadLocation.setEditable(true);
            singleReadLocation.setVisible(true);
            leftReadLocation.setVisible(false);
            rightReadLocation.setVisible(false);
        } else if (pairedEnd.isSelected()) {
            leftReadLocation.setEditable(true);
            leftReadLocation.setVisible(true);
            rightReadLocation.setVisible(true);
            rightReadLocation.setEditable(true);
            singleReadLocation.setVisible(false);
        } else {
            leftReadLocation.setVisible(false);
            rightReadLocation.setVisible(false);
            singleReadLocation.setVisible(false);
        }
    }

    @FXML
    private void tophatAlignmentSelected(ActionEvent event) {
        starOptionsPane.setVisible(true);
        starOptionsLabel.setVisible(false);
        tophatOptionsLabel.setVisible(true);
        tophatAlignmentBtn.setSelected(true);
        starAlignmentBtn.setSelected(false);
        if (humanSpecies.isSelected() || mouseSpecies.isSelected() || ratSpecies.isSelected()) {
            if (GRCh37.isSelected() || GRCh38.isSelected() || Rnor.isSelected() || RGSC3.isSelected() || GRCm38.isSelected() || NCBIM37.isSelected()) {
                DGE.setDisable(false);
            }
        }
    }

    @FXML
    private void starAlignmentSelected(ActionEvent event) {
        starOptionsPane.setVisible(true);
        starOptionsLabel.setVisible(true);
        tophatOptionsLabel.setVisible(false);
        tophatAlignmentBtn.setSelected(false);
        starAlignmentBtn.setSelected(true);
        if (humanSpecies.isSelected() || mouseSpecies.isSelected() || ratSpecies.isSelected()) {
            if (GRCh37.isSelected() || GRCh38.isSelected() || Rnor.isSelected() || RGSC3.isSelected() || GRCm38.isSelected() || NCBIM37.isSelected()) {
                DGE.setDisable(false);
            }
        }
    }

    @FXML
    private void getProjectIndex(MouseEvent event) throws IOException {
        int index = existingProjectView.getSelectionModel().getSelectedIndex();
                loadRecentFailed.setVisible(false);
        File refLocation = null;
        String projectName = null;
            if (index == 0) {
                refLocation = new File (projectWrapper.p1.projectLocation);
                projectName = projectWrapper.p1.projectName;
                project = projectWrapper.p1;
            } else if (index == 1) {
                refLocation = new File (projectWrapper.p2.projectLocation);
                projectName = projectWrapper.p2.projectName;
                project = projectWrapper.p2;
            } else if (index == 2) {
                refLocation = new File (projectWrapper.p3.projectLocation);
                projectName = projectWrapper.p3.projectName;
                project = projectWrapper.p3;
            } else if (index == 3) {
                refLocation = new File (projectWrapper.p4.projectLocation);
                projectName = projectWrapper.p4.projectName;
                project = projectWrapper.p4;
            } else if (index == 4) {
                refLocation = new File (projectWrapper.p5.projectLocation);
                projectName = projectWrapper.p5.projectName;
                project = projectWrapper.p5;
            } else if (index == 5) {
                refLocation = new File (projectWrapper.p6.projectLocation);
                projectName = projectWrapper.p6.projectName;
                project = projectWrapper.p6;
            } else if (index == 6) {
                refLocation = new File (projectWrapper.p7.projectLocation);
                projectName = projectWrapper.p7.projectName;
                project = projectWrapper.p7;
            } else if (index == 7) {
                refLocation = new File (projectWrapper.p8.projectLocation);
                projectName = projectWrapper.p8.projectName;
                project = projectWrapper.p8;
            } else if (index == 8) {
                refLocation = new File (projectWrapper.p9.projectLocation);
                projectName = projectWrapper.p9.projectName;
                project = projectWrapper.p9;
            } else if (index == 9) {
                refLocation = new File (projectWrapper.p10.projectLocation);
                projectName = projectWrapper.p10.projectName;
                project = projectWrapper.p10;
            } else {
                loadRecentFailed.setVisible(true);
            }
            if (!loadRecentFailed.isVisible()) {
                projectNameLabel.setText(projectName);
                projectStatusPane.setVisible(true);
                resetSettings();
                loadProject(refLocation, projectName);
                System.out.print("Project: "+project.projectLocation);
        }        
    }

    @FXML
    private void getLogs(ActionEvent event) throws IOException, JSchException {
    String projectFilename = mainpath;
    FileReader fr = new FileReader(projectFilename);
    BufferedReader textReader = new BufferedReader(fr);
    int numOfLines = readLines(projectFilename);
    String IP="none";
    String MethToAut="none";
    String PEM="none";
    String userName="none";
    String home="none";
    for(int i = 0; i < numOfLines; i++) {
    String line = textReader.readLine();
    String[] array = line.split("\t");
    String location=array[0];
    location=location+sep+projectNameLabel.getText();
    String refProjectName=array[1];
    if(refProjectName.equals(projectNameLabel.getText())){
    String currentProjectFilename = location + sep + "output.txt";
    FileReader fr1 = new FileReader(currentProjectFilename);
    BufferedReader textReader1 = new BufferedReader(fr1);
    int numOfLines1 = readLines(currentProjectFilename);
    String CurrentStatus = "";
    for(int j = 0; j < numOfLines1; j++) {
    String line1 = textReader1.readLine();
    String[] array1 = line1.split("\t");
    if(j==1){
    userName=array1[0];
    IP=array1[1];
    home=array1[2];
    MethToAut=array1[3];
    PEM=array1[4];
    break;
    }
    }
    String connectionStatus=checkConnection.main(PEM, IP, userName, MethToAut);
    if(connectionStatus.equals("success")){
    getLogFiles.main(userName,IP,PEM,projectNameLabel.getText(), location, MethToAut, home);
    }
    }
    }
    }
    
    @FXML
    private void checkStatus(ActionEvent event) throws IOException, JSchException {
    String projectFilename = mainpath;
    FileReader fr = new FileReader(projectFilename);
    BufferedReader textReader = new BufferedReader(fr);
    int numOfLines = readLines(projectFilename);
    String IP="none";
    String MethToAut="none";
    String PEM="none";
    String userName="none";
    String home="none";
    for(int i = 0; i < numOfLines; i++) {
    String line = textReader.readLine();
    String[] array = line.split("\t");
    String location=array[0];
    location=location+sep+projectNameLabel.getText();
    String refProjectName=array[1];
    if(refProjectName.equals(projectNameLabel.getText())){
    String currentProjectFilename = location + sep + "output.txt";
    FileReader fr1 = new FileReader(currentProjectFilename);
    BufferedReader textReader1 = new BufferedReader(fr1);
    int numOfLines1 = readLines(currentProjectFilename);
    String CurrentStatus = "";
    for(int j = 0; j < numOfLines1; j++) {
    String line1 = textReader1.readLine();
    String[] array1 = line1.split("\t");
    if(j==1){
    userName=array1[0];
    IP=array1[1];
    home=array1[2];
    MethToAut=array1[3];
    PEM=array1[4];
    break;
    }
    }
    String connectionStatus=checkConnection.main(PEM, IP, userName, MethToAut);
    if(connectionStatus.equals("success")){
    CurrentStatus=statusCheck.main(userName,IP,PEM,projectNameLabel.getText(), location, MethToAut, home);
    System.out.print(CurrentStatus);
    statusLabel.setText("Status: "+CurrentStatus);
    if(CurrentStatus.equals("Done!")){
    retrieveResultsButton.setDisable(false);
    primerSelection.setDisable(false);
    }
    break;
    }
    }
    }
    }

    @FXML
    private void GRCh37Click(ActionEvent event) {
        GRCh38.setSelected(false);
        GRCh37.setSelected(true);
        if (tophatAlignmentBtn.isSelected() || starAlignmentBtn.isSelected()) {
            DGE.setDisable(false);
        }
    }

    @FXML
    private void GRCh38Click(ActionEvent event) {
        GRCh38.setSelected(true);
        GRCh37.setSelected(false);
        if (tophatAlignmentBtn.isSelected() || starAlignmentBtn.isSelected()) {
            DGE.setDisable(false);
        }
    }

    @FXML
    private void ratSpeciesClick(ActionEvent event) {
        humanSpecies.setSelected(false);
        mouseSpecies.setSelected(false);
        ratSpecies.setSelected(true);
        GRCh38.setVisible(false);
        GRCh37.setVisible(false);
        GRCm38.setVisible(false);
        NCBIM37.setVisible(false);
        RGSC3.setVisible(true);
        Rnor.setVisible(true);
        GRCh38.setSelected(false);
        GRCh37.setSelected(false);
        GRCm38.setSelected(false);
        NCBIM37.setSelected(false);
        RGSC3.setSelected(false);
        Rnor.setSelected(false);
        DGE.setDisable(true);
    }

    @FXML
    private void GRCm38Click(ActionEvent event) {
        GRCm38.setSelected(true);
        NCBIM37.setSelected(false);
        if (tophatAlignmentBtn.isSelected() || starAlignmentBtn.isSelected()) {
            DGE.setDisable(false);
        }
    }

    @FXML
    private void NCBIM37Click(ActionEvent event) {
        NCBIM37.setSelected(true);
        GRCm38.setSelected(false);
        if (tophatAlignmentBtn.isSelected() || starAlignmentBtn.isSelected()) {
            DGE.setDisable(false);
        }
    }

    @FXML
    private void RnorClick(ActionEvent event) {
        Rnor.setSelected(true);
        RGSC3.setSelected(false);
        if (tophatAlignmentBtn.isSelected() || starAlignmentBtn.isSelected()) {
            DGE.setDisable(false);
        }
    }

    @FXML
    private void RGSCClick(ActionEvent event) {
        RGSC3.setSelected(true);
        Rnor.setSelected(false);
        if (tophatAlignmentBtn.isSelected() || starAlignmentBtn.isSelected()) {
            DGE.setDisable(false);
        }
    }

    @FXML
    private void showSampleInfo(MouseEvent event) {
        int ind = sampleList.getSelectionModel().getSelectedIndex();
        System.out.print(ind);
        if (ind != -1) {
            sampleInfo selectedSample = (sampleInfo) allSamples.get(ind);
            sampleName.setText(selectedSample.sampleName);
            String selectedGroup=selectedSample.groupName;
            List groupItems = groups.getItems();
            int groupIndex = groupItems.indexOf(selectedGroup);
            System.out.print("Index: "+groupIndex);
            groups.getSelectionModel().select(groupIndex);
            groups1.getSelectionModel().select(groupIndex);
            adapterSettings library = selectedSample.library;
            if (!library.main5.equals("NONE")) {main5Seq.setText(library.main5);}
            else {main5Seq.setText("");}
            if (!library.main3.equals("NONE")) {main3Seq.setText(library.main3);}
            else {main3Seq.setText("");}
            directionBox.setValue(library.direction);
            adapterLength.setText(library.length);
            libType.setValue(library.name);
            libType.setVisible(true);
            main5Seq.setVisible(true);
            main3Seq.setVisible(true);
            singleEnd.setVisible(true);
            pairedEnd.setVisible(true);
            if (selectedSample.meanInsertSize.contains("-1")) {insertSize.clear();}
            else {insertSize.setText(selectedSample.meanInsertSize);}
            if (selectedSample.CV.contains("-1")) {cVar.clear();}
            else {cVar.setText(selectedSample.CV);}
            if (selectedSample.readLocation.contains("|")) {
                singleEnd.setSelected(false);
                pairedEnd.setSelected(true);
                if (!library.right5.equals("NONE")) {right5Seq.setText(library.right5);}
                else {right5Seq.setText("");}
                if (!library.right3.equals("NONE")) {right3Seq.setText(library.right3);}
                else {right3Seq.setText("");}
                right5Seq.setVisible(true);
                right3Seq.setVisible(true);
                rightSeqLabel.setVisible(true);
                singleReadLabel.setVisible(false);
                String[] readLocations = selectedSample.readLocation.split("\\|");
                leftReadLocation.setText(readLocations[0]);
                rightReadLocation.setText(readLocations[1]);
                leftReadLocation.setVisible(true);
                leftReadLabel.setVisible(true);
                rightReadLocation.setVisible(true);
                rightReadLabel.setVisible(true);
                pairedEnd.setSelected(true);
                singleEnd.setSelected(false);
            }
            else {
                pairedEnd.setSelected(false);
                singleEnd.setSelected(false);
                rightSeqLabel.setVisible(false);
                right3Seq.setVisible(false);
                right5Seq.setVisible(false);
                leftReadLabel.setVisible(false);
                rightReadLabel.setVisible(false);
                rightReadLocation.setVisible(false);
                singleReadLocation.setText(selectedSample.readLocation);
                singleReadLabel.setVisible(true);
                singleReadLocation.setVisible(true);
                singleEnd.setSelected(true);
                pairedEnd.setSelected(false);
            }
        }
    }

    @FXML
    private void checkForCustom(ActionEvent event) {
        String libcheck = libType.getValue();
        if (libcheck.contains("Custom")) {
            directionBox.setValue("unstranded");
            adapterLength.setText("120");
            adapterLength.setDisable(true);
            main3Seq.setText("AGATCGGAAGAGC");
            main3Seq.setDisable(true);
            main5Seq.setText("");
            main5Seq.setDisable(true);
            right3Seq.setText("AGATCGGAAGAGC");
            right3Seq.setDisable(true);
            right5Seq.setText("");
            right5Seq.setDisable(true);
        }
        adapterSettings library = new adapterSettings(libcheck);
        if(library.main3=="NONE"){
        main3Seq.setText("");    
        }
        else{
        main3Seq.setText(library.main3);
        }
        if(library.main5=="NONE"){
        main5Seq.setText("");
        }
        else{
        main5Seq.setText(library.main5);    
        }
        adapterLength.setText(library.length);
        directionBox.setValue(library.direction);
        customLibraryPane.setVisible(true);
        if (singleEnd.isSelected()) {
            rightSeqLabel.setVisible(false);
            right3Seq.setVisible(false);
            right5Seq.setVisible(false);
            mainSeqLabel.setText("Adapter Sequence:");
        }
        else if (pairedEnd.isSelected()) {
            mainSeqLabel.setText("Left Adapter Sequence:");
            rightSeqLabel.setVisible(true);
            right3Seq.setVisible(true);
            right5Seq.setVisible(true);
            if(library.right3=="NONE"){
            right3Seq.setText("");
            }
            else{
            right3Seq.setText(library.right3);    
            }
            if(library.right5=="NONE"){
            right5Seq.setText("");
            }
            else{
            right5Seq.setText(library.right5);
            }
        }
    }

    @FXML
    private void customAdapter(ActionEvent event) {
        if (adapterDefaultBtn.isSelected()) {
            directionBox.setValue("unstranded");
            adapterLength.setText("120");
            adapterLength.setDisable(true);
            main3Seq.setText("AGATCGGAAGAGC");
            main3Seq.setDisable(true);
            main5Seq.setText("");
            main5Seq.setDisable(true);
            right3Seq.setText("AGATCGGAAGAGC");
            right3Seq.setDisable(true);
            right5Seq.setText("");
            right5Seq.setDisable(true);
            insertSize.clear();
            insertSize.setVisible(false);
            insertLabel.setVisible(false);
            cVar.clear();
            cVar.setVisible(false);
            cvLabel.setVisible(false);
        }
        else if (!adapterDefaultBtn.isSelected()) {
            adapterLength.setDisable(false);
            main3Seq.setDisable(false);
            main5Seq.setDisable(false);
            right3Seq.setDisable(false);
            right5Seq.setDisable(false);
            insertSize.setVisible(true);
            insertLabel.setVisible(true);
            cVar.setVisible(true);
            cvLabel.setVisible(true);
        }
    }
    
    
    @FXML
    private void submitGeneList(ActionEvent event) throws IOException {
        String geneListName = project.projectLocation + sep + project.projectName + sep + "gene_list.txt";
        FileWriter geneList = new FileWriter(geneListName);
        String genes = primerArea.getText();
        geneList.write(genes);
        geneList.close();
        String projectFilename = mainpath;
        FileReader fr = new FileReader(projectFilename);
        BufferedReader textReader = new BufferedReader(fr);
        int numOfLines = readLines(projectFilename);
        String IP="none";
        String MethToAut="none";
        String PEM="none";
        String userName="none";
        String home="none";
        for(int i = 0; i < numOfLines; i++) {
        String line = textReader.readLine();
        String[] array = line.split("\t");
        String location=array[0];
        location=location+sep+projectNameLabel.getText();
        String refProjectName=array[1];
        if(refProjectName.equals(projectNameLabel.getText())){
        String currentProjectFilename = location + sep + "output.txt";
        FileReader fr1 = new FileReader(currentProjectFilename);
        BufferedReader textReader1 = new BufferedReader(fr1);
        int numOfLines1 = readLines(currentProjectFilename);
        for(int j = 0; j < numOfLines1; j++) {
        String line1 = textReader1.readLine();
        String[] array1 = line1.split("\t");
        if(j==1){
        userName=array1[0];
        IP=array1[1];
        home=array1[2];
        MethToAut=array1[3];
        PEM=array1[4];
        break;
        }
        }
        String connectionStatus=checkConnection.main(PEM, IP, userName, MethToAut);
        if(connectionStatus.equals("success")){
        primer.main(location,PEM,IP,projectNameLabel.getText(),userName,MethToAut,home);
        }
        }
        }
    }
    
    void writeProjectFile() throws IOException {
        File outputProjectFile = new File(mainpath);
        FileWriter file = new FileWriter(outputProjectFile);
        BufferedWriter output = new BufferedWriter(file);
        output.flush();
        if (!isProjectEmpty(projectWrapper.p1)) {output.write(projectWrapper.p1.projectLocation + "\t" + projectWrapper.p1.projectName); output.newLine();}
        if (!isProjectEmpty(projectWrapper.p2)) {output.write(projectWrapper.p2.projectLocation + "\t" + projectWrapper.p2.projectName); output.newLine();}
        if (!isProjectEmpty(projectWrapper.p3)) {output.write(projectWrapper.p3.projectLocation + "\t" + projectWrapper.p3.projectName); output.newLine();}
        if (!isProjectEmpty(projectWrapper.p4)) {output.write(projectWrapper.p4.projectLocation + "\t" + projectWrapper.p4.projectName); output.newLine();}
        if (!isProjectEmpty(projectWrapper.p5)) {output.write(projectWrapper.p5.projectLocation + "\t" + projectWrapper.p5.projectName); output.newLine();}
        if (!isProjectEmpty(projectWrapper.p6)) {output.write(projectWrapper.p6.projectLocation + "\t" + projectWrapper.p6.projectName); output.newLine();}
        if (!isProjectEmpty(projectWrapper.p7)) {output.write(projectWrapper.p7.projectLocation + "\t" + projectWrapper.p7.projectName); output.newLine();}
        if (!isProjectEmpty(projectWrapper.p8)) {output.write(projectWrapper.p8.projectLocation + "\t" + projectWrapper.p8.projectName); output.newLine();}
        if (!isProjectEmpty(projectWrapper.p9)) {output.write(projectWrapper.p9.projectLocation + "\t" + projectWrapper.p9.projectName); output.newLine();}
        if (!isProjectEmpty(projectWrapper.p10)) {output.write(projectWrapper.p10.projectLocation + "\t" + projectWrapper.p10.projectName); output.newLine();}
        output.close();
    }

    void cuffdiffDefaultOptions() {
        pairwiseCompBtn.setSelected(true);
        cuffdiffMaskYes.setSelected(true);
        cuffdiffBiasYes.setSelected(true);
        cuffdiffMultiYes.setSelected(true);
        cuffdiffGeometric.setSelected(true);
        cuffdiffTotalNo.setSelected(true);
        cuffdiffCompYes.setSelected(true);
        cuffdiffPooled.setSelected(true);
        cuffdiffMinimum.setText("10");
        cuffdiffFalse.setText("0.05");
        cuffdiffOtherOptions.setText("");
        cuffdiffCustomButton.setSelected(false);
        cuffdiffDefaultButtons.setSelected(true);
        cuffdiffAdvancedOptions.setVisible(false);
    }
    
    void cuffdiffCustomOptions() {
        cuffdiffCustomButton.setSelected(true);
        cuffdiffDefaultButtons.setSelected(false);
        cuffdiffAdvancedOptions.setVisible(true);
    }
    
    @FXML
    private void cuffdiffDefaults(ActionEvent event) {
        cuffdiffDefaultOptions();
    }

    @FXML
    private void cuffdiffCustom(ActionEvent event) {
        cuffdiffCustomOptions();
    }
    
    @FXML
    private void starDefaults(ActionEvent event) {
        starDefaultButton.setSelected(true);
        starCustomButton.setSelected(false);
        starOptions.setText("");
        starOptionPane.setVisible(false);
    }

    @FXML
    private void starCustom(ActionEvent event) {
        starCustomButton.setSelected(true);
        starDefaultButton.setSelected(false);
        starOptionPane.setVisible(true);
    }

    void cufflinksCustomOptions() {
        cufflinksCustomButton.setSelected(true);
        cufflinksAdvancedOptions.setVisible(true);
        cufflinksDefaultButton.setSelected(false);
    }
    
    @FXML
    private void cufflinksCustom(ActionEvent event) {
        cufflinksCustomOptions();
    }

    void cufflinksDefaultOptions () {
        cufflinksMaskYes.setSelected(true);
        cufflinksMaskNo.setSelected(false);
        cufflinksBiasYes.setSelected(true);
        cufflinksBiasNo.setSelected(false);
        cufflinksMultiYes.setSelected(true);
        cufflinksMultiNo.setSelected(false);
        cufflinksUpperYes.setSelected(false);
        cufflinksUpperNo.setSelected(true);
        cufflinksTotalYes.setSelected(true);
        cufflinksTotalNo.setSelected(false);
        cufflinksLengthYes.setSelected(false);
        cufflinksLengthNo.setSelected(true);
        cufflinksEffYes.setSelected(false);
        cufflinksEffNo.setSelected(true);
        cufflinksFraction.setText("1.0");
        cufflinksOtherOptions.setText("");
        cufflinksCustomButton.setSelected(false);
        cufflinksDefaultButton.setSelected(true);
        cufflinksAdvancedOptions.setVisible(false);
    }
    
    
        @Override
    public void initialize(URL url, ResourceBundle rb) {
        projectStatusPane.setVisible(false);
        resetSettings();
        BooleanBinding bb = new BooleanBinding() {
        {
            super.bind(username.textProperty(),
                connectionAddress.textProperty(),
                keyFile.textProperty(), homeFolder.textProperty(), passwordField.textProperty());
        }

        @Override
        protected boolean computeValue() {
                if (passwordBtn.isSelected()) {
                    return (username.getText().isEmpty()
                            || connectionAddress.getText().isEmpty()
                            || passwordField.getText().isEmpty() || homeFolder.getText().isEmpty());
                }
            else{
                return (username.getText().isEmpty()
                            || connectionAddress.getText().isEmpty()
                            || keyFile.getText().isEmpty() || homeFolder.getText().isEmpty());
            }
        }
        };
        finalSubmitBtn.disableProperty().bind(bb);
        
        if (projectFile.exists()) {
            try {
                populateRecentProjectList();
            } catch (IOException ex) {
                java.util.logging.Logger.getLogger(PipelineUIController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    void loadProject(File refLocation, String projectName) throws FileNotFoundException, IOException {
            String path = refLocation + sep + projectName + sep + "output.txt";
            File checkSubmit = new File(path);
            if (checkSubmit.exists()) {
                groupsTab.setDisable(false);
                addSamples.setDisable(false);
                parameters.setDisable(false);
                DGE.setDisable(false);
                submitTab.setDisable(false);
                checkStatusBtn.setDisable(false);
                getLogsButton.setDisable(false);
                finalSubmitBtn.setVisible(false);
            }
            else {
                
                project = new projectData(projectName, refLocation.getPath());
                groupsTab.setDisable(false);
                addSamples.setDisable(true);
                parameters.setDisable(true);
                DGE.setDisable(true);
                submitTab.setDisable(true);
                checkStatusBtn.setDisable(true);
                getLogsButton.setDisable(true);
                finalSubmitBtn.setVisible(true);
            }
            if (checkSubmit.exists()) {
            FileReader fr = new FileReader(path);
            BufferedReader textReader = new BufferedReader(fr);
            int numOfLines = readLines(path);
            String[] fileData = new String[numOfLines];
            for(int i = 0; i < numOfLines; i++) {
                fileData[i] = textReader.readLine();
            }
            textReader.close();
            String location = refLocation.getPath();
            String name = fileData[0];
            if (fileData[1].equals("local:")) {
                remotePane.setVisible(false);
            } else {
                remotePane.setVisible(true);
                String[] cloudSettings = fileData[1].split("\t");
                String user = cloudSettings[0];
                String address = cloudSettings[1];
                String folder = cloudSettings[2];
                String AuthMeth = cloudSettings[3];
                String keyfile = cloudSettings[4];
                if (AuthMeth.equals("PASS")) {
                    passwordBtn.setSelected(true);
                    fileAuthBtn.setSelected(false);
                    keyFileBrowse.setVisible(false);
                    keyFile.setVisible(false);
                    passwordField.setText(keyfile);
                    authLabel.setText("Password:");
                }
                else if (AuthMeth.equals("KEY")) {
                    passwordBtn.setSelected(false);
                    fileAuthBtn.setSelected(true);
                    passwordField.setVisible(false);
                    keyFileBrowse.setVisible(true);
                    keyFile.setVisible(true);
                    authLabel.setText("Key File:");
                }
                authenticationPane.setVisible(true);
                if (cloudSettings.length == 6) {
                    project.jobSched = new jobSchedule(cloudSettings[5]);
                    jobSchedPane.setVisible(true);
                    jobScheduleBtn.setSelected(true);
                    queueName.setText(project.jobSched.queue);
                    memUsage.setText(project.jobSched.maxMem);
                    processorNum.setText(project.jobSched.processorNumber);
                    maxTime.setText(project.jobSched.wait);
                    LSFproject.setText(project.jobSched.LSFprojectName);
                }
                project.serverUser = user;
                project.EC2Address = address;
                project.keyFileLocation = keyfile;
                project.home = folder;
                username.setText(user);
                connectionAddress.setText(address);
                homeFolder.setText(folder);
            }
            String groupString = fileData[2];
            String[] groupList = groupString.split(" ");
            int groupNumber = groupList.length;
            listViewItems.clear();
            System.out.print(groupString);
            for(int j = 0; j < groupNumber; j++) {
                String nameOfGroup = groupList[j];
                listViewItems.add(nameOfGroup);
                groups.setItems(listViewItems);
                groups1.setItems(listViewItems);
            }
            String[] options = fileData[3].split("\\|");
            String[] basicOptions = options[0].split(":");
            switch (basicOptions[0]) {
                case "yes":
                    trimReadsYesBtn.setSelected(true);
                    trimReadsNoBtn.setSelected(false);
                    break;
                case "no":
                    trimReadsNoBtn.setSelected(true);
                    trimReadsYesBtn.setSelected(false);
                    break;
            }
            if (basicOptions[1].contains(",")) {
                String[] transFilter = basicOptions[1].split(",");
                filterGenesYesBtn.setSelected(true);
                filterGenesNoBtn.setSelected(false);
                totalFilter.setText(transFilter[0]);
                totalFilter.setVisible(true);
                totalFilterLabel.setVisible(true);
                groupFilter.setText(transFilter[1]);
                groupFilter.setVisible(true);
                groupFilterLabel.setVisible(true);
            } else if (!basicOptions[1].contains(",")) {
                filterGenesNoBtn.setSelected(true);
                filterGenesYesBtn.setSelected(false);
                totalFilter.clear();
                groupFilter.clear();
                totalFilterLabel.setVisible(false);
                groupFilterLabel.setVisible(false);
                totalFilter.setVisible(false);
                groupFilter.setVisible(false);
            }
            if (basicOptions[2].equals("no")) {
                basicFilterNo.setSelected(true);
                basicFilterYes.setSelected(false);
                thresholdValue.clear();
                thresholdLabel.setVisible(false);
                thresholdValue.setVisible(false);
            } else {
                basicFilterYes.setSelected(true);
                basicFilterNo.setSelected(false);
                thresholdValue.setText(basicOptions[2]);
                thresholdLabel.setVisible(true);
                thresholdValue.setVisible(true);
            }
            switch (basicOptions[3]) {
                case "human":
                    humanSpecies.setSelected(true);
                    mouseSpecies.setSelected(false);
                    ratSpecies.setSelected(false);
                    GRCh38.setVisible(true);
                    GRCh37.setVisible(true);
                    switch (basicOptions[4]) {
                        case "GRCh37":
                            GRCh37.setSelected(true);
                            GRCh38.setSelected(false);
                            break;
                        case "GRCh38":
                            GRCh38.setSelected(true);
                            GRCh37.setSelected(false);
                            break;
                    }                        
                    break;
                case "mouse":
                    mouseSpecies.setSelected(true);
                    humanSpecies.setSelected(false);
                    ratSpecies.setSelected(false);
                    GRCm38.setVisible(true);
                    NCBIM37.setVisible(true);
                    switch(basicOptions[4]) {
                        case "GRCm38":
                            GRCm38.setSelected(true);
                            NCBIM37.setSelected(false);
                            break;
                        case "NCBIM37":
                            NCBIM37.setSelected(true);
                            GRCm38.setSelected(false);
                            break;
                    }
                    break;
                case "rat":
                    ratSpecies.setSelected(true);
                    humanSpecies.setSelected(false);
                    mouseSpecies.setSelected(false);
                    Rnor.setVisible(true);
                    RGSC3.setVisible(true);
                    switch (basicOptions[4]) {
                        case "Rnor_5.0":
                            Rnor.setSelected(true);
                            RGSC3.setSelected(false);
                            break;
                        case "RGSC3.4":
                            RGSC3.setSelected(true);
                            Rnor.setSelected(false);
                            break;
                    }
                    break;
            }
            switch (basicOptions[5]) {
                case "yes":
                    splicingYes.setSelected(true);
                    splicingNo.setSelected(false);
                    break;
                case "no":
                    splicingNo.setSelected(true);
                    splicingYes.setSelected(false);
                    break;
            }
            switch (basicOptions[8]) {
                case "STAR":
                    starAlignmentBtn.setSelected(true);
                    tophatAlignmentBtn.setSelected(false);
                    starOptionsPane.setVisible(true);
                    starOptionsLabel.setVisible(true);
                    tophatOptionsLabel.setVisible(false);
                    String starSavedOptions = options[1];
                    if (starSavedOptions.length() > 14) {
                        starSavedOptions = starSavedOptions.substring(14);
                        starDefaultButton.setSelected(false);
                        starCustomButton.setSelected(true);
                        starOptionPane.setVisible(true);
                        starOptions.setText(starSavedOptions);
                    }
                    else {
                        starCustomButton.setSelected(false);
                        starOptions.setText("");
                        starOptionPane.setVisible(false);
                    }
                    break;
                case "tophat":
                    tophatAlignmentBtn.setSelected(true);
                    starAlignmentBtn.setSelected(false);
                    starOptionsPane.setVisible(true);
                    starOptionsLabel.setVisible(false);
                    tophatOptionsLabel.setVisible(true);
                    String tophatSavedOptions = options[2];
                    if (tophatSavedOptions.length() > 16) {
                        tophatSavedOptions = tophatSavedOptions.substring(16);
                        starDefaultButton.setSelected(false);
                        starCustomButton.setSelected(true);
                        starOptionPane.setVisible(true);
                        starOptions.setVisible(true);
                        starOptions.setText(tophatSavedOptions);
                    }
                    else {
                        starCustomButton.setSelected(false);
                        starOptions.setText("");
                        starOptionPane.setVisible(false);
                    }
                    break;
            }
            String cufflinksSavedOptions = options[3];
            if (!cufflinksSavedOptions.contains("-M:-u:-j 1.0") || cufflinksSavedOptions.length() != 12) {
                cufflinksCustomOptions();
                cufflinksDefaultButton.setSelected(false);
                cufflinksCustomButton.setSelected(true);
                if (!cufflinksSavedOptions.contains("-M:")) {cufflinksMaskYes.setSelected(false); cufflinksMaskNo.setSelected(true);}
                if (cufflinksSavedOptions.contains("-b:")) {cufflinksBiasYes.setSelected(true); cufflinksBiasNo.setSelected(false);}
                if (!cufflinksSavedOptions.contains("-u:")) {cufflinksMultiYes.setSelected(false); cufflinksMultiNo.setSelected(true);}
                if (cufflinksSavedOptions.contains("--upper-quartile-norm:")) {cufflinksUpperYes.setSelected(true); cufflinksUpperNo.setSelected(false);}
                if (cufflinksSavedOptions.contains("--total-hits-norm:")) {cufflinksTotalYes.setSelected(true); cufflinksTotalNo.setSelected(false);}
                if (cufflinksSavedOptions.contains("--no-effective-length-correction:")) {cufflinksEffYes.setSelected(false); cufflinksEffNo.setSelected(true);}
                if (cufflinksSavedOptions.contains("--no-length-correction:")) {cufflinksLengthYes.setSelected(false); cufflinksLengthNo.setSelected(true);}
                String[] linksOptions = cufflinksSavedOptions.split("-j ");
                linksOptions = linksOptions[1].split(":");
                cufflinksFraction.setText(linksOptions[0]);
                int linksNum = linksOptions.length;
                if (linksNum == 2) {cufflinksOtherOptions.setText(linksOptions[1]);}
            }
            else {cufflinksDefaultButton.setSelected(true); cufflinksCustomButton.setSelected(false);}
            String cuffdiffSavedOptions = options[4];
            if (!cuffdiffSavedOptions.equals("SKIP")) {
                DECuffdiff.setSelected(true);
                cuffdiffOptionsLabel.setVisible(true);
                cuffdiffDefaultButtons.setSelected(false);
                cuffdiffDefaultButtons.setVisible(true);
                cuffdiffCustomButton.setVisible(true);
                cuffdiffCustomButton.setSelected(true);
                if (!cuffdiffSavedOptions.contains("-M:-u") || cuffdiffSavedOptions.length() != 5) {
                    cuffdiffCustomOptions();
                    if (!cuffdiffSavedOptions.contains("-M:")) {cuffdiffMaskYes.setSelected(false); cuffdiffMaskNo.setSelected(true);}
                    if (cuffdiffSavedOptions.contains("-b:")) {cuffdiffBiasYes.setSelected(true); cuffdiffBiasNo.setSelected(false);}
                    if (!cuffdiffSavedOptions.contains("-u:")) {cuffdiffMultiYes.setSelected(false); cuffdiffMultiNo.setSelected(true);}
                    if (cuffdiffSavedOptions.contains("--total-hits-norm")) {cuffdiffTotalYes.setSelected(true); cuffdiffTotalNo.setSelected(false);}
                    if (cuffdiffSavedOptions.contains(" classic-fpkm:")) {cuffdiffGeometric.setSelected(false); cuffdiffClassic.setSelected(true);}
                    if (cuffdiffSavedOptions.contains(" quartile:")) {cuffdiffGeometric.setSelected(false); cuffdiffQuartile.setSelected(true);}
                    if (cuffdiffSavedOptions.contains("--compatible-hits-norm:")) {cuffdiffCompYes.setSelected(true); cuffdiffCompNo.setSelected(false);}
                    if (cuffdiffSavedOptions.contains(" per-condition:")) {cuffdiffPooled.setSelected(false); cuffdiffPer.setSelected(true);}
                    if (cuffdiffSavedOptions.contains(" blind:")) {cuffdiffPooled.setSelected(false); cuffdiffBlind.setSelected(true);}
                    if (cuffdiffSavedOptions.contains(" poisson:")) {cuffdiffPooled.setSelected(false); cuffdiffPoisson.setSelected(true);}
                    String[] diffOptions = cuffdiffSavedOptions.split("-c ");
                    diffOptions = diffOptions[1].split(":");
                    cuffdiffMinimum.setText(diffOptions[0]);
                    int diffNum = diffOptions.length;
                    if (diffNum == 3) {cuffdiffOtherOptions.setText(diffOptions[2]);}
                    String diffFalse = diffOptions[1].substring(6);
                    cuffdiffFalse.setText(diffFalse);
                }
            }
            else {cuffdiffDefaultButtons.setSelected(true); cuffdiffCustomButton.setSelected(false);}
            String edgeROptions = options[5];
            if (!edgeROptions.equals("SKIP")) {
                DEedgeR.setSelected(true);
                edgeRPane.setVisible(true);
                String[] edgeOptions = edgeROptions.split(":");
                switch (edgeOptions[0]) {
                    case "yes":
                        exactTest.setSelected(true);
                        break;
                    case "no":
                        exactTest.setSelected(false);
                }
                switch (edgeOptions[1]) {
                    case "yes":
                        GML.setSelected(true);
                        break;
                    case "no":
                        GML.setSelected(false);
                        break;
                }
                edgeRFDR.setText(edgeOptions[2]);
                switch (edgeOptions[3]) {
                    case "bonferroni":
                        edgeRFDRMethod.getSelectionModel().select("bonferroni");
                        break;
                    case "fdr":
                        edgeRFDRMethod.getSelectionModel().select("fdr");
                }
                String[] edgecomp = edgeOptions[4].split("-");
                combinations.addAll(Arrays.asList(edgecomp));
                comparisonsBox.setItems(combinations);
            }
            String DESeqOptions = options[6];
            if (!DESeqOptions.equals("SKIP")) {
                DEDESeq2.setSelected(true);
                DESeq2Pane.setVisible(true);
                String[] DESeq2Options = DESeqOptions.split(":");
                DESeq2FDR.setText(DESeq2Options[0]);
                switch (DESeq2Options[1]) {
                    case "bonferroni":
                        DESeq2FDRMethod.getSelectionModel().select("bonferroni");
                        break;
                    case "fdr":
                        DESeq2FDRMethod.getSelectionModel().select("fdr");
                }
                String[] DESeq2comp = DESeq2Options[2].split("-");
                combinationsDESeq2.addAll(Arrays.asList(DESeq2comp));
                comparisonsBoxDESeq2.setItems(combinationsDESeq2);
            }
            allSamples.clear();
            samplesListView.clear();
            for (int x = 4; x < fileData.length; x++) {
                String[] sampleInfo = fileData[x].split("\t");
                String sampleGroup = sampleInfo[0];
                String sampleName = sampleInfo[1];
                String sampleLocation = sampleInfo[2];
                adapterSettings samplePrep = new adapterSettings();
                samplePrep.createFromOutput(sampleInfo[3]);
                String sampleInsert = sampleInfo[4];
                String sampleCV = sampleInfo[5];
                sampleInfo loadSample = new sampleInfo(sampleGroup, sampleName, sampleLocation, samplePrep, sampleInsert, sampleCV);
                allSamples.add(loadSample);
                samplesListView.add(sampleName);
            }
            sampleList.setItems(samplesListView);
            project = new projectData(name, location); 
            groupsTab.setDisable(false);
            addSamples.setDisable(false);
            parameters.setDisable(false);
            submitTab.setDisable(false);
            DGE.setDisable(false);
    }
    if(!checkStatusBtn.isDisabled()){
            checkStatusBtn.fire();    
            }
    selectableGroups1.setAll(groups.getItems());
    selectableGroups2.setAll(groups.getItems());
    selectableGroups1DESeq2.setAll(groups.getItems());
    selectableGroups2DESeq2.setAll(groups.getItems());
    comparisonGroups1.getItems().setAll(selectableGroups1);
    comparisonGroups2.getItems().setAll(selectableGroups2);
    }
    
    @FXML
    private void cufflinksDefaults(ActionEvent event) {
        cufflinksDefaultOptions();
    }
    
    @FXML
    private void createNewProject(ActionEvent event) throws IOException {
        PipelineUI current = new PipelineUI();
        projectData newproject = new projectData();
        newproject = current.showAddProjectDialog(newproject);
        resetSettings();
        current_index=0;
        if (newproject.projectName != null && newproject.projectLocation != null) {
            project = newproject;
            groupsTab.setDisable(false);
            checkStatusBtn.setDisable(true);
            getLogsButton.setDisable(true);
            addSamples.setDisable(true);
            parameters.setDisable(true);
            submitTab.setDisable(true);
            //DGE.setDisable(true);
            finalSubmitBtn.setVisible(true);
            if (!isProjectEmpty(projectWrapper.p9)) {projectWrapper.p10 = projectWrapper.p9;}
            if (!isProjectEmpty(projectWrapper.p8)) {projectWrapper.p9 = projectWrapper.p8;}
            if (!isProjectEmpty(projectWrapper.p7)) {projectWrapper.p8 = projectWrapper.p7;}
            if (!isProjectEmpty(projectWrapper.p6)) {projectWrapper.p7 = projectWrapper.p6;}
            if (!isProjectEmpty(projectWrapper.p5)) {projectWrapper.p6 = projectWrapper.p5;}
            if (!isProjectEmpty(projectWrapper.p4)) {projectWrapper.p5 = projectWrapper.p4;}
            if (!isProjectEmpty(projectWrapper.p3)) {projectWrapper.p4 = projectWrapper.p3;}
            if (!isProjectEmpty(projectWrapper.p2)) {projectWrapper.p3 = projectWrapper.p2;}
            if (!isProjectEmpty(projectWrapper.p1)) {projectWrapper.p2 = projectWrapper.p1;}
            projectWrapper.p1 = project;
            projectNames.add(0, project.projectName);
            existingProjectView.setItems(projectNames);
            existingProjectView.getSelectionModel().select(0);
            projectNameLabel.setText(project.projectName);
            projectStatusPane.setVisible(true);
            writeProjectFile();
        }
    }
    
    int readLines (String path) throws IOException {
        FileReader readFile = new FileReader(path);
        BufferedReader bf = new BufferedReader(readFile);
        String aLine;
        int numLines = 0;
        aLine = bf.readLine();
        while (aLine != null) {
            numLines++;
            aLine = bf.readLine();
        }
        bf.close();
        return numLines;
    }

    @FXML
    private void addGroup(ActionEvent event) {
        if (groupName.getText() != "") {
            String name = groupName.getText();
            listViewItems.add(name);
            groups.setItems(listViewItems);
            groups1.setItems(listViewItems);
            groupName.clear();
        }
        addSamples.setDisable(false);
    }

    @FXML
    private void removeGroup(ActionEvent event) {
        final int ind = groups.getSelectionModel().getSelectedIndex();
        if (ind != -1) {
            listViewItems.remove(ind);
            groups.setItems(listViewItems);
            groups1.setItems(listViewItems);
        }
        if (listViewItems.size() == 0)
        {
            addSamples.setDisable(true);
        }
    }

    @FXML
    private void chooseFile(ActionEvent event) {
        FileChooser libraryFile = new FileChooser();
        libraryFile.setTitle("Select Resource File:");
        File refFile = libraryFile.showOpenDialog(null);
        String path;
        if (refFile != null) {
            singleReadLocation.clear();
            path = refFile.getPath();
            singleReadLocation.appendText(path);
        }
    }

    @FXML
    private void singleEndSelected(ActionEvent event) {
        libTypeLabel.setVisible(true);
        libType.setVisible(true);
        libType.setDisable(false);
        if (pairedEnd.isSelected()) {
            pairedEnd.setSelected(false);
        }
        libTypes.setAll("","Custom single", "Illumina mRNA single", "Illumina small RNA single");
        libType.getItems().setAll(libTypes);
        singleReadLocation.setVisible(true);
        rightReadLocation.setVisible(false);
        rightReadLocation.clear();
        leftReadLocation.setVisible(false);
        leftReadLocation.clear();
        if (useCloudFiles.isSelected()) {
            singleReadLocationBtn.setVisible(false);
            singleReadLocation.setEditable(true);
        } else {
            singleReadLocation.setEditable(false);
            singleReadLocationBtn.setVisible(true);
        }
        rightReadLocationBtn.setVisible(false);
        leftReadLocationBtn.setVisible(false);
        singleReadLabel.setVisible(true);
        leftReadLabel.setVisible(false);
        rightReadLabel.setVisible(false);
        int libIndex = libType.getSelectionModel().getSelectedIndex();
        String library;
        if (libIndex!= -1) {library = libTypes.get(libIndex);}
        else {library = "Custom single";}
        if (library.equals("Custom single")) {
            mainSeqLabel.setText("Adapter Sequence:");
            right3Seq.setVisible(false);
            right5Seq.setVisible(false);
            rightSeqLabel.setVisible(false);
        }
    }

    @FXML
    private void pairedEndSelected(ActionEvent event) {
        libTypeLabel.setVisible(true);
        libType.setVisible(true);
        libType.setDisable(false);
        if (singleEnd.isSelected()) {
            singleEnd.setSelected(false);
        }
        libTypes.setAll("","Custom paired", "Epicenter paired", "NEB small RNA paired", "NEB Ultra paired");
        libType.getItems().setAll(libTypes);
        singleReadLocation.setVisible(false);
        singleReadLocation.clear();
        rightReadLocation.setVisible(true);
        leftReadLocation.setVisible(true);
        singleReadLocationBtn.setVisible(false);
        if (useCloudFiles.isSelected()) {
            rightReadLocation.setEditable(true);
            leftReadLocation.setEditable(true);
            rightReadLocationBtn.setVisible(false);
            leftReadLocationBtn.setVisible(false);
        } else {
            rightReadLocation.setEditable(false);
            leftReadLocation.setEditable(false);
            rightReadLocationBtn.setVisible(true);
            leftReadLocationBtn.setVisible(true);
        }
        singleReadLabel.setVisible(false);
        leftReadLabel.setVisible(true);
        rightReadLabel.setVisible(true);
        int libIndex = libType.getSelectionModel().getSelectedIndex();
        String library;
        if (libIndex != -1) {library = libTypes.get(libIndex);}
        else {library = "Custom paired";}
        mainSeqLabel.setText("Left Adapter Sequence:");
        right3Seq.setVisible(true);
        right5Seq.setVisible(true);
        rightSeqLabel.setVisible(true);
    }

    @FXML
    private void chooseLeftRead(ActionEvent event) {
        FileChooser leftFile = new FileChooser();
        leftFile.setTitle("Select Resource File:");
        File refFile = leftFile.showOpenDialog(null);
        String path;
        if (refFile != null) {
            leftReadLocation.clear();
            path = refFile.getPath();
            leftReadLocation.appendText(path);
        }
    }

    @FXML
    private void chooseRightRead(ActionEvent event) {
        FileChooser rightFile = new FileChooser();
        rightFile.setTitle("Select Resource File:");
        File refFile = rightFile.showOpenDialog(null);
        String path;
        if (refFile != null) {
            rightReadLocation.clear();
            path = refFile.getPath();
            rightReadLocation.appendText(path);
        }
    }

    @FXML
    private void trimReadsYesClicked(ActionEvent event) {
        if (trimReadsNoBtn.isSelected()) {
            trimReadsNoBtn.setSelected(false);
        }
    }

    @FXML
    private void trimReadsNoClicked(ActionEvent event) {
        if (trimReadsYesBtn.isSelected()) {
            trimReadsYesBtn.setSelected(false);
        }
    }

    @FXML
    private void pairwiseClicked(ActionEvent event) {
        if (timecourseCompBtn.isSelected()) {
            timecourseCompBtn.setSelected(false);
        }
    }

    @FXML
    private void timecourseClicked(ActionEvent event) {
        if (pairwiseCompBtn.isSelected()) {
            pairwiseCompBtn.setSelected(false);
        }
    }

    @FXML
    private void filterGenesYes(ActionEvent event) {
        if (filterGenesNoBtn.isSelected()) {
            filterGenesNoBtn.setSelected(false);
        }
        thresholdLabel.setVisible(true);
        thresholdValue.setVisible(true);
    }

    @FXML
    private void filterGenesNo(ActionEvent event) {
        if (filterGenesYesBtn.isSelected()) {
            filterGenesYesBtn.setSelected(false);
        }
        thresholdLabel.setVisible(false);
        thresholdValue.setVisible(false);
    }
    
    @FXML
    private void basicFilterYesClick(ActionEvent event) {
        basicFilterNo.setSelected(false);
        totalFilter.setVisible(true);
        groupFilter.setVisible(true);
        totalFilterLabel.setVisible(true);
        groupFilterLabel.setVisible(true);
    }

    @FXML
    private void basicFilterNoClick(ActionEvent event) {
        basicFilterYes.setSelected(false);
        totalFilter.setVisible(false);
        groupFilter.setVisible(false);
        totalFilterLabel.setVisible(false);
        groupFilterLabel.setVisible(false);
    }

    @FXML
    private void humanSpeciesClick(ActionEvent event) {
        humanSpecies.setSelected(true);
        mouseSpecies.setSelected(false);
        ratSpecies.setSelected(false);
        GRCh38.setVisible(true);
        GRCh37.setVisible(true);
        GRCm38.setVisible(false);
        NCBIM37.setVisible(false);
        RGSC3.setVisible(false);
        Rnor.setVisible(false);
        GRCh38.setSelected(false);
        GRCh37.setSelected(false);
        GRCm38.setSelected(false);
        NCBIM37.setSelected(false);
        RGSC3.setSelected(false);
        Rnor.setSelected(false);
        DGE.setDisable(true);
    }

    @FXML
    private void mouseSpeciesClick(ActionEvent event) {
        mouseSpecies.setSelected(true);
        humanSpecies.setSelected(false);
        ratSpecies.setSelected(false);
        GRCh38.setVisible(false);
        GRCh37.setVisible(false);
        GRCm38.setVisible(true);
        NCBIM37.setVisible(true);
        RGSC3.setVisible(false);
        Rnor.setVisible(false);
        GRCh38.setSelected(false);
        GRCh37.setSelected(false);
        GRCm38.setSelected(false);
        NCBIM37.setSelected(false);
        RGSC3.setSelected(false);
        Rnor.setSelected(false);
        DGE.setDisable(true);
    }

    @FXML
    private void splicingYesClick(ActionEvent event) {
        splicingYes.setSelected(true);
        splicingNo.setSelected(false);
    }

    @FXML
    private void splicingNoClick(ActionEvent event) {
        splicingNo.setSelected(true);
        splicingYes.setSelected(false);
    }

    @FXML
    private void cufflinksMaskYesClick(ActionEvent event) {
        cufflinksMaskYes.setSelected(true);
        cufflinksMaskNo.setSelected(false);
    }

    @FXML
    private void cufflinksMaskNoClick(ActionEvent event) {
        cufflinksMaskNo.setSelected(true);
        cufflinksMaskYes.setSelected(false);
    }

    @FXML
    private void cufflinksBiasYesClick(ActionEvent event) {
        cufflinksBiasYes.setSelected(true);
        cufflinksBiasNo.setSelected(false);
    }

    @FXML
    private void cufflinksBiasNoClick(ActionEvent event) {
        cufflinksBiasNo.setSelected(true);
        cufflinksBiasYes.setSelected(false);
    }

    @FXML
    private void cufflinksMultiYesClick(ActionEvent event) {
        cufflinksMultiYes.setSelected(true);
        cufflinksMultiNo.setSelected(false);
    }

    @FXML
    private void cufflinksMultiNoClick(ActionEvent event) {
        cufflinksMultiNo.setSelected(true);
        cufflinksMultiYes.setSelected(false);
    }

    @FXML
    private void cufflinksUpperYesClick(ActionEvent event) {
        cufflinksUpperYes.setSelected(true);
        cufflinksUpperNo.setSelected(false);
    }

    @FXML
    private void cufflinksUpperNoClick(ActionEvent event) {
        cufflinksUpperNo.setSelected(true);
        cufflinksUpperYes.setSelected(false);
    }

    @FXML
    private void cufflinksTotalYesClick(ActionEvent event) {
        cufflinksTotalYes.setSelected(true);
        cufflinksTotalNo.setSelected(false);
    }

    @FXML
    private void cufflinksTotalNoClick(ActionEvent event) {
        cufflinksTotalNo.setSelected(true);
        cufflinksTotalYes.setSelected(false);
    }


    @FXML
    private void cufflinksLengthYesClick(ActionEvent event) {
        cufflinksLengthYes.setSelected(true);
        cufflinksLengthNo.setSelected(false);
    }

    @FXML
    private void cufflinksLengthNoClick(ActionEvent event) {
        cufflinksLengthNo.setSelected(true);
        cufflinksLengthYes.setSelected(false);
    }

    @FXML
    private void cufflinksEffYesCick(ActionEvent event) {
        cufflinksEffYes.setSelected(true);
        cufflinksEffNo.setSelected(false);
    }

    @FXML
    private void cufflinksEffNoClick(ActionEvent event) {
        cufflinksEffNo.setSelected(true);
        cufflinksEffYes.setSelected(false);
    }

    @FXML
    private void cuffdiffMaskYesClick(ActionEvent event) {
        cuffdiffMaskYes.setSelected(true);
        cuffdiffMaskNo.setSelected(false);
    }

    @FXML
    private void cuffdiffMaskNoClick(ActionEvent event) {
        cuffdiffMaskNo.setSelected(true);
        cuffdiffMaskYes.setSelected(false);
    }

    @FXML
    private void cuffdiffBiasYesClick(ActionEvent event) {
        cuffdiffBiasYes.setSelected(true);
        cuffdiffBiasNo.setSelected(false);
    }

    @FXML
    private void cuffdiffBiasNoClick(ActionEvent event) {
        cuffdiffBiasNo.setSelected(true);
        cuffdiffBiasYes.setSelected(false);
    }

    @FXML
    private void cuffdiffMultiYesClick(ActionEvent event) {
        cuffdiffMultiYes.setSelected(true);
        cuffdiffMultiNo.setSelected(false);
    }

    @FXML
    private void cuffdiffMultiNoClick(ActionEvent event) {
        cuffdiffMultiNo.setSelected(true);
        cuffdiffMultiYes.setSelected(false);
    }

    @FXML
    private void cuffdiffClassicClick(ActionEvent event) {
        cuffdiffClassic.setSelected(true);
        cuffdiffGeometric.setSelected(false);
        cuffdiffQuartile.setSelected(false);
    }

    @FXML
    private void cuffdiffGeometricClick(ActionEvent event) {
        cuffdiffGeometric.setSelected(true);
        cuffdiffClassic.setSelected(false);
        cuffdiffQuartile.setSelected(false);
    }

    @FXML
    private void cuffdiffQuartileClick(ActionEvent event) {
        cuffdiffQuartile.setSelected(true);
        cuffdiffClassic.setSelected(false);
        cuffdiffGeometric.setSelected(false);
    }

    @FXML
    private void cuffdiffTotalYesClick(ActionEvent event) {
        cuffdiffTotalYes.setSelected(true);
        cuffdiffTotalNo.setSelected(false);
    }

    @FXML
    private void cuffdiffTotalNoClick(ActionEvent event) {
        cuffdiffTotalNo.setSelected(true);
        cuffdiffTotalYes.setSelected(false);
    }

    @FXML
    private void cuffdiffCompYesClick(ActionEvent event) {
        cuffdiffCompYes.setSelected(true);
        cuffdiffCompNo.setSelected(false);
    }

    @FXML
    private void cuffdiffCompNoClick(ActionEvent event) {
        cuffdiffCompNo.setSelected(true);
        cuffdiffCompYes.setSelected(false);
    }

    @FXML
    private void cuffdiffPooledClick(ActionEvent event) {
        cuffdiffPooled.setSelected(true);
        cuffdiffPer.setSelected(false);
        cuffdiffBlind.setSelected(false);
        cuffdiffPoisson.setSelected(false);
    }

    @FXML
    private void cuffdiffPerClick(ActionEvent event) {
        cuffdiffPer.setSelected(true);
        cuffdiffPooled.setSelected(false);
        cuffdiffBlind.setSelected(false);
        cuffdiffPoisson.setSelected(false);
    }

    @FXML
    private void cuffdiffBlindClick(ActionEvent event) {
        cuffdiffBlind.setSelected(true);
        cuffdiffPooled.setSelected(false);
        cuffdiffPer.setSelected(false);
        cuffdiffPoisson.setSelected(false);
    }

    @FXML
    private void cuffdiffPoissonClick(ActionEvent event) {
        cuffdiffPoisson.setSelected(true);
        cuffdiffPooled.setSelected(false);
        cuffdiffPer.setSelected(false);
        cuffdiffBlind.setSelected(false);
    }
    
    
    private void applySettings() {
        String reads = null;
        String transcript = null;
        String expression = null;
        String animal = null;
        String genes = null;
        String timepaircomp = null;
        String alignment = null;
        String splice = null;
        if (trimReadsYesBtn.isSelected()){reads = "yes";}
        else if (trimReadsNoBtn.isSelected()){reads = "no";}
        else {}
        if (basicFilterYes.isSelected()){transcript = totalFilter.getText() + "," + groupFilter.getText();}
        else if (basicFilterNo.isSelected()){transcript = "no";}
        else {}
        if (filterGenesYesBtn.isSelected()) {expression = thresholdValue.getText();}
        else if (filterGenesNoBtn.isSelected()) {expression = "no";}
        else {}
        if (humanSpecies.isSelected()) {
            animal = "human";
            if (GRCh37.isSelected()) {genes = "GRCh37";}
            else if (GRCh38.isSelected()) {genes = "GRCh38";}
            else {}
        }
        else if (mouseSpecies.isSelected()) {
            animal = "mouse";
            if (NCBIM37.isSelected()) {genes = "NCBIM37";}
            else if (GRCm38.isSelected()) {genes = "GRCm38";}
            else {}
        }
        else if (ratSpecies.isSelected()) {
            animal = "rat";
            if (RGSC3.isSelected()) {genes = "RGSC3.4";}
            else if (Rnor.isSelected()) {genes = "Rnor_5.0";}
            else {}
        }
        else {}
        if (splicingYes.isSelected()) {splice = "yes";}
        else if (splicingNo.isSelected()) {splice = "no";}
        else {}
        if (tophatAlignmentBtn.isSelected()) {alignment = "tophat";}
        else if (starAlignmentBtn.isSelected()) {alignment = "STAR";}
        else {}
        if (pairwiseCompBtn.isSelected()) {timepaircomp = "no";}
        else if (timecourseCompBtn.isSelected()) {timepaircomp = "yes";}
        else {}
        basic = new basicParameters(reads, transcript, expression, animal, genes, timepaircomp, splice, alignment);
        if (useCloudFiles.isSelected()) {basic.readFromWhere = "cloud";}
        else if (uploadFromComputer.isSelected()) {basic.readFromWhere = "computer";}
        String linksMask = null;
        String linksBias = null;
        String linksMulti = null;
        String linksUpper = null;
        String linksTotal = null;
        String linksEffective = null;
        String linksLength = null;
        String linksFraction = null;
        String linksOther = null;
        if (cufflinksMaskYes.isSelected()) {linksMask = "-M:";}
        if (cufflinksMaskNo.isSelected()) {linksMask = "";}
        if (cufflinksBiasYes.isSelected()) {linksBias = "-b:";}
        if (cufflinksBiasNo.isSelected()) {linksBias = "";}
        if (cufflinksMultiYes.isSelected()) {linksMulti = "-u:";}
        if (cufflinksMultiNo.isSelected()) {linksMulti = "";}
        if (cufflinksUpperYes.isSelected()) {linksUpper = "--upper-quartile-norm:";}
        if (cufflinksUpperNo.isSelected()) {linksUpper = "";}
        if (cufflinksTotalYes.isSelected()) {linksTotal = "--total-hits-norm:";}
        if (cufflinksTotalNo.isSelected()) {linksTotal = "";}
        if (cufflinksEffYes.isSelected()) {linksEffective = "";}
        if (cufflinksEffNo.isSelected()) {linksEffective = "--no-effective-length-correction:";}
        if (cufflinksLengthYes.isSelected()) {linksLength = "";}
        if (cufflinksLengthNo.isSelected()) {linksLength = "--no-length-correction:";}
        if (!cufflinksOtherOptions.getText().equals("")) {linksOther = cufflinksOtherOptions.getText();linksFraction = "-j " + cufflinksFraction.getText() + ":";System.out.print("other");}
        else {linksOther = "";linksFraction = "-j " + cufflinksFraction.getText();System.out.print("No other");}
        cufflinks = new cufflinksParameters(linksMask, linksBias, linksMulti, linksUpper, linksTotal, linksEffective, linksLength, linksFraction, linksOther);
        String diffMask = null;
        String diffBias = null;
        String diffMulti = null;
        String diffMethod = null;
        String diffTotal = null;
        String diffComp = null;
        String diffMin = null;
        String diffDiscovery = null;
        String diffDispersion = null;
        String diffOther = null;
        if (cuffdiffMaskYes.isSelected()) {diffMask = "-M:";}
        if (cuffdiffMaskNo.isSelected()) {diffMask = "";}
        if (cuffdiffBiasYes.isSelected()) {diffBias = "-b:";}
        if (cuffdiffBiasNo.isSelected()) {diffBias = "";}
        if (cuffdiffMultiYes.isSelected()) {diffMulti = "-u:";}
        if (cuffdiffMultiNo.isSelected()) {diffMulti = "";}
        if (cuffdiffClassic.isSelected()) {diffMethod = "classic-fpkm:";}
        if (cuffdiffGeometric.isSelected()) {diffMethod = "geometric:";}
        if (cuffdiffQuartile.isSelected()) {diffMethod = "quartile:";}
        if (cuffdiffTotalYes.isSelected()) {diffTotal = "--total-hits-norm:";}
        if (cuffdiffTotalNo.isSelected()) {diffTotal = "";}
        if (cuffdiffCompYes.isSelected()) {diffComp = "--compatible-hits-norm:";}
        if (cuffdiffCompNo.isSelected()) {diffComp = "";}
        if (cuffdiffPooled.isSelected()) {diffDispersion = "pooled:";}
        if (cuffdiffPer.isSelected()) {diffDispersion = "per-condition:";}
        if (cuffdiffBlind.isSelected()) {diffDispersion = "blind:";}
        if (cuffdiffPoisson.isSelected()) {diffDispersion = "poisson:";}
        diffMin = "-c " + cuffdiffMinimum.getText() + ":";
        if (!cuffdiffOtherOptions.getText().equals("")) {diffOther = cuffdiffOtherOptions.getText();diffDiscovery = "--FDR " + cuffdiffFalse.getText() + ":";}
        else {diffOther = "";diffDiscovery = "--FDR " + cuffdiffFalse.getText();}
        cuffdiff = new cuffdiffParameters(diffMask, diffBias, diffMulti, diffMethod, diffTotal, diffComp, diffMin, diffDiscovery, diffDispersion, diffOther);
    }
        
    @FXML
    private void loadExistingProject(ActionEvent event) throws FileNotFoundException, IOException {
        DirectoryChooser projectDirectory = new DirectoryChooser();
        projectDirectory.setTitle("Select Project Folder:");
        File initialLocation = projectDirectory.showDialog(null);
        if(initialLocation!= null) {
        String midLoc = initialLocation.getAbsolutePath();
        String finalpath = midLoc.substring(0, midLoc.lastIndexOf(sep));
        File refLocation = new File(finalpath);
        String projectName = midLoc.substring(midLoc.lastIndexOf(sep)+1);
        if (refLocation != null) {
            loadProject(refLocation, projectName);
            if (!isProjectEmpty(projectWrapper.p9)) {projectWrapper.p10.change(projectWrapper.p9);}
            if (!isProjectEmpty(projectWrapper.p8)) {projectWrapper.p9.change(projectWrapper.p8);}
            if (!isProjectEmpty(projectWrapper.p7)) {projectWrapper.p8.change(projectWrapper.p7);}
            if (!isProjectEmpty(projectWrapper.p6)) {projectWrapper.p7.change(projectWrapper.p6);}
            if (!isProjectEmpty(projectWrapper.p5)) {projectWrapper.p6.change(projectWrapper.p5);}
            if (!isProjectEmpty(projectWrapper.p4)) {projectWrapper.p5.change(projectWrapper.p4);}
            if (!isProjectEmpty(projectWrapper.p3)) {projectWrapper.p4.change(projectWrapper.p3);}
            if (!isProjectEmpty(projectWrapper.p2)) {projectWrapper.p3.change(projectWrapper.p2);}
            if (!isProjectEmpty(projectWrapper.p1)) {projectWrapper.p2.change(projectWrapper.p1);}
            projectWrapper.p1.change(project);
            writeProjectFile();
            populateRecentProjectList();
            existingProjectView.getSelectionModel().select(0);
            projectNameLabel.setText(project.projectName);
            projectStatusPane.setVisible(true);
        }
    }
    }

@FXML
    private void retrieveResults(ActionEvent event) throws IOException, JSchException {
    String projectFilename = mainpath;
    FileReader fr = new FileReader(projectFilename);
    BufferedReader textReader = new BufferedReader(fr);
    int numOfLines = readLines(projectFilename);
    String IP="none";
    String PEM="none";
    String MethToAuth="none";
    String UserName="none";
    String home = "none";
    for(int i = 0; i < numOfLines; i++) {
    String line = textReader.readLine();
    String[] array = line.split("\t");
    String location=array[0];
    location=location+sep+projectNameLabel.getText();
    String refProjectName=array[1];
    if(refProjectName.equals(projectNameLabel.getText())){
    String currentProjectFilename = location + sep + "output.txt";
    FileReader fr1 = new FileReader(currentProjectFilename);
    BufferedReader textReader1 = new BufferedReader(fr1);
    int numOfLines1 = readLines(currentProjectFilename);
    for(int j = 0; j < numOfLines1; j++) {
    String line1 = textReader1.readLine();
    String[] array1 = line1.split("\t");
    if(j==1){
    UserName=array1[0];
    IP=array1[1];
    home=array1[2];
    MethToAuth=array1[3];
    PEM=array1[4];
    break;
    }
    }
    String connectionStatus=checkConnection.main(PEM, IP, UserName, MethToAuth);
    if(connectionStatus.equals("success")){
    SCPFrom.main(UserName,IP,PEM,projectNameLabel.getText(), location, MethToAuth,home);
    break;
    }
    }
    }
    }
    
    @FXML
    private void finishSetup(ActionEvent event) throws IOException, JSchException {
        finalSubmitBtn.setVisible(false);
        applySettings();
        String outputFile = project.projectLocation + sep + project.projectName + sep + "output.txt";
        FileWriter file = new FileWriter(outputFile);
        BufferedWriter output = new BufferedWriter(file);
        output.write(project.projectName);
        output.newLine();
        String AuthMeth = new String();
            project.serverUser = username.getText();
            project.EC2Address = connectionAddress.getText();
            project.home = homeFolder.getText();
            String key = new String();
            if (passwordBtn.isSelected()) {key = passwordField.getText();AuthMeth="PASS";}
            else if (fileAuthBtn.isSelected()) {key = keyFile.getText();AuthMeth="KEY";}
            project.keyFileLocation =  key;
            if (jobScheduleBtn.isSelected()) {
                project.jobSched = new jobSchedule();
                output.write(project.serverUser + "\t" + project.EC2Address + "\t" + project.home + "\t" + AuthMeth + "\t" + project.keyFileLocation + "\t" + project.jobSched.output);
            }
            else {output.write(project.serverUser + "\t" + project.EC2Address + "\t" + project.home + "\t" +  AuthMeth + "\t" + project.keyFileLocation);}
        output.newLine();
        for (int j = 0; j < listViewItems.size(); j++)
        {
            String groupName = (String) listViewItems.get(j);
            output.write(groupName);
            int h = j+1;
            if (h < listViewItems.size()) {output.write(" ");}
        }
        output.newLine();
        output.write(basic.rawReads + ":" + basic.transcriptFilter + ":" + basic.expressionFilter + ":" + basic.species + ":" + basic.genome + ":" + basic.pairwise + ":" + basic.splicing + ":" + basic.readFromWhere + ":" + basic.alignment + "|Star-settings");
        if (basic.alignment == "STAR") {
            if (starDefaultButton.isSelected()) {output.write("|TopHat-settings|");}
            else if (starCustomButton.isSelected()) {output.write(" " + starOptions.getText() + "|TopHat-settings|");}
        }
        else if (basic.alignment == "tophat") {
            if (starDefaultButton.isSelected()) {
                output.write("|TopHat-settings|");
            }
            else if (starCustomButton.isSelected()) {
                output.write("|TopHat-settings " + starOptions.getText() + "|");
            }
        }
        if(cufflinksDefaultButton.isSelected()){
        output.write("-M:-u:-j 1.0|");    
        }
        else{output.write(cufflinks.cufflinksMask + cufflinks.cufflinksBias + cufflinks.cufflinksMulti + cufflinks.cufflinksUpper + cufflinks.cufflinksTotal + cufflinks.cufflinksEffective + cufflinks.cufflinksLength + cufflinks.cufflinksFraction + cufflinks.cufflinksOther + "|");
        }
        if(DECuffdiff.isSelected()){
        if(cuffdiffDefaultButtons.isSelected()){
        output.write("-M:-u|");    
        }
        else{
        output.write(cuffdiff.cuffdiffMask + cuffdiff.cuffdiffBias + cuffdiff.cuffdiffMulti + "--library-norm-method " + cuffdiff.cuffdiffNormMethod + cuffdiff.cuffdiffTotal + cuffdiff.cuffdiffComp + "--dispersion-method " + cuffdiff.cuffdiffDispersion + cuffdiff.cuffdiffMin + cuffdiff.cuffdiffFalse + cuffdiff.cuffdiffOther + "|");
        }
        }
        else{
        output.write("SKIP|");    
        }
        if(DEedgeR.isSelected()){
        if(exactTest.isSelected()){
        output.write("yes:");    
        }
        else{
        output.write("no:");    
        }
        if(GML.isSelected()){
        output.write("yes:");    
        }
        else{
        output.write("no:");    
        }
        output.write(edgeRFDR.getText()+":");
        output.write(edgeRFDRMethod.getSelectionModel().getSelectedItem()+":");
        for (int i = 0; i < combinations.size(); i++)
        {
        String combination = combinations.get(i);
        output.write(combination+"-");
        }
        output.write("|");
        }
        if(DEDESeq2.isSelected()){
        output.write(DESeq2FDR.getText()+":");
        output.write(DESeq2FDRMethod.getSelectionModel().getSelectedItem()+":");
        for (int i = 0; i < combinationsDESeq2.size(); i++)
        {
        String combination = combinationsDESeq2.get(i);
        output.write(combination+"-");
        }    
        }
        else{
        output.write("|SKIP");    
        }
        output.newLine();
        for (int i = 0; i < allSamples.size(); i++)
        {
            sampleInfo currentSample = (sampleInfo) allSamples.get(i);
            String thisGroup = (String) currentSample.groupName;
            String sampleName = (String) currentSample.sampleName;
            String fileLocation = (String) currentSample.readLocation;
            String libOutput = currentSample.library.output;
            String meanInsert = (String) currentSample.meanInsertSize;
            if (meanInsert.equals("")){
            meanInsert="-1";    
            }
            String CofV = (String) currentSample.CV;
            if (CofV.equals("")){
            CofV="-1";    
            }
            output.write(thisGroup + "\t" + sampleName + "\t" + fileLocation + "\t" + libOutput + "\t" + meanInsert + "\t" + CofV); output.newLine();
        }
        output.close();
        String connectionStatus=checkConnection.main(project.keyFileLocation, project.EC2Address, project.serverUser, AuthMeth);
        if(connectionStatus.equals("success")){
        File jarfile = new File(mainpath);
        String absolutePath = jarfile.getAbsolutePath();
        String filePath = absolutePath.substring(0,absolutePath.lastIndexOf(File.separator));
        String lfile1 = project.projectLocation + sep + project.projectName + sep + "output.txt";
        String rfile1 = "'" + project.home + "/project.txt" +"'";
        String lfile1_2 = project.projectLocation + sep + project.projectName + sep + "project.txt";
        copyPipeline.CopyTo(lfile1, rfile1, project.keyFileLocation, project.EC2Address, project.serverUser, AuthMeth);
        SCPFrom.CopyFrom(project.serverUser, project.EC2Address, project.keyFileLocation, lfile1_2, rfile1, AuthMeth);
        File f = new File(lfile1_2);
        if(f.exists()){
        copyPipeline.main(filePath,project.projectName, project.keyFileLocation, project.EC2Address, AuthMeth, project.serverUser, project.home, outputFile, uploadFromComputer.isSelected());
        SingleSelectionModel<Tab> selectionModel = tabPane.getSelectionModel();
        selectionModel.select(startTab);
        }
        else{
        finalSubmitBtn.setVisible(true);
        try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
                }
        SwingUtilities.invokeLater(new Runnable() {
        public void run() {    
        JOptionPane.showMessageDialog(null, "Can't access the home folder", "Check the path and permissions of the home folder", JOptionPane.INFORMATION_MESSAGE);    
        }
    });
        }    
        }
    }
    

    @FXML
    private void addSample(ActionEvent event) {
        final int index = groups1.getSelectionModel().getSelectedIndex();
        final int ind2 = libType.getSelectionModel().getSelectedIndex();
        if (index == -1) {
            noGroupError.setVisible(true);
        } else {
            String selectedGroup = (String) listViewItems.get(index);
            String name = sampleName.getText();
            String location = null;
            String CV = cVar.getText();
            if(CV==""){
                CV="-1";
            }
            String insert = insertSize.getText();
            if(insert==""){
                insert="-1";
            }
            int libIndex = libType.getSelectionModel().getSelectedIndex();
            String libraryName = libTypes.get(libIndex);
            adapterSettings library = new adapterSettings(libraryName);
            noGroupError.setVisible(false);
            if (singleEnd.isSelected()) {
                location = singleReadLocation.getText();
                if (sampleName.getText().length() != 0) {
                    if (singleReadLocation.getText().length() != 0) {
                        if (library.exists()) {
                            samplesListView.add(name);
                            sampleList.setItems(samplesListView);
                            sampleInfo thisSample = new sampleInfo(selectedGroup, name, location, library, insert, CV);
                            allSamples.add(thisSample);
                            resetSample();
                        }
                    }
                }
            }
            if (pairedEnd.isSelected()) {
                String leftLocation = leftReadLocation.getText();
                String rightLocation = rightReadLocation.getText();
                location = leftLocation + "|" + rightLocation;
                if (sampleName.getText().length() != 0) {
                    if ((leftReadLocation.getText().length() != 0) && (rightReadLocation.getText().length() != 0)) {
                        if (library.exists()) {
                            samplesListView.add(name);
                            sampleList.setItems(samplesListView);
                            sampleInfo thisSample = new sampleInfo(selectedGroup, name, location, library, insert, CV);
                            allSamples.add(thisSample);
                            resetSample();
                        }
                    }
                }
            }
        }
        Boolean groupCheck = false;
        int X = 0;
        for (int i = 0; i < listViewItems.size(); i++) {
            String groupCheckName = (String) listViewItems.get(i);
            for (int j = 0; j < allSamples.size(); j++) {
                sampleInfo currentSamp = (sampleInfo) allSamples.get(j);
                String currentSampleGroup = currentSamp.groupName;
                if (currentSampleGroup == groupCheckName) {groupCheck = true;}
            }
            if (groupCheck) {X++;}
            groupCheck = false;
        }
        if (X >= listViewItems.size()) {parameters.setDisable(false);}
    }

    @FXML
    private void deleteSample(ActionEvent event) {
        final int index = sampleList.getSelectionModel().getSelectedIndex();
        if (index != -1) {
            samplesListView.remove(index);
            sampleList.setItems(samplesListView);
            allSamples.remove(index);
        }
        if (samplesListView.size() == 0)
        {
            parameters.setDisable(true);
        }
    }

    @FXML
    private void lookForKey(ActionEvent event) {
        FileChooser keyFileChooser = new FileChooser();
        keyFileChooser.setTitle("Select .pem Key File:");
        File pemKey = keyFileChooser.showOpenDialog(null);
        String path;
        if (pemKey != null) {
            keyFile.clear();
            path = pemKey.getPath();
            keyFile.appendText(path);
        }
    }
}
