package org.fossasia.badgemagic.ui

import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import org.fossasia.badgemagic.R
import org.fossasia.badgemagic.data.BadgeConfig
import org.fossasia.badgemagic.databinding.ActivityEditBadgeBinding
import org.fossasia.badgemagic.util.Converters
import org.fossasia.badgemagic.util.SendingUtils
import org.fossasia.badgemagic.util.StorageUtils
import org.fossasia.badgemagic.viewmodels.EditBadgeViewModel
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class EditBadgeActivity : AppCompatActivity() {

    private val viewModel by viewModel<EditBadgeViewModel>()
    private lateinit var fileName: String
    private val storageUtils: StorageUtils by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = DataBindingUtil.setContentView<ActivityEditBadgeBinding>(this, R.layout.activity_edit_badge)
        binding.viewModel = viewModel

        if (intent.hasExtra("badgeJSON") && intent.hasExtra("fileName")) {
            viewModel.drawingJSON.set(intent?.extras?.getString("badgeJSON"))
            fileName = intent?.extras?.getString("fileName") ?: ""
        }

        viewModel.savedButton.observe(
            this,
            Observer {
                if (it) {
                    val badgeConfig = SendingUtils.getBadgeFromJSON(viewModel.drawingJSON.get() ?: "{}")
                    badgeConfig.hexStrings = Converters.convertBitmapToLEDHex(
                        Converters.convertStringsToLEDHex(binding.drawLayout.getCheckedList()),
                        false
                    )
                    StoreAsync(fileName, badgeConfig, viewModel, storageUtils).execute()
                    Toast.makeText(this, R.string.saved_edited_badge, Toast.LENGTH_LONG).show()
                    finish()
                }
            }
        )

        viewModel.resetButton.observe(
            this,
            Observer {
                if (it) {
                    binding.drawLayout.resetCheckListWithDummyData()
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
                intent.putExtra("badge", "badge")
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {

        class StoreAsync(private val fileName: String, private val badgeConfig: BadgeConfig, private val viewModel: EditBadgeViewModel, private val storageUtil: StorageUtils) : AsyncTask<Void, Void, Void>() {
            override fun doInBackground(vararg params: Void?): Void? {
                storageUtil.saveEditedBadge(badgeConfig, fileName)
                return null
            }

            override fun onPostExecute(result: Void?) {
                viewModel.updateFiles()
                super.onPostExecute(result)
            }
        }
    }
}
