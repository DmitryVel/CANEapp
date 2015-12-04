#Copyright (C) 2015  Dmitry Velmeshev <dvelmeshev@med.miami.edu>

from __future__ import division
import os
from optparse import OptionParser
import re
import sys
import os, glob
import subprocess
import multiprocessing
import math

parser = OptionParser()
parser.add_option("-i", "--input", type=str, dest="input",
                    help="The name of input file")
parser.add_option("-f", "--folder", type=str, dest="folder",
                    help="The name of the home folder")
folders=[]
(options, args) = parser.parse_args()
project_file_name=options.input
project_file=open(project_file_name, "r")
project_lines=project_file.readlines()
cores=multiprocessing.cpu_count()
home_folder=options.folder
mem_config_location="/proc/meminfo"
mem_file=open(mem_config_location,"r")
total_mem_line=mem_file.next()
total_mem_split=re.split("\s+",total_mem_line)
total_mem=int(total_mem_split[1])

def process_cuff_options(Cuffdiff_options,mask,chromo):
    Cuffdiff_options_split=re.split("\:",Cuffdiff_options)
    Cuffdiff_options=""
    for i in xrange(len(Cuffdiff_options_split)):
        Cuffdiff_options_element=Cuffdiff_options_split[i]
        Cuffdiff_options=Cuffdiff_options+Cuffdiff_options_element+" "
    Cuffdiff_options=Cuffdiff_options[:-1]
    print "Cuffdiff: "+Cuffdiff_options
    if "-M" in Cuffdiff_options_split:
        Cuffdiff_options=Cuffdiff_options[3:]
        Cuffdiff_options=Cuffdiff_options+" -M "+mask
    if "-b" in Cuffdiff_options_split:
        Cuffdiff_options=Cuffdiff_options[3:]
        Cuffdiff_options=Cuffdiff_options+" -b "+chromo
    return Cuffdiff_options

def get_tools():
    tools_filename=os.path.join(home_folder,"Pipeline","tools","tools.txt")
    tools_file=open(tools_filename,"r")
    tools_lines=tools_file.readlines()
    tophat=re.split("\s+",tools_lines[0])[0]
    os.environ['PATH'] += ':'+tophat+'/'
    tophat=os.path.join(tophat,"tophat2")
    STAR=re.split("\s+",tools_lines[1])[0]
    Cufflinks=re.split("\s+",tools_lines[2])[0]
    CNCI=re.split("\s+",tools_lines[3])[0]
    primer_3=re.split("\s+",tools_lines[4])[0]
    samtools=re.split("\s+",tools_lines[5])[0]
    bowtie=re.split("\s+",tools_lines[6])[0]
    SRA=re.split("\s+",tools_lines[7])[0]
    htseq_count=re.split("\s+",tools_lines[8])[0]
    R=re.split("\s+",tools_lines[9])[0]
    os.environ['PATH'] += ':'+samtools+'/'
    os.environ['PATH'] += ':'+bowtie+'/'
    os.environ['PATH'] += ':'+home_folder+'/beta/'
    os.environ['PATH'] += ':'+home_folder+'/bin/'
    return [STAR,tophat,Cufflinks,CNCI,primer_3,SRA,htseq_count,samtools,R]
    
def get_design(project_name,project_lines,groups):
    global transfer
    data_list=[]
    labels=[]
    transcripts=[]
    replicates=[]
    job_list=[]
    for i in xrange(len(project_lines)-4):
        line=project_lines[i+4]
        split=re.split("\t",line)
        group=split[0]
        sample=split[1]
        read=split[2]
        if "|" in read:
            read_split_split=re.split("\|",read)
            if transfer!="computer":
                left_read_path=read_split_split[0]
                right_read_path=read_split_split[1]
            else:
                left_read_split=re.split("\\\\",read_split_split[0])
                left_read_split=re.split(os.path.sep,left_read_split[-1])
                right_read_split=re.split("\\\\",read_split_split[1])
                right_read_split=re.split(os.path.sep,right_read_split[-1])
                left_read_path=os.path.join(home_folder,project_name,left_read_split[-1])
                right_read_path=os.path.join(home_folder,project_name,right_read_split[-1])
            read=left_read_path+":"+right_read_path
        else:
            if transfer=="computer":
                read_split=re.split("\\\\",read)
                read_split=re.split(os.path.sep,read_split[-1])
                read=os.path.join(home_folder,project_name,read_split[-1])
        library=split[3]
        mean=split[4]
        CV=split[5]
        CV=CV.replace("\n","")
        CV=CV.replace("\r","")
        data_list.append([group,sample,read,library,mean,CV])
    master_list=[]
    group_mask=[]
    for i in xrange(len(groups)):
        ref_group=groups[i]
        group_list=[]
        sample_list=[]
        for j in xrange(len(data_list)):
            next_element=data_list[j]
            next_group=next_element[0]
            if next_group==ref_group and ref_group not in group_mask:
                group_list.append(next_element[1:])
                sample_list.append(next_element[1])
                job_list.append(next_element[1:])
                transcripts.append(os.path.join(home_folder,project_name,next_element[1],"transcripts.gtf"))
        if ref_group not in group_mask:
            master_list.append([ref_group,group_list])
            labels.append([ref_group,sample_list])
            replicates.append(len(sample_list))
        group_mask.append(ref_group)
    print str([labels,transcripts,replicates,job_list])
    return [labels,transcripts,replicates,job_list]
    
