#Copyright (C) 2015  Dmitry Velmeshev <dvelmeshev@med.miami.edu>

from __future__ import division
from optparse import OptionParser
import re
import sys
import os, glob
import linecache
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
parser.add_option("--reference", type=str, dest="reference",
                    help="Reference")
parser.add_option("--tools", type=str, dest="tools",
                    help="Tools")
parser.add_option("--aligner", type=str, dest="aligner",
                    help="Aligner")
parser.add_option("-p", "--path", type=str, dest="path",
                    help="Path to working directory")
parser.add_option("-t", "--library_type", type=str, dest="library_type",
                    help="Name of library prep")
parser.add_option("-m", "--mean", type=float, dest="mean",
                    help="Mean of insert from BioA")
parser.add_option("-c", "--CV", type=float, dest="CV",
                    help="Coefficient of variation from BioA")
parser.add_option("-r", "--read_length", type=int, dest="read_length",
                    help="Length of sequencing read")
parser.add_option("-n", "--read_name", type=str, dest="read_name",
                    help="Read name")
parser.add_option("-s", "--sample_name", type=str, dest="sample_name",
                    help="Sample_name")
parser.add_option("-o", "--trimming", type=str, dest="trimming",
                    help="Trim/do not trim")
parser.add_option("--cores", type=str, dest="cores",
                    help="Number of cores")
parser.add_option("-f", "--folder", type=str, dest="folder",
                    help="The name of the home folder")
(options, args) = parser.parse_args()
total_samples=options.total_samples
sample_number=options.sample_number
home_folder=options.folder
cuff_options=options.cuff
path=options.path
print os.path.sep
path_split=re.split(os.path.sep,path)
LSF=options.LSF
LSF_split=re.split(",",LSF)
project_dir=os.path.join(home_folder,path_split[-2])
if LSF!="none":
    LSFProject=LSF_split[4]
    LSFProject=LSFProject.replace("\n","")
    LSFProject=LSFProject.replace("\r","")
    decompress_out=os.path.join(project_dir,"logs","decompress"+sample_number+".out")
    decompress_L_out=os.path.join(project_dir,"logs","decompress_L"+sample_number+".out")
    decompress_R_out=os.path.join(project_dir,"logs","decompress_R"+sample_number+".out")
    LSF_decompress_run=['bsub','-o',decompress_out,'-q',LSF_split[0],"-n","1","-W",LSF_split[3],"-P",LSFProject]
    LSF_decompress_run_L=['bsub','-o',decompress_L_out,'-q',LSF_split[0],"-n","1","-W",LSF_split[3],"-P",LSFProject]
    LSF_decompress_run_R=['bsub','-o',decompress_R_out,'-q',LSF_split[0],"-n","1","-W",LSF_split[3],"-P",LSFProject]
    trim_out=os.path.join(project_dir,"logs","trim"+sample_number+".out")
    LSF_trim_run=['bsub','-o',trim_out,'-q',LSF_split[0],"-n","1","-W",LSF_split[3],"-P",LSFProject]
else:
    LSF_decompress_run=[]
    LSF_trim_run=[]
os.chdir(project_dir)
reference=options.reference
tools=options.tools
aligner=options.aligner
cores=options.cores
cores_per_sample=cores
trimming=options.trimming
library_type=options.library_type
mean=options.mean
CV=options.CV
read_length=options.read_length
read_name=options.read_name
sample_name=options.sample_name
temp_file=open(os.path.join(path,"temp_file_1"), "w")
SRA=re.split(",",tools[1:-1])[5]
SRA=SRA[2:-1]
t=5000000
s=30000000


