package smy.com.kotlintest

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import java.net.URL

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var listView = findViewById<RecyclerView>(R.id.listView)
        listView.layoutManager = LinearLayoutManager(this)
        listView.adapter = TestListAdapter(mItems)
        //toast("smy")
    }

    fun toast(message: String,
              tag: String = javaClass.simpleName,
              lenght: Int = Toast.LENGTH_SHORT){
        Toast.makeText(this, "$tag $message", lenght).show()
    }

    private val mItems = listOf<String>(
            "Who am I",
            "Where am I",
            "What am I doing",
            "Who are you",
            "Where are you",
            "what are you doing"
    );

    class TestListAdapter(val items: List<String>) : RecyclerView.Adapter<TestListAdapter.ViewHolder>(){

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(TextView(parent.context))
        }

        override fun getItemCount(): Int = items.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.textView.text = items[position]
        }

        class ViewHolder(var textView : TextView) : RecyclerView.ViewHolder(textView)
    }

    class Request(val url: String){
        fun run(){
            val jsonStr = URL(url).readText()
            Log.d(javaClass.simpleName, jsonStr)
        }
    }

}
