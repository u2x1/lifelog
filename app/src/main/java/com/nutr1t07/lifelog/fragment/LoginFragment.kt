package com.nutr1t07.lifelog.fragment

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.nutr1t07.lifelog.R
import com.nutr1t07.lifelog.data.mHost
import com.nutr1t07.lifelog.helpers.userSession
import com.nutr1t07.lifelog.helpers.username
import kotlinx.android.synthetic.main.fragment_login.*
import org.json.JSONObject
import java.util.*

class LoginFragment : Fragment() {
    private var usernameExist: Boolean? = null
    private lateinit var queue: RequestQueue

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        queue = Volley.newRequestQueue(activity?.applicationContext)
        username_editText.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                setTextViewColor(username_info_textView, android.R.color.holo_blue_bright)
                username_info_textView.setText(R.string.checking_availability)
                usernameExist = null
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            private var timer = Timer()
            private val DELAY: Long = 1000 // milliseconds
            override fun afterTextChanged(s: Editable) {
                timer.cancel()
                timer = Timer()
                timer.schedule(
                    object : TimerTask() {
                        override fun run() {
                            val username = username_editText.text
                            val url = "${mHost}/user/exist/$username"
                            val request = StringRequest(
                                Request.Method.GET,
                                url,
                                Response.Listener { response ->
                                    if (response == "true") {
                                        setTextViewColor(
                                            username_info_textView,
                                            android.R.color.holo_green_dark
                                        )
                                        username_info_textView.setText(R.string.user_exists)
                                        login_register_btn.setText(R.string.login)
                                        usernameExist = true
                                    } else {
                                        setTextViewColor(
                                            username_info_textView,
                                            android.R.color.holo_red_dark
                                        )
                                        username_info_textView.setText(R.string.user_not_exists)
                                        login_register_btn.setText(R.string.register)
                                        usernameExist = false
                                    }
                                },
                                Response.ErrorListener {
                                    setTextViewColor(
                                        username_info_textView,
                                        android.R.color.holo_red_dark
                                    )
                                    username_info_textView.setText(R.string.something_went_wrong)
                                    Log.e("wtf", it.toString())
                                })
                            queue.add(request)
                        }
                    },
                    DELAY
                )
            }
        })

        login_register_btn.setOnClickListener {
            if (username_editText.text.isEmpty() || username_editText.text.isEmpty()) {
                Toast.makeText(
                    context,
                    R.string.username_or_passwd_empty, Toast.LENGTH_SHORT
                )
                    .show()
                return@setOnClickListener
            }
            if (usernameExist == null) {
                Toast.makeText(
                    context,
                    R.string.wait_for_availability_check, Toast.LENGTH_SHORT
                )
                    .show()
                return@setOnClickListener
            }
            val json = JSONObject()
            json.put("username", username_editText.text)
            json.put("password", passwd_editText.text)
            if (usernameExist == false) {
                AlertDialog.Builder(activity)
                    .setTitle(R.string.proceed_to_register)
                    .setPositiveButton(R.string.confirm) { _, _ ->
                        val url = "$mHost/user/register/"
                        queue.add(
                            JsonObjectRequest(Request.Method.POST, url, json,
                                Response.Listener {
                                    when (it.getInt("code")) {
                                        100 -> {
                                            Toast.makeText(
                                                context,
                                                R.string.register_success,
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            username_editText.text =
                                                username_editText.text // reset availability
                                            toggleEditTextAbility(true)
                                        }
                                        101 -> { /* user already exists: never happens */
                                        }
                                    }
                                }
                                , Response.ErrorListener { toggleEditTextAbility(true) })
                        )
                        toggleEditTextAbility(false)
                    }.create().show()
            } else if (usernameExist == true) {
                val url = "$mHost/user/login/"
                queue.add(
                    JsonObjectRequest(
                        Request.Method.POST, url, json,
                        Response.Listener {
                            when (it.getInt("code")) {
                                100 -> { // success
                                    context?.username =
                                        username_editText.text.toString()
                                    context?.userSession =
                                        it.getJSONObject("values").getString("contents")

                                    parentFragmentManager.popBackStack()
                                    Toast.makeText(
                                        context,
                                        R.string.login_success,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                102 -> {/* user not exists: never happens */
                                }
                                103 -> { // Incorrect password
                                    Toast.makeText(
                                        context,
                                        R.string.incorrect_passwd,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    toggleEditTextAbility(true)
                                }
                            }

                        },
                        Response.ErrorListener { toggleEditTextAbility(true) }
                    )
                )
                toggleEditTextAbility(false)
            }
        }
    }

    private fun setTextViewColor(textView: TextView, color: Int) {
        context?.let {
            textView.setTextColor(
                ContextCompat.getColor(
                    it,
                    color
                )
            )
        }
    }

    private fun toggleEditTextAbility(enable: Boolean) {
        if (enable) {
            username_editText.isEnabled = true
            passwd_editText.isEnabled = true
        } else {
            username_editText.isEnabled = false
            passwd_editText.isEnabled = false
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    companion object {
        const val USER_SESSION = "user_session"
        const val USER_NAME = "user_name"
        const val LAST_SYNC_TIME = "last_sync_time"
    }
}