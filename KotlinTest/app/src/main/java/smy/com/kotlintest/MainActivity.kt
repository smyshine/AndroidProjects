package smy.com.kotlintest

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var listView = findViewById<RecyclerView>(R.id.listView)
        listView.layoutManager = LinearLayoutManager(this)
        listView.adapter = TestListAdapter(mItems)
        toast("smy")
    }

    fun toast(message: String,
              tag: String = javaClass.simpleName,
              lenght: Int = Toast.LENGTH_SHORT){
        Toast.makeText(this, "$tag $message", lenght).show()
    }

    private val mItems = listOf<String>(
            "It's Monday, and it's sleepy",
            "It's Tuesday, and it's busy",
            "It's Wednesday, and it's happy",
            "It's Thursday, and it's ppy",
            "It's Friday, and it's slow"
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
}
