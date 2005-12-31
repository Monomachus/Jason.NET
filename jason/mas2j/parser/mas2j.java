/* Generated By:JavaCC: Do not edit this line. mas2j.java */
package jason.mas2j.parser;

import java.util.*;
import java.io.*;
import jason.mas2j.*;
import jIDE.*;

public class mas2j implements mas2jConstants {


    MAS2JProject project;

    // Run the parser
    public static void main (String args[]) {

      String name;
      mas2j parser;
      MAS2JProject project = new MAS2JProject();

      if (args.length==1) {
        name = args[0];
        System.err.println("mas2j: reading from file " + name + " ..." );
                try {
                  parser = new mas2j(new java.io.FileInputStream(name));
                } catch(java.io.FileNotFoundException e){
                  System.err.println("mas2j: file \"" + name + "\" not found.");
                  return;
        }
      } else {
                System.out.println("mas2j: usage must be:");
                System.out.println("      java mas2j <MASConfFile>");
                System.out.println("Output to file <MASName>.xml");
        return;
      }

      // parsing
      try {
                project = parser.mas();
                Config.get().fix();
        project.setDirectory(new File(".").getAbsolutePath());
                System.out.println("mas2j: "+name+" parsed successfully!\n");
                project.writeXMLScript();
        project.writeScripts();

        int step = 1;
        System.out.println("To run your MAS:");
        //System.out.println("  1. chmod u+x *.sh");
        System.out.println("  "+step+". compile the java files (script ./compile-"+project.getSocName()+".sh)");
        step++;
        if (project.getInfrastructure().equals("Saci")) {
             System.out.println("  "+step+". run saci (script ./saci-"+project.getSocName()+".sh)");
             step++;
        }
        System.out.println("  "+step+". run your agents (script ./"+project.getSocName()+".sh)");
      }
      catch(ParseException e){
                System.err.println("mas2j: parsing errors found... \n" + e);
      }
    }

/* Configuration Grammar */
  final public MAS2JProject mas() throws ParseException {
                                 Token soc;
    jj_consume_token(MAS);
    soc = jj_consume_token(ASID);
                                 project = new MAS2JProject();
                                 project.setSocName(soc.image);
    jj_consume_token(35);
    infra();
    environment();
    control();
    agents();
    jj_consume_token(36);
                              {if (true) return project;}
    throw new Error("Missing return statement in function");
  }

