/* 
 * Copyright (c) 2020 Razeware LLC
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
 * This project and source code may use libraries or frameworks that are
 * released under various Open-Source licenses. Use of those libraries and
 * frameworks are governed by their own individual licenses.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.raywenderlich.android.captainslog.ui.entries

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.raywenderlich.android.captainslog.R

private const val ENCRYPTED_PREFS = "ENCRYPTED_PREFS"
private const val ENCRYPTED_PREFS_LOG_KEY = "ENCRYPTED_PREFS_LOG_KEY"

class EntriesViewModel(application: Application) : AndroidViewModel(application) {

  val snackbar: LiveData<String?>
    get() = _snackbar

  private val _snackbar = MutableLiveData<String?>()

  private val context = getApplication<Application>().applicationContext

  private val sharedPreferences by lazy {
    val masterKeys = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
    val keyEncryptionScheme = EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV
    val valueEncryptionScheme = EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM

    EncryptedSharedPreferences.create(
      ENCRYPTED_PREFS,
      masterKeys,
      context,
      keyEncryptionScheme,
      valueEncryptionScheme
    )
  }

  fun onSnackbarShown() {
    _snackbar.value = null
  }

  fun showSnackbar(message: String) {
    _snackbar.value = message
  }

  fun getLogKey(): String? {
    return sharedPreferences.getString(ENCRYPTED_PREFS_LOG_KEY, null)
  }

  fun setLogKey(current: String?, new: String?) {
    if (current != getLogKey()) {
      _snackbar.value = context.getString(R.string.error_current_log_key_incorrect)
      return
    }

    if (new.isNullOrBlank()) {
      sharedPreferences.edit().putString(ENCRYPTED_PREFS_LOG_KEY, null).apply()
      _snackbar.value = context.getString(R.string.message_log_key_cleared)
    } else {
      sharedPreferences.edit().putString(ENCRYPTED_PREFS_LOG_KEY, new).apply()
      _snackbar.value = context.getString(R.string.message_log_key_set)
    }
  }
}