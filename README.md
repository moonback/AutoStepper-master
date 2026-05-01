# AutoStepper

> Transformez votre musique en niveaux StepMania en quelques secondes.

**AutoStepper** analyse le rythme de vos fichiers audio et génère automatiquement des fichiers `.sm` prêts à jouer — bannières, difficulté, BPM variable inclus.

---

## Démarrage rapide

Double-cliquez sur `AutoStepper.jar` et suivez les 3 étapes :

1. **Choisissez votre musique** — glissez-déposez un fichier ou un dossier entier.
2. **Configurez les options** — difficulté, BPM, silence automatique.
3. **Générez** — cliquez sur **Démarrer la génération** et récupérez votre pack.

> Aucune installation requise. Java suffit.

---

## Ce qu'AutoStepper fait pour vous

| Fonctionnalité | Description |
|---|---|
| **Génération complète** | Tous les niveaux de Beginner à Challenge, en un clic |
| **BPM Variable** | Détecte les changements de tempo tout au long du morceau |
| **Mines intelligentes** | Placement basé sur l'énergie audio (FFT) |
| **Détection des silences** | Ignore les zones vides au début et à la fin |
| **Extraction de métadonnées** | Lit le titre, l'artiste et le genre de vos fichiers audio (ID3) |
| **Pack de chanson complet** | Crée un dossier prêt pour StepMania avec `.sm`, musique et images |
| **Prévisualisation audio** | Lecteur intégré avec visualisation de l'onde sonore en temps réel |
| **Images personnalisées** | Bannière et fond par morceau, avec glisser-déposer |
| **Artwork automatique** | Recherche de visuels en ligne si aucune image n'est fournie |
| **Traitement en lot** | Traitez un dossier entier d'un coup |
| **Mémoire des préférences** | Vos réglages sont sauvegardés entre les sessions |

---

## Interface graphique

L'interface est organisée en sections :

**📁 Fichiers et Dossiers**
Sélectionnez votre dossier de musique. Les morceaux détectés s'affichent automatiquement. Vous pouvez aussi glisser-déposer directement dans les champs.

**📊 Liste des musiques**
Cliquez sur un morceau pour lui assigner ses propres images. Le tableau indique l'état de personnalisation de chaque fichier.

**🖼️ Personnalisation visuelle**
Glissez vos images dans les zones Bannière et Fond. Chaque morceau peut avoir son propre pack visuel.

**⚙️ Options**
Activez ou désactivez BPM Variable, Mines via Énergie et Coupure des Silences. Réglez la difficulté (Hard Mode) et la durée d'analyse.

**Journal d'exécution**
Suivez l'avancement en bas de la fenêtre. Utilisez **Réinitialiser Tout** pour repartir de zéro.

---

## Ligne de commande

Pour les utilisateurs avancés, AutoStepper s'utilise également en terminal.

```bash
# Analyse simple
java -jar AutoStepper.jar input="ma_musique.mp3"

# Dossier complet vers StepMania
java -jar AutoStepper.jar input="./Musique/" output="C:/Games/StepMania/Songs/MonPack/"

# Mode difficile sur toute la durée
java -jar AutoStepper.jar input="techno.mp3" duration=0 hard=true

# Mise à jour d'un fichier .sm existant
java -jar AutoStepper.jar input="remix.mp3" updatesm=true
```

### Arguments disponibles

| Argument | Description |
|---|---|
| `input` | Fichier audio ou dossier de musiques |
| `output` | Dossier de destination du pack |
| `duration` | Secondes à traiter (`0` = morceau entier) |
| `hard` | `true` pour des patterns plus complexes |
| `updatesm` | `true` pour mettre à jour un `.sm` existant |
| `maxbpm` | Limite haute pour la détection du BPM (défaut : 170) |

---

## Compilation

Pour recompiler le projet :

1. Installez le **JDK (Java Development Kit)**.
2. Placez vos librairies dans le dossier `lib/`.
3. Exécutez `build.bat` à la racine du projet.
4. Le fichier `AutoStepper.jar` sera régénéré automatiquement.

---

## Dépendances

- **Minim** — Analyse audio et détection de rythme
- **Jsoup** — Recherche d'images en ligne
- **Trove** — Gestion optimisée des données

Toutes les dépendances sont incluses dans le projet.

---

## Compatibilité

- **Java 8+** requis
- Formats audio : MP3, WAV
- Formats image : PNG, JPG
- Compatible Windows, macOS, Linux

---

*Développé par Maysson.D — v1.8*
