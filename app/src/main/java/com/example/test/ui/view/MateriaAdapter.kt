package com.example.test.ui.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.test.R
import com.example.test.data.model.Materia

class MateriaAdapter(
    private var materias: List<Materia>,
    private val onItemClick: (Materia) -> Unit,
    private val onEditClick: (Materia) -> Unit,   // Nuevo Callback para editar
    private val onDeleteClick: (Materia) -> Unit  // Nuevo Callback para eliminar
) : RecyclerView.Adapter<MateriaAdapter.MateriaViewHolder>() {

    inner class MateriaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nombreMateria: TextView = itemView.findViewById(R.id.nombreMateria)
        val imagenMateria: ImageView = itemView.findViewById(R.id.imageMateria)
        val btnMoreOptions: ImageView = itemView.findViewById(R.id.btnMoreOptions)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MateriaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_materia, parent, false)
        return MateriaViewHolder(view)
    }

    override fun onBindViewHolder(holder: MateriaViewHolder, position: Int) {
        val materia = materias[position]
        holder.nombreMateria.text = materia.nombreMateria

        // 1. Clic en la tarjeta (Ir a detalles)
        holder.itemView.setOnClickListener {
            onItemClick(materia)
        }

        // 2. Clic en los 3 puntitos (Abrir Menú)
        holder.btnMoreOptions.setOnClickListener { view ->
            showPopupMenu(view, materia)
        }
    }

    // Función para mostrar el menú emergente
    private fun showPopupMenu(view: View, materia: Materia) {
        val popup = PopupMenu(view.context, view)
        popup.inflate(R.menu.menu_opciones) // Inflamos el XML que creamos

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_edit -> {
                    onEditClick(materia) // Avisamos al Activity que edite
                    true
                }
                R.id.action_delete -> {
                    onDeleteClick(materia) // Avisamos al Activity que elimine
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    override fun getItemCount(): Int = materias.size

    fun updateList(newList: List<Materia>) {
        materias = newList
        notifyDataSetChanged()
    }
}