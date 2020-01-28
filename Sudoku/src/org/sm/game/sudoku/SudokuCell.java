package org.sm.game.sudoku;

import java.util.ArrayList;
import java.awt.*;

public class SudokuCell
{
    private static final int MAX_MARK = 3;

    private static int cellDim = 20;
    private static Font markFont = null;
    private static Color fgProblemColor = null;
    private static Color fgGuessColor = null;
    private static boolean showAnswer = false;
    private static Color fgAnswerColor = Color.red;
    private static boolean showHint = false;

    private static Font menuFont = null;
    private static Color fgMenuColor = new Color(162, 162, 162);
    private static Color fgActiveMenuColor = Color.green;
    private static int maxMenu = 9;
    private static int menuPerRow = (int)Math.sqrt(maxMenu);

    private byte value;     // negative if it is given, positive if it is the answer
    private byte guess;
    private ArrayList<Byte> marks;
    private Color bgColor;
    private boolean selected;
    private boolean mouseOver;
    private int ptX;
    private int ptY;
    private int activeMenu;
    private Color markColor = null;

    public SudokuCell()
    {
        value = 0;
        guess = 0;
        marks = null;
        bgColor = null;
        selected = false;
        mouseOver = false;
        activeMenu = -1;
    }

    public void initValue(int value)
    {
        this.value = (byte)value;
        guess = 0;
        marks = null;
    }

    public boolean setGuess(byte guess)
    {
        boolean firstGuess;

        if (value > 0)
        {
            firstGuess = this.guess == 0;
            this.guess = guess;
            marks = null;
        }
        else
            firstGuess = false;

        return firstGuess;
    }

    public byte getGuess()
    {
        return guess;
    }

    public boolean isPreset()
    {
        return value < 0;
    }
    
    public boolean isCorrect()
    {
        return value < 0 || guess == value;
    }

    public boolean isGood()
    {
        return value < 0 || guess == 0 || guess == value;
    }

    public boolean setMark(byte mark)
    {
        if (value < 0)
            return false;

        if (marks == null)
            marks = new ArrayList<Byte>();

        boolean suceeded = true;

        if (marks.contains(mark))   // unmark (toggle)
            marks.remove(Byte.valueOf(mark));
        else if (marks.size() < MAX_MARK)
        {
            boolean done = false;
            for (int i = 0; i < marks.size() && !done; i++)
            {
                if (marks.get(i) > mark)
                {
                    marks.add(i, mark);
                    done = true;
                }
            }

            if (!done)
                marks.add(mark);
        }
        else
            suceeded = false;

        return suceeded;
    }

    public Byte[] getMarks()
    {
        return marks == null ? null : marks.toArray(new Byte[marks.size()]); 
    }
    
    public void paintComponent(Graphics g)
    {
        Color oldColor = g.getColor();

        g.setColor(Color.white);
        g.draw3DRect(ptX, ptY, cellDim-1, cellDim-1, !selected);

        if (bgColor != null)
        {
            g.setColor(bgColor);
            g.fillRect(ptX + 2, ptY + 2, cellDim-4, cellDim-4);
        }

        if (value < 0 || guess != 0)
        {
            if (value < 0)
                g.setColor(fgProblemColor);
            else if (showHint && guess != value)
                g.setColor(fgAnswerColor);
            else
                g.setColor(fgGuessColor);

            drawValue(g, value < 0 ? -value : guess);
        }

        if (showAnswer && value > 0 && guess != value)
        {
            g.setColor(fgAnswerColor);
            drawValue(g, value);
        }

        if (marks != null && marks.size() > 0)
        {
            g.setColor(markColor);
            Font oldFont = g.getFont();
            g.setFont(markFont);

            FontMetrics fm = g.getFontMetrics();
            int yoff = fm.getAscent() + 3;
            char[] buf = new char[1];
            int xoff = 5;
            for (byte m: marks)
            {
                buf[0] = (char)('0' + m);
                g.drawChars(buf, 0, 1, ptX + xoff, ptY + yoff);
                xoff += 5 + fm.charWidth(buf[0]);
            }

            g.setFont(oldFont);
        }

        if (mouseOver && !showAnswer)
        {
            Font oldFont = g.getFont();
            g.setFont(menuFont);

            FontMetrics fm = g.getFontMetrics();
            char[] buf = new char[1];
            int groupDim = cellDim / menuPerRow;
            int yoff = fm.getAscent() + (groupDim - fm.getHeight()) / 2;

            for (int i = 0; i < maxMenu; i++)
            {
                if (i == activeMenu)
                    g.setColor(fgActiveMenuColor);
                else
                    g.setColor(fgMenuColor);

                buf[0] = (char)('1' + i);
                int xoff = (groupDim - fm.charWidth(buf[0])) / 2 + (i % menuPerRow) * groupDim;
                g.drawChars(buf, 0, 1, ptX + xoff, ptY + yoff);

                if ((i+1) % menuPerRow == 0)
                    yoff += groupDim;
            }

            g.setFont(oldFont);
        }

        g.setColor(oldColor);
    }
    
    private void drawValue(Graphics g, int value)
    {
        char[] buf = new char[1];
        buf[0] = (char)('0' + value);

        FontMetrics fm = g.getFontMetrics();
        int yoff = fm.getAscent() + (cellDim - fm.getHeight()) / 2;
        int xoff = (cellDim - fm.charWidth(buf[0])) / 2;
        g.drawChars(buf, 0, 1, ptX + xoff, ptY + yoff);
    }

    public boolean mouseMoved(Point point)
    {
        boolean needRepaint = false;

        if (value > 0)
        {
            int newActive = getMenuIndexForPoint(point);

            needRepaint = activeMenu != newActive;
            activeMenu = newActive;
            mouseOver = true;
        }

        return needRepaint;
    }

    public int getMenuIndexForPoint(Point point)
    {
        Point tPoint = new Point(point);
        int groupDim = cellDim / menuPerRow;

        tPoint.translate(-ptX, -ptY);

        return ((int)tPoint.getY() / groupDim) * menuPerRow + (int)tPoint.getX() / groupDim;
    }

    public void setBackgroundColor(Color bgColor)
    {
        this.bgColor = bgColor;
    }

    public void setMarkColor(Color markColor)
    {
        this.markColor = markColor;
    }

    public void setSelected(boolean selected)
    {
        this.selected = selected;
    }

    public void mouseExited()
    {
        mouseOver = false;
    }

    public void setTopLeft(int ptX, int ptY)
    {
        this.ptX = ptX;
        this.ptY = ptY;
    }

    public Rectangle getCellRect()
    {
        return new Rectangle(ptX, ptY, cellDim, cellDim);
    }

    public static void setCellDim(int cellDim)
    {
        SudokuCell.cellDim = cellDim;
    }

    public static void setProblemColor(Color problemColor)
    {
        fgProblemColor = problemColor;
    }

    public static void setGuessColor(Color guessColor)
    {
        fgGuessColor = guessColor;
    }

    public static void setShowHint(boolean showHint)
    {
        SudokuCell.showHint = showHint;
    }
    
    public static boolean getShowHint()
    {
        return showHint;
    }

    public static void setMarkFont(Font markFont)
    {
        SudokuCell.markFont = markFont;
    }

    public static void setMenuFont(Font menuFont)
    {
        SudokuCell.menuFont = menuFont;
    }

    public static void setShowAnswer(boolean showAnswer)
    {
        SudokuCell.showAnswer = showAnswer;
    }
}
