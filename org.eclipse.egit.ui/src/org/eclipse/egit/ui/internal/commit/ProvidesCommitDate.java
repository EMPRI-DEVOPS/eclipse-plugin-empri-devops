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

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import org.eclipse.egit.ui.Activator;
import org.eclipse.egit.ui.UIPreferences;
import org.eclipse.egit.ui.internal.preferences.PrivacyPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * Provides the commit date for a commit.
 *
 */
public class ProvidesCommitDate {

	private IPreferenceStore preferenceStore;

	/**
	 * Default constructor
	 */
	public ProvidesCommitDate() {
		preferenceStore = Activator.getDefault().getPreferenceStore();
	}

	/**
	 * @return Dates to be used for a commit. Potentially provides a redated
	 *         commit date according to the preferences from
	 *         {@link PrivacyPreferencePage}. If the commit date gets redated,
	 *         the {@link CommitDateResult} will contain the original date so it
	 *         can be saved in an encrypted form.
	 */
	public CommitDateResult commitDate() {
		LocalDateTime commitDate = LocalDateTime.now();
		LocalDateTime originalCommitDate = commitDate;
		int year = commitDate.getYear();
		int month = commitDate.getMonthValue();
		int dayOfMonth = commitDate.getDayOfMonth();
		int hour = commitDate.getHour();
		int minute = commitDate.getMinute();
		int second = commitDate.getSecond();

		if (preferenceStore.getBoolean(UIPreferences.MODIFY_COMMIT_DATE)) {

			if (preferenceStore.getBoolean(UIPreferences.MODIFY_COMMIT_MONTH)) {
				month = 1;
			}
			if (preferenceStore.getBoolean(UIPreferences.MODIFY_COMMIT_DAY)) {
				dayOfMonth = 1;
			}
			if (preferenceStore.getBoolean(UIPreferences.MODIFY_COMMIT_HOUR)) {
				hour = 0;
			}
			if (preferenceStore.getBoolean(UIPreferences.MODIFY_COMMIT_MINUTE)) {
				minute = 0;
			}
			if (preferenceStore.getBoolean(UIPreferences.MODIFY_COMMIT_SECOND)) {
				second = 0;
			}

			commitDate = LocalDateTime.of(year, month, dayOfMonth, hour, minute,
					second);
		}

		if (preferenceStore.getBoolean(UIPreferences.LIMIT_COMMIT_TIME)) {
			int lowerLimit = preferenceStore.getInt(UIPreferences.LOWER_COMMIT_TIME_LIMIT);
			LocalDateTime lowerLimitDate = LocalDateTime.of(year, month, dayOfMonth, lowerLimit, 0,
					0);
			int upperLimit = preferenceStore.getInt(UIPreferences.UPPER_COMMIT_TIME_LIMIT);
			LocalDateTime upperLimitDate = LocalDateTime.of(year, month, dayOfMonth, upperLimit, 0,
					0);
			if (commitDate.isBefore(lowerLimitDate)) {
				commitDate = lowerLimitDate;
			} else if (commitDate.isAfter(upperLimitDate)) {
				commitDate = upperLimitDate;
			}
		}

		return new CommitDateResult(Date.from(originalCommitDate.atZone(ZoneId.systemDefault()).toInstant()),
				Date.from(commitDate.atZone(ZoneId.systemDefault()).toInstant()));
	}

	/**
	 * Value class that holds the commit date for a commit. If the privacy
	 * plugin changes the commit date it also holds the original commit date.
	 *
	 */
	public class CommitDateResult {

		private final Date originalCommitDate;

		private final Date commitDate;

		/**
		 * @param originalCommitDate
		 *            original commit date, will be equal to the commit date, if
		 *            the commit date hasn't been redated
		 * @param redactedCommitDate
		 *            commit date, possibly redated by the privacy plugin
		 */
		private CommitDateResult(
				Date originalCommitDate,
				Date redactedCommitDate) {
			this.originalCommitDate = originalCommitDate;
			this.commitDate = redactedCommitDate;
		}

		/**
		 * @return original commit date, will be equal to the commit date, if it
		 *         hasn't been redated
		 */
		public Date getOriginalCommitDate() {
			return originalCommitDate;
		}

		/**
		 * @return commit date, possibly redated by the privacy plugin
		 */
		public Date getCommitDate() {
			return commitDate;
		}

	}

}
