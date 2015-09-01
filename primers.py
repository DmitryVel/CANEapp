#Copyright (C) 2015  Dmitry Velmeshev <dvelmeshev@med.miami.edu>

from __future__ import print_function
import re
import subprocess
import os, sys
from optparse import OptionParser
import subprocess
import multiprocessing

parser = OptionParser()
parser.add_option("-p", "--path", type=str, dest="path",
                    help="Path to working directory")
parser.add_option("-f", "--folder", type=str, dest="folder",
                    help="Path to home folder")
(options, args) = parser.parse_args()
path=options.path
gtf_name=os.path.join(path,"results","final.gtf")
input_file_name=os.path.join(path,"gene_list.txt")
locus_list=[]
sign_genes_file=open(os.path.join(path,input_file_name),"r")
sign_gene_lines=sign_genes_file.readlines()
for i in range(len(sign_gene_lines)):
    sign_gene=sign_gene_lines[i]
    sign_gene=sign_gene[:-1]
    locus_list.append(sign_gene)
print(locus_list)
home_folder=options.folder
tools_filename=os.path.join(home_folder,"Pipeline","tools","tools.txt")
tools_file=open(tools_filename,"r")
tools_lines=tools_file.readlines()
tophat=re.split("\s+",tools_lines[0])[0]
tophat=os.path.join(tophat,"tophat2")
STAR=re.split("\s+",tools_lines[1])[0]
Cufflinks=re.split("\s+",tools_lines[2])[0]
CNCI=re.split("\s+",tools_lines[3])[0]
primer3=re.split("\s+",tools_lines[4])[0]
samtools=re.split("\s+",tools_lines[5])[0]
samtools=os.path.join(samtools,"samtools")

def check_overhang(check_iso_junct,junction):
    X=int(junction[0])
    Y=int(junction[1])
    X1=int(check_iso_junct[0])
    Y1=int(check_iso_junct[1])
    x_in_range=0
    y_in_range=0
    if X==X1 and Y==Y1:
        return 1
    if X1<X:
        if X1+10>=X:
            x_in_range=1
    if X1>X:
        if X1-10<=X:
            x_in_range=1
    if Y1<Y:
        if Y1+10>=Y:
            y_in_range=1
    if Y1>Y:
        if Y1-10<=Y:
            y_in_range=1
    if x_in_range==1 and y_in_range==1:
        return 1
    else:
        return 0
    
def splice_search():
    line=selected[0]
    split=re.split("\s+",line)
    isoform=split[11]
    isoform=isoform[1:-1]
    y_prev=0
    Junctions=[]
    offset = 10
    for line in selected:
        split=re.split("\s+",line)
        isoform1=split[11]
        isoform1=isoform1[1:-1]
        if isoform1 == isoform:
            y=split[4]
            x=split[3]
            second_exon_length=int(y)-int(x)
            if y_prev != 0:
                Junctions.append([y_prev,x,first_exon_length,second_exon_length])
                y_prev=y
                first_exon_length=int(y)-int(x)
            else:
                y_prev=y
                first_exon_length=int(y)-int(x)
        else:
            y_prev=split[4]
            break
    Junctions_rest=[]
    all_junctions=[]
    current_iso="none"
    for line in selected:
        split=re.split("\s+",line)
        isoform1=split[11]
        if current_iso!="none":
            if isoform1!=current_iso:
                if Junctions_rest!=[]:
                    all_junctions.append(Junctions_rest)
                current_iso=isoform1
                Junctions_rest=[]
                y_prev=0
        else:
            current_iso=isoform1          
        y=split[4]
        x=split[3]
        second_exon_length=int(y)-int(x)
        if y_prev != 0:                
            Junctions_rest.append([y_prev,x,first_exon_length,second_exon_length])
            y_prev=y
            first_exon_length=int(y)-int(x)
        else:
            y_prev=y
            first_exon_length=int(y)-int(x)
    all_junctions.append(Junctions_rest)
    N=0
    worked=0
    junct_index = []
    global working_junctions
    working_junctions = []
    left_lengths = []
    right_lengths = []
    for i in range(len(Junctions)):
        junction=Junctions[i]
        first_exon_length=junction[2]
        second_exon_length=junction[3]
        for j in range(len(all_junctions)):
            transcript2=all_junctions[j]
            for z in range(len(transcript2)):
                check_iso_junct = transcript2[z]
                check_overhang_result=check_overhang(check_iso_junct,junction)
                if check_overhang_result==1:
                    current_first_exon_length=check_iso_junct[2]
                    if current_first_exon_length<first_exon_length:
                        first_exon_length=current_first_exon_length
                    current_second_exon_length=check_iso_junct[3]
                    if current_second_exon_length<second_exon_length:
                        second_exon_length=current_second_exon_length
                N=N+check_overhang_result
        if (N==len(all_junctions)):
            print("Splice junction ", junction)
            worked=worked+1
            junct_index.append(i)
            working_junctions.append(junction)
            left_lengths.append(first_exon_length)
            right_lengths.append(second_exon_length)
        N=0
    if worked==0:
        look_for_exon()
    else:
        splice_sequence(working_junctions, left_lengths, right_lengths)
        
        
