package edu.georgetown.library.fileAnalyzer.filetest.iiif;

import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import edu.georgetown.library.fileAnalyzer.filetest.iiif.IIIFEnums.IIIFLookup;
import edu.georgetown.library.fileAnalyzer.util.XMLUtil;
import edu.georgetown.library.fileAnalyzer.util.XMLUtil.SimpleNamespaceContext;
import gov.nara.nwts.ftapp.importer.DelimitedFileReader;

public class MetadataInputFileBuilder {

        public static final String NA = "NA.txt";
        public enum InputFileType {
                NA, Property, CSV, DC, METS, EAD, REST;
        }
        
        private ArrayList<FilenameFilter> filters = new ArrayList<>();
        
        public MetadataInputFileBuilder() {
                filters.add(new FilenameFilter(){
                        public boolean accept(File dir, String name) {
                                return name.toLowerCase().equals("mets.xml");
                        }
                });
                filters.add(new FilenameFilter(){
                        public boolean accept(File dir, String name) {
                                return name.toLowerCase().equals("dublin_core.xml");
                        }
                });
        }
        
        public MetadataInputFile identifyFile(File parent, String s) throws InputFileException {
                if (s.isEmpty()) throw new InputFileException("No Metadata Input File Specified");
                return identifyFile(new File(parent, s));
        }

        public MetadataInputFile findMetadataFile(File parent, MetadataInputFile manifestMeta) throws InputFileException {
                MetadataInputFile returnFile = identifyFile(parent, NA);
                for(FilenameFilter ff: filters) {
                        String[] matches = parent.list(ff);
                        if (matches.length > 0) {
                                returnFile = identifyFile(parent, matches[0]);
                                if (returnFile.getInputFileType() == InputFileType.NA) {
                                        return manifestMeta;
                                }
                                return returnFile;
                        }
                }
                return manifestMeta;
        }
        
        public MetadataInputFile identifyFile(File f) throws InputFileException {
                if (f == null) throw new InputFileException("Null Input File"); 
                if (f.getName().toLowerCase().endsWith(".xml")) {
                        return new XMLInputFile(f);
                } else if (f.getName().toLowerCase().endsWith(".prop")) {
                        return new PropertyFile(f);
                } else if (f.getName().toLowerCase().endsWith(".csv")) {
                        
                }
                return new UnidentifiedInputFile();
        }
        
        
                
        abstract class DefaultInput implements MetadataInputFile {
                InputFileType fileType;
                @Override
                public String getValue(IIIFLookup key, String def){
                        return def;
                }
                @Override
                public File getFile() {
                        return null;
                }
                @Override
                public InputFileType getInputFileType() {
                        return fileType;
                }
                @Override
                public void setCurrentKey(String s) {
                        //no action except for CSV
                }
                @Override
                public List<RangePath> getInitRanges(RangePath parent, ManifestProjectTranslate manifestTranslate) {
                        return new ArrayList<RangePath>();
                }
         }

        abstract class DefaultInputFile extends DefaultInput {
                File file;
                @Override
                public File getFile() {
                        return file;
                }
                DefaultInputFile(File file) {
                        this.file = file;
                }
         }
        
        class UnidentifiedInputFile extends DefaultInput {

                @Override
                public InputFileType getInputFileType() {
                        return InputFileType.NA;
                }
       }

        //TODO
        class RESTResponseInputFile  extends DefaultInput {
                @Override
                public InputFileType getInputFileType() {
                        return InputFileType.REST;
                }
        }

        
        class XMLInputFile extends DefaultInputFile {
                Document d;
                XPath xp = XMLUtil.xf.newXPath();
                XMLInputFile(File file) throws InputFileException {
                        super(file);
                        try {
                                d = XMLUtil.dbf_ns.newDocumentBuilder().parse(file);
                                if (d == null) {
                                        throw new InputFileException(String.format("File [%s] cannot be parsed", file.getName()));
                                }
                                SimpleNamespaceContext nsContext = new XMLUtil().new SimpleNamespaceContext();
                                
                                //For mets.xml
                                nsContext.add("dim", "http://www.dspace.org/xmlns/dspace/dim");
                                nsContext.add("mets", "http://www.loc.gov/METS/");
                                nsContext.add("mods", "http://www.loc.gov/mods/v3");
                                
                                //For EAD files
                                nsContext.add("ead", "urn:isbn:1-931666-22-9");
                                nsContext.add("ns2", "http://www.w3.org/1999/xlink");

                                xp.setNamespaceContext(nsContext);
                                
                                String ns = d.getDocumentElement().getNamespaceURI();
                                String tag = d.getDocumentElement().getTagName();
                                if (ns == null) {
                                        ns = "";
                                }
                                if (tag.equals("dublin_core")) {
                                        fileType = InputFileType.DC;
                                } else if (ns.equals("urn:isbn:1-931666-22-9")) {
                                        fileType = InputFileType.EAD;
                                } else if (ns.equals("http://www.loc.gov/METS/") || tag.equals("mets")) {
                                        fileType = InputFileType.METS;
                                } else {                                        
                                        throw new InputFileException(String.format("Cannot identify XML file [%s]", file.getName()));
                                }
                        } catch (SAXException | IOException | ParserConfigurationException e) {
                                throw new InputFileException(e.getMessage());
                        }
                }
                
