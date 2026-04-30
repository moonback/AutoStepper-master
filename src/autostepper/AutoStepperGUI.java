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
import java.awt.dnd.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import ddf.minim.*;
import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.ArrayList;
import java.util.prefs.Preferences;

public class AutoStepperGUI extends JFrame {

    // --- Palette de couleurs premium ---
    private static final Color BG_DARK = new Color(18, 18, 24);
    private static final Color BG_CARD = new Color(28, 28, 38);
    private static final Color BG_INPUT = new Color(38, 38, 50);
    private static final Color ACCENT = new Color(99, 102, 241); // Indigo
    private static final Color ACCENT_HOVER = new Color(129, 132, 255);
    private static final Color ACCENT_GREEN = new Color(16, 185, 129);
    private static final Color ACCENT_GREEN_H = new Color(52, 211, 153);
    private static final Color TEXT_PRIMARY = new Color(230, 230, 245);
    private static final Color TEXT_SECONDARY = new Color(148, 148, 168);
    private static final Color BORDER_COLOR = new Color(55, 55, 72);
    private static final Color LOG_GREEN = new Color(52, 211, 153);

    // --- Composants ---
    private JTextField txtInput, txtOutput, txtCustomImage, txtCustomBackground;
    private JSpinner spinDuration;
    private JCheckBox chkHardMode, chkSmartMines, chkDetectSilence, chkVariableBPM;
    private JTextArea logArea;
    private JButton btnStart;
    private JLabel lblBannerPreview, lblBgPreview;
    private JProgressBar progressBar;
    private JTable songTable;
    private SongTableModel songTableModel;
    private AudioPlayer currentPlayer;
    private WaveformPanel waveformPanel;
    private JButton btnPlay;
    private Preferences prefs = Preferences.userRoot().node(this.getClass().getName());

    // --- Variables du design multi-étape ---
    private CardLayout wizardLayout;
    private JPanel wizardPanel;
    private int currentStep = 0;
    private JButton btnNext, btnPrev;
    private JLabel lblStepTracker;
    // --- Data Model for per-song customization ---
    private static class SongEntry {
        File file;
        String customBanner = "";
        String customBG = "";

        SongEntry(File f) {
            this.file = f;
        }

        @Override
        public String toString() {
            return file.getName();
        }
    }

    private class SongTableModel extends javax.swing.table.AbstractTableModel {
        private java.util.ArrayList<SongEntry> entries = new java.util.ArrayList<>();
        private String[] columnNames = { "Musique", "Bannière", "Fond / Vidéo" };

        public void setEntries(java.util.List<File> files) {
            entries.clear();
            for (File f : files)
                entries.add(new SongEntry(f));
            fireTableDataChanged();
        }

        public SongEntry getEntry(int row) {
            return (row >= 0 && row < entries.size()) ? entries.get(row) : null;
        }

        public java.util.List<SongEntry> getEntries() {
            return entries;
        }

        @Override
        public int getRowCount() {
            return entries.size();
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public String getColumnName(int col) {
            return columnNames[col];
        }

        @Override
        public Object getValueAt(int row, int col) {
            SongEntry e = entries.get(row);
            switch (col) {
                case 0:
                    return e.file.getName();
                case 1:
                    return e.customBanner.isEmpty() ? "(Auto)" : new File(e.customBanner).getName();
                case 2:
                    return e.customBG.isEmpty() ? "(Auto)" : new File(e.customBG).getName();
                default:
                    return "";
            }
        }
    }

    public AutoStepperGUI() {
        setTitle("AutoStepper v1.8");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(860, 800);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(760, 680));

        // Panel racine
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(BG_DARK);
        root.setBorder(new EmptyBorder(16, 16, 16, 16));
        setContentPane(root);