  final public void infra() throws ParseException {
                              Token t;
                              project.setInfrastructure("Centralised");
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case ARCH:
    case INFRA:
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case ARCH:
        jj_consume_token(ARCH);
                              System.err.println("The id <architecture> was replaced by <infrastructure> in .mas2j syntax, please use the new id.");
        break;
      case INFRA:
        jj_consume_token(INFRA);
        break;
      default:
        jj_la1[0] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
      jj_consume_token(37);
      t = jj_consume_token(INFRAV);
                              project.setInfrastructure(t.image);
      break;
    default:
      jj_la1[1] = jj_gen;
      ;
    }
  }

  final public void agents() throws ParseException {
                              project.initAgMaps();
    jj_consume_token(AGS);
    jj_consume_token(37);
    label_1:
    while (true) {
      agent();
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case ASID:
        ;
        break;
      default:
        jj_la1[2] = jj_gen;
        break label_1;
      }
    }
  }

  final public void agent() throws ParseException {
                              Token agName;
                              Token qty; Token value;
                              Token host;
                              AgentParameters ag = new AgentParameters();
    agName = jj_consume_token(ASID);
                              ag.name = agName.image;
                              ag.asSource = new File(agName.image+".asl");
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case ASID:
    case PATH:
      ag.asSource = fileName();
      break;
    default:
      jj_la1[3] = jj_gen;
      ;
    }
    ag.options = ASoptions();
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case ASAGARCHCLASS:
      jj_consume_token(ASAGARCHCLASS);
      ag.archClass = className();
      break;
    default:
      jj_la1[4] = jj_gen;
      ;
    }
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case ASAGCLASS:
      jj_consume_token(ASAGCLASS);
      ag.agClass = className();
      break;
    default:
      jj_la1[5] = jj_gen;
      ;
    }
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case 38:
      jj_consume_token(38);
      qty = jj_consume_token(NUMBER);
                              ag.qty = Integer.parseInt(qty.image);
      break;
    default:
      jj_la1[6] = jj_gen;
      ;
    }
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case AT:
      jj_consume_token(AT);
      host = jj_consume_token(STRING);
                              ag.host = host.image;
      break;
    default:
      jj_la1[7] = jj_gen;
      ;
    }
    jj_consume_token(39);
                              project.addAgent(ag);
  }

  final public File fileName() throws ParseException {
                              String path = "";
                              Token t;
                              Token i;
                              Token e;
                              String ext = ".asl";
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case PATH:
      t = jj_consume_token(PATH);
                              path = t.image;
      break;
    default:
      jj_la1[8] = jj_gen;
      ;
    }
    i = jj_consume_token(ASID);
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case 40:
      jj_consume_token(40);
      e = jj_consume_token(ASID);
                              ext = "." + e.image;
      break;
    default:
      jj_la1[9] = jj_gen;
      ;
    }
                              //if (!path.startsWith(File.separator)) {
                              //  path = destDir + path;
                              //}
                              {if (true) return new File( path + i.image + ext);}
    throw new Error("Missing return statement in function");
  }

  final public String className() throws ParseException {
                            Token c; String p = "";
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case CLASSID:
      c = jj_consume_token(CLASSID);
      break;
    case ASID:
      c = jj_consume_token(ASID);
      break;
    default:
      jj_la1[10] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case 40:
      jj_consume_token(40);
      p = className();
                             {if (true) return c.image + "." + p;}
      break;
    default:
      jj_la1[11] = jj_gen;
      ;
    }
                             {if (true) return c.image;}
    throw new Error("Missing return statement in function");
  }

  final public Map ASoptions() throws ParseException {
                             Map opts = new HashMap();
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case 41:
      jj_consume_token(41);
      opts = procOption(opts);
      label_2:
      while (true) {
        switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
        case 42:
          ;
          break;
        default:
          jj_la1[12] = jj_gen;
          break label_2;
        }
        jj_consume_token(42);
        opts = procOption(opts);
      }
      jj_consume_token(43);
      break;
    default:
      jj_la1[13] = jj_gen;
      ;
    }
    {if (true) return opts;}
    throw new Error("Missing return statement in function");
  }

  final public Map procOption(Map opts) throws ParseException {
                            Token opt; Token oval;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case ASOEE:
      opt = jj_consume_token(ASOEE);
      jj_consume_token(44);
      oval = jj_consume_token(ASOEEV);
                                      opts.put(opt.image,oval.image);
      break;
    case ASOIB:
      opt = jj_consume_token(ASOIB);
      jj_consume_token(44);
      oval = jj_consume_token(ASOIBV);
                                      opts.put(opt.image,oval.image);
      break;
    case ASOSYNC:
      opt = jj_consume_token(ASOSYNC);
      jj_consume_token(44);
      oval = jj_consume_token(ASOBOOL);
                                      opts.put(opt.image,oval.image);
      break;
    case ASONRC:
      opt = jj_consume_token(ASONRC);
      jj_consume_token(44);
      oval = jj_consume_token(NUMBER);
                                      opts.put(opt.image,oval.image);
      break;
    case ASOV:
      opt = jj_consume_token(ASOV);
      jj_consume_token(44);
      oval = jj_consume_token(NUMBER);
                                      opts.put(opt.image,oval.image);
      break;
    case ASID:
      opt = jj_consume_token(ASID);
      jj_consume_token(44);
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case STRING:
        oval = jj_consume_token(STRING);
        break;
      case ASID:
        oval = jj_consume_token(ASID);
        break;
      case NUMBER:
        oval = jj_consume_token(NUMBER);
        break;
      default:
        jj_la1[14] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
                                      opts.put(opt.image,oval.image);
      break;
    default:
      jj_la1[15] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
                                       {if (true) return opts;}
    throw new Error("Missing return statement in function");
  }

  final public void environment() throws ParseException {
                              Token host = null; String envClass = null;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case ENV:
      jj_consume_token(ENV);
      jj_consume_token(37);
      envClass = className();
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case AT:
        jj_consume_token(AT);
        host = jj_consume_token(STRING);
        break;
      default:
        jj_la1[16] = jj_gen;
        ;
      }
      break;
    default:
      jj_la1[17] = jj_gen;
      ;
    }
                              project.setEnvClass(envClass);
                              if (host != null) {
                                          project.setEnvHost(host.image);
                                  }
  }

  final public void control() throws ParseException {
                              Token host =  null;
                              String tControlClass = null;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case CONTROL:
      jj_consume_token(CONTROL);
      jj_consume_token(37);
      tControlClass = className();
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case AT:
        jj_consume_token(AT);
        host = jj_consume_token(STRING);
        break;
      default:
        jj_la1[18] = jj_gen;
        ;
      }
      break;
    default:
      jj_la1[19] = jj_gen;
      ;
    }
                              project.setControlClass(tControlClass);
                              if (host != null) {
                                   project.setControlHost(host.image);
                              }
  }

  public mas2jTokenManager token_source;
  SimpleCharStream jj_input_stream;
  public Token token, jj_nt;
  private int jj_ntk;
  private int jj_gen;
  final private int[] jj_la1 = new int[20];
  static private int[] jj_la1_0;
  static private int[] jj_la1_1;
  static {
      jj_la1_0();
      jj_la1_1();
   }
   private static void jj_la1_0() {
      jj_la1_0 = new int[] {0x3000,0x3000,0x8000000,0x28000000,0x1000000,0x800000,0x0,0x800,0x20000000,0x0,0x18000000,0x0,0x0,0x0,0xe000000,0x83a8000,0x800,0x200,0x800,0x400,};
   }
   private static void jj_la1_1() {
      jj_la1_1 = new int[] {0x0,0x0,0x0,0x0,0x0,0x0,0x40,0x0,0x0,0x100,0x0,0x100,0x400,0x200,0x0,0x0,0x0,0x0,0x0,0x0,};
   }

  public mas2j(java.io.InputStream stream) {
    jj_input_stream = new SimpleCharStream(stream, 1, 1);
    token_source = new mas2jTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 20; i++) jj_la1[i] = -1;
  }

  public void ReInit(java.io.InputStream stream) {
    jj_input_stream.ReInit(stream, 1, 1);
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 20; i++) jj_la1[i] = -1;
  }

  public mas2j(java.io.Reader stream) {
    jj_input_stream = new SimpleCharStream(stream, 1, 1);
    token_source = new mas2jTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 20; i++) jj_la1[i] = -1;
  }

  public void ReInit(java.io.Reader stream) {
    jj_input_stream.ReInit(stream, 1, 1);
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 20; i++) jj_la1[i] = -1;
  }

  public mas2j(mas2jTokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 20; i++) jj_la1[i] = -1;
  }

  public void ReInit(mas2jTokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 20; i++) jj_la1[i] = -1;
  }

  final private Token jj_consume_token(int kind) throws ParseException {
    Token oldToken;
    if ((oldToken = token).next != null) token = token.next;
    else token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    if (token.kind == kind) {
      jj_gen++;
      return token;
    }
    token = oldToken;
    jj_kind = kind;
    throw generateParseException();
  }

  final public Token getNextToken() {
    if (token.next != null) token = token.next;
    else token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    jj_gen++;
    return token;
  }

  final public Token getToken(int index) {
    Token t = token;
    for (int i = 0; i < index; i++) {
      if (t.next != null) t = t.next;
      else t = t.next = token_source.getNextToken();
    }
    return t;
  }

  final private int jj_ntk() {
    if ((jj_nt=token.next) == null)
      return (jj_ntk = (token.next=token_source.getNextToken()).kind);
    else
      return (jj_ntk = jj_nt.kind);
  }

  private java.util.Vector jj_expentries = new java.util.Vector();
  private int[] jj_expentry;
  private int jj_kind = -1;

  public ParseException generateParseException() {
    jj_expentries.removeAllElements();
    boolean[] la1tokens = new boolean[45];
    for (int i = 0; i < 45; i++) {
      la1tokens[i] = false;
    }
    if (jj_kind >= 0) {
      la1tokens[jj_kind] = true;
      jj_kind = -1;
    }
    for (int i = 0; i < 20; i++) {
      if (jj_la1[i] == jj_gen) {
        for (int j = 0; j < 32; j++) {
          if ((jj_la1_0[i] & (1<<j)) != 0) {
            la1tokens[j] = true;
          }
          if ((jj_la1_1[i] & (1<<j)) != 0) {
            la1tokens[32+j] = true;
          }
        }
      }
    }
    for (int i = 0; i < 45; i++) {
      if (la1tokens[i]) {
        jj_expentry = new int[1];
        jj_expentry[0] = i;
        jj_expentries.addElement(jj_expentry);
      }
    }
    int[][] exptokseq = new int[jj_expentries.size()][];
    for (int i = 0; i < jj_expentries.size(); i++) {
      exptokseq[i] = (int[])jj_expentries.elementAt(i);
    }
    return new ParseException(token, exptokseq, tokenImage);
  }

  final public void enable_tracing() {
  }

  final public void disable_tracing() {
  }

}