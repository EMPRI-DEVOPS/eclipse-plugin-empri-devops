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

import java.io.BufferedReader;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.egit.ui.Activator;
import org.eclipse.egit.ui.UIPreferences;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * Encodes and decodes the original commit date to a commit message.
 *
 */
public class OriginalCommitDateEncoder {

	private static final String PREFIX = "GitPrivacy: "; //$NON-NLS-1$

	private static final Pattern pattern = Pattern
			.compile("(?m)^" + PREFIX + "(\\S+)"); //$NON-NLS-1$ //$NON-NLS-2$

	private IPreferenceStore preferenceStore;

	private Crypto crypto;

	private SimpleDateFormat format;

	/**
	 * Initializes the crypto class for encoding and decoding. Adds a listener
	 * to password and salt so the crypto class gets rebuilt when they change.
	 */
	public OriginalCommitDateEncoder() {
		preferenceStore = Activator.getDefault().getPreferenceStore();
		crypto = new Crypto(
				preferenceStore.getString(UIPreferences.GIT_PRIVACY_PASSWORD),
				preferenceStore.getString(UIPreferences.GIT_PRIVACY_PASSWORD_SALT));
		format = new SimpleDateFormat("Z"); //$NON-NLS-1$

		preferenceStore.addPropertyChangeListener(e -> {
			if (UIPreferences.GIT_PRIVACY_PASSWORD.equals(e.getProperty())) {
				crypto = new Crypto(
						(String) e.getNewValue(),
						preferenceStore.getString(UIPreferences.GIT_PRIVACY_PASSWORD_SALT));
			} else if (UIPreferences.GIT_PRIVACY_PASSWORD_SALT.equals(e.getProperty())) {
				crypto = new Crypto(
						preferenceStore.getString(UIPreferences.GIT_PRIVACY_PASSWORD),
						(String) e.getNewValue());
			}
		});
	}

	/**
	 * @param commitMessage
	 *            message of a commit
	 * @param originalCommitDate
	 *            original commit date to be added in an encrypted form to the
	 *            commit message
	 * @return commit message with the original commit date added in an
	 *         encrypted form
	 */
	public String encode(String commitMessage,
			Date originalCommitDate) {
		if (doesntAlreadyContainDate(commitMessage)) {
			StringBuilder newCommitMessage = new StringBuilder(
					commitMessage);
			return addEncodedOriginalCommitDate(
					newCommitMessage,
					originalCommitDate).toString();
		} else {
			return commitMessage;
		}
	}

	private boolean doesntAlreadyContainDate(String commitMessage) {
		return !new BufferedReader(new StringReader(commitMessage))
				.lines()
				.anyMatch(line -> line.startsWith(PREFIX));
	}

	private StringBuilder addEncodedOriginalCommitDate(
			StringBuilder newCommitMessage,
			Date originalCommitDate) {
		String authoredDate = toRawDateFormat(originalCommitDate);
		String commitedDate = authoredDate;
		String toEncrypt = authoredDate + ";" //$NON-NLS-1$
				+ commitedDate;
		return newCommitMessage.append(System
				.lineSeparator())
				.append(System.lineSeparator())
				.append(PREFIX)
				.append(crypto.encrypt(toEncrypt));
	}

	/**
	 * @param originalCommitDate
	 * @return Seconds sine 1970 in UTC plus time zone
	 */
	private String toRawDateFormat(Date originalCommitDate) {
		String timeZone = format.format(originalCommitDate);
		String dateString = String.valueOf(originalCommitDate.getTime() / 1000)
				+ " " //$NON-NLS-1$
				+ timeZone;
		return dateString;
	}

	/**
	 * Extracts the encrypted original commit date from a commit message.
	 *
	 * @param commitMessage
	 * @return the decrypted original commit date
	 */
	public Optional<DecodedDates> decode(String commitMessage) {
		Matcher matcher = pattern.matcher(commitMessage);
		if (matcher.find()) {
			String decrypted = crypto.decrypt(matcher.group(matcher.groupCount()));
			if (decrypted.isEmpty()) {
				return Optional.empty();
			}
			return Optional.of(toDate(decrypted));
		}
		return Optional.empty();
	}

	private DecodedDates toDate(String decrypted) {
		String[] dates = decrypted.split(";"); //$NON-NLS-1$
		String[] authored = dates[0].split(" "); //$NON-NLS-1$
		String[] commited = dates[1].split(" "); //$NON-NLS-1$
		ZonedDateTime authoredDateTime = zonedDateTime(authored);
		ZonedDateTime committedDateTime = zonedDateTime(commited);
		return new DecodedDates(authoredDateTime, committedDateTime);
	}

	private ZonedDateTime zonedDateTime(String[] authored) {
		Instant instant = Instant
				.ofEpochSecond(Long.valueOf(authored[0]).longValue());
		ZoneOffset zoneOffset = ZoneOffset.of(authored[1]);
		return ZonedDateTime.ofInstant(instant, zoneOffset);
	}

	/**
	 * Holds the original authored date and committed date of a commit.
	 */
	public class DecodedDates {

		private ZonedDateTime authoredDateTime;

		private ZonedDateTime committedDateTime;

		/**
		 * @param authoredDateTime
		 * @param committedDateTime
		 */
		protected DecodedDates(
				ZonedDateTime authoredDateTime,
				ZonedDateTime committedDateTime) {
			super();
			this.authoredDateTime = authoredDateTime;
			this.committedDateTime = committedDateTime;
		}

		/**
		 * @return authored date time
		 */
		public ZonedDateTime getAuthoredDateTime() {
			return authoredDateTime;
		}

		/**
		 * @return committed
		 */
		public ZonedDateTime getCommittedDateTime() {
			return committedDateTime;
		}

	}

}
