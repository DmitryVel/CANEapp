#Copyright (C) 2015  Dmitry Velmeshev <dvelmeshev@med.miami.edu>

from __future__ import division
import re
import sys
from optparse import OptionParser
import os, glob

parser = OptionParser()
parser.add_option("-t", "--tracking", type=str, dest="tracking",
                    help="The name of tracking file")
parser.add_option("-p", "--path", type=str, dest="path",
                    help="Path")
parser.add_option("-r", "--replicates_list", type=str, dest="replicates_list",
                    help="List of replicates")
parser.add_option("-g", "--group_filter", type=int, dest="group_filter",
                    help="Percent threshold for filtering within a group")
parser.add_option("-b", "--total_filter", type=int, dest="total_filter",
                    help="Percent threshold for filtering among all samples")
(options, args) = parser.parse_args()
path=options.path
group_filter=options.group_filter
total_filter=options.total_filter
group_filter=group_filter/100
total_filter=total_filter/100
tracking_name=options.tracking
output_file=open(os.path.join(path,"recurrent.txt"), "w")
replicates=options.replicates_list
replicates=replicates[1:-1]
replicates=re.split(",",replicates)
new_replicates=[]
for i in xrange(len(replicates)):
    element=int(replicates[i])
    new_replicates.append(element)

def check(FPKM_list):
    other_groups_occurence=0
    recurrent=0
    groups=[]
    total_size=0
    FPKMs=FPKM_list
    for i in xrange(len(new_replicates)):
        replicate=new_replicates[i]
        group=FPKM_list[:replicate]
        groups.append(group)
        FPKM_list=FPKM_list[replicate:]
    for j in xrange(len(groups)):
        group=groups[j]
        zero_count_group=group.count(0)
        total_size=total_size+len(group)
        if (zero_count_group/len(group))<=(1-group_filter):
            recurrent=1
        if zero_count_group<len(group):
            other_groups_occurence=other_groups_occurence+1
    if recurrent==1 and other_groups_occurence==1:
        recurrent=1
    else:
        recurrent=0
    zero_count=FPKMs.count(0)
    threshold=float((1-total_filter)*(len(FPKMs)))
    if zero_count<=threshold:
        recurrent=1
    return recurrent
    
def break_block(block):
    if block=='-':
        return 0
    else:
        split=re.split("\|", block)
        FPKM=float(split[3])
        return FPKM

def check_recurrency():
    tracking_file=open(os.path.join(path,tracking_name), "r")
    for line in tracking_file:
        fpkm_list=[]
        line=line[:-1]
        split=re.split("\s+", line)
        N=len(split)-4
        ref_construct=split[0]
        for i in xrange(N):
            FPKM_block=split[i+4]
            FPKM=break_block(FPKM_block)
            fpkm_list.append(FPKM)
        recurrency=check(fpkm_list)
        if recurrency==1:
            output_file.write(ref_construct)
            output_file.write("\n")
    output_file.close()
        
                
check_recurrency()
