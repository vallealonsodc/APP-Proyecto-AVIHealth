package com.us.app_gsti.pantallas.paciente

import android.Manifest
import android.app.ActivityOptions
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.us.app_gsti.pantallas.CabeceraPaciente


class MapasPacienteActivity : CabeceraPaciente(), OnMapReadyCallback, GoogleMap.OnMyLocationClickListener {

    private lateinit var btnTeleconsultas: Button
    private lateinit var btnCitas: Button
    private lateinit var map : GoogleMap

    //para los permisos de localización en tiempo real del mapa
    companion object {
        const val REQUEST_CODE_LOCATION = 0
    }

    private val TAG: String = MapasPacienteActivity::class.java.getSimpleName()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.us.app_gsti.R.layout.activity_mapas_paciente)

        // Este código sirve para que la barra de navigationBar (la de abajo que te permite volver atras o salir de la app) se oculte.
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        // Ocultar la barra de navegación y la barra de estado.
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())

        // Configurar el comportamiento para que la barra de navegación se muestre temporalmente con un gesto de deslizamiento.
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        //------------------------------------------------------------------------------------------

        val mapasButton = findViewById<Button>(com.us.app_gsti.R.id.btn_mapa)

        // Cambiar el color del icono cuando estas en esta activity
        val blueColor = ContextCompat.getColor(this, com.us.app_gsti.R.color.blue) // Replace with your actual blue color resource
        mapasButton?.compoundDrawables?.get(1)?.colorFilter =
            BlendModeColorFilterCompat.createBlendModeColorFilterCompat(blueColor, BlendModeCompat.SRC_ATOP)

        //------------------------------------------------------------------------------------------

        btnCitas = findViewById(com.us.app_gsti.R.id.btn_citas)
        btnCitas.setOnClickListener{
            intent = Intent(this,  CitasPacienteActivity::class.java)

            val options = ActivityOptions.makeCustomAnimation(
                this, com.us.app_gsti.R.transition.fade_in, com.us.app_gsti.R.transition.fade_out
            )

            startActivity(intent, options.toBundle())
        }

        btnTeleconsultas = findViewById(com.us.app_gsti.R.id.btn_teleconsulta)
        btnTeleconsultas.setOnClickListener{
            intent = Intent(this,  TeleconsultasPacienteActivity::class.java)

            val options = ActivityOptions.makeCustomAnimation(
                this, com.us.app_gsti.R.transition.fade_in, com.us.app_gsti.R.transition.fade_out
            )

            startActivity(intent, options.toBundle())
        }

        val btnDesplegable = findViewById<Button>(com.us.app_gsti.R.id.btn_settings_paciente)
        btnDesplegable.setOnClickListener { view ->
            showMenuPaciente(view)
        }

        //-----------------------------------
        createMapFragment()

    }

    //------------------------MAPA----------------------

    //crea la lógica del contenedor del mapa
    private fun createMapFragment() {
        val mapFragment = supportFragmentManager
            .findFragmentById(com.us.app_gsti.R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    //cuando el mapa este creado...
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        enableMyLocation()
        initialMarker()

        // Agrega el primer marcador

        // Agrega el primer marcador
        val locationOne = LatLng(37.4066629, -5.9863801) // Reemplaza con tus coordenadas

        map.addMarker(MarkerOptions().position(locationOne).title("Hospital Universitario Virgen Macarena"))

        // Agrega el segundo marcador

        // Agrega el segundo marcador
        val locationTwo = LatLng(37.3614156, -5.9800085) // Reemplaza con tus coordenadas

        map.addMarker(MarkerOptions().position(locationTwo).title("Hospital Universitario Virgen del Rocío"))

        // Opcional: Mover la cámara para que ambas localizaciones sean visibles

        // Opcional: Mover la cámara para que ambas localizaciones sean visibles
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(locationOne, 10f))
        stylishMap()
    }


    //-----------------Permisos para localización inicial----------------------------------
    private fun isPermissionsGranted() = ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    //Habilita la localización
    private fun enableMyLocation() {
        //comprueba que el mapa esté inicializado
        if (!::map.isInitialized) return
        //comprueba que los permisos estan aceptados
        if (isPermissionsGranted()) {
            //si están acpetados activa la localización en tiempo real
            map.isMyLocationEnabled = true
        } else {
            //si no están aceptados pide los permisos
            requestLocationPermission()
        }
    }

    private fun requestLocationPermission() {
        //si los permisos ya han sido pedidos y los ha rechazado --> toast
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            Toast.makeText(this, "Ve a ajustes y acepta los permisos", Toast.LENGTH_SHORT).show()
        } else {
            //si no han sido pedidos aun --> los pide
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_CODE_LOCATION)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            //si los acepta --> activa la localización
            REQUEST_CODE_LOCATION -> if(grantResults.isNotEmpty() && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                map.isMyLocationEnabled = true
            }else{
                Toast.makeText(this, "Para activar la localización ve a ajustes y acepta los permisos", Toast.LENGTH_SHORT).show()
            }
            else -> {}
        }
    }

    override fun onResumeFragments() {
        super.onResumeFragments()
        if (!::map.isInitialized) return
        if(!isPermissionsGranted()){
            map.isMyLocationEnabled = false
            Toast.makeText(this, "Para activar la localización ve a ajustes y acepta los permisos", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onMyLocationClick(p0: Location) {
        Toast.makeText(this, "Estás en ${p0.latitude}, ${p0.longitude}", Toast.LENGTH_SHORT).show()
    }

    //Crea el marcador inicial del mapa
    private fun initialMarker() {
        val favoritePlace = LatLng(37.389810117336594, -5.984484716446584)
        map.addMarker(MarkerOptions().position(favoritePlace).title("Sevilla"))
        //acerca zoom al marcador
        map.animateCamera(
            CameraUpdateFactory.newLatLngZoom(favoritePlace, 10f),
            3000,
            null
        )
    }


    //Estilo del mapa
    private fun stylishMap(){
        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            val success: Boolean = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    this, com.us.app_gsti.R.raw.style_json
                )
            )
            if (!success) {
                Log.e(TAG, "Style parsing failed.")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e(TAG, "Can't find style. Error: ", e)
        }
    }



}