def look_for_exon():
    size=200
    line=selected[0]
    split=re.split("\s+",line)
    isoform=split[11]
    isoform=isoform[1:-1]
    List1=[]
    global fragment
    result=0
    for i in range(len(selected)):
        line=selected[i]
        split=re.split("\s+",line)
        isoform1=split[11]
        isoform1=isoform1[1:-1]
        if isoform1 == isoform:
            y=split[4]
            y=int(y)
            x=split[3]
            x=int(x)
            length = y-x
            if length >= size:
                L=(length-size)+1
                for k in range(L):
                    fragment=[x+k,x+k+size]
                    result=frag_check(fragment)
                    if result==1:
                        print ("Exonic region ", fragment)
                        exon_sequence(fragment)
                        break
    if result==0:
        fragment = 0
        print ("No common splice junctions or exonic regions found.")
        #final_output.write("Gene ID: " + input_gene + "\n"+"No primers could be designed"+"\n")
                        
def frag_check(fragment):
    line=selected[0]
    split=re.split("\s+",line)
    isoform=split[11]
    isoform=isoform[1:-1]
    current_iso=0
    success=0
    N_of_iso=0
    for i in range(len(selected)):
        line=selected[i]
        split=re.split("\s+",line)
        isoform1=split[11]
        isoform1=isoform1[1:-1]
        x=split[3]
        x=int(x)
        y=split[4]
        y=int(y)
        if x <= fragment[0] and y >= fragment[1]:
            success=success+1
        if current_iso!=isoform1:
            N_of_iso = N_of_iso+1
            current_iso=isoform1
    if N_of_iso==success:
        return 1
    else:
        return 0

