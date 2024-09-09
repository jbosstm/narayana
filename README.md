# narayanaDocumentation

This repository contains the documentation for the various projects within Narayana and the product specific enhancements.

This Maven project uses the AsciiDoctor Maven plugin to build an HTML website.
Use the following command to build the website:
```shell
mvn clean generate-resources
```
The website will be produced in the `./target/html` folder.

## Procedure to migrate from DocBook to AsciiDoc

There are two complementary ways to migrate the Narayana documentation from DocBook to AsciiDoc.

**NOTE**: These two techniques to migrate the Narayana documentation are "complementary" as alone they don't produce good results.
In fact, neither of them can be used independently to reliably migrate the Narayana documentation to AsciiDoc.
On the other hand, using both techniques together can give us a good margin to be confident enough that the outcome is the best result possible.

### Pandoc

Once pandoc is installed in your system, the command to migrate a file is:
```
pandoc --wrap=none -f docbook -t asciidoc {input_xml_file} > {output_adoc_file}
```
For example (this is the last file I was working on):
```
pandoc --wrap=none -f docbook -t asciidoc project/en-US/jts/trailmap.xml > project/en-US/jts/trailmap.adoc
```

Although I prepared a script to convert all XML files to AsciiDoc files, I ended up 'converting'/'working on' single files.

```
#!/bin/bash

# Find all XML files and store their paths in an array
xml_files=($(find . -name '*.xml'))

# Loop through each XML file
for xml_file in "${xml_files[@]}"; do
    # Construct the output AsciiDoc file name
    asciidoc_file="${xml_file%.xml}.asciidoc"

    # Convert XML to AsciiDoc using pandoc
    pandoc --wrap=none -f docbook -t asciidoc "$xml_file" -o "$asciidoc_file"

    # Output the generated AsciiDoc file name
    echo "Generated AsciiDoc file: $asciidoc_file"
done
```

**_WARNING_**: I've noticed that pandoc, in some cases, skipped entire parts of the XML document (it seems that pandoc is not compatible with some DocBook tags and/or the order they are positioned).
This problem forced me to check manually every adoc file produced with pandoc and compare it with its XML counterpart.

### DocBookRx

To (partially) overcome the problem described above, I tried to install and use DocBookRx.
The outcome is definitely better than what pandoc is able to produce.
Unfortunately, also DocBookRx is far from being a perfect migration tool.
Thus, manual check should be carried out to avoid mistakes.

#### Ruby installation

Before installing DocBookRx, ensure that Ruby 2.7.x is installed on the system. On Fedora 40, execute the following commands:

```
sudo dnf install rbenv ruby-build-rbenv
echo 'eval "$(rbenv init -)"' >> ~/.bashrc
source ~/.bashrc

rbenv install 2.7.6
rbenv global 2.7.6
ruby -v
```

More details can be found [here](https://developer.fedoraproject.org/start/sw/web-app/rails.html)

#### DocBookRx Installation

Follow instructions [here](https://github.com/asciidoctor/docbookrx?tab=readme-ov-file#installing-the-development-version)

**NOTE**: Make sure that Ruby 2.7.6 is in use

To use DocBookRx, go into the folder where you cloned the repo and run:
```
bundle exec docbookrx ${PATH_TO_THE_DOCUMENTATION_REPO}/project/en-US/jts/trailmap.xml
```

I also wrote a couple of scripts to help me with this task:

* Script to convert XML files using DocBookRx.
it must be run in the DocBookRx folder and the path to the documentation repo should be passed as parameter:
```
#!/bin/bash

# Find all XML files and store their paths in an array
xml_files=($(find "$1" -type f -name "*.xml"))

# Loop through each XML file
for xml_file in "${xml_files[@]}"; do
    # Convert XML to AsciiDoc using docbookrx 
    bundle exec docbookrx "$xml_file"
done
```

As I didn't (easily) find an option to tell DocBookRx to name the output file following the pattern `*-docbookrx.adoc`, I wrote a simple script to do so:
```
#!/bin/bash

# Check if path argument is provided
if [ $# -eq 0 ]; then
    echo "Usage: $0 <path>"
    exit 1
fi

# Check if the provided path exists
if [ ! -d "$1" ]; then
    echo "Error: Path '$1' does not exist."
    exit 1
fi

# Find all .adoc files in the provided path and its subdirectories
find "$1" -type f -name '*.adoc' | while read -r file; do
    # Rename the file
    new_name="${file%.adoc}-docbookrx.adoc"
    mv "$file" "$new_name"
    echo "Renamed $file to $new_name"
done
```
Of course, the script to rename the adoc files should be run immediately after the script to convert XML files using DocBookRx and before running pandoc.

### Comparing the two adoc files

At this point, it is much easier to compare the two adoc files obtained from DocBookRx and Pandoc.
I used IntelliJ to do so but it is really up to you how you want to compare them.

### Tricks used to simplify the job

To correct the output from DocBookRx, I used some RegEx patterns in IntelliJ to replace some mistakes it makes:
```
// Looks for things like [class]`class_name`
\[{1}[a-zA-Z]+\]{1}`{2}([a-zA-Z_ \-.:]+?)`{2}
`$1`

// Looks for things like [class]_class_name_
\[{1}[a-zA-Z]+\]{1}_{1}([a-zA-Z_ \-.:]+?)_{1}
_$1_

// Looks for things like `class_name` ,
`{1}([a-zA-Z]+?)`{1} ,
`$1`,

// Looks for things like `class_name` .
`{1}([a-zA-Z]+?)`{1} \.
`$1`.

// Space before pattern
 `{1}([a-zA-Z]+?)`{1} ,
`$1`,

// Space before pattern
 `{1}([a-zA-Z]+?)`{1} \.
`$1`.
```

## Rules applied

* One sentence per line: at every full stop, new line.
* Code snippets should be loaded from files (as DocBookRx does).
* DocBookRx converts DocBook classes, interfaces, etc. with `[class/interface/etc.]''Name_Of_The_Class''`.
Using the RegEx pattern, I modified it to look like `'Name_Of_The_Class'` (where you should replace ' with `)
* I don't like the space after the class name and before the comma/full stop.
I used a RegEx pattern to correct that.
* Tables are usually formatted very badly by both software: this needs to be corrected
* I usually review the code files and try to update the formatting
* Images can be aligned to the center (IIRC `align="center"` should be added into `[]` after the filename of the image)
* DocBookRx is definitely the best of the two software: use it as the golden standard
* Skim through the content of both files (while comparing): some parts could be missed by one (or both) software

