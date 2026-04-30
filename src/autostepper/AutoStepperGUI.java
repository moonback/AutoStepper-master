package autostepper;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.prefs.Preferences;

public class AutoStepperGUI extends JFrame {

    // --- Palette de couleurs premium ---
    private static final Color BG_DARK       = new Color(18, 18, 24);
    private static final Color BG_CARD       = new Color(28, 28, 38);
    private static final Color BG_INPUT      = new Color(38, 38, 50);
    private static final Color ACCENT        = new Color(99, 102, 241);   // Indigo
    private static final Color ACCENT_HOVER  = new Color(129, 132, 255);
    private static final Color ACCENT_GREEN  = new Color(16, 185, 129);
    private static final Color ACCENT_GREEN_H= new Color(52, 211, 153);
    private static final Color TEXT_PRIMARY   = new Color(230, 230, 245);
    private static final Color TEXT_SECONDARY = new Color(148, 148, 168);
    private static final Color BORDER_COLOR   = new Color(55, 55, 72);
    private static final Color LOG_GREEN     = new Color(52, 211, 153);

    // --- Composants ---
    private JTextField txtInput, txtOutput, txtCustomImage, txtCustomBackground;
    private JSpinner spinDuration;
    private JCheckBox chkHardMode;
    private JTextArea logArea;
    private JButton btnStart;
    private JLabel lblBannerPreview, lblBgPreview;
    private JProgressBar progressBar;
    private Preferences prefs = Preferences.userRoot().node(this.getClass().getName());

    public AutoStepperGUI() {
        setTitle("AutoStepper v1.7");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(780, 720);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(680, 600));

        // Panel racine
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(BG_DARK);
        root.setBorder(new EmptyBorder(16, 16, 16, 16));
        setContentPane(root);

