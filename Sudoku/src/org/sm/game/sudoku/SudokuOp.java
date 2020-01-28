package org.sm.game.sudoku;

public class SudokuOp
{
    public static enum OP { SET, DEL, MARK }

    private OP op;
    private SudokuPos pos;
    private byte oldValue;
    private Byte[] oldMarks;
    private byte newValue;
    private boolean goodSoFar;

    public SudokuOp(OP op, SudokuPos pos, byte oldValue, Byte[] marks,
                    byte newValue, boolean goodSoFar)
    {
        this.op = op;
        this.pos = new SudokuPos(pos.getY(), pos.getX());
        this.oldValue = oldValue;
        this.oldMarks = marks;
        this.newValue = newValue;
        this.goodSoFar = goodSoFar;
    }

    public OP getOp()
    {
        return op;
    }

    public SudokuPos getPos()
    {
        return pos;
    }

    public byte getOldValue()
    {
        return oldValue;
    }

    public Byte[] getOldMarks()
    {
        return oldMarks;
    }
    
    public byte getNewValue()
    {
        return newValue;
    }

    public boolean getGoodSoFar()
    {
        return goodSoFar;
    }
}
