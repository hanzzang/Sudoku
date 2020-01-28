package org.sm.game.sudoku;

import java.util.ArrayList;

public class SudokuLevel
{
    private final static ArrayList<SudokuLevel> _levels = new ArrayList<SudokuLevel>();

    public static final SudokuLevel EASY_LEVEL = new SudokuLevel("Easy", 38);
    public static final SudokuLevel MEDIUM_LEVEL = new SudokuLevel("Medium", 32);
    public static final SudokuLevel HARD_LEVEL = new SudokuLevel("Hard", 21);
    public static final SudokuLevel EXTREME_LEVEL = new SudokuLevel("Extreme", 17);
    
    private int initialGiven;
    private String name;

    public SudokuLevel(String name, int initialGiven)
    {
        this.name = name;
        this.initialGiven = initialGiven;

        synchronized (_levels)
        {
            if (_levels.contains(this))
                throw new RuntimeException("Sudoku Level (" + name + ") is already defined");
            else
                _levels.add(this);
        }
    }

    public String getName()
    {
        return name;
    }

    public int getInitialGiven()
    {
        return initialGiven;
    }

    public boolean equals(Object obj)
    {
        boolean equal;

        if (obj != null && this.getClass().isAssignableFrom(obj.getClass()))
            equal = name.equalsIgnoreCase(((SudokuLevel)obj).name);
        else
            equal = super.equals(obj);

        return equal;
    }

    public static SudokuLevel getLevel(String name)
    {
        SudokuLevel level = null;

        synchronized (_levels)
        {
            for (int i = 0; i < _levels.size() && level == null; i++)
            {
                if (_levels.get(i).getName().equalsIgnoreCase(name))
                    level = _levels.get(i);
            }
        }

        return level;
    }
    
    public static String[] getLevelStrings()
    {
        ArrayList<String> levels = new ArrayList<String>();
        
        synchronized (_levels)
        {
            for (SudokuLevel level: _levels)
                levels.add(level.getName());
        }
        
        return levels.toArray(new String[levels.size()]);
    }
}
