package com.nutr1t07.lifelog.activity

import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.nutr1t07.lifelog.R
import com.nutr1t07.lifelog.data.syncEvents
import com.nutr1t07.lifelog.helpers.*
import java.util.*

private const val TITLE_TAG = "settingsActivityTitle"

class SettingsActivity : AppCompatActivity(),
    PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(
                    R.id.settings,
                    HeaderFragment()
                )
                .commit()
        } else {
            title = savedInstanceState.getCharSequence(TITLE_TAG)
        }
        supportFragmentManager.addOnBackStackChangedListener {
            if (supportFragmentManager.backStackEntryCount == 0) {
                setTitle(R.string.title_activity_settings)
            }
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Save current activity title so we can set it again after a configuration change
        outState.putCharSequence(TITLE_TAG, title)
    }

    override fun onSupportNavigateUp(): Boolean {
        if (supportFragmentManager.popBackStackImmediate()) {
            return true
        }
        return super.onSupportNavigateUp()
    }

    override fun onPreferenceStartFragment(
        caller: PreferenceFragmentCompat,
        pref: Preference
    ): Boolean {
        // Instantiate the new Fragment
        val args = pref.extras
        val fragment = supportFragmentManager.fragmentFactory.instantiate(
            classLoader,
            pref.fragment
        ).apply {
            arguments = args
            setTargetFragment(caller, 0)
        }
        // Replace the existing Fragment with the new Fragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.settings, fragment)
            .addToBackStack(null)
            .commit()
        title = pref.title
        return true
    }

    class HeaderFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.header_preferences, rootKey)

        }
    }


    class SyncFragment : PreferenceFragmentCompat() {
        override fun onResume() {
            super.onResume()
            refreshUI()
            activity?.title = getString(R.string.sync_header)
        }

        private fun refreshUI() {
            val loginBtn: Preference? = findPreference("pref_login_btn")
            val infoTextView: Preference? = findPreference("pref_info")
            val logoutBtn: Preference? = findPreference("pref_logout_btn")
            val syncBtn: Preference? = findPreference("pref_sync_btn")
            if (context?.userSession != null) {
                syncBtn?.isEnabled = true
                loginBtn?.isVisible = false
                logoutBtn?.isVisible = true
                infoTextView?.let {
                    val username = context?.username ?: "null"
                    val lastSyncTime =
                        context?.lastSyncTime?.let { time ->
                            if (time == 0L) getString(R.string.never) else getyyyyMMddHHmmFromDate(
                                Date(time)
                            )
                        }
                    it.summary = getString(
                        R.string.user_info_text,
                        username,
                        lastSyncTime
                    )
                    it.isVisible = true
                }
            } else {
                syncBtn?.isEnabled = false
                loginBtn?.isVisible = true
                logoutBtn?.isVisible = false
                infoTextView?.isVisible = false
            }
        }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            refreshUI()
            findPreference<Preference>("pref_logout_btn")?.setOnPreferenceClickListener {
                context?.logoutUser()
                findPreference<Preference>("pref_login_btn")?.isVisible = true
                findPreference<Preference>("pref_logout_btn")?.isVisible = false
                findPreference<Preference>("pref_info")?.isVisible = false
                true
            }
            findPreference<Preference>("pref_sync_btn")?.setOnPreferenceClickListener {
                Toast.makeText(
                    context,
                    R.string.sync_start,
                    Toast.LENGTH_SHORT
                ).show()
                syncEvents(activity?.applicationContext!!) { refreshUI() }
                true
            }
        }


        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.sync_preferences, rootKey)
        }
    }
}