def get_project_info(project_lines):
    global total_mem
    global cores
    global transfer
    project_name=project_lines[0]
    project_name=project_name.replace("\n","")
    project_name=project_name.replace("\r","")
    print "Project name: "+ project_name
    ip_address=re.split("\t",project_lines[1])[1]
    print "ip address: "+ ip_address
    key_file_location=re.split("\t",project_lines[1])[4]
    key_file_location=key_file_location.replace("\n","")
    key_file_location=key_file_location.replace("\r","")
    print "Key file: "+ key_file_location
    try:
        LSF=re.split("\t",project_lines[1])[5]
        LSF=re.split(",",LSF)
        total_mem=int(LSF[1])*1000
        cores=int(LSF[2])
    except:
        LSF=[]
    groups=re.split("\s+",project_lines[2])
    groups=groups[:-1]
    print "Groups: "+ str(groups)
    options_split=re.split("\|",project_lines[3])
    basic_options=options_split[0]
    basic_options_split=re.split("\;",basic_options)
    trimming=basic_options_split[0]
    print "Trimming: "+ trimming
    if basic_options_split[1]!="no":
        transcript_filter=re.split(",",basic_options_split[1])
    else:
        transcript_filter="no"
    print "Transcript filter: "+ str(transcript_filter)
    exp_filter=basic_options_split[2]
    print "Expression filter: "+ exp_filter
    species=basic_options_split[3]
    print "Species: "+ species
    assembly=basic_options_split[4]
    print "Assembly: "+ assembly
    time_series=basic_options_split[7]
    print "Time series: "+ time_series
    splicing=basic_options_split[8]
    print "Splicing: "+ splicing
    transfer=basic_options_split[9]
    print "Transfer: "+ transfer
    aligner=basic_options_split[10]
    print "Aligner: "+ aligner
    STAR_options=options_split[1]
    print "STAR option: "+ STAR_options
    TopHat_options=options_split[2]
    print "TopHat options: "+ TopHat_options
    Cufflinks_options=options_split[3]
    print "Cufflinks options: "+ Cufflinks_options
    Cuffdiff_options=options_split[4]
    print "Cuffdiff options: "+ Cuffdiff_options
    edgeR_options=options_split[5]
    print "edgeR options: "+ edgeR_options
    DESeq_options=options_split[6]
    print "DEseq options: "+ DESeq_options
    return [project_name,groups,trimming,transcript_filter,exp_filter,species,assembly,time_series,splicing,aligner,STAR_options,TopHat_options,Cufflinks_options,Cuffdiff_options,LSF,edgeR_options,DESeq_options]
     
def create_job(sample_name,read_name,library_type,insert_length,insert_CV,reference,tools,aligner,trimming,Cufflinks_options,cores_per_sample,sample_number,LSF):
    current_dir=os.path.join(path,sample_name)
    folders.append(current_dir)
    os.system("mkdir "+"'"+current_dir+"'")
    if LSF!=[]:
        LSFProject=LSF[4]
        LSFProject=LSFProject.replace("\n","")
        LSFProject=LSFProject.replace("\r","")
        LSF_run=str(LSF[0]+","+LSF[1]+","+LSF[2]+","+LSF[3]+","+LSFProject)
        LSF_pars=['bsub','-o',os.path.join(path,"logs",sample_name+'_lib.out'),'-e',os.path.join(path,"logs",sample_name+'_lib.err'),'-q',"general","-n","1",'-R','"rusage[mem=15000] span[hosts=1]"',"-W","160:00","-P",LSFProject]
    else:
        LSF_run="none"
        LSF_pars=[]
    print str(["python",os.path.join(home_folder,"beta/library.py"),"-f",home_folder,"--cuff",Cufflinks_options,"--cores",str(cores_per_sample),"-n",read_name,"-o",trimming,"-s",sample_name,"-p",current_dir,"-L",os.path.join(home_folder,"beta/libraries.txt"),"-t",library_type,"-m",insert_length,"-c",insert_CV,"-r","100","--reference",str(reference),"--tools",str(tools),"--aligner",aligner])       
    subprocess.Popen(LSF_pars+["python",os.path.join(home_folder,"beta/library.py"),"--LSF",LSF_run,"--total_samples",str(total_samples),"--sample_number",str(sample_number),"-f",home_folder,"--cuff",Cufflinks_options,"--cores",str(cores_per_sample),"-n",read_name,"-o",trimming,"-s",sample_name,"-p",current_dir,"-t",library_type,"-m",insert_length,"-c",insert_CV,"-r","100","--reference",str(reference),"--tools",str(tools),"--aligner",aligner])

