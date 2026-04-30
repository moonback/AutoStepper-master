===========================================
DESCRIPTION DU RÉSULTAT DE LA COMPILATION
===========================================

Lorsque vous compilez un projet d'application Java possédant une classe principale, l'IDE
copie automatiquement tous les fichiers JAR du classpath du projet dans le dossier
dist/lib de votre projet. L'IDE ajoute également chaque fichier JAR à l'élément
Class-Path du fichier manifeste (MANIFEST.MF) du fichier JAR de l'application.

Pour exécuter le projet depuis la ligne de commande, allez dans le dossier dist et
tapez ce qui suit :

java -jar "AutoStepper.jar" 

Pour distribuer ce projet, compressez le dossier dist (incluant le dossier lib)
et distribuez le fichier ZIP.

Remarques :

* Si deux fichiers JAR sur le classpath du projet portent le même nom, seul le premier
fichier JAR est copié dans le dossier lib.
* Seuls les fichiers JAR sont copiés dans le dossier lib.
Si le classpath contient d'autres types de fichiers ou de dossiers, ces fichiers (dossiers)
ne sont pas copiés.
* Si une bibliothèque sur le classpath du projet possède également un élément Class-Path
spécifié dans son manifeste, le contenu de cet élément Class-Path doit se trouver sur
le chemin d'exécution (runtime path) du projet.
* Pour définir une classe principale dans un projet Java standard, faites un clic droit sur le nœud
du projet dans la fenêtre Projets et choisissez Propriétés. Cliquez ensuite sur Exécuter et saisissez
le nom de la classe dans le champ Classe Principale. Vous pouvez également saisir manuellement le
nom de la classe dans l'élément Main-Class du manifeste.
