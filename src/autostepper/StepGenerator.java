package autostepper;

import gnu.trove.list.array.TFloatArrayList;
import java.util.ArrayList;
import java.util.Random;

/**
 *
 * @author Phr00t
 */
public class StepGenerator {
    
    static private int MAX_HOLD_BEAT_COUNT = 4;
    static private char EMPTY = '0', STEP = '1', HOLD = '2', STOP = '3', MINE = 'M';
    
    static Random rand = new Random();
    
    static private int getHoldCount() {
        int ret = 0;
        if( holding[0] > 0f ) ret++;
        if( holding[1] > 0f ) ret++;
        if( holding[2] > 0f ) ret++;
        if( holding[3] > 0f ) ret++;
        return ret;
    }
    
    static private int getRandomHold() {
        int hc = getHoldCount();
        if(hc == 0) return -1;
        int pickHold = rand.nextInt(hc);
        for(int i=0;i<4;i++) {
            if( holding[i] > 0f ) {
                if( pickHold == 0 ) return i;
                pickHold--;
            }
        }
        return -1;
    }
    
    // crée une ligne de notes, avec de nombreuses vérifications, équilibrages et filtrages
    static float[] holding = new float[4];
    static float lastJumpTime;
    static ArrayList<char[]> AllNoteLines = new ArrayList<>();
    static float lastKickTime = 0f;
    static int commaSeperator, commaSeperatorReset, mineCount, holdRun;
    
    private static char[] getHoldStops(int currentHoldCount, float time, int holds) {
        char[] holdstops = new char[4];
        holdstops[0] = '0';
        holdstops[1] = '0';
        holdstops[2] = '0';
        holdstops[3] = '0';
        if( currentHoldCount > 0 ) {
            while( holds < 0 ) {
                int index = getRandomHold();
                if( index == -1 ) {
                    holds = 0;
                    currentHoldCount = 0;
                } else {
                    holding[index] = 0f;
                    holdstops[index] = STOP;
                    holds++; currentHoldCount--;
                }
            }
            // s'il y a encore des holds, soustraire le compteur jusqu'à 0
            for(int i=0;i<4;i++) {
                if( holding[i] > 0f ) {
                    holding[i] -= 1f;
                    if( holding[i] <= 0f ) {
                        holding[i] = 0f;
                        holdstops[i] = STOP;
                        currentHoldCount--;
                    }
                } 
            }
        }        
        return holdstops;
    }
    
    private static String getNoteLineIndex(int i) {
        if( i < 0 || i >= AllNoteLines.size() ) return "0000";
        return String.valueOf(AllNoteLines.get(i));
    }
    
    private static String getLastNoteLine() {
        return getNoteLineIndex(AllNoteLines.size()-1);
    }
    