def get_reference(project_info):
    reference_filename=os.path.join(home_folder,"Pipeline","ref","reference.txt")
    reference_file=open(reference_filename,"r")
    project_lines=reference_file.readlines()
    species=project_info[5]
    assembly=project_info[6]
    STAR_index="none"
    Bowtie_index="none"
    for i in xrange(len(project_lines)):
        project_line=project_lines[i]
        project_line_split=re.split("\s+",project_line)
        found=0
        if project_line_split[0]==species:
            if project_line_split[1]==assembly:
                content=project_line_split[2]
                if content[-2:]=="fa":
                    chromo=content
                    found=1
                if content[-4:]=="2bit":
                    twoBit=content
                    found=1
                if content[-4:]=="STAR":
                    STAR_index=content
                    found=1
                if content[-7:]=="ref.gtf":
                    refer=content
                    found=1
                if content[-8:]=="mask.gtf":
                    mask=content
                    found=1
                if content[-3:]=="ref":
                    ENS_ref=content
                if content[-6:]=="bowtie":
                    Bowtie_index=content
    return [chromo,twoBit,Bowtie_index,STAR_index,refer,mask,ENS_ref]

def filter_ambiguous(input_file):
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

def Cuffquant(quant,samples_and_strands,LSF,LSFProject,aligner,path,Cuffdiff_options,gtf):
    Cuffdiff_options_split=re.split(" ",Cuffdiff_options)
    quant_options=""
    skip=0
    for element in Cuffdiff_options_split:
        if element!="--FDR":
            if skip==0:
                quant_options=quant_options+element+" "
            else:
                skip=0
        else:
            skip=1
    quant_options=quant_options[:-1]
    Pr=cores
    Mr=total_mem
    status_file=open(os.path.join(home_folder,project_name,"status.txt"),"a")
    status_file.write("Quantifying reads in loci...\n")
    status_file.close()
    quant_memory=20000000
    N=len(samples_and_strands)
    progress_file=open(os.path.join(path,"quant_progress.txt"),"w")
    progress_file.close()
    finished=0
    samples_left=N    
    while finished!=N:
        try:
            progress_file=open(os.path.join(path,"quant_progress.txt"),"r")
            progress_lines=progress_file.readlines()
            if len(progress_lines)>finished:
                last_process=progress_lines[-1]
                used_cores=re.split("\s+",last_process)
                used_cores=used_cores[0]
                used_cores=int(used_cores)
                finished=len(progress_lines)
                Mr=Mr+used_cores*t
                Pr=Pr+used_cores
            progress_file.close()
        except:
            continue
        if LSF!=[]:
            cores_per_sample=int(total_mem/quant_memory)
            if cores_per_sample>cores:
                cores_per_sample=cores
            if len(samples_and_strands)>0:
                samples_and_strand=samples_and_strands[0]
                samples_and_strands=samples_and_strands[1:]
                sample=samples_and_strand[0]
                strand=samples_and_strand[1]
                if aligner=="tophat":
                    read=os.path.join(path,sample,"accepted_hits.bam")
                if aligner=="STAR":
                    read=os.path.join(path,sample,"Aligned.sortedByCoord.out.bam")
                if strand=="reverse":
                    strand_sel="fr-firststrand"
                if strand=="yes":
                    strand_sel="fr-secondstrand"
                if strand=="no":
                    strand_sel="fr-unstranded"
                LSF_run=['bsub','-o',os.path.join(path,"logs",sample+"_quant.out"),'-e',os.path.join(path,"logs",sample+"_quant.err"),'-R','"rusage[mem='+"120000"+'] span[hosts=1]"','-q',"bigmem","-n","4","-W","24:00","-P",LSFProject]
                subprocess.call(LSF_run+["python",os.path.join(home_folder,"beta/Cuffquant.py"),"--quant",quant,"--settings",quant_options,"-s",strand_sel,"-i",read,"-g",gtf,"-p",os.path.join(path,sample),"--mainpath",path,"--cores","8"])
        if samples_left>0 and LSF==[]:
            for i in xrange(samples_left):
                if i<samples_left:
                    cores_per_sample=int(Mr/(quant_memory*(samples_left-i)))
                    if cores_per_sample>=1:
                        break
            if Pr>=cores_per_sample and cores_per_sample>=1 and LSF==[]:
                Mr=Mr-cores_per_sample*quant_memory
                Pr=Pr-cores_per_sample
                if len(samples_and_strands)>0:
                    samples_left=samples_left-1
                    samples_and_strand=samples_and_strands[0]
                    samples_and_strands=samples_and_strands[1:]
                    sample=samples_and_strand[0]
                    strand=samples_and_strand[1]
                    if aligner=="tophat":
                        read=os.path.join(path,sample,"accepted_hits.bam")
                    if aligner=="STAR":
                        read=os.path.join(path,sample,"Aligned.sortedByCoord.out.bam")
                    if strand=="reverse":
                        strand_sel="fr-firststrand"
                    if strand=="yes":
                        strand_sel="fr-secondstrand"
                    if strand=="no":
                        strand_sel="fr-unstranded"
                    subprocess.call(["python",os.path.join(home_folder,"beta/Cuffquant.py"),"--quant",quant,"--settings",quant_options,"-s",strand_sel,"-i",read,"-g",gtf,"-p",os.path.join(path,sample),"--mainpath",path,"--cores",str(cores_per_sample)])
                
    
