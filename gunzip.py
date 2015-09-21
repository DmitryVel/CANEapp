#Copyright (C) 2015  Dmitry Velmeshev <dvelmeshev@med.miami.edu>

from __future__ import division
from optparse import OptionParser
import re
import sys
import os, glob
import linecache
import subprocess

parser = OptionParser()
parser.add_option("-p","--path", type=str, dest="path",
                    help="Project directory")
parser.add_option("-i","--input", type=str, dest="input",
                    help="Compressed file")
parser.add_option("-o","--output", type=str, dest="output",
                    help="Decompressed file")
(options, args) = parser.parse_args()
project_dir=options.path
read_name_decompressed=options.output
read_name_compressed=options.input


def main():
    f=open(os.path.join(project_dir,read_name_decompressed),"w")
    subprocess.call(["gzip","-dc",read_name_compressed],stdout=f)
    f.close()

main()
