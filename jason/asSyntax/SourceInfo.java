package jason.asSyntax;

/**
 * Store information about the file source of some data.
 */
public class SourceInfo {

    private String      source  = null; 
	private int         beginSrcLine = -1; // the line this literal appears in the source
	private int         endSrcLine   = -1;

    public SourceInfo() {        
    }
    
    public SourceInfo(SourceInfo o) {
        setSrc(o);
    }
    
    public void setSrc(SourceInfo o) {
        source       = o.source;
        beginSrcLine = o.beginSrcLine;
        endSrcLine   = o.endSrcLine;
    }
    public void setSrc(Object o) {
        if (o instanceof SourceInfo)
            setSrc((SourceInfo)o);
    }
    
    public void setSrc(String asSource) {
        source = asSource;
    }
    public String getSrc() {
        return source;
    }

    public void setSrcLine(int i) {
		beginSrcLine = i;
	}
    public int getSrcLine() {
    	return beginSrcLine;
    }

    public void setSrcLines(int b, int e) {
        beginSrcLine = b;
        endSrcLine   = e;
    }

    public void setBeginSrcLine(int i) {
        beginSrcLine = i;
    }
    public int getBeginSrcLine() {
        return beginSrcLine;
    }

    public void setEndSrcLine(int i) {
        endSrcLine = i;
    }
    public int getEndSrcLine() {
        return endSrcLine;
    }

    public String getErrorMsg() {
        return getSrc() + (getSrcLine() >= 0 ? ":"+getSrcLine() : "");       
    }    
}
