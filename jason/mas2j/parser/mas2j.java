/* Generated By:JavaCC: Do not edit this line. mas2j.java */
package jason.mas2j.parser;

import java.util.*;
import java.io.*;
import jason.mas2j.*;
import jason.asSyntax.*;
import jason.asSemantics.*;
import jason.jeditplugin.*;

public class mas2j implements mas2jConstants {


    MAS2JProject project;

    // Run the parser
    public static void main (String args[]) {

      String name;
      mas2j parser;
      MAS2JProject project = new MAS2JProject();

      if (args.length == 0) {
                System.out.println("mas2j: usage must be:");
                System.out.println("      java mas2j <MASConfFile>");
                System.out.println("Output to file build.xml");
        return;
      } else {
        name = args[0];
        System.err.println("mas2j: reading from file " + name + " ..." );
                try {
                  parser = new mas2j(new java.io.FileInputStream(name));
                } catch(java.io.FileNotFoundException e){
                  System.err.println("mas2j: file \"" + name + "\" not found.");
                  return;
        }
      }

      boolean runmas   = args.length >= 2 && args[1].equals("run");
      boolean debugmas = args.length >= 2 && args[1].equals("debug");
      if (debugmas) runmas = true;

      // parsing
      try {
                project = parser.mas();
                Config.get().fix();
                File file = new File(name);
        File directory = file.getAbsoluteFile().getParentFile();
        project.setDirectory(directory.toString());
        project.setProjectFile(file);
                System.out.println("mas2j: "+name+" parsed successfully!\n");
                MASLauncherInfraTier launcher = project.getInfrastructureFactory().createMASLauncher();
                launcher.setProject(project);
                launcher.writeScripts(debugmas);

                if (runmas) {
                    new Thread(launcher, "MAS-Launcher").start();
                } else {
                System.out.println("To run your MAS, just type \"ant -f bin/build.xml\"");
            }
      } catch(Exception e){
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
    jj_consume_token(37);
    infra();
    environment();
    control();
    agents();
    directives();
    classpath();
    sourcepath();
    jj_consume_token(38);
                              {if (true) return project;}
    throw new Error("Missing return statement in function");
  }

  final public void infra() throws ParseException {
                              ClassParameters infra;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case INFRA:
      jj_consume_token(INFRA);
      jj_consume_token(39);
      infra = classDef();
                              project.setInfrastructure(infra);
      break;
    default:
      jj_la1[0] = jj_gen;
      ;
    }
  }