def Tophat_Cufflinks_paired(left_read,right_read,mate_distance,mate_SD):
    global tools
    global LSF
    global cores
    global reference
    global cuff_options
    global strand_sel
    temp_file_1 = open(os.path.join(path,"temp_file_1"), "r")
    reference=re.split(",",reference[1:-1])
    cuff_options=re.split("\s+",cuff_options)
    tools=re.split(",",tools[1:-1])
    sample_name=path_split[-1]
    chromo=reference[0]
    chromo=chromo[1:-1]
    Bowtie_index=reference[2]
    Bowtie_index=Bowtie_index[2:-1]
    STAR_index=reference[3]
    STAR_index=STAR_index[2:-1]
    refer=reference[4]
    refer=refer[2:-1]
    mask=reference[5]
    mask=mask[2:-1]
    STAR=tools[0]
    STAR=STAR[1:-1]
    Tophat=tools[1]
    Tophat=Tophat[2:-1]
    Cufflinks=tools[2]
    Cufflinks=Cufflinks[2:-1]
    Cuffcompare=os.path.join(Cufflinks,"cuffcompare")
    Cufflinks=os.path.join(Cufflinks,"cufflinks")
    library_type="fr-"+strand_sel
    status_file=open(os.path.join(project_dir,"status.txt"),"a")
    status_file.write("Aligning sample "+sample_number+" of "+total_samples+"\n")
    status_file.close()
    if LSF!="none":
        LSF=re.split(",",LSF)
        LSFProject=LSF[4]
        LSFProject=LSFProject.replace("\n","")
        LSFProject=LSFProject.replace("\r","")
    else:
        LSF=[]
        LSF_run=[]
    if aligner=="tophat":
        memory=int((((int(cores))*t)/1000))
        if LSF!=[]:
            LSF_run=['bsub','-o',os.path.join(project_dir,"logs","align"+sample_number+".out"),'-e',os.path.join(project_dir,"logs","align"+sample_number+".err"),'-q',LSF[0],'-R','"rusage[mem='+str(memory)+'] span[hosts=1]"',"-n",cores,"-W",LSF[3],"-P",LSFProject]
        subprocess.call(LSF_run+[Tophat,"-o",path,"-r",str(mate_distance),"--mate-std-dev",str(mate_SD),"-p",cores,"--library-type",library_type,Bowtie_index,left_read,right_read])
        if LSF!=[]:
            while True:
                try:
                    open(os.path.join(project_dir,"logs","align"+sample_number+".out"),"r")
                    break
                except:
                    continue
        aligned_reads=os.path.join(path,"accepted_hits.bam")
    if aligner=="STAR":
        memory=int((((int(cores))*s)/1000))
        if LSF!=[]:
            LSF_run=['bsub','-o',os.path.join(project_dir,"logs","align"+sample_number+".out"),'-e',os.path.join(project_dir,"logs","align"+sample_number+".err"),'-q',LSF[0],'-R','"rusage[mem='+str(memory)+'] span[hosts=1]"',"-n",cores,"-W",LSF[3],"-P",LSFProject]
        subprocess.call(LSF_run+[STAR,"--outFileNamePrefix",path+"/","--runThreadN",cores,"--genomeDir",STAR_index,"--outSAMstrandField", "intronMotif","--outSAMtype","BAM","SortedByCoordinate","--readFilesIn",left_read,right_read])
        if LSF!=[]:
            while True:
                try:
                    open(os.path.join(project_dir,"logs","align"+sample_number+".out"),"r")
                    break
                except:
                    continue
        cores=int(cores)*2
        if LSF!=[] and cores>LSF[2]:
            cores=LSF[2]
        cores=str(cores)
        aligned_reads=os.path.join(path,"Aligned.sortedByCoord.out.bam")
    status_file=open(os.path.join(project_dir,"status.txt"),"a")
    status_file.write("Reconstructing sample "+sample_number+" of "+total_samples+"\n")
    status_file.close()
    memory=int((((int(cores))*t)/1000))
    if LSF!=[]:
        LSF_run=['bsub','-o',os.path.join(project_dir,"logs","Cufflinks"+sample_number+".out"),'-e',os.path.join(project_dir,"logs","Cufflinks"+sample_number+".err"),'-q',LSF[0],'-R','"rusage[mem='+str(memory)+'] span[hosts=1]"',"-n",cores,"-W",LSF[3],"-P",LSFProject]
    subprocess.call(LSF_run+[Cufflinks]+cuff_options+["-o",path,"-p",cores,"--library-type",library_type,aligned_reads])
    if LSF!=[]:
        while True:
            try:
                open(os.path.join(project_dir,"logs","Cufflinks"+sample_number+".out"),"r")
                break
            except:
                continue
    if LSF!=[]:
        LSF_run=['bsub','-o',os.path.join(project_dir,"logs","Cuffcompare"+sample_number+".out"),'-e',os.path.join(project_dir,"logs","Cuffcompare"+sample_number+".err"),'-q',LSF[0],"-n","1","-W",LSF[3],"-P",LSFProject]
    subprocess.call(LSF_run+[Cuffcompare,"-o",os.path.join(path,sample_name),"-r",refer,os.path.join(path,"transcripts.gtf")])
    if LSF!=[]:
        while True:
            try:
                open(os.path.join(project_dir,"logs","Cuffcompare"+sample_number+".out"),"r")
                break
            except:
                continue
    os.chdir(path)
    os.chdir("..")
    progress_file_location=os.path.abspath(os.curdir)
    progress_file=open(os.path.join(progress_file_location,"progress.txt"),"a")
    progress_file.write("Paired "+library_type+" "+cores_per_sample)
    progress_file.write("\n")
    progress_file.close()
    

