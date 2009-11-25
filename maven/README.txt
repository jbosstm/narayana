
We don't actually use maven for building the project. However, the artifacts we produce should be available
for consumption by maven based projects. Therefore, we use ../build-release-pkgs.xml to drive mvn deploy-file
to upload them to the jboss repository. The pom in this file is a template processed by build-release-pkgs.xml
and should not be called directly from maven.
