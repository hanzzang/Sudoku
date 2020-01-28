package org.sm.game.sudoku;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import java.awt.event.*;
import java.awt.*;

public class SudokuMainDlg extends JDialog implements ISudokuPanelEventListener
{
    private static final long serialVersionUID = -7025807362019448967L;

    private static final int DEF_WIDTH = 9;
    
    private JButton startButton;
    private JComboBox levelCombo;
    private SudokuPanel sudokuPanel;
    private JLabel timerLabel;
    private JLabel remainLabel;
    private Timer elapsedTimer;
    private boolean usedHint;
    private int initialCount;

    JMenuItem menuStart;

    private static final String GAME_START = "Start Game";
    private static final String GAME_STOP = "Stop Game";
    private static final String GAME_HINT = "Enable Hint";
    private static final String GAME_EXIT = "Exit Game";

    private static final String EDIT_UNDO = "Undo";
    private static final String EDIT_UNDO_TO_LAST_GOOD = "Undo to Last Good";
    private static final String EDIT_REDO = "Redo";

    private static class Timer extends Thread
    {
        JLabel timerLabel;
        boolean stopIt;
        long startMSec;

        private Timer(JLabel timerLabel)
        {
            this.timerLabel = timerLabel;
            stopIt = false;
        }

        public void run()
        {
            startMSec = System.currentTimeMillis();

            while (!stopIt)
            {
                try
                {
                    Thread.sleep(500);

                    int elapsed = (int)((System.currentTimeMillis() - startMSec + 50)/1000);
                    timerLabel.setText(String.format("%02d:%02d", elapsed/60, elapsed%60));
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
        }

        private long stopIt()
        {
            stopIt = true;
            return System.currentTimeMillis() - startMSec;
        }
    }

    public SudokuMainDlg()
        throws Exception
    {
        super((Frame)null, "HN Sudoku", true);

        initContentPane();

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        getRootPane().setDefaultButton(startButton);

        initMenu();

        elapsedTimer = null;
    }

    private void onCancel()
    {
        dispose();
    }

    private void initContentPane()
        throws Exception
    {
        Container contentPane = getContentPane();
        BoxLayout boxLayout = new BoxLayout(contentPane, BoxLayout.Y_AXIS);

        contentPane.setLayout(boxLayout);
        if (contentPane instanceof JPanel)
        {
            JPanel cp = (JPanel)contentPane;
            cp.setBorder(new CompoundBorder(cp.getBorder(), new EmptyBorder(10,10,10,10)));   // margins
        }
        setContentPane(contentPane);

        Box header = new Box(BoxLayout.X_AXIS);
        header.setFont(contentPane.getFont().deriveFont(11.0F));

        startButton = new JButton("Start");
        startButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                startGame();
            }
        });
        header.add(startButton);
        header.add(Box.createHorizontalGlue());

        header.add(new JLabel("Level:"));

        levelCombo = new JComboBox(SudokuLevel.getLevelStrings());
        levelCombo.setSelectedItem(SudokuConfig.getInstance().getLevel().getName());
        header.add(levelCombo);
        header.add(Box.createHorizontalGlue());

        header.add(new JLabel("Elapsed:"));
        timerLabel = new JLabel("00:00");
        timerLabel.setFont(new Font("OCR A Extended", 0, 14));
        timerLabel.setForeground(Color.blue);
        header.add(timerLabel);
        header.add(Box.createHorizontalGlue());

        header.add(new JLabel("Filled:"));
        remainLabel = new JLabel("00/81");
        remainLabel.setForeground(Color.blue);
        header.add(remainLabel);

        contentPane.add(header);
        contentPane.add(Box.createVerticalStrut(7));

        SudokuPanel sp = getSudokuPanel();
        contentPane.add(sp);

