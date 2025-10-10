package com.example.amply.ui.reservation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.amply.R
import com.example.amply.model.ReservationExtended
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

class ReservationListFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyState: LinearLayout
    private lateinit var adapter: ReservationCardAdapter
    private var status: String = "Pending"

    // Retrofit API Interface
    interface ReservationApi {
        @GET("api/v1/reservations")
        fun getReservations(): Call<List<ReservationExtended>>
    }

    companion object {
        private const val ARG_STATUS = "status"

        // Factory method to create a new instance of the fragment with a specific status
        fun newInstance(status: String): ReservationListFragment {
            val fragment = ReservationListFragment()
            val args = Bundle()
            args.putString(ARG_STATUS, status)
            fragment.arguments = args
            return fragment
        }
    }

    // Called when fragment is created
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            status = it.getString(ARG_STATUS, "Pending")
        }
    }

    // Inflates the fragment's layout
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_reservation_list, container, false)
    }

    // Called after the view has been created
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views
        recyclerView = view.findViewById(R.id.recyclerViewReservations)
        emptyState = view.findViewById(R.id.emptyState)

        // Setup RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = ReservationCardAdapter(mutableListOf()) { reservation ->
            // Handle item click - navigate to details
            Toast.makeText(
                requireContext(),
                "Clicked: ${reservation.reservationCode}",
                Toast.LENGTH_SHORT
            ).show()
        }
        recyclerView.adapter = adapter

        // Fetch reservations from API
        fetchReservations()
    }

    // Fetches reservations from backend using Retrofit, filters by status, and updates UI
    private fun fetchReservations() {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://conor-truculent-rurally.ngrok-free.dev/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(ReservationApi::class.java)

        api.getReservations().enqueue(object : Callback<List<ReservationExtended>> {
            override fun onResponse(
                call: Call<List<ReservationExtended>>,
                response: Response<List<ReservationExtended>>
            ) {
                if (response.isSuccessful) {
                    val allReservations = response.body() ?: emptyList()
                    
                    // Filter reservations by status
                    val filteredReservations = allReservations.filter { reservation ->
                        reservation.status.equals(status, ignoreCase = true)
                    }

                    if (filteredReservations.isNotEmpty()) {
                        adapter.updateData(filteredReservations)
                        showRecyclerView()
                    } else {
                        showEmptyState()
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Failed to load reservations: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                    showEmptyState()
                }
            }

            override fun onFailure(call: Call<List<ReservationExtended>>, t: Throwable) {
                Toast.makeText(
                    requireContext(),
                    "Network error: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
                showEmptyState()
            }
        })
    }

    // Shows RecyclerView and hides empty state
    private fun showRecyclerView() {
        recyclerView.visibility = View.VISIBLE
        emptyState.visibility = View.GONE
    }

    // Shows empty state and hides RecyclerView
    private fun showEmptyState() {
        recyclerView.visibility = View.GONE
        emptyState.visibility = View.VISIBLE
    }
}
