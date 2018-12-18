package com.github.herokotlin.photobrowser

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.TypedValue
import android.view.View
import android.widget.Toast
import com.github.herokotlin.photobrowser.model.Photo
import kotlinx.android.synthetic.main.photo_browser_activity.*

class PhotoBrowserActivity: AppCompatActivity() {

    companion object {

        private const val KEY_INDEX = "index"

        private const val KEY_PHOTOS = "photos"

        private const val KEY_PAGE_MARGIN = "pageMargin"

        fun newInstance(context: Context, photos: ArrayList<Photo>, index: Int, pageMargin: Int) {
            val intent = Intent(context, PhotoBrowserActivity::class.java)
            intent.putParcelableArrayListExtra(KEY_PHOTOS, photos)
            intent.putExtra(KEY_INDEX, index)
            intent.putExtra(KEY_PAGE_MARGIN, pageMargin)
            context.startActivity(intent)
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        var flags = View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            flags = flags or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        }

        window.decorView.systemUiVisibility = flags

        supportActionBar?.hide()

        setContentView(R.layout.photo_browser_activity)

        val photos = intent.getParcelableArrayListExtra<Photo>(KEY_PHOTOS)

        val index = intent.getIntExtra(KEY_INDEX, 0)

        val pageMargin = intent.getIntExtra(KEY_PAGE_MARGIN, 0)


        browserView.callback = object: PhotoBrowserCallback {
            override fun onTap(photo: Photo) {
                finish()
            }
            override fun onSave(photo: Photo, success: Boolean) {
                Toast.makeText(applicationContext,  if (success) R.string.photo_browser_save_success else R.string.photo_browser_save_failure, Toast.LENGTH_SHORT).show()
            }
        }

        browserView.photos = photos

        browserView.index = index

        browserView.pageMargin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, pageMargin.toFloat(), resources.displayMetrics).toInt()

    }
}