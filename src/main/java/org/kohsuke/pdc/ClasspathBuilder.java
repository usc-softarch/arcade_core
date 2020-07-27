package org.kohsuke.pdc;

import java.io.File;

/**
 * Builds a CLASSPATH string from {@link File}s.
 * 
 * @author
 *      Kohsuke Kawaguchi (kk@kohsuke.org)
 */
public final class ClasspathBuilder {
    /**
     * Path separator.
     * The default value is platform-dependent.
     */
    private String separator = File.pathSeparator;
    
    private final StringBuffer buf = new StringBuffer();
    
    /**
     * Overrides the platform-default separator string.
     */
    public void setSeparator( String sep ) {
        this.separator = sep;
    }
    
    public void reset() {
        buf.setLength(0);
    }
    
    /**
     * Adds a new entry
     */
    public void add( File f ) {
        if( buf.length()!=0 )
            buf.append(separator);
        buf.append(f.toString());
    }
    
    /**
     * Returns the string formatted for the CLASSPATH variable.
     */
    public String getResult() {
        return buf.toString();
    }
}
