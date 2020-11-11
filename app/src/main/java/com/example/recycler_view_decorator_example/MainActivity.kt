package com.example.recycler_view_decorator_example

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.decorator.AntiAliasPaint
import com.decorator.BitmapAsCanvas
import com.decorator.Config
import com.decorator.decorateAll

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<RecyclerView>(R.id.rv).apply {
            adapter = Adapter()
            decorateAll {
                canvas(Config.Location.TOP) {
                    bitmap = BitmapAsCanvas(300, 100){
                        drawOval(0f,0f, 300f, 100f, AntiAliasPaint().apply { color = Color.BLUE })
                    }
                }
            }
        }
    }
}

class Adapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        Holder(LayoutInflater.from(parent.context).inflate(R.layout.holder_item, parent, false))

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    }

    override fun getItemCount(): Int = 10
}

class Holder(view: View) : RecyclerView.ViewHolder(view)