package autostepper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;

public class AutoStepperGUI extends JFrame {

    private JTextField txtInput;
    private JTextField txtOutput;
    private JSpinner spinDuration;
    private JCheckBox chkHardMode;
    private JCheckBox chkUseTapper;
    private JTextArea logArea;
    private JButton btnStart;

    public AutoStepperGUI() {
        setTitle("AutoStepper v1.7 - Interface Graphique");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 550);
        setLocationRelativeTo(null);

        // Panel principal avec marges
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        setContentPane(mainPanel);

        // Panel de configuration (Haut)
        JPanel configPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // --- Ligne 1 : Entrée ---
        gbc.gridx = 0; gbc.gridy = 0;
        configPanel.add(new JLabel("Musique / Dossier :"), gbc);

        txtInput = new JTextField(".");
        gbc.gridx = 1; gbc.weightx = 1.0;
        configPanel.add(txtInput, gbc);

        JButton btnBrowseInput = new JButton("Parcourir...");
        gbc.gridx = 2; gbc.weightx = 0;
        configPanel.add(btnBrowseInput, gbc);

        // --- Ligne 2 : Sortie ---
        gbc.gridx = 0; gbc.gridy = 1;
        configPanel.add(new JLabel("Dossier de sortie :"), gbc);

        txtOutput = new JTextField(".");
        gbc.gridx = 1; gbc.weightx = 1.0;
        configPanel.add(txtOutput, gbc);

        JButton btnBrowseOutput = new JButton("Parcourir...");
        gbc.gridx = 2; gbc.weightx = 0;
        configPanel.add(btnBrowseOutput, gbc);

        // --- Ligne 3 : Options ---
        JPanel optionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        chkHardMode = new JCheckBox("Mode Difficile  ");
        chkUseTapper = new JCheckBox("Utiliser Tap Manuel  ");
        optionsPanel.add(chkHardMode);
        optionsPanel.add(chkUseTapper);
        optionsPanel.add(new JLabel("  Durée (sec) : "));
        spinDuration = new JSpinner(new SpinnerNumberModel(90, 10, 600, 10));
        optionsPanel.add(spinDuration);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 3;
        configPanel.add(optionsPanel, gbc);

        mainPanel.add(configPanel, BorderLayout.NORTH);

        // Zone de logs (Centre)
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setBackground(new Color(245, 245, 245));
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Journal d'exécution"));
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Bouton de démarrage (Bas)
        btnStart = new JButton("DÉMARRER LA GÉNÉRATION DES STEPS");
        btnStart.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnStart.setPreferredSize(new Dimension(0, 50));
        btnStart.setBackground(new Color(70, 130, 180));
        btnStart.setForeground(Color.WHITE);
        mainPanel.add(btnStart, BorderLayout.SOUTH);

        // --- Événements ---
        btnBrowseInput.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser(txtInput.getText());
            chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                txtInput.setText(chooser.getSelectedFile().getAbsolutePath());
            }
        });

        btnBrowseOutput.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser(txtOutput.getText());
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                txtOutput.setText(chooser.getSelectedFile().getAbsolutePath());
            }
        });

        btnStart.addActionListener(e -> startProcess());

        // Redirection du flux System.out vers le JTextArea
        redirectSystemStreams();
    }

    private void redirectSystemStreams() {
        OutputStream out = new OutputStream() {
            @Override
            public void write(int b) {
                updateLog(String.valueOf((char) b));
            }

            @Override
            public void write(byte[] b, int off, int len) {
                updateLog(new String(b, off, len));
            }
        };
        System.setOut(new PrintStream(out, true));
        System.setErr(new PrintStream(out, true));
    }

    private void updateLog(String text) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(text);
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    private void startProcess() {
        btnStart.setEnabled(false);
        logArea.setText("");
        
        // Lancement dans un thread séparé pour ne pas bloquer l'UI
        new Thread(() -> {
            try {
                // Initialisation de Minim si nécessaire
                if (AutoStepper.minim == null) {
                    AutoStepper.minim = new ddf.minim.Minim(AutoStepper.myAS);
                }

                // Configuration des variables globales de AutoStepper à partir de la GUI
                AutoStepper.HARDMODE = chkHardMode.isSelected();
                AutoStepper.USETAPPER = chkUseTapper.isSelected();
                float duration = ((Integer) spinDuration.getValue()).floatValue();
                String inputPath = txtInput.getText();
                String outputPath = txtOutput.getText();

                if (outputPath.endsWith("/") == false && outputPath.endsWith("\\") == false) {
                    outputPath += "/";
                }

                File inputFile = new File(inputPath);
                if (inputFile.exists()) {
                    if (inputFile.isFile()) {
                        AutoStepper.myAS.analyzeUsingAudioRecordingStream(inputFile, duration, outputPath);
                    } else {
                        System.out.println("Traitement du répertoire : " + inputFile.getAbsolutePath());
                        File[] allfiles = inputFile.listFiles();
                        if (allfiles != null) {
                            for (File f : allfiles) {
                                String extCheck = f.getName().toLowerCase();
                                if (f.isFile() && (extCheck.endsWith(".mp3") || extCheck.endsWith(".wav"))) {
                                    AutoStepper.myAS.analyzeUsingAudioRecordingStream(f, duration, outputPath);
                                }
                            }
                        }
                    }
                } else {
                    System.out.println("Erreur : Le fichier ou dossier d'entrée n'existe pas.");
                }
            } catch (Exception ex) {
                System.out.println("\n[ERREUR] " + ex.getMessage());
                ex.printStackTrace();
            } finally {
                SwingUtilities.invokeLater(() -> btnStart.setEnabled(true));
            }
        }).start();
    }

    public static void launch() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {}
        
        SwingUtilities.invokeLater(() -> {
            AutoStepperGUI gui = new AutoStepperGUI();
            gui.setVisible(true);
        });
    }
}
