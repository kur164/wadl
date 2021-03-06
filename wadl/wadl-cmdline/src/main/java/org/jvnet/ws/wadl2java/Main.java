/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.php
 * See the License for the specific language governing
 * permissions and limitations under the License.
 */

/*
 * Main.java
 *
 * Created on April 27, 2006, 2:42 PM
 *
 */

package org.jvnet.ws.wadl2java;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.jvnet.ws.wadl.ast.InvalidWADLException;

import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.writer.FileCodeWriter;

/**
 * Command line support for WADL to Java tool
 *
 * <p>Usage:</p>
 *
 * <pre>java ws.wadl2java.Main -p package -o directory -s jaxrs20 file.wadl</pre>
 *
 * <p>where:</p>
 * 
 * <dl>
 * <dt><code>-p package</code></dt>
 * <dd>Specifies the package used for generated code, e.g. com.example.test</dd>
 * <dt><code>-o directory</code></dt>
 * <dd>Specifies the directory to which files will be written. E.g. if the package
 * is <code>com.example.test</code> and the directory is <code>gen-src</code> then
 * files will be written to <code>./gen-src/com/example/test</code>. The directory
 * <code>dir</code> must exist, subdirectories will be created as required.</dd>
 * <dt><code>-s jaxrs20</code></dt>
 * <dd>Specifies the generation style for the code, defaults to jersey1x</dd>
 * <dt><code>file.wadl</code></dt>
 * <dd>The WADL file to process.</dd>
 * </dl>
 * @author mh124079
 */
public class Main {
    
    /**
     * Print out the usage message
     */
    protected static void printUsage() {
        System.err.println(Wadl2JavaMessages.USAGE());
    }
    
    /**
     * Entry point for the command line WADL to Java tool.
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            int i=0;
            File outputDir = null;
            String pkg = null;
            String generationStyle = Wadl2Java.STYLE_DEFAULT;
            boolean autoPackage = false;
            List<File> customizations = new ArrayList<File>();
            List<String> xjcArguments = new ArrayList<String>();
            while (i<args.length-1) {
                if (args[i].equals("-o")) {
                    outputDir = new File(args[i+1]);
                    i+=2;
                } else if (args[i].equals("-p")) {
                    pkg = args[i+1];
                    i+=2;
                } else if (args[i].equals("-s")) {
                    generationStyle = args[i+1];
                    i+=2;                    
                } else if (args[i].equals("-c")) {
                    customizations.add(new File(args[i+1]));
                    i+=2;
                } else if (args[i].equals("-a")) {
                    autoPackage = true;
                    i+=1;
                } else if (args[i].equals("-xjcArgument")) {
                    xjcArguments.add(args[i+1]);
                    i+=2;
                } else {
                    System.err.println(Wadl2JavaMessages.UNKNOWN_OPTION(args[i]));
                    printUsage();
                    return;
                }
            }
            if (i > args.length-1 || outputDir==null || pkg==null) {
                printUsage();
                return;
            }
            URI wadlDesc = new URI(args[args.length-1]);
            if (wadlDesc.getScheme()==null || wadlDesc.getScheme().equals("file")) {
                // assume a file if not told otherwise
                File wadlFile = new File(wadlDesc.getPath());
                if (!wadlFile.exists() || !wadlFile.isFile()) {
                    System.err.println(Wadl2JavaMessages.NOT_A_FILE(wadlFile.getPath()));
                    printUsage();
                    System.exit(1);
                }
                if (!outputDir.exists() || !outputDir.isDirectory()) {
                    System.err.println(Wadl2JavaMessages.NOT_A_DIRECTORY(outputDir.getPath()));
                    printUsage();
                    System.exit(1);
                }
                for (File customization: customizations) {
                    if (!customization.exists() || !customization.isFile()) {
                        System.err.println(Wadl2JavaMessages.NOT_A_FILE(customization.getPath()));
                        printUsage();
                        System.exit(1);
                    }
                }
                wadlDesc = wadlFile.toURI();
            }
            Wadl2Java w = new Wadl2Java(new Wadl2Java.Parameters()
                .setRootDir(outputDir.toURI())
                .setCodeWriter(new FileCodeWriter(outputDir))
                .setPkg(pkg)
                .setAutoPackage(autoPackage)
                .setCustomizationsAsFiles(customizations)
                .setXjcArguments(xjcArguments)
                .setGenerationStyle(generationStyle));
            
            w.process(wadlDesc);
        } catch (InvalidWADLException ex) {
            ex.printStackTrace();
        } catch (URISyntaxException ex) {
            ex.printStackTrace();
        } catch (JAXBException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (JClassAlreadyExistsException ex) {
            ex.printStackTrace();
        }
    }
    
}
