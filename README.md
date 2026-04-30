AutoStepper est un programme console Java conçu pour créer automatiquement des fichiers StepMania SM avec les fonctionnalités suivantes :

*   Générer tous les niveaux de difficulté
*   Générer les "holds" (notes maintenues) et les "jumps" (sauts)
*   Obtenir les bannières et les images de fond (artwork)
*   S'exécuter localement sans interaction
*   Traiter plusieurs fichiers musicaux à la fois
*   Plusieurs méthodes de détection du tempo (BPM)
*   Support multiplateforme

Voici donc **AutoStepper** par Maysson.D. Vous pouvez obtenir le programme complet dans le dossier "dist".

Il fonctionne en ligne de commande avec des arguments, tous optionnels. Si vous lancez simplement le programme Java, il scannera et traitera tous les fichiers mp3 (et wav) du répertoire courant, et créera des dossiers pour chaque chanson dans le même répertoire (avec 90 secondes de "steps").

Les arguments sont :

    input=[fichier/répertoire] output=[répertoire des chansons] duration=[secondes à traiter] tap=[true/false] tapsync=[décalage en secondes pour le tap, par défaut : -0.11] hard=[true/false] updatesm=[true/false]

Exemple :

    java -jar AutoStepper.jar input="./songs/" duration=130 hard=true

Si vous réglez `tap=true`, AutoStepper n'essaiera pas de calculer automatiquement le BPM ou le décalage (offset), mais vous demandera d'appuyer sur ENTRÉE sur 30 battements consécutifs. AutoStepper fera ensuite le reste.

Il est préférable de laisser AutoStepper traiter un grand nombre de musiques, puis d'extraire celles qu'il n'a pas réussi à traiter parfaitement pour utiliser `tap=true` sur elles.

`updatesm=true` recherchera les fichiers .sm existants utilisant les mêmes noms de fichiers générés par AutoStepper. S'ils sont trouvés, il récupérera le décalage et le BPM de ces fichiers et mettra simplement à jour les steps. C'est utile pour mettre à jour des steps générés avec d'anciennes versions d'AutoStepper, ou pour changer l'argument "hard" sans avoir à recalculer le BPM ou les temps de décalage.

Vous pouvez également utiliser le résultat comme base pour éditer et perfectionner les chansons, AutoStepper se chargeant de la plus grande partie du travail fastidieux.

Je précise qu'il est optimisé pour une utilisation sur tapis (pad), et non pour le clavier (la difficulté n'est pas assez élevée, par exemple).

** LICENCE : Licence MIT modifiée pour restreindre l'utilisation commerciale et exiger l'attribution **

Copyright (c) 2018 Maysson.D

L'autorisation est accordée par la présente, gratuitement, à toute personne obtenant une copie de ce logiciel et des fichiers de documentation associés (le "Logiciel"), de l'utiliser sous réserve de restrictions d'utilisation commerciale et d'une obligation d'attribution à Maysson.D. Vous êtes libre d'utiliser, copier, modifier, fusionner, publier, distribuer ce Logiciel pour des usages privés, personnels et non commerciaux tant que Maysson.D est crédité.

L'avis de copyright ci-dessus et cet avis d'autorisation doivent être inclus dans toutes les copies ou parties substantielles du Logiciel.

LE LOGICIEL EST FOURNI "EN L'ÉTAT", SANS GARANTIE D'AUCUNE SORTE, EXPRESSE OU IMPLICITE, Y COMPRIS, MAIS SANS S'Y LIMITER, LES GARANTIES DE QUALITÉ MARCHANDE, D'ADÉQUATION À UN USAGE PARTICULIER ET D'ABSENCE DE CONTREFAÇON. EN AUCUN CAS LES AUTEURS OU LES TITULAIRES DU DROIT D'AUTEUR NE POURRONT ÊTRE TENUS RESPONSABLES DE TOUTE RÉCLAMATION, DOMMAGE OU AUTRE RESPONSABILITÉ, QUE CE SOIT DANS LE CADRE D'UN CONTRAT, D'UN DÉLIT OU AUTRE, DÉCOULANT DE, OU EN RELATION AVEC LE LOGICIEL OU L'UTILISATION OU D'AUTRES RAPPORTS DANS LE LOGICIEL.
