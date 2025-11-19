package com.example.test.ui.view

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.test.R
import com.example.test.data.model.Materia

class MateriaAdapter(private var materias: List<Materia>) :
    RecyclerView.Adapter<MateriaAdapter.MateriaViewHolder>() {

    inner class MateriaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nombreMateria: TextView = itemView.findViewById(R.id.nombreMateria)
        val imagenMateria: ImageView = itemView.findViewById(R.id.imageMateria)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MateriaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_materia, parent, false)
        return MateriaViewHolder(view)
    }

    override fun onBindViewHolder(holder: MateriaViewHolder, position: Int) {
        val materia = materias[position]
        holder.nombreMateria.text = materia.nombreMateria

        // Cada card tiene comportamiento independiente
        holder.itemView.setOnClickListener {

            val context = holder.itemView.context
            val intent = Intent(context, DetalleMateriaActivity::class.java)

            // Pasamos los datos
            intent.putExtra("nombreMateria", materia.nombreMateria)
            intent.putExtra("id", materia.id) // si tu modelo tiene un ID

            context.startActivity(intent)

        }



    }

    override fun getItemCount(): Int = materias.size

    fun updateList(newList: List<Materia>) {
        materias = newList
        notifyDataSetChanged()
    }
}
