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
    private JTextField txtCustomImage;
    private JTextField txtCustomBackground;
    private JSpinner spinDuration;
    private JCheckBox chkHardMode;
    private JCheckBox chkUseTapper;
    private JTextArea logArea;
    private JButton btnStart;

    public AutoStepperGUI() {
        setTitle("AutoStepper v1.7 - Interface Graphique");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 650);
        setLocationRelativeTo(null);

        // Panel principal avec marges
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        setContentPane(mainPanel);

        // Titre de l'application
        JLabel lblTitle = new JLabel("AutoStepper - Générateur StepMania", SwingConstants.CENTER);
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 22));
        lblTitle.setForeground(new Color(50, 50, 50));
        mainPanel.add(lblTitle, BorderLayout.NORTH);

        // Panel de configuration central
        JPanel configPanel = new JPanel();
        configPanel.setLayout(new BoxLayout(configPanel, BoxLayout.Y_AXIS));
        
        // --- SECTION 1 : Fichiers et Dossiers ---
        JPanel filesPanel = new JPanel(new GridBagLayout());
        filesPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "📁 Fichiers et Dossiers"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 10, 8, 10);
        
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        filesPanel.add(new JLabel("Musique / Dossier :"), gbc);
        txtInput = new JTextField(".");
        gbc.gridx = 1; gbc.weightx = 1.0;
        filesPanel.add(txtInput, gbc);
        JButton btnBrowseInput = new JButton("Parcourir...");
        gbc.gridx = 2; gbc.weightx = 0;
        filesPanel.add(btnBrowseInput, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        filesPanel.add(new JLabel("Dossier de sortie :"), gbc);
        txtOutput = new JTextField(".");
        gbc.gridx = 1; gbc.weightx = 1.0;
        filesPanel.add(txtOutput, gbc);
        JButton btnBrowseOutput = new JButton("Parcourir...");
        gbc.gridx = 2; gbc.weightx = 0;
        filesPanel.add(btnBrowseOutput, gbc);
        
        configPanel.add(filesPanel);
        configPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // --- SECTION 2 : Personnalisation Visuelle ---
        JPanel visualPanel = new JPanel(new GridBagLayout());
        visualPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "🖼️ Personnalisation Visuelle"));
        
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        visualPanel.add(new JLabel("Image (Bannière) :"), gbc);
        txtCustomImage = new JTextField("");
        txtCustomImage.setToolTipText("Image étroite affichée dans le menu");
        gbc.gridx = 1; gbc.weightx = 1.0;
        visualPanel.add(txtCustomImage, gbc);
        JButton btnBrowseImage = new JButton("Parcourir...");
        gbc.gridx = 2; gbc.weightx = 0;
        visualPanel.add(btnBrowseImage, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        visualPanel.add(new JLabel("Image (Fond) :"), gbc);
        txtCustomBackground = new JTextField("");
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
        optionsPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "⚙️ Options de Génération"));
        
        chkHardMode = new JCheckBox("Mode Difficile");
        chkUseTapper = new JCheckBox("Utiliser Tap Manuel");
        optionsPanel.add(chkHardMode);
        optionsPanel.add(chkUseTapper);
        
        optionsPanel.add(new JLabel("Durée (sec) :"));
        spinDuration = new JSpinner(new SpinnerNumberModel(90, 10, 600, 10));
        optionsPanel.add(spinDuration);

        JButton btnAdvancedOptions = new JButton("Options Avancées...");
        optionsPanel.add(btnAdvancedOptions);

        btnAdvancedOptions.addActionListener(e -> showAdvancedOptionsDialog());
        
        configPanel.add(optionsPanel);

        // Zone de logs (Centre)
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setBackground(new Color(245, 245, 245));
        logArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        logArea.setMargin(new Insets(5, 5, 5, 5));
        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY), "📝 Journal d'exécution"));
        
        // Ajouter configPanel et scrollPane au centre de mainPanel
        JPanel centerWrapper = new JPanel(new BorderLayout(0, 15));
        centerWrapper.add(configPanel, BorderLayout.NORTH);
        centerWrapper.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(centerWrapper, BorderLayout.CENTER);

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
                AutoStepper.USETAPPER = chkUseTapper.isSelected();
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
