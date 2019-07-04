/*
 * Original work copyright (C) 2012 The CyanogenMod Project
 * Derived work copyright (C) 2019 OpenFTC Team
 *
 * * Licensed under the GNU GPLv2 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl-2.0.txt
 */

package org.openftc;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

class Checksum
{
    static String calculateMD5(File file) throws NoSuchAlgorithmException, IOException
    {
        MessageDigest digest = MessageDigest.getInstance("MD5");
        InputStream is = new FileInputStream(file);

        byte[] buffer = new byte[8192];
        int read;

        while ((read = is.read(buffer)) > 0)
        {
            digest.update(buffer, 0, read);
        }

        is.close();

        byte[] md5sum = digest.digest();
        BigInteger bigInt = new BigInteger(1, md5sum);
        String output = bigInt.toString(16);
        // Fill to 32 chars
        output = String.format("%32s", output).replace(' ', '0');
        return output;
    }

    static String calculateSHA1(File file) throws NoSuchAlgorithmException, IOException
    {
        MessageDigest digest = MessageDigest.getInstance("SHA1");
        InputStream is = new FileInputStream(file);

        byte[] buffer = new byte[8192];
        int read;

        while ((read = is.read(buffer)) > 0)
        {
            digest.update(buffer, 0, read);
        }

        is.close();

        byte[] md5sum = digest.digest();
        BigInteger bigInt = new BigInteger(1, md5sum);
        String output = bigInt.toString(16);
        // Fill to 32 chars
        output = String.format("%32s", output).replace(' ', '0');
        return output;
    }
}