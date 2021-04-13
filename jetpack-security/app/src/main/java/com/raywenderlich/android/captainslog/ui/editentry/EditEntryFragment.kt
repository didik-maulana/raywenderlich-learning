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

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar
import com.raywenderlich.android.captainslog.R
import com.raywenderlich.android.captainslog.utils.hideSoftKeyboard
import kotlinx.android.synthetic.main.fragment_edit_entry.*

class EditEntryFragment : Fragment(), Toolbar.OnMenuItemClickListener {

  private val viewModel: EditEntryViewModel by viewModels()

  private val existingEntryName get() = navArgs<EditEntryFragmentArgs>().value.entryStardate

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.fragment_edit_entry, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    toolbar.setNavigationOnClickListener {
      saveEntry()
      navigateUp()
    }
    toolbar.inflateMenu(R.menu.toolbar_edit_menu)
    toolbar.setOnMenuItemClickListener(this@EditEntryFragment)

    if (existingEntryName.isNotBlank()) {
      stardateEditText.setText(existingEntryName)
      bodyEditText.setText(viewModel.decryptEntry(existingEntryName))
    }

    requireActivity().onBackPressedDispatcher.addCallback(this, true) {
      saveEntry()
      findNavController().popBackStack()
    }

    viewModel.snackbar.observe(viewLifecycleOwner, Observer { text ->
      text?.let {
        Snackbar.make(view, text, Snackbar.LENGTH_SHORT).show()
        viewModel.onSnackbarShown()
      }
    })
  }

  override fun onMenuItemClick(item: MenuItem?): Boolean {
    return when (item?.itemId) {
      R.id.menu_edit_done -> {
        saveEntry()
        NavHostFragment.findNavController(this).navigateUp()
        navigateUp()
        true
      }
      R.id.menu_edit_delete -> {
        viewModel.deleteEntry(existingEntryName)
        navigateUp()
        true
      }
      else -> false
    }
  }

  private fun saveEntry() {
    viewModel.encryptEntry(
      stardateEditText.text.toString(),
      bodyEditText.text.toString(),
      existingEntryName
    )
  }

  private fun navigateUp() {
    NavHostFragment.findNavController(this).navigateUp()
    hideSoftKeyboard(activity as? Activity)
  }
}
