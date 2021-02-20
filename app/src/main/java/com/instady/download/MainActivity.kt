package com.instady.download

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.instady.download.fragment.DownloadFragment
import com.instady.download.fragment.InputFragment
import com.instady.download.fragment.WebFragment
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : BaseActivity() {
    private val titles = arrayOf(R.string.input, R.string.download, R.string.browser)
    private val icons = arrayOf(
        R.drawable.sel_link,
        R.drawable.sel_download,
        R.drawable.sel_browser
    )
    private val fragmentList: MutableList<Fragment> = ArrayList()

    companion object {
        const val TAG = "instagram"
        val REQUEST_PERMISSION = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        const val REQUEST_PERMISSION_CODE = 10
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        initViews()
        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(this, REQUEST_PERMISSION, REQUEST_PERMISSION_CODE)
        }
        val saveToAlbum: Boolean =
            getSharedPreferences("data", Context.MODE_PRIVATE).getBoolean("saveToAlbum", true)
        Log.d(TAG, saveToAlbum.toString())
        nav_view.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.share_app -> {
                    Log.d(TAG, "onOptionsItemSelected: ")
                    val intent = Intent(Intent.ACTION_SEND)
                    intent.type = "text/plain";
                    intent.putExtra(Intent.EXTRA_SUBJECT, title);
                    intent.putExtra(
                        Intent.EXTRA_TEXT,
                        getString(R.string.share_app_message)
                    )
                    startActivity(Intent.createChooser(intent, title))
                }
                R.id.support_app -> {
                    val uri = Uri.parse("market://details?id=$packageName")
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(Intent.createChooser(intent, getString(R.string.choose_market)))
                }
                R.id.download_setting -> {
                    val intent = Intent(this, DownloadSettingActivity::class.java)
                    startActivity(intent)
                }
                R.id.storage_setting -> {
                    val intent = Intent(this, StorageSettingActivity::class.java)
                    startActivity(intent)
                }
                R.id.support_us -> {
                    val intent = Intent(this, DonateActivity::class.java)
                    startActivity(intent)
                }
            }
            true
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.drawer_btn -> drawerLayout.openDrawer(GravityCompat.END)
        }
        return true
    }

    private fun initViews() {
        //初始化fragment
        fragmentList.add(InputFragment())
        fragmentList.add(DownloadFragment())
        fragmentList.add(WebFragment())
        //初始化viewPage
        viewPager!!.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int {
                return fragmentList.size
            }

            override fun createFragment(position: Int): Fragment {
                return fragmentList[position]
            }
        }
        viewPager.offscreenPageLimit = 3
        val tabLayoutMediator =
            TabLayoutMediator(tab_layout, viewPager) { tab: TabLayout.Tab, position: Int ->
                tab.text = getString(titles[position])
                tab.setIcon(icons[position])
            }
        tabLayoutMediator.attach()
        title = getString(R.string.title_1)
        tab_layout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                var position = tab!!.position.toInt()
                Log.d("xdy", "selected$position")
                when (position) {
                    0 -> title = getString(R.string.title_1)
                    1 -> title = getString(R.string.title_2)
                    2 -> title = getString(R.string.title_3)
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun allPermissionsGranted(): Boolean {
        return REQUEST_PERMISSION.all { it ->
            ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
        }
    }
}