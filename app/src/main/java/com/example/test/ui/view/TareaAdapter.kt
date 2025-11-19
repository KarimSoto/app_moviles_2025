package com.example.test.ui.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.test.R
import com.example.test.data.model.TareaConMateria
import android.content.Intent
import com.example.test.ui.view.DetalleTareaActivity


class TareaAdapter(private var tareas: List<TareaConMateria>) :
    RecyclerView.Adapter<TareaAdapter.TareaViewHolder>() {

    inner class TareaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nombreTarea: TextView = itemView.findViewById(R.id.nombreTarea)
        val fechaEntrega: TextView = itemView.findViewById(R.id.fechaEntrega)
        val materiaNombre: TextView = itemView.findViewById(R.id.materiaNombre)
    }

    // 🔹 Este método FALTABA: crea la vista de cada item
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TareaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tarea, parent, false)
        return TareaViewHolder(view)
    }

    // 🔹 Asigna los datos a los elementos de la vista
    override fun onBindViewHolder(holder: TareaViewHolder, position: Int) {
        val tarea = tareas[position]
        holder.nombreTarea.text = tarea.nombreTarea
        holder.fechaEntrega.text = tarea.fechaEntrega
        holder.materiaNombre.text = tarea.materiaNombre

        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, DetalleTareaActivity::class.java)
            intent.putExtra("id", tarea.id)  // si tienes un ID
            intent.putExtra("nombreTarea", tarea.nombreTarea)
            intent.putExtra("fechaEntrega", tarea.fechaEntrega)
            intent.putExtra("completada", tarea.completada)
            intent.putExtra("materiaId", tarea.materiaId)
            context.startActivity(intent)
        }

    }

    override fun getItemCount(): Int = tareas.size

    fun updateList(newList: List<TareaConMateria>) {
        tareas = newList
        notifyDataSetChanged()
    }
}
