/*******************************************************************************
 * Copyright (C) 2020, Fabian Pfaff <fabian.pfaff@vogella.com>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.egit.ui.internal.commit;

import java.util.Arrays;

import com.goterl.lazycode.lazysodium.LazySodiumJava;
import com.goterl.lazycode.lazysodium.SodiumJava;
import com.goterl.lazycode.lazysodium.interfaces.Scrypt;
import com.goterl.lazycode.lazysodium.interfaces.SecretBox;
import com.goterl.lazycode.lazysodium.utils.Base64MessageEncoder;
import com.goterl.lazycode.lazysodium.utils.Key;

/**
 * Utility class that encrypts and decrypts Strings using the scrypt salsa208
 * method.
 *
 */
public class Crypto {

	private Key key;

	private SecretBox.Native secretBox;

	private static Base64MessageEncoder encoder;

	private static LazySodiumJava lazySodium;

	private static final SodiumJava sodiumJava = new SodiumJava();

	static {
		encoder = new Base64MessageEncoder();
		lazySodium = new LazySodiumJava(sodiumJava, encoder);
	}

	/**
	 * @param password
	 *            for crypto operations
	 * @param salt
	 *            for crypto operations
	 *
	 */
	public Crypto(String password, String salt) {
		secretBox = lazySodium;
		key = buildKey(password, salt, lazySodium);
	}

	private Key buildKey(String password, String salt, Scrypt.Native scryt) {
		byte[] pwHash = new byte[SecretBox.KEYBYTES];
		byte[] pass = password.getBytes();
		boolean pwHashWorked = scryt.cryptoPwHashScryptSalsa208Sha256(
				pwHash, pwHash.length,
				pass, pass.length,
				encoder.decode(salt),
				Scrypt.SCRYPTSALSA208SHA256_OPSLIMIT_INTERACTIVE,
				Scrypt.SCRYPTSALSA208SHA256_MEMLIMIT_INTERACTIVE);
		if (!pwHashWorked) {
			System.out.println("pwHash failed"); //$NON-NLS-1$
		}
		return Key.fromBytes(pwHash);
	}

	/**
	 * @param toEncrypt
	 *            String to encrypt
	 * @return encrypted message
	 */
	public String encrypt(String toEncrypt) {
		byte[] nonceBytes = lazySodium.nonce(SecretBox.NONCEBYTES);

		byte[] date = toEncrypt.getBytes();
		byte[] cipherBytes = new byte[SecretBox.MACBYTES + date.length];
		boolean encryptionWorked = secretBox.cryptoSecretBoxEasy(cipherBytes,
				date, date.length, nonceBytes, key.getAsBytes());
		if (!encryptionWorked) {
			return "ERROR"; //$NON-NLS-1$
		}
		byte[] noncePlusCipherBytes = Arrays.copyOf(nonceBytes,
				nonceBytes.length + cipherBytes.length);
		System.arraycopy(cipherBytes, 0, noncePlusCipherBytes,
				nonceBytes.length, cipherBytes.length);

		String noncePlusCypher = encoder.encode(noncePlusCipherBytes);
		return noncePlusCypher;
	}

	/**
	 * @param encrypted message
	 * @return decrypted message
	 */
	public String decrypt(String encrypted) {
		byte[] noncePlusCipherBytes = encoder.decode(encrypted);
		byte[] nonceBytes = new byte[SecretBox.NONCEBYTES];
		System.arraycopy(noncePlusCipherBytes, 0, nonceBytes, 0,
				SecretBox.NONCEBYTES);
		byte[] cipherBytes = new byte[noncePlusCipherBytes.length
				- SecretBox.NONCEBYTES];
		System.arraycopy(noncePlusCipherBytes, SecretBox.NONCEBYTES,
				cipherBytes,
				0, noncePlusCipherBytes.length - SecretBox.NONCEBYTES);
		byte[] decrypted = new byte[cipherBytes.length - SecretBox.MACBYTES];
		boolean decryptionWorked = secretBox
				.cryptoSecretBoxOpenEasy(decrypted, cipherBytes,
						cipherBytes.length, nonceBytes, key.getAsBytes());
		if (!decryptionWorked) {
			return ""; //$NON-NLS-1$
		}

		return new String(decrypted);
	}

	/**
	 * @return randomly generated salt, base64 encoded
	 */
	public static String generateSalt() {
		return encoder.encode(lazySodium.randomBytesBuf(
				Math.toIntExact(Scrypt.SCRYPTSALSA208SHA256_SALT_BYTES)));
	}

}