def Tophat_Cufflinks_single(read_name,fragment_length,fragment_SD):
    global tools
    global LSF
    global cores
    global reference
    global cuff_options
    global strand_sel
    reference=re.split(",",reference[1:-1])
    cuff_options=re.split("\s+",cuff_options)
    tools=re.split(",",tools[1:-1])
    sample_name=path_split[-1]
    chromo=reference[0]
    chromo=chromo[1:-1]
    Bowtie_index=reference[2]
    Bowtie_index=Bowtie_index[2:-1]
    STAR_index=reference[3]
    STAR_index=STAR_index[2:-1]
    refer=reference[4]
    refer=refer[2:-1]
    mask=reference[5]
    mask=mask[2:-1]
    Tophat=tools[1]
    Tophat=Tophat[2:-1]
    STAR=tools[0]
    STAR=STAR[1:-1]
    Cufflinks=tools[2]
    Cufflinks=Cufflinks[2:-1]
    Cuffcompare=os.path.join(Cufflinks,"cuffcompare")
    Cufflinks=os.path.join(Cufflinks,"cufflinks")
    library_type="fr-"+strand_sel
    status_file=open(os.path.join(project_dir,"status.txt"),"a")
    status_file.write("Aligning sample "+sample_number+" of "+total_samples+"\n")
    status_file.close()
    if LSF!="none":
        LSF=re.split(",",LSF)
        LSFProject=LSF[4]
        LSFProject=LSFProject.replace("\n","")
        LSFProject=LSFProject.replace("\r","")
    else:
        LSF=[]
        LSF_run=[]
    if aligner=="tophat":
        memory=int((((int(cores))*t)/1000))
        if LSF!=[]:
            LSF_run=['bsub','-o',os.path.join(project_dir,"logs","align"+sample_number+".out"),'-e',os.path.join(project_dir,"logs","align"+sample_number+".err"),'-q',LSF[0],'-R','"rusage[mem='+str(memory)+'] span[hosts=1]"',"-n",cores,"-W",LSF[3],"-P",LSFProject]
        subprocess.call(LSF_run+[Tophat,"-o",path,"-p",cores,"--library-type",library_type,Bowtie_index,read_name])
        if LSF!=[]:
            while True:
                try:
                    open(os.path.join(project_dir,"logs","align"+sample_number+".out"),"r")
                    break
                except:
                    continue
        aligned_reads=os.path.join(path,"accepted_hits.bam")
    if aligner=="STAR":
        memory=int((((int(cores))*s)/1000))
        if LSF!=[]:
            LSF_run=['bsub','-o',os.path.join(project_dir,"logs","align"+sample_number+".out"),'-e',os.path.join(project_dir,"logs","align"+sample_number+".err"),'-q',LSF[0],'-R','"rusage[mem='+str(memory)+'] span[hosts=1]"',"-n",cores,"-W",LSF[3],"-P",LSFProject]
        print str(LSF_run+[STAR,"--outFileNamePrefix",path+"/","--runThreadN",cores,"--genomeDir",STAR_index,"--outSAMstrandField", "intronMotif","--outSAMtype","BAM","SortedByCoordinate","--readFilesIn",read_name])
        subprocess.call(LSF_run+[STAR,"--outFileNamePrefix",path+"/","--runThreadN",cores,"--genomeDir",STAR_index,"--outSAMstrandField", "intronMotif","--outSAMtype","BAM","SortedByCoordinate","--readFilesIn",read_name])
        if LSF!=[]:
            while True:
                try:
                    open(os.path.join(project_dir,"logs","align"+sample_number+".out"),"r")
                    break
                except:
                    continue
        cores=int(cores)*2
        if LSF!=[] and cores>LSF[2]:
            cores=LSF[2]
        cores=str(cores)
        aligned_reads=os.path.join(path,"Aligned.sortedByCoord.out.bam")
    status_file=open(os.path.join(project_dir,"status.txt"),"a")
    status_file.write("Reconstructing sample "+sample_number+" of "+total_samples+"\n")
    status_file.close()
    memory=int((((int(cores))*t)/1000))
    if LSF!=[]:
        LSF_run=['bsub','-o',os.path.join(project_dir,"logs","Cufflinks"+sample_number+".out"),'-q',LSF[0],'-R','"rusage[mem='+str(memory)+'] span[hosts=1]"',"-n",cores,"-W",LSF[3],"-P",LSFProject]
    subprocess.call(LSF_run+[Cufflinks]+cuff_options+["-o",path,"-p",cores,"-j","1.0","-m",str(int(fragment_length)),"-s",str(int(fragment_SD)),"-M",mask,"--library-type",library_type,aligned_reads])
    if LSF!=[]:
        while True:
            try:
                open(os.path.join(project_dir,"logs","Cufflinks"+sample_number+".out"),"r")
                break
            except:
                continue
    if LSF!=[]:
        LSF_run=['bsub','-o',os.path.join(project_dir,"logs","Cuffcompare"+sample_number+".out"),'-e',os.path.join(project_dir,"logs","Cuffcompare"+sample_number+".err"),'-q',LSF[0],"-n","1","-W",LSF[3],"-P",LSFProject]
    subprocess.call(LSF_run+[Cuffcompare,"-o",os.path.join(path,sample_name),"-r",refer,os.path.join(path,"transcripts.gtf")])
    if LSF!=[]:
        while True:
            try:
                open(os.path.join(project_dir,"logs","Cuffcompare"+sample_number+".out"),"r")
                break
            except:
                continue
    os.chdir(path)
    os.chdir("..")
    progress_file_location=os.path.abspath(os.curdir)
    progress_file=open(os.path.join(progress_file_location,"progress.txt"),"a")
    progress_file.write("Single "+library_type+" "+str(int(fragment_length))+" "+str(int(fragment_SD))+" "+cores_per_sample)
    progress_file.write("\n")
    progress_file.close()