  final public void agents() throws ParseException {
                              project.initAgMap();
    jj_consume_token(AGS);
    jj_consume_token(39);
    label_1:
    while (true) {
      agent();
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case ASID:
        ;
        break;
      default:
        jj_la1[1] = jj_gen;
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
      jj_la1[2] = jj_gen;
      ;
    }
    ag.options = ASoptions();
    label_2:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case AT:
      case ASAGCLASS:
      case ASAGARCHCLASS:
      case BBCLASS:
      case 40:
        ;
        break;
      default:
        jj_la1[3] = jj_gen;
        break label_2;
      }
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case ASAGARCHCLASS:
        jj_consume_token(ASAGARCHCLASS);
        ag.archClass = classDef();
        break;
      case ASAGCLASS:
        jj_consume_token(ASAGCLASS);
        ag.agClass = classDef();
        break;
      case BBCLASS:
        jj_consume_token(BBCLASS);
        ag.bbClass = classDef();
        break;
      case 40:
        jj_consume_token(40);
        qty = jj_consume_token(NUMBER);
                            ag.qty = Integer.parseInt(qty.image);
        break;
      case AT:
        jj_consume_token(AT);
        host = jj_consume_token(STRING);
                            ag.host = host.image;
        break;
      default:
        jj_la1[4] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
    }
    jj_consume_token(41);
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
      jj_la1[5] = jj_gen;
      ;
    }
    i = jj_consume_token(ASID);
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case 42:
      jj_consume_token(42);
      e = jj_consume_token(ASID);
                              ext = "." + e.image;
      break;
    default:
      jj_la1[6] = jj_gen;
      ;
    }
                              //if (!path.startsWith(File.separator)) {
                              //  path = destDir + path;
                              //}
                              {if (true) return new File( path + i.image + ext);}
    throw new Error("Missing return statement in function");
  }

  final public ClassParameters classDef() throws ParseException {
                                Token c; String p = ""; ClassParameters cp = new ClassParameters();
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case ID:
      c = jj_consume_token(ID);
      break;
    case ASID:
      c = jj_consume_token(ASID);
      break;
    default:
      jj_la1[7] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
                                          cp.className = c.image;
    label_3:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case 42:
        ;
        break;
      default:
        jj_la1[8] = jj_gen;
        break label_3;
      }
      jj_consume_token(42);
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case ID:
        c = jj_consume_token(ID);
        break;
      case ASID:
        c = jj_consume_token(ASID);
        break;
      default:
        jj_la1[9] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
                                          cp.className += "." + c.image;
    }
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case 43:
      jj_consume_token(43);
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case NUMBER:
      case STRING:
      case ASID:
      case ID:
        switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
        case ID:
          c = jj_consume_token(ID);
          break;
        case ASID:
          c = jj_consume_token(ASID);
          break;
        case NUMBER:
          c = jj_consume_token(NUMBER);
          break;
        case STRING:
          c = jj_consume_token(STRING);
          break;
        default:
          jj_la1[10] = jj_gen;
          jj_consume_token(-1);
          throw new ParseException();
        }
                                                                    cp.parameters.add(c.image);
        label_4:
        while (true) {
          switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
          case 44:
            ;
            break;
          default:
            jj_la1[11] = jj_gen;
            break label_4;
          }
          jj_consume_token(44);
          switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
          case ID:
            c = jj_consume_token(ID);
            break;
          case ASID:
            c = jj_consume_token(ASID);
            break;
          case NUMBER:
            c = jj_consume_token(NUMBER);
            break;
          case STRING:
            c = jj_consume_token(STRING);
            break;
          default:
            jj_la1[12] = jj_gen;
            jj_consume_token(-1);
            throw new ParseException();
          }
                                                                    cp.parameters.add(c.image);
        }
        break;
      default:
        jj_la1[13] = jj_gen;
        ;
      }
      jj_consume_token(45);
      break;
    default:
      jj_la1[14] = jj_gen;
      ;
    }
                             {if (true) return cp;}
    throw new Error("Missing return statement in function");
  }

  final public Map ASoptions() throws ParseException {
                             Map opts = new HashMap();
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case 46:
      jj_consume_token(46);
      opts = procOption(opts);
      label_5:
      while (true) {
        switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
        case 44:
          ;
          break;
        default:
          jj_la1[15] = jj_gen;
          break label_5;
        }
        jj_consume_token(44);
        opts = procOption(opts);
      }
      jj_consume_token(47);
      break;
    default:
      jj_la1[16] = jj_gen;
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
      jj_consume_token(48);
      oval = jj_consume_token(ASOEEV);
                                        opts.put(opt.image,oval.image);
      break;
    case ASOIB:
      opt = jj_consume_token(ASOIB);
      jj_consume_token(48);
      oval = jj_consume_token(ASOIBV);
                                        opts.put(opt.image,oval.image);
      break;
    case ASOSYNC:
      opt = jj_consume_token(ASOSYNC);
      jj_consume_token(48);
      oval = jj_consume_token(ASOBOOL);
                                        opts.put(opt.image,oval.image);
      break;
    case ASONRC:
      opt = jj_consume_token(ASONRC);
      jj_consume_token(48);
      oval = jj_consume_token(NUMBER);
                                        opts.put(opt.image,oval.image);
      break;
    case ASOV:
      opt = jj_consume_token(ASOV);
      jj_consume_token(48);
      oval = jj_consume_token(NUMBER);
                                        opts.put(opt.image,oval.image);
      break;
    case ASID:
      opt = jj_consume_token(ASID);
      jj_consume_token(48);
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
      case ID:
        oval = jj_consume_token(ID);
        break;
      default:
        jj_la1[17] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
                                        opts.put(opt.image,oval.image);
      break;
    default:
      jj_la1[18] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
                                         {if (true) return opts;}
    throw new Error("Missing return statement in function");
  }

  final public void environment() throws ParseException {
                              Token host = null; ClassParameters envClass = null;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case ENV:
      jj_consume_token(ENV);
      jj_consume_token(39);
      envClass = classDef();
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case AT:
        jj_consume_token(AT);
        host = jj_consume_token(STRING);
        break;
      default:
        jj_la1[19] = jj_gen;
        ;
      }
      break;
    default:
      jj_la1[20] = jj_gen;
      ;
    }
                              project.setEnvClass(envClass);
                              if (host != null) {
                                          envClass.host = host.image;
                                  }
  }

  final public void control() throws ParseException {
                              Token host =  null;
                              ClassParameters controlClass = null;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case CONTROL:
      jj_consume_token(CONTROL);
      jj_consume_token(39);
      controlClass = classDef();
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case AT:
        jj_consume_token(AT);
        host = jj_consume_token(STRING);
        break;
      default:
        jj_la1[21] = jj_gen;
        ;
      }
      break;
    default:
      jj_la1[22] = jj_gen;
      ;
    }
                              project.setControlClass(controlClass);
                              if (host != null) {
                                   controlClass.host = host.image;
                              }
  }

  final public void classpath() throws ParseException {
                              Token cp;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case CLASSPATH:
      jj_consume_token(CLASSPATH);
      jj_consume_token(39);
      label_6:
      while (true) {
        cp = jj_consume_token(STRING);
        jj_consume_token(41);
                              project.addClassPath(cp.image);
        switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
        case STRING:
          ;
          break;
        default:
          jj_la1[23] = jj_gen;
          break label_6;
        }
      }
      break;
    default:
      jj_la1[24] = jj_gen;
      ;
    }
  }

  final public void sourcepath() throws ParseException {
                              Token cp;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case SOURCEPATH:
      jj_consume_token(SOURCEPATH);
      jj_consume_token(39);
      label_7:
      while (true) {
        cp = jj_consume_token(STRING);
        jj_consume_token(41);
                              project.addSourcePath(cp.image);
        switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
        case STRING:
          ;
          break;
        default:
          jj_la1[25] = jj_gen;
          break label_7;
        }
      }
      break;
    default:
      jj_la1[26] = jj_gen;
      ;
    }
  }

  final public void directives() throws ParseException {
                               Token directiveId; ClassParameters directiveClass = null;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case DIRECTIVE:
      jj_consume_token(DIRECTIVE);
      jj_consume_token(39);
      label_8:
      while (true) {
        directiveId = jj_consume_token(ASID);
        jj_consume_token(48);
        directiveClass = classDef();
        jj_consume_token(41);
                              project.addDirectiveClass(directiveId.image, directiveClass);
        switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
        case ASID:
          ;
          break;
        default:
          jj_la1[27] = jj_gen;
          break label_8;
        }
      }
      break;
    default:
      jj_la1[28] = jj_gen;
      ;
    }
  }

  public mas2jTokenManager token_source;
  SimpleCharStream jj_input_stream;
  public Token token, jj_nt;
  private int jj_ntk;
  private int jj_gen;
  final private int[] jj_la1 = new int[29];
  static private int[] jj_la1_0;
  static private int[] jj_la1_1;
  static {
      jj_la1_0();
      jj_la1_1();
   }
   private static void jj_la1_0() {
      jj_la1_0 = new int[] {0x1000,0x20000000,0xa0000000,0x7000800,0x7000800,0x80000000,0x0,0x60000000,0x0,0x60000000,0x78000000,0x0,0x78000000,0x78000000,0x0,0x0,0x0,0x78000000,0x205a8000,0x800,0x200,0x800,0x400,0x10000000,0x2000,0x10000000,0x4000,0x20000000,0x200000,};
   }
   private static void jj_la1_1() {
      jj_la1_1 = new int[] {0x0,0x0,0x0,0x100,0x100,0x0,0x400,0x0,0x400,0x0,0x0,0x1000,0x0,0x0,0x800,0x1000,0x4000,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,};
   }

  public mas2j(java.io.InputStream stream) {
     this(stream, null);
  }
  public mas2j(java.io.InputStream stream, String encoding) {
    try { jj_input_stream = new SimpleCharStream(stream, encoding, 1, 1); } catch(java.io.UnsupportedEncodingException e) { throw new RuntimeException(e); }
    token_source = new mas2jTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 29; i++) jj_la1[i] = -1;
  }

  public void ReInit(java.io.InputStream stream) {
     ReInit(stream, null);
  }
  public void ReInit(java.io.InputStream stream, String encoding) {
    try { jj_input_stream.ReInit(stream, encoding, 1, 1); } catch(java.io.UnsupportedEncodingException e) { throw new RuntimeException(e); }
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 29; i++) jj_la1[i] = -1;
  }

  public mas2j(java.io.Reader stream) {
    jj_input_stream = new SimpleCharStream(stream, 1, 1);
    token_source = new mas2jTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 29; i++) jj_la1[i] = -1;
  }

  public void ReInit(java.io.Reader stream) {
    jj_input_stream.ReInit(stream, 1, 1);
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 29; i++) jj_la1[i] = -1;
  }

  public mas2j(mas2jTokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 29; i++) jj_la1[i] = -1;
  }

  public void ReInit(mas2jTokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 29; i++) jj_la1[i] = -1;
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
    boolean[] la1tokens = new boolean[49];
    for (int i = 0; i < 49; i++) {
      la1tokens[i] = false;
    }
    if (jj_kind >= 0) {
      la1tokens[jj_kind] = true;
      jj_kind = -1;
    }
    for (int i = 0; i < 29; i++) {
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
    for (int i = 0; i < 49; i++) {
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
