package org.sm.game.sudoku;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.event.*;
import java.util.ArrayList;

public class SudokuPanel extends JPanel
{
    private static final long serialVersionUID = -8506759405037607744L;
    
    private int width;
    private int groupWidth;
    private SudokuPos curPos;
    private SudokuPos mousePos;
    private int celldim;

    private SudokuCell[][] cells;
    private int filled;
    private ArrayList<ISudokuPanelEventListener> eventListener;

    private boolean started;
    private ArrayList<SudokuOp> UndoOps;
    private ArrayList<SudokuOp> RedoOps;

    public SudokuPanel(int width)
    {
        addKeyListener(new KeyAdapter()
        {
            public void keyPressed(KeyEvent e)
            {
                onKeyPressed(e);
            }
        });

        addMouseListener(new MouseAdapter()
        {
            public void mousePressed(MouseEvent e)
            {
                onMousePressed(e);
            }

            public void mouseExited(MouseEvent e)
            {
                onMouseExited(e);
            }
        });

        addMouseMotionListener(new MouseMotionAdapter()
        {
            public void mouseMoved(MouseEvent e)
            {
                onMouseMoved(e);
            }
        });

        this.width = width;
        groupWidth = (int)Math.sqrt(width);
        curPos = new SudokuPos(width / 2, width / 2);
        mousePos = new SudokuPos(-1, -1);

        cells = new SudokuCell[width][width];
        Color oddGroupColor = new Color(208,208,208);
        Color evenMarkColor = new Color(192, 192, 192);
        Color oddMarkColor = new Color(176, 176, 176);
        SudokuCell cell;
        for (int y = 0; y < width; y++)
        {
            for (int x = 0; x < width; x++)
            {
                cell = new SudokuCell();
                if (getGroupIndex(y, x) % 2 == 1)
                {
                    cell.setBackgroundColor(oddGroupColor);
                    cell.setMarkColor(oddMarkColor);
                }
                else
                    cell.setMarkColor(evenMarkColor);
                if (curPos.equals(y, x))
                    cell.setSelected(true);
                setCell(y, x, cell);
            }
        }

        SudokuCell.setProblemColor(new Color(0, 0, 128));
        SudokuCell.setGuessColor(new Color(0, 128, 0));

        eventListener = null;
        setFilled(0);

        started = false;
        UndoOps = new ArrayList<SudokuOp>();
        RedoOps = new ArrayList<SudokuOp>();
    }

    public void setProblem(byte[][] problem, byte[][] answer, int initial)
    {
        setFilled(initial);

        for (int y = 0; y < width; y++)
        {
            for (int x = 0; x < width; x++)
                getCell(y, x).initValue(problem[y][x] > 0 ? -problem[y][x] : answer[y][x]);
        }

        SudokuCell.setShowAnswer(false);
        
        repaint();

        started = true;
    }

    public void addEventListener(ISudokuPanelEventListener listener)
    {
        if (eventListener == null)
            eventListener = new ArrayList<ISudokuPanelEventListener>();

        eventListener.add(listener);
    }

    public boolean isSolved()
    {
        boolean solved = true;

        for (int y = 0; y < width && solved; y++)
        {
            for (int x = 0; x < width && solved; x++)
                solved = getCell(y, x).isCorrect();
        }

        return solved;
    }

    public void showAnswer()
    {
        SudokuCell.setShowAnswer(true);
        repaint();

        started = false;
        UndoOps.clear();
        RedoOps.clear();
    }

