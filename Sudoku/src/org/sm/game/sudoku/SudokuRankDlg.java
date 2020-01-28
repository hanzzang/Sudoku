package org.sm.game.sudoku;

import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import javax.swing.text.JTextComponent;

public class SudokuRankDlg extends JDialog implements ActionListener, KeyListener
{
    private static final long serialVersionUID = -8495583028752907807L;

    private JComboBox levelCombo;
    private SudokuTable rankTable;
    private JButton okButton;
    private int editableRow;
    
    private static class SudokuTable extends JTable
    {
        private static final long serialVersionUID = 407849527398082441L;

        public SudokuTable(TableModel model)
        {
            super(model);
        }
        
        public TableCellRenderer getCellRenderer(int row, int col) 
        {
            TableCellRenderer renderer = super.getCellRenderer(row, col);
            
            if (renderer instanceof JComponent)
            {
                Color bg = isCellEditable(row, 0) ? Color.ORANGE : getBackground();
                ((JComponent)renderer).setBackground(bg);
            }
            
            return renderer;
        }
    }
    
    private static class SudokuRankTableModel extends AbstractTableModel
    {
        private static final long serialVersionUID = -3407670764822583274L;
        
        private static final String[] columns =
        {
                "Name", "Initial Count", "Time", "Date"
        };
        
        private static final SimpleDateFormat tsdf = new SimpleDateFormat("mm:ss.SSS");
        private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        
        private SudokuRank[] ranks;
        private int editableRow;
        
        private SudokuRankTableModel()
        {
            ranks = null;
            editableRow = -1;
        }
        
        public String getColumnName(int col)
        {
            return columns[col];
        }
        
        public int getColumnCount()
        {
            return columns.length;
        }
        
        public Class<?> getColumnClass(int col)
        {
            Object value = getValueAt(0, col);
            
            return value == null ? String.class : value.getClass();
        }
        
        public boolean isCellEditable(int row, int col)
        {
            return col == 0 && editableRow == row;
        }

        public int getRowCount()
        {
            return ranks == null ? 0 : ranks.length;
        }

        public Object getValueAt(int row, int col)
        {
            Object value = null;
            
            if (row < ranks.length)
            {
                SudokuRank rank = ranks[row];
                
                if (col == 0)
                    value = rank.getName();
                else if (col == 1)
                    value = rank.getInitialCount();
                else if (col == 2)
                    value = tsdf.format(new Date(rank.getTime()));
                else if (col == 3)
                    value = sdf.format(rank.getDate());
            }
            
            return value;
        }
        
        public void setValueAt(Object aValue, int row, int col)
        {
            if (col == 0 && isCellEditable(row, col))
            {
                SudokuRank rank = ranks[row];
                rank.setName(aValue == null ? null : aValue.toString());
            }
        }
        
        private void setRanks(SudokuRank[] ranks, int editableRow)
        {
            this.ranks = ranks;
            this.editableRow = editableRow;
            
            fireTableDataChanged();
        }
    }
    
