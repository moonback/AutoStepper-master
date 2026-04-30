# AutoStepper v1.7 - Par Maysson.D

**AutoStepper** est un outil Java puissant conçu pour générer automatiquement des fichiers StepMania (`.sm`) à partir de vos fichiers audio (MP3, WAV). Il analyse le rythme, détecte les battements et crée des flèches adaptées à la musique.

---

## ✨ Fonctionnalités
*   **Interface Moderne (Dark Mode) (Nouveau)** : Design sombre et élégant pour une meilleure lisibilité.
*   **Glisser-Déposer (Drag & Drop)** : Glissez directement vos musiques et images dans l'application.
*   **Extraction de Métadonnées (ID3) (Nouveau)** : AutoStepper lit automatiquement le titre, l'artiste et le genre de vos fichiers audio !
*   **Pack de Chanson Complet (Nouveau)** : Crée automatiquement un dossier prêt pour StepMania contenant le fichier `.sm`, la musique et les images.
*   **Mémoire des Préférences** : L'application se souvient de vos derniers réglages.
*   **Génération complète** : Crée tous les niveaux de difficulté (Beginner à Challenge).
*   **Analyse avancée** : Support des notes maintenues (holds), des sauts (jumps) et des mines.
*   **Artwork Automatique** : Recherche automatique de bannières et fonds sur Google Images.
*   **Images Personnalisées** : Possibilité d'utiliser votre propre bannière et arrière-plan.
*   **Multi-fichiers** : Traitez une chanson seule ou tout un dossier d'un coup.

---

## 🚀 Utilisation

### 🖥️ Mode Interface Graphique (Recommandé)
Double-cliquez simplement sur le fichier **`AutoStepper.jar`**. L'interface est divisée en sections claires :
1. **📁 Fichiers et Dossiers** : 
   * Sélectionnez votre fichier/dossier de musique et le dossier de sortie.
   * *Astuce : Vous pouvez **glisser-déposer (Drag & Drop)** directement vos fichiers depuis l'explorateur Windows dans ces champs !*
2. **🖼️ Personnalisation Visuelle** : 
   * (Optionnel) Glissez-déposez une image pour la bannière et une image pour le fond du jeu.
3. **⚙️ Options de Génération** : 
   * Réglez la durée. **Laissez à `0` pour générer les steps sur toute la longueur de la musique.**
   * Choisissez le mode difficile, ou ouvrez les **Options Avancées** pour renseigner les balises détaillées du fichier SM.
4. Cliquez sur **DÉMARRER LA GÉNÉRATION DES STEPS** et suivez la progression dans le journal d'exécution. Le bouton sera grisé tant qu'aucune musique n'est renseignée.

### ⌨️ Mode Ligne de commande
Pour les utilisateurs avancés, vous pouvez utiliser l'application via un terminal. Voici quelques exemples courants :

**1. Analyse simple d'une chanson :**
```bash
java -jar AutoStepper.jar input="ma_musique.mp3"
```

**2. Traiter tout un dossier et envoyer vers StepMania :**
```bash
java -jar AutoStepper.jar input="./Musique/" output="C:/Games/StepMania/Songs/MonPack/"
```

**3. Mode difficile sur toute la durée du morceau :**
```bash
java -jar AutoStepper.jar input="techno.mp3" duration=0 hard=true
```

**4. Mettre à jour les steps d'un fichier .sm existant :**
```bash
java -jar AutoStepper.jar input="remix.mp3" updatesm=true
```

**Arguments disponibles :**
- `input` : Fichier audio ou dossier contenant des musiques.
- `output` : Dossier où le pack de chanson sera créé.
- `duration` : Secondes à traiter (utilisez `0` pour toute la chanson).
- `hard` : `true` pour générer des patterns plus complexes (plus de flèches).
- `updatesm` : `true` pour mettre à jour un fichier `.sm` existant au lieu d'en créer un nouveau.
- `maxbpm` : Limite haute pour la détection du BPM (défaut : 170).

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