        // ====== EN-TÊTE ======
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 14, 0));

        JLabel lblTitle = new JLabel("AutoStepper") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ACCENT);
                int[] px = { 0, 12, 8, 18, 6, 10, 0 };
                int[] py = { 10, 10, 18, 18, 30, 30, 20 };
                g2.fillPolygon(px, py, 7);
                g2.dispose();
                g.translate(24, 0);
                super.paintComponent(g);
            }
        };
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 32));
        lblTitle.setForeground(TEXT_PRIMARY);
        header.add(lblTitle, BorderLayout.WEST);

        JLabel lblSub = new JLabel("v1.8  —  par Maysson.D  ");
        lblSub.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        lblSub.setForeground(TEXT_SECONDARY);
        lblSub.setBorder(new EmptyBorder(12, 0, 0, 0));

        lblStepTracker = new JLabel("Étape 1 / 2");
        lblStepTracker.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblStepTracker.setForeground(ACCENT_GREEN);
        lblStepTracker.setBorder(new EmptyBorder(12, 12, 0, 20));

        JPanel eastHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        eastHeader.setOpaque(false);
        eastHeader.add(lblStepTracker);
        eastHeader.add(lblSub);

        header.add(eastHeader, BorderLayout.EAST);

        root.add(header, BorderLayout.NORTH);

        // ====== CENTRE (config + logs) ======
        JPanel center = new JPanel(new BorderLayout(0, 12));
        center.setOpaque(false);

        // ====== CENTRE (wizard) ======
        wizardLayout = new CardLayout();
        wizardPanel = new JPanel(wizardLayout);
        wizardPanel.setOpaque(false);

        // --- ÉTAPE 1 : Fichiers & Options ---
        JPanel step1 = new JPanel();
        step1.setLayout(new BoxLayout(step1, BoxLayout.Y_AXIS));
        step1.setOpaque(false);
        step1.add(buildFilesCard());
        step1.add(Box.createRigidArea(new Dimension(0, 10)));
        step1.add(buildOptionsCard());

        // --- ÉTAPE 2 : Musiques & Personnalisation ---
        JPanel step2 = new JPanel();
        step2.setLayout(new BoxLayout(step2, BoxLayout.Y_AXIS));
        step2.setOpaque(false);
        step2.add(buildSongListCard());
        step2.add(Box.createRigidArea(new Dimension(0, 10)));
        step2.add(buildVisualCard());

        JScrollPane scroll1 = new JScrollPane(step1);
        scroll1.setBorder(null);
        scroll1.setOpaque(false);
        scroll1.getViewport().setOpaque(false);
        scroll1.getVerticalScrollBar().setUnitIncrement(16);
        scroll1.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        JScrollPane scroll2 = new JScrollPane(step2);
        scroll2.setBorder(null);
        scroll2.setOpaque(false);
        scroll2.getViewport().setOpaque(false);
        scroll2.getVerticalScrollBar().setUnitIncrement(16);
        scroll2.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        wizardPanel.add(scroll1, "STEP1");
        wizardPanel.add(scroll2, "STEP2");

        center.add(wizardPanel, BorderLayout.CENTER);

        root.add(center, BorderLayout.CENTER);

        // ====== BAS (progress + bouton) ======
        JPanel bottomPanel = new JPanel(new BorderLayout(0, 8));
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(new EmptyBorder(12, 0, 0, 0));

        progressBar = new JProgressBar();
        progressBar.setIndeterminate(false);
        progressBar.setPreferredSize(new Dimension(0, 6));
        progressBar.setBorderPainted(false);
        progressBar.setBackground(BG_CARD);
        progressBar.setForeground(ACCENT_GREEN);

        // Journal en bas réduit pour laisser place au scroll
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setBackground(new Color(12, 12, 18));
        logArea.setForeground(LOG_GREEN);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 11));
        logArea.setMargin(new java.awt.Insets(8, 8, 8, 8));

        JScrollPane scrollLog = new JScrollPane(logArea);
        scrollLog.setBorder(null);
        scrollLog.setPreferredSize(new Dimension(0, 120));

        JPanel bottomArea = new JPanel(new BorderLayout(0, 5));
        bottomArea.setOpaque(false);
        bottomArea.add(progressBar, BorderLayout.NORTH);
        bottomArea.add(scrollLog, BorderLayout.CENTER);

        btnStart = createGradientButton("DÉMARRER", ACCENT_GREEN, ACCENT_GREEN_H);
        btnStart.setPreferredSize(new Dimension(220, 56));
        btnStart.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnStart.setVisible(false);

        btnPrev = createAccentButton("◄ Précédent");
        btnPrev.setPreferredSize(new Dimension(140, 56));
        btnPrev.setVisible(false);

        btnNext = createAccentButton("Suivant ►");
        btnNext.setPreferredSize(new Dimension(140, 56));

        btnPrev.addActionListener(e -> setStep(0));
        btnNext.addActionListener(e -> setStep(1));

        JButton btnReset = new JButton("Réinitialiser");
        btnReset.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnReset.setForeground(TEXT_SECONDARY);
        btnReset.setBackground(BG_CARD);
        btnReset.setFocusPainted(false);
        btnReset.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        btnReset.setPreferredSize(new Dimension(140, 56));
        btnReset.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnReset.addActionListener(e -> resetAll());

        JButton btnHelp = new JButton("Aide & Notice");
        btnHelp.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnHelp.setForeground(TEXT_PRIMARY);
        btnHelp.setBackground(new Color(59, 130, 246)); // Bleu
        btnHelp.setFocusPainted(false);
        btnHelp.setBorder(BorderFactory.createLineBorder(new Color(37, 99, 235)));
        btnHelp.setPreferredSize(new Dimension(140, 56));
        btnHelp.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnHelp.addActionListener(e -> showHelpDialog());

        JPanel leftActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftActions.setOpaque(false);
        leftActions.add(btnReset);
        leftActions.add(btnHelp);

        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightActions.setOpaque(false);
        rightActions.add(btnPrev);
        rightActions.add(btnNext);
        rightActions.add(btnStart);

        JPanel actionPanel = new JPanel(new BorderLayout(10, 0));
        actionPanel.setOpaque(false);
        actionPanel.add(leftActions, BorderLayout.WEST);
        actionPanel.add(rightActions, BorderLayout.EAST);

        bottomArea.add(actionPanel, BorderLayout.SOUTH);
        root.add(bottomArea, BorderLayout.SOUTH);

        // ====== ÉVÉNEMENTS ======
        wireEvents();
        redirectSystemStreams();
        loadPreferences();
        setupValidation();
        setupDragAndDrop();
        setupImagePreviews();
        checkForUpdates();

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                savePreferences();
            }
        });
    }

    // ============================================================
    // NAVIGATION WIZARD
    // ============================================================

    private void setStep(int step) {
        currentStep = step;
        if (currentStep == 0) {
            wizardLayout.show(wizardPanel, "STEP1");
            btnPrev.setVisible(false);
            btnNext.setVisible(true);
            btnStart.setVisible(false);
            lblStepTracker.setText("Étape 1 / 2");
        } else if (currentStep == 1) {
            scanInput();
            wizardLayout.show(wizardPanel, "STEP2");
            btnPrev.setVisible(true);
            btnNext.setVisible(false);
            btnStart.setVisible(true);
            lblStepTracker.setText("Étape 2 / 2");
        }
    }

    // ============================================================
    // CONSTRUCTION DES CARTES
    // ============================================================

    private JPanel buildFilesCard() {
        JPanel card = createCard("Fichiers et Dossiers");
        card.setLayout(new GridBagLayout());
        GridBagConstraints g = defaultGbc();

        JButton btnIn = createBrowseBtn("btnBrowseInput");
        JButton btnOut = createBrowseBtn("btnBrowseOutput");

        txtInput = styledField(".");
        txtInput.setToolTipText("Glissez-déposez vos fichiers audio ici");
        txtOutput = styledField(".");
        txtOutput.setToolTipText("Dossier où sera créé le pack StepMania");

        addRow(card, g, 0, "Musique / Dossier", txtInput, btnIn);
        addRow(card, g, 1, "Dossier de sortie", txtOutput, btnOut);

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

        // Auto-scan on path change
        txtInput.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                scanInput();
            }

            public void removeUpdate(DocumentEvent e) {
                scanInput();
            }

            public void changedUpdate(DocumentEvent e) {
                scanInput();
            }
        });

        return card;
    }

    private JPanel buildSongListCard() {
        JPanel card = createCard("\uD83C\uDFB5 Musiques Détectées (Sélectionnez pour personnaliser)");
        card.setLayout(new BorderLayout(0, 8));
        card.setPreferredSize(new Dimension(0, 180));

        songTableModel = new SongTableModel();
        songTable = new JTable(songTableModel);
        songTable.setBackground(BG_INPUT);
        songTable.setForeground(TEXT_PRIMARY);
        songTable.setGridColor(BORDER_COLOR);
        songTable.setSelectionBackground(ACCENT);
        songTable.setSelectionForeground(Color.WHITE);
        songTable.setRowHeight(25);
        songTable.getTableHeader().setBackground(BG_CARD);
        songTable.getTableHeader().setForeground(ACCENT);
        songTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

        songTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting())
                updateVisualCardFromSelection();
        });

        JScrollPane scroll = new JScrollPane(songTable);
        scroll.getViewport().setBackground(BG_INPUT);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        card.add(scroll, BorderLayout.CENTER);

        JButton btnScan = createAccentButton("Actualiser la liste");
        btnScan.addActionListener(e -> scanInput());
        JPanel bp = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        bp.setOpaque(false);
        bp.add(btnScan);
        card.add(bp, BorderLayout.SOUTH);

        return card;
    }

    private void scanInput() {
        String path = txtInput.getText().trim();
        if (path.isEmpty())
            return;
        File f = new File(path);
        java.util.ArrayList<File> found = new java.util.ArrayList<>();
        if (f.isFile() && (path.toLowerCase().endsWith(".mp3") || path.toLowerCase().endsWith(".wav"))) {
            found.add(f);
        } else if (f.isDirectory()) {
            File[] files = f.listFiles();
            if (files != null) {
                for (File sub : files) {
                    if (sub.isFile() && (sub.getName().toLowerCase().endsWith(".mp3")
                            || sub.getName().toLowerCase().endsWith(".wav"))) {
                        found.add(sub);
                    }
                }
            }
        }
        songTableModel.setEntries(found);
        if (found.size() > 0)
            songTable.setRowSelectionInterval(0, 0);
    }

    private void updateVisualCardFromSelection() {
        int row = songTable.getSelectedRow();
        SongEntry entry = songTableModel.getEntry(row);
        if (entry != null) {
            txtCustomImage.setText(entry.customBanner);
            txtCustomBackground.setText(entry.customBG);
            updateDropZonePreview(entry.customBanner, lblBannerPreview, "Bannière Spécifique");
            updateDropZonePreview(entry.customBG, lblBgPreview, "Fond Spécifique");

            // Mettre à jour le lecteur audio
            waveformPanel.setSong(entry.file);
        }
    }

    private JPanel buildVisualCard() {
        JPanel card = createCard("\uD83C\uDFA7 Édition & Prévisualisation");
        card.setLayout(new BorderLayout(0, 10));

        JPanel dropZones = new JPanel(new GridLayout(1, 2, 14, 0));
        dropZones.setOpaque(false);

        txtCustomImage = styledField("");
        txtCustomBackground = styledField("");
        lblBannerPreview = new JLabel();
        lblBgPreview = new JLabel();

        JPanel dropBanner = createDropZone("\uD83D\uDDBC\uFE0F Bannière", "Glissez une image ici", txtCustomImage,
                lblBannerPreview);
        JPanel dropBg = createDropZone("\uD83C\uDFA8 Fond / Vidéo", "Glissez une image ou vidéo ici",
                txtCustomBackground, lblBgPreview);

        dropZones.add(dropBanner);
        dropZones.add(dropBg);
        card.add(dropZones, BorderLayout.NORTH);

        // Audio Preview Panel
        JPanel audioPanel = new JPanel(new BorderLayout(10, 0));
        audioPanel.setOpaque(false);
        audioPanel.setPreferredSize(new Dimension(0, 100));

        waveformPanel = new WaveformPanel();
        audioPanel.add(waveformPanel, BorderLayout.CENTER);

        btnPlay = new JButton("\u25B6") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(currentPlayer != null && currentPlayer.isPlaying() ? ACCENT_GREEN : ACCENT);
                g2.fillOval(4, 4, getWidth() - 8, getHeight() - 8);
                g2.setColor(Color.WHITE);
                if (currentPlayer != null && currentPlayer.isPlaying()) {
                    g2.fillRect(getHeight() / 2 - 6, getHeight() / 2 - 8, 4, 16);
                    g2.fillRect(getHeight() / 2 + 2, getHeight() / 2 - 8, 4, 16);
                } else {
                    int[] px = { getHeight() / 2 - 4, getHeight() / 2 - 4, getHeight() / 2 + 8 };
                    int[] py = { getHeight() / 2 - 8, getHeight() / 2 + 8, getHeight() / 2 };
                    g2.fillPolygon(px, py, 3);
                }
                g2.dispose();
            }
        };
        btnPlay.setPreferredSize(new Dimension(60, 60));
        btnPlay.setFocusPainted(false);
        btnPlay.setBorderPainted(false);
        btnPlay.setContentAreaFilled(false);
        btnPlay.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnPlay.addActionListener(e -> togglePlay());

        audioPanel.add(btnPlay, BorderLayout.WEST);
        card.add(audioPanel, BorderLayout.CENTER);

        return card;
    }

    private class WaveformPanel extends JPanel {
        private float[] energyHistory; // RMS energy accumulated over time
        private int energyWriteIndex = 0;
        private Timer animTimer;

        public WaveformPanel() {
            setOpaque(false);
            setPreferredSize(new Dimension(0, 80));
            animTimer = new Timer(33, e -> {
                captureEnergy();
                repaint();
            });
        }

        private void captureEnergy() {
            if (currentPlayer == null || !currentPlayer.isPlaying())
                return;
            if (energyHistory == null || currentPlayer.length() <= 0)
                return;

            // Map current position to a slot in the history array
            float progress = (float) currentPlayer.position() / currentPlayer.length();
            int slot = (int) (progress * energyHistory.length);
            if (slot < 0)
                slot = 0;
            if (slot >= energyHistory.length)
                slot = energyHistory.length - 1;

            // Calculate RMS from the live mix buffer
            float rms = currentPlayer.mix.level();
            energyHistory[slot] = Math.max(energyHistory[slot], rms);
        }

        public void setSong(File file) {
            if (file == null || !file.exists())
                return;

            if (currentPlayer != null) {
                currentPlayer.close();
                currentPlayer = null;
            }

            // Reset waveform history
            energyHistory = new float[600];

            System.out.println("Lecture de l'aperçu : " + file.getName());

            new Thread(() -> {
                try {
                    if (AutoStepper.minim == null) {
                        AutoStepper.minim = new Minim(AutoStepper.myAS);
                    }
                    currentPlayer = AutoStepper.minim.loadFile(file.getAbsolutePath(), 1024);
                    System.out.println("Audio chargé (" + (currentPlayer.length() / 1000) + "s).");

                    SwingUtilities.invokeLater(() -> {
                        animTimer.start();
                        repaint();
                    });
                } catch (Exception e) {
                    System.err.println("Erreur lors du chargement : " + e.getMessage());
                }
            }).start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int mid = h / 2;

            // Background rounded
            g2.setColor(BG_INPUT);
            g2.fillRoundRect(0, 0, w, h, 12, 12);

            if (currentPlayer != null) {
                // --- Draw accumulated energy history as waveform bars ---
                if (energyHistory != null) {
                    float step = (float) energyHistory.length / w;
                    for (int x = 0; x < w; x++) {
                        int idx = (int) (x * step);
                        if (idx >= energyHistory.length)
                            idx = energyHistory.length - 1;
                        float val = energyHistory[idx];
                        if (val > 0) {
                            int amp = (int) (val * mid * 2.5f);
                            if (amp > mid - 2)
                                amp = mid - 2;
                            g2.setColor(ACCENT.darker());
                            g2.drawLine(x, mid - amp, x, mid + amp);
                        }
                    }
                }

                // --- Draw live oscilloscope from current buffer ---
                if (currentPlayer.isPlaying()) {
                    float[] mixBuf = currentPlayer.mix.toArray();
                    g2.setColor(ACCENT);
                    g2.setStroke(new BasicStroke(1.5f));
                    int bufLen = mixBuf.length;
                    int prevY = mid;
                    for (int x = 0; x < w; x++) {
                        int bufIdx = (int) ((float) x / w * bufLen);
                        if (bufIdx >= bufLen)
                            bufIdx = bufLen - 1;
                        int y = mid - (int) (mixBuf[bufIdx] * mid * 0.9f);
                        if (x > 0)
                            g2.drawLine(x - 1, prevY, x, y);
                        prevY = y;
                    }
                    g2.setStroke(new BasicStroke(1f));
                }

                // --- Playhead ---
                float progress = (float) currentPlayer.position() / Math.max(1, currentPlayer.length());
                int px = (int) (progress * w);
                g2.setColor(ACCENT_GREEN);
                g2.setStroke(new BasicStroke(2f));
                g2.drawLine(px, 2, px, h - 2);
                g2.setStroke(new BasicStroke(1f));

                // --- Time label ---
                int posSec = currentPlayer.position() / 1000;
                int lenSec = currentPlayer.length() / 1000;
                String timeStr = String.format("%d:%02d / %d:%02d", posSec / 60, posSec % 60, lenSec / 60, lenSec % 60);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
                g2.setColor(TEXT_SECONDARY);
                g2.drawString(timeStr, w - 90, h - 6);

                // --- Step arrows preview ---
                if (currentPlayer.isPlaying()) {
                    drawStepPreview(g2, px, mid);
                }
            } else {
                g2.setColor(TEXT_SECONDARY);
                g2.setFont(new Font("Segoe UI", Font.ITALIC, 12));
                g2.drawString("Sélectionnez une musique puis appuyez sur ▶", 20, mid + 5);
            }

            // Center line
            g2.setColor(new Color(255, 255, 255, 30));
            g2.drawLine(0, mid, w, mid);

            g2.dispose();
        }

        private void drawStepPreview(Graphics2D g2, int px, int mid) {
            g2.setFont(new Font("Segoe UI", Font.BOLD, 22));
            String[] arrows = { "←", "↓", "↑", "→" };
            Color[] colors = {
                    new Color(239, 68, 68), // Gauche: Rouge
                    new Color(59, 130, 246), // Bas: Bleu
                    new Color(16, 185, 129), // Haut: Vert
                    new Color(245, 158, 11) // Droite: Orange
            };

            // Empêcher l'affichage de sortir de l'écran à droite
            int startX = px + 20;
            if (startX > getWidth() - 110)
                startX = px - 110;

            long time = System.currentTimeMillis();
            float audioLevel = currentPlayer.mix.level() * 5f; // Amplification pour l'effet visuel

            for (int i = 0; i < 4; i++) {
                int ax = startX + (i * 24);
                int ay = mid + 5; // Position de la cible

                // 1. Dessiner les cibles "fantômes"
                g2.setColor(new Color(255, 255, 255, 40));
                g2.drawString(arrows[i], ax, ay);

                // 2. Simuler une note défilante
                double speed = 0.12; // Vitesse réduite pour une meilleure lisibilité
                int distance = 180; // Distance totale de parcours

                // Décalage pour chaque flèche pour éviter qu'elles arrivent toutes en même
                // temps
                int scrollY = (int) ((time * speed + (i * 45)) % distance);
                int noteY = (mid + (distance / 2)) - scrollY; // La note monte

                // 3. Effet de Hit / Flash
                if (Math.abs(noteY - ay) < 15 && audioLevel > 0.3f) {
                    // Lueur d'impact
                    g2.setColor(new Color(colors[i].getRed(), colors[i].getGreen(), colors[i].getBlue(),
                            100 + (int) (Math.min(1f, audioLevel) * 100)));
                    g2.fillRoundRect(ax - 2, ay - 20, 24, 24, 8, 8);

                    // Flèche blanche éclatante
                    g2.setColor(Color.WHITE);
                    g2.drawString(arrows[i], ax, ay);
                }
                // 4. Dessiner la note seulement si elle n'a pas encore dépassé la cible de trop
                else if (noteY >= ay - 15) {
                    // Fondu en approche
                    int alpha = Math.min(255, Math.max(0, (noteY - (ay - 15)) * 8));
                    g2.setColor(new Color(colors[i].getRed(), colors[i].getGreen(), colors[i].getBlue(), alpha));
                    g2.drawString(arrows[i], ax, noteY);
                }
            }
        }
    }

    private void togglePlay() {
        if (currentPlayer == null) {
            System.out.println("Aucun fichier audio chargé.");
            return;
        }
        if (currentPlayer.isPlaying()) {
            currentPlayer.pause();
            System.out.println("Pause.");
        } else {
            currentPlayer.play();
            System.out.println("Lecture en cours...");
        }
        btnPlay.repaint();
    }

    private class DropZonePanel extends JPanel {
        private boolean hovering = false;
        private String title, hint;
        private JTextField field;
        private JLabel preview;

        public DropZonePanel(String title, String hint, JTextField field, JLabel preview) {
            this.title = title;
            this.hint = hint;
            this.field = field;
            this.preview = preview;
            setLayout(new BorderLayout(0, 6));
            setOpaque(false);
            setBorder(new EmptyBorder(12, 12, 12, 12));

            JLabel lblTitle = new JLabel(title);
            lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
            lblTitle.setForeground(TEXT_PRIMARY);
            lblTitle.setHorizontalAlignment(SwingConstants.CENTER);

            preview.setPreferredSize(new Dimension(140, 100));
            preview.setHorizontalAlignment(SwingConstants.CENTER);
            preview.setVerticalAlignment(SwingConstants.CENTER);
            preview.setFont(new Font("Segoe UI", Font.ITALIC, 11));
            preview.setForeground(TEXT_SECONDARY);
            preview.setText(hint);

            JButton btnBrowse = createAccentButton("Parcourir...");
            btnBrowse.addActionListener(e -> {
                browseFile(field);
                updateDropZonePreview(field.getText(), preview, hint);
            });

            JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
            bottom.setOpaque(false);
            bottom.add(btnBrowse);

            add(lblTitle, BorderLayout.NORTH);
            add(preview, BorderLayout.CENTER);
            add(bottom, BorderLayout.SOUTH);

            setTransferHandler(new TransferHandler() {
                public boolean canImport(TransferSupport s) {
                    boolean ok = s.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
                    setHovering(ok);
                    return ok;
                }

                public boolean importData(TransferSupport s) {
                    try {
                        List<File> files = (List<File>) s.getTransferable()
                                .getTransferData(DataFlavor.javaFileListFlavor);
                        if (files != null && !files.isEmpty()) {
                            String path = files.get(0).getAbsolutePath();
                            field.setText(path);
                            updateDropZonePreview(path, preview, hint);
                            validateInputs();
                        }
                        setHovering(false);
                        return true;
                    } catch (Exception e) {
                        return false;
                    }
                }
            });

            field.getDocument().addDocumentListener(new DocumentListener() {
                public void insertUpdate(DocumentEvent e) {
                    updateEntryFromField();
                    updateDropZonePreview(field.getText(), preview, hint);
                }

                public void removeUpdate(DocumentEvent e) {
                    updateEntryFromField();
                    updateDropZonePreview(field.getText(), preview, hint);
                }

                public void changedUpdate(DocumentEvent e) {
                    updateEntryFromField();
                    updateDropZonePreview(field.getText(), preview, hint);
                }
            });
        }

        public void setHovering(boolean h) {
            this.hovering = h;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(hovering ? new Color(99, 102, 241, 25) : BG_INPUT);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
            float[] dash = { 6f, 4f };
            g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10f, dash, 0f));
            g2.setColor(hovering ? ACCENT : BORDER_COLOR);
            g2.drawRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 14, 14);
            g2.dispose();
        }
    }

    private JPanel createDropZone(String title, String hint, JTextField linkedField, JLabel previewLabel) {
        return new DropZonePanel(title, hint, linkedField, previewLabel);
    }

    private void updateEntryFromField() {
        if (songTable == null || songTableModel == null)
            return;
        int row = songTable.getSelectedRow();
        SongEntry entry = songTableModel.getEntry(row);
        if (entry != null) {
            entry.customBanner = txtCustomImage.getText();
            entry.customBG = txtCustomBackground.getText();
            songTableModel.fireTableRowsUpdated(row, row);
        }
    }

    private void updateDropZonePreview(String path, JLabel label, String defaultHint) {
        if (path == null || path.trim().isEmpty() || !new File(path).exists()) {
            label.setIcon(null);
            label.setText(defaultHint);
            label.setForeground(TEXT_SECONDARY);
            return;
        }
        if (path.toLowerCase().endsWith(".mp4")) {
            label.setIcon(null);
            label.setText("\u25B6 VIDEO");
            label.setFont(new Font("Segoe UI", Font.BOLD, 14));
            label.setForeground(ACCENT_GREEN);
            return;
        }
        try {
            ImageIcon icon = new ImageIcon(path);
            Image img = icon.getImage().getScaledInstance(130, 90, Image.SCALE_SMOOTH);
            label.setIcon(new ImageIcon(img));
            label.setText("");
        } catch (Exception e) {
            label.setIcon(null);
            label.setText("Aperçu indisponible");
            label.setForeground(TEXT_SECONDARY);
        }
    }

    private JPanel buildOptionsCard() {
        JPanel card = createCard("Options de Génération & Algorithme");
        card.setLayout(new FlowLayout(FlowLayout.LEFT, 16, 8));

        chkHardMode = createStyledCheckBox("Mode Difficile");
        card.add(chkHardMode);

        chkSmartMines = createStyledCheckBox("Mines Intelligentes (Énergie)");
        card.add(chkSmartMines);

        chkDetectSilence = createStyledCheckBox("Couper les Silences");
        card.add(chkDetectSilence);

        chkVariableBPM = createStyledCheckBox("BPM Variable (Expérimental)");
        card.add(chkVariableBPM);

        card.add(styledLabel("Durée (0 = Tout) :"));
        spinDuration = new JSpinner(new SpinnerNumberModel(0, 0, 3600, 10));
        spinDuration.setPreferredSize(new Dimension(70, 28));
        spinDuration.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        card.add(spinDuration);

        return card;
    }

    private JCheckBox createStyledCheckBox(String text) {
        JCheckBox chk = new JCheckBox(text);
        chk.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        chk.setForeground(TEXT_PRIMARY);
        chk.setBackground(BG_CARD);
        chk.setFocusPainted(false);
        return chk;
    }

    // ============================================================
    // COMPOSANTS RÉUTILISABLES
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
        lbl.setPreferredSize(new Dimension(140, 100));
        lbl.setHorizontalAlignment(SwingConstants.CENTER);
        lbl.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lbl.setForeground(TEXT_SECONDARY);
        lbl.setOpaque(false);
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
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(ACCENT);
            }

            public void mouseExited(MouseEvent e) {
                btn.setBackground(BG_INPUT);
            }
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
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(ACCENT_HOVER);
            }

            public void mouseExited(MouseEvent e) {
                btn.setBackground(ACCENT);
            }
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
        g.gridx = 0;
        g.gridy = row;
        g.weightx = 0;
        panel.add(styledLabel(label), g);
        g.gridx = 1;
        g.weightx = 1.0;
        panel.add(field, g);
        g.gridx = 2;
        g.weightx = 0;
        panel.add(btn, g);
    }

    // ============================================================
    // ÉVÉNEMENTS
    // ============================================================

    private void wireEvents() {
        // Boutons Parcourir (Input et Output liés par nom)
        // On les retrouve par getName() dans addRow, mais ici on les câble directement
        // Input browse
        for (Component c : ((JPanel) getContentPane().getComponent(1)).getComponents()) {
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
    // PRÉFÉRENCES
    // ============================================================

    private void loadPreferences() {
        txtInput.setText(prefs.get("input", "."));
        txtOutput.setText(prefs.get("output", "."));
        txtCustomImage.setText(prefs.get("customImage", ""));
        txtCustomBackground.setText(prefs.get("customBackground", ""));
        chkHardMode.setSelected(prefs.getBoolean("hardMode", false));
        chkSmartMines.setSelected(prefs.getBoolean("smartMines", true));
        chkDetectSilence.setSelected(prefs.getBoolean("detectSilence", true));
        chkVariableBPM.setSelected(prefs.getBoolean("variableBPM", true));
        spinDuration.setValue(prefs.getInt("duration", 0));
    }

    private void savePreferences() {
        prefs.put("input", txtInput.getText());
        prefs.put("output", txtOutput.getText());
        prefs.put("customImage", txtCustomImage.getText());
        prefs.put("customBackground", txtCustomBackground.getText());
        prefs.putBoolean("hardMode", chkHardMode.isSelected());
        prefs.putBoolean("smartMines", chkSmartMines.isSelected());
        prefs.putBoolean("detectSilence", chkDetectSilence.isSelected());
        prefs.putBoolean("variableBPM", chkVariableBPM.isSelected());
        prefs.putInt("duration", (Integer) spinDuration.getValue());
    }

    // ============================================================
    // VALIDATION
    // ============================================================

    private void setupValidation() {
        DocumentListener validator = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                validateInputs();
            }

            public void removeUpdate(DocumentEvent e) {
                validateInputs();
            }

            public void changedUpdate(DocumentEvent e) {
                validateInputs();
            }
        };
        txtInput.getDocument().addDocumentListener(validator);
        validateInputs();
    }

    private void validateInputs() {
        btnStart.setEnabled(!txtInput.getText().trim().isEmpty());
    }

    // ============================================================
    // PRÉVISUALISATIONS
    // ============================================================

    private void setupImagePreviews() {
        // Previews are now managed inside createDropZone listeners
        updateDropZonePreview(txtCustomImage.getText(), lblBannerPreview, "Glissez une image ici");
        updateDropZonePreview(txtCustomBackground.getText(), lblBgPreview, "Glissez une image ou vidéo ici");
    }

    // ============================================================
    // DRAG & DROP
    // ============================================================

    private void setupDragAndDrop() {
        enableDragAndDrop(txtInput);
        enableDragAndDrop(txtOutput);
        enableDragAndDrop(txtCustomImage);
        enableDragAndDrop(txtCustomBackground);
    }

    private static final String CURRENT_VERSION = "v1.8";
    private static final String UPDATE_URL = "https://api.github.com/repos/moonback/AutoStepper-master/releases/latest";

    private void checkForUpdates() {
        new Thread(() -> {
            try {
                System.out.println("Recherche de mises à jour en cours via GitHub...");
                java.net.URL url = new java.net.URL(UPDATE_URL);
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/vnd.github.v3+json");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                if (conn.getResponseCode() == 200) {
                    java.io.BufferedReader in = new java.io.BufferedReader(
                            new java.io.InputStreamReader(conn.getInputStream()));
                    String inputLine;
                    StringBuilder content = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        content.append(inputLine);
                    }
                    in.close();

                    String json = content.toString();
                    String searchKey = "\"tag_name\":";
                    int idx = json.indexOf(searchKey);
                    if (idx != -1) {
                        int start = json.indexOf("\"", idx + searchKey.length()) + 1;
                        int end = json.indexOf("\"", start);
                        String latestVersion = json.substring(start, end);

                        if (CURRENT_VERSION.equals(latestVersion)) {
                            System.out.println("Mise à jour automatique : Vous disposez de la dernière version ("
                                    + CURRENT_VERSION + ").");
                        } else {
                            System.out.println("Mise à jour automatique : Une nouvelle version est disponible ("
                                    + latestVersion + ") !");
                            System.out.println("Veuillez consulter le dépôt GitHub pour la télécharger.");
                            SwingUtilities.invokeLater(() -> {
                                JOptionPane.showMessageDialog(this,
                                        "Une nouvelle version (" + latestVersion + ") d'AutoStepper est disponible !\n"
                                                +
                                                "Vous utilisez actuellement la version " + CURRENT_VERSION + ".",
                                        "Mise à jour disponible",
                                        JOptionPane.INFORMATION_MESSAGE);
                            });
                        }
                    } else {
                        System.out.println("Impossible de lire la version depuis le serveur.");
                    }
                } else {
                    System.out.println(
                            "Aucune information de mise à jour trouvée (Code: " + conn.getResponseCode() + ").");
                }
            } catch (Exception e) {
                System.out.println("Impossible de se connecter au serveur de mise à jour (" + e.getMessage() + ").");
            }
        }).start();
    }

    @SuppressWarnings("unchecked")
    private void enableDragAndDrop(JTextField textField) {
        textField.setTransferHandler(new TransferHandler() {
            public boolean canImport(TransferSupport s) {
                boolean ok = s.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
                if (ok)
                    textField.setBorder(BorderFactory.createLineBorder(ACCENT, 2));
                return ok;
            }

            @Override
            protected void exportDone(JComponent source, Transferable data, int action) {
                textField.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER_COLOR),
                        new EmptyBorder(5, 8, 5, 8)));
            }

            public boolean importData(TransferSupport s) {
                textField.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER_COLOR),
                        new EmptyBorder(5, 8, 5, 8)));
                if (!canImport(s))
                    return false;
                try {
                    Transferable t = s.getTransferable();
                    List<File> files = (List<File>) t.getTransferData(DataFlavor.javaFileListFlavor);
                    if (files != null && files.size() > 0) {
                        String path = files.get(0).getAbsolutePath();
                        textField.setText(path);
                        if (textField == txtInput
                                && (path.toLowerCase().endsWith(".mp3") || path.toLowerCase().endsWith(".wav"))) {
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

        // Reset border on drag exit is tricky with TransferHandler alone, adding
        // listener
        textField.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                textField.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER_COLOR),
                        new EmptyBorder(5, 8, 5, 8)));
            }
        });
    }

    // ============================================================
    // DIALOGUE OPTIONS AVANCÉES
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
        JTextField txtG = styledField(AutoStepper.genre);

        String[] labels = { "Titre Translit", "Sous-titre Translit", "Artiste Translit", "Genre" };
        JTextField[] fields = { txtTT, txtST, txtAT, txtG };
        for (int i = 0; i < labels.length; i++) {
            g.gridx = 0;
            g.gridy = i;
            g.weightx = 0;
            panel.add(styledLabel(labels[i]), g);
            g.gridx = 1;
            g.weightx = 1.0;
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
        g.gridx = 0;
        g.gridy = labels.length;
        g.gridwidth = 2;
        panel.add(btnRow, g);

        dialog.setContentPane(panel);
        dialog.setVisible(true);
    }

    // ============================================================
    // REDIRECTION DES LOGS
    // ============================================================

    private void redirectSystemStreams() {
        OutputStream out = new OutputStream() {
            public void write(int b) {
                updateLog(String.valueOf((char) b));
            }

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

    private void resetAll() {
        if (JOptionPane.showConfirmDialog(this, "Voulez-vous vraiment tout réinitialiser ?", "Confirmation",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }
        txtInput.setText(".");
        txtOutput.setText(".");
        txtCustomImage.setText("");
        txtCustomBackground.setText("");
        chkHardMode.setSelected(false);
        chkSmartMines.setSelected(true);
        chkDetectSilence.setSelected(true);
        chkVariableBPM.setSelected(true);
        spinDuration.setValue(0);
        logArea.setText("");
        progressBar.setValue(0);
        if (songTableModel != null) {
            songTableModel.setEntries(new java.util.ArrayList<>());
        }
        updateDropZonePreview("", lblBannerPreview, "Glissez une image ici");
        updateDropZonePreview("", lblBgPreview, "Glissez une image ou vidéo ici");
        validateInputs();
        setStep(0);
        System.out.println("Application réinitialisée.");
    }

    private void showHelpDialog() {
        JDialog dialog = new JDialog(this, "Aide & Notice d'utilisation", true);
        dialog.setSize(600, 650);
        dialog.setLocationRelativeTo(this);

        JEditorPane htmlPane = new JEditorPane("text/html", "");
        htmlPane.setEditable(false);
        htmlPane.setBackground(BG_CARD);
        htmlPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        htmlPane.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        String htmlContent = "<html><body style='color:#E6E6F5; font-family:Segoe UI; padding:15px; margin:0;'>"
                + "<h2 style='color:#6366F1; border-bottom: 1px solid #373748; padding-bottom: 5px;'>Bienvenue dans AutoStepper v1.8</h2>"
                + "<p>AutoStepper est un outil qui génère automatiquement des niveaux (steps) StepMania à partir de n'importe quel fichier audio.</p>"
                + "<h3 style='color:#10B981;'>1. Fonctionnement de base</h3>"
                + "<ul>"
                + "<li><b>Fichier ou Dossier</b> : Glissez un MP3/WAV ou tout un dossier dans la zone <i>Musique / Dossier</i>.</li>"
                + "<li><b>Dossier de Sortie</b> : Choisissez où le pack StepMania sera sauvegardé (par défaut, dans le dossier actuel).</li>"
                + "<li><b>Bouton Démarrer</b> : Lance la création. Un dossier contenant la musique, le fichier .sm généré, la bannière et l'arrière-plan sera créé.</li>"
                + "</ul>"
                + "<h3 style='color:#10B981;'>2. L'Éditeur & Prévisualisation</h3>"
                + "<ul>"
                + "<li>Cliquez sur une musique dans la <b>Liste des musiques</b>.</li>"
                + "<li>Le lecteur audio s'active. Vous pouvez appuyer sur <b>Play</b> pour écouter et visualiser les flèches défiler en rythme !</li>"
                + "<li>Glissez vos propres images (Bannière ou Arrière-plan) dans les encarts à droite pour personnaliser spécifiquement la chanson sélectionnée.</li>"
                + "</ul>"
                + "<h3 style='color:#10B981;'>3. Options Algorithmiques</h3>"
                + "<ul>"
                + "<li><b>Mode Difficile</b> : Augmente drastiquement la densité des flèches générées.</li>"
                + "<li><b>Mines Intelligentes</b> : Ajoute des mines (M) lors des moments très intenses de la musique.</li>"
                + "<li><b>Couper les Silences</b> : L'IA détecte si la chanson a un long silence au début ou à la fin, et l'ignore automatiquement pour éviter de générer des pas dans le vide.</li>"
                + "<li><b>BPM Variable</b> : Découpe la musique en segments. Si le tempo change (ex: transition ou accélération), le fichier .sm s'adaptera au lieu de garder un BPM fixe.</li>"
                + "</ul>"
                + "<hr style='border:0; border-bottom:1px solid #373748; margin-top:20px;'/>"
                + "<p style='font-size:11px; color:#9494A8; text-align:center;'>Développé par Maysson.D</p>"
                + "</body></html>";

        htmlPane.setText(htmlContent);
        htmlPane.setCaretPosition(0);

        JScrollPane scrollPane = new JScrollPane(htmlPane);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        JButton btnClose = new JButton("Fermer");
        btnClose.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnClose.setForeground(Color.WHITE);
        btnClose.setBackground(ACCENT);
        btnClose.setFocusPainted(false);
        btnClose.setBorder(new EmptyBorder(8, 20, 8, 20));
        btnClose.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnClose.addActionListener(e -> dialog.dispose());

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setBackground(BG_DARK);
        bottomPanel.add(btnClose);

        dialog.setLayout(new BorderLayout());
        dialog.add(scrollPane, BorderLayout.CENTER);
        dialog.add(bottomPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    // ============================================================
    // PROCESSUS PRINCIPAL
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
                AutoStepper.SMART_MINES = chkSmartMines.isSelected();
                AutoStepper.DETECT_SILENCE = chkDetectSilence.isSelected();
                AutoStepper.VARIABLE_BPM = chkVariableBPM.isSelected();

                float duration = ((Integer) spinDuration.getValue()).floatValue();
                String outputPathRaw = txtOutput.getText();
                if (!outputPathRaw.endsWith("/") && !outputPathRaw.endsWith("\\")) {
                    outputPathRaw += "/";
                }
                final String finalOutputPath = outputPathRaw;

                if (songTableModel.getEntries().isEmpty()) {
                    System.out.println("Erreur : Aucune musique détectée dans la liste.");
                    return;
                }

                int total = songTableModel.getEntries().size();
                java.util.concurrent.atomic.AtomicInteger progress = new java.util.concurrent.atomic.AtomicInteger(0);
                int threads = Math.max(1, Runtime.getRuntime().availableProcessors() - 1);
                java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors
                        .newFixedThreadPool(threads);

                for (SongEntry entry : songTableModel.getEntries()) {
                    executor.submit(() -> {
                        try {
                            File f = entry.file;
                            String cImg = entry.customBanner.trim().isEmpty() ? null : entry.customBanner;
                            String cBg = entry.customBG.trim().isEmpty() ? null : entry.customBG;

                            System.out.println("\n--- Analyse de : " + f.getName() + " ---");
                            if (cImg != null)
                                System.out.println(" > Bannière forcée : " + new File(cImg).getName());
                            if (cBg != null)
                                System.out.println(" > Fond forcé : " + new File(cBg).getName());

                            AutoStepper asInstance = new AutoStepper();
                            AutoStepper.Metadata md = AutoStepper.loadMetadata(f.getAbsolutePath());
                            asInstance.analyzeUsingAudioRecordingStream(f, duration, finalOutputPath, md, cImg, cBg);
                        } catch (Exception ex) {
                            System.out.println("\n[ERREUR] " + ex.getMessage());
                            ex.printStackTrace();
                        } finally {
                            int current = progress.incrementAndGet();
                            SwingUtilities.invokeLater(() -> {
                                if (progressBar.isIndeterminate())
                                    progressBar.setIndeterminate(false);
                                progressBar.setValue((int) ((current / (float) total) * 100));
                            });
                        }
                    });
                }

                executor.shutdown();
                try {
                    executor.awaitTermination(Long.MAX_VALUE, java.util.concurrent.TimeUnit.NANOSECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
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
    // LANCEMENT
    // ============================================================

    public static void launch() {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
        }

        SwingUtilities.invokeLater(() -> {
            AutoStepperGUI gui = new AutoStepperGUI();
            gui.setVisible(true);
        });
    }
}
