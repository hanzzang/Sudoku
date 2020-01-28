package org.sm.game.sudoku;

import java.io.File;
import java.util.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.*;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class SudokuConfig
{
    private static final String PROP_SETTING = "sudoku.settings";
    private static final String DEFAULT_SETTING = "gsSudoku.xml";
    private static final int MAX_RANK = 20;

    private static final String ROOT_NODE = "GSSudokuSettings";
    private static final String LEVEL_ATTR = "level";
    private static final String RANKS_NODE = "sudoku.ranks";
    private static final String RANK_NODE = "sudoku.rank";
    private static final String NAME_ATTR = "name";
    private static final String INITIAL_COUNT_ATTR = "initial.count";
    private static final String TIME_ATTR = "time";
    private static final String DATE_ATTR = "date";
    
    private static SudokuConfig instance = null;
    
    private String configPath;
    private SudokuLevel level;
    private Map<SudokuLevel,List<SudokuRank>> rankMap;
    private XPath xpath;
    private boolean bDirty;
    
    public static SudokuConfig getInstance()
        throws Exception
    {
        if (instance == null)
            instance = new SudokuConfig();
        
        return instance;
    }

    private SudokuConfig()
        throws Exception
    {
        File file;
        
        configPath = System.getProperty(PROP_SETTING);
        if (configPath == null || configPath.length() == 0)
            configPath = DEFAULT_SETTING;
        
        file = new File(configPath);
        
        rankMap = new HashMap<SudokuLevel,List<SudokuRank>>();
        bDirty = false;
        
        if (file.canRead())
            readConfig(file);
        else
            level = SudokuLevel.EASY_LEVEL;
    }

    public SudokuLevel getLevel()
    {
        return level;
    }
    
    public void setLevel(String strLevel)
    {
        SudokuLevel level = SudokuLevel.getLevel(strLevel);
        
        if (this.level != level)
        {
            this.level = level;
            bDirty = true;
        }
    }
    
    public int addRank(SudokuRank rank)
    {
        int i;
        List<SudokuRank> ranks = rankMap.get(rank.getLevel());
        
        if (ranks == null)
        {
            ranks = new ArrayList<SudokuRank>();
            rankMap.put(rank.getLevel(), ranks);
        }
        
        for (i = 0; i < ranks.size(); i++)
        {
            if (ranks.get(i).getTime() > rank.getTime())
                break;
        }
        
        if (i < MAX_RANK)
        {
            ranks.add(i, rank);
            
            while (ranks.size() > MAX_RANK)
                ranks.remove(MAX_RANK);
            
            bDirty = true;
        }
        else
            i = -1;
        
        return i;
    }
    
    public SudokuRank[] getRanks(SudokuLevel level)
    {
        List<SudokuRank> ranks = rankMap.get(level);
        
        return ranks == null ? new SudokuRank[0] : ranks.toArray(new SudokuRank[ranks.size()]);
    }
    
    public void saveSudokuConfig()
    {
        try
        {
            if (bDirty)
            {
                saveConfig();
                bDirty = false;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    private void readConfig(File file)
        throws Exception
    {
        try
        {
            DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = db.parse(file);
            
            xpath = XPathFactory.newInstance().newXPath();
            initConfig(doc);
        }
        finally
        {
            xpath = null;
        }
    }

    private void initConfig(Document doc)
        throws XPathExpressionException
    {
        Element settings = getElement('/' + ROOT_NODE, doc);
    
        level = SudokuLevel.getLevel(settings.getAttribute(LEVEL_ATTR));
        
        NodeList ranksNodes = getNodeList(RANKS_NODE, settings);
        for (int r = 0; r < ranksNodes.getLength(); r++)
        {
            Element ranks = (Element)ranksNodes.item(r);
            SudokuLevel level = SudokuLevel.getLevel(ranks.getAttribute(LEVEL_ATTR));
            NodeList rankNodes = getNodeList(RANK_NODE, ranks);
            for (int i = 0; i < rankNodes.getLength(); i++)
                addRank((Element)rankNodes.item(i), level);
        }
    }
    
    private void addRank(Element rank, SudokuLevel level)
    {
        String strCount = rank.getAttribute(INITIAL_COUNT_ATTR);
        int iCount = strCount == null || strCount.length() == 0 ? -1 : Integer.parseInt(strCount);
        
        addRank(new SudokuRank(rank.getAttribute(NAME_ATTR),
                               Long.valueOf(rank.getAttribute(TIME_ATTR)),
                               level,
                               iCount,
                               new Date(Long.parseLong(rank.getAttribute(DATE_ATTR)))));
    }

    private Element getElement(String expression, Object item)
        throws XPathExpressionException
    {
        return (Element)xpath.evaluate(expression, item, XPathConstants.NODE);
    }
    
    private NodeList getNodeList(String expression, Object item)
        throws XPathExpressionException
    {
        return (NodeList)xpath.evaluate(expression, item, XPathConstants.NODESET);
    }
    
    private void saveConfig()
        throws Exception
    {
        DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = db.newDocument();
        Element rootNode = doc.createElement(ROOT_NODE);
        
        rootNode.setAttribute(LEVEL_ATTR, level.getName());
        doc.appendChild(rootNode);
    
        for (Map.Entry<SudokuLevel,List<SudokuRank>> entry : rankMap.entrySet()) 
        {
            Element ranksNode = doc.createElement(RANKS_NODE);
            List<SudokuRank> ranks = entry.getValue();

            ranksNode.setAttribute(LEVEL_ATTR, entry.getKey().getName());
            
            if (ranks != null)
            {
                for (SudokuRank rank: ranks)
                {
                    Element rankNode = doc.createElement(RANK_NODE);
                    rankNode.setAttribute(NAME_ATTR, rank.getName());
                    rankNode.setAttribute(TIME_ATTR, Long.toString(rank.getTime()));
                    rankNode.setAttribute(INITIAL_COUNT_ATTR, Integer.toString(rank.getInitialCount()));
                    rankNode.setAttribute(DATE_ATTR, Long.toString(rank.getDate().getTime()));
                    
                    ranksNode.appendChild(rankNode);
                }
            }
            
            rootNode.appendChild(ranksNode);
        }
        
        saveDocument(doc, configPath);
    }
    
    private void saveDocument(Document doc, String path)
    {
        try
        {
            // save the XML configuration to the file
            Transformer trans = TransformerFactory.newInstance().newTransformer();
            trans.setOutputProperty(OutputKeys.METHOD, "xml");
            trans.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            trans.setOutputProperty(OutputKeys.INDENT, "yes");
            trans.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
    
            trans.transform(new DOMSource(doc), new StreamResult(path));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
