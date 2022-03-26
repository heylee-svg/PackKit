package com.dhg.packkit.designview

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.dhg.packkit.R


import android.view.View
import android.widget.FrameLayout


class ViewActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.flow_layout)
//        setContentView(R.layout.layout_design_view)
//        val root = findViewById<View>(R.id.root) as FrameLayout
//        root.addView(MyRegionView(this@ViewActivity))

    }
}