package org.sm.game.sudoku;

import java.util.Date;

public class SudokuRank
{
    private String name;
    private long time;
    private SudokuLevel level;
    private int initialCount;
    private Date date;

    public SudokuRank(String name, long time, SudokuLevel level,
                      int initialCount, Date date)
    {
        this.name = name;
        this.time = time;
        this.level = level;
        this.initialCount = initialCount;
        this.date = date == null ? new Date() : date;
    }

    public void setName(String name)
    {
        this.name = name;
    }
    
    public String getName()
    {
        return name;
    }
    
    public long getTime()
    {
        return time;
    }
    
    public String getTimeStr()
    {
        int msec = (int)(time % 1000);
        int sec = (int)(time / 1000);
        int min = sec / 60;
        
        sec = sec % 60;
        
        return String.format("%02d:%02d.%03d", min, sec, msec);
    }
    
    public SudokuLevel getLevel()
    {
        return level;
    }
    
    public int getInitialCount()
    {
        return initialCount;
    }
    
    public Date getDate()
    {
        return date;
    }
}
