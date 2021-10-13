package kr.kro.fatcats.locationmemory

import android.Manifest
import android.content.pm.PackageManager
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.util.FusedLocationSource
import kotlinx.coroutines.*
import kr.kro.fatcats.locationmemory.databinding.ActivityMainBinding
import kr.kro.fatcats.locationmemory.model.Image
import kotlin.coroutines.CoroutineContext


@RequiresApi(Build.VERSION_CODES.N)
class MainActivity : AppCompatActivity() , OnMapReadyCallback ,CoroutineScope{

    private lateinit var binding: ActivityMainBinding
    private lateinit var map: NaverMap
    private lateinit var locationSource : FusedLocationSource
    private val mediaList = mutableListOf<Image>()
    private val requestCode = 1011
    private val job = Job()


    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()
    }

    private fun initView() {
        getNowLocation()
        setNaverMap()
        setClick()
    }
    private fun setClick() = with(binding){
        currentLocationButton.setOnClickListener {
            getPermission()
            if(ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.READ_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.ACCESS_MEDIA_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
                launch(coroutineContext) {
                    getPhoto()
                    setMarker()
                }
            }
        }
    }

    private suspend fun setMarker()=withContext(Dispatchers.Main){

        binding.setVariable(BR.visibilityBool,false)

        if(mediaList.size != 0){
            mediaList.map {
                it.modelLatLng?.let { gps ->
                    val marker  = Marker()
                    marker.captionText = it.title
                    marker.position =gps
                    marker.map = this@MainActivity.map
                }
            }
        }

    }
    private fun getNowLocation(){
        locationSource = FusedLocationSource(this,PERMISSION_REQUEST_CODE)
    }

    private fun setNaverMap(){
        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as MapFragment?
            ?: MapFragment.newInstance().also {
                supportFragmentManager.beginTransaction().add(R.id.mapFragment, it).commit()
            }
        mapFragment.getMapAsync(this@MainActivity)

    }

    private fun getPermission() {
        if(ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.READ_EXTERNAL_STORAGE) !=
            PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.ACCESS_MEDIA_LOCATION) !=
            PackageManager.PERMISSION_GRANTED){
            val arr = arrayOf<String>(Manifest.permission.ACCESS_MEDIA_LOCATION, Manifest.permission.READ_EXTERNAL_STORAGE)
            ActivityCompat.requestPermissions(this@MainActivity, arr, requestCode)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (locationSource.onRequestPermissionsResult(requestCode, permissions, grantResults)) {
            if (!locationSource.isActivated) { // 권한 거부됨
                map.locationTrackingMode = LocationTrackingMode.None
            }
            return
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onMapReady(map: NaverMap) {
        this.map = map
        // 줌 범위 설정
        map.maxZoom = 18.0
        map.minZoom = 6.0
        //위치 추적
        map.uiSettings.isLocationButtonEnabled = true
        map.locationSource = locationSource

    }

    private suspend fun getPhoto()=withContext(Dispatchers.IO){

        binding.setVariable(BR.visibilityBool,true)

        val listUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val proj = arrayOf<String>(MediaStore.Images.Media._ID, MediaStore.Images.Media.TITLE)
        val cursor = contentResolver.query(listUri,proj,null,null,null,null)
        var count = 0
        cursor?.let {
            while (cursor.moveToNext()){
                var index = cursor.getColumnIndex(proj[0])
                val id = cursor.getString(index)
                index = cursor.getColumnIndex(proj[1])
                val title = cursor.getString(index)
                val contentUri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

                val image = Image(id,title,contentUri,getLnt(contentUri))
                mediaList.add(image)
                count ++
            }
        }
    }

    private fun getLnt(uri : Uri) : LatLng? {
        var lat : Double = 0.0
        var lng : Double = 0.0
        var latLng : LatLng? = null
        val inputStream = contentResolver.openInputStream(uri)
        inputStream?.let {
            var exif  = ExifInterface(inputStream)
            val latString = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE)
            val lngString = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE)
            val gps1 = latString?.split(",")
            val gps2 = lngString?.split(",")

            gps1?.let {
                val one = it[0].split("/")
                val two = it[1].split("/")
                val three = it[1].split("/")
                lat = (one[0].toDouble()/one[1].toDouble()+(two[0].toDouble()/two[1].toDouble())/60+(three[0].toDouble()/three[1].toDouble())/3600)
                Log.d("tts1","${lat} ")
            }
            gps2?.let {
                val one = it[0].split("/")
                val two = it[1].split("/")
                val three = it[1].split("/")
                lng = (one[0].toDouble()/one[1].toDouble()+(two[0].toDouble()/two[1].toDouble())/60+(three[0].toDouble()/three[1].toDouble())/3600)
                Log.d("tts2","${lng} ")
            }
            latLng = LatLng(lat,lng)
            inputStream.close()
        }
        return latLng
    }

    companion object{
        const val PERMISSION_REQUEST_CODE = 101
    }





}