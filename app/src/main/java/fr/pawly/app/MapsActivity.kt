package fr.pawly.app

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import fr.pawly.app.databinding.ActivityMapsBinding

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    companion object {
        private const val LOCATION_PERMISSION_REQUEST = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "Gardiens autour de moi 📍"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = true

        // Demande permission localisation
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST
            )
        } else {
            afficherPosition()
        }
    }

    @SuppressLint("MissingPermission")
    private fun afficherPosition() {
        mMap.isMyLocationEnabled = true

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            val centre = if (location != null) {
                LatLng(location.latitude, location.longitude)
            } else {
                // Fallback Paris si GPS indisponible
                LatLng(48.8566, 2.3522)
            }

            // Zoom sur la position
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(centre, 13f))

            // Gardiens simulés autour de la position réelle
            val offset = 0.01
            val gardiens = listOf(
                Triple(
                    LatLng(centre.latitude + offset, centre.longitude + offset),
                    "Thomas — Dog-sitter", "Chiens & chats — 20€/j ⭐ 4.8"
                ),
                Triple(
                    LatLng(centre.latitude - offset, centre.longitude + offset * 2),
                    "Sarah — Pension animaux", "Maison avec jardin — 25€/j ⭐ 4.9"
                ),
                Triple(
                    LatLng(centre.latitude + offset * 2, centre.longitude - offset),
                    "Lucas — Garde & promenade", "Disponible — 18€/j ⭐ 4.7"
                ),
                Triple(
                    LatLng(centre.latitude - offset * 2, centre.longitude - offset * 2),
                    "Emma — Pension pro", "Chiens, chats, lapins — 22€/j ⭐ 5.0"
                ),
                Triple(
                    LatLng(centre.latitude + offset * 0.5, centre.longitude - offset * 1.5),
                    "Marc — Pet-sitter", "Spécialiste NAC — 15€/j ⭐ 4.6"
                )
            )

            gardiens.forEach { (pos, title, snippet) ->
                mMap.addMarker(
                    MarkerOptions()
                        .position(pos)
                        .title(title)
                        .snippet(snippet)
                        .icon(BitmapDescriptorFactory.defaultMarker(
                            BitmapDescriptorFactory.HUE_GREEN
                        ))
                )
            }

            Toast.makeText(
                this, "📍 ${gardiens.size} gardiens trouvés autour de vous",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            afficherPosition()
        } else {
            Toast.makeText(
                this, "Permission refusée — affichage sur Paris",
                Toast.LENGTH_SHORT
            ).show()
            mMap.animateCamera(
                CameraUpdateFactory.newLatLngZoom(LatLng(48.8566, 2.3522), 13f)
            )
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}