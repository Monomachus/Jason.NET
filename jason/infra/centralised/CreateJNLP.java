package jason.infra.centralised;

import java.io.File;
import java.io.PrintWriter;

/** creates a JNLP file for a project */
public class CreateJNLP {
	public static void main(String[] args) {
		try {
			String projectName = args[0];
			//String mas2jFile   = args[1];
			String file = projectName+".jnlp";
			PrintWriter out = new PrintWriter(new File(file));
			out.println("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
			out.println("<jnlp  spec=\"1.0+\" codebase=\"http://localhost/\" href=\""+file+"\" >");
			out.println("<information>");
			out.println("  <title>Jason Application: "+projectName+"</title>");
			out.println("  <vendor>Open Source</vendor>");
			out.println("  <homepage href=\"http://jason.sf.net\"/>");
			out.println("  <description>Jason Example</description>");
			out.println("  <offline-allowed/>");
			out.println("</information>");
			out.println("<security><all-permissions /></security>");
			out.println("<resources>");
			out.println("  <j2se version=\"1.5+\" href=\"http://java.sun.com/products/autodl/j2se\"/>");
			//out.println("  <jar href=\"jason.jar\" />");
			out.println("  <jar href=\""+projectName+".jar\" />");
			out.println("</resources>");
			out.println("<application-desc main-class=\"jason.infra.centralised.RunCentralisedMAS\" >");
			//out.println("  <argument>jar:file:"+projectName+".jar!"+mas2jFile+"</argument>");
			out.println("</application-desc>");
			out.println("</jnlp>");

			out.close();
			System.out.print("File "+file+" created!");
			System.out.println(" Update the codebase (in the second line of this file) with your URL.");

		} catch (Exception e) {
			System.err.println("Error creating the jnlp file:");
			e.printStackTrace();
		}
		
	}
}
