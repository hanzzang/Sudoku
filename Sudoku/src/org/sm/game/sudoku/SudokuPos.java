package org.sm.game.sudoku;

public class SudokuPos
{
    private int y;
    private int x;

    public SudokuPos()
    {
        setPos(0, 0);
    }

    public SudokuPos(int y, int x)
    {
        setPos(y, x);
    }

    public void setPos(int y, int x)
    {
        this.y = y;
        this.x = x;
    }

    public int getY()
    {
        return y;
    }

    public int getX()
    {
        return x;
    }

    public boolean equals(int y, int x)
    {
        return this.y == y && this.x == x;
    }

    public boolean equals(Object obj)
    {
        boolean equal;

        if (obj instanceof SudokuPos)
        {
            SudokuPos pos = (SudokuPos)obj;
            equal = equals(pos.getY(), pos.getX());
        }
        else
            equal = super.equals(obj);

        return equal;
    }

    public boolean isValid()
    {
        return y >= 0 && x >= 0;
    }
}
