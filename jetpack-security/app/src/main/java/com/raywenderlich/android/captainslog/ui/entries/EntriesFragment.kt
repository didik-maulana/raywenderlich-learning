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

import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.EditText
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.raywenderlich.android.captainslog.R
import com.raywenderlich.android.captainslog.model.LogEntry
import com.raywenderlich.android.captainslog.utils.DirectoryLiveData
import kotlinx.android.synthetic.main.fragment_entries.*

class EntriesFragment : Fragment(),
  EntryAdapter.EntryAdapterListener {

  private val viewModel: EntriesViewModel by viewModels()

  @RequiresApi(Build.VERSION_CODES.P)
  private val authenticationCallback = object : BiometricPrompt.AuthenticationCallback() {
    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
      super.onAuthenticationSucceeded(result)
      editLogKey()
    }

    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
      super.onAuthenticationError(errorCode, errString)
      viewModel.showSnackbar(getString(R.string.error_unable_to_authenticate_biometrically))
    }
  }

  private lateinit var promptInfo: BiometricPrompt.PromptInfo
  private lateinit var biometricPrompt: BiometricPrompt

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    setHasOptionsMenu(true)
    return inflater.inflate(R.layout.fragment_entries, container, false)
  }

  @RequiresApi(Build.VERSION_CODES.P)
  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    (activity as? AppCompatActivity)?.setSupportActionBar(toolbar)

    viewModel.snackbar.observe(viewLifecycleOwner, Observer { text ->
      text?.let {
        Snackbar.make(view, text, Snackbar.LENGTH_SHORT).show()
        viewModel.onSnackbarShown()
      }
    })

    val adapter = EntryAdapter(this)
    recyclerView.adapter = adapter

    DirectoryLiveData(requireContext().filesDir)
      .observe(viewLifecycleOwner, Observer { newList ->
      adapter.submitList(newList)
    })

    fab.setOnClickListener {
      findNavController().navigate(R.id.action_EntriesFragment_to_EditEntryFragment)
    }

    promptInfo = BiometricPrompt.PromptInfo.Builder()
      .setTitle(getString(R.string.edit_log_key_title))
      .setDescription(getString(R.string.edit_log_key_description))
      .setDeviceCredentialAllowed(true)
      .build()

    biometricPrompt = BiometricPrompt(
      this,
      ContextCompat.getMainExecutor(context),
      authenticationCallback
    )
  }

  override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
    inflater.inflate(R.menu.menu_main, menu)
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    return when (item.itemId) {
      R.id.action_key -> {
        handleLogKeyPressed()
        true
      }
      else -> super.onOptionsItemSelected(item)
    }
  }

  private fun handleLogKeyPressed() {
    when (BiometricManager.from(requireContext()).canAuthenticate()) {
      BiometricManager.BIOMETRIC_SUCCESS -> biometricPrompt.authenticate(promptInfo)
      BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE,
      BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED,
      BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> editLogKey()
    }
  }

  private fun editLogKey() {
    if (viewModel.getLogKey() == null) showSetLogKeyDialog() else showResetLogKeyDialog()
  }

  override fun onEntryClicked(entry: LogEntry) {
    if (viewModel.getLogKey() == null) {
      editEntry(entry)
    } else {
      buildLogKeyDialog {
        if (it == viewModel.getLogKey()) {
          editEntry(entry)
        } else {
          viewModel.showSnackbar(getString(R.string.error_incorrect_log_key))
        }
      }.show()
    }
  }

  private fun editEntry(entry: LogEntry) {
    findNavController().navigate(
      EntriesFragmentDirections.actionEntriesFragmentToEditEntryFragment(
        entry.stardate
      )
    )
  }

  private fun showSetLogKeyDialog() {
    buildLogKeyDialog(title = R.string.dialog_set_log_key_title) {
      viewModel.setLogKey(viewModel.getLogKey(), it)
    }.show()
  }

  private fun showResetLogKeyDialog() {
    buildResetLogKeyDialog { current, new ->
      viewModel.setLogKey(current, new)
    }.show()
  }

  private fun buildLogKeyDialog(
    @StringRes title: Int = R.string.dialog_log_key_title,
    onPositiveClicked: (value: String) -> Unit
  ): AlertDialog {
    val view = View.inflate(requireContext(), R.layout.alert_dialog_log_key_layout, null)
    val editTextView: EditText = view.findViewById(R.id.log_key_input_edit_text)

    return MaterialAlertDialogBuilder(requireContext())
      .setTitle(title)
      .setView(view)
      .setPositiveButton(R.string.dialog_log_key_positive_button) { _, _ ->
        onPositiveClicked(editTextView.text.toString())
      }
      .setNegativeButton(R.string.dialog_log_key_negative_button) { _, _ -> }
      .create()
  }

  private fun buildResetLogKeyDialog(
    @StringRes title: Int = R.string.dialog_new_log_key_title,
    onPositiveClicked: (current: String, new: String) -> Unit
  ): AlertDialog {
    val view = View.inflate(requireContext(), R.layout.alert_dialog_reset_log_key_layout, null)
    val logKeyEditTextView: EditText = view.findViewById(R.id.log_key_input_edit_text)
    val newLogKeyEditTextView: EditText = view.findViewById(R.id.new_log_key_input_edit_text)

    return MaterialAlertDialogBuilder(requireContext())
      .setTitle(title)
      .setView(view)
      .setPositiveButton(R.string.dialog_new_log_key_positive_button) { _, _ ->
        onPositiveClicked(
          logKeyEditTextView.text.toString(),
          newLogKeyEditTextView.text.toString()
        )
      }
      .setNegativeButton(R.string.dialog_new_log_key_negative_button) { _, _ -> }
      .create()
  }
}
