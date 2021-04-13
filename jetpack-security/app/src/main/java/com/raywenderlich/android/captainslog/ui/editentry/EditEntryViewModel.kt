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

package com.raywenderlich.android.captainslog.ui.editentry

import android.app.Application
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKeys
import com.raywenderlich.android.captainslog.R
import com.raywenderlich.android.captainslog.utils.urlEncode
import java.io.File
import java.lang.Exception

class EditEntryViewModel(application: Application) : AndroidViewModel(application) {

  val snackbar: LiveData<String?>
    get() = _snackbar

  private val _snackbar = MutableLiveData<String?>()

  private val context = getApplication<Application>().applicationContext

  fun onSnackbarShown() {
    _snackbar.value = null
  }

  fun encryptEntry(stardate: String, body:String, existingName: String) {
    if (stardate.isBlank()) return

    try {
      deleteEntry(existingName)
      val encryptedFile = getEncryptedEntry(stardate)
      encryptedFile.openFileOutput().use { output ->
        output.write(body.toByteArray())
      }
    } catch (e: Exception) {
      e.printStackTrace()
      _snackbar.value = context.getString(R.string.error_unable_to_save_entry)
    }
  }

  fun decryptEntry(stardate: String): String {
    val encryptedFile = getEncryptedEntry(stardate)
    try {
      encryptedFile.openFileInput().use { input ->
        return String(input.readBytes(), Charsets.UTF_8)
      }
    } catch (e: Exception) {
      e.printStackTrace()
      _snackbar.value = context.getString(R.string.error_unable_to_decrypt)
      return ""
    }
  }

  fun deleteEntry(stardate: String) {
    if (stardate.isBlank()) return
    val file = File(context.filesDir, stardate.urlEncode())
    if (file.exists()) file.delete()
  }

  private fun getEncryptedEntry(name: String): EncryptedFile {
    val advanceSpec = KeyGenParameterSpec.Builder(
      "master_key",
      KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
    ).apply {
      setBlockModes(KeyProperties.BLOCK_MODE_GCM)
      setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
      setKeySize(256)
//      setUserAuthenticationRequired(true)
//      setUserAuthenticationValidityDurationSeconds(15)

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        setUnlockedDeviceRequired(true)
        setIsStrongBoxBacked(true)
      }
    }.build()

    //val keyGenParameterSpec = MasterKeys.AES256_GCM_SPEC
    val masterKeyAlias = MasterKeys.getOrCreate(advanceSpec)
    val fileEncryptionScheme = EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB

    return EncryptedFile.Builder(
      File(context.filesDir, name.urlEncode()),
      context,
      masterKeyAlias,
      fileEncryptionScheme
    ).build()
  }
}