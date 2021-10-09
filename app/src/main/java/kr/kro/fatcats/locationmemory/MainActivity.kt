package kr.kro.fatcats.locationmemory

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kr.kro.fatcats.locationmemory.databinding.ActivityMainBinding
import kotlin.coroutines.CoroutineContext


class MainActivity : AppCompatActivity() , OnMapReadyCallback ,CoroutineScope{

    private lateinit var binding: ActivityMainBinding
    private lateinit var map: NaverMap
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
        setNaverMap()
    }

    private fun setNaverMap()  {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as MapFragment?
            ?: MapFragment.newInstance().also {
                supportFragmentManager.beginTransaction().add(R.id.mapFragment, it).commit()
            }
        mapFragment.getMapAsync(this)

    }

    override fun onMapReady(map: NaverMap) {
        this.map = map
        // 줌 범위 설정
        map.maxZoom = 18.0
        map.minZoom = 10.0

        // 지도 위치 이동
        val cameraUpdate = CameraUpdate.scrollTo(LatLng(37.497801, 127.027591))
        map.moveCamera(cameraUpdate)

//        // 현위치 버튼 기능
//        val uiSetting = map.uiSettings
//        uiSetting.isLocationButtonEnabled = false // 뷰 페이져에 가려져 이후 레이아웃에 정의 하였음.
//
//        currentLocationButton.map = naverMap // 이후 정의한 현위치 버튼에 네이버맵 연결
//
//        // -> onRequestPermissionsResult // 위치 권한 요청
//        locationSource =
//            FusedLocationSource(this@MainActivity, LOCATION_PERMISSION_REQUEST_CODE)
//        naverMap.locationSource = locationSource
//
        // 지도 다 로드 이후에 가져오기
//        getHouseListFromAPI()

    }



}