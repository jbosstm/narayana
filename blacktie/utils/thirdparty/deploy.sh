# This will fail as it conflicts with some nexus rules for jar deployment, you can't replace the artifact but you can't release a version of the jar with just a classifier and not the main jar unless it is a zip

echo "You MUST have all files ready for deploy from all BPA in order to run this script or subsequent runs will fail with artifact uniqueness constraints - if you agree enter Y"
read y
if [ $y != Y ]
then
  echo "OK - try when you have them"
  exit
fi

for i in vc9x32 centos54x64 centos55x32 fc18x64
  for j in apr-1-1.5.2.BT2 log4cxx-902683
  do
    mvn deploy:deploy-file -Durl=https://repository.jboss.org/nexus/service/local/staging/deploy/maven2/ -DrepositoryId=jboss-releases-repository -Dfile=$j-$i.jar -DpomFile=$j.pom -Dpackaging=jar -Dclassifier=$i
  done
  mvn deploy:deploy-file -Durl=https://repository.jboss.org/nexus/service/local/staging/deploy/maven2/ -DrepositoryId=jboss-releases-repository -Dfile=$j-vc9x32.jar -DpomFile=$j.pom -Dpackaging=jar -Dsources=$j-sources.jar

  for j in protoc-2.4.1.3 cppunit-1.12 expat-2.0.1 xercesc-3.0.1
  do
    mvn deploy:deploy-file -Durl=https://repository.jboss.org/nexus/service/local/staging/deploy/maven2/ -DrepositoryId=jboss-releases-repository -Dfile=$j-$i.zip -DpomFile=$j.pom -Dpackaging=zip -Dclassifier=$i -Dsources=$j-sources.zip
  done
done