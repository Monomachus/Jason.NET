package jason.bb;

import jason.asSemantics.Agent;
import jason.asSyntax.DefaultTerm;
import jason.asSyntax.ListTerm;
import jason.asSyntax.ListTermImpl;
import jason.asSyntax.Literal;
import jason.asSyntax.PredicateIndicator;
import jason.asSyntax.StringTerm;
import jason.asSyntax.StringTermImpl;
import jason.asSyntax.Structure;
import jason.asSyntax.Term;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of BB that stores some beliefs in a relational data base.
 * 
 *  @author Jomi
 */
public class JDBCPersistentBB extends DefaultBeliefBase {
    static private Logger logger     = Logger.getLogger(JDBCPersistentBB.class.getName());

    static final String   COL_PREFIX = "term";
    static final String   COL_NEG    = "j_negated";
    static final String   COL_ANNOT  = "j_annots";

    protected int extraCols = 0;
    
    protected Connection  conn;
    protected String      url;
    protected String      agentName;
    
    public JDBCPersistentBB() {
        extraCols = 2;
    }
    
    // map of bels in DB
    Map<PredicateIndicator, ResultSetMetaData> belsDB = new HashMap<PredicateIndicator, ResultSetMetaData>();

    // TODO: get column names/type
    // [book(5,book,columns(id(integer), title(varchar(20)),....
    