    private static void makeNoteLine(String lastLine, float time, int steps, int holds, boolean mines, TFloatArrayList FFTMaxes, float timePerFFT) {
        if( steps == 0 ) {
            char[] ret = getHoldStops(getHoldCount(), time, holds);
            AllNoteLines.add(ret);
            return;
        }
        if( steps > 1 && time - lastJumpTime < (mines ? 2f : 4f) ) steps = 1; // ne pas spammer les sauts
        if( steps >= 2 ) {
             // pas de mains (hands)
            steps = 2;
            lastJumpTime = time;
        }
        // on ne peut pas tenir (hold) ou sauter plus de 2
        int currentHoldCount = getHoldCount(); 
        if( holds + currentHoldCount > 2 ) holds = 2 - currentHoldCount;
        if( steps + currentHoldCount > 2 ) steps = 2 - currentHoldCount;
        // si on a eu une série de 3 holds, on n'en crée pas de nouveau pour éviter que le joueur ne tourne sur lui-même
        if( holdRun >= 2 && holds > 0 ) holds = 0;
        // est-ce qu'on arrête les holds ?
        char[] noteLine = getHoldStops(currentHoldCount, time, holds);
        // si on fait un pas, mais qu'on vient de finir un hold, on déplace cette fin de hold vers le haut pour laisser le temps
        // nécessaire au déplacement vers la nouvelle note
        if( steps > 0 && lastLine.contains("3") ) {
            int currentIndex = AllNoteLines.size()-1;
            char[] currentLine = AllNoteLines.get(currentIndex);
            for(int i=0;i<4;i++) {
                if( currentLine[i] == '3' ) {
                    // on a un arrêt de hold ici, déplaçons-le vers le haut
                    currentLine[i] = '0';
                    char[] nextLineUp = AllNoteLines.get(currentIndex-1);
                    if( nextLineUp[i] == '2' ) {
                        nextLineUp[i] = '1';
                    } else nextLineUp[i] = '3';
                }
            }
        }
        // ok, on crée les pas (steps)
        String completeLine;
        char[] orig = new char[4];
        orig[0] = noteLine[0];
        orig[1] = noteLine[1];
        orig[2] = noteLine[2];
        orig[3] = noteLine[3];
        float[] willhold = new float[4]; 
        do {
            int stepcount = steps, holdcount = holds;
            noteLine[0] = orig[0];
            noteLine[1] = orig[1];
            noteLine[2] = orig[2];
            noteLine[3] = orig[3];
            willhold[0] = 0f;
            willhold[1] = 0f;
            willhold[2] = 0f;
            willhold[3] = 0f;
            while(stepcount > 0) {
                int stepindex = rand.nextInt(4);
                if( noteLine[stepindex] != EMPTY || holding[stepindex] > 0f ) continue;
                if( holdcount > 0 ) {
                    noteLine[stepindex] = HOLD;
                    willhold[stepindex] = MAX_HOLD_BEAT_COUNT;
                    holdcount--; stepcount--;
                } else {
                    noteLine[stepindex] = STEP;
                    stepcount--;
                }
            }
            // mettre une mine ? IA basée sur l'énergie audio
            if( mines && FFTMaxes != null && AutoStepper.SMART_MINES ) {
                boolean smartMine = shouldPlaceMine(time, FFTMaxes, timePerFFT);
                if( smartMine ) {
                    // Placer la mine sur une position vide aléatoire
                    int minePos = rand.nextInt(4);
                    if( noteLine[minePos] == EMPTY && holding[minePos] <= 0f ) {
                        noteLine[minePos] = MINE;
                    }
                }
            }
            completeLine = String.valueOf(noteLine);
        } while( completeLine.equals(lastLine) && completeLine.equals("0000") == false );
        if( willhold[0] > holding[0] ) holding[0] = willhold[0];
        if( willhold[1] > holding[1] ) holding[1] = willhold[1];
        if( willhold[2] > holding[2] ) holding[2] = willhold[2];
        if( willhold[3] > holding[3] ) holding[3] = willhold[3];
        if( getHoldCount() == 0 ) {
            holdRun = 0;
        } else holdRun++;
        AllNoteLines.add(noteLine);
    }
    
    private static boolean isNearATime(float time, TFloatArrayList timelist, float threshold) {
        for(int i=0;i<timelist.size();i++) {
            float checktime = timelist.get(i);
            if( Math.abs(checktime - time) <= threshold ) return true;
            if( checktime > time + threshold ) return false;
        }
        return false;
    }
    
    private static float getFFT(float time, TFloatArrayList FFTAmounts, float timePerFFT) {
        int index = Math.round(time / timePerFFT);
        if( index < 0 || index >= FFTAmounts.size()) return 0f;
        return FFTAmounts.getQuick(index);
    }
    
    private static boolean sustainedFFT(float startTime, float len, float granularity, float timePerFFT, TFloatArrayList FFTMaxes, TFloatArrayList FFTAvg, float aboveAvg, float averageMultiplier) {
        int endIndex = (int)Math.floor((startTime + len) / timePerFFT);
        if( endIndex >= FFTMaxes.size() ) return false;
        int wiggleRoom = Math.round(0.1f * len / timePerFFT);
        int startIndex = (int)Math.floor(startTime / timePerFFT);
        int pastGranu = (int)Math.floor((startTime + granularity) / timePerFFT);
        boolean startThresholdReached = false;
        for(int i=startIndex;i<=endIndex;i++) {
            float amt = FFTMaxes.getQuick(i);
            float avg = FFTAvg.getQuick(i) * averageMultiplier;
            if( i <= pastGranu ) {
                startThresholdReached |= amt >= avg + aboveAvg;
            } else {
                if( startThresholdReached == false ) return false;
                if( amt < avg ) {
                    wiggleRoom--;
                    if( wiggleRoom <= 0 ) return false;
                }
            }
        }
        return true;
    }
    