        // ====== EN-TÊTE ======
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 14, 0));

        JLabel lblTitle = new JLabel("AutoStepper");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitle.setForeground(TEXT_PRIMARY);
        header.add(lblTitle, BorderLayout.WEST);

        JLabel lblSub = new JLabel("v1.7  —  par Maysson.D");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSub.setForeground(TEXT_SECONDARY);
        lblSub.setBorder(new EmptyBorder(10, 0, 0, 0));
        header.add(lblSub, BorderLayout.EAST);

        root.add(header, BorderLayout.NORTH);

        // ====== CENTRE (config + logs) ======
        JPanel center = new JPanel(new BorderLayout(0, 12));
        center.setOpaque(false);

        JPanel configArea = new JPanel();
        configArea.setOpaque(false);
        configArea.setLayout(new BoxLayout(configArea, BoxLayout.Y_AXIS));

        // --- Carte : Fichiers ---
        configArea.add(buildFilesCard());
        configArea.add(Box.createRigidArea(new Dimension(0, 10)));

        // --- Carte : Visuel ---
        configArea.add(buildVisualCard());
        configArea.add(Box.createRigidArea(new Dimension(0, 10)));

        // --- Carte : Options ---
        configArea.add(buildOptionsCard());
        configArea.add(Box.createRigidArea(new Dimension(0, 10)));

        center.add(configArea, BorderLayout.NORTH);

        // --- Zone de logs ---
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setBackground(new Color(12, 12, 18));
        logArea.setForeground(LOG_GREEN);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        logArea.setMargin(new Insets(12, 12, 12, 12));
        logArea.setCaretColor(LOG_GREEN);

        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setBorder(createCardBorder("Journal d'exécution"));
        scrollPane.getViewport().setBackground(new Color(12, 12, 18));
        center.add(scrollPane, BorderLayout.CENTER);

        root.add(center, BorderLayout.CENTER);

        // ====== BAS (progress + bouton) ======
        JPanel bottomPanel = new JPanel(new BorderLayout(0, 8));
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(new EmptyBorder(12, 0, 0, 0));

        progressBar = new JProgressBar();
        progressBar.setIndeterminate(false);
        progressBar.setPreferredSize(new Dimension(0, 4));
        progressBar.setBorderPainted(false);
        progressBar.setBackground(BG_CARD);
        progressBar.setForeground(ACCENT_GREEN);
        bottomPanel.add(progressBar, BorderLayout.NORTH);

        btnStart = createGradientButton("DÉMARRER LA GÉNÉRATION", ACCENT_GREEN, ACCENT_GREEN_H);
        btnStart.setPreferredSize(new Dimension(0, 52));
        btnStart.setFont(new Font("Segoe UI", Font.BOLD, 15));
        bottomPanel.add(btnStart, BorderLayout.CENTER);

        root.add(bottomPanel, BorderLayout.SOUTH);

        // ====== ÉVÉNEMENTS ======
        wireEvents();
        redirectSystemStreams();
        loadPreferences();
        setupValidation();
        setupDragAndDrop();
        setupImagePreviews();

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) { savePreferences(); }
        });
    }

    // ============================================================
    //  CONSTRUCTION DES CARTES
    // ============================================================

    private JPanel buildFilesCard() {
        JPanel card = createCard("Fichiers et Dossiers");
        card.setLayout(new GridBagLayout());
        GridBagConstraints g = defaultGbc();

        JButton btnIn = createBrowseBtn("btnBrowseInput");
        JButton btnOut = createBrowseBtn("btnBrowseOutput");

        txtInput = styledField(".");
        txtOutput = styledField(".");

        addRow(card, g, 0, "Musique / Dossier", txtInput, btnIn);
        addRow(card, g, 1, "Dossier de sortie",  txtOutput, btnOut);

        btnIn.addActionListener(e -> {
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

        btnOut.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser(txtOutput.getText());
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                txtOutput.setText(chooser.getSelectedFile().getAbsolutePath());
            }
        });

        return card;
    }

    private JPanel buildVisualCard() {
        JPanel card = createCard("Personnalisation Visuelle");
        card.setLayout(new GridBagLayout());
        GridBagConstraints g = defaultGbc();

        JButton btnBI = createBrowseBtn("btnBrowseImage");
        JButton btnBB = createBrowseBtn("btnBrowseBackground");

        // Banner row
        g.gridx = 0; g.gridy = 0; g.weightx = 0;
        card.add(styledLabel("Bannière"), g);
        txtCustomImage = styledField("");
        g.gridx = 1; g.weightx = 1.0;
        card.add(txtCustomImage, g);
        g.gridx = 2; g.weightx = 0;
        card.add(btnBI, g);

        lblBannerPreview = createPreviewLabel();
        g.gridx = 3;
        card.add(lblBannerPreview, g);

        // Background row
        g.gridx = 0; g.gridy = 1; g.weightx = 0;
        card.add(styledLabel("Fond / Vidéo"), g);
        txtCustomBackground = styledField("");
        g.gridx = 1; g.weightx = 1.0;
        card.add(txtCustomBackground, g);
        g.gridx = 2; g.weightx = 0;
        card.add(btnBB, g);

        lblBgPreview = createPreviewLabel();
        g.gridx = 3;
        card.add(lblBgPreview, g);

        // Wire browse buttons
        btnBI.addActionListener(e -> browseFile(txtCustomImage));
        btnBB.addActionListener(e -> browseFile(txtCustomBackground));

        return card;
    }

    private JPanel buildOptionsCard() {
        JPanel card = createCard("Options de Génération");
        card.setLayout(new FlowLayout(FlowLayout.LEFT, 16, 8));

        chkHardMode = new JCheckBox("Mode Difficile");
        chkHardMode.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        chkHardMode.setForeground(TEXT_PRIMARY);
        chkHardMode.setBackground(BG_CARD);
        chkHardMode.setFocusPainted(false);
        card.add(chkHardMode);

        card.add(styledLabel("Durée (0 = Tout) :"));
        spinDuration = new JSpinner(new SpinnerNumberModel(0, 0, 3600, 10));
        spinDuration.setPreferredSize(new Dimension(70, 28));
        spinDuration.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        card.add(spinDuration);

        JButton btnAdv = createAccentButton("Options Avancées");
        btnAdv.addActionListener(e -> showAdvancedOptionsDialog());
        card.add(btnAdv);

        return card;
    }

    // ============================================================
    //  COMPOSANTS RÉUTILISABLES
    // ============================================================

    private JPanel createCard(String title) {
        JPanel panel = new JPanel();
        panel.setBackground(BG_CARD);
        panel.setBorder(createCardBorder(title));
        return panel;
    }

    private Border createCardBorder(String title) {
        Border line = BorderFactory.createLineBorder(BORDER_COLOR, 1);
        Border titled = BorderFactory.createTitledBorder(line, "  " + title + "  ",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 12), ACCENT);
        Border padding = new EmptyBorder(6, 10, 10, 10);
        return BorderFactory.createCompoundBorder(titled, padding);
    }

    private JTextField styledField(String text) {
        JTextField tf = new JTextField(text);
        tf.setBackground(BG_INPUT);
        tf.setForeground(TEXT_PRIMARY);
        tf.setCaretColor(TEXT_PRIMARY);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(5, 8, 5, 8)));
        return tf;
    }

    private JLabel styledLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lbl.setForeground(TEXT_SECONDARY);
        return lbl;
    }

    private JLabel createPreviewLabel() {
        JLabel lbl = new JLabel("—");
        lbl.setPreferredSize(new Dimension(80, 28));
        lbl.setHorizontalAlignment(SwingConstants.CENTER);
        lbl.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lbl.setForeground(TEXT_SECONDARY);
        lbl.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        lbl.setOpaque(true);
        lbl.setBackground(BG_INPUT);
        return lbl;
    }

    private JButton createBrowseBtn(String name) {
        JButton btn = new JButton("...");
        btn.setName(name);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setPreferredSize(new Dimension(38, 28));
        btn.setForeground(TEXT_PRIMARY);
        btn.setBackground(BG_INPUT);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(ACCENT); }
            public void mouseExited(MouseEvent e)  { btn.setBackground(BG_INPUT); }
        });
        return btn;
    }

    private JButton createAccentButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(Color.WHITE);
        btn.setBackground(ACCENT);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(6, 16, 6, 16));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(ACCENT_HOVER); }
            public void mouseExited(MouseEvent e)  { btn.setBackground(ACCENT); }
        });
        return btn;
    }

    private JButton createGradientButton(String text, Color c1, Color c2) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, c1, getWidth(), 0, c2);
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private GridBagConstraints defaultGbc() {
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(5, 6, 5, 6);
        return g;
    }

    private void addRow(JPanel panel, GridBagConstraints g, int row, String label, JTextField field, JButton btn) {
        g.gridx = 0; g.gridy = row; g.weightx = 0;
        panel.add(styledLabel(label), g);
        g.gridx = 1; g.weightx = 1.0;
        panel.add(field, g);
        g.gridx = 2; g.weightx = 0;
        panel.add(btn, g);
    }

    // ============================================================
    //  ÉVÉNEMENTS
    // ============================================================

    private void wireEvents() {
        // Boutons Parcourir (Input et Output liés par nom)
        // On les retrouve par getName() dans addRow, mais ici on les câble directement
        // Input browse
        for (Component c : ((JPanel)getContentPane().getComponent(1)).getComponents()) {
            // skip - we wire in buildFilesCard children
        }

        btnStart.addActionListener(e -> startProcess());
    }

    private void browseFile(JTextField target) {
        JFileChooser chooser = new JFileChooser(target.getText());
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            target.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    // ============================================================
    //  PRÉFÉRENCES
    // ============================================================

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

    // ============================================================
    //  VALIDATION
    // ============================================================

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
        btnStart.setEnabled(!txtInput.getText().trim().isEmpty());
    }

    // ============================================================
    //  PRÉVISUALISATIONS
    // ============================================================

    private void setupImagePreviews() {
        DocumentListener pl = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { updateAllPreviews(); }
            public void removeUpdate(DocumentEvent e) { updateAllPreviews(); }
            public void changedUpdate(DocumentEvent e) { updateAllPreviews(); }
        };
        txtCustomImage.getDocument().addDocumentListener(pl);
        txtCustomBackground.getDocument().addDocumentListener(pl);
        updateAllPreviews();
    }

    private void updateAllPreviews() {
        updateImagePreview(txtCustomImage.getText(), lblBannerPreview, 80, 28);
        updateImagePreview(txtCustomBackground.getText(), lblBgPreview, 80, 28);
    }

    private void updateImagePreview(String path, JLabel label, int w, int h) {
        if (path == null || path.trim().isEmpty() || !new File(path).exists()) {
            label.setIcon(null);
            label.setText("—");
            return;
        }
        if (path.toLowerCase().endsWith(".mp4")) {
            label.setIcon(null);
            label.setText("VIDEO");
            label.setForeground(ACCENT_GREEN);
            return;
        }
        try {
            ImageIcon icon = new ImageIcon(path);
            Image img = icon.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
            label.setIcon(new ImageIcon(img));
            label.setText("");
            label.setForeground(TEXT_SECONDARY);
        } catch (Exception e) {
            label.setIcon(null);
            label.setText("Err");
        }
    }

    // ============================================================
    //  DRAG & DROP
    // ============================================================

    private void setupDragAndDrop() {
        enableDragAndDrop(txtInput);
        enableDragAndDrop(txtOutput);
        enableDragAndDrop(txtCustomImage);
        enableDragAndDrop(txtCustomBackground);
    }

    @SuppressWarnings("unchecked")
    private void enableDragAndDrop(JTextField textField) {
        textField.setTransferHandler(new TransferHandler() {
            public boolean canImport(TransferSupport s) {
                return s.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
            }
            public boolean importData(TransferSupport s) {
                if (!canImport(s)) return false;
                try {
                    Transferable t = s.getTransferable();
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
                } catch (Exception e) { return false; }
            }
        });
    }

    // ============================================================
    //  DIALOGUE OPTIONS AVANCÉES
    // ============================================================

    private void showAdvancedOptionsDialog() {
        JDialog dialog = new JDialog(this, "Options Avancées", true);
        dialog.setSize(420, 280);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BG_CARD);
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));
        GridBagConstraints g = defaultGbc();

        JTextField txtTT = styledField(AutoStepper.titleTranslit);
        JTextField txtST = styledField(AutoStepper.subTitleTranslit);
        JTextField txtAT = styledField(AutoStepper.artistTranslit);
        JTextField txtG  = styledField(AutoStepper.genre);

        String[] labels = {"Titre Translit", "Sous-titre Translit", "Artiste Translit", "Genre"};
        JTextField[] fields = {txtTT, txtST, txtAT, txtG};
        for (int i = 0; i < labels.length; i++) {
            g.gridx = 0; g.gridy = i; g.weightx = 0;
            panel.add(styledLabel(labels[i]), g);
            g.gridx = 1; g.weightx = 1.0;
            panel.add(fields[i], g);
        }

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnRow.setOpaque(false);

        JButton btnSave = createAccentButton("Enregistrer");
        btnSave.addActionListener(ev -> {
            AutoStepper.titleTranslit = txtTT.getText();
            AutoStepper.subTitleTranslit = txtST.getText();
            AutoStepper.artistTranslit = txtAT.getText();
            AutoStepper.genre = txtG.getText();
            dialog.dispose();
        });
        JButton btnCancel = createBrowseBtn("cancel");
        btnCancel.setText("Annuler");
        btnCancel.setPreferredSize(new Dimension(80, 28));
        btnCancel.addActionListener(ev -> dialog.dispose());

        btnRow.add(btnCancel);
        btnRow.add(btnSave);
        g.gridx = 0; g.gridy = labels.length; g.gridwidth = 2;
        panel.add(btnRow, g);

        dialog.setContentPane(panel);
        dialog.setVisible(true);
    }

    // ============================================================
    //  REDIRECTION DES LOGS
    // ============================================================

    private void redirectSystemStreams() {
        OutputStream out = new OutputStream() {
            public void write(int b) { updateLog(String.valueOf((char) b)); }
            public void write(byte[] b, int off, int len) { updateLog(new String(b, off, len)); }
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

    // ============================================================
    //  PROCESSUS PRINCIPAL
    // ============================================================

    private void startProcess() {
        btnStart.setEnabled(false);
        logArea.setText("");
        progressBar.setIndeterminate(true);

        new Thread(() -> {
            try {
                if (AutoStepper.minim == null) {
                    AutoStepper.minim = new ddf.minim.Minim(AutoStepper.myAS);
                }

                AutoStepper.HARDMODE = chkHardMode.isSelected();
                AutoStepper.customImagePath = txtCustomImage.getText().trim().isEmpty() ? null : txtCustomImage.getText();
                AutoStepper.customBackgroundPath = txtCustomBackground.getText().trim().isEmpty() ? null : txtCustomBackground.getText();
                float duration = ((Integer) spinDuration.getValue()).floatValue();
                String inputPath = txtInput.getText();
                String outputPath = txtOutput.getText();

                if (!outputPath.endsWith("/") && !outputPath.endsWith("\\")) {
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
                                String ext = f.getName().toLowerCase();
                                if (f.isFile() && (ext.endsWith(".mp3") || ext.endsWith(".wav"))) {
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
                SwingUtilities.invokeLater(() -> {
                    btnStart.setEnabled(true);
                    progressBar.setIndeterminate(false);
                    progressBar.setValue(100);
                });
            }
        }).start();
    }

    // ============================================================
    //  LANCEMENT
    // ============================================================

    public static void launch() {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {}

        SwingUtilities.invokeLater(() -> {
            AutoStepperGUI gui = new AutoStepperGUI();
            gui.setVisible(true);
        });
    }
}
