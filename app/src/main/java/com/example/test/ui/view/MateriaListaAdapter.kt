package com.example.test.ui.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.test.R
import com.example.test.data.model.Materia

class MateriaListaAdapter(
    private var materias: List<Materia>,
    private val onItemClick: (Materia) -> Unit
) : RecyclerView.Adapter<MateriaListaAdapter.MateriaViewHolder>() {

    inner class MateriaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nombreMateria: TextView = itemView.findViewById(R.id.nombreMateria)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MateriaViewHolder {
        // Aquí cargamos el NUEVO diseño de lista horizontal
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_materia_lista, parent, false)
        return MateriaViewHolder(view)
    }

    override fun onBindViewHolder(holder: MateriaViewHolder, position: Int) {
        val materia = materias[position]
        holder.nombreMateria.text = materia.nombreMateria

        holder.itemView.setOnClickListener {
            onItemClick(materia)
        }
    }

    override fun getItemCount(): Int = materias.size

    fun updateList(newList: List<Materia>) {
        materias = newList
        notifyDataSetChanged()
    }
}