def splice_sequence (working_junctions, left_lengths, right_lengths):
    sequence=""
    for p in range(len(working_junctions)):
        first_hundred = [int(working_junctions[p][0])-left_lengths[p],int(working_junctions[p][0])]
        second_hundred=[int(working_junctions[p][1]),int(working_junctions[p][1])+right_lengths[p]]
        chromo_range=(chromosome + ":" + str(first_hundred[0]) + "-" + str(first_hundred[1]))
        p=subprocess.Popen([samtools,"faidx",fasta, chromo_range],stdout=subprocess.PIPE)
        print(str([samtools,"faidx",fasta, chromo_range]))
        out,err=p.communicate()
        sequence=""
        if  strand == "-":
            for i in range(len(out)):
                if out[i] == "A":
                    sequence=sequence+"T"
                if out[i] == "C":
                    sequence=sequence+"G"
                if out[i] == "G":
                    sequence=sequence+"C"
                if out[i] == "T":
                    sequence=sequence+"A"
        else:
            for i in range(len(out)):
                if out[i] == "A":
                        sequence=sequence+"A"
                if out[i] == "C":
                    sequence=sequence+"C"
                if out[i] == "G":
                    sequence=sequence+"G"
                if out[i] == "T":
                    sequence=sequence+"T"
        target=str(len(sequence))
        chromo_range=(chromosome + ":" + str(second_hundred[0]) + "-" + str(second_hundred[1]))
        p=subprocess.Popen([samtools,"faidx",fasta, chromo_range],stdout=subprocess.PIPE)
        out,err=p.communicate()
        if strand == "-":
            for i in range(len(out)):
                if out[i] == "A":
                    sequence=sequence+"T"
                if out[i] == "C":
                    sequence=sequence+"G"
                if out[i] == "G":
                    sequence=sequence+"C"
                if out[i] == "T":
                    sequence=sequence+"A"
            sequence=sequence[::-1]
        else:
            for i in range(len(out)):
                if out[i] == "A":
                        sequence=sequence+"A"
                if out[i] == "C":
                    sequence=sequence+"C"
                if out[i] == "G":
                    sequence=sequence+"G"
                if out[i] == "T":
                    sequence=sequence+"T"
        primer_sequence(input_gene, sequence,target)

    
def exon_sequence (fragment):
    sequence=""
    chromo_range=(chromosome + ":"+str(fragment[0]) + "-" + str(fragment[1]))
    p=subprocess.Popen([samtools,"faidx",fasta, chromo_range],stdout=subprocess.PIPE)
    out,err=p.communicate()
    print(out)
    if strand == "-":
        for i in range(len(out)):
            if out[i] == "A":
                sequence=sequence+"T"
            if out[i] == "C":
                sequence=sequence+"G"
            if out[i] == "G":
                sequence=sequence+"C"
            if out[i] == "T":
                sequence=sequence+"A"
            sequence=sequence[::-1]
    else:
        for i in range(len(out)):
            if out[i] == "A":
                    sequence=sequence+"A"
            if out[i] == "C":
                sequence=sequence+"C"
            if out[i] == "G":
                sequence=sequence+"G"
            if out[i] == "T":
                sequence=sequence+"T"
    target = str((len(sequence)/2)-1)
    primer_sequence(input_gene, sequence, target)

def primer_sequence(input_gene, sequence, target):
        global primer3
        primer3_input_file=open(os.path.join(path,"primer3_input.txt"),"w")
        primer3_input_file.write("SEQUENCE_ID = " + input_gene + "\n")
        primer3_input_file.write("SEQUENCE_TEMPLATE="+sequence+"\n")
        primer3_input_file.write("SEQUENCE_TARGET="+target+",1\n")
        primer3_input_file.write("PRIMER_TASK=pick_detection_primers\n")
        primer3_input_file.write("PRIMER_PICK_LEFT_PRIMER=1\n")
        primer3_input_file.write("PRIMER_PICK_INTERNAL_OLIGO=0\n")
        primer3_input_file.write("PRIMER_PICK_RIGHT_PRIMER=1\n")
        primer3_input_file.write("PRIMER_OPT_SIZE=18\n")
        primer3_input_file.write("PRIMER_MIN_SIZE=15\n")
        primer3_input_file.write("PRIMER_MAX_SIZE=21\n")
        primer3_input_file.write("PRIMER_MAX_NS_ACCEPTED=0\n")
        primer3_input_file.write("PRIMER_PRODUCT_SIZE_RANGE=80-120\n")
        primer3_input_file.write("PRIMER_THERMODYNAMIC_PARAMETERS_PATH=/scratch/dvelmeshev/Pipeline/tools/primer3-2.3.6/src/primer3_config/")
        primer3_input_file.write("\n")
        primer3_input_file.write("=")
        primer3_input_file.close()
        primer3_input=os.path.join(path,"primer3_input.txt")
        print (primer3+" < "+"'"+primer3_input+"'"+" > "+"'"+os.path.join(path,"primer3_output.txt")+"'")
        os.system(primer3+" < "+"'"+primer3_input+"'"+" > "+"'"+os.path.join(path,"primer3_output.txt")+"'")
        primer3_output_file=open(os.path.join(path,"primer3_output.txt"),"r")
        primer3_out_lines=primer3_output_file.readlines()
        write_final_output(primer3_out_lines)

