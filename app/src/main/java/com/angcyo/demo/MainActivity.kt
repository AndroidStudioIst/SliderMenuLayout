package com.angcyo.demo

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import com.angcyo.library.SliderMenuLayout

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val sliderMenuLayout: SliderMenuLayout = findViewById(R.id.slider_menu_layout)
        sliderMenuLayout.sliderCallback = object : SliderMenuLayout.SimpleSliderCallback() {
            override fun onMenuSlider(menuLayout: SliderMenuLayout, ratio: Float, isTouchDown: Boolean) {
                super.onMenuSlider(menuLayout, ratio, isTouchDown)
                Log.i("angcyo", "$ratio  $isTouchDown")
                if (!isTouchDown) {
                    if (ratio >= 1f) {
                        Log.i("angcyo", "菜单之前:${menuLayout.isOldMenuOpen} ->完全打开")
                    } else if (ratio <= 0f) {
                        Log.i("angcyo", "菜单之前:${menuLayout.isOldMenuOpen} ->完全关闭")
                    }
                }
            }
        }

        val button1: View = findViewById(R.id.button1)
        val button2: View = findViewById(R.id.button2)

        button1.setOnClickListener {
            Log.i("angcyo", "按钮1点击")
        }
        button2.setOnClickListener {
            Log.i("angcyo", "按钮2点击")
        }

        val contentCenterButton: View = findViewById(R.id.content_center_button)
        contentCenterButton.setOnClickListener {
            sliderMenuLayout.openMenu()
        }
        val menuCenterButton: View = findViewById(R.id.menu_center_button)
        menuCenterButton.setOnClickListener {
            sliderMenuLayout.closeMenu()
        }
    }
}
