#Copyright (C) 2015  Dmitry Velmeshev <dvelmeshev@med.miami.edu>

from __future__ import division
from optparse import OptionParser
import re
import sys
import os, glob
import math
import subprocess

parser = OptionParser()
parser.add_option("--LSF", type=str, dest="LSF",
                    help="LSF settings")
parser.add_option("--total_samples", type=str, dest="total_samples",
                    help="Total number of samples")
parser.add_option("--sample_number", type=str, dest="sample_number",
                    help="Cufflinks options")
parser.add_option("--cuff", type=str, dest="cuff",
                    help="Cufflinks options")
parser.add_option("-r", "--reference", type=str, dest="reference",
                    help="Reference")
parser.add_option("--aligner", type=str, dest="aligner",
                    help="Aligner")
parser.add_option("--tools", type=str, dest="tools",
                    help="Tools")
parser.add_option("-p", "--path", type=str, dest="path",
                    help="Path")
parser.add_option("-L", "--input_left", type=str, dest="input_left",
                    help="The name of left reads file")
parser.add_option("-R", "--input_right", type=str, dest="input_right",
                    help="The name of right reads file")
parser.add_option("-1", "--left_output", type=str, dest="left_output",
                    help="Output location")
parser.add_option("-2", "--right_output", type=str, dest="right_output",
                    help="Output location")
parser.add_option("-t", "--trimming", type=str, dest="trimming",
                    help="Trim/do not trim (yes/no)")
parser.add_option("-c", "--cores", type=str, dest="cores",
                    help="Number of cores")
parser.add_option("-f", "--folder", type=str, dest="folder",
                    help="The name of the home folder")
(options, args) = parser.parse_args()
LSF=options.LSF
if LSF!="none":
    LSF=re.split(",",LSF)
aligner=options.aligner
total_samples=options.total_samples
sample_number=options.sample_number
cuff_options=options.cuff
cuff_options=re.split("\s+",cuff_options)
path=options.path
reference=options.reference
reference=re.split(",",reference[1:-1])
tools=options.tools
tools=re.split(",",tools[1:-1])
cores=options.cores
trimming=options.trimming
temp_file_1 = open(os.path.join(path,"temp_file_1"), "r")
left_inputFileName = options.input_left
left_inputFile = open(left_inputFileName, "r")
right_inputFileName = options.input_right
right_inputFile = open(right_inputFileName, "r")
home_folder=options.folder
path_split=re.split(os.path.sep,path)
project_dir=os.path.join(home_folder,path_split[-2])
adaptor=temp_file_1.readlines()
adaptor_split=re.split("\s+", adaptor[0])
fragment_mean=adaptor_split[1]
fragment_mean=float(fragment_mean)
fragment_SD=adaptor_split[2]
fragment_SD=float(fragment_SD)
adapter_length=float(adaptor_split[3])
adaptor_5_1=adaptor_split[4]
adaptor_3_1=adaptor_split[5]
adaptor_5_2=adaptor_split[6]
adaptor_3_2=adaptor_split[7]
library_type=adaptor_split[9]
left_outFilename=options.left_output
right_outFilename=options.right_output
quality_change=0
remove_list=[]


def mean(reads_list):
    total=0
    for i in xrange(len(reads_list)):
        read_length=int(reads_list[i])
        total=total+read_length
    mean=total/(len(reads_list))
    return mean

def SD(reads_list, mean):
    sum_of_squares=0
    for i in xrange(len(reads_list)):
        read_length=int(reads_list[i])
        square_diff=(read_length-mean)*(read_length-mean)
        sum_of_squares=sum_of_squares+square_diff
    SD=math.sqrt(int(sum_of_squares)/len(reads_list))
    return SD
        
def allindices(string, sub, listindex, offset):
	if (string.find(sub) == -1):
		return listindex
	else:
		offset = string.index(sub)+offset
		listindex.append(offset)
		string = string[(string.index(sub)+1):]
		return allindices(string, sub, listindex, offset+1)
	    
def test(line, adaptor_5, adaptor_3):
    if re.search(adaptor_5,line):
            indexes=allindices(line, adaptor_5, [], 0)
            index_5=indexes[-1]
            index_5=index_5+len(adaptor_5)+1
            if re.search(adaptor_3,line):
                indexes=allindices(line, adaptor_3, [], 0)
                index_3=indexes[0]
                trunc=line[index_5:index_3]
            else:
                trunc=line[index_5:]
    else:
        if re.search(adaptor_3,line):
            indexes=allindices(line, adaptor_3, [], 0)
            index_3=indexes[0]
            if re.search(adaptor_5,line):
                indexes=allindices(line, adaptor_5, [], 0)
                index_5=indexes[-1]
                index_5=index_5+len(adaptor_5)+1
                trunc=line[index_5:index_3]
            else:
                trunc=line[:index_3]
        else:
            trunc=line
    return len(trunc)