def write_final_output(primer3_out_lines):
    global working_junctions
    left_primers = []
    right_primers = []
    target_lengths = []
    acceptable_lefts = [20,42,64]
    acceptable_rights =[21,43,65]
    acceptable_targets = [38,60,82]
    #acceptable_lefts = [20,42,64,86,108]
    #acceptable_rights =[21,43,65,87,109]
    #acceptable_targets = [38,60,82,104,126]
    for number in range(len(primer3_out_lines)):
        if len(primer3_out_lines)>=16:
            if primer3_out_lines[16] == "PRIMER_PAIR_NUM_RETURNED=0\n":
                print("Cannot develop primers for this gene.")
                break
            else:
                current_line = primer3_out_lines[number]
                if number in acceptable_lefts:
                    left_product = current_line[23::]
                    left_product = left_product[:-1:]
                    left_primers.append(left_product)
                if number in acceptable_rights:
                    right_product = current_line[24::]
                    right_product = right_product[:-1:]
                    right_primers.append(right_product)
                if number in acceptable_targets:
                    target = current_line[27::]
                    target = target[:-1:]
                    target_lengths.append(target)
        else:
            break
    print(str(left_primers))
    if (left_primers != []):
        if (working_junctions != []):
            Junct = working_junctions[0]
            working_junctions=working_junctions[1:]
            for item in range(len(left_primers)):
                Left = left_primers[item]
                Right = right_primers[item]
                Target = target_lengths[item]
                final_output.write("Gene ID: " + input_gene + "\n")
                final_output.write("Splice Junction " + chromosome + ":" + str(Junct[0]) + "-"+ str(Junct[1]) + "\n")
                final_output.write("Left Primer: " + Left + "\n")
                final_output.write("Right Primer: " + Right + "\n")
                final_output.write("Target Length: " + Target + "\n")
        elif (fragment != 0):
            for item in range(len(left_primers)):
                final_output.write("Gene ID: " + input_gene + "\n")
                final_output.write("Common Exon " + chromosome + ":" + str(fragment[0]) + "-" + str(fragment[1]) + "\n")
                final_output.write("Left Primer: " + left_primers[item] + "\n")
                final_output.write("Right Primer: " + right_primers[item] + "\n")
                final_output.write("Target Length: " + target_lengths[item] + "\n")
    



project_file=open(os.path.join(path,"project.txt"),"r")
project_file_lines=project_file.readlines()                  
home=re.split("\t",project_file_lines[1])
home=home[2]
options_split=re.split("\|",project_file_lines[3])
basic_options=options_split[0]
basic_options_split=re.split("\:",basic_options)
species=basic_options_split[3]
assembly=basic_options_split[4]
ref_file=open(os.path.join(home,"Pipeline","ref","reference.txt"),"r")
ref_lines=ref_file.readlines()
for i in xrange(len(ref_lines)):
    ref_line=ref_lines[i]
    ref_line_split=re.split("\s+",ref_line)
    if ref_line_split[0]==species:
        if ref_line_split[1]==assembly:
            content=ref_line_split[2]
            if content[-2:]=="fa":
                fasta=content                  
final_output = open(os.path.join(path,"primers.txt"), "w")
for i in range(len(locus_list)):
    input_gene=locus_list[i]
    gtf=open(os.path.join(path,gtf_name),"r")
    selected=[]
    for line in gtf:
        split=re.split("\s+",line)
        locus=split[9]
        locus=locus[1:-2]
        if input_gene==locus:
            strand=split[6]
            chromosome = split[0]
            selected.append(line)
    splice_search()
    gtf.close()            
final_output.close()
status_file=open(os.path.join(path,"primer_status.txt"), "w")
status_file.close()