        addWindowFocusListener(new WindowAdapter()
        {
            public void windowGainedFocus(WindowEvent e)
            {
                sudokuPanel.requestFocusInWindow();
            }
        });
    }

    private SudokuPanel getSudokuPanel()
    {
        sudokuPanel = new SudokuPanel(DEF_WIDTH);
        sudokuPanel.setFocusable(true);
        sudokuPanel.setFont(new Font("Century Gothic", Font.BOLD, 16));
        sudokuPanel.setMarkFont(new Font("Arial Black", Font.PLAIN, 9));
        sudokuPanel.setMenuFont(new Font("Arial Black", Font.PLAIN, 10));
        sudokuPanel.addEventListener(this);

        return sudokuPanel;
    }

    protected void initMenu()
    {
        JMenuBar menu = new JMenuBar();
        JMenu menuGame = new JMenu("Game");

        menuGame.setMnemonic('G');
        menu.add(menuGame);

        menuStart = new JMenuItem(GAME_START);
        menuStart.setMnemonic('S');
        menuStart.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
        menuStart.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                if (GAME_START.equals(menuStart.getText()))
                    startGame();
                else
                    stopGame();
            }
        });
        menuGame.add(menuStart);

        JCheckBoxMenuItem menuShowHint = new JCheckBoxMenuItem(GAME_HINT);
        menuShowHint.setMnemonic('H');
        menuShowHint.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H, InputEvent.CTRL_DOWN_MASK));
        menuShowHint.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                JCheckBoxMenuItem showHint = (JCheckBoxMenuItem)e.getSource();
                setShowHint(showHint.getState());
                usedHint = usedHint || SudokuCell.getShowHint();
            }
        });
        menuGame.add(menuShowHint);
        setShowHint(menuShowHint.getState());

        menuGame.addSeparator();

        JMenuItem menuExit = new JMenuItem(GAME_EXIT);
        menuExit.setMnemonic('X');
        menuExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_DOWN_MASK));
        menuExit.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                onCancel();
            }
        });
        menuGame.add(menuExit);

        JMenu menuEdit = new JMenu("Edit");

        menuEdit.setMnemonic('E');
        menu.add(menuEdit);

        JMenuItem menuUndo = new JMenuItem(EDIT_UNDO);
        menuUndo.setMnemonic('U');
        menuUndo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK));
        menuUndo.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                onUndo();
            }
        });
        menuEdit.add(menuUndo);

        JMenuItem menuUndoToLastGood = new JMenuItem(EDIT_UNDO_TO_LAST_GOOD);
        menuUndoToLastGood.setMnemonic('L');
        menuUndoToLastGood.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                onUndoToLastGood();
                usedHint = true;
            }
        });
        menuEdit.add(menuUndoToLastGood);

        JMenuItem menuRedo = new JMenuItem(EDIT_REDO);
        menuRedo.setMnemonic('R');
        menuRedo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK));
        menuRedo.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                onRedo();
            }
        });
        menuEdit.add(menuRedo);

        setJMenuBar(menu);
    }

    public void onFilledChanged(int filled)
    {
        setFilledCount(filled);
    }

    private void startGame()
    {
        if (elapsedTimer == null)
        {
            Sudoku sudoku;
            try
            {
                String strLevel = (String)levelCombo.getSelectedItem();
                
                setCursor(new Cursor(Cursor.WAIT_CURSOR));
                sudoku = new Sudoku(DEF_WIDTH, strLevel);
                sudokuPanel.setProblem(sudoku.getProblem(), sudoku.getAnswer(), sudoku.getActualInitial());

                elapsedTimer = new Timer(timerLabel);
                elapsedTimer.start();
                startButton.setText("Stop");
                menuStart.setText(GAME_STOP);
                levelCombo.setEnabled(false);
                sudokuPanel.requestFocusInWindow();
                
                usedHint = SudokuCell.getShowHint();
                SudokuConfig.getInstance().setLevel(strLevel);
                initialCount = sudoku.getActualInitial();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            setCursor(null);
        }
        else
            stopGame();
    }

    private void stopGame()
    {
        long elapsedMSec = elapsedTimer.stopIt();
        elapsedTimer = null;

        startButton.setText("Start");
        menuStart.setText(GAME_START);
        levelCombo.setEnabled(true);

        sudokuPanel.showAnswer();
        
        if (usedHint || sudokuPanel.isSolved() == false)
            elapsedMSec = -1;
        
        SudokuRankDlg dlgRank = new SudokuRankDlg(this, elapsedMSec, initialCount);
        
        dlgRank.pack();
        moveToCenter(dlgRank);
        dlgRank.setVisible(true);
        
        try
        {
            SudokuConfig.getInstance().saveSudokuConfig();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void setFilledCount(int filled)
    {
        int total = DEF_WIDTH * DEF_WIDTH;

        remainLabel.setText(String.format("%d/%d", filled, total));

        if (total == filled && sudokuPanel.isSolved())
            stopGame();
    }

    private void setShowHint(boolean showHint)
    {
        sudokuPanel.setShowHint(showHint);
    }

    private void onUndo()
    {
        sudokuPanel.undo();
    }

    private void onUndoToLastGood()
    {
        sudokuPanel.undoToLastGood();
    }

    private void onRedo()
    {
        sudokuPanel.redo();
    }

    public static void moveToCenter(Component component)
    {
        Dimension dParent;
        Dimension dComponent = component.getSize();
        Component parent = component.getParent();
        Point offset;

        if (component instanceof Dialog || parent == null)
        {
            dParent = Toolkit.getDefaultToolkit().getScreenSize();
            offset = new Point(0, 0);
        }
        else
        {
            dParent = parent.getSize();
            offset = parent.getLocation();
        }
        
        component.setLocation(offset.x + (int)(dParent.getWidth()-dComponent.getWidth())/2,
                              offset.y + (int)(dParent.getHeight()-dComponent.getHeight())/2);
    }
    
    public static void main(String[] args)
            throws Exception
    {
        UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        
        SudokuMainDlg dlg = new SudokuMainDlg();
        
        dlg.pack();
        moveToCenter(dlg);
        dlg.setVisible(true);
        System.exit(0);
    }
}