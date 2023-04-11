import android.R
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity


// server
class MainActivity : AppCompatActivity() {
    var serverIp: TextView? = null
    var clientMsg: TextView? = null
    private val SERVER_PORT = 18888
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        serverIp = findViewById<View>(R.id.serverIp) as TextView
        clientMsg = findViewById<View>(R.id.clientMsg) as TextView
        serverIp!!.text = ipAddress // ip 가져오기
        val serverThread: Thread = Thread(serverThread())
        serverThread.start()
    }

    internal inner class serverThread : Thread() {
        override fun run() {
            try {
                // server socket 생성
                val serverSocket = DatagramSocket(SERVER_PORT)
                Log.d("VR", "Sever Socket Created")

                // client메세지를 받을 배열 선언
                val receiveMsg = ByteArray(256)
                // server datagram 생성
                val serverPacket = DatagramPacket(receiveMsg, receiveMsg.size)

                // 2.
                serverSocket.receive(serverPacket)
                val receive_data: String =
                    String(serverPacket.getData(), 0, serverPacket.getLength())
                runOnUiThread {
                    clientMsg!!.text = "클라이언트로 부터 받은 메세지 : $receive_data"
                }
                val EchoMsg: String =
                    ipAddress + " sends UDP echo message " + "'" + receive_data + "'"
                val port: Int = serverPacket.getPort()
                val send_packet = DatagramPacket(
                    EchoMsg.toByteArray(),
                    EchoMsg.length,
                    serverPacket.getAddress(),
                    port
                )
                serverSocket.send(send_packet)
                Log.d("VR", "Packet Send")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // TODO Auto-generated catch block
    private val ipAddress: String
        private get() {
            var ip = ""
            try {
                val enumNetworkInterfaces: Enumeration<NetworkInterface> =
                    NetworkInterface.getNetworkInterfaces()
                while (enumNetworkInterfaces.hasMoreElements()) {
                    val networkInterface: NetworkInterface = enumNetworkInterfaces.nextElement()
                    val enumInetAddress: Enumeration<InetAddress> =
                        networkInterface.getInetAddresses()
                    while (enumInetAddress.hasMoreElements()) {
                        val inetAddress: InetAddress = enumInetAddress.nextElement()
                        if (inetAddress.isSiteLocalAddress()) {
                            ip += """
                                 ${"IP address: " + inetAddress.getHostAddress()}
                                 
                                 """.trimIndent()
                        }
                    }
                }
            } catch (e: SocketException) {
                // TODO Auto-generated catch block
                e.printStackTrace()
                ip += """
                     ${"Something Wrong! " + e.toString()}
                     
                     """.trimIndent()
            }
            return ip
        }
}