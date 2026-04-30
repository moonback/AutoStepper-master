package autostepper;

import ddf.minim.AudioMetaData;
import ddf.minim.AudioSample;
import ddf.minim.Minim;
import ddf.minim.MultiChannelBuffer;
import ddf.minim.analysis.BeatDetect;
import ddf.minim.analysis.FFT;
import ddf.minim.spi.AudioRecordingStream;
import gnu.trove.list.array.TFloatArrayList;
import java.io.*;
import java.util.ArrayList;

/**
 *
 * @author Phr00t
 */
public class AutoStepper {

    public static boolean DEBUG_STEPS = false;
    public static float MAX_BPM = 170f, MIN_BPM = 70f, BPM_SENSITIVITY = 0.05f, STARTSYNC = 0.0f;
    public static double TAPSYNC = -0.11;
    public static boolean HARDMODE = false, UPDATESM = false;
    public static String customImagePath = null;
    public static String customBackgroundPath = null;
    public static String songTitle = "";
    public static String songArtist = "";
    public static String titleTranslit = "";
    public static String subTitleTranslit = "";
    public static String artistTranslit = "";
    public static String genre = "";

    public static Minim minim;
    public static AutoStepper myAS = new AutoStepper();

    public static void loadMetadata(String filename) {
        songTitle = "";
        songArtist = "";
        genre = "";
        if (minim == null) minim = new Minim(myAS);
        AudioSample sample = minim.loadSample(filename, 512);
        if (sample != null) {
            AudioMetaData meta = sample.getMetaData();
            if (meta != null) {
                if (!meta.title().trim().isEmpty()) songTitle = meta.title();
                if (!meta.author().trim().isEmpty()) songArtist = meta.author();
                if (!meta.genre().trim().isEmpty()) genre = meta.genre();
            }
            sample.close();
        }
    }

    public static final int KICKS = 0, ENERGY = 1, SNARE = 2, HAT = 3;

    // données collectées de la chanson
    private final TFloatArrayList[] manyTimes = new TFloatArrayList[4];
    private final TFloatArrayList[] fewTimes = new TFloatArrayList[4];

    // pour minim
    public String sketchPath(String fileName) {
        return fileName;
    }

    // pour minim
    public InputStream createInput(String fileName) {
        try {
            return new FileInputStream(new File(fileName));
        } catch (Exception e) {
            return null;
        }
    }

    // analyseur d'arguments
    public static String getArg(String[] args, String argname, String def) {
        try {
            for (String s : args) {
                s = s.replace("\"", "");
                if (s.startsWith(argname)) {
                    return s.substring(s.indexOf("=") + 1).toLowerCase();
                }
            }
        } catch (Exception e) {
        }
        return def;
    }

