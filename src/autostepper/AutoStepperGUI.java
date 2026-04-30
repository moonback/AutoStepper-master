package autostepper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.prefs.Preferences;

public class AutoStepperGUI extends JFrame {

    private JTextField txtInput;
    private JTextField txtOutput;
    private JTextField txtCustomImage;
    private JTextField txtCustomBackground;
    private JSpinner spinDuration;
    private JCheckBox chkHardMode;
    private JTextArea logArea;
    private JButton btnStart;
    
    private Preferences prefs = Preferences.userRoot().node(this.getClass().getName());

    public AutoStepperGUI() {
        setTitle("AutoStepper v1.7 - Interface Graphique");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 650);
        setLocationRelativeTo(null);

        // Panel principal avec thème sombre
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBackground(new Color(30, 30, 35));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        setContentPane(mainPanel);

        // Titre de l'application
        JLabel lblTitle = new JLabel("AutoStepper - Générateur StepMania", SwingConstants.CENTER);
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 26));
        lblTitle.setForeground(new Color(220, 220, 230));
        mainPanel.add(lblTitle, BorderLayout.NORTH);

        // Panel de configuration central
        JPanel configPanel = new JPanel();
        configPanel.setBackground(new Color(30, 30, 35));
        configPanel.setLayout(new BoxLayout(configPanel, BoxLayout.Y_AXIS));
        
        // Style commun pour les labels et bordures
        Color textColor = new Color(200, 200, 210);
        Color panelBg = new Color(45, 45, 50);
        
        // --- SECTION 1 : Fichiers et Dossiers ---
        JPanel filesPanel = new JPanel(new GridBagLayout());
        filesPanel.setBackground(panelBg);
        filesPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(70, 70, 80)), "📁 Fichiers et Dossiers", 0, 0, null, textColor));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 10, 8, 10);
        
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        JLabel lblIn = new JLabel("Musique / Dossier :"); lblIn.setForeground(textColor);
        filesPanel.add(lblIn, gbc);
        txtInput = new JTextField(".");
        txtInput.setBackground(new Color(60, 60, 65));
        txtInput.setForeground(Color.WHITE);
        txtInput.setCaretColor(Color.WHITE);
        gbc.gridx = 1; gbc.weightx = 1.0;
        filesPanel.add(txtInput, gbc);
        JButton btnBrowseInput = new JButton("Parcourir...");
        gbc.gridx = 2; gbc.weightx = 0;
        filesPanel.add(btnBrowseInput, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        JLabel lblOut = new JLabel("Dossier de sortie :"); lblOut.setForeground(textColor);
        filesPanel.add(lblOut, gbc);
        txtOutput = new JTextField(".");
        txtOutput.setBackground(new Color(60, 60, 65));
        txtOutput.setForeground(Color.WHITE);
        txtOutput.setCaretColor(Color.WHITE);
        gbc.gridx = 1; gbc.weightx = 1.0;
        filesPanel.add(txtOutput, gbc);
        JButton btnBrowseOutput = new JButton("Parcourir...");
        gbc.gridx = 2; gbc.weightx = 0;
        filesPanel.add(btnBrowseOutput, gbc);
        
        configPanel.add(filesPanel);
        configPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // --- SECTION 2 : Personnalisation Visuelle ---
        JPanel visualPanel = new JPanel(new GridBagLayout());
        visualPanel.setBackground(panelBg);
        visualPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(70, 70, 80)), "🖼️ Personnalisation Visuelle", 0, 0, null, textColor));
        
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        JLabel lblImg = new JLabel("Image (Bannière) :"); lblImg.setForeground(textColor);
        visualPanel.add(lblImg, gbc);
        txtCustomImage = new JTextField("");
        txtCustomImage.setBackground(new Color(60, 60, 65));
        txtCustomImage.setForeground(Color.WHITE);
        txtCustomImage.setCaretColor(Color.WHITE);
        txtCustomImage.setToolTipText("Image étroite affichée dans le menu");
        gbc.gridx = 1; gbc.weightx = 1.0;
        visualPanel.add(txtCustomImage, gbc);
        JButton btnBrowseImage = new JButton("Parcourir...");
        gbc.gridx = 2; gbc.weightx = 0;
        visualPanel.add(btnBrowseImage, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        JLabel lblBg = new JLabel("Image (Fond) :"); lblBg.setForeground(textColor);
        visualPanel.add(lblBg, gbc);
        txtCustomBackground = new JTextField("");
        txtCustomBackground.setBackground(new Color(60, 60, 65));
        txtCustomBackground.setForeground(Color.WHITE);
        txtCustomBackground.setCaretColor(Color.WHITE);
        txtCustomBackground.setToolTipText("Image affichée pendant le jeu");
        gbc.gridx = 1; gbc.weightx = 1.0;
        visualPanel.add(txtCustomBackground, gbc);
        JButton btnBrowseBackground = new JButton("Parcourir...");
        gbc.gridx = 2; gbc.weightx = 0;
        visualPanel.add(btnBrowseBackground, gbc);
        
        configPanel.add(visualPanel);
        configPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // --- SECTION 3 : Options de Génération ---
        JPanel optionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        optionsPanel.setBackground(panelBg);
        optionsPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(70, 70, 80)), "⚙️ Options de Génération", 0, 0, null, textColor));
        
        chkHardMode = new JCheckBox("Mode Difficile");
        chkHardMode.setBackground(panelBg);
        chkHardMode.setForeground(textColor);
        optionsPanel.add(chkHardMode);
        
        JLabel lblDur = new JLabel("Durée (sec, 0=Tout) :"); lblDur.setForeground(textColor);
        optionsPanel.add(lblDur);
        spinDuration = new JSpinner(new SpinnerNumberModel(0, 0, 3600, 10));
        optionsPanel.add(spinDuration);

        JButton btnAdvancedOptions = new JButton("Options Avancées...");
        optionsPanel.add(btnAdvancedOptions);

        btnAdvancedOptions.addActionListener(e -> showAdvancedOptionsDialog());
        
        configPanel.add(optionsPanel);

        // Zone de logs (Centre)
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setBackground(new Color(20, 20, 25));
        logArea.setForeground(new Color(150, 255, 150));
        logArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        logArea.setMargin(new Insets(10, 10, 10, 10));
        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(100, 100, 110)), "📝 Journal d'exécution", 0, 0, null, textColor));
        
        // Ajouter configPanel et scrollPane au centre de mainPanel
        JPanel centerWrapper = new JPanel(new BorderLayout(0, 15));
        centerWrapper.setBackground(new Color(30, 30, 35));
        centerWrapper.add(configPanel, BorderLayout.NORTH);
        centerWrapper.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(centerWrapper, BorderLayout.CENTER);

        // Bouton de démarrage (Bas)
        btnStart = new JButton("DÉMARRER LA GÉNÉRATION DES STEPS");
        btnStart.setFont(new Font("SansSerif", Font.BOLD, 16));
        btnStart.setPreferredSize(new Dimension(0, 60));
        btnStart.setBackground(new Color(50, 150, 80));
        btnStart.setForeground(Color.WHITE);
        btnStart.setFocusPainted(false);
        btnStart.setBorder(BorderFactory.createRaisedBevelBorder());
        mainPanel.add(btnStart, BorderLayout.SOUTH);

        // --- Événements ---
        btnBrowseInput.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser(txtInput.getText());
            chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                String path = chooser.getSelectedFile().getAbsolutePath();
                txtInput.setText(path);
                if (path.toLowerCase().endsWith(".mp3") || path.toLowerCase().endsWith(".wav")) {
                    AutoStepper.loadMetadata(path);
                }
            }
        });

        btnBrowseOutput.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser(txtOutput.getText());
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                txtOutput.setText(chooser.getSelectedFile().getAbsolutePath());
            }
        });

        btnBrowseImage.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser(txtCustomImage.getText());
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                txtCustomImage.setText(chooser.getSelectedFile().getAbsolutePath());
            }
        });

        btnBrowseBackground.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser(txtCustomBackground.getText());
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                txtCustomBackground.setText(chooser.getSelectedFile().getAbsolutePath());
            }
        });

        btnStart.addActionListener(e -> startProcess());

        // Redirection du flux System.out vers le JTextArea
        redirectSystemStreams();

        // Initialisations supplémentaires
        loadPreferences();
        setupValidation();
        setupDragAndDrop();
        
        // Sauvegarde des préférences à la fermeture
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                savePreferences();
            }
        });
    }

    private void loadPreferences() {
        txtInput.setText(prefs.get("input", "."));
        txtOutput.setText(prefs.get("output", "."));
        txtCustomImage.setText(prefs.get("customImage", ""));
        txtCustomBackground.setText(prefs.get("customBackground", ""));
        chkHardMode.setSelected(prefs.getBoolean("hardMode", false));
        spinDuration.setValue(prefs.getInt("duration", 0));
    }

    private void savePreferences() {
        prefs.put("input", txtInput.getText());
        prefs.put("output", txtOutput.getText());
        prefs.put("customImage", txtCustomImage.getText());
        prefs.put("customBackground", txtCustomBackground.getText());
        prefs.putBoolean("hardMode", chkHardMode.isSelected());
        prefs.putInt("duration", (Integer) spinDuration.getValue());
    }

    private void setupValidation() {
        DocumentListener validator = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { validateInputs(); }
            public void removeUpdate(DocumentEvent e) { validateInputs(); }
            public void changedUpdate(DocumentEvent e) { validateInputs(); }
        };
        txtInput.getDocument().addDocumentListener(validator);
        validateInputs();
    }

    private void validateInputs() {
        String in = txtInput.getText().trim();
        btnStart.setEnabled(!in.isEmpty());
    }

    private void setupDragAndDrop() {
        enableDragAndDrop(txtInput);
        enableDragAndDrop(txtOutput);
        enableDragAndDrop(txtCustomImage);
        enableDragAndDrop(txtCustomBackground);
    }

    private void enableDragAndDrop(JTextField textField) {
        textField.setTransferHandler(new TransferHandler() {
            @Override
            public boolean canImport(TransferSupport support) {
                return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
            }

            @Override
            public boolean importData(TransferSupport support) {
                if (!canImport(support)) return false;
                try {
                    Transferable t = support.getTransferable();
                    List<File> files = (List<File>) t.getTransferData(DataFlavor.javaFileListFlavor);
                    if (files != null && files.size() > 0) {
                        String path = files.get(0).getAbsolutePath();
                        textField.setText(path);
                        if (textField == txtInput && (path.toLowerCase().endsWith(".mp3") || path.toLowerCase().endsWith(".wav"))) {
                            AutoStepper.loadMetadata(path);
                        }
                        validateInputs();
                    }
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }
        });
    }

    private void showAdvancedOptionsDialog() {
        JDialog dialog = new JDialog(this, "Options Avancées", true);
        dialog.setSize(400, 250);
        dialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new GridLayout(5, 2, 10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JTextField txtTitleTranslit = new JTextField(AutoStepper.titleTranslit);
        JTextField txtSubTitleTranslit = new JTextField(AutoStepper.subTitleTranslit);
        JTextField txtArtistTranslit = new JTextField(AutoStepper.artistTranslit);
        JTextField txtGenre = new JTextField(AutoStepper.genre);

        panel.add(new JLabel("Title Translit:"));
        panel.add(txtTitleTranslit);
        panel.add(new JLabel("Subtitle Translit:"));
        panel.add(txtSubTitleTranslit);
        panel.add(new JLabel("Artist Translit:"));
        panel.add(txtArtistTranslit);
        panel.add(new JLabel("Genre:"));
        panel.add(txtGenre);

        JButton btnSave = new JButton("Enregistrer");
        btnSave.addActionListener(ev -> {
            AutoStepper.titleTranslit = txtTitleTranslit.getText();
            AutoStepper.subTitleTranslit = txtSubTitleTranslit.getText();
            AutoStepper.artistTranslit = txtArtistTranslit.getText();
            AutoStepper.genre = txtGenre.getText();
            dialog.dispose();
        });
        
        JButton btnCancel = new JButton("Annuler");
        btnCancel.addActionListener(ev -> dialog.dispose());

        panel.add(btnSave);
        panel.add(btnCancel);

        dialog.add(panel);
        dialog.setVisible(true);
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
                AutoStepper.customImagePath = txtCustomImage.getText().trim().isEmpty() ? null : txtCustomImage.getText();
                AutoStepper.customBackgroundPath = txtCustomBackground.getText().trim().isEmpty() ? null : txtCustomBackground.getText();
                float duration = ((Integer) spinDuration.getValue()).floatValue();
                String inputPath = txtInput.getText();
                String outputPath = txtOutput.getText();

                if (outputPath.endsWith("/") == false && outputPath.endsWith("\\") == false) {
                    outputPath += "/";
                }

                File inputFile = new File(inputPath);
                if (inputFile.exists()) {
                    if (inputFile.isFile()) {
                        AutoStepper.loadMetadata(inputFile.getAbsolutePath());
                        AutoStepper.myAS.analyzeUsingAudioRecordingStream(inputFile, duration, outputPath);
                    } else {
                        System.out.println("Traitement du répertoire : " + inputFile.getAbsolutePath());
                        File[] allfiles = inputFile.listFiles();
                        if (allfiles != null) {
                            for (File f : allfiles) {
                                String extCheck = f.getName().toLowerCase();
                                if (f.isFile() && (extCheck.endsWith(".mp3") || extCheck.endsWith(".wav"))) {
                                    AutoStepper.loadMetadata(f.getAbsolutePath());
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
