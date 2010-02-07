package uk.ac.warwick.dcs.boss.model.dao.beans;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * A person represents every single person in the database.  Markers, administrators, students, or clowns.
 * @author davidbyard
 *
 */
public class Person extends Entity {
	/**
	 * The email address of the person.
	 */
	private String emailAddress;
	
	/**
	 * A friendly name to call the person by.
	 */
	private String chosenName;
	
	/**
	 * SHA-256 hash of the person's password, used if the SessionFactory wants to use internal authentication.
	 */
	private String password;
	
	/**
	 * The username of the person.  May not be shared with any other person.
	 */
	private String uniqueIdentifier;
	
	/**
	 * Whether the person is an administrator or not.
	 */
	private Boolean administrator;

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress.toLowerCase();
	}

	public String getEmailAddress() {
		return emailAddress;
	}

	public void setChosenName(String chosenName) {
		this.chosenName = chosenName;
	}

	public String getChosenName() {
		return chosenName;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPassword() {
		return password;
	}

	public void setUniqueIdentifier(String uniqueIdentifier) {
		this.uniqueIdentifier = uniqueIdentifier;
	}

	public String getUniqueIdentifier() {
		return uniqueIdentifier;
	}

	public void setAdministrator(Boolean administrator) {
		this.administrator = administrator;
	}

	public Boolean isAdministrator() {
		return administrator;
	}

	/**
	 * Digest to a byte[] array and hash to readable string format.
	 * PARTIALLY OBTAINED FROM: http://www.devx.com/tips/Tip/13540
	 * TODO: Consolidate in some sort of library.
	 * @return result String buffer in String format 
	 * @param in byte[] buffer to convert to string format
	 */
	public static String passwordHash(String passwordToHash) {
		MessageDigest digest = null;
		byte hash[] = null;

		byte ch = 0x00;
		int i = 0;

		try {
			digest = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			throw new Error("Java libraries don't support SHA-256");
		}

		try {
			hash = digest.digest(passwordToHash.getBytes("UTF8"));
		} catch (UnsupportedEncodingException e) {
			throw new Error("Java libraries don't support UTF8");
		}

		if (hash == null || hash.length <= 0) {
			return null;
		}

		String pseudo[] = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
				"A", "B", "C", "D", "E", "F" };

		StringBuffer out = new StringBuffer(hash.length * 2);

		while (i < hash.length) {
			ch = (byte) (hash[i] & 0xF0); // Strip off high nibble
			ch = (byte) (ch >>> 4); // shift the bits down
			ch = (byte) (ch & 0x0F); // must do this if high order bit is on!
			out.append(pseudo[(int) ch]); // convert the nibble to a String Character

			ch = (byte) (hash[i] & 0x0F); // Strip off low nibble 
			out.append(pseudo[(int) ch]); // convert the nibble to a String Character

			i++;
		}

		String rslt = new String(out);
		return rslt;
	}
}