def main(inputFile1,inputFile2):
        inserts_file=open(os.path.join(path,"insert.txt"),"w")
        N=0
        i=0
        trimmed=0
        longer=0
        L=[]
        R=[]
        global library_type
        global cores
        global LSF
        status_file=open(os.path.join(project_dir,"status.txt"),"a")
        if trimming=="yes":
            fileOut1 = open(left_outFilename, "w")
            fileOut2 = open(right_outFilename, "w")
            status_file.write("Trimming sample "+sample_number+" of "+total_samples+"\n")
            status_file.close()
            for line1_1 in inputFile1:
                line2_1=inputFile2.next()
                try:
                    line1_2=inputFile1.next()
                    line2_2=inputFile2.next()
                except:
                    break
                line1_3=inputFile1.next()
                line2_3=inputFile2.next()
                line1_4=inputFile1.next()
                line2_4=inputFile2.next()
                short1=test(line1_2,adaptor_5_1, adaptor_3_1)
                short2=test(line2_2,adaptor_5_2, adaptor_3_2)
                i=i+1
                new_line=line1_1[-1]
                if short1>20 and short2>20:
                    L.append(short1)
                    R.append(short2)
                    N=N+1
                    longer=longer+1
                    fileOut1.write(line1_1)
                    index_5=0
                    index_3=len(line1_2)-1
                    if re.search(adaptor_5_1,line1_2):
                        indexes=allindices(line1_2, adaptor_5_1, [], 0)
                        index_5=indexes[-1]
                        index_5=index_5+len(adaptor_5_1)+1
                        if re.search(adaptor_3_1,line1_2):
                            indexes=allindices(line1_2, adaptor_3_1, [], 0)
                            index_3=indexes[0]
                            fileOut1.write(line1_2[index_5:index_3])
                            fileOut1.write(new_line)
                            trimmed=trimmed+1
                        else:
                            trimmed=trimmed+1
                            fileOut1.write(line1_2[index_5:])
                    else:
                        if re.search(adaptor_3_1,line1_2):
                            indexes=allindices(line1_2, adaptor_3_1, [], 0)
                            index_3=indexes[0]
                            if re.search(adaptor_5_1,line1_2):
                                indexes=allindices(line1_2, adaptor_5_1, [], 0)
                                index_5=indexes[-1]
                                index_5=index_5+len(adaptor_5_1)+1
                                fileOut1.write(line1_2[index_5:index_3])
                                fileOut1.write(new_line)
                                trimmed=trimmed+1
                            else:
                                trimmed=trimmed+1
                                fileOut1.write(line1_2[:index_3])
                                fileOut1.write(new_line)
                        else:
                            fileOut1.write(line1_2)
                    fileOut1.write(line1_3)
                    fileOut1.write(line1_4[index_5:index_3])
                    fileOut1.write(new_line)
                new_line=line2_1[-1]
                if short1>20 and short2>20:
                    N=N+1
                    longer=longer+1
                    fileOut2.write(line2_1)
                    index_5=0
                    index_3=index_3=len(line2_2)-1
                    if re.search(adaptor_5_2,line2_2):
                        indexes=allindices(line2_2, adaptor_5_2, [], 0)
                        index_5=indexes[-1]
                        index_5=index_5+len(adaptor_5_2)+1
                        if re.search(adaptor_3_2,line2_2):
                            indexes=allindices(line2_2, adaptor_3_2, [], 0)
                            index_3=indexes[0]
                            fileOut2.write(line2_2[index_5:index_3])
                            fileOut2.write(new_line)
                            trimmed=trimmed+1
                        else:
                            trimmed=trimmed+1
                            fileOut2.write(line2_2[index_5:])
                    else:
                        if re.search(adaptor_3_2,line2_2):
                            indexes=allindices(line2_2, adaptor_3_2, [], 0)
                            index_3=indexes[0]
                            if re.search(adaptor_5_2,line2_2):
                                indexes=allindices(line2_2, adaptor_5_2, [], 0)
                                index_5=indexes[-1]
                                index_5=index_5+len(adaptor_5_2)+1
                                fileOut2.write(line2_2[index_5:index_3])
                                fileOut2.write(new_line)
                                trimmed=trimmed+1
                            else:
                                trimmed=trimmed+1
                                fileOut2.write(line2_2[:index_3])
                                fileOut2.write(new_line)
                        else:
                            fileOut2.write(line2_2)
                    fileOut2.write(line2_3)
                    fileOut2.write(line2_4[index_5:index_3])
                    fileOut2.write(new_line)
            mean_L=mean(L)
            mean_R=mean(R)
            SD_L=SD(L,mean_L)
            SD_R=SD(R,mean_R)
            print path
            print "Original reads: "+str(i)
            print "Trimmed reads: "+str((trimmed*100)/(i*2))
            print "Discarded reads: "+str(100*((2*i)-longer)/(i*2))
            print "Number of reads: "+str(longer/2)
            print "Mean of left read: "+str(mean_L)
            print "Mean of right read: "+str(mean_R)
            print "SD of left read: "+str(SD_L)
            print "SD of right read: "+str(SD_R)
            if fragment_mean!=-1:
                mate_distance=fragment_mean-adapter_length-mean_L-mean_R
                mate_distance=int(mate_distance)
            else:
                mate_distance=50
            if fragment_SD!=-1:
                mate_SD=math.sqrt((SD_L*SD_L)+(SD_R*SD_R)+(fragment_SD*fragment_SD))
                mate_SD=int(mate_SD)
            else:
                mate_SD=20
            fileOut1.close()
            fileOut2.close()
        else:
            inputFile1.next()
            inputFile2.next()
            mean_L=len(inputFile1.next())-1
            mean_R=len(inputFile2.next())-1
            if fragment_mean!=-1:
                mate_distance=fragment_mean-adapter_length-mean_L-mean_R
                mate_distance=int(mate_distance)
            else:
                mate_distance=50
            if fragment_SD!=-1:
                mate_SD=fragment_SD
                mate_SD=int(mate_SD)
            else:
                mate_SD=20
        inserts_file.write(str(mate_distance)+"\t"+str(mate_SD))
        inserts_file.close()
               

main(left_inputFile,right_inputFile)


