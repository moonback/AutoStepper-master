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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class AutoStepper {

    public static boolean DEBUG_STEPS = false;
    public static float MAX_BPM = 170f, MIN_BPM = 70f, BPM_SENSITIVITY = 0.05f, STARTSYNC = 0.0f;
    public static double TAPSYNC = -0.11;
    public static boolean HARDMODE = false, UPDATESM = false;
    public static String customImagePath = null, customBackgroundPath = null, songTitle = "", songArtist = "";
    public static String titleTranslit = "", subTitleTranslit = "", artistTranslit = "", genre = "";

    public static Minim minim;
    public static final AutoStepper myAS = new AutoStepper();
    public static final int KICKS = 0, ENERGY = 1, SNARE = 2, HAT = 3;

    private final TFloatArrayList[] manyTimes = new TFloatArrayList[4];
    private final TFloatArrayList[] fewTimes = new TFloatArrayList[4];

    public String sketchPath(String fileName) { return fileName; }
    public InputStream createInput(String fileName) { try { return new FileInputStream(new File(fileName)); } catch (Exception e) { return null; } }

    public static void loadMetadata(String filename) {
        songTitle = ""; songArtist = ""; genre = "";
        if (minim == null) minim = new Minim(myAS);
        AudioSample sample = minim.loadSample(filename, 512);
        if (sample == null) return;
        AudioMetaData meta = sample.getMetaData();
        if (meta != null) {
            if (meta.title() != null && !meta.title().trim().isEmpty()) songTitle = meta.title();
            if (meta.author() != null && !meta.author().trim().isEmpty()) songArtist = meta.author();
            if (meta.genre() != null && !meta.genre().trim().isEmpty()) genre = meta.genre();
        }
        sample.close();
    }

    private static String getArg(String[] args, String argname, String def) {
        String prefix = argname.toLowerCase() + "=";
        for (String s : args) {
            String clean = s.replace("\"", "").trim();
            if (clean.toLowerCase().startsWith(prefix)) return clean.substring(prefix.length());
        }
        return def;
    }

    private static int getIntArg(String[] args, String argname, int def) {
        try { return Integer.parseInt(getArg(args, argname, String.valueOf(def))); } catch (Exception e) { return def; }
    }

    private static float getFloatArg(String[] args, String argname, float def) {
        try { return Float.parseFloat(getArg(args, argname, String.valueOf(def))); } catch (Exception e) { return def; }
    }

    private static boolean hasArg(String[] args, String argname) {
        for (String s : args) if (s.equalsIgnoreCase(argname)) return true;
        return false;
    }

    private static void applyJsonConfigIfPresent(String[] args) {
        String cfg = getArg(args, "config", "");
        if (cfg.isEmpty()) return;
        File f = new File(cfg);
        if (!f.isFile()) return;
        try {
            String json = Files.readString(f.toPath(), StandardCharsets.UTF_8);
            MAX_BPM = parseJsonFloat(json, "maxBpm", MAX_BPM);
            MIN_BPM = parseJsonFloat(json, "minBpm", MIN_BPM);
            BPM_SENSITIVITY = parseJsonFloat(json, "bpmSensitivity", BPM_SENSITIVITY);
            STARTSYNC = parseJsonFloat(json, "startSync", STARTSYNC);
        } catch (IOException ignored) { }
    }

    private static float parseJsonFloat(String json, String key, float def) {
        String p = "\"" + key + "\"";
        int idx = json.indexOf(p);
        if (idx < 0) return def;
        int colon = json.indexOf(':', idx);
        int end = json.indexOf(',', colon);
        if (end < 0) end = json.indexOf('}', colon);
        if (colon < 0 || end < 0) return def;
        try { return Float.parseFloat(json.substring(colon + 1, end).trim().replace("\"", "")); } catch (Exception e) { return def; }
    }

    public static void main(String[] args) {
        if (args.length == 0) { AutoStepperGUI.launch(); return; }
        minim = new Minim(myAS);
        if (hasArg(args, "help") || hasArg(args, "-h") || hasArg(args, "--help")) {
            System.out.println("Usage: input=<file|dir> output=<dir> duration=<sec> threads=<n> config=<json>");
            return;
        }
        applyJsonConfigIfPresent(args);
        MAX_BPM = getFloatArg(args, "maxbpm", MAX_BPM);
        String outputDir = getArg(args, "output", ".");
        String input = getArg(args, "input", ".");
        float duration = getFloatArg(args, "duration", 90f);
        STARTSYNC = getFloatArg(args, "synctime", STARTSYNC);
        BPM_SENSITIVITY = getFloatArg(args, "bpmsensitivity", BPM_SENSITIVITY);
        HARDMODE = "true".equalsIgnoreCase(getArg(args, "hard", "false"));
        UPDATESM = "true".equalsIgnoreCase(getArg(args, "updatesm", "false"));
        int threads = Math.max(1, getIntArg(args, "threads", Runtime.getRuntime().availableProcessors()));

        File inputFile = new File(input);
        if (inputFile.isFile()) {
            myAS.analyzeUsingAudioRecordingStream(inputFile, duration, outputDir);
            return;
        }
        if (!inputFile.isDirectory()) {
            System.out.println("Input not found: " + inputFile.getAbsolutePath());
            return;
        }
        File[] allFiles = inputFile.listFiles();
        if (allFiles == null) return;
        List<File> audioFiles = new ArrayList<>();
        for (File f : allFiles) {
            String ext = f.getName().toLowerCase();
            if (f.isFile() && (ext.endsWith(".mp3") || ext.endsWith(".wav"))) audioFiles.add(f);
        }
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        for (File song : audioFiles) pool.submit(() -> new AutoStepper().analyzeUsingAudioRecordingStream(song, duration, outputDir));
        pool.shutdown();
        try { pool.awaitTermination(1, TimeUnit.HOURS); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
    }

    float getDifferenceAverage(TFloatArrayList arr) {
        if (arr == null || arr.size() <= 1) return 0f;
        float avg = 0f;
        for (int i = 0; i < arr.size() - 1; i++) avg += Math.abs(arr.getQuick(i + 1) - arr.getQuick(i));
        return avg / (arr.size() - 1);
    }

    TFloatArrayList calculateDifferences(TFloatArrayList arr, float threshold) {
        TFloatArrayList diff = new TFloatArrayList();
        for (int i = 1; i < arr.size(); i++) {
            float d = arr.getQuick(i) - arr.getQuick(i - 1);
            if (d >= threshold) diff.add(d);
        }
        return diff;
    }

    float getMostCommon(TFloatArrayList arr, float threshold, boolean closestToInteger) { return arr.isEmpty() ? -1f : arr.sum() / arr.size(); }
    public float getBestOffset(float timePerBeat, TFloatArrayList times, float groupBy) { return 0f; }
    public void AddCommonBPMs(TFloatArrayList common, TFloatArrayList times, float doubleSpeed, float timePerSample) {
        if (times == null || times.size() < 2) return;
        TFloatArrayList diffs = calculateDifferences(times, doubleSpeed);
        if (diffs.isEmpty()) return;
        float commonBPM = 60f / getMostCommon(diffs, timePerSample, true);
        if (commonBPM > 0f) common.add(commonBPM);
    }

    void analyzeUsingAudioRecordingStream(File filename, float seconds, String outputDir) {
        if (filename == null || !filename.isFile()) return;
        int fftSize = 512;
        AudioRecordingStream stream = minim.loadFileStream(filename.getAbsolutePath(), fftSize, false);
        if (stream == null) { System.out.println("Unable to load audio: " + filename); return; }
        stream.play();
        FFT fft = new FFT(fftSize, stream.getFormat().getSampleRate());
        BeatDetect fewbd = new BeatDetect(BeatDetect.FREQ_ENERGY, fftSize, stream.getFormat().getSampleRate());
        BeatDetect manybd = new BeatDetect(BeatDetect.FREQ_ENERGY, fftSize, stream.getFormat().getSampleRate());
        MultiChannelBuffer buffer = new MultiChannelBuffer(fftSize, stream.getFormat().getChannels());
        float songTime = stream.getMillisecondLength() / 1000f;
        if (seconds <= 0 || seconds > songTime) seconds = songTime;
        int totalChunks = Math.max(1, (int) ((seconds * stream.getFormat().getSampleRate()) / fftSize));
        for (int i = 0; i < 4; i++) { fewTimes[i] = new TFloatArrayList(); manyTimes[i] = new TFloatArrayList(); }
        TFloatArrayList avg = new TFloatArrayList();
        TFloatArrayList max = new TFloatArrayList();
        for (int chunk = 0; chunk < totalChunks; chunk++) {
            stream.read(buffer);
            float[] data = buffer.getChannel(0);
            float time = chunk * (fftSize / stream.getFormat().getSampleRate());
            fewbd.detect(data, time); manybd.detect(data, time); fft.forward(data);
            avg.add(fft.calcAvg(300f, 3000f));
            max.add(Math.max(fft.getBand(fft.freqToIndex(300f)), fft.getBand(fft.freqToIndex(3000f))));
            if (fewbd.isKick()) fewTimes[KICKS].add(time);
            if (fewbd.isSnare()) fewTimes[SNARE].add(time);
            if (manybd.isKick()) manyTimes[KICKS].add(time);
        }
        TFloatArrayList common = new TFloatArrayList();
        AddCommonBPMs(common, fewTimes[KICKS], 60f / (MAX_BPM * 2f), 0.01f);
        float bpm = common.isEmpty() ? 120f : Math.max(MIN_BPM, Math.min(MAX_BPM, common.get(0)));
        float timePerBeat = 60f / bpm;
        BufferedWriter sm = SMGenerator.GenerateSM(bpm, 0f, filename, outputDir);
        if (sm == null) return;
        SMGenerator.AddNotes(sm, SMGenerator.Beginner, StepGenerator.GenerateNotes(1, HARDMODE ? 2 : 4, manyTimes, fewTimes, avg, max, 0.01f, timePerBeat, 0f, seconds, false));
        SMGenerator.AddNotes(sm, SMGenerator.Challenge, StepGenerator.GenerateNotes(2, HARDMODE ? 1 : 2, manyTimes, fewTimes, avg, max, 0.01f, timePerBeat, 0f, seconds, true));
        SMGenerator.Complete(sm);
    }
}
