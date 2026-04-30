# AutoStepper v1.7 - Par Maysson.D

**AutoStepper** est un outil Java puissant conçu pour générer automatiquement des fichiers StepMania (`.sm`) à partir de vos fichiers audio (MP3, WAV). Il analyse le rythme, détecte les battements et crée des flèches adaptées à la musique.

---

## ✨ Fonctionnalités
*   **Interface Graphique Améliorée (Nouveau)** : Design structuré, facile à lire, avec sécurité anti-erreur.
*   **Glisser-Déposer (Drag & Drop) (Nouveau)** : Glissez directement vos musiques et images dans l'application pour gagner du temps !
*   **Mémoire des Préférences (Nouveau)** : L'application se souvient de vos derniers réglages à chaque réouverture.
*   **Génération complète** : Crée tous les niveaux de difficulté (Beginner à Challenge).
*   **Analyse avancée** : Support des notes maintenues (holds), des sauts (jumps) et des mines.
*   **Artwork Automatique** : Recherche automatique de bannières et fonds sur Google Images.
*   **Images Personnalisées** : Possibilité d'uploader votre propre **Bannière** et votre propre **Arrière-plan** séparément.
*   **Métadonnées Avancées** : Ajoutez manuellement le Titre Translit, l'Artiste Translit, le Genre, etc., via le bouton d'Options Avancées.
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
