if [ "${ACE_VER}" == "" ]; then
    echo ACE_VER not set
    exit -1
fi

if [ "${bpa}" == "" ]; then
    echo bpa not set
    exit -1
fi

if [ "$1" = "build" ]; then
shift
echo "Making ACE+TAO-$ACE_VER"
rm -rf ACE+TAO-$ACE_VER
mkdir ACE+TAO-$ACE_VER
if [ -e ACE+TAO-$ACE_VER.zip ]; then
	echo "Already downloaded"
else
	wget http://download.dre.vanderbilt.edu/previous_versions/ACE+TAO-$ACE_VER.zip
    if [ "$?" != 0 ]; then
       echo could not download
       exit -1
    fi
fi
(cd ACE+TAO-$ACE_VER && unzip ../ACE+TAO-$ACE_VER.zip)
export ACE_ROOT=`pwd`/ACE+TAO-$ACE_VER/ACE_wrappers
export TAO_ROOT=$ACE_ROOT/TAO
export LD_LIBRARY_PATH=$ACE_ROOT/lib

dos2unix $ACE_ROOT/bin/*.sh
echo "#define ACE_INITIALIZE_MEMORY_BEFORE_USE 1
#include \"ace/config-linux.h\"" > $ACE_ROOT/ace/config.h

echo "include \$(ACE_ROOT)/include/makeinclude/platform_linux.GNU" > $ACE_ROOT/include/makeinclude/platform_macros.GNU

for i in $ACE_ROOT/ace $ACE_ROOT/apps/gperf/src $ACE_ROOT/ACEXML $TAO_ROOT/TAO_IDL $TAO_ROOT/tao
do
(cd $i && make)
done

(cd  $TAO_ROOT/orbsvcs/orbsvcs && make CosNaming_Serv)

ant -Dlinux=true ace
fi


if [ "$1" = "deploy" ]; then
mvn deploy:deploy-file $SETTINGS_FILE -Durl=https://repository.jboss.org/nexus/service/local/staging/deploy/maven2/ -DrepositoryId=jboss-releases-repository -Dfile=`pwd`/build/lib/ace-$ACE_VER-$bpa.zip -DpomFile=`pwd`/poms/ace-$ACE_VER.pom -Dpackaging=zip -Dclassifier=$bpa
fi
