### Run Scripts

// AJOUT FILE
javac -cp .\core\lucene-core-8.8.1.jar;.\analysis\common\lucene-analyzers-common-8.8.1.jar;.\queryparser\lucene-queryparser-8.8.1.jar ajoutfile.java
java -cp .\core\lucene-core-8.8.1.jar;.\analysis\common\lucene-analyzers-common-8.8.1.jar;.\queryparser\lucene-queryparser-8.8.1.jar;. ajoutfile


// RECHERCHE FILE
javac -cp .\core\lucene-core-8.8.1.jar;.\analysis\common\lucene-analyzers-common-8.8.1.jar;.\queryparser\lucene-queryparser-8.8.1.jar recherchefile.java
java -cp .\core\lucene-core-8.8.1.jar;.\analysis\common\lucene-analyzers-common-8.8.1.jar;.\queryparser\lucene-queryparser-8.8.1.jar;. recherchefile