    // analyseur d'arguments
    public static boolean hasArg(String[] args, String argname) {
        for (String s : args) {
            if (s.toLowerCase().equals(argname))
                return true;
        }
        return false;
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            AutoStepperGUI.launch();
            return;
        }
        minim = new Minim(myAS);
        String outputDir, input;
        float duration;
        System.out.println("Début du programme...");
        if (hasArg(args, "help") || hasArg(args, "h") || hasArg(args, "?") || hasArg(args, "-help")
                || hasArg(args, "-?") || hasArg(args, "-h")) {
            System.out.println("Usage des arguments (tous les champs sont optionnels) :\n"
                    + "input=<fichier ou dossier> output=<dossier des chansons> duration=<secondes à traiter, par défaut : 90> tap=<true/false> tapsync=<décalage de temps du tap, par défaut : -0.11> hard=<true/false> updatesm=<true/false>");
            return;
        }
        MAX_BPM = Float.parseFloat(getArg(args, "maxbpm", "170f"));
        outputDir = getArg(args, "output", ".");
        if (outputDir.endsWith("/") == false)
            outputDir += "/";
        input = getArg(args, "input", ".");
        duration = Float.parseFloat(getArg(args, "duration", "90"));
        STARTSYNC = Float.parseFloat(getArg(args, "synctime", "0.0"));
        BPM_SENSITIVITY = Float.parseFloat(getArg(args, "bpmsensitivity", "0.05"));
        HARDMODE = getArg(args, "hard", "false").equals("true");
        UPDATESM = getArg(args, "updatesm", "false").equals("true");
        File inputFile = new File(input);
        if (inputFile.isFile()) {
            myAS.analyzeUsingAudioRecordingStream(inputFile, duration, outputDir);
        } else if (inputFile.isDirectory()) {
            System.out.println("Traitement du répertoire : " + inputFile.getAbsolutePath());
            File[] allfiles = inputFile.listFiles();
            for (File f : allfiles) {
                String extCheck = f.getName().toLowerCase();
                if (f.isFile() &&
                        (extCheck.endsWith(".mp3") || extCheck.endsWith(".wav"))) {
                    myAS.analyzeUsingAudioRecordingStream(f, duration, outputDir);
                } else {
                    System.out.println("Fichier non supporté ignoré : " + f.getName());
                }
            }
        } else {
            System.out.println("Impossible de trouver des fichiers d'entrée.");
        }
    }

    TFloatArrayList calculateDifferences(TFloatArrayList arr, float timeThreshold) {
        TFloatArrayList diff = new TFloatArrayList();
        int currentlyAt = 0;
        while (currentlyAt < arr.size() - 1) {
            float mytime = arr.getQuick(currentlyAt);
            int oldcurrentlyat = currentlyAt;
            for (int i = currentlyAt + 1; i < arr.size(); i++) {
                float diffcheck = arr.getQuick(i) - mytime;
                if (diffcheck >= timeThreshold) {
                    diff.add(diffcheck);
                    currentlyAt = i;
                    break;
                }
            }
            if (oldcurrentlyat == currentlyAt)
                break;
        }
        return diff;
    }

    float getDifferenceAverage(TFloatArrayList arr) {
        float avg = 0f;
        for (int i = 0; i < arr.size() - 1; i++) {
            avg += Math.abs(arr.getQuick(i + 1) - arr.getQuick(i));
        }
        if (arr.size() <= 1)
            return 0f;
        return avg / arr.size() - 1;
    }

    float getMostCommon(TFloatArrayList arr, float threshold, boolean closestToInteger) {
        ArrayList<TFloatArrayList> values = new ArrayList<>();
        for (int i = 0; i < arr.size(); i++) {
            float val = arr.get(i);
            // vérifier cette valeur dans nos listes actuelles
            boolean notFound = true;
            for (int j = 0; j < values.size(); j++) {
                TFloatArrayList tal = values.get(j);
                for (int k = 0; k < tal.size(); k++) {
                    float listValue = tal.get(k);
                    if (Math.abs(listValue - val) < threshold) {
                        notFound = false;
                        tal.add(val);
                        break;
                    }
                }
                if (notFound == false)
                    break;
            }
            // si elle n'a pas été trouvée, commencer une nouvelle liste
            if (notFound) {
                TFloatArrayList newList = new TFloatArrayList();
                newList.add(val);
                values.add(newList);
            }
        }
        // obtenir la liste la plus longue
        int longest = 0;
        TFloatArrayList longestList = null;
        for (int i = 0; i < values.size(); i++) {
            TFloatArrayList check = values.get(i);
            if (check.size() > longest ||
                    check.size() == longest && getDifferenceAverage(check) < getDifferenceAverage(longestList)) {
                longest = check.size();
                longestList = check;
            }
        }
        if (longestList == null)
            return -1f;
        if (longestList.size() == 1 && values.size() > 1) {
            // une seule valeur, pas besoin de moyenne... mais quoi choisir ?
            // on prend juste la plus petite... ou l'entier, si on veut cela à la place
            if (closestToInteger) {
                float closestIntDiff = 1f;
                float result = arr.getQuick(0);
                for (int i = 0; i < arr.size(); i++) {
                    float diff = Math.abs(Math.round(arr.getQuick(i)) - arr.getQuick(i));
                    if (diff < closestIntDiff) {
                        closestIntDiff = diff;
                        result = arr.getQuick(i);
                    }
                }
                return result;
            } else {
                float smallest = 99999f;
                for (int i = 0; i < arr.size(); i++) {
                    if (arr.getQuick(i) < smallest)
                        smallest = arr.getQuick(i);
                }
                return smallest;
            }
        }
        // calculer la moyenne
        float avg = 0f;
        for (int i = 0; i < longestList.size(); i++) {
            avg += longestList.get(i);
        }
        return avg / longestList.size();
    }

    public float getBestOffset(float timePerBeat, TFloatArrayList times, float groupBy) {
        TFloatArrayList offsets = new TFloatArrayList();
        for (int i = 0; i < times.size(); i++) {
            offsets.add(times.getQuick(i) % timePerBeat);
        }
        return getMostCommon(offsets, groupBy, false);
    }

    public void AddCommonBPMs(TFloatArrayList common, TFloatArrayList times, float doubleSpeed, float timePerSample) {
        float commonBPM = 60f / getMostCommon(calculateDifferences(times, doubleSpeed), timePerSample, true);
        if (commonBPM > MAX_BPM) {
            common.add(commonBPM * 0.5f);
        } else if (commonBPM < MIN_BPM / 2f) {
            common.add(commonBPM * 4f);
        } else if (commonBPM < MIN_BPM) {
            common.add(commonBPM * 2f);
        } else
            common.add(commonBPM);
    }

    void analyzeUsingAudioRecordingStream(File filename, float seconds, String outputDir) {
        int fftSize = 512;

        System.out.println("\n[--- Traitement de " + seconds + "s de " + filename.getName() + " ---]");
        AudioRecordingStream stream = minim.loadFileStream(filename.getAbsolutePath(), fftSize, false);

        // on dit de "jouer" pour pouvoir lire le flux.
        stream.play();

        // création des objets fft/beatdetect pour l'analyse
        BeatDetect manybd = new BeatDetect(BeatDetect.FREQ_ENERGY, fftSize, stream.getFormat().getSampleRate());
        BeatDetect fewbd = new BeatDetect(BeatDetect.FREQ_ENERGY, fftSize, stream.getFormat().getSampleRate());
        BeatDetect manybde = new BeatDetect(BeatDetect.SOUND_ENERGY, fftSize, stream.getFormat().getSampleRate());
        BeatDetect fewbde = new BeatDetect(BeatDetect.SOUND_ENERGY, fftSize, stream.getFormat().getSampleRate());
        manybd.setSensitivity(BPM_SENSITIVITY);
        manybde.setSensitivity(BPM_SENSITIVITY);
        fewbd.setSensitivity(60f / MAX_BPM);
        fewbde.setSensitivity(60f / MAX_BPM);

        FFT fft = new FFT(fftSize, stream.getFormat().getSampleRate());

        // création du tampon pour lire le flux
        MultiChannelBuffer buffer = new MultiChannelBuffer(fftSize, stream.getFormat().getChannels());

        // calcul du nombre d'échantillons dans le flux
        float songTime = stream.getMillisecondLength() / 1000f;
        if (seconds <= 0 || seconds > songTime) {
            seconds = songTime;
        }
        int totalSamples = (int) (songTime * stream.getFormat().getSampleRate());
        float timePerSample = fftSize / stream.getFormat().getSampleRate();

        // analyse des échantillons par blocs
        int totalChunks = (totalSamples / fftSize) + 1;

        System.out.println("Détection des battements en cours...");
        for (int i = 0; i < fewTimes.length; i++) {
            if (fewTimes[i] == null)
                fewTimes[i] = new TFloatArrayList();
            if (manyTimes[i] == null)
                manyTimes[i] = new TFloatArrayList();
            fewTimes[i].clear();
            manyTimes[i].clear();
        }
        TFloatArrayList MidFFTAmount = new TFloatArrayList(), MidFFTMaxes = new TFloatArrayList();
        float largestAvg = 0f, largestMax = 0f;
        int lowFreq = fft.freqToIndex(300f);
        int highFreq = fft.freqToIndex(3000f);
        for (int chunkIdx = 0; chunkIdx < totalChunks; ++chunkIdx) {
            stream.read(buffer);
            float[] data = buffer.getChannel(0);
            float time = chunkIdx * timePerSample;
            // analyse du canal gauche
            manybd.detect(data, time);
            manybde.detect(data, time);
            fewbd.detect(data, time);
            fewbde.detect(data, time);
            fft.forward(data);
            // traitement fft
            float avg = fft.calcAvg(300f, 3000f);
            float max = 0f;
            for (int b = lowFreq; b <= highFreq; b++) {
                float bandamp = fft.getBand(b);
                if (bandamp > max)
                    max = bandamp;
            }
            if (max > largestMax)
                largestMax = max;
            if (avg > largestAvg)
                largestAvg = avg;
            MidFFTAmount.add(avg);
            MidFFTMaxes.add(max);
            // stockage des temps de percussion de base
            if (manybd.isKick())
                manyTimes[KICKS].add(time);
            if (manybd.isHat())
                manyTimes[HAT].add(time);
            if (manybd.isSnare())
                manyTimes[SNARE].add(time);
            if (manybde.isOnset())
                manyTimes[ENERGY].add(time);
            if (fewbd.isKick())
                fewTimes[KICKS].add(time);
            if (fewbd.isHat())
                fewTimes[HAT].add(time);
            if (fewbd.isSnare())
                fewTimes[SNARE].add(time);
            if (fewbde.isOnset())
                fewTimes[ENERGY].add(time);
        }
        System.out.println("Moyenne des médiums la plus forte pour normaliser à 1 : " + largestAvg);
        System.out.println("Maximum des médiums le plus fort pour normaliser à 1 : " + largestMax);
        float scaleBy = 1f / largestAvg;
        float scaleMaxBy = 1f / largestMax;
        for (int i = 0; i < MidFFTAmount.size(); i++) {
            MidFFTAmount.replace(i, MidFFTAmount.get(i) * scaleBy);
            MidFFTMaxes.replace(i, MidFFTMaxes.get(i) * scaleMaxBy);
        }

        // calculer les différences entre les éléments percussifs avec pondération (IA Smart BPM)
        TFloatArrayList common = new TFloatArrayList();
        float doubleSpeed = 60f / (MAX_BPM * 2f);
        
        for (int i = 0; i < fewTimes.length; i++) {
            int weight = 1;
            if (i == KICKS) weight = 4;   // Les basses sont les plus fiables pour le BPM
            if (i == ENERGY) weight = 2;  // L'énergie globale est un bon indicateur secondaire
            
            for (int w = 0; w < weight; w++) {
                AddCommonBPMs(common, fewTimes[i], doubleSpeed, timePerSample * 1.5f);
                AddCommonBPMs(common, manyTimes[i], doubleSpeed, timePerSample * 1.5f);
            }
        }
        float BPM = 0f, startTime = 0f, timePerBeat = 0f;
        if (UPDATESM) {
            File smfile = SMGenerator.getSMFile(filename, outputDir);
            if (smfile.exists()) {
                try (BufferedReader br = new BufferedReader(new FileReader(smfile))) {
                    while (br.ready() && (BPM == 0f || startTime == 0f)) {
                        String line = br.readLine();
                        if (line.contains("#OFFSET:")) {
                            int off = line.indexOf("#OFFSET:") + 8;
                            int end = line.indexOf(";", off);
                            startTime = Float.parseFloat(line.substring(off, end));
                            System.out.println("Heure de début du fichier SM : " + startTime);
                        }
                        if (line.contains("#BPMS:")) {
                            int off = line.indexOf("#BPMS:");
                            off = line.indexOf("=", off) + 1;
                            int end = line.indexOf(";", off);
                            BPM = Float.parseFloat(line.substring(off, end));
                            System.out.println("BPM du fichier SM : " + BPM);
                        }
                    }
                    timePerBeat = 60f / BPM;
                } catch (Exception e) {
                }
            } else {
                System.out.println("Fichier SM à mettre à jour introuvable : " + smfile.getAbsolutePath());
            }
        }
        if (BPM == 0f) {
            if (common.isEmpty()) {
                System.out.println("[--- ÉCHEC : IMPOSSIBLE DE CALCULER LE BPM ---]");
                return;
            }
            BPM = Math.round(getMostCommon(common, 0.5f, true));
            
            // Calcul de la confiance (BPM Confidence)
            int matches = 0;
            for (int i = 0; i < common.size(); i++) {
                if (Math.abs(common.get(i) - BPM) < 1.0f) matches++;
            }
            float confidence = (float) matches / common.size() * 100f;

            timePerBeat = 60f / BPM;
            TFloatArrayList startTimes = new TFloatArrayList();
            for (int i = 0; i < fewTimes.length; i++) {
                startTimes.add(getBestOffset(timePerBeat, fewTimes[i], 0.01f));
                startTimes.add(getBestOffset(timePerBeat, manyTimes[i], 0.01f));
            }
            // donner un poids supplémentaire aux Kicks (fewKicks)
            float kickStartTime = getBestOffset(timePerBeat, fewTimes[KICKS], 0.01f);
            startTimes.add(kickStartTime);
            startTimes.add(kickStartTime);
            startTime = -getMostCommon(startTimes, 0.02f, false);
            
            System.out.println(" > Indice de Confiance BPM : " + String.format("%.1f", confidence) + "%");
        }
        System.out.println("Temps par battement : " + timePerBeat + ", BPM : " + BPM);
        System.out.println("Heure de début : " + startTime);

        // --- IA Smart Difficulty Suggestion ---
        float avgEnergy = 0f;
        for (int i = 0; i < fewTimes.length; i++) {
            avgEnergy += fewTimes[i].size();
        }
        avgEnergy /= (seconds > 0 ? seconds : 180f); 

        int suggestedRating = Math.round(BPM / 18f) + Math.min(6, Math.round(avgEnergy / 15f));
        if (HARDMODE) suggestedRating += 2;
        suggestedRating = Math.max(1, Math.min(15, suggestedRating));

        System.out.println("\n[🤖 IA ANALYSE]");
        System.out.println(" > BPM Détecté : " + BPM);
        System.out.println(" > Énergie Rythmique : " + String.format("%.2f", avgEnergy));
        System.out.println(" > DIFFICULTÉ ESTIMÉE : " + suggestedRating + "/15");
        System.out.println("----------------------------------------------");

        // génération du fichier SM
        BufferedWriter smfile = SMGenerator.GenerateSM(BPM, startTime, filename, outputDir);

        if (HARDMODE)
            System.out.println("Mode Difficile activé ! Des flèches en plus pour vous ! :-O");

        SMGenerator.AddNotes(smfile, SMGenerator.Beginner, StepGenerator.GenerateNotes(1, HARDMODE ? 2 : 4, manyTimes,
                fewTimes, MidFFTAmount, MidFFTMaxes, timePerSample, timePerBeat, startTime, seconds, false));
        SMGenerator.AddNotes(smfile, SMGenerator.Easy, StepGenerator.GenerateNotes(1, HARDMODE ? 1 : 2, manyTimes,
                fewTimes, MidFFTAmount, MidFFTMaxes, timePerSample, timePerBeat, startTime, seconds, false));
        SMGenerator.AddNotes(smfile, SMGenerator.Medium, StepGenerator.GenerateNotes(2, HARDMODE ? 4 : 6, manyTimes,
                fewTimes, MidFFTAmount, MidFFTMaxes, timePerSample, timePerBeat, startTime, seconds, false));
        SMGenerator.AddNotes(smfile, SMGenerator.Hard, StepGenerator.GenerateNotes(2, HARDMODE ? 2 : 4, manyTimes,
                fewTimes, MidFFTAmount, MidFFTMaxes, timePerSample, timePerBeat, startTime, seconds, false));
        SMGenerator.AddNotes(smfile, SMGenerator.Challenge, StepGenerator.GenerateNotes(2, HARDMODE ? 1 : 2, manyTimes,
                fewTimes, MidFFTAmount, MidFFTMaxes, timePerSample, timePerBeat, startTime, seconds, true));
        SMGenerator.Complete(smfile);

        System.out.println("[--------- SUCCÈS ----------]");
    }
}