def main(read_name):
    global strand_sel
    library_type_split=re.split("\:",library_type)
    lib_type=library_type_split[1]
    if lib_type=="single":
        L_5=library_type_split[2]
        L_3=library_type_split[3]
        adapter_length=int(library_type_split[4])
        strand_sel=library_type_split[5]
    if lib_type=="paired":
        L_5=library_type_split[2]
        L_3=library_type_split[3]
        R_5=library_type_split[4]
        R_3=library_type_split[5]
        adapter_length=int(library_type_split[6])
        strand_sel=library_type_split[7]
    if lib_type=="paired":
        if mean==-1:
            ins_mean=200
        else:
            ins_mean=mean-adapter_length
        if CV==-1:
            SD=-1
        else:
            SD=(CV/100)*mean
        split_read=re.split("\:",read_name)
        print str(split_read)
        read_L=split_read[0]
        read_L_local=read_L
        read_R=split_read[1]
        read_R_local=read_R
        decompress=0
        RUN_SRA=0
        if read_L_local[-3:]=="bz2":
            decompress=1
            read_L_local_new=read_L_local[:-4]
            read_L_local_new=re.split(os.path.sep,read_L_local_new)[-1]
            subprocess.call(LSF_decompress_run_L+["python",os.path.join(home_folder,"beta/bz2.py"),"-p",project_dir,"-i",read_L_local,"-o",read_L_local_new])
            read_L_local=read_L_local[:-4]
            read_L_local=re.split(os.path.sep,read_L_local)[-1]
            read_L_local=os.path.join(project_dir,read_L_local)
        if read_L_local[-6:]=="tar.gz":
            decompress=1
            subprocess.call(LSF_decompress_run_L+["tar","-zxvf",read_L_local])
            read_L_local=read_L_local[:-7]
            read_L_local=re.split(os.path.sep,read_L_local)[-1]
            read_L_local=os.path.join(project_dir,read_L_local)
        if read_L_local[-3:]==".gz" and not read_L_local[-6:]=="tar.gz":
            decompress=1
            read_L_local_new=read_L_local[:-3]
            read_L_local_new=re.split(os.path.sep,read_L_local_new)[-1]
            subprocess.call(LSF_decompress_run_L+["python",os.path.join(home_folder,"beta/gunzip.py"),"-p",project_dir,"-i",read_L_local,"-o",read_L_local_new])
            read_L_local=read_L_local[:-3]
            read_L_local=re.split(os.path.sep,read_L_local)[-1]
            read_L_local=os.path.join(project_dir,read_L_local)
        if read_L_local[-4:]==".tar":
            decompress=1
            subprocess.call(LSF_decompress_run_L+["tar","-xvf",read_L_local])
            read_L_local=read_L_local[:-5]
            read_L_local=re.split(os.path.sep,read_L_local)[-1]
            read_L_local=os.path.join(project_dir,read_L_local)
        if read_R_local[-3:]=="bz2":
            decompress=1
            read_R_local_new=read_R_local[:-4]
            read_R_local_new=re.split(os.path.sep,read_R_local_new)[-1]
            subprocess.call(LSF_decompress_run_R+["python",os.path.join(home_folder,"beta/bz2.py"),"-p",project_dir,"-i",read_R_local,"-o",read_R_local_new])
            read_R_local=read_R_local[:-4]
            read_R_local=re.split(os.path.sep,read_R_local)[-1]
            read_R_local=os.path.join(project_dir,read_R_local)
        if read_R_local[-6:]=="tar.gz":
            decompress=1
            subprocess.call(LSF_decompress_run_R+["tar","-zxvf",read_R_local])
            read_R_local=read_R_local[:-7]
            read_R_local=re.split(os.path.sep,read_R_local)[-1]
            read_R_local=os.path.join(project_dir,read_R_local)
        if read_R_local[-3:]==".gz" and not read_R_local[-6:]=="tar.gz":
            decompress=1
            read_R_local_new=read_R_local[:-3]
            read_R_local_new=re.split(os.path.sep,read_R_local_new)[-1]
            subprocess.call(LSF_decompress_run_R+["python",os.path.join(home_folder,"beta/gunzip.py"),"-p",project_dir,"-i",read_R_local,"-o",read_R_local_new])
            read_R_local=read_R_local[:-3]
            read_R_local=re.split(os.path.sep,read_R_local)[-1]
            read_R_local=os.path.join(project_dir,read_R_local)
        if read_R_local[-4:]==".tar":
            decompress=1
            subprocess.call(LSF_decompress_run_R+["tar","-xvf",read_R_local])
            read_R_local=read_R_local[:-5]
            read_R_local=re.split(os.path.sep,read_R_local)[-1]
            read_R_local=os.path.join(project_dir,read_R_local)
        if LSF!="none" and decompress==1 and RUN_SRA==0:
            while True:
                try:
                    open(decompress_L_out,"r")
                    break
                except:
                    continue
        if LSF!="none" and decompress==1 and RUN_SRA==0:
            while True:
                try:
                    open(decompress_R_out,"r")
                    break
                except:
                    continue
        if read_L_local[-3:]=="sra":
            decompress=1
            RUN_SRA=1
            subprocess.call(LSF_decompress_run+[SRA,"-I","--split-files",read_L_local])
            read_L_local=read_L_local[:-4]+"_1.fastq"
            read_L_local=re.split(os.path.sep,read_L_local)[-1]
            read_L_local=os.path.join(project_dir,read_L_local)
            read_R_local=read_R_local[:-4]+"_2.fastq"
            read_R_local=re.split(os.path.sep,read_R_local)[-1]
            read_R_local=os.path.join(project_dir,read_R_local)
        if LSF!="none" and decompress==1 and RUN_SRA==1:
            while True:
                try:
                    open(decompress_out,"r")
                    break
                except:
                    continue
        temp_file.write("paired")
        temp_file.write("\t")
        temp_file.write(str(mean))
        temp_file.write("\t")
        temp_file.write(str(SD))
        temp_file.write("\t")
        temp_file.write(str(adapter_length))
        temp_file.write("\t")
        temp_file.write(str(L_5))
        temp_file.write("\t")
        temp_file.write(str(L_3))
        temp_file.write("\t")
        temp_file.write(str(R_5))
        temp_file.write("\t")
        temp_file.write(str(R_3))
        temp_file.write("\t")
        temp_file.write("trim")
        temp_file.write("\t")
        temp_file.write(strand_sel)
        temp_file.close()
        subprocess.call(LSF_trim_run+["python",os.path.join(home_folder,"beta/adaptor_trimming_paired.py"),"-f",home_folder,"--LSF",LSF,"--total_samples",total_samples,"--sample_number",sample_number,"--cuff",cuff_options,"-c",cores,"-r",reference,"--tools",tools,"-L",read_L_local,"-R",read_R_local,"-1",os.path.join(path,sample_name)+"_L.txt","-2",os.path.join(path,sample_name)+"_R.txt","-p",path,"-t",trimming,"--aligner",aligner])
        if LSF!="none":
            while True:
                try:
                    open(trim_out,"r")
                    break
                except:
                    continue
        if trimming=="yes":
            left_read=os.path.join(path,sample_name)+"_L.txt"
            right_read=os.path.join(path,sample_name)+"_R.txt"
        else:
            left_read=read_L_local
            right_read=read_R_local
        inserts_file=open(os.path.join(path,"insert.txt"),"r")
        inserts_line=inserts_file.next()
        inserts_split=re.split("\t",inserts_line)
        mate_distance=inserts_split[0]
        mate_SD=inserts_split[1]
        Tophat_Cufflinks_paired(left_read,right_read,int(mate_distance),int(mate_SD))       
    if lib_type=="single":
        read_name_local=read_name
        decompress=0
        if read_name_local[-3:]=="bz2":
            decompress=1
            read_name_local_new=read_name_local[:-4]
            read_name_local_new=re.split(os.path.sep,read_name_local_new)[-1]
            print os.path.join(project_dir,read_name_local_new)
            print read_name_local
            subprocess.call(LSF_decompress_run+["python",os.path.join(home_folder,"beta/bz2.py"),"-p",project_dir,"-i",read_name_local,"-o",read_name_local_new])
            read_name_local=read_name_local[:-4]
            read_name_local=re.split(os.path.sep,read_name_local)[-1]
            read_name_local=os.path.join(project_dir,read_name_local)
        if read_name_local[-6:]=="tar.gz":
            decompress=1
            subprocess.call(LSF_decompress_run+["tar","-zxvf",read_name_local])
            read_name_local=read_name_local[:-7]
            read_name_local=re.split(os.path.sep,read_name_local)[-1]
            read_name_local=os.path.join(project_dir,read_name_local)
        if read_name_local[-3:]==".gz" and not read_name_local[-6:]=="tar.gz":
            decompress=1
            read_name_local_new=read_name_local[:-3]
            read_name_local_new=re.split(os.path.sep,read_name_local_new)[-1]
            subprocess.call(LSF_decompress_run+["python",os.path.join(home_folder,"beta/gunzip.py"),"-p",project_dir,"-i",read_name_local,"-o",read_name_local_new])
            read_name_local=read_name_local[:-3]
            read_name_local=re.split(os.path.sep,read_name_local)[-1]
            read_name_local=os.path.join(project_dir,read_name_local)
        if read_name_local[-4:]==".tar":
            decompress=1
            subprocess.call(LSF_decompress_run+["tar","-xvf",read_name_local])
            read_name_local=read_name_local[:-5]
            read_name_local=re.split(os.path.sep,read_name_local)[-1]
            read_name_local=os.path.join(project_dir,read_name_local)
        if read_name_local[-3:]=="sra":
            decompress=1
            print SRA+" "+read_name_local
            subprocess.call(LSF_decompress_run+[SRA,read_name_local])
            read_name_local=read_name_local[:-4]+".fastq"
            read_name_local=re.split(os.path.sep,read_name_local)[-1]
            read_name_local=os.path.join(project_dir,read_name_local)
        if LSF!="none" and decompress==1:
            while True:
                try:
                    open(decompress_out,"r")
                    break
                except:
                    continue
        if mean==-1:
            ins_mean=-1
        else:
            ins_mean=mean-adapter_length
        if CV==-1:
            SD=-1
        else:
            SD=(CV/100)*mean
        temp_file.write("single")
        temp_file.write("\t")
        temp_file.write(str(ins_mean))
        temp_file.write("\t")
        temp_file.write(str(L_5))
        temp_file.write("\t")
        temp_file.write(str(L_3))
        temp_file.write("\t")
        if SD==-1:
            temp_file.write(str((SD)))
        else:
            temp_file.write(str(int(SD)))
        temp_file.write("\t")
        temp_file.write("trim")
        temp_file.write("\t")
        temp_file.write(strand_sel)
        temp_file.close()
        print str(LSF_trim_run+["python",os.path.join(home_folder,"beta/adaptor_trimming_single.py"),"-f",home_folder,"--LSF",LSF,"--total_samples",total_samples,"--sample_number",sample_number,"--cuff",cuff_options,"-c",cores,"-r",reference,"--tools",tools,"-i",read_name_local,"-o",sample_name+".txt","-p",path,"-t",trimming,"--aligner",aligner])
        subprocess.call(LSF_trim_run+["python",os.path.join(home_folder,"beta/adaptor_trimming_single.py"),"-f",home_folder,"--LSF",LSF,"--total_samples",total_samples,"--sample_number",sample_number,"--cuff",cuff_options,"-c",cores,"-r",reference,"--tools",tools,"-i",read_name_local,"-o",sample_name+".txt","-p",path,"-t",trimming,"--aligner",aligner])
        if LSF!="none":
            while True:
                try:
                    open(trim_out,"r")
                    break
                except:
                    continue
        if trimming=="yes":
            outFileName=sample_name+".txt"
            read_name=os.path.join(path,outFileName)
        else:
            read_name=read_name_local
        if mean==-1:
            ins_mean=200
        else:
            ins_mean=mean-adapter_length
        if CV==-1:
            SD=80
        else:
            SD=(CV/100)*mean
        Tophat_Cufflinks_single(read_name,int(ins_mean),int(SD))
        

main(read_name)
