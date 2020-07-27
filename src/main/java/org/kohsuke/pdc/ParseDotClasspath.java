package org.kohsuke.pdc;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Parses Eclipse's &#x2E;classpath files and turns them into
 * the CLASSPATH env var format.
 * 
 * @author
 *      Kohsuke Kawaguchi (kk@kohsuke.org)
 * @author joshua
 */
public class ParseDotClasspath {

  private static final String KIND_VAR = "var";

  static Set kinds = new HashSet(Arrays.asList(new String[]{"lib", "output", "src", KIND_VAR}));
  private static boolean useOutputKind = true;

  private static void usage ()
  {
        System.err.println(
            "Usage: java -jar parse-dot-classpath.jar [<options>] <file>*\n"+
            "Reads Eclipse's .classpath file(s), format them as a CLASSPATH\n"+
            "env var format, then print it to stdout.\n"+
            "\n"+
            "if a file is specified, it will be read as a .classpath file\n"+
            "if a directory is specified, .classpath inside ths given directory will be read\n"+
            "if no argument is specified, ./.classpath will be read\n"+
            "\n"+
            "Options:\n"+
            "  -s <sep>: change the path separator to <sep>.\n" +
            "Define system properties to use as the prefix of kind=var classpath entries\n" +
            "for example -DM2_REPO=c:/m2/repository\n"
        );
    }

    public static void main(String[] args) throws Exception {
        ClasspathBuilder builder = new ClasspathBuilder();
        List a = new ArrayList();

        for( int i=0; i<args.length; i++ ) {
            String arg = args[i].intern();
            if(arg=="-s") {
                builder.setSeparator(args[++i]);
                continue;
            }
            if(arg.startsWith("-")) {
                usage();
                System.exit(-1);
            }
            a.add(arg);
        }

        if(a.size()==0)
            // no argument specified. read from the current dir
            parseDotClasspath(new File("./.classpath"),builder);

        for( int i=0; i<a.size(); i++ ) {
            File dotCp = new File((String)a.get(i));
            if( dotCp.isDirectory() ) {
                // if directory, append the default name
                dotCp = new File( dotCp, ".classpath" );
            }
            parseDotClasspath(dotCp,builder);
        }

        // print the result
        System.out.println(builder.getResult());

        //System.exit(0);
    }
    
    /**
     * Reads a ".classpath" file and turns it into a string
     * formatted to fit the CLASSPATH variable.
     */
    public static void parseDotClasspath( File dotClasspath, final ClasspathBuilder builder ) throws IOException,SAXException,ParserConfigurationException {
        // all entries in .classpath are relative to this directory.
        final File baseDir = dotClasspath.getParentFile().getAbsoluteFile();

//        XMLReader parser = XMLReaderFactory.createXMLReader();
        SAXParserFactory spf =SAXParserFactory.newInstance();
        spf.setNamespaceAware(true);
        XMLReader parser = spf.newSAXParser().getXMLReader();
        parser.setContentHandler(new DefaultHandler() {
            public void startElement(String uri,String localName,String qname,Attributes atts) {
                if( !localName.equals("classpathentry") )
                    return; // unknown

                String kind = atts.getValue("kind");
                if (kind != null && kinds.contains(kind)) {
                    String path = atts.getValue("path");
                    if (kind.equals(KIND_VAR)) {
                        int i = path.indexOf('/');
                        String dir = System.getenv(path.substring(0, i));
                        path = dir + '/' + path.substring(i + 1);
                    }

                    builder.add(absolutize(baseDir, path));
                }
                
                if (useOutputKind) {
					String output = atts.getValue("output");
					if (output != null) {
						builder.add(absolutize(baseDir, output));
					}
                }
            }
        });
        parser.parse(dotClasspath.toURL().toString());
    }

    private static File absolutize( File base, String path ) {
        path = path.replace('/',File.separatorChar);
        File child = new File(path);
        if(child.isAbsolute())
            return child;
        else
            return new File(base,path);
    }
}
