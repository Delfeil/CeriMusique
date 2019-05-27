javac -d ./WEB-INF/classes/ ./regex/*.java ./correspondance/*.java
rm correspondance/*_.*
rm ./WEB-INF/classes/correspondance/*_.*
jar cvf correspondance.war WEB-INF/*
asadmin deploy --force correspondance.war
