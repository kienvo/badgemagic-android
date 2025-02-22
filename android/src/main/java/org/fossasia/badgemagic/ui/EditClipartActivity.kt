package org.fossasia.badgemagic.ui

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import org.fossasia.badgemagic.R
import org.fossasia.badgemagic.databinding.ActivityEditClipartBinding
import org.fossasia.badgemagic.util.Converters
import org.fossasia.badgemagic.util.StorageUtils
import org.fossasia.badgemagic.viewmodels.EditClipartViewModel
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class EditClipartActivity : AppCompatActivity() {

    private val editClipartViewModel by viewModel<EditClipartViewModel>()
    private lateinit var fileName: String
    private val storageUtils: StorageUtils by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val activityDrawBinding = DataBindingUtil.setContentView<ActivityEditClipartBinding>(this, R.layout.activity_edit_clipart)
        activityDrawBinding.viewModel = editClipartViewModel

        if (intent.hasExtra("fileName")) {
            fileName = intent?.extras?.getString("fileName") ?: ""
            editClipartViewModel.drawingJSON.set(Converters.convertDrawableToLEDHex(storageUtils.getClipartFromPath(fileName), false))
        }

        editClipartViewModel.savedButton.observe(
            this,
            Observer {
                if (it) {
                    if (storageUtils.saveEditedClipart(Converters.convertStringsToLEDHex(activityDrawBinding.drawLayout.getCheckedList()), fileName)) {
                        Toast.makeText(this, R.string.clipart_saved_success, Toast.LENGTH_LONG).show()
                        editClipartViewModel.updateClipArts()
                    } else
                        Toast.makeText(this, R.string.clipart_saved_error, Toast.LENGTH_LONG).show()
                    finish()
                }
            }
        )

        editClipartViewModel.resetButton.observe(
            this,
            Observer {
                if (it) {
                    activityDrawBinding.drawLayout.resetCheckListWithDummyData()
                }
            }
        )
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.open_folder, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.open_Folder -> {
                val intent = Intent(this, DrawerActivity::class.java)
                intent.putExtra("clipart", "clipart")
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
