package com.erkindilekci.travelbook

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.erkindilekci.travelbook.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var placeList : ArrayList<Place>
    private lateinit var placeAdapter: PlaceAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        placeList = ArrayList<Place>()

        placeAdapter = PlaceAdapter(placeList)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = placeAdapter



        try {
            val database = this.openOrCreateDatabase("Places", MODE_PRIVATE, null)

            val cursor = database.rawQuery("SELECT * FROM places", null)
            val placeNameIndex = cursor.getColumnIndex("placename")
            val idIndex = cursor.getColumnIndex("id")

            while (cursor.moveToNext()){
                val name = cursor.getString(placeNameIndex)
                val id = cursor.getInt(idIndex)
                val place = Place(name, id)
                placeList.add(place)
            }
            placeAdapter.notifyDataSetChanged()
            cursor.close()



        } catch (e: Exception){
            e.printStackTrace()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.travel_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.add_item){
            val intent = Intent(this@MainActivity, DetailActivity::class.java)
            intent.putExtra("info", "new")
            startActivity(intent)
        }
        return super.onOptionsItemSelected(item)
    }
}