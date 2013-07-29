Convert the raw asciidoc text into docbook format:

        asciidoc -b docbook RESTAT_Guide.txt

which will produce a RESTAT\_Guide.xml, edit this file to replace the
contents of the  <author> section with the contents of Author\_Group.xml
(TODO figure out how to include the authorgroup in asciidoc)