                @Override
                public String getValue(IIIFLookup key, String def) {
                        String xq = key.getFileTypeKey(fileType);
                        if (xq != null) {
                                return getXPathValue(d, xq, def);
                        }
                        return def;
                }
                public String getXPathValue(Node d, String xq, String def) {
                        return XMLUtil.getXPathValue(xp, d, xq, def);
                }

                @Override
                public List<RangePath> getInitRanges(RangePath parent, ManifestProjectTranslate manifestTranslate) {
                        ArrayList<RangePath> rangePaths = new ArrayList<>();
                        RangePath rp = new RangePath("Subjects", "Subjects");
                        rangePaths.add(rp);
                        rp.setParent(parent);
                        parent.addChildRange(rp);
                        if (fileType == InputFileType.EAD) {
                                try {
                                        NodeList nl = (NodeList)xp.evaluate("//ead:c01", d, XPathConstants.NODESET);
                                        for(int i=0; i<nl.getLength(); i++) {
                                               addRange(manifestTranslate, rangePaths, nl.item(i), rp);
                                        }
                                } catch (XPathExpressionException e) {
                                        e.printStackTrace();
                                }
                        }
                        return rangePaths;
                }
                
                public void addRange(ManifestProjectTranslate manifestTranslate, List<RangePath> rangePaths, Node n, RangePath parent) throws XPathExpressionException {
                        String rName = manifestTranslate.rangeTranslate(getXPathValue(n, "ead:did/ead:unittitle", "n/a"));
                        String rPath = getPath(n);
                        RangePath rp = new RangePath(rPath, rName);
                        rp.setParent(parent);
                        parent.addChildRange(rp);
                        rangePaths.add(rp);
                        manifestTranslate.registerEADRange(xp, n, rp);
                        NodeList nl = (NodeList)xp.evaluate("ead:c02|ead:c03|ead:c04", n, XPathConstants.NODESET);
                        for(int i=0; i<nl.getLength(); i++) {
                                addRange(manifestTranslate, rangePaths, nl.item(i), rp);
                        }
                }
                
                public String getPath(Node n) throws XPathExpressionException {
                        StringBuilder sb = new StringBuilder();
                        NodeList nl = (NodeList)xp.evaluate("ancestor-or-self::ead:c01|ancestor-or-self::ead:c02|ancestor-or-self::ead:c03|ancestor-or-self::ead:c04", n, XPathConstants.NODESET);
                        for(int i=0; i<nl.getLength(); i++) {
                                Node cn = nl.item(i);
                                NodeList cnl = (NodeList)xp.evaluate("preceding-sibling::ead:c01|preceding-sibling::ead:c02|preceding-sibling::ead:c03|preceding-sibling::ead:c04", cn, XPathConstants.NODESET);
                                if (i > 0) {
                                        sb.append("-");
                                }
                                sb.append(String.format("%03d", cnl.getLength()));
                        }
                        return sb.toString();
                }
        }
        
        public class CSVInputFile extends DefaultInputFile {
                HashMap<String,Integer> cols = new HashMap<>(); 
                ArrayList<String> keys = new ArrayList<>();
                HashMap<String,Vector<String>> values = new HashMap<>();
                Vector<String> currentRow = null;
                CSVInputFile(File file) throws InputFileException {
                        super(file);
                        fileType = InputFileType.CSV;
                        try {
                                Vector<Vector<String>> data = DelimitedFileReader.parseFile(file, ",");
                                Vector<String> header = new Vector<>();
                                if (data.size() > 0) {
                                        header = data.get(0);
                                        for(int i=1; i<header.size(); i++) {
                                                cols.put(header.get(i), i);
                                        }
                                }
                                for(int r=1; r<data.size(); r++) {
                                        Vector<String> row = data.get(r);
                                        if (header.size() == row.size()) {
                                                keys.add(row.get(0));
                                                values.put(row.get(0), row);
                                        }
                                        
                                }
                        } catch (IOException e) {
                                throw new InputFileException("CSV Parsing Error "+e.getMessage());
                        }
                }
                
                @Override
                public void setCurrentKey(String key) {
                        currentRow = values.containsKey(key) ? values.get(key) : null;
                }
                
                @Override
                public String getValue(IIIFLookup key, String def) {
                        if (currentRow == null) {
                                return def;
                        }
                        String col = key.getFileTypeKey(fileType);
                        if (col == null) {
                                return def;
                        }
                        if (cols.containsKey(col)) {
                                return currentRow.get(cols.get(col));
                        }
                        return def;
                }

        }

        public class PropertyFile extends DefaultInputFile {
                Properties prop = new Properties();
                PropertyFile(File file) throws InputFileException {
                        super(file);
                        fileType = InputFileType.Property;
                        try {
                                prop.load(new FileReader(file));
                        } catch (Exception e) {
                                throw new InputFileException("Property Parsing Error "+e.getMessage());
                        }
                }
                
                @Override
                public String getValue(IIIFLookup key, String def) {
                        String propkey = key.getFileTypeKey(fileType);
                        return prop.getProperty(propkey, def);
                }

        }
}