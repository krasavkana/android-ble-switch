package com.krasavkana.android.bleswitch;

import android.os.Bundle;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

public class SettingsFragment extends PreferenceFragmentCompat {

    public static SettingsFragment newInstance(String rootKey) {
        SettingsFragment fragment = new SettingsFragment();
        Bundle bundle = new Bundle();
        // 第1引数をPreferenceFragmentCompat.ARG_PREFERENCE_ROOTとすることでonCreatePreferencesの第2引数がここでputしたrootKeyになります
        bundle.putString(PreferenceFragmentCompat.ARG_PREFERENCE_ROOT, rootKey);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        // クリックされたPreferenceScreen毎にPreferenceのカスタマイズなど
        switch (rootKey) {
            case "preference_appearance":
                onCreateAppearancePreferences();
                break;
            case "preference_others":
                break;
        }
    }

    private void onCreateAppearancePreferences() {
        // テーマ設定の現在の値をSummaryに表示
        final ListPreference themePreference = (ListPreference) findPreference("preference_theme");
        themePreference.setSummary(themePreference.getEntry());
        themePreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                int indexOfValue = themePreference.findIndexOfValue(String.valueOf(newValue));
                themePreference.setSummary(indexOfValue >= 0 ? themePreference.getEntries()[indexOfValue] : null);
                return true;
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // ActionBarのタイトルに現在表示中のPreferenceScreenのタイトルをセット
        String rootKey = getArguments().getString(PreferenceFragmentCompat.ARG_PREFERENCE_ROOT);
        getActivity().setTitle(findPreference(rootKey).getTitle());
    }

}