def main(reference,project_info,design,tools):
    global cores_per_sample
    global path
    samples_and_strands=[]
    Mr=total_mem
    Tophat_min_p=1
    t=10000000
    s=30000000
    STAR_min_p=1
    Cufflinks_min_p=1
    Pr=cores
    sample_number=0
    project_name=project_info[0]
    path=os.path.join(home_folder,project_name)
    trimming=project_info[2]
    transcript_filter=project_info[3]
    exp_filter=project_info[4]
    if transcript_filter!="no":
        total_filter=transcript_filter[0]
        group_filter=transcript_filter[1]
    expression_filter=project_info[4]
    time_series=project_info[7]
    aligner=project_info[9]
    Cufflinks_options=project_info[12]
    Cuffdiff_options=project_info[13]
    LSF=project_info[14]
    edgeR_options=project_info[15]
    DESeq_options=project_info[16]
    chromo=reference[0]
    twoBit=reference[1]
    Bowtie_index=reference[2]
    STAR_index=reference[3]
    refer=reference[4]
    mask=reference[5]
    ENS_ref=reference[6]
    labels=design[0]
    transcripts=design[1]
    replicates=design[2]
    job_list=design[3]
    STAR=tools[0]
    tophat=tools[1]
    Cufflinks=tools[2]
    Cuffcompare=os.path.join(Cufflinks,"cuffcompare")
    Cuffdiff=os.path.join(Cufflinks,"cuffdiff")
    quant=os.path.join(Cufflinks,"cuffquant")
    Cufflinks=os.path.join(Cufflinks,"cufflinks")
    CNCI=tools[3]
    primer_3=tools[4]
    htseq_count=tools[6]
    samtools=tools[7]
    R=tools[8]
    samtools=os.path.join(samtools,"samtools")
    N=len(job_list)
    progress_file=open(os.path.join(path,"progress.txt"),"w")
    progress_file.close()
    finished=0
    sample_size=0
    Cufflinks_options=process_cuff_options(Cufflinks_options,mask,chromo)
    samples_left=N
    read_formats=[]
    progress_file=open(os.path.join(path,"progress.txt"),"r")
    progress_lines=progress_file.readlines()
    print len(progress_lines)
    print N
    while finished!=N:
        try:
            progress_file=open(os.path.join(path,"progress.txt"),"r")
            progress_lines=progress_file.readlines()
            if len(progress_lines)>finished:
                last_process=progress_lines[-1]
                used_cores=re.split("\s+",last_process)
                used_cores=used_cores[-2]
                used_cores=int(used_cores)
                finished=len(progress_lines)
                if aligner=="tophat":
                    Mr=Mr+used_cores*t
                    Pr=Pr+used_cores
                if aligner=="STAR":
                    Mr=Mr+used_cores*s
                    Pr=Pr+used_cores
            progress_file.close()
        except:
            continue
        if LSF!=[]:
            if aligner=="tophat":
                cores_per_sample=8
                min_p=Tophat_min_p
            if aligner=="STAR":
                cores_per_sample=2
                min_p=STAR_min_p
            if cores_per_sample>cores:
                cores_per_sample=cores
            if len(job_list)>0:
                job=job_list[0]
                job_list=job_list[1:]
                sample_name=job[0]
                read_name=job[1]
                library_type=job[2]
                insert_length=job[3]
                insert_CV=job[4]
                library_type_split=re.split("\:",library_type)
                read_format=library_type_split[1]
                read_formats.append([sample_name,read_format])
                if read_format=="single":
                    strand_sel=library_type_split[5]
                if read_format=="paired":
                    strand_sel=library_type_split[7]
                if strand_sel=="firststrand":
                    strand="reverse"
                if strand_sel=="secondstrand":
                    strand="yes"
                if strand_sel=="unstranded":
                    strand="no"
                samples_and_strands.append([sample_name,strand])
                sample_number=sample_number+1
                print "Running with "+str(cores_per_sample)+" cores per sample"
                create_job(sample_name,read_name,library_type,insert_length,insert_CV,reference,tools,aligner,trimming,Cufflinks_options,cores_per_sample,sample_number,LSF)
        if samples_left>0 and LSF==[]:
            for i in xrange(samples_left):
                if i<samples_left:
                    if aligner=="tophat":
                        cores_per_sample=int(Mr/(t*(samples_left-i)))
                        min_p=Tophat_min_p
                    if aligner=="STAR":
                        cores_per_sample=int(Mr/(s*(samples_left-i)))
                        min_p=STAR_min_p
                    if cores_per_sample>=min_p:
                        break
            if Pr>=cores_per_sample and cores_per_sample>=min_p and LSF==[]:
                if aligner=="tophat":
                    Mr=Mr-cores_per_sample*t
                    Pr=Pr-cores_per_sample
                if aligner=="STAR":
                    Mr=Mr-cores_per_sample*s
                    Pr=Pr-cores_per_sample
                if len(job_list)>0:
                    samples_left=samples_left-1
                    job=job_list[0]
                    job_list=job_list[1:]
                    sample_name=job[0]
                    read_name=job[1]
                    library_type=job[2]
                    insert_length=job[3]
                    insert_CV=job[4]
                    library_type_split=re.split("\:",library_type)
                    read_format=library_type_split[1]
                    read_formats.append([sample_name,read_format])
                    if read_format=="single":
                        strand_sel=library_type_split[5]
                    if read_format=="paired":
                        strand_sel=library_type_split[7]
                    if strand_sel=="secondstrand":
                        strand="reverse"
                    if strand_sel=="firststrand":
                        strand="yes"
                    if strand_sel=="unstranded":
                        strand="no"
                    samples_and_strands.append([sample_name,strand])
                    sample_number=sample_number+1
                    print "Running with "+str(cores_per_sample)+" cores per sample"
                    create_job(sample_name,read_name,library_type,insert_length,insert_CV,reference,tools,aligner,trimming,Cufflinks_options,cores_per_sample,sample_number,LSF)
    replicates=str(replicates)
    paired=0
    for k in xrange(len(progress_lines)):
        progress_line=progress_lines[k]
        progress_line_split=re.split("\s+",progress_line)
        if progress_line_split[0]=="Paired":
            paired=1
    S=0
    mean_sum=0
    SD_sq_sum=0
    mean_diff_square_sum=0
    if paired==0:
        for k in xrange(len(progress_lines)):
            progress_line=progress_lines[k]
            progress_line_split=re.split("\s+",progress_line)
            mean=int(progress_line_split[2])
            SD=int(progress_line_split[3])
            S=S+1
            mean_sum=mean_sum+mean
            SD_sq_sum=SD_sq_sum+SD*SD
        agg_mean=mean_sum/S
        for k in xrange(len(progress_lines)):
            progress_line=progress_lines[k]
            progress_line_split=re.split("\s+",progress_line)
            mean=int(progress_line_split[2])
            mean_diff=mean-agg_mean
            mean_diff_square_sum=mean_diff_square_sum+mean_diff*mean_diff
        agg_SD=math.sqrt(((SD_sq_sum)+mean_diff_square_sum)/S)
        print str(agg_mean)
        print str(agg_SD)
    library=progress_line_split[1]
    diff_labels=""
    for i in xrange(len(labels)):
        samples_list=labels[i]
        group=samples_list[0]
        diff_labels=diff_labels+group+","
    diff_labels=diff_labels[:-1]
    Cuffdiff_memory=10000000
    Cuffdiff_cores=int(total_mem/Cuffdiff_memory)
    if LSF!=[]:
        if Cuffdiff_cores>int(LSF[2]):
            Cuffdiff_cores=int(LSF[2])
    if Cuffdiff_options!="SKIP":
        Cuffdiff_options=process_cuff_options(Cuffdiff_options,mask,chromo)
        diff=[Cuffdiff,"-o",path,"-p",str(Cuffdiff_cores),"--library-type",library,"-L",diff_labels]
        if time_series=="yes":
            diff.append("-T")
        if paired==0:
            diff=diff+["-m",str(agg_mean),"-s",str(agg_SD)]
        if transcript_filter!="no":
            diff=diff+re.split("\s+",Cuffdiff_options)+[os.path.join(path,"filtered.gtf")]
        else:
            diff=diff+re.split("\s+",Cuffdiff_options)+[os.path.join(path,"all.combined.gtf")]
        for i in xrange(len(labels)):
            samples_list=labels[i]
            samples_list=samples_list[1]
            read_group=""
            for j in xrange(len(samples_list)):
                sample=samples_list[j]
                read=os.path.join(path,sample,"abundances.cxb")
                read_group=read_group+","+read
            diff.append(read_group[1:])
    status_file=open(os.path.join(home_folder,project_name,"status.txt"),"a")
    status_file.write("Combining transcript files...\n")
    status_file.close()
    if LSF!=[]:
        LSFProject=LSF[4].replace("\n","")
        LSFProject=LSFProject.replace("\r","")
        LSF_run=['bsub','-o',os.path.join(path,"logs","cuffcompare.out"),'-e',os.path.join(path,"logs","cuffcompare.err"),'-q',"general","-n","1","-W","160:00","-P",LSFProject]
    else:
        LSF_run=[]
    subprocess.call(LSF_run+[Cuffcompare,"-o",os.path.join(path,"all"),"-r",refer]+transcripts)
    if LSF!=[]:
        while True:
            try:
                open(os.path.join(path,"logs","cuffcompare.out"),"r")
                break
            except:
                continue
    if transcript_filter!="no":
        status_file=open(os.path.join(home_folder,project_name,"status.txt"),"a")
        status_file.write("Filtering spurious transcripts...\n")
        status_file.close()
        if LSF!=[]:
            LSF_run=['bsub','-o',os.path.join(path,"logs","recurrent.out"),'-e',os.path.join(path,"logs","recurrent.err"),'-q',"general","-n","1","-W","160:00","-P",LSFProject]
        subprocess.call(LSF_run+["python",os.path.join(home_folder,"beta/recurrent.py"),"-t","all.tracking","-p",path,"-r",replicates,"-g",group_filter,"-b",total_filter])
        if LSF!=[]:
            while True:
                try:
                    open(os.path.join(path,"logs","recurrent.out"),"r")
                    break
                except:
                    continue
        if LSF!=[]:
            LSF_run=['bsub','-o',os.path.join(path,"logs","novel.out"),'-e',os.path.join(path,"logs","novel.err"),'-q',"general","-n","1","-W","160:00","-P",LSFProject]
        subprocess.call(LSF_run+["python",os.path.join(home_folder,"beta/novel.py"),"-i","all.combined.gtf","-r","recurrent.txt","-p",path])
        if LSF!=[]:
            while True:
                try:
                    open(os.path.join(path,"logs","novel.out"),"r")
                    break
                except:
                    continue
        status_file=open(os.path.join(home_folder,project_name,"status.txt"),"a")
        status_file.write("Classifying transcripts...\n")
        status_file.close()
        if LSF!=[]:
            LSF_run=['bsub','-o',os.path.join(path,"logs","classify.out"),'-e',os.path.join(path,"logs","classify.err"),'-q',"general","-n","1","-W","160:00","-P",LSFProject]
        subprocess.call(LSF_run+["python",os.path.join(home_folder,"beta/classify.py"),"-i","stringent.gtf","-f",ENS_ref,"-p",path])
        if LSF!=[]:
            while True:
                try:
                    open(os.path.join(path,"logs","classify.out"),"r")
                    break
                except:
                    continue
    else:
        status_file=open(os.path.join(home_folder,project_name,"status.txt"),"a")
        status_file.write("Classifying transcripts...\n")
        status_file.close()
        if LSF!=[]:
            LSF_run=['bsub','-o',os.path.join(path,"logs","classify.out"),'-e',os.path.join(path,"logs","classify.err"),'-q',"general","-n","1","-W","160:00","-P",LSFProject]
        subprocess.call(LSF_run+["python",os.path.join(home_folder,"beta/classify.py"),"-i","all.combined.gtf","-f",ENS_ref,"-p",path])
        if LSF!=[]:
            while True:
                try:
                    open(os.path.join(path,"logs","classify.out"),"r")
                    break
                except:
                    continue
    os.mkdir(os.path.join(path,"results"))
    status_file=open(os.path.join(home_folder,project_name,"status.txt"),"a")
    status_file.write("Estimating coding potential...\n")
    status_file.close()
    if LSF!=[]:
        LSF_run=['bsub','-o',os.path.join(path,"logs","coding_potential.out"),'-e',os.path.join(path,"logs","coding_potential.err"),'-q',"general",'-R','"rusage[mem='+"28000"+'] span[hosts=1]"',"-n","8","-W","160:00","-P",LSFProject]
    subprocess.call(LSF_run+["python",os.path.join(home_folder,"beta/coding_potential.py"),"-a",twoBit,"-p",path,"-c","8","--CNCI",CNCI])
    if LSF!=[]:
        while True:
            try:
                open(os.path.join(path,"logs","coding_potential.out"),"r")
                break
            except:
                continue
    run_cuffdiff="no"
    if Cuffdiff_options!="SKIP":
        gtf=os.path.join(path,"filtered.gtf")
        Cuffquant(quant,samples_and_strands,LSF,LSFProject,aligner,path,Cuffdiff_options,gtf)
        status_file=open(os.path.join(home_folder,project_name,"status.txt"),"a")
        status_file.write("Running Cuffdiff..\n")
        status_file.close()
        if LSF!=[]:
            LSF_run=['bsub','-o',os.path.join(path,"logs","cuffdiff.out"),'-e',os.path.join(path,"logs","cuffdiff.out"),'-q',"bigmem",'-R','"rusage[mem='+"120000"+'] span[hosts=1]"',"-n","8","-W","24:00","-P",LSFProject]
        subprocess.call(LSF_run+diff)
        if LSF!=[]:
            while True:
                try:
                    open(os.path.join(path,"logs","cuffdiff.out"),"r")
                    break
                except:
                    continue
        status_file=open(os.path.join(home_folder,project_name,"status.txt"),"a")
        status_file.write("Formatting results...\n")
        status_file.close()
        if LSF!=[]:
            LSF_run=['bsub','-o',os.path.join(path,"logs","format.out"),'-e',os.path.join(path,"logs","format.err"),'-q',"general","-n","1","-W","160:00","-P",LSFProject]
        subprocess.call(LSF_run+["python",os.path.join(home_folder,"beta/format.py"),"-p",path,"-s",str(labels)])
        if LSF!=[]:
            while True:
                try:
                    open(os.path.join(path,"logs","format.out"),"r")
                    break
                except:
                    continue
    if edgeR_options!="SKIP" or DESeq_options!="SKIP":
        if transcript_filter=="no":
            input_file=open(os.path.join(path,"stringent.gtf"), "r")
            filter_ambiguous(input_file)
        gtf=os.path.join(path,"filtered.gtf")
        status_file=open(os.path.join(home_folder,project_name,"status.txt"),"a")
        status_file.write("Quantifying reads in loci...\n")
        status_file.close()
	print str(samples_and_strands)
        HTSeq_memory=5000000
        HTSeq_allowed=int(total_mem/HTSeq_memory)
        HTSeq_finished=0
        HTSeq_progress_file=open(os.path.join(path,"HTSeq_progress.txt"),"a")
        HTSeq_progress_file.close()
        completed_HTSeq_runs=[]
        if HTSeq_allowed>int(cores):
            HTSeq_allowed=int(cores)
        for j in xrange(len(samples_and_strands)):
            samples_and_strand=samples_and_strands[j]
            sample=samples_and_strand[0]
            strand=samples_and_strand[1]
            if aligner=="tophat":
                read=os.path.join(path,sample,"accepted_hits.bam")
            if aligner=="STAR":
                read=os.path.join(path,sample,"Aligned.sortedByCoord.out.bam")
	    if LSF!=[]:
                LSF_run=['bsub','-o',os.path.join(path,"logs",sample+"_HTSeq.out"),'-e',os.path.join(path,"logs",sample+"_HTSeq.err"),'-q',"general","-n","1",'-R','"rusage[mem='+"28000"+'] span[hosts=1]"',"-W","160:00","-P",LSFProject]
                print str(LSF_run)
	    print  str(["python",os.path.join(home_folder,"beta/HTSeq.py"),"--sam",samtools,"-c",htseq_count,"-s",strand,"-i",read,"-g",gtf,"-p",os.path.join(path,sample),"--mainpath",path])
            if LSF!=[]:
                subprocess.call(LSF_run+["python",os.path.join(home_folder,"beta/HTSeq.py"),"--sam",samtools,"-c",htseq_count,"-s",strand,"-i",read,"-g",gtf,"-p",os.path.join(path,sample),"--mainpath",path])
            else:
                if HTSeq_allowed>0:
                    subprocess.Popen(["python",os.path.join(home_folder,"beta/HTSeq.py"),"--sam",samtools,"-c",htseq_count,"-s",strand,"-i",read,"-g",gtf,"-p",os.path.join(path,sample),"--mainpath",path])
                    HTSeq_allowed=HTSeq_allowed-1
                else:
                    while HTSeq_allowed<1:
                        HTSeq_progress_file=open(os.path.join(path,"HTSeq_progress.txt"),"r")
                        if HTSeq_finished<len(HTSeq_progress_file.readlines()):
                            HTSeq_allowed=HTSeq_allowed+(len(HTSeq_progress_file.readlines())-HTSeq_finished)
                            HTSeq_finished=len(HTSeq_progress_file.readlines())
                            HTSeq_progress_file.close()
        if LSF==[]:
            while HTSeq_finished<len(samples_and_strands):
                HTSeq_progress_file=open(os.path.join(path,"HTSeq_progress.txt"),"r")
                HTSeq_finished=len(HTSeq_progress_file.readlines())
                HTSeq_progress_file.close()
        if LSF!=[]:
            HtSeq_completed=0
            while True:
                if len(samples_and_strands)==HtSeq_completed:
                    break
                for j in xrange(len(samples_and_strands)):
                    samples_and_strand=samples_and_strands[j]
                    sample=samples_and_strand[0]
                    try:
                        open(os.path.join(path,"logs",sample+"_HTSeq.out"),"r")
                        if not sample in completed_HTSeq_runs:
                            completed_HTSeq_runs.append(sample)
                            HtSeq_completed=HtSeq_completed+1
                    except:
                        continue
        sample_names=""
        group_list=""
        formats=""
        for i in xrange(len(labels)):
            samples_list=labels[i]
            group=samples_list[0]
            samples_list=samples_list[1]
            group_list=group_list+","+group+":"+str(len(samples_list))
            for j in xrange(len(samples_list)):
                sample_names=sample_names+","+samples_list[j]
                for k in xrange(len(read_formats)):
                    read_format=read_formats[k]
                    if samples_list[j]==read_format[0]:
                        formats=formats+","+read_format[1]
        sample_names=sample_names[1:]
        group_list=group_list[1:]
        formats=formats[1:]
        DESeq_options=DESeq_options.replace("\n","")
        DESeq_options=DESeq_options.replace("\r","")
        DESeq_options=str(DESeq_options)
        edgeR_options=str(edgeR_options)
        if LSF!=[]:
            LSF_run=['bsub','-o',os.path.join(path,"logs","R.out"),'-e',os.path.join(path,"logs","R.err"),'-q',"general","-n","1",'-R','"rusage[mem='+"28000"+'] span[hosts=1]"',"-W","160:00","-P",LSFProject]
        print str(["python",os.path.join(home_folder,"beta/gene_expression.py"),"-p",path,"-s",sample_names,"-g",group_list,"-r",R,"-f",formats,"--DESeq_options",DESeq_options,"--edgeR_options",edgeR_options])
        subprocess.call(LSF_run+["python",os.path.join(home_folder,"beta/gene_expression.py"),"-p",path,"-s",sample_names,"-g",group_list,"-r",R,"-f",formats,"--DESeq_options",DESeq_options,"--edgeR_options",edgeR_options])
        if LSF!=[]:
            while True:
                try:
                    open(os.path.join(path,"logs","R.out"),"r")
                    break
                except:
                    continue
    status_file=open(os.path.join(home_folder,project_name,"status.txt"),"a")
    status_file.write("Done!\n")
    status_file.close()
    progress_file=open(os.path.join(path,"progress.txt"),"a")
    progress_file.write("done\n")
    final_files=os.listdir(os.path.join(path,"results"))
    for i in xrange(len(final_files)):
        final_file=final_files[i]
        filesize=os.path.getsize(os.path.join(path,"results",final_file))
        progress_file.write(final_file+"\t"+str(filesize)+"\n")
    progress_file.close()

project_info=get_project_info(project_lines)
project_name=project_info[0]
groups=project_info[1]
design=get_design(project_name,project_lines,groups)
reference=get_reference(project_info)
tools=get_tools()
status_file=open(os.path.join(home_folder,project_name,"status.txt"),"a")
total_samples=len(design[1])
main(reference,project_info,design,tools)


