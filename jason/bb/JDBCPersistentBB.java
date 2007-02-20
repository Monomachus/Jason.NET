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
 */
public class JDBCPersistentBB extends DefaultBeliefBase {
    static private Logger                      logger     = Logger.getLogger(JDBCPersistentBB.class.getName());

    static final String                        COL_PREFIX = "term";
    static final String                        COL_NEG    = "j_negated";
    static final String                        COL_ANNOT  = "j_annots";

    Connection                                 conn;

    // map of bels in DB
    Map<PredicateIndicator, ResultSetMetaData> belsDB     = new HashMap<PredicateIndicator, ResultSetMetaData>();

    /**
     * args[0] is the Database Engine JDBC drive. args[1] is the JDBC URL
     * connection string, args[2] is the username, args[3] is the password,
     * args[4] AS list with beliefs mapped to DB, each element is in the form
     * "bel(arity[,table_name])".
     * 
     * Example in .mas2j project:<br>
     * <code>agents: a beliefBaseClass jason.bb.JDBCPersistentBB(
     * "org.hsqldb.jdbcDriver", "jdbc:hsqldb:bookstore", "sa", "",
     * "[book(5,author(2,book_author)]")</code><br>
     */
    @Override
    public void init(Agent ag, String[] args) {
        try {
            logger.fine("Loading driver " + args[0]);
            Class.forName(args[0]);
            logger.fine("Connecting: url= " + args[1] + ", user=" + args[2] + ", password=" + args[3]);
            conn = DriverManager.getConnection(args[1], args[2], args[3]);

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
                    StringBuilder ct = new StringBuilder("create table " + table + " (");
                    for (int c = 0; c < arity; c++) {
                        ct.append(COL_PREFIX + c + " varchar, ");
                    }
                    ct.append(COL_NEG + " boolean, " + COL_ANNOT + " varchar);");
                    logger.fine("Creating table: " + ct);
                    stmt.executeUpdate(ct.toString());
                    rs = stmt.executeQuery("select * from " + table);
                }
                stmt.close();
                belsDB.put(new PredicateIndicator(ts.getFunctor(), arity), rs.getMetaData());
                belsDB.put(new PredicateIndicator("~"+ts.getFunctor(), arity), rs.getMetaData());
            }
            logger.fine("Map=" + belsDB);
        } catch (ArrayIndexOutOfBoundsException e) {
            logger.log(Level.SEVERE, "Wrong parameters for JDBCPersistenBB initialisation.", e);
        } catch (ClassNotFoundException e) {
            logger.log(Level.SEVERE, "Error loading jdbc driver " + args[0], e);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "DB connection failure. url= " + args[1] + ", user=" + args[2] + ", password=" + args[3], e);
        }
    }

    @Override
    public void stop() {
        try {
            Statement st = conn.createStatement();
            st.execute("SHUTDOWN");
            conn.close(); // if there are no other open connection
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error in shutdown SGBD ", e);
        }
    }

    protected boolean isDB(Literal l) {
        return belsDB.get(l.getPredicateIndicator()) != null;
    }

    protected boolean isCreatedByJason(PredicateIndicator pi) throws SQLException {
        ResultSetMetaData meta = belsDB.get(pi);
        if (meta != null) {
            int cols = meta.getColumnCount();
            return meta.getColumnName(cols - 1).equalsIgnoreCase(COL_NEG) && meta.getColumnName(cols).equalsIgnoreCase(COL_ANNOT);
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
                } catch (Exception e) {}
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
            stmt.executeUpdate("delete from " + getTableName(pi));
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
    public Iterator<Literal> getRelevant(Literal l) {
        final PredicateIndicator pi = l.getPredicateIndicator();
        if (belsDB.get(pi) == null)
            return super.getRelevant(l);
        
        if (l.isVar()) {
            // all bels are relevant
            return iterator();
        } else {
            // get all rows of l's table
            Statement stmt = null;
            try {
                stmt = conn.createStatement();
                final ResultSet rs = stmt.executeQuery("select * from " + getTableName(pi));
                return new Iterator<Literal>() {
                    boolean hasNext = true;
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
            } finally {
                try {
                    stmt.close();
                } catch (Exception e) {
                }
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
                    ResultSet rs = stmt.executeQuery("select count(*) from " + getTableName(pi));
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
            }
        }
        return count + super.size();
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
                    ResultSet rs = stmt.executeQuery("select * from " + getTableName(pi));
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
            }
        }    
        return all.iterator();
    }
    

    protected Literal resultSetToLiteral(ResultSet rs, PredicateIndicator pi) throws SQLException {
        ResultSetMetaData meta = belsDB.get(pi);
        boolean isJasonTable = isCreatedByJason(pi);
        Literal ldb = new Literal(pi.getFunctor());
        int end = meta.getColumnCount();
        if (isJasonTable)
            end = end - 2;
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
    
    private String getTableName(Literal l) throws SQLException {
        return getTableName(l.getPredicateIndicator());
    }
    private String getTableName(PredicateIndicator pi) throws SQLException {
        ResultSetMetaData meta = belsDB.get(pi);
        return meta.getTableName(1);
    }
    
    private String getSelect(Literal l) throws SQLException {
        return "select * from "+getTableName(l)+getWhere(l);
    }

    private String getWhere(Literal l) throws SQLException {
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

    private String getInsert(Literal l) throws SQLException {
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
            }
        }
    }
}
