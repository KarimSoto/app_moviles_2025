package com.example.test.ui.view

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.test.R
import com.example.test.data.model.TareaConMateria
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class TareaAdapter(private var tareas: List<TareaConMateria>) :
    RecyclerView.Adapter<TareaAdapter.TareaViewHolder>() {

    inner class TareaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nombreTarea: TextView = itemView.findViewById(R.id.nombreTarea)
        val fechaEntrega: TextView = itemView.findViewById(R.id.fechaEntrega)
        val materiaNombre: TextView = itemView.findViewById(R.id.materiaNombre)
        val colorBar: View = itemView.findViewById(R.id.colorBar)
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

        // --- LÓGICA DE COLORES (SEMÁFORO) ---
        val colorRes = try {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val fechaEntregaDate = LocalDate.parse(tarea.fechaEntrega, formatter)
            val hoy = LocalDate.now()

            when {
                tarea.completada -> R.color.status_completed // VERDE (Completada)
                fechaEntregaDate.isBefore(hoy) -> R.color.status_late // ROJO (Vencida)
                else -> R.color.status_pending // NARANJA (Pendiente / A tiempo)
            }
        } catch (e: Exception) {
            // Si la fecha está vacía o tiene formato incorrecto, mostramos naranja por defecto
            R.color.status_pending
        }

        // CORRECCIÓN: Usamos setBackgroundColor para asegurar que el cambio se vea
        holder.colorBar.setBackgroundColor(ContextCompat.getColor(context, colorRes))

        // -------------------------------------

        holder.itemView.setOnClickListener {
            val intent = Intent(context, DetalleTareaActivity::class.java)
            intent.putExtra("id", tarea.id)
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