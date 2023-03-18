package com.startupsurveys.ui.home

import android.app.AlertDialog
import android.app.ProgressDialog.show
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.startupsurveys.MainActivity
import com.startupsurveys.R
import com.startupsurveys.adapter.SurveyAdapter
import com.startupsurveys.model.Survey
import com.startupsurveys.ui.survey.SurveyFragment
import com.startupsurveys.util.SurveyHelper.Companion.getDemoAppNames


class HomeFragment : Fragment() {
    private lateinit var searchView: SearchView
    private lateinit var listView: ListView
    private lateinit var headerImageView: ImageView

    private lateinit var adapter: SurveyAdapter
    private lateinit var boxList: ArrayList<Survey>


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the view into the active container

        val root = inflater.inflate(R.layout.fragment_home, container, false)

        searchView = root.findViewById(R.id.search_view)
        listView = root.findViewById(R.id.list_view)
        headerImageView = root.findViewById(R.id.header_image_view)

        headerImageView.setOnClickListener(View.OnClickListener {

            // Open a dialog with an edit text that enables setting an address
            val dialog = AlertDialog.Builder(context as Context)
                .setTitle("Set your address")
                .setMessage("Enter your XRP address to receive rewards")
                .setView(R.layout.dialog_set_address)
                .setPositiveButton("Save") { dialog, which ->
                    // Get the edit text content
                    val editText =
                        (dialog as AlertDialog).findViewById<EditText>(R.id.edit_text_address)
                    val address = editText?.text.toString()
                    (activity as MainActivity).prefManager.saveString("USER_ADDRESS", address)
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel") { dialog, which ->
                    dialog.cancel()
                }
                .create()
            // Set the edit text of the dialog to the user address
            dialog.setOnShowListener {
                val editText =
                    (dialog as AlertDialog).findViewById<EditText>(R.id.edit_text_address)
                editText?.setText((activity as MainActivity).prefManager.getString("USER_ADDRESS", ""))
            }

            dialog.show()
        })


        // Set up the list root

        // Set up the list root
        boxList = ArrayList()
        boxList.addAll(getDemoAppNames())

        adapter = SurveyAdapter(context as Context, boxList)
        listView.adapter = adapter

        // Set up the search bar
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter.filter(newText)
                return false
            }
        })

        // Set up item click listener

        // Set up item click listener
        listView.setOnItemClickListener { adapterView, root, i, l ->
            // Show error toast if address not in pref manager
            if ((activity as MainActivity).prefManager.getString("USER_ADDRESS", "") == "") {
                Toast.makeText(context, "Set your address for rewards by clicking the Surveyr logo", Toast.LENGTH_SHORT).show()
                return@setOnItemClickListener
            }

            val selected: Survey = adapter.getItem(i) as Survey
            val bundle = bundleOf(
                "appName" to selected.appName,
                "numQuestions" to selected.numQuestions,
                "reward" to selected.reward
            )
            (activity as MainActivity).navigateTo(SurveyFragment(), bundle)
        }

        return root
    }


    override fun onDestroyView() {
        super.onDestroyView()
    }
}