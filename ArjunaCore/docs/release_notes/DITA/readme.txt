How To Build This Document (UNIX)
------------------------------------------
0. Set up your DITA environment properly by installing the DITA Open Toolkit and setting up ~/.bashrc properly.
1. Add concepts, tasks, or references, in the appropriate directories.
2. Add them to the hierarchy and sequence ditamap files.
3. Build the books using the runbuild.sh script in this directory.
4. Check your output. It will be in output/hierarchy or output/sequence, with a subdirectory per target.

Syntax
----------
Build the hierarchy map, in PDF format
$ ./runbuild.sh hierarchy dita2pdf2

Build the hierarchy and sequence maps, in the default (XHTML) format
$ ./runbuild.sh all

Remove all previously-built content from the output directory
$ ./runbuild.sh clean

You can optionally add a target. Check the build scripts for available targets. Interesting ones include: xhtml (the default), all (builds all targets), dita2pdf2, dita2eclipsehelp


How To Build This Document (Windows)
------------------------------------------
0. Set up your DITA environment properly by installing the DITA Open Toolkit and setting up your environment properly.
1. Add concepts, tasks, or references, in the appropriate directories.
2. Add them to the hierarchy and sequence ditamap files.
3. Build the books using the Ant scripts provided, by means of the runbuild.bat script in this directory. For example.

Syntax
----------
Build the hierarchy map in the default (XHTML) format, save output to the output/ directory
% runbuild.bat hierarchy output

Build the sequence map in PDF format, save output to the output/ directory
% runbuild.bat dita2pdf2 sequence output
