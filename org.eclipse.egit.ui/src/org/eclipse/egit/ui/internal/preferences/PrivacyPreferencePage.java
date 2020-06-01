/*******************************************************************************
 * Copyright (C) 3030 Fabian Pfaff <fabian.pfaff@vogella.com>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.egit.ui.internal.preferences;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.egit.ui.Activator;
import org.eclipse.egit.ui.UIPreferences;
import org.eclipse.egit.ui.UIUtils;
import org.eclipse.egit.ui.internal.UIText;
import org.eclipse.egit.ui.internal.commit.Crypto;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.Policy;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

/** Preferences for committing with enhanced privacy. */
public class PrivacyPreferencePage extends DoublePreferencesPreferencePage
		implements IWorkbenchPreferencePage {

	private BooleanFieldEditor modifyCommitDateEditor;

	private BooleanFieldEditor modifyCommitMonthEditor;

	private BooleanFieldEditor modifyCommitDayEditor;

	private BooleanFieldEditor modifyCommitHourEditor;

	private BooleanFieldEditor modifyCommitMinuteEditor;

	private BooleanFieldEditor modifyCommitSecondEditor;

	private BooleanFieldEditor limitCommitTimeEditor;

	private IntegerFieldEditor lowerLimitEditor;

	private IntegerFieldEditor upperLimitEditor;

	private BooleanFieldEditor saveOriginalCommitDateEditor;

	private StringFieldEditor passwordEditor;

	private StringFieldEditor saltEditor;

	private Group redactingGroup;

	private Group encryptionGroup;

	private Group decryptionGroup;

	/** */
	public PrivacyPreferencePage() {
		super(GRID);
		setTitle("Privacy"); //$NON-NLS-1$
	}

	@Override
	public void init(IWorkbench workbench) {
		// Nothing to do
	}

	@Override
	protected IPreferenceStore doGetPreferenceStore() {
		return Activator.getDefault().getPreferenceStore();
	}

	@Override
	protected IPreferenceStore doGetSecondaryPreferenceStore() {
		return new ScopedPreferenceStore(InstanceScope.INSTANCE,
				org.eclipse.egit.core.Activator.getPluginId());
	}

	@Override
	protected void createFieldEditors() {
		redactingGroup = new Group(getFieldEditorParent(),
				SWT.SHADOW_ETCHED_IN);
		redactingGroup.setText(
				UIText.PrivacyPreferencePage_git_privacy_redating_group);
		GridDataFactory.fillDefaults().grab(true, false).span(3, 1)
				.applyTo(redactingGroup);

		modifyCommitDateEditor = new BooleanFieldEditor(
				UIPreferences.MODIFY_COMMIT_DATE,
				UIText.PrivacyPreferencePage_modify_commit_date, SWT.NONE,
				redactingGroup);
		addField(modifyCommitDateEditor);

		modifyCommitMonthEditor = new BooleanFieldEditor(
				UIPreferences.MODIFY_COMMIT_MONTH,
				UIText.PrivacyPreferencePage_modify_commit_month,
				SWT.NONE,
				redactingGroup);
		GridDataFactory.fillDefaults().indent(UIUtils.getControlIndent(), 0)
				.applyTo(modifyCommitMonthEditor
						.getDescriptionControl(redactingGroup));
		addField(modifyCommitMonthEditor);
		modifyCommitMonthEditor
				.setEnabled(
				getPreferenceStore()
						.getBoolean(UIPreferences.MODIFY_COMMIT_DATE),
						redactingGroup);

		modifyCommitDayEditor = new BooleanFieldEditor(
				UIPreferences.MODIFY_COMMIT_DAY,
				UIText.PrivacyPreferencePage_modify_commit_day,
				SWT.NONE,
				redactingGroup);
		GridDataFactory.fillDefaults().indent(UIUtils.getControlIndent(), 0)
				.applyTo(modifyCommitDayEditor
						.getDescriptionControl(redactingGroup));
		addField(modifyCommitDayEditor);
		modifyCommitDayEditor.setEnabled(
				getPreferenceStore()
						.getBoolean(UIPreferences.MODIFY_COMMIT_DATE),
				redactingGroup);

		modifyCommitHourEditor = new BooleanFieldEditor(
				UIPreferences.MODIFY_COMMIT_HOUR,
				UIText.PrivacyPreferencePage_modify_commit_hour,
				SWT.NONE,
				redactingGroup);
		GridDataFactory.fillDefaults().indent(UIUtils.getControlIndent(), 0)
				.applyTo(modifyCommitHourEditor
						.getDescriptionControl(redactingGroup));
		addField(modifyCommitHourEditor);
		modifyCommitHourEditor.setEnabled(
				getPreferenceStore()
						.getBoolean(UIPreferences.MODIFY_COMMIT_DATE),
				redactingGroup);

		modifyCommitMinuteEditor = new BooleanFieldEditor(
				UIPreferences.MODIFY_COMMIT_MINUTE,
				UIText.PrivacyPreferencePage_modify_commit_minute,
				SWT.NONE,
				redactingGroup);
		GridDataFactory.fillDefaults().indent(UIUtils.getControlIndent(), 0)
				.applyTo(modifyCommitMinuteEditor
						.getDescriptionControl(redactingGroup));
		addField(modifyCommitMinuteEditor);
		modifyCommitMinuteEditor.setEnabled(
				getPreferenceStore()
						.getBoolean(UIPreferences.MODIFY_COMMIT_DATE),
				redactingGroup);

		modifyCommitSecondEditor = new BooleanFieldEditor(
				UIPreferences.MODIFY_COMMIT_SECOND,
				UIText.PrivacyPreferencePage_modify_commit_second,
				SWT.NONE,
				redactingGroup);
		GridDataFactory.fillDefaults().indent(UIUtils.getControlIndent(), 0)
				.applyTo(modifyCommitSecondEditor
						.getDescriptionControl(redactingGroup));
		addField(modifyCommitSecondEditor);
		modifyCommitSecondEditor.setEnabled(
				getPreferenceStore()
						.getBoolean(UIPreferences.MODIFY_COMMIT_DATE),
				redactingGroup);


		limitCommitTimeEditor = new BooleanFieldEditor(
				UIPreferences.LIMIT_COMMIT_TIME,
				UIText.PrivacyPreferencePage_limit_commit_time,
				redactingGroup);
		addField(limitCommitTimeEditor);

		lowerLimitEditor = new IntegerFieldEditor(
				UIPreferences.LOWER_COMMIT_TIME_LIMIT,
				UIText.PrivacyPreferencePage_lower_commit_time_limit,
				redactingGroup);
		GridDataFactory.fillDefaults().indent(UIUtils.getControlIndent(), 0)
				.applyTo(lowerLimitEditor
						.getLabelControl(redactingGroup));
		addField(lowerLimitEditor);
		lowerLimitEditor.setEnabled(
				getPreferenceStore()
						.getBoolean(UIPreferences.LIMIT_COMMIT_TIME),
				redactingGroup);


		upperLimitEditor = new IntegerFieldEditor(
				UIPreferences.UPPER_COMMIT_TIME_LIMIT,
				UIText.PrivacyPreferencePage_upper_commit_time_limit,
				redactingGroup);
		GridDataFactory.fillDefaults().indent(UIUtils.getControlIndent(), 0)
				.applyTo(upperLimitEditor
						.getLabelControl(redactingGroup));
		addField(upperLimitEditor);
		upperLimitEditor.setEnabled(
				getPreferenceStore()
						.getBoolean(UIPreferences.LIMIT_COMMIT_TIME),
				redactingGroup);

		updateMargins(redactingGroup);

		encryptionGroup = new Group(getFieldEditorParent(),
				SWT.SHADOW_ETCHED_IN);
		encryptionGroup.setText(
				UIText.PrivacyPreferencePage_git_privacy_encryption_group);
		GridDataFactory.fillDefaults().grab(true, false).span(3, 1)
				.applyTo(encryptionGroup);

		saveOriginalCommitDateEditor = new BooleanFieldEditor(
				UIPreferences.SAVE_ORIGINAL_COMMIT_DATE,
				UIText.PrivacyPreferencePage_save_original_commit_date,
				encryptionGroup);
		addField(saveOriginalCommitDateEditor);

		// TODO(FAP): how to stop "restore to defaults" from deleting password?
		passwordEditor = new StringFieldEditor(
				UIPreferences.GIT_PRIVACY_PASSWORD,
				UIText.PrivacyPreferencePage_git_privacy_password,
				encryptionGroup) {

			@Override
			protected void doFillIntoGrid(Composite parent, int numColumns) {
				super.doFillIntoGrid(parent, numColumns);

				getTextControl().setEchoChar('*');
			}
		};
		addField(passwordEditor);

		saltEditor = new StringFieldEditor(
				UIPreferences.GIT_PRIVACY_PASSWORD_SALT,
				UIText.PrivacyPreferencePage_git_privacy_password_salt,
				encryptionGroup);
		addField(saltEditor);

		updateMargins(encryptionGroup);

		decryptionGroup = new Group(getFieldEditorParent(),
				SWT.SHADOW_ETCHED_IN);
		decryptionGroup.setText(
				UIText.PrivacyPreferencePage_git_privacy_decryption_group);
		GridDataFactory.fillDefaults().grab(true, false).span(3, 1)
				.applyTo(decryptionGroup);
		BooleanFieldEditor showOriginalCommitDate = new BooleanFieldEditor(
				UIPreferences.GIT_PRIVACY_HISTORY_COLUMN_ORIGINAL_DATE,
				UIText.PrivacyPreferencePage_show_original_commit_date_in_history_view,
				decryptionGroup);
		addField(showOriginalCommitDate);

		BooleanFieldEditor showOriginalCommitDateInCommitMessage = new BooleanFieldEditor(
				UIPreferences.GIT_PRIVACY_COMMIT_MESSAGE_ORIGINAL_DATE,
				UIText.PrivacyPreferencePage_show_original_commit_date_in_commit_message,
				decryptionGroup);
		addField(showOriginalCommitDateInCommitMessage);

		updateMargins(decryptionGroup);
	}

	@Override
	protected void initialize() {
		super.initialize();
		modifyCommitDateEditor.setPropertyChangeListener(event -> {
			if (FieldEditor.VALUE.equals(event.getProperty())) {
				boolean newValue = ((Boolean) event.getNewValue())
						.booleanValue();
				modifyCommitMonthEditor.setEnabled(
						newValue,
						redactingGroup);
				modifyCommitDayEditor.setEnabled(
						newValue,
						redactingGroup);
				modifyCommitHourEditor.setEnabled(
						newValue,
						redactingGroup);
				modifyCommitMinuteEditor.setEnabled(
						newValue,
						redactingGroup);
				modifyCommitSecondEditor.setEnabled(
						newValue,
						redactingGroup);
			}
		});

		limitCommitTimeEditor.setPropertyChangeListener(event -> {
			if (FieldEditor.VALUE.equals(event.getProperty())) {
				boolean newValue = ((Boolean) event.getNewValue())
						.booleanValue();
				lowerLimitEditor.setEnabled(newValue, redactingGroup);
				upperLimitEditor.setEnabled(newValue, redactingGroup);
			}
		});

		doGetPreferenceStore().addPropertyChangeListener(event -> {
			final String prop = event.getProperty();
			if (UIPreferences.GIT_PRIVACY_PASSWORD.equals(prop)) {
				String salt = Crypto.generateSalt();
				// saltEditor.setStringValue(salt);
				doGetPreferenceStore().setValue(
						UIPreferences.GIT_PRIVACY_PASSWORD_SALT,
						salt);
				saltEditor.load();
			}
		});

	}

	@Override
	protected void performDefaults() {
		super.performDefaults();
	}

	@Override
	public boolean performOk() {
		if (lowerLimitEditor.getIntValue() > upperLimitEditor.getIntValue()) {
			String message = JFaceResources.format(
					"PreferenceDialog.saveErrorMessage", //$NON-NLS-1$
					new Object[] { getTitle(),
							UIText.PrivacyPreferencePage_limit_commit_time_error_message });
			Policy.getStatusHandler().show(
					new Status(IStatus.ERROR, Activator.getPluginId(), message),
					JFaceResources
							.getString("PreferenceDialog.saveErrorTitle")); //$NON-NLS-1$
			return false;
		}
		if (saveOriginalCommitDateEditor.getBooleanValue()
				&& passwordEditor.getStringValue().isEmpty()) {
			String message = JFaceResources.format(
					"PreferenceDialog.saveErrorMessage", //$NON-NLS-1$
					new Object[] { getTitle(),
							UIText.PrivacyPreferencePage_password_empty_error_message });
			Policy.getStatusHandler().show(
					new Status(IStatus.ERROR, Activator.getPluginId(), message),
					JFaceResources
							.getString("PreferenceDialog.saveErrorTitle")); //$NON-NLS-1$
			return false;
		}

		return super.performOk();
	}

	private void updateMargins(Group group) {
		// make sure there is some room between the group border
		// and the controls in the group
		GridLayout layout = (GridLayout) group.getLayout();
		layout.marginWidth = 5;
		layout.marginHeight = 5;
	}

}
