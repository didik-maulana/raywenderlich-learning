/*
 * Copyright (c) 2019 Razeware LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * Notwithstanding the foregoing, you may not use, copy, modify, merge, publish,
 * distribute, sublicense, create a derivative work, and/or sell copies of the
 * Software in any work that is designed, intended, or marketed for pedagogical or
 * instructional purposes related to programming, coding, application development,
 * or information technology.  Permission for such use, copying, modification,
 * merger, publication, distribution, sublicensing, creation of derivative works,
 * or sale is expressly withheld.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.raywenderlich.airlock

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.fragment_settings.*

class SettingsFragment : DialogFragment(), RadioGroup.OnCheckedChangeListener {

  private lateinit var prefs: SharedPreferences
  
  override fun onStart() {
    super.onStart()

    dialog?.window?.setLayout(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.fragment_settings, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    modeGroup.setOnCheckedChangeListener(this)

    (activity?.applicationContext as AirlockApplication).let { app ->
      prefs = app.prefs
    }

    when (prefs.getInt(Constants.MODE_KEY, 0)) {
      Mode.LIGHT.ordinal -> light.isChecked = true
      Mode.DARK.ordinal -> dark.isChecked = true
      Mode.SYSTEM.ordinal, Mode.BATTERY.ordinal -> system.isChecked = true
      else -> light.isChecked = true
    }

    if (isPreAndroid10()) {
      system.text = getString(R.string.battery_saver)
    }
  }

  override fun onCheckedChanged(rg: RadioGroup?, checkedId: Int) {
    when (checkedId) {
      R.id.light -> switchToMode(AppCompatDelegate.MODE_NIGHT_NO, Mode.LIGHT)
      R.id.dark -> switchToMode(AppCompatDelegate.MODE_NIGHT_YES, Mode.DARK)
      R.id.system -> {
        if (isPreAndroid10()) {
          switchToMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY, Mode.SYSTEM)
        } else {
          switchToMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM, Mode.SYSTEM)
        }
      }
    }
  }

  private fun switchToMode(nightMode: Int, mode: Mode) {
    AppCompatDelegate.setDefaultNightMode(nightMode)
    prefs.edit().putInt(Constants.MODE_KEY, mode.ordinal).apply()
  }

  private fun isPreAndroid10(): Boolean = Build.VERSION.SDK_INT < Build.VERSION_CODES.Q

}