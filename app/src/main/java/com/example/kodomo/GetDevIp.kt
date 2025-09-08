// File: app/src/main/java/com/example/myipapp/GetDevIp.kt
package com.example.data

import java.net.InetAddress
import java.net.NetworkInterface
import java.net.SocketException
import java.util.Enumeration

class GetDevIp {
    companion object {
        fun getDeviceIpAddress(): String? {
            return try {
                val networkInterfaces: Enumeration<NetworkInterface> = NetworkInterface.getNetworkInterfaces()

                for (networkInterface in networkInterfaces) {
                    if (!networkInterface.isLoopback && networkInterface.isUp) {
                        val inetAddresses: Enumeration<InetAddress> = networkInterface.inetAddresses
                        for (inetAddress in inetAddresses) {
                            if (!inetAddress.isLoopbackAddress && inetAddress.address.size == 4) {
                                return inetAddress.hostAddress
                            }
                        }
                    }
                }

                null // No suitable IP found
            } catch (e: SocketException) {
                e.printStackTrace()
                null
            }
        }
    }
}