    public SudokuRankDlg(Dialog parent, long elapsedMSec, int initialCount)
    {
        super(parent, "Tetris Rank", true);

        try
        {
            SudokuConfig config = SudokuConfig.getInstance();
            initContentPane(config);
            checkScores(elapsedMSec, initialCount, config);
            if (rankTable.getModel().getValueAt(0, 0) != null)
                initColumnSizes();
            
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            getRootPane().setDefaultButton(okButton);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    private void initContentPane(SudokuConfig config)
    {
        Container contentPane = getContentPane();
        JPanel mainPanel;

        if (contentPane instanceof JPanel)
            mainPanel = (JPanel)contentPane;
        else
        {
            mainPanel = new JPanel();
            setContentPane(mainPanel);
        }
        
        mainPanel.setBorder(new CompoundBorder(mainPanel.getBorder(),
                                               new EmptyBorder(10,10,10,10)));   // margins
        mainPanel.setLayout(new BorderLayout(6, 6));

        Box boxLevel = new Box(BoxLayout.X_AXIS);
        boxLevel.add(Box.createHorizontalGlue());
        boxLevel.add(new JLabel("Level:"));
        levelCombo = new JComboBox(SudokuLevel.getLevelStrings());
        levelCombo.setSelectedItem(config.getLevel().getName());
        levelCombo.addActionListener(this);
        boxLevel.add(levelCombo);
        boxLevel.add(Box.createHorizontalGlue());
        mainPanel.add(boxLevel, BorderLayout.NORTH);
        
        rankTable = new SudokuTable(new SudokuRankTableModel());
        rankTable.setAutoCreateRowSorter(true);
        rankTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        //Create the scroll pane and add the table to it.
        JScrollPane scrollPane = new JScrollPane(rankTable);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        Box boxButtons = new Box(BoxLayout.X_AXIS);
        boxButtons.add(Box.createHorizontalGlue());
        okButton = new JButton("OK");
        okButton.addActionListener(this);
        boxButtons.add(okButton);
        boxButtons.add(Box.createHorizontalGlue());
        mainPanel.add(boxButtons, BorderLayout.SOUTH);
        
        rankTable.addKeyListener(this);
    }

    private void initColumnSizes()
    {
        String[] headers = SudokuRankTableModel.columns;
        TableCellRenderer tcr = rankTable.getTableHeader().getDefaultRenderer();
        TableColumnModel tcm = rankTable.getColumnModel();
        TableColumn column = null;
        Component comp;
        ArrayList<Integer> widths = new ArrayList<Integer>();
        int sum = 0;

        for (int col = 0; col < headers.length; col++)
        {
            column = tcm.getColumn(col);
            comp = tcr.getTableCellRendererComponent(rankTable, headers[col],
                                                     false, false, 0, 0);
            int width = comp.getPreferredSize().width;
            
            TableCellRenderer renderer = rankTable.getCellRenderer(0, col);
            for (int row = 0; row < rankTable.getRowCount(); row++)
            {
                comp = renderer.getTableCellRendererComponent(rankTable, rankTable.getValueAt(row, col),
                                                         false, false, row, col);
                int w = comp.getPreferredSize().width;
                
                if (w > width)
                    width = w;
            }
            
            column.setPreferredWidth(width);
            widths.add(width);
            sum += width;
        }
        
        int preferredWidth = rankTable.getParent().getPreferredSize().width;
        
        for (int col = 0; col < rankTable.getColumnCount(); col++)
            tcm.getColumn(col).setPreferredWidth(preferredWidth*widths.get(col)/sum);
    }

    private void checkScores(long elapsedMSec, int initialCount, SudokuConfig config)
    {
        SudokuRankTableModel tblModel = (SudokuRankTableModel)rankTable.getModel();
        
        if (elapsedMSec > 0)
        {
            SudokuRank rank = new SudokuRank(null, elapsedMSec, config.getLevel(),
                                             initialCount, null);
            
            editableRow = config.addRank(rank);
        }
        else
            editableRow = -1;
        
        tblModel.setRanks(config.getRanks(config.getLevel()), editableRow);
    }
    
    protected void processWindowEvent(WindowEvent e) 
    {
        super.processWindowEvent(e);
        
        if (e.getID() == WindowEvent.WINDOW_OPENED)
        {
            if (editableRow >= 0)
            {
                rankTable.editCellAt(editableRow, 0);
                Component c = rankTable.getEditorComponent();
                rankTable.requestFocusInWindow();
                if (c instanceof JTextComponent)
                    ((JTextComponent)c).getCaret().setVisible(true);
            }
        }
    }
    
    public void actionPerformed(ActionEvent e)
    {
        if (e.getSource() == levelCombo)
        {
            SudokuRankTableModel tblModel = (SudokuRankTableModel)rankTable.getModel();
            SudokuLevel level = SudokuLevel.getLevel((String)levelCombo.getSelectedItem());
            
            try
            {
                SudokuConfig config = SudokuConfig.getInstance();
                
                tblModel.setRanks(config.getRanks(level),
                                  level == config.getLevel() ? editableRow : -1);
            }
            catch (Exception er)
            {
                er.printStackTrace();
            }
        }
        else if (e.getSource() == okButton)
        {
            if (rankTable.isEditing())
                rankTable.getCellEditor().stopCellEditing();
            dispose();
        }
    }

    public void keyTyped(KeyEvent e)
    {
    }

    public void keyPressed(KeyEvent e)
    {
        if (e.getKeyCode() == KeyEvent.VK_ENTER && e.getSource() == rankTable)
        {
            if (rankTable.isEditing() == false)
                dispose();
        }
    }

    public void keyReleased(KeyEvent e)
    {
    }
}
