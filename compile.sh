# Remove the last compilation and documentation
echo 'Remove the last release...'
rm -r compiled/classes/*
rm -r release/doc/*
rm -r release/*.jar
echo 'Done !'
# Library compilation
echo 'Compile the library...'
#javac -cp compiled/classes/:../3plib-download/3plib-0.1.0.jar -d compiled/classes src/fr/imag/spaceex/*.java -Xlint:unchecked
javac -cp compiled/classes/:../3plib-download/3plib-0.1.0.jar -d compiled/classes src/fr/imag/spaceex/*.java -Xlint:unchecked
echo 'Done !'
# JAR generation
echo 'JAR generation...'
jar cf release/spaceex-adapter.jar -C compiled/classes/ .
echo 'Done !'