    public static String GenerateNotes(int stepGranularity, int skipChance,
                                       TFloatArrayList[] manyTimes,
                                       TFloatArrayList[] fewTimes,
                                       TFloatArrayList FFTAverages, TFloatArrayList FFTMaxes, float timePerFFT,
                                       float timePerBeat, float timeOffset, float totalTime,
                                       boolean allowMines) {      
        // réinitialisation des variables
        AllNoteLines.clear();
        lastJumpTime = -10f;
        holdRun = 0;
        holding[0] = 0f;
        holding[1] = 0f;
        holding[2] = 0f;
        holding[3] = 0f;
        lastKickTime = 0f;
        commaSeperatorReset = 4 * stepGranularity;
        float lastSkippedTime = -10f;
        int totalStepsMade = 0, timeIndex = 0;
        boolean skippedLast = false;
        float timeGranularity = timePerBeat / stepGranularity;
        for(float t = timeOffset; t <= totalTime; t += timeGranularity) {
            int steps = 0, holds = 0;
            String lastLine = getLastNoteLine();
            if( t > 0f ) {
                float fftavg = getFFT(t, FFTAverages, timePerFFT);
                float fftmax = getFFT(t, FFTMaxes, timePerFFT);
                boolean sustained = sustainedFFT(t, 0.75f, timeGranularity, timePerFFT, FFTMaxes, FFTAverages, 0.25f, 0.45f);
                boolean nearKick = isNearATime(t, fewTimes[AutoStepper.KICKS], timePerBeat / stepGranularity);
                boolean nearSnare = isNearATime(t, fewTimes[AutoStepper.SNARE], timePerBeat / stepGranularity);
                boolean nearEnergy = isNearATime(t, fewTimes[AutoStepper.ENERGY], timePerBeat / stepGranularity);
                steps = sustained || nearKick || nearSnare || nearEnergy ? 1 : 0;
                if( sustained ) {
                    holds = 1 + (nearEnergy ? 1 : 0);
                } else if( fftmax < 0.5f ) {
                    holds = fftmax < 0.25f ? -2 : -1;
                }
                if( nearKick && (nearSnare || nearEnergy) && timeIndex % 2 == 0 &&
                    steps > 0 && lastLine.contains("1") == false && lastLine.contains("2") == false && lastLine.contains("3") == false ) {
                     // on ne saute que dans les zones intenses, sur les battements complets (pas de demi-temps)
                    steps = 2;
                }
                // attendez, est-ce qu'on ignore de nouvelles flèches ?
                // si on vient de finir un saut, pas de demi-temps
                // si on maintient quelque chose, pas de flèches sur demi-temps
                if( timeIndex % 2 == 1 &&
                    (skipChance > 1 && timeIndex % 2 == 1 && rand.nextInt(skipChance) > 0 || getHoldCount() > 0) ||
                    t - lastJumpTime < timePerBeat ) {
                    steps = 0;
                    if( holds > 0 ) holds = 0;
                }                
            }
            if( AutoStepper.DEBUG_STEPS ) {
                makeNoteLine(lastLine, t, timeIndex % 2 == 0 ? 1 : 0, -2, allowMines, FFTMaxes, timePerFFT);
            } else makeNoteLine(lastLine, t, steps, holds, allowMines, FFTMaxes, timePerFFT);
            totalStepsMade += steps;
            timeIndex++;
        }
        // ok, on assemble AllNotes
        String AllNotes = "";
        commaSeperator = commaSeperatorReset;
        for(int i=0;i<AllNoteLines.size();i++) {
            AllNotes += getNoteLineIndex(i) + "\n";
            commaSeperator--;
            if( commaSeperator == 0 ) {
                AllNotes += ",\n";
                commaSeperator = commaSeperatorReset;
            }
        }
        // remplir les derniers vides
        while( commaSeperator > 0 ) {
            AllNotes += "3333";
            commaSeperator--;
            if( commaSeperator > 0 ) AllNotes += "\n";
        }
        int _stepCount = AllNotes.length() - AllNotes.replace("1", "").length();
        int _holdCount = AllNotes.length() - AllNotes.replace("2", "").length();
        int _mineCount = AllNotes.length() - AllNotes.replace("M", "").length();
        System.out.println("Flèches : " + _stepCount + ", Holds : " + _holdCount + ", Mines : " + _mineCount);
        return AllNotes;
    }

    // --- IA de Placement des Mines basée sur l'énergie ---
    static boolean shouldPlaceMine(float time, TFloatArrayList FFTMaxes, float timePerFFT) {
        int idx = Math.round(time / timePerFFT);
        if (idx < 0 || idx >= FFTMaxes.size()) return false;
        float energy = FFTMaxes.getQuick(idx);
        // Plus l'énergie est forte, plus la probabilité de mine est élevée
        if (energy > 0.8f) return rand.nextInt(3) == 0;   // 33% en zone très intense
        if (energy > 0.5f) return rand.nextInt(6) == 0;   // 17% en zone intense
        if (energy > 0.3f) return rand.nextInt(12) == 0;  // 8% en zone moyenne
        return false;  // Pas de mine en zone calme
    }
    
}
