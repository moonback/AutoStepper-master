package autostepper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author Phr00t
 */
public class SMGenerator {

    private static String Header = "#TITLE:$TITLE;\n" +
            "#SUBTITLE:;\n" +
            "#ARTIST:$ARTIST;\n" +
            "#TITLETRANSLIT:$TITLETRANSLIT;\n" +
            "#SUBTITLETRANSLIT:$SUBTITLETRANSLIT;\n" +
            "#ARTISTTRANSLIT:$ARTISTTRANSLIT;\n" +
            "#GENRE:$GENRE;\n" +
            "#CREDIT:AutoStepper par Maysson.D;\n" +
            "#BANNER:$BANNERIMAGE;\n" +
            "#BACKGROUND:$BACKIMAGE;\n" +
            "#LYRICSPATH:;\n" +
            "#CDTITLE:;\n" +
            "#MUSIC:$MUSICFILE;\n" +
            "#OFFSET:$STARTTIME;\n" +
            "#SAMPLESTART:30.0;\n" +
            "#SAMPLELENGTH:30.0;\n" +
            "#SELECTABLE:YES;\n" +
            "#BPMS:0.000000=$BPM;\n" +
            "#STOPS:;\n" +
            "#KEYSOUNDS:;\n" +
            "#ATTACKS:;";

    public static String Challenge = "Challenge:\n" +
            "     10:";

    public static String Hard = "Hard:\n" +
            "     8:";

    public static String Medium = "Medium:\n" +
            "     6:";

    public static String Easy = "Easy:\n" +
            "     4:";

    public static String Beginner = "Beginner:\n" +
            "     2:";

    private static String NoteFramework = "//---------------dance-single - ----------------\n" +
            "#NOTES:\n" +
            "     dance-single:\n" +
            "     :\n" +
            "     $DIFFICULTY\n" +
            "     0.733800,0.772920,0.048611,0.850698,0.060764,634.000000,628.000000,6.000000,105.000000,8.000000,0.000000,0.733800,0.772920,0.048611,0.850698,0.060764,634.000000,628.000000,6.000000,105.000000,8.000000,0.000000:\n"
            +
            "$NOTES\n" +
            ";\n\n";

    private static void copyFileUsingStream(File source, File dest) throws IOException {
        InputStream is = null;
        OutputStream os = null;
        try {
            is = new FileInputStream(source);
            os = new FileOutputStream(dest);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        } finally {
            if (is != null) is.close();
            if (os != null) os.close();
        }
    }

    public static void AddNotes(BufferedWriter smfile, String difficulty, String notes) {
        try {
            smfile.write(NoteFramework.replace("$DIFFICULTY", difficulty).replace("$NOTES", notes));
        } catch (Exception e) {
        }
    }

    public static void Complete(BufferedWriter smfile) {
        try {
            smfile.close();
        } catch (Exception e) {
        }
    }

    public static File getSMFile(File songFile, String outputdir) {
        String filename = songFile.getName();
        File dir = new File(outputdir, filename + "_dir/");
        return new File(dir, filename + ".sm");
    }

