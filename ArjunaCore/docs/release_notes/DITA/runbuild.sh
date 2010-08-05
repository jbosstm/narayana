#!/bin/bash

# Debugging - uncomment to turn on debugging
# echo='/bin/echo'

# Generic build script for DITA OpenToolkit

# Set up the environment

# You can put all of this in ~/.bashrc instead, but it might
# conflict with other Ant configurations
# Assumes the DITA-OT is extracted to ~/bin
export DITA_DIR=~/bin/DITA-OT1.5.2
export DITA_HOME=$DITA_DIR
export PATH=$DITA_HOME:$PATH

export ANT_OPTS="-Xmx512m $ANT_OPTS"
export ANT_OPTS="$ANT_OPTS -Djavax.xml.transform.TransformerFactory=net.sf.saxon.TransformerFactoryImpl"
export ANT_HOME="$DITA_DIR"/tools/ant
export PATH="$DITA_DIR"/tools/ant/bin:"$PATH"

NEW_CLASSPATH="$DITA_DIR/lib:$DITA_DIR/lib/dost.jar:$DITA_DIR/lib/resolver.jar:$DITA_DIR/lib/icu4j.jar"
NEW_CLASSPATH="$DITA_DIR/lib/saxon/saxon9.jar:$DITA_DIR/lib/saxon/saxon9-dom.jar:$NEW_CLASSPATH"
NEW_CLASSPATH="$DITA_DIR/lib/saxon/saxon9-dom4j.jar:$DITA_DIR/lib/saxon/saxon9-jdom.jar:$NEW_CLASSPATH"
NEW_CLASSPATH="$DITA_DIR/lib/saxon/saxon9-s9api.jar:$DITA_DIR/lib/saxon/saxon9-sql.jar:$NEW_CLASSPATH"
NEW_CLASSPATH="$DITA_DIR/lib/saxon/saxon9-xom.jar:$DITA_DIR/lib/saxon/saxon9-xpath.jar:$DITA_DIR/lib/saxon/saxon9-xqj.jar:$NEW_CLASSPATH"
if test -n "$CLASSPATH"
then
    export CLASSPATH="$NEW_CLASSPATH":"$CLASSPATH"
else
    export CLASSPATH="$NEW_CLASSPATH"
fi


    # The first argument is hierarchy, sequence, or all
case $1 in
    hierarchy)
	    $echo ant -f ant_scripts/hierarchy_all.xml $2
	    ;;
    sequence)
	    $echo ant -f ant_scripts/sequence_all.xml $2
	    ;;
    all)
	    $echo ant -f ant_scripts/hierarchy_all.xml $2
	    $echo ant -f ant_scripts/sequence_all.xml $2
	    ;;
    clean)
	    # Deletes everything in output directory
	    read -p "This will delete all of your output. Are you sure?" yn
	    case $yn in
		[Yy]* ) rm -rf output/*; echo "Done";;
		[Nn]* ) echo "Not deleting!";;
		* ) echo "Please answer yes or no.";;
	    esac
	    ;;
    *)
	    echo "Usage: $0 <hierarchy|sequence|all|clean> [target]"
	    echo "Example: $0 hierarchy dita2pdf2"
	    echo "Debugging is available by editing the script."
	    exit
	    ;;
esac

    # Do the stuff


