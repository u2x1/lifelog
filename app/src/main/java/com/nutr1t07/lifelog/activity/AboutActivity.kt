package com.nutr1t07.lifelog.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import com.nutr1t07.lifelog.R
import kotlinx.android.synthetic.main.activity_about.*

class AboutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        about_abstract.text = HtmlCompat.fromHtml(
            getString(R.string.about_abstract),
            HtmlCompat.FROM_HTML_MODE_COMPACT
        )
        about_quote.text = HtmlCompat.fromHtml(
            getString(R.string.about_quote),
            HtmlCompat.FROM_HTML_MODE_COMPACT
        )
        about_build_info.text = getString(
            R.string.build_info,
            packageManager.getPackageInfo(packageName, 0).versionName
        )
    }
}