    public static BufferedWriter GenerateSM(float BPM, float startTime, File songfile, String outputdir) {
        String filename = songfile.getName();
        String songname = filename.replace(".mp3", "").replace(".wav", "").replace(".MP3", "").replace(".WAV", "");
        
        // Tentative de découpage Artiste - Titre si les tags sont vides
        if (AutoStepper.songTitle.isEmpty() || AutoStepper.songArtist.isEmpty()) {
            if (songname.contains(" - ")) {
                String[] parts = songname.split(" - ", 2);
                if (AutoStepper.songArtist.isEmpty()) AutoStepper.songArtist = parts[0].trim();
                if (AutoStepper.songTitle.isEmpty()) AutoStepper.songTitle = parts[1].trim();
            }
        }

        String finalTitle = (AutoStepper.songTitle != null && !AutoStepper.songTitle.isEmpty()) ? AutoStepper.songTitle : songname;
        String finalArtist = (AutoStepper.songArtist != null && !AutoStepper.songArtist.isEmpty()) ? AutoStepper.songArtist : "AutoStepper par Maysson.D";
        
        File dir = new File(outputdir, filename + "_dir/");
        dir.mkdirs();
        File smfile = new File(dir, filename + ".sm");
        // Gestion de la bannière
        File bannerFile = new File(dir, filename + "_banner.png");
        String bannerFileName = "";
        if (AutoStepper.customImagePath != null && new File(AutoStepper.customImagePath).exists()) {
            System.out.println("Utilisation de la bannière personnalisée : " + AutoStepper.customImagePath);
            try {
                copyFileUsingStream(new File(AutoStepper.customImagePath), bannerFile);
                bannerFileName = bannerFile.getName();
            } catch (IOException e) {
                System.out.println("Erreur lors de la copie de la bannière : " + e.getMessage());
            }
        }

        // Gestion de l'arrière-plan
        File bgFile = new File(dir, filename + "_bg.png");
        String bgFileName = "";
        if (AutoStepper.customBackgroundPath != null && new File(AutoStepper.customBackgroundPath).exists()) {
            System.out.println("Utilisation de l'arrière-plan personnalisé : " + AutoStepper.customBackgroundPath);
            try {
                copyFileUsingStream(new File(AutoStepper.customBackgroundPath), bgFile);
                bgFileName = bgFile.getName();
            } catch (IOException e) {
                System.out.println("Erreur lors de la copie de l'arrière-plan : " + e.getMessage());
            }
        }

        // Compléter avec la recherche Google si nécessaire
        if (bannerFileName.isEmpty() || bgFileName.isEmpty()) {
            File searchImgFile = new File(dir, filename + "_img.png");
            if (searchImgFile.exists() == false) {
                System.out.println("Tentative de récupération d'une image internet pour compléter...");
                GoogleImageSearch.FindAndSaveImage(
                        songname.replace("(", " ").replace(")", " ").replace("www.", " ").replace("_", " ")
                                .replace("-", " ").replace("&", " ").replace("[", " ").replace("]", " "),
                        searchImgFile.getAbsolutePath());
            }
            if (searchImgFile.exists()) {
                System.out.println("Image internet récupérée !");
                if (bannerFileName.isEmpty()) bannerFileName = searchImgFile.getName();
                if (bgFileName.isEmpty()) bgFileName = searchImgFile.getName();
            } else {
                System.out.println("Aucune image internet trouvée :(");
            }
        }

        try {
            smfile.delete();
            File musicFileDest = new File(dir, filename);
            try {
                copyFileUsingStream(songfile, musicFileDest);
            } catch (IOException e) {
                System.out.println("Erreur lors de la copie de la musique : " + e.getMessage());
            }

            BufferedWriter writer = new BufferedWriter(new FileWriter(smfile));
            writer.write(
                    Header.replace("$TITLE", finalTitle)
                            .replace("$ARTIST", finalArtist)
                            .replace("$TITLETRANSLIT", AutoStepper.titleTranslit != null ? AutoStepper.titleTranslit : "")
                            .replace("$SUBTITLETRANSLIT", AutoStepper.subTitleTranslit != null ? AutoStepper.subTitleTranslit : "")
                            .replace("$ARTISTTRANSLIT", AutoStepper.artistTranslit != null ? AutoStepper.artistTranslit : "")
                            .replace("$GENRE", AutoStepper.genre != null ? AutoStepper.genre : "")
                            .replace("$BANNERIMAGE", bannerFileName)
                            .replace("$BACKIMAGE", bgFileName)
                            .replace("$MUSICFILE", filename)
                            .replace("$STARTTIME", Float.toString(startTime + AutoStepper.STARTSYNC))
                            .replace("$BPM", Float.toString(BPM)));
            return writer;
        } catch (Exception e) {
        }
        return null;
    }
}
