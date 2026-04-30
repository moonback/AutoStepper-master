# AutoStepper v1.7 - Par Maysson.D

**AutoStepper** est un outil Java puissant conçu pour générer automatiquement des fichiers StepMania (`.sm`) à partir de vos fichiers audio (MP3, WAV). Il analyse le rythme, détecte les battements et crée des flèches adaptées à la musique.

---

## ✨ Fonctionnalités
*   **Interface Graphique (Nouveau)** : Plus besoin de taper des commandes, utilisez la fenêtre conviviale !
*   **Génération complète** : Crée tous les niveaux de difficulté (Beginner à Challenge).
*   **Analyse avancée** : Support des notes maintenues (holds), des sauts (jumps) et des mines.
*   **Artwork Automatique** : Recherche automatique de bannières et fonds sur Google Images.
*   **Image Personnalisée (Nouveau)** : Possibilité d'uploader votre propre image pour le fond.
*   **Mode Tap Manuel** : Calculez le BPM vous-même en tapant sur votre clavier si l'analyse automatique échoue.
*   **Multi-fichiers** : Traitez une chanson seule ou tout un dossier d'un coup.

---

## 🚀 Utilisation

### 🖥️ Mode Interface Graphique (Recommandé)
Double-cliquez simplement sur le fichier **`AutoStepper.jar`**.
1. Sélectionnez votre fichier ou dossier de musique.
2. Choisissez le dossier où vous voulez enregistrer les résultats.
3. (Optionnel) Sélectionnez une image personnalisée pour la bannière.
4. Cliquez sur **DÉMARRER LA GÉNÉRATION**.

### ⌨️ Mode Ligne de commande
Pour les utilisateurs avancés, vous pouvez toujours utiliser les arguments :
```bash
java -jar AutoStepper.jar input="ma_musique.mp3" duration=90 hard=true output="./ma_sortie/"
```
**Arguments disponibles :**
- `input` : Fichier ou dossier source.
- `output` : Dossier de destination.
- `duration` : Secondes à traiter (défaut : 90s).
- `hard` : `true` pour ajouter plus de flèches.
- `tap` : `true` pour synchroniser manuellement le rythme.

---

## 🛠️ Compilation et Développement
Si vous souhaitez modifier le code source ou recompiler le projet :

1. Assurez-vous d'avoir le **JDK (Java Development Kit)** installé.
2. Placez vos librairies dans le dossier `lib/`.
3. Lancez le script **`build.bat`** à la racine du projet.
4. Le nouveau fichier `AutoStepper.jar` sera généré automatiquement.

---

## 📦 Dépendances
AutoStepper utilise les bibliothèques suivantes (incluses dans le projet) :
*   **Minim** : Pour l'analyse audio et la détection de rythme.
*   **Jsoup** : Pour la recherche d'images.
*   **Trove** : Pour la gestion optimisée des données.

---
*Développé à l'origine par Phr00t, amélioré et traduit par Maysson.D.*