    public void setFont(Font font)
    {
        super.setFont(font);

        BufferedImage bi = new BufferedImage(1, 1, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics g = bi.createGraphics();
        g.setFont(font);
        FontMetrics fm = g.getFontMetrics();
        int charHeight = fm.getHeight();

        celldim = charHeight * 2 + 1;

        setPreferredSize(new Dimension(celldim*width, celldim*width));

        SudokuCell.setCellDim(celldim);

        for (int y = 0; y < width; y++)
        {
            for (int x = 0; x < width; x++)
                getCell(y, x).setTopLeft(x*celldim, y*celldim);
        }
    }

    public void setMarkFont(Font font)
    {
        SudokuCell.setMarkFont(font);
        repaint();
    }

    public void setMenuFont(Font font)
    {
        SudokuCell.setMenuFont(font);
    }

    public void setShowHint(boolean showHint)
    {
        SudokuCell.setShowHint(showHint);
        repaint();
    }

    public void undo()
    {
        if (UndoOps.size() > 0)
        {
            SudokuOp op = UndoOps.remove(UndoOps.size()-1);
            SudokuOp.OP opType = op.getOp();

            if (SudokuOp.OP.SET == opType)
            {
                setGuess(op.getPos(), op.getOldValue(), false);
                Byte[] marks = op.getOldMarks();
                if (marks != null)
                {
                    for (Byte mark: marks)
                        setMark(op.getPos(), mark, false);
                }
            }
            else if (SudokuOp.OP.DEL == opType)
                setGuess(op.getPos(), op.getOldValue(), false);
            else if (SudokuOp.OP.MARK == opType)
                setMark(op.getPos(), op.getNewValue(), false);

            setCurCell(op.getPos().getY(), op.getPos().getX());

            RedoOps.add(op);
        }
    }

    public void undoToLastGood()
    {
        while (UndoOps.size() > 0)
        {
            SudokuOp op = UndoOps.get(UndoOps.size()-1);

            if (op.getOp() == SudokuOp.OP.SET && op.getGoodSoFar())
                break;

            undo();
        }
    }

    public void redo()
    {
        if (RedoOps.size() > 0)
        {
            SudokuOp op = RedoOps.remove(RedoOps.size()-1);
            SudokuOp.OP opType = op.getOp();

            if (SudokuOp.OP.SET == opType || SudokuOp.OP.DEL == opType)
                setGuess(op.getPos(), op.getNewValue(), false);
            else if (SudokuOp.OP.MARK == opType)
                setMark(op.getPos(), op.getNewValue(), false);

            setCurCell(op.getPos().getY(), op.getPos().getX());
            
            UndoOps.add(op);
        }
    }

    protected void paintComponent(Graphics g)
    {
        super.paintComponent(g);

        Shape clip = g.getClip();

        for (int y = 0; y < width; y++)
        {
            for (int x = 0; x < width; x++)
            {
                SudokuCell cell = getCell(y, x);
                if (clip.intersects(cell.getCellRect()))
                    cell.paintComponent(g);
            }
        }
    }

    protected void onKeyPressed(KeyEvent e)
    {
        switch (e.getKeyCode())
        {
            case KeyEvent.VK_RIGHT:
                if (curPos.getX() < width - 1)
                    setCurCell(curPos.getY(), curPos.getX()+1);
                break;

            case KeyEvent.VK_LEFT:
                if (curPos.getX() > 0)
                    setCurCell(curPos.getY(), curPos.getX()-1);
                break;

            case KeyEvent.VK_UP:
                if (curPos.getY() > 0)
                    setCurCell(curPos.getY()-1, curPos.getX());
                break;

            case KeyEvent.VK_DOWN:
                if (curPos.getY() < width - 1)
                    setCurCell(curPos.getY()+1, curPos.getX());
                break;

            case KeyEvent.VK_BACK_SPACE:
            case KeyEvent.VK_DELETE:
            case KeyEvent.VK_ESCAPE:
                setGuess(curPos, (byte)0);
                break;
        }

        int kc = e.getKeyCode();
        if (started && (KeyEvent.VK_1 <= kc && kc <= KeyEvent.VK_9
                        || KeyEvent.VK_NUMPAD1 <= kc && kc <= KeyEvent.VK_NUMPAD9))
        {
            byte key = (byte)(kc > KeyEvent.VK_9 ? kc - KeyEvent.VK_NUMPAD0 : kc - KeyEvent.VK_0);
            if (e.getModifiers() == 0)
                setGuess(curPos, key);
            else
                setMark(curPos, key);
        }
    }

    protected void onMousePressed(MouseEvent e)
    {
        SudokuPos pos = getCellPosForPoint(e.getPoint());
        setCurCell(pos.getY(), pos.getX());

        //System.out.println(e);
        if (started)
        {
            byte number = (byte)(getCell(pos).getMenuIndexForPoint(e.getPoint()) + 1);
            if (e.getButton() == MouseEvent.BUTTON1)
                setGuess(pos, number);
            else
                setMark(pos, number);
        }
    }

    protected void onMouseMoved(MouseEvent e)
    {
        SudokuPos pos = getCellPosForPoint(e.getPoint());

        if (!mousePos.equals(pos) && mousePos.isValid())
        {
            getCell(mousePos).mouseExited();
            invalidateCell(mousePos);
        }

        mousePos = pos;
        SudokuCell cell = getCell(mousePos);
        if (cell.mouseMoved(e.getPoint()))
            invalidateCell(mousePos);
    }

    protected void onMouseExited(MouseEvent e)
    {
        if (mousePos.isValid())
        {
            getCell(mousePos).mouseExited();
            invalidateCell(mousePos);
            mousePos.setPos(-1, -1);
        }
    }

    private int getGroupIndex(int y, int x)
    {
        return (y / groupWidth) * groupWidth + x / groupWidth;
    }

    private SudokuPos getCellPosForPoint(Point pt)
    {
        return new SudokuPos((int)pt.getY()/celldim, (int)pt.getX()/celldim);
    }

    protected void setCurCell(int y, int x)
    {
        if (curPos.getY() != y || curPos.getX() != x)
        {
            getCell(curPos).setSelected(false);
            invalidateCell(curPos);
            curPos.setPos(y, x);
            getCell(curPos).setSelected(true);
            invalidateCell(curPos);
        }
    }

    protected void invalidateCell(SudokuPos pos)
    {
        invalidateCell(pos.getY(), pos.getX());
    }

    protected void invalidateCell(int y, int x)
    {
        repaint(getCell(y, x).getCellRect());
    }

    protected void setFilled(int filled)
    {
        this.filled = filled;

        if (eventListener != null && eventListener.size() > 0)
        {
            for (ISudokuPanelEventListener listener: eventListener)
                listener.onFilledChanged(filled);
        }
    }

    protected SudokuCell getCell(SudokuPos pos)
    {
        return getCell(pos.getY(), pos.getX());
    }

    protected SudokuCell getCell(int y, int x)
    {
        return cells[y][x];
    }

    protected void setCell(int y, int x, SudokuCell cell)
    {
        cells[y][x] = cell;
    }

    public boolean isGoodSoFar()
    {
        boolean good = true;

        for (int y = 0; y < width && good; y++)
        {
            for (int x = 0; x < width && good; x++)
                good = getCell(y, x).isGood();
        }

        return good;
    }

    protected void setGuess(SudokuPos pos, byte guess)
    {
        setGuess(pos, guess, true);
    }

    protected void setGuess(SudokuPos pos, byte guess, boolean undoable)
    {
        SudokuCell cell = getCell(pos);
        byte oldValue = cell.getGuess();
        Byte[] oldMarks = cell.getMarks();
        boolean firstGuess = cell.setGuess(guess);
        SudokuOp op = null;

        if (oldValue != guess)
        {
            if (guess == 0)
            {
                if (!firstGuess)
                    setFilled(filled - 1);
                if (undoable)
                {
                    op = new SudokuOp(SudokuOp.OP.DEL, pos, oldValue, null,
                                      guess, isGoodSoFar());
                }
            }
            else
            {
                if (firstGuess)
                    setFilled(filled + 1);
    
                if (undoable)
                {
                    op = new SudokuOp(SudokuOp.OP.SET, pos, oldValue, oldMarks,
                                      guess, isGoodSoFar());
                }
            }
    
            if (op != null)
            {
                UndoOps.add(op);
                RedoOps.clear();
            }
    
            invalidateCell(pos);
        }
    }

    protected void setMark(SudokuPos pos, byte mark)
    {
        setMark(pos, mark, true);
    }

    protected void setMark(SudokuPos pos, byte mark, boolean undoable)
    {
        SudokuCell cell = getCell(pos);
        SudokuOp op = null;
        boolean invalid = false;

        if (cell.setMark(mark))
        {
            invalid = true;
            if (undoable)
                op = new SudokuOp(SudokuOp.OP.MARK, pos, (byte)0, null, mark, true);
        }

        if (op != null)
        {
            UndoOps.add(op);
            RedoOps.clear();
        }

        if (invalid)
            invalidateCell(pos);
    }
}
