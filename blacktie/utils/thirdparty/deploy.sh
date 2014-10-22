export bpa=centos70x64

# This will fail as it conflicts with some nexus rules for jar deployment, you can't replace the artifact but you can't release a version of the jar with just a classifier and not the main jar unless it is a zip
for i in apr-1-1.3.3 log4cxx-902683
do
mvn deploy:deploy-file -Durl=https://repository.jboss.org/nexus/service/local/staging/deploy/maven2/ -DrepositoryId=jboss-releases-repository -Dfile=`pwd`/$bpa/$i-$bpa.jar -DpomFile=`pwd`/poms/$i.pom -Dpackaging=jar -Dclassifier=$bpa -Dsources=`pwd`/$bpa/$i-$bpa.jar
mvn deploy:deploy-file -Durl=https://repository.jboss.org/nexus/service/local/staging/deploy/maven2/ -DrepositoryId=jboss-releases-repository -Dfile=`pwd`/$bpa/$i-$bpa.jar -DpomFile=`pwd`/poms/$i.pom -Dpackaging=jar
done

for i in protoc-2.4.1.3 cppunit-1.12 expat-2.0.1 xercesc-3.0.1
do
mvn deploy:deploy-file -Durl=https://repository.jboss.org/nexus/service/local/staging/deploy/maven2/ -DrepositoryId=jboss-releases-repository -Dfile=`pwd`/$bpa/$i-$bpa.zip -DpomFile=`pwd`/poms/$i.pom -Dpackaging=zip -Dclassifier=$bpa -Dsources=`pwd`/$bpa/$i-$bpa.zip
done