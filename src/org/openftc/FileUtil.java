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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

class FileUtil
{
    static void dumpRawAsciiToDisk(String ascii, File out) throws IOException
    {
        FileOutputStream fileOutputStream = new FileOutputStream(out.getAbsolutePath());
        fileOutputStream.write(ascii.getBytes(Charset.forName("ASCII")));
        fileOutputStream.flush();
        fileOutputStream.close();
    }

    static void zipDir(String dirPath) throws IOException
    {
        Path sourceDir = Paths.get(dirPath);
        String zipFileName = dirPath.concat(".zip");

        ZipOutputStream outputStream = new ZipOutputStream(new FileOutputStream(zipFileName));
        Files.walkFileTree(sourceDir, new SimpleFileVisitor<Path>()
        {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) throws IOException
            {
                Path targetFile = sourceDir.relativize(file);
                outputStream.putNextEntry(new ZipEntry(targetFile.toString()));
                byte[] bytes = Files.readAllBytes(file);
                outputStream.write(bytes, 0, bytes.length);
                outputStream.closeEntry();
                return FileVisitResult.CONTINUE;
            }
        });
        outputStream.close();
    }

    static void deleteFolder(File folder)
    {
        File[] files = folder.listFiles();

        if (files != null)
        {
            for (File f : files)
            {
                if (f.isDirectory())
                {
                    deleteFolder(f);
                }
                else
                {
                    f.delete();
                }
            }
        }

        folder.delete();
    }
}
