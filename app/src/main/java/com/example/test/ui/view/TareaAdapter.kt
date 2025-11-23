package com.example.test.ui.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.test.R
import com.example.test.data.model.TareaConMateria
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class TareaAdapter(
    private var tareas: List<TareaConMateria>,
    // Callbacks opcionales por si solo quieres ver la lista sin acciones en algunos lados
    private val onItemClick: ((TareaConMateria) -> Unit)? = null,
    private val onEditClick: ((TareaConMateria) -> Unit)? = null,
    private val onDeleteClick: ((TareaConMateria) -> Unit)? = null
) : RecyclerView.Adapter<TareaAdapter.TareaViewHolder>() {

    inner class TareaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nombreTarea: TextView = itemView.findViewById(R.id.nombreTarea)
        val fechaEntrega: TextView = itemView.findViewById(R.id.fechaEntrega)
        val materiaNombre: TextView = itemView.findViewById(R.id.materiaNombre)
        val colorBar: View = itemView.findViewById(R.id.colorBar)
        val btnMoreOptions: ImageView = itemView.findViewById(R.id.btnMoreOptions)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TareaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tarea, parent, false)
        return TareaViewHolder(view)
    }

    override fun onBindViewHolder(holder: TareaViewHolder, position: Int) {
        val tarea = tareas[position]
        val context = holder.itemView.context

        holder.nombreTarea.text = tarea.nombreTarea
        holder.fechaEntrega.text = "Entrega: ${tarea.fechaEntrega}"
        holder.materiaNombre.text = "Materia: ${tarea.materiaNombre}"

        // Lógica de colores (Semáforo)
        val colorRes = try {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val fechaEntregaDate = LocalDate.parse(tarea.fechaEntrega, formatter)
            val hoy = LocalDate.now()
            when {
                tarea.completada -> R.color.status_completed
                fechaEntregaDate.isBefore(hoy) -> R.color.status_late
                else -> R.color.status_pending
            }
        } catch (e: Exception) { R.color.status_pending }
        holder.colorBar.setBackgroundColor(ContextCompat.getColor(context, colorRes))

        // Clic en la tarjeta (Detalles)
        holder.itemView.setOnClickListener {
            onItemClick?.invoke(tarea)
        }

        // Clic en los 3 puntitos (Menú)
        holder.btnMoreOptions.setOnClickListener { view ->
            showPopupMenu(view, tarea)
        }
    }

    private fun showPopupMenu(view: View, tarea: TareaConMateria) {
        val popup = PopupMenu(view.context, view)
        popup.inflate(R.menu.menu_opciones) // Usamos el mismo menú que materias
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_edit -> {
                    onEditClick?.invoke(tarea)
                    true
                }
                R.id.action_delete -> {
                    onDeleteClick?.invoke(tarea)
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    override fun getItemCount(): Int = tareas.size

    fun updateList(newList: List<TareaConMateria>) {
        tareas = newList
        notifyDataSetChanged()
    }
}