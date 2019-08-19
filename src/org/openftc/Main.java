/*
 * Copyright (c) 2018 OpenFTC Team
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.openftc;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Main
{
    @Parameter(names = "-h", help = true, description = "Print help")
    private boolean help;

    @Parameter(names = {"-i", "--input"}, description = "AAR/JAR input file", required = true)
    private String inputFilepath;

    @Parameter(names = {"-s", "--sources"}, description = "Sources JAR file (optional)", required = false)
    private String sourcesFilepath;

    @Parameter(names = {"-o", "--output"}, description = "ZIP Output file", required = true)
    private String outputFilepath;

    @Parameter(names = {"-g", "--group"}, description = "Group name", required = true)
    private String groupName;

    @Parameter(names = {"-a", "--artifact"}, description = "Artifact name", required = true)
    private String artifactName;

    @Parameter(names = {"-v", "--version"}, description = "Artifact version", required = true)
    private String artifactVersion;

    private String artifactExtension;
    private String artifactPackaging;
    private static final String ARTIFACT_PACKAGING_AAR = "aar";
    private static final String ARTIFACT_PACKAGING_JAR = "jar";
    private static final String AAR_FILE_EXTENSION = ".aar";
    private static final String JAR_FILE_EXTENSION = ".jar";

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException
    {
        Main instance = new Main();

        JCommander jCommander =
                JCommander.newBuilder()
                        .addObject(instance)
                        .programName("java -jar AAR_Repackager.jar")
                        .build();

        try
        {
            jCommander.parse(args);

            if (instance.help)
            {
                System.out.println("AAR/JAR Repackager v1.0");
                jCommander.usage();
            }
            else
            {
                instance.run();
            }
        }
        catch (ParameterException e)
        {
            System.err.println(e.getMessage() + "\nRun with -h for usage details");
        }
    }


    private void run() throws IOException, NoSuchAlgorithmException
    {
        if(inputFilepath.endsWith(AAR_FILE_EXTENSION))
        {
            artifactExtension = AAR_FILE_EXTENSION;
            artifactPackaging = ARTIFACT_PACKAGING_AAR;
        }
        else if(inputFilepath.endsWith(JAR_FILE_EXTENSION))
        {
            artifactExtension = JAR_FILE_EXTENSION;
            artifactPackaging = ARTIFACT_PACKAGING_JAR;
        }
        else
        {
            throw new RuntimeException("Was not given JAR or AAR as input");
        }

        String[] groupNames = groupName.split("\\.");

        StringBuilder stringBuilder = new StringBuilder();

        /*
         * Trim off the extension because it will be added
         * when we ZIP the directory
         */
        if(outputFilepath.endsWith(".zip"))
        {
            outputFilepath = outputFilepath.substring(0, outputFilepath.lastIndexOf('.'));
        }

        stringBuilder.append(outputFilepath);

        for (String s : groupNames)
        {
            stringBuilder.append(File.separator).append(s).append(File.separator);
        }

        stringBuilder.append(artifactName);

        File folderToPutMetadataIn = new File(stringBuilder.toString());

        stringBuilder.append(File.separator).append(artifactVersion).append(File.separator);

        File folderToPutArtifactIn = new File(stringBuilder.toString());

        folderToPutArtifactIn.mkdirs();

        String nameOfAarArtifact = artifactName + "-" + artifactVersion + artifactExtension;
        String fullPathToArtifact = folderToPutArtifactIn.getAbsolutePath() + File.separator + nameOfAarArtifact;

        /*
         * Copy the artifact into the correct path
         */
        Files.copy(Paths.get(inputFilepath), Paths.get(fullPathToArtifact), StandardCopyOption.REPLACE_EXISTING);

        /*
         * Add the checksum files for the artifact
         */
        String artifact_md5 = Checksum.calculateMD5(new File(fullPathToArtifact));
        String artifact_sha1 = Checksum.calculateSHA1(new File(fullPathToArtifact));
        FileUtil.dumpRawAsciiToDisk(artifact_md5, new File(fullPathToArtifact + ".md5"));
        FileUtil.dumpRawAsciiToDisk(artifact_sha1, new File(fullPathToArtifact + ".sha1"));

        /*
         * Copy the POM file to the artifact folder
         */
        String nameOfPom = artifactName + "-" + artifactVersion + ".pom";
        Path pathToPom = Paths.get(folderToPutArtifactIn.getAbsolutePath() + File.separator + nameOfPom);
        Files.copy(getClass().getResourceAsStream("/artifact-pom.pom"), pathToPom, StandardCopyOption.REPLACE_EXISTING);

        /*
         * Do a find and replace for the needed items in the POM
         */
        Charset charset = StandardCharsets.UTF_8;
        String pomContent = new String(Files.readAllBytes(pathToPom), charset);
        pomContent = pomContent
                .replaceAll("GROUP_ID_HERE", groupName)
                .replaceAll("ARTIFACT_ID_HERE", artifactName)
                .replaceAll("ARTIFACT_VERSION_HERE", artifactVersion)
                .replaceAll("ARTIFACT_EXTENSION_HERE", artifactPackaging);
        Files.write(pathToPom, pomContent.getBytes(charset));

        /*
         * Add checksum files for the POM
         */
        makeChecksumsForFile(pathToPom.toAbsolutePath().toString());

        /*
         * Copy the metadata file to the correct folder
         */
        Path pathToMetaData = Paths.get(folderToPutMetadataIn.getAbsolutePath() + File.separator + "maven-metadata.xml");
        Files.copy(getClass().getResourceAsStream("/maven-metadata.xml"), pathToMetaData, StandardCopyOption.REPLACE_EXISTING);

        /*
         * Do a find and replace for the needed items in the metadata
         */
        Charset charset2 = StandardCharsets.UTF_8;
        String metadataContent = new String(Files.readAllBytes(pathToMetaData), charset2);
        metadataContent = metadataContent
                .replaceAll("GROUP_ID_HERE", groupName)
                .replaceAll("ARTIFACT_ID_HERE", artifactName)
                .replaceAll("ARTIFACT_VERSION_HERE", artifactVersion)
                .replaceAll("ARTIFACT_DATE_HERE", new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
        Files.write(pathToMetaData, metadataContent.getBytes(charset2));

        /*
         * Add checksum files for metadata
         */
        makeChecksumsForFile(pathToMetaData.toAbsolutePath().toString());

        /*
         * Do we need to process a sources JAR too?
         */
        if (sourcesFilepath != null)
        {
            /*
             * Ok so looks like we do have a source archive
             * First, copy it into the correct folder
             */
            String nameOfSourcesArchive = artifactName + "-" + artifactVersion + "-sources.jar";
            String fullPathToSourcesArchive = folderToPutArtifactIn.getAbsolutePath() + File.separator + nameOfSourcesArchive;
            Files.copy(Paths.get(sourcesFilepath), Paths.get(fullPathToSourcesArchive), StandardCopyOption.REPLACE_EXISTING);

            /*
             * Add checksum files for sources JAR
             */
            makeChecksumsForFile(fullPathToSourcesArchive);
        }

        /*
         * Alright we're all done, ZIP it up!
         */
        FileUtil.zipDir(outputFilepath);

        /*
         * A little clean up before we get out of dodge
         */
        FileUtil.deleteFolder(new File(outputFilepath));
    }

    private void makeChecksumsForFile(String path) throws IOException, NoSuchAlgorithmException
    {
        String md5 = Checksum.calculateMD5(new File(path));
        String sha1 = Checksum.calculateSHA1(new File(path));
        FileUtil.dumpRawAsciiToDisk(md5, new File(path + ".md5"));
        FileUtil.dumpRawAsciiToDisk(sha1, new File(path + ".sha1"));
    }
}
