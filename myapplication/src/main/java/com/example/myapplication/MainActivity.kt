package com.example.myapplication

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMapSdk
import com.naver.maps.map.overlay.Marker
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.*
import java.nio.charset.Charset

class MainActivity : AppCompatActivity() {

    private lateinit var addressInput: EditText
    private lateinit var dataInput: EditText
    private var address: String? = null
    private var tmp: String? = null
    private var locationPermissionGranted = false
    private lateinit var udpSocket: DatagramSocket

    private lateinit var getIP: String
    private lateinit var getData: String

    private lateinit var mapView: MapView

    private val PERMISSIONS_REQUEST_LOCATION = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NaverMapSdk.getInstance(this).setClient(
            NaverMapSdk.NaverCloudPlatformClient("wdv1jpedef")
        )
        setContentView(R.layout.activity_main)

        // 메인 서버 : ip : 15.165.129.230 / port : 8080
        connectToServer("15.165.129.230" ,8080)

        Log.d("LSH","Connect to server")


        udpSocket = DatagramSocket()
        //처음 연결요청 할 send 값 안 적어도 될거같아요 어차피 메인서버 아닌가요??
        addressInput = findViewById(R.id.addressInput)
        dataInput = findViewById(R.id.dataInput)

        val button = findViewById<View>(R.id.button) as Button
        // 내 IP 192.168.240.1
        // 서버 IP 15.165.22.113
        button.setOnClickListener {
            // 예외처리는 모두 생략해버렸습니다.
            Log.d("LSH", "button Click")
            Log.d("LSH", "send to ${addressInput.text.toString()} ${dataInput.text.toString()}")

            //GPS가 작동하지 않는 경우는, 권한이 없거나, GPS 모듈이 꺼져있는 경우
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // 권한이 없는 경우 권한 요청 다이얼로그를 표시합니다.
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    PERMISSIONS_REQUEST_LOCATION
                )
            }

            val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                // GPS 모듈이 꺼져 있는 경우
                // 사용자에게 GPS 모듈을 켜도록 요청하는 코드 추가
                // GPS 모듈을 켜도록 사용자에게 요청
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }

            val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            Log.d("LSH", "Try to get location")

            //val location = getCurrentLocation()
            if (location != null) {
                    Log.d("LSH", "Get location {${location.latitude} ${location.longitude}")
                    val msg = "Latitude : {${location.latitude}} Longitude : {${location.longitude}}"
                    Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_LONG).show()}
                else {
                    Log.d("LSH", "lost it")
                }


            // GPS 정보 누락시 GPS 정보가 logcat에 뜨지 않고 lost it 반환
                //val thread: UdpSocketThread =
                //    UdpSocketThread(addressInput.text.toString(), dataInput.text.toString())

            //서버로 받은 값은 getIP, getPort로 UDP 보내기
            val thread: UdpSocketThread =
                        UdpSocketThread(getIP)
                Log.d("LSH", "UDP created")
                thread.start()
                Log.d("LSH", "Did you get it?")
            }

//GPS marker

        // 레이아웃 파일에서 MapView를 가져와서 초기화합니다.
        mapView = findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)

        val marker = Marker().apply {
            position = LatLng(37.5670135, 126.9783740)
            setOnClickListener {
                //Toast.makeText(context, "마커 클릭됨", Toast.LENGTH_SHORT).show()
                true
            }
        }

// NaverMap 객체를 가져오기 위해 MapView에서 getMapAsync 메서드를 사용합니다.
        mapView.getMapAsync { naverMap ->
            marker.map = naverMap // 마커를 지도에 추가합니다.
        }


                /*
            if (locationPermissionGranted) {
                val location = getCurrentLocation()
                Log.d("LSH", "Get location")
                if (location == null) {
                    Toast.makeText(this, "Location not found", Toast.LENGTH_SHORT).show()
                } else {
                    addressInput?.let { address ->
                        val addressString = address.text.toString().trim()
                        if (addressString.isBlank()) {
                            // addressInput이 빈 값인 경우 처리
                            return@setOnClickListener
                        }
                        this.address = addressString
                    } ?: run {
                        // addressInput이 null인 경우 처리
                        return@setOnClickListener
                    }

                    dataInput?.let { data ->
                        val dataString = "LAT:${location.latitude},LNG:${location.longitude}"
                        tmp = dataString
                    } ?: run {
                        // dataInput이 null인 경우 처리
                        return@setOnClickListener
                    }

                    val thread: UdpSocketThread = UdpSocketThread(address!!, tmp!!)
                    Log.d("LSH", "UDP created")
                    thread.start()
                    Log.d("LSH", "Did you get it?")
                }
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    LOCATION_PERMISSION_REQUEST_CODE
                )
            }

             */
            }
        }
/*
        private fun getCurrentLocation(): Location? {
            // 위치 권한이 있는지 확인
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                // 위치 권한이 있으면 LocationManager를 사용하여 현재 위치를 가져옴
                val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
                val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                Log.d("LSH", "get location")
                return location
            } else {
                // 위치 권한이 없으면 null 반환
                Log.d("LSH", "getCurrentLocation didn't do it")
                return null
            }
        }*/
internal class UdpSocketThread(var ip: String) :
    Thread() {
    override fun run() {
        var data : String="test"
        Log.d("LSH", "Thread ip: {$ip}")
        try {
            val datagramSocket = DatagramSocket()
            val inetAddress = InetAddress.getByName(ip)
            val datagramPacket =
                DatagramPacket(
                    data.toByteArray(),
                    data.toByteArray().size,
                    inetAddress,
                    3000
                )
            datagramSocket.send(datagramPacket)
        } catch (e: SocketException) {
            Log.e("LSH", "SocketException")
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}

//TCP 연결
fun connectToServer(host: String, port: Int) {
    Thread {
        try {
            val socket = Socket()
            val socketAddress = InetSocketAddress(host, port)
            val SOCKET_TIMEOUT = 5000 // 5 seconds in milliseconds
            socket.connect(socketAddress, SOCKET_TIMEOUT)

            //val socket = Socket(host, port)
            Log.d("LSH","TCP connection established")

            val outputStream = socket.getOutputStream()
            outputStream.write("test\n".toByteArray())
            val byte:ByteArray = ByteArray(1001)
            val IS : InputStream = socket.getInputStream()
            //Log.d("LSH","IS = $IS")
            val bytecount = IS.read(byte)
            println("가져온 문자열 개수 : "+bytecount)
            Log.d("LSH","byte count = $bytecount")
            var temp = String(byte)
            println("copyofRange처리 이전값"+temp)
            Log.d("LSH","temp = $temp")
            var nbyte = byte.copyOfRange(0,bytecount)

            val msg = String(nbyte)

            println("Line 220번"+msg)
            Log.d("LSH","Received input: $msg")

            val (getIP, getPort) = parseJsonConfig(msg)
            println("IP: $getIP, Port: $getPort") // IP: 15.165.22.113, Port: 3000
            Log.d("LSH","IP: $getIP, Port: $getPort")
            socket.close()
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d("LSH","Connect failed")
        }
    }.start()
}

fun parseJsonConfig(jsonString: String): Pair<String, Int> {
    val jsonObject = JSONObject(jsonString)
    val ip = jsonObject.getString("IP")
    val port = jsonObject.getInt("PORT")
    return Pair(ip, port)
}









