#Copyright (C) 2015  Dmitry Velmeshev <dvelmeshev@med.miami.edu>

import re
from optparse import OptionParser
import sys
import os, glob


parser = OptionParser()
parser.add_option("-i", "--input", type=str, dest="input",
                    help="The name of gtf file")
parser.add_option("-r", "--reference", type=str, dest="reference",
                    help="The name of reference file")
parser.add_option("-p", "--path", type=str, dest="path",
                    help="Path")
(options, args) = parser.parse_args()
inputFileName = options.input
path=options.path
reference_name=options.reference
input_file=open(os.path.join(path,inputFileName), "r")
output_file=open(os.path.join(path,"stringent.gtf"), "w")
recurrent_file=open(os.path.join(path,reference_name), "r")
recurrent=recurrent_file.readlines()

def filter_ambiguous():
    input_file=open(os.path.join(path,"stringent.gtf"), "r")
    filtered_out=open(os.path.join(path,"filtered.gtf"), "w")
    removed_out=open(os.path.join(path,"removed.gtf"), "w")
    for line in input_file:
        if re.split("\t",line)[6]==".":
            removed_out.write(line)
        else:
            filtered_out.write(line)
    filtered_out.close()
    removed_out.close()
    input_file.close()
    
def recurrnet(current_id):
    current_id=current_id[1:-2]
    found=0
    for i in xrange(len(recurrent)):
        ref_id=recurrent[i]
        ref_id=ref_id[:-1]
        if ref_id==current_id:
            print current_id
            found=1
            return 1
            break
    if found==0:
        return 0
    
def main():
    current_id="nothing"
    line_list=[]
    for line in input_file:
        split=re.split("\s+", line)
        exon_number=split[13]
        transcript_id=split[11]
        class_code=split[17]
        if len(split)>21:
            class_code2=split[21]
        if current_id==transcript_id:
            line_list.append(line)
        else:
            if len(line_list)>1:
                for i in xrange(len(line_list)):
                    out_line=line_list[i]
                    output_file.write(out_line)
                line_list=[]
            else:
                if len(line_list)==1 and recurrnet(current_id)==1:
                    for i in xrange(len(line_list)):
                        out_line=line_list[i]
                        output_file.write(out_line)
                line_list=[]
        if exon_number=='"1";':
            current_id=transcript_id
            line_list.append(line)
        else:
            if len(line_list)>1:
                for i in xrange(len(line_list)):
                    out_line=line_list[i]
                    output_file.write(out_line)
                line_list=[]
            else:
                if len(line_list)==1 and recurrnet(current_id)==1:
                    for i in xrange(len(line_list)):
                        out_line=line_list[i]
                        output_file.write(out_line)
                    line_list=[]
    if len(line_list)>1:
        for i in xrange(len(line_list)):
            out_line=line_list[i]
            output_file.write(out_line)
    else:
        if len(line_list)==1 and recurrnet(current_id)==1:
            for i in xrange(len(line_list)):
                out_line=line_list[i]
                output_file.write(out_line)
            line_list=[]
    output_file.close()
            
main()
filter_ambiguous()
