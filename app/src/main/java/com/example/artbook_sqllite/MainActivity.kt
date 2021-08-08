package com.example.artbook_sqllite

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.artbook_sqllite.databinding.ActivityMainBinding
import java.lang.Exception

class MainActivity : AppCompatActivity() {

    private lateinit var bindig : ActivityMainBinding
    private lateinit var artList: ArrayList<Art>
    private lateinit var artAdaptor : RecyclerAdaptor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindig = ActivityMainBinding.inflate(layoutInflater)
        val view = bindig.root
        setContentView(view)

        artList = ArrayList<Art>()

        artAdaptor = RecyclerAdaptor(artList)
        bindig.recyclerReview.layoutManager = LinearLayoutManager(this)
        bindig.recyclerReview.adapter = artAdaptor

        try {
            val database = this.openOrCreateDatabase("Arts", MODE_PRIVATE,null)

            val cursor = database.rawQuery("SELECT * FROM arts",null)
            var nameArtIx = cursor.getColumnIndex("artName")
            var idIx = cursor.getColumnIndex("id")

            while(cursor.moveToNext()){
                val name = cursor.getString(nameArtIx)
                val id = cursor.getInt(idIx)
                val art = Art(name,id)
                artList.add(art)
            }
            artAdaptor.notifyDataSetChanged()

            cursor.close()

        }catch (e: Exception){
            e.printStackTrace()
        }
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.art_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if(item.itemId == R.id.itemSave){
            val intent = Intent(this@MainActivity, DetailsActivity::class.java)
            intent.putExtra("info", "new")
            startActivity(intent)
        }

        return super.onOptionsItemSelected(item)
    }
}