    /**
     * args[0] is the Database Engine JDBC drive. args[1] is the JDBC URL
     * connection string, args[2] is the username, args[3] is the password,
     * args[4] AS list with beliefs mapped to DB, each element is in the form
     * "bel(arity[,table_name])".
     * 
     * The url can use the agent name as parameter as in "jdbc:mysql://localhost/%s".
     * In this case, %s will be replaced by the agent's name. 
     * 
     * Example in .mas2j project:<br>
     * <code>agents: a beliefBaseClass jason.bb.JDBCPersistentBB(
     * "org.hsqldb.jdbcDriver", "jdbc:hsqldb:bookstore", "sa", "",
     * "[book(5,author(2,book_author)]")</code><br>
     */
    @Override
    public void init(Agent ag, String[] args) {
        try {
            agentName = ag.getTS().getUserAgArch().getAgName();
        } catch (Exception _) {
            logger.warning("Can not get the agent name!");
            agentName = "none";
        }
        try {
            logger.fine("Loading driver " + args[0]);
            Class.forName(args[0]);
            url = String.format(args[1], agentName);
            logger.fine("Connecting: url= " + url + ", user=" + args[2] + ", password=" + args[3]);
            conn = DriverManager.getConnection(url, args[2], args[3]);

            // load tables mapped to DB
            ListTerm lt = ListTermImpl.parseList(args[4]);
            for (Term t : lt) {
                Structure ts = (Structure)t;
                int arity    = Integer.parseInt(ts.getTerm(0).toString());
            	String table = ts.getFunctor();
                if (ts.getTermsSize() >= 2) {
                	table = ts.getTerm(1).toString();
                }

                // create the table and get its Metadata
                Statement stmt = conn.createStatement();
                ResultSet rs;
                try {
                    rs = stmt.executeQuery("select * from " + table);
                } catch (SQLException e) {
                    // create table
                    stmt.executeUpdate(getCreateTable(table, arity));
                    rs = stmt.executeQuery("select * from " + table);
                }
                belsDB.put(new PredicateIndicator(ts.getFunctor(), arity), rs.getMetaData());
                belsDB.put(new PredicateIndicator("~"+ts.getFunctor(), arity), rs.getMetaData());
                stmt.close();
            }
            logger.fine("Map=" + belsDB);
        } catch (ArrayIndexOutOfBoundsException e) {
            logger.log(Level.SEVERE, "Wrong parameters for JDBCPersistentBB initialisation.", e);
        } catch (ClassNotFoundException e) {
            logger.log(Level.SEVERE, "Error loading jdbc driver " + args[0], e);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "DB connection failure. url= " + url + ", user=" + args[2] + ", password=" + args[3], e);
        }
    }

    @Override
    public void stop() {
        try {
            if (url.startsWith("jdbc:hsqldb")) {
                conn.createStatement().execute("SHUTDOWN");
            }
            conn.close(); // if there are no other open connection
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error in shutdown SGBD ", e);
        }
    }

    protected boolean isDB(Literal l) {
        return belsDB.get(l.getPredicateIndicator()) != null;
    }

    protected String getCreateTable(String table, int arity) throws SQLException {
        StringBuilder ct = new StringBuilder("create table " + table + " (");
        for (int c = 0; c < arity; c++) {
            ct.append(COL_PREFIX + c + " varchar(256), ");
        }
        ct.append(COL_NEG + " boolean, " + COL_ANNOT + " varchar(256))");
        logger.fine("Creating table: " + ct);
        return ct.toString();
    }
    
    protected boolean isCreatedByJason(PredicateIndicator pi) throws SQLException {
        ResultSetMetaData meta = belsDB.get(pi);
        if (meta != null) {
            int cols = meta.getColumnCount();
            return cols >= extraCols && 
                   meta.getColumnName(cols - 1).equalsIgnoreCase(COL_NEG) && 
                   meta.getColumnName(cols).equalsIgnoreCase(COL_ANNOT);
        }
        return false;
    }

    @Override
    public Literal contains(Literal l) {
        if (!isDB(l))
            return super.contains(l);

        Statement stmt = null;
        try {
            // create a literal from query
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(getSelect(l));
            if (rs.next()) {
                return resultSetToLiteral(rs,l.getPredicateIndicator());
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "SQL Error", e);
        } finally {
            try {
                stmt.close();
            } catch (Exception e) {
                logger.log(Level.WARNING, "SQL Error closing connection", e);
            }
        }
        return null;
    }

    @Override
    public boolean add(Literal l) {
        if (!isDB(l))
            return super.add(l);

        Literal bl = contains(l);
        Statement stmt = null;
        try {
            if (bl != null) {
                if (isCreatedByJason(l.getPredicateIndicator())) {
                    // add only annots
                    if (l.hasSubsetAnnot(bl))
                        // the current bel bl already has l's annots
                        return false;
                    else {
                        // "import" annots from the new bel
                        bl.importAnnots(l);
                        
                        // check if it needs to be added in the percepts list
                        if (l.hasAnnot(TPercept)) {
                            percepts.add(bl);
                        }

                        // store bl annots
                        stmt = conn.createStatement();
                        stmt.executeUpdate("update "+getTableName(bl)+" set "+COL_ANNOT+" = '"+bl.getAnnots()+"' "+getWhere(l));
                        return true;
                    }
                }
            } else {
                // create insert command
                stmt = conn.createStatement();
                stmt.executeUpdate(getInsert(l));
                // add it in the percepts list
                if (l.hasAnnot(TPercept)) {
                    percepts.add(l);
                }
                return true;
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "SQL Error", e);
        } finally {
            try {
                stmt.close();
            } catch (Exception e) {
            }
        }
        return false;
    }

    
    @Override
    public boolean remove(Literal l) {
        if (!isDB(l))
            return super.add(l);

        Literal bl = contains(l);
        if (bl != null) {
            Statement stmt = null;
            try {
                if (l.hasSubsetAnnot(bl)) {
                    if (l.hasAnnot(TPercept)) {
                        percepts.remove(bl);
                    }
                    boolean result = bl.delAnnot(l) || !bl.hasAnnot();
                    stmt = conn.createStatement();
                    if (bl.hasAnnot() && isCreatedByJason(l.getPredicateIndicator())) {
                        // store new bl annots
                        stmt.executeUpdate("update "+getTableName(bl)+" set "+COL_ANNOT+" = '"+bl.getAnnots()+"' "+getWhere(l));
                    } else {
                        // remove from DB
                        stmt.executeUpdate("delete from "+getTableName(bl)+getWhere(bl));                        
                    }
                    return result;                    
                }
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "SQL Error", e);
            } finally {
                try {
                    stmt.close();
                } catch (Exception e) {
                    logger.log(Level.WARNING, "SQL Error closing connection", e);                    
                }
            }
        }
        return false;
    }

    
    @Override
    public boolean abolish(PredicateIndicator pi) {
        if (belsDB.get(pi) == null)
            return super.abolish(pi);

        Statement stmt = null;
        try {
            stmt = conn.createStatement();
            stmt.executeUpdate(getDeleteAll(pi));
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "SQL Error", e);
        } finally {
            try {
                stmt.close();
            } catch (Exception e) {
                logger.log(Level.WARNING, "SQL Error closing connection", e);
            }
        }
        return false;
    }

    protected String getDeleteAll(PredicateIndicator pi) throws SQLException {
        return "delete from " + getTableName(pi);
    }

    
    // use the same statement for all queries, so previous queries are closed before new ones
    private Statement relevantStmt;
    
    /** returns a statement for getRelevant method */
    protected Statement getRelevantStatement() throws SQLException {
        if (relevantStmt == null) {
            relevantStmt = conn.createStatement();
        }
        return relevantStmt;
    }
    
    @Override
    public Iterator<Literal> getRelevant(Literal l) {
        final PredicateIndicator pi = l.getPredicateIndicator();
        if (belsDB.get(pi) == null)
            return super.getRelevant(l);
        
        if (l.isVar()) {
            // all bels are relevant
            return iterator();
        } else {
            // get all rows of l's table
            try {
                final ResultSet rs = getRelevantStatement().executeQuery(getSelectAll(pi));
                return new Iterator<Literal>() {
                    boolean hasNext   = true;
                    boolean firstcall = true;
                    public boolean hasNext() {
                        if (firstcall) {
                            try {
                                hasNext = rs.next();
                            } catch (SQLException e) {
                                logger.log(Level.SEVERE, "SQL Error", e);
                            }
                            firstcall = false;
                        }
                        return hasNext;
                    }
                    public Literal next() {
                        try {
                            if (firstcall) {
                                hasNext = rs.next();
                                firstcall = false;
                            }
                            Literal l = resultSetToLiteral(rs,pi);
                            hasNext = rs.next();
                            return l;
                        } catch (SQLException e) {
                            logger.log(Level.SEVERE, "SQL Error", e);
                        }
                        return null;
                    }
                    public void remove() { 
                        logger.warning("remove in jdbc get relevant is not implemented!");  
                    }
                };
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "SQL Error", e);
            }
        }
        return null;
    }

    @Override
    public int size() {
        int count = 0;
        Statement stmt = null;
        try {
            stmt = conn.createStatement();
            // for all tables, count rows
            for (PredicateIndicator pi : belsDB.keySet()) {
                if (!pi.getFunctor().startsWith("~")) {
                    ResultSet rs = stmt.executeQuery(getCountQuery(pi));
                    if (rs.next()) {
                        count += rs.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "SQL Error", e);
        } finally {
            try {
                stmt.close();
            } catch (Exception e) {
                logger.log(Level.WARNING, "SQL Error closing connection", e);
            }
        }
        return count + super.size();
    }
    
    protected String getCountQuery(PredicateIndicator pi) throws SQLException {
        return "select count(*) from " + getTableName(pi);
    }

    @Override
    public Iterator<Literal> iterator() {
        List<Literal> all = new ArrayList<Literal>(size());
        
        Iterator<Literal> is = super.iterator();
        while (is.hasNext()) {
            all.add(is.next());
        }
        
        Statement stmt = null;
        try {
            stmt = conn.createStatement();
            // for all tables, get rows literal
            for (PredicateIndicator pi : belsDB.keySet()) {
                if (!pi.getFunctor().startsWith("~")) {
                    ResultSet rs = stmt.executeQuery(getSelectAll(pi));
                    while (rs.next()) {
                        all.add( resultSetToLiteral(rs, pi));
                    }
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "SQL Error", e);
        } finally {
            try {
                stmt.close();
            } catch (Exception e) {
                logger.log(Level.WARNING, "SQL Error closing connection", e);
            }
        }    
        return all.iterator();
    }
    

    /** translates the current line of a result set into a Literal */
    protected Literal resultSetToLiteral(ResultSet rs, PredicateIndicator pi) throws SQLException {
        ResultSetMetaData meta = belsDB.get(pi);
        boolean isJasonTable = isCreatedByJason(pi);
        Literal ldb = new Literal(pi.getFunctor());
        int end = meta.getColumnCount();
        if (isJasonTable)
            end = end - extraCols;
        for (int c = 1; c <= end; c++) {
            String sc = rs.getString(c);
            Term parsed = null;
            if (sc.trim().length() == 0) {
                parsed = new StringTermImpl("");
            } else if (Character.isUpperCase(sc.charAt(0))) {
                // there no var at BB
                parsed = new StringTermImpl(sc);
            } else {
                parsed = DefaultTerm.parse(sc);
                
                // if the parsed term is not equals to sc, try as string
                if (!parsed.toString().equals(sc)) {
                    parsed = DefaultTerm.parse(sc = "\"" + sc + "\"");
                }
            }
            ldb.addTerm(parsed);
        }
        if (isJasonTable) {
            ldb.setNegated(!rs.getBoolean(end + 1));
            ldb.setAnnots(ListTermImpl.parseList(rs.getString(end + 2)));
        }
        return ldb;
    }
    
    protected String getTableName(Literal l) throws SQLException {
        return getTableName(l.getPredicateIndicator());
    }
    
    protected String getTableName(PredicateIndicator pi) throws SQLException {
        ResultSetMetaData meta = belsDB.get(pi);
        return meta.getTableName(1);
    }
    
    protected String getSelect(Literal l) throws SQLException {
        return "select * from "+getTableName(l)+getWhere(l);
    }

    protected String getSelectAll(PredicateIndicator pi) throws SQLException {
        return "select * from " + getTableName(pi);
    }
    
    protected String getWhere(Literal l) throws SQLException {
        ResultSetMetaData meta = belsDB.get(l.getPredicateIndicator());
        StringBuilder q = new StringBuilder(" where ");
        String and = "";
        // for all ground terms of l
        for (int i = 0; i < l.getTermsSize(); i++) {
            Term t = l.getTerm(i);
            if (t.isGround()) {
                q.append(and);
                String ts;
                if (t.isString()) {
                    ts = "'" + ((StringTerm) t).getString() + "'";
                } else if (t.isNumeric()) {
                    ts = t.toString();
                } else {
                    ts = "'" + t.toString() + "'";
                }
                q.append(meta.getColumnName(i + 1) + " = " + ts);
                and = " and ";
            }
        }
        if (isCreatedByJason(l.getPredicateIndicator())) {
            q.append(and + COL_NEG + " = " + l.negated());
        }
        //System.out.println(q.toString());
        return q.toString();
    }

    protected String getInsert(Literal l) throws SQLException {
        StringBuilder q = new StringBuilder("insert into ");
        ResultSetMetaData meta = belsDB.get(l.getPredicateIndicator());
        q.append(meta.getTableName(1));
        q.append(" values(");

        // values
        for (int i = 0; i < l.getTermsSize(); i++) {
            Term t = l.getTerm(i);
            if (t.isString()) {
                q.append("'" + ((StringTerm) t).getString() + "'");
            } else {
                q.append("'" + t.toString() + "'");
            }
            if (i < meta.getColumnCount() - 1) {
                q.append(",");
            }
        }
        if (isCreatedByJason(l.getPredicateIndicator())) {
            q.append(l.negated() + ",");
            if (l.hasAnnot()) {
                q.append("\'" + l.getAnnots() + "\'");
            } else {
                q.append("\'[]\'");
            }
        }
        q.append(")");
        return q.toString();
    }

    /** just create some data to test */
    public void test() {
        Statement stmt = null;
        try {
            // add a "legacy" table
            stmt = conn.createStatement();
            try {
                stmt.executeUpdate("drop table publisher");
            } catch (Exception e) {
            }
            stmt.executeUpdate("create table publisher (id integer, name varchar)");
            stmt.executeUpdate("insert into publisher values(1, 'Springer')");
            stmt.executeUpdate("insert into publisher values(2, 'MIT Press')");
            ResultSetMetaData meta = stmt.executeQuery("select * from publisher").getMetaData();
            belsDB.put(new PredicateIndicator("publisher", 2), meta);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "SQL Error", e);
        } finally {
            try {
                stmt.close();
            } catch (Exception e) {
                logger.log(Level.WARNING, "SQL Error closing connection", e);
            }
        }
    }
}
